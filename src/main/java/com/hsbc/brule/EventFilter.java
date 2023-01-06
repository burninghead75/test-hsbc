package com.hsbc.brule;


public interface EventFilter {
    boolean match(Event event);

    final class GreaterThan implements EventFilter {

        final float threshold;

        public GreaterThan(float threshold) {
            this.threshold = threshold;
        }

        @Override
        public boolean match(Event event) {
            return event.getEventBody().getProbabilityOfSample() >= threshold;
        }
    }
}
