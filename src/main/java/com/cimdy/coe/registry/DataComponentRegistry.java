package com.cimdy.coe.registry;

import com.cimdy.coe.item.accessory.Buckler;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.item.state.ModifiableState;
import com.cimdy.coe.data.codec.item.state.ModifiableStateCodec;
import com.cimdy.coe.data.codec.skill.gem.SkillData;
import com.cimdy.coe.data.codec.skill.gem.SupportData;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.talent.data.JewelData;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class DataComponentRegistry {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CraftOfExile.MODID);

    public static final Supplier<DataComponentType<Integer>> LEVEL = DATA_COMPONENT_TYPES.register(
            "level", () -> DataComponentType.<Integer>builder().persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT).build());

    /**
     * 通用物品状态（{@link ModifiableState}）统一载体组件：装备 / 药剂 / 护符三类状态共用同一组件，
     * 经 {@link ModifiableStateCodec} 判别编解码（按运行时子类型分派到 {@code EquipmentState} /
     * {@code FlaskState} / {@code CharmState} / {@code GemState} 各自的 Codec）。
     * 过去三类型分处 {@code MODIFIABLE_STATE} / {@code FLASK_ITEM_STATE} / {@code CHARM_ITEM_STATE} 三组件，
     * 导致词缀 / 品质 / 稀有度设施只对装备生效；统一后通用维度真正成为通用。
     */
    public static final Supplier<DataComponentType<ModifiableState>> MODIFIABLE_STATE = DATA_COMPONENT_TYPES.register(
            "item_state", () -> DataComponentType.<ModifiableState>builder().persistent(ModifiableStateCodec.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(ModifiableStateCodec.CODEC)).build());

    /** 工作台工艺排除标签（POE2 "cannot roll X" 工艺）。 */
    public static final Supplier<DataComponentType<List<String>>> ITEM_CRAFT_TAGS = DATA_COMPONENT_TYPES.register(
            "item_craft_tags", () -> DataComponentType.<List<String>>builder()
                    .persistent(Codec.STRING.listOf())
                    .networkSynchronized(ByteBufCodecs.fromCodec(Codec.STRING.listOf())).build());

    /** 装备 Stat 条目：词缀生成时算好、穿戴时注入实体 StatStore（源前缀 "equip:"）。 */
    public static final Supplier<DataComponentType<Map<String, StatEntry>>> ITEM_STAT_ENTRIES = DATA_COMPONENT_TYPES.register(
            "item_stat_entries", () -> DataComponentType.<Map<String, StatEntry>>builder()
                    .persistent(Codec.unboundedMap(Codec.STRING, StatEntry.CODEC))
                    .networkSynchronized(ByteBufCodecs.fromCodec(Codec.unboundedMap(Codec.STRING, StatEntry.CODEC)))
                    .build());

    public static final Supplier<DataComponentType<SkillData>> SKILL_DATA = DATA_COMPONENT_TYPES.register(
            "skill_data", () -> DataComponentType.<SkillData>builder().persistent(SkillData.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(SkillData.CODEC)).build());

    public static final Supplier<DataComponentType<SupportData>> SUPPORT_DATA = DATA_COMPONENT_TYPES.register(
            "support_data", () -> DataComponentType.<SupportData>builder().persistent(SupportData.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(SupportData.CODEC)).build());

    /** 珠宝数据。 */
    public static final Supplier<DataComponentType<JewelData>> JEWEL_DATA = DATA_COMPONENT_TYPES.register(
            "jewel_data", () -> DataComponentType.<JewelData>builder().persistent(JewelData.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(JewelData.CODEC)).build());

    /**
     * 能否招架：标在 ItemStack 上，声明该物品可作招架（Parry）用。
     * 轻盾（Buckler）Java 注册时挂 {@code true}；数据生成的盾牌预设若需作为轻盾招架，
     * 也须在此组件写入 {@code true}（与 {@link #MODIFIABLE_STATE} 的 buckler 标签二选一生效，二者并存时以本组件为权威）。
     */
    public static final Supplier<DataComponentType<Boolean>> CAN_PARRY = DATA_COMPONENT_TYPES.register(
            "can_parry", () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL).build());

    /** Flask 当前充能次数（POE2 Flask 机制：右键消耗充能发动效果，充能随时间/击杀恢复）。 */
    public static final Supplier<DataComponentType<Integer>> FLASK_CHARGES = DATA_COMPONENT_TYPES.register(
            "flask_charges", () -> DataComponentType.<Integer>builder().persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT).build());

    /** Flask 上次充能恢复的游戏时刻（inventoryTick 节流）。 */
    public static final Supplier<DataComponentType<Long>> FLASK_LAST_CHARGE = DATA_COMPONENT_TYPES.register(
            "flask_last_charge", () -> DataComponentType.<Long>builder().persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG).build());

    /** Charm 当前充能次数（上限固定 80）。 */
    public static final Supplier<DataComponentType<Integer>> CHARM_CHARGES = DATA_COMPONENT_TYPES.register(
            "charm_charges", () -> DataComponentType.<Integer>builder().persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT).build());

    /**
     * Flask 每秒充能的小数进度（0~1）。
     */
    public static final Supplier<DataComponentType<Float>> FLASK_CHARGE_PROGRESS = DATA_COMPONENT_TYPES.register(
            "flask_charge_progress", () -> DataComponentType.<Float>builder().persistent(Codec.FLOAT)
                    .networkSynchronized(ByteBufCodecs.FLOAT).build());

    /** Charm 每秒充能的小数进度（0~1）。 */
    public static final Supplier<DataComponentType<Float>> CHARM_CHARGE_PROGRESS = DATA_COMPONENT_TYPES.register(
            "charm_charge_progress", () -> DataComponentType.<Float>builder().persistent(Codec.FLOAT)
                    .networkSynchronized(ByteBufCodecs.FLOAT).build());
}
