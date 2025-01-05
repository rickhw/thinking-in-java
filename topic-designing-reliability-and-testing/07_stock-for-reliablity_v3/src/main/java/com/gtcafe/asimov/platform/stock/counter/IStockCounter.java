package com.gtcafe.asimov.platform.stock.counter;

import com.gtcafe.asimov.platform.stock.counter.excpetion.StockInsufficientException;
import com.gtcafe.asimov.platform.stock.counter.excpetion.StockResumingException;

public interface IStockCounter {
    int DEFAULT_MAX_COUNTER = 1000;

    int getValue();
    void reset();

    void consume(int value) throws StockInsufficientException;
    void resume(int value) throws StockResumingException;

}
