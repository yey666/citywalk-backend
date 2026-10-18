-- Citywalk 修复脚本：修正 poi / route 表因导入字符集错误导致的乱码
-- 背景：foshan-data.sql 通过 docker exec 导入时未指定 --default-character-set=utf8mb4，
--       中文被双重编码成乱码（同 city 表）。
-- 用法（关键：必须带 --default-character-set=utf8mb4）：
--   docker exec -i <mysql容器名> mysql -uroot -p --default-character-set=utf8mb4 citywalk < docs/fix-poi-route-encoding.sql
-- 说明：按 id 定位，依据 foshan-data.sql 全新库的插入顺序：poi → 1=佛山祖庙 2=岭南天地 3=南风古灶 4=清晖园 5=梁园，
--       route → 1=禅城文化半日游 2=顺德园林美食游 3=岭南古建摄影路线。
--       若线上 id 不一致（如高德抓取的 POI 排在前面），请先执行：
--         SELECT id, name FROM poi WHERE city_id = 6;
--      确认实际 id 后，把下方 WHERE id 改成对应值。

USE citywalk;

-- 佛山 5 个核心 POI
UPDATE poi SET name = '佛山祖庙', category = '风景名胜', description = '佛山祖庙，始建于北宋元丰年间...' WHERE id = 1;
UPDATE poi SET name = '岭南天地', category = '休闲街区', description = '岭南天地，由佛山祖庙东华里片区改造而成...' WHERE id = 2;
UPDATE poi SET name = '南风古灶', category = '风景名胜', description = '南风古灶，始建于明正德年间...' WHERE id = 3;
UPDATE poi SET name = '清晖园', category = '风景名胜', description = '清晖园，广东四大名园之一...' WHERE id = 4;
UPDATE poi SET name = '梁园', category = '风景名胜', description = '梁园，广东四大名园之一...' WHERE id = 5;

-- 佛山 3 条官方路线
UPDATE route SET title = '禅城文化半日游', theme = '历史', difficulty = '轻松', best_time = '上午' WHERE id = 1;
UPDATE route SET title = '顺德园林美食游', theme = '美食', difficulty = '轻松', best_time = '上午' WHERE id = 2;
UPDATE route SET title = '岭南古建摄影路线', theme = '出片', difficulty = '轻松', best_time = '下午' WHERE id = 3;
