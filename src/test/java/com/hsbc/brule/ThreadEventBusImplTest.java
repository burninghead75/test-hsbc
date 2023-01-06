package com.hsbc.brule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

import static com.hsbc.brule.ProbabilisticRandomGenImpl.createWithGeneratedValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ThreadEventBusImplTest {

    private static final Logger logger = LoggerFactory.getLogger(ThreadEventBusImplTest.class);

    private EventBus eventBus;
    private ProbabilisticRandomGen randomGen;

    @BeforeEach
    void init() {
        eventBus = new ThreadEventBusImpl();
        randomGen = createWithGeneratedValue(2);
    }

    @Test
    void publishEvent() throws InterruptedException {

        final Subscriber subscriber1 = Mockito.mock(Subscriber.class);
        final Subscriber subscriber2 = Mockito.mock(Subscriber.class);

        eventBus.addSubscriber(subscriber1);
        eventBus.addSubscriber(subscriber2);

        CountDownLatch latch = new CountDownLatch(4);

        Mockito.doAnswer(invocationOnMock -> {
            latch.countDown();
            return null;
        }).when(subscriber1).onEventReceived(any());

        Mockito.doAnswer(invocationOnMock -> {
            latch.countDown();
            return null;
        }).when(subscriber2).onEventReceived(any());



        final Event event1 = Event.of(randomGen.nextFromSample());
        final Event event2 = Event.of(randomGen.nextFromSample());

        eventBus.publishEvent(event1);
        eventBus.publishEvent(event2);

        latch.await();

        Mockito.verify(subscriber1).onEventReceived(event1);
        Mockito.verify(subscriber1).onEventReceived(event2);
        Mockito.verify(subscriber2).onEventReceived(event1);
        Mockito.verify(subscriber2).onEventReceived(event2);

    }

    @Test
    void publishEventWithFilterSubscriber() throws InterruptedException {
        final Subscriber subscriber = Mockito.mock(Subscriber.class);
        final Subscriber subscriberWithFilter = Mockito.mock(Subscriber.class);
        final EventFilter eventFilter = Mockito.mock(EventFilter.class);

        eventBus.addSubscriber(subscriber);
        eventBus.addSubscriberForFilteredEvents(subscriberWithFilter, eventFilter);

        CountDownLatch latch = new CountDownLatch(3);

        Mockito.doAnswer(invocationOnMock -> {
            latch.countDown();
            return null;
        }).when(subscriber).onEventReceived(any());

        Mockito.doAnswer(invocationOnMock -> {
            latch.countDown();
            return null;
        }).when(subscriberWithFilter).onEventReceived(any());

        final Event event1 = Event.of(randomGen.nextFromSample());
        final Event event2 = Event.of(randomGen.nextFromSample());

        when(eventFilter.match(event1)).thenReturn(true);
        when(eventFilter.match(event2)).thenReturn(false);

        eventBus.publishEvent(event1);
        eventBus.publishEvent(event2);

        latch.await();

        Mockito.verify(subscriber, times(1)).onEventReceived(event1);
        Mockito.verify(subscriber, times(1)).onEventReceived(event2);
        Mockito.verify(subscriberWithFilter, times(1)).onEventReceived(event1);
    }

    @Test
    void publishCoalescingEvent() throws InterruptedException {
        final Subscriber subscriber = Mockito.mock(Subscriber.class);

        eventBus.addSubscriber(subscriber);

        CountDownLatch latch = new CountDownLatch(2);



        final Event event1 = CoalescingEvent.of(new ProbabilisticRandomGen.NumAndProbability(1, 0.1f));
        final Event event2 = CoalescingEvent.of(new ProbabilisticRandomGen.NumAndProbability(1, 0.3f));
        final Event event3 = CoalescingEvent.of(new ProbabilisticRandomGen.NumAndProbability(1, 0.6f));

        Mockito.doAnswer(invocationOnMock -> {
            logger.info("Received event 1");
            Thread.sleep(1000);
            latch.countDown();
            return null;
        }).when(subscriber).onEventReceived(event1);

        Mockito.doAnswer(invocationOnMock -> {
            logger.info("Received event 2");
            return null;
        }).when(subscriber).onEventReceived(event2);

        Mockito.doAnswer(invocationOnMock -> {
            logger.info("Received event 3");
            latch.countDown();
            return null;
        }).when(subscriber).onEventReceived(event3);

        eventBus.publishEvent(event1);
        Thread.sleep(100);
        eventBus.publishEvent(event2);
        Thread.sleep(100);
        eventBus.publishEvent(event3);

        latch.await();

        Mockito.verify(subscriber, times(1)).onEventReceived(event1);
        Mockito.verify(subscriber, times(1)).onEventReceived(event3);

    }

}
