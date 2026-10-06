package com.cimdy.coe.data.codec.support.stat.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>多跳原语</b>（{@code type:"chain"}）：先把 {@code inner} 解析成集合，
 * 再对其中<b>每一个元素</b>把它作为新的当前锚点（换锚 + 入参集收敛为该元素），
 * 解析 {@code then} 并合并全部结果。
 * <p>
 * 这是「A → B（被杀实体）→ C（B 附近的敌人）」成立的原因：单跳只能得到 B，
 * 第二跳必须以 B 为锚点重新做实体获取，二者无法用一层集合运算表达。
 *
 * <p><b>递归</b>：{@code inner} 与 {@code then} 的类型同为 {@code Codec.recursive} 的自引用 {@code self}，
 * 故 {@code chain} 可任意层串联（A → B → C → …）。
 *
 * <p>换锚语义见 {@link ConditionScope#withAnchor}：锚点 = 元素、入参集 = {@code [元素]}、
 * 几何中心 = 元素位置（半径 / 存在范围类节点据此以「它」为圆心）。
 *
 * <pre>
 * {"type":"chain",
 *  "inner":{"type":"filter","of":{"type":"anchor","anchor":"killed"},
 *           "where":{"type":"ignited_by_source"}},
 *  "then":{"type":"radius","radius":2.5,"camp":"enemy","center":"current"}}
 * </pre>
 */
public record ChainCondition(
        StatCondition inner,
        StatCondition then
) implements StatCondition {

    public static Codec<ChainCondition> codec(Codec<StatCondition> self) {
        return RecordCodecBuilder.create(instance -> instance.group(
                self.fieldOf("inner").forGetter(ChainCondition::inner),
                self.fieldOf("then").forGetter(ChainCondition::then)
        ).apply(instance, ChainCondition::new));
    }

    @Override
    public List<LivingEntity> resolve(ConditionScope scope) {
        if (inner == null) return List.of();
        List<LivingEntity> first = inner.resolve(scope);
        if (first.isEmpty()) return List.of();
        if (then == null) return ConditionSets.dedupe(first);
        List<LivingEntity> out = new ArrayList<>();
        for (LivingEntity element : first) {
            if (element == null) continue;
            out.addAll(then.resolve(scope.withAnchor(element)));
        }
        return ConditionSets.dedupe(out);
    }

    /** 便捷构造：{@code inner → then} 一跳。 */
    public static ChainCondition of(StatCondition inner, StatCondition then) {
        return new ChainCondition(inner, then);
    }
}
