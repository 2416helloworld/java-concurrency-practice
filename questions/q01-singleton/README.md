# Q01 线程安全的单例

实现 `org.practice.api.Q01Singleton`，保证：

1. 全局只有一个实例；
2. 构造函数不能是 `public`；
3. 提供 `public static Q01Singleton getInstance()`，多次调用（包括并发调用）返回同一个实例。

## 答案位置

```text
answers/<用户名>/q01-singleton/org/practice/answers/<用户名>/q01/Q01AnswerImpl.java
```

包名中的题目短名固定为 `q01`，实现类名固定为 `Q01AnswerImpl`。

## 判题

```bash
mvn -Danswer.user=<用户名> -Danswer.q=q01-singleton verify
```
