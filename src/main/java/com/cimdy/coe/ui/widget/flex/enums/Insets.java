package com.cimdy.coe.ui.widget.flex.enums;

    /** 四边间距，顺序 top/right/bottom/left。 */
    public final class Insets {
        public int top, right, bottom, left;
        public Insets(int all) { set(all); }
        public Insets() {}
        public void set(int all) { top = right = bottom = left = all; }
        public void set(int t, int r, int b, int l) { top = t; right = r; bottom = b; left = l; }
        public int horizontal() { return left + right; }
        public int vertical() { return top + bottom; }
    }
