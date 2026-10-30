package org.practice.api;

/**
 * Q03：手写读写锁。
 *
 * <p>要求支持多个读线程并发、写线程独占，且写线程优先：只要已有写线程在等待，
 * 新来的读线程必须排队，避免写饥饿。不支持锁升级/降级。
 */
public interface Q03ReadWriteLock {

    void lockRead();

    void unlockRead();

    void lockWrite();

    void unlockWrite();
}
