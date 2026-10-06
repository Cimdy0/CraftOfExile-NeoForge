package com.cimdy.coe.registry.world;

import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.OrbDelivery;
import com.cimdy.coe.data.helper.combat.HitScopeHelper;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.entity.custom.EnderBall;
import com.cimdy.coe.entity.custom.skill.visual.SkillBanner;
import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.cimdy.coe.entity.custom.skill.totem.SkillTotem;
import com.cimdy.coe.entity.custom.skill.totem.BallistaTotem;
import com.cimdy.coe.entity.custom.skill.totem.TrapEntity;
import com.cimdy.coe.entity.custom.skill.totem.MineEntity;

import com.cimdy.coe.entity.custom.skill.OfferingSpike;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class EntityRegistry {
    public static final DeferredRegister.Entities ENTITY_TYPES = DeferredRegister.createEntities(CraftOfExile.MODID);

    public static final Supplier<EntityType<EnderBall>> ENDER_BALL =
            ENTITY_TYPES.registerEntityType("ender_ball", EnderBall::new, MobCategory.MISC);

    /**
     * 驻场道具（{@code coe:prop}）—— 残片 / 危害物一类「放置在世界里等目标」的物件。
     *
     * <p><b>与 {@link #ORB} / {@link #GRENADE} / {@link #SKILL_BALL} 共用同一个注册基类
     * {@link SkillEntity}</b>：形态差异（怎么动、何时对谁生效）全部来自 {@code coe:entity} 数据的
     * 运动组件 ⊕ 行为组件，<b>不在实体类里</b>；注册成不同类型只是为了让事件与目标选择器
     * 能按 {@code entity_type} 直接判定「这是个什么物件」。
     *
     * <p>AABB 随作用半径缩放（{@link SkillEntity#getDimensions}），故不指定 sized。
     */
    public static final Supplier<EntityType<SkillEntity>> PROP =
            ENTITY_TYPES.registerEntityType("prop", SkillEntity::new, MobCategory.MISC);

    /**
     * Orb（{@code coe:orb}）—— <b>原地驻场、周期性对周围放能</b>的球体（POE2 风暴漩涡 / 日曜球）。
     *
     * <p><b>为什么单独注册类型却不单独写类</b>：Orb 有独立语义（事件 / 选择器要能判定
     * {@code entity_type=coe:orb}），但它的行为与危害物只差一个行为组件
     * （{@code static + pulse} vs {@code static + contact(enemy)}）——
     * 按「实体只是注册基类、行为依赖数据」的铁律，<b>新增这类物件只应加注册 + 加数据，
     * 不应再写一个实体子类</b>把 {@code pulse} 烤进 {@code tick()}。
     *
     * <p>真值（poe2db / poe2wiki 0.3.0）：风暴漩涡「每 4 秒发射一次闪电、球半径 3.6 米、
     * 持续 12 秒」；日曜球「每 1.4 秒脉冲一次、爆炸范围 2.5 米、持续 10 秒」。
     * 节奏与半径由 {@code OrbDelivery} 按技能装配（同初速度，属投递参数）。
     */
    public static final Supplier<EntityType<SkillEntity>> ORB =
            ENTITY_TYPES.registerEntityType("orb", SkillEntity::new, MobCategory.MISC);

    /**
     * 手雷（{@code coe:grenade}）—— 被扔出去、落地后按引信起爆的弹体
     * （{@code ballistic + detonate}）。
     *
     * <p>与 {@link #SKILL_ARROW}（投射物）分工：投射物飞行中命中实体即触发，
     * 手雷<b>不与实体碰撞</b>——落地 → 引信 → 以自身为中心范围引爆。
     */
    public static final Supplier<EntityType<SkillEntity>> GRENADE =
            ENTITY_TYPES.registerEntityType("grenade", SkillEntity::new, MobCategory.MISC);

    /**
     * 投射物（{@code coe:skill_arrow}）—— 经 {@link ProjectileDelivery} 生成、由 {@link SkillEntity} 承载、
     * 其命中 / 飞行机制统一在 {@link SkillEntity}「投射物路径」中驱动（数据驱动、不再绑定原版箭矢类型）。
     * 形态由 {@code coe:entity/arrow} 定义（motion=external、behavior=none、tagSet 含 arrow+projectile）。
     */
    public static final Supplier<EntityType<SkillEntity>> SKILL_ARROW =
            ENTITY_TYPES.registerEntityType("skill_arrow", SkillEntity::new, MobCategory.MISC);

    /**
     * 球（{@code coe:skill_ball}）—— 缓慢飞行、<b>本体不命中</b>、周期性对周围放能的弹体
     * （{@code ballistic + pulse}；POE2 天雷之珠 / 凛冬之眼一类）。
     *
     * <p>与 {@link #ORB} 只差运动组件：Orb 原地不动，球边飞边放能 —— 同一个行为组件
     * （{@code pulse}）配不同运动即两种形态，正是「行为依赖数据」的例证。
     */
    public static final Supplier<EntityType<SkillEntity>> SKILL_BALL =
            ENTITY_TYPES.registerEntityType("skill_ball", SkillEntity::new, MobCategory.MISC);

    // 战旗实体：AABB 随光环半径缩放（SkillBanner.getDimensions），故不指定 sized。
    public static final Supplier<EntityType<SkillBanner>> SKILL_BANNER =
            ENTITY_TYPES.registerEntityType("skill_banner", SkillBanner::new, MobCategory.MISC);

    public static final Supplier<EntityType<SkillTotem>> SKILL_TOTEM =
            ENTITY_TYPES.registerEntityType("skill_totem", SkillTotem::new, MobCategory.MISC,
                    builder -> builder.sized(0.6f, 1.5f));

    /**
     * 裂隙（{@code coe:fissure}）—— 数据驱动的静态世界物件（{@code static + pulse}）。
     *
     * <p>复用 {@link SkillEntity} 作为承载：行为完全由 {@code coe:entity/fissure} 数据驱动
     * （运动 = {@code static}、行为 = {@code pulse}、触发 = {@code phase_on_hit}），
     * 与 {@link #PROP} / {@link #ORB} / {@link #GRENADE} 同范式——新增此类物件只加注册 + 加数据，
     * 不新写实体子类（SkillTotem 不是 EntityDefCarrier，无法驱动 EntityDef 组件，故不用它承载）。
     *
     * <p>类别身份由 {@code EntityDef.type()} 字面量 {@code coe:fissure} 承载
     * （见 {@code EntityCategoryLimit.FISSURE}，受 {@code MAX_FISSURE} 上限约束）；
     * AABB 随作用半径缩放（{@link SkillEntity#getDimensions}），故不指定 sized（同 PROP 范式）。
     */
    public static final Supplier<EntityType<SkillEntity>> SKILL_FISSURE =
            ENTITY_TYPES.registerEntityType("fissure", SkillEntity::new, MobCategory.MISC);

    /**
     * 墙（{@code coe:wall}）—— 有碰撞、能阻挡技能 / 弹幕 / 移动的驻场建筑实体（冰墙 / 火墙 / 盾墙）。
     *
     * <p>复用 {@link SkillEntity} 作为承载：形态由 {@code coe:entity/wall_<x>} 数据驱动
     * （运动 = {@code wall}（physics 碰撞）、行为 = {@code pulse(enemy)}、触发 = {@code phase_on_hit(ON_TICK)}），
     * 与 {@link #PROP} / {@link #ORB} / {@link #SKILL_FISSURE} 同范式 —— 新增此类物件只加注册 + 加数据，不新写实体子类。
     *
     * <p>{@code isPickable()=true} 由 {@link SkillEntity#isPickable()} 按运动类型判定（仅墙为 true），
     * 使其它实体每 tick 被其包围盒推开；AABB 随作用半径缩放（{@link SkillEntity#getDimensions}），故不指定 sized。
     */
    public static final Supplier<EntityType<SkillEntity>> WALL =
            ENTITY_TYPES.registerEntityType("wall", SkillEntity::new, MobCategory.MISC);

    /**
     * 箭塔（{@code coe:ballista}）—— 复用 {@link SkillTotem} 全部驻场施法行为，
     * 仅作为独立实体类型使命中作用域能按实体身份注入 {@code ballista} 标签（见 {@code HitScopeHelper}）。
     * 与 {@link #SKILL_TOTEM} 同尺寸、同 MobCategory（均为 MISC 静态驻场体）。
     */
    public static final Supplier<EntityType<BallistaTotem>> BALLISTA =
            ENTITY_TYPES.registerEntityType("ballista", BallistaTotem::new, MobCategory.MISC,
                    builder -> builder.sized(0.6f, 1.5f));

    /**
     * 陷阱（{@code coe:trap}）—— 复用 {@link SkillTotem} 全部驻场施法行为，
     * 仅作为独立实体类型使命中作用域能按实体身份注入 {@code trap} 标签（见 {@code HitScopeHelper}）。
     * 与 {@link #BALLISTA} 同 MobCategory（均为 MISC 静态驻场体），尺寸更小（贴地布防）。
     */
    public static final Supplier<EntityType<TrapEntity>> TRAP =
            ENTITY_TYPES.registerEntityType("trap", TrapEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.6f, 0.5f));

    /**
     * 地雷（{@code coe:mine}）—— 复用 {@link SkillTotem} 全部驻场施法行为，
     * 仅作为独立实体类型使命中作用域能按实体身份注入 {@code mine} 标签（见 {@code HitScopeHelper}）。
     * 与 {@link #TRAP} 同 MobCategory（均为 MISC 静态驻场体），尺寸更小（贴地布防）。
     */
    public static final Supplier<EntityType<MineEntity>> MINE =
            ENTITY_TYPES.registerEntityType("mine", MineEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.6f, 0.3f));

    /** 奉献尖刺（Offering Spike）：伤害型随从实体，渲染体为 Noop（可见性由实体粒子提供）。 */
    public static final Supplier<EntityType<OfferingSpike>> OFFERING_SPIKE =
            ENTITY_TYPES.registerEntityType("offering_spike", OfferingSpike::new, MobCategory.MISC,
                    builder -> builder.sized(0.5f, 0.8f));
}
