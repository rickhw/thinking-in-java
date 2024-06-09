package com.gtcafe.race.bean.capacityUnit.impl;

import com.gtcafe.race.bean.capacityUnit.ICapacityUnit;

public class NormalCapacityUnit implements ICapacityUnit {

    private int capacityUnit = 0;

    // public NormalCapacityUnit() {
    //     System.out.printf("Init counter, value: [%s]\n", this.getValue());
    // }

    public int getValue() {
        return capacityUnit;
    }

    public void reset() {
        capacityUnit = 0;
    }

    public void operate(int value) {
        capacityUnit += value;
    }

    public void increase(int value) {
        capacityUnit = capacityUnit + value;
    }

    public void decrease(int value) {
        capacityUnit = capacityUnit - value;
    }

}
