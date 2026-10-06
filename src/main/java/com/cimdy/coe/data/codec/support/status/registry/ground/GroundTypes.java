package com.cimdy.coe.data.codec.support.status.registry.ground;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;

import com.cimdy.coe.data.helper.engine.ground.GroundFieldManager;

import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.status.StatusStatEffect;
import com.cimdy.coe.data.codec.support.status.def.StatusKind;
import com.cimdy.coe.data.codec.support.status.def.StatusType;

import java.util.List;
import java.util.Set;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * 地面类型描述符（数据驱动）：把 {@code ground_status} 字符串映射到地面的完整定义
 * （效果载荷 {@link GroundDef#stats()}、客户端渲染色、作用域门控、stat 作用域标签）。
 *
 * <p>地面不再固化为「几种存在性状态」——任意地面都由这里的数据定义其效果。
 * 原「地面存在性状态」静态注册已退役：地面的效果即 {@link StatEntry} 列表，
 * 由 {@code GroundFieldManager} 直接注入范围内实体的 {@code STAT_STORE}（无中间状态层）。
 *
 * <p><b>效果载荷（stats）已落地</b>：下方各类型的 {@code stats} 携带真实 {@link StatEntry} 列表——
 * jagged = 承伤增加（{@code coe:taken_damage} 增幅）、ignited = 点燃、shocked = 感电、chilled = 冰缓，
 * 由 {@code GroundFieldManager.applyGround} 每 5 tick 注入范围内实体的 {@code STAT_STORE}，
 * 引擎逐节点真正读取（R3 安全）。感电/冰缓地面的免疫按 {@code GroundTypes.def} 的标签匹配。
 */
public final class GroundTypes {

    /** 单种地面的完整定义。 */
    public record GroundDef(List<StatEntry> stats, int color, GroundTargeting targeting, Set<String> tags) {
        public static final GroundDef EMPTY =
                new GroundDef(List.of(), 0xFFAAAAAA, GroundTargeting.ENEMIES, Set.of());
    }

    /** 四元素地面族 id（与 {@code GateTag} 同名）。 */
    public static final String ON_JAGGED_GROUND = "on_jagged_ground";
    public static final String ON_IGNITED_GROUND = "on_ignited_ground";
    public static final String ON_SHOCKED_GROUND = "on_shocked_ground";
    public static final String ON_CHILLED_GROUND = "on_chilled_ground";

    /**
     * 按地面 id 取定义。未知 id 返回 {@link GroundDef#EMPTY}（灰、影响敌方、无标签、无效果）。
     * 各类型的 {@code stats} 携带真实效果（见下方分支）。
     */
    public static GroundDef def(String id) {
        return switch (id) {
            case ON_JAGGED_GROUND -> new GroundDef(
                    List.of(
                            ModifierStatEffect.entry(AttributeRegistry.TAKEN_DAMAGE, 0.2,
                                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                            // jagged_ground 状态作为真实效果载荷（非存在性标记层）：站在锯齿地面即获得该状态，
                            // 供 COND_ON_JAGGED_GROUND_STATUS 经 Status 节点读取（与 ignited/shocked/chilled 同构）。
                            StatusStatEffect.selfEntry(
                                    StatusDefs.JAGGED_GROUND,
                                    ExpressionCodec.of(1.0))),
                    0xFF9AA0A6, GroundTargeting.ENEMIES, Set.of("jagged_ground"));
            case ON_IGNITED_GROUND -> new GroundDef(
                    List.of(
                            StatusStatEffect.selfEntry(StatusDefs.IGNITE, ExpressionCodec.of(10.0)),
                            StatusStatEffect.selfEntry(StatusDefs.ON_IGNITED_GROUND, ExpressionCodec.of(1.0))),
                    0xFFE25822, GroundTargeting.ENEMIES, Set.of("ignited_ground"));
            case ON_SHOCKED_GROUND -> new GroundDef(
                    List.of(
                            StatusStatEffect.selfEntry(StatusDefs.SHOCK, ExpressionCodec.of(20.0)),
                            StatusStatEffect.selfEntry(StatusDefs.ON_SHOCKED_GROUND, ExpressionCodec.of(1.0))),
                    0xFF3FA0FF, GroundTargeting.ENEMIES, Set.of("shocked_ground"));
            case ON_CHILLED_GROUND -> new GroundDef(
                    List.of(
                            StatusStatEffect.selfEntry(StatusDefs.CHILL, ExpressionCodec.of(10.0)),
                            StatusStatEffect.selfEntry(StatusDefs.ON_CHILLED_GROUND, ExpressionCodec.of(1.0))),
                    0xFF6FD3E0, GroundTargeting.ENEMIES, Set.of("chilled_ground"));
            default -> GroundDef.EMPTY;
        };
    }

    private GroundTypes() {}
}
