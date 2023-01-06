package com.hsbc.brule;

import com.hsbc.brule.EventFilter.GreaterThan;

import static com.hsbc.brule.ProbabilisticRandomGenImpl.createWithGeneratedValue;

public class PlainSingleThreadTest {

    public static void main(String[] args) {
        final EventBus eventBus = new ThreadEventBusImpl();


        final Subscriber plainSubscriber = new SingleBusEventSubscriber();
        plainSubscriber.subscribe(eventBus);

        final Subscriber subscriberWithFilter = new SingleBusEventSubscriber();
        final EventFilter probabilityGreaterThan = new GreaterThan(0.5f);
        subscriberWithFilter.subscribe(eventBus, probabilityGreaterThan);

        final ProbabilisticRandomGen generator = createWithGeneratedValue();
        eventBus.publishEvent(Event.of(generator.nextFromSample()));
        eventBus.publishEvent(Event.of(generator.nextFromSample()));
        eventBus.publishEvent(Event.of(generator.nextFromSample()));

    }
}
