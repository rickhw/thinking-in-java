package com.gtcafe.asimov.platform.capacity.domain;

public interface ICapacityUnit {
    int DEFAULT_CAPACITY_UNIT = 100;
    
    public int getValue();

    public void reset();

    // public void operate(int value);

    // public void increase(int value);

    // public void decrease(int value);

    void consume(int unit) throws CapacityInsufficientException;

    void resume(int unit);
}
