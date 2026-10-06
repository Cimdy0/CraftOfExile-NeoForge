package com.cimdy.coe.data.codec.entity.render;

import com.cimdy.coe.data.codec.entity.EntityDef;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * 道具渲染定义 —— 一个实体外观的<b>完整描述</b>（多层叠加）。
 *
 * <p>这是「<b>一类实体多类渲染</b>」的落点：实体的<b>行为契约</b>由 {@code EntityDef} 定义，
 * <b>外观</b>则由本类独立定义并被 {@code EntityDef} 按 id 引用。于是同族实体
 * （如各类残片）共用同一份行为定义、各自引用不同渲染定义即可获得不同外观：
 * <pre>{@code
 *   coe:prop/remnant          行为：施放者走近拾取 → 回调 ON_HIT（全部残片一致）
 *     ├─ mana_remnant   → coe:prop_render/arcane_blue   （蓝色 GLOW 环 + 法力光点）
 *     ├─ blood_remnant  → coe:prop_render/blood_red     （红色烟柱 + 血滴）
 *     └─ vaal_remnant   → coe:prop_render/vaal_purple   （紫色球壳 + 余烬）
 * }</pre>
 *
 * <p>新增一种外观 = 加一份 {@code prop_render} 数据，<b>零 Java 改动</b>；
 * 新增一种实体 = 加一份 {@code prop} 数据（可复用既有 {@code prop_render}）。
 *
 * @param layers 渲染层，按数组顺序叠加（模型层建议置于末尾，以免被粒子遮蔽）。
 */
public record EntityRenderDef(List<EntityRenderLayer> layers) {

    public static final Codec<EntityRenderDef> CODEC = RecordCodecBuilder.create(i -> i.group(
            EntityRenderLayer.CODEC.listOf().optionalFieldOf("layers", List.of())
                    .forGetter(EntityRenderDef::layers)
    ).apply(i, EntityRenderDef::new));

    /**
     * 服务端按各层自身的 {@link EntityRenderLayer#interval()} 节流发射粒子。
     * 模型层在服务端为空实现（其表现由客户端渲染器读取）。
     *
     * @param tick 实体存活 tick 数，用于各层独立节流（同一 tick 内各层互不影响）。
     */
    public void emit(EntityRenderLayer.EmitContext context, double radius, int tick) {
        for (EntityRenderLayer layer : layers) {
            int interval = layer.interval();
            if (interval <= 0 || interval == Integer.MAX_VALUE) continue;
            if (tick % interval == 0) layer.emit(context, radius);
        }
    }

    /** 客户端模型层（取首个；多层模型暂无需求）。 */
    public java.util.Optional<EntityRenderLayer.ModelDef> model() {
        return layers.stream().map(EntityRenderLayer::model)
                .flatMap(java.util.Optional::stream).findFirst();
    }
}
