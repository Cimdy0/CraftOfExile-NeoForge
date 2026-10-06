package com.cimdy.coe.data.codec.support.math.context.query;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;

/**
 * 统一候选抽象（方案四）：选择 / 求值层面向 Candidate 编程，与具体候选域解耦。
 * <p>
 * 当前仅实体域（{@link EntityCandidate}）；未来新增物品 / 方块等候选域时，
 * 只需为该域提供一个 Candidate 适配器，{@link Selector} 管线、{@link ValueScope} 指针交换、
 * 位置类读值节点（distance 等）全部自动复用，无需复制选择/过滤/排序/输出样板。
 */
public interface Candidate {
    /** 候选空间位置（距离 / 范围类读值节点使用）。 */
    Vec3 position();

    /** 实体域视图：候选为生物实体时返回之，否则 null（实体读值节点据此判空兜底 0）。 */
    default LivingEntity living() {
        return null;
    }

    /** 域原始值（实体域 = LivingEntity；物品域 = ItemStack 等，扩展 / 调试用）。 */
    Object value();

    /** 实体域适配。 */
    static Candidate of(LivingEntity entity) {
        return new EntityCandidate(entity);
    }

    /** 实体域候选：LivingEntity 的 Candidate 视图。 */
    record EntityCandidate(LivingEntity entity) implements Candidate {
        @Override
        public Vec3 position() {
            return entity.position();
        }

        @Override
        public LivingEntity living() {
            return entity;
        }

        @Override
        public Object value() {
            return entity;
        }
    }
}
