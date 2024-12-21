package com.gtcafe.asimov.cache;


import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;

@Getter
public class CacheMetrics {
    private final AtomicLong hitCount = new AtomicLong();
    private final AtomicLong missCount = new AtomicLong();
    private final AtomicLong readCount = new AtomicLong();
    private final AtomicLong writeCount = new AtomicLong();
    private final AtomicLong readErrorCount = new AtomicLong();
    private final AtomicLong writeErrorCount = new AtomicLong();
    private final AtomicLong totalReadLatency = new AtomicLong();
    private final AtomicLong totalWriteLatency = new AtomicLong();

    public void incrementHitCount() {
        hitCount.incrementAndGet();
    }

    public void incrementMissCount() {
        missCount.incrementAndGet();
    }

    public void incrementReadCount() {
        readCount.incrementAndGet();
    }

    public void incrementWriteCount() {
        writeCount.incrementAndGet();
    }

    public void incrementReadErrorCount() {
        readErrorCount.incrementAndGet();
    }

    public void incrementWriteErrorCount() {
        writeErrorCount.incrementAndGet();
    }

    public void recordReadLatency(long latencyNanos) {
        totalReadLatency.addAndGet(latencyNanos);
    }

    public void recordWriteLatency(long latencyNanos) {
        totalWriteLatency.addAndGet(latencyNanos);
    }

    public double getHitRate() {
        long hits = hitCount.get();
        long total = hits + missCount.get();
        return total == 0 ? 0.0 : (double) hits / total;
    }

    public double getAverageReadLatency() {
        long reads = readCount.get();
        return reads == 0 ? 0.0 : (double) totalReadLatency.get() / reads / 1_000_000; // 轉換為毫秒
    }

    public double getAverageWriteLatency() {
        long writes = writeCount.get();
        return writes == 0 ? 0.0 : (double) totalWriteLatency.get() / writes / 1_000_000; // 轉換為毫秒
    }
}