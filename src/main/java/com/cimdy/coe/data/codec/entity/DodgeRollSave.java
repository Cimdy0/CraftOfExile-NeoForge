package com.cimdy.coe.data.codec.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 翻滚运行时状态（服务端权威、落玩家附件、随死亡清空）。
 * <ul>
 *   <li>{@code rollingUntil}：翻滚动作结束的游戏时刻（{@code level.getGameTime()}），用于维持冲刺速度 + 判定“正在翻滚”。</li>
 *   <li>{@code cooldownUntil}：再次翻滚的冷却截止时刻，未到则拒绝发起。</li>
 *   <li>{@code dirX/dirZ}：翻滚方向单位向量（已含冲刺朝向），每 tick 用于重写水平速度。</li>
 *   <li>{@code recoveryUntil}：被击倒地后的“调整动作”窗口，期间不可再次翻滚（亦用于客户端禁止立即重奔）。</li>
 * </ul>
 * <p>注意：奔跑（Sprint）现为<b>原版 sprint</b>（由 client {@code SprintingMixin} 经翻滚键驱动），
 * 不再落本附件——翻滚与奔跑是两个独立机制，仅通过同一按键（按住=翻滚后接奔跑）联动。</p>
 */
public record DodgeRollSave(long rollingUntil, long cooldownUntil, double dirX, double dirZ,
                            long recoveryUntil) {
    public static final Codec<DodgeRollSave> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("rolling_until").forGetter(DodgeRollSave::rollingUntil),
            Codec.LONG.fieldOf("cooldown_until").forGetter(DodgeRollSave::cooldownUntil),
            Codec.DOUBLE.fieldOf("dir_x").forGetter(DodgeRollSave::dirX),
            Codec.DOUBLE.fieldOf("dir_z").forGetter(DodgeRollSave::dirZ),
            Codec.LONG.fieldOf("recovery_until").forGetter(DodgeRollSave::recoveryUntil)
    ).apply(instance, DodgeRollSave::new));

    public static final DodgeRollSave EMPTY = new DodgeRollSave(0L, 0L, 0.0, 0.0, 0L);

    /** 是否仍在翻滚窗口内。 */
    public boolean isRolling(long now) {
        return now < rollingUntil;
    }

    /** 是否仍在冷却中（不可再次翻滚）。 */
    public boolean isOnCooldown(long now) {
        return now < cooldownUntil;
    }

    /** 是否仍处于被击倒地后的调整动作窗口。 */
    public boolean isRecovering(long now) {
        return now < recoveryUntil;
    }
}
