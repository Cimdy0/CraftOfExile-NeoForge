# 属性字段语义去重 + 作用域焊死判定报告

> 对象：`src/main/java/com/cimdy/coe/data/AttributeRegister.java`（122 属性）
> 真值源：`StatTag.java`（146 scope token + BUILTIN 层级表）、`StatGen.java`（469 处 `StatTag.` 接线，89 属性已接线）
> 审计脚本：`poe2_assets/_test/_audit_attr_dup_scope.py` → `attr_dup_scope_audit.json`
> 纪律：R1 不读 runData JSON 回比；R2 不删治「未实现」；R3 实现判定＝引擎真读；属性三定律（scope 通道 / 触发走 fire / 零基禁 inc）

---

## 0. 核心结论

| 判定项 | 结果 |
|---|---|
| **是否存在作用域类型（名焊死 scope）** | ✅ 是，且占主导：**83/122 属性为 WELDED**（名 = scope 类目词 + 维度词），10 个 SELF_NAMED（名即 scope 类目），仅 29 个 PLAIN（名内无 scope token） |
| **Q1 语义精确重复（维度+作用域键全同）** | **0 组** —— 无确切语义重复 |
| **Q3 同维度多作用域族（应归一）** | **11 族**；其中真可合并 **2 项**，其余为独立池 / 合法细分 / 保护集 |
| **Q2 双重表达（名焊死 scope 且 StatGen 又带同 scope tag）** | **43 处**；绝大多数为合法细分，**真冗余 2 项**（`AURA_MAGNITUDE`、`CHARM_DURATION`） |
| **零基倍率疑似项** | **17 处**；全部合法（base=0 即「无加成」，属零基禁 inc 的正确用法，非 bug） |

**唯一需要动手的归一项（治本，非治标）：**
1. `AURA_MAGNITUDE` → 并入 `AILMENT_MAGNITUDE+{aura}`（机械删零消费者属性 + 重定向 StatGen:882）
2. `CHARM_DURATION` → 并入 `AILMENT_DURATION+{charm}`（**修复一个真实误接 bug** + 删死属性）

---

## 1. 作用域类型判定（用户指令第二问）

属性名按「是否焊死 scope」分为三类：

- **SELF_NAMED（10）**：名本身就是 scope 类目词（ACCURACY / DAMAGE / EVASION / RESISTANCES / TAKEN_DAMAGE / TAKEN_CRIT_DAMAGE / CRIT_DAMAGE / GLORY / KNOCKBACK / TOTEM_TAKEN_AURA）。这类天然跨作用域，靠消费端 `enter(scope)` 区分维度，是**正确**的 scope 通道用法。
- **WELDED（83）**：名 = scope 类目词 + 维度词（如 `AILMENT_MAGNITUDE`、`CHARM_DURATION`、`ARCHON_RECOVERY_RATE`）。这是归一重点——同名内已焊死一个 scope，StatGen 接线时若再带同 scope tag 即「双重表达」。
- **PLAIN（29）**：名内无 scope token（如 `WORK_SPEED`、`STRENGTH`、`LEECH`、`RECOVERY_RATE`、`THORNS`）。其 scope 完全由消费端 `enter()` 决定，是最干净的写法。

**结论：作用域类型广泛存在（WELDED+SELF_NAMED = 93/122 ≈ 76%）。** 这正是「大量属性未归一」的根因——同维度（magnitude/duration/chance/rate…）被焊死成多个独立作用域族。归一方向应为「维度属性 + {scope}」，而非为每个 scope 单独建属性。

---

## 2. Q1 语义精确重复 —— 0 组

对每个属性计算 `(dim_key, scope_key)` 二元键（维度词取 magnitude/duration/chance/rate…；scope_key 取名内 scope token 并集 StatGen 的 `StatTag`）。**无一例两属性键完全相同**。说明历史上尚未出现「同一维度+同一作用域被建了两个属性」的硬重复——重复表现是「同维度多作用域族」（Q3），而非「同键重复」。

---

## 3. Q3 同维度多作用域族 —— 11 族

| # | 维度 | 族成员 | 结论 |
|---|---|---|---|
| 1 | magnitude | `AILMENT_MAGNITUDE`(28 scope) / `AURA_MAGNITUDE`(aura) / `REMNANT_MAGNITUDE`(remnant) | **AURA_MAGNITUDE 冗余**（AILMENT_MAGNITUDE 已含 {aura}）；REMNANT 单 scope 单消费者，保留 |
| 2 | duration | `AILMENT_DURATION`(29 scope) / `CHARM_DURATION`(charm) | **CHARM_DURATION 死属性**（见 §5）；并入 AILMENT_DURATION+{charm} |
| 3 | max | `MAX_MANA`/`MAX_RAGE`/`MAX_SPIRIT`/`MAX_ENERGY_SHIELD`/`MAX_VOLATILITY`/`MAX_INFUSION`/`MAX_ENDURANCE_CHARGES`/`MAX_FRENZY_CHARGES`/`MAX_POWER_CHARGES`/`MAX_REGENERATION`/`MAX_RESISTANCES`/`META_ENERGY_MAX` | **全部保护**（独立资源池，归一并池会破坏上限体系）→ DO_NOT_MERGE |
| 4 | chance | `AILMENT_CHANCE` / `BLOCK_CHANCE` / `CRIT_CHANCE` / `DODGE_CHANCE` / `TAKEN_CRIT_CHANCE` | 虽同 dim=chance，但 scope 分属 ailment/block/crit/dodge，是**不同类目**独立语义，非合并候选 |
| 5 | rate\|recovery | `RECOVERY_RATE` / `ARCHON_RECOVERY_RATE` / `COOLDOWN_RECOVERY_RATE` / `FLASK_RECOVERY_RATE` | RECOVERY_RATE 为通用；其余为 scope 专属恢复率。保留（各自 scope 不同，且 ARCHON/COOLDOWN/FLASK 无上位包含关系） |
| 6 | contribution | `AILMENT_DAMAGE_CONTRIBUTION` / `ELEMENTAL_DAMAGE_CONTRIBUTES_BLEEDING` | 维度不同（contribution vs contributes_bleeding），非合并 |
| 7 | gained | `CHARM_CHARGES_GAINED` / `FLASK_CHARGES_GAINED` | scope 分 charm/flask，独立语义，保留 |
| 8 | generation | `GLORY_GENERATION` / `VOLATILITY_GENERATION` | scope 分 glory/volatility，独立，保留（GLORY 暂无消费者，数据先行） |
| 9 | range | `PARRY_RANGE` / `PRESENCE_RANGE` | 独立 scope，保留 |
| 10 | rate | `AILMENT_DAMAGE_RATE` / `REGENERATION_RATE` | 独立 scope（ailment|damage vs regen），保留 |
| 11 | recovery | `GUARD_RECOVERY` / `STUN_RECOVERY` | 独立 scope，保留 |

**真合并候选仅有族 1 的 AURA_MAGNITUDE、族 2 的 CHARM_DURATION。** 其余族或因 scope 分属不同类目（chance/max/gained/range…）、或因独立资源池（max 全保护），应保持现状。

---

## 4. Q2 双重表达 —— 43 处分流

「双重表达」= 属性名内焊死某 scope，且 StatGen 接线又带同 scope tag。初判 43 处，逐条核实后分流：

- **合法细分（绝大多数）**：属性名限定一个 scope 类目（如 `AILMENT_CHANCE`），StatGen 的 `sgTags` 进一步细分到具体异常（`BLEED`/`POISON`/`IGNITE`…）——这是「维度属性 + {scope}」的正确用法，名内 scope 仅作人类可读锚点，接线靠 tag 精确命中。
- **真冗余（2 项）**：名内 scope == 整个 sgTags，且另一个通用维度属性已承载该 scope：
  - `AURA_MAGNITUDE`：名内 {aura} == sgTags{AURA}，且 `AILMENT_MAGNITUDE` 已接 {aura}（StatGen:853）→ 双重 magnitude 通道。
  - `CHARM_DURATION`：虽不在 Q2 列表（其 sgTags 为空），但属死属性，同 §5 处理。

> 注意：Q2 列表中的 `CHARM_EFFECT`（overlap=charm, sgTags={CHARM}）**不是**冗余——`effect` 是独立维度，全仓无通用 `EFFECT` 幅度属性承载 {charm}，且单 scope 单消费者（CharmStatusTypes:33），保留。

---

## 5. 真冗余两项详证 + 修复方案

### 5.1 AURA_MAGNITUDE —— 孤儿消费者（冗余）
- 定义：`AttributeRegister.java:199` `registerPercentage("aura_magnitude", 1.0)`
- 接线：`StatGen.java:882` `inc(map, INC_AURA_MAGNITUDE, AURA_MAGNITUDE, StatTag.AURA)`
- **全仓消费端：无**（Grep 仅定义 + StatGen，无 `readInScope` 读取）。即 `INC_AURA_MAGNITUDE` 即便授予也无人读取。
- 同 scope 已被 `AILMENT_MAGNITUDE` 覆盖：`StatGen.java:853` `INC_BUFF_MAGNITUDE → AILMENT_MAGNITUDE + {aura, not(taken)}`。
- **修复**：删除 `AURA_MAGNITUDE` 声明；将 StatGen:882 改为 `inc(map, INC_AURA_MAGNITUDE, AILMENT_MAGNITUDE, StatTag.AURA)`。INC_AURA_MAGNITUDE 自此走通用 magnitude 通道，与 BUFF_MAGNITUDE 的 {aura} 汇合。

### 5.2 CHARM_DURATION —— 真实误接 bug（死属性）
- 定义：`AttributeRegister.java:195` `registerPercentage("charm_duration", 1.0)`
- **StatGen 接线：无**（`INC_CHARM_DURATION` 实际路由到别处，见下）。
- 消费端：`CharmEngine.java:194` `readInScopePublic(player, {CHARM}, CHARM_DURATION)` → 因无接线，恒读 base=1.0（「X% increased Charm Effect Duration」**当前完全无效**）。
- 错位证据：`StatGen.java:873` `inc(map, INC_CHARM_DURATION, AILMENT_DURATION, CHARM, not(TAKEN))` —— 修饰符去了 `AILMENT_DURATION+{charm}`，但 CharmEngine 读的是 `CHARM_DURATION`（无接线）。
- **修复**：
  1. `CharmEngine.java:194` 改读 `AttributeRegister.AILMENT_DURATION`（使 `INC_CHARM_DURATION` 真正生效）。
  2. 删除 `CHARM_DURATION` 声明（死属性）。
  3. CharmEngine:191 注释同步改为「效果时长倍率（{charm}，走 AILMENT_DURATION 维度）」。
- 此修复同时解决「修饰符无效」的功能 bug 与「死属性」的归一问题——治本。

---

## 6. 零基倍率疑似项 —— 17 处（全部合法）

`AILMENT_CHANCE` / `AVOID_AILMENT_CHANCE` / `BLOCK_CHANCE` / `ARMOR_BREAK` / `LEECH` / `PROJECTILE_PIERCE_CHANCE` / `FLASK_NO_CONSUME_CHANCE` / `CHARM_ACTIVATION_CHANCE` / `DEFEND_ARMOUR` / `DODGE_CHANCE` / `CORRUPTION_ENDURES_CHANCE` / `RESERVATION_EFFICIENCY` / `REMNANT_EXTRA_CHANCE` / `REMNANT_DOUBLE_CHANCE` / `AFTERSHOCK_CHANCE` / `PUPPET_MASTER_CHANCE` / `TAKEN_CRIT_CHANCE`

均为 base=0 的百分比/加成属性（如各类 chance 默认 0 即「无概率加成」）。`inc`（ADD_MULTIPLIED_BASE）对 base=0 静默死，但这些属性本就用 `pctAdd`/`ADD_VALUE` 类接线（非 inc），符合「零基禁 inc」定律。**非 bug，不处理。**

---

## 7. 执行建议与门禁

**仅 2 项需动手，均为机械操作、不触 JSON、不删治未实现：**

| 步骤 | 文件 | 操作 | 门禁 |
|---|---|---|---|
| P-A1 | `AttributeRegister.java` | 删 L199 `AURA_MAGNITUDE` 声明 | — |
| P-A2 | `StatGen.java:882` | `AURA_MAGNITUDE` → `AILMENT_MAGNITUDE`（保留 `{AURA}`） | compileJava |
| P-C1 | `CharmEngine.java:194` | `CHARM_DURATION` → `AILMENT_DURATION` | compileJava |
| P-C2 | `AttributeRegister.java` | 删 L195 `CHARM_DURATION` 声明 | compileJava |
| P-C3 | `CharmEngine.java:191` | 注释同步（{charm} 走 AILMENT_DURATION 维度） | — |

执行后跑 `./gradlew compileJava` 验证（须正常 shell + `export JAVA_HOME=/c/Users/19063/.jdks/ms-21.0.10` + PATH，禁用 `env -i` 静默假成功）。

**不在本轮范围（前序总计划 P2–P5 顺序）：** 真值表补全 → 清基座零基 inc → 触发越界 3 处 + 悬空项 → 逐机制真实现（首机制族待定：accuracy/armor/damage/area-of-effect/taken）。

---

## 8. 产出文件
- 报告：`poe2_assets/ATTRIBUTE_SEMANTIC_SCOPE_AUDIT.md`（本文件）
- 审计脚本：`poe2_assets/_test/_audit_attr_dup_scope.py`
- 审计数据：`poe2_assets/_test/attr_dup_scope_audit.json`
