package com.cimdy.coe.ui.widget.render;

import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Renderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.function.Supplier;

/**
 * 生物预览渲染器：在矩形区域内绘制一只朝向鼠标的生物。
 *
 * <p><b>这是旧库业务耦合最严重的一处</b>：旧 {@code ILivingEntityRender} 直接
 * {@code import com.cimdy.coe.stronghold.energy.StrongholdHelper}，在渲染时用 {@code CustomData}
 * 现场反序列化出实体 —— 一个通用 UI 库既认识模组的 NBT 约定、又负责建实体，
 * 还每帧建一次。现在 lib 只接一个 {@link Supplier}：谁给实体、怎么来的、缓存与否，
 * 全是调用方的事。
 */
class EntityPreviewRenderer implements Renderer, EntityPreviewI {

    private final Supplier<LivingEntity> source;

    /** 实体缩放；&lt;=0 表示按区域高度自动推算 */
    private float scale = 0F;
    /** 是否朝向鼠标 */
    private boolean followMouse = true;

    public EntityPreviewRenderer(Supplier<LivingEntity> source) {
        this.source = source;
    }

    /** 固定缩放。默认按区域高度的一半自动推算。 */
    public EntityPreviewRenderer scale(float s) { this.scale = s; return this; }

    public EntityPreviewRenderer followMouse(boolean b) { this.followMouse = b; return this; }

    @Override
    public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, RenderContext ctx) {
        LivingEntity living = source.get();
        if (living == null || w <= 0 || h <= 0) return;

        // 预览实体未加入世界，entity id 默认为 -1，渲染时 ItemModelResolver 会读取 id 导致崩溃，
        // 这里分配一个任意非 -1 的 id 规避 "Tried to access entity ID before ID assignment"。
        // 注意不能先调 getId() 判断 —— id 为 -1 时 getId() 本身就会抛异常。
        living.setId(1);

        AABB box = living.getBoundingBox();
        float offsetY = (float) Math.max(0, (box.getYsize() - 0.5) / 2);
        float effScale = scale > 0F ? scale : h / 2.0F;

        // 不跟随鼠标时，把「鼠标」固定在区域中心，实体即正面朝前
        int mx = followMouse ? ctx.mouseX() : x + w / 2;
        int my = followMouse ? ctx.mouseY() : y + h / 2;

        InventoryScreen.extractEntityInInventoryFollowsMouse(
                g, x, y, x + w, y + h, (int) effScale, offsetY, mx, my, living);
    }

    @Override
    public Renderer copy() {
        EntityPreviewRenderer c = new EntityPreviewRenderer(source);
        c.scale = scale;
        c.followMouse = followMouse;
        return c;
    }
}
