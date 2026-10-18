-- ============================================================
-- Migration: 一次性对齐 schema
-- 补齐所有「代码已用到、但存量库可能缺失」的表与列
--
-- 背景：前端「保存路线 / AI 优化」报 500，日志提示
--       java.sql.SQLSyntaxErrorException: 表 'citywalk.route_ticket' 不存在
-- 审计结论：database.sql / community.sql 本身已是最新（10 张表 + 全部列都在），
--       但较早初始化的老库可能缺以下内容：
--         表：route_ticket、post、post_like、post_comment
--         列：city.slogan/intro
--             poi.amap_id/photos/restaurant/avoid_tip
--             route.is_official
-- 特点：全程幂等（建表用 IF NOT EXISTS；加列用 information_schema 判断），
--       可安全重复执行，不会因表/列已存在而报错。
-- 用法：mysql -u root -p < migration-create-all-missing-tables.sql
--       或在 MySQL Workbench 里选中 citywalk 库执行
-- ============================================================

USE citywalk;

-- ============================================================
-- 1. 缺失的表
-- ============================================================

-- 计划绑定车次表
CREATE TABLE IF NOT EXISTS route_ticket (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    route_id     BIGINT NOT NULL COMMENT '所属计划 route.id',
    train_code   VARCHAR(50)  COMMENT '车次号，如 G1556',
    from_station VARCHAR(100) COMMENT '出发站',
    to_station   VARCHAR(100) COMMENT '到达站',
    start_time   VARCHAR(10)  COMMENT '出发时间 HH:mm',
    arrive_time  VARCHAR(10)  COMMENT '到达时间 HH:mm',
    travel_date  VARCHAR(10)  COMMENT '乘车日期 YYYY-MM-DD',
    prices_json  TEXT         COMMENT '各席别价格 JSON，如 {"二等座":"15.0"}',
    sort_order   INT DEFAULT 0 COMMENT '车次顺序',
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_route_ticket (route_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计划绑定的车次';

-- 社区帖子表
CREATE TABLE IF NOT EXISTS post (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT NOT NULL COMMENT '发帖用户',
    poi_id     BIGINT NULL COMMENT '关联 POI',
    route_id   BIGINT NULL COMMENT '关联路线（预留）',
    title      VARCHAR(255) COMMENT '标题',
    content    TEXT COMMENT '正文',
    images     TEXT COMMENT '图片 URL JSON 数组，如 ["/uploads/a.jpg"]',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_poi (poi_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区帖子';

-- 帖子点赞表
CREATE TABLE IF NOT EXISTS post_like (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT NOT NULL COMMENT '点赞用户',
    post_id    BIGINT NOT NULL COMMENT '帖子',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_post (user_id, post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞';

-- 帖子评论表
CREATE TABLE IF NOT EXISTS post_comment (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT NOT NULL COMMENT '评论用户',
    post_id    BIGINT NOT NULL COMMENT '帖子',
    content    TEXT COMMENT '评论内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_post (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子评论';

-- ============================================================
-- 2. 缺失的列（幂等加列：仅在列不存在时执行 ALTER）
-- ============================================================

-- city.slogan
SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'city' AND COLUMN_NAME = 'slogan');
SET @s = IF(@c = 0, 'ALTER TABLE city ADD COLUMN slogan VARCHAR(255) NULL COMMENT ''一句话标语'' AFTER cover_image', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- city.intro
SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'city' AND COLUMN_NAME = 'intro');
SET @s = IF(@c = 0, 'ALTER TABLE city ADD COLUMN intro TEXT NULL COMMENT ''城市介绍'' AFTER slogan', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- poi.amap_id
SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'poi' AND COLUMN_NAME = 'amap_id');
SET @s = IF(@c = 0, 'ALTER TABLE poi ADD COLUMN amap_id VARCHAR(64) NULL COMMENT ''高德 POI ID''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- poi.photos
SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'poi' AND COLUMN_NAME = 'photos');
SET @s = IF(@c = 0, 'ALTER TABLE poi ADD COLUMN photos TEXT NULL COMMENT ''照片 JSON 数组''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- poi.restaurant
SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'poi' AND COLUMN_NAME = 'restaurant');
SET @s = IF(@c = 0, 'ALTER TABLE poi ADD COLUMN restaurant VARCHAR(500) NULL COMMENT ''推荐餐厅/美食''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- poi.avoid_tip
SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'poi' AND COLUMN_NAME = 'avoid_tip');
SET @s = IF(@c = 0, 'ALTER TABLE poi ADD COLUMN avoid_tip VARCHAR(500) NULL COMMENT ''避开时段提示''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- route.is_official
SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'route' AND COLUMN_NAME = 'is_official');
SET @s = IF(@c = 0, 'ALTER TABLE route ADD COLUMN is_official TINYINT DEFAULT 0 COMMENT ''是否官方 0/1''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
