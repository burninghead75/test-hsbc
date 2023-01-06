package com.hsbc.brule;

import java.util.UUID;
import java.util.function.BiPredicate;

public interface EventBus {

    // Feel free to replace Object with something more specific,
    // but be prepared to justify it
    void publishEvent(Event event);

    // How would you denote the subscriber?
    // EventBus will return a unique identifier ( here a UUID ).
    UUID addSubscriber(Subscriber subscriber);

    // Would you allow clients to filter the events they receive? How would the interface look like?
    UUID addSubscriberForFilteredEvents(Subscriber subscriber, EventFilter...filters);

    void shutdown();



    final BiPredicate<SubscriberWithFilters, Event> isEventMatch = (subscriberWithFilters, event) -> subscriberWithFilters.filters().stream().allMatch(eventMatcher -> eventMatcher.match(event));

}
