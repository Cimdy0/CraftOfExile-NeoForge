package com.cimdy.coe.data.codec.talent.node;

import com.cimdy.coe.data.codec.talent.effect.StatTalentEffect;
import com.cimdy.coe.data.codec.talent.effect.TalentEffect;
import com.cimdy.coe.data.codec.talent.effect.TalentEffectType;
import com.cimdy.coe.data.codec.talent.tree.TalentTreeModelLoader;
import com.cimdy.coe.client.ClientLocale;

import com.cimdy.coe.data.codec.NodeInfo;
import com.cimdy.coe.ui.screen.talent.TalentAllocator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 单个天赋节点定义（静态数据，可来自 datapack JSON）。
 *
 * <p>连接采用<b>节点中心</b>模型：每个节点通过 {@link #links()} 声明自己连接到的邻居 id，
 * 而非另立一条带 from/to 的「边」。这样：
 * <ul>
 *   <li>一个节点可被任意多个其它节点列入 {@code links}，天然支持「被无数节点链接」；</li>
 *   <li>连接<b>无前后关系</b>——只要任一邻居被点亮，本节点即可被点亮（见 {@link TalentAllocator}）。</li>
 * </ul>
 *
 * <p>节点展示文本完全由 {@link #effects()}（{@link StatTalentEffect}）驱动；不再单独维护
 * {@code stats} 文本列表，避免与效果重复。图标路径 {@link #icon()} 为非空字符串，
 * 由数据生成阶段为每个节点手动指定（不再运行时按属性/名称推断）。
 *
 * <p><b>节点类型与名均不存于节点 JSON</b>：{@link #type()} 取自文件层
 * {@code node/<type>/<name>.json} 的 {@code <type>} 目录名，{@link #name()} 取自文件名
 * {@code <name>}；二者均在加载时分别经 {@link #withType(TalentNodeType)} 与 {@link #withName(String)}
 * 注入并缓存（{@link TalentTreeModelLoader}）。CODEC 因此不再读写 {@code type} 与 {@code name} 字段，
 * 数据生成阶段仍通过 {@link #of} 为每个节点写入 name（仅用于推导文件名），但不会落盘到 JSON。
 */
public record TalentNodeDef(
        String id, float x, float y, TalentNodeType type,
        String name, ItemStack iconItem,
        List<TalentEffect> effects, List<TalentEffect> choices,
        List<String> excludes, String icon,
        List<String> links
) implements NodeInfo {

    public static final Codec<TalentNodeDef> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(TalentNodeDef::id),
            Codec.FLOAT.optionalFieldOf("x", 0f).forGetter(TalentNodeDef::x),
            Codec.FLOAT.optionalFieldOf("y", 0f).forGetter(TalentNodeDef::y),
            TalentEffectType.CODEC.listOf().optionalFieldOf("effects", List.of())
                    .forGetter(TalentNodeDef::effects),
            TalentEffectType.CODEC.listOf().optionalFieldOf("choices", List.of())
                    .forGetter(TalentNodeDef::choices),
            Codec.STRING.listOf().optionalFieldOf("excludes", List.of())
                    .forGetter(TalentNodeDef::excludes),
            Codec.STRING.optionalFieldOf("icon", "").forGetter(TalentNodeDef::icon),
            Codec.STRING.listOf().optionalFieldOf("links", List.of()).forGetter(TalentNodeDef::links)
    ).apply(instance, (id, x, y, effects, choices, excludes, icon, links) ->
            new TalentNodeDef(id, x, y, null, null, ItemStack.EMPTY,
                    effects, choices, excludes, icon, links)));

    /**
     * 载入时按所在文件层注入节点类型：类型不再存于节点 JSON，而取自
     * {@code node/<type>/<attr>.json} 的 {@code <type>} 目录名（见 {@link TalentTreeModelLoader}）。
     */
    public TalentNodeDef withType(TalentNodeType type) {
        return new TalentNodeDef(id, x, y, type, name, iconItem, effects, choices,
                excludes, icon, links);
    }

    /**
     * 载入时按所在文件名注入节点名：名不再存于节点 JSON，而取自
     * {@code node/<type>/<name>.json} 的 {@code <name>} 文件名（见 {@link TalentTreeModelLoader}）。
     */
    public TalentNodeDef withName(String name) {
        return new TalentNodeDef(id, x, y, type, name, iconItem, effects, choices,
                excludes, icon, links);
    }

    /**
     * 带显式坐标构造 + 节点中心连接（{@code links}）：声明本节点连接到的邻居 id。
     * 所有节点都必须携带坐标（由数据生成器从数据库翻译），不再有「无坐标」分支。
     */
    public TalentNodeDef(String id, float x, float y, TalentNodeType type, String name,
                         ItemStack icon,
                         List<TalentEffect> effects, List<TalentEffect> choices,
                         List<String> links) {
        this(id, x, y, type, name, icon, effects, choices, List.of(), "", links);
    }

    // ---- 数据生成工厂 ----
    // 吸收恒为 null 的 type、恒为 ItemStack.EMPTY 的图标物品，以及恒为 List.of() 的
    // choices / excludes（按需显式传入）。用于削减生成代码里大量重复且为空的参数。

    /** 最简：无 choices、无 excludes。 */
    public static TalentNodeDef of(String id, float x, float y, String name, String icon,
                                   List<TalentEffect> effects, List<String> links) {
        return new TalentNodeDef(id, x, y, null, name, ItemStack.EMPTY,
                effects, List.of(), List.of(), icon, links);
    }

    /** 带 choices（多选一），无 excludes。 */
    public static TalentNodeDef of(String id, float x, float y, String name, String icon,
                                   List<TalentEffect> effects, List<TalentEffect> choices, List<String> links) {
        return new TalentNodeDef(id, x, y, null, name, ItemStack.EMPTY,
                effects, choices, List.of(), icon, links);
    }

    /** 该节点是否提供多选一（如旅行节点在 力量/敏捷/智力 间三选一）。 */
    public boolean hasChoice() {
        return !choices.isEmpty();
    }

    @Override
    public Component displayName() {
        // 无树上下文回退（数据生成 / 调试）：直接用 name 作翻译键。
        return Component.translatable(name);
    }

    @Override
    public Component displayName(Identifier tree) {
        // 渲染翻译键：talent.<树名>.<命名空间>.<name>（name 为全小写下划线形式）。
        // tree 由 Screen 经画布注入（见 PannableGraphCanvas.setTreeId）。
        if (tree != null) {
            return Component.translatable("talent." + tree.getPath()
                    + "." + tree.getNamespace() + "." + name);
        }
        return Component.translatable(name);
    }

    @Override
    public List<Component> statLines() {
        List<Component> lines = new ArrayList<>();
        for (TalentEffect e : effects) lines.addAll(e.describe(ClientLocale.get()));
        if (hasChoice()) {
            List<Component> opts = new ArrayList<>();
            for (TalentEffect e : choices) opts.addAll(e.describe(ClientLocale.get()));
            lines.add(Component.translatable("talent.coe.choose_one").append(String.join(" / ", opts.stream().map(Component::getString).toList())));
        }
        if (!excludes.isEmpty()) {
            lines.add(Component.translatable("talent.coe.excludes", String.join(", ", excludes)));
        }
        if (!links.isEmpty()) {
            lines.add(Component.translatable("talent.coe.links").append(String.join(", ", links)));
        }
        return lines;
    }
}
