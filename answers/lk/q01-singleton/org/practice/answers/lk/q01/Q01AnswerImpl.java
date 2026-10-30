package org.practice.answers.lk.q01;

import org.practice.api.Q01Singleton;

/**
 * Q01 答案：双重检查锁（Double-Checked Locking）单例。
 */
public final class Q01AnswerImpl implements Q01Singleton {
    private static volatile Q01Singleton instance;
    private Q01AnswerImpl() {
    }

    public static Q01Singleton getInstance() {
        // TODO: 在这里实现 DCL
        if(instance == null){
            synchronized (Q01AnswerImpl.class){
                if(instance == null){
                    instance = new Q01AnswerImpl();
                }
            }
        }
        return instance;

    }
}
