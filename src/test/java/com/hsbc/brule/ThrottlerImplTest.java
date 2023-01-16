package com.hsbc.brule;

import com.hsbc.brule.bus.CircularBufferEventBus;
import com.hsbc.brule.event.NumAndProbabilityEvent;
import com.hsbc.brule.generator.ProbabilisticRandomGen;
import com.hsbc.brule.publisher.PublisherService;
import com.hsbc.brule.subscriber.Subscriber;
import com.hsbc.brule.throttler.MaxEventPublishedThrottler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.hsbc.brule.generator.ProbabilisticRandomGenImpl.createWithGeneratedValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.times;


class ThrottlerImplTest {

    private static final Logger logger = LoggerFactory.getLogger(ThrottlerImplTest.class);

    private CircularBufferEventBus eventBus;
    private ProbabilisticRandomGen randomGen;

    @BeforeEach
    void init() {
        eventBus = new CircularBufferEventBus(NumAndProbabilityEvent::new);
        randomGen = createWithGeneratedValue(2);
    }

    @Test
    void shouldNotPublishWhileThrottled() throws InterruptedException {

        final int maxEventPublish = 5;

        final MaxEventPublishedThrottler throttler = new MaxEventPublishedThrottler(maxEventPublish, 20000);

        final Subscriber subscriber = Mockito.mock(Subscriber.class);

        Mockito.doAnswer(invocationOnMock -> {
            logger.info("Receiv event in subscriber 1");
            return null;
        }).when(subscriber).onEventReceived(any());

        eventBus.addSubscriber(subscriber);


        final PublisherService publisher1 = new PublisherService(eventBus, 100);
        final PublisherService publisher2 = new PublisherService(eventBus, 100);

        eventBus.addThrottler(throttler);
        throttler.notifyWhenCanProceed(publisher1);
        throttler.notifyWhenCanProceed(publisher2);

        eventBus.start();

        final ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.submit(publisher1);
        executorService.submit(publisher2);

        Thread.sleep(2000);

        Mockito.verify(subscriber, atMost(maxEventPublish)).onEventReceived(any());

    }

}
