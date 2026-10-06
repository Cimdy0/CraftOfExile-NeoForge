package com.cimdy.coe.data.codec.item.equipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/**
 * 药剂槽位组定义（配置文件驱动，对应 {@code config/coe/slots.json} 的 flask_slots 条目）。
 *
 * <p>与 {@link AccessorySlotDef} 同构：一组 {@code count} 个槽，组内所有槽仅允许放入
 * {@code category} 对应类别的物品（如 {@code coe:life_flask} / {@code coe:mana_flask}）。
 * 类别判定统一走 {@code ItemTypeChecker} / {@code ModifiableStateOps}，不依赖物品标签或 instanceof。
 */
public record FlaskSlotDef(int count, Identifier category) {
    public static final Codec<FlaskSlotDef> CODEC = RecordCodecBuilder.create(in -> in.group(
            Codec.INT.fieldOf("count").forGetter(FlaskSlotDef::count),
            Identifier.CODEC.fieldOf("category").forGetter(FlaskSlotDef::category)
    ).apply(in, FlaskSlotDef::new));

    /** 网络流编解码（字段顺序 / 类型与 record 一致），供 general-purpose-10 的 SlotSyncPacket 序列化。 */
    public static final StreamCodec<RegistryFriendlyByteBuf, FlaskSlotDef> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, FlaskSlotDef::count,
                    Identifier.STREAM_CODEC, FlaskSlotDef::category,
                    FlaskSlotDef::new);
}
