# 天赋节点 aftershock 接线审计（治本批次·节点层）

> 触发：上一轮完成 aftershock **属性层**归一（删 2 作用域焊死属性、StatGen 重建 3 条 chance 条目、AreaDeliveryExecutor 改 readInScopePublic 聚合）后，发现天赋树 JSON 全节点不带 effects —— 节点仍 inert。本轮回填节点 effects，打通「天赋→属性→引擎」完整链路。

## 根因（节点层）
6 个 aftershock 天赋节点在 gen 文件中 `effects = List.of()`（空），导致：
- 数据生成阶段节点 JSON 仅含 `icon/id/links/x/y`，**无 effects**；
- 玩家点亮节点时不授予任何 stat，引擎 `readInScopePublic` 永远读到 0 → 余震几率天赋"点了没用"。

这与用户"治标不治本"的批评同构：**属性层归一了，但数据落点（节点）没接上**，等于没改。

## 修复
给 6 个节点接 `TalentNodeGen.attrCond(attr, amount, ADD_VALUE, List.of(), tags...)`（空 conditions + 作用域标签），与 StatGen `inc(...,"slam")` 同源机制。补 4 文件 import（TalentNodeGen / StatTag / AttributeModifier / AttributeRegister）。

| 节点(id) | 属性 | 标签 | 数值 | 文件:行 |
|---|---|---|---|---|
| aftershocks (50253) | AFTERSHOCK_AREA | {slam} | 0.30 | NotableNodes01:102 |
| behemoth (5642) | AFTERSHOCK_CHANCE | {slam} | 0.05 | NotableNodes01:510 |
| rhythm_of_fire (37619) | AFTERSHOCK_CHANCE | {slam} | 0.05 | NotableNodes06:153 |
| split_the_earth (13980) | AFTERSHOCK_CHANCE | {slam,mace} | 0.10 | NotableNodes06:879 |
| mace_aftershock_chance (12276) | AFTERSHOCK_CHANCE | {slam,mace} | 0.08 | MinorNodes14:605 |
| shapeshifted_aftershock_chance (34490) | AFTERSHOCK_CHANCE | {slam,form} | 0.10 | MinorNodes18:198 |

标签语义：`AreaDeliveryExecutor` 在 slam 技能上下文组装 scope={slam}∪{mace?}∪{form?}，经 `readInScopePublic(lc, scope, AFTERSHOCK_CHANCE)` 聚合 → 子集匹配，等价原「三段属性相加」。

## 门禁
| 门 | 结果 |
|---|---|
| compileJava | ✅ |
| runData | ✅（Talent Tree provider 240ms） |
| 节点 JSON effects | ✅ 6/6 再生带 effects + 正确 attribute/tags |

## 范围边界
- `AFTERSHOCK_AREA` 节点（aftershocks）标签 {slam}：executor 用无作用域 `getAttributeValue` 读取，与既有 StatGen `SLAM_SKILLS...AFTERSHOCK_AREA` 条目（同标签）行为一致——余震本身仅在 slam 上下文触发，故加成恒在相关时生效，无回归。
- **仅接 aftershock 部分**：rhythm_of_fire / behemoth 等节点注释含多条 stat（火伤/生命/范围），本次只接 aftershock 那条；其余 stat 仍 inert，属独立 backlog（天赋节点 effects 系统性缺失，需逐节点补）。
- 未触碰 IMPLEMENT reserved-empty 核心（AREA_OF_EFFECT / DEFEND_ARMOUR）及 TAKEN_* 归一（#60/#61 待办）。

## 结论
aftershock 家族的「属性层归一 + 节点接线」已端到端打通，是可复用的治本范式：对任意 inert 天赋节点，用 `TalentNodeGen.attrCond` 接归一属性 + 作用域标签即可。
