package com.hsbc.brule.throttler;

public interface Throttler {
    // check if we can proceed (poll)
    ThrottleResult shouldProceed();
    // subscribe to be told when we can proceed (Push)

    void notifyWhenCanProceed(Throttled throttled);

    void newEvent();
    enum ThrottleResult {
        PROCEED, // publish, aggregate etc
        DO_NOT_PROCEED //
    }

    interface Throttled {
        void canPushEvent();

        void shouldKeepEvent();
    }
}
