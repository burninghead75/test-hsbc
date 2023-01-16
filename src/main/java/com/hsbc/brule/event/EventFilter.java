package com.hsbc.brule.event;


public interface EventFilter<E extends Event> {
    boolean match(E event);
}
