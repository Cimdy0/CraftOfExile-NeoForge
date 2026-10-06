package com.cimdy.coe.data.codec.skill.context;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;

import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.data.gen.stat.effect.StatEffectGen;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceType;
import com.cimdy.coe.data.codec.skill.phase.action.combo.ComboAction;
import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.skill.core.SkillCaster;
import com.cimdy.coe.data.codec.skill.support.SupportProfile;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.status.def.StatusType;
import com.cimdy.coe.data.codec.support.status.runtime.AilmentTransfer;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.cimdy.coe.data.codec.support.status.runtime.TransferStore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SkillContext {
    private final LivingEntity caster;
    private final int bind;
    private SupportProfile supportProfile = SupportProfile.EMPTY;
    /**
     * 本次施放的临时伤害倍率（默认 1）。
     * 由 ChargeAction（effect_per_charge × 实际消耗充能数）与 ComboAction（effect_multiplier_per_stack × 实际消耗层数）
     * 在消耗时乘入，DamageAction 结算时统一消费。SkillContext 每次施放新建，无跨施放残留。
     */
    private float transientDamageMultiplier = 1f;
    /** 一次性冷却旁路标志（战吼消耗充能跳过冷却），setCooldown 消费即清除。 */
    private boolean bypassCooldownOnce = false;
    /** 单次激活标志：非玩家施法者首 tick 后自动松开，使一次性技能结算并结束（玩家恒为 false）。 */
    private boolean autoRelease = false;
    /**
     * 本次施放的连击计数（缓存自 ComboTracker，由 ComboAction 在技能执行前灌入）。
     * 取非负整数值；-1 表示该技能不使用连击机制。
     */
    private int comboCount = -1;
    /** 本次施放对应的连击 ID（与 ComboTracker 中的 key 对应）。 */
    private String comboId = null;
    /**
     * 本次施放的荣耀计数（缓存自 GloryTracker，由 Banner 类技能执行前灌入）。
     * -1 表示不使用荣耀机制。
     */
    private int gloryAmount = -1;
    /** 本次施放对应的荣耀 ID（与 GloryTracker 中的 key 对应）。 */
    private String gloryId = null;
    /**
     * 本次施放对应的技能 id（{@link net.minecraft.resources.Identifier} 字符串，由 SkillStatScopes.enter 注入）。
     * 供技能侧资源转换的来源过滤（sourceTag {@code "skill:<id>"}）按具体技能精确限定。
     */
    private String skillId = null;

    /**
     * 本次施放的技能 stat 标签集（{@code attack}/{@code spell}/{@code command}/{@code warcry} …），
     * 由 {@code Skill.getStatTags} 在施放冷却结算前注入（{@link #skillStatTags}）。
     * 供 {@link #setCooldown} 读取作用域化 {@code COOLDOWN_RECOVERY_RATE}
     * （{@code command}/{@code warcry} 变体，见 {@code StatEffectGen}）使用；缺省空集退化为全局读取（旧行为一致）。
     */
    private Set<String> skillStatTags = Set.of();

    /**
     * 本次施放的<b>技能参数槽</b>（{@code $name} → 值）。
     *
     * <p>由 {@code SkillCaster} 在施放前从 {@code Skill.params()}（定义期默认值 ⊕ {@code SkillRead} 的
     * vars 覆盖层）注入，经 {@link ExpressionContext#forSkillContext} / {@link ExpressionContext#forSkill}
     * 进入求值上下文，使技能数据里的 {@code $reserve} / {@code $radius} … 真实可读——
     * 「预设 + 实例参数」模型的运行期落点（R3：无此注入则参数槽恒为空，整个模型是死线）。
     */
    private Map<String, ExpressionCodec.Value> variables = Map.of();

    public SkillContext(LivingEntity caster, int bind) {
        this.caster = caster;
        this.bind = bind;
    }

    /** 注入本次施放的技能 id（由 SkillStatScopes.enter 调用）。 */
    public SkillContext skillId(String skillId) {
        this.skillId = skillId;
        return this;
    }

    /** 注入本次施放的技能 stat 标签集（供作用域化冷却回复速率读取，见 {@link #setCooldown}）。 */
    public SkillContext skillStatTags(Set<String> skillStatTags) {
        this.skillStatTags = skillStatTags == null ? Set.of() : skillStatTags;
        return this;
    }

    /** 本次施放的技能 id（未注入返回 null）。 */
    public @Nullable String getSkillId() {
        return skillId;
    }

    /** 注入本次施放的技能参数槽（{@code Skill.params()}，含 SkillRead 的 vars 覆盖层）。 */
    public SkillContext variables(Map<String, ExpressionCodec.Value> variables) {
        this.variables = variables == null ? Map.of() : variables;
        return this;
    }

    /** 本次施放的技能参数槽（缺省空 Map）。 */
    public Map<String, ExpressionCodec.Value> getVariables() {
        return variables;
    }

    /**
     * 运行期注入单个参数槽（{@code $name} → 值）：把本次施放中途产生的数值写进参数槽。
     *
     * <p>与定义期 {@link #variables}（施放前由 {@code Skill.params()} 整体灌入）互补——
     * 供「消费通道」类机制在动作执行期写入运行期值：如「消耗异常」把被消费强度注入
     * {@code $consumed_<id>}，使同一次施放后续动作的表达式真实读到该值
     * （{@code ExpressionContext.forSkillContext} 每次求值都重新读取本 Map，故注入即时可见）。
     * 首次注入时把不可变默认 Map 拷为可变副本，不改动定义期默认值。
     */
    public SkillContext putVariable(String name, ExpressionCodec.Value value) {
        if (!(this.variables instanceof HashMap<String, ExpressionCodec.Value>)) {
            this.variables = new HashMap<>(this.variables);
        }
        this.variables.put(name, value);
        return this;
    }

    /** 写入实体 Context 单例的实时输入状态（press）。 */
    public SkillContext press(boolean isPress) {
        ExpressionContext.get(caster).setPress(isPress);
        return this;
    }

    /** 写入实体 Context 单例的实时输入状态（pressTime）。 */
    public SkillContext pressTime(int pressTime) {
        ExpressionContext.get(caster).setPressTime(pressTime);
        return this;
    }

    public int getPressTime() {
        return ExpressionContext.get(caster).getPressTime();
    }

    public double getSkillLevel() {
        return ExpressionContext.get(caster).getSkillLevel();
    }

    public LivingEntity getCaster() {
        return caster;
    }

    public boolean isPress() {
        return ExpressionContext.get(caster).isPress();
    }

    public SkillContext charge(float chargeAmount) {
        ExpressionContext.get(caster).setChargeAmount(chargeAmount);
        return this;
    }

    public float getChargeAmount() {
        return ExpressionContext.get(caster).getChargeAmount();
    }

    public boolean isCharging() {
        return isPress();
    }

    public boolean isJustReleased() {
        return ExpressionContext.get(caster).isJustReleased();
    }

    public SkillContext justReleased(boolean isJustReleased) {
        ExpressionContext.get(caster).setJustReleased(isJustReleased);
        return this;
    }

    public SkillContext skillLevel(int skillLevel) {
        ExpressionContext.get(caster).setSkillLevel(skillLevel);
        return this;
    }

    public SkillContext quality(double quality) {
        ExpressionContext.get(caster).setQuality(quality);
        return this;
    }

    public double getQuality() {
        return ExpressionContext.get(caster).getQuality();
    }

    public int getBind() {
        return this.bind;
    }

    /** 当前技能所链接辅助宝石的聚合效果（默认 EMPTY，不修改任何数值）。 */
    public SkillContext supportProfile(SupportProfile supportProfile) {
        this.supportProfile = supportProfile;
        return this;
    }

    public SupportProfile getSupportProfile() {
        return supportProfile;
    }

    /** 将一个倍率乘入本次施放的临时伤害倍率（充能/连击消耗效果）。 */
    public SkillContext multiplyTransientDamage(float multiplier) {
        if (multiplier > 0) this.transientDamageMultiplier *= multiplier;
        return this;
    }

    /** 本次施放的临时伤害倍率（默认 1，由 DamageAction 结算时消费）。 */
    public float getTransientDamageMultiplier() {
        return transientDamageMultiplier;
    }

    public void setCooldown(double cooldown) {
        // 战吼语义（POE2 Warcry）：消耗耐力充能成功时跳过本次冷却（一次性标志，消费即清除）
        if (bypassCooldownOnce) {
            bypassCooldownOnce = false;
            SkillCaster.of(this.caster).setCooldown(this.bind, 0);
            return;
        }
        // 冷却回复速率（COOLDOWN_RECOVERY_RATE，{command}/{warcry} 作用域变体由 StatEffectGen 注册，base=1.0）：
        // 速率越高冷却越短，故有效冷却 = cooldown / rate。连续倍率经 fold 通道读取，不另建 FIRE 效果。
        // 作用域并入本次施放技能标签（{command}/{warcry}/{grenade} 变体均由 getStatTags 发射，故均已 live）：
        // grenade 技能经 SkillFactory.thrownAttack 的 List.of(GateTag.GRENADE) 注入 Skill.tags → getStatTags 发射 grenade 标签；
        // 全局（无标签）条目恒匹配，不丢失基础回复速率。
        double rate = readCooldownRecovery(this.caster, this.skillStatTags);
        int ticks = (int) Math.round(cooldown / Math.max(0.01, rate));
        SkillCaster.of(this.caster).setCooldown(this.bind, ticks);
    }

    /** 读 COOLDOWN_RECOVERY_RATE：并入本次施放技能标签（{@code command}/{@code warcry} 变体），使「对 [某类] 技能」冷却回复生效。
     * 全局（无标签）条目恒匹配，故不丢失基础回复速率；攻击/法术/元素等无关标签无对应作用域变体，安全无害。 */
    private static double readCooldownRecovery(LivingEntity living, Set<String> tags) {
        if (living == null) return 1.0;
        AutoCloseable scope = StatScopes.enter(living, StatContext.of(tags, null));
        try {
            return living.getAttributeValue(AttributeRegistry.COOLDOWN_RECOVERY_RATE);
        } finally {
            try {
                scope.close();
            } catch (Exception ignored) {
            }
        }
    }

    /** 请求本次施放跳过冷却（由 ChargeAction bypass_cooldown 在消耗充能成功时置位）。 */
    public void requestCooldownBypass() {
        this.bypassCooldownOnce = true;
    }

    // ---- combo (连击) 缓存与消耗 ----

    /** 灌入本次施放的连击 ID 与当前层数（由 ComboAction 在技能执行前调用）。 */
    public SkillContext combo(String comboId, int count) {
        this.comboId = comboId;
        this.comboCount = Math.max(0, count);
        return this;
    }

    /** 当前技能的连击层数（未设置返回 0）。 */
    public int getComboCount() {
        return Math.max(0, comboCount);
    }

    /** 消耗连击层数作为资源代价（由 ResourceType.COMBO.cost 调用）。 */
    public void consumeCombo(int amount) {
        if (comboId == null || comboCount <= 0 || amount <= 0) return;
        int real = Math.min(amount, comboCount);
        comboCount -= real;
        // 同步回持久层
        ResourceRegistry.consumeCombo(caster, comboId, real, List.of(), null, null, null);
    }

    // ---- end combo ----

    // ---- glory (荣耀) 缓存与消耗 ----

    /** 灌入本次施放的荣耀 ID 与当前数量（由 Banner 类技能执行前调用）。 */
    public SkillContext glory(String gloryId, int amount) {
        this.gloryId = gloryId;
        this.gloryAmount = Math.max(0, amount);
        return this;
    }

    /** 当前技能的荣耀值（未设置返回 0）。 */
    public int getGloryAmount() {
        return Math.max(0, gloryAmount);
    }

    /** 消耗荣耀作为资源代价（由 ResourceType.GLORY.cost 调用）。 */
    public void consumeGlory(int amount) {
        if (gloryId == null || gloryAmount <= 0 || amount <= 0) return;
        // 持久层可能因「不消耗荣耀」几率跳过扣点，故以实际扣减量为准（返回 0 = 本次未消耗）
        int real = ResourceRegistry.consumeGlory(caster, gloryId, Math.min(amount, gloryAmount));
        gloryAmount -= real;
    }

    // ---- end glory ----

    // ---- ailment transfer (P6 消费侧接线) ----
    // 把 P2-3 建的 store/consume 转存通道接入阶段递归执行期：本上下文贯穿整个施放/递归，
    // 任一 stage/action 均可经这些方法读取/取回被消费异常的记账数值（异常消费 → 跨阶段转存）。
    // 生产侧已由 StatusEngine.consumeAilment 在 clear 之后写入（AilmentTransfer.store）。

    /** 触发异常消费并把数值记账到施法者 TransferStore（消费侧生产入口：异常消费 → 跨阶段转存）。 */
    public boolean consumeAilmentOn(LivingEntity target, StatusType type) {
        return StatusEngine.consumeAilment(caster, target, type);
    }

    /** 施法者上是否存在某异常的转存记录（不消费）。 */
    public boolean hasTransferredAilment(StatusType type) {
        return AilmentTransfer.has(caster, type);
    }

    /** 取回并清除一条转存记录（一次性消费语义）；无则 null。 */
    public TransferStore.@Nullable TransferRecord consumeTransferredAilment(StatusType type) {
        return AilmentTransfer.consume(caster, type);
    }

    // ---- end ailment transfer ----

    /** 设置单次激活标志（true=首 tick 后自动松开）。 */
    public SkillContext autoRelease(boolean autoRelease) {
        this.autoRelease = autoRelease;
        return this;
    }

    /** 是否单次激活（非玩家施法者默认 true）。 */
    public boolean autoRelease() {
        return autoRelease;
    }

    /**
     * 命中上下文 — 投递产生的命中信息（命中了什么、怎么命中的）。
     * 归入 SkillContext 作为运算上下文的一部分；命中数据仅存于 ExpressionContext（ExContext）一处，
     * 判定统一经 ExpressionContext.getHitContext() 获取（SkillContext 不再另存一份，避免双份缓存）。
     * 实体/方块命中由 entity/block 的 Optional 存在性直接推导（isEntity/isBlock/isMiss）。
     */
    public static record HitContext(
            Optional<Entity> entity,
            Optional<BlockPos> block,
            Optional<Direction> blockFace,
            Vec3 position,
            Vec3 normal,
            float distance,
            Optional<Float> sourceDamageMultiplier
    ) {
        public static HitContext entityHit(Entity entity, Vec3 position, Vec3 normal, float distance) {
            return entityHit(entity, position, normal, distance, Optional.empty());
        }

        public static HitContext entityHit(Entity entity, Vec3 position, Vec3 normal, float distance, Optional<Float> sourceMult) {
            return new HitContext(Optional.of(entity), Optional.empty(),
                    Optional.empty(), position, normal, distance, sourceMult);
        }

        public static HitContext blockHit(BlockPos block, Direction face, Vec3 position, Vec3 normal) {
            return new HitContext(Optional.empty(), Optional.of(block),
                    Optional.of(face), position, normal, 0f, Optional.empty());
        }

        public boolean isEntity() { return entity.isPresent(); }
        public boolean isBlock() { return block.isPresent(); }
        public boolean isMiss() { return entity.isEmpty() && block.isEmpty(); }
    }
}
