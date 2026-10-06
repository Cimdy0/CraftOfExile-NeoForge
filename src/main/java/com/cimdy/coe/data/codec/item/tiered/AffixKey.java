package com.cimdy.coe.data.codec.item.tiered;

import com.cimdy.coe.data.codec.item.mod.identity.Location;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Optional;

/**
 * 一条词缀栏目的<b>位置键</b>：<b>装备部位 × 落位</b>两段。
 *
 * <p>作为 {@code Map<AffixKey, List<TierBonus>>} 的键（见 {@link BonusFamily}）：位置只写一次，
 * 该位置上<b>每一阶装什么词缀、取多少数值</b>都挂在值里 —— 故本键<b>不持有词缀</b>。
 * 词缀挪进值后，「同一部位同一落位换一条词缀（精髓第四阶）」与「同一部位同一落位只换数值（符文）」
 * 是同一件事，<b>键的数目也不再随词缀数量膨胀</b>。</p>
 *
 * <h2>为什么本键要能变成字符串 —— JSON 的 map 键只能是字符串</h2>
 * JSON（{@code JsonOps}）的 {@code MapLike} 只给出 STRING 类型的键，故 {@code Codec.unboundedMap}
 * 的键 codec <b>必须写出一个字符串动态</b>；直接把本记录当键、用「对象字段」的 codec 去写，
 * 运行期会报「期望字符串」。解决办法是给键一个<b>规范化字符串形态</b>：
 *
 * <pre>{@code "<部位>~<落位>"    one_hand, !wand, !staff, !sceptre~prefix
 *                               无落位（符文）: *~-          }</pre>
 *
 * 两段以 {@value #SEPARATOR} 拼接：部位表达式（{@code armor} / {@code weapon, !wand, !staff} /
 * {@code *}…）只含 {@code , ! | *} 与字母，落位是 {@code prefix} / {@code suffix} /
 * {@value #NO_LOCATION}（无落位），该分隔符在两者中都不可能出现，故切分无歧义。
 *
 * @param branch   装备部位表达式（{@code BranchExpr} 口径，对宿主类型链求值）
 * @param location 落位；{@link Optional#empty()} = 无落位（符文）
 */
public record AffixKey(String branch, Optional<Location> location) {

    /** 键内部分隔符：部位 / 落位两段之间的唯一分隔符（两者均不含此字符）。 */
    public static final char SEPARATOR = '~';

    /** 无落位时的占位段（符文）。 */
    public static final String NO_LOCATION = "-";

    /**
     * 键的编解码：<b>字符串 ↔ 复合键</b>。
     *
     * <p>用 {@code Codec.STRING.comapFlatMap} 而非 {@code RecordCodecBuilder} —— 键不是
     * 「字段名 = 键值」的对象形态，而是<b>一条扁平字符串</b>，故走「解析 / 反向序列化」两条自定义通路。</p>
     */
    public static final Codec<AffixKey> CODEC = Codec.STRING.comapFlatMap(AffixKey::parse, AffixKey::encode);

    /** 规范化字符串：{@code 部位~落位}（无落位写 {@value #NO_LOCATION}）。 */
    public String encode() {
        return branch + SEPARATOR + location.map(Location::getName).orElse(NO_LOCATION);
    }

    /** 由规范化字符串还原键；段数 / 落位名任一不符即报错（fail-fast，不静默回落）。 */
    private static DataResult<AffixKey> parse(String encoded) {
        String[] parts = encoded.split(String.valueOf(SEPARATOR), 2);
        if (parts.length != 2) {
            return DataResult.error(() -> "键格式应为 <部位>~<落位>: " + encoded);
        }
        Optional<Location> location;
        try {
            location = locationOf(parts[1]);
        } catch (IllegalArgumentException e) {
            return DataResult.error(e::getMessage);
        }
        return DataResult.success(new AffixKey(parts[0], location));
    }

    /** 落位段 → 落位：{@value #NO_LOCATION}（或空）即无落位；其余按 {@link Location#getName()} 解，
     * <b>未知落位直接抛错</b>（不回落 {@link Location#PREFIX} —— 那会把「无落位」变成「有落位」）。 */
    private static Optional<Location> locationOf(String segment) {
        return switch (segment) {
            case "", NO_LOCATION -> Optional.empty();
            case "prefix" -> Optional.of(Location.PREFIX);
            case "suffix" -> Optional.of(Location.SUFFIX);
            default -> throw new IllegalArgumentException("键中的落位非法: " + segment);
        };
    }
}
