package com.cimdy.coe.data.codec.item.tiered;

import com.cimdy.coe.data.codec.item.addition.AdditionType;
import com.cimdy.coe.data.codec.item.mod.identity.Location;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 族定义（一个 JSON 文件 = 一族）：<b>按「装备部位 × 词缀 × 落位」分键的分阶阶梯表</b>。
 *
 * <p>由符文（PoE2 <b>Augment 增幅器</b>大类）与精髓共用 —— 精髓<b>不属于</b>增幅器，
 * 只是数据模型（族 × 阶 × 宿主分支加成）与符文同构，故共用本组通用设施。</p>
 *
 * <p>族名（{@code family}）与条目 id <b>不入 JSON</b>：文件名即族名（{@code Desert_Rune.json → Desert}），
 * 各阶条目 id 由{@link Tier} 与载体拼接（符文 {@code Lesser_Desert_Rune}、精髓 {@code lesser_body_essence}）；
 * 名称与模型路径同样由 {@link Tier} 拼接（见 {@link Tier#id} / {@link Tier#modelTexture}）。</p>
 *
 * <h2>数据结构：{@code Map<AffixKey, List<TierBonus>>}</h2>
 * <ul>
 *   <li><b>键 {@link AffixKey}</b> = 装备部位 × 落位 —— <b>一族内只写一次</b>，
 *       其规范化字符串形态即 JSON 的 map 键（解决「复合对象当键无法序列化」的问题，
 *       见 {@link AffixKey}）；</li>
 *   <li><b>值 {@link TierBonus}</b> = 该位置上<b>一阶</b>的完整定义：这条阶上装什么词缀
 *       （{@link AdditionType}）、幅度多少、属于哪一阶。同位置同落位可以一格一阶地往下列。</li>
 * </ul>
 * 之所以是「键 + 阶梯列表」而不是「键/值一对一」或「把键塞进 {@code List<Pair<…>>}」：
 * 前者一个位置存不下多阶，后者会让键随每阶重复 —— 两者都等于把同一份身份抄写 N 遍。
 *
 * <pre>{@code
 * "ladders": {
 *   "armor~prefix": [
 *      { "type": "coe:add_max_health", "min": 55, "max": 64, "tier": "lesser" },
 *      { "type": "coe:add_max_health", "min": 85, "max": 99, "tier": "normal" } ],
 *   "*~-": [
 *      { "type": "coe:add_dexterity", "min": 6, "max": 6, "tier": "lesser" } ] } }
 * }</pre>
 *
 * <p><b>词缀在值里，所以「换词缀」与「换数值」是同一件事</b>：精髓第四阶把 {@code type}
 * 换成另一条词缀，符文第四阶只把 {@code min}/{@code max} 抬高 —— 两者都是「再列一格」，
 * 键不必新增、构造因此同构（这是词缀留在键里的做法做不到的）。</p>
 *
 * <p>落位（prefix / suffix）是<b>每条词缀自带</b>的维度（见 {@link Bonus}）—— 同族不同词缀
 * 可各占各的落位；符文无落位概念，键的第二段为 {@value AffixKey#NO_LOCATION}。</p>
 *
 * <h2>不涉及的两件事</h2>
 * <ul>
 *   <li><b>族名 / 条目 id</b>：由文件名与 {@link Tier} 派生，不进 JSON。</li>
 *   <li><b>装备部位如何求值</b>：部位是 {@link AffixKey} 的一个字段（对应 {@code BranchExpr} 口径的
 *       表达式），求值由运行侧 {@code BranchExpr.collect} 按宿主类型链完成。</li>
 * </ul>
 *
 * <h2>建造者：按部位逐阶，部位键与词缀各写一次</h2>
 * <pre>{@code
 * BonusFamily.builder()
 *     .branch("armor")
 *         .tier(bonus(ModAdditions.ADD_MAX_HEALTH, 55, 64))                     // LESSER
 *         .tier(bonus(ModAdditions.ADD_MAX_HEALTH, 85, 99))                     // NORMAL
 *         .tier(bonus(ModAdditions.MAX_HEALTH_INC, 100, 119, Location.PREFIX))  // PERFECT（换词缀）
 *     .branch("accessory")
 *         .tier(bonus(ModAdditions.ADD_MAX_HEALTH, 46, 55))
 *         .build();
 * }</pre>
 * 部位键只写一次，{@code tier(...)} 的<b>调用次序即阶序</b>（从低往高）；
 * 所有部位都只写一阶则为 {@link Tier#SPECIAL}（精髓腐化阶 / 符文单阶族）。
 * 无加成族（如 Masterwork 升级符文）由<b>不声明任何部位</b>表达（{@code builder().build()}）。
 * 声明侧不写 {@code tier} —— 阶由调用次序注入，避免「声明的阶与排列不一致」这类重复数据。
 *
 * @see AffixKey
 * @see TierBonus
 */
public record BonusFamily(Map<AffixKey, List<TierBonus>> ladders) {

    public static final Codec<BonusFamily> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(AffixKey.CODEC, TierBonus.CODEC.listOf())
                    .optionalFieldOf("ladders", Map.of())
                    .forGetter(BonusFamily::ladders)
    ).apply(instance, BonusFamily::new));

    public BonusFamily {
        // 归一化：保序（键按声明次序）+ 不可变值（族定义与状态均只读）。
        ladders = new LinkedHashMap<>(ladders);
        ladders.replaceAll((key, list) -> List.copyOf(list));
    }

    /**
     * 本族涉及的阶（<b>低阶在前</b>，{@link Tier#SPECIAL} 垫底）—— 由阶梯中出现的
     * {@link Tier} 推导，非声明字段。供加载侧按阶展开（一族 × 一阶 = 一条条目）。
     */
    public List<Tier> tiers() {
        Set<Tier> present = new LinkedHashSet<>();
        for (List<TierBonus> ladder : ladders.values()) {
            for (TierBonus bonus : ladder) {
                present.add(bonus.tier());
            }
        }
        List<Tier> out = new ArrayList<>(present);
        // 阶号越小阶越高，倒排即「低阶在前、特殊级（0）垫底」。
        out.sort(Comparator.comparingInt(Tier::getId).reversed());
        return List.copyOf(out);
    }

    /**
     * 解析出某一阶的<b>按装备部位分组</b>加成（{@code 部位表达式 → 该阶加成列表}），
     * 即运行侧存储 {@code RuneState} / {@code EssenceState} 的 {@code branches} 形态，
     * 供 {@code BranchExpr.collect} 按宿主类型链求值。该阶无数值的身份不出现在结果中。
     */
    public Map<String, List<Bonus>> resolve(Tier tier) {
        Map<String, List<Bonus>> out = new LinkedHashMap<>();
        for (Map.Entry<AffixKey, List<TierBonus>> e : ladders.entrySet()) {
            AffixKey key = e.getKey();
            for (TierBonus bonus : e.getValue()) {
                if (bonus.tier() == tier) {
                    out.computeIfAbsent(key.branch(), k -> new ArrayList<>())
                            .add(new Bonus(bonus.min(), bonus.max(), bonus.type(), key.location()));
                }
            }
        }
        return Collections.unmodifiableMap(out);
    }

    /** 由文件名取族名（identifier 安全的小写路径）：去 {@code .json} 与族后缀（{@code _Rune} / {@code _essence}）后整体小写；
     *  返回值是精髓/符文条目的身份路径（如 {@code abrasion}），须直接作为 {@link Identifier} 路径使用，故必须全小写 {@code [a-z0-9/._-]}；
     *  展示名另由 lang 键（{@code item.coe.essence.<path>}）提供，不在此处大写。 */
    public static String familyOfFileName(String fileName, String suffix) {
        String name = fileName;
        if (name.endsWith(".json")) {
            name = name.substring(0, name.length() - ".json".length());
        }
        if (name.toLowerCase(Locale.ROOT).endsWith(suffix.toLowerCase(Locale.ROOT))) {
            name = name.substring(0, name.length() - suffix.length());
        }
        return name.toLowerCase(Locale.ROOT);
    }

    // ── 建造者 ─────────────────────────────────────────────────────────────

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 一条加成的声明（尚未绑定阶 —— 阶由 {@code tier(...)} 的调用次序注入）。
     *
     * <p>{@code location} 为<b>落位</b>（精髓：{@link Location#PREFIX} / {@link Location#SUFFIX}；
     * 符文：{@link Optional#empty()} —— 无落位概念），随词缀逐条声明而非全族共用。</p>
     */
    public record Amount(AdditionType type, double min, double max, Optional<Location> location) {
    }

    /**
     * 以 {@link AdditionType} 声明一条加成（作者侧直接给 {@code ModAdditions} 常量，
     * 不写字符串 id —— 类型写错在编译期即暴露）—— <b>不给落位即 {@link Optional#empty()}（符文口吻）</b>。
     */
    public static Amount bonus(AdditionType type, double min, double max) {
        return new Amount(type, min, max, Optional.empty());
    }

    /** 声明一条<b>带落位</b>的加成（精髓口吻）：同族各词缀可各占各的落位。 */
    public static Amount bonus(AdditionType type, double min, double max, Location location) {
        return new Amount(type, min, max, Optional.of(location));
    }

    /** 族建造者：按部位逐阶（从低往高）。 */
    public static final class Builder {

        /** 部位键 → 该部位的<b>各阶层</b>（{@code tier(...)} 的调用次序；一个阶层 = 同阶并列的若干条加成）。 */
        private final Map<String, List<List<Amount>>> branches = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * 声明一个装备部位分支：<b>本部位各阶的加成仍要继续用 {@link BranchBuilder#tier} 逐阶给出</b>，
         * 部位键因此只写一次。返回 {@link BranchBuilder} 以便直接接 {@code tier(...)}。
         */
        public BranchBuilder branch(String key) {
            BranchBuilder branch = new BranchBuilder(this, key);
            branches.put(key, branch.ordered);
            return branch;
        }

        /**
         * 收尾：摊平为 {@code 位置键 → 阶梯}。
         *
         * <p>同一部位的每一「阶层」里可以有多条加成（同阶并列、装不同词缀的若干条），它们<b>共享同一个键
         * （部位 × 落位）</b>，各自成为阶梯上的一格；因此<b>各阶层的条数可以不同</b>，某层某词缀缺席
         * 即该阶段不含这一格。</p>
         *
         * <p><b>同一部位不同落位的加成会落在不同键上</b>（精髓一条族里既有前缀词缀也有后缀词缀时）：
         * 落位是键的一部分，键不能替它做主张。</p>
         */
        public BonusFamily build() {
            int tierCount = 0;
            for (List<List<Amount>> rungs : branches.values()) {
                tierCount = Math.max(tierCount, rungs.size());
            }
            boolean special = tierCount == 1;
            Map<AffixKey, List<TierBonus>> out = new LinkedHashMap<>();
            for (Map.Entry<String, List<List<Amount>>> e : branches.entrySet()) {
                List<List<Amount>> rungs = e.getValue();
                for (int i = 0; i < rungs.size(); i++) {
                    Tier tier = special ? Tier.SPECIAL : Tier.ascending(i);
                    for (Amount a : rungs.get(i)) {
                        AffixKey key = new AffixKey(e.getKey(), a.location());
                        out.computeIfAbsent(key, k -> new ArrayList<>())
                                .add(new TierBonus(a.type(), a.min(), a.max(), tier));
                    }
                }
            }
            return new BonusFamily(out);
        }
    }

    /**
     * 单个装备部位的建造者：{@code tier(...)} 的<b>调用次序即阶序</b>（第 1 次 = 最低阶）。
     *
     * <p>阶是<b>族的维度</b>而非部位的，故本builder不持有阶字段。</p>
     */
    public static final class BranchBuilder {

        private final Builder owner;
        private final String key;
        private final List<List<Amount>> ordered;

        private BranchBuilder(Builder owner, String key) {
            this.owner = owner;
            this.key = key;
            this.ordered = new ArrayList<>();
        }

        /** 本部位的<b>下一阶</b>加成（可并列多条）；无参调用表示该阶本部位无加成。 */
        public BranchBuilder tier(Amount... amounts) {
            ordered.add(List.of(amounts));
            return this;
        }

        /** 本部位的部位表达式（{@code BranchExpr} 键），供调试与断言。 */
        public String key() {
            return key;
        }

        /**
         * 接续声明<b>下一个</b>装备部位分支（部位键与各词缀各写一次，故分支之间可以直接连写）。
         * 部位是<b>同级的并列项</b>，阶才是本builder内部的东西，故此处不持有阶。
         */
        public BranchBuilder branch(String key) {
            return owner.branch(key);
        }

        /** 收尾（等价于 {@link Builder#build()}），使 {@code branch(…).tier(…).branch(…).build()} 可连写。 */
        public BonusFamily build() {
            return owner.build();
        }
    }
}
