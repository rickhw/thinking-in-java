package com.gtcafe.asimov.crypto.domain;

import org.slf4j.MDC;

import com.gtcafe.asimov.crypto.CryptoConstants;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CryptoContext implements CryptoConstants {

    private String requestId;
    private int consumed;
    private int beforeRemaining;
    private int afterRemaining;
    private int accepted;           // 0: accepted, 1: rejected
    private long processTime;
    private int returnedCapacity;   // 0: returned, 1: not returned

    public static void updateContext(CryptoContext context) {
        MDC.put(CryptoConstants.X__REQUEST_ID, context.getRequestId());
        MDC.put(F__CAPACITY_CONSUMED, Integer.toString(context.getConsumed()));
        MDC.put(F__CAPACITY_REMAINING_BEFORE, Integer.toString(context.getBeforeRemaining()));
        MDC.put(F__CAPACITY_REMAINING_AFTER, Integer.toString(context.getAfterRemaining()));
        MDC.put(F__CAPACITY_ACCEPTED, Integer.toString(context.getAccepted()));
        MDC.put(F__CAPACITY_PROCESS_TIME, Long.toString(context.getProcessTime()));
        MDC.put(F__CAPACITY_RETURNED_CAPACITY, Integer.toString(context.getReturnedCapacity()));
    }
}