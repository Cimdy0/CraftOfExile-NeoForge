package com.cimdy.coe.ui.widget.component.panel;

import com.cimdy.coe.ui.widget.core.Node;
import com.cimdy.coe.ui.widget.component.util.MathUtil;
import com.cimdy.coe.ui.widget.flex.core.FlexNode;
import com.cimdy.coe.ui.widget.render.Theme;

/**
 * Two-pane container split by a draggable divider. Horizontal splits left/right,
 * vertical splits top/bottom. The divider can be dragged to resize both panes;
 * the ratio (0..1) is also settable programmatically.
 *
 * <p>Follows the same "leaf to parent, manual child layout" trick as
 * {@link Scrollable}: it reports itself as a flex leaf so the parent sizes the
 * whole split, then positions its two children directly in {@link #applyFlex}.</p>
 */
public class SplitPane extends Node {

    public enum Orientation { HORIZONTAL, VERTICAL }

    private final Node first = new Node().column();
    private final Node second = new Node().column();
    private final Node divider = new Node();
    private Orientation orientation = Orientation.HORIZONTAL;
    private double ratio = 0.5;
    private int dividerThickness = 4;
    private boolean dragging = false;

    public SplitPane() {
        this.id = "split-pane";
        first.id = "split-first";
        second.id = "split-second";
        divider.id = "split-divider";
        divider.with(Theme.handle());
        super.add(first);
        super.add(divider);
        super.add(second);
    }

    public static SplitPane horizontal(Node a, Node b) {
        return new SplitPane().orientation(Orientation.HORIZONTAL).first(a).second(b);
    }

    public static SplitPane vertical(Node a, Node b) {
        return new SplitPane().orientation(Orientation.VERTICAL).first(a).second(b);
    }

    public SplitPane first(Node n) { first.clearChildren(); if (n != null) first.add(n); return this; }
    public SplitPane second(Node n) { second.clearChildren(); if (n != null) second.add(n); return this; }

    public SplitPane ratio(double r) { this.ratio = MathUtil.clamp(r, 0, 1); return this; }

    public SplitPane orientation(Orientation o) {
        this.orientation = o;
        if (o == Orientation.HORIZONTAL) { first.column(); second.column(); }
        else { first.row(); second.row(); }
        return this;
    }

    public SplitPane dividerThickness(int t) { this.dividerThickness = Math.max(2, t); return this; }

    @Override
    protected FlexNode toFlex() {
        FlexNode fn = new FlexNode();
        fn.style.copyFrom(this.flex);
        return fn;
    }

    @Override
    protected void applyFlex(FlexNode fn, int baseX, int baseY) {
        super.applyFlex(fn, baseX, baseY);
        relayout();
    }

    private void relayout() {
        int t = dividerThickness;
        int w = Math.max(0, width), h = Math.max(0, height);
        if (orientation == Orientation.HORIZONTAL) {
            int fw = (int) ((w - t) * ratio);
            int sw = w - t - fw;
            first.absX = absX;  first.absY = absY;  first.width = Math.max(0, fw); first.height = h;
            divider.absX = absX + fw; divider.absY = absY; divider.width = t; divider.height = h;
            second.absX = absX + fw + t; second.absY = absY; second.width = Math.max(0, sw); second.height = h;
        } else {
            int fh = (int) ((h - t) * ratio);
            int sh = h - t - fh;
            first.absX = absX; first.absY = absY; first.width = w; first.height = Math.max(0, fh);
            divider.absX = absX; divider.absY = absY + fh; divider.width = w; divider.height = t;
            second.absX = absX; second.absY = absY + fh + t; second.width = w; second.height = Math.max(0, sh);
        }
        if (first.width > 0 && first.height > 0) first.layout(first.absX, first.absY, first.width, first.height);
        if (second.width > 0 && second.height > 0) second.layout(second.absX, second.absY, second.width, second.height);
    }

    @Override
    protected boolean onMouseClicked(double lx, double ly, int button) {
        if (button == 0 && nearDivider(lx, ly)) { dragging = true; return true; }
        return false;
    }

    @Override
    protected boolean onMouseDragged(double lx, double ly, int button, double dx, double dy) {
        if (!dragging) return false;
        if (orientation == Orientation.HORIZONTAL) ratio = MathUtil.clamp(lx / (width - dividerThickness), 0, 1);
        else ratio = MathUtil.clamp(ly / (height - dividerThickness), 0, 1);
        relayout();
        return true;
    }

    @Override
    protected boolean onMouseReleased(double lx, double ly, int button) {
        if (dragging) { dragging = false; return true; }
        return false;
    }

    private boolean nearDivider(double lx, double ly) {
        int t = dividerThickness;
        if (orientation == Orientation.HORIZONTAL) {
            int fw = (int) ((width - t) * ratio);
            return lx >= fw - 3 && lx <= fw + t + 3 && ly >= 0 && ly <= height;
        } else {
            int fh = (int) ((height - t) * ratio);
            return ly >= fh - 3 && ly <= fh + t + 3 && lx >= 0 && lx <= width;
        }
    }

    @Override
    public Node copy() {
        SplitPane s = new SplitPane();
        copyPropsInto(s);
        s.orientation = orientation;
        s.ratio = ratio;
        s.dividerThickness = dividerThickness;
        for (Node c : first.children) s.first.add(c.copy());
        for (Node c : second.children) s.second.add(c.copy());
        return s;
    }
}
