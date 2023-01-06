package com.hsbc.brule;

import com.hsbc.brule.ProbabilisticRandomGen.NumAndProbability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.hsbc.brule.ProbabilisticRandomGenImpl.createWithGeneratedValue;

public class PublisherService implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(PublisherService.class);

    private final ProbabilisticRandomGen generator;
    private final EventBus eventBus;

    public PublisherService(EventBus eventBus) {
        this(eventBus, 10);
    }

    public PublisherService(EventBus eventBus, int numberOfEvents) {
        logger.info("New publisher created");
        this.eventBus = eventBus;
        this.generator = createWithGeneratedValue(numberOfEvents);
    }


    @Override
    public void run() {
        logger.info("Publisher started");
        NumAndProbability numAndProbability = this.generator.nextFromSample();
        while ( numAndProbability != null){
            try {
                final Event event = Event.of(numAndProbability);
                logger.info("Publish Event {} ",event);
                eventBus.publishEvent(event);
                Thread.sleep(500);
                numAndProbability = this.generator.nextFromSample();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };

    }
}
