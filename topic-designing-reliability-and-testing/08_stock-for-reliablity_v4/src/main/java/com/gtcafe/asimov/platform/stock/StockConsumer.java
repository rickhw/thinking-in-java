package com.gtcafe.asimov.platform.stock;

import com.gtcafe.asimov.platform.stock.counter.ICapacityUnit;
import com.gtcafe.asimov.platform.stock.counter.excpetion.CapacityResumingException;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@Slf4j
public class StockConsumer extends Thread {

    private ICapacityUnit capacityUnit;
    private StockContext context;

    public StockConsumer(ICapacityUnit capacityUnit, StockContext context) {
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
            context.setHasReturnedStock(true);

        } catch (CapacityResumingException e) {
            // Assert.isTrue(isAlive(), null);
            context.setHasReturnedStock(false);
            context.setAccepted(false);

            // e.printStackTrace();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // write access log
        StockContext.updateContext(context);
        log.info("StockConsumer: {}", context);
    }
}
