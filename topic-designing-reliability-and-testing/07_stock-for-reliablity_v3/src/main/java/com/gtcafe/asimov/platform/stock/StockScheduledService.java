package com.gtcafe.asimov.platform.stock;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.gtcafe.asimov.platform.stock.counter.IStockCounter;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StockScheduledService {
    
    @Autowired
    private IStockCounter stockCounter;

    // 固定每 1000 毫秒執行一次
    @Scheduled(fixedRate = 1000) 
    public void executeTaskEverySecond() {
        performTask();
    }

    // log stock counter every second
    private void performTask() {
        MDC.put("stock.counter", Integer.toString(stockCounter.getValue()));
        log.info(stockCounter.toString());
    }
}
