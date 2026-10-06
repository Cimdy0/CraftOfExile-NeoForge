package com.cimdy.coe.data.codec.item.mod.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/**
 * 装备物品孔（通用插槽设施，非「符文专用」）。
 *
 * <p>POE2 实证：武器 / 护甲携带 <b>item socket（augment socket）</b>，可插入
 * <b>符文（Rune）</b>、<b>魂核（Soul Core）</b>及其它 augment；孔由工匠石（Artificer's Orb）开出，
 * 上限为「双手武器 / 胸甲 2 个，单手武器与其它护甲 1 个」。</p>
 *
 * <p>本记录刻意把「孔类别」与「孔内容」分开：孔的语义类别由 {@code type} 承载
 * （未来新增珠宝孔等只需加常量），孔内物件由 {@code content} 承载（引用已注册的 augment 数据 id）。
 * 二者解耦后，符文 / 魂核 / 未来新 augment 共用同一套孔设施，无需改动孔本身。</p>
 */
public record EquipmentSocket(String type, Optional<Identifier> content) {

    /** 孔类别：augment（符文 / 魂核 / 其它 augment 共用）。 */
    public static final String AUGMENT = "augment";

    /** 空 augment 孔（工匠石开出的初始形态）。 */
    public static final EquipmentSocket EMPTY_AUGMENT = new EquipmentSocket(AUGMENT, Optional.empty());

    public static final Codec<EquipmentSocket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("type", AUGMENT).forGetter(EquipmentSocket::type),
            Identifier.CODEC.optionalFieldOf("content").forGetter(EquipmentSocket::content)
    ).apply(instance, EquipmentSocket::new));

    public static EquipmentSocket empty(String type) {
        return new EquipmentSocket(type, Optional.empty());
    }

    public static EquipmentSocket of(String type, Identifier content) {
        return new EquipmentSocket(type, Optional.of(content));
    }

    /** 孔内是否有已插入的 augment。 */
    public boolean isEmpty() {
        return content.isEmpty();
    }

    /** 返回同类别的空孔（用于「取出」语义）。 */
    public EquipmentSocket cleared() {
        return new EquipmentSocket(type, Optional.empty());
    }
}
