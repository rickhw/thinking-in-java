package com.beginnertechies.googleapi.bean;

import java.util.concurrent.locks.ReentrantLock;

public class CapacityUnit { 

    private int capacityUnit = 0;
    private ReentrantLock locker = new ReentrantLock();
  
    public CapacityUnit() {
        System.out.printf("Init counter, value: [%s]\n", this.getValue());
    }

    public int getValue() {
        return capacityUnit;
    }

    public void operate(int value) {
        locker.lock();


        try {
            capacityUnit += value;
        } catch (Exception ex) {
            // ...
        } finally {
            // 解锁操作
            locker.unlock();
        }
        
        System.out.printf("After operate: Thread: [%s], Value: [%s]\n", Thread.currentThread().getName(),  this.getValue());
    }



}
