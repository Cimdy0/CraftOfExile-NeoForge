package com.cimdy.coe.ui.widget.attach;

/**
 * 附件预设三档：把「一个屏幕该挂多重的 Decor / Sfx / Vfx」收敛成枚举常量（规格 §4 各屏默认附件表）。
 *
 * <p>三档：
 * <ul>
 *   <li>{@link #LIGHT}：常驻小窗 / 弹窗 / 径向 —— 只留必要装饰 + 关键音效，<b>无自绘粒子</b>；</li>
 *   <li>{@link #STANDARD}（默认）：常规窗口 —— 标题带 + 角点 + 标准音效族 + 成功 burst；</li>
 *   <li>{@link #RICH}：天赋研究 / 大型工作台 —— 在 STANDARD 上叠加双层描线 + 聚焦光晕 + 悬停音效 + 更多粒子。</li>
 * </ul>
 *
 * <p>九类屏目标（规格 §4.6）：TELEPORT 传送 / SITE 站点 / STOCK 存量 / CUT 切割 /
 * SKILL 技能 / CODEX 图鉴 / RADIAL 径向 / TALENT 天赋研究 / DIALOG 弹窗。
 *
 * <p>克制装配（计划四·六）：Vfx.cap 默认 64；同屏活跃特效 &lt;10；不产生世界粒子；
 * 取色一律走 Theme 令牌。Screen 取到装配好的 {@link Decor}/{@link Sfx}/{@link Vfx} 后
 * 用 {@code node.attach(...)} 挂到根窗格即可。
 *
 * <p>潜在复用方：所有 M1 屏幕（各 Screen 构造根窗格时调本类取默认附件）。
 */
public enum AttachmentPresets {
    LIGHT, STANDARD, RICH;

    /** 九类屏目标（规格 §4.6）。 */
    public enum Target {
        TELEPORT,   // 传送 / 快速旅行
        SITE,       // 站点管理
        STOCK,      // 存量
        CUT,        // 切割
        SKILL,      // 技能设定
        CODEX,      // 图鉴
        RADIAL,     // 径向菜单
        TALENT,     // 天赋 / 研究
        DIALOG      // 弹窗
    }

    /** 取默认档（STANDARD）。 */
    public static AttachmentPresets defaults() { return STANDARD; }

    // ==================================================================
    // Decor
    // ==================================================================

    /** 按档 + 屏类装配静态装饰。 */
    public static Decor decorFor(AttachmentPresets p, Target t) {
        Decor d = Decor.of();
        if (p == LIGHT) {
            // 轻档：弹窗/径向/传送只留角点，不画标题带，避免小窗视觉过载
            d.cornerDeco();
            return d;
        }
        // STANDARD 起：标题带 + 角点
        d.titleBand().cornerDeco();
        // 切割/技能走 gemGold 标题色已在标题带内，这里不再叠色
        if (p == RICH) {
            // 富档：双层描线 + 聚焦光晕
            d.doubleBorder().focusGlow();
            if (t == Target.TALENT || t == Target.RADIAL) {
                d.divider(Decor.Divider.VERTICAL);
            }
        }
        return d;
    }

    // ==================================================================
    // Sfx
    // ==================================================================

    /**
     * 按档 + 屏类装配音效族（附录 D.3：高频 HOVER / SCROLL 默认关）。
     * 挂载点就绪：各屏装配（Screen.attach）由接入矩阵 shard 完成，本方法只给默认族。
     */
    public static Sfx sfxFor(AttachmentPresets p, Target t) {
        if (p == LIGHT) {
            // 轻档：只留关键确认 + 禁用警告，不开悬停 / 滚轮
            return Sfx.of(Sfx.Kind.SELECT, Sfx.Kind.CONFIRM, Sfx.Kind.CANCEL, Sfx.Kind.WARN).hover(false).scroll(false);
        }
        if (p == STANDARD) {
            boolean droppable = (t == Target.SKILL || t == Target.CUT || t == Target.SITE);
            // D.3：TAB 切页 / WARN 禁用警告 / ERR 无效操作 全屏常备；SCROLL 高频默认关（scroll(false)）
            Sfx s = droppable
                    ? Sfx.of(
                            Sfx.Kind.OPEN, Sfx.Kind.CLOSE,
                            Sfx.Kind.SELECT, Sfx.Kind.TAB,
                            Sfx.Kind.CONFIRM, Sfx.Kind.CANCEL,
                            Sfx.Kind.SUCCESS, Sfx.Kind.FAIL, Sfx.Kind.WARN, Sfx.Kind.ERR,
                            Sfx.Kind.DROP)
                    : Sfx.of(
                            Sfx.Kind.OPEN, Sfx.Kind.CLOSE,
                            Sfx.Kind.SELECT, Sfx.Kind.TAB,
                            Sfx.Kind.CONFIRM, Sfx.Kind.CANCEL,
                            Sfx.Kind.SUCCESS, Sfx.Kind.FAIL, Sfx.Kind.WARN, Sfx.Kind.ERR);
            return s.hover(false).scroll(false);
        }
        // RICH：标准族 + 悬停（滚轮仍默认关，避免翻页噪音）
        Sfx rich = sfxFor(STANDARD, t);
        return rich.hover(true).scroll(false);
    }

    // ==================================================================
    // Vfx
    // ==================================================================

    /**
     * 按档 + 屏类装配自绘粒子附件（cap 默认 64，同屏活跃 &lt;10）。
     *
     * <p><b>D.4 默认关</b>：{@link Vfx#MASTER_ENABLED} 全局默认 {@code false}，即便此处
     * {@code enabled(true)} 也不实际渲染 —— 由全局开关 + 接入矩阵按屏开启。六特效命名工厂
     * （{@code windowOpen / pressedStars / cutSuccess / teleportRipple / stockZeroFlash /
     * skillSwapBand}）数量档位见 {@link Vfx} 常量；Screen 装配处只读常量、不新增配置菜单。
     */
    public static Vfx vfxFor(AttachmentPresets p, Target t) {
        if (p == LIGHT) {
            // 轻档：关闭自绘粒子（弹窗/径向/传送不需要）
            return new Vfx().cap(64).enabled(false);
        }
        // STANDARD / RICH：启用 burst（绑定/切割成功光点），cap 64；
        // 实际是否渲染仍受 Vfx.MASTER_ENABLED（默认关）约束。
        return new Vfx().cap(64).enabled(true);
    }
}
