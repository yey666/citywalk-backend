-- Citywalk 修复脚本：修正 city 表因导入字符集错误导致的乱码
-- 背景：之前用 docker exec 导入数据时未指定 --default-character-set=utf8mb4，
--       导致 UTF-8 中文被按单字节字符集（Windows-1252/latin1）误读后二次编码，成了乱码。
-- 用法（关键：必须带 --default-character-set=utf8mb4）：
--   docker exec -i <mysql容器名> mysql -uroot -p --default-character-set=utf8mb4 citywalk < docs/fix-city-encoding.sql
-- 说明：按 id 定位城市；若线上 id 不一致，请改用 WHERE name = '...'。

USE citywalk;

UPDATE city SET
    name = '苏州',
    region = '华东',
    description = '苏州，江南水乡的代表，园林、古街、小桥流水。适合 2-4 小时的街区漫步，出片点位密集。',
    slogan = '上有天堂，下有苏杭',
    intro = '苏州，一座被园林包围的城市。拙政园的精致、平江路的慢生活、金鸡湖的现代感，苏州把江南的温婉和现代交织在一起。'
WHERE id = 1;

UPDATE city SET
    name = '成都',
    region = '西南',
    description = '成都，一座来了就不想走的城市。宽窄巷子、锦里、玉林路，茶馆与火锅并存。',
    slogan = '一杯茶可以喝一下午的城市',
    intro = '成都，一座来了就不想走的城市。这里的节奏很慢，慢到你可以用一个下午在人民公园喝一杯茶，看一场掏耳朵。'
WHERE id = 2;

UPDATE city SET
    name = '西安',
    region = '西北',
    description = '西安，十三朝古都。城墙、钟楼、回民街、大唐不夜城，历史与现代交织。',
    slogan = '十三朝古都，历史活在街上',
    intro = '西安，古称长安。城墙根下，历史不是博物馆里的展品，是活在街上的日常。回民街的烟火、大雁塔的钟声，西安把千年时光，揉进了一碗泡馍里。'
WHERE id = 3;

UPDATE city SET
    name = '杭州',
    region = '华东',
    description = '杭州，一半山水一半城。西湖、龙井村、南宋御街，适合慢走的诗意路线。',
    slogan = '上有天堂，下有苏杭',
    intro = '杭州，一座被诗意包围的城市。西湖的水、灵隐的钟、龙井的茶，杭州把江南的温柔藏在每一个转角。'
WHERE id = 4;

UPDATE city SET
    name = '厦门',
    region = '华南',
    description = '厦门，海岛城市。鼓浪屿、沙坡尾、环岛路，文艺与海风并存。',
    slogan = '海上花园，慢生活的代名词',
    intro = '厦门，一座被海风拥抱的城市。鼓浪屿的琴声、曾厝垵的文艺、环岛路的海风，厦门把慢生活写进了骨子里。'
WHERE id = 5;

UPDATE city SET
    name = '佛山',
    region = '华南',
    description = '佛山，岭南文化发源地之一。祖庙、石湾陶瓷、顺德美食，适合文化与美食主题漫游。',
    slogan = '功夫、醒狮、早茶，这里有岭南最日常的样子',
    intro = '佛山，简称“禅”，位于广东省中部，是岭南文化的重要发源地。佛山人早上先喝早茶，再去祖庙看醒狮，下午钻进岭南天地的小巷，晚上在南风古灶的窑火边散步。'
WHERE id = 6;
