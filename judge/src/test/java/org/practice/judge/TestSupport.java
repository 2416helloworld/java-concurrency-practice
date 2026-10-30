package org.practice.judge;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 判题用的小工具，避免各测试重复写线程/等待代码。
 */
final class TestSupport {

    private TestSupport() {
    }

    static Thread startDaemon(String name, Runnable body) {
        Thread thread = new Thread(body, name);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    static boolean await(CountDownLatch latch, long millis) {
        try {
            return latch.await(millis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
