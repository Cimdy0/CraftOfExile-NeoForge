package com.cimdy.coe.data.codec.talent.effect;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Optional;

/**
 * 天赋效果：节点授予的「一个效果」。采用注册表 + Codec 分发，使新增天赋能力类型
 * 只需注册一个实现，无需改动 {@link TalentEffectApplier} 或任何施加逻辑。
 *
 * <p>内置实现：{@link StatTalentEffect} —— 自定义属性（Stat）修正。
 *
 * <p>所有天赋效果统一经 {@link TalentEffectApplier} 以「来源键」写入玩家 {@code STAT_STORE} 附件、
 * 并以同前缀整体移除（全量重建），实现纯 Stat 的增删，不再依赖原版 AttributeModifier / limiter 跟踪。
 *
 * <p>未来新增天赋机制（如「条件效果：满能量护盾时 +X 法伤」「伤害转换」「命中触发」等）
 * 只需实现本接口并在 {@link TalentEffectType} 注册，数据（JSON）即可直接驱动，实现真正的数据驱动拓展。
 */
public interface TalentEffect {
    /** 效果类型 id（写入 JSON 的 "type" 字段，用于 Codec 分发）。 */
    String type();

    /**
     * 把效果施加到玩家。实现应把自身 Stat 条目以「来源键」
     * （{@code coe.talent.<tree>.<node>.<index>#<entryIdx>}）写入玩家 {@code STAT_STORE} 附件，
     * 由 {@link TalentEffectApplier} 在重建时统一按前缀移除，无需自行维护任何跟踪列表。
     */
    default void apply(Player player, Identifier treeId, String nodeId, int index) {}

    /** UI 展示文本。 */
    List<Component> describe();

    /**
     * 本地化 UI 展示文本（P3）；默认回落到 {@link #describe()}（locale 不影响旧子类）。
     * {@link StatTalentEffect} 覆写为逐条透传 locale。
     */
    default List<Component> describe(java.util.Locale locale) {
        return describe();
    }

    /**
     * 该效果作为「多选一」选项时，选中后节点应渲染的图标（资源文件名，不含扩展名）。
     * 默认无 —— 节点沿用自身 {@code icon}。多选一节点（旅行节点换图标 / 显著节点仅高亮）
     * 由界面依据此字段与节点类型决定渲染行为。
     */
    default Optional<String> icon() {
        return Optional.empty();
    }
}
