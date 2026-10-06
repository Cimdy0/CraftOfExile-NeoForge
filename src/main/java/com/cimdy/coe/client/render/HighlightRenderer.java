package com.cimdy.coe.client.render;

import com.cimdy.coe.CraftOfExile;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 世界中「方块 / AABB 高亮框」的客户端渲染与状态管理（原 {@code event.RenderEvent}，铁律 #56 迁出）。
 *
 * <p>仅客户端加载（{@code value = Dist.CLIENT}），服务端不会触碰 Blaze3D 类。
 * LINES 管线同时被 {@link GroundFieldRenderer} 复用。
 */
@EventBusSubscriber(modid = CraftOfExile.MODID, value = Dist.CLIENT)
public class HighlightRenderer {
    public static final RenderPipeline LINES_NO_DEPTH;

    static {
        LINES_NO_DEPTH = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath("coe", "pipeline/lines_no_depth"))
                .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
                .build();
    }

    public static final RenderType LINES_NO_DEPTH_RENDER_TYPE = RenderType.create(
            "lines_no_depth",
            RenderSetup.builder(LINES_NO_DEPTH)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                    .createRenderSetup()
    );

    // 方块高亮记录 (BlockPos -> (颜色, 剩余tick, 是否无限))
    private static final Map<BlockPos, HighlightEntry> blockHighlights = new LinkedHashMap<>();
    // AABB高亮记录 (AABB -> (颜色, 剩余tick, 是否无限))
    private static final Map<AABB, HighlightEntry> aabbHighlights = new LinkedHashMap<>();

    /**
     * 高亮数据条目
     */
    private static class HighlightEntry {
        final int color;
        int remainingTicks;
        final boolean infinite;

        HighlightEntry(int color, int ticks) {
            this.color = color;
            this.remainingTicks = ticks;
            this.infinite = ticks < 0;
        }
    }

    // ==================== 方块高亮管理 ====================
    /** 添加一个持续100tick的高亮 */
    public static void addHighlightedPosition(BlockPos pos) {
        addHighlightedPosition(pos, 0xFF00FF00, 100);
    }

    /** 添加一个指定颜色的高亮 (默认持续100tick) */
    public static void addHighlightedPosition(BlockPos pos, int color) {
        addHighlightedPosition(pos, color, 100);
    }

    /** 添加一个指定颜色和持续时间的方块高亮 (duration ≤ 0 表示无限) */
    public static void addHighlightedPosition(BlockPos pos, int color, int duration) {
        int ticks = duration <= 0 ? -1 : duration;
        blockHighlights.put(pos.immutable(), new HighlightEntry(color, ticks));
    }

    /** 移除指定位置的方块高亮 */
    public static void removeHighlightedPosition(BlockPos pos) {
        blockHighlights.remove(pos);
    }

    /** 批量添加方块高亮 (统一颜色, 无限持续) */
    public static void addHighlightedPositions(Collection<BlockPos> positions, int color) {
        addHighlightedPositions(positions, color, -1);
    }

    public static void addHighlightedPositions(Collection<BlockPos> positions, int color, int duration) {
        if (positions != null) {
            for (BlockPos pos : positions) addHighlightedPosition(pos, color, duration);
        }
    }

    // ==================== AABB 高亮管理 ====================
    /** 添加一个持续100tick的AABB高亮 */
    public static void addHighlightedAABB(AABB aabb) {
        addHighlightedAABB(aabb, 0xFF00FF00, 100);
    }

    /** 添加一个指定颜色的AABB高亮 (默认持续100tick) */
    public static void addHighlightedAABB(AABB aabb, int color) {
        addHighlightedAABB(aabb, color, 100);
    }

    /** 添加一个指定颜色和持续时间的AABB高亮 (duration ≤ 0 表示无限) */
    public static void addHighlightedAABB(AABB aabb, int color, int duration) {
        int ticks = duration <= 0 ? -1 : duration;
        aabbHighlights.put(aabb, new HighlightEntry(color, ticks));
    }

    /** 移除指定AABB高亮 (需要完全相同的AABB对象) */
    public static void removeHighlightedAABB(AABB aabb) {
        aabbHighlights.remove(aabb);
    }

    // ==================== 渲染 ====================
    @SubscribeEvent
    public static void onSubmitCustomGeometry(SubmitCustomGeometryEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        // 更新计时 (非无限高亮)
        updateTimers();

        if (blockHighlights.isEmpty() && aabbHighlights.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 camVec = mc.gameRenderer.mainCamera().position();
        SubmitNodeCollector collector = event.getSubmitNodeCollector();

        poseStack.pushPose();
        poseStack.translate(-camVec.x, -camVec.y, -camVec.z);

        collector.submitCustomGeometry(poseStack, LINES_NO_DEPTH_RENDER_TYPE, (matrix, consumer) -> {
            // 绘制方块高亮
            for (var entry : blockHighlights.entrySet()) {
                BlockPos pos = entry.getKey();
                if (!mc.level.isLoaded(pos)) continue;
                VoxelShape shape = mc.level.getBlockState(pos).getShape(mc.level, pos);
                if (shape.isEmpty()) shape = Blocks.GLASS.defaultBlockState().getShape(mc.level, pos);
                AABB aabb = shape.bounds().move(pos);
                renderLineBox(matrix, consumer, aabb, entry.getValue().color);
            }

            // 绘制AABB高亮
            for (var entry : aabbHighlights.entrySet()) {
                renderLineBox(matrix, consumer, entry.getKey(), entry.getValue().color);
            }
        });

        poseStack.popPose();
    }

    /** 每帧更新非无限高亮的剩余时间 */
    private static void updateTimers() {
        blockHighlights.values().removeIf(entry -> {
            if (entry.infinite) return false;
            entry.remainingTicks--;
            return entry.remainingTicks <= 0;
        });
        aabbHighlights.values().removeIf(entry -> {
            if (entry.infinite) return false;
            entry.remainingTicks--;
            return entry.remainingTicks <= 0;
        });
    }

    // ==================== 绘制工具 ====================
    private static void renderLineBox(PoseStack.Pose pose, VertexConsumer consumer, AABB aabb, int color) {
        float minX = (float) aabb.minX;
        float minY = (float) aabb.minY;
        float minZ = (float) aabb.minZ;
        float maxX = (float) aabb.maxX;
        float maxY = (float) aabb.maxY;
        float maxZ = (float) aabb.maxZ;

        float[][] lines = {
                {minX, minY, minZ, maxX, minY, minZ},
                {minX, minY, minZ, minX, maxY, minZ},
                {minX, minY, minZ, minX, minY, maxZ},
                {maxX, maxY, maxZ, minX, maxY, maxZ},
                {maxX, maxY, maxZ, maxX, minY, maxZ},
                {maxX, maxY, maxZ, maxX, maxY, minZ},
                {minX, maxY, minZ, maxX, maxY, minZ},
                {minX, maxY, minZ, minX, maxY, maxZ},
                {maxX, minY, minZ, maxX, maxY, minZ},
                {maxX, minY, minZ, maxX, minY, maxZ},
                {minX, minY, maxZ, maxX, minY, maxZ},
                {minX, minY, maxZ, minX, maxY, maxZ}
        };

        float lineWidth = 1.0F;

        for (float[] line : lines) {
            consumer.addVertex(pose, line[0], line[1], line[2])
                    .setColor(color)
                    .setNormal(pose, 1, 1, 1)
                    .setLineWidth(lineWidth);
            consumer.addVertex(pose, line[3], line[4], line[5])
                    .setColor(color)
                    .setNormal(pose, 1, 1, 1)
                    .setLineWidth(lineWidth);
        }
    }
}
