-- Citywalk 迁移脚本：给 poi / route 表补齐实体新增字段
-- 背景：Poi / Route 实体后来新增了 avoid_tip、restaurant、photos、amap_id、is_official，
--       但存量数据库缺这些列，导致 /api/poi/by-city 与 /api/city/{id}/routes 报 500。
-- 用法：mysql -u root -p citywalk < docs/migration-add-poi-route-columns.sql
-- 注意：重复执行会报 “Duplicate column name”，属正常现象，可忽略。

USE citywalk;

ALTER TABLE poi
    ADD COLUMN amap_id VARCHAR(64) NULL COMMENT '高德 POI ID',
    ADD COLUMN photos TEXT NULL COMMENT '照片 JSON 数组',
    ADD COLUMN restaurant VARCHAR(500) NULL COMMENT '推荐餐厅/美食',
    ADD COLUMN avoid_tip VARCHAR(500) NULL COMMENT '避开时段提示';

ALTER TABLE route
    ADD COLUMN is_official TINYINT DEFAULT 0 COMMENT '是否官方 0/1';
