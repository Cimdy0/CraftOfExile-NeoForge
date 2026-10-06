package com.cimdy.coe.data.helper.hub;

import com.cimdy.coe.data.codec.skill.helper.resource.ResourceCost;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceType;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 资源保留管理器 — 跟踪每个玩家每种资源被“保留”的量（<b>按资源分池</b>，不限于 SPIRIT）。
 *
 * <p>PoE2 对应：光环 / 先驱等技能通过保留（reservation）持续占用一部分资源（精魂 / 生命 / 魔力…），
 * 被保留的部分不再可用于其他技能，但也不会被消耗掉；技能停止时自动释放。
 *
 * <p>可用量 = 总资源 − 已保留量。{@link com.cimdy.coe.data.codec.skill.helper.resource.ResourceType}
 * 在判定时扣除已保留量，{@link com.cimdy.coe.data.codec.skill.helper.resource.ResourceCost#runCost}
 * 在施放时登记保留。
 *
 * <p><b>资源维度由参数承载</b>（{@link GateTag} 资源标签：{@code spirit}/{@code health}/{@code mana} …），
 * 池的本体是「玩家 → 资源 → 来源组件 → 量」三层表，加一种可保留资源<b>零 Java 改动</b>，
 * 只需调用方传入新资源键。既有的精魂调用方全部显式传 {@link GateTag#SPIRIT}。
 *
 * <p>保留量按<b>来源组件</b>分段累加，避免不同来源互相覆盖：
 * <ul>
 *   <li>{@link #AURA} 组件由 {@link SpiritManager} 每 tick 覆盖式写入
 *       （精魂光环为常驻、会从空重算，故用覆盖而非累加）；</li>
 *   <li>{@link #CAST} 组件由 {@link com.cimdy.coe.data.codec.skill.helper.resource.ResourceCost#runCost}
 *       在技能施放时叠加登记（非光环的保留型技能）。</li>
 * </ul>
 * {@link #getReserved} 返回该资源所有组件之和，故两类保留共存时互不丢失（排除「光环覆盖式写入误吞
 * 施放型保留」的隐患）。
 */
public class ReservationManager {
    /** 玩家 → 资源 → (保留来源组件 → 保留量)。 */
    private static final Map<UUID, Map<String, Map<String, Float>>> RESERVED = new ConcurrentHashMap<>();

    /** 精魂光环组件键：由 SpiritManager 每 tick 覆盖写入。 */
    public static final String AURA = "aura";
    /** 技能施放组件键：由 ResourceCost.runCost 叠加登记。 */
    public static final String CAST = "cast";
    /** 图腾组件键：由 SpiritManager 每 tick 覆盖写入（在场图腾数 × 每个图腾保留量）。 */
    public static final String TOTEM = "totem";

    /** 登记一笔保留（叠加，归入 {@link #CAST} 组件）。 */
    public static void reserve(UUID playerId, String resource, float amount) {
        if (amount <= 0) return;
        components(playerId, resource).merge(CAST, amount, Float::sum);
    }

    /**
     * 释放一笔施放型保留（按金额扣减 {@link #CAST} 组件，下限夹紧 0）。
     * 供 {@code SkillInstance.teardown()} 在技能结束时调用，修复「CAST 组件只累加、永不回收」的保留泄漏。
     * 精魂保留不归此处（由 {@link #AURA}/{@link #TOTEM} 组件，由 SpiritManager 每 tick 重算）。
     */
    public static void releaseCast(UUID playerId, String resource, float amount) {
        if (amount <= 0) return;
        Map<String, Float> comps = componentsOrNull(playerId, resource);
        if (comps == null) return;
        float cur = comps.getOrDefault(CAST, 0f);
        float next = Math.max(0f, cur - amount);
        if (next <= 0f) comps.remove(CAST);
        else comps.put(CAST, next);
        prune(playerId, resource, comps);
    }

    /** 获取某玩家某资源当前已保留的总量（所有组件之和）。 */
    public static float getReserved(UUID playerId, String resource) {
        Map<String, Float> comps = componentsOrNull(playerId, resource);
        if (comps == null) return 0f;
        float sum = 0f;
        for (float v : comps.values()) sum += v;
        return sum;
    }

    /**
     * 覆盖式设置某玩家某资源的<b>光环</b>保留量（{@link #AURA} 组件）。
     * 与 {@link #reserve} 的累加语义不同，本方法直接替换 AURA 组件（精魂光环每 tick 从空重算，
     * 必须覆盖而非累加），且<b>不影响 {@link #CAST} 组件</b>，二者在 {@link #getReserved} 中求和，
     * 从而排除「光环覆盖式写入误吞施放型保留」的隐患。
     */
    public static void setReserved(UUID playerId, String resource, float amount) {
        setComponent(playerId, resource, AURA, amount);
    }

    /**
     * 覆盖式设置某玩家某资源的<b>图腾</b>保留量（{@link #TOTEM} 组件，每 tick 从在场图腾数重算）。
     * 与 {@link #AURA} 同为覆盖语义（数量随时变化，须整体重算），且互不影响、在 {@link #getReserved} 求和。
     */
    public static void setTotemReserved(UUID playerId, String resource, float amount) {
        setComponent(playerId, resource, TOTEM, amount);
    }

    /** 覆盖式写组件（amount ≤ 0 视为无保留 → 移除该组件）。 */
    private static void setComponent(UUID playerId, String resource, String component, float amount) {
        if (amount <= 0) {
            Map<String, Float> comps = componentsOrNull(playerId, resource);
            if (comps == null) return;
            comps.remove(component);
            prune(playerId, resource, comps);
            return;
        }
        components(playerId, resource).put(component, amount);
    }

    // —— 未保留量（unreserved）通用判定 ——
    //
    // 「未保留量」= 资源上限 − 已保留量；「未保留部分已满」= 当前量 ≥ 未保留量。
    // 二者<b>完全由资源键参数化</b>：上限 / 当前量取自 ResourceEventHelper 的资源表（CURRENTS / MAXES），
    // 保留量取自本类的分资源池，故不写任何 health/mana/spirit 专属分支。
    // 典型消费点：ResourceEventHelper#onHeal 的「资源满 → 移除偷取」门控
    // （「Life Leech effects are not removed when Unreserved Life is Filled」）。

    /**
     * 某资源的<b>未保留量</b>（上限 − 已保留，下限夹紧 0）。
     *
     * @param living   实体（服务端权威；客户端无保留表，故按 0 处理）
     * @param resource 资源键（{@link GateTag} 资源标签）
     * @return 未保留量；资源键未知（无上限表项）时返回 {@code -1}，调用方应据此跳过判定
     */
    public static float unreserved(LivingEntity living, String resource) {
        if (living == null) return -1f;
        float max = ResourceEventHelper.max(living, resource);
        if (max < 0f) return -1f;
        return Math.max(0f, max - getReserved(living.getUUID(), resource));
    }

    /**
     * 某资源的<b>未保留部分是否已满</b>（当前量 ≥ 上限 − 已保留）。
     * 与 {@link #unreserved} 同源，同样完全由资源键参数化。
     */
    public static boolean isUnreservedFull(LivingEntity living, String resource) {
        float cap = unreserved(living, resource);
        if (cap < 0f) return false;
        return ResourceEventHelper.current(living, resource) >= cap;
    }

    /** 清理离线玩家的保留量（仅保留 {@code online} 集合内的条目）。 */
    public static void cleanup(Set<UUID> online) {
        RESERVED.keySet().retainAll(online);
    }

    // —— 内部：三层表访问 ——

    /** 取（必要时创建）某玩家某资源的组件表。 */
    private static Map<String, Float> components(UUID playerId, String resource) {
        return RESERVED.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(resource, k -> new ConcurrentHashMap<>());
    }

    /** 只读取组件表（不存在返回 null，不创建中间层）。 */
    private static Map<String, Float> componentsOrNull(UUID playerId, String resource) {
        Map<String, Map<String, Float>> byResource = RESERVED.get(playerId);
        return byResource == null ? null : byResource.get(resource);
    }

    /** 组件表空则连同资源层 / 玩家层一并回收，避免空壳条目累积。 */
    private static void prune(UUID playerId, String resource, Map<String, Float> comps) {
        if (!comps.isEmpty()) return;
        Map<String, Map<String, Float>> byResource = RESERVED.get(playerId);
        if (byResource == null) return;
        byResource.remove(resource);
        if (byResource.isEmpty()) RESERVED.remove(playerId);
    }
}
