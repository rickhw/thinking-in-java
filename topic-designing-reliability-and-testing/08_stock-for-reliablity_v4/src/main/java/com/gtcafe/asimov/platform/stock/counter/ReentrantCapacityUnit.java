package com.gtcafe.asimov.platform.stock.counter;

import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import com.gtcafe.asimov.platform.stock.counter.excpetion.CapacityInsufficientException;
import com.gtcafe.asimov.platform.stock.counter.excpetion.CapacityResumingException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ReentrantCapacityUnit implements ICapacityUnit {
    private int capacityUnit = DEFAULT_MAX_CAPACITY_UNIT;
    private ReentrantLock locker = new ReentrantLock();

    @Override
    public int getRemaining() {
        return capacityUnit;
    }

    @Override
    public void reset() {
        capacityUnit = DEFAULT_MAX_CAPACITY_UNIT;
    }

    @Override
    public void consume(int value) throws CapacityInsufficientException {
        locker.lock();

        if (value > capacityUnit) {
            locker.unlock();    // free the lock before throwing exception, to avoid deadlock
            throw new CapacityInsufficientException("capacity unit is insufficient: required=" + value + ", current=" + capacityUnit);
        }

        try {
            capacityUnit -= value;
        } catch (Exception ex) {
        } finally {
            locker.unlock();
        }
    }

    @Override
    public void resume(int value) throws CapacityResumingException {
        locker.lock();

        int resumingValue = (value + capacityUnit) ;

        if ( resumingValue > DEFAULT_MAX_CAPACITY_UNIT) {
            locker.unlock();    // free the lock before throwing exception, to avoid deadlock
            throw new CapacityResumingException("unexpected resuming value: resuming value=" + resumingValue + ", MAX=" + DEFAULT_MAX_CAPACITY_UNIT);
        }

        try {
            capacityUnit += value;
        } catch (Exception ex) {
        } finally {
            locker.unlock();
        }
    }

    public String toString() {
        return "ReentrantCapacityUnit: capacityUnit=" + capacityUnit;
    }
}
