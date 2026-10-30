package org.practice.answers.alice.q03;

import org.practice.api.Q03ReadWriteLock;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Q03 答案：ReentrantLock + 两个 Condition 实现的写优先读写锁。
 *
 * <p>写优先策略：只要存在正在等待的写线程，新来的读线程也要排队。
 */
public final class Q03AnswerImpl implements Q03ReadWriteLock {

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition canRead = lock.newCondition();
    private final Condition canWrite = lock.newCondition();

    private int readers;
    private boolean writer;
    private int waitingWriters;

    @Override
    public void lockRead() {
        lock.lock();
        try {
            while (writer || waitingWriters > 0) {
                canRead.awaitUninterruptibly();
            }
            readers++;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void unlockRead() {
        lock.lock();
        try {
            readers--;
            if (readers == 0) {
                canWrite.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void lockWrite() {
        lock.lock();
        try {
            waitingWriters++;
            try {
                while (writer || readers > 0) {
                    canWrite.awaitUninterruptibly();
                }
                writer = true;
            } finally {
                waitingWriters--;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void unlockWrite() {
        lock.lock();
        try {
            writer = false;
            canWrite.signalAll();
            canRead.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
