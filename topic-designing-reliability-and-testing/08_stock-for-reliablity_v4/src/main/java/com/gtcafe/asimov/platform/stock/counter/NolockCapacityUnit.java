package com.gtcafe.asimov.platform.stock.counter;

import org.springframework.stereotype.Service;

import com.gtcafe.asimov.platform.stock.counter.excpetion.CapacityInsufficientException;
import com.gtcafe.asimov.platform.stock.counter.excpetion.CapacityResumingException;

@Service
public class NolockCapacityUnit implements ICapacityUnit {
    private int capacityUnit = DEFAULT_MAX_CAPACITY_UNIT;

    @Override
    public int getValue() {
        return capacityUnit;
    }

    @Override
    public void reset() {
        capacityUnit = DEFAULT_MAX_CAPACITY_UNIT;
    }

    @Override
    public void consume(int value) throws CapacityInsufficientException {
        if (value > capacityUnit) {
            throw new CapacityInsufficientException("capacity unit is insufficient: required=" + value + ", current=" + capacityUnit);
        }
        capacityUnit -= value;
    }

    @Override
    public void resume(int value) throws CapacityResumingException {
        int resumingValue = (value + capacityUnit) ;

        if ( resumingValue > DEFAULT_MAX_CAPACITY_UNIT) {
            throw new CapacityResumingException("unexpected resuming value: resuming value=" + resumingValue + ", MAX=" + DEFAULT_MAX_CAPACITY_UNIT);
        }
        capacityUnit += value;
    }

    public String toString() {
        return "NolockCapacityUnit: capacityUnit=" + capacityUnit;
    }
}
