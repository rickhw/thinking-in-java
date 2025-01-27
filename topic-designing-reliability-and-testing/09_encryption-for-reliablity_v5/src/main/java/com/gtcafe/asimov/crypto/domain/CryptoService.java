package com.gtcafe.asimov.crypto.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gtcafe.asimov.crypto.capacity.ICapacityUnit;
import com.gtcafe.asimov.crypto.capacity.exception.CapacityInsufficientException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CryptoService {

    @Autowired
    private ICapacityUnit capacityUnit;

    public void acquire(CryptoContext context) {
        try {
            context.setBeforeRemaining(capacityUnit.remaining());

            capacityUnit.consume(context.getConsumed());

            context.setAccepted(CryptoContext.V__ACCEPTED);
            context.setAfterRemaining(capacityUnit.remaining());

            CryptoContext.updateContext(context);
            log.info("StockRequest: {}", context);

        } catch (CapacityInsufficientException e) {
            context.setAccepted(CryptoContext.V__REJECTED);
            context.setAfterRemaining(capacityUnit.remaining());

            CryptoContext.updateContext(context);
            log.error("StockInsufficientException: {}", context);
            return;
        }

        CryptoConsumer consumer = new CryptoConsumer(capacityUnit, context);
        consumer.start();
    }
}
