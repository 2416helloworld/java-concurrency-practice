package org.practice.judge;

import org.junit.jupiter.api.Test;
import org.practice.api.Q02Deadlock;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Q02DeadlockTest {

    private static final ThreadMXBean MX_BEAN = ManagementFactory.getThreadMXBean();

    @Test
    void oppositeLockOrderReproducesDeadlock() throws Exception {
        Q02Deadlock answer = Answers.newAnswer("q02", "Q02AnswerImpl", Q02Deadlock.class);
        ReentrantLock a = new ReentrantLock();
        ReentrantLock b = new ReentrantLock();
        CyclicBarrier bothFirstLocked = new CyclicBarrier(2);
        Runnable afterFirstLock = () -> {
            try {
                bothFirstLocked.await(2, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        };

        Thread t1 = TestSupport.startDaemon("q02-deadlock-t1", () -> invokeLock(() -> answer.lockAToB(a, b, afterFirstLock)));
        Thread t2 = TestSupport.startDaemon("q02-deadlock-t2", () -> invokeLock(() -> answer.lockBToA(a, b, afterFirstLock)));

        long[] deadlocked = null;
        long deadline = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < deadline) {
            long[] ids = MX_BEAN.findDeadlockedThreads();
            if (ids != null && ids.length >= 2) {
                deadlocked = ids;
                break;
            }
            Thread.sleep(20);
        }
        assertNotNull(deadlocked, "相反加锁顺序应能复现死锁");

        // 清理：中断后 lockInterruptibly 抛出异常，finally 会释放第一把锁
        t1.interrupt();
        t2.interrupt();
        t1.join(1000);
        t2.join(1000);
    }

    @Test
    void fixedOrderDoesNotDeadlock() throws Exception {
        Q02Deadlock answer = Answers.newAnswer("q02", "Q02AnswerImpl", Q02Deadlock.class);
        ReentrantLock a = new ReentrantLock();
        ReentrantLock b = new ReentrantLock();
        AtomicBoolean failed = new AtomicBoolean(false);
        Runnable body = () -> {
            for (int i = 0; i < 2000; i++) {
                if (!answer.lockInFixedOrder(a, b)) {
                    failed.set(true);
                    return;
                }
            }
        };

        Thread t1 = TestSupport.startDaemon("q02-fixed-t1", body);
        Thread t2 = TestSupport.startDaemon("q02-fixed-t2", body);
        t1.join(3000);
        t2.join(3000);

        assertFalse(t1.isAlive() || t2.isAlive(), "固定加锁顺序不应死锁");
        assertFalse(failed.get(), "固定顺序加锁应始终成功");
    }

    @Test
    void tryLockWithTimeoutDoesNotDeadlock() throws Exception {
        Q02Deadlock answer = Answers.newAnswer("q02", "Q02AnswerImpl", Q02Deadlock.class);
        ReentrantLock a = new ReentrantLock();
        ReentrantLock b = new ReentrantLock();
        CountDownLatch held = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        // 用另一个线程持有 a，避免 ReentrantLock 的可重入让当前线程直接 tryLock 成功
        Thread holder = TestSupport.startDaemon("q02-lock-holder", () -> {
            a.lock();
            try {
                held.countDown();
                TestSupport.await(release, 2000);
            } finally {
                a.unlock();
            }
        });
        assertTrue(TestSupport.await(held, 1000));

        long start = System.nanoTime();
        boolean acquired = answer.tryLockBoth(b, a, 500);
        long elapsed = (System.nanoTime() - start) / 1_000_000;

        assertFalse(acquired, "第二把锁超时应返回 false");
        assertTrue(elapsed >= 400, "tryLock 应等到超时，实际 " + elapsed + "ms");
        assertFalse(b.isLocked(), "超时后必须释放已持有的第一把锁");
        assertTrue(a.isLocked(), "a 仍应被持有线程占用");

        release.countDown();
        holder.join(1000);
        assertFalse(a.isLocked(), "持有线程应已释放 a");

        assertTrue(answer.tryLockBoth(a, b, 500), "无竞争时应成功");
        assertFalse(a.isLocked(), "成功后应释放 a");
        assertFalse(b.isLocked(), "成功后应释放 b");
    }

    private interface LockAction {
        void run() throws InterruptedException;
    }

    private static void invokeLock(LockAction action) {
        try {
            action.run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException ignored) {
            // 屏障被打破等异常直接结束线程
        }
    }
}
