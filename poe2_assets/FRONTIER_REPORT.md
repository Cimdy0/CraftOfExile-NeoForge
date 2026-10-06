# 接线前沿穷尽性核查报告（Phase 21）

## 一、发现 / 门禁指标

| 指标 | 当前值 | 说明 |
|---|---|---|
| StatIds 常量总数 | 1708 | 权威源 `poe2_assets/poe2_tree_clean.json` 派生 |
| 已接线 map.put | **1059** | 完工度 **62.1%** |
| EFF 注册 | 633 | `StatEffectIds`（源码） |
| COND 注册 | 68 | `StatConditionIds` |
| target | 14 | `StatTargetIds` |
| entry 数 | 1043 | 含组合条目成员 |
| compileJava | ✅ | — |
| verify_gen (PENDING 模式) | **ALL CHECKS PASS** | runData 计数标 PENDING，不阻断 |
| audit_opfamily | A=0 / B=0 / D=0 | 零基误报=58、语义豁免=12，unresolved=0 |
| ⚠️ 生成 JSON | 滞后（effect 566 < 633） | 未跑 runData 同步，交付前须补 |

## 二、纯数据接线空间已彻底穷尽（五路交叉验证）

| # | Scout | 结果 | 结论 |
|---|---|---|---|
| 1 | `scope_auto2.py`（正确解析 Wire*.java） | PLAN=**0** | 作用域变体空间尽 |
| 2 | `exact_scope_reuse.py`（修正 harvest_widen 仅扫 StatGen 的静默 bug） | 6 reuse 候选**全含隐藏条件**（TOTEM/RECENTLY/SUPPORT_GEMS），41 new-EFF **全为条件/per-数误当作用域** | R3 全部驳回 |
| 3 | `cond_demand_scan.py` | 23 需求跨 8 COND，**均机制类**（INTIMIDATE/CRITICAL_WEAKNESS/AGGRAVATED 等施加状态）或需否定/per-数 | R3/Phase12 排除 |
| 4 | `cond_frontier_scan.py` | 16 未暴露 StatusType，同上机制类 | 排除 |
| 5 | `eff_unused_match.py`（20 已注册未引用 EFF → 精确匹配未接线 StatId） | **0 绑定** | ADD_CHAOS/COLD/FIRE_DAMAGE 已接线；TO_CAST_SPEED 等 StatId 不存在 |

## 三、剩余 ~649 未接线归因

- **元机制**（R2 不接）：CANNOT_ / IMMUNE / GAIN_ / 施加状态类。
- **需新数值型 COND 原语**：部分状态 backing 已注册但属「施加机制」而非数值判定，按 R3 排除。
- **per-数 / 复合条件机制**：FOR_EACH_…_UP_TO / HIT+MELEE 等需新 Effect 类型。
- **零基属性**（结构性不可解）：base=0 属性无法 ADD_MULTIPLIED_BASE，除非架构层改 base 或引入加算池。

## 四、下一步（需架构层裁定，非纯数据写入）

当前 62.1% 是「不引入引擎改造与架构裁定」的**真实上限**。要继续推进，仅剩三条架构级路径，请择一授权：

1. **新「数值型」COND 原语**：仅挑选 backing 已注册且为布尔状态（非施加机制）的状态，纯数据新增COND并接线（低风险，但可接项极少）。
2. **新 Effect 类型**：实现 per-数、承伤侧乘算、伤害转换 magnitude 等当前缺失的机制（需引擎层编码）。
3. **零基属性改造**：为 base=0 属性引入加算池 / 修正 base，解锁 BLOCK_CHANCE / STUN_THRESHOLD / MANA_COST_EFFICIENCY 等约 40+ 条死词条修复（架构级改动）。

> 严守 R3 铁律：本轮**未批量臆造任何接线**。若坚持在纯数据层继续，结论是无安全可接项。
