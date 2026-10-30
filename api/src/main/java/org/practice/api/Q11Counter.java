package org.practice.api;

/**
 * Q11：高并发计数。实现必须使用 {@link java.util.concurrent.atomic.LongAdder}。
 */
public interface Q11Counter {

    void increment();

    void add(long delta);

    /**
     * 当前累计值（不要求线性一致快照）。
     */
    long sum();
}
