package com.gtcafe.asimov.platform.stock;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gtcafe.asimov.platform.stock.counter.ReentrantStockCounter;
import com.gtcafe.asimov.platform.stock.counter.excpetion.StockInsufficientException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StockService {

    @Autowired
    private ReentrantStockCounter counter;

    public void acquire(StockContext context) {
        try {
            context.setBeforeRemaining(counter.getValue());

            counter.consume(context.getConsumed());

            context.setAccepted(true);
            context.setAfterRemaining(counter.getValue());

            // log.info("StockRequest: {}", context);

        } catch (StockInsufficientException e) {
            context.setAccepted(false);
            context.setAfterRemaining(counter.getValue());

            // write access log
            MDC.put("X-Request-Id", context.getRequestId());
            MDC.put("stock.consumed", Integer.toString(context.getConsumed()));
            MDC.put("stock.remaining.before", Integer.toString(context.getBeforeRemaining()));
            MDC.put("stock.remaining.after", Integer.toString(context.getAfterRemaining()));
            MDC.put("stock.isAccepted",Boolean.toString(context.isAccepted()));
            MDC.put("stock.processTime", Long.toString(context.getProcessTime()));
            MDC.put("stock.hasReturnedStock", Boolean.toString(context.isHasReturnedStock()));

            log.error("StockInsufficientException: {}", context);
            return;
        }

        StockConsumer handler = new StockConsumer(counter, context);
        handler.start();
    }
}
