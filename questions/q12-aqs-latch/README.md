# Q12 基于 AQS 自定义“一次性门闩”

## 场景

不用 `CountDownLatch`，继承 `AbstractQueuedSynchronizer` 实现一个“倒计数门闩”。

## 要求

1. 实现 `countDown()` 和 `await()`，`state` 表示剩余计数。
2. 说明该用共享模式还是独占模式，为什么。
3. 解释 `tryAcquireShared` 返回值 `>0 / ==0 / <0` 分别代表什么。

## 契约

继承 `AbstractQueuedSynchronizer` 并实现 `org.practice.api.Q12Latch`：

- 构造方法参数为初始计数 `int`；
- `getCount()`、`countDown()`、`await()`、`await(timeoutMillis)`；
- 计数归零后不再递减。

## 考察点

AQS 模板方法、共享模式、state 语义。

## 参考要点

- 共享模式：多个线程可同时 `await`，计数归零时全部放行。
- `tryAcquireShared` 返回 `state == 0 ? 1 : -1`（简化版）；负值表示失败需排队，0 表示成功但无剩余，正值表示成功且还有剩余可唤醒后继。
- `countDown` 用 `compareAndSetState` 循环减一，减到 0 时 AQS 自动唤醒共享队列。

## 答案位置

```text
answers/<用户名>/q12-aqs-latch/org/practice/answers/<用户名>/q12/Q12AnswerImpl.java
```

## 判题

- 3 个等待线程在计数为 1 时仍阻塞，归零后全部唤醒；
- 超时等待返回 false，归零后立即返回 true；
- 重复 `countDown` 不会变负数；`await` 可被中断；初始为 0 时立即放行。
