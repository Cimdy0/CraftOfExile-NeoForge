package com.cimdy.coe.ui.screen.gem;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.ui.widget.render.Draw;
import com.cimdy.coe.ui.widget.render.RenderContext;
import com.cimdy.coe.ui.widget.render.Renderer;
import com.cimdy.coe.ui.widget.render.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * 「不可用」标记：图标去色 + 右下角红色感叹号角标。
 *
 * <p>与 {@link GemIconRender} 同属 gem 业务层（不属于 UI 库）：叠在图标渲染器<b>之后</b>，
 * 命中条件（精魂不足 / 属性需求不足 / 等级不足）由调用方判定后决定是否挂上这一层。
 *
 * <p>去色用中性灰半透遮罩实现（贴图 blit 拿不到灰度管道，且这里是 14~18px 的小图标，
 * 半透灰遮罩与真灰度肉眼无差）。角标取既有的 {@code textures/gui/unuse.png}
 * （红底白感叹号，40×40，原图是按大尺寸画的，这里整体缩到角标边长）。
 */
public class UnusableMark implements Renderer {

    public static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "textures/gui/unuse.png");

    /** 源贴图边长（正方形）。 */
    private static final int TEX = 40;
    /** 角标边长。 */
    private static final int BADGE = 8;
    /** 去色遮罩色：与 HUD 的红色「不可施放」遮罩 0x80FF3030 同族，这里用中性灰表达「灰掉」。
     *  B3：改从当前风格预设取（实例字段，随换肤生效）。 */
    private final int desaturate = Theme.current().gemDesaturate();

    /** 覆盖区域边长（图标实际占用的方形区域，通常 14 或 16）。 */
    private final int size;

    public UnusableMark(int size) {
        this.size = size;
    }

    @Override
    public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, RenderContext ctx) {
        int box = Math.min(size, Math.min(w, h));
        if (box <= 0) return;
        Draw.fill(g, x, y, box, box, desaturate);
        int b = Math.min(BADGE, box);
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                x + box - b, y + box - b, 0, 0, b, b, TEX, TEX);
    }

    @Override
    public Renderer copy() {
        return new UnusableMark(size);
    }
}
