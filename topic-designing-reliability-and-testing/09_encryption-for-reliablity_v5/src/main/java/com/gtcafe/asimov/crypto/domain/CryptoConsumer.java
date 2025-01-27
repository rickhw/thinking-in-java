package com.gtcafe.asimov.crypto.domain;

import com.gtcafe.asimov.crypto.capacity.ICapacityUnit;
import com.gtcafe.asimov.crypto.capacity.exception.CapacityResumingException;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@Slf4j
public class CryptoConsumer extends Thread {

    private ICapacityUnit capacityUnit;
    private CryptoContext context;

    public CryptoConsumer(ICapacityUnit capacityUnit, CryptoContext context) {
        this.capacityUnit = capacityUnit;
        this.context = context;
    }

    @Override
    public void run() {
        try {
            // simulate processing time
            Thread.sleep(context.getProcessTime());

            // resume the capacity counter
            capacityUnit.resume(context.getConsumed());

            // 歸還 capacity
            context.setReturnedCapacity(CryptoContext.V__RETURNED);

        } catch (CapacityResumingException e) {
            context.setReturnedCapacity(CryptoContext.V__NOT_RETURNED);
            context.setAccepted(CryptoContext.V__REJECTED);
        } catch (InterruptedException e) {
            context.setReturnedCapacity(CryptoContext.V__NOT_RETURNED);
            context.setAccepted(CryptoContext.V__REJECTED);
            e.printStackTrace();
        }

        // write access log
        CryptoContext.updateContext(context);
        log.info("StockConsumer: {}", context);
    }
}
