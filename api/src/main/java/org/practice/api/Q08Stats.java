package org.practice.api;

/**
 * Q08 压测结果。
 */
public final class Q08Stats {

    private final long totalRequests;
    private final long successes;
    private final long failures;
    private final long durationMillis;

    public Q08Stats(long totalRequests, long successes, long failures, long durationMillis) {
        this.totalRequests = totalRequests;
        this.successes = successes;
        this.failures = failures;
        this.durationMillis = durationMillis;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public long getSuccesses() {
        return successes;
    }

    public long getFailures() {
        return failures;
    }

    public long getDurationMillis() {
        return durationMillis;
    }
}
