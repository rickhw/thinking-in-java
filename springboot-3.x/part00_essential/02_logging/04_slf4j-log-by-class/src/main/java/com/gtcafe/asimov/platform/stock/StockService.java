package com.gtcafe.asimov.platform.stock;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gtcafe.asimov.platform.stock.domain.ReentrantStockCounter;
import com.gtcafe.asimov.platform.stock.domain.StockInsufficientException;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StockService {

    @Autowired
    private ReentrantStockCounter cu;

    @Autowired
    private MeterRegistry meterRegistry;

    public void acquire(int unit) {
        

        try {
            cu.consume(unit);

            // 更新 metrics: consumed 和剩餘的 stock
            meterRegistry.counter("stock.consumed").increment(unit);
            meterRegistry.gauge("stock.remaining", cu, ReentrantStockCounter::getValue);

            MDC.put("stockRemaining", String.valueOf(cu.getValue()));
            MDC.put("stockConsumed", String.valueOf(unit));

            log.info("acquire: [{}], remaining: [{}]", unit, cu.getValue());

        } catch (StockInsufficientException e) {
            log.error("stock insufficient: {}", e.getMessage());
            return;
        }

        // async
        StockConsumer handler = new StockConsumer(cu, unit, meterRegistry);
        handler.start();
    }
}
