package com.gtcafe.asimov.platform.stock.domain;

import com.gtcafe.asimov.platform.stock.domain.excpetion.StockInsufficientException;
import com.gtcafe.asimov.platform.stock.domain.excpetion.StockResumingException;

public interface IStockCounter {
    int DEFAULT_MAX_COUNTER = 1000;

    int getValue();
    void reset();

    void consume(int value) throws StockInsufficientException;
    void resume(int value) throws StockResumingException;

}
