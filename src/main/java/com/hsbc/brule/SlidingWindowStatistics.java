package com.hsbc.brule;

public interface SlidingWindowStatistics<I> {
    void add(I measurement);

    // subscriber will have a callback that'll deliver a Statistics instance (push)
    void subscribeForStatistics(StatisticsSubscriber subscriber);

    // get latest statistics (poll)
    Statistics getLatestStatistics();

    public interface Statistics {
        float getMean();

        int getMode();

        float getPercentile(int value);
    }

    public interface StatisticsSubscriber {

        void onStatisticReceived(Statistics statistics);

    }
}
