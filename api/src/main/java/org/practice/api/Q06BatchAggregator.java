package org.practice.api;

/**
 * Q06：批量任务并行 + 整体超时汇总。
 */
public interface Q06BatchAggregator {

    /**
     * 并行执行全部任务，最多等待 timeoutMillis。
     *
     * <p>单个任务失败不影响其他任务；超时后应取消未完成任务，并立即返回已收集到的结果。
     * 返回的成功列表包含任务返回值，失败列表包含失败原因（异常信息或“超时”）。
     * 不能抛出异常，也不能因超时而阻塞超过 timeoutMillis 太多。
     */
    Q06BatchResult aggregate(java.util.List<java.util.concurrent.Callable<String>> tasks, long timeoutMillis);
}
