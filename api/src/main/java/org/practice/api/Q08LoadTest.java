package org.practice.api;

/**
 * Q08：CyclicBarrier 多轮并发压测。
 */
public interface Q08LoadTest {

    /**
     * @param users   每轮并发用户数（也就是屏障参与者数量）
     * @param rounds  轮数
     * @param request 每次请求执行的动作，抛异常视为该次请求失败
     */
    Q08Stats run(int users, int rounds, Runnable request);
}
