package com.hsbc.brule.bus;

import com.hsbc.brule.event.Event;

public class CircularBuffer<E extends Event> {

    private final Object[] buffer;

    private volatile long minUnavailableIndex = - 1;

    private static final double DEFAULT_FACTOR = 8;

    private final int bufferSize;

    public CircularBuffer(EventFactory<E> eventFactory) {
        this(DEFAULT_FACTOR, eventFactory);
    }

    public CircularBuffer(double factor, EventFactory<E> eventFactory) {
        bufferSize = (int)Math.pow(2,factor);
        this.minUnavailableIndex = bufferSize;
        this.buffer = new Object[bufferSize];
        for (int i = 0; i < bufferSize; i++) {
                this.buffer[i] = eventFactory.createEvent();
        }
    }

    public E get(long index){
        return (E)buffer[Utils.mod(index, bufferSize)];
    }

    public void lastValue(long lastValueConsumed){
        long minUnavailableIndex = lastValueConsumed + this.bufferSize;
        if(this.minUnavailableIndex != minUnavailableIndex){
            this.minUnavailableIndex = minUnavailableIndex;
        }
    }

    public long getMinUnavailableIndex() {
        return this.minUnavailableIndex;
    }
}
