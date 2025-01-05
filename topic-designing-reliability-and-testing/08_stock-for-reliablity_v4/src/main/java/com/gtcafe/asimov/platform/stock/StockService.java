package com.gtcafe.asimov.platform.stock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gtcafe.asimov.platform.stock.counter.ICapacityUnit;
import com.gtcafe.asimov.platform.stock.counter.excpetion.CapacityInsufficientException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StockService {

    @Autowired
    private ICapacityUnit capacityUnit;

    public void acquire(StockContext context) {
        try {
            context.setBeforeRemaining(capacityUnit.getValue());

            capacityUnit.consume(context.getConsumed());

            context.setAccepted(true);
            context.setAfterRemaining(capacityUnit.getValue());

            StockContext.updateContext(context);
            log.info("StockRequest: {}", context);

        } catch (CapacityInsufficientException e) {
            context.setAccepted(false);
            context.setAfterRemaining(capacityUnit.getValue());

            StockContext.updateContext(context);
            log.error("StockInsufficientException: {}", context);
            return;
        }

        StockConsumer consumer = new StockConsumer(capacityUnit, context);
        consumer.start();
    }
}
