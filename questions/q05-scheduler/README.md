# Q05 延迟任务 + 周期任务调度器

## 场景

订单 30 分钟未支付自动取消；同时每 5 分钟清理一次过期缓存。

## 要求

1. 用 `ScheduledThreadPoolExecutor` 实现两者。
2. 说明 `scheduleAtFixedRate` 与 `scheduleWithFixedDelay` 的区别，本场景该选哪个。
3. 任务抛异常会怎样？如何保证周期任务不因一次异常而终止？

## 契约

实现 `org.practice.api.Q05Scheduler`：

- `scheduleOnce(task, delayMillis)`：延迟执行一次（订单取消）；
- `scheduleAtFixedDelay(task, initialDelayMillis, delayMillis)`：固定延迟周期执行（缓存清理）；
- `close()`：关闭调度器。

## 考察点

Scheduled 线程池、周期语义、异常处理。

## 参考要点

- 清理缓存用 `scheduleWithFixedDelay`（避免任务堆积）；取消订单用 `schedule` 一次性延迟。
- 周期任务若抛未捕获异常会终止后续执行，需在任务体内 `try-catch` 全包。
- `scheduleAtFixedRate` 按固定频率（可能并发堆积），`scheduleWithFixedDelay` 按上次结束到下次开始的间隔。

## 答案位置

```text
answers/<用户名>/q05-scheduler/org/practice/answers/<用户名>/q05/Q05AnswerImpl.java
```

## 判题

- 一次性任务在延迟后执行；
- 周期任务每次抛异常，仍能继续执行至少 3 次。
