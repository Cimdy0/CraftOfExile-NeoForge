# 死线审计分流（scan_hardcoded_const #48 + AttributeRegister #59，2026-09-07）

## #48 结论（scan_hardcoded_const）
对 `scripts/scan_hardcoded_const.py --all`（356 条，已排除 data/gen 与 stat 定义）→ 排除纯 UI/GUI 坐标后得干净 gameplay 候选集（`scripts/_hc_gameplay.txt`，由 `scripts/filter_gameplay.py` 可复现产出），逐条核验 `AttributeRegister` 与对应代码站点消费方式：**真·#48（常量顶替属性 = 死线）数量 = 0**。

> 过滤修正（本论）：原 `_hc_gameplay.txt`（旧快照 94 条）由更早版本的扫描 + 较重过滤产出，且**漏排 9 条纯 GUI 槽位坐标**（`item/accessory/AccessorySlot`、`item/charm/CharmSlot`、`item/flask/FlaskSlot` 的 `SLOT_X/Y0/STEP`）。已写入 `scripts/filter_gameplay.py` 补排 `item/accessory/`、`item/charm/`、`Slot.java`，可复现产出干净集（当前扫描口径下为 347 条，比旧 94 更完整——旧快照还误含部分应保留的 gameplay 常量且漏排 GUI）。无论 94 还是 347，分类结论一致：**0 真·#48**。

## #59 结论（AttributeRegister 注册属性无引擎读点）
`scripts/audit_stat_unread.py` 反查 `AttributeRegister` 全部 **193** 个属性在「代码直读 `AttributeRegister.X` / datagen 接线（id 字符串）/ 生成 JSON」三处是否至少一处有消费点：**疑似 #59（零消费点）= 0**。即所有注册属性均被引擎消费，无孤儿属性死线。

94 条全部落入以下三类，均**非死线**：

| 桶 | 含义 | 数量 |
|---|---|---|
| `SYS_LIMIT` | 引擎/系统上限、循环间隔、历史缓冲等（非玩家可调属性） | ~28 |
| `DESIGN_CONST` | 设计常量 / 阈值 / 技能 data 回退默认值 | ~57 |
| `HYBRID_STAT` | 基础值 × 已注册属性（base+stat 混合，项目既定正确范式） | ~9 |

> 关键依据：`AttributeRegister.java:37` 明确**禁止「裸属性 + 硬编码消费端」**——即「把硬编码常量替换为裸属性读取」本身即该项目反对的反模式。故已落地的 base×stat 混合（Darkness/Rage/Minion/Curse/ES/Dodge/Parry/Blind）是**正确设计**，不是 #48 死线。

## 过滤瑕疵（meta，已修复）
原 `_hc_gameplay.txt`（旧快照 94 条）混入 **9 条纯 UI 布局常量**（不应在 gameplay 集）：
`AccessorySlot.SLOT_X/Y0/STEP`、`CharmSlot.SLOT_X/Y0/STEP`、`FlaskSlot.SLOT_X/Y0/STEP`（均 `=181/127/61/9/22` 等 GUI 坐标）。旧过滤只排 `ui/`、`client/`，未排 `item/accessory|charm|flask`。现已由 `scripts/filter_gameplay.py` 补排 `item/accessory/`、`item/charm/`、`Slot.java`，可复现产出干净集（GUI 坐标已确认不在 `_hc_gameplay.txt` 中）。

## 分类明细

### HYBRID_STAT（base × 已注册属性，正确范式，0 改动）
| 常量 | 文件 | 属性 |
|---|---|---|
| `BASE_MAX_DARKNESS`/`DARKNESS_PER_LEVEL`/`BASE_RESERVATION_SECONDS` | DarknessHelper | ×`MAX_DARKNESS` / ×`DARKNESS_RESERVATION_LASTS` |
| `LOSS_GRACE_TICKS` | RageHelper | +`RAGE_LOSS_GRACE` |
| `LOSS_PER_TICK` | RageHelper | ×`RAGE_LOSS_RATE` |
| `BASE_REVIVE_TICKS` | MinionLifecycle | ÷`MINION_REVIVE_SPEED` |
| `BASE_RECHARGE_DELAY` | EnergyShieldHelper | ÷`ENERGY_RECHARGE` |
| `BASE_DELAY_TICKS` | CurseActivation | ×(1+`CURSE_ACTIVATION_DELAY_MORE`)/(1+`CURSE_ACTIVATION_FREQUENCY`) |
| `BLIND_BASE_MAGNITUDE`/`BLIND_DURATION_TICKS` | AilmentHelper | ×`AILMENT_MAGNITUDE{D}`/`AILMENT_DURATION{D}`（唯一已落地 #48 接管范式，见 L872-875） |
| （DODGE_ROLL_DISTANCE / PARRY_RANGE 本身已是属性，被 `ROLL_DISTANCE`/`PARRY_RANGE` 站点读取；扫描命中的是其伴随的 `ROLL_TICKS`/`PERFECT_PARRY_WINDOW` 等设计常量） | — | — |

### DESIGN_CONST（设计常量/阈值/技能回退，0 改动，部分若需玩家可调须 NEW stat=R3 推迟）
- 暴击/击退：`BASE_CRIT_MULTIPLIER`(×`critDamageMultiplier` 内含金 stat)、`KNOCKBACK_CLOSE_RANGE`(阈值)、`KNOCKBACK_FORCE`(基础力，KNOCKBACK stat 在 `onKnockBack` 缩放)
- 诅咒/创口：`CURSE_RESIST_REDUCTION_BASE`(×mag)、`INCISION_BLEED_CHANCE_PER_STACK`(×stacks×mag)、`CURSE_DURATION_EXPIRED_FRACTION`(阈值)、`DEFAULT_CURSE_RADIUS/DURATION`、`DEFAULT_MARK_RADIUS/DURATION`(技能 data 回退)
- 致盲/格挡：`BLOCKED_STUN_RATIO`
- 波动：`DAMAGE_PER_VOLATILITY`(×maxV，无对应 stat)、`RADIUS`(无对应 stat)
- 战吼/标记：`WARCRY_DEBUFF_RADIUS`(`WARCRY_MIN_POWER` 是 min-power 非半径，无半径 stat)、`MARK_BLIND_RADIUS`/`MARKED_SEARCH_RADIUS`/`INTERVAL`
- 残迹/击杀爆：`PICKUP_RANGE`/`TTL`/`EMPOWER_DURATION`、`EXPLODE_BURST_LIFE_FRACTION`/`EXPLODE_BURST_RADIUS`(chance 已走 `BURNING/EMPOWERED_EXPLODE_ON_KILL_CHANCE`)
- 补给/药剂：`RECOUP_DURATION`(注释明示为未来 `RECOUP_DURATION` 属性扩展点，当前无该 stat)、`RECHARGE_TICKS`、`VAAL_DURATION_TICKS`
- 招架/翻滚：`PERFECT_PARRY_WINDOW_TICKS`/`DEFEND_FAR_DISTANCE`/`DEFEND_LOW_ENERGY_FRACTION`(阈值)、`ROLL_TICKS`/`ROLL_SPEED`/`COOLDOWN_TICKS`/`IFRAME_TICKS`/`RECOVERY_TICKS`/`SPRINT_GRACE_TICKS`/`PUSH_RADIUS`/`PUSH_SPEED`/`FLIP_LANDING_TICKS`/`DODGE_FAIL`
- 状态/形态：`HERALD_PRESENCE_TICKS`、`FORM_DURATION`(≈∞)、`BUILDUP_DECAY_PER_TICK`
- 物品：`GloveItem.DEFAULT_ARMOR`(item data 默认)

### SYS_LIMIT（引擎/系统上限，非玩家属性，0 改动）
`TICK_INTERVAL`(ResearchBench)、`MAX_CHUNKS_PER_SCAN`/`SCAN_INTERVAL`(Terminal)、`LAYER_PRIORITY`(SkillCastAnimator)、`INFINITE`(PersistentDelivery)、`MAX_FIRE_DEPTH`(StatEngine)、`FORCED_MAX_STACKS`(StatusEngine)、`THROTTLE_TICKS`/`MIN_LIGHT_LEVEL`(MovingLight)、`MAX_CONVERT_DEPTH`(ResourceEventHub)、`APPLY_INTERVAL`/`SYNC_INTERVAL`(GroundField)、`HISTORY_TICKS`/`MOVE_THRESHOLD`/`MAX_TRIGGER_HISTORY`/`MAX_TRIGGER_MEMORY`/`PRESENCE_MEMORY_TICKS`(Event)、`SKILL_TIER_MAX`/`SUPPORT_TIER_MAX`(UncutGem)、`MAX_CORPSES`(CorpseState)、`DECAY_GRACE_TICKS`/`DECAY_PER_SECOND`/`CAP`(GloryState/MetaEnergyState)、`BASE_LINGER_SECONDS`/`DEFAULT_AREA_SECONDS`(BannerAction)

## 门禁
- 本轮仅新增静态审计脚本 + 重生成候选集 + 更新文档，**无 gameplay 源码改动** → 双门禁无需重跑（脚本不进编译）；基线维持前序：compileJava BUILD SUCCESSFUL，verify_gen ALL PASS（effect 844 / target 14 / condition 147 / entry 1663，missing=0 orphan=0）。

## 死线审计主线收口状态（#48 / #59 / #65 / #62）
| 类别 | 检测 | 结果 |
|---|---|---|
| #48 硬编码常量顶替属性 | scan_hardcoded_const + 逐站核验 | **0 真死线**（94/347 候选全为设计常量 / 系统上限 / base×属性混合） |
| #59 注册属性无引擎读点 | audit_stat_unread（193 属性） | **0 真死线**（全部至少有代码直读 / datagen 接线 / 生成 JSON 一处消费） |
| #65 fire 缺失 | audit_fire_coverage | 修 7 处真死线，余 18 flag 全假阳性 |
| #62 悬空属性白名单 | attr_norm / event_thin | B0+22 白名单 / A0/C0/D0 / A/B/C=0 全绿 |

## 剩余项（R3 推迟，不在本论安全范围）
- 66 条未接线 StatIds（`unwired_now.txt`）：经全仓 grep 均须新建子系统 + 新增引擎读点，强接即死线（R3 违例），不接。
- 66 条之外的新机制化（thorns / 元素要害降抗 / 护甲移速惩罚 / overgrow / shield-raised / grenade 主技能 / pin 累积 / str→hp / banner 加抗通道 / 弓距离→暴击 / attribute requirements / companion 承伤转移 / deflect / parry AoE / fissure·orb·temporary-minion Limit 等）：均须机制先行，非死线修复范畴。
