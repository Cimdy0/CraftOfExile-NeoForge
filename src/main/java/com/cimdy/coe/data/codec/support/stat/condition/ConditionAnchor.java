package com.cimdy.coe.data.codec.support.stat.condition;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * 命名锚点：条件解析中「站在谁身上」的命名实体引用。
 * <p>
 * Target 的两个基本项由<b>事件上下文</b>决定 —— KILL 事件传入（你，被杀实体）、HIT 事件传入（你，被命中实体）——
 * 故本枚举即为该上下文的具名投影：
 * <ul>
 *   <li>{@link #SELF} —— A，「你」（stat 持有者）；</li>
 *   <li>{@link #TRIGGER} —— B，事件的另一方（命中结算中互持的「交互对方」）；</li>
 *   <li>{@link #KILLED} —— 本次<b>被杀</b>实体（事件名含 kill/death 时解析，否则空集）；</li>
 *   <li>{@link #HIT} —— 本次<b>被命中</b>实体（事件名含 hit 时解析，否则空集）；</li>
 *   <li>{@link #EVENT} —— 本次事件对象（不判事件名，KILL/HIT 均解析）；</li>
 *   <li>{@link #SOURCE} —— 对方（相对当前锚点：站在 B 上时取 A，否则取 B / 事件对象）；</li>
 *   <li>{@link #CURRENT} —— 当前锚点（{@code chain} 换锚后的元素；未换锚时回落事件对象 → B → A）。</li>
 * </ul>
 *
 * <p>解析结果恒为 0/1 元素集：未绑定即空集 = 不满足（调用方无需额外布尔层）。
 */
public enum ConditionAnchor implements StringRepresentable {
    SELF("self"),
    TRIGGER("trigger"),
    KILLED("killed"),
    HIT("hit"),
    EVENT("event"),
    SOURCE("source"),
    CURRENT("current");

    private final String name;

    ConditionAnchor(String name) {
        this.name = name;
    }

    public static final Codec<ConditionAnchor> CODEC = StringRepresentable.fromEnum(ConditionAnchor::values);

    @Override
    public String getSerializedName() {
        return name;
    }

    /** 解析为 0/1 元素集（未绑定 → 空集）。 */
    public List<LivingEntity> resolve(ConditionScope scope) {
        LivingEntity e = entity(scope);
        return e == null ? List.of() : List.of(e);
    }

    /** 解析为单个实体（未绑定 → null）。 */
    @Nullable
    public LivingEntity entity(ConditionScope scope) {
        if (scope == null) return null;
        return switch (this) {
            case SELF -> scope.owner();
            case TRIGGER -> scope.triggerTarget();
            case KILLED -> isKillEvent(scope) ? scope.eventEntity() : null;
            case HIT -> isHitEvent(scope) ? scope.eventEntity() : null;
            case EVENT -> scope.eventEntity();
            case SOURCE -> {
                // 相对当前锚点取「对方」：站在非 A 的实体上 → 对方即 A；否则对方即 B（回落事件对象）
                LivingEntity anchor = scope.anchor();
                if (anchor != null && anchor != scope.owner()) yield scope.owner();
                yield scope.triggerTarget() != null ? scope.triggerTarget() : scope.eventEntity();
            }
            case CURRENT -> {
                if (scope.anchor() != null) yield scope.anchor();
                if (scope.eventEntity() != null) yield scope.eventEntity();
                if (scope.triggerTarget() != null) yield scope.triggerTarget();
                yield scope.owner();
            }
        };
    }

    private static boolean isKillEvent(ConditionScope scope) {
        String n = scope.eventName();
        if (n == null) return false;
        String lower = n.toLowerCase(java.util.Locale.ROOT);
        return lower.contains("kill") || lower.contains("death");
    }

    private static boolean isHitEvent(ConditionScope scope) {
        String n = scope.eventName();
        if (n == null) return false;
        return n.toLowerCase(java.util.Locale.ROOT).contains("hit");
    }
}
