# Batch 32 — CHANCE_TO 门控族（暴击 / 取反中毒）收口

**日期**：2026-08-31 ｜ **门禁**：compileJava PASS → runData PASS → verify_gen **ALL CHECKS PASS（无 PEND）**

## 改动（+2 接线 · +1 新 COND）

| 文件 | 改动 |
|---|---|
| `StatConditionIds.java` | 新增 `COND_NOT_POISON_STATUS`（`not_poison_status`） |
| `StatConditionGen.java` | 注册 `COND_NOT_POISON_STATUS` = `CompareValue(StatusValue(POISON), ConstantValue(0.0), 1.0, 1.0, EQUAL)` |
| `WireAilment.java` | 接 2 条：`CHANCE_TO_INFLICT_BLEEDING_ON_CRITICAL_HIT_WITH_ATTACKS`、`CHANCE_TO_POISON_ON_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_NOT_POISONED` |
| `scripts/verify_gen.py` | 条件基线 100 → 101 |

## 接线明细（均 live，非死线，守 R3）

1. `CHANCE_TO_INFLICT_BLEEDING_ON_CRITICAL_HIT_WITH_ATTACKS`
   `self(COND_CRIT_HIT, EFF_ADD_VALUE_ATTRIBUTE_BLEED_COE_AILMENT_CHANCE)`
   — `COND_CRIT_HIT` 由伤害管线在命中阶段写入 `DamageContext.isCrit()`，live。
2. `CHANCE_TO_POISON_ON_HIT_DAMAGE_AGAINST_ENEMIES_THAT_ARE_NOT_POISONED`
   `onTarget(COND_NOT_POISON_STATUS, EFF_ADD_VALUE_ATTRIBUTE_COE_AILMENT_CHANCE_POISON)`
   — 镜像已接的 `CHANCE_TO_POISON_ON_HIT_DAMAGE`，仅加「目标未中毒」门控。

## 关键机制（可复用）

**取反状态 COND 范式**：`StatusValue(X)` 未持有时输出 0、持有时输出正。
「NOT 某状态」= `CompareValue(StatusValue(X.id, EMPTY), ConstantValue(0.0), 1.0, 1.0, ComparisonOperator.EQUAL)`
→ 未持有时 `EQUAL 0` 命中(1.0)。纯数据、零新节点、镜像对应正状态 COND。
后续 `…_AGAINST_ENEMIES_THAT_ARE_NOT_<STATUS>` 族可逐条加 `COND_NOT_<STATUS>` 解锁。

## 门禁指标

| 指标 | Batch 31 | Batch 32 | Δ |
|---|---|---|---|
| map.put | 1302 | **1304** | +2 |
| entry JSON | 1303 | **1305** | +2 |
| effect | 675 | 675 | — |
| condition | 100 | **101** | +1 |
| target | 14 | 14 | — |
| 覆盖率 | 76.2% | **76.35%** | +0.15pp |
| unresolved refs | 0 | 0 | — |

## 侦察结论（CHANCE_TO 簇纯数据 vein 已尽）

- 采用「真实 `public static final Identifier` 常量 + entry JSON 存在性」双判，排除早前扫描的 substring 误报（如 `CHANCE_TO_SHOCK` 实为 `INC_CHANCE_TO_SHOCK` 子串）。
- `CHANCE_TO_MAIM_ON_HIT_DAMAGE` 非真实常量（仅 `ATTACKS_HAVE_CHANCE_TO_MAIM_*` 已接）。
- 剩余未接 CHANCE_TO：多为机制类（combo / pierce / knockback / avoid-death / corpse）或缺对应 AILMENT_CHANCE EFF（daze、`further_than_xm`）→ R3 排除。

## 下一步（ROI 降序，待下个「继续」裁定）

① 残迹元素类型（RemnantMarker 加元素字段，3 条）；② Companion 概念（PresenceCountValue.Kind 补 COMPANION）；
③ Echo 真重复施放；④ Ancestral Boost（Warrior）；⑤ `TGT_ALL_ON_TARGET` 验证（解锁 ENEMIES_YOU_CURSE_* 族）。
