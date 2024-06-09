package com.gtcafe.race.singleton.capacityUnit.impl;

import com.gtcafe.race.singleton.capacityUnit.ICapacityUnit;

public class NativeCapacityUnit implements ICapacityUnit {

    private int capacityUnit = 0;

    public int getValue() {
        return capacityUnit;
    }

    public void reset() {
        capacityUnit = 0;
    }

    public synchronized void operate(int value) {
        capacityUnit += value;
    }


    public synchronized void increase(int value) {
        capacityUnit = capacityUnit + value;
    }

    public synchronized void decrease(int value) {
        capacityUnit = capacityUnit - value;
    }

}
