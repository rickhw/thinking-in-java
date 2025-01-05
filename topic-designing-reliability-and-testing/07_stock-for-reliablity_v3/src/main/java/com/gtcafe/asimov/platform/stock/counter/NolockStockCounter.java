package com.gtcafe.asimov.platform.stock.counter;

import org.springframework.stereotype.Service;

import com.gtcafe.asimov.platform.stock.counter.excpetion.StockInsufficientException;
import com.gtcafe.asimov.platform.stock.counter.excpetion.StockResumingException;

@Service
public class NolockStockCounter implements IStockCounter {
    private int currentCounter = DEFAULT_MAX_COUNTER;

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
        if (value > currentCounter) {
            throw new StockInsufficientException("stock unit is insufficient: required=" + value + ", current=" + currentCounter);
        }
        currentCounter -= value;
    }

    @Override
    public void resume(int value) throws StockResumingException {
        int resumingValue = (value + currentCounter) ;

        if ( resumingValue > DEFAULT_MAX_COUNTER) {
            throw new StockResumingException("unexpected resuming value: resuming value=" + resumingValue + ", MAX=" + DEFAULT_MAX_COUNTER);
        }
        currentCounter += value;
    }

    public String toString() {
        return "NolockStockCounter: " + currentCounter;
    }
}
