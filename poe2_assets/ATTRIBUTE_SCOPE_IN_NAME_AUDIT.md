# 属性字段名内嵌作用域 · 归一审计（2026-08-22）

## §1 设计原则（用户裁定）

> `readInScope(attacker, Set.of(StatTag.TOTEM, StatTag.TAKEN), AttributeRegister.TOTEM_TAKEN_AURA)`
> —— 属性字段中包含作用域本就不应该。

**原则**：属性的 Java 字段名（含 key）**不得内嵌作用域（scope）**。作用域只应存在于 `StatTag`，
在 write（`pctAdd`/`inc`/`attrCond` 等）与 read（`readInScope`/`getAttributeValue`）时经标签注入。
字段名只应描述「机制 / 维度 / 幅度」，不应把 `{totem,taken}` 这类作用在 read 端重复编码进名字。

## §2 扫描方法

脚本：`poe2_assets/_test/_scan_scope_in_name.py`

1. 解析 `StatTag.java` 全量标签常量名 + 标签值（148 个）。
2. 解析 `AttributeRegister.java` 全部属性声明（字段名 + key）。
3. 对每属性扫描 `src/main/java` 所有 `AttributeRegister.X` 出现处，提取同行注入的 `StatTag.*` 标签集合
   （write 端：StatGen `pctAdd/inc/chance` + 天赋 `attrCond`；read 端：`readInScope`）。
4. 比对：字段名（大写拆分）是否命中「实际被注入的作用域标签」。

判定真违规 = 字段名内嵌了「write/read 端实际注入」的作用域标签（即作用域被名字重复编码）。

## §3 扫描结果

属性总数 = 118；字段名内嵌「实际注入作用域」候选 = **46**。

### §3.1 45 个为合法维度载体（非违规）

`DAMAGE` / `CRIT_CHANCE` / `CRIT_DAMAGE` / `DODGE_CHANCE` / `KNOCKBACK` / `GLORY` /
`AILMENT_*` / `PROJECTILE_*` / `FLASK_*` / `CHARM_*` / `RESISTANCE_PENETRATION` /
`EFFECT_AREA` / `STATUS_*` / `TAKEN_*` / `MAX_*_CHARGES` / `CORPSE_RETAIN_CHANCE` /
`CORRUPTION_ENDURES_CHANCE` / `BANNER_LINGER` / `STUN_RECOVERY` / `PARRY_RANGE` /
`REMNANT_EXTRA_CHANCE` / `ARCHON_RECOVERY_RATE` / `VOLATILITY_*` / `GUARD_RECOVERY` 等。

**共性**：字段名 = 维度本身（如 `CRIT_CHANCE` 即「暴击几率」维度），写入/读取用的 `StatTag`
就是同一维度（`CRIT`），并非冗余重复——属正常约定，保留。

### §3.2 1 个真违规：`TOTEM_TAKEN_AURA`

| 项 | 值 |
|---|---|
| 字段名 token | `TOTEM` `TAKEN` `AURA`（三者均为 StatTag） |
| read 作用域 | `{StatTag.TOTEM, StatTag.TAKEN}` |
| write 作用域 | `{TOTEM, TAKEN}`（NotableNodes07 5580 attrCond） |

字段名把 read 端注入的 **两个**作用域标签 `TOTEM`+`TAKEN` 原样拼接，且无可独立辨识的机制词
（本质机制 = 「图腾光环使附近敌人承伤增加」的幅度，是一个具体机制，却用作用域拼接命名）。
符合用户所指的「字段名内嵌作用域」违规。

## §4 关键发现：为何不能并入通用 `TAKEN_DAMAGE`

存在通用承伤维度载体 `TAKEN_DAMAGE`（key `taken_damage`）。表面看「图腾承伤增幅」应是其
`{totem,taken}` 作用域实例。但经查 `TagExpr.matchLeaf` + `StatContext.satisfies`：

> `entryTags.matches(scope)` 为真 ⇔ 条目标签 **⊆** 查询作用域（条目须比查询更具体）。

即 `readInScope(entity, {TOTEM, TAKEN}, X)` 会读取 **所有** 标签 ⊆ `{TOTEM, TAKEN}` 的 `X` 条目，
包含 `{TAKEN}`-only、`{}`-only 等其它承伤来源 → **过计数**。

故当前专用属性是为**隔离**该图腾光环而存在，不能简单并入 `TAKEN_DAMAGE`。结论：保留专用载体，
仅将字段名去作用域化。

## §5 执行（三道门禁全过）

`TOTEM_TAKEN_AURA` → **`AURA`**（key `aura`）；`{TOTEM, TAKEN}` 作用域**保留在 StatTag**，字段不内嵌。

- `AttributeRegister.java:344`：字段改名 + 注释补「作用域仅存于 StatTag，字段不内嵌作用域」。
- `NotableNodes07.java:997`（5580 Watchtowers 天赋）：`attrCond(AttributeRegister.AURA, 0.02, TOTEM, TAKEN)`。
- `DamagePipeline.java:736,739`（承伤管道）：`readInScope(attacker/master, {TOTEM, TAKEN}, AttributeRegister.AURA)`。

门禁：
- `compileJava` ✅（仅 deprecation/unchecked 提示）
- `runData` ✅（Stat Entries provider finished；原孤立 `totem_taken_aura.json` 已移除，无 `aura.json` 需生成——
  该属性由天赋注入 STAT_STORE、管线读取，不经 StatGen/StatIds 数据条目）
- `grep` ✅（`src/main/java` 无 `TOTEM_TAKEN_AURA`/`totem_taken_aura` 残留；仅 `.new`/`.pre_annotate` 备份与历史注释）

### §5.1 命名说明

字段 `AURA` 含 `StatTag.AURA` 标签，但 `AURA` **不在** read 作用域 `{TOTEM, TAKEN}` 内，
故不重复编码 read 作用域（满足用户原则）。`StatTag.AURA` 用于光环类 buff 分类
（`AILMENT_MAGNITUDE` + `{aura,not(taken)}` 等），与 `AttributeRegister.AURA`（图腾承伤光环幅度）
是「标签 vs 属性」不同概念，读取作用域不含 `AURA`，无功能冲突。

## §6 结论

- 全量 118 属性中，**唯一**真正「字段名内嵌作用域」的违规即 `TOTEM_TAKEN_AURA`，已改名 `AURA` 闭环。
- 其余 45 个候选为合法维度载体（字段名=维度，标签=同维度），非冗余，保留。
- 本轴与既有两轴（语义漂移 §/`FOLD-vs-FIRE`）正交：语义漂移管「描述→Ids→属性」语义损耗；
  FOLD-vs-FIRE 管「效果数值应走属性还是 Effect」；本轴管「属性字段名不得重复编码作用域」。
