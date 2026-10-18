-- Citywalk 数据填充脚本：写入 6 个城市的 slogan / intro
-- 前提：已先执行 docs/migration-add-city-slogan-intro.sql（city 表已加 slogan、intro 两列）
-- 用法：mysql -u root -p citywalk < docs/migration-fill-city-slogan-intro.sql
-- 注意：按 id 定位城市；若线上 city 表的 id 与下方不一致，请改用 WHERE name = '城市名'。

USE citywalk;

UPDATE city SET
    slogan = '功夫、醒狮、早茶，这里有岭南最日常的样子',
    intro = '佛山，简称“禅”，位于广东省中部，是岭南文化的重要发源地。佛山人早上先喝早茶，再去祖庙看醒狮，下午钻进岭南天地的小巷，晚上在南风古灶的窑火边散步。'
WHERE id = 6; -- 佛山

UPDATE city SET
    slogan = '上有天堂，下有苏杭',
    intro = '苏州，一座被园林包围的城市。拙政园的精致、平江路的慢生活、金鸡湖的现代感，苏州把江南的温婉和现代交织在一起。'
WHERE id = 1; -- 苏州

UPDATE city SET
    slogan = '一杯茶可以喝一下午的城市',
    intro = '成都，一座来了就不想走的城市。这里的节奏很慢，慢到你可以用一个下午在人民公园喝一杯茶，看一场掏耳朵。'
WHERE id = 2; -- 成都

UPDATE city SET
    slogan = '十三朝古都，历史活在街上',
    intro = '西安，古称长安。城墙根下，历史不是博物馆里的展品，是活在街上的日常。回民街的烟火、大雁塔的钟声，西安把千年时光，揉进了一碗泡馍里。'
WHERE id = 3; -- 西安

UPDATE city SET
    slogan = '上有天堂，下有苏杭',
    intro = '杭州，一座被诗意包围的城市。西湖的水、灵隐的钟、龙井的茶，杭州把江南的温柔藏在每一个转角。'
WHERE id = 4; -- 杭州

UPDATE city SET
    slogan = '海上花园，慢生活的代名词',
    intro = '厦门，一座被海风拥抱的城市。鼓浪屿的琴声、曾厝垵的文艺、环岛路的海风，厦门把慢生活写进了骨子里。'
WHERE id = 5; -- 厦门
