# Stat 数据层接线 — 重审报告 + 全局推进计划（2026-08-28）

> 背景：用户观察到前序推进因上下文被压缩而丢失「归一原则」，导致在 Stat 子部分实现概念上混淆、
> 反复尝试误接/错接又回退，进入「上下文省略 → 推进极少且带错」的死循环。本文件在**不动代码**
> 的前提下先重审、再给全局计划与细化方向，作为后续推进的单一权威依据（避免多份并行文档轮回）。

## 一、重审结论（本会话已落地的 35 条接线）

**审计工具**：`poe2_assets/audit_opfamily.py`（全量 op 族校验）+ `poe2_assets/audit_session.py`（仅本会话引入的 EFF/COND 常量相关接线隔离校验）。

### 1.1 本会话接线 op 族正确性 — **0 错误**
- 隔离出 35 条使用本会话新常量（`EFF_*_COE_CRIT_*` 5 个 ALWAYS + 13 个新 COND 原语）的接线。
- `INC_*`→`ADD_MULTIPLIED_BASE`、`MORE_*`→`ADD_MULTIPLIED_TOTAL`、`TO_*`→`ADD_VALUE` 全部匹配。
- `OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH_WHILE_CHANNELLING`→`EFF_HEALTH_RECOUP`（特殊 effect，无 op）正确。
- **结论**：已提交代码中**不存在 op 族误接**。用户担心的「误接/错接」在**已落地代码**层面未被实证。

### 1.2 本会话接线作用域模式 — **0 模式偏离**
- `AGAINST_<status>_ENEMIES` 全部采用既有 Pattern 2：`stat(COND_<STATUS>_STATUS, TGT_COND_ON_TARGET, EFF_...)`，
  与 `INC_DAMAGE_WITH_HITS_AGAINST_FROZEN_ENEMIES` 等历史接线逐字同构。
- `WHILE_<self-state>` 全部采用既有 self-status 模式：`stat(COND_<STATE>_LIVING_STATE, TGT_SELF_ONLY, EFF_...)`，
  与 `INC_ENERGY_SHIELD_RECHARGE_RATE_WHILE_SHAPESHIFTED` 等同构。
- **结论**：本会话接线是**模式一致的复制**，未引入新的作用域错接。

### 1.3 `verify_gen.py` 13/13 为何不能证明正确
- 该门禁仅查：唯一性 / 引用可解析 / 计数硬编码匹配 / `$value` 使用。
- **它不查 op 族匹配、不查作用域语义**。一个 `INC_*`→`ADD_VALUE` EFF 的错接能 13/13 通过。
- 故前序「13/13 全过 = 安全」是**虚假信心**；本会话 op 审计才是真正的正确性闸门。

### 1.4 45 条 CHECK-B「mismatch」的本质（重要，勿盲目修）
- `audit_opfamily.py` CHECK B 报 45 条 `StatId 前缀期望 op ≠ EFF 实际 op` 的不匹配。
- **全部指向既有的、前序大重构（refactor §13–§29）时代的 EFF**（如 `EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_MINECRAFT_ARMOR`、`EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_STRENGTH`、`COND_LIVING_STATE_LOW_LIFE`）。
- 涉及属性：ARMOUR/STRENGTH/DEXTERITY/INTELLIGENCE/MAX_ENERGY_SHIELD/SPIRIT/RAGE/RESERVATION/BLOCK_CHANCE/各 ailment-threshold 等**基础池属性**。
- **判读**：这是「基础池属性的 increased 应走 MULTIPLIED_BASE 还是 MULTIPLIED_TOTAL/ADD_VALUE」的**代码库级设计一致性问题**，非本会话回归。
- **处置**（守 R3：不得臆造）：**不盲目修正**。列入「基础池属性 op 族专项审计」待办，需架构/用户裁定，不在机械接线轮次动它（动了可能破坏既有玩法约定）。

## 二、全局未接线存量拆解（858 条）

`classify_unwired.py` 输出（unwired_buckets.json）：

| Bucket | 数量 | 含义 | 处置 |
|---|---|---|---|
| F | 392 | 元机制/非数值（reserved-empty，R2） | 不接 |
| A | 0 | 条件+COND✓+EFF✓ 可直接接 | **机械收割已穷尽** |
| B | 6 | 条件+COND 缺失但注册表已有 backing | 加纯数据 COND + 接（低风险） |
| C | 218 | 条件+无注册表 backing | **需引擎 StatusType/求值点 → 延后（设计决策）** |
| D | 31 | 无条件+EFF 存在 | 可接，但**每条须精确 scope 核验**（非模糊 attr 匹配） |
| E | 211 | 无条件+EFF 缺失 | **需新 EFF 变体设计（真设计工作）** |

### 关键认知（轮回根因）
- **Bucket A = 0**：说明「条件从句且 COND+EFF 双满足」的机械可接项**已全部接完**。前序 scout 反复扫出的 0 剩余是真实的，不是漏。
- **D 的 31 含噪声**：classifier 按 attr 名模糊匹配，把 scope 不符的也计入（如 `INC_MANA_RECOVERY_RATE→MAX_MANA`、`INC_MANA_COST_OF_SKILLS→MAX_MANA` 实为 scope 错配）。D 的真实「干净可接」子集 < 31，须逐条 scope 核验。
- **结论**：剩余可接项**不再有免费午餐**，每条都需 R3 级人工语义核验。这正是前序「机械收割→错接→回退→循环」的成因。

## 三、细化推进方向（安全边界）

### 立即可做（低风险，守门：op 审计 + 13/13）
1. **Bucket D 精确子集**：对 31 候选逐条 grep EFF 注册表确认 scope 标签精确匹配后接线。
   - 干净样本：`INC_ACCURACY_WITH_BOWS` 等武器系需确认存在 `bow`-scope EFF；不存在则转 E。
   - 规则：stat 的武器/元素/作用域限定词必须体现在 EFF 的 tags/name 中，否则不接（防 scope 错配）。
2. **Bucket B（6 条）**：为注册表已有的 StatusType/LivingState 补纯数据 COND 原语（仿 §Phase4续范式），再接线。

### 需设计决策（不擅自臆造，列 backlog 待用户/架构裁定）
3. **Bucket C（218）**：缺注册表 backing 的条件（presence/herald/totem/charm/arcane_surge/weapon-type/enemy-type/新 StatusType）→ 引擎层补 StatusType + 求值点。
4. **Bucket E（211）**：缺对应 op 变体的无条件 stat（如 `TO_ALL_ATTRIBUTES`、`INC_HEALTH_REGENERATION_RATE` 缺 inc 变体）→ 设计新 EFF 变体（同属性/同机制仅 op 不同）。
5. **基础池 op 族专项（45 条 CHECK-B）**：独立于本接线轮次，需架构裁定。

## 四、推进门禁（防轮回的硬约束）
每次改动后必须：
1. `audit_opfamily.py` CHECK A/B **0 新增 mismatch**（尤其 B 不得新增本会话引入的错接）。
2. `audit_session.py` 本会话接线 **0 op mismatch**。
3. `compileJava` → `runData --no-configuration-cache` → `verify_gen.py` 13/13。
4. 新增 EFF/COND 必同步 `verify_gen.py` 硬编码计数（effect/condition）。
5. **绝不**靠模糊 matcher 批量写；每条接线须经 scope 语义核验（R3）。

## 五、推进记录（2026-08-28 续·执行批次）

### 5.1 本批次接线（Bucket D 精确子集，6 条，已落地 + 三道门禁全过）
- 插入位置：`StatGen.buildEntries` 末尾（`OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH_WHILE_CHANNELLING` 之后、`return map` 之前）。
- 接线（均 `stat(TGT_SELF_ONLY, EFF_...)`，`INC_*`→`ADD_MULTIPLIED_BASE`，复用既有同构先例）：
  - `INC_DAMAGE_WITH_CROSSBOWS` → `EFF_DAMAGE_CROSSBOW_DAMAGE`（`{DAMAGE,CROSSBOW}`，先例 `INC_CROSSBOW_DAMAGE`）
  - `HERALD_SKILLS_DEAL_INC_DAMAGE` → `EFF_DAMAGE_DAMAGE_HERALD`（`{DAMAGE,HERALD}`，先例 `INC_HERALD_DAMAGE`/`INC_DAMAGE_WHILE_AFFECTED_BY_A_HERALD`）
  - `EVERY_RAGE_ALSO_GRANTS_INC_FIRE_DAMAGE` → `EFF_DAMAGE_DAMAGE_FIRE`（`{DAMAGE,FIRE}`，先例 `INC_FIRE_DAMAGE_WHILE_IGNITED`）
  - `EVERY_RAGE_ALSO_GRANTS_INC_PHYSICAL_DAMAGE` → `EFF_DAMAGE_DAMAGE_PHYSICAL`（`{DAMAGE,PHYSICAL}`，先例 `INC_PHYSICAL_DAMAGE`）
  - `EVERY_RAGE_ALSO_GRANTS_INC_SPELL_DAMAGE` → `EFF_DAMAGE_DAMAGE_SPELL`（`{DAMAGE,SPELL}`，先例 `INC_SPELL_DAMAGE`）
  - `EVERY_RAGE_ALSO_GRANTS_YOU_INC_MINION_DAMAGE` → `EFF_DAMAGE_DAMAGE_MINION`（`{MINION,DAMAGE}`，先例 `MINION_DAMAGE`）
- **排除项**：`INC_DAMAGE_WHILE_YOU_HAVE_A_TOTEM` — `EFF_DAMAGE_TOTEM` 的 `{TOTEM}` 标签语义为「图腾自身伤害」，与「持图腾时自身伤害提升」不符（缺 presence 条件原语，见 buildEntries 内 973 行 companion/totem 推迟注释）→ **推迟**。
- **门禁结果**：`audit_opfamily` CHECK A=0 / CHECK B=45（仍为前序基础池基线，本批 0 新增）；`audit_session` session 接线 op mismatch=0；`compileJava`✅→`runData --no-configuration-cache`✅→`verify_gen.py` **13/13 PASS**，`entry=922`（916→+6），effect=550/target=14/condition=52 不变。

### 5.2 决定性发现：Bucket D「干净可接」子集已彻底穷尽（防轮回关键证据）
- 枚举全部 53 个 DAMAGE 系 `ModifierStatEffect` EFF 的真实标签集，对全部 unwired StatIds 做「scope 精确匹配」matcher，产出 **13 条单标签候选 + 1 条多标签候选**，逐条人工 R3 复核，**14 条全部为 false positive**：
  - 元机制重定向（非简单增伤）：`INC_AND_REDUCTIONS_TO_COMPANION_DAMAGE_ALSO_APPLY_TO_YOU`、`INC_AND_REDUCTIONS_TO_PROJECTILE_SPEED_ALSO_APPLY_TO_DAMAGE_WITH_BOWS`。
  - 隐藏条件（per-数/近期/距离/耗血）：`INC_ATTACK_DAMAGE_FOR_EACH_DIFFERENT_NON_INSTANT_SPELL...`、`INC_ATTACK_DAMAGE_FOR_EACH_MINION...`、`INC_SPELL_DAMAGE_FOR_EACH_DIFFERENT_NON_INSTANT_ATTACK...`、`INC_SPELL_DAMAGE_WITH_SPELLS_THAT_COST_HEALTH`、`INC_MELEE_DAMAGE_WITH_HIT_DAMAGE_AT_CLOSE_RANGE`。
  - 属性错配（非 DAMAGE）：`INC_BALLISTA_CRITICAL_DAMAGE_BONUS`(CRIT_DAMAGE)、`INC_STUN_BUILDUP_WITH_MELEE_DAMAGE`(STUN)、`INC_PARRY_HIT_DAMAGE_AREA_OF_EFFECT`(AOE)、`INC_SPELL_AREA_DAMAGE`(AOE)、`INC_ATTACK_AND_CAST_SPEED_WITH_ELEMENTAL_SKILLS`(SPEED)。
  - 过作用域（限定词未体现在 EFF 标签）：`INC_ATTACK_COLD_DAMAGE`(`{DAMAGE,COLD}` 漏 ATTACK)、`INC_PHYSICAL_DAMAGE_WITH_BOWS`(`{DAMAGE,PHYSICAL}` 漏 BOW)。
- **结论**：机械「无条件和 EFF 存在」的可接项已彻底接完；本批 6 条即 Bucket D 全部正确集。剩余每条均落入 B/C/E（条件原语 / 引擎设计 / 新 EFF 变体）或属性错配，属设计决策，非机械可接。**此证据永久记录，避免上下文压缩后重跑 matcher 再陷轮回。**

### 5.3 下一步（需引擎设计 / 用户决策，非机械接线）
- **Bucket B（6 条）**：`INC_ATTACK_DAMAGE_IF_YOU_HAVE_SHAPESHIFTED_TO_AN_ANIMAL_FORM_RECENTLY` 等「recently/形态」条件 → 需先确认 `LivingStateModes`/事件历史是否真有对应 backing，再补纯数据 COND 原语（仿 Phase 4续）+ `verify_gen` condition 计数 52→N。
- **Bucket C（218）**：presence/herald/totem/charm/arcane_surge/weapon-type/enemy-type 条件 → 引擎层补 StatusType + 求值点。
- **Bucket E（211）**：缺 op 变体的无条件 stat → 设计新 EFF 变体（同属性/同机制仅 op 不同）。
- **基础池 op 族专项（45 条 CHECK-B）**：独立于本接线轮次，需架构裁定（ARMOUR/STRENGTH/DEXTERITY/MAX_ENERGY_SHIELD/SPIRIT/RAGE/RESERVATION/ailment-threshold 等 `INC_*` 应走 MULTIPLIED_BASE 还是 MULTIPLIED_TOTAL/ADD_VALUE 的代码库级一致性）。
- **TAKEN 侧 / 承伤侧**：`INC_DAMAGE_WHILE_YOU_HAVE_A_TOTEM` 等 presence 类、承受侧语义，待条件原语补齐后统一处理。

## 六、本轮扩展侦察结论（2026-08-28 第二轮推进）

> 上一轮已确认「DAMAGE 系精确子集已穷尽」。本轮把 matcher 从 `DAMAGE` 单属性**扩展到全部 309 个 `ModifierStatEffect` EFF**（脚本 `poe2_assets/harvest_widen.py`），并侦察了条件原语基础设施，目标：确认是否存在「条件原语已有引擎 backing、可纯数据补 COND 后接线」的安全带。结论：**不存在**。

### 6.1 全属性扩展 harvest — 干净候选 = 0
- 对全部 unwired StatIds（现 852 条）做 EXACT scope 相等 + op 族匹配 + 属性引用匹配（脚本 `harvest_widen.py`，逻辑：stat 的 scope token 集合 == EFF 非属性标签集合，op 前缀匹配，属性 token 出现于 stat 名）。
- 产出候选仅 **1 条 base + 6 条 scoped**，逐条人工 R3 复核，**全部 false positive**：
  - `INC_DAMAGE_WITH_PLANT_SKILLS`→`DAMAGE_DAMAGE`：隐藏作用域 `PLANT_SKILLS` 未体现在 EFF 标签（过作用域）。
  - 4× `INC_CRITICAL_DAMAGE_BONUS_WITH_{BOW/CROSSBOW/SPEAR/BALLISTA}`：属性是 `CRIT_DAMAGE` 而非 `DAMAGE`（属性错配）。
  - `INC_SPELL_DAMAGE_WITH_SPELLS_THAT_COST_HEALTH`：隐藏条件 `COST_HEALTH`（条件未消）。
  - `INC_STUN_BUILDUP_WITH_MELEE_DAMAGE`：`STUN_BUILDUP`（状态积蓄）非伤害幅度（属性错配）。
- **结论**：全属性机械 harvest 的干净可接集 = **空集**。前序 6 条（crossbow/herald/fire/physical/spell/minion）即全局最大正确集。

### 6.2 条件原语基础设施侦察 — 「已有 backing 的可接安全带」同样为空
已存在且已用于已接 stat 的 COND 原语（`StatConditionGen.java`）：
- `COND_LIVING_STATE_SHAPESHIFTED`(L75)、`COND_LIVING_STATE_LOW_LIFE`(L69)、`COND_HEAVY_STUN_STATUS`(L68)、各 Ailment/Ground/Debuff `StatusValue`、`EventHistoryValue`(RECENTLY_TAKEN_HIT/STUN/USE_ITEM/TOTEM)。
- 即 `SHAPESHIFTED` / `ON_LOW_LIFE` / `HEAVY_STUNNED`(状态) / `RECENTLY_TAKEN_*` 等**纯数据条件已可用**。

但 Bucket B 的 6 条 unwired stat 需要的是**额外、当前无 backing 的条件**：
- `RECENTLY_SHAPESHIFTED_TO_ANIMAL/HUMAN_FORM`：`EventName.FORM_CHANGED/FORM_CLEARED`(EventName.java:222-224) 仅记录「形态切换」事件，未带「动物/人类」分形标签的 LivingState/Condition 原语 → **无 backing**。
- `RECENTLY_HEAVY_STUNNED`：仅有 `COND_HEAVY_STUN_STATUS`（当前状态），无「近期」变体 LivingState → **无 backing**（仅有 `RECENTLY_TOOK_DOT` 等，无 heavy-stun-recently）。
- `WHILE_NOT_ON_LOW_MANA`：仅有 `LOW_LIFE`，无 `NOT_LOW_MANA`/`NOT_ON_LOW_MANA` LivingState → **无 backing**；且分类器将其 EFF 错配为 `COE_MAX_MANA`（攻击速度≠最大mana，分类器噪声）。
- `AGAINST_IMMOBILISED_ENEMIES`（#4 的承受侧）：`StatTag.IMMOBILISE` 存在，但 `StatEffectGen` 中**无任何 EFF 携带 IMMOBILISE 标签**（grep 全量 0 命中）；已接的 `INC_CRITICAL_HIT_CHANCE_AGAINST_IMMOBILISED_ENEMIES`(StatGen:614) 用的是无 IMMOBILISE 标签的 `COE_CRIT_CHANCE_CRIT`，属**过作用域近似**。且无 target-side（EVENT_TARGET）条件工厂支持 → **无法正确接线**。

### 6.3 决定性结论（写死，防轮回）
1. **机械精确接线安全带 = 空集**（DAMAGE 单属性 + 全属性扩展两次独立确认）。
2. **「条件原语已有 backing」安全带 = 空集**：现有 COND 原语已被已接 stat 消耗；未接 stat 所需条件（recently-形态分形 / recently-重晕 / not-low-mana / against-immobilised-target）均缺引擎 backing。
3. 剩余 852 条 unwired **全部**落入 B/C/E + 基础池 op 专项：每条都需 R3 级「引擎真读 / 新 EFF 变体 / 新 COND 原语 + StatusType」之一的**设计决策**，无法靠模式复制安全推进。
4. 故：**继续机械接线已无意义 = 进入设计决策带**。下一步必须由用户裁定方向（见 5.3 的 B/C/E/基础池四选一或并行），助手不得臆造条件原语或 EFF 变体（R2/R3 铁律）。

> 本结论与上一轮 §5.2「Bucket D 干净子集已彻底穷尽」互为交叉验证：两次独立 matcher（单属性 + 全属性）均产出 0 干净候选，且条件基础设施侦察确认无「已有 backing 的安全带」。

### 6.4 修正：被 has_cond 过滤漏判的 COND 原语式安全带（「对低/满血敌人」族）
> ⚠️ **重要更正**：§6.1/§6.2 的「空集」结论针对的是**EFF 标签作用域式**机械 harvest（`harvest_widen.py` 与 §5.2 单属性 matcher 均用 `has_cond()` 过滤掉名字含 `AGAINST`/`WHILE`/`ON_` 的 stat）。这一过滤**整族漏掉**了「对低/满血敌人」族——它**不走 EFF 标签作用域**，而走 **COND 原语 + `TGT_COND_ON_TARGET`** 机制（条件体现在 COND 原语针对目标实体求值，EFF 用基础无标签族）。这是 §6.2 把「条件原语已有 backing 安全带=空集」误判的根因，也是用户警告的「上下文损失→漏接」的另一面。

**证据（全部来自已读源码，非臆造）**：
- 先例 `DAMAGE_LUCKY_VS_LOW_LIFE`(StatGen:387) = `stat(COND_LIVING_STATE_LOW_LIFE, TGT_COND_ON_TARGET, EFF_DAMAGE_LUCKY)` —— 证明 `COND_LIVING_STATE_LOW_LIFE`+`TGT_COND_ON_TARGET` **目标侧低血**可用。
- AGAINST 族 (StatGen:1022-1063) = `stat(COND_<STATUS>, TGT_COND_ON_TARGET, <基础 EFF>)` —— 证明「目标持有状态→对敌增益」模式逐字同构。
- `COND_LIVING_STATE_LOW_LIFE`(StatConditionGen:69) 与 `COND_FULL_LIFE_LIVING_STATE`(L66) 结构**完全相同**（`LivingStateValue(mode, 80.0)`，仅 `LivingStateModes` 枚举不同）。故 FULL_LIFE 镜像经 `TGT_COND_ON_TARGET` 目标侧求值是**结构对称成立**的（纯数据条件 + 目标重定向）。
- 基础 EFF 全部已存在且被兄弟接线使用：`EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE`(DAMAGE)、`EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE`(CRIT_CHANCE)、`EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_DAMAGE`(CRIT_DAMAGE, MULTIPLIED_BASE)、`EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AILMENT_BUILDUP_STUN`(STUN 标签, L154)、`EFF_DAMAGE_LUCKY`(L542)。

**与 §6.2 其他「无法接」结论不冲突**：`AGAINST_IMMOBILISED_ENEMIES` 仍**不可接**（无 `IMMOBILISE` COND 原语，`StatConditionGen` 仅 `StatTag.IMMOBILISE` 标签无对应 StatusType 条件）；recently-形态/重晕/not-low-mana 仍无 backing。仅 LOW/FULL_HEALTH 族因条件原语早已注册而可接。

### 6.5 本轮接线记录（「对低/满血敌人」安全带 6 条，已落地 + 三道门禁全过）
- 插入位置：`StatGen.buildEntries` 末尾（`INC_STUN_BUILDUP…` 注释块之后、`return map` 之前）。
- 接线（均 `stat(COND_*, TGT_COND_ON_TARGET, EFF_*)`，`INC_*`→`ADD_MULTIPLIED_BASE`；LUCKY 为 flag 效应无 op；BUILDUP 走 AILMENT_BUILDUP 属性）：
  - `DAMAGE_WITH_HITS_IS_LUCKY_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH` → `COND_LIVING_STATE_LOW_LIFE` + `TGT_COND_ON_TARGET` + `EFF_DAMAGE_LUCKY`（仿 L387）
  - `INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH` → `COND_LIVING_STATE_LOW_LIFE` + `TGT_COND_ON_TARGET` + `EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE`
  - `INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_ON_FULL_HEALTH` → `COND_FULL_LIFE_LIVING_STATE` + `TGT_COND_ON_TARGET` + `EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE`（FULL_LIFE 镜像）
  - `INC_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_THAT_ARE_ON_FULL_HEALTH` → `COND_FULL_LIFE_LIVING_STATE` + `TGT_COND_ON_TARGET` + `EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE`
  - `INC_CRITICAL_DAMAGE_BONUS_AGAINST_ENEMIES_THAT_ARE_ON_FULL_HEALTH` → `COND_FULL_LIFE_LIVING_STATE` + `TGT_COND_ON_TARGET` + `EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_DAMAGE`
  - `INC_STUN_BUILDUP_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH` → `COND_LIVING_STATE_LOW_LIFE` + `TGT_COND_ON_TARGET` + `EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AILMENT_BUILDUP_STUN`（仿 L968 electrocute 先例）
- **门禁结果**：`audit_opfamily` CHECK A=0 / CHECK B=45（前序基线，**本批 0 新增**；grep 确认 6 个新 StatId 均未出现在任一 mismatch 段）；`compileJava`✅→`runData --no-configuration-cache`✅→`verify_gen.py` **13/13 PASS**，entry=**928**（922→+6），effect=550/target=14/condition=52 不变、0 unresolved、0 非 $value。
- **剩余未接 unwired 存量更新**：852 − 6 = **846 条**，仍全部落入 Bucket B（recently-形态/重晕/not-low-mana，仍无 backing）/ C（引擎设计）/ E（新 EFF 变体）/ 基础池 45 条 CHECK-B op 专项 / TAKEN 侧，均属设计决策。

### §6.7 Phase 7 — 自侧 WHILE/ON 在场条件族（数据驱动匹配，彻底关闭上下文盲区）

**方法升级（防轮回核心）**：新建 `poe2_assets/harvest_conditions.py` —— 解析 `StatGen.buildEntries()` 全量接线图为 (StatIds → EFF+COND+TGT) 真相表，对每条 unwired 名**剥离条件子句**（AGAINST_*/VS_*/WHILE_*/ON_*）得到 base subject，查表定位 base 已接线 EFF；条件子句**仅映射到已存在 COND 原语**。这样 EFF 精确复用（op 族构造一致），零臆造。

**两处关键 bug（即「假穷尽」根因，已修）**：
1. `name[:idx]` 含尾随下划线 → base 变 `INC_ARMOR_AND_EVASION_` 匹配不到已接线键 → 误报 0 候选。**修正**：`base = name[:idx].rstrip("_")`。
2. COND_MAP 含 `AGAINST_` / `AGAINST_ENEMIES_THAT_ARE` 通配回退 → 把 `COND_FREEZE_STATUS` 错配给所有 AGAINST 子句（含敌人类型 DEMONS/UNDEAD/HUMANOIDS/MARKED/RARE_UNIQUE/CONSTRUCTS/破甲/距离等**无对应 COND 原语**者）。**修正**：删通配，仅保留精确状态子句。修后候选从 29（含大量误报）→ **12 真实候选**（全自侧、全真实 COND）。

**接线纪律（C5 op 族）**：12 候选中跳过 `INC_STUN_THRESHOLD_*` / `INC_BLOCK_CHANCE_WHILE_*` —— 其 base EFF 为 `ADD_VALUE`(flat)，与 `INC_` 前缀 op 族不符（C5：「仅 flat 缺 inc 变体者才是真缺口」）→ 属 Bucket E，不臆造。实际接 **7 条**：
- `GAIN_OF_DAMAGE_AS_EXTRA_{COLD,FIRE,LIGHTNING}_DAMAGE_WHILE_SHAPESHIFTED` → `COND_LIVING_STATE_SHAPESHIFTED` + `TGT_SELF_ONLY` + 各自 convert-gain EFF（复用 L982/984/986）。
- `INC_ARMOR_AND_EVASION_WHILE_LEECHING` → `COND_LEECHING_LIVING_STATE` + `TGT_SELF_ONLY` + `EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_EVASION_MINECRAFT_ARMOR`（复用 L189）。
- `INC_SKILL_SPEED_WHILE_SHAPESHIFTED` → `COND_LIVING_STATE_SHAPESHIFTED` + `TGT_SELF_ONLY` + `EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CAST_SPEED`（复用 L758，op 族完美匹配）。
- `REGENERATE_OF_MAX_HEALTH_PER_SECOND_WHILE_{ON_LOW_HEALTH,SURROUNDED}` → 各自 COND + `TGT_INLINE_SELF` + `EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_HEALTH_REGEN`（复用 L867）。

**门禁结果**：`compileJava`✅→`runData --no-configuration-cache`✅→`verify_gen.py` **13/13 PASS**，entry=**940**（933→+7），effect=550/target=14/condition=52 不变、`0 unresolved`、`0 非 $value`。`audit_opfamily` CHECK A=0 / CHECK B=**47**（46→+1，仅 `INC_ARMOR_AND_EVASION_WHILE_LEECHING`；其 base `INC_ARMOR_AND_EVASION` 已在基线 CHECK B 列表 L20，同属 MULTIPLIED_TOTAL 例外类，**非新错误类**；其余 6 条 0 新增 mismatch）。

**结论（机械安全带真正穷尽，第三类确认）**：EFF 标签作用域式 + COND 原语式（AGAINST 目标侧 + WHILE/ON 自侧）三类安全带现已全部扫完并补接。剩余 unwired 全部属 Bucket B/C/E + 基础池 CHECK-B + TAKEN 侧——**均须设计裁定**，机械复制已无可安全推进项。

> ⚠️ **§6.7「三类真正穷尽」结论的自我修正（Phase 8 发现）**：该结论依赖 `harvest_conditions.py` 的 `COND_MAP` 已覆盖**全部 56 个 COND 原语**的条款词汇。但 Phase 8 复核发现 `COND_MAP` 仍漏掉了 `COND_COUNT_KILL_RECENTLY` / `COND_COUNT_RECENTLY_USE_TOTEM` / `COND_PRESENCE_RARITY_RARE_UNIQUE` 等的条款写法（`IF_YOU_VE_KILLED_RECENTLY` / `AGAINST_RARE_OR_UNIQUE_ENEMIES`），导致 RECENTLY 击杀族与 RARE/UNIQUE 在场族被整族漏扫。这正是「上下文损失→漏接」的同类盲区：**匹配器的词汇表本身必须逐条对照 56 个 COND 原语补齐，否则「穷尽」是假象**。Phase 8 已补齐并复扫。

### §6.8 Phase 8 — RECENTLY 击杀族 + RARE/UNIQUE 在场族（补齐 COND_MAP 后复扫）

**动机**：逐条对照 `StatConditionGen` 全 56 个 COND 原语，发现 §6.7 的 `COND_MAP` 漏了三类有先例的条款写法，须补齐后复扫，否则「穷尽」是假象。

**扩展 COND_MAP（均有 StatGen 先例）**：
- `IF_YOU_VE_KILLED_RECENTLY` → `COND_COUNT_KILL_RECENTLY`（先例 L970 `INC_AREA_OF_EFFECT_IF_YOU_VE_KILLED_RECENTLY`、L1050 `INC_CRIT_CHANCE_IF_YOU_HAVE_KILLED_RECENTLY`、L630 `INC_DAMAGE_RECENTLY_KILLED`，均为自侧 `EventHistoryValue`）。
- `AGAINST_RARE_OR_UNIQUE_ENEMIES` / `AGAINST_RARE_AND_UNIQUE_ENEMIES` → `COND_PRESENCE_RARITY_RARE_UNIQUE`（先例 L927 `INC_ACCURACY_AGAINST_RARE_OR_UNIQUE_ENEMIES`、L1043 `INC_ATTACK_DAMAGE_AGAINST_RARE_OR_UNIQUE_ENEMIES`，均为**自侧** `TGT_SELF_ONLY` + `PresenceRarityValue`，名字里的 AGAINST 实为「在场稀有/传奇敌人」自侧条件）。

**复扫结果（matcher 出 8 候选，按 C5 op 族纪律筛）**：
- **接 4 条**（base EFF 为 MULTIPLIED_* 或既有 CullingStatEffect，op 族一致、零新增 EFF/COND）：
  - `INC_ATTACK_SPEED_IF_YOU_VE_KILLED_RECENTLY` / `INC_MOVEMENT_SPEED_IF_YOU_VE_KILLED_RECENTLY` → `COND_COUNT_KILL_RECENTLY` + `TGT_SELF_ONLY` + 各自 `MULTIPLIED_BASE` EFF（复用 L578/L723 同源）。
  - `INC_DAMAGE_WITH_HIT_DAMAGE_AGAINST_RARE_AND_UNIQUE_ENEMIES` → `COND_PRESENCE_RARITY_RARE_UNIQUE` + `TGT_SELF_ONLY` + `EFF_..._COE_DAMAGE_DAMAGE`（**L1113 已证明该 base 用此 EFF**，L1043 已证明该 COND+TGT，1:1 同构，零臆造）。
  - `INC_CULLING_STRIKE_THRESHOLD_AGAINST_RARE_OR_UNIQUE_ENEMIES` → 同上 COND+TGT + `EFF_CAUSED_HIT_CULLING`（`CullingStatEffect`，复用 L616 同源，非 ModifierStatEffect，不触 CHECK A/B）。
- **跳过 4 条**（C5 真缺口，flat `ADD_VALUE` base EFF 接 `INC_` 前缀 → Bucket E，不臆造）：`INC_BLOCK_CHANCE_WHILE_SURROUNDED`、4×`INC_STUN_THRESHOLD_WHILE_*`（CHANNELLING/ON_FULL_HEALTH/PARRYING/SHAPESHIFTED）。
- **跳过 `INC_CHANCE_TO_INFLICT_AILMENTS_AGAINST_RARE_OR_UNIQUE_ENEMIES`**：base `INC_CHANCE_TO_INFLICT_AILMENTS` 非独立接线（matcher 未命中），强行接需臆造 ailment-chance EFF → R3 禁止。

**门禁结果**：`compileJava`✅→`runData --no-configuration-cache`✅→`verify_gen.py` **13/13 PASS**，entry=**944**（940→+4），effect=550/target=14/condition=52 不变、0 unresolved、0 非 $value。`audit_opfamily` CHECK A=0 / CHECK B=**47**（与 Phase 7 基线持平，**本批 0 新增**；4 个新 StatId 经 grep 确认未出现在任一 mismatch 段）。

**结论（机械安全带真正穷尽，最终态）**：在「COND_MAP 已逐条对照 56 个 COND 原语补齐」前提下，机械安全带（EFF 标签作用域式 + COND 原语式目标侧 AGAINST/VS_状态 + COND 原语式自侧 WHILE/ON_状态 + COND 原语式自侧 RECENTLY/在场稀有）现已全部扫完并补接。剩余 unwired 全落 Bucket B/C/E + 基础池 CHECK-B + TAKEN 侧，**均须设计裁定**，机械复制已无可安全推进项。

---

## 七、Phase 9 — 基础池 CHECK-B 专项：发现「零基禁 inc」法则，45 条 → 0 真错接

> ⚠️ **本节推翻 §1.4 的判读**。§1.4 把 45 条 CHECK-B 归为「代码库级设计一致性问题，需架构裁定」——**理由错了**。真相是：绝大多数条目受 `AttributeRegister` 自身确立的**三定律 #3「零基禁 inc」**约束，当前接法是唯一可行解，属**审计器误报**。

### 7.1 关键证据：`AttributeRegister.java` 头部三定律（L30-33）

```
3. 零基禁 inc：base=0 属性只能 pctAdd(ADD_VALUE)/ADD_MULTIPLIED_TOTAL，绝不可 inc
   （fold 式 ADD_MULTIPLY_BASE ≡ base*ΣmultBase ≡ 0 静默死，见 [零基] 标注属性）。
```

**机制澄清（此前混淆点）**：
- `ADD_MULTIPLIED_BASE` = **属性注册 default base** × Σmult（**不看**其他 flat 加成累积值）
- `ADD_MULTIPLIED_TOTAL` = **当前累积值** × (1 + mult)

⇒ 对 base=0 的属性，BASE **永久归零**（词条静默失效），而 TOTAL 在有 flat 加成时**有效**。故「把 `INC_*` 改接 BASE」不是修 bug，而是**制造死词条**。

### 7.2 全量 base 盘点（`poe2_assets/attr_base_table.py` → `attr_base_table.json`）

- 解析 108 个属性：`base==0` **47 个** / `base!=0` **61 个**。
- 显式 `[零基·禁inc]` 标注仅 **8 个**（STRENGTH/DEXTERITY/INTELLIGENCE/FOUND_ITEM_NUMBER/FOUND_ITEM_RARITY/STATUS_EXTRA_STACKS/STATUS_MAX_STACKS/ADD_EMPOWERED_ATTACKS），**39 个 base=0 属性缺标注**（文档缺口，不影响运行时）。
- 判定以 **base 数值**为准（标注不可靠）：`attr_base_table.py` 同时输出 marker/base 不一致清单。

### 7.3 47 条 CHECK-B 三分类（`poe2_assets/checkb_zerobase_split.py`）

| 分类 | 数量 | 含义 | 处置 |
|---|---|---|---|
| 零基误报 | 33 | 目标 op=BASE 但属性 base=0 | **豁免，保持现状**（改则死） |
| 语义豁免 | 12 | 前缀≠实际语义，语料/源码证实当前 op 正确 | **豁免**，不改接线、不改 StatIds（R2） |
| 真错接 | 2 | op 族违反且 base≠0 | **修正** |

**12 条语义豁免的逐条证据**（R3：全部经语料/源码核验，非批量臆造）：
- `INC_REGENERATE_LIFE_*`（10 条）：语料原文 `Regenerate 0.1%/0.2%/0.5%/0.75%/1%/1.5% of maximum Life per second` → **flat 加算**，`ADD_VALUE` 正确；`INC_` 是生成期命名产物。
- `ADD_RECENTLY_CONSUMED_DAMAGE`：`BuffStatusTypes.RECENTLY_CONSUMED` 注释明写「换算为 **inc_damage_attack 式增伤**」；且 EFF 标签为 `TagExpr(DAMAGE & not(TAKEN))` = **增伤侧**（命名里的 `_TAKEN` 实为「排除承受侧」）→ `ADD_MULTIPLIED_BASE` 正确。
- `INC_MAX_RAGE`：Rage 语料仅 `+N to Maximum Rage`(flat) 与 `100% more Maximum Rage`，**不存在 "increased Maximum Rage"** → 无源常量；与同义 `TO_MAX_RAGE` 对齐保持 `ADD_VALUE`，按 R2 不动。

### 7.4 本轮修正（2 条真错接，新增 2 个 EFF 变体，零引擎改动）

| StatId | 语料原文 | 修正 |
|---|---|---|
| `MORE_MAX_RAGE` | `100% more Maximum [Rage]` | `ADD_MULTIPLIED_BASE` → 新增 `EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_MAX_RAGE`（MAX_RAGE base=50 非零基） |
| `TO_CRITICAL_HIT_CHANCE_OF_HERALD_SKILLS` | `+8% to [Critical\|Critical Hit] Chance of [Herald] Skills` | `ADD_MULTIPLIED_BASE` → 新增 `EFF_ADD_VALUE_ATTRIBUTE_COE_CRIT_CHANCE_HERALD`（`{HERALD}` 作用域仿既有 BASE 变体） |

- 落点均为「既有属性 + 既有 op 枚举 + 既有 TagExpr」，纯数据，零引擎改动（Phase 4 续范式）。
- 同步 `scripts/verify_gen.py` effect 计数 **550 → 552**（硬编码，否则门禁 FAIL）。

### 7.5 审计器升级（`poe2_assets/audit_opfamily.py`）

CHECK B 由「单一 fail 计数」改为**三分类**，并把零基法则内建：
```
CHECK B actionable: 0
CHECK B zero-base exempt: 33   (三定律#3: base==0 → inc would die)
CHECK B semantics-allowlisted: 12   (corpus/source proves current op correct)
CHECK B raw prefix mismatches: 45
```
- 新增 `attr_base()`（读 `attr_base_table.json` + `VANILLA_BASE` 表）。
- 新增 `SEMANTIC_ALLOWLIST` / `SEMANTIC_ALLOWLIST_PREFIX`：**每条必须带内联证据注释**，禁止无据加白。
- **修 bug**：EFF 属性解析正则 `AttributeRegister\.(\w+)` → `(?:AttributeRegister|Attributes)\.(\w+)`。此前不识别 vanilla `Attributes.ARMOR`，导致 4 条 vanilla 护甲条目（`INC_ARMOUR`/`INC_ARMOUR_EVASION`/`INC_ARMOR_AND_EVASION`/`..._WHILE_LEECHING`）被误报为 actionable。

### 7.6 门禁结果（Phase 9 终态）

`compileJava`✅ → `runData --no-configuration-cache`✅ → `verify_gen.py` **13/13 PASS**
- effect=**552**（550→+2）/ target=14 / condition=52 / entry=**944**（不变）
- 0 unresolved、0 非 `$value`
- `audit_opfamily`：**CHECK A=0 / CHECK B actionable=0**

### 7.7 结论与剩余方向

1. **基础池 CHECK-B 专项已清零**：47 条「疑似错接」经零基法则 + 语料核验，证实仅 2 条真错接（已修），其余 45 条为误报/豁免。此前「45→46→47 递增」的焦虑源于审计器不识零基法则。
2. **对 Bucket E 的重要推论**：Phase 7/8 跳过的 `INC_STUN_THRESHOLD_*` / `INC_BLOCK_CHANCE_*` / `INC_RESERVATION_EFFICIENCY` 等，**其真实阻塞原因不是「缺 EFF 变体」，而是属性 base=0 —— 补 BASE 变体同样会静默归零**。故这批应重分类为**结构性不可解**，除非架构层调整属性 base 或引入「加算池」概念。**不得**为其创建新 ADD_MULTIPLIED_BASE EFF（三定律 #3）。
3. 剩余可推进项仍为 Bucket B/C/E 中的**非零基**子集 + 需引擎 backing 的条件族，均须设计裁定，严守 R2/R3。

---

## 八、Phase 10 — 死 EFF / 死接线：零基法则的反面（6 条真实词条静默失效，已修）

> Phase 9 处理的是「**不该**接 BASE 的别接」；本节是它的**反面**：**已经**接了 BASE、且属性 base=0 的接线 —— 这些词条**当前正在静默失效**（贡献恒为 0），是**真 bug**（非审计误报）。

### 8.1 审计盲区：CHECK B 看不见「正确但已死」的接线

CHECK B 的规则是「`INC_*` 应走 `ADD_MULTIPLIED_BASE`」。因此一条 `INC_*` 接了 BASE 的接线**会被判为正确**——但若该属性 base=0，它就是死词条。**CHECK B 结构上无法发现这类问题**，必须新增独立检查。

### 8.2 引擎侧确证（`StatEngine.fold` L229-231）

```java
double baseValue = living.getAttributeBaseValue(attribute);
out = original + r.add() + baseValue * r.multBase();
out *= (1 + r.multTotal());
```

`baseValue` 取的是**属性注册 base**（`getAttributeBaseValue`），**不是当前累积值** —— 与 §7.1 的机制判断完全一致，零基归零成立。
另核查：全项目仅 `SkillTotem:84` 对 `MAX_HEALTH`（图腾实体）`setBaseValue`，**vanilla `Attributes.ARMOR` 的 base=0.0 未被任何代码覆盖**。

### 8.3 扫描结果（`poe2_assets/dead_eff_scan.py` → `dead_eff_scan.json`）

- 402 个 `ModifierStatEffect` 中 250 个是 `ADD_MULTIPLIED_BASE`，其中**仅 1 个**落在 base=0 属性上：
  `EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ARMOR`（vanilla `Attributes.ARMOR` base=0.0）。
- 该死 EFF **被 6 条接线使用** → **6 条词条静默失效**：
  `INC_ARMOR`、`INC_ARMOR_WHILE_STATIONARY`、`INC_ARMOR_WHILE_SURROUNDED`、`INC_ARMOR_WHILE_SHAPESHIFTED`、`INC_ARMOR_WHILE_IGNITED`、`INC_ARMOR_WHILE_BLEEDING`。

### 8.4 语料核验：失效的恰好是真实词条，可用的反而是空壳

| StatId | 语料原文 | 修复前接法 | 状态 |
|---|---|---|---|
| `INC_ARMOR` | `10%/12%/15%/18%/25%/30% increased [Armour]`、`20% reduced [Armour]` | 死 EFF（BASE） | ❌ 归零 |
| `INC_ARMOR_WHILE_STATIONARY` | `15%/30% increased [Armour] while stationary` | 死 EFF（BASE） | ❌ 归零 |
| `INC_ARMOR_WHILE_SURROUNDED` | `20%/30% increased [Armour] while [Surrounded]` | 死 EFF（BASE） | ❌ 归零 |
| `INC_ARMOR_WHILE_SHAPESHIFTED` | `10%/40% increased [Armour] while [Shapeshifted]` | 死 EFF（BASE） | ❌ 归零 |
| `INC_ARMOR_WHILE_IGNITED` | `50% increased [Armour] while [Ignited]` | 死 EFF（BASE） | ❌ 归零 |
| `INC_ARMOR_WHILE_BLEEDING` | `30%/50% increased [Armour] while Bleeding` | 死 EFF（BASE） | ❌ 归零 |
| `INC_ARMOUR`（英式拼写） | **无语料命中**（空壳 StatId） | TOTAL | ✅ 可用 |

⇒ 美式/英式两个重复 StatId 中，**有真实语料的那个反而接了死 EFF** —— 典型「上下文丢失导致的错接」。

### 8.5 修复

- 6 条接线：`EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ARMOR` → `EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_MINECRAFT_ARMOR`（复用既有 EFF，**零新增注册**，与 `INC_ARMOUR` 同构）。
- 死 EFF 保留（R2 不删），但在 `StatEffectGen` 加**警告注释**说明其恒等于 0、护甲 increased 应改走 TOTAL 变体。
- 权衡说明：PoE 的 increased 语义上是加算池，而 `ADD_MULTIPLIED_TOTAL` 是连乘 → 多个护甲 increased 词缀会**连乘而非加算**（数值偏差）。但在 base=0 属性上这是**三定律 #3 规定的唯一可行解**，且「有偏差的有效」远优于「归零失效」。彻底修正需架构层引入加算池。

### 8.6 审计器新增 CHECK D

```
CHECK D: DEAD EFF — ADD_MULTIPLIED_BASE on a base==0 attribute (silently contributes 0)
  -> 1 dead EFF(s);  0 DEAD WIRING(S) using them
    [dead EFF] EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ARMOR
        attr=ARMOR(vanilla base=0.0)  tags=['ALWAYS']  (unused — latent trap)
```
CHECK D 与 CHECK B **正交**：B 查「前缀 vs op」，D 查「op 在该属性上是否会归零」。此后任何新建的零基 BASE EFF 都会被 D 抓到。

### 8.7 门禁结果（Phase 10 终态）

`compileJava`✅ → `runData --no-configuration-cache`✅ → `verify_gen.py` **13/13 PASS**
- effect=**552** / target=14 / condition=52 / entry=**944**（均不变，本次零新增注册）、0 unresolved
- `audit_opfamily`：**CHECK A=0 · CHECK B actionable=0 · CHECK D dead wirings=0**
- CHECK B zero-base exempt 33→**39**（+6，改接 TOTAL 后归入豁免）、raw prefix mismatches 45→**51**（同上）

### 8.8 结论

零基法则的两面现已**双向闭环**：
- **正面（CHECK B 零基豁免）**：阻止把可用接线改成会死的 BASE（Phase 9，33+6=39 条）。
- **反面（CHECK D 死 EFF）**：抓出已经接了 BASE 且正在失效的接线（Phase 10，修复 6 条）。

此后新增 EFF/接线前，只需先查 `attr_base_table.json` 确认属性 base，即可避免此类静默死。

---

## 九、Phase 11 — op 变体补全：从「属性猜测」改为「需求驱动 + 同族推断」

有了 `attr_base_table.json` 后，Bucket E（211 条「缺 op 变体」）才第一次可以被**安全地**切分。此前不敢动，是因为判断「某 stat 对应哪个属性」靠名字猜测，违反 R3。

### 9.1 方法：同族推断代替属性猜测（`poe2_assets/opgap_demand.py`）

1. 由 `StatGen` 已接线条目建立**真相表**（wired stat → attr + tags + op）。
2. 对每条 unwired stat，用词元重叠（Jaccard ≥ 0.5）找**最相似的已接线兄弟**，**复用兄弟的 (attr, tags)**——不猜属性。
3. 由 StatId 前缀得期望 op；若 `(attr, tags, op)` 组合尚不存在 → 记为需求。
4. 按三定律 #3 判定该变体**是否可建**（BASE on base==0 → 禁建）。

### 9.2 结果：可建 2 个、禁建 6 个

**可建（2 个变体 / 3 条需求）**：

| 缺失变体 | 属性(base) | 需求 stat | 语料原文 |
|---|---|---|---|
| `FLASK_RECOVERY_RATE` + `{HEALTH,FLASK}` + `ADD_MULTIPLIED_TOTAL` | 1.0 | `MORE_RECOVERY_FROM_FLASKS` | `30% more Recovery from [Flask\|Flasks]` |
| 同上 | 1.0 | `MORE_HEALTH_RECOVERY_FROM_FLASKS` | `50% less Life Recovery from [Flask\|Flasks]` ⚠️见 §10 |
| `REGENERATION_RATE` + `{HEALTH,REGEN}` + `ADD_MULTIPLIED_BASE` | 1.0 | `INC_HEALTH_REGENERATION_RATE` | `5~25% increased Life Regeneration rate` |

同族证据充分：`REGENERATION_RATE` 的 BASE 变体**已有 ENERGY_REGEN / MANA_REGEN 两个分支，独缺 HEALTH**——属补齐而非新设计。

**禁建（6 个变体 / 16 条需求，三定律 #3）**：`RESERVATION_EFFICIENCY`(5)、`ARMOR`+`{EVASION}`(3)、`BLOCK_CHANCE`(3)、`AILMENT_THRESHOLD`+`{STUN}`(3)、`PUPPET_MASTER_DURATION`(1)、`MAX_ENERGY_SHIELD`(1)。这批**证实并量化了 §7.7 的推论**——它们不是「等设计 EFF 变体」，而是**结构性不可解**，应从 Bucket E 移出并永久挂起。

另 142 条 unwired 无置信兄弟（相似度不足），本轮不动。

### 9.3 结论

Bucket E 的「安全可推进子集」已被穷尽到 **2 个变体**。至此，不引入引擎改造与架构裁定的接线空间**已全部用尽**。

---

## 十、⚠️ 极性缺陷（定时炸弹）：reduced/less 归一未变号

> **当前无运行时影响，但天赋树数据一旦接入即会全面爆发。**必须在接入前解决。

### 10.1 现象

`node_stat_map.json` 中：
- `MORE_HEALTH_RECOVERY_FROM_FLASKS` ← `50% **less** Life Recovery from Flasks`，`value = **+0.5**`
- `INC_HEALTH_REGENERATION_RATE` ← `20% **reduced** Life Regeneration rate`，`value = **+0.2**`

**value 恒取绝对值，方向信息丢失。**

### 10.2 根因

`extract_templates.py` L37-40 与 `resolve_node_stats.py` L38-39 有**刻意的极性归一**：
```
reduce/reduced/reduces/reducing -> increased      less -> more
remove/removes/removed/removing -> add            lose/loses/lost/losing -> gain
```
归一让 reduced/less 复用同一 StatId，但**未同步将 value 取负**。

### 10.3 影响面（`poe2_assets/polarity_audit.py` → `polarity_audit.json`）

| 类别 | 数量 | 其中已接线 |
|---|---|---|
| **极性冲突**（同一 StatId 既含 increased/more 又含 reduced/less） | **42** | **25** |
| **纯负向**（整个 StatId 都是惩罚） | **56** | **18** |

典型后果（树数据接入后）：
- `INC_ARMOR` ← `20% reduced [Armour]` → 护甲 **+20%**（应为 −20%）
- `INC_DAMAGE` ← `10% reduced Damage` → 伤害 **+10%**（应为 −10%）
- `INC_MAX_HEALTH` ← `5% reduced maximum Life` → 生命 **+5%**（应为 −5%）
- `INC_CRITICAL_HIT_CHANCE` ← `10~20% reduced Critical Hit Chance` → 暴击率**上升**

### 10.4 为何当前无实际影响

`$value` 是**占位符**，真值在「生成词缀时注入」（`AdditionType` L100/L103，来自 roll 值）。而：
- affix 侧 grep 无 reduced/less 词缀 → 注入值无反向问题；
- 天赋树节点数据**尚未接入运行时**（`poe2_tree` 在 Java 侧仅见于 `StatIds.java` 注释）→ 含 reduced 的语料还没有成为运行时数据。

### 10.5 修复注意（不可简单全量取负）

存在**双重否定**语义，例：`INC_PENALTY_TO_ACCURACY_AT_RANGE` ← `30% **reduced penalty** to Accuracy Rating at range`——「惩罚减少」实为**正向**，取负反而错。

⇒ 修复须结合语义逐条裁定，不能机械加负号。建议：在 `resolve_node_stats.py` 增加 `polarity` 字段（POS/NEG/AMBIGUOUS）输出，人工裁定后再生成带符号的 value。

### 10.6 ✅ 已修复（2026-08-29）

`resolve_node_stats.py` 现按极性取负：`extract_values(piece, negate=...)`，由 `classify(piece)` 判定
POS / NEG / AMBIGUOUS / NONE，**仅 NEG 取负**。

**双重否定防护（`DOUBLE_NEG` 模式表）**——以下语型含负向词但实为**增益**，一律判 AMBIGUOUS、不取负：

| 语型 | 实质 |
|---|---|
| `reduced / less penalty` | 惩罚变小 = 增益 |
| `reduced / less ... cost` | 消耗降低 = 增益 |
| `reduced / less ... taken` | 承受降低 = 增益 |
| `reduced loss / drain` | 损失减少 = 增益 |
| `slower expire(s)` | 衰减更慢 = 增益 |
| `reduced ... delay` | 延迟缩短（通常增益） |

**端到端验证（重跑生成后逐条核对）**：

| 语料 | 修复前 | 修复后 |
|---|---|---|
| `50% less Life Recovery from Flasks` | +0.50 | **−0.50** |
| `20% reduced Life Regeneration rate` | +0.20 | **−0.20** |
| `10% reduced Damage` | +0.10 | **−0.10** |
| `5% reduced maximum Life` | +0.05 | **−0.05** |
| `30% reduced penalty to Accuracy` | +0.30 | **+0.30**（双重否定，正确规避） |
| `30% increased Damage` | +0.30 | +0.30 |

**结果**：
- 在线反向效果风险 **25 → 0**；纯负向已接线 **18 → 0**；生成负值条目 **237** 条。
- Java 侧**零改动**（改动仅在语料分析层），`verify_gen` 885 条 / CHECK A=0 / B actionable=0 / D dead wirings=0 全部保持不变。

> **踩坑**：`CLASSIFY` 代码块必须用 **raw 三引号字符串**（`r'''...'''`）。用普通三引号时 Python 会把 `\b` 吃掉变成退格符，
> 导致 `re.search(r"\b"+词表+r"\b")` 的单词边界失效、全部判为 NONE——症状是「看起来 patch 成功但符号没变」。
> （`\s` 不是合法转义故侥幸保留，造成「双重否定检测正常、取负却失效」的迷惑现象。）

---

## 十一、代码质量重构（清理 / 工厂方法 / 分区拆分）

### 11.1 清理冗余注释（`poe2_assets/cleanup_comments.py`）

`StatGen.java` 注释密度 11%（133/1212 行），其中大量是**过程性日志**：「Phase N 做了哪批」「已由 xxx.py 校验」「matcher 曾误接…」。这类内容随轮次失效，且 84% 的接线**根本没有分节注释**（739/881），分组信息本就不完整。

- 移除 **17 处** Phase 阶段标记（保留其后的语义分组标题）。
- 移除 **22 处** 行号引用（如「复用 L578/L723 同源」）——行号随代码改动必然失效，属于会腐烂的注释。
- **保留**：语义分组标题、法则性约束（零基禁 inc、死 EFF 警告、极性 TODO）。

### 11.2 提取工厂方法（`poe2_assets/extract_factories.py`）

实测 881 条接线的形状分布与 target 分布：

| 形状 | 条数 |
|---|---|
| `TGT+EFF` | 662 |
| `COND+TGT+EFF` | 219 |

| Target | 占比 |
|---|---|
| **TGT_SELF_ONLY** | **740（84%）** |
| TGT_COND_ON_TARGET | 69（7.8%） |
| 其余 8 种 | 72（8.2%） |

⇒ 84% 的接线在重复拼写 `StatTargetIds.TGT_SELF_ONLY`。提取：
- `self(eff)` = `stat(TGT_SELF_ONLY, eff)` —— 替换 **740** 处
- `self(cond, eff)`、`onTarget(cond, eff)` = `stat(cond, TGT_COND_ON_TARGET, eff)` —— 替换 **69** 处
- 一并修复 11.1 引入的注释缩进退化。

**验证**：`effect=554 / entry=947` 与改造前**逐项一致** → 证明纯语法糖，零语义变更。

### 11.3 按语义域分区拆分（`poe2_assets/split_statgen.py`）

`StatGen.java` 原 1228 行（其中约 740 行是接线）。因 84% 接线无分节注释，**无法按注释拆分**，改为按 EFFECT 的属性域归并：

| 分区 | 条数 | 域 |
|---|---|---|
| WireDamage | 152 | DAMAGE / CRIT_* / TAKEN_DAMAGE |
| WireAilment | 135 | AILMENT_* |
| WireDefense | 90 | ARMOR / EVASION / RESISTANCES / BLOCK |
| WireResource | 92 | REGEN / ENERGY / LEECH / 资源上限 |
| WireSpeed | 57 | CAST / ATTACK / MOVEMENT SPEED |
| WireMisc | 355 | 其余 + 非 ModifierStatEffect |
| **合计** | **881** | |

结果：`StatGen.java` **1228 → 248 行**；新增 `stat/wirings/`：`WiringDsl` + 6 个 `Wire*` 分区类。

### 11.4 ⚠️ 重构踩坑（三个必须记住的教训）

1. **`\s{2,}` 会吃掉换行** —— 用它在全文压缩空白，把 1211 行的文件压成 28 行。压缩**水平**空白必须写 `[ \t]{2,}`。
2. **删「空括号」会毁掉无参构造** —— 规则 `\(\s*\)` 把 `new LinkedHashMap<>()` 变成 `new LinkedHashMap<>;`，编译失败。批量文本替换**必须限定到注释行**，并设安全门（行数 / 接线数不变才允许写入）。
3. **拆分后门禁会静默假通过（最危险）** —— 接线移出 `StatGen` 后，`verify_gen.py` 与 `audit_opfamily.py` 仍只解析 `buildEntries()` **方法体**，于是解析到 **0 条接线**，而「0 条」天然满足「无重复」「0 mismatch」，全部检查**假 PASS**。修法：
   - 两脚本改为扫描 `StatGen.java` + `stat/wirings/Wire*.java`（`_statgen_sources()`）；
   - 不再限定方法体，改为全源扫描；
   - **新增守卫项** `buildEntries non-empty`，count=0 直接 FAIL，杜绝同类假通过复发。

### 11.5 门禁（重构后）

`compileJava`✅ → `runData`✅ → `verify_gen` **ALL CHECKS PASS**（`map.put count = 881`，含新增 non-empty 守卫）
`audit_opfamily`：**CHECK A=0 · CHECK B actionable=0 · CHECK D dead wirings=0**（881 条接线全部解析）
生成数据 `effect=554 / target=14 / condition=52 / entry=947` —— 与重构前**完全一致**。

---

## 十二、接线建造者（StatBuilder）：让「维度组合」成为一等公民

> 由用户提出：Stat 由 effect / target / condition 多个部件组合而成，适合建造者模式；
> 应支持**以 effect 构建后赋予 target 与 condition**，并支持**反向构建**（后者把前者转为 builder 后继续构建）。

### 12.1 为什么比工厂方法更进一步

§11.2 的 `self()/onTarget()` 只是省掉重复参数，仍是「一次成型」。而接线真正的重复模式是
**同 effect 派生多个条件/作用域变体**（424 条条件类接线大量属此类）。建造者让这种派生成为一等表达。

### 12.2 设计（`stat/wirings/StatBuilder.java`）

**不可变**——每次叠加返回**新实例**，故基础接线可安全派生多个变体：

```java
var dmg = wiring(EFF_..._DAMAGE_DAMAGE).self();          // 起手：以 effect 构建 + 自身作用域
map.put(StatIds.INC_DAMAGE,                dmg.build());
map.put(StatIds.INC_DAMAGE_WHILE_IGNITED,  dmg.when(COND_IGNITE_STATUS).build());
map.put(StatIds.INC_DAMAGE_WHILE_CHILLED,  dmg.when(COND_CHILL_STATUS).build());
```

| 能力 | API |
|---|---|
| 以 effect 起手 | `StatBuilder.effect(eff)` / `effect(eff, target)` |
| 作用域 | `.self()`（TGT_SELF_ONLY，84%）/ `.onTarget()` / `.target(id)` |
| 条件（可链式多次） | `.when(cond)` / `.when(c1, c2, ...)` |
| 参数 | `.param(k, v)` |
| **反向构建** | `rewire(stat)` → `StatBuilder.from(stat)` |
| 收尾 | `.build()` |

**反向构建的可行性依据**：`Stat` 是 record，且 `StatEffectRead(Identifier ref)`、
`StatTargetsRead(Identifier ref)`、`ContextNodeRead(Identifier ref)` 三者都仅持有引用 id，
故可从已构建的 Stat 完整反解出 effect / target / conditions 三个维度，继续叠加后重建。

### 12.3 兼容策略（避免二次全量改动）

`WiringDsl` 的 `self(eff)` / `self(cond, eff)` / `onTarget(cond, eff)` **签名保持不变**，
内部改为经由 `StatBuilder` 实现。因此既有 **881 条接线一行不改**即获得建造者语义，
新增接线可自由选择链式写法。另暴露 `wiring(eff)` 与 `rewire(stat)` 两个建造者入口。

> 未把 881 条全量改写为链式：现有接线按 **StatId 语义**分组（分散在 6 个分区类），
> 而链式写法要求按 **effect** 分组才显出优势；重排收益不抵改动风险，故保持现状、
> 只提供能力。若日后按 effect 重新组织接线，链式写法即可自然落地。

### 12.4 门禁

`compileJava`✅ → `runData`✅ → `verify_gen` **ALL CHECKS PASS**（881 条）→
`audit_opfamily` CHECK A=0 / B actionable=0 / D dead wirings=0；
生成数据 `effect=554 / entry=947` 与建造者改造前一致。

---

## 十三、Phase 12 — 纯数据 COND 前沿：新建 4 个 COND + 1 个 EFF，再接 4 条

§9.3 曾判定「可安全推进空间已用尽」，但该结论只覆盖了 **op 变体**维度，未覆盖
**COND 原语**维度。本轮补上这一维。

### 13.1 前沿扫描（`cond_frontier_scan.py`）

| 类别 | 数量 |
|---|---|
| 已暴露 COND | 52 |
| 已注册 StatusType 但无 COND | **18** |
| 已注册 LivingStateMode 但无 COND | **3** |

按技能 §Phase4续：只要 backing 已注册，新建 COND 就是**纯数据**（常量 + 一条 `m.put`），零引擎改动。

### 13.2 需求驱动筛选（`cond_demand_scan.py`）

21 个候选里只有 **9 个**被 unwired stat 真正引用，共 31 条需求。逐条 R3 核验后
**绝大多数被排除**，原因（这是本轮最有价值的认知）：

- **机制类而非数值类**：`WARCRIES_INFLICT_CRITICAL_WEAKNESS_ON_ENEMIES`、
  `ENEMIES_ARE_INTIMIDATED_FOR_SECONDS_WHEN_YOU_IMMOBILISED_THEM`、
  `BLEEDING_YOU_INFLICT_IS_AGGRAVATED` 等——是「施加状态」的机制，走 fire 而非 ModifierStatEffect，按 R2 不接。
- **RECENTLY 事件历史而非状态在场**：`INC_EVASION_IF_YOU_VE_DODGE_ROLLED_RECENTLY`、
  `INC_ATTACK_SPEED_IF_YOU_VE_SUCCESSFULLY_PARRIED_RECENTLY`——需 `EventHistoryValue`，
  与 `LivingStateValue` 是不同机制，不能混用。
- **零基禁 inc**：`INC_STUN_THRESHOLD_WHILE_PARRYING`（AILMENT_THRESHOLD base=0）。
- **元机制**：`CANNOT_*`、`UNAFFECTED_BY_*`、`DODGE_ROLL_CANNOT_AVOID_DAMAGE`。

### 13.3 落地（4 个 COND + 1 个 EFF + 4 条接线）

**新建 COND（均为纯数据，backing 已注册且已核实行号）**：

| COND | 来源 |
|---|---|
| `COND_LOW_MANA_LIVING_STATE` | `LivingStateModes.LOW_MANA`（L17） |
| `COND_DODGE_ROLLING_LIVING_STATE` | `LivingStateModes.DODGE_ROLLING`（L27） |
| `COND_INCISION_STATUS` | `DebuffStatusTypes.INCISION`（L153） |
| `COND_PRESENCE_MARK_STATUS` | `DebuffStatusTypes.PRESENCE_MARK`（L160） |

**新建 EFF**：`EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_TAKEN_DAMAGE`
（扫描确认 TAKEN_DAMAGE 原有变体中**没有** `ADD_MULTIPLIED_TOTAL`；base=1.0 非零基，可建）。

**接线 4 条**（语料均已核验）：

| StatId | 语料 | 落点 |
|---|---|---|
| `INC_CRITICAL_HIT_CHANCE_AGAINST_MARKED_ENEMIES` | `20% increased [Critical] Hit Chance against [Mark\|Marked] Enemies` | COND_PRESENCE_MARK + TGT_COND_ON_TARGET + CRIT_CHANCE(BASE) |
| `INC_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT_AGAINST_ENEMIES_AFFECTED_BY_INCISION` | `15% increased Magnitude of Bleeding you inflict against Enemies affected by [Incision]` | COND_INCISION + TGT_COND_ON_TARGET + AILMENT_MAGNITUDE{BLEED,not TAKEN} |
| `DAMAGE_PENETRATION_LIGHTNING_RESISTANCES_IF_ON_LOW_MANA` | `Damage Penetrates 10% Lightning Resistance if on Low Mana` | COND_LOW_MANA + RESISTANCE_PENETRATION{LIGHTNING}(ADD_VALUE) |
| `TAKE_MORE_DAMAGE_FROM_HITS_WHILE_DODGE_ROLLING` | `Take 30% **less** Damage from Hits while Dodge Rolling` ⚠️极性 | COND_DODGE_ROLLING + 新建 TAKEN_DAMAGE(TOTAL) |

同步 `verify_gen.py`：`condition 52→56`、`effect 554→555`。

### 13.4 复核：剩余 827 条的真实构成

| 类别 | 数量 | 说明 |
|---|---|---|
| 无 op 前缀（元机制/非数值） | 479 | `CANNOT_*`／免疫／施加状态／机制开关，按 R2 **不接** |
| 含条件子句 | 424 | 多数需 COND；已建 56 个，余下多为 RECENTLY/per-数/复合条件 |
| `INC/REDUCED` | 309 | 大量落在零基属性（结构性不可解） |
| `TO/ADD` | 31 | 多数缺对应 EFF 变体 |
| `MORE/LESS` | 8 | 同上 |

⇒ 经 op 变体（§9）+ COND 原语（§13）**两个维度**的扫描，不引入引擎改造与架构裁定的接线空间**确已用尽**。

---

## 十四、Phase 15 — 三个新维度（条件变体 / 作用域 / 事件历史）+ 承伤侧

§13 的「已用尽」只覆盖了**新建 COND 原语**，未覆盖「把现有 COND 复用到新 stat」。补上这三个维度后
接线 **891 → 915**（完工度 51.6% → **53.6%**）。

### 14.1 条件变体（`harvest_v2.py`）— 14 条

**关键改进**：Phase 8 的教训是手写 `COND_MAP` 会漏条款写法。本轮改为**从现有接线自动学习**
「条件子句 → COND」映射（对每个已接线的条件型 stat，反解其 base = 最长的已接线前缀，记录 clause → COND）。
这样 60 个 COND 全部自动覆盖，不会因漏写而整族漏扫。首轮 12 条；新建 4 个事件型 COND 后**迭代重扫**再得 2 条。

### 14.2 作用域标签变体（`harvest_scope.py`）— 1 条

模式 `<BASE>_WITH_<SCOPE>`：若已存在 attr+op 相同、且名称带该 scope 关键词的 EFF，即可复用。
扫出 15 条候选，但 **14 条是误配**——它们后缀含 `MELEE`/`HERALD`/`COMPANION` 等作用域词，
实质却是 `WHILE_...`、`IF_YOU_VE_..._RECENTLY`、`FOR_EACH_...` **条件从句**。
只有 `INC_COOLDOWN_RECOVERY_RATE_FOR_GRENADE_SKILLS` 是纯作用域。

### 14.3 事件历史 COND（RECENTLY 族）— 4 个 COND + 9 条

`RECENTLY` 类 COND 的 backing 是 `EventHistoryValue(EventName.X, RECENTLY, 80.0)`。
`EventName` 有 **78 个常量**，但此前只暴露 6 个为 COND → 与 Phase 12 同理，其余可**纯数据新建**。
本轮按需求优先级新建 4 个：`CAUSED_CRIT`、`CAUSED_PARRY`、`consumeCharge(FRENZY)`、`consumeCharge(ENDURANCE)`，接 9 条。

**排除的否定式**：`INC_CRITICAL_DAMAGE_BONUS_IF_YOU_HAVEN_T_DEALT_A_CRITICAL_HIT_RECENTLY` 等
`HAVEN'T` 否定式无法用 `EventHistoryValue` 直接表达，不动（否则会造成反向条件）。

### 14.4 承伤侧（TAKEN）— 1 条

`INC_DAMAGE_TAKEN_WHILE_ON_LOW_HEALTH`（"5% increased Damage taken while on Low Life"）。
⚠️ **它按前缀会被误配到 base `INC_DAMAGE`（造成伤害）** —— 正是这一点使其未被 harvest_v2 扫出，
从而**避免了一次「承伤/造成伤害」错接**。已单独指定 `TAKEN_DAMAGE` 的 EFF 接线。

`INC_ARMOR_BREAK_TAKEN` 正确排除：ARMOR_BREAK 无 BASE 变体且 **base=0（零基禁 inc）**。
（其语料 value 现为 −0.25 / −0.5，极性修复生效。）

### 14.5 门禁（Phase 15 终态）

| 项 | 值 |
|---|---|
| 接线 | **914**（verify_gen）/ 915（含 StatGen 直挂） |
| effect / condition / target | **555** / **60** / 14 |
| unresolved | 0 |
| `verify_gen` | **ALL CHECKS PASS** |
| `audit_opfamily` | CHECK A=**0** · CHECK B actionable=**0** · CHECK D dead wirings=**0** |
| 完工度 | 1708 常量 / 915 已接线 = **53.6%** |

### 14.6 剩余 794 条归因（复核）

- **无 base（约 480 条）**：全新语义域，需新属性/引擎层，数据层不可能接。
- **有 base 但 suffix 是条件（约 230 条）**：多为 `RECENTLY`(55)、`WHILE_`(43)、`PER/EACH`(35)。
  可继续按需新建事件型 COND（`EventName` 仍有 70+ 未暴露），但**每种仅对应 1~4 条 stat，边际收益递减**。
- **元机制 / per-数 / 复合条件 / 零基**：按 R2/R3 不动。

⇒ 后续若继续，优先批量暴露 `EventName` 中需求较高的事件，但整体已进入边际收益递减区。

---

## 十五、Phase 16 — 事件历史 COND 批量暴露 + 迭代收敛

接线 **915 → 931**（完工度 53.6% → **54.5%**）。

### 15.1 批量暴露 EventName（新建 6 个 COND，接 15 条）

`EventName` 有 **78 个常量**，Phase 15 只暴露了 10 个。本轮按需求优先级继续暴露：

| 新建 COND | EventName | 接线 |
|---|---|---|
| `COND_COUNT_RECENTLY_CAUSED_BLOCK` | `CAUSED_BLOCK` | 2 |
| `COND_COUNT_RECENTLY_CAUSED_AILMENT` | `CAUSED_AILMENT` | 2 |
| `COND_COUNT_RECENTLY_META_SKILL_TRIGGERED` | `META_SKILL_TRIGGERED` | 1（+1 迭代） |
| `COND_COUNT_RECENTLY_FORM_CHANGED` | `FORM_CHANGED` | 2（+1 迭代） |
| `COND_COUNT_RECENTLY_GAIN_CHARGE` | `GAIN_CHARGE` | 1 |
| `COND_COUNT_RECENTLY_ATTACK_START` | `ATTACK_START` | 1 |

另用**已有** COND 复用了 5 条（`TAKEN_HIT`×3、`TAKEN_STUN`×1、`USE_ITEM`×1）。

### 15.2 迭代重扫的复利效应

每轮接线后重跑 `harvest_v2.py`，新建的 COND 会成为新的兄弟证据，持续产出候选：

| 轮次 | 真相表 | 候选 |
|---|---|---|
| 第 1 轮 | 891 | 12 |
| 第 2 轮 | 911（+4 事件 COND） | 2 |
| 第 3 轮 | 928（+6 事件 COND） | 2 |

⇒ **「接线 → 重扫 → 再接线」的迭代是有效策略**，不应扫一次就下结论。

### 15.3 谨慎排除的（避免错接）

- **否定式**：`..._IF_YOU_HAVEN_T_DEALT_A_CRITICAL_HIT_RECENTLY` 等，`EventHistoryValue` 无否定能力 → 不动。
- **复合事件**：`INC_ARMOR_IF_YOU_VE_HIT_DAMAGE_AN_ENEMY_WITH_A_MELEE_ATTACK_RECENTLY` 需「CAUSED_HIT **且** MELEE」，`EventHistoryValue` 无法可靠表达双限定 → 不动。
- **per-数机制**：`FOR_EACH_..._UP_TO`、`PER_..._CHARGE` → 机制，非条件。
- **语义不符的现成 COND**：`COND_COUNT_RECENTLY_USE_TOTEM` 是「近期**用过**」≠「当前持有」；`TAKEN_STUN` 是「被击晕」≠「击晕敌人」→ 一律不套用。

### 15.4 终态

| 项 | 值 |
|---|---|
| 接线 | **930**（verify_gen）/ 931 |
| effect / condition / target | **555** / **66** / 14 |
| entry | 994 |
| unresolved | 0 |
| `verify_gen` | **ALL CHECKS PASS** |
| `audit_opfamily` | CHECK A=**0** · CHECK B actionable=**0** · CHECK D dead wirings=**0** |
| 完工度 | 1708 常量 / 931 已接线 = **54.5%** |

### 15.5 ⚠️ 待办：最后 2 条未跑 runData

第 3 轮迭代的 2 条
（`INC_CRITICAL_HIT_CHANCE_IF_YOU_VE_TRIGGERED_A_SKILL_RECENTLY`、
`INC_STUN_BUILDUP_IF_YOU_HAVE_SHAPESHIFTED_TO_AN_ANIMAL_FORM_RECENTLY`）
已通过 `compileJava` + `audit_opfamily`(A/B/D=0) + `verify_gen`(930 条, unresolved=0)，
但 **runData 未跑完**（工具 300s 超时，而 Gradle runData 需 5~15min）。
二者为零新增注册的纯镜像接线，风险极低；**建议手动补跑一次 `./gradlew runData` 完成最终确认**。
→ **已于 2026-08-30 闭环**：补跑成功，entry 994 → **996**，condition 66 / effect 555 全部一致，确认无误。

⇒ 后续若继续，优先批量暴露 `EventName` 中需求较高的事件，但整体已进入边际收益递减区。

---

## 十六、Phase 17 — 工作原则确立 + 时间窗辅助方法 + 事件 COND 收尾

接线 **930 → 935**（完工度 54.5% → **54.7%**）。

### 16.1 用户确立的工作原则（已写入技能 §11.11）

1. **增量重检**：每轮后只做 `compileJava` → `audit_opfamily` → `verify_gen`（源码级），不做全量重检。
2. **不每轮跑 runData 数 JSON**：`src/generated/**` 由 **CODEC 工厂生成**，一般不会出问题；
   **仅在①新增/删除 EFF/COND 注册项、②交付前最终确认**时跑。
   仅新增接线（零新增注册）时只编译 + 审计即可推进下一轮。
3. **常用时间窗走辅助方法，禁止裸魔数**：`RECENTLY` = 4 秒 = 80 tick（20 tick/s），
   窗口收敛为具名常量，**便于后续由 Stat 注入**。
4. 改源码不要与 runData 并发（Gradle 会重启 processResources，前功尽弃）。
5. 长任务必须 `run_in_background=true`；`cmd` 被安全策略拦截、`setsid` 不存在、`nohup &` 会被回收。

### 16.2 时间窗辅助方法落地

`EventHistoryValue` 新增：

```java
public static final double TICKS_PER_SECOND = 20.0;
public static final double RECENTLY_WINDOW_TICKS = 4.0 * TICKS_PER_SECOND;   // = 80
public static double ticks(double seconds)              { return seconds * TICKS_PER_SECOND; }
public static EventHistoryValue recently(String name)   { ... }  // 默认 4s 窗口
public static EventHistoryValue recently(String name, double windowTicks) { ... }
public static EventHistoryValue recently(String name, String tag)           { ... }
public static EventHistoryValue count(String name)                          { ... }
```

**重构**：`StatConditionGen` 中 **16 处** `EventHistoryModes.RECENTLY, 80.0` 裸魔数全部改为
`EventHistoryValue.recently(...)`。语义完全等价（窗口仍为 80.0），故**生成 JSON 不变**。

### 16.3 本轮接线（5 条）+ 新增 2 个 COND

| 新建 COND | EventName | 接线 |
|---|---|---|
| `COND_COUNT_RECENTLY_CAUSED_HIT` | `CAUSED_HIT`（主动击中） | 2 |
| `COND_COUNT_RECENTLY_FORM_CLEARED` | `FORM_CLEARED`（形变回人形） | 1 |

另复用已有 `CAUSED_AILMENT` 接 2 条（冰缓 / 点燃敌人后）。

### 16.4 主动排除（R3 纪律）

- `INC_ELEMENTAL_AILMENTS_AILMENT_APPLICATION_...` —— `AILMENT_CHANCE` **base = 0.0**，
  `INC_` 前缀要求的 BASE 变体会是**死词条**（三定律 #3），不可建也不可接。
- `META_SKILLS_GAIN_INC_ENERGY_...` —— base 未接线，且「gain Energy」与 `META_ENERGY_MAX`
  语义未必等价，不臆造。
- RECENTLY 剩余 33 条：全为 per-数（`FOR_EACH_..._UP_TO`）、复合事件（`HIT + MELEE` 双限定）
  或需 target 重定向（友军 / 领域），均不可接。

**第 4 轮迭代重扫：0 候选** —— clause→COND 学习已穷尽。

### 16.5 当前状态

| 项 | 值 |
|---|---|
| 接线（源码侧） | **935** |
| effect / condition / target | **555** / **68** / 14 |
| 完工度 | 1708 常量 / 935 已接线 = **54.7%** |
| `compileJava` | ✅ |
| `audit_opfamily` | CHECK A=**0** · B actionable=**0** · D dead wirings=**0** |
| `verify_gen` | ⚠️ 仅 `condition count 68` 一项 FAIL（产物未重跑，仍 66 / entry 996） |

> 该 FAIL 是**预期内的账目滞后**：本轮新增 2 个 COND 注册项，`src/generated` 尚未重跑 runData。
> 按原则①补跑一次 `./gradlew runData`（须 `run_in_background=true`）即可恢复全绿，非代码缺陷。
