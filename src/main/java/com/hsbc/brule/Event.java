package com.hsbc.brule;

import com.hsbc.brule.ProbabilisticRandomGen.NumAndProbability;

import java.time.Instant;

public class Event {

    protected final NumAndProbability numAndProbability;

    protected final long timeStampInNano = System.nanoTime();

    protected Event(NumAndProbability numAndProbability) {
        this.numAndProbability = numAndProbability;
    }


    public NumAndProbability getEventBody() {
        return this.numAndProbability;
    }

    public static Event of(NumAndProbability numAndProbability) {
        return new Event(numAndProbability);
    }

    public boolean isCoalescing(){
        return false;
    }

    @Override
    public String toString() {
        return "Event{" +
                "numAndProbability=" + numAndProbability +
                '}';
    }

    public long getTimeStampInNano(){
        return timeStampInNano;
    }

}


