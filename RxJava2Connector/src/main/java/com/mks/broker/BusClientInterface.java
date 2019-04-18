package com.mks.broker;

import com.mks.broker.utils.Deal;
import com.mks.broker.utils.Event;
import com.mks.broker.utils.Log;
import org.reactivestreams.Subscriber;

public interface BusClientInterface {

    public void publish(BusClientInterface.Bus bus, Event payload);
    public void subscribe(Bus bus, Subscriber<? extends Event> s);

    public enum Bus{
        DEALS,
        SYSTEM_EVENTS
    }
}
