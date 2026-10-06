package com.cimdy.coe.entity.custom.skill;

import com.cimdy.coe.data.codec.skill.phase.event.trigger.SkillCastEvent;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.entity.EntityDef;
import com.cimdy.coe.data.codec.entity.EntityRead;
import com.cimdy.coe.data.codec.entity.behavior.EntityMotions;
import com.cimdy.coe.data.codec.entity.render.EntityRenderDef;
import com.cimdy.coe.data.codec.entity.render.EntityRenderLayer;
import com.cimdy.coe.data.codec.entity.trigger.EntityTrigger;
import com.cimdy.coe.data.codec.entity.trigger.EntityTriggers;
import com.cimdy.coe.data.codec.entity.behavior.EntityBehavior;
import com.cimdy.coe.data.codec.entity.behavior.EntityRuntime;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.SkillPhaseInstanceTracker;
import com.cimdy.coe.data.codec.support.meta.Camp;
import com.cimdy.coe.data.helper.engine.entity.EntityStages;
import com.cimdy.coe.data.codec.skill.context.SkillContext.HitContext;
import com.cimdy.coe.data.codec.skill.phase.action.control.LifecycleTrigger;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.ProjectileDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.collision.Stop;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryRuntimeContext;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryTracker;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.SkillPhaseInstanceTracker;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.projectile.ProjectileDeliveryExecutor;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.context.node.EntityQuery;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.helper.combat.DamageHelper;
import com.cimdy.coe.entity.EntityDefCarrier;
import com.cimdy.coe.entity.RenderIdCarrier;
import com.cimdy.coe.registry.AttributeRegistry;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import com.mojang.serialization.Codec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * 技能在世界中的物件<b>唯一注册基类</b> —— 残片 / 危害物 / Orb / 手雷 / 飞行球
 * 全部共用本类，<b>行为差异完全由 {@code coe:entity} 数据驱动</b>。
 *
 * <h2>实体 / 行为拆分（架构铁律）</h2>
 * 本类<b>不认识任何具体组件</b>：没有 {@code instanceof Ballistic}、没有「引信」「脉冲间隔」
 * 之类的专属字段。它只做四件与形态无关的事：
 * <ol>
 *   <li><b>存在</b>：同步 render id / 半径，按 {@link EntityDef#boxHeight()}（与运动方式）算 AABB；</li>
 *   <li><b>驱动</b>：每 tick 把 {@link EntityRuntime} 视图交给运动组件（怎么动）与<b>每个</b>行为组件
 *       （何时对谁生效），任一返回「已完成」即销毁；</li>
 *   <li><b>执行</b>：按 {@link EntityDef#onTrigger()} 数据对目标执行效果（默认点燃阶段 ON_HIT）；</li>
 *   <li><b>存档</b>：形态以 {@link EntityRead} 入档，运行态以<b>键值槽</b>还原。</li>
 * </ol>
 *
 * <h2>解耦的落点：{@link EntityRuntime} + 键值运行态</h2>
 * 组件签名里出现宿主类 ⇒ 数据 codec 层反向依赖实体层 ⇒ 「加形态 = 加数据」退化为「加形态 = 改实体类」。
 * 故组件只看 {@link EntityRuntime} 契约，宿主以 {@link Scoped} 视图按组件实例<b>自动分域</b>：
 * 组件里写 {@code rt.timer("fuse")}，实际落在 {@code b0.fuse} —— 同一行为配多次也互不踩踏，
 * 宿主不必为任何一种行为预留字段。
 *
 * <p>于是：一种新形态 = 一份数据（运动组件 ⊕ 行为组件列表），<b>零 Java 改动</b>；
 * 「独立语义」则由<b>注册不同的 {@code EntityType}</b> 表达（{@code coe:prop} / {@code coe:orb} /
 * {@code coe:grenade} / {@code coe:skill_ball}），事件与目标选择器可直接按类型判定，
 * 而行为仍然来自数据 —— 两个诉求不必再靠「写一个子类」来同时满足。
 *
 * <h2>形态组合表（数据即全部真相）</h2>
 * <pre>{@code
 *   coe:remnant      static    + [contact(self)]   —— 施放者走近即拾取
 *   coe:hazard       static    + [contact(enemy)]  —— 敌人穿越即触发
 *   coe:orb          static    + [pulse]           —— 原地驻场周期放能（风暴漩涡 / 日曜球）
 *   coe:grenade      ballistic + [detonate]        —— 抛射 → 落地 → 引信 → 引爆
 *   coe:ball         ballistic + [pulse]           —— 边飞边放能（天雷之珠）
 *   coe:arrow        external  + [none]            —— 原版箭矢（外部系统驱动）
 * }</pre>
 * 行为是<b>列表</b>，故「周期施加 + 到期引爆」（{@code [pulse, detonate]}）这类复合形态
 * 也是纯数据组合 —— 不必为它新写一个专属行为。
 *
 * <h2>施法者契约</h2>
 * 本实体不是施法者，只持 owner —— 一切效果以 owner 为 caster 回调技能阶段
 * （与投射物「产生效果时去找 owner」同一条规则）。
 *
 * <h2>生命周期（{@link #tick()}）</h2>
 * 运动 → 各行为 → 到期 → 粒子。任一步返回「应销毁」即 {@code discard()}；
 * 销毁时经 {@link SkillPhaseInstanceTracker#release} 归还阶段引用计数。
 *
 * <p><b>存档重载语义</b>：形态还原（客户端可见），但 {@code phaseId} 不持久化 ⇒
 * 阶段实例已不存在 ⇒ {@link #fireTriggers(LivingEntity)} 一律不触发（避免无结算的假触发）。
 */
public class SkillEntity extends Entity implements EntityDefCarrier, RenderIdCarrier {
    private static final EntityDataAccessor<String> DATA_RENDER =
            SynchedEntityData.defineId(SkillEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> DATA_RADIUS =
            SynchedEntityData.defineId(SkillEntity.class, EntityDataSerializers.FLOAT);
    /**
     * 技能皮肤键（技能 id）：装配 / 读档经 {@link #applyRead} 写入，由 SynchedEntityData 同步到客户端，
     * 使客户端也能按技能 id 走 {@code SkillSkinDefs} 染色通道（{@link #skinKey()} 的客户端兜底）。
     * 与 {@link #DATA_RENDER} 同用 STRING 序列化（技能 id 即 {@code EntityRead.render()} 的字符串形式）。
     */
    private static final EntityDataAccessor<String> DATA_SKIN_KEY =
            SynchedEntityData.defineId(SkillEntity.class, EntityDataSerializers.STRING);

    /** 默认作用半径（格），未装配时的兜底值。 */
    private static final float DEFAULT_RADIUS = 1.5F;
    /** 默认存活时长（tick）。 */
    private static final int DEFAULT_LIFETIME = 200;
    /** 默认包围盒高度（方块）。 */
    private static final float DEFAULT_BOX_HEIGHT = 0.4F;
    /** 粒子刷新间隔（tick）：宿主通用调度，不属任何形态。 */
    private static final int PARTICLE_INTERVAL = 3;
    /** 附近施法扫描半径（格）：玩家施法时向外搜索 Orb 的兜底范围（覆盖风暴漩涡半径 + 范围词缀放大）。 */
    private static final double NEARBY_CAST_SEARCH = 10.0;

    private static final Codec<Map<String, Integer>> TIMERS_CODEC =
            Codec.unboundedMap(Codec.STRING, Codec.INT);
    private static final Codec<Map<String, Double>> PARAMS_CODEC =
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE);

    /** 已解析的形态定义（仅服务端持有；客户端靠同步的 render id 渲染）。 */
    @Nullable
    private EntityDef def;
    /** 装配来源（引用 + 覆写）：持久化的唯一依据，见 {@link EntityDefCarrier}。 */
    @Nullable
    private EntityRead read;
    /** 已解析的渲染定义（服务端发射粒子用）。 */
    @Nullable
    private EntityRenderDef renderDef;

    /** 运动组件的运行时视图（{@link #def} 变更时重建）。 */
    private EntityRuntime motionView = new Scoped(this, "motion.");
    /** 行为槽（行为 + 其按实例分域的运行时视图）。 */
    private List<Slot> slots = List.of();

    @Nullable
    private EntityReference<LivingEntity> owner;
    /** 施放者 UUID（= owner.getUUID()）；重载后不持久化。 */
    @Nullable
    protected UUID casterId;
    /** 技能阶段 UUID；重载后阶段实例已不存在，不持久化。 */
    @Nullable
    protected UUID phaseId;

    /** 存活时长（tick），到期自毁。 */
    private int lifetime = DEFAULT_LIFETIME;
    /** 作用半径（格）：由投递计算（含辅助宝石与范围词缀）后装配进来。 */
    private float radius = DEFAULT_RADIUS;
    /**
     * 运动是否已停止（落地 / 撞墙后停住）—— 由运动组件写入、由引信类行为读取。
     *
     * <p>这是宿主唯一保留的<b>具名运动状态</b>：它描述的是「弹体现在停住了吗」这一通用事实，
     * 而非某一运动组件的私有概念（见 {@link EntityRuntime#motionStopped()}）。
     */
    private boolean motionStopped = false;

    /** 组件计时器（键已按实例分域，如 {@code b0.fuse}）。 */
    private final Map<String, Integer> timers = new HashMap<>();
    /** 组件运行参数：投递注入的全局参数（无前缀）可被各组件 {@code param()} 回退读到。 */
    private final Map<String, Double> params = new HashMap<>();

    /**
     * 行为槽：行为与其运行时视图成对保存 —— 视图必须<b>与行为实例一一对应</b>，
     * 才能让同一行为的多个实例各自持有独立运行态。
     */
    private record Slot(EntityBehavior behavior, EntityRuntime runtime) {
    }

    /**
     * 组件的运行时视图 —— 宿主把 {@link EntityRuntime} 的全部能力委托给实体本身，
     * 并给计时器 / 参数加<b>实例前缀</b>（分域），使组件无需自己拼键。
     */
    private record Scoped(SkillEntity host, String prefix) implements EntityRuntime {
        @Override
        public float radius() {
            return this.host.radius();
        }

        @Override
        public float boxHeight() {
            return this.host.boxHeight();
        }

        @Override
        public int tickCount() {
            return this.host.tickCount;
        }

        @Override
        public boolean expired() {
            return this.host.isExpired();
        }

        @Override
        @Nullable
        public LivingEntity owner() {
            return this.host.getOwner();
        }

        @Override
        public Vec3 velocity() {
            return this.host.getDeltaMovement();
        }

        @Override
        public void velocity(Vec3 v) {
            this.host.setDeltaMovement(v);
        }

        /** SELF 只解析方块碰撞：实体一律穿过（球「本体不命中」、手雷不被半空小怪挡下）。 */
        @Override
        public void integrate() {
            this.host.move(MoverType.SELF, this.host.getDeltaMovement());
        }

        @Override
        public boolean blockCollided() {
            return this.host.onGround() || this.host.horizontalCollision || this.host.verticalCollision;
        }

        @Override
        public boolean motionStopped() {
            return this.host.motionStopped;
        }

        @Override
        public void motionStopped(boolean stopped) {
            this.host.motionStopped = stopped;
        }

        @Override
        public List<LivingEntity> targets(float radius, String kind) {
            return this.host.queryTargets(radius, kind);
        }

        @Override
        @Nullable
        public LivingEntity target(float radius, String kind) {
            return this.host.queryTarget(radius, kind);
        }

        @Override
        public void fireTriggers(LivingEntity target) {
            this.host.fireTriggers(target);
        }

        @Override
        public void fireExpire(LivingEntity target) {
            this.host.fireExpire(target);
        }

        @Override
        public void fireAura(LivingEntity target) {
            this.host.fireAura(target);
        }

        @Override
        public void emitRender(float radius) {
            this.host.emitRender(radius);
        }

        @Override
        public int timer(String key) {
            return this.host.timers.getOrDefault(this.prefix + key, 0);
        }

        @Override
        public void timer(String key, int ticks) {
            this.host.timers.put(this.prefix + key, ticks);
        }

        /** 先查本实例的分域参数，未命中再回退到投递注入的<b>全局</b>参数。 */
        @Override
        public double param(String key) {
            Double v = this.host.params.get(this.prefix + key);
            return v != null ? v : this.host.params.getOrDefault(key, 0.0);
        }
    }

    public SkillEntity(EntityType<? extends SkillEntity> type, Level level) {
        super(type, level);
        // 重力一律关闭：抛射的重力由 Ballistic 组件每 tick 自行施加，
        // 避免与原版重力和 move() 的碰撞解析相互干扰（同既有球 / 手雷实现）。
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_RENDER, "");
        builder.define(DATA_RADIUS, DEFAULT_RADIUS);
        builder.define(DATA_SKIN_KEY, "");
    }

    // ───────────── 装配 ─────────────

    /**
     * 由投递在生成时装配：owner / 阶段 / 存活时长 / 作用半径 + 形态引用。
     *
     * <p>初速度<b>不在此设置</b> —— 由执行器调用 {@link #setDeltaMovement(Vec3)}
     * 给出（速度是技能层「推多远」，属投递参数）；同理节奏经 {@link #putParam(String, double)} 注入。
     */
    public void configure(LivingEntity owner, @Nullable UUID phaseId, int lifetime, float radius, EntityRead read) {
        this.owner = EntityReference.of(owner);
        this.casterId = owner.getUUID();
        this.phaseId = phaseId;
        this.lifetime = Math.max(1, lifetime);
        this.radius = Math.max(0.1F, radius);
        this.applyRead(read);
        if (this.def != null) {
            // 是否需要真实碰撞由运动组件自行声明（宿主不认识任何具体组件）
            this.noPhysics = !this.def.motion().physics();
            // 行为运行态（引信等）只在新生成时装填一次 —— 读档时由持久化的计时器还原
            for (Slot slot : this.slots) {
                slot.behavior().init(slot.runtime());
            }
            // 阶段式实体（通用设施）：装配即掷一次「按概率直接以末阶段出现」——实体被创建的公共路径就在此处，
            // 故全部投递入口（Hazard / Remnant / Orb / Ball / Thrown）共用本读点，不在各执行器里重复。
            EntityStages.applySpawnRoll(this, owner);
            // 生成反馈：生成音效 + 一次性生成爆发。仅在实体被投递创建的公共路径（configure）触发——
            // 重载路径走 restoreDef→applyRead，不调用 configure，故重载后不重复爆发（与 SkillInstance 释放节拍同源：只在生成瞬间一次）。
            this.playSpawnFeedback();
        }
        if (!this.level().isClientSide()) {
            this.getEntityData().set(DATA_RADIUS, this.radius);
            this.refreshDimensions();
        }
    }

    /**
     * 生成瞬间的一次性反馈：生成音效 + 生成爆发（{@code on_spawn} 效果通道）。
     *
     * <p>由 {@link #configure}（实体被投递创建的<b>唯一</b>公共路径）调用，重载路径（{@code restoreDef→applyRead}）
     * 不调用 configure，故<b>重载后不重复爆发</b>——与 {@code SkillInstance#emitReleaseBeat} 只在施放完成瞬间播一次的口径同源。
     *
     * <p><b>缺省不发声 / 不爆发</b>：{@code spawn_sound} / {@code on_spawn} 均为缺省空，未配置的实体维持现状
     * （不引入硬编码默认值，与实体侧「无字段则零行为」的验收口径一致）。播放口径沿用 {@code SkillInstance}
     * （{@code createVariableRangeEvent} + {@code SoundSource.PLAYERS} + 0.6/0.8 音量），避免双轨风格。
     */
    private void playSpawnFeedback() {
        if (this.level().isClientSide() || this.def == null) return;
        // 生成音效：缺省不发声（未配置 spawn_sound 的实体维持现状）
        this.def.spawnSound().ifPresent(id -> {
            Vec3 p = this.position().add(0.0, this.getBbHeight() * 0.5, 0.0);
            this.level().playSound(null, p.x, p.y, p.z,
                    SoundEvent.createVariableRangeEvent(id), SoundSource.PLAYERS, 0.6F, 0.8F);
        });
        // 生成爆发：一次性粒子（逐层直接 emit，不经 interval 节流）；target/caster 取 owner（生成时本实体即施放者产物）
        if (!this.def.onSpawn().isEmpty()) {
            LivingEntity caster = this.getOwner();
            EntityTrigger.EntityTriggerContext ctx = new EntityTrigger.EntityTriggerContext(
                    caster, caster, this, (ServerLevel) this.level(), this.casterId, this.phaseId);
            for (EntityTrigger trigger : this.def.onSpawn()) {
                trigger.fire(ctx);
            }
        }
    }

    @Override
    @Nullable
    public EntityRead entityRead() {
        return this.read;
    }

    @Override
    @Nullable
    public EntityDef def() {
        return this.def;
    }

    /** 按实体引用装配形态 —— 装配与存档重载<b>共用本方法</b>，二者产物严格一致。 */
    @Override
    public void applyRead(EntityRead read) {
        this.read = read;
        this.def = read.resolve().orElse(null);
        if (this.def == null) {
            CraftOfExile.LOGGER.error("技能实体：未找到实体定义 {}", read.ref());
            this.renderDef = null;
            this.slots = List.of();
            return;
        }
        this.renderDef = this.def.renderDef().orElse(null);
        // 组件视图按「行为在列表中的下标」分域 —— 顺序即身份（同一行为配两次也各持运行态）
        this.motionView = new Scoped(this, "motion.");
        List<Slot> built = new ArrayList<>();
        for (int i = 0; i < this.def.behavior().size(); i++) {
            built.add(new Slot(this.def.behavior().get(i), new Scoped(this, "b" + i + ".")));
        }
        this.slots = List.copyOf(built);
        if (!this.level().isClientSide()) {
            this.getEntityData().set(DATA_RENDER, this.def.render().toString());
            // 技能皮肤键：把技能 id（装配期经 Skill#withDeliveryRender 写入 EntityRead.render 的原生技能 id）
            // 镜像进同步字段，供客户端染色通道按技能 id 索引 SkillSkinDefs（服务端经 entityRead() 直读无需同步）。
            this.getEntityData().set(DATA_SKIN_KEY, this.read.render().map(Identifier::toString).orElse(""));
            // 名称：有才设（无 name 时保持原版默认），走原版 customName 通道
            this.def.name().ifPresent(name -> {
                this.setCustomName(name);
                this.setCustomNameVisible(true);
            });
        }
    }

    // ───────────── 供组件读取的访问器 ─────────────

    /** 作用半径：服务端读权威字段，客户端读同步数据。 */
    public float radius() {
        return this.level().isClientSide() ? this.getEntityData().get(DATA_RADIUS) : this.radius;
    }

    /** 包围盒高度（方块）：形态数据给出，未装配时取兜底值。 */
    public float boxHeight() {
        return this.def != null ? this.def.boxHeight() : DEFAULT_BOX_HEIGHT;
    }

    /** 存活到顶（tick）；引信类行为据此兜底引爆，避免弹体永驻。 */
    public boolean isExpired() {
        return this.tickCount >= this.lifetime;
    }

    @Nullable
    public LivingEntity getOwner() {
        return EntityReference.getLivingEntity(this.owner, this.level());
    }

    /** 设置施法者（投射物生成入口在装配前预置 owner 用）。 */
    public void setOwner(LivingEntity owner) {
        this.owner = EntityReference.of(owner);
    }

    /**
     * 由投递注入的<b>全局</b>运行参数（技能层参数，非形态契约）。
     *
     * <p>组件经 {@link EntityRuntime#param(String)} 先查自己的分域键、再回退到全局键，
     * 故投递无需知道该参数落在第几个行为槽上。
     */
    public void putParam(String key, double value) {
        this.params.put(key, value);
    }

    @Override
    public Optional<Identifier> getRenderId() {
        String s = this.getEntityData().get(DATA_RENDER);
        return s.isEmpty() ? Optional.empty() : Optional.of(Identifier.parse(s));
    }

    /**
     * 技能皮肤键（技能 id）：服务端经 {@link #entityRead()} 直读 {@code EntityRead.render()}（装配 / 读档后本字段非空）；
     * 客户端 {@code this.read} 不随网络生成而设置，故回落到同步字段 {@link #DATA_SKIN_KEY}。
     * 二者皆空时视为「无皮肤键」，由渲染层回落渲染定义自带 tint。
     */
    public Optional<Identifier> skinKey() {
        Optional<Identifier> fromRead = this.read != null ? this.read.render() : Optional.empty();
        if (fromRead.isPresent()) return fromRead;
        String key = this.getEntityData().get(DATA_SKIN_KEY);
        return key.isEmpty() ? Optional.empty() : Optional.of(Identifier.parse(key));
    }

    /**
     * 渲染器读的半径（算立牌尺寸）—— 与 {@link #radius()} 同源。
     *
     * <p><b>必须同步</b>：客户端不跑 {@link #tick()}（只在 ServerLevel 执行），
     * 故 {@code radius} 字段在客户端恒为默认值；不同步则范围词缀放大时外观不跟随。
     */
    @Override
    public float getRadius() {
        return this.radius();
    }

    // ───────────── 索敌与触发（供组件经 EntityRuntime 调用） ─────────────

    /**
     * 范围内<b>全部</b>合目标（AoE 口径：对每个目标各生效一次）。
     *
     * @param kind {@link EntityBehavior#TARGET_SELF} / {@link EntityBehavior#TARGET_ENEMY} / {@code any}
     */
    public List<LivingEntity> queryTargets(float radius, String kind) {
        LivingEntity owner = this.getOwner();
        if (EntityBehavior.TARGET_SELF.equals(kind)) {
            return owner != null && owner.isAlive()
                    && owner.position().distanceToSqr(this.position()) <= (double) radius * radius
                    ? List.of(owner) : List.of();
        }
        return ExpressionContext.of(this.level(), this).queryEntities(
                EntityQuery.sphere(this.position(), radius)
                        .exclude(owner).aliveOnly()
                        .filter(e -> !EntityBehavior.TARGET_ENEMY.equals(kind) || isHostile(e)));
    }

    /** 范围内<b>最近</b>的一个合目标（接触触发口径：一次只触发一个）。 */
    @Nullable
    public LivingEntity queryTarget(float radius, String kind) {
        LivingEntity owner = this.getOwner();
        if (EntityBehavior.TARGET_SELF.equals(kind)) {
            return owner != null && owner.isAlive()
                    && owner.position().distanceToSqr(this.position()) <= (double) radius * radius
                    ? owner : null;
        }
        return ExpressionContext.of(this.level(), this).queryEntity(
                EntityQuery.sphere(this.position(), radius)
                        .exclude(owner).aliveOnly()
                        .filter(e -> !EntityBehavior.TARGET_ENEMY.equals(kind) || isHostile(e))
                        .nearestTo(this.position()))
                .orElse(null);
    }

    /** 敌人判定：敌对生物且非玩家阵营（与既有危害物口径一致）。 */
    private static boolean isHostile(LivingEntity e) {
        return e instanceof Enemy && !Camp.PLAYER.equals(e.getData(AttachmentRegistry.CAMP));
    }

    /**
     * 对一个目标执行 {@link EntityDef#onTrigger()} 全部效果。
     *
     * <p><b>重载后不触发</b>：{@code phaseId} 为空意味着阶段实例已不存在，
     * 此时触发只能产生「没有结算的假触发」（点燃失效 / 空事件），故静默跳过。
     */
    public void fireTriggers(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel level)) return;
        LivingEntity caster = this.getOwner();
        if (this.def == null || caster == null || this.casterId == null || this.phaseId == null) return;
        // 命中音效：缺省不发声（未配置 hit_sound 的实体维持现状）；以目标位置为声源，与释放节拍同源口径。
        // 注意：脉冲类行为（pulse）每节拍各触发一次，会每节拍播放——属数据驱动选择，用户可对脉冲实体不配置 hit_sound。
        this.def.hitSound().ifPresent(id -> {
            Vec3 p = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0);
            level.playSound(null, p.x, p.y, p.z,
                    SoundEvent.createVariableRangeEvent(id), SoundSource.PLAYERS, 0.6F, 0.8F);
        });
        EntityTrigger.EntityTriggerContext ctx = new EntityTrigger.EntityTriggerContext(
                caster, target, this, level, this.casterId, this.phaseId);
        for (EntityTrigger trigger : this.def.onTrigger()) {
            trigger.fire(ctx);
        }
    }

    /**
     * 对一个目标执行 {@link EntityDef#onExpire()} 全部效果（实体到期 / 引爆时的第二效果通道）。
     *
     * <p><b>回退 on_trigger</b>：若实体未定义 {@code on_expire}，则回退到 {@link #fireTriggers}，
     * 以兼容仅有单一效果通道的既有形态（如手雷：引爆即唯一致命效果，无需区分通道）。
     * 重载后不触发的规则与 {@link #fireTriggers} 一致（{@code phaseId} 为空则静默跳过）。
     */
    public void fireExpire(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel level)) return;
        LivingEntity caster = this.getOwner();
        if (this.def == null || caster == null || this.casterId == null || this.phaseId == null) return;
        List<EntityTrigger> list = this.def.onExpire().isEmpty() ? this.def.onTrigger() : this.def.onExpire();
        EntityTrigger.EntityTriggerContext ctx = new EntityTrigger.EntityTriggerContext(
                caster, target, this, level, this.casterId, this.phaseId);
        for (EntityTrigger trigger : list) {
            // target 为 null（半径内无敌人时的兜底触发）时，仅执行不依赖目标的效果（Deliver 留残迹），
            // 目标相关效果（PhaseOnHit / ApplyStatus(on=target) 等）跳过，避免对 null 解引用
            if (target == null && !(trigger instanceof EntityTriggers.Deliver)) continue;
            trigger.fire(ctx);
        }
    }

    /**
     * 第三效果通道：玩家在<b>本实体作用半径内</b>施放法术时执行 {@link EntityDef#onNearbyCast()} 全部效果。
     *
     * <p><b>caster 仍是玩家（stat 作用域不变），prop 是本实体（由球放出）</b>——这正是用户对 ④ 的硬性要求：
     * 「球是施法主体，但球施法这个行为数据驱动化、不切换施法者主体」。效果由数据定义
     * （复用 {@code PhaseOnHit} 点燃玩家阶段的 ON_HIT 闪电），与脉冲通道同源、互不串扰。
     *
     * <p>目标选取同 pulse 行为：对每个命中目标各点燃一次（AoE 口径），故 {@code target} 必非 null——
     * 调用方应以 {@link #queryTargets} 的结果逐个传入（{@link EntityTriggers.PhaseOnHit} 会解引用 target）。
     */
    public void fireNearbyCast(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel level)) return;
        LivingEntity caster = this.getOwner();
        if (this.def == null || caster == null || this.casterId == null || this.phaseId == null) return;
        if (this.def.onNearbyCast().isEmpty()) return;
        EntityTrigger.EntityTriggerContext ctx = new EntityTrigger.EntityTriggerContext(
                caster, target, this, level, this.casterId, this.phaseId);
        for (EntityTrigger trigger : this.def.onNearbyCast()) {
            trigger.fire(ctx);
        }
    }

    /**
     * 第四效果通道：对目标执行 {@link EntityDef#onAura()} 全部效果（持续性光环状态施加，如日曜球「1 米内点燃」）。
     * 门控与 {@link #fireNearbyCast} 同构：{@code onAura} 为空则静默跳过；{@code phaseId} 为空（重载后）静默跳过。
     */
    public void fireAura(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel level)) return;
        LivingEntity caster = this.getOwner();
        if (this.def == null || caster == null || this.casterId == null || this.phaseId == null) return;
        if (this.def.onAura().isEmpty()) return;
        EntityTrigger.EntityTriggerContext ctx = new EntityTrigger.EntityTriggerContext(
                caster, target, this, level, this.casterId, this.phaseId);
        for (EntityTrigger trigger : this.def.onAura()) {
            trigger.fire(ctx);
        }
    }

    /**
     * 空间转发器：玩家施法成功后，扫描附近、owner=玩家、且定义了 {@code onNearbyCast} 的 Orb，
     * 对每个 Orb 取作用半径内全部敌人并执行其 {@link #fireNearbyCast}（球额外放电）。
     *
     * <p><b>为何绕开 CAST 事件</b>：{@code SkillCastEvent} 全代码库从未被 {@code publish}，
     * 属零发射点死线；故不新增事件类型，改用纯空间判定直触 Orb，零新触发器、零死分支。
     * 门控完全数据驱动：仅 {@code onNearbyCast} 非空的 Orb（当前仅 Orb of Storms）才会响应，
     * frostBomb / sunOrb 等保持空列表 → 不产生任何行为（非枚举硬编码）。
     */
    public static void triggerNearbyOrbsOnCast(LivingEntity player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        for (Entity e : level.getEntities(player, player.getBoundingBox().inflate(NEARBY_CAST_SEARCH))) {
            if (!(e instanceof SkillEntity se)) continue;
            EntityDef def = se.def();
            if (def == null || def.onNearbyCast().isEmpty()) continue;
            if (se.casterId == null || !se.casterId.equals(player.getUUID())) continue;
            if (player.distanceToSqr(se.position()) > (double) se.radius() * se.radius()) continue;
            for (LivingEntity t : se.queryTargets(se.radius(), EntityBehavior.TARGET_ENEMY)) {
                se.fireNearbyCast(t);
            }
        }
    }

    /** 按渲染定义的粒子层发射一次表现（模型层在服务端为空实现，由客户端渲染）。 */
    public void emitRender(float radius) {
        if (this.renderDef == null || !(this.level() instanceof ServerLevel level)) return;
        EntityRenderLayer.EmitContext ec = new EntityRenderLayer.EmitContext(
                level, this.getX(), this.getY(), this.getZ(), level.getRandom());
        this.renderDef.emit(ec, radius, this.tickCount);
    }

    // ───────────── 生命周期 ─────────────

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;
        // 投射物路径：本实体由 ProjectileDelivery 生成（持有 DeliveryRuntimeContext 且其投递为投射物）。
        // 统一在此处理「飞行 + 碰撞 + 命中」，替换原 SkillArrow 对原版 AbstractArrow 命中回调的依赖；
        // 任何承载实体只要持有投射物上下文即走此路径，故非箭矢投射物（指向其它 coe:entity 引用）亦可共享机制。
        DeliveryRuntimeContext projCtx = DeliveryTracker.get(this.getUUID());
        if (projCtx != null && projCtx.delivery() instanceof ProjectileDelivery delivery) {
            this.tickProjectile(level, projCtx, delivery);
            return;
        }
        if (this.def == null) {
            // 无形态（数据表缺失 / 被删）：安静到期消失，不产生任何行为
            if (this.isExpired()) this.discard();
            return;
        }
        // 运动 → 各行为 → 到期：任一步判定「应销毁」即结束（顺序不可换：
        // 手雷必须先落地，才能由行为接手引信；到期兜底放在行为之后，引信类行为才能兜底引爆）
        // 阶段式实体（通用设施）：时间驱动的阶段推进，挂在既有的实体 tick 路径上，不新起 ticker。
        EntityStages.tick(this);
        if (this.def.motion().move(this.motionView)) {
            this.discard();
            return;
        }
        for (Slot slot : this.slots) {
            if (slot.behavior().tick(slot.runtime())) {
                this.discard();
                return;
            }
        }
        if (this.isExpired()) {
            // 到期第二效果通道：若实体定义了 {@code on_expire}（如「消散爆炸」形态），对范围内敌人各点燃一次；
            // 未定义则维持原行为（直接销毁，无额外触发）。
            // 仅 Detonate 行为会在引信归零时自行 {@code fireExpire} 并 discard，不会抵达此处重复触发；
            // 此处只兜底「无 detonate 行为却定义了 on_expire」的形态（Orb / 球等驻场 / 飞行体的到期特殊效果）。
            if (!this.def.onExpire().isEmpty()) {
                List<LivingEntity> targets = this.queryTargets(this.radius, EntityBehavior.TARGET_ENEMY);
                // 不依赖目标的效果（如留残迹 Deliver）仅触发一次，避免按敌人数重复生成残片；
                // 目标相关效果（如到期点燃 PhaseOnHit）仍对每个敌人各触发一次。
                LivingEntity caster = this.getOwner();
                EntityTrigger.EntityTriggerContext single = new EntityTrigger.EntityTriggerContext(
                        caster, targets.isEmpty() ? null : targets.get(0), this, level, this.casterId, this.phaseId);
                for (EntityTrigger trigger : this.def.onExpire()) {
                    if (trigger instanceof EntityTriggers.Deliver) trigger.fire(single);
                }
                for (LivingEntity t : targets) {
                    EntityTrigger.EntityTriggerContext ctx = new EntityTrigger.EntityTriggerContext(
                            caster, t, this, level, this.casterId, this.phaseId);
                    for (EntityTrigger trigger : this.def.onExpire()) {
                        if (!(trigger instanceof EntityTriggers.Deliver)) trigger.fire(ctx);
                    }
                }
            }
            this.discard();
            return;
        }
        if (this.tickCount % PARTICLE_INTERVAL == 0) {
            this.emitRender(this.def.motion().visualRadius(this.motionView));
        }
    }

    // ───────────── 通用覆写 ─────────────

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (DATA_RADIUS.equals(key)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }

    /** AABB 由运动方式决定宽度（静止按作用半径铺开 / 弹体取自身尺寸），高度取形态数据。 */
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        float width = this.def != null ? this.def.motion().width(this.motionView) : DEFAULT_BOX_HEIGHT;
        return EntityDimensions.scalable(Math.max(0.1F, width), this.boxHeight());
    }

    /** 刷新尺寸时保持位置不变（避免被吸附到方块中心）。 */
    @Override
    public void refreshDimensions() {
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        super.refreshDimensions();
        this.setPos(x, y, z);
    }

    @Override
    public void onRemoval(Entity.RemovalReason reason) {
        if (this.casterId != null && this.phaseId != null) {
            SkillPhaseInstanceTracker.release(this.casterId, this.phaseId);
        }
        // 投射物上下文清理（避免阶段引用计数泄漏）：若本实体仍持有投射物运行态则一并释放。
        DeliveryRuntimeContext dctx = DeliveryTracker.get(this.getUUID());
        if (dctx != null) {
            SkillPhaseInstanceTracker.release(dctx.casterId(), dctx.phaseId());
            DeliveryTracker.remove(this.getUUID());
        }
        super.onRemoval(reason);
    }

    // ───────────── 投射物路径（替代原 SkillArrow 对 AbstractArrow 命中回调的依赖） ─────────────

    /**
     * 投射物每 tick：飞行速度修正 → 返回施法者 → 到期 → 移动 + 碰撞 → 写回上下文。
     *
     * <p>原 {@code SkillArrow} 的命中由原版 {@code AbstractArrow} 回调驱动；本方法自行做碰撞检测
     * （{@link ProjectileUtil#getHitResultOnMoveVector}），使投射物机制不再绑定原版箭矢类型，
     * 任何 {@code coe:entity} 引用（含 {@code coe:arrow}）只要经 {@link ProjectileDelivery} 生成即走此路径。
     */
    private void tickProjectile(ServerLevel level, DeliveryRuntimeContext ctx, ProjectileDelivery delivery) {
        // 1. 飞行模式每 tick 速度修正
        Vec3 currentVel = this.getDeltaMovement();
        Vec3 newVel = delivery.flight().tickVelocity(currentVel, ctx.ticksAlive(), this);
        if (newVel != currentVel) {
            this.setDeltaMovement(newVel);
        }

        // 1.5 返回施法者阶段：每 tick 重定向飞向施法者，接近时销毁
        if (ctx.returning()) {
            Entity owner = this.getOwner();
            if (owner == null) {
                this.discard();
                return;
            }
            Vec3 toCaster = owner.getEyePosition().subtract(this.position());
            if (toCaster.length() < 1.5) {
                this.discard();
                return;
            }
            double speed = this.getDeltaMovement().length();
            this.setDeltaMovement(toCaster.normalize().scale(speed));
        }

        // 2. 到期检测 → 点燃 ON_EXPIRE
        int age = ctx.ticksAlive();
        if (age >= delivery.lifetime().getInt()) {
            SkillPhaseInstance phase = SkillPhaseInstanceTracker.find(ctx.casterId(), ctx.phaseId());
            if (phase != null) {
                phase.fireTrigger(LifecycleTrigger.ON_EXPIRE);
            }
            this.discard();
            return;
        }

        // 3. 碰撞检测（移动前取命中结果，覆盖本次移动线段）+ 移动
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this,
                e -> e != this.getOwner() && e.isAlive() && e.isPickable());
        this.move(MoverType.SELF, this.getDeltaMovement());
        if (hit.getType() == HitResult.Type.ENTITY) {
            this.handleDeliveryHit(((EntityHitResult) hit).getEntity(), null, ctx, delivery);
        } else if (hit.getType() == HitResult.Type.BLOCK) {
            this.handleDeliveryHit(null, (BlockHitResult) hit, ctx, delivery);
        }

        // 4. 写回递增后的上下文
        DeliveryTracker.update(this.getUUID(), ctx.incrementTick());
    }

    /**
     * 统一处理 Delivery 系统的命中逻辑（原 {@code SkillArrow#handleDeliveryHit} 迁移至此）：
     * 构建 HitContext → 点燃 ON_HIT → 应用 CollisionPolicy（穿透 / 连锁 / 分裂 / 返回 / 几率穿透）。
     *
     * <p>命中判定由能力（持有 {@link DeliveryRuntimeContext} 且投递为投射物）驱动，不再经
     * {@code instanceof Arrow / SkillArrow} 分叉，使任意投射物载体共享同一套命中机制。
     */
    private void handleDeliveryHit(@Nullable Entity entity, @Nullable BlockHitResult blockResult,
                                  DeliveryRuntimeContext ctx, ProjectileDelivery delivery) {
        HitContext hit;
        if (entity != null) {
            hit = HitContext.entityHit(entity, this.position(), this.getDeltaMovement(),
                    (float) this.position().distanceTo(ctx.origin()), Optional.of(ctx.damageMultiplier()));
        } else {
            BlockPos pos = blockResult.getBlockPos();
            Direction face = blockResult.getDirection();
            hit = HitContext.blockHit(pos, face, this.position(),
                    Vec3.atLowerCornerOf(face.getUnitVec3i()));
        }

        // 2. 点燃 ON_HIT —— 通过 tracker 反查 SkillPhaseInstance
        SkillPhaseInstance phase = SkillPhaseInstanceTracker.find(ctx.casterId(), ctx.phaseId());
        if (phase != null) {
            phase.fireTrigger(LifecycleTrigger.ON_HIT, hit);
        }

        // 3. 应用碰撞策略
        boolean continueFlying;
        if (entity != null) {
            // 避免同一实体重复命中
            if (ctx.hasHit(entity.getUUID())) {
                return;
            }
            continueFlying = delivery.collision().onHitEntity(this, entity, ctx);
            // 辅助宝石穿透：将默认 Stop 策略升级为穿透（仅当基础策略本就停止时叠加）
            if (!continueFlying && ctx.supportPierce() > 0 && delivery.collision() instanceof Stop) {
                continueFlying = ctx.hitCount() < ctx.supportPierce();
            }
            // 基础穿透（PROJECTILE_PIERCE，{projectile} 作用域）：机会穿透/辅助穿透耗尽后仍可继续飞行的「次数」来源。
            // 由原 ArrowPiercing 混合的「次数定档」语义迁移而来，改为命中当刻读取、按能力（持有 projectile 上下文）触发，
            // 不再依赖原版 AbstractArrow 的 pierceLevel（该字段已随 SkillArrow 移除而废弃）。
            if (!continueFlying && delivery.collision() instanceof Stop) {
                int basePierce = (int) DamageHelper.readInScopePublic(this.getOwnerOrFallback(),
                        Set.of(GateTag.PROJECTILE), AttributeRegistry.PROJECTILE_PIERCE);
                if (basePierce > 0) {
                    continueFlying = ctx.hitCount() < basePierce;
                }
            }
            // 辅助宝石连锁：将停止的投射物重定向至新的邻近目标（对应 PoE 的 Chain）
            if (!continueFlying && ctx.depth() < delivery.maxDepth()
                    && ctx.supportChain() > 0 && ctx.chainCount() < ctx.supportChain()) {
                LivingEntity next = findChainTarget(ctx);
                if (next != null) {
                    Vec3 toTarget = next.position().subtract(this.position()).normalize();
                    double speed = this.getDeltaMovement().length();
                    this.setDeltaMovement(toTarget.scale(speed));
                    ctx = ctx.addChain();
                    continueFlying = true;
                }
            }
            // 辅助宝石分裂：命中时分裂出额外投射物（对应 PoE 的 Fork）
            if (!continueFlying && ctx.depth() < delivery.maxDepth()
                    && ctx.supportFork() > 0 && this.level() instanceof ServerLevel serverLevel) {
                double speed = this.getDeltaMovement().length();
                Vec3 dir = this.getDeltaMovement().normalize();
                // 子投射物继承父深度 +1（递归安全上限由 delivery.maxDepth() 兜底）
                ProjectileDeliveryExecutor.spawnForks(serverLevel, delivery,
                        ctx.casterId(), ctx.phaseId(), this.position(), dir, this.getOwner(),
                        ctx.depth() + 1, (float) speed, ctx.supportPierce(), ctx.supportChain(), ctx.supportFork(),
                        ctx.splitDamageMultiplier(), ctx.damageMultiplier());
                ctx = ctx.consumeFork();
                continueFlying = true; // 原投射物继续飞行
            }
            // 辅助宝石返回施法者：停止的投射物重定向飞回施法者（对应 PoE 的 Return）
            if (!continueFlying && ctx.returnToCaster() && !ctx.returning()) {
                Entity owner = this.getOwner();
                if (owner != null) {
                    Vec3 toCaster = owner.getEyePosition().subtract(this.position()).normalize();
                    double speed = this.getDeltaMovement().length();
                    this.setDeltaMovement(toCaster.scale(speed));
                    ctx = ctx.addReturning();
                    continueFlying = true;
                }
            }
            // 几率穿透（PROJECTILE_PIERCE_CHANCE，{projectile} 作用域）：次数穿透（CollisionPolicy.Pierce /
            // 辅助宝石 supportPierce / 基础 PROJECTILE_PIERCE）耗尽后，每次命中独立几率判定（POE2「X% chance to pierce」，与次数穿透不冲突）
            // 几率按「本次被撞敌人」实时求值（该敌人绑为射手作用域的交互对方），使「穿透<某状态>敌人」
            // 类条目（条件经 source.* 读对方状态）在命中当刻成立；非生物目标退回按射手读。
            if (!continueFlying) {
                LivingEntity owner = this.getOwnerOrFallback();
                if (owner != null) {
                    float chance = (float) DamageHelper.readInScopeAgainst(owner, Set.of(GateTag.PROJECTILE),
                            AttributeRegistry.PROJECTILE_PIERCE_CHANCE,
                            entity instanceof LivingEntity victim ? victim : null);
                    if (chance > 0 && this.getRandom().nextFloat() < chance) {
                        continueFlying = true;
                    }
                }
            }
            ctx = ctx.addHit(entity.getUUID());
        } else {
            continueFlying = delivery.collision().onHitBlock(this, blockResult, ctx);
        }

        // 4. 更新 tracker
        DeliveryTracker.update(this.getUUID(), ctx);

        // 5. 不继续飞行则销毁
        if (!continueFlying) {
            this.discard();
        }
    }

    /** 几率穿透 / 基础穿透读取射手 stat 的兜底：owner 为空时退回 null（无 Stat 持有者）。 */
    @Nullable
    private LivingEntity getOwnerOrFallback() {
        return this.getOwner();
    }

    /**
     * 为连锁（Chain）寻找最近的、尚未命中过的存活实体。
     * 参考 HomingFlight 的就近目标选取逻辑。
     */
    private LivingEntity findChainTarget(DeliveryRuntimeContext ctx) {
        // 获取实体统一经 Context 数据基站回调（EntityQuery 包围盒扫描 + 就近排序）
        return ExpressionContext.of(this.level(), this).queryEntity(
                        EntityQuery.box(this.getBoundingBox().inflate(15))
                                .exclude(this.getOwner()).aliveOnly()
                                .filter(e -> !ctx.hasHit(e.getUUID()))
                                .nearestTo(this.position()))
                .orElse(null);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // 世界物件而非可被攻击的目标
        return false;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean isPickable() {
        // 墙体（Wall 运动）开启可碰撞：使其它实体每 tick 被其包围盒推开，实现「阻挡技能 / 移动」语义。
        return this.def != null && this.def.motion() instanceof EntityMotions.Wall;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        this.tickCount = valueInput.getIntOr("Age", 0);
        this.lifetime = valueInput.getIntOr("Lifetime", DEFAULT_LIFETIME);
        this.radius = valueInput.getFloatOr("Radius", DEFAULT_RADIUS);
        this.motionStopped = valueInput.getBooleanOr("MotionStopped", false);
        valueInput.read("Timers", TIMERS_CODEC).ifPresent(m -> {
            this.timers.clear();
            this.timers.putAll(m);
        });
        valueInput.read("Params", PARAMS_CODEC).ifPresent(m -> {
            this.params.clear();
            this.params.putAll(m);
        });
        this.getEntityData().set(DATA_RADIUS, this.radius);
        // 重载路径：SynchedEntityData.set 不触发 onSyncedDataUpdated（服务器侧），
        // 显式刷新 AABB 使重载后非默认半径的实体包围盒正确（否则粗筛漏圈外实体）。
        this.refreshDimensions();
        this.owner = EntityReference.read(valueInput, "Owner");
        // 形态还原：必须先于 tick 的行为判定，否则重载后的实体是「无定义空壳」
        this.restoreDef(valueInput);
        // casterId / phaseId 不持久化：重载后阶段实例已不存在，实体静默驻场至到期
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        valueOutput.putInt("Age", this.tickCount);
        valueOutput.putInt("Lifetime", this.lifetime);
        valueOutput.putFloat("Radius", this.radius);
        valueOutput.putBoolean("MotionStopped", this.motionStopped);
        valueOutput.store("Timers", TIMERS_CODEC, this.timers);
        valueOutput.store("Params", PARAMS_CODEC, this.params);
        EntityReference.store(this.owner, valueOutput, "Owner");
        this.saveDef(valueOutput);
    }
}
