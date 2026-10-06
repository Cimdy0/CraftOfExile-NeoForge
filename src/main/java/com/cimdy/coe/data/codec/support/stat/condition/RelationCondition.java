package com.cimdy.coe.data.codec.support.stat.condition;

import com.cimdy.coe.data.codec.support.addition.AdditionData;
import com.cimdy.coe.data.codec.support.addition.AdditionKeys;
import com.cimdy.coe.entity.custom.skill.totem.SkillTotem;
import com.cimdy.coe.helper.OwnerHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Optional;

/**
 * 归属关系节点（{@code type:"relation"}）：按「谁的什么」取实体。
 * <p>
 * 归属扫描集中在本节点：64 格扫描 + {@code owner_uuid} /
 * {@link SkillTotem} / {@code IS_COMPANION} / {@code ALLY} 判据。
 *
 * <pre>
 * {"type":"relation","relation":"minion","of":"self"}
 * </pre>
 */
public record RelationCondition(
        Relation relation,
        Optional<ConditionAnchor> of
) implements StatCondition {

    /** 归属类别：随从 / 图腾 / 同伴 / 盟友 / 当前锚点的主人。 */
    public enum Relation implements StringRepresentable {
        MINION("minion"),
        TOTEM("totem"),
        COMPANION("companion"),
        ALLY("ally"),
        /** 当前锚点的所有者（"owned by" —— 由实体反查其主人）。 */
        OWNED_BY("owned_by");

        private final String name;

        Relation(String name) {
            this.name = name;
        }

        public static final Codec<Relation> CODEC = StringRepresentable.fromEnum(Relation::values);

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final Codec<RelationCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Relation.CODEC.fieldOf("relation").forGetter(RelationCondition::relation),
            ConditionAnchor.CODEC.optionalFieldOf("of").forGetter(RelationCondition::of)
    ).apply(instance, RelationCondition::new));

    @Override
    public List<LivingEntity> resolve(ConditionScope scope) {
        if (relation == null) return List.of();
        LivingEntity ofEntity = of == null ? null : of.map(a -> a.entity(scope)).orElse(null);
        LivingEntity holder = ofEntity != null ? ofEntity : scope.owner();
        if (holder == null) return List.of();
        return switch (relation) {
            case MINION -> resolveMinions(holder);
            case TOTEM -> resolveTotems(holder);
            case COMPANION -> resolveCompanions(holder);
            case ALLY -> resolveAllies(holder);
            case OWNED_BY -> {
                LivingEntity target = scope.anchor() != null ? scope.anchor()
                        : (ofEntity != null ? ofEntity : holder);
                Entity owner = target == null ? null : OwnerHelper.getOwner(target);
                yield owner instanceof LivingEntity le ? List.of(le) : List.of();
            }
        };
    }

    public static List<LivingEntity> resolveMinions(LivingEntity owner) {
        if (owner == null) return List.of();
        java.util.ArrayList<LivingEntity> out = new java.util.ArrayList<>();
        for (Entity e : owner.level().getEntities(owner, owner.getBoundingBox().inflate(64.0))) {
            if (e instanceof LivingEntity le && owner.equals(OwnerHelper.getOwner(le))) out.add(le);
        }
        return out;
    }

    public static List<LivingEntity> resolveTotems(LivingEntity owner) {
        if (owner == null) return List.of();
        java.util.ArrayList<LivingEntity> out = new java.util.ArrayList<>();
        for (Entity e : owner.level().getEntities(owner, owner.getBoundingBox().inflate(64.0))) {
            if (e instanceof SkillTotem totem && owner.equals(totem.getOwner())) out.add(totem);
        }
        return out;
    }

    public static List<LivingEntity> resolveCompanions(LivingEntity owner) {
        if (owner == null) return List.of();
        java.util.ArrayList<LivingEntity> out = new java.util.ArrayList<>();
        for (Entity e : owner.level().getEntities(owner, owner.getBoundingBox().inflate(64.0))) {
            if (e instanceof LivingEntity le
                    && AdditionData.getOrDefault(le, AdditionKeys.IS_COMPANION, 0.0) != 0.0
                    && owner.equals(OwnerHelper.getOwner(le))) out.add(le);
        }
        return out;
    }

    public static List<LivingEntity> resolveAllies(LivingEntity owner) {
        if (owner == null) return List.of();
        java.util.ArrayList<LivingEntity> out = new java.util.ArrayList<>();
        for (Entity e : owner.level().getEntities(owner, owner.getBoundingBox().inflate(64.0))) {
            if (e instanceof LivingEntity le && e != owner
                    && AdditionData.getOrDefault(le, AdditionKeys.ALLY, 0.0) != 0.0) out.add(le);
        }
        return out;
    }

    /** 便捷构造。 */
    public static RelationCondition of(ConditionAnchor anchor, Relation relation) {
        return new RelationCondition(relation, Optional.ofNullable(anchor));
    }
}
