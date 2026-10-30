# Q09 用 Condition 实现阻塞队列

## 场景

不用 `ArrayBlockingQueue`，手写一个有界阻塞队列。

## 要求

1. 用 `ReentrantLock` + 两个 `Condition`（`notFull` / `notEmpty`）实现 `put` / `take`。
2. 实现带超时的 `offer(e, timeout)` 和 `poll(timeout)`。
3. 解释为什么等待必须用 `while` 而非 `if`。

## 契约

实现 `org.practice.api.Q09BlockingQueue<E>`，构造方法参数为容量 `int`：

- `put` 满时阻塞、`take` 空时阻塞；
- `offer(e, timeoutMillis)` 超时返回 false；
- `poll(timeoutMillis)` 超时返回 null；
- `size()` 返回当前元素数。

## 考察点

`Condition` 精准唤醒、虚假唤醒、超时等待。

## 参考要点

- `notFull.awaitNanos(...)` 处理超时，返回剩余纳秒。
- `while` 防虚假唤醒 + 被唤醒后条件可能又变了。
- `signal` 而非 `signalAll` 提升效率。

## 答案位置

```text
answers/<用户名>/q09-blocking-queue/org/practice/answers/<用户名>/q09/Q09AnswerImpl.java
```

## 判题

- 满时 `offer` 超时返回 false、空时 `poll` 超时返回 null，等待时间接近超时；
- FIFO 顺序；
- 满时 `put` 阻塞，腾出空间后完成；
- 4 线程并发生产/消费 100 个元素，不丢失不重复。
