package com.cimdy.coe.data.gen.talent;

import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.talent.effect.StatTalentEffect;
import com.cimdy.coe.data.codec.talent.effect.TalentEffect;
import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;
import com.cimdy.coe.data.gen.stat.StatGen;
import net.minecraft.resources.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 天赋节点 Stat 注入：唯一公开方法 {@link #stat(Map)} 直接引用 Gen 层 {@link StatIds} 条目，
 * 结合数值 Map 完成注入；所有具体效果条目均经 StatGen 生成（{@code EFF_*} + {@code StatIds}），
 * 不在此处内联构造 {@code Stat / ModifierStatEffect}。
 */
public final class TalentNodeGen {
    private TalentNodeGen() {
    }

    /**
     * 天赋树唯一的 Stat 注入入口：直接引用 Gen 层 {@link StatIds} 条目（{@code $value} 模板），
     * 结合数值 Map 注入实际幅度。每个条目经 {@link StatGen#ref} 解析为同一底层 concrete 条目，
     * 仅幅度不同——满足「所有 Ids 以 StatGen 形式生成、Stat 构建全部使用 StatIds 构建方法」的铁律。
     *
     * @param values {@link StatIds} 条目 id → 数值 的映射（如 {@code Map.of(StatIds.TO_STRENGTH, 5.0)}）
     */
    public static StatTalentEffect stat(Map<Identifier, Double> values) {
        List<StatEntry> entries = values.entrySet().stream()
                .map(e -> StatGen.ref(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        return StatTalentEffect.ofEntries(entries);
    }

    /**
     * 单 stat 带值授予：等价于 {@link #stat(Map)} 的 {@code Map.of(id, value)} 形式，
     * 但免去 {@code Map.of(...)} 样板，使生成的节点类更干净（见 poe2_assets/gen_talent_stats.py）。
     *
     * @param id    须引用的 {@link StatIds} 条目 id（{@code $value} 模板）
     * @param value 实际幅度
     */
    public static StatTalentEffect stat(Identifier id, double value) {
        return stat(Map.of(id, value));
    }

    /**
     * 无值授予：引用 {@link StatIds} 条目但<b>不注入幅度</b>，用于 Wire 已把 {@code amount} 烘死为常量的条目。
     *
     * <p>适用面极窄，误用会静默失效，务必先确认目标条目在 {@code Wire*.java} 中写了显式
     * {@code .amount("1.0")}（或别的常量表达式）：
     * <ul>
     *   <li><b>只能用</b>于常量 amount 条目——多数是「机制型 flag」（{@code 1.0} 表示该机制开/关，与数值大小无关），
     *       典型如 {@code ENERGY_SHIELD_DOES_NOT_RECHARGE}、{@code YOUR_MINIONS_ARE_GIGANTIC}。
     *       这类条目若再走 {@link #stat(Map)} 注入 {@code 1.0}，虽结果相同却是冗余；注入别的数值则被
     *       {@code StatRead} 的 {@code vars} 覆盖语义静默忽略（虚值），是必须消灭的 BUG。</li>
     *   <li><b>不能</b>用于 {@code $value} 模板条目：默认 amount 是 {@code MathParser.parse("$value")}，
     *       不注入时 {@code value} 取兜底 0 → 条目在运行期恒为 0 而完全失效——这正是本仓
     *       {@code StatusBehavior} 曾踩过的坑（写了 condition 却没喂幅度，表现为「条件成立但效果为零」）。
     *       模板条目请一律用 {@link #stat(Map)}。</li>
     * </ul>
     * 另注意：{@code StatEntry} 只有一个数值槽（见 {@code StatEntry} 的「唯一数值原则」），故 flag 与
     * 「同语料里的另一个数值」（如某秒数时间窗）不能共存于同一条目——时间窗需另开通道，勿塞进 {@code $value}。
     *
     * @param ids 需引用的 {@link StatIds} 条目 id（常量 amount 条目）
     */
    public static StatTalentEffect stat(Identifier... ids) {
        List<StatEntry> entries = Arrays.stream(ids).map(StatGen::ref).collect(Collectors.toList());
        return StatTalentEffect.ofEntries(entries);
    }

    /**
     * 普通属性节点（力量 / 智慧 / 敏捷 +5 三选一），links 为连接节点 id 列表。
     * 三选一项各自引用 Gen 层 {@link StatIds#TO_STRENGTH} / {@link StatIds#TO_INTELLIGENCE} /
     * {@link StatIds#TO_DEXTERITY} 条目（均 {@code $value} 模板，幅度 5.0），经 {@link #stat(Map)} 注入。
     */
    public static TalentNodeDef attributeNode(String id, float x, float y, List<String> links) {
        TalentEffect str = stat(Map.of(StatIds.TO_STRENGTH, 5.0));
        TalentEffect intel = stat(Map.of(StatIds.TO_INTELLIGENCE, 5.0));
        TalentEffect dex = stat(Map.of(StatIds.TO_DEXTERITY, 5.0));
        return TalentNodeDef.of(id, x, y, "attribute_node", "passives_attribute",
                List.of(), List.of(str, intel, dex), links);
    }
}
