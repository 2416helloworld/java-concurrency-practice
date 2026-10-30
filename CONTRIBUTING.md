# 贡献指南

## 添加答案

1. 在 `answers/<你的用户名>/<题目目录>/` 下创建实现类，包名固定为
   `org.practice.answers.<你的用户名>.qNN`，实现类固定为 `QNNAnswerImpl`。
2. 实现类需实现 `api` 模块中对应的题目接口，并满足题目 README 中的约定。
3. 本地验证：

   ```bash
   mvn -Danswer.user=<你的用户名> verify
   ```

## 注意

- 不要修改 `api/`、`judge/`、`questions/`。
- 不要修改根 `pom.xml` 中的判题属性默认值。
- 答案中不要引入额外依赖。
