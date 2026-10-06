package com.cimdy.coe.data.codec.entity.trigger;

import com.cimdy.coe.data.codec.entity.EntityDef;

import com.cimdy.coe.data.codec.entity.render.EntityRenderLayer;
import com.cimdy.coe.data.codec.entity.render.EntityRenderLayers;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.phase.action.combo.DeliveryAction;
import com.cimdy.coe.data.codec.skill.context.SkillContext.HitContext;
import com.cimdy.coe.data.codec.skill.phase.action.control.LifecycleTrigger;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.SkillPhaseInstanceTracker;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.delivery.Delivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.DeliveryExecutors;
import com.cimdy.coe.data.codec.skill.phase.event.SkillEventBus;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.cimdy.coe.data.codec.support.status.def.StatusType;
import com.cimdy.coe.event.Event;
import com.cimdy.coe.event.combat.GameEvent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.cimdy.coe.entity.custom.skill.SkillEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * 内置道具触发效果实现 —— 覆盖残片/危害物全部既有触发语义，并天然可扩展。
 *
 * <p>与 {@code ActionTypes} 的动作体系、{@code EntityRenderLayers} 的渲染层同构：新增一种触发效果
 * = 加一个注册实现 + 数据包写一条，绝不硬编码进实体类的分发逻辑。
 */
public final class EntityTriggers {

    private EntityTriggers() {
    }

    /**
     * 点燃所属阶段<b>指定生命周期触发点</b>（默认 ON_HIT）：所有残片/危害物的核心行为。
     * 残片/危害物的「拾取/穿越触发」本身只回调触发点，具体伤害/状态/增益由技能数据决定。
     *
     * <p>参数化 {@link LifecycleTrigger} 后，同一个触发效果既能服务常规触发（{@code on_trigger} → ON_HIT），
     * 也能服务<b>第二效果通道</b>（{@code on_expire} → ON_EXPIRE），使
     * {@code pulse ⊕ detonate} 这类复合形态可让「周期脉冲」与「到期引爆」分别点燃不同的技能阶段动作。
     * 数据包中缺省即 ON_HIT，故既有形态数据无需改动。
     */
    public record PhaseOnHit(LifecycleTrigger trigger) implements EntityTrigger {
        /** 默认实例：{@code EntityDef.on_trigger} 缺省即 { phase_on_hit }，保证任何道具都至少点燃 ON_HIT。 */
        public static final PhaseOnHit INSTANCE = new PhaseOnHit(LifecycleTrigger.ON_HIT);
        /** 第二效果通道实例：{@code EntityDef.on_expire} 用之点燃 ON_EXPIRE（如 Frost Bomb 的引爆伤害）。 */
        public static final PhaseOnHit ON_EXPIRE = new PhaseOnHit(LifecycleTrigger.ON_EXPIRE);
        public static final Codec<PhaseOnHit> CODEC = RecordCodecBuilder.create(i -> i.group(
                LifecycleTrigger.CODEC.optionalFieldOf("trigger", LifecycleTrigger.ON_HIT)
                        .forGetter(PhaseOnHit::trigger)
        ).apply(i, PhaseOnHit::new));

        @Override
        public EntityTrigger.Type<?> getType() {
            return EntityTriggerTypes.PHASE_ON_HIT;
        }

        @Override
        public void fire(EntityTriggerContext ctx) {
            Vec3 targetPos = ctx.target().position().add(0, ctx.target().getBbHeight() * 0.5, 0);
            Vec3 dir = targetPos.subtract(ctx.prop().position());
            float distance = (float) dir.length();
            HitContext hit = HitContext.entityHit(ctx.target(), targetPos,
                    distance > 1e-4 ? dir.normalize() : Vec3.ZERO, distance);
            SkillPhaseInstanceTracker.fire(ctx.casterId(), ctx.phaseId(), trigger, hit);
        }
    }

    /**
     * 发射事件（owner 可为 target/caster）：残片拾取 emit {@code collect_remnant} 即走此通道
     * （此前仅由 RemnantState 发射，通用残片形态拾取时缺失发射点 → 死线）。
     */
    public record EmitEvent(String event, List<String> tags, String owner) implements EntityTrigger {
        public static final Codec<EmitEvent> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("event").forGetter(EmitEvent::event),
                Codec.STRING.listOf().optionalFieldOf("tags", List.of()).forGetter(EmitEvent::tags),
                Codec.STRING.optionalFieldOf("owner", "caster").forGetter(EmitEvent::owner)
        ).apply(i, EmitEvent::new));

        @Override
        public EntityTrigger.Type<?> getType() {
            return EntityTriggerTypes.EMIT_EVENT;
        }

        @Override
        public void fire(EntityTriggerContext ctx) {
            LivingEntity who = "target".equals(owner) ? ctx.target() : ctx.caster();
            GameEvent.emit(event, who, (LivingEntity) null, Set.copyOf(tags));
        }
    }

    /**
     * 记录计时事件（on 可为 caster/target）：危害物记录 hazard_triggered（「recently」窗口）
     * 即走此——此前该记录在危害物通路上恒为空（半实现），经此接通。
     */
    public record RecordEvent(String event, String on, List<String> tags) implements EntityTrigger {
        public static final Codec<RecordEvent> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("event").forGetter(RecordEvent::event),
                Codec.STRING.optionalFieldOf("on", "caster").forGetter(RecordEvent::on),
                Codec.STRING.listOf().optionalFieldOf("tags", List.of()).forGetter(RecordEvent::tags)
        ).apply(i, RecordEvent::new));

        @Override
        public EntityTrigger.Type<?> getType() {
            return EntityTriggerTypes.RECORD_EVENT;
        }

        @Override
        public void fire(EntityTriggerContext ctx) {
            LivingEntity who = "target".equals(on) ? ctx.target() : ctx.caster();
            Event.record(who, event, Set.copyOf(tags));
        }
    }

    /**
     * 施加状态（on 可为 target/caster）：危害物对穿越目标施 hazard_mark（4s=80t，对应「recently」口径）
     * 即走此——此前硬编码于实体类，现抽出为数据驱动效果。
     */
    public record ApplyStatus(String status, int duration, double magnitude, String on) implements EntityTrigger {
        public static final Codec<ApplyStatus> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("status").forGetter(ApplyStatus::status),
                Codec.INT.optionalFieldOf("duration", 80).forGetter(ApplyStatus::duration),
                Codec.DOUBLE.optionalFieldOf("magnitude", 1.0).forGetter(ApplyStatus::magnitude),
                Codec.STRING.optionalFieldOf("on", "target").forGetter(ApplyStatus::on)
        ).apply(i, ApplyStatus::new));

        @Override
        public EntityTrigger.Type<?> getType() {
            return EntityTriggerTypes.APPLY_STATUS;
        }

        @Override
        public void fire(EntityTriggerContext ctx) {
            StatusType.byId(status).ifPresent(t -> {
                LivingEntity who = "caster".equals(on) ? ctx.caster() : ctx.target();
                StatusEngine.apply(who, t, ctx.caster(), magnitude, duration, 1);
            });
        }
    }

    /**
     * 实体触发一次投递物（实体侧 {@code DeliveryAction}）：残片/危害物在<b>触发/到期/拾取</b>时
     * 经此生成一次投递物（残片实体 / 投射物 / 范围 …），复用 {@link DeliveryExecutors} 全套执行器。
     *
     * <p><b>为什么需要它</b>：此前的投递物只在阶段初始化（{@code SkillPhase.initialize}）与事件回调
     * （{@code DeliveryAction}）两条路径生成，实体<b>自身</b>无法在到期/触发时主动生成投递物——
     * 这正是「实体到期留残迹」「危害物触发落下陨石」一类效果长期缺位的根因。本触发器即实体侧的投递入口，
     * 与 {@code DeliveryAction} 同一口径，不重复实现执行逻辑。
     *
     * <p><b>落点</b>：以<b>实体自身位置</b>为投递中心（残迹/投射物落在 orb 原地），施放者/阶段实例沿用
     * 实体上下文（caster/phaseId），保证生成的残片在 owner 走过时仍能反查阶段实例点燃 ON_HIT。
     * {@code skill} 在实体触发场景无对应实例，置 null（投递执行器对 null skill 已守卫）。
     */
    public record Deliver(Delivery delivery) implements EntityTrigger {
        public static final Codec<Deliver> CODEC = RecordCodecBuilder.create(i -> i.group(
                Delivery.CODEC.fieldOf("delivery").forGetter(Deliver::delivery)
        ).apply(i, Deliver::new));

        @Override
        public EntityTrigger.Type<?> getType() {
            return EntityTriggerTypes.DELIVER;
        }

        @Override
        public void fire(EntityTriggerContext ctx) {
            UUID phaseId = ctx.phaseId();
            if (phaseId == null) return;
            UUID casterId = ctx.casterId();
            SkillPhaseInstance instance = SkillPhaseInstanceTracker.find(casterId, phaseId);
            ActionContext actionCtx = new ActionContext(
                    null,
                    ctx.caster(),
                    Optional.empty(),
                    ctx.prop().position(),
                    Vec3.ZERO,
                    1.0f,
                    0,
                    SkillEventBus.forEntity(ctx.caster()),
                    phaseId,
                    null,
                    Optional.empty());
            DeliveryExecutors.execute(delivery, actionCtx, casterId, phaseId, instance);
        }
    }

    /**
     * 一次性爆发粒子（实体侧 {@code EntityRenderLayer}）：与 {@code EntityRenderDef.emit()} 的
     * 「tick 循环 + interval 节流」语义<b>相反</b>——本触发器在<b>触发瞬间</b>把各层<b>各发射一次</b>，
     * <b>不经 interval 节流</b>（直接调用 {@link EntityRenderLayer#emit}，绕过 {@code tick % interval == 0} 判定）。
     *
     * <p><b>为何独立成触发器</b>：既有 {@code EntityRenderDef} / 各层都是「驻场持续表现」语义
     * （{@link EntityRuntime#emitRender} 按 interval 节流、随实体存活反复刷），而「生成 / 命中 / 引爆」这类
     * <b>一次性爆发</b>在当前数据模型里无处表达，只能硬编码。本触发器复用现成的五种渲染层
     * （Ring/Pillar/Sphere/Point/Model），只改变调用语义，让「一次性爆发」也成为数据可挂的通道：
     * 把同一套分层粒子定义挂到 {@code on_spawn} / {@code on_trigger} / {@code on_expire} 即分别成为
     * 生成爆发 / 命中爆发 / 引爆爆发，零新粒子体系。
     *
     * <p><b>半径</b>：各层半径按实体触发半径（{@code SkillEntity#radius()}）的相对倍率缩放，
     * 与驻场表现同源；非 {@code SkillEntity} 载体（理论上不应触发本效果）回落 1.5 格。
     */
    public record EmitVfx(List<EntityRenderLayer> layers) implements EntityTrigger {
        public static final Codec<EmitVfx> CODEC = RecordCodecBuilder.create(i -> i.group(
                EntityRenderLayer.CODEC.listOf().fieldOf("layers").forGetter(EmitVfx::layers)
        ).apply(i, EmitVfx::new));

        @Override
        public EntityTrigger.Type<?> getType() {
            return EntityTriggerTypes.EMIT_VFX;
        }

        @Override
        public void fire(EntityTriggerContext ctx) {
            ServerLevel level = ctx.level();
            Entity prop = ctx.prop();
            double radius = prop instanceof SkillEntity se ? se.radius() : 1.5;
            EntityRenderLayer.EmitContext ec = new EntityRenderLayer.EmitContext(
                    level, prop.getX(), prop.getY(), prop.getZ(), level.getRandom());
            // 一次性：直接逐层 emit，不经 interval 节流（与 EntityRenderDef.emit 的 tick%interval 语义相反）
            for (EntityRenderLayer layer : layers) {
                layer.emit(ec, radius);
            }
        }
    }

    /**
     * 触发一次音效（实体侧）：在触发瞬间于实体位置播放一条原版 / 已注册 {@link SoundEvent}。
     *
     * <p>与 {@link EmitVfx} 同属「一次性反馈」通道：复用 {@code SkillInstance#emitReleaseBeat} 的
     * 播放口径（{@code createVariableRangeEvent} + {@code SoundSource.PLAYERS} + 0.6/0.8 音量），
     * <b>不引入新的硬编码默认值</b>——音效 id 必须由数据提供（指向原版或本模组的 SoundEvent），
     * 不配置则不发声（与实体侧「无字段则维持现状」的验收口径一致）。
     *
     * <p>落点：以<b>实体自身位置</b>为声源（{@code prop} 即本实体），与实体侧其余效果同源；
     * 频率/音量沿用 SkillInstance 既有做法，避免双轨风格。
     */
    public record PlaySound(Identifier sound) implements EntityTrigger {
        public static final Codec<PlaySound> CODEC = RecordCodecBuilder.create(i -> i.group(
                Identifier.CODEC.fieldOf("sound").forGetter(PlaySound::sound)
        ).apply(i, PlaySound::new));

        @Override
        public EntityTrigger.Type<?> getType() {
            return EntityTriggerTypes.PLAY_SOUND;
        }

        @Override
        public void fire(EntityTriggerContext ctx) {
            ServerLevel level = ctx.level();
            Entity prop = ctx.prop();
            Vec3 p = prop.position().add(0.0, prop.getBbHeight() * 0.5, 0.0);
            level.playSound(null, p.x, p.y, p.z,
                    SoundEvent.createVariableRangeEvent(sound), SoundSource.PLAYERS, 0.6F, 0.8F);
        }
    }
}
