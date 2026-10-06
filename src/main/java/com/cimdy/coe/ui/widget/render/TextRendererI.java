package com.cimdy.coe.ui.widget.render;

import net.minecraft.network.chat.Component;

import com.cimdy.coe.ui.widget.render.Align;
import com.cimdy.coe.ui.widget.render.VAlign;

public interface TextRendererI extends Renderer {

    TextRendererI line(Component text);

    TextRendererI line(Component text, Component hoverText);

    TextRendererI hoverText(Component text);

    TextRendererI setLine(int index, Component text);

    TextRendererI color(int argb);

    TextRendererI colorHover(int argb);

    TextRendererI colorActive(int argb);

    TextRendererI colorDisabled(int argb);

    TextRendererI align(Align a);

    TextRendererI vAlign(VAlign a);

    TextRendererI lineSpacing(int px);

    TextRendererI wrap(boolean b);

    TextRendererI autoScale(boolean b);

    TextRendererI shadow(boolean b);

    TextRendererI fixedScale(float s);
}
