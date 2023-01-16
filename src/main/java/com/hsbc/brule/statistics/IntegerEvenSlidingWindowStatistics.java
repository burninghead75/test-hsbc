package com.hsbc.brule.statistics;

import com.hsbc.brule.event.NumAndProbabilityEvent;
import com.hsbc.brule.statistics.SlidingWindowStatistics;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


public class IntegerEvenSlidingWindowStatistics implements SlidingWindowStatistics<NumAndProbabilityEvent> {

    private final static int DEFAULT_SIZE = 1000;
    final Set<StatisticsSubscriber> subscribers = new HashSet<>();

    final long timeWindowsInNano;

    final NumAndProbabilityEvent[] events = new NumAndProbabilityEvent[DEFAULT_SIZE];
    final long[] timeStamps = new long[DEFAULT_SIZE];

    private int insertIndex = -1;


    public IntegerEvenSlidingWindowStatistics(long timeWindowsInMillis) {
        timeWindowsInNano = timeWindowsInMillis * 1000;
    }

    @Override
    public void add(NumAndProbabilityEvent measurement) {
        //assuming event arrived in order
        insertIndex++;
        if (insertIndex >= events.length) insertIndex = 0;

        events[insertIndex] = measurement;
        timeStamps[insertIndex] = System.nanoTime();
    }

    @Override
    public void subscribeForStatistics(StatisticsSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public Statistics getLatestStatistics() {
        if (insertIndex == -1) return new IntegerEventStatistics(new int[]{});
        int index;
        long minTimeInNanos = this.timeStamps[insertIndex] - this.timeWindowsInNano;

        for (index = insertIndex; index > -1 && timeStamps[index] >= minTimeInNanos; --index) ;

        if (index == -1) {
            for (index = events.length - 1; index > insertIndex && timeStamps[index] >= minTimeInNanos; --index) ;
        }
        int[] values;
        if (insertIndex > index) {
            if (timeStamps[index] >= minTimeInNanos) {
                values = transformToIntValues(index, insertIndex);
            } else {
                values = transformToIntValues(index, insertIndex);
            }
        } else {
            if (timeStamps[index] >= minTimeInNanos) {
                values = transformReverseToIntValues(index, insertIndex);
            } else {
                insertIndex++;
                if (insertIndex == this.events.length) {
                    values = transformToIntValues(0, insertIndex);
                } else {
                    values = transformReverseToIntValues(index, insertIndex);
                }
            }
        }
        return new IntegerEventStatistics(values);
    }

    int[] transformToIntValues(int startIndex, int lastIndexIncluded) {
        int[] values = new int[lastIndexIncluded - startIndex + 1];
        int cpt = 0;
        for (int i = startIndex; i <= lastIndexIncluded; i++) {
            values[cpt++] = events[i].getNumber();
        }
        return values;
    }

    int[] transformReverseToIntValues(int oldestIndex, int lastIndex) {
        int[] values = new int[events.length - oldestIndex + lastIndex + 1];
        int cpt = 0;
        for (int i = oldestIndex; i < values.length; i++) {
            values[cpt++] = events[i].getNumber();
        }
        for (int i = 0; i <= lastIndex; i++) {
            values[cpt++] = events[i].getNumber();
        }
        return values;
    }

    public static class IntegerEventStatistics implements Statistics {

        final int[] values;

        final float mean;

        final int mode;


        public IntegerEventStatistics(int[] values) {

            this.values = values;

            mean = computeMean(values);

            mode = computeMode(values);

        }

        private float computeMean(int[] values) {
            float sum = Arrays.stream(values).sum();
            return sum / values.length;
        }

        private int computeMode(int[] values) {
            return Arrays.stream(values)
                    .boxed()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                    .entrySet()
                    .stream()
                    .max(Comparator.comparing(Map.Entry::getValue))
                    .get().getKey();
        }


        @Override
        public float getMean() {
            return mean;
        }

        @Override
        public int getMode() {
            return mode;
        }

        @Override
        public float getPercentile(int value) {
            long di = Arrays.stream(values).filter(i -> i < value).count();
            long de = Arrays.stream(values).filter(i -> i == value).count();
            float percentile = (di + 0.5f * de) * 100 / values.length;
            return percentile;
        }
    }
}
