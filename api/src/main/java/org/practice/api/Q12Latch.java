package org.practice.api;

/**
 * Q12：基于 AQS 共享模式实现的一次性倒计数门闩。
 */
public interface Q12Latch {

    int getCount();

    void countDown();

    /**
     * 等待计数归零。
     */
    void await() throws InterruptedException;

    /**
     * 等待计数归零，最多等待 timeoutMillis；归零返回 true，超时返回 false。
     */
    boolean await(long timeoutMillis) throws InterruptedException;
}
