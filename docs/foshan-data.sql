-- 佛山核心景点
INSERT INTO poi (city_id, name, category, lat, lng, photo_score, food_score, history_score, culture_score, description) VALUES
                                                                                                                            (6, '佛山祖庙', '风景名胜', 23.035795, 113.112770, 4, 3, 5, 5, '佛山祖庙，始建于北宋元丰年间...'),
                                                                                                                            (6, '岭南天地', '休闲街区', 23.029341, 113.117082, 5, 4, 3, 5, '岭南天地，由佛山祖庙东华里片区改造而成...'),
                                                                                                                            (6, '南风古灶', '风景名胜', 23.005324, 113.079185, 4, 3, 5, 4, '南风古灶，始建于明正德年间...'),
                                                                                                                            (6, '清晖园', '风景名胜', 22.835706, 113.255764, 5, 3, 5, 5, '清晖园，广东四大名园之一...'),
                                                                                                                            (6, '梁园', '风景名胜', 23.038184, 113.113271, 5, 3, 5, 5, '梁园，广东四大名园之一...');

-- 佛山官方路线
INSERT INTO route (city_id, title, theme, duration, difficulty, best_time, status, description) VALUES
                                                                                                    (6, '禅城文化半日游', '历史', 4, '轻松', '上午', 'official', '...'),
                                                                                                    (6, '顺德园林美食游', '美食', 6, '轻松', '上午', 'official', '...'),
                                                                                                    (6, '岭南古建摄影路线', '出片', 3, '轻松', '下午', 'official', '...');

-- 佛山路线节点
INSERT INTO route_node (route_id, poi_id, sort_order, stay_duration, tip, photo_spot) VALUES
                                                                                          (4, 74, 1, 90, '...', '...'),
    ...