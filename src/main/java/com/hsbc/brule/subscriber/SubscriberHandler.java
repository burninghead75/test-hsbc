package com.hsbc.brule.subscriber;

import com.hsbc.brule.bus.CircularBuffer;
import com.hsbc.brule.publisher.PublisherOrchestrator;
import com.hsbc.brule.event.Event;
import com.hsbc.brule.event.EventFilter;
import com.hsbc.brule.event.NumAndProbabilityEvent;

import java.util.Objects;

public class SubscriberHandler<E extends Event> implements Runnable {
    private final Subscriber<E> subscriber;
    private final CircularBuffer<E> buffer;
    private volatile long lastReadValue = -1;

    private final SubscriberOrchestrator subscriberOrchestrator;

    private final PublisherOrchestrator<E> publisherOrchestrator;

    private final EventFilter<E>[] filters;

    SubscriberHandler(SubscriberOrchestrator subscriberOrchestrator, Subscriber<E> subscriber, CircularBuffer<E> buffer, PublisherOrchestrator<E> publisherOrchestrator, EventFilter<E>...filters) {
        this.subscriberOrchestrator = subscriberOrchestrator;
        this.subscriber = subscriber;
        this.filters = Objects.requireNonNullElseGet(filters, () -> new EventFilter[]{});
        this.buffer = buffer;
        this.publisherOrchestrator = publisherOrchestrator;
    }


    public long getLastReadValue() {
        return lastReadValue;
    }

    @Override
    public void run() {

        System.out.println("Started");

        while (true) {
            long lastPublishedEventIndex;
            while ((lastPublishedEventIndex = publisherOrchestrator.getCurrentIndex()) <= lastReadValue) {
                Thread.yield();
            }
            long nextValue = this.lastReadValue +1;
            E event = this.buffer.get(nextValue);
            NumAndProbabilityEvent e = (NumAndProbabilityEvent)event;
            if(applyFilter(event)){
                this.subscriber.onEventReceived(event);
            }
            this.lastReadValue = nextValue;
            this.subscriberOrchestrator.eventReceivedDone();
        }
    }

    private boolean applyFilter(E event) {
        for (EventFilter<E> filter : filters) {
            if(!filter.match(event))return false;
        }
        return true;
    }
}
