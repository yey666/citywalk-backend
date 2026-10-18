-- ============================================================
-- Migration: 创建 route_ticket（计划绑定车次）表
-- 原因：前端「保存路线 / AI 优化」报 500，日志提示
--       java.sql.SQLSyntaxErrorException: 表 'citywalk.route_ticket' 不存在
-- 来源：与 docs/database.sql 中的建表语句一致，字段已对照实体类
--       RouteTicket.java 确认完全匹配。
-- 用法：mysql -u root -p < migration-create-route-ticket.sql
--       或在 MySQL Workbench 里选中 citywalk 库执行
-- ============================================================

USE citywalk;

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
