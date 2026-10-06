package com.cimdy.coe.ui.widget.render;

import net.minecraft.network.chat.Component;

public interface TextFieldRendererI extends Renderer {

    TextFieldRendererI placeholder(Component c);

    TextFieldRendererI textColor(int argb);

    TextFieldRendererI textColorDisabled(int argb);

    TextFieldRendererI placeholderColor(int argb);

    TextFieldRendererI selectionColor(int argb);

    TextFieldRendererI padding(int px);
}
