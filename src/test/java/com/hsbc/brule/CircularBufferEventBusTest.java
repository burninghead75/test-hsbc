package com.hsbc.brule;

import com.hsbc.brule.bus.CircularBufferEventBus;
import com.hsbc.brule.event.EventFilter;
import com.hsbc.brule.event.NumAndProbabilityEvent;
import com.hsbc.brule.generator.ProbabilisticRandomGen;
import com.hsbc.brule.publisher.PublisherService;
import com.hsbc.brule.subscriber.Subscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static com.hsbc.brule.generator.ProbabilisticRandomGenImpl.createWithGeneratedValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

class CircularBufferEventBusTest {

    private static final Logger logger = LoggerFactory.getLogger(CircularBufferEventBusTest.class);

    private CircularBufferEventBus<NumAndProbabilityEvent> eventBus;
    private ProbabilisticRandomGen randomGen;

    @BeforeEach
    void init() {
        eventBus = new CircularBufferEventBus(NumAndProbabilityEvent::new);
        randomGen = createWithGeneratedValue(2);
    }

    @Test
    void publishEvent() throws InterruptedException {

        final Subscriber subscriber1 = Mockito.mock(Subscriber.class);
        final Subscriber subscriber2 = Mockito.mock(Subscriber.class);

        eventBus.addSubscriber(subscriber1);
        eventBus.addSubscriber(subscriber2);
        eventBus.start();

        CountDownLatch latch = new CountDownLatch(4);

        Mockito.doAnswer(invocationOnMock -> {
            latch.countDown();
            return null;
        }).when(subscriber1).onEventReceived(any());

        Mockito.doAnswer(invocationOnMock -> {
            latch.countDown();
            return null;
        }).when(subscriber2).onEventReceived(any());

        publish(randomGen);

        latch.await();

        Mockito.verify(subscriber1, times(2)).onEventReceived(any());
        Mockito.verify(subscriber2, times(2)).onEventReceived(any());

    }

    @Test
    void publishEventWithFilterSubscriber() throws InterruptedException {

        final Subscriber subscriber1 = Mockito.mock(Subscriber.class);
        final Subscriber subscriber2 = Mockito.mock(Subscriber.class);

        eventBus.addSubscriber(subscriber1);
        EventFilter<NumAndProbabilityEvent> eventFilter =new EventFilter<NumAndProbabilityEvent>() {
            @Override
            public boolean match(NumAndProbabilityEvent event) {
                return event.getProbabilityOfSample() >= 0.5f;
            }
        };
        eventBus.addSubscriberForFilteredEvents(subscriber2,  eventFilter);

        final ProbabilisticRandomGen.NumAndProbability proba1 = new ProbabilisticRandomGen.NumAndProbability(1, 0.2f);
        final ProbabilisticRandomGen.NumAndProbability proba2 = new ProbabilisticRandomGen.NumAndProbability(2, 0.7f);

        eventBus.start();

        CountDownLatch latch = new CountDownLatch(3);

        Mockito.doAnswer(invocationOnMock -> {
            latch.countDown();
            return null;
        }).when(subscriber1).onEventReceived(any());

        Mockito.doAnswer(invocationOnMock -> {
            latch.countDown();
            return null;
        }).when(subscriber2).onEventReceived(any());

        publish(proba1);
        publish(proba2);

        latch.await();

        Thread.sleep(1000);

        Mockito.verify(subscriber1, times(2)).onEventReceived(any());
        Mockito.verify(subscriber2, times(1)).onEventReceived(any());

    }

    @Test
    void multiplePublisherWithMultipleSubscriber() throws InterruptedException {



        final Subscriber subscriber1 = Mockito.mock(Subscriber.class);
        final Subscriber subscriber2 = Mockito.mock(Subscriber.class);

        eventBus.addSubscriber(subscriber1);
        eventBus.addSubscriber(subscriber2);
        eventBus.start();

        CountDownLatch latch = new CountDownLatch(8);

        Mockito.doAnswer(invocationOnMock -> {
            logger.info("Receiv event in subscriber 1");
            latch.countDown();
            return null;
        }).when(subscriber1).onEventReceived(any());

        Mockito.doAnswer(invocationOnMock -> {
            logger.info("Receiv event in subscriber 2");
            latch.countDown();
            return null;
        }).when(subscriber2).onEventReceived(any());

        final PublisherService publisher1 = new PublisherService(eventBus, 2);
        final PublisherService publisher2 = new PublisherService(eventBus, 2);

        final ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(publisher1);
        Thread.sleep(200);
        executor.submit(publisher2);

        latch.await();

        Mockito.verify(subscriber1, times(4)).onEventReceived(any());
        Mockito.verify(subscriber2, times(4)).onEventReceived(any());

    }

    void publish(ProbabilisticRandomGen sampleGenerator) {

        ProbabilisticRandomGen.NumAndProbability sample = sampleGenerator.nextFromSample();
        while (sample != null) {
            if (sample.getNumber() == 750) {
                System.out.println("Kikou les lol");
            }
            publish(sample);
            sample = sampleGenerator.nextFromSample();
        }
        System.out.println("Stop");
    }

    void publish(ProbabilisticRandomGen.NumAndProbability sample) {
        final long nextIndex = eventBus.getNextIndex();
        final NumAndProbabilityEvent event = eventBus.get(nextIndex);
        event.setNumber(sample.getNumber());
        event.setProbabilityOfSample(sample.getProbabilityOfSample());
        eventBus.publish(nextIndex);
    }

}
