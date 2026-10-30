package org.practice.answers.alice.q09;

import org.practice.api.Q09BlockingQueue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Q09 答案：ReentrantLock + notFull / notEmpty 两个 Condition 的有界阻塞队列。
 */
public final class Q09AnswerImpl<E> implements Q09BlockingQueue<E> {

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();
    private final Object[] items;
    private int count;
    private int putIndex;
    private int takeIndex;

    public Q09AnswerImpl(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity 必须为正数");
        }
        this.items = new Object[capacity];
    }

    @Override
    public void put(E element) throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (count == items.length) {
                notFull.await();
            }
            enqueue(element);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public E take() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (count == 0) {
                notEmpty.await();
            }
            return dequeue();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean offer(E element, long timeoutMillis) throws InterruptedException {
        long nanos = TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        lock.lockInterruptibly();
        try {
            while (count == items.length) {
                if (nanos <= 0) {
                    return false;
                }
                nanos = notFull.awaitNanos(nanos);
            }
            enqueue(element);
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public E poll(long timeoutMillis) throws InterruptedException {
        long nanos = TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        lock.lockInterruptibly();
        try {
            while (count == 0) {
                if (nanos <= 0) {
                    return null;
                }
                nanos = notEmpty.awaitNanos(nanos);
            }
            return dequeue();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }

    private void enqueue(E element) {
        items[putIndex] = element;
        putIndex = (putIndex + 1) % items.length;
        count++;
        notEmpty.signal();
    }

    @SuppressWarnings("unchecked")
    private E dequeue() {
        E element = (E) items[takeIndex];
        items[takeIndex] = null;
        takeIndex = (takeIndex + 1) % items.length;
        count--;
        notFull.signal();
        return element;
    }
}
