package com.gtcafe.race.singleton.capacityUnit;

public interface ICapacityUnit {

    public int getValue();

    public void reset();

    public void operate(int value);

    public void increase(int value);

    public void decrease(int value);
}
