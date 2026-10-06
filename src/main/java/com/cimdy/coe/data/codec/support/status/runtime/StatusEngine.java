package com.cimdy.coe.data.codec.support.status.runtime;

import com.cimdy.coe.data.codec.support.status.def.StatusDefs;
import com.cimdy.coe.data.codec.support.status.def.StatusKind;
import com.cimdy.coe.data.codec.support.status.def.StatusType;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.data.codec.support.meta.Camp;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.damage.DamageTypes;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * 统一状态引擎（取代 AilmentHelper 散落逻辑 + MobEffect 特例叠加）。
 *
 * <p>提供：apply（施加/合并）/ query（查询）/ tick（每 tick 衰减 + 回调）/ clear。
 * 施加结果写入实体的 {@code StatusStore} 附件。状态彼此独立（文档 §0.4）。
 *
 * <p>注意：本引擎为骨架（Phase 1）。各 {@link StatusType#tickLogic} 后续接入实际 DoT /
 * 控制 / 增益逻辑；属性层（Strength）后续在 tickLogic 内触发对应 Attribute 修饰。
 */
public final class StatusEngine {

    private StatusEngine() {}

    /** 强制叠层时的层数上限（默认不可叠加状态在被强制叠层时使用的宽松上限）。 */
    private static final int FORCED_MAX_STACKS = 1000;

    /** Archon 类别标签：用于互斥判定（持有任一 archon 标签状态即视为已持 Archon）。 */
    private static final String ARCHON_TAG = "archon";
    /** Archon 恢复冷却状态 id。 */
    private static final String ARCHON_RECOVERY_ID = StatusDefs.ARCHON_RECOVERY.id;
    /** 形态类别标签：用于互斥判定（持有任一 form 标签状态即视为已变形）。 */
    private static final String FORM_TAG = "form";

    /** 在施法者 {status, 状态id} 作用域下聚合读取整数型加成（层数上限加算 / 施加时额外层数）；无施法者返回 0。 */
    private static int scopedStacks(LivingEntity caster, StatusType type, Holder<Attribute> attr) {
        if (caster == null) return 0;
        AutoCloseable s = StatScopes.enter(caster, StatContext.of(Set.of(GateTag.STATUS, type.id), null));
        try {
            return (int) Math.round(caster.getAttributeValue(attr));
        } finally {
            try { s.close(); } catch (Exception ignored) {}
        }
    }

    /**
     * 技能直接附加自身为状态：状态 <b>id = 技能 id</b>（数据驱动，运行时按需
     * 动态注册 {@link StatusType#dynamic}，幂等）、<b>图标 = 技能图标</b>（gui/gem/&lt;技能名&gt;.png
     * 经 {@link StatusInstance#iconOverride} 覆盖 HUD）——区别于「通用标记状态」方案：
     * 每个技能即独立状态，目标持有的是该技能本身。
     *
     * @param target        承受方（诅咒技能 = 范围内敌人）
     * @param caster        施法者
     * @param skillId       技能 id（状态 id 来源）
     * @param kind          状态大类（诅咒技能 = {@link StatusKind#CURSE}）
     * @param magnitude     强度（纯标记状态传 1.0）
     * @param durationTicks 持续时间（技能未显式给定时按此值）
     */
    public static void applySkill(LivingEntity target, LivingEntity caster, Identifier skillId,
                                  StatusKind kind, double magnitude, int durationTicks) {
        StatusType type = StatusType.dynamic(skillId.toString(), kind, durationTicks);
        if (kind == StatusKind.CURSE && !reserveCurseSlot(target, caster, type)) return;
        Identifier icon = Identifier.fromNamespaceAndPath(skillId.getNamespace(),
                "textures/gui/gem/" + skillId.getPath() + ".png");
        apply(target, type, caster, magnitude, durationTicks, 1, false, false, icon);
    }

    /**
     * 诅咒额度门控：为待施加的诅咒腾出位置（POE2「You can apply an additional Curse」）。
     *
     * <p>目标身上的诅咒数（不含本技能自身的刷新）达到施法者额度时，顶掉<b>剩余时间最短</b>的一个——
     * 新诅咒不会被静默丢弃，被顶掉的是即将失效的旧诅咒。额度读<b>施法者</b>的 {@code MAX_CURSES}
     * （POE2 中额度属施法者维度：多玩家对同一目标各有各的额度），缺省回退 1。
     *
     * @return false = 施法者额度 ≤ 0，不可施加诅咒，本次施加整条作废（不顶替、不施加）。
     */
    private static boolean reserveCurseSlot(LivingEntity target, @Nullable LivingEntity caster, StatusType type) {
        double limit = caster != null && caster.getAttributes().hasAttribute(AttributeRegistry.MAX_CURSES)
                ? caster.getAttributeValue(AttributeRegistry.MAX_CURSES) : 1.0;
        if (limit <= 0) return false;
        StatusStore store = store(target);
        List<Map.Entry<StatusType, StatusInstance>> cursed = new ArrayList<>();
        for (Map.Entry<StatusType, StatusInstance> e : store.instances.entrySet()) {
            if (e.getKey().kind == StatusKind.CURSE && e.getKey() != type) cursed.add(e);
        }
        while (cursed.size() + 1 > limit) {
            Map.Entry<StatusType, StatusInstance> weakest = null;
            for (Map.Entry<StatusType, StatusInstance> e : cursed) {
                if (weakest == null || e.getValue().durationTicks < weakest.getValue().durationTicks) weakest = e;
            }
            if (weakest == null) break;
            clear(target, weakest.getKey());
            cursed.remove(weakest);
        }
        return true;
    }

    /**
     * 诅咒技能施放：对施法者周围 {@code radius} 格内<b>敌方</b>实体施加「技能即状态」
     * （{@link #applySkill}，kind=CURSE，id=技能 id、图标=技能图标）。敌我判定沿
     * {@code TargetSelector.resolvePresenceEnemies} 的阵营语义：目标阵营与施法者不同且非 NONE。
     */
    public static void applyCurseToNearby(LivingEntity caster, Identifier skillId,
                                          double radius, int durationTicks) {
        Camp ownerCamp = caster.getData(AttachmentRegistry.CAMP);
        if (ownerCamp == Camp.NONE) return;
        var entities = caster.level().getEntitiesOfClass(LivingEntity.class,
                caster.getBoundingBox().inflate(radius));
        for (LivingEntity e : entities) {
            if (e == caster) continue;
            Camp c = e.getData(AttachmentRegistry.CAMP);
            if (c != Camp.NONE && c != ownerCamp) {
                applySkill(e, caster, skillId, StatusKind.CURSE, 1.0, durationTicks);
            }
        }
    }

    /**
     * 施加状态。合并规则：
     * <ul>
     *   <li>可叠层(stackable)：层数累加至 maxStacks，时长取较大值，强度取较大值。</li>
     *   <li>不可叠层：替换实例（刷新时长 / 取较大强度）。</li>
     * </ul>
     */
    private static StatusStore store(LivingEntity target) {
        StatusStore s = target.getData(AttachmentRegistry.STATUS_STORE);
        if (s == StatusStore.EMPTY) {
            s = new StatusStore();
            target.setData(AttachmentRegistry.STATUS_STORE, s);
        }
        return s;
    }

    public static void apply(LivingEntity target, StatusType type, LivingEntity caster,
                             double magnitude, int durationTicks, int stacks) {
        apply(target, type, caster, magnitude, durationTicks, stacks, false, false, null);
    }

    /**
     * 施加状态，可携带技能图标（{@code iconOverride} 非 null 时该状态 HUD 使用技能图标而非 gui/status/&lt;id&gt;）。
     */
    public static void apply(LivingEntity target, StatusType type, LivingEntity caster,
                             double magnitude, int durationTicks, int stacks, Identifier iconOverride) {
        apply(target, type, caster, magnitude, durationTicks, stacks, false, false, iconOverride);
    }

    /**
     * 施加状态，可携带技能图标（{@code iconOverride} 非 null 时该状态 HUD 使用技能图标而非 gui/status/&lt;id&gt;）。
     *
     * <p>叠层规则（Stat 驱动）：
     * <ul>
     *   <li>层数上限 = {@code type.maxStacks} + 施法者 {@code status_max_stacks}（{status, 状态id} 作用域加算）。
     *       默认不可叠层的状态（上限 1）经 +N 后即可叠至 1+N 层——即「上限增加」型接口
     *       （poe2 实证：Comorbidity「+1 层中毒上限」同型）。</li>
     *   <li>{@code type.maxStacks <= 0} = <b>无上限</b>（{@code StatusTypeDef.Builder#uncapped()}）：
     *       cap 直接取 {@code Integer.MAX_VALUE}，不叠加 {@code status_max_stacks}、也不夹紧；
     *       溢出陷阱见 {@link #maxStacks(int)} 的 javadoc。</li>
     *   <li>上限计算以 {@code type.maxStacks} 为基（恒 ≥ 1，见 {@code StatusTypeDef.Builder#maxStacks(int)} 的
     *       哨值溢出陷阱说明）：{@code cap = max(1, maxStacks + capBonus)}。填 {@code Integer.MAX_VALUE}
     *       之类哨值会随 + capBonus 溢出成负数、被 {@code Math.max(1, …)} 压回 1，故上限一律走显式小值声明。</li>
     *   <li>施法者 {@code status_extra_stacks}：每次施加额外 +N 层（对可叠层状态如中毒即「造成中毒时层数增加」）。</li>
     * </ul>
     *
     * @param forceStack 为 true 时忽略上限判定强制叠层（程序侧接口，上限放宽至 {@link #FORCED_MAX_STACKS}）。
     */
    public static void apply(LivingEntity target, StatusType type, LivingEntity caster,
                             double magnitude, int durationTicks, int stacks, boolean auraSourced) {
        apply(target, type, caster, magnitude, durationTicks, stacks, false, auraSourced, null);
    }

    public static void apply(LivingEntity target, StatusType type, LivingEntity caster,
                             double magnitude, int durationTicks, int stacks, boolean forceStack, boolean auraSourced, Identifier iconOverride) {
        // 免疫短路（ImmuneStatEffect，fold 型查询）：目标对该状态免疫则整体跳过施加
        if (StatEngine.isImmune(target, type)) return;
        // Charm 中央钩子（原则①）：任何异常/控制/眩晕状态落地前，先问玩家是否装了对应免疫护符；
        // 命中则消耗充能、授予免疫状态并抢占原状态施加（与 isImmune 同构，无 per-feature 回调散落）。
        if (com.cimdy.coe.item.charm.CharmEngine.onStatusApplied(target, type)) return;
        // Archon 互斥 + 恢复守卫（中央入口，按标签判定；非 archon 状态直接放行）
        if (archonBlocked(target, type)) return;
        int capBonus = scopedStacks(caster, type, AttributeRegistry.STATUS_MAX_STACKS);
        int extraStacks = Math.max(0, scopedStacks(caster, type, AttributeRegistry.STATUS_EXTRA_STACKS));
        // maxStacks <= 0 = 无上限声明（uncapped()）：跳过夹紧，cap = Integer.MAX_VALUE；
        // Math.min(cap, ...) 在这种情况下天然放行，且不存在 maxStacks + capBonus 的溢出路径。
        int cap = forceStack ? FORCED_MAX_STACKS
                : (type.maxStacks <= 0 ? Integer.MAX_VALUE : Math.max(1, type.maxStacks + capBonus));
        boolean canStack = type.stackable || forceStack || cap > 1;

        StatusStore store = store(target);
        StatusInstance existing = store.get(type);
        if (existing != null && canStack) {
            // 叠层：累加层数（+施加时额外层数）、取较高持续时间/强度
            existing.stacks = Math.min(cap, existing.stacks + Math.max(1, stacks) + extraStacks);
            existing.durationTicks = Math.max(existing.durationTicks, scaleStatusDuration(caster, target, type, durationTicks));
            existing.magnitude = Math.max(existing.magnitude, magnitude);
            existing.source = caster == null ? null : caster.getUUID();
            existing.auraSourced = auraSourced;
            existing.effectMul = effectMul(target, type, caster, auraSourced);
            if (iconOverride != null) existing.iconOverride = iconOverride;
            // 层数变化即时刷新 stat 贡献（否则要等每秒 resync，创口/穿刺层数变化最多滞后 1 秒）。
            // 无 statProvider 的状态：StatusStatBridge.remove/add 各自短路（payload 为空 / provider 为空），
            // 均为空操作，故无需额外 hasProvider 守卫。
            StatusStatBridge.remove(target, existing);
            StatusStatBridge.add(target, existing);
        } else {
            // 不可叠层且已存在：先移除旧 stat 贡献，避免残留（resync 另有每秒兜底）。
            if (existing != null) StatusStatBridge.remove(target, existing);
            StatusInstance inst = new StatusInstance(type, magnitude, scaleStatusDuration(caster, target, type, durationTicks),
                    Math.min(cap, Math.max(1, stacks) + extraStacks),
                    caster == null ? null : caster.getUUID(), 0.0, null, iconOverride);
            inst.auraSourced = auraSourced;
            inst.effectMul = effectMul(target, type, caster, auraSourced);
            inst.appliedTick = target.level().getGameTime();
            store.put(inst);
            StatusStatBridge.add(target, inst);
        }
    }

    /**
     * 施加伤害性异常（DoT）：携带按来源伤害类型拆分的 {@code composition}（索引对齐
     * DamageTypeRegistry.DAMAGE_TYPE_TAGS：0=物理 1=混沌 2=火焰 3=冰霜 4=闪电）。
     * 各份额外会在 tick 时以各自来源伤害类型（或被 ailment_output_convert 改写后的输出类型）分别结算、
     * 分别被对应抗性/护甲减免（用户机制：以来源伤害类型分别输出，而非统一成某种类型）。
     *
     * <p>叠层规则同 {@link #apply}：层数上限 = maxStacks + status_max_stacks；每次施加额外 +status_extra_stacks 层。
     *
     * <p>输出伤害类型修正（ailment_output_convert）<b>不在此冻结</b>：按用户要求，该修正必须在每 tick 结算时、
     * 抗性减免之前，以施法者当前 Stat 作用域动态解析（见 StatusBehavior.dealSplitDoT），从而反映施加后变化的属性
     * （如中途获得「被你点燃的敌人承受混沌伤害代替火焰」修正即实时生效）。
     */
    public static void apply(LivingEntity target, StatusType type, LivingEntity caster,
                             double[] composition, int durationTicks) {
        apply(target, type, caster, composition, durationTicks, false);
    }

    public static void apply(LivingEntity target, StatusType type, LivingEntity caster,
                             double[] composition, int durationTicks, boolean auraSourced) {
        // 免疫短路（ImmuneStatEffect，fold 型查询）：目标对该状态免疫则整体跳过施加
        if (StatEngine.isImmune(target, type)) return;
        // Charm 中央钩子（与上方非 DoT 路径同构）
        if (com.cimdy.coe.item.charm.CharmEngine.onStatusApplied(target, type)) return;
        int capBonus = scopedStacks(caster, type, AttributeRegistry.STATUS_MAX_STACKS);
        int extraStacks = Math.max(0, scopedStacks(caster, type, AttributeRegistry.STATUS_EXTRA_STACKS));
        // 同 apply：maxStacks <= 0 = 无上限声明，不叠加 capBonus、不夹紧。
        int cap = type.maxStacks <= 0 ? Integer.MAX_VALUE : Math.max(1, type.maxStacks + capBonus);
        boolean canStack = type.stackable || cap > 1;

        double total = 0;
        for (double c : composition) total += c;

        StatusStore store = store(target);
        StatusInstance existing = store.get(type);
        if (existing != null && canStack) {
            // 叠层：各来源分量取较高值（与 magnitude=max 一致），层数累加
            if (existing.dotComposition == null) existing.dotComposition = new double[DamageTypes.SLOT_CAPACITY];
            for (int t = 0; t < DamageTypes.width(); t++)
                existing.dotComposition[t] = Math.max(existing.dotComposition[t], composition[t]);
            existing.durationTicks = Math.max(existing.durationTicks, scaleStatusDuration(caster, target, type, durationTicks));
            existing.magnitude = Math.max(existing.magnitude, total);
            existing.source = caster == null ? null : caster.getUUID();
            existing.auraSourced = auraSourced;
            existing.effectMul = effectMul(target, type, caster, auraSourced);
            existing.stacks = Math.min(cap, existing.stacks + 1 + extraStacks);
            // 层数变化即时刷新 stat 贡献（与上方非 DoT 叠层分支同口径；无 provider 时两侧均短路为空操作）。
            StatusStatBridge.remove(target, existing);
            StatusStatBridge.add(target, existing);
        } else {
            if (existing != null) StatusStatBridge.remove(target, existing);
            int dur = scaleStatusDuration(caster, target, type, durationTicks);
            int st = Math.min(cap, 1 + extraStacks);
            long appliedAt = target.level().getGameTime();
            if (existing != null) {
                // 刷新不得销毁再施加应保留的状态（与上方叠层分支同口径）：
                // 时长取较大值（原实现直写新时长，一次弱刷新可缩短既有异常）；
                // 层数沿用既有值、叠加本次施加的 extra_stacks 并仍受 cap 钳制（不叠加基础层：叠加语义仍归 MERGE 分支）——与 MERGE 分支同口径：新施加 min(cap, 1 + extraStacks)（L285）/ 刷新既有 min(cap, max(1, existing.stacks) + extraStacks)。此处无需重新夹紧：cap 本身已夹紧；且该分支可达且 existing!=null ⇒ !canStack ⇒ !stackable && cap<=1，而 cap=max(1, maxStacks+capBonus) 恒 ≥1，故 cap 恒为 1，min(cap, ·) 会把额外层数夹回 1——行为与修前完全一致，仅补齐口径不变玩家观感。
                // appliedTick 沿用原施加时刻，「已存续 N 秒」类条件（流血 >4s 等）不被刷新重置。
                dur = Math.max(existing.durationTicks, dur);
                st = Math.min(cap, Math.max(1, existing.stacks) + extraStacks);
                appliedAt = existing.appliedTick;
            }
            StatusInstance inst = new StatusInstance(type, total, dur, st,
                    caster == null ? null : caster.getUUID(), 0.0, null, null,
                    composition.clone());
            inst.auraSourced = auraSourced;
            inst.effectMul = effectMul(target, type, caster, auraSourced);
            inst.appliedTick = appliedAt;
            // 加重档沿用既有值（新施加时为缺省 0），DoT 结算按 (1+aggravatedFraction) 放大。
            inst.aggravatedFraction = existing == null ? 0.0 : existing.aggravatedFraction;
            store.put(inst);
            StatusStatBridge.add(target, inst);
        }
    }

    public static void apply(LivingEntity target, StatusType type, LivingEntity caster,
                             double magnitude, int durationTicks) {
        apply(target, type, caster, magnitude, durationTicks, 1);
    }

    /** 仅当不存在时施加（用于标称状态的唯一实例）。 */
    public static boolean applyIfAbsent(LivingEntity target, StatusType type, LivingEntity caster,
                                        double magnitude, int durationTicks) {
        StatusStore store = target.getData(AttachmentRegistry.STATUS_STORE);
        if (store.has(type)) return false;
        apply(target, type, caster, magnitude, durationTicks, 1);
        return true;
    }

    public static StatusInstance query(LivingEntity target, StatusType type) {
        return target.getData(AttachmentRegistry.STATUS_STORE).get(type);
    }

    public static boolean has(LivingEntity target, StatusType type) {
        return target.getData(AttachmentRegistry.STATUS_STORE).has(type);
    }

    /**
     * 是否正受任一 Archon 增益影响（按 {@link #ARCHON_TAG} 标签判定，与 {@link #archonBlocked}
     * 的互斥判定同源；恢复冷却状态 {@code archon_recovery} 无 archon 标签故不计入）。
     * 供语料「while affected by an Archon Buff」类条件门控条目读取。
     */
    public static boolean hasArchon(LivingEntity target) {
        StatusStore store = store(target);
        for (StatusType t : StatusType.all()) {
            if (t.tags().contains(ARCHON_TAG) && store.has(t)) return true;
        }
        return false;
    }

    public static double magnitude(LivingEntity target, StatusType type) {
        return target.getData(AttachmentRegistry.STATUS_STORE).magnitude(type);
    }

    public static int stacks(LivingEntity target, StatusType type) {
        return target.getData(AttachmentRegistry.STATUS_STORE).stacks(type);
    }

    /**
     * 目标身上全部<b>活跃状态</b>的作用域标签并集（状态 id + 其注册分类标签）。
     *
     * <p>用途是事件闭环的「对象状态维」：把「事件涉及的那个实体当前处于何种状态」并入
     * {@code StatEngine.fire} 的作用域标签集，使语料中成族的「事件 × 目标状态」条目
     * （如「on Killing a Poisoned Enemy」「when you Heavy Stun a Frozen Enemy」）
     * 能以 tags {@code [kill, poison]} 这样的<b>组合</b>表达，
     * 而不必为每种组合各立一个 1:1 占位属性（归一原则：基础维 × 作用域/条件）。</p>
     *
     * <p>返回空集表示目标无任何活跃状态（调用方无需额外判空）。</p>
     */
    public static Set<String> activeTags(LivingEntity target) {
        StatusStore store = target.getData(AttachmentRegistry.STATUS_STORE);
        if (store.instances.isEmpty()) return Set.of();
        Set<String> tags = new HashSet<>();
        for (StatusType type : store.instances.keySet()) {
            tags.add(type.id);
            tags.addAll(type.tags());
        }
        return tags;
    }

    /**
     * 累加某异常的积蓄值（POE2 buildup meter），返回累加后的总额。
     * 积蓄条存于 {@link StatusStore#buildup}（独立于活动实例，按目标+状态类型维护），
     * 由 {@link #tick(LivingEntity)} 每 tick 衰减。满 1.0（= 门槛）即视为触发。
     */
    public static double addBuildup(LivingEntity target, StatusType type, double amount) {
        StatusStore store = store(target);
        double v = store.buildup.getOrDefault(type, 0.0) + amount;
        store.buildup.put(type, v);
        return v;
    }

    /** 读取当前积蓄值（未积累则为 0）。 */
    public static double getBuildup(LivingEntity target, StatusType type) {
        return store(target).buildup.getOrDefault(type, 0.0);
    }

    /**
     * 触发后重置积蓄条：{@code remaining <= 0} 则清零；否则保留溢出（POE2 大命中可溢出到下一次积累）。
     */
    public static void resetBuildup(LivingEntity target, StatusType type, double remaining) {
        StatusStore store = store(target);
        if (remaining <= 0) store.buildup.remove(type);
        else store.buildup.put(type, remaining);
    }

    /**
     * Archon 互斥 + 恢复守卫（中央入口）：仅对带 {@link #ARCHON_TAG} 标签的状态生效。
     * <ul>
     *   <li>已持有任意 archon 标签状态（含其它 Archon）→ 拒绝（互斥）；</li>
     *   <li>处于 archon_recovery 恢复冷却 → 拒绝。</li>
     * </ul>
     * 非 archon 状态直接返回 false（不介入普通施加）。
     */
    private static boolean archonBlocked(LivingEntity target, StatusType type) {
        if (!type.tags().contains(ARCHON_TAG)) return false;
        StatusStore store = store(target);
        for (StatusType t : StatusType.all()) {
            if (t != type && t.tags().contains(ARCHON_TAG) && store.has(t)) return true;
        }
        StatusType recovery = StatusType.byId(ARCHON_RECOVERY_ID).orElse(null);
        return recovery != null && store.has(recovery);
    }

    /**
     * 失去 Archon 后进入恢复冷却（中央统一调用）：施加 archon_recovery，时长 400 tick = 20s，可被 {@code {archon}}
     * 作用域的两条正交属性缩放：
     * <ul>
     *   <li>{@code ARCHON_RECOVERY_RATE}（语料「expires X% faster/slower」的<b>更快</b>侧）：rate &gt; 1 = 更快（时长缩短）；</li>
     *   <li>{@code ARCHON_RECOVERY_PERIOD_MULT}（<b>更慢</b>侧）：period &gt; 1 = 更慢（周期延长）。
     *       「expires {X}% slower」= 周期 ×(1+X)，加性「抬高速率」无法表达「更慢」（不可取负），故另立周期属性承载 slower。</li>
     * </ul>
     * 恢复时长 ticks = 400 × period / rate。经本类中央 apply，因恢复状态无 archon 标签，不会被互斥守卫拦截。
     */
    private static void grantArchonRecovery(LivingEntity target) {
        StatusType recovery = StatusType.byId(ARCHON_RECOVERY_ID).orElse(null);
        if (recovery == null) return;
        double rate = scopedArchonRate(target);
        double periodMult = scopedArchonPeriodMult(target);
        int ticks = (int) Math.max(1, Math.round(400.0 * periodMult / rate));
        apply(target, recovery, target, 1.0, ticks, 1);
    }

    /** Archon 恢复速率倍率（Stat 驱动）：在 {archon} 作用域读 ARCHON_RECOVERY_RATE（默认 1.0）。 */
    private static double scopedArchonRate(LivingEntity target) {
        if (target == null) return 1.0;
        AutoCloseable s = StatScopes.enter(target, StatContext.of(Set.of(ARCHON_TAG), null));
        try {
            double rate = target.getAttributeValue(AttributeRegistry.ARCHON_RECOVERY_RATE);
            return rate > 0 ? rate : 1.0;
        } finally {
            try { s.close(); } catch (Exception ignored) {}
        }
    }

    /**
     * Archon 恢复「周期」倍率（Stat 驱动）：在 {archon} 作用域读 ARCHON_RECOVERY_PERIOD_MULT（默认 1.0）。
     * 承载「Archon recovery period expires {X}% slower」——正值抬高周期 = 恢复更慢（ticks = 400 × period / rate）。
     */
    private static double scopedArchonPeriodMult(LivingEntity target) {
        if (target == null) return 1.0;
        AutoCloseable s = StatScopes.enter(target, StatContext.of(Set.of(ARCHON_TAG), null));
        try {
            double period = target.getAttributeValue(AttributeRegistry.ARCHON_RECOVERY_PERIOD_MULT);
            return period > 0 ? period : 1.0;
        } finally {
            try { s.close(); } catch (Exception ignored) {}
        }
    }

    /** Archon 增益时长倍率（Stat 驱动）：在 {archon} 作用域读 AILMENT_DURATION（默认 1.0，与其它状态时长同属性，tag 区分）。 */
    public static double archonBuffDurationMultiplier(LivingEntity target) {
        if (target == null) return 1.0;
        AutoCloseable s = StatScopes.enter(target, StatContext.of(Set.of(ARCHON_TAG), null));
        try {
            return target.getAttributeValue(AttributeRegistry.ARCHON_DURATION);
        } finally {
            try { s.close(); } catch (Exception ignored) {}
        }
    }

    /** Archon 增益效果倍率（Stat 驱动）：在 {archon} 作用域读 AILMENT_MAGNITUDE（默认 1.0，与其它状态强度同属性，tag 区分）。 */
    public static double archonBuffEffectMultiplier(LivingEntity target) {
        if (target == null) return 1.0;
        AutoCloseable s = StatScopes.enter(target, StatContext.of(Set.of(ARCHON_TAG), null));
        try {
            return target.getAttributeValue(AttributeRegistry.AILMENT_MAGNITUDE);
        } finally {
            try { s.close(); } catch (Exception ignored) {}
        }
    }

    /** Buff 时长倍率（Stat 驱动）：读 BUFF_DURATION_MULT（默认 1.0，ADD_MULTIPLIED_BASE 累积）。 */
    public static double buffDurationMultiplier(LivingEntity target) {
        if (target == null) return 1.0;
        return Math.max(0.0, target.getAttributeValue(AttributeRegistry.BUFF_DURATION_MULT));
    }

    /** Debuff 时长倍率（Stat 驱动）：读 DEBUFF_DURATION_MULT（默认 1.0，ADD_MULTIPLIED_BASE 累积；<1=更短=更快消失）。 */
    public static double debuffDurationMultiplier(LivingEntity target) {
        if (target == null) return 1.0;
        return Math.max(0.0, target.getAttributeValue(AttributeRegistry.DEBUFF_DURATION_MULT));
    }

    /** Form 时长倍率（Stat 驱动）：在 {shapeshifted} 作用域读 BUFF_DURATION_MULT（默认 1.0）。
     *  Form 为自侧持续形态，其「技能效果时长」由 shapeshift 词缀经 {shapeshifted} 作用域驱动（R3 真读点）。 */
    private static double formDurationMultiplier(LivingEntity target) {
        if (target == null) return 1.0;
        AutoCloseable s = StatScopes.enter(target, StatContext.of(Set.of(GateTag.SHAPESHIFTED), null));
        try {
            return Math.max(0.0, target.getAttributeValue(AttributeRegistry.BUFF_DURATION_MULT));
        } finally {
            try { s.close(); } catch (Exception ignored) {}
        }
    }

    /**
     * Debuff 类异常时长倍率（Stat 驱动）：读 AILMENT_DURATION，按状态 id 标签缩放
     * （如 blind/hinder 等由施法者施加的 debuff 时长；对应 INC_BLIND_DURATION / INC_HINDER_DURATION）。
     * 在 {状态id, !taken} 作用域读取——taken 缺省即视为「你施加的」异常，匹配 EFF 标签。
     * 无对应时长 EFF 的状态返回基础 1.0（=不缩放），故对其它 debuff 无副作用。
     */
    private static double debuffAilmentDurationMultiplier(LivingEntity caster, StatusType type) {
        if (caster == null) return 1.0;
        AutoCloseable s = StatScopes.enter(caster, StatContext.of(Set.of(type.id, GateTag.not(GateTag.TAKEN)), null));
        try {
            return Math.max(0.0, caster.getAttributeValue(AttributeRegistry.DEBUFF_DURATION));
        } finally {
            try { s.close(); } catch (Exception ignored) {}
        }
    }

    /**
     * 按状态类型施加 buff/debuff 时长倍率（BUFFS_ON_YOU_EXPIRE_SLOWER / DEBUFFS_ON_YOU_EXPIRE_FASTER）。
     * 仅对 StatusKind.BUFF / DEBUFF 生效，其它类型（ailment/control/charge/curse…）原样返回；时长 <=0 不缩放。
     * 中央 apply 统一调用，覆盖技能直接施加与 Stat→Status 桥接两条路径（StatusStatEffect 亦经此落地）。
     * debuff 类额外叠加「你施加的异常时长」倍率（AILMENT_DURATION，按状态 id 标签）。
     */
    private static int scaleStatusDuration(LivingEntity caster, LivingEntity target, StatusType type, int durationTicks) {
        if (durationTicks <= 0) return durationTicks;
        double mul = 1.0;
        if (type.kind == StatusKind.BUFF) mul = buffDurationMultiplier(target);
        else if (type.kind == StatusKind.FORM) mul = formDurationMultiplier(target);
        else if (type.kind == StatusKind.DEBUFF) {
            mul = debuffDurationMultiplier(target);
            mul *= debuffAilmentDurationMultiplier(caster, type);
        }
        return (int) Math.max(1, Math.round(durationTicks * mul));
    }

    /**
     * 统一计算状态效果幅度倍率（AILMENT_MAGNITUDE）：进攻侧（施法者，{ailment,type,!taken}）
     * × 防守侧（目标，{ailment,type,taken}）× [若光环/存在来源] 光环额外倍率（施法者，{aura,!taken}）。
     * 缺省 1.0 表示不加成。凋零（withered）/ 附魔（infusion）标记并入对应作用域，使「凋零下的状态」获得对应加成。
     */
    private static double effectMul(LivingEntity target, StatusType type, @Nullable LivingEntity caster, boolean auraSourced) {
        if (caster == null) return 1.0;
        Set<String> off = new HashSet<>(Set.of(GateTag.AILMENT, type.id));
        if (has(caster, StatusDefs.WITHER)) off.add(GateTag.WITHERED);
        if (activeTags(caster).contains(GateTag.INFUSION)) off.add(GateTag.INFUSION);
        double om = AilmentHelper.withScope(caster, off, AttributeRegistry.AILMENT_MAGNITUDE);
        Set<String> def = new HashSet<>(Set.of(GateTag.AILMENT, type.id, GateTag.TAKEN));
        if (has(target, StatusDefs.WITHER)) def.add(GateTag.WITHERED);
        double dm = AilmentHelper.withScope(target, def, AttributeRegistry.AILMENT_MAGNITUDE);
        double aura = auraSourced ? AilmentHelper.withScope(caster, Set.of(GateTag.AURA, GateTag.not(GateTag.TAKEN)), AttributeRegistry.AILMENT_MAGNITUDE) : 1.0;
        return om * dm * aura;
    }
    /**
     * 使用非瞬发攻击时移除全部 Archon（中央入口）：由「技能被使用」事件回调调用。
     * 每个 archon 经 {@link #clear} 移除，进而触发恢复冷却——符合 POE2「非瞬发攻击立即移除 Archon」。
     */
    public static void clearArchons(LivingEntity target) {
        StatusStore store = store(target);
        for (StatusType t : StatusType.all()) {
            if (t.tags().contains(ARCHON_TAG) && store.has(t)) clear(target, t);
        }
    }

    public static void clear(LivingEntity target, StatusType type) {
        StatusStore store = store(target);
        StatusInstance inst = store.get(type);
        if (inst != null) {
            StatusStatBridge.remove(target, inst);
            store.remove(type);
            // 中央移除副作用：失去 Archon 后进入恢复冷却（其余状态无副作用）。
            if (inst.type.tags().contains(ARCHON_TAG)) grantArchonRecovery(target);
            // 中央移除副作用：失去形态后记录「变回人类」事件（供 recently:form_cleared 条件），
            // 并经 StatEngine.fire 真实发射（tag=form_cleared，other=target→triggerTarget=玩家），
            // 使 FORM_CLEARED 事件驱动的 GrantOnEventStatEffect（变回人形获秘能波动）真正触发——
            // 原仅 Event.record 不入 fire 通道，该接线此前为死线（#64 同型）。
            if (inst.type.tags().contains(FORM_TAG)) {
                com.cimdy.coe.event.Event.record(target, com.cimdy.coe.event.EventName.FORM_CLEARED);
                com.cimdy.coe.event.combat.GameEvent.emit(com.cimdy.coe.event.EventName.FORM_CLEARED, target, target, Set.of());
                // 变回人形：按变形时长授予限时「+X% 法术伤害」（10%/秒，上限 80%，持续 8s）——GAP-FIX#13 消费端。
                grantHumanFormSpellDamage(target, inst);
            }
            // 中央移除副作用：PIN 解除时发射 UNPINNED 事件（owner=施加钉刺者 caster、other=被解除钉刺敌人），
            // 经 StatEngine.fire 驱动「敌人解除钉刺后致残」类 GrantOnEventStatEffect 真触发（与 FORM_CLEARED 同源，避免死线）。
            if (inst.type == StatusDefs.PIN) {
                LivingEntity caster = inst.source != null ? (LivingEntity) target.level().getEntity(inst.source) : null;
                if (caster != null) {
                    com.cimdy.coe.event.combat.GameEvent.emit(com.cimdy.coe.event.EventName.UNPINNED, caster, target, Set.of());
                }
            }
        }
    }

    /**
     * 消耗目标身上的某一异常/状态（「消耗异常」原语）：目标未持有该状态则返回 {@code false}；
     * 否则读取当前强度/剩余时长/层数 → {@link #clear} 移除 → 记录「消耗异常」事件（owner=消耗方）→
     * 若消耗方持有 {@link com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect} 标记（其 conditionTags 须含本状态 id）
     * 且按幅度掷骰命中，则以原强度/时长/层数把同一状态重新施加回目标（POE2「消耗敌人某异常时按概率重新施加」，
     * StatId {@code CHANCE_ON_CONSUMING_A_SHOCK_ON_AN_ENEMY_TO_REAPPLY_IT}）。
     *
     * <p>状态用 {@link StatusType} 指定（调用方传 {@code StatusDefs.SHOCK} 即为「消耗电击」）——
     * 与 {@link #has}/{@link #magnitude}/{@link #clear}/{@link #apply} 同源，无新增读点。
     *
     * @param consumer 消耗方（掷骰与标记读取主体；可为 null，null 时仅移除并记录、不重施）
     * @param target   被消耗状态所在实体
     * @param type     要消耗的具体状态类型
     * @return 目标原本是否持有该状态（即本次是否真的消耗掉了）
     */
    public static boolean consumeAilment(@Nullable LivingEntity consumer, LivingEntity target, StatusType type) {
        StatusStore store = store(target);
        if (!store.has(type)) return false;
        StatusInstance inst = store.get(type);
        double intensity = inst.magnitude;
        int remaining = inst.durationTicks;
        int stacks = Math.max(1, inst.stacks);
        clear(target, type);
        // ── P2-3 转存通道（store/transfer）：被消费异常的原始数值记账 ──
        // 紧随 clear 之后、在重施逻辑之前记录，纯记账不改变既有消费行为；后续 store/transfer 消费方
        // （blood_hunt / elemental_sundering 等 POE2「Consume X」族）经 AilmentTransfer 取用该数值，
        // 本钩子不实现任何转存效果（iron_ward / plague_bearer 在 POE2 中与消耗异常无关，非本通道消费方）。
        AilmentTransfer.store(consumer != null ? consumer : target, type,
                (int) Math.round(intensity), remaining, stacks);
        // 「消耗某异常时按概率重新施加」门控：通用机制——异常身份由标记实例的 conditionTags 承载，
        // 读取侧传入本状态 id 作为作用域标签，故任意异常（电击/冰缓/点燃…）皆可复用，不焊死电击。
        if (consumer != null) {
            double chance = StatEngine.rollChance(consumer,
                    "ailment_reapply_on_consume", Set.of(type.id));
            if (chance > 0 && consumer.getRandom().nextDouble() < Math.min(1.0, chance)) {
                apply(target, type, consumer, intensity, remaining, stacks);
            }
        }
        return true;
    }

    /**
     * 直接设定状态层数（资源型状态，如元素灌入）：clamp 到 [0, +∞)，归零则清除状态。
     * 供 {@link #apply} 之外的「权威写入口」（如 {@code ResourceRegistry}）使用，
     * 确保状态实例存在且层数精确可控（与 {@link #stacks} 读取对应）。
     */
    public static void setStacks(LivingEntity target, StatusType type, int stacks) {
        stacks = Math.max(0, stacks);
        if (stacks <= 0) {
            clear(target, type);
            return;
        }
        StatusStore store = store(target);
        StatusInstance inst = store.get(type);
        if (inst != null) {
            inst.stacks = stacks;
        } else {
            StatusInstance ni = new StatusInstance(type, 1.0, type.defaultDurationTicks, stacks,
                    target.getUUID(), 0.0, null, null);
            ni.appliedTick = target.level().getGameTime();
            store.put(ni);
            StatusStatBridge.add(target, ni);
        }
    }

    /** 每 tick 驱动全部状态衰减与回调（由订阅 LivingTickEvent 调用）。 */
    public static void tick(LivingEntity target) {
        List<StatusInstance> expired = store(target).tickAll(target);
        // 到期副作用（如 Archon 失去后进入恢复冷却）在迭代外统一处理，避免并发修改。
        for (StatusInstance inst : expired) {
            if (inst.type.tags().contains(ARCHON_TAG)) grantArchonRecovery(target);
            // 到期移除形态：同 clear 路径，记录 + 真实发射 FORM_CLEARED（other=target→triggerTarget=玩家）。
            if (inst.type.tags().contains(FORM_TAG)) {
                com.cimdy.coe.event.Event.record(target, com.cimdy.coe.event.EventName.FORM_CLEARED);
                com.cimdy.coe.event.combat.GameEvent.emit(com.cimdy.coe.event.EventName.FORM_CLEARED, target, target, Set.of());
                // 变回人形：按变形时长授予限时「+X% 法术伤害」（10%/秒，上限 80%，持续 8s）——GAP-FIX#13 消费端。
                grantHumanFormSpellDamage(target, inst);
            }
            // 到期解除钉刺：同 clear 路径，发射 UNPINNED（owner=caster、other=敌人），驱动「解除钉刺致残」真触发。
            if (inst.type == StatusDefs.PIN) {
                LivingEntity caster = inst.source != null ? (LivingEntity) target.level().getEntity(inst.source) : null;
                if (caster != null) {
                    com.cimdy.coe.event.combat.GameEvent.emit(com.cimdy.coe.event.EventName.UNPINNED, caster, target, Set.of());
                }
            }
        }
    }

    /**
     * 变回人形消费端（GAP-FIX#13 / backlog #42）：当携带形态状态被清除（clear 或 tick 到期）时，
     * 若玩家持有「变回人形获 +X% 法术伤害」被动标记，则按变形时长授予限时 {@code HUMAN_FORM_SPELL_DMG} 增益。
     * 标记经 {@code StatEngine.markerMagnitude(kind, Set.of())} 读取（< 1.0 视为未持有，直接返回）；
     * 幅度 = min(0.10 × 变形秒数, 0.80)，持续 160 tick（8s）。
     */
    private static void grantHumanFormSpellDamage(LivingEntity target, StatusInstance inst) {
        if (!StatEngine.gateOf(target, "shapeshift_human_form_spell_dmg", Set.of())) return;
        long elapsedTicks = target.level().getGameTime() - inst.appliedTick;
        double elapsedSecs = elapsedTicks / 20.0;
        double bonus = Math.min(0.10 * elapsedSecs, 0.80);
        StatusEngine.apply(target, StatusDefs.HUMAN_FORM_SPELL_DMG, target, bonus, 160, 1);
    }

    // ── 形态（Shapeshift）中央入口（POE2 Druid：形态=状态，使用形态技能变身、施放法术变回）──

    /** 是否已持有任意形态（带 form 标签的状态）。余震生产者（shapeshift_slam 变体）亦用。 */
    public static boolean hasForm(LivingEntity target) {
        StatusStore store = store(target);
        for (StatusType t : StatusType.all()) {
            if (t.tags().contains(FORM_TAG) && store.has(t)) return true;
        }
        return false;
    }

    /**
     * 获得形态（中央入口）：先移除当前形态（POE2 即时切换），再施加新形态。
     * 记录 {@code form_changed} 事件（带新形态标签），供 {@code recently:form_changed} 条件使用。
     * 由「技能被使用」回调（SkillInstance：技能含 shapeshift 标签）调用。
     */
    public static void applyForm(LivingEntity target, StatusType form) {
        if (!form.tags().contains(FORM_TAG)) return;
        clearForms(target);
        apply(target, form, target, 1.0, form.defaultDurationTicks, 1);
        com.cimdy.coe.event.Event.record(target, com.cimdy.coe.event.EventName.FORM_CHANGED, form.tags());
    }

    /**
     * 移除全部形态（中央入口）：由「施放法术」回调（SkillInstance：技能含 spell 标签）调用，
     * 与 POE2「施放法术立即变回人类」一致。每个 form 经 {@link #clear} 移除。
     */
    public static void clearForms(LivingEntity target) {
        StatusStore store = store(target);
        for (StatusType t : StatusType.all()) {
            if (t.tags().contains(FORM_TAG) && store.has(t)) clear(target, t);
        }
    }

    // ── 异常门槛模型（文档 §1.1 / §3.4）──────────────────────────────
    // 门槛 = 最大生命 × 适应倍率（玩家 0.5 / 怪物 1.0，POE2 定义常数）。
    // 适应倍率经 Stat 属性 AILMENT_THRESHOLD_ADAPTATION（{ailment, threshold} 作用域）可被数据覆盖/缩放。
    // 异常强度/积蓄/时长计算统一由 AilmentHelper 按 Stat 属性驱动——不再有按状态 id 的 applyAilment 强度公式。

    /**
     * 异常门槛：玩家 = 最大生命 × 0.5，怪物 = × 1.0（POE2 定义常数）。
     * 适应倍率可在 {@code {ailment, threshold}} 作用域内经 Stat 属性
     * {@link AttributeRegistry#AILMENT_THRESHOLD_ADAPTATION}（默认 1.0）由数据放大/缩小，
     * 取代原纯硬编码（数据侧可给玩家挂 -0.5 修正或以词缀调整）。
     */
    public static double ailmentThreshold(LivingEntity target) {
        return ailmentThreshold(target, target.getMaxHealth());
    }

    /** 异常门槛（可指定基数池）：默认池为最大生命；stun 门槛改用能量护盾时由 StunMeter 传入 ES 池。 */
    public static double ailmentThreshold(LivingEntity target, double basePool) {
        double base = target instanceof Player ? 0.5 : 1.0;
        return basePool * base * scopedAilmentAdaptation(target);
    }

    /** 门槛适应倍率（Stat 驱动）：在 {ailment, threshold} 作用域内读 AILMENT_THRESHOLD_ADAPTATION（默认 1.0）。 */
    private static double scopedAilmentAdaptation(LivingEntity target) {
        AutoCloseable s = StatScopes.enter(target,
                StatContext.of(Set.of(GateTag.AILMENT, GateTag.THRESHOLD), null));
        try {
            return target.getAttributeValue(AttributeRegistry.AILMENT_THRESHOLD_ADAPTATION);
        } finally {
            try {
                s.close();
            } catch (Exception ignored) {
            }
        }
    }
}
