# Q10 ABA 问题复现与解决

## 场景

用 CAS 实现一个无锁栈的 pop，复现 ABA 问题。

## 要求

1. 写出会导致 ABA 的代码（线程 1 读 A，线程 2 把 A→B→A，线程 1 CAS 成功但栈已变）。
2. 用 `AtomicStampedReference` 解决，说明版本号如何工作。
3. 讨论 `AtomicMarkableReference` 的适用场景。

## 契约

实现 `org.practice.api.Q10AbaDemo`：

- `reproduceWithAtomicReference()`：用 `AtomicReference` 实现的 Treiber 栈，执行同一段交错后返回 CAS 是否成功（应返回 true）；
- `fixWithAtomicStampedReference()`：同样交错，但用版本号，返回 CAS 是否成功（应返回 false）。

## 考察点

CAS、ABA、版本号、引用原子类。

## 参考要点

- `AtomicStampedReference.compareAndSet(expectedRef, newRef, expectedStamp, newStamp)`。
- `AtomicMarkableReference` 只用一个 boolean 标记（关心“是否被改过”而非改了几次）。
- 典型 ABA 场景：无锁栈/队列节点复用、内存回收后地址复用。

## 答案位置

```text
answers/<用户名>/q10-aba/org/practice/answers/<用户名>/q10/Q10AnswerImpl.java
```

## 判题

- `AtomicReference` 版本必须返回 true（ABA 复现）；
- `AtomicStampedReference` 版本必须返回 false（版本号拦截）。
