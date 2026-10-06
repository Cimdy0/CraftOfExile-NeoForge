# Step13 — Wave-12 块作用域/条件归一（第一批：干净家族）

> 目标：对 `AttributeRegister.java` 第 264 行 `// ───────── 批量落地（Wave-12 数据先行…）` 注释之后
> 的占位属性批量做「作用域 / 条件」归一（设计说明 D 类：条件/复合属性 → 并入基础属性 + 条件/作用域标签）。

## 执行结果（本批）

### A. 作用域收口：5 个 `UNAFFECTED_*` → 1 个基类 `UNAFFECTED` + 作用域/条件标签
| 原属性 | 原生成物 | 新落点（属性 + 标签） |
|---|---|---|
| UNAFFECTED_CHILLED_GROUND | unaffected_chilled_ground.json | `coe:unaffected` + `{chilled_ground}` |
| UNAFFECTED_SHOCKED_GROUND | unaffected_shocked_ground.json | `coe:unaffected` + `{shocked_ground}` |
| UNAFFECTED_ELEMENTAL_WEAKNESS | unaffected_elemental_weakness.json | `coe:unaffected` + `{"elemental_weakness"}`（自由串，无需登记枚举） |
| UNAFFECTED_CHILL_DURING_DODGE | unaffected_chill_during_dodge_roll.json | `coe:unaffected` + `{chill, dodge}` |
| UNAFFECTED_CHILL_LEECHING_MANA | unaffected_chill_while_mana_leech_mana.json | `coe:unaffected` + `{chill, leeching}` |

- `AttributeRegister`：删除 5 个独立属性，新增基类 `UNAFFECTED = registerPercentage("unaffected", 1.0)`（数据先行，读取端按作用域接入）。
- `StatGen`：5 处 writer 改写指向 `AttributeRegister.UNAFFECTED` 并补标签。
- 生成物文件名由 `StatIds` id 决定（不变），仅内部 `attribute` 字段指向基类 —— **非 stale，无需手动删 JSON**。

### B. 作用域收口：DEFEND_ARMOUR_CRITICAL → DEFEND_ARMOUR + {crit}
| 原属性 | 新落点 |
|---|---|
| DEFEND_ARMOUR_CRITICAL | `coe:defend_armour` + `{crit}` |

- 暴击命中招架是「护甲招架」的作用域变体，归一为基类 + `{crit}` 标签（范式同 AILMENT_MAGNITUDE 的 take/subject 作用域）。

### C. 条件标签显式化（属性保留，仅补标签——直接「条件处理」）
| 属性 | 原标签 | 新标签 |
|---|---|---|
| ELEMENTAL_AILMENT_APPLICATION_SHAPESHIFTED | （无） | `{shapeshifted}` |
| RAGE_SHAPESHIFTING_HUMAN_FORM | `{rage}` | `{rage, shapeshifted}` |

- `shapeshifted` 条件原先仅由属性名隐式编码，现已以 `StatTag.SHAPESHIFTED` 显式承载，读取端可据此作用域门控。

## 门禁指标
| 门 | 结果 |
|---|---|
| compileJava | ✅ |
| runData | ✅（Stat Entries finished；BUILD SUCCESSFUL） |
| `_plana.py` 注入面审计 | ✅ SPLIT=RISK=FIX=MIXED=ORPHAN=BARE-RISK=0 |
| 旧属性 id 生成物残留 | ✅ 0（grep 全仓 `coe:unaffected_*`/`coe:defend_armour_critical` 零命中） |

> 注：`runData` 在本环境偶发「守护进程 socket 超时」假阴性（exit 1、空输出）与 `checksums.lock` 锁残留；
> 清空 `.gradle/9.2.1/checksums/checksums.lock` 后重跑即稳定通过（已两次验证）。

## 剩余 Wave-12 作用域/条件属性 —— 待裁定（第二批候选）

> 以下属性名仍编码作用域/条件，但本批**未动**（理由见各条）。是否继续归一请你裁定。

| 属性 | 编码维度 | 提议归一 | 风险/备注 |
|---|---|---|---|
| MINIONS_CRITICAL | 主语作用域 {minion} | `CRIT_CHANCE` + `{minion}` | 低（数据先行）；effect 由 `chance`→`inc` 到 crit_chance 基类，语义更正确 |
| REMOVE_CURSE_CHANNELLING | 技能类型 {channeling} | 新基类 `REMOVE_CURSE` + `{channeling}` | 中：需新建基类；属性名原意「引导后上诅咒」语义偏「施加」 |
| REMOVE_CURSE_MANA_FLASK | 技能类型 {flask} | 新基类 `REMOVE_CURSE` + `{flask}` | 同上 |
| METRES_MELEE_STRIKE_RANGE_2 | 条件（近 8s 投射命中） | `METRES_MELEE_STRIKE_RANGE` + 条件标签 | 中：条件是动态时间窗，需新条件标签（如 `recent_projectile_8s`） |
| CANNOT_BLINDED_FULL_LIFE | 条件 {full_health} | `ImmuneStatEffect` + `{full_health}` | 中：effect 类型迁移（已有 CANNOT_BE_BLINDED→ImmuneStatEffect 范式） |
| CANNOT_CRITICALLY_PARRYING | 条件 {parry} | `ImmuneStatEffect` + `{parry}` | 中：同上 |

### 判定为「不宜折叠 / 已处理」的同类（本批不归并）
- 已带作用域标签、且**无干净基类可并**：`ATTACKS_MAIM_POISONED`{maim}、`FASTER_START_INHERENT_RAGE`{rage}、`METRES_DODGE_ROLL_DISTANCE`{dodge}、`METRES_MELEE_STRIKE_RANGE`{melee}、`AURA_BUFFS_AFFECT_ALLIES`{aura}、`MINIONS_INFLICT_WITHERED`{withered}。
- 主语作用域但无对应基类（尺寸/随从专属，强行并基类会丢失语义）：`MINIONS_GIGANTIC`、`BONUSES_AMULET_MINIONS`、`ATTACKS_CRITICAL`。

## 下一步方向（请你选）
1. **继续第二批 6 项**（建 `REMOVE_CURSE` 基类 + `ImmuneStatEffect` 迁移 + 动态条件标签）。
2. **只做安全子集**（仅 `MINIONS_CRITICAL`→`CRIT_CHANCE`+{minion}，其余挂 backlog）。
3. **停手**：本批 8 项已覆盖最干净的作用域/条件家族，剩余为语义分歧项，收尾本轮。
