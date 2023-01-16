package com.hsbc.brule.bus;

import com.hsbc.brule.event.Event;
import com.hsbc.brule.event.EventFilter;
import com.hsbc.brule.subscriber.Subscriber;

public interface EventBus<E extends Event> {

    // Feel free to replace Object with something more specific,
    // but be prepared to justify it

    // How would you denote the subscriber?
    // EventBus will return a unique identifier ( here a UUID ).
    void addSubscriber(Subscriber<E> subscriber);

    // Would you allow clients to filter the events they receive? How would the interface look like?
    void addSubscriberForFilteredEvents(Subscriber<E> subscriber, EventFilter<E>...filters);

    void start();

    void shutdown();

}
