package com.hsbc.brule.bus;

import com.hsbc.brule.bus.EventBus;
import com.hsbc.brule.event.Event;
import com.hsbc.brule.event.EventFilter;
import com.hsbc.brule.subscriber.Subscriber;

import java.util.*;

public class SingleThreadEventBusImpl<E extends Event> implements EventBus<E> {

    private final List<Subscriber<E>> subscribers = new ArrayList<>();
    private final Map<Subscriber<E>, EventFilter<E>[]> subscribersWithFilters = new HashMap<>();

    public void publish(E event) {

        for (Subscriber<E> subscriber : subscribers) {
            subscriber.onEventReceived(event);
        }

        for (Map.Entry<Subscriber<E>, EventFilter<E>[]> entry : subscribersWithFilters.entrySet()) {
            boolean publish = true;
            for (EventFilter<E> eventFilter : entry.getValue()) {
                if(!eventFilter.match(event)){
                    publish = false;
                    break;
                }
            }

            if(publish){
                entry.getKey().onEventReceived(event);
            }
        }
    }

    @Override
    public void addSubscriber(Subscriber<E> subscriber) {
         addSubscriberForFilteredEvents(subscriber);
    }

    @Override
    public void addSubscriberForFilteredEvents(Subscriber<E> subscriber, EventFilter<E>... filters) {
        this.subscribersWithFilters.put(subscriber, filters);

    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {
        //DO NOTHING
    }
}


