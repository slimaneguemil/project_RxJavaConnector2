package com.mks.broker;

import com.mks.broker.utils.Deal;
import com.mks.broker.utils.Event;
import com.mks.broker.utils.utils;
import io.reactivex.BackpressureOverflowStrategy;
import io.reactivex.Flowable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.schedulers.Schedulers;
import com.mks.broker.utils.Log;
import org.reactivestreams.Subscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.binder.PollableMessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicLong;


@Service
@Lazy
public class ServiceRxJava2 implements BusClientInterface {

    private final Channels.PolledProcessor polledProcessor;
    private final PollableMessageSource channelPollDeals, channelPollLogs;
    private final MessageChannel channelOutputDeals, channelOutputLogs;
    private Flowable<Deal> streamDeals;
    private  Flowable<Log> streamLogs;
    @Value("${mks.broker.stream.buffer:10000}")
    Long mks_buffer;
    @Value("${mks.broker.poll.sleep:10}")
    Long mks_poll_sleep;

    @PostConstruct
    public void init(){
        System.out.println(" post construct stream.buffer: "+ this.mks_buffer );
        System.out.println(" post construct poll.sleep: "+ this.mks_poll_sleep);

        streamDeals = generateStreamDeals()
                .subscribeOn(Schedulers.computation())
                .onBackpressureBuffer(mks_buffer,
                        ()-> utils.log("Buffer Overflow ********************** " )
                        , BackpressureOverflowStrategy.ERROR)
                .doOnCancel(() -> System.out.println(" doOncancel is done ! " ))
                .observeOn(Schedulers.io())
                .publish()
                .refCount(1);

        streamLogs = generateStreamLogs()
                .subscribeOn(Schedulers.computation())
                .onBackpressureBuffer(mks_buffer,
                        ()-> utils.log("Buffer Overflow ********************** " )
                        , BackpressureOverflowStrategy.ERROR)
                .doOnNext(s -> System.out.println("doonNext log = " + s))
                .doOnCancel(() -> System.out.println(" doOncancel is done ! " ))
               .observeOn(Schedulers.io())
                .publish()
                .refCount(1);
    }

    ServiceRxJava2(Channels.PolledProcessor polledProcessor) {
        this.polledProcessor = polledProcessor;
        this.channelPollDeals = polledProcessor.input();
        this.channelPollLogs = polledProcessor.input2();
        this.channelOutputDeals = polledProcessor.output();
        this.channelOutputLogs = polledProcessor.output2();


    }

    Flowable<Deal> generateStreamDeals()  {
        return Flowable.generate(() -> new
                        AtomicLong(0),
                (state, emitter) -> {
                    Long current = state.incrementAndGet();
                    boolean result = false;
                    result = this.channelPollDeals.poll(m -> {
                        Deal payload = (Deal) m.getPayload();
                        utils.log("pollling Deal: "+ m) ;

                        emitter.onNext(payload);
                        Log l = new Log();
                        l.setContent(payload.toString());
                        this.channelOutputLogs.send(MessageBuilder.withPayload(l).build());
                    }, new ParameterizedTypeReference<Deal>() {
                    });
                    try {
                        Thread.sleep(mks_poll_sleep);
                    } catch (InterruptedException e) {
                       // e.printStackTrace();
                    }
                    if(result) {
                        Log l = new Log();
                       l.setContent("flowservice: SUCCESS");

                        this.channelOutputLogs.send(MessageBuilder.withPayload(l).build());
                    }
                }
        );
    }

    Flowable<Log> generateStreamLogs()  {
        return Flowable.generate(() -> new
                        AtomicLong(0),
                (state, emitter) -> {
                    utils.log("pollling Log ..... ") ;
                    Long current = state.incrementAndGet();
                    boolean result = false;
                    result = this.channelPollLogs.poll(m -> {
                        Log payload = (Log) m.getPayload();
                        utils.log("pollling Log: "+ payload) ;
                        emitter.onNext(payload);
                    }, new ParameterizedTypeReference<Log>() {
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // e.printStackTrace();
                    }
                    if(result) {

                       // this.channelOutputLogs.send(MessageBuilder.withPayload(l).build());
                    }
                }
        );
    }

    public void publish(BusClientInterface.Bus bus, Event payload){
        //this.direct.send(MessageBuilder.withPayload(payload).build());
        switch (bus){
            case DEALS:
                this.channelOutputDeals.send(MessageBuilder.withPayload(payload).build());
                break;
            case SYSTEM_EVENTS:
                this.channelOutputLogs.send(MessageBuilder.withPayload(payload).build());
                break;
        }

    }

    public void subscribe(BusClientInterface.Bus bus, Subscriber<? extends Event> s) {
        switch (bus){
            case DEALS:
                this.streamDeals.subscribe((Subscriber<? super Deal>) s);
                break;
            case SYSTEM_EVENTS:
                this.streamLogs.subscribe((Subscriber<? super com.mks.broker.utils.Log>) s);
                break;
            default:
        }
    }

}
