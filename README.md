# Citywalk AI 旅行规划平台

基于 RAG 的城市漫步路线生成工具。用户通过 3D 地球选城市，AI 基于真实 POI 数据生成 Citywalk 路线，支持多维度偏好筛选（出片 / 美食 / 历史 / 文艺）。
## 技术栈

**后端**：Spring Boot 3.3.5 + Spring AI + MyBatis-Plus + MySQL + Redis
**前端**：（待补充）

## 核心功能

- 3D 地球选城市 + 城市解说
- AI 路线生成（RAG 检索 + LLM 编排）
- 12306 票价查询
- 路线保存、编辑、分享
- 社区（帖子、点赞、评论）
- 个人中心（我的计划、收藏、行程）

## 快速开始

### 环境要求

- JDK 17
- MySQL 8.x
- Redis 7.x
- Maven 3.8+

### 配置

1. 克隆项目
2. 复制 `src/main/resources/application.yaml.example` 为 `application.yaml`
3. 填入你的配置：
   - MySQL 用户名密码
   - Redis 地址
   - DeepSeek API Key
   - 高德地图 API Key

### 启动

\`\`\`bash
mvn spring-boot:run
\`\`\`

访问 `http://localhost:8080/api/city/list`

## 项目结构

\`\`\`
src/main/java/com/citywalk/backend/
├── controller/     # 接口层
├── service/        # 业务逻辑层
├── mapper/         # 数据访问层
├── entity/         # 实体类
└── config/         # 配置类（待补充）
\`\`\`

## 开发进度

- [x] 项目初始化
- [x] 城市列表接口
- [ ] POI 数据入库
- [ ] RAG 检索链路
- [ ] AI 路线生成
