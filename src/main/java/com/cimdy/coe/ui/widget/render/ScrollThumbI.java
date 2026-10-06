package com.cimdy.coe.ui.widget.render;

import java.util.function.DoubleSupplier;

public interface ScrollThumbI extends Renderer {

    ScrollThumbI autoLength(DoubleSupplier visibleRatio);

    ScrollThumbI minLength(int px);

    ScrollThumbI orientation(Orientation o);

    ScrollThumbI length(int px);
}
