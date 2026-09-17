# Citywalk AI 旅行规划平台

基于 RAG 的城市漫步路线生成工具。用户通过 3D 地球选城市，AI 基于真实 POI 数据生成 Citywalk 路线，支持多维度偏好筛选（出片 / 美食 / 历史 / 文艺）。

## 技术栈

**后端**
- Spring Boot 3.3.5
- Spring AI 1.0.0-M6
- MyBatis-Plus 3.5.7
- MySQL 8.0
- Redis Stack（RediSearch 向量检索）
- Knife4j 4.5.0（接口文档）
- JWT + BCrypt（认证）

**外部服务**
- 高德地图 API（POI 数据）
- 硅基流动（Embedding，BAAI/bge-m3）
- DeepSeek（LLM，deepseek-chat）
- MCP Server 12306（火车票查询）

**前端**（待开发）
- React + Vite + Tailwind
- react-globe.gl

## 核心功能

### 已实现

- ✅ 城市列表 + 城市详情
- ✅ 城市官方路线列表 + 路线详情
- ✅ AI 路线生成（RAG 检索 + DeepSeek 编排）
- ✅ 12306 票价查询（MCP 协议接入）
- ✅ 用户系统（注册、登录、JWT 拦截器）
- ✅ 路线保存 + 我的路线
- ✅ Swagger/Knife4j 接口文档

### 待实现

- [ ] 社区（帖子、点赞、评论、feed 流）
- [ ] 个人中心（收藏、发布、多天计划）
- [ ] 图片上传
- [ ] Redis 两级缓存（12306 车次 + 余票）
- [ ] AI 接口限流（Redis 令牌桶）
- [ ] SSE 流式返回
- [ ] 前端（3 个主界面 + 2 个详情页）

## 快速开始

### 环境要求

- JDK 17
- Maven 3.8+
- MySQL 8.x
- Docker Desktop（用于 Redis Stack 和 MCP Server 12306）
- IDEA

### 依赖的外部服务

| 服务 | 用途 | 获取方式 |
|------|------|----------|
| 高德开放平台 | POI 数据 | 免费申请「Web 服务」Key |
| 硅基流动 | Embedding | 免费注册，模型选 BAAI/bge-m3 |
| DeepSeek | LLM | 充值 20 元够开发用 |
| MCP Server 12306 | 火车票查询 | Docker 一键部署 |

### 启动步骤

**1. 启动 Redis Stack**

```bash
docker run -d --name redis-citywalk -p 6379:6379 redis/redis-stack-server:7.2.0-v6
2. 启动 MCP Server 12306

bash
docker run -d -p 8000:8000 --name mcp-12306 drfccv/mcp-server-12306:latest
3. 初始化数据库

sql
CREATE DATABASE citywalk DEFAULT CHARACTER SET utf8mb4;
然后执行 docs/database.sql 里的建表语句。

4. 配置 application.yaml

复制 src/main/resources/application.yaml.example 为 application.yaml，填入：

MySQL 用户名密码

高德 Key

硅基流动 Key

DeepSeek Key

JWT secret（至少 32 字符）

5. 启动项目

bash
mvn spring-boot:run
6. 访问接口文档

text
http://localhost:8080/doc.html
项目结构
text
src/main/java/com/citywalk/backend/
├── controller/          # 接口层
│   ├── CityController
│   ├── RouteController
│   ├── RagController
│   ├── AuthController
│   ├── PoiController
│   └── TrainController
├── service/             # 业务逻辑层
│   └── impl/
├── mapper/              # 数据访问层
├── entity/              # 实体类
├── dto/                 # 数据传输对象
├── config/              # 配置类
│   ├── AmapConfig
│   ├── EmbeddingConfig
│   ├── JwtInterceptor
│   ├── WebMvcConfig
│   └── Knife4jConfig
└── util/                # 工具类
    ├── AmapPoiFetcher
    ├── JwtUtil
    └── UserContext
核心技术点
1. RAG 检索链路
text
用户输入："苏州 出片 半天"
    ↓
查询向量化（硅基流动 BAAI/bge-m3）
    ↓
Redis Stack 向量检索 → Top 10 POI
    ↓
结构化过滤（城市、距离、时长）
    ↓
DeepSeek 编排生成路线
    ↓
返回结构化 JSON
踩过的坑：

Redis VectorStore 只能用 database 0（RediSearch 硬限制）

Spring AI 1.0.0-M6 检索时不返回 metadata → 解决方案：text 加 poiId: 前缀 + 正则提取

OpenAiEmbeddingModel 必须传 options，否则用默认模型名

2. 12306 票价查询（MCP 协议）
MCP（Model Context Protocol）不是普通 REST API，调用流程：

initialize → 拿 mcp-session-id（响应头）

tools/call → 调 query-ticket-price 工具

解析 SSE 流（text/event-stream），提取 data: {...} 行

UTF-8 解码（RestTemplate 默认用 ISO-8859-1，中文会乱码）

3. 用户系统
BCrypt 密码加密

JWT 生成和校验（HS256）

JwtInterceptor + UserContext（ThreadLocal）存当前用户

拦截器保护 /api/user/** 和 /api/route/*/save

接口清单
城市
方法	路径	说明
GET	/api/city/list	城市列表
GET	/api/city/{id}/overview	城市详情
路线
方法	路径	说明
GET	/api/city/{id}/routes	城市官方路线
GET	/api/route/{id}	路线详情
POST	/api/route/{id}/save	保存路线
GET	/api/user/routes	我的路线
AI 规划
方法	路径	说明
POST	/api/rag/generate-route	AI 生成路线
GET	/api/rag/init-poi-vectors	POI 向量化
GET	/api/rag/search	向量检索测试
GET	/api/rag/test-embedding	Embedding 测试
认证
方法	路径	说明
POST	/api/auth/register	注册
POST	/api/auth/login	登录
票价
方法	路径	说明
GET	/api/train/query	12306 票价查询
开发进度
☑ 环境搭建
☑ 数据库设计
☑ POI 数据入库（高德 API）
☑ 城市接口
☑ RAG 检索链路
☑ AI 路线生成
☑ 路线保存
☑ 用户系统
☑ 12306 票价查询
☑ Swagger 接口文档
□ 社区
□ 个人中心
□ 前端
踩坑文档
详见 docs/ 目录：

01-环境搭建踩坑.md

02-硅基流动Embedding配置踩坑.md

03-RedisStack镜像拉取踩坑.md

04-RAG检索链路设计.md

License
MIT

text

---

## 📌 操作步骤

1. **打开 `README.md`**
2. **全选删除**
3. **粘贴上面这版**
4. **保存**
5. **提交**：

```bash
git add README.md
git commit -m "docs: 更新 README，反映当前完成的功能"
git push