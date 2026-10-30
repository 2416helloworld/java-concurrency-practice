package org.practice.judge;

import org.junit.jupiter.api.Test;
import org.practice.api.Q11Counter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Q11CounterTest {

    private static final int THREADS = 8;
    private static final int PER_THREAD = 100_000;

    @Test
    void implementationUsesLongAdder() {
        Class<?> answerType = Answers.load("q11", "Q11AnswerImpl");
        boolean usesLongAdder = java.util.Arrays.stream(answerType.getDeclaredFields())
                .anyMatch(field -> field.getType() == LongAdder.class);
        assertTrue(usesLongAdder, "Q11 实现必须使用 LongAdder 字段");
    }

    @Test
    void countsExactlyUnderContention() throws Exception {
        Q11Counter counter = Answers.newAnswer("q11", "Q11AnswerImpl", Q11Counter.class);
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREADS);
        try {
            for (int i = 0; i < THREADS; i++) {
                pool.execute(() -> {
                    try {
                        start.await();
                        for (int j = 0; j < PER_THREAD; j++) {
                            counter.increment();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }
            start.countDown();
            assertTrue(done.await(10, TimeUnit.SECONDS), "并发计数应在超时前完成");

            assertEquals((long) THREADS * PER_THREAD, counter.sum(), "高并发下不能丢计数");

            counter.add(-(long) THREADS * PER_THREAD);
            assertEquals(0L, counter.sum());
        } finally {
            pool.shutdownNow();
        }
    }
}
