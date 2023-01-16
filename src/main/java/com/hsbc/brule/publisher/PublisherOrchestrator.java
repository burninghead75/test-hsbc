package com.hsbc.brule.publisher;

import com.hsbc.brule.bus.CircularBuffer;
import com.hsbc.brule.event.Event;

public class PublisherOrchestrator<E extends Event> {

    private volatile long currentIndex = -1;

    private final CircularBuffer<E> buffer;


    public PublisherOrchestrator(CircularBuffer<E> buffer) {
        this.buffer = buffer;
    }

    public long getNextIndex() {
        long nextIndex = currentIndex + 1;
        while (nextIndex > this.buffer.getMinUnavailableIndex()){
            Thread.yield();
        }
        currentIndex = nextIndex;
        return currentIndex;
    }

    E get(long index) {
        return buffer.get(index);
    }

    public void publish(long index) {
        if (index < currentIndex) {
            throw new RuntimeException("Another index is more recent");
        }
    }

    public long getCurrentIndex() {
        return this.currentIndex;
    }
}
