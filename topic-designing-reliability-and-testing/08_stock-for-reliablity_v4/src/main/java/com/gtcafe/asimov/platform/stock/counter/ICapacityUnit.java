package com.gtcafe.asimov.platform.stock.counter;

import com.gtcafe.asimov.platform.stock.counter.excpetion.CapacityInsufficientException;
import com.gtcafe.asimov.platform.stock.counter.excpetion.CapacityResumingException;

public interface ICapacityUnit {
    int DEFAULT_MAX_CAPACITY_UNIT = 40;

    int getRemaining();
    void reset();
    void consume(int value) throws CapacityInsufficientException;
    void resume(int value) throws CapacityResumingException;
}
