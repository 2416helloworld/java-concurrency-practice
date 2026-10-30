package org.practice.judge;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 加载用户答案类。
 *
 * <p>对 {@code org.practice.answers.*} 采用 child-first，确保每次判题都从答案编译输出目录
 * 读取用户自己的实现，而不是测试类路径上可能存在的旧版本。
 */
public final class AnswerClassLoader {

    private static final String ANSWER_PACKAGE_PREFIX = "org.practice.answers.";

    private AnswerClassLoader() {
    }

    public static Class<?> load(String fqcn) throws Exception {
        String classesDir = System.getProperty("answer.classes.dir");
        if (classesDir == null || classesDir.isBlank()) {
            throw new IllegalStateException("缺少系统属性 answer.classes.dir");
        }
        Path dir = Paths.get(classesDir).toAbsolutePath().normalize();
        if (!Files.isDirectory(dir)) {
            throw new IllegalStateException("答案编译目录不存在，请先编译 answers 模块: " + dir);
        }

        URL[] urls = {dir.toUri().toURL()};
        ClassLoader loader = new URLClassLoader(urls, AnswerClassLoader.class.getClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                synchronized (getClassLoadingLock(name)) {
                    Class<?> loaded = findLoadedClass(name);
                    if (loaded == null && name.startsWith(ANSWER_PACKAGE_PREFIX)) {
                        try {
                            loaded = findClass(name);
                        } catch (ClassNotFoundException ignored) {
                            // 回退到父加载器（例如答案类引用了 api 之外的依赖）
                        }
                    }
                    if (loaded == null) {
                        loaded = super.loadClass(name, false);
                    }
                    if (resolve) {
                        resolveClass(loaded);
                    }
                    return loaded;
                }
            }
        };
        return Class.forName(fqcn, true, loader);
    }
}
