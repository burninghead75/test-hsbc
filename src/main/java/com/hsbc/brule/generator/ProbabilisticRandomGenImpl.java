package com.hsbc.brule.generator;

import java.util.*;
import java.util.stream.IntStream;

public class ProbabilisticRandomGenImpl implements ProbabilisticRandomGen {

    public static final int DEFAULT_NUMBER_OF_SAMPLE = 100;

    private final NumAndProbability[] numAndProbabilities;

    private int index = -1;

    private final int maxIndex;

    private ProbabilisticRandomGenImpl(List<NumAndProbability> numAndProbabilities){
        maxIndex = numAndProbabilities.size();
        this.numAndProbabilities = numAndProbabilities.toArray(new NumAndProbability[maxIndex]);
    }

    @Override
    public NumAndProbability nextFromSample() {
        return ++index < maxIndex ? numAndProbabilities[index] : null;
    }

    public static ProbabilisticRandomGen createWithGeneratedValue() {
        return ProbabilisticRandomGenImpl.createWithGeneratedValue(DEFAULT_NUMBER_OF_SAMPLE);
    }

    public static ProbabilisticRandomGen createWithGeneratedValue(int numberOfSample){
        final Random random = new Random();
        final List<NumAndProbability> numAndProbabilityList = new ArrayList<>(numberOfSample);
        IntStream.rangeClosed(1,numberOfSample).forEach( i -> {
            numAndProbabilityList.add(new NumAndProbability(i, random.nextFloat(0,1)));
        });
        return new ProbabilisticRandomGenImpl(numAndProbabilityList);
    }

    public static ProbabilisticRandomGen create(List<NumAndProbability> numAndProbabilities){
        return new ProbabilisticRandomGenImpl(numAndProbabilities);
    }
}
