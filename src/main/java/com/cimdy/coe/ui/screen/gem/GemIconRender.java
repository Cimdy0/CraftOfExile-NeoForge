package com.cimdy.coe.ui.screen.gem;

import com.cimdy.coe.ui.widget.component.list.ListView;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Renderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * 技能宝石图标渲染器（gem 业务专属，不属于 UI 库）。
 *
 * <p>直接 blit 一个纹理 {@link Identifier}；纹理为 {@link #EMPTY}（空气）时不画。
 * 这里只是一个不可变的固定纹理 —— 列表行的数据由 {@code ListView} 的 binder 在绑定时就确定，
 * 不通过 DataKey 每帧从 NBT 读纹理。
 */
public class GemIconRender implements Renderer {

    public static final Identifier EMPTY = Identifier.fromNamespaceAndPath("minecraft", "air");

    private final Identifier texture;
    private final int texW, texH;

    public GemIconRender(Identifier texture, int texW, int texH) {
        this.texture = texture;
        this.texW = texW;
        this.texH = texH;
    }

    @Override
    public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, RenderContext ctx) {
        if (texture == null || texture.equals(EMPTY) || w <= 0 || h <= 0) return;
        g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, texW, texH, texW, texH);
    }

    @Override
    public int preferredWidth() { return texW; }

    @Override
    public int preferredHeight() { return texH; }

    @Override
    public Renderer copy() {
        return new GemIconRender(texture, texW, texH);
    }
}
