package com.hsbc.brule;

import java.util.*;
import java.util.stream.IntStream;

public class ProbabilisticRandomGenImpl implements ProbabilisticRandomGen {

    public static final int DEFAULT_NUMBER_OF_SAMPLE = 100;
    private final Queue<NumAndProbability> numAndProbabilitiesQueue = new LinkedList<>();

    private ProbabilisticRandomGenImpl(Collection<NumAndProbability> numAndProbabilities){
        numAndProbabilitiesQueue.addAll(numAndProbabilities);
    }

    @Override
    public NumAndProbability nextFromSample() {
        return numAndProbabilitiesQueue.poll();
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

    public static ProbabilisticRandomGen create(Collection<NumAndProbability> numAndProbabilities){
        return new ProbabilisticRandomGenImpl(numAndProbabilities);
    }
}
