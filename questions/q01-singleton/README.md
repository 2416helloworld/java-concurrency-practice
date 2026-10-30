# Q01 线程安全的单例（双重检查锁）

## 场景

实现一个懒加载的单例，要求线程安全、高性能。

## 要求

1. 用双重检查锁（DCL）实现。
2. 说明 `volatile` 为什么不可或缺（从指令重排角度解释）。
3. 再给出一个“静态内部类”实现，并对比两者优劣。

## 契约

- 实现 `org.practice.api.Q01Singleton`；
- 构造函数不能是 `public`；
- 提供 `public static Q01Singleton getInstance()`，多次调用（包括并发调用）返回同一实例。

## 考察点

`volatile` 语义、DCL 的隐患、类加载机制。

## 参考要点

- `instance = new Singleton()` 分三步：分配内存 → 初始化对象 → 引用指向内存。第 2、3 步可能重排，
  其他线程可能拿到“非 null 但尚未初始化完成”的对象；`volatile` 禁止该重排。
- 静态内部类利用 JVM 类加载的线程安全 + 懒加载（首次访问才加载），代码更简洁，通常更推荐。
- 静态内部类无法传递构造参数；DCL 可以，但写法更容易出错。

## 答案位置

```text
answers/<用户名>/q01-singleton/org/practice/answers/<用户名>/q01/Q01AnswerImpl.java
```

## 判题

```bash
mvn -Danswer.user=<用户名> verify
```

判题检查：实现接口、`getInstance` 为静态且返回 `Q01Singleton`、构造函数非 public、顺序与 32 线程并发下实例唯一。
