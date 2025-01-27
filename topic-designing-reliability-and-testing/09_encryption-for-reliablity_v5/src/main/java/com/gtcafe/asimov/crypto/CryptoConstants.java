package com.gtcafe.asimov.crypto;

public interface CryptoConstants {
    String X__REQUEST_ID = "X-Request-Id";

    // Fields
    String F__CAPACITY_CONSUMED = "capacity.consumed";
    String F__CAPACITY_REMAINING_BEFORE = "capacity.remaining.before";
    String F__CAPACITY_REMAINING_AFTER = "capacity.remaining.after";
    String F__CAPACITY_REMAINING = "capacity.remaining";
    String F__CAPACITY_ACCEPTED = "capacity.accepted";
    String F__CAPACITY_PROCESS_TIME = "capacity.processTime";
    String F__CAPACITY_RETURNED_CAPACITY = "capacity.returnedCapacity";

    // Values for F__CAPACITY_ACCEPTED
    int V__ACCEPTED = 0;
    int V__REJECTED = 1;

    // Values for F__CAPACITY_RETURNED_CAPACITY
    int V__RETURNED = 0;
    int V__NOT_RETURNED = 1;
    
}
