package org.practice.answers.alice.q11;

import org.practice.api.Q11Counter;

import java.util.concurrent.atomic.LongAdder;

/**
 * Q11 答案：LongAdder 分段累加，避免单点 CAS 自旋。
 */
public final class Q11AnswerImpl implements Q11Counter {

    private final LongAdder adder = new LongAdder();

    @Override
    public void increment() {
        adder.increment();
    }

    @Override
    public void add(long delta) {
        adder.add(delta);
    }

    @Override
    public long sum() {
        return adder.sum();
    }
}
