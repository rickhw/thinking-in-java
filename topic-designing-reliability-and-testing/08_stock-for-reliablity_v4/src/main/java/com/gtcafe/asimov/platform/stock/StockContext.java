package com.gtcafe.asimov.platform.stock;

import org.slf4j.MDC;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class StockContext  {

    private String requestId;
    private int consumed;
    private int beforeRemaining;
    private int afterRemaining;
    private boolean isAccepted;
    private long processTime;
    private boolean hasReturnedStock;

    public static void updateContext(StockContext context) {
        MDC.put("X-Request-Id", context.getRequestId());
        MDC.put("stock.consumed", Integer.toString(context.getConsumed()));
        MDC.put("stock.remaining.before", Integer.toString(context.getBeforeRemaining()));
        MDC.put("stock.remaining.after", Integer.toString(context.getAfterRemaining()));
        MDC.put("stock.isAccepted",Boolean.toString(context.isAccepted()));
        MDC.put("stock.processTime", Long.toString(context.getProcessTime()));
        MDC.put("stock.hasReturnedStock", Boolean.toString(context.isHasReturnedStock()));
    }
}