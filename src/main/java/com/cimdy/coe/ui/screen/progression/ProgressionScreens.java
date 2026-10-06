package com.cimdy.coe.ui.screen.progression;

import com.cimdy.coe.event.net.NetEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

/**
 * 进度系统界面的客户端打开入口。
 *
 * <p>存在的意义是<b>隔离端</b>：{@code NetEvent} 是双端共用类，若在其中直接引用
 * {@link Minecraft} 与 Screen，专用服务器加载该类时有触碰客户端类的风险。
 * 把所有客户端调用收敛到本类，服务端永远不会解析到它。
 */
public final class ProgressionScreens {

    private ProgressionScreens() {
    }

    /** 打开研究树界面，并记住是哪台研究台发起的（后续 StartResearch 会带回该坐标）。 */
    public static void openResearch(BlockPos benchPos) {
        Minecraft.getInstance().setScreenAndShow(new ResearchTreeScreen(benchPos));
    }

    /** 打开科技树界面。{@code benchPos} 可为 null（从非研究台入口进入时无法切到研究树）。 */
    public static void openTech(BlockPos benchPos) {
        Minecraft.getInstance().setScreenAndShow(new TechTreeScreen(benchPos));
    }
}
