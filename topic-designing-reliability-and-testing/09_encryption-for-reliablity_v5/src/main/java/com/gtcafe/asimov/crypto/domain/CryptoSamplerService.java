package com.gtcafe.asimov.crypto.domain;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.gtcafe.asimov.crypto.CryptoConstants;
import com.gtcafe.asimov.crypto.capacity.ICapacityUnit;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CryptoSamplerService {
    
    @Autowired
    private ICapacityUnit capacityUnit;

    @Scheduled(fixedRate = 1000) 
    public void executeTaskEverySecond() {
        MDC.put(CryptoConstants.F__CAPACITY_REMAINING, Integer.toString(capacityUnit.remaining()));
        log.info(capacityUnit.toString());
    }
}
