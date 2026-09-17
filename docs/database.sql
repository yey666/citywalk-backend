-- Citywalk 数据库初始化脚本
-- 用法：mysql -u root -p < docs/database.sql
-- 或在 MySQL Workbench 里打开执行

CREATE DATABASE IF NOT EXISTS citywalk
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE citywalk;

-- ============================================================
-- 1. 城市表
-- ============================================================
CREATE TABLE IF NOT EXISTS city (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    name VARCHAR(50) NOT NULL COMMENT '城市名',
    region VARCHAR(50) COMMENT '所属区域',
    lat DECIMAL(10, 6) NOT NULL COMMENT '纬度',
    lng DECIMAL(10, 6) NOT NULL COMMENT '经度',
    description TEXT COMMENT '城市解说',
    cover_image VARCHAR(255) COMMENT '封面图',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='城市表';

-- ============================================================
-- 2. POI 表
-- ============================================================
CREATE TABLE IF NOT EXISTS poi (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                   city_id BIGINT NOT NULL COMMENT '所属城市',
                                   name VARCHAR(100) NOT NULL COMMENT 'POI 名称',
    category VARCHAR(50) COMMENT '类别',
    address VARCHAR(255) COMMENT '地址',
    lat DECIMAL(10, 6) NOT NULL,
    lng DECIMAL(10, 6) NOT NULL,
    photo_score TINYINT DEFAULT 3 COMMENT '出片指数 1-5',
    food_score TINYINT DEFAULT 0 COMMENT '美食指数 1-5',
    history_score TINYINT DEFAULT 3 COMMENT '历史价值 1-5',
    culture_score TINYINT DEFAULT 3 COMMENT '文艺氛围 1-5',
    open_hours VARCHAR(100) COMMENT '开放时间',
    price_level TINYINT DEFAULT 0 COMMENT '价格等级 0-5',
    photo_spot VARCHAR(500) COMMENT '具体机位描述',
    description TEXT COMMENT '叙事文本（进向量库）',
    min_duration INT DEFAULT 30 COMMENT '最短停留分钟',
    transit_tip VARCHAR(500) COMMENT '到下一个点的交通提示',
    best_visit_time VARCHAR(50) COMMENT '最佳到访时段',
    avoid_time VARCHAR(50) COMMENT '避开时段',
    source VARCHAR(50) DEFAULT 'amap' COMMENT '数据来源',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_city (city_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='POI 表';

-- ============================================================
-- 3. 路线表
-- ============================================================
CREATE TABLE IF NOT EXISTS route (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     city_id BIGINT NOT NULL COMMENT '所属城市',
                                     title VARCHAR(100) NOT NULL COMMENT '路线标题',
    theme VARCHAR(50) COMMENT '主题：出片/美食/历史/文艺',
    duration INT COMMENT '时长（小时）',
    difficulty VARCHAR(20) COMMENT '轻松/中等/暴走',
    best_time VARCHAR(20) COMMENT '上午/下午/傍晚/夜游',
    status VARCHAR(20) DEFAULT 'draft' COMMENT 'draft/saved/shared/official',
    cover_image VARCHAR(255),
    description TEXT COMMENT '路线简介',
    user_id BIGINT COMMENT '创建者（官方为 NULL）',
    day_index INT DEFAULT NULL COMMENT '多天计划中的第几天',
    trip_id BIGINT DEFAULT NULL COMMENT '所属多天计划',
    ai_generated TINYINT DEFAULT 0 COMMENT '是否 AI 生成',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    collect_count INT DEFAULT 0 COMMENT '收藏数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_city (city_id),
    INDEX idx_status (status)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路线表';

-- ============================================================
-- 4. 路线节点表
-- ============================================================
CREATE TABLE IF NOT EXISTS route_node (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                          route_id BIGINT NOT NULL,
                                          poi_id BIGINT NOT NULL,
                                          sort_order INT NOT NULL COMMENT '节点顺序',
                                          stay_duration INT COMMENT '建议停留分钟数',
                                          tip VARCHAR(500) COMMENT '本地人提示',
    photo_spot VARCHAR(500) COMMENT '本节点机位',
    INDEX idx_route (route_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路线节点表';

-- ============================================================
-- 5. 用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像 URL',
    role VARCHAR(20) DEFAULT 'user' COMMENT 'user/admin',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 6. 用户收藏路线表
-- ============================================================
CREATE TABLE IF NOT EXISTS user_route (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                          user_id BIGINT NOT NULL,
                                          route_id BIGINT NOT NULL,
                                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                          UNIQUE KEY uk_user_route (user_id, route_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏的路线';

-- ============================================================
-- 初始数据：5 个城市
-- ============================================================
INSERT INTO city (name, region, lat, lng, description) VALUES
                                                           ('苏州', '华东', 31.298886, 120.585316, '苏州，江南水乡的代表，园林、古街、小桥流水。适合 2-4 小时的街区漫步，出片点位密集。'),
                                                           ('成都', '西南', 30.572815, 104.066801, '成都，一座来了就不想走的城市。宽窄巷子、锦里、玉林路，茶馆与火锅并存。'),
                                                           ('西安', '西北', 34.341568, 108.940174, '西安，十三朝古都。城墙、钟楼、回民街、大唐不夜城，历史与现代交织。'),
                                                           ('杭州', '华东', 30.274084, 120.155070, '杭州，一半山水一半城。西湖、龙井村、南宋御街，适合慢走的诗意路线。'),
                                                           ('厦门', '华南', 24.479834, 118.089425, '厦门，海岛城市。鼓浪屿、沙坡尾、环岛路，文艺与海风并存。');