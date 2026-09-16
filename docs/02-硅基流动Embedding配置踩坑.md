# 硅基流动 Embedding 配置踩坑记录

## 问题现象

- Postman 直接调硅基流动 API 成功（返回 1024 维向量）
- Java 代码调 Embedding 报 400：`Model does not exist`
- 硅基流动的模型是免费的 `BAAI/bge-m3`

## 排查过程

1. **Postman 测试** → 确认硅基流动 API 本身没问题
2. **Java 加日志** → 看到请求发出去了，但返回 400
3. **对比 Postman 和 Java 的请求** → 发现 Java 没传 model 字段

## 根本原因

`EmbeddingConfig.java` 创建 `OpenAiEmbeddingModel` 时，
没有传 `OpenAiEmbeddingOptions`，导致 Spring AI 用了默认模型名
（OpenAI 的 `text-embedding-ada-002`）。

硅基流动不认识这个模型名，返回 `Model does not exist`。

## 解决方案

在 `EmbeddingConfig.java` 里显式指定模型：

\`\`\`java
OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
.model(model)   // 关键：把配置的模型名传进去
.build();

return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, options);
\`\`\`

## Spring AI 1.0.0-M6 的 API 细节

- Builder 方法名是 `model(...)`，不是 `withModel(...)`
- 构造函数是三参数：`(OpenAiApi, MetadataMode, OpenAiEmbeddingOptions)`

## 排查心得

"Postman 能通、Java 不通"的通用排查思路：
1. Postman 先测原始 API（确认外部服务本身没问题）
2. Java 加日志看实际请求 URL 和 Body
3. 对比两者差异，定位到具体字段
4. 改配置或代码

**核心规律**：配置文件里的参数不会自动生效，代码必须显式读取、显式使用。