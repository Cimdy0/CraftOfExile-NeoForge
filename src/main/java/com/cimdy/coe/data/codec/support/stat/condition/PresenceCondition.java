package com.cimdy.coe.data.codec.support.stat.condition;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceCountValue;
import com.cimdy.coe.data.codec.support.meta.Camp;
import com.cimdy.coe.event.Event;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 存在范围节点（{@code type:"presence"}）：POE2 的「in your Presence」全域范围取实体。
 * <p>
 * 数据源与过滤口径<b>逐字镜像</b> {@code TargetSelector.resolvePresence*}：
 * {@link Event.State#presenceMembers()}（{@code EntityOperationEvent} 每 tick 维护）+ 阵营过滤
 * （复用 {@link PresenceCountValue.Relation#matches}）。
 * {@code of} 指定「谁的 Presence」（默认 A；可为 {@code chain} 换锚后的 {@code current}）。
 *
 * <pre>
 * {"type":"presence","camp":"enemy","of":"current"}
 * </pre>
 */
public record PresenceCondition(
        PresenceCountValue.Relation camp,
        Optional<ConditionAnchor> of
) implements StatCondition {

    public static final Codec<PresenceCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PresenceCountValue.Relation.CODEC.optionalFieldOf("camp", PresenceCountValue.Relation.ANY)
                    .forGetter(PresenceCondition::camp),
            ConditionAnchor.CODEC.optionalFieldOf("of").forGetter(PresenceCondition::of)
    ).apply(instance, PresenceCondition::new));

    @Override
    public List<LivingEntity> resolve(ConditionScope scope) {
        LivingEntity ofEntity = of == null ? null : of.map(a -> a.entity(scope)).orElse(null);
        LivingEntity tracker = ofEntity != null ? ofEntity : scope.owner();
        if (tracker == null) return List.of();
        Camp trackerCamp = tracker.getData(AttachmentRegistry.CAMP);
        Level level = tracker.level();
        Event.State state = Event.get(tracker);
        List<LivingEntity> out = new ArrayList<>();
        for (UUID id : state.presenceMembers()) {
            if (level.getEntity(id) instanceof LivingEntity le && le != tracker
                    && camp.matches(tracker, trackerCamp, le)) {
                out.add(le);
            }
        }
        return out;
    }

    /** 便捷构造：某实体 Presence 范围内按阵营取。 */
    public static PresenceCondition of(ConditionAnchor anchor, PresenceCountValue.Relation camp) {
        return new PresenceCondition(camp, Optional.ofNullable(anchor));
    }
}
