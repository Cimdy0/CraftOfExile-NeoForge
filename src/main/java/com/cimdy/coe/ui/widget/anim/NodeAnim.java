package com.cimdy.coe.ui.widget.anim;

/**
 * 节点的渲染动画状态（alpha / 位移 / 缩放 / 光晕强度）。
 *
 * <p>动画原语（Fade / Slide / Scale / Pulse）把数值写进这里，
 * {@link AnimatedNode#render} 据此对整棵子树施加变换与透明度 —— 布局坐标（absX/absY）不变，
 * 纯视觉偏移，命中测试与 tooltip 不受影响。
 */
public class NodeAnim {

    /** 透明度 0..1，1 = 不透明（默认）。 */
    public float alpha = 1F;

    /** 视觉偏移（px），相对节点自身左上角。 */
    public int offsetX;
    public int offsetY;

    /** 缩放，1 = 原始大小（默认）。 */
    public float scale = 1F;

    /** 光晕强度 0..1（激活 / 低值警告等脉冲用，由组件自行消费）。 */
    public float glow = 0F;

    /** 是否存在需要特殊渲染的变换。全默认时走 Node 原始渲染路径（零回归）。 */
    public boolean hasTransform() {
        return alpha < 1F || offsetX != 0 || offsetY != 0 || scale != 1F || glow > 0F;
    }
}
