package com.cimdy.coe.data.helper.hub;

import com.cimdy.coe.data.gen.stat.effect.StatEffectGen;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceType;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.effect.convert.ResourceConvertStatEffect;
import com.cimdy.coe.data.helper.impl.value.LeechValue;
import com.cimdy.coe.event.Event;
import com.cimdy.coe.event.combat.GameEvent;
import com.cimdy.coe.event.EventName;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * 资源变更统一中枢：所有「改变资源量」的操作（heal / 自然回复 / 偷取 / 伤害）都先经本类，
 * 由其计算 actual / overflow、执行转化（如生命偷取→魔力）、触发满移除回调与带数值回调，
 * 并写入 {@link ExpressionContext} 供 Stat 检测。
 * <p>
 * 资源类型即字符串键（与 {@link GateTag} 资源标签同源：{@code health}/{@code mana}/{@code energy} …）。
 * SPIRIT（精魂）为保留资源，不参与偷取/自然回复/转化。
 */
public final class ResourceEventHelper {

    private ResourceEventHelper() {
    }

    /** 当前生效的回调（默认实现见 {@link DefaultResourceCallback}；Stat 侧可替换以接入转化/检测）。 */
    private static ResourceCallback CALLBACK = new DefaultResourceCallback();

    /** 转化递归深度上限：GAIN 共存模式会递归调用 {@link #onHeal}（A→B 再转 B→C…），
     *  若配置出 A→B 且 B→A 的双向规则则无限递归，故按线程设深度闸（与 StatEngine#fire 的 MAX_FIRE_DEPTH 同思路）。 */
    private static final int MAX_CONVERT_DEPTH = 8;
    private static final ThreadLocal<int[]> CONVERT_DEPTH = ThreadLocal.withInitial(() -> new int[1]);


    /** 当前量（经统一资源注册表；未知资源键返回 0）。 */
    public static float current(LivingEntity e, String resource) {
        return (float) ResourceRegistry.current(e, resource);
    }

    /**
     * 资源上限（未知资源键返回 {@code -1}，供调用方据此跳过「未保留量」类判定，
     * 而不是把未知资源误判为「上限 0 → 恒满」）。
     */
    public static float max(LivingEntity e, String resource) {
        return (float) ResourceRegistry.max(e, resource);
    }

    /** 回复结果。 */
    public record HealResult(float requested, float actual, float overflow, String resource) {
    }

    /** 消耗结果：{@code actual} 为实际扣减量，{@code missing} 为因资源不足而未能扣减的部分。 */
    public record SpendResult(float requested, float actual, float missing, String resource) {
    }

    /**
     * 回复来源门控：某资源被禁止从某来源回复时返回 {@code true}（整笔回复作废）。
     *
     * <p>集中一处判定，供「cannot recover X from Y」族扩展——各资源的禁用标记都是零基属性
     * （{@code ≥1.0} 即生效），由对应 Helper 暴露语义化访问器。
     */
    private static boolean isRecoveryBlocked(LivingEntity target, String resource, @Nullable String sourceTag) {
        if (sourceTag == null) return false;
        if (GateTag.ENERGY_SHIELD.equals(resource) && GateTag.REGEN.equals(sourceTag)
                && ResourceRegistry.isEnergyShieldRegenDisabled(target)) return true;
        // 除生命偷取外不得恢复生命：HEALTH 资源下，凡是来源非 leech（regen/flask/recoup/stat 等）均整笔作废；
        // leech 来源（"leech" 或 "leech:..."）放行。标记由 CANNOT_RECOVER_HEALTH_OTHER_THAN_FROM_HEALTH_LEECH 经 EFF 平加，≥1.0 即生效（R3 真读，非死线）。
        if (GateTag.HEALTH.equals(resource)
                && StatEngine.gateOf(target, "cannot_recover_health_other_than_from_health_leech", Set.of())
                && !(sourceTag != null && sourceTag.startsWith("leech"))) {
            return true;
        }
        return false;
    }

    /**
     * 统一回复入口。
     *
     * @param target    回复目标
     * @param resource  资源键（{@link GateTag} 资源标签）
     * @param requested 请求回复量
     * @param sourceTag 来源标签（如 {@code "leech"} / {@code "leech:coe:fireball"} / {@code "regen"} / {@code "block"}）
     * @param source    来源实体（可为 null）
     */
    public static HealResult onHeal(LivingEntity target, String resource, float requested,
                                     @Nullable String sourceTag, @Nullable LivingEntity source, boolean allowOverflow) {
        if (requested <= 0) return new HealResult(requested, 0, 0, resource);

        // 1. 转化回调（Stat 驱动；默认不转）。GAIN 共存模式会在回调内递归 onHeal，
        //    深度闸：超过上限跳过转化、按原资源直接回复，防 A↔B 双向互转死循环。
        ConvertResult conv;
        int[] depth = CONVERT_DEPTH.get();
        if (depth[0] >= MAX_CONVERT_DEPTH) {
            conv = ConvertResult.replace(resource, requested);
        } else {
            depth[0]++;
            try {
                conv = CALLBACK.onConvert(resource, requested, target, sourceTag, source);
            } finally {
                depth[0]--;
            }
        }
        String finalResource = conv.resource();
        float finalAmount = conv.amount();

        // 1b-2. 怒气无效标记：被标记实体无法获得怒气（整笔作废）。NO_RAGE_EFFECT 为零基标记属性（≥1.0 即生效，R3 真读，非死线）；
        //       怒气恒为 0 → 其加成（读 status:rage）自然失效。覆盖怒气增益入口 / 两类 regen 全部获取入口。
        if (GateTag.RAGE.equals(finalResource) && StatEngine.gateOf(target, "no_rage_effect", Set.of())) {
            return new HealResult(requested, 0, 0, finalResource);
        }

        // 1b. 来源门控：某资源被禁止从某来源回复时整笔作废（如「不能从再生回复能量护盾」）。
        //     与「充能禁用」是两回事——后者拦的是 tick 驱动，本门控拦的是来源标签，二者互不重叠。
        if (isRecoveryBlocked(target, finalResource, sourceTag)) {
            return new HealResult(requested, 0, 0, finalResource);
        }

        // 2. 后端恢复（actual / overflow）——统一经 ResourceRegistry（各资源条目已绑定既有存储后端）。
        //    allowOverflow（POE2「can Overflow maximum Mana」）：魔力回复可超过上限，直接裸写不钳制，
        //    但仍走下方 RESOURCE_GAIN / recordHeal / resource_convert 统一记账，避免绕过中枢。
        //    仅对最终资源为 mana 时生效；转化后非 mana 或其它资源回退钳制写入。
        float actual;
        float overflow;
        if (allowOverflow && GateTag.MANA.equals(finalResource)) {
            float before = target.getData(AttachmentRegistry.MANA);
            target.setData(AttachmentRegistry.MANA, before + finalAmount); // 不钳制，可超 max
            actual = finalAmount;
            overflow = 0f;
        } else {
            actual = (float) ResourceRegistry.generate(target, finalResource, finalAmount, null, target.level().getGameTime());
            overflow = Math.max(0f, finalAmount - actual);
        }

        // 2b. 溢出转移（EXCESS 模式）：先喂饱原资源，只有装不下的量才按规则转给另一资源
        //     （「Excess Life Recovery from Regeneration is applied to Energy Shield」）。
        //     沿用原 sourceTag，使目标资源侧的同源门控依旧生效；深度闸由递归 onHeal 自身保证。
        if (overflow > 0) {
            for (StatEngine.ResourceConversion c : StatEngine.excessConversions(
                    target, ResourceType.byName(finalResource), overflow, sourceTag)) {
                onHeal(target, c.to().getSerializedName(), c.amount(), sourceTag, source);
            }
        }

        // 3. 满移除回调（仅 leech 来源，且资源已满）
        //    门控「未保留部分填满时保留偷取」（MarkerStatEffect）：
        //    资源身份由标记<b>实例的 conditionTags</b> 承载（StatEffectGen 烘焙 {health}），故本分支
        //    不写任何 health 专属判断——把资源键交给 markerMagnitude 的查询标签集即可。
        //    命中条件：标记幅度 > 0 且 该资源确有保留 且 其<b>未保留</b>部分已满（ReservationManager 通用判定）。
        if (sourceTag != null && sourceTag.startsWith("leech") && isFull(finalResource, target)
                && !leechRetainedWhileUnreservedFull(target, finalResource)) {
            CALLBACK.onResourceFull(finalResource, target);
        }

        // 4. 带数值回调（实际回复 / 溢出）
        CALLBACK.onHealed(finalResource, requested, actual, overflow, target, sourceTag, source);

        // 5. Context 回复量检测
        ExpressionContext.recordHeal(target, finalResource, requested, actual, overflow);

        // 6. 事件记录：资源键即标签，来源以 ":" 分层拼接（如 "rage:melee"），供 recently/count 与 fold 读取
        if (actual > 0) {
            Event.record(target, EventName.RESOURCE_GAIN, tagOf(finalResource, sourceTag));
        }

        return new HealResult(requested, actual, overflow, finalResource);
    }

    /** 统一回复入口（5 参便捷重载）：默认不允许多余上限的溢出回复。 */
    public static HealResult onHeal(LivingEntity target, String resource, float requested,
                                     @Nullable String sourceTag, @Nullable LivingEntity source) {
        return onHeal(target, resource, requested, sourceTag, source, false);
    }

    /**
     * 统一消耗入口：与 {@link #onHeal} 对称，承载技能耗蓝、怒气消耗、怒气固有流失等一切「减少资源」的操作。
     *
     * @param target    目标
     * @param resource  资源键（{@link GateTag} 资源标签）
     * @param requested 请求扣减量
     * @param sourceTag 来源标签（如 {@code "skill"} / {@code "inherent_loss"}）
     * @param source    来源实体（可为 null）
     */
    public static SpendResult onSpend(LivingEntity target, String resource, float requested,
                                      @Nullable String sourceTag, @Nullable LivingEntity source) {
        if (requested <= 0) return new SpendResult(requested, 0, 0, resource);

        // 1. 转化回调（Stat 驱动；默认不转）：扣除方向同样可被 resource_convert 重定向
        ConvertResult conv = CALLBACK.onConvert(resource, requested, target, sourceTag, source);
        String finalResource = conv.resource();
        float finalAmount = conv.amount();

        // GAIN 模式（扣除方向）：源资源照常扣，目标资源也扣转换份额
        if (conv.mode() == ResourceConvertStatEffect.Mode.GAIN && !finalResource.equals(resource)) {
            ResourceRegistry.consume(target, resource, requested);
        }

        float actual = (float) ResourceRegistry.consume(target, finalResource, finalAmount);
        float missing = Math.max(0f, requested - actual);

        CALLBACK.onSpent(resource, requested, actual, missing, target, sourceTag, source);
        ExpressionContext.recordSpend(target, resource, requested, actual, missing);
        if (actual > 0) {
            // RESOURCE_SPEND 真实 fire：事件名 resource_spend 自动并入 fireTags，资源键（如 "mana"）作标签；
            // 真实消耗量 actual 经 GameEvent.emit 的 value → StatEngine.fire 的 eventValue 透传给效果
            // （如「消耗 X% 最大魔力→执政官」按单次消耗额阈值判定，R3 安全、读取点真实）。
            GameEvent.emit(EventName.RESOURCE_SPEND, target, null, Set.of(resource, tagOf(resource, sourceTag)), null, (int) Math.round(actual));
            // 怒气消耗金额通道：供 tickRageRegenFromSpent 经 Event.State.sum 读取窗口内总消耗（R3 安全）。
            if (GateTag.RAGE.equals(resource)) {
                Event.record(target, EventName.RAGE_SPENT, (int) Math.round(actual));
            }
        }
        return new SpendResult(requested, actual, missing, resource);
    }

    /** 资源标签：资源键 + 可选来源维度（{@code "health:leech"}），与偷取标签同构。 */
    private static String tagOf(String resource, @Nullable String sourceTag) {
        return sourceTag == null || sourceTag.isEmpty() ? resource : resource + ":" + sourceTag;
    }

    /**
     * 「某资源的偷取效果在其<b>未保留</b>部分填满时不被移除」判定（通用，资源键参数化）。
     *
     * <p>语义（PoE2：「Life Leech effects are not <i>removed</i> when <i>Unreserved</i> Life is Filled」）：
     * 当资源有被保留的部分时，真正「填满」的判据是<b>未保留部分</b>（上限 − 保留）而非总量上限；
     * 未保留部分填满即视为满，此时偷取实例<b>保留</b>而非清除。
     *
     * <p>判定全部由资源键驱动：标记实例 conditionTags（{@code {health}}）决定本机制作用于哪个资源，
     * 保留量取 {@link ReservationManager} 的<b>分资源池</b>，未保留量/满判定取其通用谓词。
     * 故「魔力偷取在未保留魔力填满时不移除」只需新增一条 conditionTags={@code {mana}} 的同 Kind EFF，零 Java 改动。
     */
    private static boolean leechRetainedWhileUnreservedFull(LivingEntity target, String resource) {
        if (!StatEngine.gateOf(target, "leech_retain_while_unreserved_full",
                Set.of(resource))) return false;
        // 有保留才有「未保留部分」可言：无保留时未保留量 = 上限，退化为普通满判定（不保留偷取）。
        if (ReservationManager.getReserved(target.getUUID(), resource) <= 0f) return false;
        return ReservationManager.isUnreservedFull(target, resource);
    }

    private static boolean isFull(String resource, LivingEntity e) {
        double m = ResourceRegistry.max(e, resource);
        return m > 0 && ResourceRegistry.current(e, resource) >= m;
    }

    // —— 回调接口与默认实现 ——

    public interface ResourceCallback {
        /** 转化：返回重定向后的 (资源, 量)。默认不转。 */
        default ConvertResult onConvert(String resource, float amount, LivingEntity target,
                                        @Nullable String sourceTag, @Nullable LivingEntity source) {
            return ConvertResult.replace(resource, amount);
        }

        /** 资源满时的回调（如移除对应偷取）。默认：移除该资源所有偷取实例。 */
        default void onResourceFull(String resource, LivingEntity target) {
            LeechValue.removeResource(target, resource);
        }

        /** 带数值回复回调：requested / actual / overflow。 */
        default void onHealed(String resource, float requested, float actual, float overflow,
                              LivingEntity target, @Nullable String sourceTag, @Nullable LivingEntity source) {
        }

        /** 带数值消耗回调：requested / actual / missing（资源不足未扣减部分）。 */
        default void onSpent(String resource, float requested, float actual, float missing,
                             LivingEntity target, @Nullable String sourceTag, @Nullable LivingEntity source) {
        }
    }

    public record ConvertResult(String resource, float amount, ResourceConvertStatEffect.Mode mode) {
        public static ConvertResult replace(String resource, float amount) {
            return new ConvertResult(resource, amount, ResourceConvertStatEffect.Mode.REPLACE);
        }

        public static ConvertResult gain(String resource, float amount) {
            return new ConvertResult(resource, amount, ResourceConvertStatEffect.Mode.GAIN);
        }
    }

    /** 默认回调：接入资源转化（偷取 / 自然回复 / 补给 / 击回 等一切「回复」方向，与技能扣除方向共用同一套 resource_convert 规则）。 */
    public static class DefaultResourceCallback implements ResourceCallback {
        @Override
        public ConvertResult onConvert(String resource, float amount, LivingEntity target,
                                       @Nullable String sourceTag, @Nullable LivingEntity source) {
            ResourceType from = ResourceType.byName(resource);
            if (from == null) return ConvertResult.replace(resource, amount);
            Optional<StatEngine.ResourceConversion> conv = StatEngine.convert(target, from, amount, sourceTag);
            if (conv.isEmpty()) return ConvertResult.replace(resource, amount);
            StatEngine.ResourceConversion c = conv.get();
            if (c.mode() == ResourceConvertStatEffect.Mode.REPLACE) {
                // 去A取B：原本回复 resource 的量，改为回复 c.to()
                return ConvertResult.replace(c.to().getSerializedName(), c.amount());
            }
            // GAIN（共存）：resource 照常回复，额外再以 c.to() 回复 amount 份额
            onHeal(target, c.to().getSerializedName(), c.amount(), sourceTag, source);
            return ConvertResult.gain(resource, amount);
        }
    }
}
