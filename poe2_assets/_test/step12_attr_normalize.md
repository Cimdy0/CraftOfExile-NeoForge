# Step12 属性归一收尾：零引用孤儿清理 + "大量待归一"构成审计

## 本次执行（删除类归一，安全项清零）

删除 2 个**孤儿注册属性**（全工程零引用、无对应 StatIds、StatGen 写点已在前序 recoup 归一中移除，功能已由 `recoup` 效果类型 JSON 承接）：

| 属性 | 原注册 | 实际状态 |
|---|---|---|
| `MINIONS_RECOUP_DAMAGE_TAKEN` | `minions_recoup_damage_taken` | 零引用；`minions_recoup_of_damage_taken_as_health.json` 走 `recoup` 类型 `target:minion`，不引用此属性 |
| `RECOUP_DAMAGE_TAKEN_TOTEMS` | `recoup_damage_taken_totems` | 零引用；`recoup_of_damage_taken_by_your_totems_as_health.json` 走 `recoup` 类型 `target:totem`，不引用此属性 |

属性注册总数：**209 → 207**。

## 门禁指标

| 门 | 结果 |
|---|---|
| compileJava | ✅（仅过时 API 警告） |
| runData | ✅ stat_entry=851（不变，孤儿本无 JSON 条目）；recoup JSON 完好；02:37 mtime 证明确已重生成 |
| `_plana.py` 注入面 | ✅ RISK/MIXED/FIX/BARE-RISK 全零 |
| 生成物旧属性引用 | ✅ 零残留 |
| **zero-reference (dead)** | ✅ **0**（删除类归一已无安全目标） |

## "大量需要归一"的真实构成（审计结论）

重跑全量引用面审计（209 → 207 属性），分类如下：

| 类别 | 数量 | 判定 |
|---|---|---|
| 运行时读取（CONSUMED/runtime） | 88 | 有消费端，**不可删** |
| 数据先行占位（GEN-ONLY） | ~80 | StatGen 写点存在、消费端未接入；按项目惯例"条目先建、消费端后补"**故意保留** |
| 装备侧引用（equip） | 17 | 词缀 attribute 通道引用，**不可删** |
| 零引用（dead） | **0** | 已全部清理（step11 的 5 个 + 本次 2 个 = 共 7 个孤儿） |

**结论**：用户观察到的"大量需要归一的属性" = 这 ~80 个 GEN-ONLY 数据先行占位。它们**不是错误**，而是 POE2 语料批量落地的脚手架——每个对应一条天赋/词缀描述，待对应机制读取端（消费者）实现后按 `{作用域}` 标签读取。在消费者就位前删除/合并属**推测性改动**，违背"不会出问题"约束，且会丢失数据先行工作。

## 仍可作"归一"的候选（需设计决策，非安全删除）

若要对 GEN-ONLY 家族做 step11 式作用域收口，以下家族语义相近、可用「单基类属性 + 标签作用域」替多个独立属性（但**当前均无消费者**，收口后基类仍为数据先行，需同步规划读取端）：

| 家族 | 成员 | 候选基类 | 风险 |
|---|---|---|---|
| `UNAFFECTED_*` | CHILL_DURING_DODGE / CHILL_LEECHING_MANA / CHILLED_GROUND / ELEMENTAL_WEAKNESS / SHOCKED_GROUND | `UNAFFECTED + {ground/state 标签}` | 中（需规划 5 个地面/状态读取端） |
| `METRES_*` | DODGE_ROLL_DISTANCE / MELEE_STRIKE_RANGE / MELEE_STRIKE_RANGE_2 | `DISTANCE + {场景标签}` | 中（`_2` 疑似重复，需核对语料） |
| `AFTERSHOCK_CHANCE_*` | (base) / MACE_SLAM / SHAPESHIFT_SLAM | `AFTERSHOCK_CHANCE + {武器标签}` | 低（已有基类，仅 2 个限定变体） |
| `LEVEL_ALL_*` | CHAOS / COLD | `SKILL_LEVEL + {伤害类型标签}` | 中 |
| `REMOVE_CURSE_*` | CHANNELLING / MANA_FLASK | `REMOVE_CURSE + {触发标签}` | 低 |

## 下一步建议（待用户裁定）

1. **停手**：接受"大量"为有意的数据先行占位，删除类归一已清零（dead=0），本轮结束。
2. **收口单一低风险家族**：仅对 `AFTERSHOCK_CHANCE_*`（已有基类，仅 2 变体）做作用域收口，作为范式验证。
3. **收口全部家族**：对上述 5 个家族统一做基类+作用域重构（需同步规划各消费者读取端标签）。
4. **仅修消费者侧的未来工作**：把"新语料一律基类+作用域、禁建 `*_后缀` 属性"作为惯例写入 `AttributeRegister` 头部注释，迁移挂入 backlog，不再批量改 GEN-ONLY。
