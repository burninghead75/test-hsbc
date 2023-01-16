package com.hsbc.brule.bus;

import com.hsbc.brule.event.Event;
import com.hsbc.brule.event.EventFilter;
import com.hsbc.brule.publisher.PublisherOrchestrator;
import com.hsbc.brule.subscriber.Subscriber;
import com.hsbc.brule.subscriber.SubscriberOrchestrator;
import com.hsbc.brule.throttler.Throttler;

import java.util.ArrayList;
import java.util.List;

public class CircularBufferEventBus<E extends Event> implements EventBus<E> {

    private final CircularBuffer<E> buffer;

    private final PublisherOrchestrator<E> publisherOrchestrator;

    private final SubscriberOrchestrator<E> subscriberOrchestrator;

    private final List<Throttler> throttlers = new ArrayList<>();

    public CircularBufferEventBus(EventFactory<E> eventFactory) {
        this.buffer = new CircularBuffer<>(eventFactory);
        this.publisherOrchestrator = new PublisherOrchestrator<>(this.buffer);
        this.subscriberOrchestrator = new SubscriberOrchestrator<>(this.buffer, this.publisherOrchestrator);

    }

    public void addThrottler(Throttler throttler){
        this.throttlers.add(throttler);
    }

    public void publish(long eventIndex) {
        this.publisherOrchestrator.publish(eventIndex);
        for (Throttler throttler : this.throttlers) {
            throttler.newEvent();
        }
    }

    public long getNextIndex(){
        return this.publisherOrchestrator.getNextIndex();
    }

    public E get(long index){
        return this.buffer.get(index);
    }

    @Override
    public void addSubscriber(Subscriber<E> subscriber) {
        this.subscriberOrchestrator.addSubscriber(subscriber);

    }

    @Override
    public void addSubscriberForFilteredEvents(Subscriber<E> subscriber, EventFilter<E>... filters) {
        this.subscriberOrchestrator.addSubscriberForFilteredEvents(subscriber, filters);

    }

    @Override
    public void start(){
        this.subscriberOrchestrator.start();
    }
    @Override
    public void shutdown() {
        this.subscriberOrchestrator.shutdown();

    }
}
