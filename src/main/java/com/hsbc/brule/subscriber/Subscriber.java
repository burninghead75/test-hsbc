package com.hsbc.brule.subscriber;

import com.hsbc.brule.event.Event;
import com.hsbc.brule.throttler.Throttler;
import com.hsbc.brule.throttler.Throttler.Throttled;

public interface Subscriber<E extends Event> {

    void onEventReceived(E event);

    void setThrottler(Throttler throttler);

    void notifyWhenCanProceed(Throttled throttled);
}
