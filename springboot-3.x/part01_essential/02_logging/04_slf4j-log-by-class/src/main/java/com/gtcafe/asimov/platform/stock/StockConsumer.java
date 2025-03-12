package com.gtcafe.asimov.platform.stock;

import org.slf4j.MDC;

import com.gtcafe.asimov.platform.stock.domain.ReentrantStockCounter;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@Slf4j
public class StockConsumer extends Thread {

    private ReentrantStockCounter cu;
    private int unit;
    private MeterRegistry meterRegistry;

    public StockConsumer(ReentrantStockCounter cu, int unit, MeterRegistry meterRegistry) {
        this.cu = cu;
        this.unit = unit;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void run() {
        try {
            long processTime = (long) (Math.random() * 10000);
            // log.info("process time: [{}ms]", processTime);
            Thread.sleep(processTime);

            cu.resume(unit);

            // 更新 metrics: 剩餘的 stock
            meterRegistry.gauge("stock.remaining", cu, ReentrantStockCounter::getValue);

            // after resume, the consumed value should be updated
            MDC.put("stockRemaining", String.valueOf(cu.getValue()));
            MDC.put("stockConsumed", String.valueOf(unit));


            log.info("resumed: [{}], remaining: [{}], processTime: [{}ms]", unit, cu.getValue(), processTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
