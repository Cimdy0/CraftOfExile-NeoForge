package com.cimdy.coe.data.codec.stronghold.entity;

import com.cimdy.coe.data.codec.stronghold.task.helper.dispatch.TaskDispatcher;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.addition.AdditionData;
import com.cimdy.coe.data.codec.support.addition.AdditionKeys;
import com.cimdy.coe.helper.MathHelper;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 一条已捕获实体的存档记录（玩家附件 {@link com.cimdy.coe.registry.world.AttachmentRegistry#PLAYER_ENTITY} 的列表元素）。
 *
 * <p><b>盟友标记</b>：{@code ally} 表示该实体被玩家指定为「盟友」（帕鲁式随行伙伴）。
 * 用 {@link Optional} 包装以兼容旧存档（缺字段 = 非盟友），读取一律走 {@link #isAlly()}。
 * 盟友不参与据点工作，见 {@code TaskDispatcher#canWork}。
 */
public record SaveEntity(CustomData data, Map<String, Double> displayData, Optional<Boolean> ally, Optional<Boolean> combatant) {
    public static final Codec<SaveEntity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CustomData.CODEC.fieldOf("data").forGetter(SaveEntity::data),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).fieldOf("display")
                    .forGetter(SaveEntity::displayData),
            Codec.BOOL.optionalFieldOf("ally").forGetter(SaveEntity::ally),
            Codec.BOOL.optionalFieldOf("combatant").forGetter(SaveEntity::combatant)
    ).apply(instance, SaveEntity::new));

    public SaveEntity(CustomData data, Map<String, Double> displayData) {
        this(data, displayData, Optional.empty(), Optional.empty());
    }

    /** 是否为盟友（旧存档缺字段时为 false）。 */
    public boolean isAlly() {
        return ally.orElse(Boolean.FALSE);
    }

    public SaveEntity withAlly(boolean value) {
        return new SaveEntity(data, displayData, Optional.of(value), combatant);
    }

    /** 是否参与战斗（帕鲁式「工作 ↔ 战斗」开关，默认 true：据点遭入侵须防御）。 */
    public boolean isCombatant() {
        return combatant.orElse(Boolean.TRUE);
    }

    public SaveEntity withCombatant(boolean value) {
        return new SaveEntity(data, displayData, ally, Optional.of(value));
    }

    /** 存档 NBT 内记录的实体 UUID（不创建实体，成本远低于 {@link #getEntityFromData}）。 */
    public Optional<UUID> uuid() {
        return uuidOf(data);
    }

    public static Optional<UUID> uuidOf(CustomData data) {
        // Entity#save 以 UUIDUtil.CODEC 写入 "UUID"，即 4 元素 int 数组（IntArrayTag）
        return data.copyTag().getIntArray("UUID")
                .filter(a -> a.length == 4)
                .map(UUIDUtil::uuidFromIntArray);
    }

    public LivingEntity getEntityFromData(Level level) {
        if (StrongholdHelper.createEntityFromData(level, data) instanceof LivingEntity living) {
            living.setData(AttachmentRegistry.LEVEL, (int) this.getValue("Level", 1));
            living.setHealth((float) this.getValue("Health", 0));
            return living;
        }
        return null;
    }

    /** 收编/回收一个实体：盟友 / 战斗开关均从实体附件继承，避免回收后丢失。 */
    public static SaveEntity of(LivingEntity living) {
        return new SaveEntity(
                StrongholdHelper.entityToCustomData(living),
                putEntityDisplayData(living),
                // 原 AttachmentRegistry.ALLY / COMBATANT 已迁入 AdditionData（key=ally / combatant，combatant 默认 true）
                Optional.of(AdditionData.getOrDefault(living, AdditionKeys.ALLY, 0.0) != 0.0),
                Optional.of(AdditionData.getOrDefault(living, AdditionKeys.COMBATANT, 1.0) != 0.0));
    }

    public static Map<String, Double> putEntityDisplayData(LivingEntity living) {
        Map<String, Double> data = new HashMap<>();
        data.put("Level", Double.valueOf(living.getData(AttachmentRegistry.LEVEL)));
        data.put("Health", (double) living.getHealth());
        data.put("MaxHealth", (double) living.getMaxHealth());
        data.put("San", StrongholdHelper.getSan(living));
        return data;
    }

    public List<Component> getEntityDisplayData(Level level) {
        List<Component> list = new ArrayList<>();
        Entity entity = StrongholdHelper.createEntityFromData(level, data);
        if (entity == null) return list;
        list.add(createLevelComponent(getName(entity), getValue("Level", 1)));
        list.add(createHealthComponent(getValue("Health", 0), getValue("MaxHealth", 0)));
        list.add(createSanComponent(getValue("San", 0)));
        if (isAlly()) list.add(allyComponent());
        if (!isCombatant()) list.add(combatantComponent());
        return list;
    }

    public static List<Component> getEntityInfo(Entity entity) {
        List<Component> list = new ArrayList<>();
        if (entity instanceof LivingEntity living) {
            list.add(createLevelComponent(getName(living), living.getData(AttachmentRegistry.LEVEL)));
            list.add(createHealthComponent(living.getHealth(), living.getMaxHealth()));
            list.add(createSanComponent(StrongholdHelper.getSan(living)));
            // 原 AttachmentRegistry.ALLY / COMBATANT 已迁入 AdditionData（key=ally / combatant，combatant 默认 true）
            if (AdditionData.getOrDefault(living, AdditionKeys.ALLY, 0.0) != 0.0) list.add(allyComponent());
            if (AdditionData.getOrDefault(living, AdditionKeys.COMBATANT, 1.0) == 0.0)
                list.add(combatantComponent());
        }
        return list;
    }

    /** 盟友角标：格子左上角的小字。 */
    public static Component allyMarkComponent() {
        return Component.translatable("hold.terminal.coe.ally");
    }

    private static Component allyComponent() {
        return Component.translatable("hold.terminal.coe.ally").append(":")
                .append(Component.translatable("hold.terminal.coe.ally_no_work"));
    }

    /** 战斗开关角标：设为「专注工作」时显示。 */
    private static Component combatantComponent() {
        return Component.translatable("hold.terminal.coe.combatant").append(":")
                .append(Component.translatable("hold.terminal.coe.combatant_work_only"));
    }

    private static Component createLevelComponent(MutableComponent name, double level) {
        return name.append("  " + "Lv: ").append(MathHelper.df2.format(level));
    }

    private static Component createHealthComponent(double health, double maxHealth) {
        return Component.translatable("Health").append(": ")
                .append(Component.literal(MathHelper.df2.format(health)))
                .append(Component.literal("/")
                        .append(Component.literal(MathHelper.df2.format(maxHealth))));
    }

    private static Component createSanComponent(double san) {
        return Component.translatable("San").append(": ")
                .append(Component.literal(MathHelper.df2.format(san)))
                .append(Component.literal("/")
                        .append(Component.literal("100")));
    }

    public static MutableComponent getName(Entity entity) {
        return entity.getCustomName() == null ?
                Component.empty().append(entity.getName())
                : Component.empty().append(entity.getCustomName())
                .append("(").append(entity.getType().getDescription()).append(")");
    }

    public SaveEntity addValue(String key, double value) {
        Map<String, Double> map = new HashMap<>(displayData);
        map.put(key, value);
        return new SaveEntity(data, map, ally, combatant);
    }

    public double getValue(String key, double defaultValue) {
        return this.displayData.getOrDefault(key, defaultValue);
    }
}