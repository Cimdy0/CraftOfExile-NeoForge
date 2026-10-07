package com.cimdy.coe.data.codec.skill.phase.delivery;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 投递方式 — 技能效果怎么到达目标。
 * 将原 Form 的"投递"和"效果"职责分离：
 * Delivery 只管怎么到达目标，Action 管产生什么效果。
 */
public interface Delivery {
    Codec<Map<Delivery.Type<?>, Delivery>> MAP_CODEC = Codec.dispatchedMap(
            DeliveryTypes.REGISTRY.byNameCodec(),
            Delivery.Type::codec);

    Codec<Delivery> CODEC = MAP_CODEC.xmap(
            map -> map.values().iterator().next(),
            expr -> Map.of(expr.getType(), expr));

    Type<?> getType();

    /**
     * 技能皮肤机制：返回把本投递「外观引用（render）」覆盖为给定技能 id 的副本。
     * 默认原样返回（自身/生成/范围等无实体形态的投递不覆写）。
     * 投射物 / 危害物 / 残片一类持有实体引用的投递会覆写其 {@code ref.render}。
     * 由 {@code Skill.withDeliveryPhase} 在递归重建阶段树时调用。
     */
    default Delivery withRender(Identifier skillId) { return this; }

    /**
     * 命中过滤条件 — 统一 math 表达式，经 {@code ExpressionContext} 求值；
     * 非零（≥1）即通过，点燃 ON_HIT。默认 1.0 表示不过滤（命中即生效）。
     * <p>
     * 替代旧 {@code List<HitTestType> hitTests}，将 is_entity / is_block / is_miss / distance
     * 等判定收编为 {@code hit_state:<mode>} Context 读值节点，多个条件用 {@code and} 组合。
     * <p>
     * JSON 示例：{@code "hit_test": "hit_state:is_entity >= 1"}、
     * {@code "hit_test": "hit_state:is_entity >= 1 and hit_state:distance <= 10"}。
     */
    default ExpressionCodec.Value hitTest() { return ExpressionCodec.of(1.0); }

    /**
     * 本投放形态自述的 stat 标签（投射物→DELIVERY_PROJECTILE、范围→DELIVERY_AREA …）。
     * 动作执行与伤害命中作用域均会并入这些标签，使「投射物伤害提高」「范围效果扩大」类
     * 带标签的天赋加成在对应投放形态的行为内即时生效。
     */
    default Set<String> statTags() { return Set.of(); }

    /**
     * 投射物/范围的生成原点覆盖。
     * 返回空时由 executor 回退到施法者眼睛位置（原行为）；
     * 返回非空时，其值解释为「沿发射方向前移的格数」，原点 = eye + direction * value。
     * <p>
     * 注：当前项目内无「表达式 → Vec3」设施，也无 Vec3 codec，
     * 故 origin 以标量（double）表达，由 executor 沿 direction 偏移求得实际 Vec3。
     */
    default Optional<ExpressionCodec.Value> origin() { return Optional.empty(); }

    /**
     * 递归深度安全上限（弹射 / 连锁 / 分裂 / 分裂叉 的复合递归）。
     * 仅作兜底安全上限，不影响任何合法的有限深度投递；默认 64 远大于任何合法使用。
     */
    default int maxDepth() { return 64; }

    record Type<T extends Delivery>(String id, Codec<T> codec) {}
}
