package com.cimdy.coe.ui.widget.render;

import java.util.function.Supplier;

public interface ToggleRendererI extends Renderer {

    ToggleRendererI style(ToggleStyle s);

    ToggleRendererI rounding(int r);
}
