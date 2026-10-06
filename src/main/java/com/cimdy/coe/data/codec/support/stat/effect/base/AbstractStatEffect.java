package com.cimdy.coe.data.codec.support.stat.effect.base;

import com.cimdy.coe.data.codec.support.stat.tag.DomainTags;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * StatEffect 抽象基类：承载两类标签——
 * <ul>
 *   <li><b>事件标签</b> {@link #tags()}（{@link GateExpr} 布尔表达式，供 {@link GateExpr#matches} / {@link StatEngine#conditionsHold} 求值，
 *       决定触发型效果何时 fire）；</li>
 *   <li><b>领域标签</b> {@link #domainTags()}（{@code Set<String>}，mod 分类维度：fire/physical/attack/…，
 *       供 Bench/Fossil/Essence 等修饰系统做域匹配与排除，D6）。</li>
 * </ul>
 *
 * <p>数值（amount）、条件（conditions）、三段选择器（{@link com.cimdy.coe.data.codec.support.stat.StatTargets}）
 * 已全部上移到条目类 {@code StatEntry}，效果只回答「做什么」+「属于哪些领域/触发于哪些事件」。
 * 因此子类只允许携带：身份字段、语义字段、事件标签、领域标签。
 *
 * <p><b>构造重载</b>：单参 {@code AbstractStatEffect(GateExpr)} 保持旧调用点不变（领域标签默认为空）；
 * 两参 / 变参版本供需要声明领域的子类使用，避免为新增领域标签而改动全部 45 个子类构造签名。
 *
 * @see StatEffect#tags() 事件标签布尔表达式（{@link GateExpr#matches} / {@link StatEngine#conditionsHold} 求值）
 * @see StatEffect#domainTags() 领域标签（D6，mod 分类维度）
 */
public abstract class AbstractStatEffect implements StatEffect {

    private final GateExpr tags;
    private final Set<String> domainTags;

    /** 仅事件标签，无领域标签（旧调用点兼容）。 */
    protected AbstractStatEffect(GateExpr tags) {
        this(tags, Set.of());
    }

    /** 事件标签 + 变参领域标签。 */
    protected AbstractStatEffect(GateExpr tags, String... domainTags) {
        this(tags, Set.of(domainTags));
    }

    /** 事件标签 + 显式领域标签集合（权威构造）。 */
    protected AbstractStatEffect(GateExpr tags, Set<String> domainTags) {
        this.tags = tags == null ? GateExpr.ALWAYS : tags;
        this.domainTags = domainTags == null ? Set.of() : Set.copyOf(domainTags);
    }

    @Override
    public final GateExpr tags() {
        return tags;
    }

    @Override
    public Set<String> domainTags() {
        // 显式字段优先（构造参数 / JSON "domain" 覆盖逃生舱）；为空则自动从事件标签派生（D6 终态）。
        return domainTags.isEmpty() ? DomainTags.fromTags(tags) : domainTags;
    }

    /** 事件标签表达式编解码：结构化布尔树（{@code tag}/{@code and}/{@code or}/{@code not}），直接序列化 {@link GateExpr} 的 Node 树，去除旧 {@code List<String>} 冗余包装层。 */
    public static final Codec<GateExpr> TAGS_CODEC = GateExpr.CODEC;

    /** 平铺式可选字段 {@code tags}，供各子类 codec 直接复用。 */
    public static final MapCodec<GateExpr> TAGS_MAP_CODEC =
            TAGS_CODEC.optionalFieldOf("tags", GateExpr.ALWAYS);

    /** 领域标签集合编解码：JSON {@code domain} 数组为领域字符串列表。 */
    public static final Codec<Set<String>> DOMAIN_CODEC =
            Codec.STRING.listOf().xmap(HashSet::new, ArrayList::new);

    /** 平铺式可选字段 {@code domain}，供各子类 codec 复用（声明效果所属领域，D6）。 */
    public static final MapCodec<Set<String>> DOMAIN_MAP_CODEC =
            DOMAIN_CODEC.optionalFieldOf("domain", Set.of());
}
