package com.cimdy.coe.data.helper.impl.aura;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.status.StatusValue;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.meta.Camp;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.cimdy.coe.data.codec.support.status.runtime.StatusInstance;
import com.cimdy.coe.data.codec.support.status.def.StatusKind;
import com.cimdy.coe.data.codec.support.status.def.StatusType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * Mark（标记）的施加与读取引擎：遵循「技能型动态附加 Status」裁定，Mark 不再以独立附件存储，
 * 而是以 {@link StatusKind#MARK} 类状态经 {@link StatusEngine} 施加到目标实体的 StatusStore，
 * 由引擎统一 tick / 过期 / 真读（R3 安全，非死线）。
 *
 * <p>施放时由 Mark 技能对周围敌方实体施加 Mark 状态（source = 标记者、duration = 技能给定），
 * 标记的<b>收益</b>（「你标记的敌人承受更多伤害 / 被致盲附近敌人 / 无法暴击 …」）由施法者侧
 * {@code ENEMIES_YOU_MARK_*} 族 Stat 经 {@code onTarget(COND_PRESENCE_MARK_STATUS)} 在「目标身上持有本状态」时
 * 落地——{@code StatusValue} 解析 {@code mark} 读取 StatusStore 即激活，施法者属性经 {@code cond_on_target}
 * 的 value 实体（stat 持有者）真读。状态来源（source）由 {@link StatusInstance} 记录，供 {@link #isMarkedBy} 的来源匹配。
 */
public final class MarkHelper {

    /** Mark 状态 id（动态注册，运行时按需注册，幂等）。 */
    public static final String MARK_ID = "mark";
    /** Mark 状态分类标签（供 Stat 作用域构建）。 */
    public static final String MARK_TAG = "mark";

    private MarkHelper() {
    }

    /** 对施法者周围 {@code radius} 格内<b>敌方</b>实体施加 Mark 状态（新覆盖旧，含 source）。 */
    public static void applyMarkToNearby(LivingEntity caster, double radius, int durationTicks) {
        Camp ownerCamp = caster.getData(AttachmentRegistry.CAMP);
        if (ownerCamp == Camp.NONE) return;
        Level level = caster.level();
        StatusType mark = StatusDefs.MARK;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class,
                caster.getBoundingBox().inflate(radius))) {
            if (e == caster) continue;
            Camp c = e.getData(AttachmentRegistry.CAMP);
            if (c != Camp.NONE && c != ownerCamp) {
                StatusEngine.apply(e, mark, caster, 1.0, durationTicks, 1, true);
            }
        }
    }

    /** 目标是否持有 Mark 状态（用于 {@code COND_PRESENCE_MARK_STATUS}）。 */
    public static boolean isMarked(LivingEntity marked, long gameTime) {
        return StatusType.byId(MARK_ID)
                .map(t -> StatusEngine.has(marked, t))
                .orElse(false);
    }

    /** 目标是否被指定来源持有有效期内 Mark 状态（用于 {@link com.cimdy.coe.data.helper.impl.aura.MarkBlindAura} 的来源匹配）。 */
    public static boolean isMarkedBy(LivingEntity marked, UUID source, long gameTime) {
        return StatusType.byId(MARK_ID)
                .map(t -> {
                    StatusInstance m = StatusEngine.query(marked, t);
                    return m != null && m.source != null && m.source.equals(source);
                })
                .orElse(false);
    }
}
