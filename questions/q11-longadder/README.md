# Q11 用 LongAdder 替代 AtomicLong 的场景

## 场景

高并发计数（如 QPS 统计），`AtomicLong` 成为瓶颈。

## 要求

1. 说明 `AtomicLong` 在超高并发下的瓶颈（CAS 自旋失败）。
2. 用 `LongAdder` 改写，解释其“分段累加 + 最终汇总”原理。
3. 什么场景不适合用 `LongAdder`？（如需要精确的实时快照、需要 CAS 更新为特定值）

## 契约

实现 `org.practice.api.Q11Counter`（`increment / add / sum`），内部必须持有 `LongAdder` 字段。

## 考察点

伪共享、Cell 分段、`sum()` 的非原子性。

## 参考要点

- `LongAdder` 内部 `Cell[]` 数组，线程按 hash 分散到不同 Cell，减少竞争；`sum()` 遍历累加，非强一致。
- 需要“读多写少且要精确值”或“需要 `getAndSet`”时用 `AtomicLong`。
- 高并发写、低频读的计数器才适合 `LongAdder`。

## 答案位置

```text
answers/<用户名>/q11-longadder/org/practice/answers/<用户名>/q11/Q11AnswerImpl.java
```

## 判题

- 反射检查答案类必须声明 `LongAdder` 字段；
- 8 线程 × 10 万次自增，`sum()` 必须精确等于 80 万，减法后归零。
