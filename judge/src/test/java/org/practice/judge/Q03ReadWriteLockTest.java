package org.practice.judge;

import org.junit.jupiter.api.Test;
import org.practice.api.Q03ReadWriteLock;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Q03ReadWriteLockTest {

    @Test
    void multipleReadersCanEnterTogether() {
        Q03ReadWriteLock rw = Answers.newAnswer("q03", "Q03AnswerImpl", Q03ReadWriteLock.class);
        CountDownLatch acquired = new CountDownLatch(3);
        CountDownLatch release = new CountDownLatch(1);

        rw.lockRead();
        try {
            for (int i = 0; i < 3; i++) {
                TestSupport.startDaemon("q03-reader-" + i, () -> {
                    rw.lockRead();
                    try {
                        acquired.countDown();
                        TestSupport.await(release, 2000);
                    } finally {
                        rw.unlockRead();
                    }
                });
            }
            assertTrue(TestSupport.await(acquired, 1000), "多个读线程应能同时进入");
        } finally {
            release.countDown();
            rw.unlockRead();
        }
    }

    @Test
    void writerExcludesReaders() {
        Q03ReadWriteLock rw = Answers.newAnswer("q03", "Q03AnswerImpl", Q03ReadWriteLock.class);
        CountDownLatch writerAcquired = new CountDownLatch(1);
        CountDownLatch releaseWriter = new CountDownLatch(1);

        rw.lockRead();
        TestSupport.startDaemon("q03-writer", () -> {
            rw.lockWrite();
            try {
                writerAcquired.countDown();
                TestSupport.await(releaseWriter, 2000);
            } finally {
                rw.unlockWrite();
            }
        });

        assertFalse(TestSupport.await(writerAcquired, 300), "读锁持有期间写线程必须阻塞");
        rw.unlockRead();
        assertTrue(TestSupport.await(writerAcquired, 2000), "读锁释放后写线程应进入");
        releaseWriter.countDown();
    }

    @Test
    void waitingWriterBlocksNewReaders() {
        Q03ReadWriteLock rw = Answers.newAnswer("q03", "Q03AnswerImpl", Q03ReadWriteLock.class);
        CountDownLatch writerAcquired = new CountDownLatch(1);
        CountDownLatch reader2Acquired = new CountDownLatch(1);
        CountDownLatch releaseReader2 = new CountDownLatch(1);

        rw.lockRead(); // 读线程 R1 持有读锁
        TestSupport.startDaemon("q03-waiting-writer", () -> {
            rw.lockWrite();
            try {
                writerAcquired.countDown();
            } finally {
                rw.unlockWrite();
            }
        });

        TestSupport.sleep(150); // 让写线程先进入等待

        TestSupport.startDaemon("q03-reader-2", () -> {
            rw.lockRead();
            try {
                reader2Acquired.countDown();
                TestSupport.await(releaseReader2, 2000);
            } finally {
                rw.unlockRead();
            }
        });

        assertFalse(TestSupport.await(reader2Acquired, 400),
                "有写线程等待时，新读线程应排队（写优先）");

        rw.unlockRead();
        assertTrue(TestSupport.await(writerAcquired, 2000), "写线程应优先获得写锁");
        assertTrue(TestSupport.await(reader2Acquired, 2000), "写线程释放后读线程应进入");
        releaseReader2.countDown();
    }
}
