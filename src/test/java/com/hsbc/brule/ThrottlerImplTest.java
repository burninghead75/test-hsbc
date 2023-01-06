package com.hsbc.brule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

import static com.hsbc.brule.ProbabilisticRandomGenImpl.createWithGeneratedValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

class ThrottlerImplTest {

    private static final Logger logger = LoggerFactory.getLogger(ThrottlerImplTest.class);

    private EventBus eventBus;
    private ProbabilisticRandomGen randomGen;

    @BeforeEach
    void init() {
        eventBus = new ThreadEventBusImpl();
        randomGen = createWithGeneratedValue(2);
    }

    @Test
    void shouldNotPublishWhileThrottled() throws InterruptedException {
        final Subscriber subscriber = Mockito.mock(Subscriber.class);
        final ThrottlerImpl throttler = new ThrottlerImpl();

        final CountDownLatch latch = new CountDownLatch(2);

        Mockito.doAnswer(invocationOnMock -> {
            Throttler.Throttled throttled = invocationOnMock.getArgument(0);
            throttler.notifyWhenCanProceed(throttled);
            return null;
        }).when(subscriber).notifyWhenCanProceed(any());

        eventBus.addSubscriber(subscriber);

        final Event event1 = Event.of(randomGen.nextFromSample());
        final Event event2 = Event.of(randomGen.nextFromSample());

        Mockito.doAnswer(invocationOnMock -> {
            logger.info("Received event 1");
            latch.countDown();
            return null;
        }).when(subscriber).onEventReceived(event1);

        Mockito.doAnswer(invocationOnMock -> {
            logger.info("Received event 2");
            latch.countDown();
            return null;
        }).when(subscriber).onEventReceived(event2);

        eventBus.publishEvent(event1);
        Thread.sleep(100);
        Mockito.verify(subscriber, times(1)).onEventReceived(event1);
        throttler.shouldNotProceed();
        Thread.sleep(500);
        eventBus.publishEvent(event2);
        Thread.sleep(1000);
        Mockito.verify(subscriber, never()).onEventReceived(event2);
        Thread.sleep(500);
        throttler.shouldProceed();
        Thread.sleep(500);
        Mockito.verify(subscriber, times(1)).onEventReceived(event2);

        latch.await();


    }

}
