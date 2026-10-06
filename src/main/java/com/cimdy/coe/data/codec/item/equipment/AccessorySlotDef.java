package com.cimdy.coe.data.codec.item.equipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/**
 * 饰品 / 药剂 / 护符 槽位组定义（服务端配置驱动）。
 *
 * <p>每个条目 = 一组槽：{@code count} 个槽，组内所有槽只允许放入
 * {@code category} 所对应的类别物品——即物品 {@link com.cimdy.coe.data.codec.item.state.ModifiableState#types()}
 * 类型链命中该类别路径（如 {@code coe:accessory/amulet} / {@code coe:armor/legs} /
 * {@code coe:weapon/off_hand/quiver} / {@code coe:life_flask} / {@code coe:mana_flask} /
 * {@code coe:accessory/charm}）。类别判定统一走 {@code ItemTypeChecker} / {@code ModifiableStateOps}，
 * 不再依赖物品标签或 instanceof。
 *
 * <p>数据源为服务端配置文件 {@code config/coe/slots.json}（由
 * {@link com.cimdy.coe.event.data.SlotConfigLoader} 在服务端 reload 时加载）；
 * 文件缺失或解析失败时由各 SlotRegistry 走内置兜底
 * （饰品 amulet / legs / quiver，药剂 life_flask / mana_flask），可被用户配置覆盖。
 */
public record AccessorySlotDef(int count, Identifier category) {
    public static final Codec<AccessorySlotDef> CODEC = RecordCodecBuilder.create(in -> in.group(
            Codec.INT.fieldOf("count").forGetter(AccessorySlotDef::count),
            Identifier.CODEC.fieldOf("category").forGetter(AccessorySlotDef::category)
    ).apply(in, AccessorySlotDef::new));

    /** 网络流编解码（字段顺序 / 类型与 record 一致），供 general-purpose-10 的 SlotSyncPacket 序列化。 */
    public static final StreamCodec<RegistryFriendlyByteBuf, AccessorySlotDef> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, AccessorySlotDef::count,
                    Identifier.STREAM_CODEC, AccessorySlotDef::category,
                    AccessorySlotDef::new);
}
