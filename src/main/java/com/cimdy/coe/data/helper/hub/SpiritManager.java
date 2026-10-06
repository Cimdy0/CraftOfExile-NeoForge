package com.cimdy.coe.data.helper.hub;

import com.cimdy.coe.data.codec.skill.modifier.impl.effect.EventListenEffect;
import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;

import com.cimdy.coe.data.helper.skill.SummonCountHelper;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.core.SkillEntry;
import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import com.cimdy.coe.data.codec.skill.modifier.SkillModifier;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.phase.event.SkillEventBus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.gem.SkillData;
import com.cimdy.coe.data.codec.skill.gem.KeyBind;
import com.cimdy.coe.data.codec.skill.gem.SkillAssembly;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceCost;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceType;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceUsage;
import com.cimdy.coe.data.codec.skill.phase.SkillPhase;
import com.cimdy.coe.data.codec.skill.phase.SkillPhasePlayer;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.control.LifecycleTrigger;
import com.cimdy.coe.data.codec.skill.phase.action.damage.ModifierAction;import com.cimdy.coe.data.codec.skill.phase.action.damage.StatAction;
import com.cimdy.coe.data.codec.skill.phase.action.control.TriggeredAction;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryExecutors;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.engine.StatStore;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.cimdy.coe.data.codec.support.status.def.StatusKind;
import com.cimdy.coe.data.codec.skill.phase.event.trigger.ReservationChangeEvent;
import com.cimdy.coe.event.data.LoadEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;
import java.util.Set;

/**
 * 精魂光环管理器 — 让装配了精魂宝石的玩家自动获得常驻光环效果（无需按键施放）。
 *
 * <p>PoE2 对应：精魂宝石一旦镶嵌即激活，持续占用一部分灵魂值（保留 / reservation），
 * 为玩家提供常驻增益（如火焰伤害加成）。本管理器每 tick 遍历在线玩家：
 * <ol>
 *   <li>读取其技能装配表，找出所有精魂宝石；</li>
 *   <li>按槽位顺序激活，直到超出总灵魂值池（保留语义）；</li>
 *   <li>对每个激活的精魂，重新应用其 {@code on_cast} 的 {@link ModifierAction}（作用于自身），
 *       使用稳定 modId 刷新而非叠加；</li>
 *   <li>登记灵魂值保留量，并移除已失效光环的 modifier。</li>
 * </ol>
 */
public class SpiritManager {
    /** 每个玩家上一 tick 激活的精魂光环「来源键」集合（非裸属性 id —— 见 {@link ModifierAction#auraModId}），
     *  用于移除失效光环。同属性不同 op/tags 的光环（四颗 Purity）各自成键，互不误删。 */
    private static final Map<UUID, Set<Identifier>> LAST_AURA_MODS = new ConcurrentHashMap<>();
    /** 常驻事件监听句柄：玩家 → 技能 id → 句柄列表（光环停用时注销，避免监听器泄漏后继续响应）。 */
    private static final Map<UUID, Map<Identifier, List<SkillEventBus.ListenerHandle>>> LAST_AURA_LISTENS =
            new ConcurrentHashMap<>();
    /** 上一 tick 由 {@link StatAction} 写入玩家 STAT_STORE 的条目来源 modId 集合，
     *  用于精魂光环停用时按 {@code timed:<modId>} 前缀精确回收（否则常驻条目泄漏）。 */
    private static final Map<UUID, Set<Identifier>> LAST_AURA_STATS = new ConcurrentHashMap<>();
    /** 周期门控倒计时：玩家 → 「精魂 id#动作序号」→ 剩余 tick。
     *  由 {@link #applyPhaseAura} 在常驻重放时读写，光环停用时按差集回收（见 tickPlayer）。 */
    private static final Map<UUID, Map<String, Integer>> AURA_PERIOD_TIMERS = new ConcurrentHashMap<>();
    /** 已激活的「快行（Travel）」精魂技能：玩家 → 装配数据（含宝石 id / 等级 / 品质）。
     *  带 {@link GateTag#TRAVEL} 标签的精魂宝石激活时替换闪避翻滚（PoE2 Blink 真值），
     *  由 {@link com.cimdy.coe.helper.DodgeRollHelper} 读取；等级须一并带出 ——
     *  Blink 的传送距离随宝石等级成长，触发时无等级则成长项恒为 Lv1（半接线）。 */
    private static final Map<UUID, SkillData> ACTIVE_TRAVEL = new ConcurrentHashMap<>();

    /** 取该玩家当前已激活的快行技能装配数据（无则 null）。供翻滚入口按数据决定「翻滚 or 快行技能」。 */
    public static SkillData getActiveTravel(ServerPlayer player) {
        return player == null ? null : ACTIVE_TRAVEL.get(player.getUUID());
    }

    /** 动态光环状态刷新时长（tick）：每 tick 由 applyAuraStatus 以本时长幂等重放，
     *  常驻期间恒为满值；宝石失效（精魂不足/摘除）后约本时长到期自动移除贡献。 */
    private static final int AURA_REFRESH_TICKS = 40;

    public static void tickAll(MinecraftServer server) {
        Set<UUID> online = new HashSet<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            online.add(player.getUUID());
            tickPlayer(player);
        }
        ReservationManager.cleanup(online);
    }

    private static void tickPlayer(ServerPlayer player) {
        SkillAssembly assembly = KeyBind.getMergedAssembly(player);
        float totalSpirit = ResourceRegistry.getSpirit(player);
        float reserved = 0;
        Set<Identifier> activeMods = new HashSet<>();
        Set<Identifier> activeStats = new HashSet<>();
        Map<Identifier, List<SkillEventBus.ListenerHandle>> activeListens = new HashMap<>();
        SkillData activeTravel = null;
        // 周期门控：本 tick 遍历实际用到的计时键；循环结束后按差集回收（光环停用后不留残留计时器）
        Map<String, Integer> periodTimers =
                AURA_PERIOD_TIMERS.computeIfAbsent(player.getUUID(), k -> new HashMap<>());
        Set<String> periodUsed = new HashSet<>();

        int slot = 0;
        for (SkillData sd : assembly.slots()) {
            if (!sd.isEmpty() && sd.isSpirit() && assembly.isAuraEnabled(slot)) {
                SkillEntry entry = LoadEvent.getSpirit(sd.id());
                Skill skill = entry == null ? null : entry.resolve();
                if (skill != null) {
                    float cost = getSpiritReserve(skill, reservationEfficiency(player, skill));
                    if (reserved + cost <= totalSpirit + 1e-3f) { // 灵魂值池充足才激活
                        reserved += cost;
                        // 宝石等级注入表达式上下文：常驻光环无 SkillInstance，
                        // 不注入则条目里一切随等级缩放的数值（如「每 (1.8—1.42) 秒」）恒按 Lv1 求值（半接线）。
                        ExpressionContext.get(player).setSkillLevel(sd.level().get());
                        applyAura(player, sd.id(), skill, activeMods, activeStats,
                                periodUsed, periodTimers);
                        applyAuraStatus(player, sd.id(), skill);
                        if (skill.tags().contains(GateTag.TRAVEL)) activeTravel = sd;
                        registerPersistentListens(player, skill, sd.id(), activeListens);
                    }
                }
            }
            slot++;
        }

        // 本 tick 未用到的周期计时器 = 对应光环已停用（关闭 / 精魂不足 / 摘除），一并丢弃
        periodTimers.keySet().retainAll(periodUsed);

        float oldReserved = ReservationManager.getReserved(player.getUUID(), GateTag.SPIRIT);
        ReservationManager.setReserved(player.getUUID(), GateTag.SPIRIT, reserved);
        // 图腾保留精魂（「Totems reserve X Spirit each」）：每 tick 按「在场图腾数 × 每个图腾保留量」覆盖写入
        // （TOTEM 组件，与 AURA 并列求和；数量随生成/到期变化，故用覆盖而非累加，R3 真读 SummonCountHelper）。
        float perTotem = (float) player.getAttributeValue(AttributeRegistry.TOTEM_SPIRIT_RESERVATION);
        ReservationManager.setTotemReserved(player.getUUID(), GateTag.SPIRIT,
                SummonCountHelper.getTotemCount(player) * perTotem);
        // 仅在保留量实际变化时发布 ReservationChangeEvent（消除悬空契约死线；变化才发布，避免每 tick 空转刷总线）。
        float newReserved = ReservationManager.getReserved(player.getUUID(), GateTag.SPIRIT);
        if (Math.abs(newReserved - oldReserved) > 1e-3f) {
            SkillEventBus.forEntity(player).publish(
                    new ReservationChangeEvent(player, null, oldReserved, newReserved, totalSpirit));
        }
        // 同步一份到客户端：ReservationManager 是服务端静态表，客户端无从读取「精魂不足」
        player.setData(AttachmentRegistry.RESERVED_SPIRIT,
                ReservationManager.getReserved(player.getUUID(), GateTag.SPIRIT));

        // 移除上一 tick 激活、但本 tick 不再激活的光环 modifier
        Set<Identifier> last = LAST_AURA_MODS.getOrDefault(player.getUUID(), Set.of());
        for (Identifier modId : last) {
            if (!activeMods.contains(modId)) removeAura(player, modId);
        }
        LAST_AURA_MODS.put(player.getUUID(), activeMods);
        // 常驻 Stat 条目回收：本 tick 未激活的 StatAction 条目按来源键前缀移除
        for (Identifier modId : LAST_AURA_STATS.getOrDefault(player.getUUID(), Set.of())) {
            if (!activeStats.contains(modId)) removeAuraStats(player, modId);
        }
        LAST_AURA_STATS.put(player.getUUID(), activeStats);
        // 常驻事件监听注销：本 tick 未激活（关闭开关 / 精魂不足）的光环，其监听一并撤销
        for (Map.Entry<Identifier, List<SkillEventBus.ListenerHandle>> e
                : LAST_AURA_LISTENS.getOrDefault(player.getUUID(), Map.of()).entrySet()) {
            if (!activeListens.containsKey(e.getKey())) {
                for (SkillEventBus.ListenerHandle h : e.getValue()) h.unregister();
            }
        }
        LAST_AURA_LISTENS.put(player.getUUID(), activeListens);
        // 快行技能表：未激活（关闭 / 精魂不足 / 摘除）时一并清除，翻滚入口随即回退为普通翻滚。
        if (activeTravel != null) ACTIVE_TRAVEL.put(player.getUUID(), activeTravel);
        else ACTIVE_TRAVEL.remove(player.getUUID());
    }

    /** 移除某精魂光环经 {@link StatAction} 写入的常驻 Stat 条目（按来源键前缀精确寻址）。 */
    private static void removeAuraStats(ServerPlayer player, Identifier modId) {
        if (!player.hasData(AttachmentRegistry.STAT_STORE)) return;
        StatStore store = player.getData(AttachmentRegistry.STAT_STORE);
        player.setData(AttachmentRegistry.STAT_STORE, store.removeByKeyPrefix("timed:" + modId));
    }

    /**
     * 击杀爆发入口 —— 点亮玩家全部<b>已激活</b>捷的击杀触发相位（子相位，ON_HIT）。
     *
     * <p>PoE2 中捷的伤害几乎全部来自触发效果（击杀引发元素爆发），本方法是这些伤害的唯一权威路径。
     * 同时它也是「捷技能造成的伤害提高」类词条的<b>唯一真实读点</b>：爆发伤害经
     * {@code DamageAction} 结算，而本方法在进入相位前按技能标签（{@code herald} + 技能名）
     * 建立作用域，故 {@code {herald}} 作用域的增伤/暴击/范围词条在此命中。
     *
     * <p><b>为何必须显式建立作用域</b>：精魂光环走常驻重放路径，不存在
     * {@code SkillInstance}，{@code SkillStatScopes.enter} 无从被调用；不补作用域则
     * {@code herald} 标签永不入栈，全部 herald 作用域词条恒不生效（静默死线）。
     */
    public static void triggerHeraldProcs(LivingEntity killer, LivingEntity victim) {
        if (!(killer instanceof ServerPlayer player) || victim == null) return;
        SkillAssembly assembly = KeyBind.getMergedAssembly(player);
        float totalSpirit = ResourceRegistry.getSpirit(player);
        float reserved = 0;

        int slot = 0;
        for (SkillData sd : assembly.slots()) {
            if (!sd.isEmpty() && sd.isSpirit() && assembly.isAuraEnabled(slot)) {
                SkillEntry entry = LoadEvent.getSpirit(sd.id());
                Skill skill = entry == null ? null : entry.resolve();
                if (skill != null) {
                    float cost = getSpiritReserve(skill, reservationEfficiency(player, skill));
                    if (reserved + cost <= totalSpirit + 1e-3f) {
                        reserved += cost;
                        fireProcPhases(player, victim, skill, sd.id());
                    }
                }
            }
            slot++;
        }
    }

    /** 点燃一个技能的击杀触发子相位：以死者为中心做一次范围爆发。 */
    private static void fireProcPhases(ServerPlayer player, LivingEntity victim, Skill skill, Identifier skillId) {
        Set<String> tags = new LinkedHashSet<>(skill.tags());
        tags.add(skillId.getPath());
        try (var scope = StatScopes.enter(player, StatContext.of(tags, skillId))) {
            for (SkillPhasePlayer root : skill.phases()) {
                fireProcPhase(player, victim, root.phase());
            }
        } catch (Exception ignored) {
        }
    }

    private static void fireProcPhase(ServerPlayer player, LivingEntity victim, SkillPhase phase) {
        for (SkillPhasePlayer child : phase.children()) {
            fireProcPhase(player, victim, child.phase());
        }
        // 只处理「有投递形态 + ON_HIT 动作」的相位：常驻光环主相位（ON_CAST）在此跳过，
        // 故本方法可安全复用技能树，无需另建一份 proc 数据结构。
        if (phase.delivery().isEmpty()) return;
        boolean hasHit = phase.actions().stream().anyMatch(ta -> ta.trigger() == LifecycleTrigger.ON_HIT);
        if (!hasHit) return;

        ActionContext ctx = new ActionContext(
                null, player, Optional.empty(),
                victim.position(), Vec3.ZERO, 1.0f, 0,
                null, null, null, Optional.empty());
        SkillPhaseInstance instance = new SkillPhaseInstance(phase, UUID.randomUUID());
        instance.setActionContext(ctx);
        DeliveryExecutors.execute(phase.delivery().get(), ctx,
                player.getUUID(), instance.getPhaseID(), instance);
    }

    /**
     * 保留效率（Reservation Efficiency）读取：按 (1 - 效率) 缩放每段光环的精魂保留量。
     *
     * <p><b>作用域</b> = 技能自带标签 + {@code reservation} + {@code spirit}。
     * {@code reservation} 必须带入：保留效率类效果的标签恒含该限定，空作用域会让
     * <b>全部</b>保留效率词条（含最通用的 {@code INC_RESERVATION_EFFICIENCY}）永不命中。
     * 技能标签（{@code aura} / {@code herald} / {@code minion} / {@code meta} / {@code companion}）
     * 决定各自的细分变体（如「捷技能的保留效率提高」）是否命中，故按技能逐个求值而非全局取一次。
     *
     * <p><b>零基属性须 {@code add()}</b>：{@code RESERVATION_EFFICIENCY} 以 base=0 登记
     * （POE2 中「效率」是纯增量），{@code multBase()} 恒为 0 会使整条链路空转。
     */
    private static float reservationEfficiency(ServerPlayer player, Skill skill) {
        Set<String> tags = new HashSet<>(skill.tags());
        tags.add(GateTag.RESERVATION);
        tags.add(GateTag.SPIRIT);
        // 并入结构型 {meta} 标签（镜像 Skill#getStatTags）：元技能宿主（阶段树含 MetaSkillAction）经 cast_on_* 辅助宝石
        // 动态注入，数据侧 tags 从不含 meta；读点不调 getStatTags 故此前恒缺 meta → 该效果为死线。此处补发，使命中。
        if (skill.isMetaSkill()) tags.add(GateTag.META);
        return (float) StatEngine.compute(player, AttributeRegistry.RESERVATION_EFFICIENCY,
                StatContext.of(tags.toArray(new String[0]))).add();
    }

    /** 从精魂 Skill 的 cost 中提取 PRESERVE spirit 的保留量，并按保留效率缩放（无上下文求值）。 */
    private static float getSpiritReserve(Skill skill, float reservationEfficiency) {
        for (ResourceCost cost : skill.cost()) {
            if (cost.usage() == ResourceUsage.PRESERVE && cost.type() == ResourceType.SPIRIT) {
                // 效率 >= 1 时保留归零（POE2 不出现负保留），下限夹紧 0。
                float scale = Math.max(0.0f, 1.0f - reservationEfficiency);
                // 带技能参数槽求值：保留量可写作 $reserve 槽位（预设模板），由 SkillRead.vars 覆盖。
                return (float) cost.value().getValue(
                        ExpressionContext.builder().build().withVariables(skill.params())) * scale;
            }
        }
        return 0;
    }

    /** 每 tick 重新应用精魂光环的 on_cast modifier（作用于施法者自身）。 */
    /**
     * 常驻事件监听注册 —— 「While active, when X happens → Y」类精魂宝石（Combat Frenzy、
     * Siphon Elements 等）的<b>唯一</b>生效入口。
     *
     * <p><b>为何需要它</b>：{@link EventListenPhase#register} 原本只被 {@code SkillPhase.initialize}
     * （单次施放）调用；精魂光环走本类的常驻重放路径、<b>不存在施放</b>，其
     * {@code EventListenEffect} 永远拿不到注册点 —— 监听形同虚设（静默死线）。本方法在光环
     * 激活期注册，并在停用的 tick 由 {@link #tickPlayer} 的 diff 注销，生命周期与光环一致。
     *
     * <p><b>跨 tick 句柄继承</b>：注册是「去重」的（{@code tryReserve}），重复激活不会重复注册，
     * 但也不会再返回句柄。故必须从上一 tick 的登记表中继承句柄，否则关闭光环时无从撤销。
     */
    private static void registerPersistentListens(ServerPlayer player, Skill skill, Identifier skillId,
                                                  Map<Identifier, List<SkillEventBus.ListenerHandle>> active) {
        if (skill.modifiers() == null || skill.modifiers().isEmpty()) return;
        Map<Identifier, List<SkillEventBus.ListenerHandle>> prev =
                LAST_AURA_LISTENS.getOrDefault(player.getUUID(), Map.of());
        List<SkillEventBus.ListenerHandle> handles = active.computeIfAbsent(skillId,
                k -> new ArrayList<>(prev.getOrDefault(k, List.of())));
        if (!handles.isEmpty()) return; // 已注册且仍激活：沿用既有句柄，不重复注册
        SkillInstance instance = new SkillInstance(player, skill, new SkillContext(player, 0), skillId);
        SkillEventBus bus = SkillEventBus.forEntity(player);
        // 宿主相位仅作为事件回调的 SkillPhaseInstance 载体（其 actions 不被重放），取首个根相位即可
        SkillPhase host = skill.phases().isEmpty() ? null : skill.phases().get(0).phase();
        for (SkillModifier modifier : skill.modifiers()) {
            if (modifier instanceof EventListenEffect listen) {
                listen.eventListen().registerDetachable(instance, player, bus, host).ifPresent(handles::add);
            }
        }
    }

    private static void applyAura(ServerPlayer player, Identifier skillId, Skill skill,
                                 Set<Identifier> activeMods, Set<Identifier> activeStats,
                                 Set<String> periodUsed, Map<String, Integer> periodTimers) {
        // target = 施法者自身；skill 为 null（光环无固定 SkillInstance），
        // 当前精魂 modifier 为固定值，对 null SkillContext 安全。
        ActionContext actionCtx = new ActionContext(
                null, player,
                Optional.empty(),
                player.position(), Vec3.ZERO, 1.0f, 0,
                null, null, null, Optional.empty()
        );
        // 动作序号（深度优先遍历序，技能数据运行期不变 ⇒ 键稳定），用于周期计时器寻址
        int[] actionIndex = {0};
        for (SkillPhasePlayer phasePlayer : skill.phases()) {
            applyPhaseAura(player, skillId, phasePlayer.phase(), actionCtx, activeMods, activeStats,
                    actionIndex, periodUsed, periodTimers);
        }
    }

    /**
     * 每 tick 重新应用精魂光环的 on_cast 动作（作用于施法者自身）。
     *
     * <p><b>周期门控</b>：带 {@code period_ticks} 的动作按「玩家 + 精魂 id + 动作序号」倒计时节流，
     * 每 {@code period} tick 才执行一次 —— 这是「While active, every X seconds Y」类常驻真值的
     * 唯一落点（此前该类真值只能每 tick 触发，故整体缺失）。计时器随光环停用回收。
     */
    private static void applyPhaseAura(ServerPlayer player, Identifier skillId, SkillPhase phase,
                                       ActionContext actionCtx, Set<Identifier> activeMods,
                                       Set<Identifier> activeStats, int[] actionIndex,
                                       Set<String> periodUsed, Map<String, Integer> periodTimers) {
        if (phase.actions() != null) {
            for (TriggeredAction ta : phase.actions()) {
                int index = actionIndex[0]++;
                if (ta.trigger() != LifecycleTrigger.ON_CAST) continue;
                if (ta.period().isPresent()) {
                    String key = skillId + "#" + index;
                    periodUsed.add(key);
                    // 间隔按表达式求值（可写 skill.level：真值「每 (12—10.1) 秒」随等级缩短）
                    int period = (int) Math.max(1, ta.period().get().getValue(ExpressionContext.get(player)));
                    int left = periodTimers.getOrDefault(key, period) - 1;
                    if (left > 0) {
                        periodTimers.put(key, left);
                        continue;
                    }
                    periodTimers.put(key, period);
                }
                // 光环重放只执行「幂等」的两类动作：属性修改器（同来源键覆盖）与状态标记（刷新时长）。
                // 伤害 / 位移 / 召唤等一次性动作不得在此重放——那会让常驻光环每 tick 打一次人。
                if (ta.action() instanceof ModifierAction ma) {
                    ma.execute(actionCtx);
                    // 登记「本动作实际写入的来源键」而非裸属性 id：回收侧必须与写入侧同一派生，
                    // 否则常驻条目泄漏；同属性不同 op/tags 的光环（四颗 Purity）也才能各自独立回收。
                    if (ma.attributeId().isPresent()) {
                        Identifier modId = ma.modId(actionCtx.skill() != null
                                ? actionCtx.skill().getContext() : null);
                        if (modId != null) activeMods.add(modId);
                    }                } else if (ta.action() instanceof StatAction st) {
                    // Stat 条目（典型：Presence 光环 —— 「你存在范围内的敌人 X」）。
                    // 每 tick 以同一来源键覆盖写入 = 刷新；条目自身带 duration 计时域，
                    // 即使本清理路径漏掉也会自灭（双保险）。
                    st.execute(actionCtx, actionCtx.caster() instanceof LivingEntity le
                            ? ExpressionContext.get(le) : null);
                    activeStats.add(st.modId());
                }
            }
        }
        if (phase.children() != null) {
            for (SkillPhasePlayer child : phase.children()) {
                applyPhaseAura(player, skillId, child.phase(), actionCtx, activeMods, activeStats,
                        actionIndex, periodUsed, periodTimers);
            }
        }
    }

    /**
     * 施加动态光环状态：状态 id = 技能 id，按 {@code StatusBehavior#AURA_PROVIDER_BY_ID} 分发
     * 其数值贡献（直落真实属性，非旧 attrBuff 的死线）。每 tick 幂等重放：
     * {@link StatusEngine#applySkill} 内部以「不可叠层 + 取较大时长」规则刷新，不叠层。
     * 数值幅度基准 1.0（光环增益不随层数缩放）。
     */
    private static void applyAuraStatus(ServerPlayer player, Identifier skillId, Skill skill) {
        StatusKind kind = skill.tags().contains(GateTag.HERALD) ? StatusKind.HERALD : StatusKind.AURA;
        StatusEngine.applySkill(player, player, skillId, kind, 1.0, AURA_REFRESH_TICKS);
    }

    /**
     * 移除某精魂光环经 {@link ModifierAction} 写入的常驻属性条目：按稳定来源键前缀
     * {@code timed:<modId>} 精确移除（2-4 增量 API），替代按 tags 过滤整表重建。
     *
     * <p><b>modId 必须由调用方从动作本身取（{@link ModifierAction#modId}），不得在此由裸属性 id
     * 重新派生</b>：同一属性可有 op / tags 不同的多条增益（四颗 Purity 抗性、wind_dancer 与
     * 钢铁看守的闪避），按裸属性派生会让它们共用一键，回收时把仍生效的光环一并删掉。
     */
    private static void removeAura(LivingEntity entity, Identifier modId) {
        if (modId == null || !entity.hasData(AttachmentRegistry.STAT_STORE)) return;
        StatStore store = entity.getData(AttachmentRegistry.STAT_STORE);
        StatStore next = store.removeByKeyPrefix("timed:" + modId);
        if (next != store) entity.setData(AttachmentRegistry.STAT_STORE, next);
    }
}
