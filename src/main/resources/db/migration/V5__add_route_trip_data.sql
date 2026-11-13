-- ============================================================
-- V5: route 增加 trip_data_json 字段
-- 用途：把前端整条行程（城市顺序 + 每城景点 + 城际交通备注）以 JSON 存下来，
--       便于 getDetail 反向回显到首页 tripData（清缓存后也能恢复）。
-- 幂等：仅当列不存在时才 ALTER TABLE ADD COLUMN。
-- ============================================================

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'route' AND COLUMN_NAME = 'trip_data_json');
SET @s = IF(@c = 0, 'ALTER TABLE route ADD COLUMN trip_data_json LONGTEXT NULL COMMENT ''前端 tripData 的 JSON 快照（城市+景点+交通备注）'' AFTER ai_generated', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
