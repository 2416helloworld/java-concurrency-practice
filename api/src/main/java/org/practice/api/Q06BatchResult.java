package org.practice.api;

import java.util.List;

/**
 * Q06 的汇总结果：成功列表 + 失败列表。
 */
public final class Q06BatchResult {

    private final List<String> successes;
    private final List<String> failures;

    public Q06BatchResult(List<String> successes, List<String> failures) {
        this.successes = List.copyOf(successes);
        this.failures = List.copyOf(failures);
    }

    public List<String> getSuccesses() {
        return successes;
    }

    public List<String> getFailures() {
        return failures;
    }
}
