-- ============================================================
-- V6: route_node.poi_id 从 BIGINT 改为 VARCHAR(64)
-- 背景：前端改为直接使用高德 PlaceSearch 返回的字符串 POI ID
--      （如 "B02F506RX3"），原来的 BIGINT 无法存储，且反序列化会报 500。
-- ============================================================
ALTER TABLE route_node
    MODIFY COLUMN poi_id VARCHAR(64) NOT NULL COMMENT 'POI 标识（高德字符串 ID 或本地 POI ID）';
