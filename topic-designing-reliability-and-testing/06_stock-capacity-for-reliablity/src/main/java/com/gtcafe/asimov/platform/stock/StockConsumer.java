package com.gtcafe.asimov.platform.stock;

import org.slf4j.MDC;
import org.springframework.util.Assert;

import com.gtcafe.asimov.platform.stock.domain.ReentrantStockCounter;
import com.gtcafe.asimov.platform.stock.domain.excpetion.StockResumingException;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@Slf4j
public class StockConsumer extends Thread {

    private ReentrantStockCounter counter;
    private StockContext context;

    public StockConsumer(ReentrantStockCounter counter, StockContext context) {
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
        MDC.put("X-Request-Id", context.getRequestId());
        MDC.put("stock.consumed", Integer.toString(context.getConsumed()));
        MDC.put("stock.remaining.before", Integer.toString(context.getBeforeRemaining()));
        MDC.put("stock.remaining.after", Integer.toString(context.getAfterRemaining()));
        MDC.put("stock.isAccepted",Boolean.toString(context.isAccepted()));
        MDC.put("stock.processTime", Long.toString(context.getProcessTime()));
        MDC.put("stock.hasReturnedStock", Boolean.toString(context.isHasReturnedStock()));

        log.info("StockConsumer: {}", context);
    }
}
