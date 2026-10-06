package com.cimdy.coe.data.codec.entity.behavior;

import com.cimdy.coe.data.codec.entity.EntityDef;

import com.cimdy.coe.data.codec.entity.render.EntityRenderLayers;
import com.cimdy.coe.data.codec.entity.trigger.EntityTriggers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;
import com.cimdy.coe.entity.custom.skill.SkillEntity;

/**
 * 内置运动组件实现 —— 与 {@code EntityTriggers} / {@code EntityRenderLayers} 同构：
 * 新增一种运动 = 加一个注册实现 + 数据包写一条，绝不硬编码进实体类的 {@code tick()}。
 *
 * <p>全部实现只经 {@link EntityRuntime} 的运动原语工作，不接触宿主实体类。
 */
public final class EntityMotions {

    private EntityMotions() {
    }

    /**
     * 静止（{@code coe:static}）—— 位置恒定，AABB 随作用半径缩放。
     *
     * <p>残片 / 危害物 / <b>球状驻场物件（Orb）</b> 都用它：它们被「放置」在世界的某点，
     * 在存活期内原地生效，自身不移动 —— 与「飞行」是两种运动，不是同一种的两个参数。
     */
    public record Static() implements EntityMotion {
        public static final Static INSTANCE = new Static();
        public static final Codec<Static> CODEC = MapCodec.unit(INSTANCE).codec();

        @Override
        public EntityMotion.Type<?> getType() {
            return EntityMotionTypes.STATIC;
        }

        @Override
        public boolean move(EntityRuntime rt) {
            return false;
        }

        /** 静止形态：AABB 直径 = 作用半径 ×2（危害物/球的可见范围即其作用范围）。 */
        @Override
        public float width(EntityRuntime rt) {
            return rt.radius() * 2.0F;
        }

        /** 静止形态：粒子按作用范围铺开（让玩家看见「这一片都危险」）。 */
        @Override
        public float visualRadius(EntityRuntime rt) {
            return rt.radius();
        }
    }

    /**
     * 墙体（{@code coe:wall}）—— 静止但<b>开启真实碰撞</b>的驻场建筑实体。
     *
     * <p>冰墙 / 火墙 / 盾墙本质是「有碰撞、能阻挡技能与移动的建筑」，独立于
     * 「地面云（static + contact）」与「直接冰冻（status）」的第三种机制。本组件声明
     * {@link #physics()} 返回 {@code true}，使 {@link com.cimdy.coe.entity.custom.skill.SkillEntity#configure}
     * 将实体置为 {@code noPhysics=false}；配合 {@link com.cimdy.coe.entity.custom.skill.SkillEntity#isPickable()}
     * 对墙体返回 {@code true}，其它实体在每 tick 的 {@code pushEntities} 中会被本实体的包围盒推开 ——
     * 即「阻挡」语义的真正落地点。
     *
     * <p>位置恒静止（{@code move()} 返回 false）；AABB 随作用半径铺开（同 {@code static}），
     * 高度取实体形态数据（{@code box_height}），故墙是被「放置」的立方体建筑而非飞行物。
     */
    public record Wall() implements EntityMotion {
        public static final Wall INSTANCE = new Wall();
        public static final Codec<Wall> CODEC = MapCodec.unit(INSTANCE).codec();

        @Override
        public EntityMotion.Type<?> getType() {
            return EntityMotionTypes.WALL;
        }

        /** 墙体需要真实碰撞：令宿主开启 noPhysics=false，使其包围盒能推开其它实体。 */
        @Override
        public boolean physics() {
            return true;
        }

        @Override
        public boolean move(EntityRuntime rt) {
            return false;
        }

        @Override
        public float width(EntityRuntime rt) {
            return rt.radius() * 2.0F;
        }

        @Override
        public float visualRadius(EntityRuntime rt) {
            return rt.radius();
        }
    }

    /**
     * 抛射（{@code coe:ballistic}）—— 每 tick 施加重力，<b>只与方块碰撞</b>（实体一律穿过）。
     *
     * <p>投掷物（手雷）与飞行球共用本组件，二者差别<b>不在运动</b>而在行为
     * （{@code detonate} 落地引爆 vs {@code pulse} 边飞边放能）以及撞块后的处置：
     * <ul>
     *   <li>{@code discard_on_block=true}：撞到方块即消散（球：撞墙就没了）；</li>
     *   <li>{@code discard_on_block=false}：撞到方块即停住，交给行为继续处理（手雷：落地 → 引信）。</li>
     * </ul>
     *
     * <p><b>需要宿主开启物理</b>：{@link #physics()} 返回 {@code true}（由组件声明，
     * 而非宿主 {@code instanceof} 判定）—— 弹体必须真实解析碰撞，否则穿墙。
     *
     * @param gravity        每 tick 下坠速度（0 = 平飞，如电球）
     * @param discardOnBlock 撞到方块时是否直接消散（false = 停住，由行为接手）
     */
    public record Ballistic(float gravity, boolean discardOnBlock) implements EntityMotion {
        public static final Codec<Ballistic> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.FLOAT.optionalFieldOf("gravity", 0.0F).forGetter(Ballistic::gravity),
                Codec.BOOL.optionalFieldOf("discard_on_block", true).forGetter(Ballistic::discardOnBlock)
        ).apply(i, Ballistic::new));

        @Override
        public EntityMotion.Type<?> getType() {
            return EntityMotionTypes.BALLISTIC;
        }

        @Override
        public boolean physics() {
            return true;
        }

        @Override
        public boolean move(EntityRuntime rt) {
            if (rt.motionStopped()) return false;
            Vec3 v = rt.velocity();
            rt.velocity(new Vec3(v.x, v.y - gravity, v.z));
            // SELF 只解析方块碰撞：实体一律穿过（球「本体不命中」、手雷不被半空小怪挡下）
            rt.integrate();
            if (rt.blockCollided()) {
                if (discardOnBlock) return true;
                rt.motionStopped(true);
                rt.velocity(Vec3.ZERO);
            }
            return false;
        }

        /**
         * 飞行弹体：AABB 取自身尺寸（{@code box_height} 见方）。
         *
         * <p><b>不能按作用半径铺开</b>：手雷的引爆半径远大于它的身体（2.5m vs 0.25m），
         * 若 AABB 按半径算，弹体在飞行途中就会被「身体」卡住，落地判定完全失真。
         */
        @Override
        public float width(EntityRuntime rt) {
            return rt.boxHeight();
        }

        /** 飞行中只画本体拖尾；停住后放大到作用半径（引信期的「要炸了」预告）。 */
        @Override
        public float visualRadius(EntityRuntime rt) {
            return rt.motionStopped() ? rt.radius() * 0.6F : 0.3F;
        }
    }

    /**
     * 外部驱动（{@code coe:external}）—— 本层不介入运动。
     *
     * <p>用于<b>不由 {@code EntityDef} 行为体系驱动</b>的载体：原版箭矢（{@code coe:skill_arrow}）
     * 的运动与命中由原版 {@code AbstractArrow} + 投递的 {@code FlightPattern} 负责。
     * 显式声明「运动在外部」而不是留空，是为了让数据自解释 —— 读数据的人不必猜
     * 「是不是忘了配」。
     */
    public record External() implements EntityMotion {
        public static final External INSTANCE = new External();
        public static final Codec<External> CODEC = MapCodec.unit(INSTANCE).codec();

        @Override
        public EntityMotion.Type<?> getType() {
            return EntityMotionTypes.EXTERNAL;
        }

        @Override
        public boolean move(EntityRuntime rt) {
            return false;
        }

        @Override
        public float width(EntityRuntime rt) {
            return rt.boxHeight();
        }

        @Override
        public float visualRadius(EntityRuntime rt) {
            return rt.radius();
        }
    }
}
