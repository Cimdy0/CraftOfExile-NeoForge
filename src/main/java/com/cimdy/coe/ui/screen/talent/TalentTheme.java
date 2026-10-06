package com.cimdy.coe.ui.screen.talent;
import com.cimdy.coe.ui.widget.render.Theme;

import com.cimdy.coe.data.codec.talent.node.TalentNodeType;
import com.cimdy.coe.ui.widget.graph.painter.NodePainter;

/**
 * 天赋树视觉色板 —— 现在是 <b>转发层</b>（工作流 B3 第二波）。
 *
 * <p>以前这里是 {@code public static final int} 常量：类加载时即冻结，
 * 而且<b>完全绕开</b> {@link com.cimdy.coe.ui.widget.render.StylePreset}，
 * 于是 {@code Theme.apply(preset)} 对天赋树零效果 —— 这正是「换肤不生效」的两大死角之一。
 * 现在全部改成方法，每次调用都从 {@code Theme.current()} 取，换肤才能真正作用到树上。
 *
 * <p>节点半径/底色按类型（{@link TalentNodeType}），边框与连线按分配状态。
 * 状态语义见 {@link TalentAllocator}：已点亮（金）/ 休眠（灰）/ 可点（绿）/ 锁定（深灰）。
 */
public final class TalentTheme {


    public static int borderLocked()    { return Theme.current().talentBorderLocked(); }
    public static int borderAvailable() { return Theme.current().talentBorderAvailable(); }

    public static int edgeDim()     { return Theme.current().talentEdgeDim(); }
    public static int edgeLit()     { return Theme.current().talentEdgeLit(); }

    /** 被辐射珠宝覆盖的节点外框颜色。 */
    public static int jewelLit()  { return Theme.current().talentJewelLit(); }

    /** 珠宝插槽：空槽青框 / 已插珠宝金框。 */
    public static int socketEmpty()  { return Theme.current().talentSocketEmpty(); }
    public static int socketFilled() { return Theme.current().talentSocketFilled(); }

    /** 武器组边框色（索引 0=全局，1=I，2=II…）。 */
    public static int bindingColor(int weaponSet) {
        return switch (weaponSet) {
            case 0 -> Theme.current().talentBindingGlobal();   // 全局：青
            case 1 -> Theme.current().talentBinding1();        // I：红
            case 2 -> Theme.current().talentBinding2();        // II：绿
            default -> Theme.current().talentBindingDefault(); // 其余：灰蓝
        };
    }

    /** 休眠武器组的暗色版本。 */
    public static int bindingColorDim(int weaponSet) {
        int c = bindingColor(weaponSet);
        int r = (c >> 16) & 0xFF, g = (c >> 8) & 0xFF, b = c & 0xFF;
        return 0xFF000000 | ((r >> 1) << 16) | ((g >> 1) << 8) | (b >> 1);
    }

    public static int radiusFor(TalentNodeType t) {
        return t.radius();
    }

    public static int colorFor(TalentNodeType t) {
        return t.color();
    }

    private TalentTheme() {}
}
