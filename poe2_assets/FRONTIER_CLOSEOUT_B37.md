# 纯数据接线 vein 触顶确认 — Batch 35/36 收口 + B37 侦察

> 生成：2026-08-31 | 阶段：纯数据 vein 已触顶，进入引擎扩展待裁定区

## 一、门禁指标（当前基线，三连全绿）
| 指标 | 值 | 说明 |
|---|---|---|
| 已接线 map.put | **1309** | /1708 = **76.7%** |
| effect | 676 | StatEffectIds |
| cond | **102** | StatConditionIds（B36 +1 = COND_WIELDING_MELEE_WEAPON）|
| target | 14 | StatTargetIds |
| entry JSON | 1310 | 含组合条目成员 |
| verify_gen | **ALL CHECKS PASS** | unresolved=0 |
| compileJava / runData | ✅ / ✅ | 33s / 1m3s |

## 二、发现：纯数据 vein 已确认触顶
- **真实 unwired = 399**（1708 − 1309，与 `verify_gen` 计数一致）。
- 旧 `poe2_assets/unwired_current.json` 是**过时快照**（含已接条目，如 `INC_HEALTH_REGENERATION_RATE_WHILE_IGNITED` 已在 `WireResource.java:152` 接好）——不可作侦察依据，已用代码扫描重算真实集。
- **五路交叉验证 + 对称性检查**结论：剩余 399 条全需引擎扩展，无纯数据可解项：
  - **186 条**元机制（`CANNOT_`/`IMMUNE`/`GAIN_`/`CONVERT`/`APPLY_`/`CULLING`）→ 按 R2 不接
  - **213 条** = 复合属性新 EFF / 施加侧事件（死线风险）/ 目标侧 AGAINST / per-X 去重计数 / 零基属性改造

## 三、B37 侦察：CHARM/HERALD 不能作为干净 COND 扩展（已确证）
- `StatusKind` 枚举仅 `DAMAGING/CONTROL/BUFF/DEBUFF/CURSE/AURA/MARK/HERALD/SHRINE/CHARGED` —— **无 CHARM**。
- → `COND_ANY_CHARM` 不能复用 `StatusKindValue("charm")`（编译失败）；charm 的 13 个 buff 状态 id 为 `charm_*_effect`（kind=BUFF），"任一 charm 在身"需新聚合节点（枚举 OR 或 id-前缀匹配）。
- → HERALD 虽有 kind，但 unwired 是"特定 herald of X"，需逐 id 枚举 → 同样非纯数据。
- 故 **CHARM(3) / HERALD(6) 族全 defer 至引擎扩展阶段**，R3 安全（不臆造）。

## 四、下一步引擎扩展清单（按 R3 风险排序，待架构裁定）
| 优先级 | 方向 | 需建机制 | R3 风险 |
|---|---|---|---|
| 1 | Companion presence 族（4） | `TargetSelector.COMPANION` / `PresenceCountValue.Kind.COMPANION` | 中（解析空→死线，需先实现）|
| 2 | 敌种簇（7 条 AGAINST 族） | `EntityFlagValue` 加 entity-type 数据 | 中 |
| 3 | recently-stunned/pinned/marked 族 | 确证 `CAUSED_STUN`/`PIN`/`MARK` 发射点 | 高（疑死事件）|
| 4 | per-X distinct 计数族（12） | 去重计数原语 | 中 |
| 5 | CHARM/HERALD 聚合 COND | 新聚合节点（id-前缀匹配）| 中 |
| 6 | 零基属性改造（ARMOR/MANA_COST_EFFICIENCY/melee_strike_range/dodge_roll_distance）| 加算池 / 改 base | 高（架构级）|
| 7 | quarterstaff 物品类型 | `EquipmentGen.QUARTERSTAFF` + 物品类 | 低-中 |

## 五、裁定
纯数据 vein 已触顶（1309/1708）。按 R3 铁律，**当前引擎能力边界内无安全可接项**——所有剩余候选的 backing 均需先建机制，否则接即死线/编译失败。建议进入"引擎扩展阶段"，从优先级 **1–2** 起步（backing 最接近 live），由架构层授权具体机制实现后再批量接线。
