package com.hsbc.brule.event;

import com.hsbc.brule.generator.ProbabilisticRandomGen;

public class NumAndProbabilityEvent implements Event{

    private int number;
    private float probabilityOfSample;

    public static NumAndProbabilityEvent of(ProbabilisticRandomGen.NumAndProbability sample) {
        NumAndProbabilityEvent event = new NumAndProbabilityEvent();
        event.setNumber(sample.getNumber());
        event.setProbabilityOfSample(sample.getProbabilityOfSample());
        return event;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public float getProbabilityOfSample() {
        return probabilityOfSample;
    }

    public void setProbabilityOfSample(float probabilityOfSample) {
        this.probabilityOfSample = probabilityOfSample;
    }
}
