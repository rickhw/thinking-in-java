package com.gtcafe.asimov.crypto.capacity;

import org.springframework.stereotype.Service;

import com.gtcafe.asimov.crypto.capacity.exception.CapacityInsufficientException;
import com.gtcafe.asimov.crypto.capacity.exception.CapacityResumingException;

@Service
public class NolockCapacityUnit implements ICapacityUnit {
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
    public void consume(int unit) throws CapacityInsufficientException {
        if (unit > remaining) {
            throw new CapacityInsufficientException("capacity unit is insufficient: required=" + unit + ", remaining=" + remaining);
        }
        remaining -= unit;
    }

    @Override
    public void resume(int resumedUnit) throws CapacityResumingException {
        int resumingUnit = (resumedUnit + remaining) ;

        if ( resumingUnit > DEFAULT_MAX_CAPACITY_UNIT) {
            throw new CapacityResumingException("unexpected resuming unit=" + resumingUnit + ", MAX=" + DEFAULT_MAX_CAPACITY_UNIT);
        }
        remaining += resumedUnit;
    }

    public String toString() {
        return "NolockCapacityUnit: totalUnit=" + remaining;
    }
}
