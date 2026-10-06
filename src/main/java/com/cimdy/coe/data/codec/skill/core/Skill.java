package com.cimdy.coe.data.codec.skill.core;

import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.runtime.SkillTriggerHelper;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.data.gen.stat.wirings.wire.attribute.WireDamage;
import com.cimdy.coe.data.gen.stat.wirings.wire.resource.WireResource;
import com.cimdy.coe.data.codec.skill.modifier.impl.effect.EventListenEffect;
import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;
import com.cimdy.coe.data.helper.hub.SpiritManager;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.gem.SkillData;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceCost;
import com.cimdy.coe.data.codec.skill.helper.cast.SkillCast;
import com.cimdy.coe.data.codec.skill.input.Click;
import com.cimdy.coe.data.codec.skill.input.Continuous;
import com.cimdy.coe.data.codec.skill.input.HoldInput;
import com.cimdy.coe.data.codec.skill.input.InputMode;
import com.cimdy.coe.data.codec.skill.modifier.SkillModifier;
import com.cimdy.coe.data.codec.skill.phase.SkillPhase;
import com.cimdy.coe.data.codec.skill.phase.SkillPhasePlayer;
import com.cimdy.coe.data.codec.skill.phase.delivery.Delivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.area.HazardDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.area.RemnantDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.BallDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.OrbDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.ProjectileDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.ThrownDelivery;
import com.cimdy.coe.data.codec.skill.phase.action.control.TriggeredAction;
import com.cimdy.coe.data.codec.skill.phase.action.MetaSkillAction;
import com.cimdy.coe.data.codec.skill.phase.event.EventListenPhase;
import com.cimdy.coe.data.codec.support.require.Require;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.cimdy.coe.data.helper.affix.ModHelper;
import com.cimdy.coe.data.codec.item.ItemTooltipAssembler;
import com.cimdy.coe.network.skill.CastAnimationPacket;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;
import com.cimdy.coe.data.codec.skill.SkillIds;

public record Skill(
        Require require,
        String equipment,
        Optional<Identifier> animation,
        SkillCast cast,
        InputMode input,
        List<ResourceCost> cost,
        List<SkillPhasePlayer> phases,
        List<SkillModifier> modifiers,
        int tier,
        List<String> tags,
        // 技能形态（Totem/Trap/Mine 等 PoE2 形态骨架）：数据驱动、Optional。
        // 落盘于 JSON 的 "form" 字段；装配期经 SkillLinkResolver 注入为技能级 stat 标签，
        // 使 allow_type 表达式可针对形态（如 "totem"）判定兼容性。详见 SkillForm。
        Optional<String> form,
        // 范围型技能（诅咒 / 标记）的范围与时长：原 4 个独立 Optional（curse_range / curse_duration /
        // mark_range / mark_duration）合并为 aoeRanges，释放 record 字段额度给施放声画身份
        // （cast_sound / cast_particle）。委托 getter {@link #curseRange()} 等保持既有调用点不变。
        Optional<AoeRanges> aoeRanges,
        /**
         * 定义期参数槽（{@code $name} → 默认值）：本技能全部 {@link ExpressionCodec.Value}
         * （保留值 / 范围半径 / 时长 / 倍率 …）中出现的 {@code $name} 在此声明默认值，
         * 由 {@link SkillRead#vars} 在引用时覆盖（实例值 &gt; 模板默认值）。
         * <p>本 record 即「一个完整的技能定义」——注册后拿到 id，该 id 就是一个可被
         * {@code SkillRead(ref, vars)} 引用的<b>预设</b>；不存在独立的「技能类型」注册表。
         */
        Map<String, ExpressionCodec.Value> params,
        // 施放标志反馈（音效 / 粒子）：技能级声明，经服务端「释放节拍」(SkillInstance#emitReleaseBeat)
        // 广播，客户端自动渲染——使「技能声画身份」成为代码定义的权威源（与 animation 同源），
        // 缺省回退灵魂火苗粒子 + NOTE_BLOCK_BASS 音。A1/A2 贯穿时间轴释放节拍据此播出。
        Optional<Identifier> castSound,
        Optional<Identifier> castParticle,
        /**
         * 本技能<b>是否可生成技能石（gem）</b>：可空，{@code 空} 默认为 {@code true}（大多数技能都会
         * 被刻印成技能石物品）。显式置 {@code false} 用于<b>不应生成技能石</b>的技能——
         * 物品授予技（item-granted）、怪物/召唤物天生技能、升华与被动授予技等，它们由
         * {@code SkillIds} + {@code SkillRead} 直接引用施放，不以宝石物品形态存在。
         * 经 {@link #canGenerateGem()} 读取（空 → true）。
         */
        Optional<Boolean> gemmable
) implements SkillEntry {

    /**
     * 便捷构造（15 参）：省略 {@code gemmable} → {@code Optional.empty()}（即默认「可生成技能石」）。
     * 使全部既有调用点（{@code with*} 副本与 datagen 工厂）无需为新增维度改签名，避免全仓补丁式改动。
     */
    public Skill(Require require, String equipment, Optional<Identifier> animation, SkillCast cast,
                 InputMode input, List<ResourceCost> cost, List<SkillPhasePlayer> phases,
                 List<SkillModifier> modifiers, int tier, List<String> tags, Optional<String> form,
                 Optional<AoeRanges> aoeRanges, Map<String, ExpressionCodec.Value> params,
                 Optional<Identifier> castSound, Optional<Identifier> castParticle) {
        this(require, equipment, animation, cast, input, cost, phases, modifiers, tier, tags, form,
                aoeRanges, params, castSound, castParticle, Optional.empty());
    }
    public static final SkillData EMPTY =
            SkillData.builder(CraftOfExile.EMPTY_LOCATION).build();

    /**
     * 范围型技能（诅咒 / 标记）的范围与时长声明。
     * 原 Skill 把 curse_range / curse_duration / mark_range / mark_duration 拆成 4 个独立 Optional 字段，
     * 已占满 RecordCodecBuilder 的 16 字段上限；合并为单个 {@code aoeRanges} 释放 record 字段额度，
     * 供施放声画身份（cast_sound / cast_particle）使用。序列化 key 为 {@code aoe_ranges} 内嵌对象。
     * <p>声明于 MAP_CODEC 之前，避免前向引用导致编译器将 AoeRanges.CODEC 误解析为外层 Skill.CODEC。
     */
    public record AoeRanges(
            Optional<ExpressionCodec.Value> curseRange,
            Optional<ExpressionCodec.Value> curseDuration,
            Optional<ExpressionCodec.Value> markRange,
            Optional<ExpressionCodec.Value> markDuration
    ) {
        public static final Codec<AoeRanges> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExpressionCodec.VALUE_CODEC.optionalFieldOf("curse_range").forGetter(AoeRanges::curseRange),
                ExpressionCodec.VALUE_CODEC.optionalFieldOf("curse_duration").forGetter(AoeRanges::curseDuration),
                ExpressionCodec.VALUE_CODEC.optionalFieldOf("mark_range").forGetter(AoeRanges::markRange),
                ExpressionCodec.VALUE_CODEC.optionalFieldOf("mark_duration").forGetter(AoeRanges::markDuration)
        ).apply(instance, AoeRanges::new));

        /** 由 4 个范围字段构造一个内嵌 AoeRanges（旧 SkillPresets.mk 接口经此桥接，无需改调用方）。 */
        public static Optional<AoeRanges> of(
                Optional<ExpressionCodec.Value> curseRange,
                Optional<ExpressionCodec.Value> curseDuration,
                Optional<ExpressionCodec.Value> markRange,
                Optional<ExpressionCodec.Value> markDuration) {
            return Optional.of(new AoeRanges(curseRange, curseDuration, markRange, markDuration));
        }
    }

    public static final MapCodec<Skill> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Require.CODEC.fieldOf("require").forGetter(Skill::require),
            Codec.STRING.optionalFieldOf("equipment", "").forGetter(Skill::equipment),
            Identifier.CODEC.optionalFieldOf("animation").forGetter(Skill::animation),
            SkillCast.CODEC.fieldOf("cast").forGetter(Skill::cast),
            InputMode.CODEC.optionalFieldOf("input", new Click()).forGetter(Skill::input),
            ResourceCost.CODEC.listOf().fieldOf("cost").forGetter(Skill::cost),
            SkillPhasePlayer.CODEC.listOf().fieldOf("phases").forGetter(Skill::phases),
            SkillModifier.CODEC.listOf().optionalFieldOf("modifiers", List.of()).forGetter(Skill::modifiers),
            Codec.INT.optionalFieldOf("tier", 1).forGetter(Skill::tier),
            Codec.STRING.listOf().optionalFieldOf("tags", List.of()).forGetter(Skill::tags),
            Codec.STRING.optionalFieldOf("form").forGetter(Skill::form),
            AoeRanges.CODEC.optionalFieldOf("aoe_ranges").forGetter(Skill::aoeRanges),
            Codec.unboundedMap(Codec.STRING, ExpressionCodec.VALUE_CODEC)
                    .optionalFieldOf("params", Map.of()).forGetter(Skill::params),
            Identifier.CODEC.optionalFieldOf("cast_sound").forGetter(Skill::castSound),
            Identifier.CODEC.optionalFieldOf("cast_particle").forGetter(Skill::castParticle),
            Codec.BOOL.optionalFieldOf("gemmable").forGetter(Skill::gemmable)
    ).apply(instance, Skill::new));

    /**
     * 本技能是否可生成技能石（可空 → 默认 {@code true}）。
     *
     * <p>{@code false} 用于「不以宝石物品形态存在」的技能：物品授予技、怪物/召唤物天生技能、
     * 升华与被动授予技等 —— 它们经 {@code SkillIds} + {@code SkillRead} 引用施放。
     */
    public boolean canGenerateGem() {
        return gemmable.orElse(true);
    }

    public static final Codec<Skill> CODEC = MAP_CODEC.codec();

    // ---- 委托 getter：保持既有 skill.curseRange() 等调用点（SkillInstance / SkillLinkResolver）不变 ----
    public Optional<ExpressionCodec.Value> curseRange() {
        return aoeRanges.flatMap(AoeRanges::curseRange);
    }
    public Optional<ExpressionCodec.Value> curseDuration() {
        return aoeRanges.flatMap(AoeRanges::curseDuration);
    }
    public Optional<ExpressionCodec.Value> markRange() {
        return aoeRanges.flatMap(AoeRanges::markRange);
    }
    public Optional<ExpressionCodec.Value> markDuration() {
        return aoeRanges.flatMap(AoeRanges::markDuration);
    }

    // ---------------------------------------------------------------- SkillEntry 形态契约

    /** 内联形态：自身即完整技能定义，解析结果就是 {@code this}。 */
    @Override
    public Skill resolve() {
        return this;
    }

    /** 返回叠加参数后的副本（定义 params ⊕ newParams，newParams 优先）。 */
    @Override
    public Skill withParams(Map<String, ExpressionCodec.Value> newParams) {
        if (newParams == null || newParams.isEmpty()) return this;
        Map<String, ExpressionCodec.Value> merged = new HashMap<>(this.params);
        merged.putAll(newParams);
        return new Skill(require, equipment, animation, cast, input, cost, phases, modifiers, tier, tags,
                Optional.empty(), aoeRanges, merged,
                castSound, castParticle);
    }

    @Override
    public SkillEntry.Type<Skill> getType() {
        return SkillEntries.INLINE;
    }

    /** 返回带指定等阶的副本（等阶仅供未切割宝石筛选，不影响运行时结算）。 */
    @Override
    public Skill withTier(int tier) {
        return new Skill(require, equipment, animation, cast, input, cost, phases, modifiers, tier, tags,
                Optional.empty(), aoeRanges, params,
                castSound, castParticle);
    }

    /** 返回覆盖需求（require）的副本——供 {@link SkillRead} 在解析时注入实例自身的需求。 */
    public Skill withRequire(Require require) {
        return new Skill(require, equipment, animation, cast, input, cost, phases, modifiers, tier, tags,
                Optional.empty(), aoeRanges, params,
                castSound, castParticle);
    }

    /** 返回覆盖资源消耗（cost）的副本——供 {@link SkillRead} 在解析时注入实例自身的消耗。 */
    public Skill withCost(List<ResourceCost> cost) {
        return new Skill(require, equipment, animation, cast, input, cost, phases, modifiers, tier, tags,
                Optional.empty(), aoeRanges, params,
                castSound, castParticle);
    }

    /** 返回追加指定「技能类型标签」（如 {@code mark}/{@code herald}/{@code curse}/{@code warcry}）后的副本，
     * 这些标签经 {@link #getLabels} 同时进入技能提示与 {@link #getStatTags}，供「对 [X] 类技能」加成命中。 */
    public Skill withTags(String... extra) {
        List<String> merged = new ArrayList<>(tags);
        for (String t : extra) {
            if (!merged.contains(t)) merged.add(t);
        }
        return new Skill(require, equipment, animation, cast, input, cost, phases, modifiers, tier, merged,
                Optional.empty(), aoeRanges, params,
                castSound, castParticle);
    }

    /**
     * 返回覆盖施放声效 / 粒子声画身份的副本——供预设声明本技能的标志性施放反馈
     * （服务端释放节拍广播、客户端自动渲染），使「技能声画身份」成为代码定义的权威源，与 {@link #animation} 同源。
     * 详见 SkillInstance#emitReleaseBeat。
     */
    public Skill withCastFx(Optional<Identifier> castSound, Optional<Identifier> castParticle) {
        return new Skill(require, equipment, animation, cast, input, cost, phases, modifiers, tier, tags,
                form, aoeRanges, params,
                castSound, castParticle);
    }

    /**
     * 返回把本技能<b>所有实体投递</b>的「外观引用」覆盖为技能 id 的副本（技能皮肤机制）。
     *
     * <p>递归处理每个阶段自身的 {@code delivery} 与其全部子阶段的 {@code delivery}，
     * 对其调用对应投递类型的 {@code withRender(skillId)}；其余字段（动作 / 子阶段 / 事件 /
     * 门槛 / 锁定 / 声画 / 形态 …）全部原样保留。
     *
     * <p>装配期（{@link com.cimdy.coe.data.codec.entity.EntityRead#resolve}）会经
     * {@code SkillSkinDefs.resolveRender} 把这个技能 id 还原为底层渲染定义，
     * 从而使「外观」按<b>技能 id</b>锁定，而非按伤害类型派生。</p>
     */
    public Skill withDeliveryRender(Identifier skillId) {
        List<SkillPhasePlayer> newPhases = new ArrayList<>();
        for (SkillPhasePlayer player : phases) {
            newPhases.add(player.withPhase(withDeliveryPhase(player.phase(), skillId)));
        }
        return new Skill(require, equipment, animation, cast, input, cost, newPhases, modifiers, tier, tags,
                form, aoeRanges, params, castSound, castParticle, gemmable);
    }

    /** 递归重建阶段树，把阶段自身投递也做 withRender 覆盖。 */
    private static SkillPhase withDeliveryPhase(SkillPhase phase, Identifier skillId) {
        Optional<Delivery> newDelivery = phase.delivery().map(d -> withDeliveryType(d, skillId));
        List<SkillPhasePlayer> newChildren = new ArrayList<>();
        for (SkillPhasePlayer child : phase.children()) {
            newChildren.add(child.withPhase(withDeliveryPhase(child.phase(), skillId)));
        }
        return new SkillPhase(newDelivery, phase.actions(), newChildren, phase.eventListen(),
                phase.minLevel(), phase.lockedFields());
    }

    /** 按投递子类型重建为带 render 覆盖的副本；未知类型原样返回。 */
    private static Delivery withDeliveryType(Delivery d, Identifier skillId) {
        if (d instanceof ProjectileDelivery p) return p.withRender(skillId);
        if (d instanceof BallDelivery b) return b.withRender(skillId);
        if (d instanceof OrbDelivery o) return o.withRender(skillId);
        if (d instanceof ThrownDelivery t) return t.withRender(skillId);
        if (d instanceof HazardDelivery h) return h.withRender(skillId);
        if (d instanceof RemnantDelivery r) return r.withRender(skillId);
        return d;
    }

    public boolean canCast(SkillContext context) {
        if (!require.matches(context)) return false;
        return canCastIgnoringRequire(context);
    }

    /**
     * 与 {@link #canCast} 相同，但<b>跳过宝石佩戴需求 {@link #require}</b>。
     *
     * <p>{@code require} 是「技能石作为物品被玩家佩戴」的语义（等级 / 力量 / 敏捷 / 智慧门槛），
     * 实体并不持有技能石——词缀赋予的技能是天生能力，不受该门控约束（POE2 怪物施放技能亦无属性需求）。
     * 技能自身的装备需求（{@link #equipment}）与资源消耗（{@link #cost}）<b>仍然生效</b>：
     * 实体仍需手持对应武器、并支付魔力 / 生命等消耗。
     * 供 {@link SkillCaster#castGranted}（怪物 AI / 召唤物）使用。
     */
    public boolean canCastIgnoringRequire(SkillContext context) {
        if (!equipment.isEmpty() && !isCurrentWeapon(context)) return false;
        return cost.isEmpty() || cost.stream().allMatch(cost -> cost.matches(context));
    }

    public boolean matches(SkillContext context) {
        if (!require.matches(context)) return false;
        if (!equipment.isEmpty() && !isCurrentWeapon(context)) return false;
        if (!cast.matches(context)) return false;
        return cost.isEmpty() || cost.stream().allMatch(cost -> cost.matches(context));
    }

    public List<SkillPhaseInstance> run(SkillInstance skillInstance) {
        SkillContext context = skillInstance.getContext();
        LivingEntity caster = context.getCaster();
        List<SkillPhaseInstance> instances = new ArrayList<>();
        // HoldInput（长按蓄力）门控：按住期间只蓄力不施放，松手且达到最低蓄力才释放
        if (input instanceof HoldInput hold) {
            if (context.isPress()) return instances;
            float minCharge = hold.minCharge().map(v -> v.getFloat(context)).orElse(0f);
            if (context.getChargeAmount() < minCharge) return instances;
        }
        if (matches(context)) {
            // 重入防护：每个顶层阶段仅初始化一次，避免每 tick 重复生成投递物（投射物/范围/光束/光环）
            Set<SkillPhase> initialized = skillInstance.getInitializedPhases();
            for (SkillPhasePlayer phase : phases) {
                if (initialized.contains(phase.phase())) continue;
                List<SkillPhaseInstance> insts = phase.run(skillInstance, null);
                instances.addAll(insts);
                if (!insts.isEmpty()) initialized.add(phase.phase());
            }
            if (!instances.isEmpty()) {
                caster.stopUsingItem();
                // 作用域化冷却回复速率：并入本次施放技能标签（{command}/{warcry} …），
                // 使「对 [某类] 技能」冷却回复生效（SkillContext 据此读 COOLDOWN_RECOVERY_RATE 作用域变体）。
                context.skillStatTags(this.getStatTags(context, skillInstance.getSkillId()));
                cast.setCooldown(context);
                cost.forEach(cast -> cast.runCost(context));
                // ④ 附近施法额外放电：玩家在 Orb of Storms 作用半径内施放 → 球额外放电一次。
                // caster 仍是玩家、prop 为球（不切换施法者主体）；由数据驱动的 on_nearby_cast 通道实现。
                SkillEntity.triggerNearbyOrbsOnCast(caster);
            }
        }

        return instances;
    }

    public boolean isCurrentWeapon(SkillContext context) {
        LivingEntity caster = context.getCaster();
        return ModHelper.currentEquipment(
                caster, caster.getMainHandItem(), equipment);
    }

    public Set<String> getLabels(SkillContext context) {
        Set<String> labels = new LinkedHashSet<>(cast.getLabels(context));
        labels.addAll(phases.stream().flatMap(s -> s.getLabels(context).stream()).toList());
        labels.addAll(tags);
        return labels;
    }

    /**
     * 技能标签系统：返回本次技能行为应携带的「技能级」stat 标签（字符串集合）。
     * 这些标签对整个技能全程生效（不随阶段变化）：
     * <ul>
     *   <li>技能名（{@code skillId.getPath()}）——数据侧 stat 直接写该名字即可命中，无需登记枚举；</li>
     *   <li>攻击/法术（{@code attack}/{@code spell}）+ 技能类型（{@code warcry}/{@code curse} …），来自既有 labels 体系；</li>
     *   <li>武器装备需求（{@code equipment} 字符串原样，如 {@code bow}，无前缀）；</li>
     *   <li>消耗类型（资源类型序列化名原样，如 {@code mana}/{@code energy}/{@code spirit}/{@code health}）。</li>
     * </ul>
     * 与阶段相关的标签（投放形态 projectile/area/beam/aura/self、行为自述元素 fire/cold/…）
     * 不在此处——它们由 {@link SkillStatScopes#enterAction} 在「每个动作」执行时
     * 按当前阶段递归并入（见 {@code Delivery.statTags()} / {@code Action.statTags()}），
     * 从而保证一段近战、二段投射物的技能里，「投射物伤害提高」只作用于投射物阶段、不会串到近战阶段。
     * <p>
     * 标签不存储，全部由技能结构（labels/equipment/cost/技能名）现取；阶段级标签则由阶段子树递归取得。
     */
    public Set<String> getStatTags(SkillContext context, Identifier skillId) {
        Set<String> tags = new LinkedHashSet<>();
        // 技能名直接作为 tag（与 ModifierStatEffect 的 tags 直接对应）
        if (skillId != null) tags.add(skillId.getPath());
        // 攻击/法术 + 技能类型标签（warcry/curse/...）来自既有 labels 体系
        tags.addAll(getLabels(context));
        // 武器装备需求（字符串原样，无 equipment_ 前缀）
        if (!equipment.isEmpty()) tags.add(equipment);
        // 消耗类型（资源类型名原样，无 cost_ 前缀）
        for (ResourceCost c : cost) tags.add(c.type().getSerializedName());
        // 引导标签：施放需要长按的技能（HoldInput 蓄力 / Continuous 持续释放）全程携带 channeling 标签，
        // 使「[Channelling] 类」天赋加成仅在引导该技能期间生效（引导是技能标签，非实体状态）。
        if (input instanceof HoldInput || input instanceof Continuous) tags.add(GateTag.CHANNELING);
        // 被触发标签：SkillTriggerHelper 用虚拟绑定槽 TRIGGER_BIND 施放被触发的技能，
        // 故 bind == TRIGGER_BIND 即「本次施放是被触发的」，全程附加 triggered 标签，
        // 使「Triggered Spells deal X」只对被触发的施放生效、不串到玩家主动施放。
        if (context.getBind() == SkillTriggerHelper.TRIGGER_BIND) tags.add(GateTag.TRIGGERED);
        // 元技能标签：技能阶段树含 MetaSkillAction（ActionTypes.META_SKILL）即为元技能宿主，全程携带 meta 标签。
        // 这是「Meta Skills have X% increased …」「META_ENERGY_MAX 上限」类已接线词缀的唯一作用域发射点——
        // 缺此前 GateTag.META 零发射点，导致 6 条已接线条目（WireDamage:129、WireResource:105/107/109/274/292）恒不匹配、全为死线。
        if (isMetaSkill()) tags.add(GateTag.META);
        return tags;
    }

    /**
     * 元技能（Meta）宿主判定：本技能的阶段树中是否挂有 {@link MetaSkillAction}（动作类型 {@code ActionTypes.META_SKILL}）。
     *
     * <p><b>判据（不用「技能 id 含 meta」之类脆弱启发式）</b>：POE2 的「Cast on Crit / Cast on Ignite」等元技能
     * 是<b>辅助宝石</b>而非独立技能——{@code SupportGemFactory.castOn} 把 {@code MetaSkillAction} 包进
     * {@link EventListenPhase}（经 {@code EventListenEffect}），由 {@code SkillLinkResolver.applyEventListens}
     * 注入到<b>被辅助技能</b>每个顶层阶段的 {@code eventListen}。故「某技能是不是元技能宿主」等价于
     * 「它的阶段树里有没有 MetaSkillAction」。
     *
     * <p><b>为什么只能是技能级标签</b>：{@code MetaSkillAction} 在 EventListenPhase 的事件回调里执行
     * （直接 {@code execute}，不自建动作作用域），其写入的元技能能量上限（{@code META_ENERGY_MAX}）读取
     * 依赖宿主技能整段施放的作用域 {@link SkillStatScopes#enter}；「Meta Skills have X% increased …」
     * 类词缀同样是对整个技能的门控，而不是某一阶段。阶段级 {@code enterAction} 无法覆盖这两类读取，
     * 因此必须在 {@link #getStatTags} 以技能级发射，对整个技能全程生效。
     */
    public boolean isMetaSkill() {
        // 未解析技能（精魂保留计算走此路径，见 SpiritManager）：辅助宝石效果尚留在 modifiers 中
        for (SkillModifier modifier : modifiers) {
            if (modifier instanceof EventListenEffect listen
                    && containsMetaAction(listen.eventListen().actions())) return true;
        }
        // 已解析技能：MetaSkillAction 已注入各顶层阶段的 actions / eventListen，并可能深埋于子阶段
        for (SkillPhasePlayer player : phases) {
            if (containsMetaAction(player.phase())) return true;
        }
        return false;
    }

    /** 递归扫描阶段树：本阶段（含事件监听）及其所有子阶段中是否含 MetaSkillAction。 */
    private static boolean containsMetaAction(SkillPhase phase) {
        if (phase == null) return false;
        if (containsMetaAction(phase.actions())) return true;
        if (phase.eventListen().map(listen -> containsMetaAction(listen.actions())).orElse(false)) return true;
        for (SkillPhasePlayer child : phase.children()) {
            if (containsMetaAction(child.phase())) return true;
        }
        return false;
    }

    private static boolean containsMetaAction(List<TriggeredAction> actions) {
        for (TriggeredAction triggered : actions) {
            if (triggered.action() instanceof MetaSkillAction) return true;
        }
        return false;
    }

    /**
     * 技能级元素标签：递归扫描阶段树（含事件监听）中所有行为的自述标签，取出元素标签
     * （{@code fire}/{@code cold}/{@code lightning}/{@code chaos}/{@code physical}）。
     *
     * <p>用途：元素标签平时只在 {@link SkillStatScopes#enterAction} 的「动作级」作用域发射，
     * 而「+X to Level of all [Element] Skills」必须在<b>施放前</b>（技能级）求值——
     * 等级在 {@code canCast} 之前就要定稿。故在此以技能级补出元素标签，
     * 供 {@link SkillCaster} 进入一次性求值作用域读取 {@code SKILL_LEVEL_BONUS_<元素>}，
     * <b>不并入</b> {@link #getStatTags}（避免整段施放被元素作用域污染）。
     */
    public Set<String> elementStatTags() {
        Set<String> out = new LinkedHashSet<>();
        for (SkillModifier modifier : modifiers) {
            if (modifier instanceof EventListenEffect listen) {
                collectElementTags(listen.eventListen().actions(), out);
            }
        }
        for (SkillPhasePlayer player : phases) collectElementTags(player.phase(), out);
        return out;
    }

    /** 元素标签白名单（与 {@link com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction#statTags()} 同源）。 */
    private static final Set<String> ELEMENT_TAGS = Set.of(
            GateTag.FIRE, GateTag.COLD, GateTag.LIGHTNING, GateTag.CHAOS, GateTag.PHYSICAL);

    private static void collectElementTags(SkillPhase phase, Set<String> out) {
        if (phase == null) return;
        collectElementTags(phase.actions(), out);
        phase.eventListen().ifPresent(listen -> collectElementTags(listen.actions(), out));
        for (SkillPhasePlayer child : phase.children()) collectElementTags(child.phase(), out);
    }

    private static void collectElementTags(List<TriggeredAction> actions, Set<String> out) {
        if (actions == null) return;
        for (TriggeredAction triggered : actions) {
            if (triggered == null || triggered.action() == null) continue;
            for (String tag : triggered.action().statTags()) {
                if (ELEMENT_TAGS.contains(tag)) out.add(tag);
            }
        }
    }

    public void runAnimation(SkillContext context) {
        if (context.getCaster() instanceof Player player) {
            if (!equipment.isEmpty()) {
                // 专用服务端不存在 Minecraft 客户端类（不在 classpath），用 FMLEnvironment.getDist() 判断是否在客户端，避免 NoClassDefFoundError
                if (FMLEnvironment.getDist() == Dist.CLIENT
                        && Minecraft.getInstance() != null && Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.resetAttackStrengthTicker();
                }
                ItemUtils.startUsingInstantly(player.level(), player, InteractionHand.MAIN_HAND);
            } else {
                // 施放动画走客户端（PAL 渲染在客户端实体上）：服务端只发通知（skillId + 施放时长），
                // 客户端按 skill/<path> 解析动画（缺失回退默认施法动画）并随施放速度变速。
                if (player instanceof ServerPlayer sp) {
                    Identifier skillId = context.getSkillId() != null
                            ? Identifier.tryParse(context.getSkillId()) : null;
                    // 广播到同维度所有玩家（而非仅施法者），使其他玩家也能看到本次施法动画
                    PacketDistributor.sendToPlayersInDimension((ServerLevel) sp.level(), new CastAnimationPacket(
                            animation(), skillId, getCastTime(context), sp.level().getGameTime()));
                }
            }
        }
    }

    public static void stopUseSkill(Player player) {
        if (player.isUsingItem()) {
            player.stopUsingItem();
            if (FMLEnvironment.getDist() == Dist.CLIENT
                    && Minecraft.getInstance() != null && Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.resetAttackStrengthTicker();
            }
        }
        // 打断施法动画（服务端 → 客户端 stop 信号；castTimeTicks<=0 表示停止）
        if (player instanceof ServerPlayer sp) {
            // 广播到同维度所有玩家，使其他玩家也能看到施法动画中断
            PacketDistributor.sendToPlayersInDimension((ServerLevel) sp.level(),
                    new CastAnimationPacket(Optional.empty(), CraftOfExile.EMPTY_LOCATION, 0, 0L));
        }
    }

    public void addDataComponent(List<Component> tooltip, SkillContext context) {
        Component costComponent = cost.stream().map(c -> c.getCostComponent(context))
                .reduce(MutableComponent::append).orElse(null);
        tooltip.add(costComponent);
        cast.addComponent(tooltip, context);

        boolean[] changed = new boolean[]{false, false, false, false};
        ItemTooltipAssembler.addAttributeRequirementTooltip(tooltip, (Player) context.getCaster(), require.getAll(context), changed);

        String require = this.equipment();
        if (!require.isEmpty()) {
            LivingEntity caster = context.getCaster();
            if (caster == null) return;
            boolean current = ModHelper.currentEquipment(caster, caster.getMainHandItem(), require);
            Component component = Component.translatable("stat.require")
                    .append(": ").withStyle(ChatFormatting.DARK_AQUA)
                    .append(Component.translatable("item.coe." + require)
                            .withStyle(current ? ChatFormatting.WHITE : ChatFormatting.RED));
            tooltip.add(component);
        }
    }

    public void addPhaseDataComponent(List<Component> tooltip, SkillContext context) {
        phases.forEach(phase -> phase.addComponent(tooltip, context));
    }

    public int getCastTime(SkillContext context) {
        return cast.getCastTime(context);
    }

}

