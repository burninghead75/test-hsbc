package com.hsbc.brule;

import com.hsbc.brule.statistics.IntegerEvenSlidingWindowStatistics;
import com.hsbc.brule.statistics.IntegerEvenSlidingWindowStatistics.IntegerEventStatistics;
import com.hsbc.brule.event.NumAndProbabilityEvent;
import com.hsbc.brule.statistics.SlidingWindowStatistics.Statistics;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

class IntegerEvenSlidingWindowStatisticsTest {

    @Test
    void integerEvenSlidingWindowStatisticsTest() throws InterruptedException {
        final IntegerEvenSlidingWindowStatistics slidingWindowStatistic = new IntegerEvenSlidingWindowStatistics(1);

        final NumAndProbabilityEvent event1 = createMockEvent(5,0.5f);
        final NumAndProbabilityEvent event2 = createMockEvent(5,0.25f);
        final NumAndProbabilityEvent event3 = createMockEvent(5,0.75f);
        final NumAndProbabilityEvent event4 = createMockEvent(3,0.15f);
        final NumAndProbabilityEvent event5 = createMockEvent(3,0.55f);
        final NumAndProbabilityEvent event6 = createMockEvent(7,0.52f);

        slidingWindowStatistic.add(event1);
        slidingWindowStatistic.add(event2);
        slidingWindowStatistic.add(event3);
        slidingWindowStatistic.add(event4);
        Thread.sleep(1500);
        slidingWindowStatistic.add(event5);
        slidingWindowStatistic.add(event6);

        final Statistics latestStatistics = slidingWindowStatistic.getLatestStatistics();

        assertThat(latestStatistics.getMode(), is(equalTo(3)));
        assertThat(latestStatistics.getMean(), is(equalTo(5f)));
    }

    NumAndProbabilityEvent createMockEvent(int value, float probability){
        final NumAndProbabilityEvent event = Mockito.mock(NumAndProbabilityEvent.class);
        when(event.getNumber()).thenReturn(value);
        when(event.getProbabilityOfSample()).thenReturn(probability);
        return event;
    }



    @Test
    void intValuesStatisticTest() {
        int[] values = new int[]{45, 80, 27, 32, 41, 49, 53, 77, 51, 41, 33, 55, 32, 77};
        final Statistics statistics = new IntegerEventStatistics(values);
        assertThat(statistics.getMean(), is(equalTo(49.5f)));
        assertThat(statistics.getMode(), is(equalTo(32)));
        assertThat(statistics.getPercentile(41), is(equalTo(35.714287f)));
    }
}
