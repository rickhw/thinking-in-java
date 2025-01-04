package com.gtcafe.asimov.platform.stock.counter.excpetion;

public class StockInsufficientException extends Exception {
    public StockInsufficientException(String message) {
        super(message);
    }
    
}
