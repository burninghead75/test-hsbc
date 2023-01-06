package com.hsbc.brule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.hsbc.brule.ProbabilisticRandomGenImpl.createWithGeneratedValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class SingleThreadEventBusImplTest {

    private EventBus eventBus;
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

        final Event event1 = Event.of(randomGen.nextFromSample());
        final Event event2 = Event.of(randomGen.nextFromSample());

        eventBus.publishEvent(event1);
        eventBus.publishEvent(event2);

        Mockito.verify(subscriber1).onEventReceived(event1);
        Mockito.verify(subscriber1).onEventReceived(event2);
        Mockito.verify(subscriber2).onEventReceived(event1);
        Mockito.verify(subscriber2).onEventReceived(event2);

    }

    @Test
    void publishEventWithFilterSubscriber() {
        final Subscriber subscriber = Mockito.mock(Subscriber.class);
        final Subscriber subscriberWithFilter = Mockito.mock(Subscriber.class);
        final EventFilter eventFilter = Mockito.mock(EventFilter.class);

        eventBus.addSubscriber(subscriber);
        eventBus.addSubscriberForFilteredEvents(subscriberWithFilter, eventFilter);

        final Event event1 = Event.of(randomGen.nextFromSample());
        final Event event2 = Event.of(randomGen.nextFromSample());

        when(eventFilter.match(event1)).thenReturn(true);
        when(eventFilter.match(event2)).thenReturn(false);

        eventBus.publishEvent(event1);
        eventBus.publishEvent(event2);

        Mockito.verify(subscriber, times(2)).onEventReceived(any());
        Mockito.verify(subscriberWithFilter, times(1)).onEventReceived(any());

    }


}
