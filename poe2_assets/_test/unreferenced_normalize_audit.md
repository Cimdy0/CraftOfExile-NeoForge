# 未引用属性 / 未实现 StatIds / 断裂通道 — 真归一化实现（执行报告）

> 用户诊断：此前 REMOVE 批次只删 StatGen 条目 → 留下 74 死属性 + 83 未实现 StatIds 的「半死」状态，其中 5 个被 AdditionType/Charm 引用却无条目 = 断裂通道。要求「对应的归一化实现」而非表面删除。

## 根因（已确认）
- 74 死属性：仅声明于 AttributeRegister，引擎/StatGen/天赋均无引用 → 未被引用也未移除。
- 83 未实现 StatIds：不在 StatGen（无生成条目）；其中 5 个被真实装备词缀系统引用（SuffixMod/PrefixMod/CharmStatusTypes）→ 词缀/护符指向无法解析的 stat = 断裂通道；其余 78 纯悬空。
- 5 个断裂 StatIds 实为被我 REMOVE 批次**误删**的接线（`StatGen.java.step6bak` 存原映射），它们本就挂在真实装备 mod / charm 上。

## 执行（彻底协同清理）
1. **恢复 5 条断裂接线**（归一化实现）：按 `.step6bak` 原映射重加 StatGen 条目；
   `CHARM_GOLDEN_RARITY` 用 `ADD_VALUE`（对齐现行 charm 约定，避开零基 `ADD_MULTIPLIED_BASE` 静默死）。
   → 救活 `FOUND_ITEM_RARITY` / `FOUND_ITEM_NUMBER` 两属性（掉落系统用 `ItemState.rarity()`，属性按「数据落地但置空」预留，不臆造消费方）。
2. **协同删除 72 死属性 + 78 悬空 StatIds**：全仓库安全扫描确认 0 外部引用后，脚本连同紧邻注释块一并删除（备份 `AttributeRegister.java.coherebak` / `StatIds.java.coherebak` 验证后已清）。

## 三道门禁
| 门 | 结果 |
|---|---|
| compileJava | ✅（无 dangling 引用） |
| runData | ✅（5 条恢复条目再生；Talent Tree 226ms） |
| 全仓安全扫描 | ✅ 0 外部引用 |

## 结果对照
| 指标 | 执行前 | 执行后 |
|---|---|---|
| 属性总数 / LIVE / DEAD | 194 / 120 / 74 | 122 / 122 / **0** |
| StatIds 总数 / 已实现 / 未实现(真) | 667 / 584 / 83 | 589 / 589 / **0** |
| 不必要的通道接线(CHANNEL) | 0 | 0 |
| 断裂通道（被系统引用却无条目） | 5 | **0** |

> 注：StatIds「文件名未匹配」仍显示 5 个（`gain_of_damage_as_extra_*` + `extra_physical`），系 `extraDamage` helper 生成文件名追加 `_damage`/`_from_X` 后缀所致误判；5 个均被 StatGen 引用、已生成变体条目 → 实际未实现 = 0。

## 结论
半死状态已彻底消除：属性层 100% 被消费、StatIds 100% 有对应生成条目、无断裂通道。这正是用户要求的「对应归一化实现」——要么恢复接线使其生效（5 个被系统引用的），要么连同属性+常量一并删除（152 个悬空），而非只删 StatGen 留半死。
