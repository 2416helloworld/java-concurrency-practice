package org.practice.api;

/**
 * Q09：基于 ReentrantLock + Condition 手写的有界阻塞队列。
 */
public interface Q09BlockingQueue<E> {

    /**
     * 队列满时阻塞，直到有空间。
     */
    void put(E element) throws InterruptedException;

    /**
     * 队列空时阻塞，直到有元素。
     */
    E take() throws InterruptedException;

    /**
     * 队列满时最多等待 timeoutMillis，仍无空间返回 false。
     */
    boolean offer(E element, long timeoutMillis) throws InterruptedException;

    /**
     * 队列空时最多等待 timeoutMillis，仍无元素返回 null。
     */
    E poll(long timeoutMillis) throws InterruptedException;

    int size();
}
