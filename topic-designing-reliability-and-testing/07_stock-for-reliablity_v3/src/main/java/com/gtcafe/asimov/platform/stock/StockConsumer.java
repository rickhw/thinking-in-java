package com.gtcafe.asimov.platform.stock;

import com.gtcafe.asimov.platform.stock.counter.IStockCounter;
import com.gtcafe.asimov.platform.stock.counter.ReentrantStockCounter;
import com.gtcafe.asimov.platform.stock.counter.excpetion.StockResumingException;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@Slf4j
public class StockConsumer extends Thread {

    private IStockCounter counter;
    private StockContext context;

    public StockConsumer(IStockCounter counter, StockContext context) {
        this.counter = counter;
        this.context = context;
    }

    @Override
    public void run() {
        try {
            // simulate processing time
            Thread.sleep(context.getProcessTime());

            // resume the stock counter
            counter.resume(context.getConsumed());

            // 歸還 stock
            context.setHasReturnedStock(true);

        } catch (StockResumingException e) {
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
