package com.hsbc.brule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.hsbc.brule.Throttler.*;

public class ThreadEventBusImpl implements EventBus {

    static void awaitTerminationAfterShutdown(ExecutorService threadPool) {
        logger.info("{} threadpool shutdown ", threadPool);
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    static final Event POISON_PILL = Event.of(new ProbabilisticRandomGen.NumAndProbability(Integer.MIN_VALUE, Float.MIN_VALUE));

    private static final Logger logger = LoggerFactory.getLogger(ThreadEventBusImpl.class);

    private static final int EVENT_QUEUE_SIZE = 100;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final BlockingQueue<Event> eventQueue = new ArrayBlockingQueue<>(EVENT_QUEUE_SIZE);

    private final Map<Integer, Event> coalescingValueMap = new ConcurrentHashMap<>();

    private final EventDispatcher eventDispatcher = new EventDispatcher(eventQueue, coalescingValueMap);

    public ThreadEventBusImpl() {
        executorService.submit(eventDispatcher);
    }

    @Override
    public void publishEvent(Event event) {
        try {
            if (event.isCoalescing()) {
                this.coalescingValueMap.put(event.getEventBody().getNumber(), event);
            }
            eventQueue.put(event);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public UUID addSubscriber(Subscriber subscriber) {
        return addSubscriberForFilteredEvents(subscriber);
    }

    @Override
    public UUID addSubscriberForFilteredEvents(Subscriber subscriber, EventFilter... filters) {
        final UUID subscriberUuid = UUID.randomUUID();
        eventDispatcher.addSubscriber(new SubscriberWithFilters(subscriber, List.of(filters)));
        return subscriberUuid;
    }

    @Override
    public void shutdown() {
        this.publishEvent(POISON_PILL);
        awaitTerminationAfterShutdown(this.executorService);
    }

    private static class EventDispatcher implements Runnable {

        private static final Logger logger = LoggerFactory.getLogger(EventDispatcher.class);

        private static final int SUBSCRIBER_QUEUE_SIZE = 100;
        public static final int INTERNAL_EXECUTOR_NB_THREADS = 10;
        private final BlockingQueue<Event> eventsQueue;

        private final Map<Integer, Event> coalescingValueMap;

        final ExecutorService executorService = Executors.newFixedThreadPool(INTERNAL_EXECUTOR_NB_THREADS);

        private final Map<SubscriberWithFilters, BlockingQueue<Event>> subscribers = new ConcurrentHashMap<>();

        private EventDispatcher(BlockingQueue<Event> queue, Map<Integer, Event> coalescingValueMap) {
            this.eventsQueue = queue;
            this.coalescingValueMap = coalescingValueMap;
        }

        public void addSubscriber(SubscriberWithFilters subscriber) {
            final ArrayBlockingQueue<Event> subscriberEventQueue = new ArrayBlockingQueue<>(SUBSCRIBER_QUEUE_SIZE);
            this.subscribers.put(subscriber, subscriberEventQueue);
            this.executorService.submit(new InternalSubscriber(subscriber.subscriber(), subscriberEventQueue, coalescingValueMap));
        }

        @Override
        public void run() {

            try {
                while (true) {
                    final Event event = this.eventsQueue.take();
                    for (Entry<SubscriberWithFilters, BlockingQueue<Event>> entry : this.subscribers.entrySet()) {
                        if (event == POISON_PILL || isEventMatch.test(entry.getKey(), event)) {
                            final BlockingQueue<Event> subscriberQueue = entry.getValue();
                            try {
                                subscriberQueue.put(event);
                            } catch (InterruptedException e) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    }
                    if (event == POISON_PILL) {
                        logger.info("POISON PILL Received");
                        awaitTerminationAfterShutdown(this.executorService);
                        return;
                    }
                }

            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }

        }


    }

    static class InternalSubscriber implements Runnable, Throttled {

        private static final Logger logger = LoggerFactory.getLogger(InternalSubscriber.class);
        private final Subscriber subscriber;

        private final BlockingQueue<Event> queue;

        private final Map<Integer, Event> coalescingValueMap;

        private final Map<Integer, Long> latestValues = new ConcurrentHashMap<>();

        private final Lock throttlingLock = new ReentrantLock();

        InternalSubscriber(Subscriber subscriber, BlockingQueue<Event> queue, Map<Integer, Event> coalescingValueMap) {
            this.subscriber = subscriber;
            this.queue = queue;
            this.coalescingValueMap = coalescingValueMap;
            subscriber.notifyWhenCanProceed(this);
        }

        @Override
        public void run() {

            while (true) {
                try {
                    final Event event = getLatestValueEvent();
                    if (event != null) {
                        if (event == POISON_PILL) {
                            logger.info("POISON_PILL - Stop running subscriber");
                            return;
                        }
                        throttlingLock.lock();
                        subscriber.onEventReceived(event);
                        throttlingLock.unlock();
                    }
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        private Event getLatestValueEvent() throws InterruptedException {
            final Event event = this.queue.take();
            if (event.isCoalescing()) {
                final Event latestEvent = this.coalescingValueMap.get(event.getEventBody().getNumber());
                final Long latestTimeStamp = this.latestValues.get(event.getEventBody().getNumber());
                if (latestTimeStamp == null || latestTimeStamp < latestEvent.timeStampInNano) {
                    this.latestValues.put(latestEvent.getEventBody().getNumber(), latestEvent.timeStampInNano);
                    return latestEvent;
                } else {
                    return null;
                }
            }
            return event;
        }

        @Override
        public void canPushEvent() {
            throttlingLock.unlock();
        }

        @Override
        public void shouldKeepEvent() {
            throttlingLock.lock();
        }
    }
}
