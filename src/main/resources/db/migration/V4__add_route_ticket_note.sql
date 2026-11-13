-- ============================================================
-- V4: route_ticket 增加 note 字段（城际交通备注，如"自驾/大巴/拼车"）
-- 幂等：仅当列不存在时才 ALTER TABLE ADD COLUMN，可安全重复执行。
-- ============================================================

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'route_ticket' AND COLUMN_NAME = 'note');
SET @s = IF(@c = 0, 'ALTER TABLE route_ticket ADD COLUMN note VARCHAR(255) NULL COMMENT ''交通备注，如自驾/大巴/拼车'' AFTER sort_order', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
