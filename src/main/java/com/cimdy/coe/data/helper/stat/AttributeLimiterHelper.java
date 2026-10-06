package com.cimdy.coe.data.helper.stat;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.support.attribute.AttributeLimiters;
import com.cimdy.coe.data.codec.support.attribute.EntityAttributeLimiter;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.helper.mod.ItemStatContributor;
import com.cimdy.coe.data.helper.mod.ModContributionScope;
import com.cimdy.coe.data.helper.engine.EquippedMovementPenalty;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttributeLimiterHelper {

    /**
     * 装备穿戴：把词缀生成时算好的装备 Stat 条目注入实体 StatStore。
     * 条目键已带 {@code "equip:" + slot + ":" + itemUuid} 前缀（由 AttributeApplicator 写入时生成），
     * 源前缀语义与 StatStore 的 {@code #} 分段寻址一致，脱下时按前缀整体移除。
     */
    public static void applyItemLimiter(
            LivingEntity living, ItemStack stack, EquipmentSlot slot) {
        if (!stack.has(DataComponentRegistry.ITEM_STAT_ENTRIES)) return;
        // 来源前缀从 ITEM_STAT_ENTRIES 既有键推导（equip:<slot>:<uuid>），与 StatStore '#' 分段寻址一致。
        String prefix = equipPrefixOf(stack, slot);
        if (prefix == null) return;
        // 装备来源移动速度惩罚（通用设施，物化点）：凡声明 movement_penalty 的已装备物品在此产出一条
        // PENALTY 角色 MOVEMENT_SPEED 条目（值在设施内取负）；门控 MarkerStatEffect 命中则整条不产出，
        // 故「忽略全部装备来源移速惩罚」覆盖未来任何新来源，无需枚举物品种类。键沿用同一来源前缀 ⇒ 脱下随前缀移除。
        // 统一经 ItemStatContributor 注入：重键 ITEM_STAT_ENTRIES + 合并额外条目（移速惩罚），持久生效。
        ItemStatContributor.contribute(living, stack,
                ModContributionScope.persistent(prefix, EquippedMovementPenalty.entries(living, stack, prefix)));
    }

    /**
     * 装备脱下：按装备来源前缀移除对应 Stat 条目。
     * 前缀取组件首条目键的 {@code '#'} 前部分（{@code "equip:" + slot + ":" + itemUuid}，
     * 由 AttributeApplicator 写入时固化）；组件为空则按 slot+空 uuid 兜底。
     */
    public static void removeItemLimiter(
            LivingEntity living, ItemStack stack, EquipmentSlot slot) {
        if (!living.hasData(AttachmentRegistry.STAT_STORE)) return;
        String prefix = equipPrefixOf(stack, slot);
        if (prefix == null) return;
        // 统一经 ItemStatContributor 按前缀移除（与计时自动过期互补，避免事件漏触发残留）。
        ItemStatContributor.revoke(living, prefix);
    }

    /** 从装备组件提取来源前缀（首条目键 '#' 前部分）；无组件返回 null。包级可见：装备技能注入复用同一前缀。 */
   public static String equipPrefixOf(ItemStack stack, EquipmentSlot slot) {
        if (!stack.has(DataComponentRegistry.ITEM_STAT_ENTRIES)) return null;
        Map<String, StatEntry> entries = stack.getOrDefault(DataComponentRegistry.ITEM_STAT_ENTRIES, Map.of());
        for (String key : entries.keySet()) {
            int i = key.indexOf('#');
            return i >= 0 ? key.substring(0, i) : key;
        }
        return "equip:" + slot + ":" + stack.getComponentsPatch().hashCode();
    }

    public static void removeLimiter(
            LivingEntity living, Holder<Attribute> attribute, Identifier id) {
        setLimiter(living, getLimiters(living).removeModifier(attribute, id));
    }

    public static void addTransientLimiter(
            LivingEntity living, Holder<Attribute> attribute, AttributeLimiters.Modifier modifiers) {
        setLimiter(living, getLimiters(living).addTransientModifier(attribute, modifiers));
    }

    public static void addPersistentLimiter(
            LivingEntity living, Holder<Attribute> attribute, AttributeLimiters.Modifier modifiers) {
        setLimiter(living, getLimiters(living).addPersistentModifier(attribute, modifiers));
    }

    public static void addTransientLimiter(
            LivingEntity living, Holder<Attribute> attribute, List<AttributeLimiters.Modifier> modifiers) {
        AttributeLimiters limiters = getLimiters(living);
        modifiers.forEach(mod -> limiters.addTransientModifier(attribute, mod));
        setLimiter(living, limiters);
    }

    public static void addPersistentLimiter(
            LivingEntity living, Holder<Attribute> attribute, List<AttributeLimiters.Modifier> modifiers) {
        AttributeLimiters limiters = getLimiters(living);
        modifiers.forEach(mod -> limiters.addPersistentModifier(attribute, mod));
        setLimiter(living, limiters);
    }

    private static void setLimiter(LivingEntity living, AttributeLimiters limiters) {
        living.setData(AttachmentRegistry.ATTRIBUTE_LIMITERS, limiters);
    }

    private static AttributeLimiters getLimiters(LivingEntity living) {
        return living.getData(AttachmentRegistry.ATTRIBUTE_LIMITERS);
    }

    // ==================== 带持续时间的 EntityAttributeLimiter.Modifier ====================

    /** 附加带持续时间的限制器（duration < 0 为永久）。 */
    public static void applyDurationLimiter(LivingEntity living, Holder<Attribute> attribute, int duration,
                                            AttributeLimiters.Modifier modifier) {
        addPersistentLimiter(living, attribute, modifier);
        EntityAttributeLimiter.Modifiers states = living.getData(AttachmentRegistry.ENTITY_LIMITER_MODIFIER);
        List<EntityAttributeLimiter.Modifier> newStates = new ArrayList<>(states.states());
        newStates.add(new EntityAttributeLimiter.Modifier(attribute, duration, modifier));
        living.setData(AttachmentRegistry.ENTITY_LIMITER_MODIFIER, new EntityAttributeLimiter.Modifiers(newStates));
    }

    /** 每 tick 推进持续时间，到期移除限制器。 */
    public static EntityAttributeLimiter.Modifiers tickDurationLimiters(LivingEntity living, int tickDelta) {
        EntityAttributeLimiter.Modifiers states = living.getData(AttachmentRegistry.ENTITY_LIMITER_MODIFIER);
        List<EntityAttributeLimiter.Modifier> newStates = new ArrayList<>();
        for (EntityAttributeLimiter.Modifier state : states.states()) {
            if (state.duration() > tickDelta || state.duration() < 0) {
                newStates.add(new EntityAttributeLimiter.Modifier(
                        state.attribute(),
                        state.duration() < 0 ? state.duration() : state.duration() - tickDelta,
                        state.modifier()));
            } else {
                removeLimiter(living, state.attribute(), state.modifier().id());
            }
        }
        return new EntityAttributeLimiter.Modifiers(newStates);
    }
}
