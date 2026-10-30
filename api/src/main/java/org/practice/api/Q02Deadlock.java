package org.practice.api;

import java.util.concurrent.locks.Lock;

/**
 * Q02：死锁复现与破除。
 *
 * <p>实现要求使用 {@link Lock#lockInterruptibly()}，并在 {@code finally} 中释放已获取的锁。
 */
public interface Q02Deadlock {

    /**
     * 先锁 a、再执行回调、最后锁 b，拿到两把锁后一直持有直到线程被中断。
     *
     * @param afterFirstLock 获取第一把锁之后、获取第二把锁之前执行的回调（判题用于制造确定的交错）
     */
    void lockAToB(Lock a, Lock b, Runnable afterFirstLock) throws InterruptedException;

    /**
     * 先锁 b、再执行回调、最后锁 a，拿到两把锁后一直持有直到线程被中断。
     */
    void lockBToA(Lock a, Lock b, Runnable afterFirstLock) throws InterruptedException;

    /**
     * 固定加锁顺序版本：无论参数顺序如何，都按同一规则决定先锁哪一把，加锁-释放后返回 true。
     * 两个线程并发调用本方法不应死锁。
     */
    boolean lockInFixedOrder(Lock a, Lock b);

    /**
     * tryLock 带超时版本：在 timeoutMillis 内依次尝试两把锁，任一把超时则释放已持有的锁并返回 false；
     * 两把都拿到时释放后返回 true。
     */
    boolean tryLockBoth(Lock a, Lock b, long timeoutMillis) throws InterruptedException;
}
