package com.gtcafe.race.bean;

import java.util.concurrent.locks.ReentrantLock;

public interface ICapacityUnit {

    public int getValue();
    public void reset();
    public void operate(int value);
}
