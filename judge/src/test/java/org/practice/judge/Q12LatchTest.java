package org.practice.judge;

import org.junit.jupiter.api.Test;
import org.practice.api.Q12Latch;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Q12LatchTest {

    @Test
    void releasesAllWaitersWhenCountReachesZero() {
        Q12Latch latch = Answers.newAnswer("q12", "Q12AnswerImpl", Q12Latch.class,
                new Class<?>[]{int.class}, 3);
        CountDownLatch arrived = new CountDownLatch(3);
        CountDownLatch done = new CountDownLatch(3);

        for (int i = 0; i < 3; i++) {
            TestSupport.startDaemon("q12-waiter-" + i, () -> {
                arrived.countDown();
                try {
                    latch.await();
                    done.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        assertTrue(TestSupport.await(arrived, 1000));
        assertFalse(TestSupport.await(done, 300), "计数未归零前 await 必须阻塞");

        latch.countDown();
        latch.countDown();
        assertFalse(TestSupport.await(done, 200), "计数为 1 时仍应阻塞");

        latch.countDown();
        assertTrue(TestSupport.await(done, 2000), "计数归零后所有等待线程应被唤醒");
        assertEquals(0, latch.getCount());
    }

    @Test
    void timedAwaitReturnsFalseOnTimeout() throws Exception {
        Q12Latch latch = Answers.newAnswer("q12", "Q12AnswerImpl", Q12Latch.class,
                new Class<?>[]{int.class}, 1);

        long start = System.nanoTime();
        assertFalse(latch.await(300), "计数未归零时超时等待应返回 false");
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        assertTrue(elapsedMillis >= 250, "应等待接近超时时间，实际 " + elapsedMillis + "ms");

        latch.countDown();
        assertTrue(latch.await(1000), "计数归零后应立刻返回 true");
    }

    @Test
    void countDownStopsAtZero() {
        Q12Latch latch = Answers.newAnswer("q12", "Q12AnswerImpl", Q12Latch.class,
                new Class<?>[]{int.class}, 1);
        latch.countDown();
        latch.countDown();
        assertEquals(0, latch.getCount(), "计数归零后不应变成负数");
    }

    @Test
    void interruptibleAwait() {
        Q12Latch latch = Answers.newAnswer("q12", "Q12AnswerImpl", Q12Latch.class,
                new Class<?>[]{int.class}, 1);
        Thread waiter = TestSupport.startDaemon("q12-interrupt", () -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        TestSupport.sleep(100);
        waiter.interrupt();
        TestSupport.sleep(100);
        assertFalse(waiter.isAlive(), "await 应可被中断");
    }

    @Test
    void awaitAfterZeroDoesNotBlock() throws Exception {
        Q12Latch latch = Answers.newAnswer("q12", "Q12AnswerImpl", Q12Latch.class,
                new Class<?>[]{int.class}, 0);
        assertTrue(latch.await(1000), "初始计数为 0 时应立即返回");
        assertEquals(0, latch.getCount());
    }
}
