package com.cimdy.coe.data.codec.skill.support;

import com.cimdy.coe.data.codec.skill.modifier.impl.effect.AdditionalActionEffect;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.DeliveryMod;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.EchoMod;
import com.cimdy.coe.data.codec.skill.modifier.impl.effect.EffectMod;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.SealMod;
import com.cimdy.coe.data.codec.skill.helper.state.EchoState;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.DeliveryStatEffect;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.modifier.SkillModifier;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.value.Value;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运行时修正聚合体 — 聚合「运行时需逐次查询」的三类修正：
 * <ul>
 *   <li>{@code deliveryMods}：投递修正（DeliveryMod），由各投递执行器按自身投递类型读取参数；</li>
 *   <li>{@code effectMods}：效果修正（EffectMod），由伤害动作按作用域（投递/伤害类型/条件）应用；</li>
 *   <li>{@code echoMods}：回响（EchoMod），由 {@code EchoState} 在施放结算时读取，按定向重放阶段；</li>
 *   <li>{@code sealMods}：封印（SealMod），由 {@code SkillInstance} 在施放结算时读取，驱动封印累积/释放。</li>
 * </ul>
 * 数据来源同时包含<b>技能自身</b>（Skill.modifiers）与<b>链接的辅助宝石</b>（SupportGem.effects）——
 * 二者共用同一套 SkillModifier 形状，因此技能本身也能做到斩杀、附加伤害、转伤等辅助宝石能做的事。
 * 等级 / 品质 / 消耗 / 速度等「装配期」特殊修正由 SkillLinkResolver 在链接时处理，不进入此处。
 *
 * <p>数值字段使用 ExpressionCodec.Value，支持常量或数学表达式。所有 getter 接受 SkillContext 用于运行时求值。
 */
public record SupportProfile(
        List<DeliveryMod> deliveryMods,
        List<EffectMod> effectMods,
        List<EchoMod> echoMods,
        List<SealMod> sealMods
) {
    public static final SupportProfile EMPTY = new SupportProfile(List.of(), List.of(), List.of(), List.of());

    public static SupportProfile from(List<SupportGem> supportGems) {
        return from(null, supportGems, List.of());
    }

    public static SupportProfile from(List<SupportGem> supportGems, List<Integer> supportLevels) {
        return from(null, supportGems, supportLevels);
    }

    /**
     * 从「技能自身 modifiers」+「辅助宝石 effects」+「持有者 Stat 投送修饰」聚合运行时修正。
     * 三者共用同一套参数形状（DeliveryStatEffect / DeliveryMod 的投送参数），因此技能、辅助宝石、
     * 装备词缀、天赋、珠宝的投送修饰被统一合并——POE2 额外投射物/穿透/链/叉可来自任意渠道。
     * 装配期特殊修正（SpecialMod）与注入动作（AdditionalActionEffect）不在此处处理，
     * 它们由 SkillLinkResolver 在链接装配时消费。
     *
     * @param skill 已解析的基础技能（可为 null，例如仅聚合辅助宝石时）
     * @param supportGems 已解析的辅助宝石定义列表
     * @param supportLevels 辅助宝石自身等级列表（保留签名兼容，当前未用于运行时修正）
     */
    public static SupportProfile from(Skill skill, List<SupportGem> supportGems, List<Integer> supportLevels) {
        if (supportGems == null) supportGems = List.of();
        List<DeliveryMod> del = new ArrayList<>();
        List<EffectMod> eff = new ArrayList<>();
        List<EchoMod> echo = new ArrayList<>();
        List<SealMod> seal = new ArrayList<>();
        // 技能自身的修正（与辅助宝石共用同一套形状）
        if (skill != null) {
            for (SkillModifier m : skill.modifiers()) {
                if (m instanceof DeliveryMod d) del.add(d);
                else if (m instanceof EffectMod e) eff.add(e);
                else if (m instanceof EchoMod c) echo.add(c);
                else if (m instanceof SealMod s) seal.add(s);
            }
        }
        // 辅助宝石的修正
        for (SupportGem gem : supportGems) {
            for (SkillModifier m : gem.effects()) {
                if (m instanceof DeliveryMod d) del.add(d);
                else if (m instanceof EffectMod e) eff.add(e);
                else if (m instanceof EchoMod c) echo.add(c);
                else if (m instanceof SealMod s) seal.add(s);
            }
        }
        // 持有者 Stat 投送修饰（装备/天赋/珠宝词缀，fold 型查询）：与技能/辅助同形状合并。
        // 入口 owner 从 ActionContext 取（from(ActionContext) 路径已注入），此处经持有者实体查询。
        return new SupportProfile(del, eff, echo, seal);
    }

    /**
     * 从 ActionContext 中提取 SupportProfile（供 Delivery/Action 执行器使用），
     * 并把持有者的 Stat 投送修饰（装备/天赋/珠宝词缀）合并进同一投送参数空间。
     */
    public static SupportProfile from(ActionContext context) {
        if (context.skill() != null && context.skill().getContext() != null) {
            SkillContext skillCtx = context.skill().getContext();
            SupportProfile base = skillCtx.getSupportProfile();
            if (!(context.caster() instanceof LivingEntity owner)) return base;
            // 合并 Stat 投送修饰：把 DeliveryStatEffect 条目并入 deliveryMods（参数语义对齐）
            List<StatEngine.DeliveryResult> statDeliveries = StatEngine.delivery(owner);
            if (statDeliveries.isEmpty()) return base;
            List<DeliveryMod> merged = new ArrayList<>(base.deliveryMods());
            for (StatEngine.DeliveryResult r : statDeliveries) {
                merged.add(deliveryFromStat(r));
            }
            return new SupportProfile(List.copyOf(merged), base.effectMods(), base.echoMods(), base.sealMods());
        }
        return EMPTY;
    }

    /** 把 Stat 投送查询结果（delivery + params）转换为 DeliveryMod（投送参数形状对齐）。 */
    private static DeliveryMod deliveryFromStat(StatEngine.DeliveryResult r) {
        Map<String, ExpressionCodec.Value> params = new HashMap<>();
        r.params().forEach((k, v) -> params.put(k, ExpressionCodec.of(v)));
        return new DeliveryMod(r.delivery(), params);
    }

    // ---- 投递参数查询（按投递类型 id 分流） ----

    private float deliveryProduct(String deliveryId, String param, SkillContext ctx, float def) {
        float m = def;
        for (DeliveryMod d : deliveryMods)
            if (d.delivery().equalsIgnoreCase(deliveryId))
                m *= d.floatParam(param, ctx, 1f);
        return m;
    }

    private int deliverySum(String deliveryId, String param, SkillContext ctx) {
        int s = 0;
        for (DeliveryMod d : deliveryMods)
            if (d.delivery().equalsIgnoreCase(deliveryId))
                s += d.intParam(param, ctx, 0);
        return s;
    }

    private boolean deliveryAnyTrue(String deliveryId, String param, SkillContext ctx) {
        for (DeliveryMod d : deliveryMods)
            if (d.delivery().equalsIgnoreCase(deliveryId))
                if (d.boolParam(param, ctx, false)) return true;
        return false;
    }

    public int additionalProjectiles(SkillContext ctx) { return deliverySum("projectile", "additional_projectiles", ctx); }
    public int pierceCount(SkillContext ctx) { return deliverySum("projectile", "pierce_count", ctx); }
    public int chainCount(SkillContext ctx) { return deliverySum("projectile", "chain_count", ctx); }
    public int forkCount(SkillContext ctx) { return deliverySum("projectile", "fork_count", ctx); }
    /** 弹幕齐射次数（barrage）：每轮齐射额外再发射一次完整齐射，形成弹幕。 */
    public int barrageCount(SkillContext ctx) { return deliverySum("projectile", "barrage_count", ctx); }
    public float projectileSpeedMultiplier(SkillContext ctx) { return deliveryProduct("projectile", "speed_multiplier", ctx, 1f); }
    public boolean fireInCircle(SkillContext ctx) { return deliveryAnyTrue("projectile", "fire_in_circle", ctx); }
    public boolean returnToCaster(SkillContext ctx) { return deliveryAnyTrue("projectile", "return_to_caster", ctx); }

    /** 按投递类型读取半径/尺寸倍率（area/aura/beam 等共用 radius_multiplier 参数名）。 */
    public float radiusMultiplier(String deliveryId, SkillContext ctx) {
        return deliveryProduct(deliveryId, "radius_multiplier", ctx, 1f);
    }

    public boolean nova(SkillContext ctx) { return deliveryAnyTrue("area", "nova", ctx); }
    public int cascadeCount(SkillContext ctx) { return deliverySum("area", "cascade", ctx); }
    public int additionalTargets(SkillContext ctx) { return deliverySum("area", "additional_targets", ctx); }

    // ---- 效果参数查询 ----

    public boolean hasAddedDamage() {
        return effectMods.stream().anyMatch(m -> m.added().isPresent());
    }

    /** 全局暴击率倍率（所有 EffectMod 连乘）。 */
    public float critChanceMultiplier(SkillContext ctx) {
        float m = 1f;
        for (EffectMod m2 : effectMods) m *= m2.critChanceMultiplier().map(v -> v.getFloat(ctx)).orElse(1f);
        return m;
    }

    /** 全局暴击伤害倍率（所有 EffectMod 连乘）。 */
    public float critDamageMultiplier(SkillContext ctx) {
        float m = 1f;
        for (EffectMod m2 : effectMods) m *= m2.critDamageMultiplier().map(v -> v.getFloat(ctx)).orElse(1f);
        return m;
    }

    /** 每次分裂（Fork）后投射物伤害倍率（所有匹配投射物作用域的 EffectMod 连乘）。 */
    public float projectileDamageMultiplierPerSplit(SkillContext ctx) {
        float m = 1f;
        for (EffectMod m2 : effectMods) {
            if (m2.delivery().isPresent() && !m2.delivery().get().equalsIgnoreCase("projectile")) continue;
            m *= m2.damageMultiplierPerSplit().map(v -> v.getFloat(ctx)).orElse(1f);
        }
        return m;
    }
}
