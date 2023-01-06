package com.hsbc.brule;

import java.util.Collection;
import java.util.Objects;

public record SubscriberWithFilters(Subscriber subscriber, Collection<EventFilter> filters) {

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SubscriberWithFilters) obj;
        return Objects.equals(this.subscriber, that.subscriber) &&
                Objects.equals(this.filters, that.filters);
    }

    @Override
    public String toString() {
        return "SubscriberWithMatchers[" +
                "subscriber=" + subscriber + ", " +
                "filters=" + filters + ']';
    }

}
