package com.cimdy.coe.ui.widget.graph.core;

/**
 * 二维相机：world ↔ screen 变换的<b>唯一</b>数学来源。
 *
 * <p>屏幕坐标一律指「相对视口左上角」的局部坐标，不含视口自身在屏幕上的位置 ——
 * 这样相机完全不需要知道画布被摆在哪里，画布挪窝时相机无需同步。
 *
 * <p>约定：{@link #camX}/{@link #camY} 是视口左上角对应的 world 坐标，
 * 于是 {@code screen = (world - cam) * zoom}。
 */
public class Camera {

    /** 视口左上角对应的 world 坐标。 */
    public float camX = 0, camY = 0;

    public float zoom = 1.0f;

    /** 缩放上下界。默认范围放得很宽，是为了同时容得下「几十个节点的小树」与「坐标量级上万的大树」。 */
    public float minZoom = 0.01f, maxZoom = 4.0f;

    // ------------------------------------------------------------------
    // 变换
    // ------------------------------------------------------------------

    public float worldToScreenX(float wx) { return (wx - camX) * zoom; }
    public float worldToScreenY(float wy) { return (wy - camY) * zoom; }

    public float screenToWorldX(float sx) { return sx / zoom + camX; }
    public float screenToWorldY(float sy) { return sy / zoom + camY; }

    /** 一个屏幕像素等于多少 world 单位。画线宽、命中容差这类「按像素说话」的量要乘它。 */
    public float pixelsToWorld() { return 1f / Math.max(1e-4f, zoom); }

    // ------------------------------------------------------------------
    // 操作
    // ------------------------------------------------------------------

    public Camera zoomRange(float min, float max) {
        if (min > 0 && min < max) {
            this.minZoom = min;
            this.maxZoom = max;
            this.zoom = clamp(zoom, min, max);
        }
        return this;
    }

    public Camera zoom(float z) { this.zoom = clamp(z, minZoom, maxZoom); return this; }

    /** 拖拽平移：吃进屏幕位移，换算成 world 位移。 */
    public void pan(float dxScreen, float dyScreen) {
        camX -= dxScreen / zoom;
        camY -= dyScreen / zoom;
    }

    /** 以光标为锚点缩放 —— 缩放后光标底下的那个 world 点保持不动。 */
    public void zoomAt(float sx, float sy, float factor) {
        float wx = screenToWorldX(sx), wy = screenToWorldY(sy);
        zoom = clamp(zoom * factor, minZoom, maxZoom);
        camX = wx - sx / zoom;
        camY = wy - sy / zoom;
    }

    /** 把某个 world 点摆到视口正中。 */
    public void centerOn(float worldX, float worldY, float viewportW, float viewportH) {
        this.camX = worldX - (viewportW / zoom) / 2f;
        this.camY = worldY - (viewportH / zoom) / 2f;
    }

    /**
     * 缩放并平移，使给定 world 包围盒（外扩 {@code padding} 个屏幕像素）恰好落进视口。
     * 初始化时用它一次性适配任意规模的图，免去为每种数据手调初始 zoom。
     */
    public void fitToBounds(float minX, float minY, float maxX, float maxY,
                            float viewportW, float viewportH, float padding) {
        float w = Math.max(1f, maxX - minX);
        float h = Math.max(1f, maxY - minY);
        float zx = (viewportW - padding * 2f) / w;
        float zy = (viewportH - padding * 2f) / h;
        this.zoom = clamp(Math.min(zx, zy), minZoom, maxZoom);
        centerOn((minX + maxX) / 2f, (minY + maxY) / 2f, viewportW, viewportH);
    }

    // ------------------------------------------------------------------
    // 可见范围（视口裁剪用）
    // ------------------------------------------------------------------

    /**
     * 当前可见的 world 矩形，外扩 {@code margin}（world 单位）。
     *
     * @return {@code [minX, minY, maxX, maxY]}
     */
    public float[] visibleWorldBounds(float viewportW, float viewportH, float margin) {
        float minX = camX - margin;
        float minY = camY - margin;
        float maxX = camX + viewportW / zoom + margin;
        float maxY = camY + viewportH / zoom + margin;
        return new float[]{minX, minY, maxX, maxY};
    }

    public Camera copy() {
        Camera c = new Camera();
        c.camX = camX;
        c.camY = camY;
        c.zoom = zoom;
        c.minZoom = minZoom;
        c.maxZoom = maxZoom;
        return c;
    }

    private static float clamp(float v, float a, float b) {
        return Math.max(a, Math.min(b, v));
    }
}
