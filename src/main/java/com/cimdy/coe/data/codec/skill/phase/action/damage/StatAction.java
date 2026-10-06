package com.cimdy.coe.data.codec.skill.phase.action.damage;
import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionTypes;

import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.engine.StatEvalContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.stat.engine.StatStore;
import com.cimdy.coe.data.codec.support.stat.entry.StatGroup;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.data.helper.hub.SpiritManager;
import com.cimdy.coe.data.codec.support.stat.effect.damage.DamageStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.resource.ResourceStatEffect;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;

/**
 * Stat 动作 —— 技能动作「直接使用 Stat」：持有一组 stat 条目 {@link StatEntry}
 * （数值 + 条件 + 选择器 + 效果），在动作执行时按效果亚型分别处理：
 * <ul>
 *   <li><b>属性亚型</b>（{@link com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect}）：
 *       追加进接收者（施法者或命中目标）的 {@link StatStore}，由
 *       {@link com.cimdy.coe.data.codec.support.stat.engine.StatEngine#fold}
 *       在读属性时按作用域即时聚合（标签 / 条件挂在各 entry 上，与天赋条目同一管线）。
 *       写入按 {@code id} 去重（同一动作重复执行为覆盖而非叠加），{@code duration > 0}
 *       时经 StatStore 内建计时域到期自灭——与 {@link ModifierAction} 同一机制。</li>
 *   <li><b>触发亚型</b>（异常类 / {@code ResourceStatEffect} / {@code DamageStatEffect} …）：
 *       在本次动作执行时<b>立即</b>经 {@link StatEffect#onTrigger} 作用于目标
 *       （而非存入 StatStore 等待后续事件），从而以 Stat 的同一套施加原语
 *       （{@code EffectAppliers}）取代旧的动作类。触发时透传<b>当前作用域栈栈顶</b>
 *       （投放形态 / 行为标签 / 技能 id），与 {@link StatEngine#fold} 取作用域同源。</li>
 * </ul>
 * 旧 Action 已全线退役（本类即其统一替代）：
 * {@code StatAction} → {@code StatusStatEffect}（{@code onTrigger} 已补 {@code AilmentHelper.fireStatusCaused}，
 * 眩晕/钉刺破甲链路闭合）；{@code RecoverAction} → {@code ResourceStatEffect}（{@code overflow} 经
 * {@code ResourceEventHelper.onHeal(allowOverflow)} 统一入口覆盖，不再直写 {@code ResourceRegistry}）；
 * {@code CleanseAction} → {@code StatusStatEffect(direction=REMOVE)}（经 {@code StatusEngine.clear} 中央出口移除状态）。
 * 三者均已删类并移出 {@code ActionTypes} 注册。
 *
 * @param entry    本动作承载的 stat 条目。<b>单条目直接传，多条目传 {@link StatGroup}</b>
 *                 （{@code StatGroup implements StatEntry}，执行时摊平为多条）——不再持有裸
 *                 {@code List<StatEntry>}，与条目体系的组合能力一致。
 * @param onTarget true = 属性亚型条目写入命中目标（如「被命中的敌人抗性降低」）；false = 写入施法者。
 *                 触发亚型条目由各自 {@link StatEntry#target()} 选择器决定作用对象，与此无关。
 * @param id       属性亚型条目的去重键；同 id 的旧条目在写入前被移除。缺省时按条目内容派生稳定 key。
 * @param duration 属性亚型条目有效期(tick)。&gt;0 时到期自动从 StatStore 移除；&le;0 视为常驻。
 */
public record StatAction(StatEntry entry, boolean onTarget,
                         Optional<Identifier> id, ExpressionCodec.Value duration) implements Action {

    public static final Codec<StatAction> CODEC = RecordCodecBuilder.create(i -> i.group(
            StatEntry.CODEC.fieldOf("effects").forGetter(StatAction::entry),
            Codec.BOOL.optionalFieldOf("on_target", false).forGetter(StatAction::onTarget),
            Identifier.CODEC.optionalFieldOf("id").forGetter(StatAction::id),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("duration", ExpressionCodec.of(0.0))
                    .forGetter(StatAction::duration)
    ).apply(i, StatAction::new));

    @Override
    public Action.Type<?> getType() {
        return ActionTypes.STAT;
    }

    /**
     * 本动作属性亚型条目的稳定去重 modId：显式 id 优先，否则按条目内容 hash 派生。
     * <p>公开供 {@link com.cimdy.coe.data.helper.hub.SpiritManager} 在精魂光环<b>停用时</b>按
     * {@code timed:<modId>} 前缀精确移除本动作写入的条目（否则常驻条目泄漏）。
     */
    public Identifier modId() {
        return id.orElseGet(() -> Identifier.fromNamespaceAndPath(
                CraftOfExile.MODID, "stat_action_" + Integer.toHexString(entry.hashCode())));
    }

    @Override
    public void execute(ActionContext context) {
        execute(context, null);
    }

    /**
     * 精魂常驻光环重放专用：写入前把属性亚型条目的数值<b>按给定上下文定值化</b>。
     *
     * <p>常驻光环没有 {@code SkillInstance}，条目若保留 {@code skill.level} 等表达式，
     * 会在稍后由 {@code PresenceAura} 用<b>实体级单例上下文</b>求值 —— 那时该上下文的等级字段
     * 可能已被其它技能的施放覆盖（等级张冠李戴，静默错误数值）。在写入侧（宝石等级刚注入完毕）
     * 定值化即消除该不确定性；光环每 tick 重写一次，动态数值仍以 1 tick 粒度刷新。
     *
     * @param bakeCtx 非空 = 定值化上下文；null = 原样写入（普通技能路径行为不变）
     */
    public void execute(ActionContext context, ExpressionContext bakeCtx) {
        LivingEntity owner = context.caster() instanceof LivingEntity l ? l : null;
        LivingEntity target = context.target().orElse(null) instanceof LivingEntity l ? l : null;
        LivingEntity recipient = onTarget ? target : owner;

        StatEvalContext evalCtx = new StatEvalContext(owner, target);
        // 作用域透传：取当前作用域栈栈顶（SkillStatScopes.enterAction 已并入投放/行为/技能标签），
        // 与 StatEngine.fold 的 ec.currentStatContext() 同源，避免触发亚型退化为空作用域。
        StatContext scope = owner != null ? StatScopes.current(owner) : StatContext.EMPTY;

        // 组合条目（StatGroup）在此摊平为多条独立条目；单一条目原样处理。
        List<StatEntry> all = entry instanceof StatGroup g ? g.flatten() : List.of(entry);
        List<StatEntry> persistent = new ArrayList<>();
        for (StatEntry entry : all) {
            if (entry.isTrigger()) {
                // 触发亚型：与 StatEngine.fire 共用标签/条件门控，再立即作用。
                // 与 StatEngine.fire 同一道缝：标签门控 → 条件门控（Condition 解析非空即满足）
                LivingEntity condEntity = evalCtx.resolve(entry.conditionTarget());
                if (condEntity == null) continue;
                if (!entry.tags().matches(scope.tags())) continue;
                if (!StatEngine.conditionsHold(entry, evalCtx, scope, condEntity)) continue;
                StatEffect effect = entry.effect();
                if (effect != null) effect.onTrigger(entry, scope, evalCtx, null);
            } else {
                // 属性亚型：累计后写入接收者 StatStore，由 StatEngine.fold 读时聚合。
                persistent.add(bakeCtx == null ? entry
                        : entry.withAmount(ExpressionCodec.of(entry.evaluateAmount(bakeCtx))));
            }
        }
        if (recipient == null || persistent.isEmpty()) return;

        SkillContext skillCtx = context.skill() != null ? context.skill().getContext() : null;
        double dur = duration.getFloat(skillCtx);
        Identifier modId = modId();
        String entryTag = ModifierAction.entryTag(modId);

        // 给每条属性条目附加去重标记 tag（用于覆盖写入与到期移除的精确过滤）
        List<StatEntry> tagged = new ArrayList<>(persistent.size());
        for (StatEntry e : persistent) {
            tagged.add(withEntryTag(e, entryTag));
        }

        StatStore store = recipient.hasData(AttachmentRegistry.STAT_STORE)
                ? recipient.getData(AttachmentRegistry.STAT_STORE) : StatStore.empty();
        // 增量 API（2-4）：按稳定来源键前缀 timed:<modId> 覆盖写入，替代整表重建；
        // 去重由 removeByKeyPrefix 保证（on_tick 每 tick 刷新为覆盖而非无限叠加；两次执行条目数
        // 不同时清理残留 #j 键）。duration > 0 统一走 StatStore 内建计时域到期自灭
        StatStore next = store.removeByKeyPrefix("timed:" + modId);
        java.util.Map<String, StatEntry> keyed = new java.util.LinkedHashMap<>();
        int i = 0;
        for (StatEntry e : tagged) keyed.put("timed:" + modId + "#" + (i++), e);
        int ticks = dur > 0 ? (int) Math.min(dur, Integer.MAX_VALUE) : 0;
        next = next.addModifiers(keyed, ticks);
        recipient.setData(AttachmentRegistry.STAT_STORE, next);
    }

    /**
     * 返回附加了去重标记 tag 的条目副本（效果标签并入 entryTag，其余字段不变）。
     * <p>
     * 只处理 {@link ModifierStatEffect}——本方法仅在非触发分支调用（{@code entry.isTrigger()==false}），
     * 该分支的条目恒为常驻形态。引用型效果（{@code effect()} 为 right）无法就地改标签，原样返回：
     * 此时不参与去重与到期，等同常驻，与旧行为一致。
     * <p>
     * 副本经 {@link ModifierStatEffect#withTags} 构造：{@code mode} / {@code penaltyRole} /
     * {@code duration} 逐一原样保留，不因重建而丢字段。
     */
    private static StatEntry withEntryTag(StatEntry entry, String entryTag) {
        if (!(entry.effect() instanceof ModifierStatEffect mod)) return entry;
        GateExpr expr = mod.tags();
        if (expr.leaves().contains(entryTag)) return entry; // 已含该标签
        // 去重标记以「与」并入表达式（结构化布尔树，直接追加叶子，不经由 List<String> 包装）
        return entry.withEffect(mod.withTags(expr.and(entryTag)));
    }
}
