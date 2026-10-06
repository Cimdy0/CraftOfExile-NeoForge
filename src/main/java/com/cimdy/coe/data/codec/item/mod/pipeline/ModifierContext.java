package com.cimdy.coe.data.codec.item.mod.pipeline;

import com.cimdy.coe.data.codec.item.state.ModifiableState;
import com.cimdy.coe.data.codec.item.mod.identity.Location;
import net.minecraft.resources.Identifier;
import java.util.List;
import java.util.Set;

/**
 * 修饰管线上下文：流经 {@link ModifierPipeline} 各阶段的不可变快照。
 * <p>字段：{@code types} / {@code locations} / {@code state} / {@code excludedTags}。
 * 各工艺系统把「本次操作」数据挂在这里，由对应 {@link ModifierStage} 臂读取并作用到候选池——
 * stage 是机制，context 携带数据，与 {@code BenchStage} 读取 {@code excludedTags} 同构。</p>
 *
 * <p>{@code state} 为通用载体 {@link ModifiableState}（装备 / 药剂 / 护符），使词缀池构建对三类载体同构：
 * 管线只需 {@code level()} 做 tier 过滤；装备专有的预设条件（局部属性 / 属性需求 / 类型链）
 * 由 {@code EquipmentModPreset.Condition} 自行做 {@code instanceof} 收窄。</p>
 */
public record ModifierContext(
        List<Identifier> types,
        List<Location> locations,
        ModifiableState state,
        Set<String> excludedTags
) {
}
