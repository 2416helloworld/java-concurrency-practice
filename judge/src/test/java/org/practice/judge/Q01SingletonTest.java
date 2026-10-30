package org.practice.judge;

import org.junit.jupiter.api.Test;
import org.practice.api.Q01Singleton;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class Q01SingletonTest {

    private static final int THREADS = 32;

    @Test
    void answerShouldImplementInterfaceAndExposeStaticGetInstance() throws Exception {
        Class<?> answerType = loadAnswerClass();
        assertTrue(Q01Singleton.class.isAssignableFrom(answerType), "答案类必须实现 Q01Singleton");

        Method getInstance = getInstanceMethod(answerType);
        assertTrue(Modifier.isStatic(getInstance.getModifiers()), "getInstance() 必须是 static");
        assertEquals(Q01Singleton.class, getInstance.getReturnType(), "getInstance() 必须返回 Q01Singleton");

        for (Constructor<?> constructor : answerType.getDeclaredConstructors()) {
            assertFalse(Modifier.isPublic(constructor.getModifiers()), "构造函数不能是 public: " + constructor);
        }
    }

    @Test
    void shouldReturnSameInstanceSequentially() throws Exception {
        Method getInstance = getInstanceMethod(loadAnswerClass());

        Object first = getInstance.invoke(null);
        Object second = getInstance.invoke(null);

        assertNotNull(first, "getInstance() 不能返回 null");
        assertSame(first, second, "多次调用必须返回同一个实例");
    }

    @Test
    void shouldReturnSameInstanceUnderConcurrency() throws Exception {
        Method getInstance = getInstanceMethod(loadAnswerClass());

        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        try {
            CyclicBarrier barrier = new CyclicBarrier(THREADS);
            List<Future<Object>> futures = new ArrayList<>(THREADS);
            for (int i = 0; i < THREADS; i++) {
                futures.add(pool.submit((Callable<Object>) () -> {
                    barrier.await(5, TimeUnit.SECONDS);
                    return getInstance.invoke(null);
                }));
            }

            Set<Object> instances = futures.stream()
                    .map(future -> {
                        try {
                            return future.get(10, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            throw new IllegalStateException("并发调用 getInstance() 失败", e);
                        }
                    })
                    .collect(Collectors.toSet());

            assertTrue(instances.stream().noneMatch(Objects::isNull), "getInstance() 不能返回 null");
            assertEquals(1, instances.size(), "并发调用必须返回同一个实例，实际得到 " + instances.size() + " 个");
        } finally {
            pool.shutdownNow();
        }
    }

    private static Class<?> loadAnswerClass() {
        return Answers.load("q01", "Q01AnswerImpl");
    }

    private static Method getInstanceMethod(Class<?> type) {
        try {
            return type.getMethod("getInstance");
        } catch (NoSuchMethodException e) {
            fail("答案类缺少 public static Q01Singleton getInstance()");
            throw new AssertionError(e);
        }
    }
}
