package com.cimdy.coe.ui.widget.render;

import com.cimdy.coe.ui.widget.render.BorderStyle;

public interface ColorRendererI extends Renderer {

    ColorRendererI background(int argb);

    ColorRendererI backgroundHover(int argb);

    ColorRendererI backgroundActive(int argb);

    ColorRendererI backgroundPressed(int argb);

    ColorRendererI backgroundDisabled(int argb);

    ColorRendererI border(int argb);

    ColorRendererI borderHover(int argb);

    ColorRendererI borderActive(int argb);

    ColorRendererI borderDisabled(int argb);

    ColorRendererI borderStyle(BorderStyle style);

    ColorRendererI cornerDots(int argb);

    ColorRendererI rounding(int r);

    ColorRendererI smooth(boolean on);

    ColorRendererI hoverGradient(int topArgb);

    ColorRendererI pressedGradient(int topArgb);
}
