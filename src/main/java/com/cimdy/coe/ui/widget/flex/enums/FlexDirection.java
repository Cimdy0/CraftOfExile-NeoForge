package com.cimdy.coe.ui.widget.flex.enums;

    public enum FlexDirection {
        ROW(true), COLUMN(false);
        private final boolean row;
        FlexDirection(boolean row) { this.row = row; }
        public boolean isRow() { return row; }
    }
