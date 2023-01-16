package com.hsbc.brule.publisher;

import com.hsbc.brule.bus.CircularBufferEventBus;
import com.hsbc.brule.event.NumAndProbabilityEvent;
import com.hsbc.brule.generator.ProbabilisticRandomGen;
import com.hsbc.brule.generator.ProbabilisticRandomGen.NumAndProbability;
import com.hsbc.brule.throttler.Throttler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.hsbc.brule.generator.ProbabilisticRandomGenImpl.createWithGeneratedValue;

public class PublisherService implements Runnable , Throttler.Throttled {

    private static final Logger logger = LoggerFactory.getLogger(PublisherService.class);

    private final ProbabilisticRandomGen generator;
    private final CircularBufferEventBus eventBus;

    private boolean canPushEvent = true;




    public PublisherService(CircularBufferEventBus eventBus, int numberOfEvents) {
        logger.info("New publisher created");
        this.eventBus = eventBus;
        this.generator = createWithGeneratedValue(numberOfEvents);
    }


    @Override
    public void run() {
        logger.info("Publisher started");
        NumAndProbability numAndProbability = this.generator.nextFromSample();
        while (numAndProbability != null) {
            try {
                if(!this.canPushEvent){
                    Thread.yield();
                }else{
                    final long nextIndex = eventBus.getNextIndex();
                    final NumAndProbabilityEvent event = (NumAndProbabilityEvent) eventBus.get(nextIndex);
                    event.setNumber(numAndProbability.getNumber());
                    event.setProbabilityOfSample(numAndProbability.getProbabilityOfSample());
                    eventBus.publish(nextIndex);
                    logger.info("Publish Event : index {} - {} - {}", nextIndex, numAndProbability.getNumber(), numAndProbability.getProbabilityOfSample());
                    Thread.sleep(10);
                    numAndProbability = this.generator.nextFromSample();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void canPushEvent() {
        logger.info("Resume publishing event");
        this.canPushEvent = true;

    }

    @Override
    public void shouldKeepEvent() {
        logger.info("Stop publishing event");
        this.canPushEvent = false;
    }
}
