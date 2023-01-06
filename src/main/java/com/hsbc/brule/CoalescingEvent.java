package com.hsbc.brule;

public class CoalescingEvent extends Event{


    protected CoalescingEvent(ProbabilisticRandomGen.NumAndProbability numAndProbability) {
        super(numAndProbability);
    }

    public static Event of(ProbabilisticRandomGen.NumAndProbability numAndProbability) {
        return new CoalescingEvent(numAndProbability);
    }

    public boolean isCoalescing(){
        return true;
    }
}
