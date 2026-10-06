package com.cimdy.coe.ui.widget.render;

/**
     * 一条刻度线。
     *
     * @param at        位置 [0,1]
     * @param color     ARGB
     * @param thickness 线宽（像素）
     */
    public record Tick(double at, int color, int thickness) {}
