-- Citywalk 迁移脚本：给 city 表补充 slogan / intro 字段
-- 用于「表已存在」的存量数据库，执行一次即可。
-- 用法：mysql -u root -p citywalk < docs/migration-add-city-slogan-intro.sql
-- 注意：重复执行会报 “Duplicate column name”，属正常现象，可忽略。

USE citywalk;

ALTER TABLE city
    ADD COLUMN slogan VARCHAR(255) NULL COMMENT '一句话标语' AFTER cover_image,
    ADD COLUMN intro TEXT NULL COMMENT '城市介绍' AFTER slogan;
