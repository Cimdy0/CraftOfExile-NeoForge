# Batch 30：灌入/残迹簇 + 召唤物复活机制

> 收口门禁：map.put = **1301**（1708 常量 ≈ **76.2%**）/ effect = **675** / condition = **99** / target = 14
> 起点（Batch 29 收口）：map.put = 1296 / effect = 672 / cond = 98

本批的核心不是「新增多少条接线」，而是**修复三类静默失效** —— 这类缺陷比未接线更隐蔽：
接线存在、JSON 存在、门禁全绿，但玩家在游戏里**永远看不到效果**。

---

## 一、死事件复活（2 处引擎改动，复活 4 条既有接线）

沿用 Batch 29 的结论：`GameEvent.emit` 一次写全两个通道 —— 无标签计数队列（供 `count:<k>` 读）
与 fire 触发通道（`fireTags` = 附加标签 ∪ 事件键）。故补一行发射点即可同时激活两类落点。

### 1.1 `COLLECT_REMNANT`（残迹拾取）

`RemnantState` 的残迹机制**早已完全实现**（生产 → 衰减衰减 → 拾取 → 授予强化），
唯独 `collect()` 未发事件，令 2 条**早已接线**的条目恒不触发：

| StatId | 落点 EFF |
|---|---|
| `RECOVER_OF_MAX_HEALTH_WHEN_YOU_COLLECT_A_REMNANT` | `EFF_COLLECT_REMNANT_HEAL_HEALTH_REMNANT` |
| `RECOVER_OF_MAX_MANA_WHEN_YOU_COLLECT_A_REMNANT` | `EFF_COLLECT_REMNANT_HEAL_MANA_REMNANT` |

修复：`RemnantState.collect` 首行加 `GameEvent.emit(COLLECT_REMNANT, player, (LivingEntity) null, Set.of(StatTag.REMNANT))`。

> ⚠️ 实现坑：`emit` 有 `(…, LivingEntity other, Set)` 与 `(…, Entity other, Set)` 两个重载，
> 传字面 `null` 会触发**歧义编译错误**，必须强转 `(LivingEntity) null`。

### 1.2 `MINION_REVIVED`（召唤物复活）—— 见第三节

---

## 二、`coe:summoned` 死读值（1 行写入，复活 1 条既有接线）

`EntityFlagValue.Flag.SUMMONED` 与 `presence_count:ally.summoned` 都读
`living.getPersistentData().contains("coe:summoned")`，但**全工程无人写入该键** → 恒为假/0。

影响面（量化后小于预期，因为该标签尚未被广泛引用）：
- `StatTag.SUMMONED` 的 EFF：**0 处**
- `Flag.SUMMONED` 的 COND：**0 处**
- `presence_count:ally.summoned` 的接线：**1 处**（`WirePerScaling:343`）

修复：`SpawnAction.spawnMinion`（召唤物生成的单一权威路径）写入该键；
同时把 `SUMMON_KEY` 提为 `EntityFlagValue` 的 public 常量，消除
`EntityFlagValue` / `PresenceCountValue` 两处私有副本的漂移风险。

---

## 三、召唤物复活机制（引擎特性，复活 3 条 + 新接 2 条）

### 3.1 语义与判据

POE2 语义：**永久召唤物（Persistent Minion）生命归零后不真死，而是进入复活倒计时**，
倒计时结束满血复归并发出 `MINION_REVIVED`（owner=主人，other=召唤物）。
限时召唤物到期直接移除，不复活。

判据复用引擎既有区分，不新增概念：
- 带 `coe:summoned` 标记（本批补齐写入）**且**
- `MINION_EXPIRE == 0`（即非限时的永久召唤物）

### 3.2 实现（不侵入伤害管线）

只在事件总线两端加钩子，完全不改动 `DamagePipeline` 的结算逻辑：

| 钩子 | 优先级 | 作用 |
|---|---|---|
| `LivingDeathEvent` | **HIGHEST** | 取消死亡 + 抬回残血 + 写入 `MINION_REVIVE` 倒计时 |
| `EntityTickEvent.Pre` | NORMAL | 逐 tick 递减；归零 → 满血 + 解除无敌 + 发射 `MINION_REVIVED` |
| `EntityInvulnerabilityCheckEvent` | **LOWEST** | 复活期间置无敌，防止被二次打死 |

两个优先级选择都有硬性理由：
- **HIGHEST**：需先于其它死亡监听（尸体生成等）拿到取消权。NeoForge 默认
  `receiveCanceled = false`，故取消后 `DamageEvent.onLivingDeath`（NORMAL）不会收到，
  召唤物不会生成尸体 —— 符合「没真死」的语义。
- **LOWEST**：`DamageEvent` 的处理器**无条件**执行 `entity.setInvulnerable(!isHit)`，
  会覆盖任意时刻的无敌设置；只有 LOWEST 才能在它之后覆盖回 `true`。

踩坑记录：复活完成时必须显式 `setInvulnerable(false)`。否则无敌状态残留 ——
且因为无敌下根本进不了受击流程，`DamageEvent` 那句重设永远没机会跑，召唤物会**永久免伤**。

### 3.3 新增与接线

| 新增 | 说明 |
|---|---|
| 属性 `MINION_REVIVE_SPEED` | `registerPercentage(…, 1.0)` —— 倍率基准（**非零基**），inc 走 `ADD_MULTIPLIED_BASE` 合规 |
| 附件 `MINION_REVIVE` | 复活中剩余 tick，0 = 未复活 |
| EFF `…_MINION_REVIVE_SPEED` | 落点新属性，`TagExpr.ALWAYS` |

| 接线 | 语料 | 落点 |
|---|---|---|
| `MINIONS_REVIVING_FASTER` | Minions Revive 25% faster | 新 EFF |
| `RECOVER_OF_MAX_HEALTH_WHEN_ONE_OF_YOUR_MINIONS_IS_REVIVING` | Recover 2% of maximum Life when one of your Minions is Revived | 既有 `EFF_HEAL_HEALTH_MINION_MINION_REVIVED` |

复活的三条既有接线（`RECOVER_LIFE_MINIONS_IS`、`RECOVER_LIFE_MINIONS_IS_MINION` 及上表第 2 条）
此前因 `MINION_REVIVED` 无发射点而集体静默失效，本批同时复活。

---

## 四、灌入簇（新增事件 1 + EFF 2 + COND 1 + 接线 3）

`RemnantState.onConsume` 是灌入层数的**唯一**消耗入口（技能成本管线
`ResourceType:108` 亦经此调用），故在此发射 `CONSUME_INFUSION` 可覆盖全部消耗路径。

| 新增 | key / 落点 |
|---|---|
| 事件 `CONSUME_INFUSION` | `consume_infusion`，附 `{infusion}` 标签 |
| EFF `…_MAX_INFUSION` | `MAX_INFUSION` 平加（`TO_` 前缀 → ADD_VALUE；base=1.0，乘算会放大成荒谬值） |
| EFF `…_CONSUME_INFUSION_CHARGE_POWER_…` | `GrantOnEvent(CHARGE, power)` @ `consume_infusion` |
| COND `COND_RECENTLY_CONSUME_INFUSION` | `EventHistoryValue.recently(CONSUME_INFUSION)` |

| 接线 | 语料 |
|---|---|
| `TO_MAX_NUMBER_OF_ELEMENTAL_INFUSIONS` | +X to maximum number of Elemental Infusions |
| `GAIN_A_POWER_CHARGE_WHEN_YOU_CONSUME_AN_ELEMENTAL_INFUSION` | Gain a Power Charge when you Consume an Elemental Infusion |
| `INC_SPELL_DAMAGE_IF_YOU_HAVE_CONSUMED_AN_ELEMENTAL_INFUSION_RECENTLY` | increased Spell Damage if you've Consumed an Elemental Infusion Recently |

**窗口判据**：语料是 "Recently"，即 PoE2 的 4 秒口径，与 `recently()` 便捷构造的固定
窗口（`RECENTLY_WINDOW_TICKS = 80 tick`）完全吻合 → **不可**像 Batch 28 那批
（8/10/20 秒）那样显式传 `ticks(N)`，否则偏离语料原意。

---

## 五、排除判据（已写入源码注释，防回退）

| 条目 | 排除原因 |
|---|---|
| `TO_MAX_FIRE/COLD/LIGHTNING_INFUSIONS` | 引擎灌入**单通道不分元素**（`InfusionHelper` 只有单一 `INFUSION` 附件） |
| `CREATE_*_REMNANTS_INSTEAD_OF_*`（3 条） | `RemnantMarker` 无元素字段 |
| `CHANCE_WHEN_COLLECTING_…_OF_THE_SAME_TYPE` | 需「同类型」，同上依赖分元素 |
| `SKILLS_HAVE_CHANCE_TO_NOT_ADD_ELEMENTAL_INFUSIONS_…` | 复合概率机制，无引擎支撑 |
| `MINIONS_REVIVING_FASTER_IF_ALL_YOUR_MINIONS_ARE_COMPANIONS` | 需「全部召唤物均为 Companion」；`PresenceCountValue.Kind` 仅 ANY/SUMMONED/TOTEM，**无 COMPANION** |
| `GAIN_VOLATILITY_WHEN_AN_ALLIES_PERSISTENT_REVIVING_MINION_IS_MINION_DEATH` | 需「复活中的召唤物被击杀」事件 —— 而本设计下复活中的召唤物**不死**，该事件永不发生 |

---

## 六、门禁

```
map.put count = 1301   unique keys=1301  dup_keys=0  unknown=0  unresolved=0
[Generated JSON] effect=675  targets=14  condition=99
RESULT: ALL CHECKS PASS
```

`scripts/verify_gen.py` 基线：effect 672 → **675**，condition 98 → **99**。

---

## 七、下一步（引擎扩展前沿，ROI 降序）

1. **`COLLECT_REMNANT` 的残迹元素类型**（3 条）：需给 `RemnantMarker` 加元素字段。
2. **Companion 概念**：`PresenceCountValue.Kind` 补 COMPANION → 解锁 Companion 簇
   （含本批排除的 `…_IF_ALL_YOUR_MINIONS_ARE_COMPANIONS`）。
3. **Echo 重复施放**：把 `spell_echo` 从「平坦 areaAoe 倍率」升级为真第二次施放实例
   → `ECHO` 标签由惰性转 live。
4. **Ancestral Boost（Warrior）**：天赋节点 `ancestral_boosted` 已存在，机制未实现
   → `ANCESTRAL_BOOST` 由惰性转 live。
5. **`TGT_ALL_ON_TARGET`** 验证（target/condition/value 全 `EVENT_TARGET`，已定义零使用）
   → 解锁 `ENEMIES_YOU_CURSE_*` 一族。
6. **分元素灌入**：把 `InfusionHelper` 从单附件升级为按元素分桶 → 解锁 4 条。
