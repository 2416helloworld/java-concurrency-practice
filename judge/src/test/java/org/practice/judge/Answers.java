package org.practice.judge;

import java.lang.reflect.InvocationTargetException;

/**
 * 按命名约定加载用户答案：{@code org.practice.answers.<user>.<qNN>.<QNN>AnswerImpl}。
 */
public final class Answers {

    private Answers() {
    }

    /**
     * @param question   题目短名，例如 {@code q02}
     * @param simpleName 实现类简单名，例如 {@code Q02AnswerImpl}
     */
    public static Class<?> load(String question, String simpleName) {
        String user = System.getProperty("answer.user", "alice");
        String fqcn = "org.practice.answers." + user + "." + question + "." + simpleName;
        try {
            return AnswerClassLoader.load(fqcn);
        } catch (Exception e) {
            throw new IllegalStateException("加载答案类失败: " + fqcn
                    + "，请确认目录 answers/" + user + "/" + question + "-*/ 下存在对应实现", e);
        }
    }

    public static <T> T newAnswer(String question, String simpleName, Class<T> apiType) {
        return newAnswer(question, simpleName, apiType, new Class<?>[0]);
    }

    /**
     * 用指定构造参数实例化答案，并转型为题目接口。
     */
    public static <T> T newAnswer(String question, String simpleName, Class<T> apiType,
                                  Class<?>[] parameterTypes, Object... args) {
        try {
            Object instance = load(question, simpleName).getConstructor(parameterTypes).newInstance(args);
            return apiType.cast(instance);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("答案构造失败: " + question, e.getCause());
        } catch (Exception e) {
            throw new IllegalStateException("实例化答案失败: " + question, e);
        }
    }
}
