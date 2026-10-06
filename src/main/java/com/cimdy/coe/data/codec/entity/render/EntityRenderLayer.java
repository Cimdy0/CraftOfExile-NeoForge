package com.cimdy.coe.data.codec.entity.render;

import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.mojang.serialization.Codec;

import java.util.Map;
import java.util.Optional;

/**
 * 道具渲染层 —— 一个实体外观的<b>一层</b>表现（一圈粒子 / 一道光柱 / 一个模型 …）。
 *
 * <p>与 {@code Action} / {@code Delivery} 同构：注册式多态（{@code EntityRenderTypes}），
 * 多层自由组合成 {@link EntityRenderDef}，使「一类实体多类渲染」成立——
 * 换外观 = 换一份 {@code EntityRenderDef} 数据，零 Java 改动。
 *
 * <h2>服务端 / 客户端分工</h2>
 * <ul>
 *   <li><b>粒子层</b>在<b>服务端</b>发射（{@link #emit}）：沿用既有 {@code SkillEntity#emitRender}
 *       的 {@code level.sendParticles} 范式，客户端无需额外同步即可显示。</li>
 *   <li><b>模型层</b>在<b>客户端</b>渲染（{@link #model()}）：实体同步 {@code prop_render} id，
 *       客户端渲染器按本层定义取模型。模型层在服务端 {@link #emit} 为空实现。</li>
 * </ul>
 *
 * <h2>尺寸约定</h2>
 * 各层的半径/高度一律写成<b>实体半径的相对倍率</b>（{@code 1.0 = 实体触发半径}），
 * 而非绝对方块数——这样同一份渲染定义可用于任意半径的残片/危害物，
 * 且「范围扩大」类词缀改半径时外观自动跟随。
 */
public interface EntityRenderLayer {

    Codec<Map<EntityRenderLayer.Type<?>, EntityRenderLayer>> MAP_CODEC = Codec.dispatchedMap(
            EntityRenderTypes.REGISTRY.byNameCodec(), EntityRenderLayer.Type::codec);

    Codec<EntityRenderLayer> CODEC = MAP_CODEC.xmap(
            map -> map.values().iterator().next(),
            layer -> Map.of(layer.getType(), layer));

    EntityRenderLayer.Type<?> getType();

    /**
     * 服务端每 {@link #interval()} tick 调用一次：在本层定义的位置发射粒子。
     * 模型层返回空实现（其表现由客户端 {@link #model()} 承担）。
     *
     * @param radius 实体当前触发半径（方块），各层按其倍率缩放。
     */
    void emit(EmitContext context, double radius);

    /** 发射间隔（tick）。模型层无意义，返回固定值即可。 */
    int interval();

    /** 客户端模型层定义；非模型层返回空。 */
    default Optional<ModelDef> model() {
        return Optional.empty();
    }

    /**
     * 服务端发射上下文：一次性打包位置、世界与随机源，避免逐层重复取用。
     */
    record EmitContext(net.minecraft.server.level.ServerLevel level,
                       double x, double y, double z,
                       net.minecraft.util.RandomSource random) {
    }

    /**
     * 客户端模型层定义 —— 世界中的<b>立牌（可选十字）</b>：贴图四边形，绕 Y 轴自转。
     *
     * <p><b>为何不是 block / item 模型</b>（2026-09-09 javap 核实，避免凭猜测发明）：
     * <ul>
     *   <li>{@code ModelManager#getBlockModelSet()} → {@code BlockModelSet#get(BlockState)}
     *       与 {@code BlockStateModelSet#get(BlockState)} <b>只接受 BlockState</b>，无法按任意模型 id 取用；</li>
     *   <li>{@code ModelManager#getItemModel(Identifier)} 虽按 id 取，但其 {@code ItemModel#update}
     *       强依赖 {@code ItemStack} + {@code ItemModelResolver} + {@code ItemOwner}，与「实体外观」无关。</li>
     * </ul>
     * 因此模型层走 {@code SubmitNodeCollector#submitCustomGeometry}（本仓 {@code SkillEntityRender}
     * 已验证的提交通道）绘制自定义几何体，换取「任意技能可自定义外观」的完全数据驱动。
     *
     * @param texture 贴图资源 id（{@code <ns>:textures/...}，不含 {@code .png}）
     * @param scale 立牌边长相对<b>实体触发半径</b>的倍率（{@code 1.0 = 与触发半径同宽}）
     * @param yOffset 立牌底边离地高度（方块）
     * @param spinDegPerTick 每 tick 绕 Y 轴自转角（度），0 = 不自转
     * @param cross 是否交叉两片（{@code true} = 十字，任意角度看都有体积感）
     */
    record ModelDef(net.minecraft.resources.Identifier texture,
                    float scale, float yOffset, float spinDegPerTick, boolean cross,
                    java.util.Optional<Integer> tint) {
        /** 便捷构造（5 参）：省略 {@code tint} → 空（即无染色覆盖）。保留既有调用点（EntityProvider）零改动。 */
        public ModelDef(net.minecraft.resources.Identifier texture,
                        float scale, float yOffset, float spinDegPerTick, boolean cross) {
            this(texture, scale, yOffset, spinDegPerTick, cross, java.util.Optional.empty());
        }

        public static final Codec<ModelDef> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.create(i -> i.group(
                net.minecraft.resources.Identifier.CODEC.fieldOf("texture").forGetter(ModelDef::texture),
                Codec.FLOAT.optionalFieldOf("scale", 1.0F).forGetter(ModelDef::scale),
                Codec.FLOAT.optionalFieldOf("y_offset", 0.0F).forGetter(ModelDef::yOffset),
                Codec.FLOAT.optionalFieldOf("spin_deg_per_tick", 0.0F).forGetter(ModelDef::spinDegPerTick),
                Codec.BOOL.optionalFieldOf("cross", true).forGetter(ModelDef::cross),
                com.mojang.serialization.Codec.INT.optionalFieldOf("tint").forGetter(ModelDef::tint)
        ).apply(i, ModelDef::new));
    }

    record Type<T extends EntityRenderLayer>(Codec<T> codec) {
    }
}
