package com.hsbc.brule;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IntegerEvenSlidingWindowStatistics implements SlidingWindowStatistics<Event> {

    final Set<StatisticsSubscriber> subscribers = new HashSet<>();

    final long timeWindowsInNano;

    final Queue<Event> events = new LinkedList<>();

    public IntegerEvenSlidingWindowStatistics(long timeWindowsInMillis) {
        timeWindowsInNano = timeWindowsInMillis * 1000;
    }

    @Override
    public void add(Event measurement) {
        //assuming event arrived in order
        long minTime = measurement.getTimeStampInNano() - timeWindowsInNano;
        while (events.peek() != null && events.peek().getTimeStampInNano() < minTime){
            events.poll();
        }
        events.add(measurement);
    }

    @Override
    public void subscribeForStatistics(StatisticsSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public Statistics getLatestStatistics() {
        int[] values = events.stream().map(event -> event.getEventBody().getNumber()).mapToInt(Integer::intValue).toArray();
        return new IntegerEventStatistics(values);
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
            long di = Arrays.stream(values).filter( i -> i < value).count();
            long de = Arrays.stream(values).filter( i -> i == value).count();
            float percentile = (di + 0.5f * de) *100/ values.length;
            return percentile;
        }
    }
}
