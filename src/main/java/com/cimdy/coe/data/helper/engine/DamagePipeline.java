package com.cimdy.coe.data.helper.engine;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;

import org.jspecify.annotations.Nullable;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.registry.DamageTypeRegistry;
import com.cimdy.coe.data.codec.support.damage.DamageTypes;
import com.cimdy.coe.registry.DamageTypeTagRegistry;
import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import com.cimdy.coe.data.codec.skill.context.DamageContext;
import com.cimdy.coe.data.codec.skill.context.SkillContext.HitContext;
import com.cimdy.coe.data.codec.skill.phase.event.SkillEventBus;
import com.cimdy.coe.data.codec.skill.context.SkillDamageSource;
import com.cimdy.coe.data.codec.support.addition.AdditionData;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.convert.DamageConvertStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.convert.DamageTakenConvertStatEffect;
import com.cimdy.coe.data.codec.support.stat.condition.RelationCondition;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.cimdy.coe.data.codec.support.status.runtime.StatusInstance;
import com.cimdy.coe.data.helper.combat.*;
import com.cimdy.coe.data.helper.hub.*;
import com.cimdy.coe.data.helper.resource.*;
import com.cimdy.coe.data.helper.status.*;
import com.cimdy.coe.event.Event;
import com.cimdy.coe.event.EventName;
import com.cimdy.coe.event.combat.GameEvent;
import com.cimdy.coe.event.TriggerContext;
import com.cimdy.coe.item.accessory.Buckler;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;

/**
 * 伤害分阶段结算管线。
 *
 * <p>把「一次命中伤害」拆成离散、可独立挂接的阶段（{@link Stage}）；每个机制（异常施加、状态承伤、减免、
 * 格挡、能量护盾/生命扣除、补给、触发）作为<b>点对点回调</b>注册到对应阶段，由 {@link #BUS} 按
 * 「阶段 + 标签过滤」分发，从而满足：
 * <ul>
 *   <li><b>点对点</b>：每个机制只在自己所属阶段被调用，不再复用 {@code LivingDamageEventPre} 这种多段大回调；</li>
 *   <li><b>标签对标签</b>：回调可声明所需作用域标签（如仅 {@code cold} 元素），不匹配则整套跳过——
 *       针对性的回调<b>不必全量计算</b>（见 {@link Bus#register(Stage, Set, Callback)}）；</li>
 *   <li><b>属性对属性</b>：每个回调内部仍走「统一属性 + 标签（{@link GateTag}）」的 Stat 模型，
 *       不 switch 特定属性（与 {@code AilmentHelper} 的门槛/积蓄统一写法同源）。</li>
 * </ul>
 *
 * <p>顺序由 {@link Stage} 的声明顺序隐式保证——{@link #run(State, LivingDamageEvent.Pre)} 严格按枚举顺序
 * 逐阶段分发；要新增时序（例如「格挡在击中判定前」）只需在枚举合适位置插入 {@code Stage} 并注册回调，
 * 无需改动既有逻辑。
 */
public final class DamagePipeline {

    private DamagePipeline() {
    }

    /** 实体槽位键（SelectorNode.slot）：攻击者侧「最近一次直接命中目标」。 */
    public static final String SLOT_HIT = "hit";
    /** 实体槽位键（SelectorNode.slot）：受害者侧「最近一次直接攻击者」。 */
    public static final String SLOT_HITTER = "hitter";

    /** 完美招架时机窗口（tick）：进入招架姿态后此 tick 数内挡下打击即判为完美招架（约 0.25s）。 */
    private static final int PERFECT_PARRY_WINDOW_TICKS = 5;

    /**
     * Defend 守护技阈值常量（POE2 语料对齐，集中可调）：
     * <ul>
     *   <li>{@code DEFEND_FAR_DISTANCE}：远距判定距离（m）——对齐 poe2db
     *       「Defend with X% of Armour against Hit Damage from Enemies further than 6m away」的 6m 阈值；</li>
     *   <li>{@code DEFEND_LOW_ENERGY_FRACTION}：能量护盾低于该比例视为「低能量护盾」——
     *       对齐 POE2 Low Energy Shield 关键词（约 50%）；{@code LOW_ENERGY} 作用域仅在「非低能量护盾」时激活。</li>
     * </ul>
     */
    private static final double DEFEND_FAR_DISTANCE = 6.0;
    private static final double DEFEND_LOW_ENERGY_FRACTION = 0.5;

    // 「被你点燃过的敌人」递增速率(0.01/秒)与上限(0.10)现由读值节点 ignite_history_fire_taken 承载
    // （写在条目 amount 表达式内，缺省即此二值），由 StatEngine.igniteHistoryFireTakenBonus 读取
    // （见 applyStatusDamageTakenModifiers）。

    /**
     * 伤害结算的离散阶段。声明顺序即执行顺序（见 {@link #run}）。
     * 各阶段对应原 {@code LivingDamageEventPre} 内联逻辑的一个决策点。
     */
    public enum Stage {
        /** 伤害数组已计算、暴击已定（{@code damages[]} 就绪）；此时命中判定已完成、作用域已激活。 */
        DAMAGE_COMPUTED,
        /** 额外伤害注入（gained as extra）：在减免前把「基于当前命中总伤害 + 资源缩放」的额外元素伤害并入伤害数组（如大法师：非引导法术获得额外闪电）。 */
        GAINED_AS_EXTRA,
        /** 减免前：伤害类型转换（去A取B / 共存也获得B），覆盖命中伤害与异常输出两个维度。 */
        OUTPUT_CONVERT,
        /** 伤害衍生异常（流血/点燃/中毒）+ 暴击效果 + 冰缓冰冻，均在「非异常命中」守卫下。 */
        AILMENT_AND_CRIT,
        /** 状态承伤修正（感电/凋零/传奇独特加成）。 */
        STATUS_DAMAGE_TAKEN,
        /** 减免前回调：承伤转化（damage_taken_convert）——把某种减免（护甲/抗性）按比例也作用于某伤害类型，
         *  在抗性/护甲计算之前就地改写 {@code damages[]}。 */
        PRE_REDUCTION,
        /** 抗性/护甲减免计算，并汇总 newDamage / totalDamageTaken。 */
        REDUCTION,
        /** 减免后结算：感电施加、护甲破坏记录。 */
        POST_REDUCTION,
        /** 招架判定（轻盾挡下前方近战/投射物打击，归零本次承伤 + 给攻击者加 Parried；置于格挡前）。 */
        PARRY,
        /** 格挡判定（架构支持置于任意时序，示例场景可前移至命中判定前）。 */
        BLOCK,
        /** Defend 守护技判定（X5 族，Stat 驱动）：命中后按触发几率 + 条件作用域幅度，以护甲为减伤池减免承伤。置于格挡后、蓝量承伤前。 */
        DEFEND,
        /** 承伤由「可受伤同伴」生命承担（node 32799，Stat 驱动）：命中伤害的该比例先扣同伴生命，剩余继续走后续资源阶段。置于 Defend 后、蓝量承伤前。 */
        COMPANION,
        /** 承受伤害由蓝量承担（Mind Over Matter）：把承伤的该比例从 mana 扣除，剩余走能量护盾/生命。先于能量护盾（ES 之前承伤）。 */
        MANA,
        RUNIC_WARD,
        /** 吸收后：能量护盾扣除（独立资源扣除回调，位于 MoM 之后——MoM 已先扣蓝量）。 */
        ENERGY,
        /** 生命扣除（含中毒眩晕、溢出伤害，一次独立资源扣除回调）。 */
        HEALTH,
        /** 触发型 stat 效果（击中/暴击）。 */
        TRIGGER
    }

    /**
     * 一次命中结算的共享可变状态，跨阶段回调传递。固定字段（构造时确定）与可变字段分开：
     * 可变字段在对应阶段被回调就地修改。
     */
    public static final class State {
        // —— 固定上下文（构造时确定） ——
        final LivingEntity living;
        final ServerLevel serverLevel;
        final DamageSource source;
        final Entity entity;
        final Entity directEntity;
        final LivingEntity attacker;
        final RandomSource random;
        final Set<String> tags;
        final boolean isAilment;
        final LivingDamageEvent.Pre event;

        // —— 可变状态（阶段间流转） ——
        AdditionData data;
        boolean crit;
        float[] damages;
        float newDamage;
        float totalDamageTaken;
        boolean blocked;

        State(LivingEntity living, ServerLevel serverLevel, DamageSource source, Entity entity,
              Entity directEntity, LivingEntity attacker, RandomSource random, Set<String> tags,
              boolean isAilment, LivingDamageEvent.Pre event) {
            this.living = living;
            this.serverLevel = serverLevel;
            this.source = source;
            this.entity = entity;
            this.directEntity = directEntity;
            this.attacker = attacker;
            this.random = random;
            this.tags = tags;
            this.isAilment = isAilment;
            this.event = event;
        }
    }

    /** 阶段回调：接收共享状态并就地修改。 */
    @FunctionalInterface
    public interface Callback {
        void run(State state);
    }

    /** 带标签过滤的回调注册项。 */
    private record Registration(Stage stage, Set<String> requiredTags, Callback callback) {
    }

    /**
     * 标签过滤的回调总线：按阶段分发，仅当状态标签满足 {@code requiredTags}（子集包含判定）才执行该回调。
     * 因此「仅对火元素生效」的回调在非火命中时整套跳过——针对性回调不必全量计算。
     */
    public static final class Bus {
        private final List<Registration> regs = new ArrayList<>();

        /** 注册一个在该阶段「始终执行」的回调（无标签过滤）。 */
        public Bus register(Stage stage, Callback callback) {
            return register(stage, Set.of(), callback);
        }

        /** 注册带标签过滤的回调：仅当状态标签包含全部 {@code requiredTags} 才执行。 */
        public Bus register(Stage stage, Set<String> requiredTags, Callback callback) {
            regs.add(new Registration(stage, requiredTags, callback));
            return this;
        }

        /** 执行指定阶段内、标签匹配的全部回调（按注册顺序）。 */
        public void runStage(Stage stage, State state) {
            for (Registration r : regs) {
                if (r.stage() != stage) continue;
                if (!state.tags.containsAll(r.requiredTags())) continue;
                r.callback().run(state);
            }
        }
    }

    /** 默认伤害结算总线：注册各阶段的既有行为，顺序与原 {@code LivingDamageEventPre} 内联逻辑一致。 */
    public static final Bus BUS = new Bus();

    static {
        BUS.register(Stage.DAMAGE_COMPUTED, DamagePipeline::stageDamageComputed);
        BUS.register(Stage.GAINED_AS_EXTRA, DamagePipeline::stageGainedAsExtra);
        BUS.register(Stage.OUTPUT_CONVERT, DamagePipeline::stageOutputConvert);
        BUS.register(Stage.AILMENT_AND_CRIT, DamagePipeline::stageAilmentAndCrit);
        BUS.register(Stage.STATUS_DAMAGE_TAKEN, DamagePipeline::stageStatusDamageTaken);
        BUS.register(Stage.PRE_REDUCTION, DamagePipeline::stagePreReduction);
        BUS.register(Stage.REDUCTION, DamagePipeline::stageReduction);
        BUS.register(Stage.POST_REDUCTION, DamagePipeline::stagePostReduction);
        BUS.register(Stage.PARRY, DamagePipeline::stageParry);
        BUS.register(Stage.BLOCK, DamagePipeline::stageBlock);
        BUS.register(Stage.DEFEND, DamagePipeline::stageDefend);
        BUS.register(Stage.COMPANION, DamagePipeline::stageCompanion);
        BUS.register(Stage.MANA, DamagePipeline::stageMana);
        BUS.register(Stage.RUNIC_WARD, DamagePipeline::stageRunicWard);
        BUS.register(Stage.ENERGY, DamagePipeline::stageEnergy);
        BUS.register(Stage.HEALTH, DamagePipeline::stageHealth);
        BUS.register(Stage.TRIGGER, DamagePipeline::stageTrigger);
    }

    /**
     * 伤害事件派发入口（{@code LivingDamageEvent.Pre}）：预计算伤害组成以推导元素作用域标签、
     * 进入本次命中作用域、构造 {@link State} 并驱动管线，最后把结果写回事件。
     *
     * <p>事件类（{@code DamageEvent}）只调用本方法，管线所需的标签与作用域由 {@link HitScopeHelper} 提供。
     */
    public static void runPre(LivingDamageEvent.Pre event) {
        LivingEntity living = event.getEntity();
        ServerLevel serverLevel = (ServerLevel) living.level();
        DamageSource source = event.getSource();
        Entity entity = source.getEntity();
        Entity directEntity = source.getDirectEntity();
        RandomSource random = serverLevel.getRandom();
        LivingEntity attacker = entity instanceof LivingEntity l ? l : null;

        // 预计算伤害组成以推导元素作用域标签：此快照不依赖元素类天赋，仅用于决定本次命中携带哪些元素标签。
        AdditionData preData = AdditionData.get(living, directEntity);
        float[] preDamages = DamageHelper.calculateDamages(event, preData, entity, serverLevel, source);
        Set<String> tags = HitScopeHelper.buildHitTags(source, attacker, directEntity, preDamages);

        // 进入本次命中的 stat 作用域：攻击者(进攻属性) 与受害者(防御属性) 均按相同命中标签即时求值。
        // 注意必须在作用域内重新取 AdditionData 快照——只有在作用域激活时取的快照才带本次命中标签加成。
        try (var scope = HitScopeHelper.enterHitScopes(living, attacker,
                StatContext.of(tags, SkillDamageSource.skillIdOf(source)))) {
            AdditionData data = AdditionData.get(living, directEntity);
            State state = new State(living, serverLevel, source, entity, directEntity, attacker, random, tags,
                    source.is(DamageTypeTagRegistry.IS_AILMENT), event);
            state.data = data;
            run(state);
            event.setNewDamage(state.newDamage);
        }
    }

    /**
     * 在已激活的命中作用域内，按阶段顺序驱动整条伤害管线。调用方（{@link #runPre}）负责
     * 进入/退出 stat 作用域，并在本方法返回后取 {@code state.newDamage} 写入事件。
     */
    public static void run(State state) {
        // D-8：攻击发起事件——伤害管线最前端的回调，供「攻击时施加 X」「攻击时叠加 X」类先行效果。
        // owner=攻击者，target=受害者；发生在伤害计算/减免之前，不随是否命中而缺失。
        if (state.attacker != null && state.attacker != state.living && !state.isAilment) {
            // 剔除 taken_* 标签（本侧为攻击侧，误传 taken_hit 等会让攻击者自身「被命中时」词缀错误触发）
            java.util.Set<String> atkTags = new HashSet<>(state.tags);
            atkTags.removeIf(t -> t.startsWith("taken_"));
            GameEvent.emit(EventName.ATTACK_START, state.attacker, state.living, atkTags);
        }
        for (Stage stage : Stage.values()) {
            BUS.runStage(stage, state);
        }
    }

    // ============================ 各阶段默认行为（逐字保留原内联逻辑） ============================

    /** 伤害计算 + 暴击判定（暴击由单一权威源决定，不再双重乘）。 */
    private static void stageDamageComputed(State s) {
        s.crit = s.source instanceof SkillDamageSource sds
                ? sds.isCrit()
                : DamageHelper.isCriticalHit(s.attacker, s.living, s.random);
        s.damages = DamageHelper.calculateDamages(s.event, s.data, s.entity, s.serverLevel, s.source);
    }

    /**
     * 额外伤害注入（gained as extra）：在减免前把「基于当前命中总伤害、按资源缩放」的额外元素伤害并入伤害数组。
     * <p>大法师：仅非引导法术（{@code !GateTag.CHANNELING}）命中，读攻击者 {@code archmage_lightning_gain}
     * （每 100 最大魔力的比率，由精魂 ModifierAction 设为 0.04）与实时 {@code max_mana}，
     * 计算 {@code 总伤害 × 比率 × 最大魔力/100} 注入闪电击槽位（index 4）。
     * 阶段置于 DAMAGE_COMPUTED 之后、各减免之前，使额外伤害同样被对应抗性减免（符合 PoE「额外伤害也走减免」语义）。
     */
    private static void stageGainedAsExtra(State s) {
        if (s.isAilment || s.attacker == null) return;
        if (!(s.source instanceof SkillDamageSource sds)) return;
        SkillInstance si = sds.getSkill();
        if (si == null) return;
        // 仅非引导法术：法术标签 + 非引导（HoldInput/Continuous 含 GateTag.CHANNELING，跳过）
        Set<String> tags = si.getSkill().getStatTags(si.getContext(), si.getSkillId());
        if (!tags.contains(GateTag.SPELL)) return;
        if (tags.contains(GateTag.CHANNELING)) return;
        float rate = readInScope(s.attacker, java.util.Set.of(), AttributeRegistry.ARCHMAGE_LIGHTNING_GAIN);
        if (rate <= 0) return;
        double maxMana = s.attacker.getAttributeValue(AttributeRegistry.MAX_MANA);
        if (maxMana <= 0) return;
        float total = 0F;
        for (int i = 0; i < DamageTypes.width(); i++) total += s.damages[i];
        if (total <= 0) return;
        float extra = (float) (total * rate * (maxMana / 100.0));
        if (extra > 0) s.damages[4] += extra;
    }

    /** 减免前：伤害类型转换（去A取B / 共存也获得B），覆盖命中伤害与异常输出三个维度。 */
    private static void stageOutputConvert(State s) {
        // 命中上下文标签（供 from_context 门控判定：仅 crit/ailment 等命中才执行对应转换）
        Set<String> hitCtx = new HashSet<>(2);
        if (s.crit) hitCtx.add("crit");
        if (s.isAilment) hitCtx.add("ailment");
        // 维度A：异常输出——施法者身上的规则把「异常原伤害类型」重路由（如被点燃者以混沌承受点燃伤害）
        //   持续伤害(DoT)维度：hits-only 规则（如 Unholy Might）在此被跳过（includeHitsOnly=false）
        if (s.source.is(DamageTypeTagRegistry.IS_AILMENT)
                && s.source instanceof SkillDamageSource sds && sds.getAilmentTag() != null
                && s.attacker != null) {
            ResourceKey<DamageType> key = s.source.typeHolder().unwrapKey().orElse(null);
            if (key != null) {
                int src = DamageTypeRegistry.dotSlot(key);
                if (src >= 0) {
                    // ailment_output_convert 门控上下文 = {ailment, <异常名>, <来源类型>}：
                    // 与 SkillDamageSource#ailmentTag 声明的作用域一致（ENGINE_PITFALLS §ailment_output_convert），
                    // 使 DamageConvertStatEffect 的 from_context 能精确到「某个异常的某种来源类型」
                    // （如「点燃的火焰输出转为混沌」）。此前 hitCtx 仅含 crit/ailment，ailmentTag 传而不用。
                    Set<String> ailmentHitCtx = new HashSet<>(hitCtx);
                    ailmentHitCtx.add(sds.getAilmentTag());
                    ailmentHitCtx.add(DamageTypeRegistry.DAMAGE_TYPE_TAGS[src]);
                    applyDamageTypeConversion(StatEngine.damageConvert(s.attacker), src, s.damages, false, ailmentHitCtx);
                }
            }
        }
        // 维度C：命中伤害——施法者身上的「外发转换」规则（不洁之力全伤转额外混沌 / 元素执政官非元素转元素）
        //   作用于施法者外发给受害者的命中伤害；hits-only 规则在此生效（includeHitsOnly=true）。
        //   缺此维度时，攻击者增益型转换（Unholy Might / avatar_of_fire）完全不作用于外发命中伤害
        //   （见 STAT_SYSTEM.md §"攻击者外发命中转换"）。
        if (!s.isAilment && s.attacker != null) {
            // 绑定「命中目标」为攻击者作用域的交互对方（sourceEntity），使 onTarget(COND_X_STATUS) 类
            // 转换规则（对冻结/感电/等敌人获得额外伤害）能按目标实时状态求值，与属性修正的 against-enemy
            // 通道一致（StatContext 文档：命中结算双方作用域互持交互对方引用）。
            AutoCloseable scope = StatScopes.enter(s.attacker,
                    StatScopes.mergeWithCurrent(s.attacker, StatContext.EMPTY.withSourceEntity(s.living)));
            try {
                List<StatEngine.DamageConversion> outRules = StatEngine.damageConvert(s.attacker);
                if (!outRules.isEmpty()) {
                    for (int src = 0; src < 5; src++) {
                        if (s.damages[src] > 0) applyDamageTypeConversion(outRules, src, s.damages, true, hitCtx);
                    }
                }
            } finally {
                try {
                    scope.close();
                } catch (Exception ignored) {
                }
            }
        }
        // 维度B：命中伤害——承受者身上的规则把「某种命中伤害类型」重路由（如承受火伤转冰）
        //   hits-only 规则在此同样生效（命中伤害才走此维度）
        List<StatEngine.DamageConversion> rules = StatEngine.damageConvert(s.living, true);
        for (int src = 0; src < 5; src++) {
            if (s.damages[src] > 0) applyDamageTypeConversion(rules, src, s.damages, true, hitCtx);
        }
    }

    /** 伤害衍生异常施加 + 暴击效果 + 冰缓冰冻（均在「非异常命中」守卫下）。 */
    private static void stageAilmentAndCrit(State s) {
        if (!s.isAilment && s.entity instanceof LivingEntity target) {
            // 伤害衍生异常（流血/点燃/中毒）由 AilmentHelper 统一施加并发布 on_ignite 元事件
            AilmentHelper.applyDamageAilment(s.source, s.living, target, s.random, s.damages, s.blocked);

            if (s.crit) {
                DamageContext.cacheDamageContext(s.source, s.living, new DamageContext().Crit(true));
                // 承暴击伤害倍率（受害者侧 TAKEN_CRIT_DAMAGE）：所有暴击命中均生效，与攻击方 CRIT_DAMAGE 独立维度
                DamageHelper.applyTakenCriticalDamage(s.living, s.damages);
                // 技能源的暴击倍率已在 DamageAction 施加，此处仅对非技能源（原版攻击）应用攻击暴击倍率，消除双重乘
                if (!(s.source instanceof SkillDamageSource)) {
                    DamageHelper.applyCriticalDamage(s.attacker, s.damages);
                }
                SkillInstance skill = s.source instanceof SkillDamageSource sds ? sds.getSkill() : null;
                if (skill != null) {
                    // 标签并入受害者（s.living）的活跃状态：使「暴击带元素异常的敌人」类
                    // EventListenPhase 条件可用 require_tags=[elemental] 表达（StatusDefs 的
                    // ignite/chill/freeze/shock 已带 ELEMENTAL 标签，无需额外归一）。
                    // 事件订阅方为攻击者 s.entity（= source.getEntity()），与总线语义一致。
                    Set<String> critTags = StatusEngine.activeTags(s.living);
                    SkillEventBus.publishMeta(s.entity, skill, EventName.CRIT,
                            critTags.toArray(new String[0]));
                }
                ExpressionContext.get(target).refreshTimer(EventName.CRIT);
            }
            DamageHelper.applyDamageMultipliers(s.attacker, s.damages);
            ExpressionContext.get(target).refreshTimer(EventName.CAUSE_DAMAGE);

            // 冰缓/冰冻由 AilmentHelper 统一施加并发布 on_freeze 元事件
            AilmentHelper.applyColdAilment(s.source, s.living, target, s.damages);
        }
    }

    /** 状态承伤修正（承伤变动统一属性通道 + 被招架 + 图腾承伤光环 + 传奇独特加成）。 */
    private static void stageStatusDamageTaken(State s) {
        applyStatusDamageTakenModifiers(s.source, s.living, s.attacker, s.tags, s.damages);
        // Charm 元素受击触发（Ruby/Sapphire/Topaz/Amethyst）：玩家受元素伤害即自动激活对应抗性护符。
        // 槽位对齐 applyDamageReductions：[1]=混沌 [2]=火 [3]=冰 [4]=雷。
        if (s.living instanceof net.minecraft.world.entity.player.Player player) {
            com.cimdy.coe.item.charm.CharmEngine.onElementalDamageTaken(player, 1, s.damages[1]);
            com.cimdy.coe.item.charm.CharmEngine.onElementalDamageTaken(player, 2, s.damages[2]);
            com.cimdy.coe.item.charm.CharmEngine.onElementalDamageTaken(player, 3, s.damages[3]);
            com.cimdy.coe.item.charm.CharmEngine.onElementalDamageTaken(player, 4, s.damages[4]);
        }
    }

    /** 减免前回调：承伤转化（damage_taken_convert）的 REROUTE 部分。
     *  <p>在抗性/护甲减免（applyDamageReductions）之前执行。对命中 REROUTE 规则（「x% 火焰伤害由护甲承受」）
     *  的伤害份额，从原槽位剥离并移入该减免手段的原生槽位（护甲→物理、火抗→火、冰抗→冰、电抗→电、混沌抗→混沌），
     *  从而<b>绕过原槽位的抗性减免</b>，改由目标手段原生减免接管。
     *  EXTRA 规则（二次减免叠加）不在此阶段处理，留待 stagePostReduction。 */
    private static void stagePreReduction(State s) {
        var all = StatEngine.damageTakenConvert(s.living);
        if (all.isEmpty()) return;

        // 本次伤害的异常/修饰标签（bleed/ignite/poison 等），用于「流血造成的物理伤害」这类组合维度
        Set<String> ailmentCtx = s.tags.stream()
                .filter(DamageTakenConvertStatEffect.AILMENT_TAGS::contains)
                .collect(Collectors.toSet());

        for (int slot = 0; slot < s.damages.length; slot++) {
            float original = s.damages[slot];
            if (original <= 0) continue;

            Set<String> slotTags = new HashSet<>(DamageHelper.slotDamageTags(slot));
            slotTags.addAll(ailmentCtx);

            // 收集命中本槽位的 REROUTE 规则，按 ratio 依次从剩余伤害中剥离（避免超额）
            float remaining = original;
            for (var sc : all) {
                if (sc.mode() != DamageTakenConvertStatEffect.Mode.REROUTE) continue;
                // 表达式匹配（GateExpr：与/或/非/嵌套）；nativeSlot 按「命中槽位」标签确定元素槽（表达式不再直接含元素）
                if (!sc.tags().matches(slotTags)) continue;
                float routed = sc.ratio() * remaining;
                if (routed <= 0) continue;
                remaining -= routed;
                s.damages[DamageTakenConvertStatEffect.nativeSlot(sc.reduction(), slotTags)] += routed;
            }
            // 原槽位只保留未被重路由的部分（这部分后续走原槽位原生减免）
            s.damages[slot] = remaining;
        }
    }

    /** 抗性/护甲减免计算（归一化：元素作用域遍历 RESISTANCES/MAX_RESISTANCES + 攻击方穿透），并汇总 newDamage / totalDamageTaken。 */
    private static void stageReduction(State s) {
        DamageHelper.applyDamageReductions(s.data, s.living, s.attacker, s.source, s.damages);
        s.newDamage = 0F;
        for (int i = 0; i < DamageTypes.width(); i++) s.newDamage += s.damages[i];
        // 合成伤害（heart_of_ice 等「视作造成 X 冰霜伤害」类，Source 带 synthetic 标记）：不造成任何真实
        // 生命/资源丢失，仅借伤害管线触发冰缓。stageAilmentAndCrit 已在 REDUCTION 之前跑过 applyColdAilment，
        // 此处把 newDamage 归零，使后续 ENERGY/MANA/HEALTH 等阶段自然跳过、事件回写伤害为 0。
        if (s.source instanceof SkillDamageSource sds && sds.isSynthetic()) s.newDamage = 0F;
        // 保存受击总伤害（能量护盾吸收前），供后续补给（Recoup）累加
        s.totalDamageTaken = s.newDamage;
    }

    /** 减免后结算：承伤转化（damage_taken_convert）的 EXTRA 部分 + 感电施加 + 护甲破坏记录。
     *  <p>EXTRA 规则（「x% 护甲值也作用于火焰」）在抗性/护甲减免（REDUCTION）之后执行：
     *  对命中槽位已减免后的伤害，额外再按 {@code ratio × 该手段公式} 做一次二次减免（叠加，不绕过原减免）。 */
    private static void stagePostReduction(State s) {
        var all = StatEngine.damageTakenConvert(s.living);
        if (!all.isEmpty()) {
            Set<String> ailmentCtx = s.tags.stream()
                    .filter(DamageTakenConvertStatEffect.AILMENT_TAGS::contains)
                    .collect(Collectors.toSet());
            for (int slot = 0; slot < s.damages.length; slot++) {
                float current = s.damages[slot];
                if (current <= 0) continue;
                Set<String> slotTags = new HashSet<>(DamageHelper.slotDamageTags(slot));
                slotTags.addAll(ailmentCtx);
                for (var sc : all) {
                    if (sc.mode() != DamageTakenConvertStatEffect.Mode.EXTRA) continue;
                    // 表达式匹配（GateExpr）；applyReduction 按「命中槽位」标签确定元素抗性公式
                    if (!sc.tags().matches(slotTags)) continue;
                    // 在已减免伤害上再套一次该手段减免公式，差额按 ratio 二次减免
                    float after = DamageTakenConvertStatEffect.applyReduction(sc.reduction(), slotTags, s.living, s.data, current, s.attacker);
                    s.damages[slot] -= sc.ratio() * (current - after);
                }
            }
        }

        if (!s.isAilment && s.entity instanceof LivingEntity target) {
            // 感电由 AilmentHelper 统一施加并发布 on_shock 元事件
            AilmentHelper.applyLightAilment(s.source, s.living, target, s.random, s.damages);
        }
        // 归一：破甲幅度由攻击方 {armor_break} 作用域的 ARMOR_BREAK 属性缩放（替代旧死属性 PHYSICAL_DAMAGE_ARMOR_BREAK）。
        // 破甲作用域并入施法/攻击技能标签（如 plant），使「植物技能破甲+X%」类词缀可按技能类型精确门控（R3 真读，非死线）。
        Set<String> armorBreakTags = new HashSet<>(Set.of(GateTag.ARMOR_BREAK));
        if (s.source instanceof SkillDamageSource sds && sds.getSkill() != null) {
            SkillInstance si = sds.getSkill();
            armorBreakTags.addAll(si.getSkill().getStatTags(si.getContext(), si.getSkillId()));
        }
        // 用 readInScopeAgainst（带 sourceEntity=受害者）而非 readInScope：破甲族有一条接线
        // （HIT_DAMAGE_ARMOR_BREAK_INC_ARMOR_ON_TARGETS_WITH_AILMENTS）用 .conditionTarget(EVENT_TARGET)，
        // 其条件须在受害者身上求值；而 readInScope 的 StatContext.of(tags, null) 恒置 sourceEntity=null →
        // triggerTarget=null → 条件被静默跳过（死线 #37）。绑定受害者后条件方可正确求值。
        double armorBreak = readInScopeAgainst(s.attacker, armorBreakTags, AttributeRegistry.ARMOR_BREAK, s.living);
        // 承受破甲倍率：受害方 {armor_break,taken} 作用域读 ARMOR_BREAK 进一步缩放破甲幅度（fold 归并 ARMOR_BREAK_TAKEN -> ARMOR_BREAK @{armor_break,taken}；R3 真读，与攻击方同模型单位）。
        double armorBreakTaken = readInScope(s.living, Set.of(GateTag.ARMOR_BREAK, GateTag.TAKEN), AttributeRegistry.ARMOR_BREAK);
        double breakMag = s.newDamage * (1 + Math.max(0, armorBreak)) * (1 + Math.max(0, armorBreakTaken));
        // #3: 法术暴击破甲 — 破甲值 = 值 × 本次物理伤害（覆盖常规公式）。PoE2 tmpl:
        // "ArmourBreak Armour on Critical Hit with Spells equal to {X}% of Physical Damage dealt"
        if (s.crit) {
            boolean isSpell = false;
            if (s.source instanceof SkillDamageSource sds && sds.getSkill() != null) {
                SkillInstance si = sds.getSkill();
                isSpell = si.getSkill().getStatTags(si.getContext(), si.getSkillId()).contains(GateTag.SPELL);
            }
            if (isSpell) {
                double abSpellPct = readInScope(s.attacker, Set.of(GateTag.SPELL, GateTag.CRIT), AttributeRegistry.ARMOR_BREAK);
                if (abSpellPct > 0) breakMag = abSpellPct * Math.max(0, s.damages[0]);
            }
        }
        StatusEngine.apply(s.living, StatusDefs.ARMOUR_BREAK, s.attacker, breakMag, 5 * 20);
        s.living.setData(AttachmentRegistry.ARMOUR_BREAK_DISPLAY, (float) breakMag);
        // S2 状态建模（破甲/完全破甲合一·第一步：激活孤儿 ARMOUR_BROKEN_FULLY）：
        // 破甲经 ARMOUR_BREAK 削减护甲（armorReduction → Attributes.ARMOR 乘算，StatusStatBridge 同步写 STAT_STORE），
        // 当护甲被削减至 ≤0 即「完全破甲」，此时施加 ARMOUR_BROKEN_FULLY（armorBreakFully → 护甲归零）。
        // 复活 Death-line 集群：DamagePipeline:829 死分支、COND_ARMOUR_BROKEN_FULLY_STATUS /
        // COND_SOURCE_ARMOUR_BROKEN_FULLY_STATUS 两条件（原恒 false）、MAIM_ON_FULLY_BREAK /
        // NO_REGEN_ON_FULLY_BREAK flag 消费端、两档 DAMAGE 效果（fully_broken_armor 标签）。
        // 下方 getAttributeValue 已反映本次削减（StatusEngine.apply 同步落地，R3 真读，非死线）。
        if (s.living.getAttributeValue(Attributes.ARMOR) <= 0.0) {
            StatusInstance ab = StatusEngine.query(s.living, StatusDefs.ARMOUR_BREAK);
            // 归一 #56：完全破甲跃迁沿真实发射 FULLY_BROKE_ARMOUR 事件（owner=破甲者，other=被破甲敌），
            // 驱动 GrantOnEventStatEffect「Enemies you Fully Armour Break are Maimed / cannot Regenerate Life」。
            // 仅跃迁沿一次：PoE2 语义下处于完全破甲期间再破甲只刷新时长，不重复授予。
            if (ab != null && !ab.fullyBroken) {
                ab.fullyBroken = true;
                if (s.attacker != null) {
                    // 完全破甲事件必须同时写计数队列（Event.record），否则 recently/count(fully_broke_armour)
                    // 恒返 0（R3 修复 #65 同型：fire 仅驱动 GrantOnEvent，recency/count 须 record 喂）。
                    // owner=破甲者、other=被破甲敌（StatEngine.fire 的 target）已正确传入，victim 作用域由 GrantOnEvent 的
                    // triggerTarget 解析，无需在本处另发 victim_<uuid> 标签（与 TAKEN_STUN/CAUSED_DOT 直接 fire 口径一致）。
                    Event.record(s.attacker, EventName.FULLY_BROKE_ARMOUR);
                    StatEngine.fire(s.attacker, s.living, StatContext.of(
                            Set.of(EventName.FULLY_BROKE_ARMOUR, GateTag.ARMOR_BREAK, GateTag.FULLY_BROKEN_ARMOR),
                            null, EventName.FULLY_BROKE_ARMOUR, null), null);
                }
            }
        }
    }

    /**
     * 招架判定（轻盾 Buckler 的 Parry 机制，poe2db 口径）：
     * 招架者（{@code s.living}）须在招架姿态下（{@code EventName.PARRY} timer 活跃）、
     * 且本次招架未被消费（{@code EventName.PARRY_CONSUMED} 不活跃）、
     * 且攻击者位于正前方 1 米内，方可「挡下下一个」打击：
     * 本次承伤归零 + 给攻击者施加 {@code PARRIED} 减益（off-balance，承伤大增）
     * + 写 {@code PARRY_CONSUMED}（姿态被消费，下次攻击前不可再招架）
     * + 造成型招架事件闭环（招架者 living 作 owner，攻击者 attacker 作目标）。
     * 置于格挡（BLOCK）之前——招架优先于格挡。
     */
    private static void stageParry(State s) {
        LivingEntity living = s.living;
        // 招架者须手持可招架物品（权威来源 CAN_PARRY 组件，回落 EquipmentState.buckler 标签）
        ItemStack offhand = living.getItemBySlot(EquipmentSlot.OFFHAND);
        if (!Buckler.canParry(offhand)) return;
        // 须处于招架姿态且未被消费
        ExpressionContext ctx = ExpressionContext.get(living);
        if (!ctx.isTimerActive(EventName.PARRY)) return;
        if (ctx.isTimerActive(EventName.PARRY_CONSUMED)) return;
        Entity attacker = s.attacker;
        if (attacker == null || attacker == living) return;
        // 攻击者须位于招架者正前方 1 米内（朝向点积 > 0 即在面前，距离阈值 1 米；PARRY_RANGE 在 {parry} 作用域缩放）
        double parryRange = readInScope(living, Set.of(GateTag.PARRY), AttributeRegistry.PARRY_RANGE);
        double dist = living.distanceTo(attacker);
        if (dist > Math.max(0.1, parryRange)) return;
        Vec3 toAttacker = attacker.position().subtract(living.position()).normalize();
        Vec3 facing = living.getLookAngle().normalize();
        if (toAttacker.dot(facing) <= 0.0) return;

        // 挡下本次：承伤归零（与 stageBlock 同源）
        DamageContext.cacheDamageContext(s.source, living, new DamageContext().Parry(true));
        s.newDamage = 0;
        // 消费招架姿态（1s 内不可再次招架，直到姿态结束重新进入）
        ctx.refreshTimer(EventName.PARRY_CONSUMED, 20);
        // 攻击者若为生物：施加 Parried 减益（off-balance，承伤大增，约 4s）+ 造成型招架事件闭环
        // （招架者 living 作 owner，攻击者作目标；投射物等非生物攻击仅归零承伤，不触发 on-cause 效果）
        if (attacker instanceof LivingEntity livingAttacker) {
            // 完美招架（Perfect Parry）判定：进入招架姿态后「极短时间内」挡下本次打击即为完美招架。
            // 时机信号取 Context 的 PARRY timer 已过 tick（getTimerElapsed），刚重新举盾时 elapsed 小 → 完美招架。
            // 注：PARRY timer 经 onUseItemStart refreshTimerInfinite 写入 elapsed=0、随服务端 tickStore 每 tick +1，
            // 故 elapsed 反映「本次进入招架以来经过的 tick」，可作为时机精确度信号。
            double parryElapsed = ctx.getTimerElapsed(EventName.PARRY, Integer.MAX_VALUE);
            boolean perfectParry = parryElapsed <= PERFECT_PARRY_WINDOW_TICKS;
            // PARRIED 强度/时长读取招架者天赋属性（Parried Debuff Effect/Duration，默认乘数 1.0），不再硬编码；
            // 招架作为 {parry} 作用域读取（与 PARRY_RANGE 同构），Stat 条目按标签聚合「招架加成」类词缀。
            // 强度/时长经 {parry} 作用域读 AILMENT_MAGNITUDE/AILMENT_DURATION（同属性 tag 区分）。
            double effectMul = readInScope(living, Set.of(GateTag.PARRY), AttributeRegistry.AILMENT_MAGNITUDE);
            double durationMul = readInScope(living, Set.of(GateTag.PARRY), AttributeRegistry.PARRY_DURATION);
            double baseMagnitude = perfectParry ? 2.0 : 1.0;
            int baseDuration = perfectParry ? 8 * 20 : 4 * 20;
            double parriedMagnitude = baseMagnitude * Math.max(0.0, effectMul);
            int parriedDuration = (int) Math.round(baseDuration * Math.max(0.0, durationMul));
            StatusEngine.apply(livingAttacker, StatusDefs.PARRIED, living, parriedMagnitude, parriedDuration);
            // 造成/承受对称闭环：招架者为造成侧 owner，攻击者为承受侧 owner（完美招架走独立事件名）。
            firePair(s, living, livingAttacker,
                    perfectParry ? EventName.CAUSED_PERFECT_PARRY : EventName.CAUSED_PARRY,
                    perfectParry ? EventName.TAKEN_PERFECT_PARRY : EventName.TAKEN_PARRY);
        }
    }

    /**
     * 造成/承受对称发射（招架/格挡等「防御方为造成侧」的事件共用）：
     * causedOwner 侧发 {@code causedEvent}、takenOwner 侧发对称的 {@code takenEvent}，
     * 两侧各自写计时 + fire，owner 为空自动跳过。
     */
    private static void firePair(State s, LivingEntity causedOwner, LivingEntity takenOwner,
                                 String causedEvent, String takenEvent) {
        Entity direct = s.source.getDirectEntity();
        Identifier skill = SkillDamageSource.skillIdOf(s.source);
        GameEvent.caused(s.source, direct, causedEvent).fireCaused(causedOwner, takenOwner, skill);
        if (takenOwner != null && takenOwner != causedOwner) {
            GameEvent.taken(s.source, direct, takenEvent).fireTaken(takenOwner, causedOwner, skill);
        }
    }

    /** 格挡判定：命中后若格挡，则本次承伤归零。 */
    private static void stageBlock(State s) {
        // Dodge 闪避判定（X5 族，Stat 驱动）：读 {dodge} 作用域 DODGE_CHANCE（默认 0），命中则整次承伤归零。
        // 置于格挡（BLOCK）之前——闪避优先于格挡（POE2 闪避为独立防御层）。
        if (s.newDamage > 0 && s.living instanceof LivingEntity living) {
            double dodgeChance = DamageHelper.readInScopePublic(living, Set.of(GateTag.DODGE), AttributeRegistry.DODGE_CHANCE);
            if (dodgeChance > 0 && s.random.nextFloat() < dodgeChance) {
                DamageContext.cacheDamageContext(s.source, living, new DamageContext().Evasion(true));
                s.newDamage = 0;
                firePair(s, living, s.attacker, EventName.CAUSED_DODGE, EventName.TAKEN_DODGE);
                return;
            }
        }
        if (DamageHelper.isBlock(s.living, s.source, s.random, s.newDamage)) {
            DamageContext.cacheDamageContext(s.source, s.living, new DamageContext().Block(true));
            float preBlock = s.newDamage;   // 偏转/格挡前（减免后）伤害
            s.newDamage = 0;
            s.blocked = true;
            // 偏转命中减伤（PreventStatEffect，DEFLECT 簇 #32/#33，GAP-FIX#11）：持有「防止 X% 偏转命中伤害」词条时，
            // 偏转命中改为承 preBlock×(1−prevent%)，而非满挡 0；无词条行为不变（仍满挡）。
            // crit 变体（#32）仅对暴击偏转命中生效；hit 变体（#33）对所有偏转命中生效，二者在暴击偏转命中叠加。
            double prevent = preventDeflectedMagnitude(s.living, s.crit);
            if (prevent > 0) {
                s.newDamage = (float) (preBlock * (1.0 - Math.min(1.0, prevent)));
            }
            // P4：造成/承受对称闭环——格挡者 living 为造成侧（「格挡时触发 X」），攻击者为承受侧（「被格挡时 X」）。
            // timer 写各自 Context 供 recently/count 查询，fire 驱动 on-cause / on-taken 效果。
            firePair(s, s.living, s.attacker, EventName.CAUSED_BLOCK, EventName.TAKEN_BLOCK);
        }
    }

    /**
     * Defend 守护技判定（X5 族，Stat 驱动）：把「Defend with X% of Armour」实现为以护甲为减伤池的守护层。
     * <p>
     * 链路对齐 q-0 设计原则——<b>触发几率在 Effect 上计算（由技能/守护技通道掌管），不单独建属性</b>；
     * <b>幅度走 DefendStatEffect（INERT 标记，直接读持有者 STAT_STORE 折叠，不经属性层）</b>：
     * <ol>
     *   <li>触发门控：由「是否装备 Defend 守护技」通道决定（support gem 链路生效即承护），无独立几率属性掷骰。</li>
     *   <li>幅度维度 {@code DefendStatEffect}（INERT 标记，不进属性层）：按命中条件作用域
     *       {@code {crit}/{projectile}/{far}/{low_energy}} 即时折叠各条件变体——
     *       条目标签 ⊆ 读取作用域即参与聚合（如暴击+投射物同时命中则叠加两变体幅度）。</li>
     *   <li>减伤额 = {@code 护甲值 × 幅度}，自 {@code s.newDamage} 扣减（对齐 Dodge 归零 / Guard 比例吸收同源的「读属性即时折叠」范式）。</li>
     * </ol>
     * 条件作用域激活（每命中实时计算）：
     * <ul>
     *   <li>{@code crit}：{@code s.crit}（管线已定的暴击权威源）；</li>
     *   <li>{@code projectile}：本次命中标签含 {@code projectile}（GameEvent 已注入）；</li>
     *   <li>{@code far}：攻击者距离 &gt; {@link #DEFEND_FAR_DISTANCE}（m）；</li>
     *   <li>{@code low_energy}：能量护盾 <b>未</b>处于低能量（&ge; {@link #DEFEND_LOW_ENERGY_FRACTION} 上限），
     *       对齐变体语义「while not on Low Energy Shield」。</li>
     * </ul>
     * 阈值常量集中可调（见类顶部 {@code DEFEND_FAR_DISTANCE}/{@code DEFEND_LOW_ENERGY_FRACTION}）。
     */
    private static void stageDefend(State s) {
        if (s.newDamage <= 0 || !(s.living instanceof LivingEntity living)) return;
        // ① 命中条件作用域：仅激活适用的条件变体（条目标签 ⊆ 读取作用域即折叠）
        Set<String> cond = new HashSet<>();
        if (s.crit) cond.add(GateTag.CRIT);
        if (s.tags.contains(GateTag.PROJECTILE)) cond.add(GateTag.PROJECTILE);
        if (s.attacker != null && s.attacker.distanceTo(living) > DEFEND_FAR_DISTANCE) cond.add(GateTag.FAR);
        float es = ResourceRegistry.getEnergyShield(living);
        float maxEs = ResourceRegistry.getMaxEnergyShield(living);
        boolean lowEnergy = maxEs > 0 && es < maxEs * DEFEND_LOW_ENERGY_FRACTION;
        if (!lowEnergy) cond.add(GateTag.LOW_ENERGY); // 变体语义：「while not on Low Energy Shield」
        double armour = living.getAttributeValue(Attributes.ARMOR);
        if (armour <= 0) return;
        // ③ 幅度维度（DefendStatEffect，INERT 标记，按 conditionTags ⊆ 命中条件折叠）：减伤 = 护甲 × 幅度
        double totalDefend = 0.0;
        double magnitude = defendMagnitude(living, cond);
        if (magnitude > 0) totalDefend += armour * magnitude;
        // ④ CHANCE_DEFEND：无条件「X% 几率以 X% 护甲承护」，每次命中掷骰一次（几率与幅度同取条目幅度）
        double cb = chanceDefendMagnitude(living);
        if (cb > 0 && s.random.nextFloat() < Math.min(1.0, cb)) totalDefend += armour * cb;
        float defended = (float) Math.min(s.newDamage, totalDefend);
        if (defended > 0) s.newDamage -= defended;
    }

    /**
     * 读持有者 STAT_STORE 中 CHANCE_DEFEND 标记条目的护甲承护幅度总和（同时作为 per-hit 触发几率）。
     * <p>折叠口径（不校验 variant / 不判 tags / 不求值 {@code e.conditions()}）已固化为
     * {@link StatEngine#markerMagnitudeUngated} 的签名，见该方法说明。
     */
    private static double chanceDefendMagnitude(LivingEntity living) {
        return StatEngine.markerMagnitudeUngated(living, MarkerStatEffect.Kind.CHANCE_DEFEND);
    }

    /**
     * 读持有者 STAT_STORE 中 CORRUPTION_ENDURES 标记条目的免死几率总和（INERT 标记，节点取消分配即失效）。
     * <p>折叠口径同 {@link #chanceDefendMagnitude}，见 {@link StatEngine#markerMagnitudeUngated}。
     */
    private static double corruptionEnduresChance(LivingEntity living) {
        return StatEngine.markerMagnitudeUngated(living, MarkerStatEffect.Kind.CORRUPTION_ENDURES);
    }

    /**
     * 读持有者 STAT_STORE 中 PreventStatEffect 条目、按 deflected/crit 门控折叠的「防止 X% 偏转命中伤害」幅度总和（INERT 标记，不 fire）。
     * <p>循环体已移入 {@link StatEngine#preventDeflectedMagnitude}（该效果非 MarkerStatEffect，
     * 身份由布尔承载、口径不与标记读口同构，详见该方法注释），使 INERT 折叠口径集中于 StatEngine。
     */
    private static double preventDeflectedMagnitude(LivingEntity living, boolean crit) {
        return StatEngine.preventDeflectedMagnitude(living, crit);
    }

    /**
     * 读持有者 STAT_STORE 中 DEFEND 标记条目、按 conditionTags ⊆ 命中条件折叠的护甲承护幅度总和。
     * <p>折叠口径（不校验 variant / 按 conditionTags 门控 / 不求值 {@code e.conditions()}）已固化为
     * {@link StatEngine#markerMagnitudeByConditionTags} 的签名，见该方法说明。
     */
    private static double defendMagnitude(LivingEntity living, Set<String> cond) {
        return StatEngine.markerMagnitudeByConditionTags(living, MarkerStatEffect.Kind.DEFEND, cond);
    }

    /** 吸收后：能量护盾扣除（独立资源扣除回调）。 */
    private static void stageEnergy(State s) {
        s.newDamage -= s.event.getContainer().getReduction(DamageContainer.Reduction.ABSORPTION);
        // Guard 吸收（X5 族，Stat 驱动）：读 {guard} 作用域 GUARD_RECOVERY（默认 0，比例吸收）。
        // POE2「Flask grant Guard equal to X% of Life Recovery」——Guard 作为承伤前的吸收层，
        // 吸收量为本次伤害 × GUARD_RECOVERY（经 DamageContainer.setReduction(ABSORPTION) 累加吸收，
        // 与既有 ABSORPTION 减除语义一致，setReduction 内部会同步扣减 newDamage）。
        if (s.newDamage > 0 && s.living instanceof LivingEntity living) {
            double guard = DamageHelper.readInScopePublic(living, Set.of(GateTag.GUARD), AttributeRegistry.GUARD_RECOVERY);
            if (guard > 0) {
                float guardAbsorb = (float) Math.min(s.newDamage, s.newDamage * guard);
                if (guardAbsorb > 0) {
                    float existing = s.event.getContainer().getReduction(DamageContainer.Reduction.ABSORPTION);
                    s.event.getContainer().setReduction(DamageContainer.Reduction.ABSORPTION, existing + guardAbsorb);
                    s.newDamage -= guardAbsorb;
                }
            }
        }
        if (s.newDamage > 0 && !s.source.is(DamageTypeTagRegistry.BYPASS_ENERGY)) {
            // 注：「受击转能量」已并入 recoup Effect（resources 含 energy，同一「承伤按比例补给」机制，
            // 经统一 fire 通道触发），管道不再硬编码。
            // 能量护盾承伤顺序统一在此阶段——
            // ① Darkness 预留（41076 Embrace the Darkness：Reserved from Darkness before Life/ES）；
            // ② ES 吸收（ResourceRegistry.absorbEnergyShield，旧 ENERGY 属性已迁移）；剩余承伤 = overflow。
            s.newDamage -= DarknessHelper.reserve(s.living, s.newDamage);
            float esBypass = Math.max(0, (float) readInScope(s.living, Set.of(GateTag.TAKEN, GateTag.ENERGY_SHIELD), AttributeRegistry.DAMAGE_TAKEN_BYPASSES_ENERGY) - 1.0F);
            float esAmount = s.newDamage * (1.0F - esBypass);
            float esOverflow;
            if (esAmount > 0 && ResourceRegistry.getMaxEnergyShield(s.living) > 0) {
                esOverflow = ResourceRegistry.absorbEnergyShield(s.living, esAmount).getSecond();
            } else {
                esOverflow = esAmount;
            }
            float esAbsorbed = Math.max(0, esAmount - esOverflow);
            s.newDamage = esOverflow + s.newDamage * esBypass;
            DamageContext.cacheDamageContext(s.source, s.living, new DamageContext()
                    .damageEnergy(esAbsorbed));
        }
    }

    private static void stageRunicWard(State s) {
        if (s.newDamage > 0 && !s.source.is(DamageTypeTagRegistry.BYPASS_ENERGY)) {
            float wardAmount = s.newDamage;
            float wardOverflow;
            if (wardAmount > 0 && ResourceRegistry.getMaxRunicWard(s.living) > 0) {
                wardOverflow = ResourceRegistry.absorbRunicWard(s.living, wardAmount).getSecond();
            } else {
                wardOverflow = wardAmount;
            }
            float wardAbsorbed = Math.max(0, wardAmount - wardOverflow);
            s.newDamage = wardOverflow;
            DamageContext.cacheDamageContext(s.source, s.living, new DamageContext()
                    .damageRunicWard(wardAbsorbed));
        }
    }

    /** 承受伤害由蓝量承担（Mind Over Matter）：受击方 {mana_take} 作用域的 MANA_TAKE_OF_DAMAGE 按比例把承伤导向 mana，
     *  但仅「被蓝量实际吸收」的那部分从承伤中扣除——剩余承伤（含 mana 不足未能承担的部分）继续走能量护盾/生命阶段
     *  （MoM 只抵消比例伤害、不会完全对消扣血）。先于 stageEnergy 运行，确保 MoM 在 ES 之前承伤。 */
    private static void stageMana(State s) {
        if (s.newDamage > 0 && !s.source.is(DamageTypeTagRegistry.BYPASS_ENERGY)) {
            double manaTake = readInScope(s.living, Set.of(GateTag.MANA_TAKE), AttributeRegistry.MANA_TAKE_OF_DAMAGE);
            if (manaTake > 0) {
                float manaDamage = (float) (s.newDamage * manaTake);
                // 把承伤的 manaTake 比例导向 mana；mana 实际吸收 actual，不足部分 missing 仍计入承伤（走 ES/生命）。
                ResourceEventHelper.SpendResult result = ResourceEventHelper.onSpend(s.living, GateTag.MANA, manaDamage, "mana_take", s.attacker);
                float actual = manaDamage - result.missing(); // 蓝量实际吸收的承伤量
                DamageContext.cacheDamageContext(s.source, s.living, new DamageContext().damageMana(actual));
                s.newDamage -= actual; // 仅扣除被蓝量实际承担的部分；剩余（1-manaTake 比例 + mana 不足部分）继续承伤
            }
        }
    }

    /** 承伤由「可受伤同伴」生命承担（在自身生命之前，node 32799）：命中伤害的该比例先扣同伴生命，剩余继续走后续资源阶段。
     *  pct 取自受击方 DAMAGE_TAKE_SHARE 属性的 {companion} scope（零基百分比，ADD_VALUE 累加）；仅命中伤害（HitDamage）生效，
     *  异常 DoT 不参与。遍历受击方的「可受伤同伴」（{@link RelationCondition#resolveCompanions} 按 IS_COMPANION 标记解析），
     *  取首个存活同伴承担该比例承伤，仅扣除其实际承担的部分。 */
    private static void stageCompanion(State s) {
        if (s.newDamage <= 0 || s.isAilment) return;               // 仅命中伤害（HitDamage）
        double pct = readInScope(s.living, Set.of(GateTag.COMPANION), AttributeRegistry.DAMAGE_TAKE_SHARE);
        if (pct <= 0) return;
        for (LivingEntity companion : RelationCondition.resolveCompanions(s.living)) {
            if (companion == null || companion == s.living || companion.isDeadOrDying()) continue;   // 可受伤同伴
            float redirect = (float) (s.newDamage * Math.min(1.0, pct));
            companion.hurt(s.source, redirect);
            s.newDamage -= redirect;                              // 仅扣除「同伴实际承担」的部分
            break;                                               // 单一「你的同伴」
        }
    }

    /** 生命扣除（含吸血、中毒眩晕、溢出伤害，独立资源扣除回调）。 */
    private static void stageHealth(State s) {
        if (s.newDamage > 0) {
            // corruption_endures（keystone 49769）：7% chance to Avoid Death from Hits——
            // 致死伤害（newDamage >= 当前生命）按 CorruptionEnduresStatEffect 幅度掷骰压至保留 1 HP。
            // 幅度由节点经 CorruptionEnduresStatEffect（INERT 标记）写入持有者 STAT_STORE，stage 直接遍历本条目求和；
            // 节点取消分配即条目移除、免死立即失效（无 Buff 残留泄漏，对比 StatusStatEffect 经 StatusEngine 施加的 StatusInstance）。
            if (s.newDamage >= s.living.getHealth()) {
                // 免死几率（#17）：corruption_endures keystone 通道 + 通用 CHANCE_TO_AVOID_DEATH 属性（分数，直接并入掷骰）。
                double avoid = Math.clamp(corruptionEnduresChance(s.living) + s.living.getAttributeValue(AttributeRegistry.CHANCE_TO_AVOID_DEATH), 0.0, 1.0);
                if (avoid > 0 && s.living.getRandom().nextDouble() < avoid) {
                    s.newDamage = Math.max(0, s.living.getHealth() - 1.0f);
                }
            }
            // 受击眩晕保留为游戏既有机制（类似元素异常），不经 Effect 抽离
            if (!s.isAilment && s.newDamage < s.living.getHealth()
                    && !AilmentHelper.avoidsPublic(s.living, s.random, StatusDefs.STUN.id)) {
                AilmentHelper.applyStunAilment(s.source, s.living, s.random, s.damages);
                // 眩晕你者致盲（#9）：你被敌人眩晕的瞬间，若持 BLIND_ON_STUN_TAKEN 标记（≥1），
                // 对眩晕者（攻击者）施加 BLIND 减益（-20% 命中/闪避，4s）。
                if (StatEngine.gateOf(s.living, "blind_on_stun_taken", Set.of())
                        && s.attacker instanceof LivingEntity stunner) {
                    // 幅度/时长走 AilmentHelper 唯一来源（#48：写死 20.0 / 80 ticks 会让 INC_BLIND_EFFECT 变死线）。
                    StatusEngine.apply(stunner, StatusDefs.BLIND, s.living,
                            AilmentHelper.blindMagnitude(s.living, stunner),
                            AilmentHelper.blindDurationTicks(s.living, stunner), 1);
                }
                // 定身敌人致恐吓（#30）：你以眩晕定身敌人（受害者为 s.living）的瞬间，
                // 经 StatEngine.fire 触发 ENEMIES_ARE_INTIMIDATED_FOR_SECONDS_WHEN_YOU_IMMOBILISED_THEM
                // （EFF_GRANT_ON_EVENT_INTIMIDATE_ON_IMMOBILISE，作用域 {immobilise} + 事件 caused_ailment），
                // 由 Stat 引擎按词条 {X} 秒授予受害敌人 INTIMIDATE（±10%，时长 = 秒数 × 20 ticks，amount≤0 自动跳过）。
                if (s.attacker != null && s.attacker != s.living) {
                    StatEngine.fire(s.attacker, s.living,
                            StatContext.of(Set.of(GateTag.IMMOBILISE), SkillDamageSource.skillIdOf(s.source), EventName.CAUSED_AILMENT, null), null);
                }
            }
            // Darkness 预留 + ES 吸收统一在 stageEnergy；此处仅扣生命。
            Pair<Float, Float> health = HealthHelper.value(s.living, s.newDamage);
            DamageContext.cacheDamageContext(s.source, s.living, new DamageContext()
                    .damageHealth(health.getFirst()).overflowDamage(health.getSecond()));
            // P5 伤害数值回调闭环：实际造成量 / 溢出量写入 Stat 通道（受害者侧 + 攻击者侧对称），
            // 供「造成/承受 X 点伤害时触发效果」「溢出伤害额外造成 Y」等 on-cause 数值条件读取。
            // 对照回复侧 Event.recordHeal 范式——数值经 Event.State 缓存，由数学节点检测。
            Event.recordDamageTaken(s.living, GateTag.HEALTH, health.getFirst(), health.getSecond());
            if (s.attacker != null && s.attacker != s.living) {
                Event.recordDamageCaused(s.attacker, GateTag.HEALTH, health.getFirst(), health.getSecond());
            }
        }
    }

    /** 补给（Recoup）：已抽离为 recoup Effect（经统一 fire 通道触发），管道不再硬编码。 */

    /** 触发型 stat 效果：击中（及暴击）事件——「击中回蓝 / 击中敌人施加异常」等。 */
    private static void stageTrigger(State s) {
        // B-0.2 守卫：Stat 触发型伤害（EffectApplier.damage 携带 statTriggered 标记）不再二次触发 stat 效果，
        // 斩断「击中时造成伤害 → 伤害管线 → 再次 fire」的自我重入递归（原实现会消耗 FIRE_DEPTH 预算）。
        if (s.source instanceof SkillDamageSource sds && sds.isStatTriggered()) return;
        Entity direct = s.source.getDirectEntity();
        if (!s.isAilment && s.attacker != null && s.attacker != s.living) {
            // 每侧单次 fire 取代 hit + caused_hit + caused_crit 三次发射。
            // 标签 = 命中事件名(caused_hit / taken_hit) ∪ 形态标签 ∪ 伤害元素(s.tags) ∪ (crit)。
            // 造成侧与承受侧共用同一份 extra 标签，两侧完全对称——承受侧同样能按元素/暴击匹配
            // （如「受到火焰伤害时触发」「被暴击时 X」）——承受侧不可缺元素维度。
            Set<String> extraTags = new HashSet<>(s.tags);
            // s.tags 是 fold（承伤前读取）作用域标签，其中的 taken_hit 属「触发维」而非修饰维——
            // 带进 fire 会让攻击者的「被命中时触发」条目在其攻击时误触发（触发维串味）。
            // fire 的触发维一律由 GameEvent 的 eventName（caused_hit / taken_hit）单独提供。
            extraTags.remove(EventName.TAKEN_HIT);
            if (s.crit) extraTags.add(GateTag.CRIT);
            Set<String> takenTags = new HashSet<>(extraTags);
            if (s.blocked) takenTags.add(GateTag.DEFLECT);
            Identifier skill = SkillDamageSource.skillIdOf(s.source);
            Set<String> recencyTags = GameEvent.recencyTagsOf(s.source, direct, s.tags);

            // 分维 recency 记录（math recently/count）独立于 fire 通道，两侧对称先行写入。
            GameEvent.recordTaggedHit(s.attacker, EventName.CAUSED_HIT, recencyTags);
            GameEvent.recordTaggedHit(s.living, EventName.TAKEN_HIT, recencyTags);
            // 暴击的 recency 键独立保留：其 fire 已并入本次命中，但仍需自己的计数/计时通道（两侧对称）。
            // 修：原用 GameEvent.recordTimer 只写 timer store，recently/count("caused_crit") 恒 false；
            // 改用 Event.record（内部含 refreshTimer，语义超集），使「近暴击」类判定可用。
            // 「近 N 秒暴击过」亦可走分维标签 recently:caused_hit:crit（由 recordTaggedHit 写入）。
            if (s.crit) {
                Event.record(s.attacker, EventName.CAUSED_CRIT);
                Event.record(s.living, EventName.TAKEN_CRIT);
                // R3 修复 #65：CAUSED_CRIT 此前只经 Event.record 写入计数队列（喂 recently/count 条件），
                // 不触发 GrantOnEventStatEffect（须 StatEngine.fire）。补真实发射点（owner=target=攻击者自身，
                // 镜像 form_cleared 自授予模式：GrantOnEventStatEffect.STATUS 取 triggerTarget=自身授予奥术涌动）。
                StatEngine.fire(s.attacker, s.attacker, StatContext.of(Set.of("caused_crit"), skill, EventName.CAUSED_CRIT, null), null);
                // 承受侧对称闭环（A-3 双向回调）：受害者同样经 StatEngine.fire 触发「被暴击时 X」on-taken 效果，
                // 镜像造成侧 caused_crit 发射——此前 TAKEN_CRIT 仅经 Event.record 写计时队列、不 fire，致双向事件单侧触发。
                StatEngine.fire(s.living, s.attacker, StatContext.of(Set.of("taken_crit"), skill, EventName.TAKEN_CRIT, null), null);
                // 暴击元事件派发（镜像 DeathHelper 的击杀元事件）：EventListenPhase 只能订阅
                // SkillEventBus，而暴击此前只写 recently/count 与 StatEngine.fire 两条通道，
                // 「暴击命中<b>带元素异常的</b>敌人 → 生成残片」一类常驻监听无从订阅（静默死线）。
                // 标签并入受害者活跃状态维（带点燃/感电等时含 elemental），使 require_tags
                // 能精确表达真值门控——与击杀侧 killTags 同口径，两条通道语义一致。
                if (s.source instanceof SkillDamageSource critSds && critSds.getSkill() != null) {
                    Set<String> critTags = new HashSet<>(Set.of("caused_crit", GateTag.CRIT));
                    critTags.addAll(StatusEngine.activeTags(s.living));
                    SkillEventBus.publishMeta(s.attacker, critSds.getSkill(), EventName.CAUSED_CRIT,
                            critTags.toArray(new String[0]));
                }
            }

            // 构造伤害上下文载体，经统一 fire 通道传给各 StatEffect（偷取 / 补给 / 受击转能量等
            // 「按造成伤害 / 承受伤害比例回复资源」类效果），使其无需在管线中硬编码副作用。
            // takenPreBlock=State.totalDamageTaken（招架/格挡前，供补给/受击转能量记账）；
            // takenPostBlock=State.newDamage（招架/格挡后，供偷取守卫，被完全招架/格挡时不偷取）。
            TriggerContext tc = new TriggerContext(s.damages,
                    s.totalDamageTaken, s.newDamage, skill, s.blocked);
            // 命中上下文注入（hit_state:* 在 Stat 条件可用）：本次伤害以「命中受害者实体」呈现，
            // 造成/承受两侧共用同一份（两侧 stat 条件均判定「本次命中了实体」）。
            // distance 用攻击者→受害者的当前距离近似（管道无投递物命中距离概念；skill delivery 侧有真实距离）。
            HitContext hc = HitContext.entityHit(s.living, s.living.position(), Vec3.ZERO,
                    (float) s.attacker.distanceTo(s.living));
            // 槽位接线（SelectorNode.slot）：攻击者 "hit" 槽 = 本次命中目标；受害者 "hitter" 槽 = 本次攻击者。
            // 使 select:{"slot":"hit"} / {"slot":"hitter"} 可选到最近一次直接命中双方。
            ExpressionContext.get(s.attacker).setSlot(SLOT_HIT, s.living);
            ExpressionContext.get(s.living).setSlot(SLOT_HITTER, s.attacker);
            // 造成侧：owner=攻击者，触发「击中回蓝 / 击中敌人施加异常」等 on-cause 效果。
            GameEvent.caused(s.source, direct, EventName.CAUSED_HIT)
                    .withHit(hc)
                    .fireCaused(s.attacker, s.living, skill, extraTags, tc);
            // 承受侧：owner=受害者，触发「被命中时回血 / 被暴击时 X」等 on-taken 效果。
            GameEvent.taken(s.source, direct, EventName.TAKEN_HIT)
                    .withHit(hc)
                    .fireTaken(s.living, s.attacker, skill, takenTags, tc);
        }
    }

    // ============================ 阶段内复用的私有辅助（原 LivingDamageEventPre 内联） ============================

    /**
     * 状态承伤修正（消费端，poe2db 口径）：
     * <ul>
     *   <li>感电（SHOCK）：承受全部伤害提高 magnitude%（基准 20%，含 DoT）；</li>
     *   <li>凋零（WITHER）：承受混沌伤害每层 +6%（上限 10 层）；</li>
     *   <li>传奇/天赋独特加成（读攻击者属性，仅对命中伤害）：
     *       enemy_while_electrocuted_damage_taken（被你电击的敌人承伤提高）、
     *       enemy_while_frozen_cold_damage_taken（冰冻敌人承受冰伤提高）、
     *       fully_broken_armor_increases_fire_damage_taken_on_hit（完全破甲敌人承受火伤提高）、
     *       damage_against_fully_broken_armor（对完全破甲敌人的伤害提高）。</li>
     * </ul>
     * damages 索引：0=物理 1=混沌 2=火 3=冰 4=雷。
     */
    private static void applyStatusDamageTakenModifiers(DamageSource source, LivingEntity living,
                                                        LivingEntity attacker, Set<String> hitTags, float[] damages) {
        // ── 承伤变动（总承伤 + 类型承伤）统一通道 ──
        // 承伤 stat 条目统一落 TAKEN_DAMAGE 属性（StatusBehavior.damageTaken：感电/恐吓/加重/护体/玩家
        // INC_TAKEN_DAMAGE 为无标签全局条目 = 总承伤增加；凋零带 chaos / 穿刺带 physical = 类似凋零的
        // 类型承伤），承伤结算在受击方作用域（命中标签）读属性一次聚合：
        // 无标签条目恒命中（总承伤增加）；带元素标签条目按命中元素命中（仅该类型成分承伤增加）。
        // 取代原 SHOCK/WITHER 硬编码分支（等价倍率、通道统一，DoT 亦经同一属性读取）。
        float takenMul = (float) readInScope(living, hitTags, AttributeRegistry.TAKEN_DAMAGE);
        if (takenMul != 1.0F && takenMul > 0) for (int i = 0; i < damages.length; i++) damages[i] *= takenMul;
        // 「被当前攻击者点燃的历史秒数」→ 递增承火伤（node 43939：每秒 +1%，封顶 +10%），仅放大火焰成分。
        // 置于 attacker==null/ailment 早退之前，使点燃 DoT 本身（source 为异常）亦被放大；秒数由 StatusBehavior#dealSplitDoT 按施加者累计。
        if (attacker != null && attacker != living) {
            double bonus = StatEngine.igniteHistoryFireTakenBonus(attacker, living);
            if (bonus > 0.0) damages[2] *= (float) (1.0 + bonus);
        }
        // 被招架（PARRIED）：受击者处于被招架状态时承伤大增（全伤害，含 DoT）。
        // 承伤倍率 = 1 + 施加时存的 magnitude（magnitude 已由招架者的 Parried Debuff Effect 放大）。
        // 该状态无承伤 stat 条目（仅减速 actionSpeed），故保留管线硬编码（不参与属性通道）。
        if (StatusEngine.has(living, StatusDefs.PARRIED)) {
            float parriedMul = 1.0F + (float) StatusEngine.magnitude(living, StatusDefs.PARRIED);
            if (parriedMul != 1.0F) for (int i = 0; i < damages.length; i++) damages[i] *= parriedMul;
        }
        // ── 图腾承伤光环（5580 Watchtowers）已移出承伤管道 ──
        // 「Each Totem applies X% increased Damage taken to Enemies in their Presence」现由
        // PresenceAuraStatEffect 授予模式（EFF_PRESENCE_AURA_TAKEN_DAMAGE，接线见 WirePerScaling）驱动：
        // 光环把一条 TAKEN_DAMAGE(ADD_MULTIPLIED_BASE) 条目写进<b>敌人自己的</b> STAT_STORE（不建状态、不硬编码图腾数），
        // 「每个图腾」由接线侧注入的 presence_count:ally.totem 缩放基数在光环刷新时求值。
        // 本管道不再参与，原 countTotemsNear 硬编码扫描一并移除。
        // 注：承伤读取（本方法 readInScope）推入的作用域 sourceEntity=null，跨实体（攻击者→受害者）维度不参与聚合，
        // 故「敌人承伤增加」类条目必须落在受害者自身 store 上——状态经 StatusStatBridge、光环经 PresenceAura 各自写入。
        // 传奇/天赋独特加成：读攻击者属性（DAMAGE 单属性 + 状态条件标签作用域），仅对命中伤害生效
        if (attacker == null || attacker == living || source.is(DamageTypeTagRegistry.IS_AILMENT)) return;
        if (StatusEngine.has(living, StatusDefs.ELECTROCUTE)) {
            // 对被电击敌人的伤害提高（替代 ENEMY_WHILE_ELECTROCUTED_DAMAGE_TAKEN）
            float mul = (float) readInScope(attacker, Set.of(GateTag.DAMAGE, GateTag.ELECTROCUTE), AttributeRegistry.DAMAGE);
            if (mul != 1.0F) for (int i = 0; i < damages.length; i++) damages[i] *= mul;
        }
        // ── 锯齿地面承伤加成：旧「存在性状态」模型硬编码分支已退役 ──
        // 现由通用地面机制驱动：锯齿地面的承伤加成（如 TAKEN_DAMAGE 增加）作为地面 stats 载荷，
        // 经 GroundFieldManager 注入站在地面上实体的 STAT_STORE，本承伤管道自然读取（数据驱动，
        // 见 GroundTypes.def(ON_JAGGED_GROUND).stats()）。注：该 stats 当前为占位空表，
        // 待词缀/技能数据填入真实效果后即闭环（用户裁定「退役并改数据点」）。
        StatusInstance abFully = StatusEngine.query(living, StatusDefs.ARMOUR_BREAK);
        if (abFully != null && abFully.fullyBroken) {
            // 对完全破甲敌人的火焰伤害提高（替代 FULLY_BROKEN_ARMOR_INCREASES_FIRE_DAMAGE_TAKEN_ON_HIT）
            damages[2] *= 1.0F + readInScope(attacker,
                    Set.of(GateTag.DAMAGE, GateTag.FIRE, GateTag.FULLY_BROKEN_ARMOR), AttributeRegistry.DAMAGE);
            // 对完全破甲敌人的伤害提高（替代 DAMAGE_AGAINST_FULLY_BROKEN_ARMOR）
            float mul = (float) readInScope(attacker, Set.of(GateTag.DAMAGE, GateTag.FULLY_BROKEN_ARMOR), AttributeRegistry.DAMAGE);
            if (mul != 1.0F) for (int i = 0; i < damages.length; i++) damages[i] *= mul;
        }
    }

    /**
     * 预减免阶段：把 {@code damages[src]} 的伤害按全量规则重路由到其它槽位。
     *
     * <p>遍历 {@code rules}（来自 {@link StatEngine#damageConvert}）；凡 {@code effect.fromTags()} 含本槽位
     * 类型标签者命中，对每条命中规则：
     * <ul>
     *   <li>{@code REPLACE}：从源槽位扣除 {@code ratio} 份额，加到目标槽位（去A取B）。</li>
     *   <li>{@code GAIN}：保留源槽位全部，额外以目标槽位承受 {@code ratio} 份额（共存也获得B）。</li>
     * </ul>
     * 在抗性/护甲减免（applyDamageReductions）之前执行，使重路由后的伤害被正确类型减免。
     * 来源判定用 {@code effect.fromTags().contains(fromTag)}——fromTags 为来源集合（多项取并集，各自独立匹配），
     * 如 {@code {elemental}} 会同时命中火/冰/电三槽；{@code {fire,cold}} 命中火槽与冰槽（非"须同时含二者"）。
     *
     * @param rules  全量已满足作用域条件的伤害类型转换规则
     * @param src    源伤害槽位（0=物理 1=混沌 2=火 3=冰 4=电）
     * @param damages 伤害数组（就地修改）
     */
    private static void applyDamageTypeConversion(List<StatEngine.DamageConversion> rules, int src, float[] damages,
                                                   boolean includeHitsOnly, Set<String> hitCtx) {
        if (damages[src] <= 0 || rules.isEmpty()) return;
        String fromTag = DamageTypeRegistry.DAMAGE_TYPE_TAGS[src];
        float srcAmount = damages[src];
        for (var conv : rules) {
            // hits-only 门控：仅命中维度（includeHitsOnly=true）才应用；DoT 维度跳过
            if (conv.hitsOnly() && !includeHitsOnly) continue;
            // 命中上下文门控（from_context，参照 Recoup.fromType 的「按条件选基数」范式）：
            // fromContext 非恒真时仅当本次命中上下文标签（crit/ailment…）命中才执行转换
            if (!conv.fromContext().isAlwaysTrue() && !conv.fromContext().matches(hitCtx)) continue;
            if (!conv.fromTags().contains(fromTag)) continue;
            float moved = srcAmount * conv.ratio();
            if (moved <= 0) continue;
            if (conv.mode() == DamageConvertStatEffect.Mode.NULLIFY) {
                // 禁止造成该类型伤害：直接从来源槽位扣除，不转入任何目标（与 REPLACE/GAIN 不同，无 to）
                damages[src] -= moved;
                continue;
            }
            int toSlot = DamageTypeRegistry.slotOf(conv.to());
            if (toSlot < 0 || toSlot == src) continue;
            if (conv.mode() == DamageConvertStatEffect.Mode.REPLACE) {
                damages[src] -= moved;
            }
            damages[toSlot] += moved;
        }
    }

    // 怒气获取走 ResourceStatEffect（Stat 条目 {"type":"heal","resource":"rage"...}），不经 HitGain 机制
    // 经 ResourceEventHelper 产出（与击回/偷取同通道），无需属性中介 + 规格表。

    /** 在标签作用域内实时读取某属性（与 AilmentHelper.withScope 同源；作用域标签即条件，属性名不带条件词）。 */
    private static float readInScope(LivingEntity entity, Set<String> tags, Holder<Attribute> attr) {
        if (entity == null) return 0f;
        AutoCloseable scope = StatScopes.enter(entity, StatContext.of(new HashSet<>(tags), null));
        try {
            return (float) entity.getAttributeValue(attr);
        } finally {
            try {
                scope.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 同 {@link #readInScope}，额外绑定「命中交互对方」为 {@code sourceEntity}。
     *
     * <p><b>为什么需要这个重载（死线 #37）</b>：{@link #readInScope} 用 2 参 {@code StatContext.of(tags, null)}，
     * 该工厂<b>恒置 {@code sourceEntity = null}</b>；而 {@code StatEngine.compute} 以
     * {@code new StatEvalContext(living, src)} 把 {@code src = readScope.sourceEntity()} 当作 {@code triggerTarget}。
     * 于是任何 {@code onTarget(...)} / {@code .conditionTarget(EVENT_TARGET)} 形式的条目
     * （它们要求条件在「命中对方」身上求值）在这种读点下拿到的 target 是 null，
     * 被 {@code StatEngine.aggregate} 的 {@code condEntity == null → continue} 静默跳过 → <b>永不生效</b>。
     *
     * <p>本重载保留原标签集、仅补上 {@code withSourceEntity(against)}（与 {@code DamagePipeline} 维度C
     * 伤害转换读点的 {@code StatContext.EMPTY.withSourceEntity(s.living)} 同法），使条件能在对方身上正确求值，
     * 且不并入外层标签（用 {@code enter} 而非 {@code enterMerged}，避免作用域意外变宽）。
     *
     * <p>注意：启用 sourceEntity 会同时打开 {@code StatEngine.compute} 的跨实体聚合分支——
     * 它只聚合对方 store 中 {@code resolveTargets(target).contains(reader)} 的条目（即对方身上明确指向本读者者），
     * 对方自己 target=SELF 的条目不会被误计入，故安全。
     */
    private static float readInScopeAgainst(LivingEntity entity, Set<String> tags, Holder<Attribute> attr,
                                            @Nullable LivingEntity against) {
        if (entity == null) return 0f;
        AutoCloseable scope = StatScopes.enter(entity,
                StatContext.of(new HashSet<>(tags), null).withSourceEntity(against));
        try {
            return (float) entity.getAttributeValue(attr);
        } finally {
            try {
                scope.close();
            } catch (Exception ignored) {
            }
        }
    }
}
