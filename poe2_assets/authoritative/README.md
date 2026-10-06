# PoE2 权威数据层（authoritative）

> **这是 PoE2 技能数据在本模组中的唯一权威源。**
> 所有 datagen、制表、UI 文案、机制族裁定都必须从这里取数。

- 冻结时间：`2026-09-20T20:48Z`（见 `source_pin.json`）
- 权威源站：`https://poe2db.tw/us/`（数值 / 英文文本 / 标签 / 等级表 / 内部 id）+ `https://poe2db.tw/tw/`（繁体中文名）
- 原始数据 chimney：`design/refactor/skill-data/crawled/`（本层的**上游**，本身不冻结）

---

## 两条铁律

1. **禁止手工编辑本目录下的任何数据文件。**
   本目录全部文件由 `scripts/` 下的脚本生成。发现数据有误 → 修上游爬取脚本 → 重跑脚本，**不要直接改 JSON**。
   （`README.md` 是唯一允许手工维护的文件。）

2. **禁止从本地 datagen 反推权威数据。**
   `src/generated/resources/data/coe/coe/item/gem/**` 与 `data/gen/gem/**` 是**产物**，不是数据源。
   `all_skills.json` 中的 `local_id` / `local_cat` / `implemented` 三个字段是 crosswalk 从本地反向写出的**对照信息**，
   **不得**作为判定依据，任何人不得用它们来"补齐"或"修正"权威字段。

---

## 文件清单

| 文件 | 用途 |
|---|---|
| `all_skills.json` | **权威全量**。结构与上游一致，仅追加顶层 `_provenance`（含源 sha256 指纹）。`entries` 不得变更。 |
| `source_pin.json` | 冻结时刻的源指纹 + 数字快照 + 2026-09-19 旧基线。漂移检测与信条，勿手工改。 |
| `by_type/{skill,support,spirit}.json` | 按 `gem_type` 拆分，供 datagen 分目录消费；`entries` 与 `all_skills.json` 逐字节同源。 |
| `zh_cn_names.json` | `id -> {zh_tw, zh_cn}`。繁体->简体由 opencc `t2s` 转换。`_meta` 下带有缺失清单、幂等性检查结果与跨文件交叉校验结果。 |
| `schema.json` | 字段字典：类型 / 含义 / **实测非空率** / 示例 / 是否参与运行时 + 判定证据。 |
| `FIELD_COVERAGE.md` | 人读版覆盖表 + 已知缺口 + 例外清单 + 与旧文档的数值对照。 |
| `mechanism_index.json` | **机制谱系索引（草案）**：tag 频次、type_tag 频次、tag 共现聚类、tag 两两共现 top60。 |
| `provenance.json` | 溯源元数据：源站、索引页行数、爬取流水线、raw 缓存、DNT 剔除数。 |
| `scripts/freeze_authoritative.js` | 生成器（node）。产出上表除 `zh_cn_names.json` / README 外的全部文件。 |
| `scripts/convert_zh.py` | 生成器（python + opencc）。产出 `zh_cn_names.json`。 |
| `scripts/verify_authoritative.py` | 完整性校验，可独立跑，不依赖 gradle。 |

---

## 当前冻结快照（实测值）

| 指标 | 值 |
|---|---|
| 总条目 | **1059** |
| `gem_type` = skill / support / spirit | **389 / 615 / 55** |
| 唯一基础技能（去 tier 变体） | **900** |
| tier 变体条目 | 271（家族数 900，其中 112 个家族全体成员都是变体） |
| `DNT-*` 残留 | **0** |
| 图标 existing / missing | **1057 / 2**（missing：`chance_to_freeze`, `styrns_anthem`） |
| 有中文名 / 缺中文名 | 793 / 266 |
| unique `tags` | 54 |
| unique `type_tags` | 180 |

> 以上全部为 **2026-09-20T20:44Z 快照**（`crawled_at`）的实测值，
> 取代了先前 1046 / 377 / 614 / 55 / 887 / 1045 的旧基线。详见 `provenance.json` 的 `drift_incident` 字段。

---

## 如何校验

```bash
# 建议在仓库根目录
C:/Users/19063/.workbuddy/binaries/python/envs/default/Scripts/python.exe \
    poe2_assets/authoritative/scripts/verify_authoritative.py
```

- 全部通过时退出码 `0`，输出 `RESULT: PASS`。
- 13 条：结构不变量用硬断言；数字快照与 `source_pin.json` 比对（可抓手工改动）；旧基线仅做 Δ 对照输出。
- `--allow-drift` 把源漂移从 FAIL 降级为 WARN；`--json` 额外写出 `verify_result.json`。
- **当前状态：13/13 PASS。**

### 漂移：源被重爬了怎么办

`verify_authoritative.py` 的第一件事是对 `crawled/all_skills.json` 取 sha256 与 `source_pin.json` 比对。
不一致 → `[00] FAIL`，并提示重新冻结。**这是设计使然**：冻结层不允许悄悄跟源走。

> 本次冻结过程中确实发生过一次并发重写（1046 → 1059），源头是仍在进行中的爬取任务。
> 处理方式即为上述机制：本层以最后一次读取为准并用 sha256 钉死，任何后续漂移都会被检出。

---

## 如何重爬 + 重新冻结

上游脚本在 `design/refactor/skill-data/_tools/`：

```bash
cd design/refactor/skill-data/_tools
node discover_v2.js        # 按 <tr data-filters> 行发现，写 discovered_v3.json
node build_list.js         # 定版清单：补 Meta_Gems 等 tab，记录 seenIn / gem_type
node crawl_details.js all zh   # 抓详情 + 中文名（带 gzip 断点缓存 raw/html_gz）
node finalize.js           # 汇总：覆盖率三态、别名归一、图标清点、生成 crawled/*.md
```

回到仓库根目录，重新冻结（三步，缺一不可）：

```bash
node poe2_assets/authoritative/scripts/freeze_authoritative.js

C:/Users/19063/.workbuddy/binaries/python/envs/default/Scripts/python.exe \
    poe2_assets/authoritative/scripts/convert_zh.py

C:/Users/19063/.workbuddy/binaries/python/envs/default/Scripts/python.exe \
    poe2_assets/authoritative/scripts/verify_authoritative.py
```

重跑后请核对 `--json` 结果，并把新的冻结数值同步更新本 README 的「当前冻结快照」表。

---

## `mechanism_index.json` 的使用警告

它是 **按 tags 共现做的数据聚类草案，不是机制族裁定**。
同一 tag 组合内的成员仅表示共享 poe2db 的**显示标签**，不等于共享运行时机制 ——
例如 `AoE + Attack + Melee + Strike` 的 13 个技能（boneshatter / axe_slash / …）在 delivery 与 DamageAction 上未必同构。

最终族边界必须由作者结合 `type_tags`、`implicits`、`level_table` 共同裁定。
本索引的输入价值在于：**先把 1059 条压成可审阅的几个口袋**，让人工裁定有抓手，而不是让机器替你定族。

---

## 已知缺口（摘要，详见 `FIELD_COVERAGE.md`）

1. `description_zh` 未抓 —— 中文名有 793 条，中文**正文**一条没有。
2. Granted 类（装备 / 升华 / 词缀授予的技能）无宝石形态，本层 0 覆盖。
3. 武器限制未结构化，只散落在 `implicits` 文本里。
4. 辅助宝石的 supports / excludes 作用域集合未抓。
5. `cost` / `cast_time` / `crit_chance` 仅 326 / 121 / 101 条有值（辅助宝石页不提供）。

---

## 与 `crawled/index.md` 的关系

`design/refactor/skill-data/crawled/index.md` 是**爬取阶段的**方法论与覆盖报告，
它的数字基于 1046 条时代，**已经过期**。本层的 `FIELD_COVERAGE.md` 带一份对照表，旧值只作趋势参考。

需要留意的一处分歧：`index.md` 称「Gems 页 1066 行 → 963 唯一」，但对 raw 缓存静态 HTML 实测
**无法复现 963**（Gems 页 gem 锚点去重仅 821，全部 14 个索引页并集同为 821）。
推测 963 来自 JS 渲染后的 DOM 或别的去重口径。
**本层一律以最终 entries 数为唯一有效口径。**
