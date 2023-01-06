package com.hsbc.brule;

import java.util.*;

public class SingleThreadEventBusImpl implements EventBus {



    private final Map<UUID, SubscriberWithFilters> subscribers = new HashMap<>();

    @Override
    public void publishEvent(Event event) {

        this.subscribers.values().stream()
                .filter(subscriber -> isEventMatch.test(subscriber, event))
                .map(subscriberWithFilters -> subscriberWithFilters.subscriber())
                .forEach(subscriber -> subscriber.onEventReceived(event));
    }

    @Override
    public UUID addSubscriber(Subscriber subscriber) {
        return addSubscriberForFilteredEvents(subscriber);
    }

    @Override
    public UUID addSubscriberForFilteredEvents(Subscriber subscriber, EventFilter... filters) {
        final UUID subscriberUuid = UUID.randomUUID();
        subscribers.put(subscriberUuid, new SubscriberWithFilters(subscriber, List.of(filters)));
        return subscriberUuid;
    }

    @Override
    public void shutdown() {
        //DO NOTHING
    }
}


