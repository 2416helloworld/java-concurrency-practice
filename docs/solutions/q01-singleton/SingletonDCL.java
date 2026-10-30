package org.practice.solutions.q01;

import org.practice.api.Q01Singleton;

/**
 * 官方参考实现：双重检查锁（Double-Checked Locking）。
 *
 * <p>仅作参考，不在任何 Maven 模块的源码目录中，不参与编译。
 */
public final class SingletonDCL implements Q01Singleton {

    private static volatile SingletonDCL instance;

    private SingletonDCL() {
    }

    public static Q01Singleton getInstance() {
        SingletonDCL local = instance;
        if (local == null) {
            synchronized (SingletonDCL.class) {
                local = instance;
                if (local == null) {
                    local = new SingletonDCL();
                    instance = local;
                }
            }
        }
        return local;
    }
}
