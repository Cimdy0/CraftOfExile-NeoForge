# Step 8 — StatGen 单位约定统一为小数（方案A）

## 决策
全系统数值约定统一为**小数**（0.30 = 30%）。三条独立证据：
1. 词缀 roll 表传小数（0.04~0.30）
2. `StatGen.ref` 调用方全传小数
3. 显示层 `TextFormat` 对 `PERCENT`/`ADD_PERCENT` 自行 `×100`

## 改动
| # | 文件 | 内容 |
|---|------|------|
| 1 | `StatGen.java` | 6 个 `/100` helper（inc/entityMod/more/pctAdd/regenMod/conditionMod）共 33 处 `MathParser.parse("$value/100")` → 裸 `$value`；清理「传整数百分点」过时注释 |
| 2 | `StatGen.java` | `ADD_STUN_THRESHOLD` 通道 `AILMENT_THRESHOLD`(倍率)→`AILMENT_THRESHOLD_ADD`(平加)；SuffixMod 滚 6~352 平加点数量纲正确 |
| 3 | 9 个装备预设文件 | 归一 25 处调用方（`ItemBasicMod.fromBase`/`Addition.from`）整数百分点→小数 |
| 4 | `StatIds.java` | `inc_dexterity`/`inc_strength` 注释收窄为各自单属性（语料「increased Attributes」按三属性拆分） |

## 修复的真实缺陷
- `ADD_STUN_THRESHOLD`：误绑倍率通道 → 平加通道（×306 爆炸 → 平加点数）
- 11 处预设放大 100×：`GLOBAL_CRIT_DAMAGE`(×3)、`ADD_FIRE_RESISTANCE`、`MANA_REGENERATION_RATE_INC`、`AILMENT_MAGNITUDE_FREEZE`、`CHANCE_TO_DODGE/CHILL/FREEZE/IGNITE`、`TAKEN_DAMAGE_INC`
- 9 处转换比率放大 100×：`PHYSICAL_TAKEN_AS_*`(×3)、`EXTRA_*_DAMAGE`(×5)、`CONVERT_EXTRA_PHYS_TO_FIRE`（15.0→0.15）
- 5 处 `/100` 整数点预设：`STUN_THRESHOLD_INC`、`ELEMENTAL_AILMENT_THRESHOLD_INC`、`MINION_ATTACK_SPEED`、`PENETRATE_CHAOS`、`DAMAGE_OVER_TIME_INC`

## 验收门
- `compileJava` ✅ BUILD SUCCESSFUL
- `runData` ✅ BUILD SUCCESSFUL（含 `verifyStatArtifacts` 守卫；stat_entry 859→860，+`inc_intelligence`）
- `_plana.py`：`SPLIT=RISK=FIX=MIXED=ORPHAN=BARE-RISK=0`
- `_preset.py`：放大/缩小 100× = 0

## 架构事实（后续接线必读）
- 天赋树走 `TalentNodeGen.attr → StatGen.talentAttr`（常量 amount，专属 `talent_*` 条目），**不经 StatIds/$value**，故天赋节点不消费 `/100` 约定。
- 注入面共 6 个：`StatGen.ref`、词缀 `AdditionType`、装备预设 `ItemBasicMod.fromBase`/`Addition.from`、`StatusBehavior.sb`、`SkillFactory.readAilmentStat`。
