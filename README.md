# Java 并发练习

用 Maven 多模块组织：题目接口固定，用户只写答案，判题测试统一运行。

## 目录结构

```text
api/     题目接口（稳定，用户不改）
answers/ 用户答案，按 用户名/题目 存放，包路径必须与目录一致
judge/   判题测试（用户不改）
questions/ 题目描述（只读），索引见 questions/README.md（Q01-Q12，Q04 为文档题）
docs/solutions/ 官方参考实现（不参与编译）
```

## 作答

在 `answers/<用户名>/<题目目录>/` 下按包路径新建实现类，例如第 2 题：

```text
answers/alice/q02-deadlock/org/practice/answers/alice/q02/Q02AnswerImpl.java
```

包名固定为 `org.practice.answers.<用户名>.qNN`，实现类固定为 `QNNAnswerImpl`。

## 判题

```bash
mvn -Danswer.user=alice verify
```

`answer.user` 指定用户名，默认 `alice`；`judge` 会按命名约定加载该用户所有题目的答案。
**未提交的题目自动跳过**，已提交但包名/类名不匹配则判为失败。

## 提 PR

1. 新建分支，只在自己用户名目录下加答案：

   ```bash
   git switch -c answer/q02
   git add answers/<用户名>
   git commit -m "提交 Q02 答案"
   git push -u origin answer/q02
   ```

2. 在 GitHub 上发起 Pull Request 到 `main`。
3. CI 会从 PR 的变更文件里自动识别 `answers/<用户名>`，只判该用户的答案；
   没改答案目录时（例如只改文档）则退回为判所有用户。

