package com.hsbc.brule.throttler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MaxEventPublishedThrottler implements Throttler {

    final AtomicInteger eventPublished = new AtomicInteger(0);
    private final long timeWindowsInMs;

    long startEventTimeStampInMs, endEventTimeStamp;

    final List<Throttled> throttledList = new ArrayList<>();

    private final int maxEventPublished;

    public MaxEventPublishedThrottler(int maxEventPublished, long timeWindowsInMs) {
        this.maxEventPublished = maxEventPublished - 1;
        this.timeWindowsInMs = timeWindowsInMs;
    }

    @Override
    public ThrottleResult shouldProceed() {
        return null;
    }

    @Override
    public void notifyWhenCanProceed(Throttled throttled) {
        this.throttledList.add(throttled);
    }

    @Override
    public void newEvent() {
        long currentTime = System.currentTimeMillis();
        if(this.endEventTimeStamp < currentTime || this.startEventTimeStampInMs == 0){
            this.startEventTimeStampInMs = currentTime;
            this.endEventTimeStamp = currentTime + this.timeWindowsInMs;
            eventPublished.set(1);
        }else{
            if(eventPublished.incrementAndGet() == maxEventPublished){
                for (Throttled throttled : throttledList) {
                    throttled.shouldKeepEvent();
                }
                do{
                    Thread.yield();
                }while(System.currentTimeMillis() < this.endEventTimeStamp);
                eventPublished.set(0);
                for (Throttled throttled : throttledList) {
                    throttled.canPushEvent();
                }
            }
        }
    }

}
