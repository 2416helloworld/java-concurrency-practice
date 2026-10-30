package org.practice.api;

/**
 * Q05：延迟任务 + 周期任务调度器。
 *
 * <p>一次性任务对应订单超时取消；周期任务对应缓存清理，需使用固定延迟语义，
 * 且单次任务抛异常不能终止后续执行。
 */
public interface Q05Scheduler extends AutoCloseable {

    /**
     * 延迟 delayMillis 后执行一次。
     */
    void scheduleOnce(Runnable task, long delayMillis);

    /**
     * 首次延迟 initialDelayMillis，之后以上一次执行结束到下一次开始的间隔 delayMillis 重复执行。
     */
    void scheduleAtFixedDelay(Runnable task, long initialDelayMillis, long delayMillis);

    /**
     * 关闭调度器并停止所有任务。
     */
    @Override
    void close();
}
