package com.cimdy.coe.data.codec.skill.core;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import java.util.Map;

/**
 * <b>技能条目接口</b>——「一个技能」的两种可互换写法。
 *
 * <h2>模型</h2>
 * <ul>
 *   <li>{@link Skill}：<b>内联形态 = 一个完整的技能定义</b>（require / cast / cost / phases /
 *       modifiers / tags … 全部平铺）。注册后得到一个 id，该 id 即一个「预设」。</li>
 *   <li>{@link SkillRead}：<b>引用形态</b>——{@code ref} 指向某个完整技能定义（预设），
 *       {@code vars} 注入该预设声明的参数槽（{@code $reserve} / {@code $radius} …）。
 *       「具体技能 = 预设 + 实例参数」，与 Stat 侧 {@code StatRead(ref, vars)} 精确同构。</li>
 * </ul>
 *
 * <h2>为什么不存在「技能类型注册表」</h2>
 * 技能类型是<b>分类维度</b>（由 {@code tags} 字符串标签 + {@code GateTag} 层级表承载，可交叉），
 * 不是一种形态。所谓「预设」本身就是一个<b>完整的技能</b>——通用战旗 =
 * 「精魂保留 + 范围投递 + Stat 效果」的一份完整 Skill 数据，其中保留值 / 范围 / 效果写作参数槽；
 * 具体战旗 = {@code SkillRead(ref=coe:generic_banner, vars={reserve:30, radius:4.0}) }。
 * <b>不为「类型」注册多个 read 形态</b>——那只是把枚举换个地方硬编码，毫无意义。
 *
 * <h2>JSON 形态</h2>
 * <ul>
 *   <li>显式派发：{@code {"entry":"read","ref":"coe:generic_banner","vars":{...}}} /
 *       {@code {"entry":"inline", ...完整技能字段...}}；</li>
 *   <li>回退派发：顶层无 {@code entry} 的历史 JSON（既有 246 个 gem 数据）按内联形态解析。</li>
 * </ul>
 * 派发键为 {@code entry}（与 Stat 侧同一取舍，避 {@code type} 冲突）。
 */
public interface SkillEntry {

    /**
     * 分发式 MapCodec：按 {@code entry} 字段（注册表名字符串）派发到 {@link SkillEntries}
     * 中登记的 {@link Skill}（内联）或 {@link SkillRead}（引用）。
     *
     * <p>必须用 {@link Codec#STRING} 做纯字符串派发（同 {@code StatEntries} 的理由）：
     * 注册表 {@code byNameCodec} 在 RegistryOps 下会把 {@code entry} 值当 holder 引用解析，
     * 而该 Forge 注册表不在 lookup 中，会报 {@code Unregistered holder}。
     */
    MapCodec<SkillEntry> MAP_CODEC = Codec.STRING.dispatchMap("entry",
            e -> SkillEntries.nameOf(e.getType()),
            SkillEntries::codecFor);

    /**
     * 完整 Codec：显式 {@code entry} 派发优先，缺失时回退内联形态。
     *
     * 它们顶层从无 {@code entry} 键；回退分支使其零迁移继续解析，而新建数据一律走显式派发。
     */
    // second 分支（Skill.CODEC）必须惰性解析：Skill 实现了带 default 方法的本接口，
    // 其类初始化会回头触发本接口的初始化，二者静态求值顺序互锁——直接引用会在初始化中途
    // 取到尚未赋值的 null（表现为编码时 NPE "this.second is null"）。延迟到真正编解码时
    // 再解析，彼时 Skill 已完成初始化。
    Codec<SkillEntry> CODEC = Codec.either(MAP_CODEC.codec(), Codec.lazyInitialized(() -> Skill.CODEC))
            .xmap(e -> e.map(s -> (SkillEntry) s, s -> (SkillEntry) s),
                    se -> se instanceof SkillRead read ? Either.left(read) : Either.right((Skill) se));

    /** 分发类型：每个形态在 {@link SkillEntries} 注册表登记自己的 Codec。 */
    record Type<T extends SkillEntry>(MapCodec<T> codec) {
    }

    // ---------------------------------------------------------------- 形态契约

    /**
     * <b>解析为完整可用技能</b>（唯一消费入口）。
     * <ul>
     *   <li>{@link Skill}：自身即为完整定义，返回 {@code this}；</li>
     *   <li>{@link SkillRead}：取被引用预设的完整定义，叠加本引用 {@code vars} 后返回。</li>
     * </ul>
     * 引用可成链（{@code ref → ref → …}）；目标缺失<b>直接抛异常</b>，不静默兜底。
     */
    Skill resolve();

    /**
     * 定义期参数槽：{@code $name} 变量在本技能全部 {@link ExpressionCodec.Value}
     * （cost 数值 / 范围半径 / 时长 / 倍率 …）中的默认值。
     * 由 {@link SkillRead#vars} 注入覆盖（实例值 &gt; 模板默认值）。缺省空 Map。
     */
    Map<String, ExpressionCodec.Value> params();

    /**
     * 有效参数 = 定义 params ⊕ 实例覆盖层（{@link SkillRead} 的 vars），实例值优先。
     * 默认实现即 {@link #params()}（内联形态无覆盖层）；{@link SkillRead} 覆写为
     * 「解析后模板 params ⊕ vars」。
     */
    default Map<String, ExpressionCodec.Value> effectiveParams() {
        return params();
    }

    /** 返回叠加参数后的副本：{@link Skill} 并入 params，{@link SkillRead} 并入 vars（保住 ref 语义）。 */
    SkillEntry withParams(Map<String, ExpressionCodec.Value> newParams);

    /**
     * 等阶（未切割宝石按等阶筛选用，不影响运行时结算）。
     * 内联形态由 record 访问器提供；引用形态转发解析结果（可被 {@code vars} 的
     * {@value #TIER_VAR} 槽覆盖）。
     */
    default int tier() {
        return resolve().tier();
    }

    /**
     * 返回带指定等阶的副本：{@link Skill} 改字段；{@link SkillRead} 写入 {@code vars} 的
     * {@value #TIER_VAR} 槽——<b>不降级为内联</b>，从而在 datagen 期（预设表尚未装载、
     * {@code resolve()} 不可用）也能给引用型条目挂等阶。
     */
    SkillEntry withTier(int tier);

    /**
     * 返回追加分类标签后的副本（<b>分类维度是一等公民</b>）。
     *
     * <p>{@link Skill} 并入自身 {@code tags} 字段；{@link SkillRead} 并入自身
     * {@code tags} 字段（<b>不降级为内联</b>——datagen 期 {@code resolve()} 不可用）。
     * 引用形态最终标签 = 预设 tags ⊕ 预设 id path（Read 标签）⊕ 本引用实例 tags。
     *
     * <p>为何 tags 在引用形态是<b>平铺字段</b>而非 {@link #TIER_VAR} 那样的 vars 槽：
     * vars 承载的是 {@link ExpressionCodec.Value} 数值参数槽，装不下字符串标签；
     * 且分类系统（多维度可交叉标签）是模型的一等维度，与 {@link Skill#tags()} 同名同义平铺，
     * 而 tier 只是一个数值筛选参数，理应走参数通道。
     */
    SkillEntry withTags(String... extra);

    /**
     * 等阶在引用形态中的承载槽名。{@link SkillRead} 只有 {@code (ref, vars)} 两个字段
     * （与 {@code StatRead} 精确同构，不为个别字段新开通道），故「实例覆盖等阶」写入 vars
     * 的这个保留槽，由 {@link SkillRead#resolve()} 在解析时应用到 {@link Skill#withTier}。
     * <p>注意：这是<b>字段名</b>而非分类枚举，不违反「分类维度禁硬编码」原则。
     */
    String TIER_VAR = "tier";

    /** 内联形态副本（引用→完整定义降级）；内联形态返回 {@code this}。 */
    default SkillEntry toInline() {
        return this;
    }

    /** 分发标记：决定 JSON 经 {@link SkillEntries} 派发到哪个形态。 */
    Type<?> getType();
}
