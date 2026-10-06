package com.cimdy.coe.data.codec.support.math.context.nodes.meta.misc;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceRarityValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceValue;

/**
 * 标记接口：本节点读取 {@code Event.State} 的 Presence 记录，需要每 tick 全域进/出扫描处于开启状态。
 *
 * <p><b>为何需要标记接口而非逐类 {@code instanceof}</b>：
 * Presence 扫描是<b>按需启用</b>的——{@code EventRefCollector} 遍历全部 stat 条目的
 * conditions / amount 表达式，只有发现 presence 依赖时才置位需求、由 {@code EventSubscription} 开启扫描；
 * 零引用时扫描恒关闭以省开销。若 {@code EventRefCollector} 按具体类逐个判定
 * （历史实现即 {@code node instanceof PresenceValue}），则每新增一个读 Presence 的节点类型都必须
 * 记得回头补一个分支——漏补不会有任何编译或运行期报错，节点只会<b>静默恒返 0</b>，
 * 表现为「条件永不命中」的哑失效，极难排查。
 *
 * <p>改由本接口统一标记后，{@code EventRefCollector} 只需一次 {@code instanceof PresenceDependent}
 * 即覆盖全部现有与未来的 presence 节点，新增节点<b>实现接口即自动接线</b>，不存在漏注册路径。
 *
 * @see PresenceValue 单候选进/出判定
 * @see PresenceRarityValue 范围内成员的稀有度聚合判定
 * @see com.cimdy.coe.data.codec.support.math.EventRefCollector
 */
public interface PresenceDependent {
}
