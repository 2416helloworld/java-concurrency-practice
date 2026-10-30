package org.practice.answers.alice.q08;

import org.practice.api.Q08LoadTest;
import org.practice.api.Q08Stats;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * Q08 答案：CyclicBarrier 让每轮 users 个线程同时起跑，CountDownLatch 等待本轮全部完成。
 */
public final class Q08AnswerImpl implements Q08LoadTest {

    @Override
    public Q08Stats run(int users, int rounds, Runnable request) {
        ExecutorService pool = Executors.newFixedThreadPool(users, runnable -> {
            Thread thread = new Thread(runnable, "q08-load");
            thread.setDaemon(true);
            return thread;
        });
        LongAdder successes = new LongAdder();
        LongAdder failures = new LongAdder();
        long start = System.nanoTime();

        try {
            for (int round = 0; round < rounds; round++) {
                CyclicBarrier startBarrier = new CyclicBarrier(users);
                CountDownLatch finished = new CountDownLatch(users);
                for (int user = 0; user < users; user++) {
                    pool.execute(() -> {
                        try {
                            startBarrier.await();
                            request.run();
                            successes.increment();
                        } catch (Throwable t) {
                            failures.increment();
                        } finally {
                            finished.countDown();
                        }
                    });
                }
                finished.await();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            pool.shutdownNow();
        }

        long durationMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        return new Q08Stats((long) users * rounds, successes.sum(), failures.sum(), durationMillis);
    }
}
