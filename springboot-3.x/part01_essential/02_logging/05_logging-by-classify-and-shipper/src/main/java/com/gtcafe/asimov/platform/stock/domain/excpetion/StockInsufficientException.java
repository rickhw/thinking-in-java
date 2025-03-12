package com.gtcafe.asimov.platform.stock.domain.excpetion;

public class StockInsufficientException extends Exception {
    public StockInsufficientException(String message) {
        super(message);
    }
    
}
