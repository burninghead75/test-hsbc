package com.hsbc.brule.subscriber;

import com.hsbc.brule.throttler.Throttler;
import com.hsbc.brule.event.NumAndProbabilityEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumAndProbabilitySubscriber implements Subscriber<NumAndProbabilityEvent> {

    private static final Logger logger = LoggerFactory.getLogger(NumAndProbabilitySubscriber.class);

    @Override
    public void onEventReceived(NumAndProbabilityEvent event) {
        logger.info("Received event {} {}", event.getNumber(), event.getProbabilityOfSample());
    }

    @Override
    public void setThrottler(Throttler throttler) {

    }

    @Override
    public void notifyWhenCanProceed(Throttler.Throttled throttled) {

    }
}
