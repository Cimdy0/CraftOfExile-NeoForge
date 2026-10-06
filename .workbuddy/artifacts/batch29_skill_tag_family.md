# Batch 29 报告：技能标签族 + 死事件复活

承接 Batch 28 收口（map.put = 1282 / 74.4%）。本批另辟两条新路径：
① **技能标签族**（`Skill.tags` 数据驱动 + `Skill.getStatTags` 发射）；
② **死事件复活**（`EventName` 已声明但全工程无发射点，导致既有接线静默失效）。

---

## 1. 关键机制发现

### 1.1 技能标签是数据驱动的，无需引擎改动即可扩展

`Skill.getStatTags(SkillContext, Identifier)` 的标签来源有五路：

| 来源 | 说明 |
|---|---|
| 技能 id 的 path | 自动作为标签（如 `fireball`） |
| `getLabels(context)` | 攻击/法术 + **技能自带 `tags` 字段**（`withTags(...)` 追加） |
| `equipment` | 武器装备需求字符串原样 |
| `cost[].type()` | 消耗资源名原样 |
| `input` 类型 | `Hold` / `Continuous` → `channeling` |

⇒ `withTags(String...)` 是既有惯例（`storm` / `plant` / `command` / `corpse` / `mark` / `warcry` / `slam` 均已用），
新增技能标签**只需数据层一行**，不必动引擎。

### 1.2 `triggered` 可用 `TRIGGER_BIND` 精确判定

`SkillTriggerHelper.trigger(...)` 用虚拟绑定槽 `TRIGGER_BIND = Integer.MAX_VALUE`
调 `SkillCaster.cast(...)`，故被触发的技能其 `SkillContext.getBind()` 恒等于该值。
在 `Skill.getStatTags` 里加一行即可区分「被触发的施放」与「主动施放」——
同一技能两种施放方式不再共享加成。

### 1.3 `_TAKEN` 命名实为「施加侧」（易误读，务必注意）

`EFF_..._AILMENT_DURATION_<TAG>_TAKEN` 的 `TagExpr` 是
`Set.of(StatTag.<TAG>, StatTag.not(StatTag.TAKEN))`。
名字里的 `TAKEN` 表示 **not(TAKEN)**，即施加方而非承伤方。
本批新增的 TOTEM / MARK 两个持续时间变体沿用此口径。

### 1.4 死事件扫描（`poe2_assets/dead_event_scan.py`，新建工具）

对 78 个 `EventName` 常量做全工程发射点扫描，初判 13 个「无发射点」。逐个人工复核后：

| 事件名 | 判定 | 依据 |
|---|---|---|
| `CAUSED_HIT` / `TAKEN_HIT` / `CAUSED_AILMENT` | **误报·live** | `GameEvent.recordTaggedHit` / `GameEvent.caused` 写入（扫描器未覆盖这些方法名） |
| `CAUSED_DODGE` / `TAKEN_DODGE` / `CAUSED_BLOCK` / `TAKEN_BLOCK` | **误报·live** | `DamagePipeline:515/524 firePair` |
| `CAUSED_PERFECT_PARRY` / `TAKEN_PARRY` / `TAKEN_PERFECT_PARRY` | **误报·live** | `DamagePipeline:486-487 firePair` |
| `COLLECT_REMNANT` | **真死** | 残迹机制未实现（与 `StatTag.REMNANT` 惰性同源），2 个 EFF 引用 |
| `MINION_REVIVED` | **真死** | 引擎**无持久召唤物复活机制**（全仓无 revive 实现），1 个 EFF + 2 条已接线静默失效 |
| `USE_WARCRY` | **已在本批修复** | 见 §2.3 |

---

## 2. 落地内容

### 2.1 新增作用域标签 4 个（`StatTag.java`）

| 标签 | 产出点 | 状态 |
|---|---|---|
| `DETONATOR = "detonator"` | `blue/detonate_dead` 加 `.withTags(StatTag.DETONATOR)`（POE2 中 Detonate Dead 确带 Detonator 标签） | **live** |
| `TRIGGERED = "triggered"` | `Skill.getStatTags`：`getBind() == SkillTriggerHelper.TRIGGER_BIND` | **live**（引擎改动 2 行） |
| `ANCESTRAL_BOOST = "ancestral_boost"` | 无（Warrior「重击后先祖强化」机制未实现；天赋节点 `ancestral_boosted_area_and_damage` 已存在） | **惰性** |
| `ECHO = "echo"` | 无（`spell_echo` / `zarokhs_refrain` 在数据层被建模为平坦 `SupportGemFactory.areaAoe` 倍率，无第二次施放实例） | **惰性** |

> 惰性标签接线的判据沿用 `StatTag.REMNANT` 既有口径：**语义正确优先于暂时惰性**。
> `ANCESTRAL_BOOST` 绝不可退而求其次接到 `MELEE`（会把加成放大到全部近战攻击，语义越界）。

### 2.2 新增 EFF 9 个（663 → 672）

| EFF | 属性 | op | 标签集 |
|---|---|---|---|
| `..._AILMENT_DURATION_TOTEM_TAKEN` | AILMENT_DURATION | ADD_MULTIPLIED_BASE | `{TOTEM, !TAKEN}` |
| `..._AILMENT_DURATION_MARK_TAKEN` | AILMENT_DURATION | ADD_MULTIPLIED_BASE | `{MARK, !TAKEN}` |
| `..._DAMAGE_DAMAGE_ANCESTRAL_BOOST_ATTACK` | DAMAGE | ADD_MULTIPLIED_BASE | `{DAMAGE, ANCESTRAL_BOOST, ATTACK}` |
| `..._DAMAGE_DAMAGE_DETONATOR` | DAMAGE | ADD_MULTIPLIED_BASE | `{DAMAGE, DETONATOR}` |
| `..._DAMAGE_DAMAGE_TRIGGERED_SPELL` | DAMAGE | ADD_MULTIPLIED_BASE | `{DAMAGE, TRIGGERED, SPELL}` |
| `..._EFFECT_AREA_ANCESTRAL_BOOST_ATTACK` | EFFECT_AREA | ADD_MULTIPLIED_BASE | `{ANCESTRAL_BOOST, ATTACK}` |
| `..._EFFECT_AREA_DETONATOR` | EFFECT_AREA | ADD_MULTIPLIED_BASE | `{DETONATOR}` |
| `..._EFFECT_AREA_ECHO_SPELL` | EFFECT_AREA | ADD_MULTIPLIED_BASE | `{ECHO, SPELL}` |
| `..._USE_WARCRY_GRANT_ON_EVENT_IGNITE_STATUS` | GrantOnEvent(IGNITE) | — | `{USE_WARCRY}` |

### 2.3 引擎改动 2 处

1. **`Skill.getStatTags`**：追加 `triggered` 标签判定（3 行）。
2. **`SkillEvent.tryGrantWarcry`**：补 `Event.record(player, EventName.USE_WARCRY)`。
   - **这是本批最有价值的一处**：`USE_WARCRY` 此前只有声明、全工程零调用点，
     导致 **已接线的 `RECOVER_OF_MAX_HEALTH_AND_MANA_WHEN_YOU_USE_A_WARCRY` 恒不触发**（潜在静默失效）。
   - 该方法是全工程唯一的「战吼类技能施放」判定点（`statTags.contains("warcry")`），是天然发射点。

### 2.4 接线 14 条（1282 → 1296）

**纯复用既有 EFF（同义异写兄弟，5 条）**

| StatId | 落点 | 同族依据 |
|---|---|---|
| `MARK_SKILLS_HAVE_INC_USE_SPEED` | CAST_SPEED `{MARK}` | ≡ `MARK_USE_SPEED` |
| `SEALED_SKILLS_HAVE_INC_SEAL_GAIN_FREQUENCY` | CAST_SPEED `{SEAL}` | ≡ `SEAL_GAIN_SPEED` |
| `INC_EFFECT_OF_YOUR_MARK_SKILLS` | AILMENT_MAGNITUDE `{MARK,!TAKEN}` | ≡ `INC_MARK_EFFECT` |
| `OFFERING_SKILLS_HAVE_INC_DURATION` | AILMENT_DURATION `{OFFERING,!TAKEN}` | ≡ `INC_OFFERING_DURATION` |
| `MARK_SKILLS_HAVE_INC_SKILL_EFFECT_DURATION` | AILMENT_DURATION `{MARK,!TAKEN}` | ≡ `BANNER_SKILLS_HAVE_INC_DURATION` 口径 |

**新 EFF 落点（8 条）**

| StatId | 落点 |
|---|---|
| `INC_TOTEM_DURATION` | AILMENT_DURATION `{TOTEM,!TAKEN}` |
| `DETONATOR_SKILLS_HAVE_INC_DAMAGE` | DAMAGE `{DAMAGE,DETONATOR}` |
| `TRIGGERED_SPELLS_DEAL_INC_SPELL_DAMAGE` | DAMAGE `{DAMAGE,TRIGGERED,SPELL}` |
| `ANCESTRALLY_BOOSTED_ATTACKS_DEAL_INC_DAMAGE` | DAMAGE `{DAMAGE,ANCESTRAL_BOOST,ATTACK}` |
| `DETONATOR_SKILLS_HAVE_INC_AREA_OF_EFFECT` | EFFECT_AREA `{DETONATOR}` |
| `ECHOED_SPELLS_HAVE_INC_AREA_OF_EFFECT` | EFFECT_AREA `{ECHO,SPELL}` |
| `INC_AREA_OF_EFFECT_OF_ANCESTRALLY_BOOSTED_ATTACKS` | EFFECT_AREA `{ANCESTRAL_BOOST,ATTACK}` |
| `ADD_IGNITE_WHEN_YOU_WARCRY` | GrantOnEvent(IGNITE) `{USE_WARCRY}` |

**死事件复活解锁（1 条）**

| StatId | 落点 |
|---|---|
| `INC_DAMAGE_FOR_EACH_TIME_YOU_VE_WARCRIED_RECENTLY` | `per("count:use_warcry")`，DAMAGE(ALWAYS)，value=0.08 / per=1.0 |

### 2.5 过期注释更正（防误导）

`WireRecent` 类 Javadoc 原写「战吼类：`USE_WARCRY` 全工程无发射点」——本批已不成立，
改写为正确的排除原因（需 `COUNT` 模式 × 逐次累进倍率，见 `WirePerScaling` 的 `per()` 范式），
并补 `INC_DAMAGE_FOR_EACH_DIFFERENT_WARCRY_...`（另需 distinct 聚合）。
`WirePerScaling` 中的「【缺口·不接线】」注释同步替换为实际接线。

---

## 3. 门禁

| 项 | Batch 28 收口 | Batch 29 收口 | Δ |
|---|---|---|---|
| map.put | 1282 | **1296** | **+14** |
| 覆盖率 | 74.4% | **75.9%** | +1.5pt |
| effect | 663 | **672** | +9 |
| condition | 98 | 98 | 0 |
| target | 14 | 14 | 0 |
| entry JSON | 1283 | **1297** | +14 |
| unresolved refs | 0 | **0** | — |

`compileJava` ✅ ×3 → `runData` ✅ ×2 → `verify_gen.py` **RESULT: ALL CHECKS PASS（无 PEND）**。
`scripts/verify_gen.py` 基线同步：`effect count 663` → `672`。

---

## 4. 排除判据（已写入源码注释，防回退）

| 条目 | 排除原因 |
|---|---|
| `INC_TOTEM_PLACEMENT_RANGE` | 无 PLACEMENT_RANGE 属性（引擎缺口） |
| `INC_MANA_COST_EFFICIENCY_OF_MARKS` | 无 MANA_COST_EFFICIENCY 属性（引擎缺口） |
| `INC_BUFF_MAGNITUDE_OF_IMPALES_INFLICTED_WITH_SPELLS` | `DebuffStatusTypes.IMPALE` 是**每层固定的物理承伤**（`damageTaken(stacks × 4%)`），幅度不经 stat 读取 → 无读取点 |
| `INC_MOVEMENT_SPEED_IF_YOU_VE_USED_A_MARK_RECENTLY` | 无 `USE_MARK` 事件（对照 `USE_TOTEM` 有 `EntityOperationEvent:259`） |
| `INC_CAST_SPEED_PER_SPELL_ECHOED_RECENTLY_UP_TO` | 需 ECHO 重复机制 + 封顶 per 计数，机制未落地 |
| `INC_DAMAGE_FOR_EACH_DIFFERENT_WARCRY_YOU_VE_USED_RECENTLY` | 需 distinct 去重聚合（事件队列只有计数与求和） |
| `MINION_REVIVED` 族 | 引擎无持久召唤物复活机制（真死事件，非接线问题） |
| `COLLECT_REMNANT` 族 | 残迹机制未实现（与 `StatTag.REMNANT` 同源） |

---

## 5. 下一步（引擎扩展前沿，按 ROI）

1. **`MINION_REVIVED` 复活机制**（真死事件，已令 2 条既有接线失效）——实现持久召唤物复活后补 `Event.record`。
2. **`COLLECT_REMNANT` 残迹机制**——补完可同时激活 `StatTag.REMNANT` 的 4 个既有 EFF。
3. **Echo 重复施放机制**——把 `spell_echo` 从「平坦 areaAoe 倍率」升级为真正的第二次施放实例，激活 `StatTag.ECHO`。
4. **Ancestral Boost（Warrior）机制**——天赋节点已存在，落地后激活 `StatTag.ANCESTRAL_BOOST`。
5. **`PresenceCountValue.Kind` 补 COMPANION**（现仅 ANY/SUMMONED/TOTEM）→ 解锁 Companion 簇 3+ 条。
6. **`count_distinct` 去重计数原语** → 解锁「for each different X」7 条。
7. **`TGT_ALL_ON_TARGET` 验证**（已定义零使用）→ 解锁 `ENEMIES_YOU_CURSE_*` 一族。
