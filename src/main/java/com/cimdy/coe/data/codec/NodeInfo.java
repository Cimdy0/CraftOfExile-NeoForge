package com.cimdy.coe.data.codec;

import com.cimdy.coe.ui.widget.graph.core.GraphNode;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * 树形节点的展示信息：名称 + 属性行，供界面绘制悬浮提示。
 *
 * <p>这是<b>业务侧</b>契约。UI 库的图画布只认自己的
 * {@code GraphNode<T>}，业务节点定义（天赋 / 科研）实现本接口后，
 * Screen 在装配画布时把它接到 {@code nodeTooltip(...)} 上即可，
 * 画布本身不知道天赋和科研的存在。
 */
public interface NodeInfo {

    /** 无树上下文时的展示名（数据生成 / 调试用的回退）。 */
    Component displayName();

    /**
     * 带树上下文的展示名。Screen 注入自己的 {@code treeId} 后调用本方法，
     * 业务实现据此拼出形如 {@code talent.<树名>.<命名空间>.<name>} 的翻译键。
     */
    default Component displayName(Identifier tree) {
        return displayName();
    }

    List<Component> statLines();
}
