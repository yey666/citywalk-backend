# 硅基流动 Embedding 配置踩坑记录

## 问题现象

Postman 调硅基流动 API 成功，但 Java 代码报 `400 Model does not exist`。

## 排查过程

1. Postman 测试 → 确认硅基流动 API 本身没问题
2. Java 代码加日志 → 看到请求发出去了，但返回 400
3. 对比 Postman 和 Java 的请求 → 发现 Java 没传 model 字段

## 根本原因

Spring AI 的 `OpenAiEmbeddingModel` 默认使用 OpenAI 的模型名，
没有传我在 `application.yaml` 里配的 `BAAI/bge-m3`。

## 解决方案

在 `EmbeddingConfig.java` 里显式指定模型：

\`\`\`java
OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
.model(model)
.build();

return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, options);
\`\`\`

注意 Spring AI 1.0.0-M6 的 API 细节：
- Builder 方法是 `model(...)` 不是 `withModel(...)`
- 构造函数是三参数 `(OpenAiApi, MetadataMode, OpenAiEmbeddingOptions)`

## 排查心得

"Postman 能通、Java 不通"这类问题的通用排查思路：
1. Postman 先测原始 API
2. Java 加日志看实际请求
3. 对比差异，定位到具体字段