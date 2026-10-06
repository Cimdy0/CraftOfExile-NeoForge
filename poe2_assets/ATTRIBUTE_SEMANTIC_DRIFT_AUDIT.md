# 属性语义漂移审计（description → StatIds → Attribute 链路）

> 阶段：只读比对 + 提案（盘查后确认范围再执行改名/归一/重实现）。
> 对象：`AttributeRegister.java`（119 属性）、`StatGen.java`（接线）、`StatIds.java`（id-string = PoE2 语义锚）。
> 审计脚本：`_test/_audit_semantic_drift.py`、`_test/_audit_drift_focused.py`、`_test/_dump_specific.py`。
> 纪律沿用：R1 不读 runData JSON 回比｜R2 不删「未实现」｜R3 实现判定＝引擎真读｜属性三定律。

---

## 0. 方法

对每个 `AttributeRegister` 属性：解析 (常量名 / key / 注解) → 解析 `StatGen` 中它的全部接线 (op / StatIds / scope tags) →
取 `StatIds` 的 `id("...")` 字符串作为 **PoE2 语义真值锚**（蛇形英文原义）。逐属性比对「注册名/注解」与「接线语义」，
定位三类漂移：① 命名漏义 ② 概念混淆（单属性承载 2+ 互不相干机制）③ 注解矛盾。

---

## 1. 核心结论

| 项 | 结果 |
|---|---|
| 属性总数 / 有接线 | 119 / 78 |
| **真正语义漂移（具体名属性）** | **1 项：`DEFEND_ARMOUR`** |
| 误报为「混淆」的合法归一载体 | 35 项（DAMAGE / ADDED_DAMAGE / AILMENT_* / MAX_* / RESISTANCES / RESISTANCE_PENETRATION / EFFECT_AREA 等，属「维度属性 + {scope}」设计，非漂移） |
| 具体名属性接线语义均匹配者 | AFTERSHOCK_AREA / AFTERSHOCK_CHANCE / COOLDOWN_RECOVERY_RATE / ENERGY_RECHARGE / LAUNCH_ADDITIONAL_PROJECTILE / STUN_RECOVERY / TAKEN_CRIT_* / TAKEN_DAMAGE |
| 轻微注解偏差（非漂移） | `THORNS`（见 §4） |

**结论：用户所指的「描述→Ids→属性 损耗、注册了毫无关系属性」在审计中唯一坐实为 `DEFEND_ARMOUR`；其本质是把 PoE2 一整个「Defend with X% of Armor」条件族 + 独立的「Chance to Defend」概率，机械焊进了一个名为 `defend_armour`、注释「护甲招架几率」的属性，既丢了「chance」维度，也丢了条件作用域结构。**

---

## 2. DEFEND_ARMOUR 漂移详证

### 2.1 当前状态
- `AttributeRegister.java:297` `DEFEND_ARMOUR = registerPercentage("defend_armour", 0.0)`；注解「护甲招架几率」。
- `StatGen.java:912` `chance(map, CHANCE_TO_DEFEND_WITH_OF_ARMOR, DEFEND_ARMOUR)` —— **概率**维度。
- `StatGen.java:939` `pctAdd(map, DEFEND_WITH_OF_ARMOR_AGAINST_CRITICAL_HITS, DEFEND_ARMOUR, {crit})` —— **暴击作用域幅度**维度。

### 2.2 真实 PoE2 语义（StatIds 全族）
`StatIds.java` 中存在完整「Defend with X% of Armor」族与独立概率项：

| StatIds 常量 | id-string 语义 | 当前接线 |
|---|---|---|
| `CHANCE_TO_DEFEND_WITH_OF_ARMOR` | {X}% chance to Defend with {X}% of Armor | ✅ → DEFEND_ARMOUR（chance） |
| `DEFEND_WITH_OF_ARMOR_AGAINST_CRITICAL_HITS` | Defend with X% of Armor **against Crit Hits** | ✅ → DEFEND_ARMOUR（{crit}） |
| `DEFEND_WITH_OF_ARMOR_AGAINST_PROJECTILE_ATTACKS` | …against Projectile Attacks | ❌ 未接线 |
| `DEFEND_WITH_OF_ARMOR_AGAINST_HIT_DAMAGE_FROM_ENEMIES_THAT_ARE_FURTHER_THAN_XM_AWAY` | …against far enemies | ❌ 未接线 |
| `DEFEND_WITH_OF_ARMOR_WHILE_NOT_ON_LOW_ENERGY_SHIELD` | …while not on Low Energy Shield | ❌ 未接线 |

### 2.3 漂移定性
- **概念混淆**：一个属性同时承载「概率触发 Defend」与「暴击下 Defend 幅度」两个互不相干机制——前者是 chance、后者是 crit-scoped 幅度，应分属两个维度。
- **命名漏义**：`defend_armour` 读若「防御护甲（被动属性）」，实际是「**主动 Defend 守护技**以 X% 护甲承伤」+「触发几率」，名未达义；注释「护甲招架几率」又把 Defend 技误作「招架/格挡」。
- **接线残缺**：条件族 3 个变体（projectile / far / low_energy）在 StatIds 存在却未接任何属性 → 真损耗点。

---

## 3. 归一/重实现提案（待用户确认范围后执行）

遵循本模组「维度属性 + {scope}」归一哲学，将 `DEFEND_ARMOUR` 拆为两个维度：

| 新属性（维度） | 接 StatIds | scope | op |
|---|---|---|---|
| `DEFEND_WITH_ARMOUR`（Defend 幅度维度） | `DEFEND_WITH_OF_ARMOR_AGAINST_CRITICAL_HITS` | {crit} | pctAdd |
| `DEFEND_WITH_ARMOUR` | `DEFEND_WITH_OF_ARMOR_AGAINST_PROJECTILE_ATTACKS` | {projectile} | pctAdd |
| `DEFEND_WITH_ARMOUR` | `…_AGAINST_HIT_DAMAGE_FROM_ENEMIES_THAT_ARE_FURTHER_THAN_XM_AWAY` | {far} | pctAdd |
| `DEFEND_WITH_ARMOUR` | `…_WHILE_NOT_ON_LOW_ENERGY_SHIELD` | {low_energy} | pctAdd |
| `CHANCE_DEFEND_WITH_ARMOUR`（Defend 触发概率维度） | `CHANCE_TO_DEFEND_WITH_OF_ARMOR` | {} | chance |

- 全部经 `StatGen` scope-fold 接线（沿用现有通道，不新增 Effect 类型）。
- 需新增 `StatTag`：`FAR` / `LOW_ENERGY`（若 PoE2 树数据需区分；`PROJECTILE` 已存在）。
- `DEFEND_ARMOUR` 旧常量 + key 删除；`StatGen:912/939` 重定向到新属性；`runData` 重生成 stat_entry。
- 消费端（Defend 守护技读取）当前缺失（孤儿/数据先行）→ 重实现须补 `readInScope(attacker, {crit|projectile|far|low_energy|…}, DEFEND_WITH_ARMOUR)` 与 `readInScope(attacker, {}, CHANCE_DEFEND_WITH_ARMOUR)`。

---

## 4. 轻微偏差（非漂移，记录备查）

`THORNS`（`AttributeRegister.java:250`，注解「荆棘反伤倍率（受击方）」）接线：
- `StatGen:1125` `inc(GAIN_PHYSICAL_THORNS_DAMAGE_EQUAL_TO_OF_MAX_HEALTH_WHILE_SHAPESHIFTED, THORNS, {physical})` ——「变形时获得等同 X% 最大生命的物理荆棘」。
- `StatGen:1645` `inc(INC_THORNS_DAMAGE, THORNS)` ——「increased Thorns Damage」。
注解「反伤倍率」与「变形时按最大生命获得荆棘」措辞略有出入，但主题一致（均为 thorns），属注释精度问题，非语义漂移。是否顺手修正由用户裁定。

---

## 5. 待用户裁定

1. **范围**：仅 `DEFEND_ARMOUR`？还是连 `THORNS` 注释一并修正？
2. **DEFEND_ARMOUR 方案**：拆 magnitude+chance 两维度（§3，推荐）／单属性改名 `DEFEND_WITH_ARMOUR` 保留 chance+pctAdd 合并／仅修正注解不改名？
3. **key 字符串**：是否一并改 `defend_armour` → `defend_with_armour`（影响生成数据 id，runData 重生成即可，无存档兼容负担）？

---

## 6. 执行结果（已闭环，三道门全过）

> 裁定：用户 q-0 选「全量 119 属性逐条裁定」、q-1 选「chance 走 Stat 值、magnitude 走 Effect 数值（可支撑更多类型）」。本 § 记录落地状态。

### 6.1 改名归一（数据层，已完成）
- `DEFEND_ARMOUR` 删除；新增 `DEFEND_WITH_ARMOUR`(幅度) + `CHANCE_DEFEND_WITH_ARMOUR`(几率)。
- `StatTag` 新增 `FAR` / `LOW_ENERGY`（`PROJECTILE`/`CRIT` 已存在）。
- `StatGen`：`CHANCE_TO_DEFEND_WITH_OF_ARMOR`→`CHANCE_DEFEND_WITH_ARMOUR`（chance，无作用域）；`DEFEND_WITH_OF_ARMOR_AGAINST_*` 4 条件变体→`DEFEND_WITH_ARMOUR` + {crit}/{projectile}/{far}/{low_energy}（pctAdd 折叠）。含原未接线的 3 变体一并接齐。
- key 字符串：`defend_armour`→`defend_with_armour` / `chance_defend_with_armour`（`runData` 重生成，无存档负担）。

### 6.2 消费端闭环（运行时，已完成）
- `DamagePipeline` 新增 `Stage.DEFEND`（BLOCK 之后、MANA 之前）与 `stageDefend`：
  1. `readInScopePublic(living, ∅, CHANCE_DEFEND_WITH_ARMOUR)` 掷骰触发（q-1：chance→Stat 值）。
  2. 实时组装条件作用域：`{crit}`(s.crit) / `{projectile}`(s.tags 含 projectile) / `{far}`(distanceTo>DEFEND_FAR_DISTANCE=6.0m) / `{low_energy}`(ES≥DEFEND_LOW_ENERGY_FRACTION=0.5 上限，对齐「while not on Low Energy Shield」)。
  3. `readInScopePublic(living, cond, DEFEND_WITH_ARMOUR)` 折叠幅度（q-1：magnitude→Effect 数值）；减伤额=`护甲值×幅度`（`Attributes.ARMOR`，由 ADD_ARMOUR/INC_ARMOUR 供给），自 `s.newDamage` 扣减。
- 阈值常量 `DEFEND_FAR_DISTANCE=6.0` / `DEFEND_LOW_ENERGY_FRACTION=0.5` 集中可调（POE2 语料对齐，属设计假设，待用户校准）。

### 6.3 门禁
| 门 | 结果 |
|---|---|
| compileJava | ✅（仅 deprecation/unchecked 警告） |
| runData | ✅（5 个 defend stat_entry JSON 再生：chance_to_defend_with_of_armor + 4 条件变体） |
| grep | ✅（`src/main/java` 无 `DEFEND_ARMOUR` 残留，仅 `.pre_annotate`/`.new` 备份与 StatGen:939 历史注释；属性注解 `[数据先行]` 已移除并补消费端说明） |

### 6.4 结论
DEFEND 族语义漂移已端到端闭环：属性名/注解/接线/条件作用域/消费端全对应 PoE2 真义。全量 119 裁定（见 `ATTRIBUTE_FULL_ADJUDICATION.md`）其余「保留」属性经审计均为语义匹配或合法维度归一载体，无更多真漂移；`THORNS`（§4）仅注释偏差，本轮回填未涉、保持现状。
