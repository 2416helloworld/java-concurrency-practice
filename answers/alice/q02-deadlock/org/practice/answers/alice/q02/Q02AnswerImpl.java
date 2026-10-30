package org.practice.answers.alice.q02;

import org.practice.api.Q02Deadlock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Q02 答案：复现死锁 + 固定顺序 / tryLock 超时两种破除方式。
 */
public final class Q02AnswerImpl implements Q02Deadlock {

    /** 拿到两把锁后停留在这里，直到线程被中断。 */
    private final CountDownLatch holdForever = new CountDownLatch(1);

    @Override
    public void lockAToB(Lock a, Lock b, Runnable afterFirstLock) throws InterruptedException {
        lockThenHold(a, b, afterFirstLock);
    }

    @Override
    public void lockBToA(Lock a, Lock b, Runnable afterFirstLock) throws InterruptedException {
        lockThenHold(b, a, afterFirstLock);
    }

    private void lockThenHold(Lock first, Lock second, Runnable afterFirstLock) throws InterruptedException {
        first.lockInterruptibly();
        try {
            afterFirstLock.run();
            second.lockInterruptibly();
            try {
                holdForever.await();
            } finally {
                second.unlock();
            }
        } finally {
            first.unlock();
        }
    }

    @Override
    public boolean lockInFixedOrder(Lock a, Lock b) {
        Lock first = canonicalFirst(a, b);
        Lock second = (first == a) ? b : a;
        first.lock();
        try {
            second.lock();
            try {
                return true;
            } finally {
                second.unlock();
            }
        } finally {
            first.unlock();
        }
    }

    @Override
    public boolean tryLockBoth(Lock a, Lock b, long timeoutMillis) throws InterruptedException {
        boolean firstAcquired = false;
        boolean secondAcquired = false;
        try {
            if (!a.tryLock(timeoutMillis, TimeUnit.MILLISECONDS)) {
                return false;
            }
            firstAcquired = true;
            if (!b.tryLock(timeoutMillis, TimeUnit.MILLISECONDS)) {
                return false;
            }
            secondAcquired = true;
            return true;
        } finally {
            if (secondAcquired) {
                b.unlock();
            }
            if (firstAcquired) {
                a.unlock();
            }
        }
    }

    private static Lock canonicalFirst(Lock a, Lock b) {
        int cmp = Integer.compare(System.identityHashCode(a), System.identityHashCode(b));
        return cmp <= 0 ? a : b;
    }
}
