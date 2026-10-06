package com.cimdy.coe.data.codec.support.math.context.node;

import com.cimdy.coe.data.codec.support.math.context.nodes.scope.SelectorNode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 通用「获取实体」查询 — Context 数据基站的实体侧拆分。
 * <p>
 * 「获取范围内实体 → 直接返回数值」被拆为两段：
 * <ol>
 *   <li><b>获取实体</b>（本类）：形状扫描（box / sphere / beam / world）+ 过滤 + 排序 + 数量限定，
 *       只负责回答「哪些实体」，全库（战斗 / 村庄 / 要塞 / 天赋磁吸等）共用同一份实现；</li>
 *   <li><b>获取数值</b>（{@link com.cimdy.coe.data.codec.support.math.context.node.ValueExpr} 读值节点 /
 *       各消费端自身逻辑）：在选出的实体上读属性、点燃 ON_HIT、施加效果、吸落物品等，由各需求方自行决定。</li>
 * </ol>
 * 本类对实体类型泛型化（{@code <T extends Entity>}）：战斗目标（{@link LivingEntity}）、掉落物
 * （{@code ItemEntity}）、动物（{@code Sheep}/{@code Cow}/{@code Animal}）、经验球（{@code ExperienceOrb}）
 * 等全部检测需求共用同一套写法，不再为每种对象各写一份 {@code getEntitiesOfClass + 内联谓词 + 手工最近排序} 样板。
 * <p>
 * 所有系统统一经 {@code ExpressionContext.queryEntities(query)} 回调 Context，
 * Context 解析 level 后执行本查询并把实体集合二次分发回调用方；消费端不再各自手写扫描样板。
 * <p>
 * 用法（链式，惰性描述，{@link #collect(Level)} / {@link #first(Level)} 时才执行）：
 * <pre>{@code
 * // 战斗：LivingEntity 便捷工厂
 * ExpressionContext.of(level, caster).queryEntities(
 *         EntityQuery.sphere(center, radius).exclude(caster).aliveOnly());
 * // 村庄 / 要塞：任意实体类型工厂（Class<T> 重载）
 * ExpressionContext.of(level, mob).queryEntity(
 *         EntityQuery.box(Sheep.class, area).filter(Sheep::readyForShearing)
 *                 .nearestTo(Vec3.atCenterOf(mob.blockPosition())));
 * }</pre>
 */
public final class EntityQuery<T extends Entity> {
    /** 全域扫描包围盒（与原 SelectorNode 全域扫描一致）。 */
    private static final double WORLD_SIZE = 6.0E7;

    private final Class<T> entityClass;
    private final AABB box;
    private Predicate<T> filter = e -> true;
    private Comparator<T> sort = null;
    private int limit = Integer.MAX_VALUE;

    private EntityQuery(Class<T> entityClass, AABB box) {
        this.entityClass = entityClass;
        this.box = box;
    }

    // ---- 形状工厂（获取实体的几何来源）----

    /** LivingEntity 矩形包围盒扫描（便捷，等价于 {@link #box(Class, AABB)} 传入 LivingEntity.class）。 */
    public static EntityQuery<LivingEntity> box(AABB box) {
        return box(LivingEntity.class, box);
    }

    /** 任意实体类型矩形包围盒扫描。 */
    public static <T extends Entity> EntityQuery<T> box(Class<T> type, AABB box) {
        return new EntityQuery<>(type, box);
    }

    /** LivingEntity 球形扫描：以 center 为球心、radius 为半径（包围盒粗筛 + 球面距离精筛）。 */
    public static EntityQuery<LivingEntity> sphere(Vec3 center, double radius) {
        return sphere(LivingEntity.class, center, radius);
    }

    /** 任意实体类型球形扫描。 */
    public static <T extends Entity> EntityQuery<T> sphere(Class<T> type, Vec3 center, double radius) {
        return new EntityQuery<>(type, AABB.ofSize(center, radius * 2, radius * 2, radius * 2))
                .filter(e -> e.position().distanceTo(center) <= radius);
    }

    /**
     * LivingEntity 光束线段扫描：沿 origin → origin + dir * range 的线段、width 宽度圆柱内的实体
     * （候选实体包围盒中心到线段的投影距离 ≤ 半宽 + 实体半宽）。
     */
    public static EntityQuery<LivingEntity> beam(Vec3 origin, Vec3 direction, double range, double width) {
        return beam(LivingEntity.class, origin, direction, range, width);
    }

    /** 任意实体类型光束线段扫描。 */
    public static <T extends Entity> EntityQuery<T> beam(Class<T> type, Vec3 origin, Vec3 direction, double range, double width) {
        Vec3 dir = direction.normalize();
        double half = width / 2.0;
        AABB box = AABB.ofSize(origin.add(dir.scale(range / 2)),
                range + width, range + width, range + width);
        return new EntityQuery<>(type, box).filter(ent -> {
            AABB bb = ent.getBoundingBox();
            Vec3 p = bb.getCenter();
            double t = p.subtract(origin).dot(dir);
            if (t < 0 || t > range) return false;
            Vec3 proj = origin.add(dir.scale(t));
            return p.distanceTo(proj) <= half + Math.max(bb.getXsize(), bb.getZsize()) * 0.5;
        });
    }

    /** 全域扫描（无空间限制；SelectorNode 语义）。 */
    public static EntityQuery<LivingEntity> world() {
        return world(LivingEntity.class);
    }

    /** 任意实体类型全域扫描。 */
    public static <T extends Entity> EntityQuery<T> world(Class<T> type) {
        return new EntityQuery<>(type, AABB.ofSize(Vec3.ZERO, WORLD_SIZE, WORLD_SIZE, WORLD_SIZE));
    }

    // ---- 过滤 / 排序 / 数量（AND 叠加，链式）----

    /** 叠加过滤条件（AND 语义，可多次调用）。 */
    public EntityQuery<T> filter(Predicate<T> predicate) {
        Predicate<T> prev = this.filter;
        this.filter = e -> prev.test(e) && predicate.test(e);
        return this;
    }

    /** 排除指定实体（null 安全，直接忽略）。 */
    public EntityQuery<T> exclude(Entity entity) {
        if (entity != null) filter(e -> e != entity);
        return this;
    }

    /** 仅保留存活实体（非 LivingEntity 类型的实体一律放行）。 */
    public EntityQuery<T> aliveOnly() {
        return filter(e -> !(e instanceof LivingEntity l) || l.isAlive());
    }

    /** 按距 anchor 由近到远排序（配合 {@link #first} 即「就近索敌」）。 */
    public EntityQuery<T> nearestTo(Vec3 anchor) {
        return sort(Comparator.comparingDouble((T e) -> e.position().distanceToSqr(anchor)));
    }

    /** 自定义排序。 */
    public EntityQuery<T> sort(Comparator<T> comparator) {
        this.sort = comparator;
        return this;
    }

    /** 截取排序后前 N 个。 */
    public EntityQuery<T> limit(int n) {
        this.limit = n;
        return this;
    }

    // ---- 执行 ----

    /** 在给定 level 上执行查询，返回可变列表。 */
    public List<T> collect(Level level) {
        List<T> result = new ArrayList<>(
                level.getEntitiesOfClass(entityClass, box, filter::test));
        if (sort != null) result.sort(sort);
        if (limit < result.size()) return new ArrayList<>(result.subList(0, limit));
        return result;
    }

    /** 执行查询并取第一个（排序后即「最优」，如 nearestTo 的最近者）。 */
    public Optional<T> first(Level level) {
        List<T> result = collect(level);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
}
