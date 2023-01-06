package com.hsbc.brule;

import com.hsbc.brule.IntegerEvenSlidingWindowStatistics.IntegerEventStatistics;
import com.hsbc.brule.ProbabilisticRandomGen.NumAndProbability;
import com.hsbc.brule.SlidingWindowStatistics.Statistics;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

class IntegerEvenSlidingWindowStatisticsTest {

    @Test
    void integerEvenSlidingWindowStatisticsTest() {
        final IntegerEvenSlidingWindowStatistics slidingWindowStatistic = new IntegerEvenSlidingWindowStatistics(1);

        final Event event1 = createMockEvent(5,9000l);
        final Event event2 = createMockEvent(5,10000l);
        final Event event3 = createMockEvent(5,10100l);
        final Event event4 = createMockEvent(3,10200l);
        final Event event5 = createMockEvent(3,10500l);
        final Event event6 = createMockEvent(7,11010l);

        slidingWindowStatistic.add(event1);
        slidingWindowStatistic.add(event2);
        slidingWindowStatistic.add(event3);
        slidingWindowStatistic.add(event4);
        slidingWindowStatistic.add(event5);
        slidingWindowStatistic.add(event6);

        final Statistics latestStatistics = slidingWindowStatistic.getLatestStatistics();

        assertThat(latestStatistics.getMode(), is(equalTo(3)));
        assertThat(latestStatistics.getMean(), is(equalTo(9f/2f)));
    }

    Event createMockEvent(int value, long timestamp){
        final Event event = Mockito.mock(Event.class);
        final NumAndProbability numAndProbability = Mockito.mock(NumAndProbability.class);
        when(event.getEventBody()).thenReturn(numAndProbability);
        when(numAndProbability.getNumber()).thenReturn(value);
        when(event.getTimeStampInNano()).thenReturn(timestamp);
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
