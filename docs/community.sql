-- 社区模块表结构
-- 用法：mysql -u root -p citywalk < docs/community.sql
-- 或在 MySQL Workbench 里打开执行

USE citywalk;

-- ============================================================
-- 1. 帖子表
-- ============================================================
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

-- ============================================================
-- 2. 点赞表
-- ============================================================
CREATE TABLE IF NOT EXISTS post_like (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT NOT NULL COMMENT '点赞用户',
    post_id    BIGINT NOT NULL COMMENT '帖子',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_post (user_id, post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞';

-- ============================================================
-- 3. 评论表
-- ============================================================
CREATE TABLE IF NOT EXISTS post_comment (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT NOT NULL COMMENT '评论用户',
    post_id    BIGINT NOT NULL COMMENT '帖子',
    content    TEXT COMMENT '评论内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_post (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子评论';
