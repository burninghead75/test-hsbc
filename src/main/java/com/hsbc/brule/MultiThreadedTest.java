package com.hsbc.brule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadedTest {


    public static void main(String[] args) throws Exception {
        testWithFilter();
    }

    public static void test() throws InterruptedException {
        final EventBus eventBus = new ThreadEventBusImpl();

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        final Subscriber plainSubscriber = new SingleBusEventSubscriber();
        plainSubscriber.subscribe(eventBus);
        final Subscriber plainSubscriber2 = new SingleBusEventSubscriber();
        plainSubscriber2.subscribe(eventBus);


        PublisherService publisher1 = new PublisherService(eventBus, 5);
        PublisherService publisher2 = new PublisherService(eventBus, 5);
        executorService.submit(publisher1);
        executorService.submit(publisher2);

        Thread.sleep(5000);
        executorService.shutdownNow();
        eventBus.shutdown();


    }


    public static void testWithFilter() throws InterruptedException {
        final EventBus eventBus = new ThreadEventBusImpl();

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        final Subscriber plainSubscriber = new SingleBusEventSubscriber();
        plainSubscriber.subscribe(eventBus);
        final Subscriber plainSubscriber2 = new SingleBusEventSubscriber();
        plainSubscriber2.subscribe(eventBus);


        PublisherService publisher1 = new PublisherService(eventBus, 5);
        PublisherService publisher2 = new PublisherService(eventBus, 5);
        executorService.submit(publisher1);
        executorService.submit(publisher2);

        Thread.sleep(5000);
        executorService.shutdownNow();
        eventBus.shutdown();


    }


}
