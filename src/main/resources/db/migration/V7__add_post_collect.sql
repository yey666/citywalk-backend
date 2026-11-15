-- ============================================================
-- V7: 帖子收藏表（post_collect）
-- ============================================================
CREATE TABLE IF NOT EXISTS post_collect (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT NOT NULL COMMENT '收藏用户',
    post_id    BIGINT NOT NULL COMMENT '帖子',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_post (user_id, post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子收藏';
