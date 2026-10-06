package com.cimdy.coe.data.gen.gem.skill;

import com.cimdy.coe.data.codec.support.value.Value;
import java.util.Optional;
import com.cimdy.coe.entity.custom.skill.totem.SkillTotem;
import com.cimdy.coe.entity.custom.skill.totem.TrapEntity;
import com.cimdy.coe.data.codec.skill.phase.action.damage.ModifierAction;
import com.cimdy.coe.data.helper.combat.HitScopeHelper;
import com.cimdy.coe.data.codec.support.math.context.nodes.meta.misc.VarRef;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.core.SkillEntry;
import com.cimdy.coe.data.codec.skill.core.SkillRead;
import com.cimdy.coe.data.codec.skill.SkillIds;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceCost;
import com.cimdy.coe.data.codec.skill.helper.cast.SkillCast;
import com.cimdy.coe.data.codec.skill.input.Click;
import com.cimdy.coe.data.codec.skill.phase.SkillPhase;
import com.cimdy.coe.data.codec.skill.phase.SkillPhasePlayer;
import com.cimdy.coe.data.codec.skill.phase.action.support.BannerAction;
import com.cimdy.coe.data.codec.skill.phase.action.movement.ConsumeCorpseAction;
import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;
import com.cimdy.coe.data.codec.skill.phase.action.control.LifecycleTrigger;
import com.cimdy.coe.data.codec.skill.phase.action.movement.SpawnAction;
import com.cimdy.coe.data.codec.skill.phase.action.damage.StatAction;
import com.cimdy.coe.data.codec.skill.phase.action.charge.ChargeAction;
import com.cimdy.coe.data.codec.skill.phase.action.charge.ChargeType;
import com.cimdy.coe.data.codec.skill.phase.action.control.TriggeredAction;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.area.AreaDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.Delivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.SelfDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.WaveDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.area.InstantDuration;
import com.cimdy.coe.data.codec.skill.phase.delivery.area.SphereShape;
import com.cimdy.coe.data.codec.support.require.Require;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.MathParser;
import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.entry.StatGroup;
import com.cimdy.coe.data.gen.stat.StatGen;
import com.cimdy.coe.registry.world.EntityRegistry;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 技能「通用预设」工厂（数据驱动迁移的锚点）。
 *
 * <p>每个预设就是一个<b>完整的 {@link Skill} 定义</b>（与 Stat 侧 {@code StatGen} 同构：先登记、拿 id、
 * 该 id 即可被 {@code SkillRead(ref)} 引用）。预设把机制的可参数化数值写作 {@code $槽}（{@link #slot}），
 * 由实例经 {@code SkillRead.vars} 注入具体值——这是「机制族通用 Read」可行的前提。
 *
 * <h2>两类预设</h2>
 * <ul>
 *   <li><b>共享通用预设</b>（{@code coe:generic_*}，本类静态块构造）：机制内数值可参数化且运行时经
 *       {@code SkillCaster} 通道（施法者 {@code SkillContext.variables}）真读，故可安全用作 {@code $槽}。
 *       覆盖 诅咒 / 印记 / 战旗 / 消耗尸体 / 变形 / 召唤。</li>
 *   <li><b>宝石专属预设</b>（{@link #registerPreset} 在注册期登记）：光环 / 捷等机制的修饰器属性
 *       （{@code ModifierAction.attributeId}）是固定 {@code Identifier}，而 {@code $槽} 是
 *       {@link ExpressionCodec.Value}（<b>数值域</b>），承载不了标识型字段，故只能把属性 id <b>烘焙</b>进预设；
 *       每个宝石一份完整预设，宝石本体降为 {@code SkillRead(ref=coe:preset_<gemname>)} 引用之。</li>
 *   <li><b>更正（2026-09-09 实证）</b>：图腾 ON_TICK <b>可以</b>走 {@code $槽}——{@code SkillTotem} 经
 *       {@code SkillPhaseInstanceTracker} 点燃的是<b>施法时 retain 的同一个 SkillPhaseInstance</b>，其
 *       {@code SkillContext.variables} 由 {@code SkillCaster} 施法时经 {@code skill.effectiveParams()}
 *       注入并持久于 {@code SkillInstance}（见 {@code ExpressionContext.forSkillContext}），故「离场实体」
 *       不影响变量求值。此前注释判定「图腾无法参数化」有误，图腾因此收敛为共享预设 {@code coe:generic_totem}。</li>
 * </ul>
 *
 * <h2>落盘</h2>
 * 全部预设（通用 + 宝石专属）由 CoeDataBootstrap.genSkillTemplate / genSkillPreset 在注册期直接经 registerTemplates / registerPresets 写入技能注册表，运行期由
 * {@code LoadEvent.loadSkill / loadSpirit} 的 {@code putIfAbsent} 进 {@code SKILL_ENTRY_MAP}，
 * 供 {@code SkillRead(ref)} 解析。宝石本体（{@code SkillRead}）同注册表同流程，两者 id 对齐（预设用
 * {@code preset_<gem>} 命名避免与宝石 id 撞车）。
 *
 * <h2>Read 标签发射</h2>
 * 实例经 {@code SkillRead} 引用预设时，{@code SkillRead.resolve()} 把 {@code ref.getPath()}
 * （即预设 id path，如 {@code generic_curse}）并入选中的技能标签，使数据侧「对 [某预设] 类技能」
 * 的加成可命中；机制标签（curse/banner/aura/herald/minion/totem/mark/shapeshifted …）则由预设自身
 * tags + 实例 tags 叠加发射。
 */
public final class SkillPresets {

    private static final Map<Identifier, Skill> PRESETS = new LinkedHashMap<>();

    /**
     * 共享通用预设（{@code generic_*}）落盘注册表：JSON 经 CapturingBootstrap 剥前缀落在
     * {@code data/coe/coe/skill/template}；运行期由 LoadEvent 的 {@code coe/skill} 前缀扫描统一装载。
     */
    public static final ResourceKey<Registry<SkillEntry>> ITEM_TEMPLATE_KEY =
            ResourceKey.createRegistryKey(Identifier.parse("coe/skill/template"));
    /**
     * 宝石专属预设（{@code preset_*}）落盘注册表：JSON 落在 {@code data/coe/coe/skill/preset}；
     * 同被 {@code coe/skill} 前缀扫描覆盖，无需新加载器。
     */
    public static final ResourceKey<Registry<SkillEntry>> ITEM_PRESET_KEY =
            ResourceKey.createRegistryKey(Identifier.parse("coe/skill/preset"));

    private SkillPresets() {
    }

    /** 登记一个预设（共享或宝石专属）。冲突且内容不同则抛异常（与 StatGen.register 同策略）。 */
    public static Skill register(Identifier id, Skill preset) {
        Skill prev = PRESETS.put(id, preset);
        if (prev != null) {
            throw new IllegalStateException("Duplicate skill preset: " + id);
        }
        return preset;
    }

    /** 宝石专属预设登记（光环 / 捷 / 图腾）：在当前宝石注册期调用，把完整 Skill 登记为可被 ref 引用的预设。 */
    public static Skill registerPreset(Identifier id, Skill preset) {
        // 路径进 SkillIds 缓存，使 {@link #read} 的 byName 可取（生成期按路径生成）。
        SkillIds.register(id);
        return register(id, preset);
    }

    /** 全部预设（含注册期追加的宝石专属预设）。由 CoeDataBootstrap.genSkillTemplate/genSkillPreset 经 registerTemplates/registerPresets 落盘。 */
    public static Map<Identifier, Skill> entries() {
        return PRESETS;
    }

    /** 共享通用预设（{@code generic_*}）落盘进 {@link #ITEM_TEMPLATE_KEY}。 */
    public static void registerTemplates(BootstrapContext<SkillEntry> bootstrap) {
        for (Map.Entry<Identifier, Skill> e : PRESETS.entrySet()) {
            if (!e.getKey().getPath().startsWith("generic_")) continue;
            bootstrap.register(ResourceKey.create(ITEM_TEMPLATE_KEY, e.getKey()), e.getValue().withTier(1));
        }
    }

    /** 宝石专属预设（{@code preset_*} 及非 generic 的其余预设）落盘进 {@link #ITEM_PRESET_KEY}。 */
    public static void registerPresets(BootstrapContext<SkillEntry> bootstrap) {
        for (Map.Entry<Identifier, Skill> e : PRESETS.entrySet()) {
            if (e.getKey().getPath().startsWith("generic_")) continue;
            bootstrap.register(ResourceKey.create(ITEM_PRESET_KEY, e.getKey()), e.getValue().withTier(1));
        }
    }

    // ============================ 构造辅助 ============================

    private static Identifier id(String name) {
        return SkillIds.id(name);
    }

    /** 引用型技能（SkillRead）的 ref 构造便捷入口（GemGen 各登记点复用）。 */
    public static SkillRead read(String presetName) {
        return SkillRead.of(SkillIds.byName(presetName));
    }

    /** {@code $槽} 数值：经 MathParser 生成 VarRef 节点，运行期由注入变量真读。 */
    private static ExpressionCodec.Value slot(String name) {
        return ExpressionCodec.of(MathParser.parse("$" + name));
    }

    private static ExpressionCodec.Value of(double v) {
        return ExpressionCodec.of(v);
    }

    /** 图腾实体 id（{@code coe:skill_totem}）：通用图腾预设 {@code coe:generic_totem} 的生成目标。 */
    private static final Identifier TOTEM_ENTITY =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "skill_totem");

    /** 箭塔实体 id（{@code coe:ballista}）：通用箭塔预设 {@code coe:generic_ballista} 的生成目标。 */
    private static final Identifier BALLISTA_ENTITY =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "ballista");

    /** 陷阱实体 id（{@code coe:trap}）：通用陷阱预设 {@code coe:generic_trap} 的生成目标。 */
    private static final Identifier TRAP_ENTITY =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "trap");

    /** 地雷实体 id（{@code coe:mine}）：通用地雷预设 {@code coe:generic_mine} 的生成目标。 */
    private static final Identifier MINE_ENTITY =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "mine");

    /** 裂隙实体 id（{@code coe:fissure}）：通用裂隙预设 {@code coe:generic_fissure} 的生成目标（实体由 EntityRegistry 登记）。 */
    private static final Identifier FISSURE_ENTITY =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "fissure");

    private static final Require EMPTY_REQ = Require.builder().build();

    private static SelfDelivery self() {
        return new SelfDelivery(ExpressionCodec.of(1.0));
    }

    private static AreaDelivery area(double radius) {
        return area(ExpressionCodec.of((float) radius));
    }

    /** 半径走 {@code $槽}（战吼/光环半径随宝石不同）。 */
    private static AreaDelivery area(ExpressionCodec.Value radius) {
        return new AreaDelivery(
                new SphereShape(radius),
                new InstantDuration(),
                ExpressionCodec.of(1.0));
    }

    /** 冲击波投递：沿施法者朝向推出向前推进的球形波前（range 距离内以 speed 格/tick 匀速前进）。 */
    private static WaveDelivery wave(double range, double radius, double speed) {
        return new WaveDelivery(
                ExpressionCodec.of(range), ExpressionCodec.of(radius),
                ExpressionCodec.of(speed), ExpressionCodec.of(1.0),
                Optional.empty());
    }

    private static SkillCast castSpell(double castTime, double cooldown, double crit) {
        return SkillCast.of(new SkillCast.Spell(
                ExpressionCodec.of(castTime), ExpressionCodec.of(cooldown), ExpressionCodec.of(crit)));
    }

    private static SkillCast castAttack(double cooldown) {
        return SkillCast.of(new SkillCast.Attack(
                Either.left(new SkillCast.Attack.WeaponAttack(ExpressionCodec.of(1.0))),
                ExpressionCodec.of(cooldown)));
    }

    private static SkillPhasePlayer phase(Delivery delivery, List<TriggeredAction> actions) {
        return new SkillPhasePlayer(Optional.empty(),
                new SkillPhase(Optional.of(delivery), actions, List.of(), Optional.empty()));
    }

    /** 组装一个内联完整技能（与 SkillFactory.build 同形，但数值走 {@code $槽} 参数）。 */
    private static Skill mk(Require require, SkillCast cast, Delivery delivery,
                            List<TriggeredAction> actions, List<ResourceCost> cost, List<String> tags,
                            Optional<ExpressionCodec.Value> curseRange, Optional<ExpressionCodec.Value> curseDuration,
                            Optional<ExpressionCodec.Value> markRange, Optional<ExpressionCodec.Value> markDuration,
                            Map<String, ExpressionCodec.Value> params) {
        return new Skill(require, "", Optional.empty(), cast, new Click(), cost,
                List.of(phase(delivery, actions)), List.of(), 1, tags,
                Optional.empty(), Skill.AoeRanges.of(curseRange, curseDuration, markRange, markDuration), params,
                Optional.empty(), Optional.empty());
    }

    // ============================ 共享通用预设（静态块构造） ============================

    static {
        buildShared();
    }

    private static void buildShared() {
        // ---- 通用诅咒：瞬发、无自身动作，经 CURSE 标签施放诅咒；范围/时长走 $槽（SkillInstance 带 SkillContext 真读）----
        register(id("generic_curse"),
                mk(EMPTY_REQ, castSpell(0, 0, 0), self(), List.of(), List.of(),
                        List.of(GateTag.CURSE),
                        Optional.of(slot("curse_range")), Optional.of(slot("curse_duration")),
                        Optional.empty(), Optional.empty(),
                        Map.of("curse_range", of(24.0), "curse_duration", of(100.0))).withCastFx(Optional.of(Identifier.fromNamespaceAndPath("minecraft", "block.beacon.activate")), Optional.of(Identifier.fromNamespaceAndPath("minecraft", "reverse_portal"))));

        // ---- 通用冲击波（Wave 子系统）：沿施法者朝向推出一道向前推进的球形波前，
        // 在 range 距离内以 speed 匀速前进、沿途每实体仅命中一次（去重）；波前视觉/音效由
        // PersistentDeliveryManager 的 wave 分支每 tick 经服务端广播（客户端自动渲染），
        // 并随贯穿时间轴 progress 绽放，与客户端施法动画同源同步。----
        register(id("generic_wave"),
                mk(EMPTY_REQ, castSpell(20, 0, 0.05), wave(10.0, 2.5, 0.4),
                        List.of(hitDamage("physical", slot("wave_damage"))), List.of(),
                        List.of("wave", GateTag.AOE),
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                        Map.of("wave_damage", of(0.0)))
                // 代码定义的施放声画身份（权威源，与动画同源）：冲击波 = 柔和小爆 + 风推 whoosh，
                // 经服务端释放节拍广播、客户端自动渲染（缺省回退灵魂火苗 + NOTE_BLOCK_BASS）。
                .withCastFx(Optional.of(Identifier.fromNamespaceAndPath("minecraft", "poof")),
                        Optional.of(Identifier.fromNamespaceAndPath("minecraft", "entity.wind_charge.whoosh"))));

        // ---- 通用印记：范围投递 + 命中伤害；MARK 标签经 SkillInstance 施加标记，范围/时长缺省回退常量 ----
        register(id("generic_mark"),
                mk(EMPTY_REQ, castSpell(20, 0, 0.05), area(2.5),
                        List.of(hitDamage("physical", slot("mark_damage"))), List.of(),
                        List.of(GateTag.MARK),
                        Optional.empty(), Optional.empty(),
                        Optional.of(slot("mark_range")), Optional.of(slot("mark_duration")),
                        Map.of("mark_damage", of(0.0), "mark_range", of(24.0), "mark_duration", of(160.0))).withCastFx(Optional.of(Identifier.fromNamespaceAndPath("minecraft", "block.beacon.activate")), Optional.of(Identifier.fromNamespaceAndPath("minecraft", "reverse_portal"))));

        // ---- 通用战旗：范围投递 + 命中伤害（常量，非战旗可调参）+ ON_CAST 放置战旗云
        // （BannerAction 三项数值均 $槽）----
        register(id("generic_banner"),
                mk(EMPTY_REQ, castSpell(20, 0, 0.05), self(),
                        List.of(new TriggeredAction(LifecycleTrigger.ON_CAST,
                                        new BannerAction(slot("banner_radius"), slot("banner_duration"),
                                                slot("banner_magnitude")))),
                        List.of(),
                        List.of(GateTag.BANNER, GateTag.AURA, GateTag.AOE),
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                        Map.of("banner_radius", of(6.0), "banner_duration", of(6.0), "banner_magnitude", of(1.0))).withCastFx(Optional.of(Identifier.fromNamespaceAndPath("minecraft", "entity.ravager.roar")), Optional.of(Identifier.fromNamespaceAndPath("minecraft", "angry_villager"))));

        // ---- 通用消耗尸体：瞬发 + ON_CAST 消耗 1 具尸体 ----
        register(id("generic_consume_corpse"),
                mk(EMPTY_REQ, castSpell(20, 0, 0.05), self(),
                        List.of(new TriggeredAction(LifecycleTrigger.ON_CAST,
                                new ConsumeCorpseAction(of(1.0)))),
                        List.of(), List.of(),
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                        Map.of()).withCastFx(Optional.of(Identifier.fromNamespaceAndPath("minecraft", "block.anvil.land")), Optional.of(Identifier.fromNamespaceAndPath("minecraft", "poof"))));

        // ---- 通用变形（物理）：范围投递 + 武器攻击；formTag 由实例 tags 注入；伤害走 $槽 ----
        register(id("generic_shapeshift_physical"),
                mk(EMPTY_REQ, castAttack(0), area(2.5),
                        List.of(hitDamage("physical", slot("dmg"))), List.of(),
                        List.of(GateTag.SHAPESHIFTED),
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                        Map.of("dmg", ExpressionCodec.ofSkillLevel(12, 2, 3))).withCastFx(Optional.of(Identifier.fromNamespaceAndPath("minecraft", "block.anvil.land")), Optional.of(Identifier.fromNamespaceAndPath("minecraft", "poof"))));

        // ---- 通用变形（闪电）：同上，伤害类型烘焙为 lightning ----
        register(id("generic_shapeshift_lightning"),
                mk(EMPTY_REQ, castAttack(0), area(2.5),
                        List.of(hitDamage("lightning", slot("dmg"))), List.of(),
                        List.of(GateTag.SHAPESHIFTED),
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                        Map.of("dmg", ExpressionCodec.ofSkillLevel(15, 2, 3))).withCastFx(Optional.of(Identifier.fromNamespaceAndPath("minecraft", "entity.shulker.shoot")), Optional.of(Identifier.fromNamespaceAndPath("minecraft", "electrical_spark"))));

        // ---- 通用变形（物理 + 命中获得怒气）：熊形 Maul 专用，命中即时 +5 Rage（StatAction 烘焙，非 $槽）----
        register(id("generic_shapeshift_physical_rage"),
                mk(EMPTY_REQ, castAttack(0), area(2.5),
                        List.of(hitDamage("physical", slot("dmg")),
                                // 模板条目必须经 ref 第二参注入 $value；否则 $value 未定义会静默回落 0.0，怒气恒为 0。
                                new TriggeredAction(LifecycleTrigger.ON_HIT,
                                        new StatAction(new StatGroup(List.of(StatGen.ref(StatIds.RAGE_ON_HIT, 5))), false, java.util.Optional.empty(), ExpressionCodec.of(0.0)))),
                        List.of(),
                        List.of(GateTag.SHAPESHIFTED),
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                        Map.of("dmg", ExpressionCodec.ofSkillLevel(18, 2, 4))).withCastFx(Optional.of(Identifier.fromNamespaceAndPath("minecraft", "entity.ravager.roar")), Optional.of(Identifier.fromNamespaceAndPath("minecraft", "angry_villager"))));

        // ---- 通用召唤：瞬发 + ON_HIT 生成僵尸；实体烘焙（minecraft:zombie），数量/时长走 $槽 ----
        register(id("generic_summon"),
                mk(EMPTY_REQ, castSpell(20, 60, 0), self(),
                        List.of(new TriggeredAction(LifecycleTrigger.ON_HIT,
                                new SpawnAction(Identifier.parse("minecraft:zombie"),
                                        slot("summon_count"), Optional.of(of(1.5)),
                                        slot("summon_duration"), of(20.0), of(8.0), of(20.0),
                                        Optional.empty()))),
                        List.of(), List.of(GateTag.MINION),
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                        Map.of("summon_count", of(2.0), "summon_duration", of(600.0))).withCastFx(Optional.of(Identifier.fromNamespaceAndPath("minecraft", "item.totem.use")), Optional.of(Identifier.fromNamespaceAndPath("minecraft", "totem_of_undying"))));

        // ---- 通用图腾：ON_HIT 立起驻场图腾（实体烘焙 coe:skill_totem）+ ON_TICK 周期攻击 ----
        // 数值全走 $槽：duration / interval / attack_range / health / damage。
        // ON_TICK 由 SkillTotem 点燃同一 SkillPhaseInstance，变量上下文仍为施法时注入的
        // SkillContext.variables（见类注释「更正」），故 $槽在离场实体上同样真读。
        register(id("generic_totem"),
                mk(EMPTY_REQ, castSpell(20, 40, 0.05), self(),
                        List.of(new TriggeredAction(LifecycleTrigger.ON_CAST,
                                        // 放置图腾的充能消耗豁免：本 ChargeAction 携带上下文标签 ["totem"]，
                                        // 与 EFF_CHARGE_COST_EXEMPT 的 conditionTags ["totem"] 求交命中 →
                                        // 消耗充能时不实际扣球但仍计为已消耗（ChargedState#consume 的豁免分支）。
                                        new ChargeAction(ChargeType.ENDURANCE, ChargeAction.ChargeActionMode.CONSUME,
                                                ExpressionCodec.of(1.0), Optional.of(ExpressionCodec.of(1.0)),
                                                Optional.empty(), Optional.empty(), false, List.of("totem"))),
                                new TriggeredAction(LifecycleTrigger.ON_HIT,
                                        new SpawnAction(TOTEM_ENTITY, of(1.0), Optional.empty(),
                                                slot("totem_duration"), slot("totem_interval"),
                                                slot("totem_range"), slot("totem_health"),
                                                Optional.empty())),
                                new TriggeredAction(LifecycleTrigger.ON_TICK,
                                        new DamageAction("physical", slot("totem_damage"),
                                                Optional.empty(), of(1.0)))),
                        List.of(), List.of(GateTag.TOTEM),
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                        Map.of("totem_duration", of(300.0), "totem_interval", of(20.0),
                                "totem_range", of(8.0), "totem_health", of(20.0),
                                "totem_damage", of(0.0))).withCastFx(Optional.of(Identifier.fromNamespaceAndPath("minecraft", "block.bell.use")), Optional.of(Identifier.fromNamespaceAndPath("minecraft", "note"))));

        // ---- 通用箭塔（物理）：克隆 generic_totem，仅生成目标改为 coe:ballista ----
        // 实体类型不同 → 命中作用域经 HitScopeHelper 额外注入 ballista 标签（点亮 5 条 SINGLE-{ballista} 效果）；
        // 其余（驻场施法 / 周期索敌 / ON_TICK 物理伤害 / $槽调参）与通用图腾一致，故复用 totem_* 槽名。
        // 预设自身带 TOTEM 标签（与 generic_totem 相同）；箭塔攻击再由 HitScopeHelper 叠加 BALLISTA 标签。
        register(id("generic_ballista"),
                mk(EMPTY_REQ, castSpell(20, 40, 0.05), self(),
                        List.of(new TriggeredAction(LifecycleTrigger.ON_HIT,
                                        new SpawnAction(BALLISTA_ENTITY, of(1.0), Optional.empty(),
                                                slot("totem_duration"), slot("totem_interval"),
                                                slot("totem_range"), slot("totem_health"),
                                                Optional.empty())),
                                new TriggeredAction(LifecycleTrigger.ON_TICK,
                                        new DamageAction("physical", slot("totem_damage"),
                                                Optional.empty(), of(1.0)))),
                        List.of(), List.of(GateTag.TOTEM),
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                        Map.of("totem_duration", of(300.0), "totem_interval", of(20.0),
                                "totem_range", of(8.0), "totem_health", of(20.0),
                                "totem_damage", of(0.0))).withCastFx(Optional.of(Identifier.fromNamespaceAndPath("minecraft", "block.bell.use")), Optional.of(Identifier.fromNamespaceAndPath("minecraft", "note"))));

        // ---- 通用陷阱（物理）：克隆 generic_totem，仅生成目标改为 coe:trap ----
        // 实体类型不同 → 命中作用域经 HitScopeHelper 额外注入 trap 标签（点亮 3 条 SINGLE-{trap} 效果：
        //   coe:damage / coe:crit_chance / coe:cast_speed）；其余（驻场施法 / 周期索敌 / ON_TICK 物理伤害 /
        //   $槽调参）与通用图腾一致，故复用 totem_* 槽名。
        // 陷阱为「一次性邻近触发」（TrapEntity.tick 在 proximity 命中后 fireTrigger(ON_TICK)+discard），
        // 预设自身带 TOTEM 标签（与 generic_totem 相同）；陷阱攻击再由 HitScopeHelper 叠加 TRAP 标签。
        register(id("generic_trap"),
                mk(EMPTY_REQ, castSpell(20, 40, 0.05), self(),
                        List.of(new TriggeredAction(LifecycleTrigger.ON_HIT,
                                        new SpawnAction(TRAP_ENTITY, of(1.0), Optional.empty(),
                                                slot("totem_duration"), slot("totem_interval"),
                                                slot("totem_range"), slot("totem_health"),
                                                Optional.empty())),
                                new TriggeredAction(LifecycleTrigger.ON_TICK,
                                        new DamageAction("physical", slot("totem_damage"),
                                                Optional.empty(), of(1.0)))),
                        List.of(), List.of(GateTag.TOTEM),
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                        Map.of("totem_duration", of(300.0), "totem_interval", of(20.0),
                                "totem_range", of(8.0), "totem_health", of(20.0),
                                "totem_damage", of(0.0))).withCastFx(Optional.of(Identifier.fromNamespaceAndPath("minecraft", "block.bell.use")), Optional.of(Identifier.fromNamespaceAndPath("minecraft", "note"))));

        // ---- 通用地雷（物理）：克隆 generic_trap，仅生成目标改为 coe:mine ----
        // 实体类型不同 → 命中作用域经 HitScopeHelper 额外注入 mine 标签（点亮 SINGLE-{mine} 效果）；
        // 其余（驻场施法 / 邻近引爆 / ON_TICK 物理伤害 / $槽调参）与通用陷阱一致，故复用 totem_* 槽名。
        // 地雷为「一次性邻近引爆」（MineEntity.tick 在 proximity 命中后 fireTrigger(ON_TICK)+discard），
        // 预设自身带 MINE 标签（与 generic_totem 的 TOTEM 区分，避免误命中彼此 SINGLE 效果）。
        register(id("generic_mine"),
                mk(EMPTY_REQ, castSpell(20, 40, 0.05), self(),
                        List.of(new TriggeredAction(LifecycleTrigger.ON_HIT,
                                        new SpawnAction(MINE_ENTITY, of(1.0), Optional.empty(),
                                                slot("totem_duration"), slot("totem_interval"),
                                                slot("totem_range"), slot("totem_health"),
                                                Optional.empty())),
                                new TriggeredAction(LifecycleTrigger.ON_TICK,
                                        new DamageAction("physical", slot("totem_damage"),
                                                Optional.empty(), of(1.0)))),
                        List.of(), List.of(GateTag.MINE),
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                        Map.of("totem_duration", of(300.0), "totem_interval", of(20.0),
                                "totem_range", of(8.0), "totem_health", of(20.0),
                                "totem_damage", of(0.0))).withCastFx(Optional.of(Identifier.fromNamespaceAndPath("minecraft", "block.bell.use")), Optional.of(Identifier.fromNamespaceAndPath("minecraft", "note"))));

        // ---- 通用裂隙（物理）：克隆通用图腾的「驻场实体 + ON_TICK 周期伤害」结构，仅生成目标改为 coe:fissure ----
        // 技能标签为字符串 "fissure"（无 GateTag.FISSURE —— 用字面量，与 EntityCategoryLimit.FISSURE.getPath() 同源），
        // 使本预设生成的实体进入 fissure 类别池（点亮 MAX_FISSURE 上限 + 额外生成 EFF）。数值复用 totem_* 同形 $槽。
        // 实体 coe:fissure 由同级 EntityRegistry 登记，本处仅引用其 Identifier；预设自身不挂 TOTEM 标签。
        register(id("generic_fissure"),
                mk(EMPTY_REQ, castSpell(20, 40, 0.05), self(),
                        List.of(new TriggeredAction(LifecycleTrigger.ON_HIT,
                                        new SpawnAction(FISSURE_ENTITY, of(1.0), Optional.empty(),
                                                slot("fissure_duration"), slot("fissure_interval"),
                                                slot("fissure_range"), slot("fissure_health"),
                                                Optional.empty())),
                                new TriggeredAction(LifecycleTrigger.ON_TICK,
                                        new DamageAction("physical", slot("fissure_damage"),
                                                Optional.empty(), of(1.0)))),
                        List.of(), List.of("fissure"),
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                        Map.of("fissure_duration", of(300.0), "fissure_interval", of(20.0),
                                "fissure_range", of(6.0), "fissure_health", of(20.0),
                                "fissure_damage", of(0.0))).withCastFx(Optional.of(Identifier.fromNamespaceAndPath("minecraft", "block.bell.use")), Optional.of(Identifier.fromNamespaceAndPath("minecraft", "note"))));

        // ---- 通用战吼（物理）：范围投递 + 命中物理伤害；半径 / 伤害 / 击退走 $槽 ----
        // damageType 是 String（标识型），族内一致故烘焙；火焰战吼另立 generic_warcry_fire。
        register(id("generic_warcry"),
                mk(EMPTY_REQ, castSpell(20, 0, 0.05), area(slot("warcry_radius")),
                        List.of(new TriggeredAction(LifecycleTrigger.ON_HIT,
                                new DamageAction("physical", slot("warcry_damage"),
                                        Optional.of(slot("warcry_knockback")), of(1.0)))),
                        List.of(), List.of(GateTag.WARCRY),
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                        Map.of("warcry_radius", of(3.5), "warcry_damage", of(0.0),
                                "warcry_knockback", of(0.0))).withCastFx(Optional.of(Identifier.fromNamespaceAndPath("minecraft", "entity.ravager.roar")), Optional.of(Identifier.fromNamespaceAndPath("minecraft", "angry_villager"))));

        // ---- 通用战吼（火焰）：同物理族，damageType 烘焙为 fire（Infernal Cry）----
        register(id("generic_warcry_fire"),
                mk(EMPTY_REQ, castSpell(20, 0, 0.05), area(slot("warcry_radius")),
                        List.of(new TriggeredAction(LifecycleTrigger.ON_HIT,
                                new DamageAction("fire", slot("warcry_damage"),
                                        Optional.empty(), of(1.0)))),
                        List.of(), List.of(GateTag.WARCRY),
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                        Map.of("warcry_radius", of(3.5), "warcry_damage", of(0.0))).withCastFx(Optional.of(Identifier.fromNamespaceAndPath("minecraft", "entity.ravager.roar")), Optional.of(Identifier.fromNamespaceAndPath("minecraft", "small_flame"))));
    }

    // ============================ 动作便捷构造 ============================

    private static TriggeredAction hitDamage(String damageType, ExpressionCodec.Value amount) {
        return new TriggeredAction(LifecycleTrigger.ON_HIT,
                new DamageAction(damageType, amount, Optional.empty(), ExpressionCodec.of(1.0)));
    }
}
