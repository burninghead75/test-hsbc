package com.hsbc.brule;

public interface Throttler {
    // check if we can proceed (poll)
    ThrottleResult shouldProceed();
    // subscribe to be told when we can proceed (Push)

    ThrottleResult shouldNotProceed();
    void notifyWhenCanProceed(Throttled throttled);
    enum ThrottleResult {
        PROCEED, // publish, aggregate etc
        DO_NOT_PROCEED //
    }

    interface Throttled {
        void canPushEvent();

        void shouldKeepEvent();
    }
}
