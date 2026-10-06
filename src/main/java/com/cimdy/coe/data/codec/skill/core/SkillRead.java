package com.cimdy.coe.data.codec.skill.core;

import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceCost;
import com.cimdy.coe.data.codec.support.require.Require;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.event.data.LoadEvent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 引用型技能条目 —— <b>「具体技能 = 预设 + 实例参数 + 实例分类标签 + 实例需求/消耗」</b>，与 Stat 侧
 * {@code StatRead(ref, vars)} 同构（多出的 {@code tags} 是技能独有的分类维度，{@code require}/{@code cost}
 * 是技能天然携带、stat 没有的需求/消耗）。
 *
 * <p>持有 {@code ref}（指向一个<b>完整技能定义</b>的 id）、{@code vars}（注入该定义声明的数值参数槽）、
 * {@code tags}（本实例追加的分类标签）、以及可选的 {@code require}/{@code cost} 覆盖。
 * 所谓「预设 / 类型」不是独立注册的一类东西——它就是一个完整的 {@link Skill} 数据，注册后拿到 id；
 * 构造具体技能就是造一个 {@code SkillRead(ref, vars)}。
 *
 * <p>与 StatRead 的唯一差别：技能天然携带 require/cost，而 stat 没有。为不引入「多 read 形态 / 技能类型
 * 注册表」（铁律），本形态在单一 read 形式上额外允许可选覆盖 require / cost——仍是同一个 read 形式，
 * 不新增枚举、不新增注册表。
 *
 * <p>示例（通用诅咒）：
 * <pre>{@code
 * // 预设：coe:generic_curse —— 完整技能，诅咒范围/时长写作参数槽
 * { "params": { "curse_range": 24.0, "curse_duration": 100.0 },
 *   "tags": [ "curse" ],
 *   "curse_range": "$curse_range", "curse_duration": "$curse_duration",
 *   "phases": [] }
 *
 * // 具体技能：Elemental Weakness（自带需求与消耗，参数覆盖范围/时长）
 * { "entry": "read", "ref": "coe:generic_curse",
 *   "require": { ... }, "cost": [ { "type": "mana", "usage": "consume", "value": 14.0 } ],
 *   "vars": { "curse_range": 24.0, "curse_duration": 100.0 } }
 * }</pre>
 *
 * <p>JSON 形态：{@code { "entry": "read", "ref": "coe:some_skill", "vars": { ... },
 * "tags": [ ... ], "require": { ... }, "cost": [ ... ] }}，除 {@code ref} 外均为可选。
 */
public record SkillRead(
        Identifier ref,
        Map<String, ExpressionCodec.Value> vars,
        List<String> tags,
        Optional<Require> require,
        Optional<List<ResourceCost>> cost
) implements SkillEntry {

    public SkillRead(Identifier ref) {
        this(ref, Map.of(), List.of(), Optional.empty(), Optional.empty());
    }

    public SkillRead(Identifier ref, Map<String, ExpressionCodec.Value> vars) {
        this(ref, vars, List.of(), Optional.empty(), Optional.empty());
    }

    public SkillRead(Identifier ref, Map<String, ExpressionCodec.Value> vars, List<String> tags) {
        this(ref, vars, tags, Optional.empty(), Optional.empty());
    }

    /** 引用形态 MapCodec：含 {@code ref} 与可选 {@code vars} / {@code tags} / {@code require} / {@code cost}。 */
    public static final MapCodec<SkillRead> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Identifier.CODEC.fieldOf("ref").forGetter(SkillRead::ref),
            Codec.unboundedMap(Codec.STRING, ExpressionCodec.VALUE_CODEC)
                    .optionalFieldOf("vars", Map.of()).forGetter(SkillRead::vars),
            Codec.STRING.listOf().optionalFieldOf("tags", List.of()).forGetter(SkillRead::tags),
            Require.CODEC.optionalFieldOf("require").forGetter(SkillRead::require),
            ResourceCost.CODEC.listOf().optionalFieldOf("cost").forGetter(SkillRead::cost)
    ).apply(i, SkillRead::new));

    /** 引用形态对应的完整 Codec（供 {@link SkillEntries} 注册表登记）。 */
    public static final Codec<SkillRead> CODEC = MAP_CODEC.codec();

    /** 分发标记：引用形态。 */
    @Override
    public SkillEntry.Type<SkillRead> getType() {
        return SkillEntries.READ;
    }

    /**
     * 从技能条目表解析本引用指向的完整技能定义（缺失则报错）。
     *
     * <p>引用链（{@code ref → ref → …}）递归展开：内层先解析为完整定义，再由本层 {@code vars} 覆盖——
     * <b>越靠外的实例值优先级越高</b>。随后应用可选覆盖：{@code tier}（来自 vars 的保留槽）、
     * {@code require} / {@code cost}（来自本引用的覆盖字段）。
     */
    @Override
    public Skill resolve() {
        SkillEntry target = LoadEvent.SKILL_ENTRY_MAP.get(this.ref);
        if (target == null) {
            throw new IllegalStateException("SkillRead 引用了未注册的 SkillEntry: " + this.ref);
        }
        Skill base = target.resolve().withParams(this.vars);
        // 实例覆盖等阶（vars 的保留槽，见 SkillEntry.TIER_VAR）
        ExpressionCodec.Value tierVar = this.vars.get(SkillEntry.TIER_VAR);
        if (tierVar != null) base = base.withTier(tierVar.getInt());
        // 实例覆盖需求 / 消耗（技能天然携带，stat 没有；read 形式单一，不引入新形态）
        if (require.isPresent()) base = base.withRequire(require.get());
        if (cost.isPresent()) base = base.withCost(cost.get());
        // Read 标签发射：被引用预设的 id path 作为技能标签随本技能全程携带。
        // 语义 = 「本技能属于该预设这一类」——如 ref=coe:generic_curse 的技能自动带 generic_curse 标签，
        // 使数据侧「对 [某预设] 类技能」的加成可命中；引用成链时逐层追加，形成完整谱系标签。
        // 再叠加本实例 tags（机制维度：banner/curse/aura/herald/minion/totem/mark/shapeshifted …）。
        List<String> emit = new ArrayList<>();
        emit.add(this.ref.getPath());
        emit.addAll(this.tags);
        return base.withTags(emit.toArray(String[]::new));
    }

    /** 定义期参数槽：转发被引用预设的有效参数（模板 params ⊕ 本引用 vars）。 */
    @Override
    public Map<String, ExpressionCodec.Value> params() {
        return resolve().params();
    }

    /** 替换参数：并入 {@code vars}（引用形态的参数槽就是 vars，不新建字段）。 */
    @Override
    public SkillEntry withParams(Map<String, ExpressionCodec.Value> newParams) {
        if (newParams == null || newParams.isEmpty()) return this;
        Map<String, ExpressionCodec.Value> merged = new HashMap<>(this.vars);
        merged.putAll(newParams);
        return new SkillRead(this.ref, merged, this.tags, this.require, this.cost);
    }

    /**
     * 替换等阶：写入 {@code vars} 的 {@link SkillEntry#TIER_VAR} 槽，<b>保住 ref 语义</b>。
     *
     * <p>不走 {@code toInline().withTier(...)} 降级——datagen 期 {@code SKILL_ENTRY_MAP}
     * 尚未装载，{@code resolve()} 必抛异常；而宝石注册（{@code Skill*Gen.reg}）正是在
     * datagen 期给每个条目挂等阶的。
     */
    @Override
    public SkillEntry withTier(int tier) {
        Map<String, ExpressionCodec.Value> merged = new HashMap<>(this.vars);
        merged.put(SkillEntry.TIER_VAR, ExpressionCodec.of(tier));
        return new SkillRead(this.ref, merged, this.tags, this.require, this.cost);
    }

    /**
     * 追加实例分类标签，<b>保住 ref 语义</b>（同 {@link #withTier} 的理由：datagen 期
     * 不可 resolve）。解析时与 Read 标签（ref path）一并发射到 {@link Skill#getStatTags()}。
     */
    @Override
    public SkillEntry withTags(String... extra) {
        if (extra == null || extra.length == 0) return this;
        List<String> merged = new ArrayList<>(this.tags);
        for (String t : extra) {
            if (!merged.contains(t)) merged.add(t);
        }
        return new SkillRead(this.ref, this.vars, List.copyOf(merged), this.require, this.cost);
    }

    /** 引用 → 内联形态降级：解析后返回完整技能定义（参数已并入其 params 槽）。 */
    @Override
    public SkillEntry toInline() {
        return resolve();
    }

    // ---------------------------------------------------------------- 流式构造辅助

    /** 覆盖本实例需求（require）。 */
    public SkillRead require(Require r) {
        return new SkillRead(this.ref, this.vars, this.tags, Optional.of(r), this.cost);
    }

    /** 覆盖本实例消耗（cost，可变参数）。 */
    public SkillRead cost(ResourceCost... c) {
        return new SkillRead(this.ref, this.vars, this.tags, this.require, Optional.of(List.of(c)));
    }

    /** 覆盖本实例消耗（cost，列表）。 */
    public SkillRead cost(List<ResourceCost> c) {
        return new SkillRead(this.ref, this.vars, this.tags, this.require, Optional.of(c));
    }

    /** 注入单个参数槽。 */
    public SkillRead vars(String key, ExpressionCodec.Value value) {
        Map<String, ExpressionCodec.Value> merged = new HashMap<>(this.vars);
        merged.put(key, value);
        return new SkillRead(this.ref, merged, this.tags, this.require, this.cost);
    }

    /** 构造引用形态（仅 ref，其余默认空）。 */
    public static SkillRead of(Identifier ref) {
        return new SkillRead(ref);
    }
}
