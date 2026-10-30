package org.practice.judge;

import org.junit.jupiter.api.Test;
import org.practice.api.Q08LoadTest;
import org.practice.api.Q08Stats;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Q08LoadTestTest {

    @Test
    void runsAllRoundsAndAggregatesStats() {
        Q08LoadTest loadTest = Answers.newAnswer("q08", "Q08AnswerImpl", Q08LoadTest.class);

        AtomicInteger invocation = new AtomicInteger();
        AtomicInteger active = new AtomicInteger();
        AtomicInteger maxActive = new AtomicInteger();
        LongAdder successes = new LongAdder();

        Runnable request = () -> {
            int index = invocation.incrementAndGet();
            maxActive.accumulateAndGet(active.incrementAndGet(), Math::max);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                active.decrementAndGet();
            }
            if (index % 5 == 0) {
                throw new IllegalStateException("每第 5 个请求失败");
            }
            successes.increment();
        };

        int users = 50;
        int rounds = 5;
        Q08Stats stats = loadTest.run(users, rounds, request);

        assertEquals((long) users * rounds, stats.getTotalRequests());
        assertEquals(200, stats.getSuccesses(), "250 次请求中每第 5 次失败");
        assertEquals(50, stats.getFailures());
        assertTrue(stats.getSuccesses() + stats.getFailures() == stats.getTotalRequests());
        assertTrue(successes.sum() == stats.getSuccesses(), "统计应与实际执行一致");
        assertTrue(maxActive.get() > 1, "应当存在并发执行（屏障/线程池生效）");
    }
}
