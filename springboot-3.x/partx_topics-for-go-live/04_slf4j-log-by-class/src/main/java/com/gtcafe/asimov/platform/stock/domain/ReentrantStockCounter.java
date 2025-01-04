package com.gtcafe.asimov.platform.stock.domain;

import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

@Service
public class ReentrantStockCounter {
    public static final int DEFAULT_STOCK_COUNT = 1000;
    private int stockCount = 1000;
    private ReentrantLock locker = new ReentrantLock();

    public int getValue() {
        return stockCount;
    }

    public void reset() {
        stockCount = DEFAULT_STOCK_COUNT;
    }

    public void consume(int value) throws StockInsufficientException {
        locker.lock();

        if (value > stockCount) {
            locker.unlock();    // free the lock before throwing exception, to avoid deadlock
            throw new StockInsufficientException("stock unit is insufficient: required=" + value + ", current=" + stockCount);
        }

        try {
            stockCount -= value;
        } catch (Exception ex) {
        } finally {
            locker.unlock();
        }
    }

    public void resume(int value) {
        locker.lock();

        try {
            stockCount += value;
        } catch (Exception ex) {
        } finally {
            locker.unlock();
        }
    }
}
