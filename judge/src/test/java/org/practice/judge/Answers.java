package org.practice.judge;

import org.junit.jupiter.api.Assumptions;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * 按命名约定加载用户答案：{@code org.practice.answers.<user>.<qNN>.<QNN>AnswerImpl}。
 *
 * <p>用户没有提交某题的源码时，本题测试直接跳过；有源码但类名/包名不匹配时判为失败，
 * 以便区分“没做”和“写错了”。
 */
public final class Answers {

    private static final String ANSWER_PACKAGE_PREFIX = "org.practice.answers.";

    private Answers() {
    }

    /**
     * @param question   题目短名，例如 {@code q02}
     * @param simpleName 实现类简单名，例如 {@code Q02AnswerImpl}
     */
    public static Class<?> load(String question, String simpleName) {
        String user = System.getProperty("answer.user", "alice");
        String fqcn = ANSWER_PACKAGE_PREFIX + user + "." + question + "." + simpleName;
        try {
            return AnswerClassLoader.load(fqcn);
        } catch (Exception e) {
            if (!hasSubmittedSource(user, question)) {
                Assumptions.abort("用户 " + user + " 未提交 " + question + " 的答案，跳过");
            }
            throw new IllegalStateException("加载答案类失败: " + fqcn
                    + "，请确认包名为 " + ANSWER_PACKAGE_PREFIX + user + "." + question
                    + "、类名为 " + simpleName, e);
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
        Class<?> type = load(question, simpleName);
        try {
            return apiType.cast(type.getConstructor(parameterTypes).newInstance(args));
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("答案构造失败: " + question, e.getCause());
        } catch (Exception e) {
            throw new IllegalStateException("实例化答案失败: " + question, e);
        }
    }

    /**
     * 判断答案目录下是否存在该题的 Java 源码（包路径片段 {@code /qNN/}）。
     */
    private static boolean hasSubmittedSource(String user, String question) {
        String root = System.getProperty("answers.root");
        if (root == null || root.isBlank()) {
            return false;
        }
        Path userDir = Paths.get(root, user);
        if (!Files.isDirectory(userDir)) {
            return false;
        }
        String marker = "/" + question + "/";
        try (Stream<Path> files = Files.walk(userDir)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .anyMatch(path -> path.toString().replace('\\', '/').contains(marker));
        } catch (Exception e) {
            return false;
        }
    }
}
