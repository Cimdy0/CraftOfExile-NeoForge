# Batch 33 — 取反状态 COND 簇收口

**日期**：2026-08-31 ｜ **门禁**：compileJava PASS → runData PASS → verify_gen **ALL CHECKS PASS（无 PEND）**

## 改动（+1 接线，零新增注册）

| 文件 | 改动 |
|---|---|
| `WireAilment.java` | 接 `INC_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT_ON_TARGETS_THAT_ARE_NOT_POISONED` |

## 接线明细（live，非死线，守 R3）

`INC_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT_ON_TARGETS_THAT_ARE_NOT_POISONED`
= `onTarget(COND_NOT_POISON_STATUS, EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AILMENT_MAGNITUDE_POISON)`
- 镜像已接 `INC_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT`（self + 同 EFF），仅加 onTarget 未中毒门控。
- 复用 Batch 32 的 `COND_NOT_POISON_STATUS`（CompareValue 取反），无新增 COND / EFF。

## 门禁指标

| 指标 | B32 | B33 |
|---|---|---|
| map.put | 1304 | **1305** (+1) |
| entry JSON | 1305 | **1306** (+1) |
| effect / condition / target | 675 / 101 / 14 | 675 / 101 / 14 |
| 覆盖率 | 76.35% | **76.4%** |
| unresolved refs | 0 | 0 |

## ⚠️ 纯数据 vein 已触顶

全量扫描确认：
- `THAT_ARE_NOT_*` 仅 2 条（B32 接 1、本批接 1）。
- `WHILE_/AGAINST_/ENEMIES_YOU_CURSE_*` 未接条目几乎全为**引擎扩展前沿**：Companion / Herald / charm / quarterstaff / offering / curse-debuff 落点敌方语义。`ENEMIES_YOU_CURSE_*`(3 条) 需 curse-debuff 目标语义 + 待核实 EFF，臆造风险高 → R3 排除。

剩余未接 ~23.6% 全部属于引擎扩展项，需新 COND / Effect / 作用域标签 / ContextNode，且必须逐条验证 live（不可臆造）。

## 下一阶段：引擎扩展前沿（ROI 降序，待下个「继续」裁定）

① 残迹元素类型（`RemnantMarker` 加元素字段，3 条）；② Companion 概念（`PresenceCountValue.Kind` 补 COMPANION）；
③ Echo 真重复施放；④ Ancestral Boost（Warrior）；⑤ `TGT_ALL_ON_TARGET` 验证（解锁 `ENEMIES_YOU_CURSE_*` 族）。
