package com.cimdy.coe.data.helper.engine;

import com.cimdy.coe.data.gen.stat.wirings.wire.flow.WireWhile;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.codec.item.mod.state.StateCalculator;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.mojang.datafixers.util.Either;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Set;
import com.cimdy.coe.data.helper.item.ModifiableStateOps;

/**
 * <b>装备来源移动速度惩罚</b>（通用设施）。
 *
 * <p>PoE2 里重甲自带「-X% 移动速度」，本设施把它做成一条<b>与来源无关</b>的通用链路：
 * 任何已装备/已入槽的物品只要在 {@link EquipmentState} 上声明了 {@code movement_penalty}
 * （护甲侧 {@code ArmorBasic#movement_penalty}，正数幅度，0.03 = -3% 移速），注入时就会被物化为
 * 一条 {@code MOVEMENT_SPEED} 的 {@code ADD_MULTIPLIED_BASE} /
 * {@link ModifierStatEffect.PenaltyRole#PENALTY} 条目，落入 StatEngine 的<b>独立惩罚子池</b>
 * （与正向 multBase 分池，见 {@code StatEngine} 的 penalty 累加器），而非挤进普通加值池。
 *
 * <p>数值在<b>注入侧取负</b>——与仓库既有范式一致（「reduced 值已取负」，见
 * {@code WireWhile#penaltyMovementSpeed}）：数据里存正数幅度，写进 Stat 的是负数。
 *
 * <p>「忽略惩罚」由 {@link com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect} 门控，
 * 消费点就在本设施的 {@link #entries}：佩戴者持有该标记时整条不产出。
 * 门控<b>不区分来源物品</b>（不写 {@code is(ItemTags.X)}），故未来新增其它「装备来源移速惩罚」
 * （盾牌 / 重靴 / 词缀派生）自动被同一标记覆盖，零 Java 改动。
 */
public final class EquippedMovementPenalty {

    private EquippedMovementPenalty() {
    }

    /**
     * 惩罚条目的键后缀：完整键 = {@code sourcePrefix + "#" + KEY_SUFFIX}。
     * 与 {@code StatStore} 的 {@code #} 分段寻址一致，脱下 / 脱槽时随前缀整体移除。
     */
    public static final String KEY_SUFFIX = "equipped_movement_penalty";

    /** 佩戴者是否忽略<b>全部</b>装备来源移动速度惩罚（{@link com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect} 真读）。 */
    public static boolean ignoredBy(LivingEntity living) {
        return living != null
                && StatEngine.gateOf(living,
                "ignore_equipped_movement_penalty", Set.of());
    }

    /** 该物品声明的移速惩罚幅度（正数；0 = 无惩罚）。通用读取：只看 EquipmentState，不关心物品种类。 */
    public static double penaltyOf(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0.0;
        return StateCalculator.getMovementPenalty(ModifiableStateOps.equipment(stack));
    }

    /**
     * 产出本物品的「装备来源移动速度惩罚」条目（至多一条，键 = {@code sourcePrefix + "#" + KEY_SUFFIX}）。
     *
     * @param living       佩戴者（用于门控读取；null 视为不忽略）
     * @param stack        被注入的物品
     * @param sourcePrefix 来源前缀（{@code "equip:<slot>:<uuid>"} / {@code "accessory:<idx>"}）
     * @return 无惩罚或被忽略时返回空 Map（调用方 {@code putAll} 即可，无需判空）
     */
    public static Map<String, StatEntry> entries(LivingEntity living, ItemStack stack, String sourcePrefix) {
        if (sourcePrefix == null || sourcePrefix.isEmpty()) return Map.of();
        if (ignoredBy(living)) return Map.of();
        double magnitude = penaltyOf(stack);
        if (Math.abs(magnitude) < 1e-9) return Map.of();
        // 取负：数据层存正数幅度，Stat 侧是「reduced」（仓库既有范式）。
        StatEntry entry = StatEntry.of(ExpressionCodec.of(-magnitude), new ModifierStatEffect(
                Attributes.MOVEMENT_SPEED,
                Either.left(AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                ModifierStatEffect.Mode.GAIN,
                ModifierStatEffect.PenaltyRole.PENALTY,
                GateExpr.ALWAYS));
        return Map.of(sourcePrefix + "#" + KEY_SUFFIX, entry);
    }
}
