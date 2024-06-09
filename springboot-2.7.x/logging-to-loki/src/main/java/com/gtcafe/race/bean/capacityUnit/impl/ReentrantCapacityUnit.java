package com.gtcafe.race.bean.capacityUnit.impl;

import com.gtcafe.race.bean.capacityUnit.ICapacityUnit;

import java.util.concurrent.locks.ReentrantLock;

public class ReentrantCapacityUnit implements ICapacityUnit {

    private int capacityUnit = 0;
    private ReentrantLock locker = new ReentrantLock();

    // public ReentrantCapacityUnit() {
    //     System.out.printf("Init counter, value: [%s]\n", this.getValue());
    // }

    public int getValue() {
        return capacityUnit;
    }

    public void reset() {
        capacityUnit = 0;
    }

    public void operate(int value) {
        locker.lock();

        try {
            capacityUnit += value;
        } catch (Exception ex) {
        } finally {
            locker.unlock();
        }
    }


    public void increase(int value) {
        locker.lock();

        try {
            capacityUnit = capacityUnit + value;
        } catch (Exception ex) {
        } finally {
            locker.unlock();
        }
    }

    public void decrease(int value) {
        locker.lock();

        try {
            capacityUnit = capacityUnit - value;
        } catch (Exception ex) {
        } finally {
            locker.unlock();
        }

    }

}
