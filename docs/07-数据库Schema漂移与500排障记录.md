# 07 - 数据库 Schema 漂移导致接口 500 的排障与治理

## 1. 故障背景
项目在本地开发环境运行正常，但部署至阿里云服务器（基于 Docker 容器化）后，前端多个核心模块（社区、我的计划、工作台）在发起请求时大面积返回 `500 Internal Server Error`。前端控制台仅提示 `AxiosError: Request failed with status code 500`，无法直接获取后端具体异常。

## 2. 故障排查链路

为定位问题，排查过程遵循“由表及里”的原则，直接查看后端服务运行日志（`app.log`）以捕获具体异常堆栈。

### 2.1 缺失表与字段（Schema 不匹配）
- **日志异常**：`java.sql.SQLSyntaxErrorException: Unknown column 'slogan' in 'field list'` 及 `Table 'citywalk.route_ticket' doesn't exist`。
- **根因分析**：服务器端的 MySQL 数据库为一个旧版本的快照。随着业务迭代，本地代码实体类新增了字段（如 `slogan`、`intro` 等）及新增表（如 `route_ticket`）。由于历史建表脚本采用了 `CREATE TABLE IF NOT EXISTS`，在服务器重复执行时，已存在的表被整体跳过，导致新增的列无法被追加。
- **初步修复**：编写并执行基于 `information_schema` 判断的幂等 SQL 迁移脚本，动态判断并补齐缺失的列与表。

### 2.2 缺失业务关联表
- **日志异常**：`Table 'citywalk.users' doesn't exist`。
- **根因分析**：社区模块（`PostServiceImpl`）在组装帖子列表时，需通过 `userMapper.selectBatchIds` 查询作者信息。然而，服务器数据库缺失 `users` 表（历史脚本遗漏）。由于应急手动建表时字段不全，导致后续连环报错。
- **后续修复**：针对 `users` 表补充 `password_hash`、`updated_at` 等与实体类映射一致的字段。

## 3. 根因总结：Schema 漂移（Schema Drift）
此次连环 500 故障的本质是典型的 **数据库结构漂移**。具体原因如下：
1. **脚本执行遗漏**：核心表与业务扩展表拆分在 `database.sql` 与 `community.sql` 中，线上部署时遗漏执行后者。
2. **缺乏版本控制**：项目未引入数据库版本管理工具，本地与服务器之间的结构同步完全依赖手动操作，极易出现遗漏。
3. **容器数据持久化**：Docker 部署时，MySQL 容器挂载了旧的数据卷，覆盖了初始化时的结构。

## 4. 长期治理方案：引入 Flyway
为从根本上解决环境间数据库结构不一致的问题，项目已引入 **Flyway** 作为数据库版本控制组件：
- 将现有建表与增量脚本重构为规范化命名（如 `V1__init_schema.sql`、`V2__community_tables.sql`）。
- 在 `application-prod.yaml` 中开启 `spring.flyway.enabled=true`。
- **预期收益**：应用启动时，Flyway 将自动比对本地的迁移脚本与数据库中的 `flyway_schema_history` 表，按序执行未应用的变更，实现代码与数据库结构的强一致性。

## 5. 复盘与最佳实践
1. **日志驱动排障**：前端 500 为表象，必须依赖后端 `grep` / `tail` 定位 `Caused by` 堆栈。
2. **脚本幂等性**：纯 SQL 部署脚本必须保证幂等（重复执行不报错），推荐使用 `IF NOT EXISTS` 及系统表判断。
3. **自动化迁移**：多环境部署必须引入 Flyway 或 Liquibase，避免人工同步结构。
4. **环境隔离与网络**：注意 Docker 容器网络配置，容器内连接同网络服务应使用服务名（如 `citywalk-mysql`）而非 `localhost`。