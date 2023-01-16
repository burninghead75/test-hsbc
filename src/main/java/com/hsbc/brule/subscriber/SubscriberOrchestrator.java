package com.hsbc.brule.subscriber;

import com.hsbc.brule.bus.CircularBuffer;
import com.hsbc.brule.publisher.PublisherOrchestrator;
import com.hsbc.brule.event.Event;
import com.hsbc.brule.event.EventFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SubscriberOrchestrator<E extends Event> {

    private volatile long currentIndex = -1;

    private final List<SubscriberHandler<E>> subscribers = new ArrayList<>();

    private final CircularBuffer<E> buffer;

    private final ExecutorService executorService = Executors.newFixedThreadPool(8);

    private final PublisherOrchestrator<E> publisherOrchestrator;

    public SubscriberOrchestrator(CircularBuffer<E> buffer, PublisherOrchestrator<E> publisherOrchestrator) {
        this.buffer = buffer;
        this.publisherOrchestrator = publisherOrchestrator;
    }


    public void addSubscriber(Subscriber<E> subscriber) {
        this.subscribers.add(new SubscriberHandler<E>(this, subscriber, buffer, this.publisherOrchestrator));
    }

    public void addSubscriberForFilteredEvents(Subscriber<E> subscriber, EventFilter<E>...filters) {
        this.subscribers.add(new SubscriberHandler<E>(this, subscriber, buffer, this.publisherOrchestrator, filters));

    }

    public long getNextIndex() {
        return currentIndex;
    }

    public long getCurrentIndex() {
        return currentIndex;
    }

    public void start() {
        for (SubscriberHandler<E> subscriber : this.subscribers) {
            final Future<?> submit = this.executorService.submit(subscriber);
        }
    }

    public void shutdown(){
        this.executorService.shutdownNow();
    }

    public void eventReceivedDone() {
        long minIndex = Long.MAX_VALUE;
        for (SubscriberHandler<E> subscriber : this.subscribers) {
            minIndex = Math.min(subscriber.getLastReadValue(), minIndex);
        }
        this.buffer.lastValue(minIndex);
    }
}
