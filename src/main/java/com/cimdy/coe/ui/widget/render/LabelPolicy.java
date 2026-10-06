package com.cimdy.coe.ui.widget.render;

import com.cimdy.coe.ui.widget.core.VisualState;

/** 标签显示策略：由调用方决定「什么时候显示两端数值」。 */
    @FunctionalInterface
    public interface LabelPolicy {
        boolean show(VisualState state);

        LabelPolicy ALWAYS = s -> true;
        LabelPolicy NEVER = s -> false;
        LabelPolicy ON_HOVER = VisualState::hovered;
    }
