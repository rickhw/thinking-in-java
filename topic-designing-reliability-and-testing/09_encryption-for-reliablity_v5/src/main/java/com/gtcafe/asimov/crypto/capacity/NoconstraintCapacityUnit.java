package com.gtcafe.asimov.crypto.capacity;

import org.springframework.stereotype.Service;

import com.gtcafe.asimov.crypto.capacity.exception.CapacityInsufficientException;
import com.gtcafe.asimov.crypto.capacity.exception.CapacityResumingException;

@Service
public class NoconstraintCapacityUnit implements ICapacityUnit {
    private int remaining = DEFAULT_MAX_CAPACITY_UNIT;

    @Override
    public int remaining() {
        return this.remaining;
    }

    @Override
    public void reset() {
        remaining = DEFAULT_MAX_CAPACITY_UNIT;
    }

    @Override
    public void consume(int consumedUnit) throws CapacityInsufficientException {
        if (consumedUnit > remaining) {
            throw new CapacityInsufficientException("capacity unit is insufficient: required=" + consumedUnit + ", current=" + remaining);
        }
        remaining -= consumedUnit;
    }

    @Override
    public void resume(int resumedUnit) throws CapacityResumingException {
        int resumingValue = (resumedUnit + remaining) ;

        if ( resumingValue > DEFAULT_MAX_CAPACITY_UNIT) {
            throw new CapacityResumingException("unexpected resuming value: resuming value=" + resumingValue + ", MAX=" + DEFAULT_MAX_CAPACITY_UNIT);
        }
        remaining += resumedUnit;
    }

    public String toString() {
        return "NoconstraintCapacityUnit: capacityUnit=" + remaining;
    }
}
