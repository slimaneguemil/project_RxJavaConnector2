package com.mks;

import com.mks.broker.BusClientInterface;
import com.mks.broker.ServiceRxJava2;
import com.mks.broker.utils.Deal;
import com.mks.broker.utils.Log;
import com.mks.broker.utils.utils;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootApplication
public class RunMe {

        static AtomicLong count= new AtomicLong(0);

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext ctx = SpringApplication.run(RunMe.class,args);
        RunDeals(ctx);
    }

    static public void RunDeals(ApplicationContext ctx){

        BusClientInterface bus =  ctx.getBean(ServiceRxJava2.class);
        //subscribing
        bus.subscribe(BusClientInterface.Bus.DEALS, getSubscriber(20));

         //publishing
        bus.publish(BusClientInterface.Bus.DEALS,generateDeals());
        bus.publish(BusClientInterface.Bus.DEALS,generateDeals());
        bus.publish(BusClientInterface.Bus.DEALS,generateDeals());
    }

    public static Subscriber<Deal> getSubscriber(final int limit) {
        return new Subscriber<Deal>() {
            AtomicLong count = new AtomicLong(0);
            Subscription s;


            public void onSubscribe(Subscription subscription) {
                this.s = subscription;
                subscription.request(1);
            }


            public void onNext(Deal Deal) {
                utils.log("@NewSubscriber 1 received = "+Deal) ;
                Deal.setEnd(System.currentTimeMillis());
                long timing = Deal.getEnd() - Deal.getStart();
                utils.log(Deal.getEnd() + "-" + Deal.getStart() + "->" + timing);
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(700 ));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                 if (count.incrementAndGet() == limit)
                    s.cancel();
                 else
                s.request(1);
            }

            public void onError(Throwable throwable) {
                System.out.println("On throwable = " + throwable);
            }

            public void onComplete() {
                System.out.println("on complete = " + count);
            }
        };
    }



    public static Deal generateDeals(){
        return utils.mockDeal(
                count.incrementAndGet(),"from @TesConnector",
                System.currentTimeMillis(),
                "1.0.2");
    }

}
