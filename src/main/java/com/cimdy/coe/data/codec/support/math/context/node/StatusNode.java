package com.cimdy.coe.data.codec.support.math.context.node;

import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import net.minecraft.world.entity.LivingEntity;

/**
 * 状态域读值节点类别（节点分类）：作用于当前候选实体的状态 / 异常读值。
 * <p>
 * 在 {@link EntityNode} 之上收敛「交互对方（sourceEntity）解析」样板：
 * {@link #interactionOther(ValueScope)} 统一取得命中结算中的对侧实体
 * （攻击者作用域持受害者 / 受害者作用域持攻击者），取代各处 copy-paste 的
 * {@code scope.root().currentStatContext().sourceEntity()} 取数。
 * <p>
 * 家族成员：状态 / 药水效果 / 元素异常种类数 / 凋零层数 / 各类 ailment（status 子包）。
 */
public interface StatusNode extends EntityNode {
    /**
     * 交互对方实体（命中结算中的对侧）：攻击者作用域持受害者，受害者作用域持攻击者。
     * 非命中场景无 StatContext -> 返回 {@code null}（调用方据此回落 0 / 不生效）。
     */
    static LivingEntity interactionOther(ValueScope scope) {
        var sc = scope.root().currentStatContext();
        return sc != null ? sc.sourceEntity() : null;
    }
}
