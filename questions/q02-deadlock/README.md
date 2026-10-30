# Q02 死锁复现与破除

## 场景

两个线程分别按相反顺序获取锁 A、B。

## 要求

1. 写出必然死锁的代码。
2. 用 `jstack` 或 `ThreadMXBean.findDeadlockedThreads()` 检测死锁。
3. 给出至少 3 种破除死锁的方案（固定加锁顺序、`tryLock` 带超时、减少锁粒度/单锁）。

## 契约

实现 `org.practice.api.Q02Deadlock`：

- `lockAToB(a, b, afterFirstLock)`：先锁 a、执行回调、再锁 b，拿到两把锁后持有到线程被中断；
- `lockBToA(...)`：同上，但先锁 b；
- `lockInFixedOrder(a, b)`：无论参数顺序，都按同一规则加锁，完成后释放并返回 true；
- `tryLockBoth(a, b, timeoutMillis)`：两把锁都在超时内拿到则释放并返回 true，否则释放已持有的锁并返回 false。

实现必须使用 `lockInterruptibly()` 并在 `finally` 中释放锁。

## 考察点

死锁四条件、`tryLock(timeout)`、死锁检测。

## 参考要点

- `ReentrantLock.tryLock(1, TimeUnit.SECONDS)` 拿不到就放弃并释放已持有的锁，破坏“请求并保持”。
- 固定顺序：所有线程先锁 A 再锁 B。
- `ThreadMXBean` 可编程检测，`jstack` 输出 `Found one Java-level deadlock`。
- 减少锁粒度或用单锁，把两次加锁合并为一次。

## 答案位置

```text
answers/<用户名>/q02-deadlock/org/practice/answers/<用户名>/q02/Q02AnswerImpl.java
```

## 判题

- 相反顺序 + 屏障制造确定交错，`findDeadlockedThreads` 必须检测到死锁；
- 固定顺序版本并发执行 2000 次不能死锁、不能失败；
- 另一线程持锁时 `tryLockBoth` 返回 false、等待接近超时、且不残留已获取的锁。
