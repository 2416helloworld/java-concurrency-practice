# Q06 批量任务并行 + 超时汇总

## 场景

调用 10 个下游接口汇总结果，总超时 2 秒，任一失败不影响其他，最终返回“成功列表 + 失败列表”。

## 要求

1. 用 `CompletableFuture` 实现，设置整体超时。
2. 失败的任务如何优雅收集，而不是让 `allOf` 整体失败？
3. 超时后如何取消未完成的任务？

## 契约

实现 `org.practice.api.Q06BatchAggregator#aggregate(List<Callable<String>>, long timeoutMillis)`，
返回 `Q06BatchResult`（成功列表 + 失败列表）。不能抛出异常，超时后应尽快返回。

## 考察点

`CompletableFuture`、`orTimeout/completeOnTimeout`、`handle`、`cancel`。

## 参考要点

- 每个任务 `.handle((r, e) -> ...)` 包装成“永不失败”的结果对象。
- `allOf(...).orTimeout(2, SECONDS).join()`；超时后取消剩余 future，再按各任务状态汇总。
- `CompletableFuture` 默认用 `ForkJoinPool.commonPool`，IO 任务应传自定义线程池。

## 答案位置

```text
answers/<用户名>/q06-batch-timeout/org/practice/answers/<用户名>/q06/Q06AnswerImpl.java
```

## 判题

- 无超时场景：成功的进成功列表，异常进失败列表；
- 含 2.5s 慢任务、整体超时 800ms：3 个已完成成功结果不丢失，失败数 ≥ 3，且 2 秒内返回。
