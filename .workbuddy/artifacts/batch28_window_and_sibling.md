# Batch 28：参数化时间窗口簇 + 同族补齐

> 承接 Batch 27（自描述生成型 id 簇，+60）。本批 +12，覆盖率 1270 → **1282**（1708 常量，**75.1%**）。

## 一、参数化时间窗口簇（6 条）

### 背景判据：recently 固定 4 秒，窗口 ≠ 4 秒必须走 count

`EventHistoryValue.recently(name)` 便捷构造窗口**固定**为 `RECENTLY_WINDOW_TICKS = 80`（PoE2 RECENTLY = 4 秒），且中缀不接受窗口后缀。语料出现 "in the past 8/10/20 seconds"、"in the past second" 时，必须走 `COUNT` 模式 + 显式 `ticks(N)`，再与常量 0 比较。

形状抽成工厂 `StatConditionGen.countWithinSecs(event, seconds)`（三处唯一差异是事件名与窗口长度）。

### 事件通道核验（R3：先证发射点，再接线）

| 事件 | 发射点 | 通道 | 可读性 |
|------|--------|------|--------|
| `CAUSED_CRIT` | `DamagePipeline:716` `Event.record(attacker, CAUSED_CRIT)` | A1 无标签 | ✓ |
| `USE_FLASK` | `FlaskHelper:89` `GameEvent.emit(...)` | A1 无标签 | ✓ |
| `CONSUME_GLORY` | `GloryState:99` `GameEvent.emit(..., real)` | A1 无标签 | ✓ |
| `TAKEN_DOT` | `AilmentHelper:256` `Event.record(living, TAKEN_DOT)` | A1 无标签 | ✓（且在 `MECHANICAL` 恒记录白名单） |
| `ATTACK_START` | `DamagePipeline:243` `GameEvent.emit(...)` | A1 无标签 | ✓ |

**关键辨析**：`GameEvent.emit(key, owner, other, tags, skill, value)` 内部只调 `Event.record(owner, key, value)`（无标签队列），其 `tags` 参数**仅**并入 `StatEngine.fire` 的 `fireTags`，**不写** `taggedTriggers`。因此 `USE_FLASK` 的 `{FLASK, <resource>}` 与 `CONSUME_GLORY` 的 `{GLORY}` 只作用于触发匹配，无标签 `count:<k>` 仍是可读通道。

**自动订阅**：`EventSubscription.MECHANICAL` 之外的事件由 `EventRefCollector` 在 stat 引用时自动订阅，故本批 COND 一旦被条目引用，事件即进入记录，不存在「无发射点恒 0」的静默失败。

### 新增 COND（6）

| COND | 表达式 | 窗口 | 语料依据 |
|------|--------|------|----------|
| `COND_COUNT_CAUSED_CRIT_8S` | `count:caused_crit` > 0 | 8s | "in the past 8 seconds"（vals=[0.25, 8.0]） |
| `COND_COUNT_USE_FLASK_10S` | `count:use_flask` > 0 | 10s | "in the past 10 seconds"（vals=[0.01, 10.0]） |
| `COND_COUNT_CONSUME_GLORY_20S` | `count:consume_glory` > 0 | 20s | "in the past 20 seconds"（vals=[15.0, 20.0]） |
| `COND_RECENTLY_TAKEN_DOT_1S` | `recently(taken_dot, 20tick)` | 1s | "in the past second" |
| `COND_NOT_RECENTLY_TAKEN_DOT_1S` | `not_recently(taken_dot, 20tick)` | 1s | "haven't ... in the past second" |
| `COND_NOT_RECENTLY_ATTACK_START` | `notRecently(attack_start)` | 4s | "Attacked Recently" |

### 新增 EFF（1）：DoT 限定承伤乘算

`EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_TAKEN_DAMAGE_DAMAGE_OVER_TIME`
= `ModifierStatEffect(TAKEN_DAMAGE, ADD_MULTIPLIED_TOTAL, GAIN, TagExpr.of({damage_over_time}))`

**为什么必须新建**：语料是 "Damage over Time"，若复用通用 `EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_TAKEN_DAMAGE`（`TagExpr.ALWAYS`），±50% 会施加到**全部承伤**（含普通命中），属语义越界（R3）。

**标签可达性证明**：
- `DamagePipeline:769` `readInScope(living, hitTags, TAKEN_DAMAGE)` 为承伤侧读取点；
- `hitTags` 由 `DamageEvent.buildHitTags` 组装，对 `IS_AILMENT` 伤害源并入 `damage_over_time` + `ailment` + 具体异常名；
- DoT tick 走 `StatusBehavior` → `dotDamageType` 伤害源 → 命中 `IS_AILMENT` 分支；
- 既有同口径先例：`DebuffStatusTypes.AGGRAVATED`（加重）用 `damageTaken(fraction, Set.of(DAMAGE_OVER_TIME))`。

### 接线（6 条）

| StatId | COND | EFF |
|--------|------|-----|
| `INC_DAMAGE_IF_YOU_VE_DEALT_A_CRITICAL_HIT_IN_THE_PAST_SECONDS` | `COND_COUNT_CAUSED_CRIT_8S` | `EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE` |
| `REGENERATE_OF_MAX_HEALTH_PER_SECOND_IF_YOU_VE_USED_A_HEALTH_FLASK_IN_THE_PAST_SECONDS` | `COND_COUNT_USE_FLASK_10S` | `EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_HEALTH_REGEN` |
| `MAX_RAGE_IF_YOU_VE_USED_A_SKILL_THAT_REQUIRES_GLORY_IN_THE_PAST_SECONDS` | `COND_COUNT_CONSUME_GLORY_20S` | `EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RAGE` |
| `TAKE_MORE_DAMAGE_OVER_TIME_IF_YOU_VE_STARTED_TAKING_DAMAGE_OVER_TIME_IN_THE_PAST_SECOND` | `COND_RECENTLY_TAKEN_DOT_1S` | 新建 DoT 承伤 EFF |
| `TAKE_MORE_DAMAGE_OVER_TIME_IF_YOU_HAVEN_T_STARTED_..._IN_THE_PAST_SECOND` | `COND_NOT_RECENTLY_TAKEN_DOT_1S` | 新建 DoT 承伤 EFF |
| `INC_ATTACK_SPEED_IF_YOU_HAVEN_T_ATTACKED_RECENTLY` | `COND_NOT_RECENTLY_ATTACK_START` | `EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_SPEED` |

## 二、同族补齐（6 条）

「EFF 令牌 ⊆ 常量令牌」的精确子集 scout（156 候选）人工核验后，取语义严格同构者：

| StatId | EFF | 同族依据 |
|--------|-----|----------|
| `MORE_RESERVATION_EFFICIENCY_OF_COMPANION_SKILLS` | `EFF_ADD_VALUE_ATTRIBUTE_RESERVATION_EFFICIENCY_COMPANION_RESERVATION` | `RESERVATION_EFFICIENCY` base=0.0 → 三定律 #3 禁止乘算；本文件 INC_/META/SPIRIT 变体一律 ADD_VALUE（「加成池」语义） |
| `INC_DURATION_OF_EACH_PUPPET_MASTER_STACK` | `EFF_ADD_VALUE_ATTRIBUTE_COE_PUPPET_MASTER_DURATION_PUPPET_MASTER` | `PUPPET_MASTER_DURATION` base=0.0，`StatEffectGen:41` 按「时长 = 400×(1+本值)」换算 → 本属性即**每层堆栈时长**缩放基数，ADD_VALUE 0.25 → 400→500（+25%） |
| `CHANCE_TO_DAZE_ON_HIT_DAMAGE` | `EFF_CAUSED_HIT_DAZE_GRANT_ON_EVENT_STATUS` | `chance_daze_on_hit.json` amount = `$value`，「数值即几率」，与 `CHANCE_DAZE_ON_HIT` 同义异写 |
| `CHANCE_TO_GAIN_VOLATILITY_ON_KILL` | `EFF_GRANT_ON_EVENT_KILL_RESOURCE_VOLATILITY` | `grant_volatility_on_kill.json` amount = `$value`，GrantOnEvent 按 value 掷骰（value=1 即必得） |
| `RECOVER_OF_MAX_HEALTH_ON_KILL` | `EFF_HEAL_HEALTH_KILL` | `ResourceStatEffect(HEAL)` 的 amount 本就是「最大生命百分比」，与 `RECOVER_PCT_HEALTH_ON_KILL` 同义异写 |
| `RECOVER_OF_MAX_HEALTH_ON_KILLING_A_POISONED_ENEMY` | `EFF_HEAL_HEALTH_KILL_POISON` | 标签 `{KILL, POISON}` 精确命中「击杀中毒目标」 |

## 三、排除判据（已写入源码 Javadoc 防回退）

**时间窗口簇内（WireRecent 类 Javadoc）**
- `INC_ARMOR_IF_YOU_VE_HIT_..._MELEE_ATTACK_RECENTLY`、`INC_STUN_THRESHOLD_IF_YOU_HAVEN_T_BEEN_STUNNED_RECENTLY`：落点 vanilla `ARMOR` 与 `AILMENT_THRESHOLD(stun)` 均 **base = 0**，ADD_MULTIPLIED_BASE 恒 0（三定律 #3）。
- `INC_EVASION_IF_YOU_VE_DODGE_ROLLED_RECENTLY`：`CAUSED_DODGE` 是**闪避掉一次命中**（`stageBlock` 的 `DODGE_CHANCE` 判定），与「翻滚」**不是同一动作**——翻滚是移动技能，未接入事件通道。
- `INC_AREA_OF_EFFECT_IF_YOU_HAVE_STUNNED_AN_ENEMY_RECENTLY`：只有 `TAKEN_STUN`，`EventCatalog` **无对称 CAUSED_STUN**。
- 战吼类：`USE_WARCRY` 全工程无发射点。
- `..._FOR_EACH_DIFFERENT_..._IN_THE_PAST_SECONDS`：需窗口内**去重**计数，事件队列只有 count/sum，无 distinct 聚合。
- `RAGE_GAINED` 相关：死键（`RageHelper` 只 `refreshTimer`，走 B 通道，count 恒 0）。
- `MINION_DIED` / 元素灌注 / 标记 / ES 回复启动：均无事件发射点。
- Hazard 类：引擎无 Hazard 概念与目标侧 hazard recency。

**同族簇内**
- `EFF_DAMAGE_LUCKY` 是**布尔**效果（`LuckyStatEffect`），用于「20% chance for Damage to be Lucky」会把全部伤害变幸运 → 语义越界，排除。
- `detonator` / `echo` / `ancestral_boost` 技能标签在引擎中**不存在**（仅出现于天赋节点名与 StatIds），建标签 EFF 会是死接线 → 归引擎扩展。
- `PresenceCountValue.Kind` 只有 `ANY / SUMMONED / TOTEM`，**无 COMPANION** → `INC_ATTACK_SPEED_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE` 归引擎扩展。
- `TGT_ALL_ON_TARGET`（target/condition/value 全为 `EVENT_TARGET`）已定义但**全工程零使用**，属未验证路径 → 「Enemies you Curse have -X% Resistance」等需落点于敌人的条目暂不接。

## 四、门禁

| 项 | Batch 27 收口 | Batch 28 收口 |
|----|---------------|---------------|
| `compileJava` | ✓ | ✓ |
| `runData` | ✓ | ✓ |
| `StatGen.buildEntries` `map.put` | 1270 | **1282**（+12） |
| unique keys / unknown / unresolved | 1270 / 0 / 0 | **1282 / 0 / 0** |
| effect | 662 | **663**（+1） |
| condition | 92 | **98**（+6） |
| target | 14 | 14 |
| entry | 1269 | **1283** |
| `verify_gen.py` | ALL CHECKS PASS | **ALL CHECKS PASS（无 PEND）** |

覆盖率：1708 常量 → 已接线 **1282**（**75.1%**），未接线 426。

## 五、下一步（引擎扩展前沿）

1. **技能标签机制**：`detonator` / `echo` / `ancestral_boost` / `channelling` / `triggered` 等技能标签未接入 Stat 作用域——一旦接入，可一次性消化 `*_SKILLS_HAVE_*` 十余条。
2. **去重计数原语**：`count_distinct(event, window)` 支持「for each different X in the past N seconds」若干条。
3. **`PresenceCountValue.Kind` 补 COMPANION**：解锁 Companion 簇（已知 `INC_ATTACK_SPEED_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE`、`INC_DAMAGE_WHILE_...`、`INC_ARMOR_EVASION_AND_ENERGY_SHIELD_WHILE_...` 等）。
4. **对称事件补全**：`CAUSED_STUN`、`MINION_DIED`、`DODGE_ROLL`、`ELEMENTAL_INFUSION_CONSUMED`、`MARK_USED`、`ES_RECHARGE_STARTED`。
5. **验证 `TGT_ALL_ON_TARGET`**：首个「效果落点于敌人」的通道，可解锁 `ENEMIES_YOU_CURSE_*` 一族。
6. **`RAGE_GAINED` / `USE_WARCRY` 发射点**：二者均为死键/无发射点，需接入 A 通道。
