package org.practice.answers.alice.q06;

import org.practice.api.Q06BatchAggregator;
import org.practice.api.Q06BatchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Q06 答案：CompletableFuture + handle 包装 + allOf orTimeout + cancel。
 */
public final class Q06AnswerImpl implements Q06BatchAggregator {

    @Override
    public Q06BatchResult aggregate(List<Callable<String>> tasks, long timeoutMillis) {
        ExecutorService pool = Executors.newFixedThreadPool(Math.max(1, tasks.size()), runnable -> {
            Thread thread = new Thread(runnable, "q06-batch");
            thread.setDaemon(true);
            return thread;
        });

        List<CompletableFuture<String>> raw = new ArrayList<>(tasks.size());
        List<CompletableFuture<Outcome>> wrapped = new ArrayList<>(tasks.size());
        try {
            for (Callable<String> task : tasks) {
                CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> call(task), pool);
                raw.add(future);
                wrapped.add(future.handle((value, error) -> error == null
                        ? new Outcome(value, null)
                        : new Outcome(null, error)));
            }

            try {
                CompletableFuture.allOf(wrapped.toArray(new CompletableFuture[0]))
                        .orTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
                        .join();
            } catch (CompletionException ignored) {
                // 超时或个别任务异常都在这里被吞掉，后面按每个任务的状态汇总
            }

            raw.forEach(future -> future.cancel(true));

            List<String> successes = new ArrayList<>();
            List<String> failures = new ArrayList<>();
            for (CompletableFuture<Outcome> future : wrapped) {
                if (!future.isDone()) {
                    failures.add("超时未完成");
                    continue;
                }
                Outcome outcome = future.getNow(null);
                if (outcome == null || outcome.error() != null) {
                    failures.add(outcome == null ? "任务失败" : describe(outcome.error()));
                } else {
                    successes.add(outcome.value());
                }
            }
            return new Q06BatchResult(successes, failures);
        } finally {
            pool.shutdownNow();
        }
    }

    private static String call(Callable<String> task) {
        try {
            return task.call();
        } catch (Exception e) {
            throw new CompletionException(e);
        }
    }

    private static String describe(Throwable error) {
        Throwable cause = (error instanceof CompletionException && error.getCause() != null)
                ? error.getCause() : error;
        return cause.getClass().getSimpleName() + ": " + cause.getMessage();
    }

    private record Outcome(String value, Throwable error) {
    }
}
