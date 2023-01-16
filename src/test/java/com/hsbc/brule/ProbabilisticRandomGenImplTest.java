package com.hsbc.brule;

import com.hsbc.brule.generator.ProbabilisticRandomGen;
import com.hsbc.brule.generator.ProbabilisticRandomGen.NumAndProbability;
import com.hsbc.brule.generator.ProbabilisticRandomGenImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


class ProbabilisticRandomGenImplTest {

    @Test
    void nextFromSample() {

        final NumAndProbability firstElement = new NumAndProbability(1, 0.31f);
        final NumAndProbability secondElement = new NumAndProbability(5, 0.17f);
        final NumAndProbability thirdElement = new NumAndProbability(3, 0.25f);

        final ProbabilisticRandomGen probabilisticRandomGen = ProbabilisticRandomGenImpl.create(List.of(firstElement,secondElement,thirdElement));

        expect(probabilisticRandomGen.nextFromSample(), 1, 0.31f);
        expect(probabilisticRandomGen.nextFromSample(), 5, 0.17f);
        expect(probabilisticRandomGen.nextFromSample(), 3, 0.25f);
        assertThat(probabilisticRandomGen.nextFromSample(), is(nullValue()));
    }

    @Test
    void nextFromGeneratedSample() {

        final ProbabilisticRandomGen probabilisticRandomGen = ProbabilisticRandomGenImpl.createWithGeneratedValue(2);
        final NumAndProbability firstElement = probabilisticRandomGen.nextFromSample();
        final NumAndProbability secondElement = probabilisticRandomGen.nextFromSample();
        assertThat(firstElement.getNumber(), is(lessThan(secondElement.getNumber())));
        assertThat(firstElement.getProbabilityOfSample(), allOf(is(greaterThanOrEqualTo(0f)), is(lessThanOrEqualTo(1f))));
        assertThat(secondElement.getProbabilityOfSample(), allOf(is(greaterThanOrEqualTo(0f)), is(lessThanOrEqualTo(1f))));
        assertThat(probabilisticRandomGen.nextFromSample(), is(nullValue()));
    }

    private void expect(NumAndProbability numAndProb, int i, float f){
        assertThat(numAndProb.getNumber(), is(equalTo(i)));
        assertThat(numAndProb.getProbabilityOfSample(), is(equalTo(f)));
    }

}
