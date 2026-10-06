package com.cimdy.coe.registry;

import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.skill.context.SkillDamageSource;
import com.cimdy.coe.data.codec.support.damage.DamageTypes;
import com.cimdy.coe.event.combat.DamageEvent;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.cimdy.coe.data.helper.engine.DamagePipeline;

public class DamageTypeRegistry {
    public static List<ResourceKey<DamageType>> DAMAGE_TYPES = new ArrayList<>();

    public static final ResourceKey<DamageType> SKILL = createDamageType("skill");

    public static final ResourceKey<DamageType> BLEEDING = createDamageType("bleeding");
    public static final ResourceKey<DamageType> IGNITE = createDamageType("ignite");
    public static final ResourceKey<DamageType> POISON = createDamageType("poison");

    // 技能伤害类型（PoE2: 物理 / 火焰 / 闪电 / 冰霜 / 混沌）
    public static final ResourceKey<DamageType> PHYSICAL = createDamageType("physical");
    public static final ResourceKey<DamageType> FIRE = createDamageType("fire");
    public static final ResourceKey<DamageType> LIGHTNING = createDamageType("lightning");
    public static final ResourceKey<DamageType> COLD = createDamageType("cold");
    public static final ResourceKey<DamageType> CHAOS = createDamageType("chaos");

    // 伤害性异常（DoT）拆分输出用的「来源伤害类型」伤害类型：每个来源伤害类型一个独立的 IS_AILMENT 伤害类型，
    // 使该份额外的持续伤害被对应抗性/护甲减免（物理→护甲+物抗、混沌→混沌抗、火→火抗……），而非统一成某种类型。
    // 见 StatusBehavior.dealSplitDoT 与 AilmentHelper 的 composition 模型。
    public static final ResourceKey<DamageType> DOT_PHYSICAL = createDamageType("dot_physical");
    public static final ResourceKey<DamageType> DOT_CHAOS = createDamageType("dot_chaos");
    public static final ResourceKey<DamageType> DOT_FIRE = createDamageType("dot_fire");
    public static final ResourceKey<DamageType> DOT_COLD = createDamageType("dot_cold");
    public static final ResourceKey<DamageType> DOT_LIGHT = createDamageType("dot_light");

    /** 伤害槽位下标 ↔ 伤害类型标签（damages[]: 0=物理 1=混沌 2=火焰 3=冰霜 4=闪电）。
     *  ADR-003 阶段2：改由 {@link DamageTypes#mainTypeTags()} 统一派生，成为单一事实来源，
     *  新增伤害类型经数据包即可扩展，不再硬编码 {@code "5"}。 */
    public static final String[] DAMAGE_TYPE_TAGS = DamageTypes.mainTypeTags();

    private static Identifier dotId(String name) {
        return Identifier.fromNamespaceAndPath(CraftOfExile.MODID, name);
    }

    /** 伤害槽位 ↔ 拆分 DoT 输出伤害类型 ResourceKey（与 {@link #DAMAGE_TYPE_TAGS} 顺序一致）。
     *  minecraft {@link ResourceKey} 与 {@link DamageTypes} 的 dot Identifier 是两个关注：
     *  槽位→dot Identifier 委托 {@link DamageTypes#dotOf(int)}，Identifier→ResourceKey 经本表反查。 */
    private static final Map<Integer, ResourceKey<DamageType>> DOT_BY_SLOT = Map.of(
            0, DOT_PHYSICAL, 1, DOT_CHAOS, 2, DOT_FIRE, 3, DOT_COLD, 4, DOT_LIGHT);
    private static final Map<Identifier, ResourceKey<DamageType>> DOT_BY_ID = Map.of(
            dotId("dot_physical"), DOT_PHYSICAL,
            dotId("dot_chaos"), DOT_CHAOS,
            dotId("dot_fire"), DOT_FIRE,
            dotId("dot_cold"), DOT_COLD,
            dotId("dot_light"), DOT_LIGHT);
    private static final Map<ResourceKey<DamageType>, Integer> DOT_SLOT_OF = Map.of(
            DOT_PHYSICAL, 0, DOT_CHAOS, 1, DOT_FIRE, 2, DOT_COLD, 3, DOT_LIGHT, 4);

    /** 伤害槽位 ↔ 对应的「拆分 DoT 输出伤害类型」ResourceKey。
     *  ADR-003 阶段2：slot → dot Identifier 委托 {@link DamageTypes#dotOf(int)}，再经 {@link #DOT_BY_ID} 反查 ResourceKey。 */
    public static ResourceKey<DamageType> dotDamageType(int slot) {
        Identifier dot = DamageTypes.dotOf(slot);
        return dot == null ? DOT_PHYSICAL : DOT_BY_ID.getOrDefault(dot, DOT_PHYSICAL);
    }

    /** 拆分 DoT 伤害类型 ResourceKey → 伤害槽位（0=物理 … 4=闪电），非 dot_* 返回 -1。 */
    public static int dotSlot(ResourceKey<DamageType> type) {
        return DOT_SLOT_OF.getOrDefault(type, -1);
    }

    /** 伤害类型标签（physical/chaos/fire/cold/lightning）→ 伤害槽位；未知返回 -1。
     *  ADR-003 阶段2：委托 {@link DamageTypes#slotOf(String)}（含全部已登记类型，旧逻辑仅 5 主类型）。 */
    public static int slotOf(String tag) {
        return DamageTypes.slotOf(tag);
    }

    private static ResourceKey<DamageType> createDamageType(String name) {
        ResourceKey<DamageType> damageType = ResourceKey.create(Registries.DAMAGE_TYPE,
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, name));
        DAMAGE_TYPES.add(damageType);
        return damageType;
    }

    public static List<ResourceKey<DamageType>> BYPASS_ENERGY_TYPE = List.of(BLEEDING, POISON);
    public static List<ResourceKey<DamageType>> AILMENT_TYPE = List.of(BLEEDING, POISON, IGNITE,
            DOT_PHYSICAL, DOT_CHAOS, DOT_FIRE, DOT_COLD, DOT_LIGHT);

    /**
     * 把技能伤害类型字符串映射为对应的 {@link DamageType} {@link ResourceKey}。
     * 供 {@link #getDamageSource(ServerLevel, String)} 与状态 DoT 源复用。
     */
    public static ResourceKey<DamageType> keyOf(String type) {
        return switch (type.toLowerCase(Locale.ROOT)) {
            case "physical" -> PHYSICAL;
            case "fire" -> FIRE;
            case "lightning" -> LIGHTNING;
            case "cold" -> COLD;
            case "chaos" -> CHAOS;
            case "bleeding", "bleed" -> BLEEDING;
            case "ignite" -> IGNITE;
            case "poison" -> POISON;
            default -> SKILL;
        };
    }

    /**
     * 根据技能伤害类型字符串构造对应的 {@link DamageSource}。
     * 用于让 {@code DamageAction} 真正按类型（火/电/冰/混沌）结算，而非统一物理伤害。
     */
    public static DamageSource getDamageSource(ServerLevel level, String type) {
        return new DamageSource(level.registryAccess().getOrThrow(keyOf(type)));
    }

    /**
     * 带攻击者的技能伤害源重载：{@code attacker} 同时作为 directEntity 与 causingEntity
     * （对齐 {@code EffectAppliers} 的 {@code attacker, attacker} 范式）。
     * <p>必须携带施法者，否则 {@code source.getEntity()} 恒为 null，导致
     * {@code DamageEvent}/{@code DamagePipeline} 中攻击者作用域入栈、异常衍生、暴击、
     * {@code inc/reduced damage}、命中上下文缓存、闪避/格挡判定、ON_KILL 全部走不到
     * ——整条技能伤害体系退化为死线。
     */
    public static DamageSource getDamageSource(ServerLevel level, String type, @Nullable Entity attacker) {
        return new SkillDamageSource(level.registryAccess().getOrThrow(keyOf(type)), attacker, attacker, null);
    }
}
