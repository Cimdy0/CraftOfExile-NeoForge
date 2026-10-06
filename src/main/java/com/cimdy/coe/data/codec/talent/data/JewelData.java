package com.cimdy.coe.data.codec.talent.data;

import com.cimdy.coe.data.codec.talent.effect.TalentEffect;
import com.cimdy.coe.data.codec.talent.effect.TalentEffectApplier;
import com.cimdy.coe.data.codec.talent.effect.TalentEffectType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.util.List;

/**
 * 珠宝（Jewel）数据：作为物品数据组件存储，描述该珠宝插入天赋树珠宝插槽后授予的效果与机制。
 *
 * <p>效果复用天赋的 {@link TalentEffect} 体系（属性修正 / 命名能力），因此珠宝与天赋节点共享
 * 同一套施加管线（见 {@link TalentEffectApplier}）：珠宝被插入某个已点亮的插槽节点后，其效果
 * 以「该节点的额外效果」形式重建施加，取出即移除，无需任何特判逻辑。
 *
 * <p><b>珠宝类型（仿 PoE2，详见 poe2db.tw/cn/Jewels）</b>：
 * <ul>
 *   <li><b>基础珠宝</b>：仅通过 {@code effects} 提供列出的属性；{@code radius=0}，不影响其它节点。</li>
 *   <li><b>辐射珠宝</b>：{@code radius>0} 时，插入插槽后会在以插槽为中心的 {@code radius}
 *       世界单位圆形范围内「辐射」——范围内的节点即便与该插槽之间没有任何图连接，也可被点亮
 *       （仿 PoE 珠宝「辐射范围内节点」的语义），且这些被辐射点亮的节点在界面上以专属描边区分。</li>
 *   <li><b>其它复杂珠宝</b>（如永恒珠宝 / 凶邪珠宝 / 配置恶劣插槽等）：当前未实现，
 *       {@code behaviors} 预留行为标签位，后续可扩展而无需改动 Codec。</li>
 * </ul>
 */
public record JewelData(Component name, List<TalentEffect> effects, float radius, List<String> behaviors) {
    public static final Codec<JewelData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.optionalFieldOf("name", Component.translatable("item.coe.jewel"))
                    .forGetter(JewelData::name),
            TalentEffectType.CODEC.listOf().optionalFieldOf("effects", List.of())
                    .forGetter(JewelData::effects),
            Codec.FLOAT.optionalFieldOf("radius", 0f)
                    .forGetter(JewelData::radius),
            Codec.STRING.listOf().optionalFieldOf("behaviors", List.of())
                    .forGetter(JewelData::behaviors)
    ).apply(instance, JewelData::new));

    public static final JewelData EMPTY = new JewelData(Component.empty(), List.of(), 0f, List.of());

    /** 是否为「辐射珠宝」：radius>0 时，插入插槽会辐射半径内节点（仿 PoE 珠宝辐射语义）。 */
    public boolean radiates() {
        return radius > 0f;
    }

}
