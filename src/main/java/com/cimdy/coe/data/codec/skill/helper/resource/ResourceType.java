package com.cimdy.coe.data.codec.skill.helper.resource;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;

import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.phase.action.charge.ChargeType;

import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.helper.affix.*;
import com.cimdy.coe.data.helper.combat.*;
import com.cimdy.coe.data.helper.hub.*;
import com.cimdy.coe.data.helper.resource.*;
import com.cimdy.coe.data.helper.skill.*;
import com.cimdy.coe.data.helper.stat.*;

import com.cimdy.coe.data.helper.status.*;
import com.cimdy.coe.helper.MathHelper;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.IntFunction;
import java.util.Set;

public enum ResourceType implements StringRepresentable {
    ENERGY("energy", 0),
    HEALTH("health", 1),
    MANA("mana", 2),
    /** PoE2: 灵魂 — 用于光环/先驱/持久增益的保留资源 */
    SPIRIT("spirit", 3),
    /** 耐力充能球：消耗用于增强物理技能 / 冷却减免 / 防御性免疫 */
    ENDURANCE_CHARGE("endurance_charge", 4),
    /** 狂怒充能球：消耗用于攻速/施法速度增强 */
    FRENZY_CHARGE("frenzy_charge", 5),
    /** 暴击充能球：消耗用于暴击率/暴伤增强 */
    POWER_CHARGE("power_charge", 6),
    /** 连击（Combo）：每技能独立累积，消耗用于技能增强（Tempest Bell 等） */
    COMBO("combo", 7),
    /** 荣耀（Glory）：每 Banner 类技能独立累积，消耗用于放置旗帜激活增益 */
    GLORY("glory", 8),
    /** 怒气（Rage）：PoE2 战争资源——近战命中获取（{melee} 标签），上限 MAX_RAGE（默认 50），固有流失自然扣除 */
    RAGE("rage", 9),
    /** 波动（Volatility）：POE2 资源——击杀/被眩晕获取，积攒至上限触发爆炸（详见 VolatilityHelper）。 */
    VOLATILITY("volatility", 10),
    /** 元素灌入（Elemental Infusion）：POE2 资源——灌入期间强化元素伤害（详见 InfusionHelper）。 */
    INFUSION("infusion", 11),
    /** 能量护盾（Energy Shield）：承伤吸收层——受击先扣 ES（细则见 EnergyShieldHelper）。 */
    ENERGY_SHIELD("energy_shield", 12);

    public static final IntFunction<ResourceType> BY_ID =
            ByIdMap.continuous(ResourceType::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);

    /** 按序列化名查找（如 {@code "health"} / {@code "mana"}），找不到返回 null。 */
    public static @Nullable ResourceType byName(String name) {
        for (ResourceType t : values()) if (t.name.equals(name)) return t;
        return null;
    }
    public static final Codec<ResourceType> CODEC =
            StringRepresentable.fromEnum(ResourceType::values);
    private final String name;
    private final int id;

    ResourceType(String name, int value) {
        this.name = name;
        this.id = value;
    }

    public int getId() {
        return this.id;
    }

    public String getSerializedName() {
        return this.name;
    }

    /** 元技能能量分槽键：技能上下文技能 id，缺省 "default"。 */
    private static String skillKey(SkillContext context) {
        String id = context == null ? null : context.getSkillId();
        return id == null || id.isEmpty() ? "default" : id;
    }

    /**
     * 连击分槽键：优先本次施放由 ComboAction 灌入的 comboId（保住 per-skill 分槽），
     * 缺省回落到 {@link ResourceRegistry} 既有的通用默认槽 {@code "default"}
     * —— 即 GrantOnEventStatEffect 的 GrantTarget.COMBO（param="default"）实际写入的槽，
     * 不在此处另编键名。
     */
    private static String comboKey(SkillContext context) {
        String id = context == null ? null : context.getComboId();
        return id == null || id.isEmpty() ? "default" : id;
    }

    /**
     * 荣耀分槽键：优先本次施放由 Banner 类技能灌入的 gloryId（保住 per-skill 分槽），
     * 缺省回落到 {@link ResourceRegistry#BANNER_GLORY}——即 DeathHelper.generateGlory 实际写入的槽。
     * 注意：不能用 {@code ResourceRegistry.current(living, "glory")}，该泛型入口恒读 "default" 槽，
     * 与荣耀的真实写入槽 "banner" 不一致，会恒读 0。
     */
    private static String gloryKey(SkillContext context) {
        String id = context == null ? null : context.getGloryId();
        return id == null || id.isEmpty() ? ResourceRegistry.BANNER_GLORY : id;
    }

    public boolean match(SkillContext context, double value) {
        LivingEntity caster = context.getCaster();
        return switch (this) {
            // 元技能能量按技能 id 分槽（保留 per-skill 语义，不走注册表泛型键）
            case ENERGY -> ResourceRegistry.getMetaEnergy(caster, skillKey(context)) > value;
            case HEALTH -> ResourceRegistry.current(caster, "health") > value;
            case MANA -> ResourceRegistry.current(caster, "mana") > value;
            // 精魂按「未保留量」判定（保留量由 ReservationManager 另行记账）
            case SPIRIT -> ResourceRegistry.getSpirit(caster) - ResourceRegistry.getReservedSpirit(caster) > value;
            case ENDURANCE_CHARGE -> ResourceRegistry.current(caster, "endurance_charge") >= (int) value;
            case FRENZY_CHARGE -> ResourceRegistry.current(caster, "frenzy_charge") >= (int) value;
            case POWER_CHARGE -> ResourceRegistry.current(caster, "power_charge") >= (int) value;
            // 连击/荣耀直读附件持久层（不再依赖 SkillContext 缓存——该缓存仅由 ComboAction 灌入，
            // 无 ComboAction 的技能恒读 -1/0，使本行恒 false）。分槽键见 comboKey/gloryKey。
            case COMBO -> ResourceRegistry.getCombo(caster, comboKey(context)).count() >= (int) value;
            case GLORY -> ResourceRegistry.getGlory(caster, gloryKey(context)) >= (int) value;
            case RAGE -> ResourceRegistry.current(caster, "rage") > value;
            case VOLATILITY -> ResourceRegistry.current(caster, "volatility") >= value;
            case INFUSION -> ResourceRegistry.current(caster, "infusion") >= value;
            case ENERGY_SHIELD -> ResourceRegistry.current(caster, "energy_shield") > value;
        };
    }

    public void cost(SkillContext context, double value) {
        LivingEntity caster = context.getCaster();
        switch (this) {
            // 元技能能量按技能 id 分槽消耗（per-skill 语义，不走注册表泛型键）
            case ENERGY -> ResourceRegistry.consumeMetaEnergy(caster, skillKey(context), (int) value);
            // 生命/魔力/能量护盾经统一资源枢纽（保留 RESOURCE_SPEND 事件与 resource_convert 接线）
            case HEALTH -> ResourceEventHelper.onSpend(caster, GateTag.HEALTH, (float) value, "skill", null);
            case MANA -> ResourceEventHelper.onSpend(caster, GateTag.MANA, (float) value, "skill", null);
            case ENERGY_SHIELD -> ResourceEventHelper.onSpend(caster, GateTag.ENERGY_SHIELD, (float) value, "skill", null);
            // 精魂按保留量消耗（不经枢纽：精魂无 RESOURCE_SPEND 语义）
            case SPIRIT -> ResourceRegistry.hurtSpirit(caster, (float) value);
            // 充能球经真实消耗通道（CONSUME_CHARGE 事件 / 上下文豁免 / 消耗跳过几率）
            case ENDURANCE_CHARGE, FRENZY_CHARGE, POWER_CHARGE -> {
                ChargeType ct = chargeType(this);
                ResourceRegistry.consumeCharge(caster, ct, (int) value, null, null, Set.of());
            }
            // 连击/荣耀直扣附件持久层（绕过 SkillContext 缓存的 comboId==null 早退）。
            // 连击经 ComboSave.consume 发射 COMBO_SPENT（sum:combo_spent 的唯一来源）；
            // 荣耀经 GlorySave.consume 发射 CONSUME_GLORY（含 GLORY_CONSUME_SKIP_CHANCE 掷骰）。
            case COMBO -> ResourceRegistry.consumeCombo(caster, comboKey(context), (int) value, List.of(), null, null, null);
            case GLORY -> ResourceRegistry.consumeGlory(caster, gloryKey(context), (int) value);
            // 怒气经统一资源枢纽（RESOURCE_SPEND 事件 + 转化），后端 spendBackend 落值
            case RAGE -> ResourceRegistry.spendRage(caster, (float) value);
            // 波动经真实消耗通道（钳制下限 0）
            case VOLATILITY -> ResourceRegistry.spendVolatility(caster, (float) value);
            // 元素灌入经唯一消耗入口（consumeAny + 玩家残迹掉落）
            case INFUSION -> ResourceRegistry.onConsumeInfusion(caster, (float) value);
        }
    }

    private static ChargeType chargeType(ResourceType rt) {
        return switch (rt) {
            case ENDURANCE_CHARGE -> ChargeType.ENDURANCE;
            case FRENZY_CHARGE -> ChargeType.FRENZY;
            case POWER_CHARGE -> ChargeType.POWER;
            default -> throw new IllegalStateException("Not a charge type: " + rt);
        };
    }

    public MutableComponent getComponent(double value) {
        String key = switch (this) {
            case ENDURANCE_CHARGE -> "endurance_charge";
            case FRENZY_CHARGE -> "frenzy_charge";
            case POWER_CHARGE -> "power_charge";
            case COMBO -> "combo";
            case GLORY -> "glory";
            case VOLATILITY -> "volatility";
            case INFUSION -> "infusion";
            default -> this.name;
        };
        return Component.literal(MathHelper.df2.format(value))
                .append(" ").append(Component.translatable("stat." + key));
    }
}