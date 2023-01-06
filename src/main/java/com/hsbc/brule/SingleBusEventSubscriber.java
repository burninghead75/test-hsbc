package com.hsbc.brule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class SingleBusEventSubscriber implements Subscriber {

    private static final Logger logger = LoggerFactory.getLogger(SingleBusEventSubscriber.class);

    private UUID busSubscriptionUUID;

    private Throttler throttler;

    @Override
    public void onEventReceived(Event event) {
        logger.info("Subscription {} : receive event {}", busSubscriptionUUID, event);
    }

    @Override
    public UUID subscribe(EventBus eventBus, EventFilter... matchers) {
        // Should add an exception if subscriber is already connected to BusEvent
        this.busSubscriptionUUID = eventBus.addSubscriberForFilteredEvents(this, matchers);
        logger.info("Subscribe to eventBus {} with id {}", eventBus, busSubscriptionUUID);
        return this.busSubscriptionUUID;
    }

    @Override
    public UUID getBusSuscriptionUUID() {
        return busSubscriptionUUID;
    }


    @Override
    public void setThrottler(Throttler throttler) {
        this.throttler = throttler;
    }

    @Override
    public void notifyWhenCanProceed(Throttler.Throttled throttled) {
        if(this.throttler != null){
            throttler.notifyWhenCanProceed(throttled);
        }
    }


}
