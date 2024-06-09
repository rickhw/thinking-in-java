package com.gtcafe.race.bean.capacityUnit;

import java.util.concurrent.locks.ReentrantLock;

public class NormalCapacityUnit implements ICapacityUnit {

    private int capacityUnit = 0;

    public NormalCapacityUnit() {
        System.out.printf("Init counter, value: [%s]\n", this.getValue());
    }

    public int getValue() {
        return capacityUnit;
    }

    public void reset() {
        capacityUnit = 0;
    }

    public void operate(int value) {

        capacityUnit += value;

        System.out.printf("After operate: Thread: [%s], Value: [%s]\n", Thread.currentThread().getName(),
                this.getValue());
    }

}
