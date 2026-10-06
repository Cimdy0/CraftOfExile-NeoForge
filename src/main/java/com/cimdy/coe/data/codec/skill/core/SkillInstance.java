package com.cimdy.coe.data.codec.skill.core;

import com.cimdy.coe.data.codec.skill.context.SkillStatScopes;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.SealMod;
import com.cimdy.coe.data.helper.hub.SpiritManager;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.status.StatusValue;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.helper.state.EchoState;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceCost;
import com.cimdy.coe.data.codec.skill.helper.cast.SealRelease;
import com.cimdy.coe.data.codec.skill.helper.state.SealState;
import com.cimdy.coe.data.codec.skill.input.Continuous;
import com.cimdy.coe.data.codec.skill.input.HoldInput;
import com.cimdy.coe.data.codec.skill.phase.SkillPhase;
import com.cimdy.coe.data.codec.skill.phase.event.trigger.ReservationChangeEvent;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.runtime.SkillTimeline;
import com.cimdy.coe.data.codec.skill.support.SupportProfile;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.cimdy.coe.event.combat.DamageEvent;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.helper.impl.CurseActivation;
import com.cimdy.coe.data.helper.hub.ReservationManager;
import com.cimdy.coe.data.codec.support.status.def.StatusKind;
import com.cimdy.coe.data.codec.support.status.def.StatusType;
import com.cimdy.coe.data.helper.impl.aura.MarkHelper;
import com.cimdy.coe.event.skill.SkillEvent;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceType;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceUsage;
import com.cimdy.coe.data.codec.skill.phase.event.SkillEventBus;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SkillInstance {
    private final Skill skill;
    private final LivingEntity caster;
    /** 基础技能石 id（数据驱动，名字任意），用于按「技能名」匹配 stat 加成。 */
    private final Identifier skillId;
    private List<SkillPhaseInstance> phaseInstances;
    private SkillContext context;
    private int time = 0;
    /** 贯穿始终的技能时间轴（服务端权威）：以施放起始世界时刻为原点、以速度缩放后的施法时长为全长。 */
    private final SkillTimeline timeline;
    /** 贯穿时间轴每 tick 算出的施放进度 0..1（服务端权威，客户端经同一世界时钟同源）；驱动效果视觉/音效对齐。 */
    private float castProgress = 0f;
    /** 施放是否已完成（进度达 1）。 */
    private boolean castComplete = false;
    /** 贯穿时间轴完成瞬间的「释放节拍」是否已发出（确保只触发一次）。 */
    private boolean released = false;
    /** 已初始化的顶层阶段，用于防止每 tick 重复初始化（见 Skill.run 重入防护）。 */
    private final Set<SkillPhase> initializedPhases = new HashSet<>();
    /** 回响调度器：待重放的阶段队列（延迟回响不因技能「结束」被丢弃，见 {@link #isStop}）。 */
    private final boolean channel;
    /** 本次引导是否已触发「引导 N 秒后施加诅咒」（防每 tick 重复施加）。 */
    private boolean curseAppliedThisChannel = false;
    private final EchoState echoState = new EchoState();
    /** 封印释放调度器：存满封印后下一次施放触发，按持有封印数延迟重放阶段实例。 */
    private final SealRelease sealRelease = new SealRelease();
    /**
     * 本实例登记的「非精魂保留型」CAST 保留量（见构造器），结束经 {@link #teardown()} 释放。
     * <b>按资源键分池</b>（{@link ResourceType#getSerializedName()}，与 {@link ReservationManager} 的资源维度同源），
     * 保留型技能扩展到其他资源时零改动。
     */
    private final Map<String, Float> reservedCast = new HashMap<>();

    public SkillInstance(LivingEntity caster, Skill skill, SkillContext context, Identifier skillId) {
        this.skill = skill;
        this.caster = caster;
        this.channel = skill.input() instanceof HoldInput || skill.input() instanceof Continuous;
        // 按下置位（施放链关键）：tick 依赖 isPress() 驱动 pressTime 累积 / HoldInput 蓄力 / Continuous 脉冲 /
        // autoRelease 结算；该置位丢失会让 isPress 恒 false，instance 首 tick 即被 isStop 移除，
        // 阶段初始化、伤害、动画全部不可达。实体一次性施放经 autoRelease 路径首 tick 后自动松开。
        this.context = context.press(true);
        this.skillId = skillId;
        // 建立贯穿始终的时间轴：起点 = 施放瞬间世界时刻；全长 = 经施法/攻击速度缩放后的施法时长
        // （Skill.getCastTime 已含 ATTACK_SPEED / CAST_SPEED / 辅助宝石 speedMultiplier）。
        // 服务端用它门控效果生成时机，客户端经 CastAnimationPacket 拿到同一 startTime 与 totalCastTicks，
        // 以世界游戏时间（两端同步）算出同一进度，使动画、效果、视觉、音效对齐到同一条时钟。
        this.timeline = new SkillTimeline(caster.level().getGameTime(), Math.max(1, skill.getCastTime(context)));
        // 跟踪本实例登记的「非精魂保留型」CAST 保留量；技能结束时经 teardown 释放，修复「CAST 永不回收」泄漏。
        // 精魂保留不归此处（由 SpiritManager 经 AURA/TOTEM 组件每 tick 重算）。
        for (ResourceCost rc : skill.cost()) {
            if (rc.usage() == ResourceUsage.PRESERVE && rc.type() != ResourceType.SPIRIT) {
                this.reservedCast.merge(rc.type().getSerializedName(),
                        (float) rc.value().getValue(context), Float::sum);
            }
        }
        // POE2：使用非瞬发攻击立即移除 Archon（中央入口：经 StatusEngine.clearArchons → 触发恢复冷却）。
        // 「技能被使用」是回调点，判定与移除逻辑统一收敛在中央状态引擎（按持有者状态上下文/标签判定）。
        if (skill.cast().cast().left().isPresent() && skill.getCastTime(context) > 0) {
            StatusEngine.clearArchons(caster);
        }
        // POE2 形态（Druid）：技能标签含 shapeshift → 立即变身（StatusEngine.applyForm 中央入口，
        // 先清当前形态再施加新形态）；技能为 Spell → 立即变回人类（clearForms）。
        // 「技能被使用」是回调点，形态判定/互斥/事件统一收敛在中央状态引擎（读技能标签 + form 标签判定）。
        Set<String> labels = skill.getLabels(context);
        if (labels.contains(GateTag.SHAPESHIFTED)) {
            StatusType form = resolveForm(labels);
            if (form != null) StatusEngine.applyForm(caster, form);
        } else if (skill.cast().cast().right().isPresent()) {
            StatusEngine.clearForms(caster);
        }
        // 诅咒技能：技能直接附加自身为状态——施放时对周围敌人施加
        // 「技能即状态」（StatusEngine.applySkill：状态 id=技能 id、图标=技能图标，kind=CURSE，
        // 运行时动态注册）。范围/时长由技能 data 提供（Skill.curseRange/curseDuration），
        // 缺省回退下方常量。
        if (labels.contains(GateTag.CURSE) && !caster.level().isClientSide()) {
            // 真读：带 SkillContext 求值，使预设写的 $curse_range / $curse_duration 参数槽
            // 由 SkillRead.vars 注入后在此生效（无 context 的 getFloat() 读不到 $ 槽 → 死线）。
            double radius = skill.curseRange()
                    .map(v -> v.getValue(context)).orElse(DEFAULT_CURSE_RADIUS);
            int duration = skill.curseDuration()
                    .map(v -> v.getInt(context)).orElse(DEFAULT_CURSE_DURATION);
            // 诅咒激活延迟：入队延迟施放（按施法者属性计算延迟，到期由 CurseActivation#tick 重放 applyCurseToNearby）
            CurseActivation.enqueue(caster, skillId, radius, duration);
            caster.setData(AttachmentRegistry.LAST_CAST_CURSE, skillId.toString());
        }
        // 标记技能（技能型）：施放时立即对周围敌人施加「Mark」标记（MarkHelper.applyMarkToNearby，
        // 经 MarkHelper.applyMarkToNearby 施加为 StatusKind.MARK 类状态（动态注册、source=标记者），遵循「技能型动态附加 Status」裁定）。
        // 范围/时长由技能 data 提供（Skill.markRange/markDuration），缺省回退下方常量。
        // 收益（你标记的敌人承受更多伤害等）由施法者侧 ENEMIES_YOU_MARK_* 经
        // onTarget(COND_PRESENCE_MARK_STATUS) 真读（StatusValue 解析 mark 标记，R3 安全，非死线）。
        if (labels.contains(GateTag.MARK) && !caster.level().isClientSide()) {
            // 真读：同 curse，带 SkillContext 使 $mark_range / $mark_duration 参数槽生效。
            double radius = skill.markRange()
                    .map(v -> v.getValue(context)).orElse(DEFAULT_MARK_RADIUS);
            int duration = skill.markDuration()
                    .map(v -> v.getInt(context)).orElse(DEFAULT_MARK_DURATION);
            MarkHelper.applyMarkToNearby(caster, radius, duration);
        }
        this.phaseInstances = skill.run(this);
        // 回响登记（首次施放）：必须在技能作用域内读取，属性通道（ECHO_COUNT）的
        // 「对 [某标签]/[某技能]」定向才成立——作用域外的读取拿不到本次技能的 GateTag。
        try (var scope = SkillStatScopes.enter(this.context, skill, skillId)) {
            this.echoState.schedule(this, this.phaseInstances);
        }
        // 封印（Seal）：本次施放累积封印 / 触发释放（自动释放回退模式）。见 handleSeal。
        this.handleSeal(this.phaseInstances);
    }

    /** 诅咒默认范围（格）：POE2 诅咒技能基础范围量级，技能 data 提供 range 前使用。 */
    private static final double DEFAULT_CURSE_RADIUS = 24.0;
    /** 诅咒默认持续时间（tick）：100 tick = 5 秒（POE2 诅咒基础持续量级，技能显式给定后覆盖）。 */
    private static final int DEFAULT_CURSE_DURATION = 100;
    /** 标记默认范围（格）：与诅咒同量级，技能 data 提供 mark_range 前使用。 */
    private static final double DEFAULT_MARK_RADIUS = 24.0;
    /** 标记默认持续时间（tick）：160 tick = 8 秒（POE2 印记基础持续量级，技能显式给定后覆盖）。 */
    private static final int DEFAULT_MARK_DURATION = 160;

    /** 按技能标签解析形态状态（shapeshifted + 形态名 → FormStatusTypes 对应状态；无匹配返回 null）。 */
    private static StatusType resolveForm(Set<String> labels) {
        for (StatusType t : StatusType.all()) {
            if (t.kind == StatusKind.FORM && t.tags().stream().anyMatch(labels::contains)) return t;
        }
        return null;
    }

    public Set<SkillPhase> getInitializedPhases() {
        return initializedPhases;
    }

    /**
     * 封印（Seal）结算 — 被封印辅助的技能每次施放累积封印，存满后下一次施放触发释放（自动释放回退模式）。
     *
     * <p>设计（详见 {@code SealState} / {@code SealMod} / {@code SealRelease}）：
     * <ul>
     *   <li>本次施放成功（{@code fresh} 非空）时，读取本技能已聚合的 {@code SealMod} 配置；</li>
     *   <li>若已存满封印（{@code seals >= maxSeals}）：本次施放即触发释放——
     *       按持有封印数 × {@code repeatPerSealBroken} 延迟重放本次阶段，随后清零（自动释放回退）；</li>
     *   <li>若未满：累积充能 {@code charge += 100 / gainEveryCastTimeMultiplier}，按阈值叠封印，
     *       并把激活配置写入附件（供释放时读取 {@code repeatPerSealBroken} / {@code areaIncPerSealBroken}）。</li>
     * </ul>
     * 服务端权威（附件不同步，客户端读不到也不会重放）；非玩家施法者（怪物/召唤物）不参与封印。
     */
    private void handleSeal(List<SkillPhaseInstance> fresh) {
        if (fresh == null || fresh.isEmpty()) return;
        if (caster.level().isClientSide()) return;
        SupportProfile profile = context.getSupportProfile();
        List<SealMod> mods = profile.sealMods();
        if (mods.isEmpty()) return;
        // 多数情况仅一个封印辅助宝石：取首个激活配置（并存多个时取其一，避免叠加歧义）。
        SealMod mod = mods.get(0);
        SealState.Save ss = SealState.Save.get(caster);
        int maxSeals = Math.max(1, mod.maxSeals());
        if (ss.seals() >= maxSeals) {
            // 已存满：本次施放触发释放 —— 按持有封印数重放，随后清零（自动释放回退）。
            int repeats = (int) Math.max(0L, (long) ss.seals() * Math.max(0, mod.repeatPerSealBroken()));
            this.sealRelease.schedule(fresh, repeats, 10, mod.areaIncPerSealBroken());
            SealState.Save.set(caster, new SealState.Save(0, 0f, maxSeals,
                    mod.gainEveryCastTimeMultiplier(), mod.repeatPerSealBroken(), mod.areaIncPerSealBroken()));
            return;
        }
        // 未满：累积充能、按阈值叠封印，并把激活配置写入附件（供释放时读取）。
        float charge = (float) (ss.charge() + 100.0 / Math.max(1.0, mod.gainEveryCastTimeMultiplier()));
        int seals = ss.seals();
        while (charge >= 1f && seals < maxSeals) {
            seals++;
            charge -= 1f;
        }
        SealState.Save.set(caster, new SealState.Save(seals, charge, maxSeals,
                mod.gainEveryCastTimeMultiplier(), mod.repeatPerSealBroken(), mod.areaIncPerSealBroken()));
    }

    public SkillInstance tick() {
        this.time++;
        // 贯穿时间轴：每 tick 以世界游戏时间（服务端 / 客户端同源）算出施放进度与完成态，
        // 使「效果视觉 / 音效」与客户端肢体动画（CastAnimationPacket 同一 startTime / castTimeTicks）
        // 对齐到同一条世界时钟 —— 攻击 / 施法速度经 SkillTimeline.totalCastTicks 已纳入，
        // 动画与效果同步伸缩（核心纠正：释放速度驱动服务端运算与客户端动画）。
        long castWorldNow = this.caster.level().getGameTime();
        this.castProgress = this.timeline.progress(castWorldNow);
        this.castComplete = this.timeline.isComplete(castWorldNow);
        // 贯穿时间轴完成（施放进度达 1）的同一世界时刻发出同步「释放节拍」（见 emitReleaseBeat），
        // 与客户端动画结束点严格对齐（仅确有施法时长的技能触发，瞬发技能不刷屏）。
        if (this.castComplete && !this.released && this.skill.getCastTime(this.context) > 0) {
            this.released = true;
            this.emitReleaseBeat();
        }
        // 引导 N 秒后施加诅咒（ADD_A_CURSE_AFTER_CHANNELLING_FOR_SECONDS）：引导累计 tick ≥ 阈值×20 时，
        // 对施法者最近施放的诅咒重放 applyCurseToNearby（R3 真读，非死线；无最近诅咒则不触发）。
        if (!this.curseAppliedThisChannel) {
            double v = caster.getAttributeValue(AttributeRegistry.ADD_A_CURSE_AFTER_CHANNELLING_FOR_SECONDS);
            if (v > 0 && this.time >= v * 20) {
                String last = caster.getData(AttachmentRegistry.LAST_CAST_CURSE);
                if (last != null && !last.isEmpty()) {
                    try {
                        StatusEngine.applyCurseToNearby(caster, Identifier.parse(last), DEFAULT_CURSE_RADIUS, DEFAULT_CURSE_DURATION);
                        this.curseAppliedThisChannel = true;
                    } catch (Exception ignored) { /* 最近诅咒已注销则跳过 */ }
                }
            }
        }

        // 进入本次 tick 技能行为的作用域：使本 tick 内所有属性读取（技能速度、效果持续时间、
        // 伤害、buff 应用等持续动作）按 ATTACK / SPELL / SKILL_* 标签即时求值。
        // 与原版伤害事件中的精确命中作用域（DamageEvent）同源、可安全嵌套——命中级 scope 在栈顶更精确。
        try (var scope = SkillStatScopes.enter(this.context, this.skill, this.skillId)) {
        if (this.context.isJustReleased()) {
            this.context = this.context.justReleased(false);
        }

        if (this.context.isPress()) {
            if (SkillEvent.cacheKeyPress.contains(caster.getUUID())) {
                this.context = this.context.press(false);
                this.context = this.context.justReleased(true);
            }

            this.context = this.context.pressTime(this.time);
        }

        // 输入模式驱动：Continuous 按住期间周期性重置阶段（脉冲式重复释放），HoldInput 累积蓄力
        if (this.skill.input() instanceof Continuous continuous && this.context.isPress()) {
            int interval = Math.max(1, (int) continuous.tickInterval().getFloat(this.context));
            int castTime = Math.max(1, this.skill.getCastTime(this.context));
            // 首次释放后（time > castTime），每 interval tick 允许顶层阶段重新初始化一次。
            // 【技能内部计时，不走 PeriodicDriver】这里的 interval 是<b>施放节拍</b>——
            // 技能状态机多久重新初始化一次顶层阶段（脉冲式重复释放），与「效果按条目 INTERVAL
            // 周期性施加到目标」是两回事：前者属技能内部、相位自施法完成起算（故减 castTime），
            // 后者由 PeriodicDriver 驱动、相位取世界刻。二者不同源，不可合并。
            if (this.time > castTime && (this.time - castTime) % interval == 0) {
                this.initializedPhases.clear();
            }
            // 到达最大引导时长后强制收尾（等价于松开按键）
            if (continuous.maxDuration().isPresent()
                    && this.time >= continuous.maxDuration().get().getFloat(this.context)) {
                this.context = this.context.press(false).justReleased(true);
            }
        } else if (this.skill.input() instanceof HoldInput hold && this.context.isPress()) {
            float max = Math.max(1f, hold.maxChargeTicks().getFloat(this.context));
            this.context = this.context.charge(Math.min(1f, this.time / max));
        }

        // 单次激活（非玩家施法者默认）：首 tick 后自动松开，使一次性技能结算并自然结束
        if (this.context.autoRelease() && this.time >= 1 && this.context.isPress()) {
            this.context = this.context.press(false).justReleased(true);
        }

        List<SkillPhaseInstance> newPhaseInstances =
                new ArrayList<>(phaseInstances.stream().flatMap(phaseInstance ->
                        phaseInstance.tick(this).stream()).toList());

        // 本次新生成的顶层阶段：先按定向登记回响（引导脉冲重新生成阶段时会自然重新登记一次），
        // 再把本 tick 到期的重放实例并入——顺序不能反，否则延迟 0 的回响会漏登记。
        List<SkillPhaseInstance> fresh = this.skill.run(this);
        this.echoState.schedule(this, fresh);
        newPhaseInstances.addAll(fresh);
        newPhaseInstances.addAll(this.echoState.tick(this));
        // 封印释放重放（存满后下一次施放触发，延迟重放阶段实例，不二次消耗/冷却）。
        newPhaseInstances.addAll(this.sealRelease.tick(this));

        this.phaseInstances = newPhaseInstances;

        return this;
        }
    }

    /**
     * 技能实例是否已结束。
     * <b>回响未完成时不算结束</b>：延迟回响（如 Echoing Cry 的 1.3 秒 = 26 tick）在原施放早已结束后
     * 才到期，若此处只看阶段集合为空，实例会被 {@code SkillEvent} 先行移除、回响永远等不到到期。
     */
    public boolean isStop() {
        return this.phaseInstances.isEmpty() && !this.context.isPress()
                && !this.echoState.hasPending() && !this.sealRelease.hasPending();
    }

    /**
     * 技能实例结束时的清理：释放其登记的「非精魂保留型」CAST 保留（修复「CAST 永不回收」泄漏），
     * 并发布 {@link ReservationChangeEvent}（消除悬空契约死线）。
     * 由 {@code SkillEvent.onSkillEvent} 在移除已停止实例时调用。
     */
    public void teardown() {
        if (this.reservedCast.isEmpty() || this.caster.level().isClientSide()) return;
        // 逐资源释放：保留池按资源分池，本实例登记在哪些资源上就释放哪些（不焊死精魂）。
        for (Map.Entry<String, Float> e : this.reservedCast.entrySet()) {
            String resource = e.getKey();
            float old = ReservationManager.getReserved(caster.getUUID(), resource);
            ReservationManager.releaseCast(caster.getUUID(), resource, e.getValue());
            float now = ReservationManager.getReserved(caster.getUUID(), resource);
            if (Math.abs(now - old) > 1e-3f && caster instanceof ServerPlayer sp) {
                SkillEventBus.forEntity(sp).publish(
                        new ReservationChangeEvent(sp, this, old, now, (float) ResourceRegistry.current(sp, "spirit")));
            }
        }
    }

    public SkillContext getContext() {
        return context;
    }

    public Skill getSkill() {
        return skill;
    }

    /** 基础技能石 id（数据驱动，任意名字），供 stat 按技能名匹配。 */
    public Identifier getSkillId() {
        return skillId;
    }

    public int getSkillCast() {
        return this.skill.getCastTime(this.context);
    }

    /** 贯穿始终的技能时间轴（服务端权威）。 */
    public SkillTimeline getTimeline() {
        return timeline;
    }

    /** 贯穿时间轴驱动的施放进度 0..1（每 tick 由世界游戏时间算出，与客户端动画同源）。 */
    public float getCastProgress() {
        return castProgress;
    }

    /** 施放是否完成（进度达 1）。 */
    public boolean isCastComplete() {
        return castComplete;
    }

    /**
     * 贯穿时间轴完成瞬间的同步「释放节拍」：在服务端施放完成的同一世界时刻，于施法者处迸发能量爆发 + 冲击音，
     * 经服务端广播使所有客户端在客户端动画结束的同一点听到 / 看到 —— 攻击 / 施法速度经 SkillTimeline 已纳入，
     * 故「动画结束」与「释放节拍」严格对齐（核心纠正：释放速度驱动服务端运算与客户端动画，客户端同步重于服务端运算）。
     */
    private void emitReleaseBeat() {
        if (this.caster.level().isClientSide()) return;
        if (this.caster.level() instanceof ServerLevel level) {
            Vec3 p = this.caster.position().add(0.0, this.caster.getBbHeight() * 0.5, 0.0);
            // 技能声明的施放粒子（缺省回退灵魂火苗）：经服务端广播，使所有客户端在客户端动画结束的同一点看到，
            // 与贯穿时间轴释放点严格对齐（核心纠正：释放速度驱动服务端运算与客户端动画，客户端同步重于服务端运算）。
            Identifier particleId = this.skill.castParticle()
                    .orElse(Identifier.fromNamespaceAndPath("minecraft", "soul_fire_flame"));
            ParticleType<?> pt = BuiltInRegistries.PARTICLE_TYPE.getValue(particleId);
            if (pt instanceof ParticleOptions opts) {
                level.sendParticles(opts, p.x, p.y, p.z, 24, 0.6, 0.6, 0.6, 0.1);
            }
            // 技能声明的施放音效（缺省回退 NOTE_BLOCK_BASS）：与粒子同源广播，确保音画严格对齐到释放节拍。
            Identifier soundId = this.skill.castSound()
                    .orElse(Identifier.fromNamespaceAndPath("minecraft", "block.note_block.bass"));
            level.playSound(null, p.x, p.y, p.z, SoundEvent.createVariableRangeEvent(soundId),
                    SoundSource.PLAYERS, 0.6F, 0.8F);
        }
    }

}
