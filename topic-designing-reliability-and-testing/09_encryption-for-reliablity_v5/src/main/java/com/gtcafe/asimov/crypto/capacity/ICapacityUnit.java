package com.gtcafe.asimov.crypto.capacity;

import com.gtcafe.asimov.crypto.capacity.exception.CapacityInsufficientException;
import com.gtcafe.asimov.crypto.capacity.exception.CapacityResumingException;

public interface ICapacityUnit {
    int DEFAULT_MAX_CAPACITY_UNIT = 40;

    int remaining();
    void reset();
    void consume(int value) throws CapacityInsufficientException;
    void resume(int value) throws CapacityResumingException;
}
