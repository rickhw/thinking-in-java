package com.gtcafe.asimov.platform.capacity;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gtcafe.asimov.platform.capacity.domain.CapacityInsufficientException;
import com.gtcafe.asimov.platform.capacity.domain.ReentrantCapacityUnitCounter;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CapacityService {

    @Autowired
    private ReentrantCapacityUnitCounter cu;

    @Autowired
    private MeterRegistry meterRegistry;

    public void acquire(int unit) {
        

        try {
            cu.consume(unit);

            // 更新 metrics: consumed 和剩餘的 capacity
            meterRegistry.counter("capacity.consumed").increment(unit);
            meterRegistry.gauge("capacity.remaining", cu, ReentrantCapacityUnitCounter::getValue);

            MDC.put("capacityRemaining", String.valueOf(cu.getValue()));
            MDC.put("capacityConsumed", String.valueOf(unit));

            log.info("acquire: [{}], remaining: [{}]", unit, cu.getValue());

        } catch (CapacityInsufficientException e) {
            log.error("capacity insufficient: {}", e.getMessage());
            return;
        }

        // async
        CapacityConsumer handler = new CapacityConsumer(cu, unit, meterRegistry);
        handler.start();
    }
}
