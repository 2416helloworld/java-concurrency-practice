# Q03 手写简易读写锁

## 场景

不用 `ReentrantReadWriteLock`，基于 `ReentrantLock` + `Condition` 实现一个读写锁。

## 要求

1. 支持多个读线程并发、写线程独占。
2. 写线程优先（避免写饥饿）。
3. 说明你的实现如何处理“读锁持有期间有写请求”。

## 契约

实现 `org.practice.api.Q03ReadWriteLock` 的四个方法：`lockRead / unlockRead / lockWrite / unlockWrite`。
不支持锁升级/降级。

## 考察点

读写锁语义、`Condition` 使用、公平性/饥饿问题。

## 参考要点

- 用 `int readers` 计读线程数，`boolean writer` 标记写。
- 写优先：增加 `waitingWriters` 计数，当有写等待时，新来的读也要排队。
- 读锁释放唤醒写；写锁释放 `signalAll`。注意不支持读写升级/降级。
- 等待必须用 `while` 循环，防止虚假唤醒和条件再次变化。

## 答案位置

```text
answers/<用户名>/q03-readwrite-lock/org/practice/answers/<用户名>/q03/Q03AnswerImpl.java
```

## 判题

- 读锁持有期间写线程阻塞，读锁释放后写线程进入；
- 多个读线程可同时进入；
- 存在等待的写线程时，新读线程必须排队，写线程先拿到写锁。
