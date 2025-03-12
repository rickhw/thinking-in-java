package com.gtcafe.asimov.platform.stock.domain;

import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import com.gtcafe.asimov.platform.stock.domain.excpetion.StockInsufficientException;
import com.gtcafe.asimov.platform.stock.domain.excpetion.StockResumingException;

@Service
public class ReentrantStockCounter implements IStockCounter {
    private int currentCounter = DEFAULT_MAX_COUNTER;
    private ReentrantLock locker = new ReentrantLock();

    @Override
    public int getValue() {
        return currentCounter;
    }

    @Override
    public void reset() {
        currentCounter = DEFAULT_MAX_COUNTER;
    }

    @Override
    public void consume(int value) throws StockInsufficientException {
        locker.lock();

        if (value > currentCounter) {
            locker.unlock();    // free the lock before throwing exception, to avoid deadlock
            throw new StockInsufficientException("stock unit is insufficient: required=" + value + ", current=" + currentCounter);
        }

        try {
            currentCounter -= value;
        } catch (Exception ex) {
        } finally {
            locker.unlock();
        }
    }

    @Override
    public void resume(int value) throws StockResumingException {
        locker.lock();

        int resumingValue = (value + currentCounter) ;

        if ( resumingValue > DEFAULT_MAX_COUNTER) {
            locker.unlock();    // free the lock before throwing exception, to avoid deadlock
            throw new StockResumingException("unexpected resuming value: resuming value=" + resumingValue + ", MAX=" + DEFAULT_MAX_COUNTER);
        }

        try {
            currentCounter += value;
        } catch (Exception ex) {
        } finally {
            locker.unlock();
        }
    }

    public String toString() {
        return "ReentrantStockCounter [currentCounter=" + currentCounter + "]";
    }
}
