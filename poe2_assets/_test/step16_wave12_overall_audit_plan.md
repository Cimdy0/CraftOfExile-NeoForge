# Wave-12 作用域/条件归一 · 整体检验与推进计划（step16）

> 检验时间：2026-08-21 ｜ 方法：静态审计 `AttributeRegister` / `StatGen` / `StatIds` 三方交叉引用
> 审计脚本：`poe2_assets/_test/_audit_overall.py` `_audit_scope.py` `_audit_scope_v3.py` `_audit_coverage.py`
> 纪律基线：step13–15 已确立「标签须对齐读取端 scope，非词法；玩家上限不可补；共享基类直接读取惰性则留空正确」

## 一、整体检验结论

Wave-12 任务本质是「对描述占位属性做**作用域和条件**归一」，不是「接线覆盖」。三维度实测：

| 维度 | 指标 | 结果 | 判定 |
|---|---|---|---|
| **接线覆盖** | StatIds 在 StatGen 落点 | 665/665 = **100%** | ✅ 完美（零孤儿、零悬空） |
| **作用域标签** | 写点带 ≥1 StatTag | 283/345 主写点；余 62 零标签 | ⚠️ 大部分已做，余小批候选 |
| **条件标签** | 写点带条件标签 | 仅 **8/665** | ❌ **系统性缺失（核心差距）** |

**「差得多」的真实含义**：不是接线（已 100%），而是**条件语义归一几乎没做**——全工程仅 8 个写点带条件标签，而含 `WHILE/WHEN/IF/PAST` 条件语义的 StatIds 至少 20+ 条在写点时丢失了条件，被当作无条件全局生效。作用域维度经 step13–15 批次化处理后，剩余为小批需核对读取端的候选。

## 二、缺口量化

### 2.1 作用域缺口（对照读取端 scope 后的安全候选，11 个）

> 来源：v3 词法扫描出 18 个「裸+含作用域词」，剔除纪律要求留空/已补/不可动项后剩余。

| 家族 | 写点 StatIds | 承接属性 | 待核标签 |
|---|---|---|---|
| 单体 | `ADD_MAX_ENERGY_SHIELD` | MAX_ENERGY_SHIELD | `{energy_shield}` |
| BLOCK×2 | `ADD_BLOCK_CHANCE` `INC_BLOCK_CHANCE` | BLOCK_CHANCE | `{block}` |
| ATTACKS×3 | `CHANCE_ATTACKS_ACCURACY_RATING` `CHANCE_ATTACKS_ACCURACY_RATING_EQUAL_CRITICAL` `INCREASED_AREA_OF_EFFECT_FOR_ATTACKS` | ACCURACY / ACCURACY / AREA_OF_EFFECT | `{attack}` |
| ARMOR×2 | `CHANCE_TO_DEFEND_WITH_OF_ARMOR` `GAIN_OF_EVASION_AS_EXTRA_ARMOR` | DEFEND_ARMOUR / EVASION | `{armor}` |
| RAGE×3 | `INC_MAX_RAGE` `MORE_MAX_RAGE` `TO_MAX_RAGE` | MAX_RAGE | `{rage}` |

**执行前须逐条核对读取端 scope**——若读取端全局读取该属性（无对应 scope 过滤），则补标会使其不再被全局匹配，须改用其他承接方式。

### 2.2 条件缺口（StatIds 含条件词但写点无对应标签，20 个候选）★重点

> 检测基于「字符串字面量条件标签」。若写点已用 `StatTag.SHAPESHIFTED` 等**常量形式**表达条件，会被误判。轮 2 执行前须逐条核对真实写点代码。

| 条件类型 | 候选写点（节选） | 建议标签 |
|---|---|---|
| WHILE SHAPESHIFTED | `DAMAGE_PENETRATION_OF_ENEMY_ELEMENTAL_DAMAGE_RESISTANCES_WHILE_SHAPESHIFTED` `GAIN_PHYSICAL_THORNS_DAMAGE_EQUAL_TO_OF_MAX_HEALTH_WHILE_SHAPESHIFTED` `INCREASED_PHYSICAL_DAMAGE_WHILE_SHAPESHIFTED` | `"shapeshifted"` 或确认 `StatTag.SHAPESHIFTED` 已用 |
| WHEN 触发 | `ADD_A_CURSE_WHEN_YOU_USE_A_MANA_FLASK` `BLIND_ENEMIES_WHEN_THEY_STUN_YOU` `CHANCE_TO_RECOVER_ALL_HEALTH_WHEN_YOU_KILL_AN_ENEMY` `GAIN_RAGE_WHEN_YOU_USE_A_HEALTH_FLASK` `INCREASED_HEALTH_RECOVERY_FROM_FLASKS_USED_WHEN_ON_LOW_HEALTH` … | `"on_mana_flask"` / `"they_stun"` / `"on_kill"` / `"low_health"` |
| IF RECENTLY | `INCREASED_CRITICAL_DAMAGE_BONUS_IF_YOU_VE_CONSUMED_A_POWER_CHARGE_RECENTLY` `INCREASED_DAMAGE_IF_YOU_HAVE_CONSUMED_A_CORPSE_RECENTLY` `INCREASED_ELEMENTAL_AILMENTS_…_IF_YOU_HAVE_SHAPESHIFTED_…_RECENTLY` | `"recently_power_charge"` / `"recently_corpse"` / `"recently_shapeshifted"` |
| WHILE SURROUNDED | `INCREASED_DEFLECTION_RATING_WHILE_SURROUNDED` `METRES_TO_DODGE_ROLL_DISTANCE_WHILE_SURROUNDED` | `"surrounded"`（批次7 已补一处，复核另一处） |

### 2.3 深层缺口（规划中）

- **129+ 个仅 1 个 StatTag 的写点**：需逐条核对是否需补第二/第三作用域（如 ATTACK vs SPELL、ELEMENTAL 三系细分）。工作量最大，是真正的大工程。
- **26 个「无 StatGen 写点但有读取端引用」的属性**：须确认是否在 `AffixGen`/`EntityGen` 等其他 gen 中接线，排除真正孤立属性。
- **机制后补占位**：`COST_EFFICIENCY` `SKILL_COST_*` `REMNANT_*` `AFTERSHOCK_*` 等「数据先行」属性仍无运行时消费者，应单列「机制后补 backlog」与数据归一解耦。

### 2.4 词法疑似但纪律要求留空/已补/不可动（防止误改与重复劳动）

以下 v3 命中项**不应补标**（已 step13–15 判定）：
- `ADD_MAX_MINION` / `ADD_MAX_TOTEM` → 玩家上限语义，补 `{minion}`/`{totem}` 语义错（step15）。
- `MINIONS_HAVE_INCREASED_CAST_SPEED` / `MINIONS_HAVE_INCREASED_EVASION` / `MINIONS_HAVE_INCREASED_HEALTH_RECOVERY_RATE` → 共享基类 `CAST_SPEED`/`EVASION`/`RECOVERY_RATE` 经 `SkillCast.java:220` 等直接 `getAttributeValue` 全局读取，标签惰性，留空正确（step14）。
- `UNAFFECTED_BY_ELEMENTAL_WEAKNESS` → 已补字符串 `{"elemental_weakness"}`（step13）。

## 三、推进计划（分轮 backlog）

**铁律（沿用）**：① 标签须对齐读取端 scope，非词法；② 仅 `taken` 被 `not()` 排除，不新增 taken 标签；③ 加性补标安全（不移除读取端所需标签）；④ vocab 内标签，玩家上限类/缺失词（fissure/orb/lucky）暂不动。

| 轮次 | 范围 | 动作 | 验收门 | 风险 |
|---|---|---|---|---|
| **轮 1** | §2.1 的 11 个作用域候选 | **先核读取端 scope**，再按家族批量补 StatTag | compile / runData / 审计 | 低–中（须核对 scope 防损坏） |
| **轮 2** ★ | §2.2 的 20 个条件候选 | 逐条核对真实写点 → 补条件标签（常量或字符串） | 同上 | 中（常量 vs 字符串须确准） |
| **轮 3** | 129+ 单标签写点 | 按家族细分，补第二/第三作用域 | 同上 | 中–高（scope 对齐判断量大） |
| **轮 4** | 26 个非-StatGen 引用属性 | 确认接线归属，清理真孤立项 | 同上 | 低 |
| **轮 5** | 数据先行占位属性 | 产出「机制后补 backlog」，与归一解耦 | 文档 | — |

**优先级建议**：轮 2（条件标签）是当前最被低估、差距最大的一块，应优先系统化处理；轮 1 是低风险小批收口。轮 3 起进入逐条深度核对，是真正的大工程量。

## 四、与历史注释的校准

- `AttributeRegister.java:264`「551 剩余描述占位属性」注释 **stale**：实际 StatIds 665 条、属性 165 个、StatGen 写点 665 引用（100% 落地）。建议改为「665 StatIds 已落地，作用域/条件归一收口中（条件标签为当前重点）」。
- 「551」更接近原始 POE2 天赋树 stat 语料条目数，而非当前未落地数。

## 五、执行前架构验证：v3 词法清单为假阳性（关键修正）

对 §2.1/§2.2 清单做「补标前必做」的读取端 scope 核对后，发现方向性错误：

1. **链路**：`living.getAttributeValue(AttributeRegister.X)` 由 `LivingAttributeValue` mixin 拦截 → `StatEngine.fold(living, X, original)`；`fold` 取 `ec.currentStatContext()` 作 scope 再 `compute` —— 即 **fold 按当前 stat context 过滤 StatEntry 的 tags**。
2. **推论**：给 StatEntry 补 tag 会将其生效**限定于含该 scope 的 context**。全局属性（如「Add X% to Block Chance」）补 `{block}` 会错误限定范围，在读取 context 不含该 scope 时加成失效 —— 即损坏（与 step15 PROJECTILE_PIERCE 反例同源）。
3. **故「裸写点 + StatIds 含作用域词」≠ 应补该标签**。§2.1 的 11 个候选（`ADD_BLOCK_CHANCE→BLOCK_CHANCE`、`ADD_MAX_ENERGY_SHIELD→MAX_ENERGY_SHIELD`、`INC_MAX_RAGE→MAX_RAGE` 等）语义均为「全局提高某属性」、读取端为全局 `getAttributeValue`，补作用域标签会错误限定 → **留空正确，不可补标**。v3 清单为假阳性。
4. §2.2 的 20 个条件候选同理：条件标签限定「仅当 context 满足条件 scope 时生效」，须确认读取 context 是否产生该条件 scope；现状仅 ~8 写点带条件标签且有真实触发消费者（`DamageEvent` 等），盲目扩面会引入死标签或失效条目。
5. **方向校正**：安全归一须**反向核对** —— 枚举所有 scope 感知读取现场（`getAttributeValue`/`fold` 调用处的 `currentStatContext` scope），确认每条写点 tags 是否对齐其「应生效的 context」。此为大工程，且 step13–15 已覆盖语义本就限定的明确项（批次4×27 / 批次5×2 / 批次6×6 / 批次7×8）。
6. **建议**：原 §2.1/§2.2 清单**不可直接执行**，转为「读取 context scope 反向核对」backlog；当前不盲目补标。
