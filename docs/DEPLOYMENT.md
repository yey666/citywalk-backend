# 数据库部署与迁移说明

本文说明 Citywalk 数据库的初始化/对齐顺序，以及 `docs/` 下所有 SQL 文件的用途。

## SQL 文件清单

| 文件 | 类型 | 作用 |
|------|------|------|
| `database.sql` | 基础建表 | 核心 7 张表（city / poi / route / route_node / users / user_route / route_ticket）+ 5 个初始城市数据 |
| `community.sql` | 基础建表 | 社区 3 张表（post / post_like / post_comment） |
| `migration-create-all-missing-tables.sql` | **对齐脚本** | 幂等补齐所有「代码已用到、但库中可能缺失」的表与列 |
| `migration-create-route-ticket.sql` | 增量迁移 | 单独建 route_ticket（已被上面的对齐脚本覆盖） |
| `migration-add-city-slogan-intro.sql` | 增量迁移 | city 表补 slogan / intro（已被对齐脚本覆盖） |
| `migration-add-poi-route-columns.sql` | 增量迁移 | poi/route 补 amap_id 等列（已被对齐脚本覆盖） |
| `migration-fill-city-slogan-intro.sql` | 数据回填 | 填充 city 的 slogan / intro 文案 |
| `fix-city-encoding.sql` | 数据修复 | 修复 city 表乱码 |
| `fix-poi-route-encoding.sql` | 数据修复 | 修复 poi / route 表乱码 |
| `foshan-data.sql` | 数据 | 佛山核心景点 + 官方路线数据 |

## 推荐部署顺序

新环境首次初始化：

```bash
# 1. 建库 + 基础表（database.sql 自带 CREATE DATABASE + USE citywalk）
mysql -u root -p < docs/database.sql

# 2. 社区表
mysql -u root -p citywalk < docs/community.sql

# 3. 幂等对齐增量（保证任何历史版本的老库都收敛到最新 schema）
mysql -u root -p citywalk < docs/migration-create-all-missing-tables.sql
```

老库升级 / 已初始化过数据库的环境：

```bash
# 只跑这一条即可（幂等，可重复执行，不会因表/列已存在而报错）
mysql -u root -p citywalk < docs/migration-create-all-missing-tables.sql
```

可选：数据与修复脚本（按需执行）

```bash
mysql -u root -p citywalk < docs/foshan-data.sql
mysql -u root -p citywalk < docs/migration-fill-city-slogan-intro.sql
```

## 关键说明

- **`database.sql` / `community.sql` 始终是最新版本**（已包含全部 10 张表与全部列），
  新环境直接跑它们即可。
- **`migration-create-all-missing-tables.sql` 是幂等的**：
  - 建表用 `CREATE TABLE IF NOT EXISTS`；
  - 加列用 `information_schema.COLUMNS` 判断列是否存在，不存在才 `ALTER TABLE ADD COLUMN`。
  所以可以放心重复执行，适合做「老库对齐」。
- 为什么需要对齐脚本：老库初始化时用的是旧版 `database.sql`，后来新增了
  `route_ticket` 表、社区三张表，以及 `city.slogan/intro`、`poi.amap_id/photos/restaurant/avoid_tip`、
  `route.is_official` 等列；而 `CREATE TABLE IF NOT EXISTS` 不会给已存在的表补列，
  所以需要一个幂等的对齐脚本统一收敛。
- 执行完 SQL **不需要重启后端**（MyBatis-Plus 运行时动态查表）。
