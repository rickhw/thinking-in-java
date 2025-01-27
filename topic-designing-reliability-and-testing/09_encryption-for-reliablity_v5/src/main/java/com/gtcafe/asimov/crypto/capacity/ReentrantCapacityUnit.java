package com.gtcafe.asimov.crypto.capacity;

import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import com.gtcafe.asimov.crypto.capacity.exception.CapacityInsufficientException;
import com.gtcafe.asimov.crypto.capacity.exception.CapacityResumingException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ReentrantCapacityUnit implements ICapacityUnit {
    private int remaining = DEFAULT_MAX_CAPACITY_UNIT;
    private ReentrantLock locker = new ReentrantLock();

    @Override
    public int remaining() {
        return this.remaining;
    }

    @Override
    public void reset() {
        this.remaining = DEFAULT_MAX_CAPACITY_UNIT;
    }

    @Override
    public void consume(int requiredUnit) throws CapacityInsufficientException {
        locker.lock();

        if (requiredUnit > remaining) {
            locker.unlock();    // free the lock before throwing exception, to avoid deadlock
            throw new CapacityInsufficientException("capacity unit is insufficient: required=" + requiredUnit + ", current=" + remaining);
        }

        try {
            remaining -= requiredUnit;
        } catch (Exception ex) {
        } finally {
            locker.unlock();
        }
    }

    @Override
    public void resume(int resumedUnit) throws CapacityResumingException {
        locker.lock();

        int resumingValue = (resumedUnit + remaining) ;

        if ( resumingValue > DEFAULT_MAX_CAPACITY_UNIT) {
            locker.unlock();    // free the lock before throwing exception, to avoid deadlock
            throw new CapacityResumingException("unexpected resuming value: resuming value=" + resumingValue + ", MAX=" + DEFAULT_MAX_CAPACITY_UNIT);
        }

        try {
            remaining += resumedUnit;
        } catch (Exception ex) {
        } finally {
            locker.unlock();
        }
    }

    public String toString() {
        return "ReentrantCapacityUnit: totalUnit=" + remaining;
    }
}
