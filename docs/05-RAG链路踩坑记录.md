RedisVectorStore 只能用 database 0（RediSearch 硬限制）

RedisVectorStore 检索时不返回 metadata（1.0.0-M6 的已知行为）

解决方案：text 加 poiId: 前缀 + 正则提取

删索引后必须重启项目，afterPropertiesSet 才会重建索引
