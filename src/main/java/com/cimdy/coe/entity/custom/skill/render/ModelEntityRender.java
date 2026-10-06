package com.cimdy.coe.entity.custom.skill.render;

import com.cimdy.coe.data.codec.entity.render.EntityRenderDef;
import com.cimdy.coe.data.codec.entity.render.EntityRenderDefs;
import com.cimdy.coe.data.codec.entity.render.EntityRenderLayer;
import com.cimdy.coe.data.codec.entity.render.SkillSkinDef;
import com.cimdy.coe.data.codec.entity.render.SkillSkinDefs;
import com.cimdy.coe.entity.EntityDefCarrier;
import com.cimdy.coe.entity.RenderIdCarrier;
import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;

import java.util.Optional;

/**
 * 数据驱动立牌渲染器 —— 一切「外观由 {@code coe:entity_render} 决定」的载体共用。
 *
 * <p><b>这是「新实体错觉」的另一半</b>：粒子（服务端 {@code sendParticles}）只提供氛围，
 * 真正的<b>外观</b>由本渲染器按实体同步下来的 render id 取 {@code EntityRenderDef} 的模型层绘制。
 * 于是同一个实体类型（如 {@code coe:grenade}），不同技能引用不同渲染定义即可呈现完全不同的手雷。
 *
 * <p><b>泛型而非每个实体一个渲染器</b>：只要实体实现 {@link RenderIdCarrier}（同步 render id + 尺寸），
 * 就复用本渲染器 —— 新增一类载体<b>零渲染代码</b>（见 {@code SkillEntityRender} 这一个空子类）。
 *
 * <p><b>绘制方式</b>：贴图立牌（{@code cross} 时交叉两片成十字），绕 Y 轴自转。
 * 尺寸 = {@link RenderIdCarrier#getRadius()} × {@link EntityRenderLayer.ModelDef#scale()}，
 * 故「范围扩大」类词缀改半径时外观自动跟随。
 *
 * <p><b>为何用自定义几何体而非 block / item 模型</b>：见
 * {@link EntityRenderLayer.ModelDef} 的核实说明 —— MC 26.2 的模型集只接受
 * {@code BlockState} 或 {@code ItemStack}，无法按任意资源 id 取用。
 *
 * @param <T> 载体实体类型（必须同时是实体与 {@link RenderIdCarrier}）
 */
public class ModelEntityRender<T extends Entity & RenderIdCarrier>
        extends EntityRenderer<T, ModelEntityRenderState> {

    public ModelEntityRender(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ModelEntityRenderState createRenderState() {
        return new ModelEntityRenderState();
    }

    @Override
    public void extractRenderState(T entity, ModelEntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.radius = entity.getRadius();
        // 服务端只同步 render id，渲染定义在客户端按 id 取（含专用服务器端客户端的兜底加载）
        // 技能皮肤表须在客户端显式兜底加载：否则 SkillSkinDefs.MAP 为空，
        // resolveRender/get 均按“空表”处理，per-skill tint 恒回落（此调用缺失会让客户端染色死路）。
        SkillSkinDefs.ensureClientLoaded();
        EntityRenderLayer.ModelDef def = entity.getRenderId()
                // 技能皮肤：同步下来的 render 可能是「技能 id」，须先还原为底层渲染定义 id
                .map(SkillSkinDefs::resolveRender)
                .flatMap(id -> {
                    EntityRenderDefs.ensureClientLoaded();
                    return EntityRenderDefs.get(id);
                })
                .flatMap(com.cimdy.coe.data.codec.entity.render.EntityRenderDef::model)
                .orElse(null);
        state.model = def;
        state.spinDegrees = def == null ? 0.0F : (entity.tickCount + partialTick) * def.spinDegPerTick();
        // 技能皮肤染色：实体引用里的 render 即「技能 id」（装配期经 Skill#withDeliveryRender 写入），
        // 皮肤按技能 id 索引；无皮肤 / 皮肤未声明 tint 时回落渲染定义自带的 ModelDef.tint；再否则无色。
        // 技能皮肤键：SkillEntity 自带 skinKey()（服务端走 entityRead，客户端回落同步字段），
        // 其它载体（无同步字段）沿用原 EntityDefCarrier 直读逻辑。
        Optional<Identifier> skinKey = entity instanceof SkillEntity se ? se.skinKey()
                : (entity instanceof EntityDefCarrier carrier && carrier.entityRead() != null
                    ? carrier.entityRead().render() : Optional.empty());
        Optional<Integer> skinTint = skinKey.flatMap(SkillSkinDefs::get).flatMap(SkillSkinDef::tint);
        state.tint = skinTint.orElseGet(() -> def == null ? null : def.tint().orElse(null));
    }

    @Override
    public void submit(ModelEntityRenderState state, PoseStack pose, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        super.submit(state, pose, collector, camera);
        EntityRenderLayer.ModelDef def = state.model;
        if (def == null) return;

        float size = state.radius * def.scale();
        if (size <= 0.0F) return;

        pose.pushPose();
        pose.translate(0.0F, def.yOffset(), 0.0F);
        if (state.spinDegrees != 0.0F) {
            pose.mulPose(Axis.YP.rotationDegrees(state.spinDegrees));
        }
        pose.scale(size, size, size);

        RenderType type = RenderTypes.entityTranslucent(def.texture());
        int light = state.lightCoords;
        // 染色：null = 沿用贴图原色（setColor(-1)）；否则套用技能皮肤 / 渲染定义 tint（ARGB）
        int color = state.tint != null ? state.tint : -1;
        collector.submitCustomGeometry(pose, type, (p, consumer) -> emitQuad(consumer, p, light, color));
        if (def.cross()) {
            // 交叉第二片：任意水平视角都有体积感（同原版 cross 型方块模型）
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            collector.submitCustomGeometry(pose, type, (p, consumer) -> emitQuad(consumer, p, light, color));
            pose.popPose();
        }
        pose.popPose();
    }

    /** 单位立牌：宽 1（x ∈ [-0.5, 0.5]）、高 1（y ∈ [0, 1]）、位于 z = 0 平面。 */
    private static void emitQuad(VertexConsumer consumer, PoseStack.Pose pose, int light, int color) {
        float h = 0.5F;
        vertex(consumer, pose, light, color, -h, 0.0F, 0.0F, 1.0F);
        vertex(consumer, pose, light, color, h, 0.0F, 1.0F, 1.0F);
        vertex(consumer, pose, light, color, h, 1.0F, 1.0F, 0.0F);
        vertex(consumer, pose, light, color, -h, 1.0F, 0.0F, 0.0F);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int light, int color,
                               float x, float y, float u, float v) {
        consumer.addVertex(pose, x, y, 0.0F)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
