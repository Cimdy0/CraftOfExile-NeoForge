# P3 — Effect 触发清理：时间门控（主）+ 条件门控（次）

> 状态：详细设计（待用户确认后实施）
> 门禁：`compileJava` 绿即通过；不跑 datagen / runClient
> 纪律：effect 内部不得有任何触发判定；删旧轨不保留兼容层；改代码不改变运行时行为

---

## 0. 模型定调（用户纠错结论）

**不存在「触发门控」这个概念。** 只有两道门控，二者都通过「直接计算数值 → 赋予效果」完成：

1. **时间门控（主）**：决定「何时算值并施加」。
   - 周期类条目：`StatEntry.interval()` / `duration()`（由 `PeriodicDriver` 的 `beat/intervalOf` 节流）。
   - 事件类条目：**事件发生的那一刻就是时间信号**（事件不是触发器，只是一次离散的时间节拍）。
2. **条件门控（次）**：决定「施加给谁 / 本次事件是否满足条件」。
   - 经 `StatCondition.resolve(ConditionScope)` 解析出实体集（空 = 不满足，短路）。
   - 条件可读取**事件近因与事件属性**（eventName / eventEntity / hitContext / resource tag / damage amount / deflected / charge type 等）—— 事件在这里只是条件能读的输入。

**effect（`*StatEffect.onTrigger`）只剩「算值 + 施加」**，不含任何 `if (本次事件是否 X) return;` 式的**适用性触发判定**（即「这次该不该施加」）。

> **重要区分（用户反馈修正）**：被禁的是「是否施加」的判定，而**不是**「用什么参数施加」。effect 完全有权**消费事件解析出的值**来参数化授予——例如本次消耗的是哪种资源、本次获得的是哪种 charge、本次伤害量是多少。这些值由事件回调 Node 解析进共享求值作用域，条件门（WHO/IF）和 effect（WHAT）读的是**同一份解析结果**，这正是「Node 如何影响 Effect」的交叉点（详见 §2.4）。当前四类 effect 内部重判（`ManaSpentArchon.isManaSpend` / `Recoup.deflected` / `ChargeConvert.chargeTypeFromEvent` / `Culling.threshold`）要消除的，是其中「适用性判定」的那一半；其中「读事件值来参数化」的那一半应改为从共享作用域读，而非在 effect 内重新扫标签/重新推断。

统一模板 = `PeriodicDriver.drive`（PeriodicDriver.java:87-107）：时间主门（`intervalOf+beat`）→ 条件次门（`StatEngine.resolveCondition`）→ `fx.onTrigger` 算值施加。该模板当前已用于周期条目，**本设计把它推广为全 Stat 系统的唯一施加路径**。

---

## 1. 当前架构事实（带行号，均经只读调研核实）

### 1.1 事件分发入口 `StatEngine.fire`
文件：`src/main/java/com/cimdy/coe/data/codec/support/stat/engine/StatEngine.java`

- L605 `List<StatEntry> candidates = store.triggersFor(scope.tags());` — 按事件标签反向索引取候选。
- L655 `if (!entry.tags().matches(scope.tags())) continue;` — **事件标签门控（当前等价于触发门）**。
- L656-662 `if (entry.condition() != null) { resolveCondition(...) } else { conditionsHold(...) }` — 条件缝。
- L667 `effect.onTrigger(entry, scope, evalCtx, tc);` — 触发点。事件在此既作分发依据又作触发动作。

`scope.tags()` / `scope.eventName()` / `scope.hitContext()` 由 `GameEvent.emit` / `fireCaused` / `fireTaken` 构造时写入；`fire` 不把 scope 压栈，`scope.sourceEntity` 多 null。

### 1.2 `StatEntry` 字段
文件：`src/main/java/com/cimdy/coe/data/codec/support/stat/entry/StatEntry.java`

- L97-98 `ExpressionCodec.Value amount();` — 唯一数值（常量或 math 树）。
- L100-101 `List<ContextNode> conditions();` — 内联谓词（非 0 即真）。
- L103-107 `@Nullable StatCondition condition();` — 实体获取条件，null = 用 Target 锁定的基础实体。
- L116 `StatTargets targets();`
- L124 `StatEffect effect();`
- L144-147 `@Nullable StatDuration duration();` / L156-159 `@Nullable StatDuration interval();` — **仅这两个时间语义字段**，`StatDuration.Kind` ∈ {DURATION, COOLDOWN, INTERVAL}。**无** periodic/beat/recursion 字段（`beat` 是 `PeriodicDriver` 静态方法）。
- L251-255 `tags()` 转发 `effect().tags()`（GateExpr）。
- L325-330 `isTrigger()`：触发子型判据（effect 自身 isTrigger 或 FOLD 且 duration≠null）。

构造/遍历：fire 走 `triggersFor`（触发桶）；`aggregate/accumulateGroupedStat` 走 `passesEntryGates`（L316-332，`effectiveTags().matches(scope.tags())`）。触发桶与 FOLD 桶互斥。

### 1.3 统一模板 `PeriodicDriver.drive`
文件：`src/main/java/com/cimdy/coe/data/helper/engine/periodic/PeriodicDriver.java`

- L52-54 `beat(tick, interval)`；L63-70 `intervalOf(entry)`（优先 `interval()`，回落 `duration()` 的 INTERVAL）。
- L87-107 `drive`：① `intervalOf`+`beat` 节流（无节拍=每刻驱动）；② `StatEngine.resolveCondition` 仅判 conditions（**不判 tags**，L94-96 注释明确「tags 是作用域标注而非触发门控」）；③ `fx.onTrigger` 算值施加。
- L113-122 `tick` 遍历 `store.periodicIndexed()` 全部周期条目。

### 1.4 条件系统 `StatCondition`
- `StatCondition.java:31-48`：`List<LivingEntity> resolve(ConditionScope scope);` — **契约是「获取实体」而非布尔**。
- `StatConditionTypes.java:56-76` 注册 9 键：`anchor` / `radius` / `presence` / `relation` / `filter` / `and` / `or` / `not` / `chain`，均 `Codec.recursive` 自引用可嵌套。
- `ConditionScope.java:39-49` 字段：`owner / triggerTarget / eventEntity / anchor / eventName / hitContext / center / statContext / base`。`filter` 复用既有 `ContextNode` 谓词（注入 `event:`/`hit_state:` 变量）；`eventEntity` 即事件近因；`source` 槽经 `anchor:source` + `RelationCondition` 读 `StatusInstance.source`。

### 1.5 事件近因原语 `EventHistoryValue`
文件：`src/main/java/com/cimdy/coe/data/codec/support/math/context/nodes/value/base/EventHistoryValue.java`
- `window` 默认 80.0 tick（L104）。`recently` 模式 = 窗口内是否发生（1/0），可由 `FilterCondition` 复用为条件次门输入。

---

## 2. 目标架构

### 2.1 StatEntry 的两轴门控模型
为每个 entry 显式定义两轴（现有字段已够用，无需新增 entry 字段）：

- **时间轴（主）**：`TriggerKind` 语义由现有字段承载 ——
  - `PERIODIC`：`interval()`/`duration()` 非空 → 走 `PeriodicDriver.tick`。
  - `EVENT`：`interval()==null && duration()==null` 且 effect 本身 `isTrigger()` → 走 `StatEngine.fire`（事件即时间信号）。
- **条件轴（次）**：`condition()` + `conditions()`，已是 StatCondition 体系。事件属性（resource type / deflected / charge type / damage threshold）在此表达，不再进 effect。

> 不新增 `periodic/beat/recursion` 字段（与用户「不臆造」纪律一致）；时间语义完全复用 `StatDuration`。

### 2.2 引擎两入口，统一尾巴
- `PeriodicDriver.tick`（周期条目）：时间主门 → 条件次门 → `onTrigger`。✓ 已存在。
- `StatEngine.fire`（事件条目）：**事件即时间主门信号** → 条件次门（取代 `tags().matches` 的语义角色）→ `onTrigger`。
  - 粗粒度事件族（如 `resource_spend` / `taken_hit` / `gain_charge`）仍可用 `triggersFor(scope.tags())` 作**派发优化**（仅决定「哪些条目可能相关」），但**细粒度资格条件不再由 `tags().matches` 承担，而由 `condition()` 承担**。
  - 删除 L655 `entry.tags().matches(scope.tags())` 作为语义门；候选集改为「事件族索引 + 全量 EVENT 条目兜底」。
- 公共尾巴：`effect.onTrigger(entry, scope, evalCtx, tc)` 内**只算值 + 施加**，零判定。

### 2.3 effect 纯化
四个 effect 的 `onTrigger` 删除内部重判，仅保留「算值 + 施加」：
- `ManaSpentArchon`：删 `isManaSpend`（L84-87），保留 ARCHON_MANA_PROGRESS 累加行为。
- `Recoup`：删 `if (deflected && !tc.deflected()) return;`（L176），保留 ratio×base→addRecoup 行为；`deflected` 字段退化为「数据 → 条件」的映射来源（见 §3.2）。
- `ChargeConvert`：删 `chargeTypeFromEvent`（L147-154），ALLY_GRANT 改为按条目固定类型（见 §3.3）。
- `Culling`：删 `if (dealt < threshold * maxHealth) return;`（L63-72），保留 `victim.hurt(...)` 斩杀动作；阈值比较移入条件（见 §3.4）。

### 2.4 事件解析值的共享机制（Node → 条件门 + Effect，用户反馈补线）
**这是原设计缺的关键接线。** 事件回调的 `EventNode`（如 `DamageEventNode` / `EventRecencyValue`，以及本设计新增的 `ChargeGainEventNode` / `ResourceSpendEventNode`）在 fire 求值一条 entry 时，会解析出事件派生值。这些值必须进入**同一份共享求值作用域**（`StatContext scope` / `ExpressionContext` / `evalCtx`），使得：

- **条件门（次）** 读这些值做 WHO/IF 判定（如 `filter` 谓词、`presence` 盟友集筛选）；
- **effect（尾巴）** 读**同一份**值做 WHAT 参数化（如「给盟友同款 charge」、「按本次耗资源累加进度」）。

即 effect 不再内部重扫标签/重新推断事件属性，而是**消费已由 Node 解析好、共享在作用域里的值**。这就是「事件回调的 Node 如何影响 Effect」——影响路径 = Node 解析 → 写入共享作用域 → 条件门与 effect 各取所需。

**分类值（categorical）如何入作用域（待裁定 O4）**：charge type / resource type / damage type 是「类别」而非数值，ContextNode 值是 double。两条路线：
- **(a) 序数节点**：新增 `ChargeTypeValue` / `ResourceTypeValue` 等节点，把类别映射为 `ordinal()` 的 double 返回。条件门用 `== ordinal(X)` 谓词，effect 用同节点读回 ordinal→枚举。**统一进 ContextNode 体系，最契合「Node 影响 Effect」**。
- **(b) 类型化槽位**：在 `ExpressionContext` / 求值作用域上加 `eventChargeType()` / `eventResourceKey()` 等类型化访问器，配薄 ContextNode 适配层。
- 无论 (a)/(b)，关键是**单一解析点 + 共享作用域**，避免 effect 内重复推断（这正是 `chargeTypeFromEvent` / `isManaSpend` 当前的坏味道）。

---

## 3. 四类 effect 重判外置落点

### 3.1 ManaSpentArchon — 资源类型（通用，非仅 mana）
- 现状：`isManaSpend` 扫 `scope.tags()` 找 `mana` / `mana:<src>`（L84-87）；且 `scope.tags()` 已经带资源键标签（RESOURCE_SPEND 发射端并入）。**问题（用户反馈）：写死 mana 太窄**——资源类型本应是一个通用维度（mana / life / energy / rage…），不应在 effect 里硬编码某一种。
- 外置（通用化）：
  - **适用门控**：`condition = filter(resource_type == <参数>)`。资源类型作为**条目参数**而非写死常量；`<参数>` 可有缺省（缺省=任意资源）。这由 §2.4 的 `ResourceTypeValue` 节点支撑（N5）。
  - **参数化授予**：effect 仍要「读本次实际消耗值并累加进度」——这部分**保留**，但改为从共享作用域读 `event_resource` 值（已解析好的资源键 + 消耗量），不再内部扫标签推断。
  - GateExpr 标签层（entry 带 `resource_spend` + 资源键标签）仍可作派发优化，但语义门交给 `resource_type` 条件。
- 可达性：条件门部分 **NEEDS NEW（N5，`ResourceTypeValue`）**；effect 内 `isManaSpend` 删除，改为读共享作用域资源值。

### 3.2 Recoup — 偏转/格挡命中（deflected）
- 现状：`deflected` 私有 boolean 字段（L59/95-98），`if (deflected && !tc.deflected()) return;`（L176）。`tc.deflected()` = `DamagePipeline.s.blocked`（DamagePipeline.java:967）。
- **关键事实**：`DamagePipeline.java:923-928` 的 `extraTags` **仅注入 `s.tags` 与 `GateTag.CRIT`，未注入 block/deflect 标签**；且 `HitStateValue.is_block` 读的是 `HitContext.isBlock()`（实体命中恒 false），**与 `s.blocked` 不是同一来源**。故当前 gate 层表达不了 deflected。
- 外置（二选一，均归 **NEEDS NEW**）：
  - **(a) 推荐**：`DamagePipeline` 在 `s.blocked==true` 时向 `extraTags` 注入 `GateTag.DEFLECT`（或 `BLOCK`）。一次注入让整条 Deflect 族条件受益；之后 Recoup 的 `deflected` 字段 → 数据层映射为 entry 带 `deflect` 标签/条件，内部判定删除。
  - (b) 新增 `HitStateValue` 的 `is_deflected` 模式（读 `TriggerContext.deflected()` / `s.blocked` 沉淀到 HitContext），供 `FilterCondition` 复用。
- 其余（承伤>0 守卫、`fromType` 已由 GateExpr 外置、basis 选 base）属数据字段/算值行为，不在外置范围。

### 3.3 ChargeConvert — 充能类型（ALLY_GRANT，打通 Node→Effect 交叉点）
- 现状：仅 `ALLY_GRANT` 模式扫 `scope.tags()` 找 `gain_<type>`（排除 `gain_charge`），映射 `ChargeType`（L136-154）。`CONVERT`/`TO_MAX` 读已配 `from/to` 字段，无事件判定。
- **用户反馈的核心漏洞**：之前的「拆多条 entry vs 动态条件节点」二选一太浅——它回避了**事件回调 Node 解析出的 charge 类型如何回流进 effect 去参数化授予**这一交叉点。`chargeTypeFromEvent` 本质上是在 effect 内**重做了一遍 Node 已经能做的值解析**，这正是坏味道。
- **正确接线（§2.4 模型）**：
  1. **单一解析点**：新增 `ChargeGainEventNode`（EventNode 子类，对应 `gain_<type>` 事件），把本次获得的 charge 类型解析为共享作用域里的 `charge_type` 值（N5，序数或类型化槽位）。
  2. **条件门（WHO）**：`ALLY_GRANT` 的 `condition` 用 `presence(ally)` 选出盟友集；如需「仅当获得某类 charge 才转」，用 `filter(charge_type == <参数>)` 谓词（参数可有缺省=任意）。
  3. **effect（WHAT）**：`chargeTypeFromEvent` 删除；effect 改为**从共享作用域读 `charge_type` 值**（与条件门读的是同一份解析结果），给盟友授予同款 charge。effect 不再扫标签、不再推断类型——只消费已解析值。
- `CONVERT`/`TO_MAX`：仍读已配 `from/to` 字段（非事件判定），无需改动接线，仅确认其行为不变。
- 结论：以「`ChargeGainEventNode` 解析 → 共享作用域 → 条件门选盟友 + effect 读类型授予」为唯一正解；原「拆多条 entry」降级为**可选退化方案**（仅当 O4 选 (b) 类型化槽位且不想引入节点时的权宜），不再是推荐主线。

### 3.4 Culling — 伤害量级阈值（斩杀）
- 现状：`dealt = tc.takenPreBlock()`（减免后、招架/格挡前）；`if (dealt < threshold * victim.getMaxHealth()) return;`（L63-72）；`victim.hurt(...)` 为斩杀动作。
- 外置：**基本 EXISTING** —— `CompareValue`（CompareValue.java:27-48）左=`DamageValue(actual/preblock)`，右=`HealthValue(max_health)` × 系数，运算符 `>=`。`amount()` 的阈值系数移入条件 RHS。
- **风险点（需核对）**：`DamageEventNode` EVENT 源 `actual` = `Event.State.lastDamageTakenActual()`；需确认其记录口径是否为 **preBlock**（即与 `tc.takenPreBlock()` 一致）。若记的是 postBlock，则：
  - **NEEDS NEW（小补丁）**：`DamageEventNode` 增 `preblock` 模式（读 `tc.takenPreBlock()` 或 `Event.State` 增记 preBlock 快照）。
  - 否则阈值会被招架/格挡量低估，斩杀偏严。
- 斩杀动作 `victim.hurt(...)` 属 effect 施加行为，保留。

---

## 4. NEEDS NEW 清单（实施前必须裁定/补的最小代码）

| # | 位置 | 内容 | 影响 |
|---|------|------|------|
| N1 | `DamagePipeline.java:923-928` | `s.blocked==true` 时向 `extraTags` 注入 `GateTag.DEFLECT` | 解锁 Recoup.deflected 外置（§3.2-a）；惠及整条 Deflect 族 |
| N2 | `DamageEventNode` / `Event.State` | 核对 `lastDamageTakenActual` 口径；若非 preBlock，补 `preblock` 模式 | Culling 阈值准确（§3.4） |
| N3 | 条件/谓词节点 | 资源类型条件谓词（读共享作用域 `resource_type` 值） | ManaSpentArchon 通用化门控（§3.1） |
| N4 | 数据生成层 | `ALLY_GRANT` 退化方案：拆「每种 charge 类型一条 entry」（仅在 O4 选 (b) 且不愿引入节点时） | ChargeConvert 权宜（§3.3） |
| N5 | 分类事件值入作用域（**O4 裁定**） | 新增 `ChargeGainEventNode` / `ResourceTypeValue` 等节点（序数 double），或在求值作用域加 `eventChargeType()`/`eventResourceKey()` 类型化槽位 | 打通 Node→Effect 交叉点（§2.4）；ChargeConvert（§3.3）、ManaSpentArchon（§3.1）共享 |

> N3 走 N5 的 `ResourceTypeValue` 节点即可，无需单独标签扫描。N4 是退化权宜，非主线。N5 的「序数节点 vs 类型化槽位」是 O4 待裁定项。

---

## 5. 数据生成层（stat wiring）调整

- 每个事件型 entry 的「触发条件」从「effect 内部重判」改为「entry.condition()/conditions() + 可选事件族标签」：
  - ManaSpentArchon 条目：`condition = filter(resource_type == <参数>)`（`<参数>` 缺省=任意资源）；effect 从共享作用域读 `event_resource` 值累加进度（通用化，非仅 mana）。
  - Recoup `deflected:true` 条目：`tags` 增 `deflect`（依赖 N1 注入），内部 `deflected` 早退删除。
  - ChargeConvert ALLY_GRANT：挂 `ChargeGainEventNode` 解析 `charge_type` 入作用域；`condition = presence(ally)`（+ 可选 `filter(charge_type == <参数>)`）；effect 读 `charge_type` 授予同款（不再扫标签）。
  - Culling 条目：`condition = CompareValue(DamageValue(actual/preblock) >= amount*HealthValue(max_health))`，`amount` 系数移入 RHS。
- `StatEngine.fire` L655 的 `tags().matches` 删除后，事件族索引 `triggersFor` 仅作派发优化；语义门由 condition 接管 —— 数据层无需为每个 entry 配事件族标签亦可正确门控（但保留族标签可缩窄候选集、利于性能）。

---

## 6. 验收

1. `compileJava` 绿（唯一门禁）。
2. 逐项核对（grep 读点，非仅编译过）：
   - 四个 effect 的 `onTrigger` 已无 `isManaSpend` / `deflected` 早退 / `chargeTypeFromEvent` / `threshold` 早退。
   - `StatEngine.fire` L655 `tags().matches` 已移除语义门角色；condition 接管。
   - N1/N2 实施处编译通过且注入点正确（DamagePipeline extraTags、DamageEventNode 模式）。
3. 三列对账（写方?落盘?读方查表?）确认无「有读无生产者」虚接：尤其 deflected 标签（N1 写方）+ Culling preblock（N2）。

---

## 7. 风险与开放裁定

- **R1（语义偏移风险）**：删除 `tags().matches` 后若 condition 未正确覆盖事件族，可能漏触发或错触发。缓解：保留族标签作派发索引 + condition 含事件族判定（双重保险），灰度核对。
- **R2（Culling 口径）**：N2 未裁定前先核对 `lastDamageTakenActual` 口径，避免斩杀阈值偏差。
- **O1（ChargeConvert ALLY_GRANT 接线）**：已定为「`ChargeGainEventNode` 解析 → 共享作用域 → 条件门选盟友 + effect 读 `charge_type` 授予」。仅「是否额外拆多条 entry（N4 退化方案）」待你确认是否要（推荐不拆，走节点主线）。
- **O2（ManaSpentArchon 资源类型）**：已从「仅 mana」泛化为通用 `resource_type` 参数（缺省=任意资源）。仅实现形态（N5 节点 vs 标签）随 O4 走。
- **O4（分类事件值入作用域机制，关键）**：charge type / resource type / damage type 是类别值，ContextNode 值是 double。① **序数节点**（新增 `ChargeGainEventNode`/`ResourceTypeValue` 返回 `ordinal()` double，条件门 `== ordinal(X)`、effect 读回映射枚举）——统一进 ContextNode，最契合「Node 影响 Effect」；② **类型化槽位**（求值作用域加 `eventChargeType()` 等访问器 + 薄适配层）。**请裁定走哪条**；本设计主推 ①。
- **O3（IGNITE_HISTORY 窄锁）**：消息2 批判的锁死点燃问题独立于本设计，通用化 `IgnitedBySourceValue` 删 `IGNITE_HISTORY` 属另一任务，本设计不触及。

---

## 8. 与既有 Node 梳理/Effect 窄锁清理的关系

- Node Step1-3（EntityNode→StatusNode→AilmentNode + EventNode 抽出 `DamageEventNode`/`EventRecencyValue`、源实体解析收口、消除状态枚举硬编码）已 compileJava 绿，本设计复用其 `DamageEventNode` 作为 Culling 条件读点。
- Effect 窄锁清理（ShockEcho/SyntheticChill 参数化）已完成；AilmentHelper 因机制各异保留（正确）。本 P3 是「触发清理」层，与「窄锁清理」正交。
