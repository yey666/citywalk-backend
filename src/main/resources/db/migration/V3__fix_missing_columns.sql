-- ============================================================
-- V3: 补齐缺失的列（幂等加列）
-- 背景：老库 / 手动建的表可能缺以下列，这里用 information_schema 判断，
--       仅在列不存在时才 ALTER TABLE ADD COLUMN，可安全重复执行。
-- ============================================================

-- ---- city 表 ----
SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'city' AND COLUMN_NAME = 'slogan');
SET @s = IF(@c = 0, 'ALTER TABLE city ADD COLUMN slogan VARCHAR(255) NULL COMMENT ''一句话标语'' AFTER cover_image', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'city' AND COLUMN_NAME = 'intro');
SET @s = IF(@c = 0, 'ALTER TABLE city ADD COLUMN intro TEXT NULL COMMENT ''城市介绍'' AFTER slogan', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---- poi 表 ----
SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'poi' AND COLUMN_NAME = 'amap_id');
SET @s = IF(@c = 0, 'ALTER TABLE poi ADD COLUMN amap_id VARCHAR(64) NULL COMMENT ''高德 POI ID''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'poi' AND COLUMN_NAME = 'photos');
SET @s = IF(@c = 0, 'ALTER TABLE poi ADD COLUMN photos TEXT NULL COMMENT ''照片 JSON 数组''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'poi' AND COLUMN_NAME = 'restaurant');
SET @s = IF(@c = 0, 'ALTER TABLE poi ADD COLUMN restaurant VARCHAR(500) NULL COMMENT ''推荐餐厅/美食''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'poi' AND COLUMN_NAME = 'avoid_tip');
SET @s = IF(@c = 0, 'ALTER TABLE poi ADD COLUMN avoid_tip VARCHAR(500) NULL COMMENT ''避开时段提示''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---- route 表 ----
SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'route' AND COLUMN_NAME = 'is_official');
SET @s = IF(@c = 0, 'ALTER TABLE route ADD COLUMN is_official TINYINT DEFAULT 0 COMMENT ''是否官方 0/1''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---- users 表（手动建的表可能缺这些字段） ----
SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'username');
SET @s = IF(@c = 0, 'ALTER TABLE users ADD COLUMN username VARCHAR(50) NULL COMMENT ''用户名''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'password_hash');
SET @s = IF(@c = 0, 'ALTER TABLE users ADD COLUMN password_hash VARCHAR(255) NULL COMMENT ''密码哈希''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'nickname');
SET @s = IF(@c = 0, 'ALTER TABLE users ADD COLUMN nickname VARCHAR(50) NULL COMMENT ''昵称''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'avatar');
SET @s = IF(@c = 0, 'ALTER TABLE users ADD COLUMN avatar VARCHAR(255) NULL COMMENT ''头像 URL''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'role');
SET @s = IF(@c = 0, 'ALTER TABLE users ADD COLUMN role VARCHAR(20) DEFAULT ''user'' COMMENT ''user/admin''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'created_at');
SET @s = IF(@c = 0, 'ALTER TABLE users ADD COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'updated_at');
SET @s = IF(@c = 0, 'ALTER TABLE users ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
