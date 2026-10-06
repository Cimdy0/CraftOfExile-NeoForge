package com.cimdy.coe.data.gen.asset;

import com.cimdy.coe.data.helper.engine.entity.EntityStages;
import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.OrbDelivery;
import com.cimdy.coe.data.gen.source.CoeDataPaths;
import com.cimdy.coe.data.gen.provider.GateTagCategoryProvider;
import com.cimdy.coe.data.codec.entity.EntityDef;
import com.cimdy.coe.data.codec.entity.trigger.EntityStage;
import com.cimdy.coe.data.codec.entity.render.EntityRenderDef;
import com.cimdy.coe.data.codec.entity.render.EntityRenderLayer;
import com.cimdy.coe.data.codec.entity.render.EntityRenderLayers;
import com.cimdy.coe.data.codec.entity.trigger.EntityTriggers;
import com.cimdy.coe.data.codec.skill.phase.action.control.LifecycleTrigger;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.area.RemnantDelivery;
import com.cimdy.coe.data.codec.entity.behavior.EntityBehavior;
import com.cimdy.coe.data.codec.entity.behavior.EntityBehaviors;
import com.cimdy.coe.data.codec.entity.behavior.EntityMotions;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.event.EventName;
import com.cimdy.coe.registry.world.EntityRegistry;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 数据生成：把内置实体定义 / 渲染定义导出为 {@code data/coe/coe/entity/*.json} 与
 * {@code data/coe/coe/entity_render/*.json}，使「同一实体类型、多技能、多外观」全部数据驱动。
 *
 * <p><b>为什么是独立命名空间</b>：实体定义<b>不属于技能</b>，技能层不内联它，
 * 只通过 {@code EntityRead} 按 id 引用并覆写 {@code render} / {@code name}。
 * 于是同一个 {@code coe:prop} 实体类型可以被任意多个技能复用，各自呈现不同外观与名字 ——
 * 玩家感知上是不同的东西，底层却是同一类实体（「新实体的错觉」）。
 *
 * <p><b>形态 = 运动组件 ⊕ 行为组件</b>：本文件就是「世界物件形态」的<b>唯一真值表</b> ——
 * 新增一种物件只需在此加一份数据（承载类型 + 外观 + 运动 + 行为），
 * 实体类与投递<b>零改动</b>（见 {@link EntityDef} 的实体/行为拆分说明）。
 *
 * <p><b>名称刻意留空</b>：内置定义不给 {@code name}，由技能侧在 {@code EntityRead} 里覆写
 * （例：魔力残片 → 覆写 name）。这样「叫什么」归使用方决定，避免基础定义越权命名。
 *
 * <p>与 {@link GateTagCategoryProvider} 同范式：定义只在 Java 侧构造一处，
 * datagen 负责导出为数据包内容，从而既可无数据包跑，又能被数据包覆盖或扩展。
 */
public class EntityProvider implements DataProvider {

    /**
     * 已生成元素投射物渲染数据（{@code coe:entity_render/<element> -> coe:textures/entity/prop/<element>.png}）的元素名。
     *
     * <p><b>为何是名单而非「按需生成」</b>：元素名同时是伤害类型（{@code SkillFactory} 据此派生外观）
     * 与渲染数据名的约定交集，物理等<b>非元素</b>伤害类型没有对应外观数据。技能侧须据本名单
     * 决定是否覆写 render —— 覆写一个不存在的渲染 id 会让该投递物<b>无模型可渲染</b>
     * （{@code EntityRenderDefs} 查不到 → 渲染层 model 为 null → 实体不可见），
     * 而非回落默认外观。故名单是两侧共用的单一真源。
     */
    public static final Set<String> ELEMENT_RENDER_NAMES = Set.of("fire", "cold", "lightning", "chaos");

    /**
     * 已生成飞行球渲染数据（{@code coe:entity_render/ball_<element>}）的元素名。
     *
     * <p><b>与 {@link #ELEMENT_RENDER_NAMES} 是两份不同的名单</b>，收录范围不同：
     * 投射物有四件元素配色箭矢（{@code fire/cold/lightning/chaos}），
     * 球只生成确有实例的电球一种（{@code ball_lightning}，即天雷之珠）。
     *
     * <p><b>为什么不与投射物共用一份名单</b>：共用会把 {@code fire/cold/chaos}
     * 也放进球族，于是 {@code ball_fire} 等 id 被派生出来却查不到任何渲染数据，
     * 球直接不可见。名单必须与本族真实存在的数据一一对应，故分列三份。
     */
    public static final Set<String> BALL_RENDER_NAMES = Set.of("lightning");

    /**
     * 已生成驻场球渲染数据（{@code coe:entity_render/orb_<element>}）的元素名。
     *
     * <p>与 {@link #BALL_RENDER_NAMES} 同理分列：只登记确有实例的两种
     * （{@code orb_lightning} = 风暴漩涡、{@code orb_fire} = 日曜球），
     * 刻意不预置 {@code orb_cold} / {@code orb_chaos} ——
     * 无人引用的外观数据是孤儿数据，而被引用的悬空 id 会让实体不可见。
     */
    public static final Set<String> ORB_RENDER_NAMES = Set.of("lightning", "fire");

    private final PackOutput output;

    public EntityProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path entityDir = CoeDataPaths.coeRoot(output).resolve("entity");
        Path renderDir = CoeDataPaths.coeRoot(output).resolve("entity_render");

        // 本次真正生成出来的元素渲染名：由 elementRender / ballElementRender /
        // orbElementRender 在生成处自报，不另维护手抄名单 —— 手抄名单正是旧 assert
        // 只能自己跟自己比的原因。
        Set<String> elementRenders = new LinkedHashSet<>();
        Set<String> ballRenders = new LinkedHashSet<>();
        Set<String> orbRenders = new LinkedHashSet<>();

        // ── coe:entity/remnant：static + contact(self) ──
        // 施放者走到即拾取：拾取时 emit collect_remnant + 回调 ON_PICKUP，拾取后销毁（rearm=false）。
        // 拾取点燃 ON_PICKUP（而非 ON_HIT）：ON_HIT 会被「生成残片的那一相位」一并执行——
        // 例如球到期留残迹时，拾取残片会误点燃球自身的 ON_HIT 脉冲伤害；且「击杀即回魔」
        // 与真值「拾取残片才回魔」相悖。ON_PICKUP 只由残片形态回调，语义唯一。
        EntityDef remnant = new EntityDef(
                EntityDef.DEFAULT_TYPE,
                Optional.empty(),
                0.4F,
                Identifier.parse("coe:arcane_blue"),
                List.of(GateTag.REMNANT),
                List.of(
                        new EntityTriggers.PhaseOnHit(LifecycleTrigger.ON_PICKUP),
                        new EntityTriggers.EmitEvent(EventName.COLLECT_REMNANT, List.of(GateTag.REMNANT), "target")),
                List.of(),
                EntityMotions.Static.INSTANCE,
                List.of(new EntityBehaviors.Contact(EntityBehavior.TARGET_SELF, false, 20, Optional.empty())));

        // ── coe:entity/hazard：static + contact(enemy) ──
        // 敌人穿越即触发：记录 hazard_triggered + 对目标施 hazard_mark + 回调 ON_HIT，
        // 触发后按 coe:hazard_rearm_chance 属性几率决定是否重新布置（危害物持续伤害的来源）。
        EntityDef hazard = new EntityDef(
                EntityDef.DEFAULT_TYPE,
                Optional.empty(),
                0.5F,
                Identifier.parse("coe:smoke"),
                List.of(GateTag.HAZARD),
                List.of(
                        EntityTriggers.PhaseOnHit.INSTANCE,
                        new EntityTriggers.RecordEvent(EventName.HAZARD_TRIGGERED, "caster", List.of()),
                        new EntityTriggers.ApplyStatus("hazard_mark", 80, 1.0, "target")),
                List.of(),
                EntityMotions.Static.INSTANCE,
                List.of(new EntityBehaviors.Contact(EntityBehavior.TARGET_ENEMY, false, 20,
                        Optional.of(Identifier.parse("coe:hazard_rearm_chance")))));

        // ── coe:entity/caltrop：static + contact(enemy, 一次性) ──
        // POE2 铁蒺藜（Trail of Caltrops，Tier 8 / 30 精魂）形态：半径 0.5 米、存活 8 秒，
        // 敌人踩中即触发并<b>销毁</b>（真值「Caltrops are destroyed when triggered」）——
        // 故不带 hazard_rearm_chance，与通用危害物（按属性几率重新布置）区分。
        // 不施 hazard_mark：铁蒺藜触发即销毁，不存在「持续处于危害物中」的语义。
        // 触发仍记 hazard_triggered：它是危害物触发事件唯一的真实发射点，
        // 「近期被危害物击中」类条件依赖它（与铁蒺藜是否驻场无关）。
        EntityDef caltrop = new EntityDef(
                EntityDef.DEFAULT_TYPE,
                Optional.empty(),
                0.25F,
                Identifier.parse("coe:caltrop"),
                List.of(GateTag.HAZARD),
                List.of(
                        EntityTriggers.PhaseOnHit.INSTANCE,
                        new EntityTriggers.RecordEvent(EventName.HAZARD_TRIGGERED, "caster", List.of())),
                List.of(),
                EntityMotions.Static.INSTANCE,
                List.of(new EntityBehaviors.Contact(EntityBehavior.TARGET_ENEMY, false, 20, Optional.empty())));

        // ── coe:entity/arrow：external + none ──
        // 箭矢的运动与命中由原版 AbstractArrow + 投递的 FlightPattern 驱动，本层不介入
        // （显式声明 external/none，让数据自解释而非「忘了配」）；本定义只承载外观与标签。
        EntityDef arrow = new EntityDef(
                Identifier.parse("coe:skill_arrow"),
                Optional.empty(),
                0.4F,
                Identifier.parse("coe:arrow"),
                List.of(GateTag.PROJECTILE, GateTag.ARROW),
                List.of(),
                List.of(),
                EntityMotions.External.INSTANCE,
                List.of(EntityBehaviors.None.INSTANCE));

        // ── coe:entity/grenade：ballistic + detonate ──
        // 抛物线飞出（gravity 0.05，撞块不消散而是停住）→ 引信 60 tick（3 秒，POE2 手雷 Detonation Time）
        // → 对半径内每个目标引爆一次。引信取 Explosive Grenade 的 3s 为共享默认值（Voltaic 2.5s 仅 0.5s 偏差，属可接受近似）。
        EntityDef grenade = new EntityDef(
                Identifier.parse("coe:grenade"),
                Optional.empty(),
                0.25F,
                Identifier.parse("coe:grenade"),
                List.of(GateTag.GRENADE),
                List.of(EntityTriggers.PhaseOnHit.INSTANCE),
                List.of(),
                new EntityMotions.Ballistic(0.05F, false),
                List.of(new EntityBehaviors.Detonate(60, EntityBehavior.TARGET_ENEMY)));

        // ── coe:entity/ball：ballistic + pulse ──
        // 平飞（gravity 0）、撞块即消散；每 4 tick（0.2 秒）对半径内每个目标各放能一次
        // —— POE2 天雷之珠真值：「每 0.2 秒击中范围内的敌人；弩箭以 1.8 公尺内的敌人为目标」。
        EntityDef ball = new EntityDef(
                Identifier.parse("coe:skill_ball"),
                Optional.empty(),
                0.5F,
                Identifier.parse("coe:ball"),
                List.of(GateTag.PROJECTILE),
                List.of(EntityTriggers.PhaseOnHit.INSTANCE),
                List.of(),
                new EntityMotions.Ballistic(0.0F, true),
                List.of(new EntityBehaviors.Pulse(4, EntityBehavior.TARGET_ENEMY)));

        // ── coe:entity/orb：static + pulse ──
        // 原地驻场的能量球：不移动、不与实体碰撞，每 interval tick 对作用半径内每个敌人各放能一次。
        // POE2 真值（poe2db / poe2wiki 0.3.0）：
        //   风暴漩涡 —— 「每 4 秒发射一次闪电；球半径 3.6 米；持续 12 秒」；
        //   日曜球   —— 「每 1.4 秒脉冲一次；爆炸范围 2.5 米；持续 10 秒」。
        // 此处的 interval 是形态自带节奏（风暴漩涡的 4 秒 = 80 tick），
        // 具体技能由 OrbDelivery 按真值覆写（同初速度，节奏属投递参数）。
        // 标签刻意留空：Orb 不是 hazard，作用域标签由 OrbDelivery 的兜底 {area, aoe} 提供。
        EntityDef orb = new EntityDef(
                Identifier.parse("coe:orb"),
                Optional.empty(),
                0.6F,
                Identifier.parse("coe:orb"),
                List.of(),
                Optional.empty(),  // spawn_sound：本形态不发声（缺省不发声，维持现状）
                Optional.empty(),  // hit_sound：本形态不发声
                List.of(EntityTriggers.PhaseOnHit.INSTANCE),
                List.of(),  // 生成效果通道 on_spawn：本形态不使用
                // 第二效果通道 on_expire：到期留残迹——在球原地生成一个可拾取的残片实体（owner 走过即点燃阶段 ON_HIT）。
                // 数据驱动、不依赖新事件/触发器类型；仅 Orb of Storms 置非空，frostBomb/sunOrb 不受影响。
                List.of(new EntityTriggers.Deliver(RemnantDelivery.DEFAULT)),
                EntityMotions.Static.INSTANCE,
                List.of(new EntityBehaviors.Pulse(80, EntityBehavior.TARGET_ENEMY)),
                List.of(),  // 第四效果通道 on_aura：本形态不使用
                // 第三效果通道：玩家在球作用半径内施放 → 球额外放电一次（点燃玩家阶段 ON_HIT 闪电）。
                // caster 仍是玩家（stat 作用域不变），prop 是本实体（由球放出）—— 满足「球施法行为数据驱动、不切换施法者主体」。
                // 仅 Orb of Storms 置非空；frostBomb / sunOrb 走次级构造器默认空列表，不受影响。
                List.of(EntityTriggers.PhaseOnHit.INSTANCE),
                List.of());  // 阶段序列 stages：本形态无阶段（非「阶段式实体」，EntityStages 对它零行为）

        // ── coe:entity/frost_bomb：static + [pulse(20,enemy), detonate(200,enemy)] ──
        // Frost Bomb（POE2 寒霜爆）真实语义的落地形态：原地悬停，周期内对半径内敌人施加曝曬
        // （pulse 走 on_trigger=ON_HIT），到期时引爆造成爆炸伤害（detonate 走 on_expire=ON_EXPIRE）。
        // 这是 on_expire 第二效果通道的<b>首个真实消费方</b>——此前该通道只声明未闭合。
        // 未定义 on_expire 的既有形态（手雷等）不受影响：SkillEntity 回退 on_trigger。
        // 命名空间复用驻场球外观（coe:orb 贴图），零新增贴图资源（R2 不回归）。
        EntityDef frostBomb = new EntityDef(
                Identifier.parse("coe:orb"),
                Optional.empty(),
                0.6F,
                Identifier.parse("coe:frost_bomb"),
                List.of(GateTag.COLD),
                Optional.empty(),  // spawn_sound
                Optional.empty(),  // hit_sound
                List.of(EntityTriggers.PhaseOnHit.INSTANCE),
                List.of(),  // on_spawn
                List.of(EntityTriggers.PhaseOnHit.ON_EXPIRE),
                EntityMotions.Static.INSTANCE,
                List.of(
                        new EntityBehaviors.Pulse(20, EntityBehavior.TARGET_ENEMY),
                        new EntityBehaviors.Detonate(200, EntityBehavior.TARGET_ENEMY)),
                // 第四效果通道 on_aura：本形态不使用
                List.of(),
                // 第三效果通道 on_nearby_cast：本形态不使用
                List.of(),
                // 阶段序列 stages：本形态无阶段（非「阶段式实体」，EntityStages 对它零行为）
                List.of());

        // ── coe:entity/sun_orb：static + [pulse(28,enemy), detonate(200,enemy)] ──
        // 日曜球（Solar Orb）真实语义：原地悬停，每 1.4 秒（28 tick）脉冲火焰伤害，
        // 到期引爆造成火伤 + 点燃（detonate 走 on_expire=ON_EXPIRE，叠加 StatAction 点燃）。
        // 复用 Frost Bomb 已验证的 on_expire 第二通道 + 通用 orb(ref) 投递平台；外观沿用已存在的 coe:orb_fire。
        EntityDef sunOrb = new EntityDef(
                Identifier.parse("coe:orb"),
                Optional.empty(),
                0.6F,
                Identifier.parse("coe:orb_fire"),
                List.of(GateTag.FIRE),
                Optional.empty(),  // spawn_sound
                Optional.empty(),  // hit_sound
                List.of(EntityTriggers.PhaseOnHit.INSTANCE),
                List.of(),  // on_spawn
                List.of(EntityTriggers.PhaseOnHit.ON_EXPIRE),
                EntityMotions.Static.INSTANCE,
                List.of(
                        new EntityBehaviors.Pulse(28, EntityBehavior.TARGET_ENEMY),
                        new EntityBehaviors.Detonate(200, EntityBehavior.TARGET_ENEMY),
                        // 光环行为：每 28 tick 对 1m 内敌人施加点燃（真值「极近敌人被点燃」，独立小半径通道）
                        new EntityBehaviors.Aura(1.0, 28, EntityBehavior.TARGET_ENEMY,
                                Identifier.parse("coe:ignite"), 1200.0)),
                // 第四效果通道 on_aura：光环行为施加点燃（1 分钟）
                List.of(new EntityTriggers.ApplyStatus("ignite", 1200, 1.0, "target")),
                List.of(),  // 第三效果通道 on_nearby_cast：本形态不使用
                List.of());  // 阶段序列 stages：本形态无阶段（非「阶段式实体」，EntityStages 对它零行为）

        // ── coe:entity/fissure：static + pulse（数据驱动裂隙世界物件）──
        // 承载类型 coe:fissure（EntityRegistry.SKILL_FISSURE，复用 SkillEntity 承载）；
        // 类别身份由 type 字面量 "coe:fissure" 承担（受 EntityCategoryLimit.FISSURE / MAX_FISSURE 约束）。
        // 行为完全数据驱动：static 静止 + pulse 周期对范围内敌人点燃阶段 ON_HIT；
        // stages 留空 → 非阶段式实体（EntityStages 对它零行为）。
        EntityDef fissure = new EntityDef(
                Identifier.parse("coe:fissure"),
                Optional.empty(),
                0.6F,
                Identifier.parse("coe:fissure"),
                List.of("fissure"),
                Optional.empty(),  // spawn_sound
                Optional.empty(),  // hit_sound
                List.of(EntityTriggers.PhaseOnHit.INSTANCE),
                List.of(),  // on_spawn
                List.of(),
                EntityMotions.Static.INSTANCE,
                List.of(new EntityBehaviors.Pulse(40, EntityBehavior.TARGET_ENEMY)),
                List.of(),
                List.of(),
                List.of());

        // ── coe:entity/skill_vine：static + pulse + 阶段序列（数据驱动毒藤，植物 overgrow 设施载体）──
        // 承载类型用既有 coe:prop（SkillEntity，与 remnant/hazard/caltrop 同范式）—— plant 纯数据、无需新 EntityType；
        // 形态 id 仍为 skill_vine（技能侧按 coe:skill_vine 引用；"coe:skill_vine" 无对应注册 EntityType，
        // 故 type 复用 coe:prop 载体，与 remnant/hazard/caltrop 一致）。
        // 标签含 "plant"（EntityStages.stageSetTagOf 据其识别植物，接入
        // PLANTS_HAVE_A_CHANCE_TO_IMMEDIATELY_OVERGROW 的 skip 几率，EFF conditionTags={plant}）。
        // stages 非空 → EntityStages 真正驱动阶段推进（sprout→grown→bloom）；末阶段 bloom 走默认 20 tick。
        EntityDef plant = new EntityDef(
                Identifier.parse("coe:prop"),
                Optional.empty(),
                0.4F,
                Identifier.parse("coe:toxic_vine"),
                List.of("plant"),
                Optional.empty(),  // spawn_sound
                Optional.empty(),  // hit_sound
                List.of(EntityTriggers.PhaseOnHit.INSTANCE),
                List.of(),  // on_spawn
                List.of(),
                EntityMotions.Static.INSTANCE,
                List.of(new EntityBehaviors.Pulse(30, EntityBehavior.TARGET_ENEMY)),
                List.of(),
                List.of(),
                List.of(
                        new EntityStage("sprout", 40),
                        new EntityStage("grown", 60),
                        new EntityStage("bloom")));

        // ── coe:entity/wall · wall_fire · wall_cold：Wall 运动（真实碰撞）+ pulse（对敌周期点燃 ON_TICK）──
        // 冰墙/火墙/盾墙的<b>真实机制</b>：墙体是「有碰撞、能阻挡技能/弹幕/移动的建筑实体」，
        // 形态 = Wall 运动（physics=true 开真实碰撞 + isPickable 推开实体）⊕ pulse（周期对范围内敌人触发 ON_TICK）。
        // 与通用裂隙（generic_fissure）同构：区别在于运动（Wall 而非 Static）与可碰撞（阻挡而非仅伤害），
        // 且墙体走专属 SpawnAction.spawnWall 分支（调 SkillEntity.configure 加载 EntityDef 并 retain 阶段），
        // 而非通用 spawnMinionEntity（不加载 EntityDef，无法驱动行为）。三者在 EntityDef 层仅外观/标签不同，
        // 共享同一 coe:wall 承载类型（WALL EntityType）。on_trigger=PhaseOnHit(ON_TICK)：墙脉冲时点燃技能 ON_TICK
        // （= DamageAction + ailment）；只有墙实体触发 ON_TICK、施法者走 self() 仅触发 ON_HIT（生成墙），故无自伤/递归。
        // box_height≈2.5F 表现「一道墙」；collision 半径由 SpawnAction.attackRange（wall_range）注入，
        // 决定墙的厚度/覆盖半径与 pulse 命中范围。
        EntityDef wall = wallDef(Identifier.parse("coe:wall"), List.of("wall", "physical"));
        EntityDef wallFire = wallDef(Identifier.parse("coe:wall_fire"), List.of("wall", "fire"));
        EntityDef wallCold = wallDef(Identifier.parse("coe:wall_cold"), List.of("wall", "cold"));

        // ── coe:entity_render/arcane_blue：蓝色 GLOW 环 + 光点（魔力残片外观）──
        EntityRenderDef arcaneBlue = new EntityRenderDef(List.of(
                new EntityRenderLayers.Ring(ParticleTypes.GLOW, 12, 1.0, 0.15, 6, 0.02),
                new EntityRenderLayers.Point(ParticleTypes.GLOW, 2, 0.2, 0.3, 4, 0.01),
                new EntityRenderLayers.Model(new EntityRenderLayer.ModelDef(
                        Identifier.parse("coe:textures/entity/prop/remnant"), 0.9F, 0.05F, 2.0F, true))));

        // ── coe:entity_render/smoke：灰色烟雾环 + 烟柱（危害物外观）──
        EntityRenderDef smoke = new EntityRenderDef(List.of(
                new EntityRenderLayers.Ring(ParticleTypes.SMOKE, 12, 1.0, 0.1, 6, 0.02),
                new EntityRenderLayers.Pillar(ParticleTypes.SMOKE, 1.5, 6, 1.0, 6, 0.02),
                new EntityRenderLayers.Model(new EntityRenderLayer.ModelDef(
                        Identifier.parse("coe:textures/entity/prop/hazard"), 1.2F, 0.0F, 0.5F, true))));

        // ── coe:entity_render/caltrop：铁蒺藜（危害物贴图 + 贴地小尺寸，不自转）──
        // 沿用 coe:hazard 贴图（零新增贴图资源，R2 不回归）：立牌缩到 0.3 倍表现「贴地尖刺」，
        // 与烟雾状危害物（1.2 倍立牌 + 烟柱 + 烟环）区分。
        EntityRenderDef caltropRender = new EntityRenderDef(List.of(
                new EntityRenderLayers.Model(new EntityRenderLayer.ModelDef(
                        Identifier.parse("coe:textures/entity/prop/hazard"), 0.3F, 0.0F, 0.0F, true))));

        // ── coe:entity_render/arrow：箭矢外观（真 3D 箭矢模型 + 贴图；无粒子层）──
        // 贴图沿用既有默认（原版 spectral_arrow），使本次改造不产生外观回归；
        // 技能侧可覆写 render 指向自己的贴图，从而得到完全不同的投射物外观。
        EntityRenderDef arrowRender = new EntityRenderDef(List.of(
                new EntityRenderLayers.Model(new EntityRenderLayer.ModelDef(
                        Identifier.withDefaultNamespace("textures/entity/projectile/spectral_arrow"),
                        1.0F, 0.0F, 0.0F, false))));

        // ── coe:entity_render/grenade：手雷外观（飞行拖尾烟点 + 立牌贴图）──
        // 缩放刻意很小（0.15 × 引爆半径）：立牌尺寸按半径换算，而手雷本体远小于它的爆炸范围。
        // 自转 8°/tick 表现飞行翻滚；落地后也在原地自转至引爆。
        EntityRenderDef grenadeRender = new EntityRenderDef(List.of(
                new EntityRenderLayers.Point(ParticleTypes.SMOKE, 1, 0.15, 0.2, 3, 0.01),
                new EntityRenderLayers.Model(new EntityRenderLayer.ModelDef(
                        Identifier.parse("coe:textures/entity/prop/grenade"), 0.15F, 0.0F, 8.0F, true))));

        // ── coe:entity_render/ball：通用能量球（GLOW 环 + 球贴图，缓慢自转）──
        // 自转 1.5°/tick：球在飞行中缓慢翻滚，与手雷的高速翻滚（8°/tick）区分。
        EntityRenderDef ballRender = new EntityRenderDef(List.of(
                new EntityRenderLayers.Ring(ParticleTypes.GLOW, 10, 0.35, 0.12, 3, 0.02),
                new EntityRenderLayers.Model(new EntityRenderLayer.ModelDef(
                        Identifier.parse("coe:textures/entity/prop/ball"), 0.55F, 0.0F, 1.5F, true))));

        // ── coe:entity_render/ball_lightning：电球（天雷之珠外观）──
        // 粒子层用电火花（放电感的来源）；模型层是电球贴图（深蓝本体 + 白色电弧亮斑）。
        // 命名遵循 ball_<damageType> 约定：SkillFactory.ball(damageType) 按此派生，新增元素只加数据。
        EntityRenderDef ballLightningRender = ballElementRender(ParticleTypes.ELECTRIC_SPARK, "lightning", ballRenders);

        // ── coe:entity_render/orb：通用驻场能量球（缓慢自转 + 悬浮光环）──
        // 与飞行球（ball）区分：Orb 是「原地悬着的球」，故自转更慢（0.8°/tick）、
        // 环半径更大（表现「作用范围」而非「本体拖尾」）。
        EntityRenderDef orbRender = new EntityRenderDef(List.of(
                new EntityRenderLayers.Ring(ParticleTypes.GLOW, 14, 0.8, 0.1, 4, 0.015),
                new EntityRenderLayers.Model(new EntityRenderLayer.ModelDef(
                        Identifier.parse("coe:textures/entity/prop/orb"), 0.7F, 0.15F, 0.8F, true))));

        // ── coe:entity_render/frost_bomb：驻场寒霜球（雪片环 + orb 外观，缓慢自转）──
        // 复用 coe:orb 贴图（零新增贴图，R2 不回归）；粒子换 SNOWFLAKE 以区分元素气质。
        EntityRenderDef frostBombRender = new EntityRenderDef(List.of(
                new EntityRenderLayers.Ring(ParticleTypes.SNOWFLAKE, 14, 0.8, 0.1, 4, 0.015),
                new EntityRenderLayers.Model(new EntityRenderLayer.ModelDef(
                        Identifier.parse("coe:textures/entity/prop/orb"), 0.7F, 0.15F, 0.8F, true))));

        // 元素 Orb 外观（coe:entity_render/orb_<damageType>）：
        // 约定式派生（同 ball_<damageType>）：OrbDelivery 的 damageType 即渲染 id，
        // 新增一种元素只在此加一份数据 —— SkillFactory 零改动。
        // 只生成**确有技能使用**的两种（闪电=风暴漩涡、火焰=日曜球）：
        // 未被任何实体/技能引用的外观数据是孤儿数据（R1），故不预置冷/混沌。
        EntityRenderDef orbLightningRender = orbElementRender(ParticleTypes.ELECTRIC_SPARK, "lightning", orbRenders);
        EntityRenderDef orbFireRender = orbElementRender(ParticleTypes.FLAME, "fire", orbRenders);

        // ── 元素投射物外观（coe:entity_render/{fire,cold,lightning,chaos}）──
        // 约定式派生：投射物技能的 damageType 即渲染 id（coe:entity_render/<damageType>），
        // 故新增一种元素只需在此加一份数据并登记进 ELEMENT_RENDER_NAMES —— SkillFactory 零改动；
        // 未匹配者（如 physical）技能侧不覆写 render，由 coe:entity/arrow 自带外观兜底（可见）。
        // 贴图是「元素配色的箭矢」（火箭/冰箭/雷箭/混沌箭），沿用 coe:skill_arrow 的 3D 箭矢模型。
        EntityRenderDef fireRender = elementRender(ParticleTypes.FLAME, "fire", elementRenders);
        EntityRenderDef coldRender = elementRender(ParticleTypes.SNOWFLAKE, "cold", elementRenders);
        EntityRenderDef lightningRender = elementRender(ParticleTypes.ELECTRIC_SPARK, "lightning", elementRenders);
        EntityRenderDef chaosRender = elementRender(ParticleTypes.SCULK_SOUL, "chaos", elementRenders);

        // 【三族名单的同步自检】
        // 旧实现是一句 assert，在本构建下从不触发：datagen 走 runs.data，而 build.gradle
        // 没给它配 jvmArgs/enableAssertions（详见 requireRendersInSync 的说明）。
        // 且它只拿 set 字面量与另一个 set 字面量比较，发现不了真实渲染数据的增减。
        // 改为把本次真正生成的名字收集起来做硬校验 ——
        // 名单多一项：技能会派生出查不到的 render id，实体不可见；
        // 名单少一项：登记了元素却没生成数据，该外观永远拿不到。
        requireRendersInSync("ELEMENT_RENDER_NAMES", ELEMENT_RENDER_NAMES, elementRenders);
        requireRendersInSync("BALL_RENDER_NAMES", BALL_RENDER_NAMES, ballRenders);
        requireRendersInSync("ORB_RENDER_NAMES", ORB_RENDER_NAMES, orbRenders);

        // ── 墙体外观（coe:entity_render/wall · wall_fire · wall_cold）──
        // 复用既有 orb / orb_fire 贴图（零新增贴图，R2 不回归）；粒子环 + 竖向光柱表现「屏障感」。
        EntityRenderDef wallRender = wallRenderDef(ParticleTypes.GLOW, "orb");
        EntityRenderDef wallFireRender = wallRenderDef(ParticleTypes.FLAME, "orb_fire");
        EntityRenderDef wallColdRender = wallRenderDef(ParticleTypes.SNOWFLAKE, "orb");

        // ── coe:entity_render/fissure：地面裂隙（贴地尘环 + 地表尘点 + 贴地立牌）──
        // 形态依据 coe:entity/fissure：static + pulse(40)，半径 0.6，是「裂开的地缝」而非柱状物，
        // 故不取 Pillar（柱状层留给战旗 / 图腾 / 墙一类竖向存在，见 smoke / wallRenderDef），
        // 只取贴地环 + 尘点，与危害物（smoke：环 + 烟柱 + 1.2 倍立牌）在轮廓上区分。
        // 环参数沿用同文件既有取值（samples 14 / radius_scale 1.0，见 orb · wall），
        // 仅把 y_offset 压到 0.05 贴地（对照 hazard 的 0.1）、interval 放缓到 8（贴合 pulse 40 tick 的节奏）。
        // 尘点取 POOF：本仓唯一「小尘土团」粒子，表现地缝扬尘；贴图沿用 coe:hazard（唯一的地面物件贴图）——
        // 缩放 0.45 介于铁蒺藜（0.3 贴地尖刺）与烟雾危害物（1.2 立牌）之间：裂隙横跨整个作用半径但仍是贴地扁平物。
        // 零新增贴图资源（R2 不回归）。
        EntityRenderDef fissureRender = new EntityRenderDef(List.of(
                new EntityRenderLayers.Ring(ParticleTypes.SMOKE, 14, 1.0, 0.05, 8, 0.01),
                new EntityRenderLayers.Point(ParticleTypes.POOF, 2, 0.15, 0.05, 8, 0.01),
                new EntityRenderLayers.Model(new EntityRenderLayer.ModelDef(
                        Identifier.parse("coe:textures/entity/prop/hazard"), 0.45F, 0.0F, 0.0F, true))));

        // ── coe:entity_render/toxic_vine：毒藤（细藤柱 + 混沌粒子 + 贴地作用环）──
        // 形态依据 coe:entity/skill_vine：static + pulse(30)，stages 非空（sprout→grown→bloom），
        // 是「越长越高的植物」，故取 Pillar（竖向层：战旗旗杆 / 图腾 / 奉献尖刺的驻场存在感）。
        // Pillar 参数对照既有竖向层收窄：radius_scale 0.35（细藤）vs smoke 1.0（浓烟柱）/ wall 0.6（光柱），
        // height_scale 1.5 沿用 smoke 的既有取值。
        // 粒子取 SCULK_SOUL：本仓 chaos 元素的既定粒子（chaos.json 即此），毒 / 混沌同气质；
        // 模型层沿用 coe:chaos 贴图（毒系配色），零新增贴图资源（R2 不回归）。
        // 不自转（0.0F）：藤蔓是扎根植物，与飞行弹体（ball 1.5 / grenade 8.0）区分。
        EntityRenderDef toxicVineRender = new EntityRenderDef(List.of(
                new EntityRenderLayers.Ring(ParticleTypes.SCULK_SOUL, 12, 1.0, 0.05, 6, 0.02),
                new EntityRenderLayers.Pillar(ParticleTypes.SCULK_SOUL, 1.5, 8, 0.35, 6, 0.02),
                new EntityRenderLayers.Model(new EntityRenderLayer.ModelDef(
                        Identifier.parse("coe:textures/entity/prop/chaos"), 0.8F, 0.0F, 0.0F, true))));

        return CompletableFuture.allOf(
                encode(cache, entityDir.resolve("remnant.json"), EntityDef.CODEC, remnant),
                encode(cache, entityDir.resolve("hazard.json"), EntityDef.CODEC, hazard),
                encode(cache, entityDir.resolve("caltrop.json"), EntityDef.CODEC, caltrop),
                encode(cache, entityDir.resolve("arrow.json"), EntityDef.CODEC, arrow),
                encode(cache, entityDir.resolve("grenade.json"), EntityDef.CODEC, grenade),
                encode(cache, entityDir.resolve("ball.json"), EntityDef.CODEC, ball),
                encode(cache, entityDir.resolve("orb.json"), EntityDef.CODEC, orb),
                encode(cache, entityDir.resolve("frost_bomb.json"), EntityDef.CODEC, frostBomb),
                encode(cache, entityDir.resolve("sun_orb.json"), EntityDef.CODEC, sunOrb),
                encode(cache, entityDir.resolve("fissure.json"), EntityDef.CODEC, fissure),
                encode(cache, entityDir.resolve("skill_vine.json"), EntityDef.CODEC, plant),
                encode(cache, renderDir.resolve("fissure.json"), EntityRenderDef.CODEC, fissureRender),
                encode(cache, renderDir.resolve("toxic_vine.json"), EntityRenderDef.CODEC, toxicVineRender),
                encode(cache, renderDir.resolve("arcane_blue.json"), EntityRenderDef.CODEC, arcaneBlue),
                encode(cache, renderDir.resolve("smoke.json"), EntityRenderDef.CODEC, smoke),
                encode(cache, renderDir.resolve("caltrop.json"), EntityRenderDef.CODEC, caltropRender),
                encode(cache, renderDir.resolve("arrow.json"), EntityRenderDef.CODEC, arrowRender),
                encode(cache, renderDir.resolve("grenade.json"), EntityRenderDef.CODEC, grenadeRender),
                encode(cache, renderDir.resolve("ball.json"), EntityRenderDef.CODEC, ballRender),
                encode(cache, renderDir.resolve("ball_lightning.json"), EntityRenderDef.CODEC, ballLightningRender),
                encode(cache, renderDir.resolve("orb.json"), EntityRenderDef.CODEC, orbRender),
                encode(cache, renderDir.resolve("frost_bomb.json"), EntityRenderDef.CODEC, frostBombRender),
                encode(cache, renderDir.resolve("orb_lightning.json"), EntityRenderDef.CODEC, orbLightningRender),
                encode(cache, renderDir.resolve("orb_fire.json"), EntityRenderDef.CODEC, orbFireRender),
                encode(cache, renderDir.resolve("fire.json"), EntityRenderDef.CODEC, fireRender),
                encode(cache, renderDir.resolve("cold.json"), EntityRenderDef.CODEC, coldRender),
                encode(cache, renderDir.resolve("lightning.json"), EntityRenderDef.CODEC, lightningRender),
                encode(cache, renderDir.resolve("chaos.json"), EntityRenderDef.CODEC, chaosRender),
                encode(cache, entityDir.resolve("wall.json"), EntityDef.CODEC, wall),
                encode(cache, entityDir.resolve("wall_fire.json"), EntityDef.CODEC, wallFire),
                encode(cache, entityDir.resolve("wall_cold.json"), EntityDef.CODEC, wallCold),
                encode(cache, renderDir.resolve("wall.json"), EntityRenderDef.CODEC, wallRender),
                encode(cache, renderDir.resolve("wall_fire.json"), EntityRenderDef.CODEC, wallFireRender),
                encode(cache, renderDir.resolve("wall_cold.json"), EntityRenderDef.CODEC, wallColdRender));
    }

    /**
     * 元素 Orb 外观：外层悬浮光环（元素粒子）+ 模型层（元素配色球贴图）。
     *
     * <p><b>为什么与飞行球（{@code ball_}）分开</b>：Orb 是驻场的「领域感」球体
     * （大而慢转、带范围光环），球是飞行弹体（小而拖尾）；形状与气质都不同，混用会让
     * 驻场球看起来像一颗飞过来的子弹。
     */
    /**
     * 墙体形态（数据驱动）：Wall 运动（physics 真实碰撞）+ pulse 对敌周期点燃 ON_TICK。
     * type 统一为 coe:wall（WALL EntityType 承载），仅 render 与标签随元素区分。
     */
    private static EntityDef wallDef(Identifier render, List<String> tags) {
        return new EntityDef(
                Identifier.parse("coe:wall"),
                Optional.empty(),
                2.5F,
                render,
                tags,
                Optional.empty(),  // spawn_sound
                Optional.empty(),  // hit_sound
                List.of(new EntityTriggers.PhaseOnHit(LifecycleTrigger.ON_TICK)),
                List.of(),  // on_spawn
                List.of(),
                EntityMotions.Wall.INSTANCE,
                List.of(new EntityBehaviors.Pulse(20, EntityBehavior.TARGET_ENEMY)),
                List.of(),
                List.of(),
                List.of());
    }

    /** 墙体外观：元素粒子环 + 竖向光柱 + 既有 orb 模型（零新增贴图）。 */
    private static EntityRenderDef wallRenderDef(net.minecraft.core.particles.ParticleOptions particle, String model) {
        return new EntityRenderDef(List.of(
                new EntityRenderLayers.Ring(particle, 14, 1.0, 0.1, 4, 0.02),
                new EntityRenderLayers.Pillar(particle, 2.5, 10, 0.6, 6, 0.02),
                new EntityRenderLayers.Model(new EntityRenderLayer.ModelDef(
                        Identifier.parse("coe:textures/entity/prop/" + model), 1.0F, 0.0F, 0.0F, false))));
    }

    private static EntityRenderDef orbElementRender(net.minecraft.core.particles.ParticleOptions particle,
                                                    String element, Set<String> generated) {
        generated.add(element);
        return new EntityRenderDef(List.of(
                new EntityRenderLayers.Ring(particle, 12, 0.7, 0.1, 4, 0.015),
                new EntityRenderLayers.Model(new EntityRenderLayer.ModelDef(
                        Identifier.parse("coe:textures/entity/prop/orb_" + element), 0.75F, 0.15F, 0.8F, true))));
    }

    /**
     * 元素投射物外观：飞行粒子（元素粒子）+ 模型层（元素配色箭矢贴图）。
     *
     * <p><b>为什么粒子与模型都给</b>：投射物既要有拖尾（飞行中可见），也要有本体；
     * 二者都由同一份 {@code coe:entity_render} 数据承载，换外观只改这一处。
     */
    private static EntityRenderDef elementRender(net.minecraft.core.particles.ParticleOptions particle,
                                                  String element, Set<String> generated) {
        generated.add(element);
        return new EntityRenderDef(List.of(
                new EntityRenderLayers.Point(particle, 1, 0.08, 0.0, 1, 0.0),
                new EntityRenderLayers.Model(new EntityRenderLayer.ModelDef(
                        Identifier.parse("coe:textures/entity/prop/" + element), 1.0F, 0.0F, 0.0F, false))));
    }

    /**
     * 元素飞行球外观（{@code coe:entity_render/ball_<element>}）：球体贴图 + 元素粒子环。
     * 层参数沿用 {@code coe:entity_render/ball} 的既有意径（环 10 段 / 自转 1.5°），只换粒子与贴图。
     */
    private static EntityRenderDef ballElementRender(net.minecraft.core.particles.ParticleOptions particle,
                                                       String element, Set<String> generated) {
        generated.add(element);
        return new EntityRenderDef(List.of(
                new EntityRenderLayers.Ring(particle, 10, 0.35, 0.1, 2, 0.02),
                new EntityRenderLayers.Model(new EntityRenderLayer.ModelDef(
                        Identifier.parse("coe:textures/entity/prop/ball_" + element), 0.6F, 0.0F, 1.5F, true))));
    }

    /**
     * gen 期硬校验：技能侧的白名单必须与本次真正写出的渲染数据<b>逐项相等</b>。
     *
     * <p><b>为什么必须是硬失败而不是 {@code assert}</b>：断言要 {@code -ea}，
     * 而本工程 datagen 实际跑的 {@code runs.data} 没有配 {@code jvmArgs} 或
     * {@code enableAssertions} —— {@code build.gradle} 的 {@code neoForge.runs} 里只有
     * {@code systemProperty}，{@code gradle.properties} 里唯一的 jvmargs 是
     * {@code org.gradle.jvmargs=-Xmx2G}，那是 Gradle 守护进程自己的堆，不作用于 JavaExec。
     * 故 datagen 期 {@code assert} 永不触发，那层校验等于没写。
     *
     * <p><b>为什么必须抛出而非静默兜底</b>：名单多一项，技能就会派生出
     * {@code coe:entity_render/*} 查不到的悬空 id，渲染层 model 解析为 null，实体彻底不可见；
     * 靠兜底吞掉只会让数据错误以「外观没生效」的形式潜伏在产物里。
     */
    private static void requireRendersInSync(String label, Set<String> declared, Set<String> generated) {
        if (declared.equals(Set.copyOf(generated))) return;
        Set<String> missing = new LinkedHashSet<>(declared);
        missing.removeAll(generated);
        Set<String> orphan = new LinkedHashSet<>(generated);
        orphan.removeAll(declared);
        throw new IllegalStateException(label + " 与实际生成的 coe:entity_render 数据不符："
                + "已登记但无渲染数据=" + missing + "（技能会派生出悬空 render id → 实体不可见），"
                + "有渲染数据但未登记=" + orphan + "（技能永远拿不到该外观）。"
                + "修法：在本文件补一份同名渲染数据并登记，或把该元素从名单中移除。");
    }

    private static <T> CompletableFuture<?> encode(CachedOutput cache, Path file, Codec<T> codec, T value) {
        DataResult<JsonElement> result = codec.encodeStart(JsonOps.INSTANCE, value);
        JsonElement json = result.result().orElseThrow(() ->
                new IllegalStateException("无法编码 entity 数据: " + file));
        return DataProvider.saveStable(cache, json, file);
    }

    @Override
    public String getName() {
        return "Entity Data";
    }
}
