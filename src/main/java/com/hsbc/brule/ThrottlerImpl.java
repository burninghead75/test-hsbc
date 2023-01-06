package com.hsbc.brule;

public class ThrottlerImpl implements Throttler {

    private Throttled throttled;

    public ThrottleResult shouldProceed(){
        throttled.canPushEvent();
        return ThrottleResult.PROCEED;
    }

    public ThrottleResult shouldNotProceed(){
        throttled.shouldKeepEvent();
        return ThrottleResult.DO_NOT_PROCEED;
    }

    @Override
    public void notifyWhenCanProceed(Throttled throttled) {
        this.throttled = throttled;
    }

}
