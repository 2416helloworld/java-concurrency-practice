# Q07 Semaphore 实现限流器 + 连接池

## 场景

一个数据库连接池，最多 5 个连接，获取连接超时 1 秒。

## 要求

1. 用 `Semaphore` 实现获取/归还，`tryAcquire` 带超时。
2. 连接对象用一个 `BlockingQueue` 存放，与 `Semaphore` 如何配合？
3. 如果连接在使用中抛异常，如何保证一定归还？

## 契约

实现 `org.practice.api.Q07ConnectionPool`：

- 构造方法参数为池大小 `int`；
- `acquire(timeoutMillis)`：超时抛 `TimeoutException`；
- `release(connection)`：重复归还应被忽略；
- `availableConnections()`：返回当前剩余可借数量。

## 考察点

`Semaphore`、资源池、`finally` 归还、泄漏防护。

## 参考要点

- `semaphore.tryAcquire(1, SECONDS)` 控制并发数，`queue.poll()` 拿真实连接。
- 归还必须在 `finally`：`queue.offer(conn); semaphore.release();`。
- 可加“借出时间戳”做泄漏检测（借出超时告警）。

## 答案位置

```text
answers/<用户名>/q07-semaphore-pool/org/practice/answers/<用户名>/q07/Q07AnswerImpl.java
```

## 判题

- 借满 5 个后第 6 次获取超时，等待时间接近超时上限；
- 归还一个后可再次借出，且连接 id 复用；
- 使用中抛异常但 `finally` 归还原，许可数必须恢复。
