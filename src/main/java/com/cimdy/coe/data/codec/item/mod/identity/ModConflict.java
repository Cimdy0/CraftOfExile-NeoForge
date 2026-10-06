package com.cimdy.coe.data.codec.item.mod.identity;

import com.cimdy.coe.data.codec.item.mod.core.impl.AffixMod;
import com.cimdy.coe.data.codec.item.state.ModifiableState;
import com.cimdy.coe.data.codec.item.addition.AdditionRead;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据驱动的冲突表：每个冲突表 = 一个 JSON 文件，<b>文件名即组名</b>（{@code data/coe/coe/affix/conflict/<group>.json}），
 * 表体为「成员列表 + 可选部位约束」。成员即互斥的 Addition 身份（命名空间限定）。
 *
 * <h2>部位约束（{@link SlotExpr}）</h2>
 * 源库的词缀互斥是<b>按部位分别列出</b>的：同一对词缀可能「胸甲上互斥、头盔上可共存」。
 * 故一张表可自带 {@code slot} 谓词，<b>只在其成立的部位上生效</b>；缺省 {@link SlotExpr#ALWAYS}
 * 即旧的「全局表」行为。这是消除<b>过度约束</b>（在无关部位上错误互斥）的关键。
 *
 * <h2>缓存</h2>
 * 全部冲突表以「<b>表路径</b>」为键缓存于 {@link #TABLES}（{@link LinkedHashMap} 保证遍历顺序稳定）。
 *
 * <h2>冲突解析（{@link #resolveConflict}）</h2>
 * 给定种子与载体：
 * <ul>
 *   <li>种子本身是已注册表路径 → 直接返回；</li>
 *   <li>否则取「<b>包含该种子</b> 且 <b>部位谓词对本载体成立</b>」的表路径；</li>
 *   <li>无命中 → 返回种子自身（自成一表，仅与自身互斥）。</li>
 * </ul>
 * 载体为 {@code null}（无载体场景，如图鉴展示）时跳过部位判定，等价旧行为。
 *
 * <h2>多归属与过度约束（已知限制）</h2>
 * 一个 Addition <b>可以</b>同部位归属多张表：源库的互斥单位是「词缀条目」而非「stat」，
 * 不同条目可共享同一 stat（例：{@code 护甲与闪避提高%} 单体条目 与
 * {@code 护甲与闪避提高% + 眩晕门槛} 复合条目分属两族、源库可共存）。
 * 本 mod 以 stat 级 {@code Addition} 建模，二者塌缩成同一身份，故判定会<b>偏严</b>（过度约束）。
 *
 * <p>机制侧的正确性已保证：解析返回<b>全部</b>命中表（顺序无关）、判定取集合相交，故不会漏判。
 * 残留的过度约束是<b>数据粒度</b>问题，须待「源库 mod 文本 → Addition 组合」映射建立后，
 * 把表的成员从 stat 升级为条目（组合签名）方可根治。{@link #validate()} 列出共享成员供人工确认。
 */
public record ModConflict(List<Identifier> members, SlotExpr slot) {

    /** 表路径 → 冲突表（LinkedHashMap 保证遍历顺序稳定）。 */
    private static final Map<Identifier, ModConflict> TABLES = new LinkedHashMap<>();

    /**
     * 反向索引：<b>成员 → 所属表路径（按注册序）</b>。
     * 存在原因：一个 Addition 可能同时出现在多张表（源库中不同词缀条目共享同一 stat），
     * 此时「解析出唯一一张表」既不成立也无必要——解析改为返回<b>全部命中表</b>，判定改集合相交。
     */
    private static final Map<Identifier, List<Identifier>> MEMBER_INDEX = new LinkedHashMap<>();

    /**
     * 单表编解码：{@code members} 必备、{@code slot} 可选（缺省 {@link SlotExpr#ALWAYS}）。
     * 组名（表路径）由文件名提供，不进表体。
     */
    public static final Codec<ModConflict> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.listOf().fieldOf("members").forGetter(ModConflict::members),
            SlotExpr.CODEC.optionalFieldOf("slot", SlotExpr.ALWAYS).forGetter(ModConflict::slot)
    ).apply(instance, ModConflict::new));

    /** 便捷构造：无部位约束（全局表）。 */
    public static ModConflict of(List<Identifier> members) {
        return new ModConflict(List.copyOf(members), SlotExpr.ALWAYS);
    }

    /** 便捷构造：带部位约束。 */
    public static ModConflict of(List<Identifier> members, SlotExpr slot) {
        return new ModConflict(List.copyOf(members), slot);
    }

    /**
     * 便捷构造（作者侧首选）：直接以 {@link AdditionRead} 常量声明成员，内部取其 {@code ref}——
     * 免去逐项手写 {@code .ref()}，也就无从漏写或写错标识符。
     *
     * @param first 首个成员（必备，保证至少一项）
     * @param rest  其余成员
     */
    public static ModConflict of(AdditionRead first, AdditionRead... rest) {
        List<Identifier> members = new ArrayList<>(rest.length + 1);
        members.add(first.ref());
        for (AdditionRead addition : rest) {
            members.add(addition.ref());
        }
        return new ModConflict(List.copyOf(members), SlotExpr.ALWAYS);
    }

    /** 便捷构造（作者侧首选）：成员 + 部位约束。 */
    public static ModConflict of(SlotExpr slot, AdditionRead first, AdditionRead... rest) {
        List<Identifier> members = new ArrayList<>(rest.length + 1);
        members.add(first.ref());
        for (AdditionRead addition : rest) {
            members.add(addition.ref());
        }
        return new ModConflict(List.copyOf(members), slot);
    }

    /** 注册一个冲突表：以表路径为键，并维护 {@link #MEMBER_INDEX}。同名覆盖（先撤销旧成员索引）。 */
    public static void register(Identifier tablePath, ModConflict table) {
        ModConflict previous = TABLES.put(tablePath, table);
        if (previous != null) {
            for (Identifier member : previous.members()) {
                List<Identifier> owners = MEMBER_INDEX.get(member);
                if (owners == null) continue;
                owners.remove(tablePath);
                if (owners.isEmpty()) MEMBER_INDEX.remove(member);
            }
        }
        for (Identifier member : table.members()) {
            MEMBER_INDEX.computeIfAbsent(member, k -> new ArrayList<>()).add(tablePath);
        }
    }

    /**
     * 解析冲突目标表集合（部位感知，<b>权威入口</b>）。
     *
     * <p>返回<b>全部</b>「包含该种子 且 部位谓词对本载体成立」的表路径，而非单一一张：
     * 同一 Addition 在源库可属多个词缀族（不同条目共享 stat），强行选一张会<b>漏判</b>互斥。
     * 故判定下沉为「两词缀的目标集合是否相交」（见 {@code AffixMod#conflicts}）。
     *
     * <ul>
     *   <li>种子本身是已注册且本部位生效的表路径 → 含其自身；</li>
     *   <li>再并上经 {@link #MEMBER_INDEX} 查得、且本部位生效的所属表；</li>
     *   <li>全空 → 仅含种子自身（自成一表，只与同种子互斥）。</li>
     * </ul>
     * 结果与遍历顺序无关（{@link LinkedHashSet} 只用于可读的报错输出）。
     *
     * @param seed    种子（表路径 / AdditionType 路径）
     * @param carrier 载体（装备 / 药剂 / 护符）；{@code null} 表示跳过部位判定
     */
    public static Set<Identifier> resolveConflicts(Identifier seed, ModifiableState carrier) {
        Set<Identifier> hits = new LinkedHashSet<>();
        ModConflict self = TABLES.get(seed);
        if (self != null && (carrier == null || self.slot().matches(carrier))) {
            hits.add(seed);
        }
        for (Identifier tablePath : MEMBER_INDEX.getOrDefault(seed, List.of())) {
            ModConflict table = TABLES.get(tablePath);
            if (table != null && (carrier == null || table.slot().matches(carrier))) {
                hits.add(tablePath);
            }
        }
        if (hits.isEmpty()) {
            hits.add(seed);
        }
        return hits;
    }

    /** 解析冲突目标表集合（无载体：跳过部位判定，等价旧行为）。 */
    public static Set<Identifier> resolveConflicts(Identifier seed) {
        return resolveConflicts(seed, null);
    }

    /**
     * 解析冲突目标路径（部位感知）：取 {@link #resolveConflicts} 中<b>最先注册</b>的一张。
     * 仅用于展示（如图鉴打印组名）；互斥判定<b>不得</b>走此单值入口。
     */
    public static Identifier resolveConflict(Identifier seed, ModifiableState carrier) {
        return resolveConflicts(seed, carrier).iterator().next();
    }

    /** 解析冲突目标路径（无载体：跳过部位判定，等价旧行为）。 */
    public static Identifier resolveConflict(Identifier seed) {
        return resolveConflict(seed, null);
    }

    /**
     * 列出被多张表共享的成员（<b>不含部位维度</b>，即忽略 {@code slot} 后仍共享者）。
     * 这是「可能过度约束」的清单，供加载后人工确认，非致命。
     */
    public static List<String> validate() {
        Map<Identifier, List<Identifier>> owners = new HashMap<>();
        for (Map.Entry<Identifier, ModConflict> e : TABLES.entrySet()) {
            for (Identifier member : e.getValue().members()) {
                owners.computeIfAbsent(member, k -> new ArrayList<>()).add(e.getKey());
            }
        }
        List<String> problems = new ArrayList<>();
        for (Map.Entry<Identifier, List<Identifier>> e : owners.entrySet()) {
            if (e.getValue().size() > 1) {
                problems.add(e.getKey() + " -> " + e.getValue());
            }
        }
        return problems;
    }

    /** 重载前清空冲突表与反向索引（与 LoadEvent 其余 loader 同口径）。 */
    public static void clear() {
        TABLES.clear();
        MEMBER_INDEX.clear();
    }
}
