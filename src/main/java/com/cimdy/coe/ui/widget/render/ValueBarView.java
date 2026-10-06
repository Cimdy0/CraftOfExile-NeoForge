package com.cimdy.coe.ui.widget.render;

import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;

public interface ValueBarView extends Renderer {

    ValueBarView progressSupplier(DoubleSupplier p);

    ValueBarView trackColor(int argb);

    ValueBarView handleColor(int argb);

    ValueBarView handleHoverColor(int argb);

    ValueBarView orientation(Orientation o);

    ValueBarView labels(double min, double max, LabelPolicy policy);

    ValueBarView labelFormat(DoubleFunction<String> f);

    ValueBarView labelScale(float s);

    ValueBarView labelColor(int argb);

    ValueBarView handle(boolean b);

    ValueBarView handleThickness(int px);

    ValueBarView handleActive(boolean b);

    Orientation orientation();

    int trackColor();

    boolean handleEnabled();

    int handleColor();

    int handleHoverColor();

    int handleThickness();
}
