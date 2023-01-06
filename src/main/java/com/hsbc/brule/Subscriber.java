package com.hsbc.brule;

import com.hsbc.brule.Throttler.Throttled;

import java.util.UUID;

public interface Subscriber {

    void onEventReceived(Event event);

    UUID subscribe(EventBus eventBus, EventFilter...matchers);

    UUID getBusSuscriptionUUID();

    void setThrottler(Throttler throttler);

    void notifyWhenCanProceed(Throttled throttled);
}
