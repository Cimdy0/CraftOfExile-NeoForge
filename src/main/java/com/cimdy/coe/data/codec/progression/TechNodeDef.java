package com.cimdy.coe.data.codec.progression;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

/**
 * 科技树节点（按钮式，非网状非树状）。
 *
 * <p>科技树按玩家等级（1-100）分段展示为按钮网格：{@code min_level} 未到则灰显，
 * 到级且科技点足够则可点击消耗 {@code cost} 点解锁。UI 由 {@code sort_order} 决定同段内次序。
 * 注意：科技树<b>没有</b>前置 DAG（与网状天赋树、树状研究树都不同），解锁门槛就是玩家等级。
 */
public record TechNodeDef(Identifier id, String name, int minLevel, int cost, TechEffect effect, int sortOrder, String icon) {

    public static final Codec<TechNodeDef> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("name", "").forGetter(TechNodeDef::name),
            Codec.INT.optionalFieldOf("min_level", 1).forGetter(TechNodeDef::minLevel),
            Codec.INT.optionalFieldOf("cost", 1).forGetter(TechNodeDef::cost),
            TechEffect.CODEC.optionalFieldOf("effect", TechEffect.EMPTY).forGetter(TechNodeDef::effect),
            Codec.INT.optionalFieldOf("sort_order", 0).forGetter(TechNodeDef::sortOrder),
            Codec.STRING.optionalFieldOf("icon", "").forGetter(TechNodeDef::icon)
    ).apply(instance, (name, minLevel, cost, effect, sortOrder, icon) ->
            new TechNodeDef(null, name, minLevel, cost, effect, sortOrder, icon)));

    /**
     * 网络编解码：与 {@link #CODEC} 的唯一差别是<b>显式携带 id</b>。
     *
     * <p>数据包 JSON 不写 id（由文件名注入），但同步到客户端时没有文件名可依，
     * 必须把 id 一并发过去，否则客户端界面无法把节点与玩家已解锁列表对上。
     */
    public static final Codec<TechNodeDef> NETWORK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(TechNodeDef::id),
            Codec.STRING.optionalFieldOf("name", "").forGetter(TechNodeDef::name),
            Codec.INT.optionalFieldOf("min_level", 1).forGetter(TechNodeDef::minLevel),
            Codec.INT.optionalFieldOf("cost", 1).forGetter(TechNodeDef::cost),
            TechEffect.CODEC.optionalFieldOf("effect", TechEffect.EMPTY).forGetter(TechNodeDef::effect),
            Codec.INT.optionalFieldOf("sort_order", 0).forGetter(TechNodeDef::sortOrder),
            Codec.STRING.optionalFieldOf("icon", "").forGetter(TechNodeDef::icon)
    ).apply(instance, TechNodeDef::new));

    /** 载入时由文件名注入 id（JSON 不含 id 字段）。 */
    public TechNodeDef withId(Identifier id) {
        return new TechNodeDef(id, name, minLevel, cost, effect, sortOrder, icon);
    }

}
