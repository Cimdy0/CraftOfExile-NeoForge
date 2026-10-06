package com.cimdy.coe.data.codec.progression.research;

import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;
import com.cimdy.coe.data.codec.NodeInfo;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * 研究树节点（树状，前置 DAG）。
 *
 * <p>与科技树不同：研究树有 {@code prerequisites}（其它研究节点 id）构成前置依赖，
 * 有 {@code x/y} 布局坐标（复用通用画布 {@code PannableGraphCanvas}），
 * 且在研究台耗时 {@code duration_ticks} + 消耗 {@code cost} 研究点后生效。
 * 效果见 {@link ResearchEffect}。
 *
 * <p>实现 {@link NodeInfo} 是为了直接被通用画布识别（悬浮 tooltip 由画布统一绘制），
 * 与天赋节点 {@code TalentNodeDef} 同一范式，避免为研究树另造一套渲染适配层。
 */
public record ResearchNodeDef(Identifier id, String name, float x, float y,
                              List<Identifier> prerequisites, int durationTicks, int cost,
                              ResearchEffect effect, String icon) implements NodeInfo {

    public static final Codec<ResearchNodeDef> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("name", "").forGetter(ResearchNodeDef::name),
            Codec.FLOAT.optionalFieldOf("x", 0f).forGetter(ResearchNodeDef::x),
            Codec.FLOAT.optionalFieldOf("y", 0f).forGetter(ResearchNodeDef::y),
            Identifier.CODEC.listOf().optionalFieldOf("prerequisites", List.of()).forGetter(ResearchNodeDef::prerequisites),
            Codec.INT.optionalFieldOf("duration_ticks", 1200).forGetter(ResearchNodeDef::durationTicks),
            Codec.INT.optionalFieldOf("cost", 1).forGetter(ResearchNodeDef::cost),
            ResearchEffect.CODEC.optionalFieldOf("effect", ResearchEffect.EMPTY).forGetter(ResearchNodeDef::effect),
            Codec.STRING.optionalFieldOf("icon", "").forGetter(ResearchNodeDef::icon)
    ).apply(instance, (name, x, y, prerequisites, durationTicks, cost, effect, icon) ->
            new ResearchNodeDef(null, name, x, y, prerequisites, durationTicks, cost, effect, icon)));

    /**
     * 网络编解码：与 {@link #CODEC} 的唯一差别是<b>显式携带 id</b>。
     * 同步到客户端时没有文件名可依，必须把 id 一并发过去（前置依赖也按 id 连边）。
     */
    public static final Codec<ResearchNodeDef> NETWORK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(ResearchNodeDef::id),
            Codec.STRING.optionalFieldOf("name", "").forGetter(ResearchNodeDef::name),
            Codec.FLOAT.optionalFieldOf("x", 0f).forGetter(ResearchNodeDef::x),
            Codec.FLOAT.optionalFieldOf("y", 0f).forGetter(ResearchNodeDef::y),
            Identifier.CODEC.listOf().optionalFieldOf("prerequisites", List.of()).forGetter(ResearchNodeDef::prerequisites),
            Codec.INT.optionalFieldOf("duration_ticks", 1200).forGetter(ResearchNodeDef::durationTicks),
            Codec.INT.optionalFieldOf("cost", 1).forGetter(ResearchNodeDef::cost),
            ResearchEffect.CODEC.optionalFieldOf("effect", ResearchEffect.EMPTY).forGetter(ResearchNodeDef::effect),
            Codec.STRING.optionalFieldOf("icon", "").forGetter(ResearchNodeDef::icon)
    ).apply(instance, ResearchNodeDef::new));

    /** 载入时由文件名注入 id（JSON 不含 id 字段）。 */
    public ResearchNodeDef withId(Identifier id) {
        return new ResearchNodeDef(id, name, x, y, prerequisites, durationTicks, cost, effect, icon);
    }

    // ==================== NodeInfo（画布 tooltip） ====================

    /**
     * 展示名：数据包填了 {@code name} 就当翻译键用，否则回退到 {@code research.coe.<路径>}。
     * 两条路都走翻译键，保证多语言可覆盖；实在没有语言项时 Minecraft 会原样显示键名，
     * 至少还能辨认是哪个节点。
     */
    @Override
    public Component displayName() {
        if (!name.isEmpty()) return Component.translatable(name);
        return Component.translatable("research." + (id == null ? "coe.unknown" : id.getNamespace() + "." + id.getPath()));
    }

    /** 研究树只有一棵，树上下文不参与命名，直接回退。 */
    @Override
    public Component displayName(Identifier tree) {
        return displayName();
    }

    /** tooltip 正文：耗时 / 研究点 / 效率加成 / 工人策略，全部以「玩家能据此决策」为准绳。 */
    @Override
    public List<Component> statLines() {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("research.coe.tooltip.cost", cost));
        lines.add(Component.translatable("research.coe.tooltip.duration", durationTicks / 20));
        for (EfficiencyBonus bonus : effect.efficiency()) {
            int percent = Math.round((bonus.multiplier() - 1f) * 100f);
            lines.add(Component.translatable("research.coe.tooltip.efficiency",
                    workTypeName(bonus.workType()), (percent >= 0 ? "+" : "") + percent + "%"));
        }
        WorkerPolicy policy = effect.policy();
        if (!policy.priorityBias().isEmpty()) {
            lines.add(Component.translatable("research.coe.tooltip.priority", workTypeName(policy.priorityBias())));
        }
        if (!policy.allowWorkTypes().isEmpty()) {
            lines.add(Component.translatable("research.coe.tooltip.allow",
                    String.join(", ", policy.allowWorkTypes())));
        }
        if (!prerequisites.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (Identifier pre : prerequisites) names.add(pre.getPath());
            lines.add(Component.translatable("research.coe.tooltip.prerequisites", String.join(", ", names)));
        }
        return lines;
    }

    /** 工种名：{@code "*"} 表示全工种，其余按 {@code work_type.coe.<名>} 取翻译。 */
    private static Component workTypeName(String workType) {
        return workType.equals("*")
                ? Component.translatable("work_type.coe.all")
                : Component.translatable("work_type.coe." + workType);
    }
}
