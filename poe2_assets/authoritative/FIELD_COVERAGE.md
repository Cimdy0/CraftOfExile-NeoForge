# 字段覆盖表（实测）

> 本文件由 `scripts/freeze_authoritative.js` 自动生成，勿手工编辑。
> 所有数字均为对 `poe2_assets/authoritative/all_skills.json`（1059 条）的**实测值**，非引自旧文档。

- 生成时间：2026-09-20T20:56:44.049Z
- 权威条目：**1059**（skill 389 / support 615 / spirit 55）
- 唯一基础技能（去 tier 变体）：**900**
- tier 变体条目：**271**

## 覆盖总览

| 字段 | 类型 | 非空数 | 非空率 | skill | support | spirit | 运行时角色 |
|---|---|---|---|---|---|---|---|
| `id` | string | 1059/1059 | 100% | 100% | 100% | 100% | runtime |
| `id_coe` | string | 1059/1059 | 100% | 100% | 100% | 100% | reference_only |
| `name_en` | string | 1059/1059 | 100% | 100% | 100% | 100% | runtime |
| `gem_type` | enum | 1059/1059 | 100% | 100% | 100% | 100% | runtime |
| `level_table` | object\|null | 1059/1059 | 100% | 100% | 100% | 100% | runtime |
| `source_url_us` | string\|null | 1059/1059 | 100% | 100% | 100% | 100% | reference_only |
| `source_url_tw` | string\|null | 1059/1059 | 100% | 100% | 100% | 100% | reference_only |
| `crawled_at` | string | 1059/1059 | 100% | 100% | 100% | 100% | reference_only |
| `is_meta` | boolean | 1059/1059 | 100% | 100% | 100% | 100% | reference_only |
| `is_lineage` | boolean | 1059/1059 | 100% | 100% | 100% | 100% | partial |
| `seen_in` | string[] | 1059/1059 | 100% | 100% | 100% | 100% | reference_only |
| `implemented` | boolean | 1059/1059 | 100% | 100% | 100% | 100% | reference_only |
| `base_id` | string | 1059/1059 | 100% | 100% | 100% | 100% | reference_only |
| `tier_variant` | boolean | 1059/1059 | 100% | 100% | 100% | 100% | reference_only |
| `icon_status` | enum | 1059/1059 | 100% | 100% | 100% | 100% | reference_only |
| `base_type` | string\|null | 1057/1059 | 99.8% | 99.5% | 100% | 100% | reference_only |
| `icon_url` | string\|null | 1057/1059 | 99.8% | 100% | 99.7% | 100% | runtime |
| `icon_local_path` | string\|null | 1057/1059 | 99.8% | 100% | 99.7% | 100% | runtime |
| `description_en` | string\|null | 1056/1059 | 99.7% | 99.7% | 99.7% | 100% | reference_only |
| `max_level` | number\|null | 1051/1059 | 99.2% | 98.5% | 100% | 96.4% | runtime |
| `tags` | string[] | 1048/1059 | 99% | 97.7% | 100% | 96.4% | runtime |
| `implicits` | string[] | 1048/1059 | 99% | 99% | 99.2% | 96.4% | reference_only |
| `gem_class` | string\|null | 1048/1059 | 99% | 97.7% | 100% | 96.4% | reference_only |
| `internal_id` | string\|null | 1047/1059 | 98.9% | 97.4% | 100% | 96.4% | reference_only |
| `requirements` | string | 1042/1059 | 98.4% | 97.9% | 98.9% | 96.4% | runtime |
| `stats` | string[] | 1002/1059 | 94.6% | 86.4% | 99.7% | 96.4% | reference_only |
| `acronyms` | string[]\|null | 980/1059 | 92.5% | 93.3% | 91.9% | 94.5% | reference_only |
| `category_tags` | string[] | 962/1059 | 90.8% | 90.5% | 90.6% | 96.4% | reference_only |
| `index_tier` | number\|null | 962/1059 | 90.8% | 90.5% | 90.6% | 96.4% | reference_only |
| `art_name` | string\|null | 962/1059 | 90.8% | 90.5% | 90.6% | 96.4% | reference_only |
| `recommended_supports` | string[]\|null | 817/1059 | 77.1% | 87.1% | 69.1% | 96.4% | reference_only |
| `icon_source_candidates` | string[] | 805/1059 | 76% | 90.5% | 65% | 96.4% | reference_only |
| `gem_color` | enum | 795/1059 | 75.1% | 44.5% | 95.6% | 61.8% | runtime |
| `name_zh` | string\|null | 793/1059 | 74.9% | 44.5% | 95.4% | 60% | runtime |
| `req_level_range` | string | 508/1059 | 48% | 96.1% | 13.2% | 96.4% | runtime |
| `type_tags` | string[] | 444/1059 | 41.9% | 99.5% | 0.3% | 100% | reference_only |
| `target_types` | string[]\|null | 441/1059 | 41.6% | 99% | 0.2% | 100% | reference_only |
| `level_range` | string\|null | 432/1059 | 40.8% | 97.2% | 0.2% | 96.4% | reference_only |
| `quality_bonus` | string\|null | 395/1059 | 37.3% | 87.7% | 0.2% | 96.4% | reference_only |
| `cost` | string\|null | 326/1059 | 30.8% | 77.1% | 0.8% | 38.2% | runtime |
| `req_attr` | object\|null | 296/1059 | 28% | 62.7% | 0% | 94.5% | runtime |
| `local_id` | string\|null | 260/1059 | 24.6% | 33.2% | 16.9% | 49.1% | reference_only |
| `local_cat` | string\|null | 260/1059 | 24.6% | 33.2% | 16.9% | 49.1% | reference_only |
| `quality_range` | number[]\|null | 259/1059 | 24.5% | 57.6% | 0.2% | 61.8% | reference_only |
| `cast_time` | string\|null | 121/1059 | 11.4% | 30.1% | 0.2% | 5.5% | reference_only |
| `crit_chance` | string\|null | 101/1059 | 9.5% | 23.1% | 1.1% | 7.3% | partial |
| `attack_speed` | string\|null | 81/1059 | 7.6% | 20.8% | 0% | 0% | reference_only |
| `description_zh` | null | 0/1059 | 0% | 0% | 0% | 0% | reference_only |

> 非空判定：空字符串 / 空数组 / 空对象 / null / undefined 一律计为空。

## 运行时角色说明

- `runtime` —— 字段已（或可直接映射）进入 `src/generated/resources/data/coe/coe` 的运行时产物。
- `partial` —— 语义相关，但接线未确认。
- `reference_only` —— 当前无对应运行时产物键，仅作真值参考 / 溯源。

判定依据：运行时产物实测顶键并集

- `item/gem/**`（537 个文件）：`entry, ref, vars, cost, require, tags, cast, params, phases, curse_duration, curse_range, mark_duration, mark_range, modifiers, effects, lineage, mana_cost_multiplier, max_level, reservation_multiplier, tag, tier, special`
- `item/gem/**/preset_*.json`（262 个文件）：`cast, cost, phases, require, tags, modifiers`

**重要**：`quality`（品质）在上述产物中命中 **0** 次 —— 既有模组完全未接线品质，与 `index.md` 的结论一致。

## 已知缺口（5）

1. **描述仅英文** —— description_zh 全表为 null —— 中文详情页正文未抓取；中文**名**已抓 793 条（繁体，见 zh_cn_names.json 转简体）。
2. **未覆盖 Granted 类** —— 装备 / 升华 / 词缀授予的技能无宝石形态，不在任何索引页，本层为 0 覆盖。
3. **武器限制未结构化** —— 武器限制只以文本形式散落在 implicits（如 Requires a Mace），无独立字段。
4. **辅助的 supports/excludes 未抓** —— 辅助宝石「可辅助哪些技能」在 poe2db 以技能类别描述呈现，未做二次解析 -> recommended_supports 之外的真实作用域集合缺失。
5. **cost / cast_time / crit_chance 仅部分有值** —— 三者分别为 326 / 121 / 101 条有值，且几乎全部缺失来自辅助宝石页（该类页面不提供这些字段）。

## 与 `crawled/index.md` 旧值对照

`index.md` 是 1046 条时代的覆盖表（已过时）。下表把它的字段计数与本层实测值并置；注意分母已从 1046 变为 1059，因此两列不可直接相减，只能看趋势。

| 字段 | index.md 旧值 | 本层实测 | 差 |
|---|---|---|---|
| `name_en` | 1046 | 1059 | +13 |
| `name_zh` | 793 | 793 | +0 |
| `description_en` | 1043 | 1056 | +13 |
| `requirements` | 1029 | 1042 | +13 |
| `internal_id` | 1034 | 1047 | +13 |
| `icon_url` | 1045 | 1057 | +12 |
| `tags` | 1035 | 1048 | +13 |
| `quality_bonus` | 394 | 395 | +1 |
| `cost` | 322 | 326 | +4 |
| `cast_time` | 119 | 121 | +2 |
| `crit_chance` | 100 | 101 | +1 |
| `type_tags` | 432 | 444 | +12 |
| `level_range` | 421 | 432 | +11 |
| （总条目） | 1046 | 1059 | +13 |

## 必填字段例外清单

- `id`：缺失 **0** 条
- `name_en`：缺失 **0** 条
- `internal_id`：缺失 **12** 条 —— firebolt, blight, frost_nova, cyclone, lacerate, puncture, toxic_rain, consume_corpse, ground_slam, perforate, wrath, grace
- `name_zh`：缺失 **266** 条（这些条目在 `zh_cn_names.json` 中按铁律**留空**，不臆造）

## 图标三态（实测）

| 状态 | 数量 |
|---|---|
| existing | 1057 |
| missing | 2 |

缺失项 id：chance_to_freeze, styrns_anthem
