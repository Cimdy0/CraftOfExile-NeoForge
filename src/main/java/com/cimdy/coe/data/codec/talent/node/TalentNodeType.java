package com.cimdy.coe.data.codec.talent.node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

/**
 * 天赋节点类型：数据驱动。每种类型自带显示名、半径、颜色与是否为基石，
 * 因此新增 / 自定义节点类型无需改动界面或主题，仅需在数据中注册即可（见 {@link TalentNodeTypeRegistry}）。
 *
 * <p>本 Codec 用于「节点类型完整定义」的序列化（{@code data/coe/coe/talent/player_talent/type/<id>.json}）；
 * 天赋树 JSON 中则以字符串 id 引用（见 {@link TalentNodeTypeRegistry#REFERENCE_CODEC}）。
 */
public record TalentNodeType(
        String id,
        Component displayName,
        int radius,
        int color,
        boolean keystone,
        boolean socket
) {
    /** 节点类型「完整定义」编解码（用于 coe/talent/player_talent/type/<id>.json）。 */
    public static final Codec<TalentNodeType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(TalentNodeType::id),
            ComponentSerialization.CODEC.fieldOf("display_name").forGetter(TalentNodeType::displayName),
            Codec.INT.fieldOf("radius").forGetter(TalentNodeType::radius),
            Codec.INT.fieldOf("color").forGetter(TalentNodeType::color),
            Codec.BOOL.optionalFieldOf("keystone", false).forGetter(TalentNodeType::keystone),
            Codec.BOOL.optionalFieldOf("socket", false).forGetter(TalentNodeType::socket)
    ).apply(instance, TalentNodeType::new));
}
