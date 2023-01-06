package com.hsbc.brule;

import com.hsbc.brule.ProbabilisticRandomGen.NumAndProbability;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


class ProbabilisticRandomGenImplTest {

    @Test
    void nextFromSample() {
        final ProbabilisticRandomGen probabilisticRandomGen = ProbabilisticRandomGenImpl.createWithGeneratedValue(2);
        final NumAndProbability firstElement = probabilisticRandomGen.nextFromSample();
        final NumAndProbability secondElement = probabilisticRandomGen.nextFromSample();
        assertThat(firstElement.getNumber(), is(lessThan(secondElement.getNumber())));
        assertThat(firstElement.getProbabilityOfSample(), allOf(is(greaterThanOrEqualTo(0f)), is(lessThanOrEqualTo(1f))));
        assertThat(secondElement.getProbabilityOfSample(), allOf(is(greaterThanOrEqualTo(0f)), is(lessThanOrEqualTo(1f))));
        assertThat(probabilisticRandomGen.nextFromSample(), is(nullValue()));
    }

}
