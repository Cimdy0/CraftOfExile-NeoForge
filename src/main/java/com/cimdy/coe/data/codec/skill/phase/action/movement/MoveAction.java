package com.cimdy.coe.data.codec.skill.phase.action.movement;
import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionTypes;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * 移动动作 — 击退 / 拉扯 / 传送 / 冲刺。
 *
 * <p><b>主体按 {@link MoveType} 分离</b>（此前一律取 {@code target()}，而 ON_CAST 阶段
 * 不存在命中目标 ⇒ {@code target()} 恒为空 ⇒ DASH / TELEPORT 静默失效：已落盘的跃击 /
 * 闪电打击等位移技能的位移部分全部空转，只剩伤害）：
 * <ul>
 *   <li>{@code KNOCKBACK} / {@code PULL}：作用于<b>命中目标</b>（无目标即无意义，静默跳过）；</li>
 *   <li>{@code DASH} / {@code TELEPORT}：作用于<b>施法者自身</b>（不依赖命中目标）。</li>
 * </ul>
 *
 * <p>{@code force} 的语义随类型而定：KNOCKBACK / PULL / DASH 为速度标量；
 * <b>TELEPORT 为最大传送距离（格）</b> —— 真值（PoE2 Blink「Teleports (4–4.9) metres」）即距离上限，
 * 超出则钳制；此前 TELEPORT 完全不读 force，等于允许点击任意远的目标点无限位移。
 */
public record MoveAction(
        MoveType type,
        ExpressionCodec.Value force,
        Optional<Vec3> direction
) implements Action {
    public static final Codec<MoveAction> CODEC = RecordCodecBuilder.create(i -> i.group(
            MoveType.CODEC.fieldOf("move_type").forGetter(MoveAction::type),
            ExpressionCodec.VALUE_CODEC.fieldOf("force").forGetter(MoveAction::force),
            Vec3.CODEC.optionalFieldOf("direction").forGetter(MoveAction::direction)
    ).apply(i, MoveAction::new));

    @Override public Action.Type<?> getType() { return ActionTypes.MOVE; }

    @Override
    public void execute(ActionContext context) {
        var skillCtx = context.skill() != null ? context.skill().getContext() : null;
        float f = force.getFloat(skillCtx);
        Vec3 dir = resolveDirection(context);
        if (dir == null) return;
        switch (type) {
            case KNOCKBACK -> context.target().ifPresent(t -> t.push(dir.x * f, dir.y * f, dir.z * f));
            case PULL -> context.target().ifPresent(t -> t.push(-dir.x * f, -dir.y * f, -dir.z * f));
            case TELEPORT -> teleport(context, dir, f);
            case DASH -> {
                Entity caster = context.caster();
                if (caster != null) caster.push(dir.x * f, dir.y * f, dir.z * f);
            }
        }
    }

    /** 方向解析：数据显式指定 &gt; 上下文方向 &gt; 施法者视线；三者皆无则 null（本次不移动）。 */
    private Vec3 resolveDirection(ActionContext context) {
        Vec3 dir = direction.orElse(context.direction());
        if (dir == null || dir.lengthSqr() < 1e-9) {
            dir = context.caster() != null ? context.caster().getLookAngle() : null;
        }
        if (dir == null || dir.lengthSqr() < 1e-9) return null;
        return dir.normalize();
    }

    /**
     * 传送：向上下文目标点（命中点 / 瞄准点）位移，距离上限 {@code maxDistance} 格。
     * 目标点与施法者重合（如 ON_CAST 无瞄准点）时退化为「沿 {@code dir} 推进 {@code maxDistance}」。
     *
     * <p>真值（PoE2 Blink「tunnel through space」）：允许穿越实体与地形，但落点须<b>可站立</b>
     * （该格与其上方均无碰撞），否则自目标点向施法者方向逐格回退寻找安全落点；全程无解则不传送
     * —— 宁可不位移，也不把施法者塞进实心方块。
     */
    private void teleport(ActionContext context, Vec3 dir, float maxDistance) {
        Entity caster = context.caster();
        if (caster == null || maxDistance <= 0) return;
        Vec3 from = caster.position();
        Vec3 dest = context.position();
        Vec3 delta = dest == null ? Vec3.ZERO : dest.subtract(from);
        double dist = delta.length();
        if (dist < 1e-3) {
            delta = dir.scale(maxDistance);
            dist = maxDistance;
        }
        if (dist > maxDistance) delta = delta.scale(maxDistance / dist);
        dest = from.add(delta);

        Vec3 safe = safeLanding(caster, from, dest, dir);
        if (safe != null) caster.teleportTo(safe.x, safe.y, safe.z);
    }

    /** 自 {@code dest} 沿 {@code -dir} 每 0.5 格回退，返回首个「本格与上方均无碰撞」的落点；无解返回 null。 */
    private static Vec3 safeLanding(Entity entity, Vec3 from, Vec3 dest, Vec3 dir) {
        Level level = entity.level();
        int steps = (int) Math.ceil(dest.distanceTo(from) / 0.5);
        for (int i = 0; i <= steps; i++) {
            Vec3 p = dest.subtract(dir.scale(i * 0.5));
            BlockPos pos = BlockPos.containing(p);
            if (isFree(level, pos) && isFree(level, pos.above())) return p;
        }
        return null;
    }

    private static boolean isFree(Level level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    public enum MoveType implements StringRepresentable {
        KNOCKBACK("knockback"),
        PULL("pull"),
        TELEPORT("teleport"),
        DASH("dash");

        public static final Codec<MoveType> CODEC = StringRepresentable.fromEnum(MoveType::values);
        private final String name;

        MoveType(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }
}
