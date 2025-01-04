package com.gtcafe.asimov.platform.stock;

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
}