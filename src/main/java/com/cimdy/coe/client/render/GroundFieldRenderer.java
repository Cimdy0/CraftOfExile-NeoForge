package com.cimdy.coe.client.render;

import com.cimdy.coe.client.render.HighlightRenderer;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.support.status.registry.ground.GroundFieldData;
import com.cimdy.coe.event.net.NetEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 地面场客户端渲染（简单渲染）：维护服务端同步来的地面场镜像，每 tick 撒地表粒子 +
 * 在 {@code SubmitCustomGeometryEvent} 中绘制贴地圆盘网格（复用 {@link HighlightRenderer} 的 LINES 管线）。
 *
 * <p>仅客户端加载（{@code value = Dist.CLIENT}）：所有客户端类引用都只在方法体内，服务端不会加载本类，
 * 杜绝专用服务器上加载 {@code net.neoforged.neoforge.client.event} 等客户端类。
 * 数据由 {@code NetEvent} 的 {@code GroundFieldSync} 处理器经 {@link #onSync} 注入。
 *
 * <p>渲染色直接取自 {@link GroundFieldData#color()}（服务端创建时按地面类型给定），
 * 不再依赖固定的地面 id 分支。
 *
 * <p>归属：本类是<b>客户端渲染机制</b>（粒子 + 贴地网格），不是事件派发层——按分层纪律
 * （事件类只做派发，机制归 {@code data/helper/**} 或 client/render 包）从 {@code event} 包迁出。
 */
@EventBusSubscriber(modid = CraftOfExile.MODID, value = Dist.CLIENT)
public final class GroundFieldRenderer {

    /** 客户端地面场镜像（id → 数据），由服务端同步整表替换。 */
    private static final Map<UUID, GroundFieldData> FIELDS = new LinkedHashMap<>();

    private GroundFieldRenderer() {}

    /** 由 NetEvent 客户端 payload 处理器调用（仅客户端执行）。 */
    public static void onSync(List<GroundFieldData> fields) {
        FIELDS.clear();
        for (GroundFieldData f : fields) FIELDS.put(f.id(), f);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        // 环形布点，每 tick 撒地表粒子。
        for (GroundFieldData f : FIELDS.values()) {
            spawnParticles(mc.level, f);
        }
    }

    @SubscribeEvent
    public static void onSubmitCustomGeometry(SubmitCustomGeometryEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || FIELDS.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 cam = mc.gameRenderer.mainCamera().position();
        SubmitNodeCollector collector = event.getSubmitNodeCollector();

        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);
        collector.submitCustomGeometry(poseStack, HighlightRenderer.LINES_NO_DEPTH_RENDER_TYPE, (matrix, consumer) -> {
            for (GroundFieldData f : FIELDS.values()) {
                renderDisc(matrix, consumer, f);
            }
        });
        poseStack.popPose();
    }

    /** 地表粒子：在半径圆盘内均匀撒点（数量 ∝ 面积），颜色按地面类型。 */
    private static void spawnParticles(Level level, GroundFieldData f) {
        int color = f.color();
        ColorParticleOption particle = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, color);
        float r = f.radius();
        int count = (int) Math.ceil((double) Math.PI * r * r);
        for (int j = 0; j < count; ++j) {
            float a = level.getRandom().nextFloat() * ((float) Math.PI * 2F);
            float rad = (float) Math.sqrt(level.getRandom().nextFloat()) * r;
            double x = f.center().x + Math.cos(a) * rad;
            double y = f.center().y + 0.05;
            double z = f.center().z + Math.sin(a) * rad;
            level.addAlwaysVisibleParticle(particle, x, y, z, 0.0, 0.01, 0.0);
        }
    }

    /** 贴地圆盘网格（同心环 + 径向辐条），复用 HighlightRenderer 的 LINES 管线；颜色按地面类型。 */
    private static void renderDisc(PoseStack.Pose pose, VertexConsumer consumer, GroundFieldData f) {
        double cx = f.center().x, cy = f.center().y + 0.02, cz = f.center().z;
        float r = f.radius();
        int color = f.color();

        int rings = 3;
        for (int ri = 1; ri <= rings; ri++) {
            float rr = r * ri / rings;
            int seg = 32;
            for (int i = 0; i < seg; i++) {
                double a0 = (double) i / seg * Math.PI * 2;
                double a1 = (double) (i + 1) / seg * Math.PI * 2;
                consumer.addVertex(pose, (float) (cx + Math.cos(a0) * rr), (float) cy, (float) (cz + Math.sin(a0) * rr))
                        .setColor(color).setNormal(pose, 0, 1, 0).setLineWidth(1.0F);
                consumer.addVertex(pose, (float) (cx + Math.cos(a1) * rr), (float) cy, (float) (cz + Math.sin(a1) * rr))
                        .setColor(color).setNormal(pose, 0, 1, 0).setLineWidth(1.0F);
            }
        }
        int spokes = 8;
        for (int i = 0; i < spokes; i++) {
            double a = (double) i / spokes * Math.PI * 2;
            consumer.addVertex(pose, (float) cx, (float) cy, (float) cz)
                    .setColor(color).setNormal(pose, 0, 1, 0).setLineWidth(1.0F);
            consumer.addVertex(pose, (float) (cx + Math.cos(a) * r), (float) cy, (float) (cz + Math.sin(a) * r))
                    .setColor(color).setNormal(pose, 0, 1, 0).setLineWidth(1.0F);
        }
    }
}
