package com.cimdy.coe.data.codec.item.state;

import com.cimdy.coe.data.codec.item.flask.FlaskState;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

/**
 * 通用物品状态（{@link ModifiableState}）判别编解码器 —— 单一 {@code DataComponentType<ModifiableState>}
 * 容器组件（{@code MODIFIABLE_STATE}）的持久化 / 网络同步 Codec。
 *
 * <p>过去装备 / 药剂 / 护符各自挂在 {@code MODIFIABLE_STATE} / {@code FLASK_ITEM_STATE} / {@code CHARM_ITEM_STATE}
 * 三个组件，导致通用维度（词缀 / 品质 / 稀有度 / 物等）事实上只对装备生效。统一后三类状态共用
 * {@code MODIFIABLE_STATE}，本 Codec 按运行时子类型分派到对应子 Codec：</p>
 * <ul>
 *   <li>编码：按 {@code instanceof} 选择 {@code EquipmentState} / {@code FlaskState} / {@code CharmState} / {@code GemState} 的 Codec。</li>
 *   <li>解码：按必填字段互斥性依次尝试四子 Codec（装备必填 {@code markers}，药剂必填 {@code resource}，
 *       护符必填 {@code trigger}，宝石必填 {@code gem_tier}），首个成功即为结果——无新增判别字段、兼容既有数据格式。</li>
 * </ul>
 */
public final class ModifiableStateCodec {

    private ModifiableStateCodec() {
    }

    public static final Codec<ModifiableState> CODEC = Codec.of(ModifiableStateCodec::encode, ModifiableStateCodec::decode);

    private static <T> DataResult<T> encode(ModifiableState input, DynamicOps<T> ops, T prefix) {
        if (input instanceof EquipmentState e) {
            return EquipmentState.CODEC.encode(e, ops, prefix);
        }
        if (input instanceof FlaskState f) {
            return FlaskState.CODEC.encode(f, ops, prefix);
        }
        if (input instanceof CharmState c) {
            return CharmState.CODEC.encode(c, ops, prefix);
        }
        if (input instanceof GemState g) {
            return GemState.CODEC.encode(g, ops, prefix);
        }
        return DataResult.error(() -> "Unknown ModifiableState subtype: " + (input == null ? "null" : input.getClass().getName()));
    }

    private static <T> DataResult<Pair<ModifiableState, T>> decode(DynamicOps<T> ops, T input) {
        var eq = EquipmentState.CODEC.decode(ops, input);
        if (eq.result().isPresent()) {
            return eq.map(p -> p.mapFirst(s -> (ModifiableState) s));
        }
        var fl = FlaskState.CODEC.decode(ops, input);
        if (fl.result().isPresent()) {
            return fl.map(p -> p.mapFirst(s -> (ModifiableState) s));
        }
        var ch = CharmState.CODEC.decode(ops, input);
        if (ch.result().isPresent()) {
            return ch.map(p -> p.mapFirst(s -> (ModifiableState) s));
        }
        var ge = GemState.CODEC.decode(ops, input);
        if (ge.result().isPresent()) {
            return ge.map(p -> p.mapFirst(s -> (ModifiableState) s));
        }
        return DataResult.error(() -> "ModifiableState decode failed: none of Equipment/Flask/Charm/Gem codecs matched");
    }
}
