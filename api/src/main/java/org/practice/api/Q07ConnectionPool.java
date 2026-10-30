package org.practice.api;

import java.util.concurrent.TimeoutException;

/**
 * Q07：Semaphore 实现的连接池。
 *
 * <p>最多同时借出固定数量的连接；获取连接最多等待 timeoutMillis；归还必须在调用方的 finally 中完成。
 */
public interface Q07ConnectionPool extends AutoCloseable {

    /**
     * 借出一个连接，超时抛出 {@link TimeoutException}。
     */
    Q07Connection acquire(long timeoutMillis) throws InterruptedException, TimeoutException;

    /**
     * 归还连接；重复归还或归还非本池连接应被忽略。
     */
    void release(Q07Connection connection);

    /**
     * 当前还能借出的连接数（应与 Semaphore 的可用许可一致）。
     */
    int availableConnections();

    @Override
    void close();
}
