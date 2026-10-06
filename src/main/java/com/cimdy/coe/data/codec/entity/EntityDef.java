package com.cimdy.coe.data.codec.entity;

import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.cimdy.coe.data.codec.entity.render.EntityRenderDef;
import com.cimdy.coe.data.codec.entity.render.EntityRenderDefs;
import com.cimdy.coe.data.codec.entity.trigger.EntityStage;
import com.cimdy.coe.data.codec.entity.trigger.EntityTrigger;
import com.cimdy.coe.data.codec.entity.trigger.EntityTriggers;
import com.cimdy.coe.data.gen.asset.EntityProvider;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.entity.behavior.EntityBehavior;
import com.cimdy.coe.data.codec.entity.behavior.EntityMotion;
import com.cimdy.coe.data.codec.entity.behavior.EntityMotions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 实体定义 —— 技能在世界中「放置出来的那个东西」的<b>完整描述</b>：
 * 由哪个 {@code EntityType} 承载、长什么样、叫什么、<b>怎么动、何时对谁生效</b>。
 *
 * <p><b>与技能体系严格同构</b>：
 * <pre>{@code
 *   Skill     （完整定义）  +  SkillRead  （引用 + vars 覆写）
 *   EntityDef （完整定义）  +  EntityRead （引用 + render / name 覆写）
 * }</pre>
 * 于是「<b>同一实体类型、被多个技能生成、呈现不同外观</b>」成立 —— 这正是本层存在的理由：
 * <pre>{@code
 *   coe:entity/remnant            —— 行为：施放者走近即拾取、拾取后消失
 *     ├─ Mana Remnants   → render=coe:arcane_blue，name=魔力残片
 *     ├─ Blood Remnant   → render=coe:blood_red，   name=血之残片
 *     └─ Harmony Remnant → render=coe:…             （辅助宝石改外观亦走同一入口）
 * }</pre>
 * 玩家感知到的是三种不同的东西，底层却是同一个 {@code coe:prop} 实体类型 ——
 * 即「用数据制造新实体的错觉」，而非为每种外观注册一个新实体类型。
 *
 * <h2>命名空间（技能层如何找到它）</h2>
 * 定义位于 {@code data/<ns>/coe/entity/*.json}（运行时注册表 {@link EntityDefs}），
 * 外观位于 {@code coe/entity_render/*.json}（{@link EntityRenderDefs}）。
 * 二者是<b>独立于技能</b>的命名空间，技能层不内联定义实体，只通过
 * {@link EntityRead} 按 id 引用并覆写 —— 与「技能引用预设」是同一范式。
 *
 * <h2>为何用 id / 字符串而非枚举</h2>
 * {@code type} / {@code render} / {@code motion} / {@code behavior} 皆为<b>分类维度</b>——
 * 按项目铁律，分类维度<b>禁止硬编码枚举</b>，必须是 id / 字符串标签 + 数据驱动层级表，
 * 以便新增一种实体或外观只加数据、不改 Java。
 *
 * <h2>实体 / 行为拆分（架构铁律）</h2>
 * 承载实体<b>只有一个类</b>（{@code SkillEntity}），注册为多个 {@code EntityType}
 * （{@code coe:prop} / {@code coe:orb} / {@code coe:grenade} / {@code coe:skill_ball} …）——
 * 「独立语义」靠<b>类型</b>表达（供事件与目标选择器判定），
 * 「怎么动 / 何时生效」全部靠本定义的两组组件表达：
 * <ul>
 *   <li>{@link #motion()} —— {@code coe:static}（静止）/ {@code coe:ballistic}（抛射）/
 *       {@code coe:external}（外部驱动，如原版箭矢）；</li>
 *   <li>{@link #behavior()} —— {@code coe:contact}（接触触发）/ {@code coe:pulse}（周期脉冲）/
 *       {@code coe:detonate}（引信引爆）/ {@code coe:none}（无自主行为）。</li>
 * </ul>
 *
 * <p><b>行为是列表</b>：宿主每 tick 依次驱动全部行为，任一返回「已完成」即结束实体。
 * 若只允许一个行为槽，「周期施加 + 到期引爆」这类复合形态就只能靠新写一个专属行为来表达
 * （等于把组合烤回 Java），故此处必须是列表 —— 组合发生在<b>数据</b>里。
 * 运动则保持单值：一个物体同一时刻只有一种运动方式，列表化只会制造歧义。
 *
 * <p>组件经 {@code EntityRuntime} 契约工作（不认识宿主类），运行态走宿主的<b>键值槽</b>，
 * 故本定义与宿主类都不再出现「只有某一种形态会读」的专属字段（那些字段必然是死字段）。
 *
 * <h2>每个字段都必须有真实读点（禁死字段）</h2>
 * 缩放刻意<b>不在此声明</b> —— 它由 {@link EntityRenderDef} 的模型层（{@code ModelDef.scale}）承担：
 * 客户端只同步 render id，拿不到本定义，故放这里的缩放恒无读点（死字段），已移除。
 *
 * @param type      承载本定义的实体类型 id（如 {@code coe:prop}）
 * @param name      显示名；为空时沿用实体类型默认名
 * @param boxHeight 包围盒高度（方块）
 * @param render    渲染定义 id（引用 {@code coe:entity_render/<name>}）
 * @param tags      形态自带的 stat 标签（{@code remnant} / {@code hazard} / {@code grenade} …）；
 *                  是投递 {@code statTags()} 的<b>真值源</b>（见 {@link EntityRead#statTags}）
 * @param spawnSound 生成音效（可选）：实体被投递<b>创建</b>时于实体位置播放一次（镜像 {@code Skill.cast_sound}）。
 *                  指向原版 / 本模组 {@code SoundEvent} 的 id；<b>缺省不发声</b>（不引入硬编码默认值，
 *                  未配置的实体维持现状）。重载路径不触发（见 {@link com.cimdy.coe.entity.custom.skill.SkillEntity#configure}）。
 * @param hitSound   命中音效（可选）：实体<b>对目标生效</b>（{@code on_trigger} 节拍）时于目标位置播放一次。
 *                  与 {@code spawn_sound} 同源、缺省不发声；脉冲类行为会每节拍播放（数据驱动，用户按需配置）。
 * @param onTrigger 触发后效果列表（点燃 ON_HIT / 发射事件 / 记录事件 / 施加状态 …）
 * @param onSpawn   生成效果通道：实体被投递<b>创建</b>时执行的一次性效果列表（生成爆发 / 生成音效 / 留残迹 …）。
 *                  缺省空 → 不产生任何生成期行为。与 {@code on_trigger} 独立：生成爆发只播一次，
 *                  不被 {@code interval} 节流吞噬（挂 {@link EntityTriggers.EmitVfx} 即一次性粒子爆发）。
 *                  重载路径不触发（仅 {@code configure} 入口），故既有实体零行为变化。
 * @param onExpire  第二效果通道：实体<b>到期 / 引爆</b>时执行的效果列表（点燃 ON_EXPIRE）。
 *                  缺省空 → 宿主回退 {@code onTrigger}，兼容仅有单一通道的既有形态（如手雷）。
 *                  复合形态（{@code pulse ⊕ detonate}）用它让周期脉冲与引爆分别点燃不同的技能阶段动作。
 * @param motion    运动组件（怎么动）
 * @param behavior  行为组件列表（何时对谁执行 {@code onTrigger}）
 * @param onNearbyCast 第三效果通道：玩家在<b>本实体作用半径内</b>施放法术时执行的效果列表
 *                     （点燃玩家阶段的指定触发点）。仅 Orb 类实体（如 Orb of Storms）置非空，
 *                     使「在球附近施法 → 球额外放电」成为<b>数据驱动</b>的技能动作，而非切换施法者主体——
 *                     {@code caster} 仍是玩家（stat 作用域不变），{@code prop} 是本实体（由球放出）。
 *                     缺省空 → 不产生任何行为。
 * @param stages       <b>阶段序列</b>（可选）：本实体被<b>阶段式实体</b>设施（{@code data.helper.engine.EntityStages}）
 *                     驱动的有序阶段列表（见 {@link EntityStage}）。缺省空 → 本实体<b>没有阶段</b>，
 *                     {@code EntityStages} 的全部 API（推进 / 跳末阶段 / 概率提前推进）对它恒 no-op ——
 *                     这是「既有实体零行为变化」的落点：只有显式声明本字段的实体才进入该设施。
 *                     序列只描述「有没有、几步、每步多久」，<b>不含任何实体类别语义</b>
 *                     （植物长成 / 图腾终形 / 召唤物成熟 / 投射物二次阶段共用同一字段）。
 */
public record EntityDef(
        Identifier type,
        Optional<Component> name,
        float boxHeight,
        Identifier render,
        List<String> tags,
        Optional<Identifier> spawnSound,
        Optional<Identifier> hitSound,
        List<EntityTrigger> onTrigger,
        List<EntityTrigger> onSpawn,
        List<EntityTrigger> onExpire,
        EntityMotion motion,
        List<EntityBehavior> behavior,
        List<EntityTrigger> onAura,
        List<EntityTrigger> onNearbyCast,
        List<EntityStage> stages
) {
    /** 默认承载类型：驻场道具（{@code coe:prop}）。 */
    public static final Identifier DEFAULT_TYPE =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "prop");

    public static final Codec<EntityDef> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.optionalFieldOf("type", DEFAULT_TYPE).forGetter(EntityDef::type),
            ComponentSerialization.CODEC.optionalFieldOf("name").forGetter(EntityDef::name),
            Codec.FLOAT.optionalFieldOf("box_height", 0.4F).forGetter(EntityDef::boxHeight),
            Identifier.CODEC.fieldOf("render").forGetter(EntityDef::render),
            Codec.STRING.listOf().optionalFieldOf("tags", List.of()).forGetter(EntityDef::tags),
            Identifier.CODEC.optionalFieldOf("spawn_sound").forGetter(EntityDef::spawnSound),
            Identifier.CODEC.optionalFieldOf("hit_sound").forGetter(EntityDef::hitSound),
            EntityTrigger.CODEC.listOf()
                    .optionalFieldOf("on_trigger", List.of(EntityTriggers.PhaseOnHit.INSTANCE))
                    .forGetter(EntityDef::onTrigger),
            EntityTrigger.CODEC.listOf()
                    .optionalFieldOf("on_spawn", List.of())
                    .forGetter(EntityDef::onSpawn),
            EntityTrigger.CODEC.listOf()
                    .optionalFieldOf("on_expire", List.of())
                    .forGetter(EntityDef::onExpire),
            EntityMotion.CODEC.optionalFieldOf("motion", EntityMotions.Static.INSTANCE)
                    .forGetter(EntityDef::motion),
            EntityBehavior.CODEC.listOf().optionalFieldOf("behavior", List.of())
                    .forGetter(EntityDef::behavior),
            EntityTrigger.CODEC.listOf().optionalFieldOf("on_aura", List.of())
                    .forGetter(EntityDef::onAura),
            EntityTrigger.CODEC.listOf().optionalFieldOf("on_nearby_cast", List.of())
                    .forGetter(EntityDef::onNearbyCast),
            // 阶段序列（可选）：缺省空 = 本实体无阶段 → 阶段式实体设施对它零行为（见 EntityStages）
            EntityStage.CODEC.listOf().optionalFieldOf("stages", List.of()).forGetter(EntityDef::stages)
    ).apply(i, EntityDef::new));

    /** 带渲染覆写的派生（供 {@link EntityRead#resolve()} 使用）：阶段序列等与外观无关的字段全部原样保留。 */
    public EntityDef withRender(Identifier newRender) {
        return new EntityDef(type, name, boxHeight, newRender, tags, spawnSound, hitSound, onTrigger, onSpawn,
                onExpire, motion, behavior, onAura, onNearbyCast, stages);
    }

    /** 带名称覆写的派生（供 {@link EntityRead#resolve()} 使用）：阶段序列原样保留。 */
    public EntityDef withName(Component newName) {
        return new EntityDef(type, Optional.of(newName), boxHeight, render, tags, spawnSound, hitSound,
                onTrigger, onSpawn, onExpire, motion, behavior, onAura, onNearbyCast, stages);
    }

    /**
     * 次级构造器：第三效果通道 {@code onNearbyCast} 默认空（无附近施法响应）。
     * 多数形态不需要「附近施法响应」，仅 {@code coe:orb}（Orb of Storms）在 {@code EntityProvider} 显式置非空。
     * 既保留既有 9 参调用点零改动，又让能力成为数据驱动开关（非硬编码枚举）：
     * 门控只看列表是否非空，frostBomb / sunOrb 等天然保持空 → 不产生任何行为。
     */
    public EntityDef(Identifier type, Optional<Component> name, float boxHeight, Identifier render,
                    List<String> tags, List<EntityTrigger> onTrigger, List<EntityTrigger> onExpire,
                    EntityMotion motion, List<EntityBehavior> behavior) {
        this(type, name, boxHeight, render, tags, Optional.empty(), Optional.empty(), onTrigger, List.of(),
                onExpire, motion, behavior, List.of(), List.of(), List.of());
    }

    /**
     * 解析引用的渲染定义；缺失表示「无渲染」（粒子层不发射、模型层不绘制）。
     *
     * <p>这是渲染定义的<b>唯一解析入口</b>：载体在装配时经本方法取渲染定义，
     * 不各自查表，避免同一份数据两处解析口径。
     */
    public Optional<EntityRenderDef> renderDef() {
        return EntityRenderDefs.get(render);
    }

    /** 形态自带的 stat 标签（不可变视图）。 */
    public Set<String> tagSet() {
        return Set.copyOf(tags);
    }
}
