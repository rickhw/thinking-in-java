package com.gtcafe.asimov.platform.stock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gtcafe.asimov.platform.stock.counter.IStockCounter;
import com.gtcafe.asimov.platform.stock.counter.excpetion.StockInsufficientException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StockService {

    @Autowired
    private IStockCounter stockCounter;

    public void acquire(StockContext context) {
        try {
            context.setBeforeRemaining(stockCounter.getValue());

            stockCounter.consume(context.getConsumed());

            context.setAccepted(true);
            context.setAfterRemaining(stockCounter.getValue());

            StockContext.updateContext(context);
            log.info("StockRequest: {}", context);

        } catch (StockInsufficientException e) {
            context.setAccepted(false);
            context.setAfterRemaining(stockCounter.getValue());

            StockContext.updateContext(context);
            log.error("StockInsufficientException: {}", context);
            return;
        }

        StockConsumer handler = new StockConsumer(stockCounter, context);
        handler.start();
    }
}
