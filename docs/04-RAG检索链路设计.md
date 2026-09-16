## RAG 检索链路设计

### 整体流程
1. POI 数据从 MySQL 读取
2. 拼接文本（name + category + address）
3. 向量化（硅基流动 BAAI/bge-m3）
4. 存入 Redis Stack（poi-index 索引）
5. 用户查询向量化
6. 相似度检索返回 Top K

### 关键决策
- Redis VectorStore 只能用 database 0（RediSearch 硬限制）
- OpenAiEmbeddingModel 必须传 options，否则用默认模型名
- Spring AI 1.0.0-M6 API：model() 不是 withModel()

## POI 数据质量问题

### 问题现象
检索"苏州出片"返回了"中国铁建""京东之家"等无关 POI。

### 原因分析
1. 高德 API 关键词太宽泛，混入商业 POI
2. 缺少旅行视角字段（photo_score、photo_spot 等）
3. 向量检索在没有相关 POI 时，返回"随机"结果

### 解决方向
1. 换关键词（用类别词而非具体名称）
2. 高德类别过滤（只保留风景名胜、博物馆等）
3. 人工精选 + 人工补字段

### 结论
RAG 的效果上限由知识库质量决定，不是模型决定的。

## 后续优化计划

- [ ] 换关键词重新拉高德 POI
- [ ] 人工筛选 20-30 个真正的 Citywalk POI
- [ ] 补充 photo_score、photo_spot、description 字段
- [ ] 重新向量化
- [ ] 验证检索质量提升