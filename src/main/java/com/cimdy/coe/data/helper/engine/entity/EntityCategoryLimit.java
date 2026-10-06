package com.cimdy.coe.data.helper.engine.entity;

import com.cimdy.coe.data.codec.skill.phase.action.movement.SpawnAction;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.support.addition.AdditionData;
import com.cimdy.coe.data.codec.support.addition.AdditionKeys;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.EntityFlagValue;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect;
import com.cimdy.coe.entity.EntityDefCarrier;
import com.cimdy.coe.registry.world.EntityRegistry;
import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.cimdy.coe.helper.OwnerHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * <b>按实体类别的数量上限</b>（通用设施）。
 *
 * <p>PoE2 里「上限」是一个反复出现的维度：图腾 / 箭塔 / 陷阱 / 球 / 同伴 / 临时召唤物 / 裂隙 …
 * 本设施把它做成一条<b>与具体类别无关</b>的通用链路：
 * <ol>
 *   <li><b>登记</b>：类别 id → 上限属性（{@link #register}），新增一类只需一行；</li>
 *   <li><b>归类</b>：实体 → 类别（{@link #categoryOf}，形态 id 优先、实体类型 key 回落），
 *       并允许把若干类别<b>并入</b>同一池（{@code aliases}，图腾族同池即此机制）；</li>
 *   <li><b>取值</b>：{@link #limit} = 类别登记属性 + {@link com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect}
 *       标记（按类别标签折叠），二者皆无 → {@link #UNLIMITED}（未登记类别零行为变化）；</li>
 *   <li><b>计数</b>：{@link #count} / {@link #canSpawn} 按「施法者维度、64 格内、归属本人」统计。</li>
 * </ol>
 *
 * <p><b>成员判定是扩展点</b>：默认按类别 id 相等计数；跨实体类型的池（临时召唤物 / 同伴）注册时
 * 传入 {@link Membership} 覆盖即可，未来任何「按池计数」的需求同理，不改动本类与调用方。
 *
 * <p><b>无类别字面量</b>：类别身份一律由 {@link Identifier} 承载，stat 侧标签由 {@link #tagsOf}
 * 单一来源产出（= 类别 id 的 path），确保 Java 读点与数据侧接线永不脱钩。
 */
public final class EntityCategoryLimit {

    private EntityCategoryLimit() {
    }

    /** 无上限哨兵：类别未登记 <b>且</b> 无 stat 加成时返回，等价于「不设限」（对既有未登记类别零行为变化）。 */
    public static final int UNLIMITED = Integer.MAX_VALUE;

    /**
     * fissure（裂隙）类别 id —— 数据侧形态 id 形态。
     * <p>实体侧 fissure 机制尚未落地：此处仅登记类别与上限接口，
     * 待 fissure 实体出现后（经 {@code SpawnAction} / 形态投递生成）自动受本设施约束，零 Java 改动。
     */
    public static final Identifier FISSURE = Identifier.fromNamespaceAndPath("coe", "fissure");

    /** 「临时召唤物」池（跨实体类型，按 SUMMON 标记计数）。 */
    public static final Identifier MINION = Identifier.fromNamespaceAndPath("coe", GateTag.MINION);

    /** 「同伴」池（跨实体类型，按 is_companion 标记计数）。 */
    public static final Identifier COMPANION = Identifier.fromNamespaceAndPath("coe", GateTag.COMPANION);

    /** 成员判定：某实体是否计入某类别的池（扩展点，默认按类别 id 相等）。 */
    public interface Membership {
        boolean matches(LivingEntity owner, Entity e);
    }

    private static final Map<Identifier, Supplier<? extends Attribute>> LIMITS = new LinkedHashMap<>();
    private static final Map<Identifier, Membership> MEMBERS = new LinkedHashMap<>();
    private static final Map<Identifier, Identifier> ALIASES = new HashMap<>();
    private static boolean ready = false;

    /** 登记一个受上限约束的实体类别（默认按类别 id 计数）。 */
    public static synchronized void register(Identifier category, Supplier<? extends Attribute> limitAttr) {
        register(category, limitAttr, null);
    }

    /** 登记一个受上限约束的实体类别，并指定成员判定（跨实体类型的池用）。 */
    public static synchronized void register(Identifier category, Supplier<? extends Attribute> limitAttr,
                                             Membership membership) {
        register(category, limitAttr, membership, new Identifier[0]);
    }

    /**
     * 登记一个受上限约束的实体类别。
     *
     * @param limitAttr  该类别上限属性（{@code Supplier} 延迟取值，规避类初始化顺序）
     * @param membership 成员判定；null = 默认（{@link #categoryOf} 等于本类别）
     * @param aliases    并入本池的其它类别 id（如箭塔 / 陷阱并入图腾池，共享同一全局额度）
     */
    public static synchronized void register(Identifier category, Supplier<? extends Attribute> limitAttr,
                                             Membership membership, Identifier... aliases) {
        if (category == null) return;
        LIMITS.put(category, limitAttr);
        if (membership != null) MEMBERS.put(category, membership);
        if (aliases != null) {
            for (Identifier a : aliases) {
                if (a != null && !a.equals(category)) ALIASES.put(a, category);
            }
        }
    }

    /** 类别归一（解析并入别名）。 */
    public static Identifier canonical(Identifier category) {
        ensure();
        if (category == null) return null;
        Identifier seen = category;
        for (int i = 0; i < 8; i++) {
            Identifier next = ALIASES.get(seen);
            if (next == null) return seen;
            seen = next;
        }
        return seen;
    }

    /**
     * 实体 → 类别：形态 id（{@link EntityDefCarrier#def()}）优先，否则回落实体类型 key。
     * <p>于是「同载体不同形态」（风暴漩涡 / 日曜球）与「不同实体类型」都能统一归类，
     * 新增载体无需改本方法。
     */
    public static Identifier categoryOf(Entity e) {
        ensure();
        if (e == null) return null;
        if (e instanceof EntityDefCarrier carrier) {
            Object def = carrier.def();
            if (def instanceof com.cimdy.coe.data.codec.entity.EntityDef ed && ed.type() != null) {
                return canonical(ed.type());
            }
        }
        return canonical(BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()));
    }

    /**
     * 由<b>待生成实体的 id</b> 解析其所属类别池；未登记的类别回落到 {@code fallback}
     * （典型：普通生物 → 临时召唤物池）。调用方无需写任何类别名。
     */
    public static Identifier categoryFor(Identifier entityId, Identifier fallback) {
        ensure();
        if (entityId == null) return fallback;
        Identifier canon = canonical(entityId);
        return LIMITS.containsKey(canon) ? canon : fallback;
    }

    /** 该类别是否已登记（有上限池）。 */
    public static boolean isRegistered(Identifier category) {
        ensure();
        return category != null && LIMITS.containsKey(canonical(category));
    }

    /**
     * 类别 → stat 侧查询标签集（单一来源）：{@code path} 形态。
     * <p>{@link MarkerStatEffect} 的 conditionTags 与 {@link StatEngine#markerMagnitude} 的查询集
     * 都经本方法产出，杜绝两侧各写字面量导致脱钩。
     */
    public static Set<String> tagsOf(Identifier category) {
        return category == null ? Set.of() : Set.of(category.getPath());
    }

    /** 该类别的当前数量上限（登记属性 + MarkerStatEffect 标记；二者皆无 → {@link #UNLIMITED}）。 */
    public static int limit(LivingEntity owner, Identifier category) {
        ensure();
        if (owner == null || category == null) return UNLIMITED;
        Identifier canon = canonical(category);
        Supplier<? extends Attribute> attr = LIMITS.get(canon);
        double base = attr == null ? 0.0 : attributeValue(owner, attr.get());
        double extra = StatEngine.magnitudeOf(owner,
                "entity_category_limit", tagsOf(canon));
        // 未登记类别且无 stat 加成 → 不设限（保证对既有未登记类别零行为变化）
        if (attr == null && extra <= 0.0) return UNLIMITED;
        return (int) Math.floor(base + extra);
    }

    /** 施法者名下、该类别在世实体的数量。 */
    public static int count(LivingEntity owner, ServerLevel level, Identifier category) {
        ensure();
        if (owner == null || level == null || category == null) return 0;
        Identifier canon = canonical(category);
        Membership membership = MEMBERS.getOrDefault(canon, defaultMembership(canon));
        int count = 0;
        for (Entity e : level.getEntities(owner, owner.getBoundingBox().inflate(64.0))) {
            if (membership.matches(owner, e)) count++;
        }
        return count;
    }

    /** 施法者是否还能再生成一个该类别的实体（未达上限）。 */
    public static boolean canSpawn(LivingEntity owner, ServerLevel level, Identifier category) {
        int limit = limit(owner, category);
        if (limit <= 0) return false;
        return count(owner, level, category) < limit;
    }

    /** 默认成员判定：归属本人 且 类别相同。 */
    private static Membership defaultMembership(Identifier canon) {
        return (owner, e) -> canon.equals(categoryOf(e)) && owner.equals(ownerOf(e));
    }

    /** 通用归属解析：技能物件（SkillEntity）→ 可归属实体 → OWNER_UUID 附件。 */
    public static LivingEntity ownerOf(Entity e) {
        if (e == null) return null;
        if (e instanceof SkillEntity se) return se.getOwner();
        if (e instanceof OwnableEntity own) {
            Entity o = own.getOwner();
            if (o instanceof LivingEntity living) return living;
        }
        if (e instanceof LivingEntity le) {
            Entity resolved = OwnerHelper.getOwner(le);
            return resolved instanceof LivingEntity living ? living : null;
        }
        return null;
    }

    /** 临时召唤物成员判定（跨实体类型：SUMMON 标记 且 非同伴）。 */
    private static boolean isMinionOf(LivingEntity owner, Entity e) {
        if (!(e instanceof LivingEntity le)) return false;
        if (!owner.equals(OwnerHelper.getOwner(le))) return false;
        if (AdditionData.getOrDefault(le, AdditionKeys.SUMMONED, 0.0) == 0.0) return false;
        return !isCompanionOf(owner, e);
    }

    /** 同伴成员判定（跨实体类型：is_companion 标记）。 */
    private static boolean isCompanionOf(LivingEntity owner, Entity e) {
        return e instanceof LivingEntity le
                && AdditionData.getOrDefault(le, AdditionKeys.IS_COMPANION, 0.0) != 0.0
                && owner.equals(OwnerHelper.getOwner(le));
    }

    private static double attributeValue(LivingEntity owner, Attribute attr) {
        if (attr == null) return 0.0;
        Holder<Attribute> holder = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attr);
        return owner.getAttributes().hasAttribute(holder) ? owner.getAttributeValue(holder) : 0.0;
    }

    private static Identifier key(EntityType<?> type) {
        return type == null ? null : BuiltInRegistries.ENTITY_TYPE.getKey(type);
    }

    /**
     * 类别登记（懒初始化，规避注册表就绪顺序）：
     * 图腾族（图腾 / 箭塔 / 陷阱）共享 {@code MAX_TOTEMS} 池；球 / 同伴 / 临时召唤物各自成池；
     * fissure 为 stat 侧接口预留。新增类别在此加一行即可。
     */
    private static synchronized void ensure() {
        if (ready) return;
        ready = true;
        register(key(EntityRegistry.SKILL_TOTEM.get()), () -> AttributeRegistry.MAX_TOTEMS.value(),
                null, key(EntityRegistry.BALLISTA.get()), key(EntityRegistry.TRAP.get()));
        register(key(EntityRegistry.ORB.get()), () -> AttributeRegistry.MAX_ORB.value());
        register(MINION, () -> AttributeRegistry.MINION_LIMIT.value(), EntityCategoryLimit::isMinionOf);
        register(COMPANION, () -> AttributeRegistry.MAX_COMPANIONS.value(), EntityCategoryLimit::isCompanionOf);
        register(FISSURE, () -> AttributeRegistry.MAX_FISSURE.value());
        // 墙（冰墙/火墙/盾墙）：复用 WALL 实体类型；wall_fire / wall_cold 形态并入同一上限池（同龙头/陷阱并图腾池同构）。
        register(key(EntityRegistry.WALL.get()), () -> AttributeRegistry.MAX_WALL.value(),
                null,
                Identifier.fromNamespaceAndPath("coe", "wall_fire"),
                Identifier.fromNamespaceAndPath("coe", "wall_cold"));
    }
}
