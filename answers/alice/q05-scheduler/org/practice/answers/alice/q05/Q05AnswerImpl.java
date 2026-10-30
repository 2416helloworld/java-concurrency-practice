package org.practice.answers.alice.q05;

import org.practice.api.Q05Scheduler;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Q05 答案：ScheduledThreadPoolExecutor 实现一次性延迟与固定延迟周期任务。
 *
 * <p>周期任务用 scheduleWithFixedDelay，并在任务体外包一层 try-catch，
 * 避免单次异常终止后续执行。
 */
public final class Q05AnswerImpl implements Q05Scheduler {

    private final ScheduledThreadPoolExecutor executor;

    public Q05AnswerImpl() {
        this.executor = new ScheduledThreadPoolExecutor(2, runnable -> {
            Thread thread = new Thread(runnable, "q05-scheduler");
            thread.setDaemon(true);
            return thread;
        });
        this.executor.setRemoveOnCancelPolicy(true);
    }

    @Override
    public void scheduleOnce(Runnable task, long delayMillis) {
        executor.schedule(task, delayMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void scheduleAtFixedDelay(Runnable task, long initialDelayMillis, long delayMillis) {
        executor.scheduleWithFixedDelay(guarded(task), initialDelayMillis, delayMillis, TimeUnit.MILLISECONDS);
    }

    private static Runnable guarded(Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (Throwable ignored) {
                // 单次任务异常不能终止周期调度
            }
        };
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
