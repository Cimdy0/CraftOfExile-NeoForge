# Batch 34 收口 + 引擎前沿路线图（2026-08-31）

## Batch 34 — 距离门控 Daze 几率（纯数据·零新增注册）

### 接线
- `CHANCE_TO_INFLICT_DAZE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_FURTHER_THAN_XM`
  = `StatBuilder.effect(EFF_CAUSED_HIT_DAZE_GRANT_ON_EVENT_STATUS)`
    `.self().when(COND_FURTHER_THAN_RANGE_OF_ENEMY).param("stat_range_m", 6.0).build()`

### 机制核验（R3 逐节点）
- `EFF_CAUSED_HIT_DAZE_GRANT_ON_EVENT_STATUS` = `GrantOnEventStatEffect`：
  `onTrigger` 读 `entry.amount().getValue()` 作触发概率（已验证 `GrantOnEventStatEffect.java:211`），
  CAUSED_HIT 事件触发时对 `triggerTarget` 施加 DAZE 状态。amount=$value（源数据 25%）即触发几率 → **live 非死线**。
- `COND_FURTHER_THAN_RANGE_OF_ENEMY` = `CompareValue(DistanceToNearestEnemyValue, VarRef("stat_range_m"), GREATER_THAN)`，
  阈值 6.0 来自条目 `stat_range_m` 参数（PoE2 源 "further than 6m"，B21 已验证 live）。
- 距离中心取 `self()`（玩家→最近敌人），与 B21 全簇一致。

### 门禁
- map.put=**1306**（+1）/ effect=**675** / condition=**101** / target=14 / entry JSON=**1307**（+1）。
- compileJava PASS → runData PASS → verify_gen **ALL CHECKS PASS**（unresolved=0，无 PEND，无重复键）。
- 零新增 COND/EFF/目标：完全复用既有原语。

---

## 纯数据 vein 触顶 —— 三路独立确认
1. **B32/B33 收口笔记**已声明 CHANCE_TO / NOT_<status> / status-conditional 簇尽。
2. **WHILE_SURROUNDED 簇全接线**：accuracy/armor/damage/attack_speed/block/evasion/movement/regen/AoE/deflection 13 条已接；仅 `METRES_TO_DODGE_ROLL_DISTANCE_WHILE_SURROUNDED` 剩（需 dodge-roll 距离属性）。
3. **`wireable_now.json`「ready」(207)** 实为「EFF 已存在但 COND 缺失」族：companion / recently-stunned / recently-pinned / recently-marked / weapon-type。其所需 COND 当前不存在或为死线。

## 剩余未接 ≈ 剩余常量 - 1306 ≈ 402 条 = 全部引擎扩展前沿

### R3 安全、可立即落地（建 COND，分类器 live）
| 包 | 前置 | ROI | 风险 |
|---|---|---|---|
| **武器持有 COND 族（bow/crossbow 先）** | `ItemTypeChecker.isBow`/`isCrossBow` 已 live；`LivingStateModes.WIELDING_AXE` 为范本 | 高（解锁 with_bows/with_crossbows 簇） | 低（镜像 axe） |
| weapon-type: quarterstaff / one-handed-melee / two-handed-melee | 需补 `isQuarterstaff`/`isOneHandedMelee`/`isTwoHandedMelee` + `EquipmentGen` 类型注册 | 高 | 中（类型注册须与武器物品对齐） |
| weapon-type: spear | `EquipmentGen.SPEAR` **不存在**，且无 spear 物品定义 → 建即死线 | — | **当前不可为**（R3 排除） |

### 需新建机制（暂无 live 读路径 → 接即死线，R3 排除）
- **Companion 簇**（`*_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE`）：`TargetSelector.COMPANION` 解析为空列表，无 `coe:companion` 写源 → 死线。
- **Dodge-roll 距离 / 姿态**：`LivingStateValue("dodge_rolling")` 硬编码 `0D`（注释「模组当前无 dodge roll 翻滚动作」）→ 死线，须先建 dodge-roll 姿态与距离属性。
- **recently-stunned / recently-pinned / recently-marked**：依赖 `CAUSED_STUN`/`PIN`/`MARK_USED` 事件发射点（memory 标记疑死）→ 须先复核事件通道。
- **残迹元素类型**（`CREATE_*_REMNANTS_INSTEAD_OF_*`）：需 `RemnantMarker` 元素字段 → infusion 单通道 rework。
- **`METRES_TO_MELEE_STRIKE_RANGE` / `DODGE_ROLL_DISTANCE_WHILE_SURROUNDED`**：需新属性 + 消费端读取。

---

## 下一阶段建议（按 ROI 降序，仍守 R3）
1. **Batch 35：武器持有 COND 族** — 先建 `COND_WIELDING_BOW`（复用 `isBow`）+ `COND_WIELDING_CROSSBOW`（复用 `isCrossBow`），解锁 with_bows / with_crossbows 簇（accuracy/damage/attack_speed/ crit 等现有 EFF 直接复用）。bow/crossbow 分类器 live，**零死线风险**。
2. **Batch 36：quarterstaff / one-handed / two-handed melee COND** — 补 `ItemTypeChecker` 分类器 + `EquipmentGen` 类型（须对齐现有武器物品注册），解锁更大武器簇。
3. **Batch 37+：事件通道补全** — 复核 `CAUSED_STUN`/`PIN`/`MARK_USED` 发射点，落地 recently-X 条件族。
4. **机制级**：dodge-roll 姿态+距离、companion 实体源、remnant 元素、新属性（armor_break / mana_cost_efficiency）→ 各自独立大特性，单列批次。

> 结论：Batch 34 是纯数据 vein 的最后一击。此后每个 Batch 都是「建一个 live 引擎原语 → 解锁一族」，不再有「只接不建」的廉价胜。
