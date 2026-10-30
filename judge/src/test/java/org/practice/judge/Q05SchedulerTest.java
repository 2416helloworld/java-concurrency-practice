package org.practice.judge;

import org.junit.jupiter.api.Test;
import org.practice.api.Q05Scheduler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

class Q05SchedulerTest {

    @Test
    void onceTaskRunsAfterDelay() {
        Q05Scheduler scheduler = Answers.newAnswer("q05", "Q05AnswerImpl", Q05Scheduler.class);
        try {
            CountDownLatch ran = new CountDownLatch(1);
            scheduler.scheduleOnce(ran::countDown, 200);
            assertTrue(TestSupport.await(ran, 2000), "一次性任务应在延迟后执行");
        } finally {
            scheduler.close();
        }
    }

    @Test
    void periodicTaskKeepsRunningAfterException() {
        Q05Scheduler scheduler = Answers.newAnswer("q05", "Q05AnswerImpl", Q05Scheduler.class);
        try {
            AtomicInteger runs = new AtomicInteger();
            scheduler.scheduleAtFixedDelay(() -> {
                runs.incrementAndGet();
                throw new IllegalStateException("boom");
            }, 0, 100);

            long deadline = System.currentTimeMillis() + 3000;
            while (runs.get() < 3 && System.currentTimeMillis() < deadline) {
                TestSupport.sleep(20);
            }
            assertTrue(runs.get() >= 3, "周期任务单次抛异常后应继续执行，实际执行 " + runs.get() + " 次");
        } finally {
            scheduler.close();
        }
    }
}
