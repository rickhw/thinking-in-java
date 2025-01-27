package com.gtcafe.asimov.platform.stock.domain;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.gtcafe.asimov.platform.stock.counter.ICapacityUnit;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StockScheduledService {
    
    @Autowired
    private ICapacityUnit capacityUnit;

    // 固定每 1000 毫秒執行一次
    @Scheduled(fixedRate = 1000) 
    public void executeTaskEverySecond() {
        performTask();
    }

    // log capacity unit every second
    private void performTask() {
        MDC.put("stock.counter", Integer.toString(capacityUnit.getRemaining()));
        log.info(capacityUnit.toString());
    }
}
