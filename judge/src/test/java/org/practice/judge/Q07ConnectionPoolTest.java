package org.practice.judge;

import org.junit.jupiter.api.Test;
import org.practice.api.Q07Connection;
import org.practice.api.Q07ConnectionPool;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Q07ConnectionPoolTest {

    @Test
    void enforcesCapacityAndReusesConnections() throws Exception {
        Q07ConnectionPool pool = Answers.newAnswer("q07", "Q07AnswerImpl", Q07ConnectionPool.class,
                new Class<?>[]{int.class}, 5);

        Q07Connection[] connections = new Q07Connection[5];
        Set<Integer> ids = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            connections[i] = pool.acquire(1000);
            ids.add(connections[i].id());
        }
        assertEquals(5, ids.size(), "连接池中的连接不应重复发放");
        assertEquals(0, pool.availableConnections());

        long start = System.nanoTime();
        assertThrows(TimeoutException.class, () -> pool.acquire(1000), "借满后应超时");
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        assertTrue(elapsedMillis >= 800, "应等待接近超时时间，实际 " + elapsedMillis + "ms");

        pool.release(connections[0]);
        assertEquals(1, pool.availableConnections());
        Q07Connection reused = pool.acquire(1000);
        assertTrue(ids.contains(reused.id()), "归还的连接应能被再次借出");
        pool.release(reused);

        for (int i = 1; i < 5; i++) {
            pool.release(connections[i]);
        }
        assertEquals(5, pool.availableConnections(), "全部归还后许可应恢复");
        pool.close();
    }

    @Test
    void usageExceptionStillReturnsConnectionWhenReleasedInFinally() throws Exception {
        Q07ConnectionPool pool = Answers.newAnswer("q07", "Q07AnswerImpl", Q07ConnectionPool.class,
                new Class<?>[]{int.class}, 1);
        try {
            Q07Connection connection = pool.acquire(1000);
            try {
                throw new RuntimeException("模拟使用中异常");
            } catch (RuntimeException expected) {
                // 模拟业务异常被上层捕获
            } finally {
                pool.release(connection);
            }
            assertEquals(1, pool.availableConnections(), "finally 归还后许可必须恢复");
        } finally {
            pool.close();
        }
    }
}
