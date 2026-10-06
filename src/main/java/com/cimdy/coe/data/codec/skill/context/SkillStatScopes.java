package com.cimdy.coe.data.codec.skill.context;

import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.gen.stat.wirings.wire.flow.WireMisc;
import com.cimdy.coe.data.codec.skill.helper.state.EchoState;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceCost;
import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.skill.helper.AncestralBoostAttacks;
import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.delivery.Delivery;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 技能行为作用域接线：在一次技能行为（施放 / 每 tick 执行）期间进入 {@link StatContext}，
 * 使施法者身上带「技能标签系统」的标签的天赋加成在该行为内即时生效
 * （消费端接线，见 StatEngine / StatScopes；与伤害事件的命中作用域同源）。
 *
 * <p>技能标签系统由 {@link Skill#getStatTags} 统一产出，包含：
 * <ul>
 *   <li>技能名（直接对应，如 {@code fireball}）——数据侧 stat 直接写该名字即可命中；</li>
 *   <li>攻击/法术（{@code attack}/{@code spell}）+ 技能类型（{@code warcry}/{@code curse} …）；</li>
 *   <li>武器装备需求（{@code equipment} 字符串原样，如 {@code bow}）；</li>
 *   <li>消耗类型（资源类型名原样，如 {@code mana}）；</li>
 *   <li>投放形态（{@code projectile}/{@code area}/{@code beam}/{@code aura}/{@code self}）与行为自述标签（如元素），
 *       按「每个动作」在 {@link #enterAction} 中递归并入，保证阶段级隔离。</li>
 * </ul>
 * 空标签集合等价于 {@link StatContext#EMPTY}（仅被动天赋生效），对当前数值无影响。
 */
public final class SkillStatScopes {

    private SkillStatScopes() {
    }

    /** 进入本次技能行为的作用域，覆盖施法者实体；返回不抛受检异常的 {@link AutoCloseable}。 */
    public static SkillScope enter(SkillContext context, Skill skill, Identifier skillId) {
        // 注入本次技能 id：供技能侧资源转换的来源过滤（ResourceCost → StatEngine.convert sourceTag "skill:<id>"）精确限定单技能
        if (context != null && skillId != null) context.skillId(skillId.toString());
        Set<String> tags = (skill != null && context != null)
                ? skill.getStatTags(context, skillId)
                : new LinkedHashSet<>();
        LivingEntity caster = context != null ? context.getCaster() : null;
        if (caster == null) return new SkillScope(() -> {});
        // 先祖强化（Ancestral Boost）：slam 技能完整执行退出后，授予施法者「下一次攻击」先祖强化计数；
        // 本次攻击技能施放时（计数 > 0 且为 attack），提前把 ancestral_boost 并入技能作用域，
        // 使「先祖强化攻击」的面积加成在施放期间（面积结算早于命中）即被正确求值。
        // 关键时序：授予发生在本作用域「退出」时（见下方 slam 分支的 close 包装），
        // 故 slam 自身的命中不会吃到该计数——仅「下一场攻击」被强化（与 POE2 机制一致）。
        boolean isSlam = skill != null && skill.tags().contains("slam");
        boolean armed = AncestralBoostAttacks.get(caster) > 0;
        boolean isAttack = tags.contains(GateTag.ATTACK);
        if (armed && isAttack) tags.add(GateTag.ANCESTRAL_BOOST);
        // 封印（Seal）作用域发射点：玩家持有 ≥1 封印时，本次技能施放携带 seal 标签，
        // 使已接线的 EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CAST_SPEED_SEAL（死线）复活。
        // 这是 seal 标签唯一发射点——缺此前该标签零发射点，导致该条目恒不匹配、是条死线。
        if (caster != null && caster.getData(AttachmentRegistry.SEAL_STATE).seals() > 0) {
            tags.add(GateTag.SEAL);
        }
        AutoCloseable inner = StatScopes.enter(caster, StatContext.of(tags, skillId));
        // slam 技能完整执行（含其自身命中）退出后，授予施法者 1 次「下次攻击先祖强化」。
        // 退出后才授予，确保 slam 自身的命中不吃到该计数（仅「下一场攻击」被强化）。
        if (isSlam) {
            return new SkillScope(() -> {
                inner.close();
                AncestralBoostAttacks.grant(caster, 1);
            });
        }
        return new SkillScope(inner);
    }

    /**
     * 进入一次「动作执行」作用域：在外层技能作用域基础上<b>合并</b>本阶段投放形态与行为自述的标签
     * （投射物→projectile、范围→area、伤害行为→元素标签 …）。
     * <p>
     * 关键点：作用域栈读取只取栈顶（{@code currentStatContext} peek），故此处必须并入当前栈顶的
     * tags / skills 再入栈，否则内层作用域会遮蔽外层的技能标签与技能 id。
     * 施法者不可用（如残缺回退上下文）时返回 no-op 作用域，行为照常执行。
     */
    public static SkillScope enterAction(ActionContext ctx, @Nullable Delivery delivery, Action action) {
        if (ctx == null || !(ctx.caster() instanceof LivingEntity caster)) {
            return new SkillScope(() -> {});
        }
        // 命中/击杀/到期等异步触发（投射物/范围/光束命中等）发生在宿主技能作用域出栈之后，
        // 此时 caster 的 StatScopes 栈顶为 StatContext.EMPTY——若仅以 current(caster).tags() 为基底，
        // 宿主技能的 {meta}/{triggered}/技能名/装备/消耗等标签会在命中读点全部丢失（典型症状：投射物命中不吃 meta/triggered 加成）。
        // 故以宿主 SkillInstance 的全量 stat 标签为基底（同步施放时与 current 内容一致、异步时补全），
        // 再并入「当前栈顶（若有）+ 投递/行为自述」标签，使命中读点与同步施放同源、且不丢失任何外层标签。
        Set<String> tags = new HashSet<>();
        if (ctx.skill() != null && ctx.skill().getSkillId() != null) {
            tags.addAll(ctx.skill().getSkill().getStatTags(ctx.skill().getContext(), ctx.skill().getSkillId()));
        }
        tags.addAll(StatScopes.current(caster).tags());
        if (delivery != null) tags.addAll(delivery.statTags());
        if (action != null) tags.addAll(action.statTags());
        Identifier skill = StatScopes.current(caster).skill();
        if (ctx.skill() != null && ctx.skill().getSkillId() != null) {
            skill = ctx.skill().getSkillId();
        }
        return new SkillScope(StatScopes.enter(caster, StatContext.of(tags, skill)));
    }

    /**
     * 进入「回响重放」作用域：在当前栈顶标签基础上<b>并入</b> {@link GateTag#ECHO}。
     *
     * <p>这是 {@code echo} 标签<b>唯一</b>的发射点 —— 缺此前该标签零发射点，
     * 导致已接线的 {@code ECHOED_SPELLS_HAVE_INC_AREA_OF_EFFECT}（WireMisc:801 →
     * EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_EFFECT_AREA_ECHO_SPELL）恒不匹配、是条死线。
     * 回响实例执行期间进入本作用域后，「Echoed Spells have X% increased Area of Effect」
     * 类词缀即只对<b>重放出来的那一次</b>生效，不串到原施放（POE2 官方数值亦为回响独享）。
     *
     * <p>并入而非覆盖：保留栈顶既有的技能标签 / 技能 id，使「法术」「技能名」「被触发」
     * 等外层标签在回响重放时依旧可见（与 {@link #enterAction} 同一口径）。
     */
    public static SkillScope enterEcho(LivingEntity caster, @Nullable SkillInstance si) {
        // 回响重放（EchoState.tick）在延迟 tick 后<b>异步</b>触发，此时宿主技能作用域已出栈
        // （StatScopes.current(caster) == EMPTY）；若仅以 current.tags() 为基底，技能名/spell/meta 等
        // 标签会全部丢失（典型症状：EFFECT_AREA_ECHO_SPELL = {echo, spell} 在异步回响恒不匹配、死线；
        // 且按技能 id 的资源转换 sourceTag 因 skill 为空而失效）。
        // 故以宿主 SkillInstance 的全量 stat 标签为基底（同步施放时与 current 内容一致、异步时补全），
        // 再并入当前栈顶（若有）+ echo，使回响重放与「原施放的一部分」同源（POE2：repeat 是原施放的一部分）。
        Set<String> tags = new HashSet<>();
        if (si != null && si.getSkillId() != null) {
            tags.addAll(si.getSkill().getStatTags(si.getContext(), si.getSkillId()));
        }
        tags.addAll(StatScopes.current(caster).tags());
        tags.add(GateTag.ECHO);
        Identifier skill = (si != null && si.getSkillId() != null)
                ? si.getSkillId() : StatScopes.current(caster).skill();
        return new SkillScope(StatScopes.enter(caster, StatContext.of(tags, skill)));
    }

    /** 不抛受检异常的技能作用域包装，便于 try-with-resources 直接使用。 */
    public static final class SkillScope implements AutoCloseable {
        private final AutoCloseable delegate;

        SkillScope(AutoCloseable delegate) {
            this.delegate = delegate;
        }

        @Override
        public void close() {
            try {
                delegate.close();
            } catch (Exception ignored) {
            }
        }
    }
}
