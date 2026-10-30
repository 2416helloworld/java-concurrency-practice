# Q08 CyclicBarrier 实现多轮并发压测

## 场景

模拟 100 个用户同时发起请求，跑 10 轮，每轮所有请求完成后统计耗时。

## 要求

1. 用 `CyclicBarrier` 让 100 个线程“同时起跑”。
2. 每轮结束后如何统计本轮总耗时与成功率？
3. 与 `CountDownLatch` 方案对比，为什么这里更适合 `CyclicBarrier`？

## 契约

实现 `org.practice.api.Q08LoadTest#run(int users, int rounds, Runnable request)`，
返回 `Q08Stats`（总请求数、成功数、失败数、总耗时毫秒）。`request` 抛异常即该次请求失败。

## 考察点

`CyclicBarrier` 复用、屏障动作、压测模型。

## 参考要点

- 屏障动作里记录/打印本轮耗时，`barrier.await()` 后一起冲刺。
- 统计用 `AtomicLong/LongAdder` 累计成功数、耗时。
- `CyclicBarrier` 可自动循环 10 轮；`CountDownLatch` 一次性，需每轮 new 一个。

## 答案位置

```text
answers/<用户名>/q08-cyclicbarrier-load/org/practice/answers/<用户名>/q08/Q08AnswerImpl.java
```

## 判题

50 用户 × 5 轮、每第 5 个请求抛异常：总数 250、成功 200、失败 50，且存在并发执行。
