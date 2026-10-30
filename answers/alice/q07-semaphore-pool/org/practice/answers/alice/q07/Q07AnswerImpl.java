package org.practice.answers.alice.q07;

import org.practice.api.Q07Connection;
import org.practice.api.Q07ConnectionPool;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Q07 答案：Semaphore 控制并发数，BlockingQueue 存放空闲连接。
 */
public final class Q07AnswerImpl implements Q07ConnectionPool {

    private final Semaphore permits;
    private final BlockingQueue<Q07Connection> idle;
    private final Set<Q07Connection> borrowed = ConcurrentHashMap.newKeySet();

    public Q07AnswerImpl(int size) {
        this.permits = new Semaphore(size);
        this.idle = new LinkedBlockingQueue<>(size);
        for (int i = 0; i < size; i++) {
            idle.offer(new PooledConnection(i));
        }
    }

    @Override
    public Q07Connection acquire(long timeoutMillis) throws InterruptedException, TimeoutException {
        if (!permits.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS)) {
            throw new TimeoutException("获取连接超时: " + timeoutMillis + "ms");
        }
        Q07Connection connection = idle.poll();
        if (connection == null) {
            permits.release();
            throw new IllegalStateException("许可与空闲连接不一致");
        }
        borrowed.add(connection);
        return connection;
    }

    @Override
    public void release(Q07Connection connection) {
        if (connection == null || !borrowed.remove(connection)) {
            return;
        }
        idle.offer(connection);
        permits.release();
    }

    @Override
    public int availableConnections() {
        return permits.availablePermits();
    }

    @Override
    public void close() {
        idle.clear();
        borrowed.clear();
    }

    private static final class PooledConnection implements Q07Connection {

        private final int id;

        private PooledConnection(int id) {
            this.id = id;
        }

        @Override
        public int id() {
            return id;
        }

        @Override
        public String toString() {
            return "Connection#" + id;
        }
    }
}
