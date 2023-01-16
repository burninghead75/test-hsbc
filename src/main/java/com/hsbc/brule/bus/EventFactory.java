package com.hsbc.brule.bus;

public interface EventFactory<T>
{
    T createEvent();
}
