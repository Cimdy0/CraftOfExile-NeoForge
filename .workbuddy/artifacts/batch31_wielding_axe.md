# Batch 31 收口：武器持有条件原语 `wielding_axe`

## 门禁
- `map.put = 1302`（1708 常量 ≈ 76.2%，+1）/ `effect = 675` / `condition = 100`（+1）→ `target = 14` / `entry JSON = 1303`
- verify **ALL CHECKS PASS（已跑 runData，无 PEND、无 unresolved）**

## 改动
新增 1 个 living-state 条件原语，解锁「持斧时」门控族：

| 文件 | 改动 |
|---|---|
| `LivingStateModes.java` | 新增常量 `WIELDING_AXE = "wielding_axe"` |
| `LivingStateValue.java` | 新增 `case "wielding_axe"` → `wieldingAxe(living)`，判定复用 `ItemTypeChecker.isAxe(mainHand)` |
| `StatConditionIds.java` | 新增 `COND_WIELDING_AXE = id("wielding_axe")` |
| `StatConditionGen.java` | Phase 6 注册 `COND_WIELDING_AXE` → `LivingStateValue(WIELDING_AXE, 80.0)` |
| `WireResource.java` | 接线 `TO_MAX_RAGE_WHILE_WIELDING_AN_AXE` = `self(COND_WIELDING_AXE, EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RAGE)` |
| `scripts/verify_gen.py` | 条件基线 99 → 100 |

复用 `TO_MAX_RAGE` 同族 **ADD_VALUE** op（`TO_` 前缀平加，base=50 非零基；`TO_MAX_RAGE_WHILE_SHAPESHIFTED` 已用同 EFF 印证）。

## 侦察结论（避开的反模式）
- **dual-wield 5 条早已接好**（WireDamage/WireMisc/WireSpeed），勿重复接线。
- 距离类真实未接是 `against enemies further than Xm` / `no_enemies_within_xm` —— 需**对目标**距离条件 `DistanceToTargetValue`，与自身距离原语 `COND_FURTHER_THAN_RANGE_OF_ENEMY` 不可混用。
- Companion 簇、`WHILE_AFFECTED_BY_HERALD_OF_X` 簇：底层实体/状态支撑缺失（Companion 实体未注册、无 herald 状态条件）→ 接了即死线，排除（守 R3）。
- "recently did X" 22 条：需大量具体 `recently()` 条件，现存 `COND_COUNT_RECENTLY_*` 不覆盖 → 留待批量引擎扩展。

## 下一步（ROI 降序）
① 残迹元素类型（RemnantMarker 加元素字段，3 条）；② Companion 概念（PresenceCountValue.Kind 补 COMPANION）；
③ Echo 重复施放（spell_echo 升级为真二次施放，ECHO 惰性转 live）；④ Ancestral Boost（Warrior）；
⑤ `TGT_ALL_ON_TARGET` 验证（解锁 ENEMIES_YOU_CURSE_* 族）；⑥ 分元素灌入（InfusionHelper 按元素分桶，4 条）；
⑦ `wielding_<weapon>` 家族（sword/mace/bow 谓词已存在，待语料出现对应 StatId 再补）。
