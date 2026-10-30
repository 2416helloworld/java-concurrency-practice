package org.practice.answers.alice.q01;

import org.practice.api.Q01Singleton;

/**
 * Q01 答案：双重检查锁（Double-Checked Locking）单例。
 */
public final class Q01AnswerImpl implements Q01Singleton {

    private static volatile Q01AnswerImpl instance;

    private Q01AnswerImpl() {
    }

    public static Q01Singleton getInstance() {
        Q01AnswerImpl local = instance;
        if (local == null) {
            synchronized (Q01AnswerImpl.class) {
                local = instance;
                if (local == null) {
                    local = new Q01AnswerImpl();
                    instance = local;
                }
            }
        }
        return local;
    }
}
