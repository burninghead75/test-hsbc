package com.hsbc.brule;

import com.hsbc.brule.bus.SingleThreadEventBusImpl;
import com.hsbc.brule.event.EventFilter;
import com.hsbc.brule.event.NumAndProbabilityEvent;
import com.hsbc.brule.generator.ProbabilisticRandomGen;
import com.hsbc.brule.subscriber.Subscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static com.hsbc.brule.generator.ProbabilisticRandomGenImpl.createWithGeneratedValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class SingleThreadEventBusImplTest {

    private SingleThreadEventBusImpl eventBus;
    private ProbabilisticRandomGen randomGen;

    @BeforeEach
    void init() {
        eventBus = new SingleThreadEventBusImpl();
        randomGen = createWithGeneratedValue(2);
    }

    @Test
    void publishEvent() {

        final Subscriber subscriber1 = Mockito.mock(Subscriber.class);
        final Subscriber subscriber2 = Mockito.mock(Subscriber.class);

        eventBus.addSubscriber(subscriber1);
        eventBus.addSubscriber(subscriber2);

        final NumAndProbabilityEvent event1 = NumAndProbabilityEvent.of(randomGen.nextFromSample());
        final NumAndProbabilityEvent event2 = NumAndProbabilityEvent.of(randomGen.nextFromSample());


        ArgumentCaptor<NumAndProbabilityEvent> argument1_1 = ArgumentCaptor.forClass(NumAndProbabilityEvent.class);
        ArgumentCaptor<NumAndProbabilityEvent> argument1_2 = ArgumentCaptor.forClass(NumAndProbabilityEvent.class);
        ArgumentCaptor<NumAndProbabilityEvent> argument2_1 = ArgumentCaptor.forClass(NumAndProbabilityEvent.class);
        ArgumentCaptor<NumAndProbabilityEvent> argument2_2 = ArgumentCaptor.forClass(NumAndProbabilityEvent.class);

        eventBus.publish(event1);
        eventBus.publish(event2);

        Mockito.verify(subscriber1, times(2)).onEventReceived(argument1_1.capture());
        Mockito.verify(subscriber2, times(2)).onEventReceived(argument2_1.capture());
        assertThat(argument1_1.getAllValues().get(0).getNumber(), is(event1.getNumber()));
        assertThat(argument1_1.getAllValues().get(0).getProbabilityOfSample(), is(event1.getProbabilityOfSample()));
        assertThat(argument2_1.getAllValues().get(0).getNumber(), is(event1.getNumber()));
        assertThat(argument2_1.getAllValues().get(0).getProbabilityOfSample(), is(event1.getProbabilityOfSample()));
        assertThat(argument1_1.getAllValues().get(1).getNumber(), is(event2.getNumber()));
        assertThat(argument1_1.getAllValues().get(1).getProbabilityOfSample(), is(event2.getProbabilityOfSample()));
        assertThat(argument2_1.getAllValues().get(1).getNumber(), is(event2.getNumber()));
        assertThat(argument2_1.getAllValues().get(1).getProbabilityOfSample(), is(event2.getProbabilityOfSample()));

    }

    @Test
    void publishEventWithFilterSubscriber() {
        final Subscriber subscriber = Mockito.mock(Subscriber.class);
        final Subscriber subscriberWithFilter = Mockito.mock(Subscriber.class);
        final EventFilter eventFilter = Mockito.mock(EventFilter.class);

        eventBus.addSubscriber(subscriber);
        eventBus.addSubscriberForFilteredEvents(subscriberWithFilter, eventFilter);

        final NumAndProbabilityEvent event1 = NumAndProbabilityEvent.of(randomGen.nextFromSample());
        final NumAndProbabilityEvent event2 = NumAndProbabilityEvent.of(randomGen.nextFromSample());

        when(eventFilter.match(event1)).thenReturn(true);
        when(eventFilter.match(event2)).thenReturn(false);

        eventBus.publish(event1);
        eventBus.publish(event2);

        Mockito.verify(subscriber, times(2)).onEventReceived(any());
        Mockito.verify(subscriberWithFilter, times(1)).onEventReceived(any());

    }


}
