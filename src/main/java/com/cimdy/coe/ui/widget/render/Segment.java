package com.cimdy.coe.ui.widget.render;

/**
     * 一段色带，位置为轨道上的归一化区间。
     *
     * @param from  起点 [0,1]
     * @param to    终点 [0,1]
     * @param color ARGB
     */
    public record Segment(double from, double to, int color) {}
