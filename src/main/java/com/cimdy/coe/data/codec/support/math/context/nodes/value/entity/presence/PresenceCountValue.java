package com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence;

import com.cimdy.coe.data.codec.support.math.context.nodes.meta.misc.PresenceDependent;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.EntityFlagValue;
import com.cimdy.coe.event.combat.SummonEvent;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.addition.AdditionData;
import com.cimdy.coe.data.codec.support.addition.AdditionKeys;
import com.cimdy.coe.data.codec.support.meta.Camp;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.EntityNode;
import com.cimdy.coe.entity.custom.skill.totem.SkillTotem;
import com.cimdy.coe.event.Event;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.Locale;

/**
 * Presence 范围内成员的<b>计数</b>读值节点：驱动「每个召唤物 / 图腾 / 敌人 +X」一族线性缩放词缀。
 *
 * <p><b>与既有 presence 节点的分工</b>：
 * <ul>
 *   <li>{@link PresenceValue} —— 单候选进/出判定（目标筛选）；</li>
 *   <li>{@link PresenceRarityValue} —— 按<b>稀有度</b>聚合（存在性 / 计数）；</li>
 *   <li>本节点 —— 按<b>阵营关系 + 实体类别</b>计数（ally 的 summon / totem、enemy 等）。</li>
 * </ul>
 * 三者共用 {@code Event.State.presenceMembers()}（当前在范围内的全部 UUID），互不替代。
 *
 * <p><b>阵营关系</b>（{@link Relation}）：{@code ANY} 不过滤；{@code ALLY} 要求与追踪者同阵营
 * <b>或</b>由追踪者拥有（{@link OwnableEntity}）——后者兜住召唤物未分配到 Camp 的情形；
 * {@code ENEMY} 要求阵营非 {@link Camp#NONE} 且异于追踪者、且非追踪者所有。
 * 追踪者自身阵营为 {@code NONE} 时 ALLY / ENEMY 无定义，直接返回 0（与
 * {@link PresenceRarityValue} 的 {@code enemiesOnly} 口径一致）。
 *
 * <p><b>实体类别</b>（{@link Kind}）：{@code ANY} 不过滤；{@code SUMMONED} 按 {@code coe:summoned}
 * 持久化键（与 {@link EntityFlagValue.Flag#SUMMONED} 同一判据）；{@code TOTEM} 按 {@link SkillTotem} 类型。
 *
 * <p>实现 {@link PresenceDependent}，故 {@code EventRefCollector} 扫到本节点即自动开启
 * Presence 全域扫描；漏实现会导致 {@code presenceMembers()} 恒空、节点静默恒返 0。
 *
 * <p><b>中缀</b>：{@code presence_count:<relation>.<kind>[.distinct]}（如 {@code presence_count:ally.totem}）。
 * 第三段 {@code .distinct} 为可选：置位后按<b>实体类型去重</b>计「种类数」而非「成员个数」
 * （如 {@code presence_count:ally.persistent_minion.distinct} = 场上不同类型的持久召唤物有几种）。
 */
public record PresenceCountValue(Relation relation, String kind, boolean distinctType) implements EntityNode, PresenceDependent {

    /** 兼容二参构造（去重置为 false）——既有 JSON 与调用点不受影响。 */
    public PresenceCountValue(Relation relation, String kind) {
        this(relation, kind, false);
    }

    public static final Codec<PresenceCountValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Relation.CODEC.optionalFieldOf("relation", Relation.ANY).forGetter(PresenceCountValue::relation),
            Codec.STRING.optionalFieldOf("kind", "any").forGetter(PresenceCountValue::kind),
            Codec.BOOL.optionalFieldOf("distinct_type", false).forGetter(PresenceCountValue::distinctType)
    ).apply(instance, PresenceCountValue::new));

    /** 阵营关系：相对追踪者的关系过滤。 */
    public enum Relation implements StringRepresentable {
        ANY("any"), ALLY("ally"), ENEMY("enemy");

        private final String name;

        Relation(String name) {
            this.name = name;
        }

        /** 成员 {@code member} 是否满足本关系（{@code tracker} 为词缀持有者）。 */
        public boolean matches(LivingEntity tracker, Camp trackerCamp, LivingEntity member) {
            Camp c = member.getData(AttachmentRegistry.CAMP);
            boolean owned = member instanceof OwnableEntity own && own.getOwner() == tracker;
            return switch (this) {
                case ANY -> true;
                case ALLY -> owned || (c != Camp.NONE && c == trackerCamp);
                case ENEMY -> !owned && c != Camp.NONE && c != trackerCamp;
            };
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        /** 中缀解析用：未知名回落 {@link #ANY}（与其余节点的「未知 → 缺省」口径一致）。 */
        public static Relation fromName(String n) {
            for (Relation v : values()) if (v.name.equals(n)) return v;
            return ANY;
        }

        public static final Codec<Relation> CODEC = StringRepresentable.fromEnum(Relation::values);
    }

    /**
     * 实体类别（开放 String）：召唤物 / 图腾 / 盟友 / 持久召唤物 / 不限。
     * 新增召唤类别时<b>无需改枚举</b>——直接传约定好的字符串（或后续扩展的注册表键）即可，
     * {@link #matches} 依据已知类别名分派，未知名回落「不匹配」。
     */
    public static final String ANY = "any";
    public static final String SUMMONED = "summoned";
    public static final String TOTEM = "totem";
    public static final String COMPANION = "companion";
    public static final String PERSISTENT_MINION = "persistent_minion";

    /** 与 {@link EntityFlagValue} 共用同一常量，避免两处私有副本漂移。 */
    private static final String SUMMON_KEY = AdditionKeys.SUMMONED;

    /** 类别判定（原 {@code Kind.matches}）：开放 String，未知类别名回落「不匹配」。 */
    public boolean matches(LivingEntity member) {
        return switch (kind) {
            case ANY -> true;
            case SUMMONED -> AdditionData.getOrDefault(member, AdditionKeys.SUMMONED, 0.0) != 0.0;
            case TOTEM -> member instanceof SkillTotem;
            // 原 AttachmentRegistry.IS_COMPANION 已迁入 AdditionData（key=is_companion）
            case COMPANION -> AdditionData.getOrDefault(member, AdditionKeys.IS_COMPANION, 0.0) != 0.0;
            // 持久召唤物：判据与 SummonEvent.isPersistentMinion 同源（召唤键存在 且 无过期时限）。
            // 注意 SUMMONED 与 PERSISTENT_MINION 是<b>子集</b>关系（持久 ⊂ 召唤），与 COMPANION（独立标记）不同。
            case PERSISTENT_MINION -> AdditionData.getOrDefault(member, AdditionKeys.SUMMONED, 0.0) != 0.0
                    && member.getData(AttachmentRegistry.MINION_EXPIRE) == 0L;
            default -> false;
        };
    }

    @Override
    public double evaluate(LivingEntity living, ValueScope scope) {
        // 追踪者 = Presence 记录的属主（词缀持有者自身），与 PresenceValue / PresenceRarityValue 同一口径
        if (!(scope.root().getEntity() instanceof LivingEntity tracker)) return 0D;
        Camp trackerCamp = tracker.getData(AttachmentRegistry.CAMP);
        if (relation != Relation.ANY && trackerCamp == Camp.NONE) return 0D;

        Level level = tracker.level();
        Event.State state = Event.get(tracker);
        int count = 0;
        // distinctType：按实体类型去重后的「种类数」而非「成员个数」，
        // 驱动「for each different type of X」一族（如每有一种不同类型的持久召唤物 +10%）。
        // 不用事件的 distinct 原语——那是窗口内的事件历史去重，而持久召唤物是<b>常驻状态</b>，
        // 窗口必过期归零；此处按实时 presence 集合聚合才是正确口径。
        Set<EntityType<?>> seenTypes = distinctType ? new HashSet<>() : null;
        for (UUID id : state.presenceMembers()) {
            if (!(level.getEntity(id) instanceof LivingEntity member) || member == tracker) continue;
            if (!relation.matches(tracker, trackerCamp, member)) continue;
            if (!matches(member)) continue;
            if (seenTypes == null) count++;
            else if (seenTypes.add(member.getType())) count++;
        }
        return count;
    }

    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(Component.empty()
                .append(StatValueKey.of("presence_count.rel", relation.getSerializedName()))
                .append(Component.literal(" "))
                .append(StatValueKey.of("presence_count.kind", kind))
                .append(Component.literal(" "))
                .append(StatValueKey.of("presence_count.measure", distinctType ? "distinct" : "count")));
    }
}
