package com.gtcafe.asimov.platform.capacity.domain;

import org.springframework.stereotype.Service;

@Service
public class NativeCapacityUnit implements ICapacityUnit {

    private int capacityUnit = DEFAULT_CAPACITY_UNIT;

    public int getValue() {
        return capacityUnit;
    }

    public void reset() {
        this.capacityUnit = DEFAULT_CAPACITY_UNIT;
    }

    public synchronized void consume(int value) {
        capacityUnit -= value;
    }

    public synchronized void resume(int value) {
        capacityUnit += value;
    }
}
