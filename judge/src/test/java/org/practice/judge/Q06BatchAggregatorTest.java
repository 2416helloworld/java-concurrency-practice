package org.practice.judge;

import org.junit.jupiter.api.Test;
import org.practice.api.Q06BatchAggregator;
import org.practice.api.Q06BatchResult;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Q06BatchAggregatorTest {

    @Test
    void collectsSuccessesAndFailuresWhenAllFinish() {
        Q06BatchAggregator aggregator = Answers.newAnswer("q06", "Q06AnswerImpl", Q06BatchAggregator.class);

        List<Callable<String>> tasks = List.of(
                () -> "ok-1",
                () -> "ok-2",
                () -> {
                    throw new IllegalStateException("bad");
                });

        Q06BatchResult result = aggregator.aggregate(tasks, 5000);

        assertEquals(Set.of("ok-1", "ok-2"), new HashSet<>(result.getSuccesses()));
        assertEquals(1, result.getFailures().size(), "恰好一个任务失败");
    }

    @Test
    void returnsWithinTimeoutAndCancelsSlowTasks() {
        Q06BatchAggregator aggregator = Answers.newAnswer("q06", "Q06AnswerImpl", Q06BatchAggregator.class);

        List<Callable<String>> tasks = List.of(
                () -> "ok-1",
                () -> {
                    throw new IllegalStateException("bad-1");
                },
                () -> "ok-2",
                () -> {
                    throw new IllegalArgumentException("bad-2");
                },
                () -> "ok-3",
                () -> {
                    Thread.sleep(2500);
                    return "slow";
                });

        long start = System.nanoTime();
        Q06BatchResult result = aggregator.aggregate(tasks, 800);
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

        assertEquals(Set.of("ok-1", "ok-2", "ok-3"), new HashSet<>(result.getSuccesses()),
                "已完成的任务结果不能因整体超时而丢失");
        assertTrue(result.getFailures().size() >= 3, "2 个失败 + 至少 1 个超时");
        assertTrue(elapsedMillis <= 2000, "整体超时后应尽快返回，实际 " + elapsedMillis + "ms");
    }
}
