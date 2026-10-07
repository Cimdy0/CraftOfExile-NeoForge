package com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * 投射物子形态枚举 —— 把飞行命中 / 落地引爆 / 边飞边脉冲 / 原地脉冲 四种子形态在<b>同一份记录</b>上以 {@code kind} 轴区分（D4丙 对称归并）。
 *
 * <p>四个子形态共用同一维护路径（同一实体引用 + 同一执行器 {@link ProjectileDeliveryExecutor}），
 * 仅在运行时按 {@code kind} 分流到各自的行为分支（投射物飞行命中 / 投掷物落地引爆 / 球边飞边脉冲 / 球原地脉冲）。
 */
public enum ProjectileKind implements StringRepresentable {
    PROJECTILE, THROWN, BALL, ORB;

    public static final Codec<ProjectileKind> CODEC = StringRepresentable.fromEnum(ProjectileKind::values);

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
