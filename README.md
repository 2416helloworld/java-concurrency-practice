# Java 并发练习

用 Maven 多模块组织：题目接口固定，用户只写答案，判题测试统一运行。

## 目录结构

```text
api/     题目接口（稳定，用户不改）
answers/ 用户答案，按 用户名/题目 存放，包路径必须与目录一致
judge/   判题测试（用户不改）
questions/ 题目描述（只读）
docs/solutions/ 官方参考实现（不参与编译）
```

## 作答

在 `answers/<用户名>/<题目目录>/` 下按包路径新建实现类，例如：

```text
answers/alice/q01-singleton/org/practice/answers/alice/q01/Q01AnswerImpl.java
```

## 判题

```bash
mvn -Danswer.user=alice -Danswer.q=q01-singleton verify
```

`answer.user` 指定用户名，`answer.q` 指定题目目录名，`answer.impl` 默认为
`org.practice.answers.${answer.user}.q01.Q01AnswerImpl`，需要时可用 `-Danswer.impl=...` 覆盖。
