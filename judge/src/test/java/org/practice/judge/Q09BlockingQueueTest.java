package org.practice.judge;

import org.junit.jupiter.api.Test;
import org.practice.api.Q09BlockingQueue;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Q09BlockingQueueTest {

    @Test
    void offerTimesOutWhenFull() throws Exception {
        Q09BlockingQueue<String> queue = newQueue(2);
        queue.put("a");
        queue.put("b");

        long start = System.nanoTime();
        assertFalse(queue.offer("c", 300), "队列满时 offer 应超时返回 false");
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

        assertTrue(elapsedMillis >= 250, "offer 应等待接近超时时间，实际 " + elapsedMillis + "ms");
        assertEquals(2, queue.size());
    }

    @Test
    void pollTimesOutWhenEmpty() throws Exception {
        Q09BlockingQueue<String> queue = newQueue(2);

        long start = System.nanoTime();
        assertNull(queue.poll(300), "队列空时 poll 应超时返回 null");
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

        assertTrue(elapsedMillis >= 250, "poll 应等待接近超时时间，实际 " + elapsedMillis + "ms");
    }

    @Test
    void keepsFifoOrder() throws Exception {
        Q09BlockingQueue<String> queue = newQueue(3);
        queue.put("a");
        queue.put("b");
        queue.put("c");

        assertEquals("a", queue.take());
        assertEquals("b", queue.take());
        assertEquals("c", queue.take());
    }

    @Test
    void putBlocksUntilSpaceAvailable() throws Exception {
        Q09BlockingQueue<String> queue = newQueue(2);
        queue.put("a");
        queue.put("b");

        CountDownLatch putDone = new CountDownLatch(1);
        Thread producer = TestSupport.startDaemon("q09-producer", () -> {
            try {
                queue.put("c");
                putDone.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        TestSupport.sleep(200);
        assertEquals(2, queue.size(), "队列满时 put 必须阻塞");
        assertEquals("a", queue.take());
        assertTrue(TestSupport.await(putDone, 2000), "腾出空间后 put 应完成");
        assertEquals(2, queue.size());
        producer.interrupt();
    }

    @Test
    void supportsConcurrentProducersAndConsumers() throws Exception {
        Q09BlockingQueue<String> queue = newQueue(5);
        ExecutorService pool = Executors.newFixedThreadPool(4);
        Set<String> consumed = ConcurrentHashMap.newKeySet();
        try {
            pool.submit(() -> produce(queue, 0, 50));
            pool.submit(() -> produce(queue, 50, 100));
            pool.submit(() -> consume(queue, consumed, 50));
            pool.submit(() -> consume(queue, consumed, 50));

            long deadline = System.currentTimeMillis() + 5000;
            while (consumed.size() < 100 && System.currentTimeMillis() < deadline) {
                Thread.sleep(20);
            }
            assertEquals(100, consumed.size(), "并发生产/消费不应丢失或重复元素");
        } finally {
            pool.shutdownNow();
        }
    }

    private static void produce(Q09BlockingQueue<String> queue, int from, int to) {
        try {
            for (int i = from; i < to; i++) {
                queue.put("item-" + i);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void consume(Q09BlockingQueue<String> queue, Set<String> consumed, int count) {
        try {
            for (int i = 0; i < count; i++) {
                consumed.add(queue.take());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @SuppressWarnings("unchecked")
    private static Q09BlockingQueue<String> newQueue(int capacity) {
        return (Q09BlockingQueue<String>) Answers.newAnswer("q09", "Q09AnswerImpl",
                Q09BlockingQueue.class, new Class<?>[]{int.class}, capacity);
    }
}
