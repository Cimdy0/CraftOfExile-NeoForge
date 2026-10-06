package com.cimdy.coe.data.codec.skill.phase.action.support;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.data.gen.stat.effect.StatEffectIds;
import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionTypes;
import com.cimdy.coe.data.codec.skill.phase.action.movement.CreateGroundAction;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;

import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.registry.world.EntityRegistry;
import com.cimdy.coe.entity.custom.skill.visual.SkillBanner;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

/**
 * 创建战旗动作（POE2 Banner 子系统）— 在施法位置投放一片<b>战旗区域</b>。
 *
 * <p>与 {@link CreateGroundAction} 同构（构造实体 → 配置 → addFreshEntity），
 * 但产出的是<b>专属战旗实体</b>：每 5 tick 对半径内友方实体施加 {@link BuffStatusTypes#BANNER_AURA}
 * 增益（在区域内持续刷新=常驻）；实体离开区域后，最后一次施加的增益按
 * {@code (基础残留 + add_banner_linger) 秒}自然过期 —— 即「离开战旗区域后增益残留 N 秒」（bannerman）。
 *
 * <p><b>Stat 缩放</b>：
 * <ul>
 *   <li>幅度经 {@code {aura, banner}} 作用域读 {@link AttributeRegistry#AILMENT_MAGNITUDE} 缩放，
 *       使 {@code inc_banner_aura_magnitude} 成为活消费者；</li>
 *   <li>残留秒数经 {@link AttributeRegistry#BANNER_LINGER}（bannerman 节点平加）读取；</li>
 *   <li>放置同时清空施法者 {@code "banner"} 荣耀槽（POE2「放置 Banner 消耗全部荣耀」最小实现）。</li>
 * </ul>
 */
public record BannerAction(
        ExpressionCodec.Value radius,
        ExpressionCodec.Value areaDuration,
        ExpressionCodec.Value magnitude
) implements Action {

    /** 基础残留秒数（无任何加成时离开区域后增益仍残留 1 秒）。 */
    private static final double BASE_LINGER_SECONDS = 1.0;
    /** 战旗区域默认存在时间（秒）。 */
    private static final double DEFAULT_AREA_SECONDS = 30.0;

    public static final Codec<BannerAction> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("radius", ExpressionCodec.of(4.0))
                    .forGetter(BannerAction::radius),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("area_duration", ExpressionCodec.of(DEFAULT_AREA_SECONDS))
                    .forGetter(BannerAction::areaDuration),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("magnitude", ExpressionCodec.of(1.0))
                    .forGetter(BannerAction::magnitude)
    ).apply(i, BannerAction::new));

    @Override public Action.Type<?> getType() { return ActionTypes.BANNER; }

    @Override
    public void execute(ActionContext context) {
        if (!(context.caster() instanceof LivingEntity caster)) return;
        if (!(caster.level() instanceof ServerLevel serverLevel)) return;

        SkillContext skillCtx = context.skill() != null ? context.skill().getContext() : null;
        float baseRadius = Math.max(0.5f, radius.getFloat(skillCtx));
        float areaSeconds = Math.max(1f, areaDuration.getFloat(skillCtx));
        float baseMagnitude = magnitude.getFloat(skillCtx);

        // {aura, banner} 作用域读 AILMENT_MAGNITUDE：使 inc_banner_aura_magnitude 成为活消费者。
        // 该缩放直接烘焙进 entry amount（与旧实现逐字段等价）；迁移后经 StatusStatEffect 落地，
        // 引擎侧 effectMul 不再二次乘 aura（auraSourced=false），从而消除旧路径的平方放大（双计量雷点）。
        Set<String> scope = Set.of(GateTag.AURA, GateTag.BANNER);
        double magnitudeMult = scopedMultiplier(caster, AttributeRegistry.AILMENT_MAGNITUDE, scope);

        // 残留秒数 = 基础 + bannerman 平加（add_banner_linger）。
        double lingerSeconds = BASE_LINGER_SECONDS
                + AilmentHelper.withScope(caster, Set.of(GateTag.BANNER, GateTag.not(GateTag.TAKEN)), AttributeRegistry.BANNER_LINGER);
        int lingerTicks = (int) Math.max(1, Math.round(lingerSeconds * 20.0));

        // 战旗数量上限：已达上限则本次放置不生效（也不消耗荣耀），见 canPlaceBanner。
        if (!canPlaceBanner(caster, serverLevel)) return;

        // 放置消耗全部荣耀（最小实现）：先记录消耗数量，战旗到期按回收比例归还（#41 / GAP-FIX#17）。
        int spentGlory = ResourceRegistry.getGlory(caster, ResourceRegistry.BANNER_GLORY);
        ResourceRegistry.clearGlory(caster, ResourceRegistry.BANNER_GLORY);

        // 「Skill Effect Duration」统一落到 AILMENT_DURATION（StatEffectIds 设计注释）；
        // 以 {AILMENT, !TAKEN} 作用域读取，使 inc_skill_effect_duration 族（含 freeze per-enemy）生效。
        double skillEffectDurationMult = AilmentHelper.withScope(caster,
                Set.of(GateTag.AILMENT, GateTag.BANNER, GateTag.not(GateTag.TAKEN)), AttributeRegistry.BANNER_DURATION);
        int lifetime = (int) Math.max(1, areaSeconds * 20.0 * skillEffectDurationMult);

        // 元素抗性增益：读取施法者 banner_ele_res（0 表示不授予），>0 才注册第二条 entry。
        double res = StatEngine.magnitudeOf(caster, "banner_ele_res", Set.of());

        Vec3 pos = context.position();
        SkillBanner banner = new SkillBanner(EntityRegistry.SKILL_BANNER.get(), serverLevel);
        banner.setPos(pos.x, pos.y, pos.z);
        // 构造两个 StatEntry（半径光环全部复用 StatusStatEffect + 新增 RADIUS_ALLIES Target + 时间层），
        // 烘焙幅度（amount 已含 {aura,banner} 缩放）后交由 SkillBanner 周期性驱动。
        StatEntry auraEntry = SkillBanner.buildAuraEntry(baseMagnitude * magnitudeMult, baseRadius, lingerTicks);
        StatEntry resEntry = res > 0.0 ? SkillBanner.buildResEntry(res, baseRadius, lingerTicks) : null;
        banner.configure(caster, auraEntry, resEntry, baseRadius, lifetime, spentGlory);
        serverLevel.addFreshEntity(banner);
    }

    /** 当前施法者是否已达战旗数量上限（见 {@link #bannerLimit}）。 */
    private static boolean canPlaceBanner(LivingEntity owner, ServerLevel level) {
        int limit = bannerLimit(owner);
        if (limit <= 0) return false;
        int count = 0;
        for (Entity e : level.getEntities(owner, owner.getBoundingBox().inflate(64.0))) {
            if (e instanceof SkillBanner banner && owner.equals(banner.getOwner())) {
                if (++count >= limit) return false;
            }
        }
        return true;
    }

    /** 战旗数量上限：直接读取施法者 MAX_BANNERS 属性（默认 1，「+X to Maximum number of placed Banners」平加上调）。 */
    private static int bannerLimit(LivingEntity owner) {
        return (int) owner.getAttributeValue(AttributeRegistry.MAX_BANNERS);
    }

    /**
     * 在指定标签作用域下读取施法者属性（倍率型百分比属性，基值 1.0），收口至 {@link ActionContext#stat}。
     */
    private static double scopedMultiplier(LivingEntity caster, Holder<Attribute> attribute, Set<String> scope) {
        return ActionContext.stat(caster, attribute, scope, null);
    }
}
