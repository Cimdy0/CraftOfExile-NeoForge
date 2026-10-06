package com.cimdy.coe.data.codec.item.tiered;

import com.cimdy.coe.data.codec.item.mod.core.impl.CommonMod;
import com.mojang.serialization.Codec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.List;
import java.util.Locale;
import java.util.function.IntFunction;

/**
 * 族 × 阶模型中的<b>阶</b> —— 符文与精髓共用同一套阶语义，差异只在于「同一阶在两种载体上的叫法 / 数值」。
 *
 * <h2>形态：枚举 + {@link StringRepresentable}（{@code (name, id)}）</h2>
 * <ul>
 *   <li>{@link #getSerializedName()}：<b>落盘名</b>（{@code special / perfect / greater / normal / lesser}），
 *       与 {@code Camp} 同口径（{@link StringRepresentable#fromEnum} 即按此键编解码）；</li>
 *   <li>{@link #getId()}：<b>阶号（0–4）</b> —— {@code 0} 特殊、{@code 1} 完美、{@code 2} 高级、{@code 3} 普通、
 *       {@code 4} 次级，<b>号越小阶越高</b>（完美是 {@code T1}）。</li>
 * </ul>
 * 阶号即渲染标签：{@link #label()} = {@code T<阶号>}（{@code T1} 完美 / {@code T2} 高级 / {@code T3} 普通 /
 * {@code T4} 次级 / {@code T0} 特殊），与 {@code CommonMod} 的阶级渲染同法（{@code T<n>} 字面量 + 深灰着色）。
 *
 * <h2>存在理由（消除交叉重复）</h2>
 * 改造前 {@code RuneState} 与 {@code EssenceState} 各自重复实现了同一套阶派生逻辑：
 * id 前缀、模型路径 {@code item/<kind>/<key>_<family>_<kind>}、需求等级阶梯（0 / 15 / 35 / 60）。
 * 二者<b>语义相同、字面量不同</b>（普通阶符文叫 {@code base}、精髓叫 {@code normal}；
 * id 前缀符文首字母大写、精髓全小写），本枚举把这套派生收敛到一处，按 {@link Carrier} 取各自字面量。
 *
 * <h2>阶序</h2>
 * {@link #ASCENDING} 为建造者「依次设定、从低往高」的序列（{@code LESSER → NORMAL → GREATER → PERFECT}）；
 * {@link #SPECIAL} 是<b>单阶族</b>专用（只构建一次）：不参与升序序列。
 * 常量声明次序与 {@link #getId()} 一致（特殊 → 次级），故 {@code ASCENDING} 与阶号反向 ——
 * 需要「低阶在前」的次序时按 {@link #getId()} 倒排（见 {@code BonusFamily#tiers}）。
 *
 * <h2>需求等级为符文专有</h2>
 * {@link #levelReq()} 只对 {@link Carrier#RUNE} 有意义（次级=0 / 普通=15 / 高级=35 / 完美=60，特殊=0）；
 * 精髓<b>没有需求等级概念</b>，取 {@link Carrier#ESSENCE} 时由调用方忽略本值。
 */
public enum Tier implements StringRepresentable {

    /** 特殊阶（单阶族专用：腐化精髓 / 单阶符文）：阶号 {@code 0}。 */
    SPECIAL("special", 0),

    /** 完美阶（最高阶）：阶号 {@code 1}。 */
    PERFECT("perfect", 1),

    /** 高级阶：阶号 {@code 2}。 */
    GREATER("greater", 2),

    /** 普通阶：阶号 {@code 3}。 */
    NORMAL("normal", 3),

    /** 次级阶（最低阶）：阶号 {@code 4}。 */
    LESSER("lesser", 4);

    /** 阶号 → 阶（{@link #BY_ID} 连续、越界取零值）。 */
    public static final IntFunction<Tier> BY_ID =
            ByIdMap.continuous(Tier::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);

    /** 分发编解码：直接复用 {@link StringRepresentable#fromEnum}，落盘键即 {@link #getSerializedName()}。 */
    public static final Codec<Tier> CODEC = StringRepresentable.fromEnum(Tier::values);

    /** 升序序列（不含 {@link #SPECIAL}）：建造者按「构建次数」依次取用（低阶 → 高阶）。 */
    public static final List<Tier> ASCENDING = List.of(LESSER, NORMAL, GREATER, PERFECT);

    private final String name;
    private final int id;

    Tier(String name, int id) {
        this.name = name;
        this.id = id;
    }

    /** 阶号（{@code 0} 特殊 / {@code 1} 完美 / {@code 2} 高级 / {@code 3} 普通 / {@code 4} 次级），号越小阶越高。 */
    public int getId() {
        return this.id;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    /** 取升序序列中的第 {@code index} 阶（越界即报错）。 */
    public static Tier ascending(int index) {
        if (index < 0 || index >= ASCENDING.size()) {
            throw new IllegalArgumentException("阶序越界: " + index);
        }
        return ASCENDING.get(index);
    }

    // ── 派生 ────────────────────────────────────────────────────────────

    /**
     * 本阶在该载体下的<b>键名</b>（拼模型路径 / 阶名翻译键用）。
     * 普通阶符文为 {@code base}、精髓为 {@code normal} —— 同一阶、两种叫法，差异在此收敛。
     */
    public String key(Carrier carrier) {
        return switch (this) {
            case LESSER -> "lesser";
            case NORMAL -> carrier == Carrier.RUNE ? "base" : "normal";
            case GREATER -> "greater";
            case PERFECT -> "perfect";
            case SPECIAL -> carrier == Carrier.RUNE ? "base" : "corrupted";
        };
    }

    /**
     * 条目 id 前缀：符文首字母大写（{@code Lesser_} / {@code Greater_} / {@code Perfect_}），
     * 精髓全小写（{@code lesser_} / {@code greater_} / {@code perfect_}）；普通阶与特殊级无前缀。
     */
    public String idPrefix(Carrier carrier) {
        String key = key(carrier);
        if (this == NORMAL || this == SPECIAL) {
            return "";
        }
        return carrier == Carrier.RUNE
                ? key.substring(0, 1).toUpperCase(Locale.ROOT) + key.substring(1) + "_"
                : key + "_";
    }

    /**
     * 条目 id：{@code <前缀><族名（按载体大小写）>_<种类>}
     * （符文 {@code Lesser_Desert_Rune}、精髓 {@code lesser_body_essence}）。
     */
    public String id(Carrier carrier, String family) {
        return idPrefix(carrier) + carrier.familyCase(family) + "_" + carrier.itemKind();
    }

    /** 模型贴图路径：{@code item/<种类>/<key>_<族名小写>_<种类>}。 */
    public String modelTexture(Carrier carrier, String family) {
        String kind = carrier.itemKind();
        return "item/" + kind + "/" + key(carrier) + "_" + family.toLowerCase(Locale.ROOT) + "_" + kind;
    }

    /**
     * 阶渲染标签：{@code T<阶号>} —— {@code T1} 完美 / {@code T2} 高级 / {@code T3} 普通 / {@code T4} 次级 / {@code T0} 特殊。
     * 物品显示名由调用方拼成 {@code <T<阶号>> + <种类>（+ 精髓类名）}，与 {@code CommonMod} 的阶级渲染同法。
     */
    public String label() {
        return "T" + id;
    }

    /**
     * 需求等级阶梯（符文专有）：完美=60 / 高级=35 / 普通=15 / 次级=0，特殊级 0。
     * 精髓无需求等级概念，不应取用本值。
     */
    public int levelReq() {
        return switch (id) {
            case 1 -> 60;
            case 2 -> 35;
            case 3 -> 15;
            default -> 0;
        };
    }

    /**
     * 是否为「升阶级」（精髓语义）：次级 / 普通 / 高级作用于<b>魔法</b>物品（升稀有并必定加一词缀）；
     * 完美与特殊级作用于<b>稀有</b>物品（先移除一条随机词缀）。
     */
    public boolean upgrades() {
        return this == LESSER || this == NORMAL || this == GREATER;
    }

    // ── 载体：符文 / 精髓（两者数据模型同构，仅字面量与等级口径不同）────────────

    /**
     * 阶的载体种类。精髓<b>不属于</b> PoE2 的 Augment 增幅器大类，
     * 只是「族 × 阶 × 宿主分支加成」的数据模型与符文同构，故共用本枚举。
     */
    public enum Carrier {

        RUNE("rune", true),
        ESSENCE("essence", false);

        private final String itemKind;
        private final boolean familyCapitalized;

        Carrier(String itemKind, boolean familyCapitalized) {
            this.itemKind = itemKind;
            this.familyCapitalized = familyCapitalized;
        }

        /** 条目种类字面量（{@code rune} / {@code essence}）：拼 id 与模型路径用。 */
        public String itemKind() {
            return itemKind;
        }

        /** 族名在 id 中的大小写：符文保持首字母大写（{@code Desert}），精髓全小写（{@code body}）。 */
        public String familyCase(String family) {
            return familyCapitalized ? family : family.toLowerCase(Locale.ROOT);
        }
    }
}
