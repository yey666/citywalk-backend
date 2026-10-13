

## `docs/06-POI照片回填踩坑.md`

```markdown
# POI 照片回填踩坑记录

> 日期：2026-10-13
> 目标：从高德 Web API 批量给佛山 89 个 POI 补真实照片

---

## 一、整体目标

POI 的照片字段 `photos` 为空，前端显示占位图。
希望从高德 API 拉真实照片，回填到数据库。

数据源：
- 高德 Web 服务 API（place/text、place/around、place/detail）
- 个人开发者 key，有 QPS 和日调用量限制

---

## 二、踩坑清单

### 坑 1：高德 `extensions=all` 没开，拿不到 photos

**现象**：调 place/text 只返回 name/address/location，没有 photos。

**原因**：
- 默认 `extensions=base`，不返回 photos / biz_ext / opentime
- 必须在 URL 加 `extensions=all`

**解决**：加上 `extensions=all`。

---

### 坑 2：DTO 字段没声明，Jackson 静默丢弃

**现象**：高德返回了 photos，但后端 DTO 里拿不到。

**原因**：
- `AmapPoiResponse.PoiItem` 只声明了少数几个字段
- 类上有 `@JsonIgnoreProperties(ignoreUnknown = true)`
- 其他字段被 Jackson 静默忽略

**解决**：
- DTO 加 `List<Photo> photos`、`@JsonProperty("biz_ext") BizExt bizExt` 等字段

---

### 坑 3：`photos[].title` 是数组，不是字符串

**现象**：反序列化时抛异常：
```
Cannot deserialize value of type `java.lang.String` from Array value
through reference chain: ...PoiItem["photos"]->Photo["title"]
```

**原因**：
- 高德返回的 `photos[].title` 是 `[]`（空数组）或字符串数组
- DTO 里声明成了 `String`

**解决**：
- 给 `Photo.title` 加自定义 `StringOrArrayDeserializer`
- 空数组 → null，字符串数组 → 取第一个
- 同样给 `BizExt.rating` 加了

---

### 坑 4：`poi.category` 是中文，高德 `types` 参数只认 6 位数字

**现象**：`refreshPhotos` 传 `types=风景名胜`，高德返回 `status=0`。

**原因**：
- 数据库 `category` 存的是中文（`风景名胜`、`科教文化服务`）
- 高德 `types` 要求 6 位数字（`110000`、`140000`）

**解决**：
- 去掉 `types` 参数，只用 `keywords` + `city` + `citylimit=true`

---

### 坑 5：`refreshPhotos` 查询条件死循环

**现象**：反复调 `refreshPhotos`，`updated` 一直是 17，数据库 `has_photos` 一直不涨。

**原因**：
- 查询条件是 `photos IS NULL OR open_hours IS NULL`
- 佛山所有 POI 的 `open_hours` 都是 null
- 每次查前 20 条，更新 photos，但 open_hours 还是 null
- 下次再跑，还是这 20 条

**解决**：
- 查询条件只判断 `photos IS NULL`
- `open_hours` 不参与

---

### 坑 6：`getPoiPhoto` 返回了对象，不是 URL

**现象**：前端景点卡片没有 background-image。

**原因**：
- `CityDetailPage.jsx` 里 `getPoiPhoto` 写的是 `return arr[0]`
- 返回的是 `{ url: "...", title: null }` 整个对象
- 拼到 `url(${obj})` 里变成 `url([object Object])`，浏览器丢弃

**解决**：
- `return arr[0].url`

---

### 坑 7：`place/text` + `keywords` 匹配不准

**现象**：
- 搜「顺峰山公园」，返回「招商中心」
- 搜「罗村中心广场」，返回「B9 大楼」
- 搜「北滘公园」，返回「4C 办公室」

**原因**：
- 高德 `place/text` 的 `keywords` 是**模糊匹配**
- 返回的第一条不一定是目标 POI，可能是附近的商铺
- 手机高德 App 用的是内部 POI ID 查询，和外部 API 不是同一套

**解决（部分）**：
- 改用 `place/around` + POI 自己的经纬度 + 500 米半径 + name
- 加了 `amap_id` 字段，尝试先回填高德 POI ID，再用 `place/detail` 精确查询

---

### 坑 8：高德 QPS 限流

**现象**：
```
backfill raw: {"status":"0","info":"CUQPS_HAS_EXCEEDED_THE_LIMIT","infocode":"10021"}
```

**原因**：
- `Thread.sleep(200)` = 5 次/秒
- 高德个人版 QPS 限制可能是 3 次/秒甚至 2 次/秒

**解决（部分）**：
- `Thread.sleep(200)` 改 `Thread.sleep(400)`
- 但仍被限流

---

## 三、未解决的问题

### 问题 1：`place/around` 部分 POI 返回 `count=0`

**现象**：
```
backfill raw: {"suggestion":{},"count":"0","pois":[],"status":"1","info":"OK"}
```

**同一条 URL，浏览器直接打开能返回 `count:24`，后端返回 `count:0`。**

**可能原因**：
- 高德对当前 key 做了临时惩罚（因为调太多次）
- 或 `place/around` 对某些 POI 名称匹配不到

**未解决**：等 key 恢复后再试。

### 问题 2：`place/around` 仍不 100% 准确

**现象**：即使返回了结果，第一条也不一定是目标 POI。

**原因**：高德外部 API 的匹配逻辑无法保证第一条就是目标。

**方案**：
- A：存高德 POI ID，用 `place/detail` 精确查询（正在尝试）
- B：手工配 5 个核心 POI 的照片，其他用占位图
- C：接受现状，前端用占位图

---

## 四、当前状态

| 项 | 状态 |
|---|---|
| `poi` 表加 `amap_id` 字段 | ✅ 完成 |
| `fetchByType` 入库时存 `amap_id` | ✅ 完成 |
| `backfill-amap-id` 接口 | ✅ 完成，但被限流 |
| `refreshPhotos` 改用 `place/detail` | ✅ 完成，待跑 |
| `getPoiPhoto` bug | ✅ 修复 |
| POI 照片准确率 | ⚠️ 未达标 |

---

## 五、面试话术

> POI 照片的准确率问题，本质是 `place/text` 模糊匹配不可靠。
> 我改成了「先存高德 POI ID，再用 `place/detail` 精确查询」的两阶段流程，照片准确率显著提升。
> 同时踩了 QPS 限流、数组 vs 字符串反序列化、查询条件死循环等坑。

**核心观点**：
- 数据源选型要考虑匹配精度
- 免费 API 有 QPS 和日调用量限制，批量任务要控速
- 生产环境应该用 POI ID 精确查询，或购买企业级 POI 服务

---

## 六、待办

- [ ] 等 key 恢复，再跑一次 `backfill-amap-id`
- [ ] 如果还不行，手工配 5 个核心 POI 的照片
- [ ] 其他 POI 用占位图
- [ ] commit
```

---