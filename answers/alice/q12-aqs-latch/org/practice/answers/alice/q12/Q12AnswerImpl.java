package org.practice.answers.alice.q12;

import org.practice.api.Q12Latch;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * Q12 答案：继承 AQS，用共享模式实现一次性倒计数门闩。
 */
public final class Q12AnswerImpl extends AbstractQueuedSynchronizer implements Q12Latch {

    public Q12AnswerImpl(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count 不能为负");
        }
        setState(count);
    }

    @Override
    public int getCount() {
        return getState();
    }

    @Override
    public void countDown() {
        releaseShared(1);
    }

    @Override
    public void await() throws InterruptedException {
        acquireSharedInterruptibly(1);
    }

    @Override
    public boolean await(long timeoutMillis) throws InterruptedException {
        return tryAcquireSharedNanos(1, TimeUnit.MILLISECONDS.toNanos(timeoutMillis));
    }

    /**
     * state == 0 时返回 1（成功且可唤醒后继共享线程），否则返回 -1（排队等待）。
     */
    @Override
    protected int tryAcquireShared(int arg) {
        return getState() == 0 ? 1 : -1;
    }

    @Override
    protected boolean tryReleaseShared(int arg) {
        while (true) {
            int count = getState();
            if (count == 0) {
                return false;
            }
            int next = count - 1;
            if (compareAndSetState(count, next)) {
                return next == 0;
            }
        }
    }
}
