package com.cimdy.coe.data.codec.support.math.context.node;

import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.AccessoryValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.AttributeValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.scope.BindExpr;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.skill.ChargeCountValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.CharmActiveValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.CompareValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.ConstantValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.scope.ValueExprGroup;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.damage.DamageValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.damage.DamageEventNode;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.target.DistanceToNearestEnemyValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.core.DistanceValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.status.EffectValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.status.ElementalAilmentTypeCountValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.EntityFlagValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.EntityTypeValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.EquipmentValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.meta.mode.EventHistoryModes;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.EventHistoryValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.EventValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.FlaskActiveValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.FlaskUsesValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.GemValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.core.HealthValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.HitStateValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.IgnitedBySourceValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.IgniteHistoryFireValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.LivingStateValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.EventRecencyValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.skill.MetaEnergyValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.core.MovementValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.target.NearbyEnemyCountValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceCountValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.meta.misc.PresenceDependent;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceRarityValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.scope.SelectorNode;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.skill.SkillStateValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.skill.SkillUsingValue;
import com.cimdy.coe.data.codec.support.math.context.node.RefExpr;
import com.cimdy.coe.data.codec.support.math.context.ValueTarget;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.status.StatusValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.status.WitherStacksValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.target.TargetRarityValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.TimeValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.TimerValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.skill.UsingSkillValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.meta.misc.VarRef;
import com.cimdy.coe.event.Event;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.*;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;

/**
 * Context 读值节点类型注册表（context_dsl_design.md §2.3 / M2）。
 * <p>
 * <b>单点注册</b>：每个检测（读值节点）以一条 {@link ValueExprType} 注册项聚合其全部序列化面向
 * （JSON type 键 / 实现类 / CODEC / 中缀反串化 / 变量前缀解析）。以下四个方向全部由注册表推导，
 * 不再各自维护平行 if 链：
 * <ol>
 *   <li>{@link #resolve}：math 变量前缀 → 节点（parse 方向）；</li>
 *   <li>{@link #toInfix}：节点 → 中缀变量串（toInfixString 往返方向）；</li>
 *   <li>{@link #CODEC} decode：JSON {@code type} 字段 → 节点 CODEC；</li>
 *   <li>{@link #CODEC} encode：节点实例类 → CODEC + type 键反查。</li>
 * </ol>
 * <b>新增一个检测 = 一个 {@link ValueExpr} 实现 + {@link #bootstrap} 中一行 register。</b>
 * 外部扩展亦可在首次使用前调用 {@link #register}。
 */
public final class ValueExprTypes {
    private ValueExprTypes() {}

    /** 按 JSON type 键索引（decode 分派）。 */
    private static final Map<String, ValueExprType<?>> BY_KEY = new LinkedHashMap<>();
    /** 按实现类索引（encode / toInfix 反查）；同类多键时首个注册项为规范形式。 */
    private static final Map<Class<?>, ValueExprType<?>> BY_CLASS = new LinkedHashMap<>();
    /** 注册顺序列表（resolve 按序尝试，保持前缀匹配优先级）。 */
    private static final List<ValueExprType<?>> ORDERED = new ArrayList<>();

    private static boolean bootstrapped = false;

    /** 注册一个节点类型；新增检测的唯一登记点。 */
    public static synchronized <T extends ValueExpr> void register(ValueExprType<T> type) {
        BY_KEY.put(type.key(), type);
        BY_CLASS.putIfAbsent(type.clazz(), type);
        ORDERED.add(type);
    }

    /** 内置节点注册（懒执行，避免与各节点 CODEC 的静态初始化环）。公开以支持跨子包（query 等）触发。 */
    public static synchronized void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;

        register(ValueExprType.of("attribute", AttributeValue.class, AttributeValue.CODEC,
                a -> "attribute:" + a.id(),
                (t, id, def) -> t.equals("attribute") ? new AttributeValue(id) : null));
        register(ValueExprType.of("effect", EffectValue.class, EffectValue.CODEC,
                e -> e.mode() + ":" + e.id(),
                (t, id, def) -> t.startsWith("effect") ? new EffectValue(id, t) : null));
        register(ValueExprType.of("timer", TimerValue.class, TimerValue.CODEC,
                TimerValue::name,
                (t, id, def) -> t.startsWith("time_") ? new TimerValue(t, def) : null));
        register(ValueExprType.of("time", TimeValue.class, TimeValue.CODEC,
                TimeValue::type,
                (t, id, def) -> t.startsWith("time.") ? new TimeValue(t) : null));
        register(ValueExprType.of("distance", DistanceValue.class, DistanceValue.CODEC,
                d -> switch (d.mode()) {
                    case REFERRER_TO_CANDIDATE -> "dist()";
                    case HIT -> "hit_state:distance";
                    default -> "distance";
                },
                (t, id, def) -> t.equals("distance") ? new DistanceValue(DistanceValue.Mode.TO_ANCHOR) : null));
        // 资源读值（合并组 A）：health / max_health / mana / max_mana / energy / max_energy / spirit / max_spirit / rage / resource
        register(ValueExprType.of("health", HealthValue.class, HealthValue.CODEC,
                HealthValue::mode,
                (t, id, def) -> switch (t) {
                    case "health", "max_health", "resource", "mana", "max_mana",
                         "energy", "max_energy", "spirit", "max_spirit", "rage"
                            -> new HealthValue(t.equals("resource") ? "health" : t);
                    default -> null;
                }));
        // resource：HealthValue 的 JSON 别名键（encode 规范形式为 health）
        register(ValueExprType.of("resource", HealthValue.class, HealthValue.CODEC, HealthValue::mode));
        register(ValueExprType.of("charge", ChargeCountValue.class, ChargeCountValue.CODEC,
                c -> "charge:" + c.charge(),
                (t, id, def) -> t.equals("charge") ? new ChargeCountValue(id.getPath()) : null));
        // 统一状态读值 / 判定节点（合并原 7 类近重复节点：StatusValue / StatusKindValue / StatusTagValue /
        // StatusAgeValue / HeavyStunValue / FullyBrokenArmorValue / CurseDurationExpiredValue）。
        // 单一 "status" 键，参数化 Match 规格（id/kind/tag/age_threshold/fraction/flag）+ output 模式；
        // 中缀简写沿用原各键形式（status:<id> / status_kind:<KIND> / status_tag:<TAG> / status_age:<id> /
        // heavy_stun_status / fully_broken_armor_status / curse_duration_expired），JSON 全量形式见 StatusValue。
        register(ValueExprType.of("status", StatusValue.class, StatusValue.CODEC,
                s -> {
                    if (s.flag().isPresent())
                        return s.flag().get() == StatusValue.Flag.HEAVY ? "heavy_stun_status" : "fully_broken_armor_status";
                    if (s.id().isPresent())
                        return s.ageThreshold().isPresent() ? "status_age:" + s.id().get() : "status:" + s.id().get();
                    if (s.tag().isPresent()) return "status_tag:" + s.tag().get();
                    if (s.kind().isPresent())
                        return s.fraction().isPresent() ? "curse_duration_expired" : "status_kind:" + s.kind().get();
                    return "status";
                },
                (t, id, def) -> {
                    return switch (t) {
                        case "status" -> new StatusValue(id.getPath(), id);
                        case "status_kind" -> StatusValue.byKind(id.getPath());
                        case "status_tag" -> StatusValue.byTag(id.getPath());
                        case "status_age" -> StatusValue.byAgeThreshold(id.getPath(), 80.0 / 20.0);
                        case "heavy_stun_status" -> StatusValue.byFlag(StatusValue.Flag.HEAVY);
                        case "fully_broken_armor_status" -> StatusValue.byFlag(StatusValue.Flag.FULLY_BROKEN);
                        case "curse_duration_expired" -> {
                            double f = 0.5;
                            try { if (!id.getPath().isEmpty()) f = Double.parseDouble(id.getPath()); }
                            catch (NumberFormatException ignored) {}
                            yield StatusValue.byCurseFraction(f);
                        }
                        default -> null;
                    };
                }));
        // 到最近敌对实体距离（距离簇基础设施）：dist_nearest_enemy，复用 EntityQuery + !isAlliedTo 既有约定
        register(ValueExprType.of("dist_nearest_enemy", DistanceToNearestEnemyValue.class, DistanceToNearestEnemyValue.CODEC,
                s -> "dist_nearest_enemy",
                (t, id, def) -> t.equals("dist_nearest_enemy") ? new DistanceToNearestEnemyValue() : null));
        // 半径内敌对实体计数（「每个近距离敌人 +X」线性缩放基础设施）：nearby_enemy_count:<radius>
        // 与 dist_nearest_enemy 分工：后者回答「最近敌人多远」（距离门控），本键回答「身边几个敌人」（数量缩放）。
        register(ValueExprType.of("nearby_enemy_count", NearbyEnemyCountValue.class, NearbyEnemyCountValue.CODEC,
                n -> "nearby_enemy_count:" + plainNumber(n.radius()),
                (t, id, def) -> {
                    if (!t.equals("nearby_enemy_count")) return null;
                    String path = id.getPath();
                    double r = NearbyEnemyCountValue.DEFAULT_RADIUS;
                    if (!path.isEmpty()) {
                        try {
                            r = Double.parseDouble(path);
                        } catch (NumberFormatException ignored) {
                            // 非数值路径视为未指定半径，退回缺省（Close Range 2m）
                        }
                    }
                    return new NearbyEnemyCountValue(r);
                }));
        // 目标身上不同「元素异常」种类数（「for each type of Elemental Ailment on Enemy」线性缩放）：
// 命中结算时攻击者作用域持受害者（sourceEntity），本节点切到对方统计其身上 ignite/chill/freeze/shock/electrocute
// 五类元素异常的去重种类数（0—5）。与 nearby_enemy_count 分工：那是「周边敌人数」，本键是「目标身上的元素异常种类数」。
// 口径依据 poe2db：Elemental Ailments = Ignite/Chill/Freeze/Shock/Electrocute，与 AilmentStatusTypes 中
// 携带 GateTag.ELEMENTAL 的成员一一对应（不含物理/混沌类 bleed/poison）。
register(ValueExprType.of("elemental_ailment_type_count", ElementalAilmentTypeCountValue.class, ElementalAilmentTypeCountValue.CODEC,
        v -> "elemental_ailment_type_count",
        (t, id, def) -> t.equals("elemental_ailment_type_count") ? new ElementalAilmentTypeCountValue() : null));
        // 凋零层数读值（「for each stack of Wither on Enemy」线性缩放）：wither_stacks，读取当前实体 WITHER 层数（0—10）。
        // 与 elemental_ailment_type_count 分工：那是「目标身上的元素异常种类数」，本键是「自身身上的凋零层数」。
        register(ValueExprType.of("wither_stacks", WitherStacksValue.class, WitherStacksValue.CODEC,
                v -> "wither_stacks",
                (t, id, def) -> t.equals("wither_stacks") ? new WitherStacksValue() : null));
        // 伤害事件读值（EventNode 家族成员，从 DamageValue 的 EVENT 分支抽取）：damage_event，读实体单例事件历史
        // （经 ValueScope#event() 候选归一）；mode 缺省 "actual"。纯增量，不影响既有 damage_taken 等键。
        register(ValueExprType.of("damage_event", DamageEventNode.class, DamageEventNode.CODEC,
                v -> "damage_event",
                (t, id, def) -> t.equals("damage_event") ? new DamageEventNode("actual") : null));
        // 事件近期状态读值（EventNode 家族成员，从 LivingStateValue 的事件模式抽取）：event_recency，
        // 统一承载 parrying / leeching / leeching_mana / recently_took_dot / recently_gained_rage 五种近期事件判定，
        // JSON 全量形式 {"type":"event_recency","mode":"<mode>","window":<ticks>}；living_state 的事件模式内部委托本节点。
        register(ValueExprType.of("event_recency", EventRecencyValue.class, EventRecencyValue.CODEC,
                v -> "event_recency",
                (t, id, def) -> t.equals("event_recency") ? new EventRecencyValue("parrying", 80.0) : null));
        // 装备读值（合并组 C：原 equipment / equipment_stat / equipment_match）：aggregate 区分 COUNT / SUM / FIRST
        register(ValueExprType.of("equipment", EquipmentValue.class, EquipmentValue.codec(),
                e -> switch (e.aggregate()) {
                    case SUM -> "equipment_stat:" + (e.slots().isPresent() && e.slots().get().size() == 1
                            ? e.slots().get().get(0).getName() + "." : "") + e.output().orElse("");
                    default -> "equipment:" + (e.slots().isPresent() && e.slots().get().size() == 1
                            ? e.slots().get().get(0).getName() : "");
                },
                (t, id, def) -> t.equals("equipment") ? EquipmentValue.fromInfixSlot(id.getPath()) : null));
        register(ValueExprType.of("equipment_stat", EquipmentValue.class, EquipmentValue.codec(),
                e -> "equipment_stat:" + (e.slots().isPresent() && e.slots().get().size() == 1
                        ? e.slots().get().get(0).getName() + "." : "") + e.output().orElse(""),
                (t, id, def) -> {
                    if (!t.equals("equipment_stat")) return null;
                    // id 路径形如 "slot.stat"（如 head.armor）或仅 "stat"（跨所有槽求和）
                    String path = id.getPath();
                    String slot = null, stat = path;
                    int dot = path.indexOf('.');
                    if (dot >= 0) { slot = path.substring(0, dot); stat = path.substring(dot + 1); }
                    List<EquipmentSlot> slots = slot == null || slot.isEmpty()
                            ? null : EquipmentValue.parseSlot(slot).map(List::of).orElse(List.of());
                    return new EquipmentValue(Optional.ofNullable(slots), Optional.empty(), Optional.of(stat), EquipmentValue.Aggregate.SUM);
                }));
        // 饰品栏读值（accessory_stat:<group>.<stat>）：group = legs/quiver/amulet 等
        // （config/coe/slots.json 的 accessory_slots 分组，无配置走内置兜底），
        // stat 复用 EquipmentValue 同一组真实读点（armor/evasion/energy/strength_requirement…）。
        // 与 equipment_stat 同构：SUM 求和 / COUNT 计数 / FIRST 首个；仅玩家持有饰品栏附件，非玩家恒读 0。
        register(ValueExprType.of("accessory_stat", AccessoryValue.class, AccessoryValue.CODEC,
                a -> "accessory_stat:" + (a.group().isPresent() ? a.group().get() + "." : "") + a.output().orElse(""),
                (t, id, def) -> {
                    if (!t.equals("accessory_stat")) return null;
                    // id 路径形如 "legs.armor" → group=legs, stat=armor
                    String path = id.getPath();
                    String grp = null, stat = path;
                    int dot = path.indexOf('.');
                    if (dot >= 0) { grp = path.substring(0, dot); stat = path.substring(dot + 1); }
                    return new AccessoryValue(Optional.ofNullable(grp), Optional.of(stat), EquipmentValue.Aggregate.SUM);
                }));
        // 宝石孔读值（合并原 SocketValue）：socket:<color>.<mode> 统计匹配颜色与种类的已镶嵌宝石数（aggregate=count）
        register(ValueExprType.of("socket", GemValue.class, GemValue.CODEC,
                g -> "socket:" + g.color().orElse("") + (g.kind().equals("all") ? "" : "." + g.kind()),
                (t, id, def) -> {
                    if (!t.equals("socket")) return null;
                    // id 路径形如 "color.mode"（如 red.all）、仅 "color"、或空（计全部）
                    String path = id.getPath();
                    String color = null, kind = "all";
                    int dot = path.indexOf('.');
                    if (dot >= 0) { color = path.substring(0, dot); kind = path.substring(dot + 1); }
                    else if (!path.isEmpty()) { color = path; }
                    return new GemValue(Optional.empty(), Optional.empty(), Optional.ofNullable(color), kind, Optional.empty(), "count");
                }));
        // 事件历史读值（合并组 B：原 CountValue + RecentlyValue + TimerValue.mode=count）：
        // count:<name> 默认 mode=count；recently:<name>[:<tag>] 经 mode=recently 分支还原。
        // count 侧额外参数化两项（供 per-X 线性缩放族「近 N 秒内触发次数」使用）：
        //   · 标签：count:<name>:<tag>（与 recently 同构：ns 承载 name、path 承载 tag）
        //   二者可组合：count:<name>.<seconds>:<tag>（窗口后缀落在 name 段——decode 只在 name 段找点号）
        register(ValueExprType.of("count", EventHistoryValue.class, EventHistoryValue.CODEC,
                ValueExprTypes::eventHistoryInfix,
                (t, id, def) -> {
                    if (!t.equals("count")) return null;
                    String name = id.getPath();
                    String tag = null;
                    if (!"minecraft".equals(id.getNamespace())) {
                        name = id.getNamespace();
                        tag = id.getPath();
                    }
                    double window = EventHistoryValue.RECENTLY_WINDOW_TICKS;
                    int dot = name.indexOf('.');
                    if (dot >= 0) {
                        try {
                            window = EventHistoryValue.ticks(Double.parseDouble(name.substring(dot + 1)));
                            name = name.substring(0, dot);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    return tag == null
                            ? new EventHistoryValue(name, EventHistoryModes.COUNT, window)
                            : new EventHistoryValue(name, Optional.of(tag), Event.State.Match.EXACT,
                                    EventHistoryModes.COUNT, window);
                }));
        // sum：<name>[.<secs>] —— 窗口内触发<b>携带值</b>之和（Event.record(e, name, int value) 的 value 累加）。
        // 与 count 的分工（关键，勿混用）：
        //   · count  = 触发<b>次数</b>：每次 Event.record 记 1，与携带值无关；
        //   · sum    = 触发<b>值总量</b>：累加 TriggerRecord.value()。
        // 典型场景：充能球消耗。ResourceRegistry.consumeCharge 只调一次
        // Event.record(living, consumeCharge(type), real)（real = 本次实际消耗球数），
        // 故「per Endurance Charge Consumed Recently」必须用 sum:consume_endurance 才等价于「球数」；
        // 若误用 count:consume_endurance，一次消耗 3 球只计 1 → 静默欠授予（近似接线，R3 禁止）。
        // 另注意：Event.record(e, name, int value) 只写<b>无标签</b>队列（triggers），故本族不支持 :<tag> 分维
        // （写 count:<name>:<tag> 会落到 countTagged 恒 0 —— 见 Event.record 三参/两参重载分工）。
        register(ValueExprType.of("sum", EventHistoryValue.class, EventHistoryValue.CODEC,
                ValueExprTypes::eventHistoryInfix,
                (t, id, def) -> {
                    if (!t.equals("sum")) return null;
                    String name = id.getPath();
                    if (!"minecraft".equals(id.getNamespace())) name = id.getNamespace();
                    double window = EventHistoryValue.RECENTLY_WINDOW_TICKS;
                    int dot = name.indexOf('.');
                    if (dot >= 0) {
                        try {
                            window = EventHistoryValue.ticks(Double.parseDouble(name.substring(dot + 1)));
                            name = name.substring(0, dot);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    return new EventHistoryValue(name, EventHistoryModes.SUM, window);
                }));
        // distinct：EventHistoryValue 的 mode=distinct 别名键（去重敌计数）。
        // 形式 distinct:<name>[.<secs>]:<tagPrefix>（如 distinct:caused_ailment.8:victim_ignite）——
        // 窗口后缀与 count 同构（点号落在 name 段），tag 段承载「对象身份标签前缀」并强制 PREFIX 匹配：
        // 记录侧写 victim_ignite:<uuid>，PREFIX 聚合同族、完整标签（含 UUID）作去重键 → 得不同敌人数。
        // 与 count 的分工：count 数「触发次数」（单敌反复点燃计多次），distinct 数「不同敌人数」（计 1 次）。
        register(ValueExprType.of("distinct", EventHistoryValue.class, EventHistoryValue.CODEC,
                ValueExprTypes::eventHistoryInfix,
                (t, id, def) -> {
                    if (!t.equals("distinct")) return null;
                    // Identifier 仅切首个 ':'：ns 承载 name[.secs]、path 承载身份标签前缀
                    String name = id.getPath();
                    String tag = null;
                    if (!"minecraft".equals(id.getNamespace())) {
                        name = id.getNamespace();
                        tag = id.getPath();
                    }
                    double window = EventHistoryValue.RECENTLY_WINDOW_TICKS;
                    int dot = name.indexOf('.');
                    if (dot >= 0) {
                        try {
                            window = EventHistoryValue.ticks(Double.parseDouble(name.substring(dot + 1)));
                            name = name.substring(0, dot);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    return tag == null
                            ? new EventHistoryValue(name, EventHistoryModes.DISTINCT, window)
                            : EventHistoryValue.distinct(name, tag, window);
                }));
        register(ValueExprType.of("movement", MovementValue.class, MovementValue.CODEC,
                m -> "movement:" + m.mode(),
                (t, id, def) -> t.equals("movement") ? new MovementValue(id.getPath(), 20) : null));
        // recently：EventHistoryValue 的 mode=recently 别名键（encode 规范形式为 count + mode=recently）
        register(ValueExprType.of("recently", EventHistoryValue.class, EventHistoryValue.CODEC,
                ValueExprTypes::eventHistoryInfix,
                (t, id, def) -> {
                    if (!t.equals("recently")) return null;
                    // Identifier 仅切第一个 ':'：ns 承载 name、path 承载 tag，绕开双冒号限制
                    // （infix 无法表达 proj:<id>/PREFIX，含 ':' 的 tag 降级为 name-only，仅 JSON 全量形式可表达）。
                    return "minecraft".equals(id.getNamespace())
                            ? new EventHistoryValue(id.getPath(), "recently", 80)
                            : new EventHistoryValue(id.getNamespace(), java.util.Optional.of(id.getPath()),
                                    Event.State.Match.EXACT, "recently", 80);
                }));
        register(ValueExprType.of("living_state", LivingStateValue.class, LivingStateValue.CODEC,
                p -> "living_state:" + p.mode(),
                (t, id, def) -> t.equals("living_state") ? new LivingStateValue(id.getPath(), 80) : null));
        // 药水效果生效中（读 FLASK_ACTIVE 附件；resource 空=任意，否则限定 mana/health）。用于「during any Flask / Mana Flask effect」族。
        register(ValueExprType.of("flask_active", FlaskActiveValue.class, FlaskActiveValue.CODEC,
                f -> "flask_active:" + (f.resource().isEmpty() ? "any" : f.resource()),
                (t, id, def) -> {
                    if (!t.equals("flask_active")) return null;
                    return new FlaskActiveValue(id.getPath().equals("any") ? "" : id.getPath());
                }));
        // 药剂剩余可用次数（读 FLASK_SAVE 槽内充能 ÷ 单次消耗；resource 空=全槽合计，否则限定 health/mana）。
        // 用于「while you have no Health Flask uses left」族（配 CompareValue < 1）。
        register(ValueExprType.of("flask_uses", FlaskUsesValue.class, FlaskUsesValue.CODEC,
                f -> "flask_uses:" + (f.resource().isEmpty() ? "any" : f.resource()),
                (t, id, def) -> {
                    if (!t.equals("flask_uses")) return null;
                    return new FlaskUsesValue(id.getPath().equals("any") ? "" : id.getPath());
                }));
        // 护符效果在身（读统一状态层；任一 charm BUFF 状态生效中即 1）。用于「while you have an active Charm」族。
        register(ValueExprType.of("charm_active", CharmActiveValue.class, CharmActiveValue.CODEC,
                f -> "charm_active",
                (t, id, def) -> t.equals("charm_active") ? new CharmActiveValue() : null));
        // 命中状态读值（is_entity / is_block / is_miss / distance），替代旧 HitTestType 体系
        register(ValueExprType.of("hit_state", HitStateValue.class, HitStateValue.CODEC,
                h -> "hit_state:" + h.mode().getSerializedName(),
                (t, id, def) -> {
                    if (!t.equals("hit_state")) return null;
                    // 命中距离已并入 distance 节点（DistanceValue.Mode.HIT），保留 hit_state:distance 别名
                    if (id.getPath().equals("distance")) return new DistanceValue(DistanceValue.Mode.HIT);
                    HitStateValue.Mode m = HitStateValue.Mode.fromName(id.getPath());
                    return m != null ? new HitStateValue(m) : null;
                }));
        register(ValueExprType.of("skill", SkillStateValue.class, SkillStateValue.CODEC,
                SkillStateValue::mode,
                (t, id, def) -> SkillStateValue.isSkillState(t) ? new SkillStateValue(t) : null));
        // 伤害读值（合并组 F）：damage.<mode> 默认 CONTEXT 源；damage_taken./damage_caused./heal./spend. → EVENT 源
        register(ValueExprType.of("damage", DamageValue.class, DamageValue.CODEC,
                d -> switch (d.source()) {
                    case EVENT -> {
                        String m = d.mode();
                        if (m.startsWith("caused.")) yield "damage_caused." + m.substring("caused.".length());
                        if (m.startsWith("heal.")) yield "heal." + m.substring("heal.".length());
                        if (m.startsWith("spend.")) yield "spend." + m.substring("spend.".length());
                        yield "damage_taken." + m;
                    }
                    default -> "damage." + d.mode();
                },
                (t, id, def) -> DamageValue.isDamageMode(t)
                        ? new DamageValue(t.substring("damage.".length())) : null));
        // 承受伤害数值读值（P5 闭环表达式侧）：damage_taken.<mode> → DamageValue.Source.EVENT
        register(ValueExprType.of("damage_taken", DamageValue.class, DamageValue.CODEC,
                d -> "damage_taken." + d.mode(),
                (t, id, def) -> DamageValue.isDamageTakenMode(t)
                        ? new DamageValue(DamageValue.Source.EVENT, t.substring("damage_taken.".length())) : null));
        // 造成伤害数值读值（对称侧）：damage_caused.<mode>（actual / overflow / resource:<r>）→ EVENT 源 caused.* 分支
        register(ValueExprType.of("damage_caused", DamageValue.class, DamageValue.CODEC,
                d -> "damage_caused." + d.mode().substring("caused.".length()),
                (t, id, def) -> t.startsWith("damage_caused.")
                        ? new DamageValue(DamageValue.Source.EVENT, "caused." + t.substring("damage_caused.".length())) : null));
        // 回复量读值（M1 闭合）：heal.<mode>（requested / actual / overflow / resource:<r>）→ EVENT 源 heal.* 分支
        register(ValueExprType.of("heal", DamageValue.class, DamageValue.CODEC,
                d -> "heal." + d.mode().substring("heal.".length()),
                (t, id, def) -> t.startsWith("heal.")
                        ? new DamageValue(DamageValue.Source.EVENT, "heal." + t.substring("heal.".length())) : null));
        // 消耗量读值（M1 闭合）：spend.<mode>（requested / actual / missing / resource:<r>）→ EVENT 源 spend.* 分支
        register(ValueExprType.of("spend", DamageValue.class, DamageValue.CODEC,
                d -> "spend." + d.mode().substring("spend.".length()),
                (t, id, def) -> t.startsWith("spend.")
                        ? new DamageValue(DamageValue.Source.EVENT, "spend." + t.substring("spend.".length())) : null));
        // 装配技能宝石读值（合并原 SkillGemValue）：gem:<gemId> 读首个命中宝石属性（aggregate=first）
        register(ValueExprType.of("gem", GemValue.class, GemValue.CODEC,
                g -> g.aggregate().equals("count")
                        ? "socket:" + g.color().orElse("") + (g.kind().equals("all") ? "" : "." + g.kind())
                        : "gem:" + g.gemId().map(Identifier::toString).orElse(""),
                (t, id, def) -> t.equals("gem")
                        ? new GemValue(Optional.empty(), Optional.of(id), Optional.empty(), "all", Optional.empty(), "first") : null));
        // 实体当前正在使用的技能：skill_using:<id>（输出 1/0，可被 predicate 检测与 math 求值共用）
        register(ValueExprType.of("skill_using", SkillUsingValue.class, SkillUsingValue.CODEC,
                s -> "skill_using:" + s.id(),
                (t, id, def) -> t.equals("skill_using") ? new SkillUsingValue(id) : null));
        // 实体当前正在使用的元素技能：skill_using_element:<element>（element=any/cold/fire/...）
        // 遍历 activeSkills 中 caster 为当前实体的技能实例，按技能定义（递归 phases→actions）判定含对应元素 DamageAction。
        // 与 SkillUsingValue（按技能 id 精确匹配）互补：本节点按「元素」聚合匹配，服务于「using Cold/Fire/any Skills」族。
        register(ValueExprType.of("skill_using_element", UsingSkillValue.class, UsingSkillValue.CODEC,
                s -> "skill_using_element:" + s.element(),
                (t, id, def) -> t.equals("skill_using_element") ? new UsingSkillValue(id.getPath()) : null));
        // 实体判定聚合节点（entity_flag）：类型安全（type/biome/dimension 为 HolderSet / ResourceKey，无字符串解析）。
        // tag/biome/dimension 走 JSON 全量对象形式；无参数旗标 summoned/owner/level 额外支持中缀简写
        // （type:<id> / biome:<id> / dimension:<id> / summoned / owner / level），输出 1.0/0.0（或 level 原始值）。
        // 注：rarity / team / camp 尚未实现，勿在 datapack 中使用；recently_entered/recently_left 已由 presence 节点落地。
        // 当前触发事件名判定：event:<name> 命中当前事件名返回 1.0，否则 0.0（替代旧 MetaSkillCondition 枚举）
        register(ValueExprType.of("event", EventValue.class, EventValue.CODEC,
                e -> "event:" + e.eventName() + (e.tag() != null ? ":" + e.tag() : ""),
                (t, id, def) -> t.equals("event") ? new EventValue(id.getPath()) : null));
        // 事件 + 标签判定：event_tag:<name>:<tag> 命中当前事件名且事件标签含 <tag> 返回 1.0（如 event_tag:caused_ailment:ignite）
        register(ValueExprType.of("event_tag", EventValue.class, EventValue.CODEC,
                e -> "event_tag:" + e.eventName() + (e.tag() != null ? ":" + e.tag() : ""),
                (t, id, def) -> {
                    if (!t.equals("event_tag")) return null;
                    String[] parts = id.getPath().split(":", 2);
                    return parts.length == 2 ? new EventValue(parts[0], parts[1]) : null;
                }));
        // entity_flag 实体标记读值：类型安全（type/biome/dimension 为 HolderSet / ResourceKey，无字符串解析）。
        // tag/biome/dimension 走 JSON 全量对象形式；无参数旗标 summoned/owner/level 额外支持中缀简写。
        register(ValueExprType.of("entity_flag", EntityFlagValue.class, EntityFlagValue.codec(),
                f -> f.flag().isPresent() ? f.flag().get().getSerializedName() : null,
                (t, id, def) -> switch (t) {
                    case "summoned" -> EntityFlagValue.summoned();
                    case "owner" -> EntityFlagValue.owner();
                    case "level" -> EntityFlagValue.level();
                    default -> null;
                }));
        // 实体类别判定：entity_type:<tag id>,
        // 成员由 datapack entity_type tag（data/coe/tags/entity_type/*.json）定义，节点只持 TagKey、
        // 求值期从 level.registryAccess() 延迟解析 —— 增删类别成员无需改代码、可由数据包覆盖。
        // infix 返回 null（仅 JSON 全量 + COND 常量两种构造途径）：tag id 为 namespace:path 两段，
        // 无法塞进单冒号 Identifier 做变量简写往返，故不假装有中缀形式。
        register(ValueExprType.of("entity_type", EntityTypeValue.class, EntityTypeValue.CODEC,
                v -> null,
                (t, id, def) -> t.equals("entity_type") ? EntityTypeValue.of(id) : null));
        // presence 实体进/出范围读值：recently_entered:<window> / recently_left:<window>
        // 读追踪者 Presence 记录；被 stat 引用时按需启用每 tick 全域扫描（EventRefCollector → EventSubscription）
        register(ValueExprType.of("presence", PresenceValue.class, PresenceValue.CODEC,
                p -> (p.mode().equals("left") ? "recently_left:" : "recently_entered:") + (long) p.window(),
                (t, id, def) -> {
                    if (!t.equals("recently_entered") && !t.equals("recently_left")) return null;
                    double w = 200.0;
                    try { w = Double.parseDouble(id.getPath()); } catch (NumberFormatException ignored) { }
                    return new PresenceValue(t.equals("recently_left") ? "left" : "entered", w);
                }));
        // presence 范围内成员稀有度聚合（「身边有稀有/传奇敌人时」）：默认仅 JSON 全量形式。
        // 结构含稀有度列表 + 阵营开关 + 聚合模式，无法压成单段中缀，故不提供通用简写（同 equipment_match）。
        // 聚焦简写 presence_rarity:count = 范围内稀有/传奇敌人计数（用于「for each rare/unique monster +X」线性缩放）。
        // 该节点为 PresenceDependent，扫描由 EventRefCollector 自动开启；稀有度取自怪物生成时写入的 RARITY 附件（live，非死读值）。
        register(ValueExprType.of("presence_rarity", PresenceRarityValue.class, PresenceRarityValue.CODEC,
                p -> p.count() ? "presence_rarity:count" : null,
                (t, id, def) -> t.equals("presence_rarity") && id.getPath().equals("count")
                        ? PresenceRarityValue.rareOrUniqueEnemyCount() : null));
        // 事件「触发目标」稀有度（「当你重晕稀有 / 传奇敌人时 X」族）：默认仅 JSON 全量形式。
        // 读本次事件作用域绑定的交互对方（StatContext.sourceEntity）的 RARITY 附件——caused_ailment
        // +heavy_stun 路径下由 AilmentHelper.applyHeavyStun 显式绑定为「被重晕的敌人」（target-rarity，
        // 精确取代 presence_rarity 的「身边有稀有敌」近似）。结构含稀有度列表，无中缀简写。
        register(ValueExprType.of("target_rarity", TargetRarityValue.class, TargetRarityValue.CODEC, null));
        // 「目标被你点燃」条件读值节点（COND_TARGET_IGNITED_BY_YOU，Phase 1 标记系统重构附属基础设施）：
        // 读受害方 IGNITE_HISTORY 附件按施加者 UUID 累计秒数是否 >0，配合「求值 >= 1.0」布尔判据。无状态节点。
        register(ValueExprType.of("ignited_by_source", IgnitedBySourceValue.class, IgnitedBySourceValue.CODEC, null));
        // 「被你点燃过的敌人」递增承火伤幅度（ignite_history_fire_taken）：原 ignite_history_fire_taken 效果
        // （纯幅度公式、无动作）归位为值节点——载体退化为 MarkerStatEffect 身份标记，幅度改由本节点在
        // amount 表达式内求值（min(累计点燃秒数*rate, cap)），消费端 StatEngine#magnitudeOf 按 kind 折叠各条目。
        // 中缀 ignite_history_fire_taken[:<rate>_<cap>]：两参数均取缺省时省略后缀（与 eventHistoryInfix 同策略）。
        // 分隔符用下划线而非点号：小数本身含点号，点号无法无歧义切分两个 double；下划线亦是 IDENT 词法
        // 与 Identifier path 的合法字符（见 MathParser.TOKEN_PATTERN）。
        register(ValueExprType.of("ignite_history_fire_taken", IgniteHistoryFireValue.class,
                IgniteHistoryFireValue.CODEC, ValueExprTypes::igniteHistoryFireTakenInfix,
                (t, id, def) -> {
                    if (!t.equals("ignite_history_fire_taken")) return null;
                    String path = id.getPath();
                    int sep = path.indexOf('_');
                    if (sep >= 0) {
                        try {
                            return new IgniteHistoryFireValue(
                                    Double.parseDouble(path.substring(0, sep)),
                                    Double.parseDouble(path.substring(sep + 1)));
                        } catch (NumberFormatException ignored) {
                            // 非数值路径视为未指定参数，退回缺省（裸键经 EMPTY_LOCATION 解析后 path="empty" 亦走此支）
                        }
                    }
                    return new IgniteHistoryFireValue();
                }));
        // presence 范围内成员计数（「每个召唤物 / 图腾 +X」线性缩放）：presence_count:<relation>.<kind>[.distinct]
        // relation = any / ally / enemy；kind = any / summoned / totem / companion / persistent_minion。
        // 与 presence_rarity 分工：后者按稀有度，本键按归属。
        // 可选第三段 .distinct：按实体类型去重计「种类数」，驱动「for each different type of X」一族
        // （持久召唤物是常驻状态而非事件，故不能用事件的 distinct 原语——窗口必过期归零）。
        register(ValueExprType.of("presence_count", PresenceCountValue.class, PresenceCountValue.CODEC,
                p -> "presence_count:" + p.relation().getSerializedName() + "." + p.kind()
                        + (p.distinctType() ? ".distinct" : ""),
                (t, id, def) -> {
                    if (!t.equals("presence_count")) return null;
                    String[] parts = id.getPath().split("\\.");
                    String rel = parts.length > 0 ? parts[0] : "any";
                    String kind = parts.length > 1 ? parts[1] : "any";
                    boolean distinct = parts.length > 2 && "distinct".equals(parts[2]);
                    return new PresenceCountValue(PresenceCountValue.Relation.fromName(rel),
                            kind, distinct);
                }));
        // 元技能能量读值：meta_energy:<key> 读实体单例的能量计数。
        register(ValueExprType.of("meta_energy", MetaEnergyValue.class, MetaEnergyValue.CODEC,
                m -> "meta_energy:" + m.key(),
                (t, id, def) -> t.equals("meta_energy") ? new MetaEnergyValue(id.getPath()) : null));
        // 实体域绑定组合子（强复用内核）：target ∈ {SELF, SOURCE, CANDIDATE, SLOT}，单一实现取代
        // 原 SourceScopeNode + CandidateScopeNode 两个独立类（零双轨残留）。JSON 全量形式
        // {"type":"bind","target":"SOURCE","node":<inner>,"slot":<name?>}；中缀 source./candidate./slot()./(空) 由 target 还原。
        register(ValueExprType.of("bind", BindExpr.class, BindExpr.CODEC,
                b -> switch (b.target()) {
                    case CANDIDATE -> "candidate." + ValueExprTypes.toInfix(b.inner());
                    case SOURCE -> "source." + ValueExprTypes.toInfix(b.inner());
                    case SLOT -> "slot(" + b.slot().orElse("") + ")." + ValueExprTypes.toInfix(b.inner());
                    case SELF -> ValueExprTypes.toInfix(b.inner());
                }, null));
        // 命名值引用：ref:<id> 引用 ValueLib 中注册的命名值（Identifier 真实路径），一处定义、处处复用（强复用 / 随意引用）。
        register(ValueExprType.of("ref", RefExpr.class, RefExpr.CODEC,
                r -> "ref:" + r.key(),
                (t, id, def) -> t.equals("ref") ? new RefExpr(id) : null));
        // dist() 函数：参照实体到候选的距离（filter / sort 表达式）。
        // dist()：参照实体到候选的相对距离（DistanceValue.Mode.REFERRER_TO_CANDIDATE）
        register(ValueExprType.of("dist", DistanceValue.class, DistanceValue.CODEC,
                d -> "dist()", (t, id, def) -> t.equals("dist") ? new DistanceValue(DistanceValue.Mode.REFERRER_TO_CANDIDATE) : null));
        // 以下仅 JSON 全量形式（结构复杂 / 注入变量，无中缀前缀简写）
        register(ValueExprType.of("equipment_match", EquipmentValue.class, EquipmentValue.codec(), null));
        register(ValueExprType.of("select", SelectorNode.class, SelectorNode.CODEC, s -> "select"));
        register(ValueExprType.of("var", VarRef.class, VarRef.CODEC, vr -> "$" + vr.name()));
        // compare：两子节点数值比较（条件门控：装备护甲>闪避 / 镶嵌辅助宝石某色占多数），仅 JSON 全量形式。
        register(ValueExprType.of("compare", CompareValue.class, CompareValue.CODEC, null));
        // const：固定常量读值（CompareValue 右操作数 / 距离簇阈值），仅 JSON 全量形式（{"type":"const","value":3.0}）。
        register(ValueExprType.of("const", ConstantValue.class, ConstantValue.CODEC, null));
        // 复式条件组：自身即 ValueExpr、数据为子节点列表（可任意层嵌套），JSON 全量形式
        // {"type":"group","nodes":[…]}。无中缀简写（≈ 组语义无法压成单段变量）。
        register(ValueExprType.of("group", ValueExprGroup.class, ValueExprGroup.CODEC, g -> null));
    }

    /** 按变量类型前缀构造对应 ValueExpr；无法识别返回 {@code null}（交由上层兜底）。 */
    public static ValueExpr resolve(String type, Identifier id, double def) {
        bootstrap();
        for (ValueExprType<?> t : ORDERED) {
            if (t.resolver() == null) continue;
            ValueExpr node = t.resolver().resolve(type, id, def);
            if (node != null) return node;
        }
        return null;
    }

    /**
     * 反向序列化：将 {@link ValueExpr} 还原为中缀字符串（供 {@code MathExpression.CODEC} 的
     * {@code toInfixString} 往返）。与 {@link #resolve} 互逆：{@code parse(toInfix(node))} 得到等价节点。
     */
    public static String toInfix(ValueExpr node) {
        bootstrap();
        ValueExprType<?> t = BY_CLASS.get(node.getClass());
        if (t != null) {
            String s = t.infixOf(node);
            if (s != null) return s;
        }
        return "attr";
    }

    /**
     * 数据驱动分派 codec：JSON 对象含 {@code type} 字段，dispatch 到对应节点 CODEC。
     * 用 {@link Codec#lazyInitialized} 打破节点类间静态初始化环（SelectorNode.CODEC ↔ ValueExpr.CODEC）。
     */
    public static final Codec<ValueExpr> CODEC = Codec.lazyInitialized(() -> {
        bootstrap();
        return new DispatchCodec();
    });

    /** 数值去尾零输出（{@code 10.0 → "10"} / {@code 2.5 → "2.5"}），避免中缀出现无意义小数位。 */
    private static String plainNumber(double v) {
        return v == (long) v ? String.valueOf((long) v) : String.valueOf(v);
    }

    /**
     * {@link EventHistoryValue} 的规范中缀（{@code count} / {@code recently} / {@code distinct} 三键共享）。
     * <p><b>为何必须共享一份</b>：{@link #BY_CLASS} 以 {@code putIfAbsent} 登记，同类多键时只有<b>首个</b>
     * 注册键（{@code count}）的 infix 函数会被 {@link #toInfix} 调用。若 recently / distinct 各自写一份，
     * 那两份永不执行，而 count 那份不认识别的 mode → 往返时 mode 静默退化为 count
     * （{@code distinct} 会被写成 {@code count:…}，去重语义丢失）。故三键统一指向本方法，按 mode 分派。
     * <p><b>窗口后缀位置</b>：点号必须落在 {@code name} 段（{@code count:<name>.<secs>:<tag>}）——
     * 各键的 decode 只在 name 段查点号：后缀若拼在<b>末尾</b>
     * （{@code count:<name>:<tag>.<secs>}），「带标签 + 非默认窗口」组合会被 decode 误解析成
     * {@code tag="poison.10"} 且窗口回落 4s，往返破坏；既有接线恰好只用到「无标签+自定义窗口」与
     * 「带标签+默认窗口」两种组合才未暴露，本方法一并处理（对既有条目输出零变化）。
     */
    /**
     * {@link IgniteHistoryFireValue} 的中缀：{@code ignite_history_fire_taken[:<rate>_<cap>]}。
     * <p>rate/cap 均取缺省时省略后缀（{@code ignite_history_fire_taken}），使最常见的接线保持短形式；
     * 非缺省时两参数一并输出，保证 {@code parse(toInfix(node))} 还原等价节点（缺一即丢语义）。
     */
    private static String igniteHistoryFireTakenInfix(IgniteHistoryFireValue v) {
        if (v.rate() == IgniteHistoryFireValue.DEFAULT_RATE
                && v.cap() == IgniteHistoryFireValue.DEFAULT_CAP) {
            return "ignite_history_fire_taken";
        }
        return "ignite_history_fire_taken:" + plainNumber(v.rate()) + '_' + plainNumber(v.cap());
    }

    private static String eventHistoryInfix(EventHistoryValue e) {
        // 含 ':' 的 tag（如 proj:<id>/PREFIX）无法用中缀表达，降级为 name-only（既有约定）
        String tag = e.tag().isPresent() && !e.tag().get().contains(":") ? ":" + e.tag().get() : "";
        double secs = e.window() / EventHistoryValue.TICKS_PER_SECOND;
        String window = Math.abs(secs - 4.0) < 1e-6 ? "" : "." + plainNumber(secs);
        return switch (e.mode()) {
            // recently 的 decode 硬编码 80 tick，故不输出窗口后缀（避免产出无法往返的串）
            case EventHistoryModes.RECENTLY -> "recently:" + e.name() + tag;
            case EventHistoryModes.DISTINCT -> "distinct:" + e.name() + window + tag;
            // sum 与 count 的分工见 register("sum") 处注释；这里必须输出 sum: 前缀，
            // 否则 encode 会退化成 count: 而 decode 回 COUNT，往返即丢语义（欠授予）。
            case EventHistoryModes.SUM -> "sum:" + e.name() + window + tag;
            default -> "count:" + e.name() + window + tag;
        };
    }

    @SuppressWarnings("unchecked")
    private static Codec<ValueExpr> cast(Codec<? extends ValueExpr> codec) {
        return (Codec<ValueExpr>) codec;
    }

    /** 手工分派 codec：decode 按 {@code type} 字段委托子 codec；encode 在子码结果上补 {@code type} 键。 */
    private static final class DispatchCodec implements Codec<ValueExpr> {

        @Override
        public <T> DataResult<Pair<ValueExpr, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<MapLike<T>> mapRes = ops.getMap(input);
            if (mapRes.result().isEmpty()) {
                return DataResult.error(() -> "ValueExpr 期望对象");
            }
            MapLike<T> map = mapRes.result().get();
            T typeVal = map.get(ops.createString("type"));
            if (typeVal == null) {
                return DataResult.error(() -> "ValueExpr 缺少 type 字段");
            }
            Optional<String> type = ops.getStringValue(typeVal).result();
            if (type.isEmpty()) {
                return DataResult.error(() -> "ValueExpr type 字段非字符串");
            }
            ValueExprType<?> nodeType = BY_KEY.get(type.get());
            if (nodeType == null) {
                return DataResult.error(() -> "未知 ValueExpr 类型: " + type.get());
            }
            return cast(nodeType.codec()).decode(ops, input)
                    .map(pair -> Pair.of(pair.getFirst(), pair.getSecond()));
        }

        @Override
        public <T> DataResult<T> encode(ValueExpr node, DynamicOps<T> ops, T prefix) {
            ValueExprType<?> nodeType = BY_CLASS.get(node.getClass());
            if (nodeType == null) {
                return DataResult.error(() -> "无法序列化 ValueExpr: " + node.getClass());
            }
            return cast(nodeType.codec()).encode(node, ops, prefix).flatMap(encoded -> {
                Map<T, T> out = new LinkedHashMap<>();
                out.put(ops.createString("type"), ops.createString(nodeType.key()));
                ops.getMap(encoded).result().ifPresent(m ->
                        m.entries().forEach(e -> out.put(e.getFirst(), e.getSecond())));
                return DataResult.success(ops.createMap(out));
            });
        }
    }
}
