package com.cimdy.coe.data.gen.stat.wirings.wire.resource;

import com.cimdy.coe.data.gen.stat.StatConditionGen;
import com.cimdy.coe.data.gen.stat.effect.StatEffectGen;
import com.cimdy.coe.data.gen.stat.wirings.WiringDsl;

import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.projectile.ProjectileDeliveryExecutor;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.EventHistoryValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.status.StatusValue;
import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.gen.stat.StatConditionIds;
import com.cimdy.coe.data.gen.stat.effect.StatEffectIds;
import com.cimdy.coe.event.combat.DamageEvent;
import com.cimdy.coe.event.combat.GameEvent;
import com.cimdy.coe.event.combat.SummonEvent;
import com.cimdy.coe.event.skill.SkillEvent;
import net.minecraft.resources.Identifier;

import java.util.Map;

import static com.cimdy.coe.data.gen.stat.wirings.WiringDsl.*;
import com.cimdy.coe.data.helper.engine.DamagePipeline;

/**
 * WireRecent 域接线：「近期事件」条件簇——以 {@code EventHistoryValue} 为门控。
 *
 * <p>两个 COND 的判据差异值得记录，是踩过的坑：
 * <ul>
 *   <li><b>窗口</b>：{@code recently:<key>} 中缀<b>固定 4 秒</b>（PoE2 RECENTLY 语义 = 80 tick）
 *       且解析器<b>不接受窗口后缀</b>。语料为 "in the past eight seconds" 时不能用 recently，
 *       必须走 {@code count:<key>} 并显式给窗口 —— 即 {@code COND_COUNT_CAUSED_MELEE_HIT_8S}
 *       用 {@code EventHistoryValue(..., COUNT, ticks(8.0))} 与常量 0 比较，而非 recently。</li>
 *   <li><b>标签</b>：{@code melee} / {@code projectile} 由 {@code GameEvent.formTagsOf} 写入，
 *       且已列入 {@code GameEvent.RECENCY_TAGS} 白名单，故分维查询成立。</li>
 * </ul>
 *
 * <h3>未接（缺口，不得以近似顶替——R3）</h3>
 * <ul>
 *   <li><s>{@code PROJECTILES_HAVE_CHANCE_TO_FORK_IF_YOU_VE_DEALT_A_MELEE_HIT_DAMAGE_IN_THE_PAST_EIGHT_SECONDS}</s>
 *       —— <b>已接</b>（2026-09-05）：新增 {@code PROJECTILE_FORK_CHANCE} 零基属性 +
 *       {@code ProjectileDeliveryExecutor#chanceFork} 发射时掷骰读点后，本条已落在本文件条件簇内。</li>
 *   <li>{@code INC_MELEE_DAMAGE_IF_YOU_VE_DEALT_A_PROJECTILE_ATTACK_HIT_DAMAGE_IN_THE_PAST_EIGHT_SECONDS}、
 *       {@code METRES_TO_MELEE_STRIKE_RANGE_..._PROJECTILE_ATTACK_HIT_DAMAGE_...}：
 *       需 <b>projectile ∧ attack</b> 复合标签；{@code RECENCY_TAGS} 白名单<b>不含 ATTACK</b>，
 *       且分维中缀只能表达单个标签（Identifier 仅切第一个 {@code :}），无法表达 AND。
 *       不可用 {@code projectile} 单项顶替——那会漏掉法术投射物、误命中。</li>
 *   <li>{@code INC_CRITICAL_DAMAGE_BONUS_IF_YOU_VE_DEALT_A_NON_CRITICAL_HIT_RECENTLY}：
 *       需「近期造成过<b>非</b>暴击」= {@code caused_hit} 存在且 {@code crit} 标签不存在，
 *       属「带属性过滤的取反」，现有事件队列不支持。</li>
 * </ul>
 *
 * <h3>参数化时间窗口簇</h3>
 * <p>把「窗口 ≠ 4 秒」的条目按语料实测值（8 / 10 / 20 / 1 / 2 秒）逐个接上，
 * 形状统一抽成 {@code StatConditionGen.countWithinSecs(event, seconds)}。
 * 新增 {@code EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_TAKEN_DAMAGE_DAMAGE_OVER_TIME}
 * （DoT 限定承伤乘算，标签 {@code damage_over_time} 由 {@code DamageEvent.buildHitTags}
 * 对 {@code IS_AILMENT} 伤害源写入），避免用通用 {@code TAKEN_DAMAGE} 把 ±50% 施加到全部承伤。
 *
 * <h3>排除（判据，勿以近似顶替——R3）</h3>
 * <ul>
 *   <li>{@code INC_ARMOR_IF_YOU_VE_HIT_..._MELEE_ATTACK_RECENTLY}、{@code INC_STUN_THRESHOLD_IF_YOU_HAVEN_T_BEEN_STUNNED_RECENTLY}：
 *       落点分别是 vanilla {@code ARMOR} 与 {@code AILMENT_THRESHOLD(stun)}，二者 <b>base = 0</b>，
 *       做 ADD_MULTIPLIED_BASE 恒为 0（三定律 #3 零基禁 inc）——这正是这两个 EFF 不存在的原因。</li>
 *   <li>{@code INC_EVASION_IF_YOU_VE_DODGE_ROLLED_RECENTLY}：{@code CAUSED_DODGE} 是<b>闪避掉一次命中</b>
 *       （{@code DamagePipeline.stageBlock} 里 {@code DODGE_CHANCE} 判定成功），
 *       与「翻滚（Dodge Roll）」<b>不是同一动作</b>——后者是移动技能，未接入事件通道。不可互换。</li>
 *   <li>{@code INC_AREA_OF_EFFECT_IF_YOU_HAVE_STUNNED_AN_ENEMY_RECENTLY}：原判「只有 {@code TAKEN_STUN}、
 *       {@code EventCatalog} 无对称 CAUSED_STUN」——<b>已推翻</b>。轻晕经 {@code AilmentHelper.applyLightStun}
 *       真 fire {@code CAUSED_AILMENT}+"stun" 标签（写入攻击者 taggedTriggers，与 CAUSED_HIT+melee 同通道），
 *       故自侧可读「我近期眩晕了敌人」。已接（见 build()「造成异常分类型近期」段）。
 *       注：重晕标签为 "heavy_stun"（{@code applyHeavyStun}），与轻晕无共享标签，且条件集为逻辑与、
 *       引擎无 OR 节点，本条仅匹配轻晕——战斗中每次有效命中都可能轻晕，功能成立、非死线；
 *       重晕覆盖需引擎引入 OR 原语后统一处理（已记入死线谱系 #23，非阻塞）。</li>
 *   <li>{@code INC_DAMAGE_FOR_EACH_TIME_YOU_VE_WARCRIED_RECENTLY}：
 *       <b>注</b>——{@code USE_WARCRY} 已<b>有</b>发射点
 *       （{@code SkillEvent.tryGrantWarcry} 记录无标签计数队列）。
 *       故排除原因<b>不是</b>事件缺失，而是「按<b>次数</b>累加」需 {@code COUNT} 模式 + 乘算比例缩放，
 *       本引擎的 {@code EventHistoryValue} 只能给出窗口内次数，无法把次数直接转成 {@code inc Damage} 的
 *       逐次累进倍率（等价的 {@code PER_} 原语见 {@code WirePerScaling}）。</li>
 *   <li>{@code INC_DAMAGE_FOR_EACH_DIFFERENT_WARCRY_YOU_VE_USED_RECENTLY}：<b>已接入</b>
 *       ——distinct 原语落地后，SkillEvent.tryGrantWarcry 在 USE_WARCRY 计数队列写入身份标签
 *       skill_<id>，走 {@code distinct:use_warcry:skill} 按战吼技能类型去重（见 WirePerScaling）。</li>
 *   <li>{@code ..._FOR_EACH_DIFFERENT_..._IN_THE_PAST_SECONDS}（非瞬发<b>法术</b> / <b>攻击</b> / 弹药种类）：
 *       同需「窗口内<b>去重</b>计数」现已可实现（distinct 模式已通），但工程<b>无「施放技能」事件</b>
 *       （只有 USE_WARCRY / CAUSED_HIT / CAUSED_AILMENT 等具体事件，{@code EventName} 无 CAST_SKILL）。
 *       要在计数队列写入 skill_<id> 必须先有「施放/使用技能」事件发射点——属引擎扩展（新事件 + 发射点），
 *       非纯数据接线，本轮未造（R3：不臆造事件）。warcry 因本身已有 USE_WARCRY 发射点才得以接入。</li>
 *   <li>{@code REGENERATE_RAGE_PER_SECOND_PER_RAGE_SPENT_RECENTLY}、{@code ALLIES_IN_YOUR_PRESENCE_..._GAIN_RAGE_RECENTLY}：
 *       {@code RAGE_GAINED} 是<b>死键</b>（RageHelper 只 {@code refreshTimer}，走 B 通道，count 恒 0）。</li>
 *   <li>{@code INC_SPELL_DAMAGE_IF_ONE_OF_YOUR_MINIONS_HAS_DIED_RECENTLY}：无 {@code MINION_DIED} 事件
 *       （只有 {@code MINION_REVIVED} 且无发射点）。</li>
 *   <li>{@code INC_SPELL_DAMAGE_IF_YOU_HAVE_CONSUMED_AN_ELEMENTAL_INFUSION_RECENTLY}、
 *       {@code INC_MOVEMENT_SPEED_IF_YOU_VE_USED_A_MARK_RECENTLY}、
 *       {@code INC_EVASION_IF_ENERGY_SHIELD_RECHARGE_HAS_STARTED_IN_THE_PAST_SECONDS}：
 *       元素灌注 / 标记 / ES 回复启动均无事件发射点。</li>
 *   <li>{@code ENEMIES_AFFECTED_BY_YOUR_HAZARDS_RECENTLY_HAVE_INC_ARMOR/EVASION}：原缺口「目标近期被我的 Hazard 影响」，
 *       经 触发时（on_trigger） 对 target 施 HAZARD_MARK 标记状态（80t=4s，对应「recently」窗口）已闭环；
 *       条件走 COND_HAZARD_MARKED（StatusValue 读敌身状态，R3 安全），onTarget 对敌施加护甲/闪避 —— 已在 WireDefense 接好，
 *       不复为缺口。</li>
 * </ul>
 */
public final class WireRecent {

    private WireRecent() {
    }

    public static void build(Map<Identifier, StatEntry> map) {
        // ===================== 近 8 秒造成过近战命中 =====================
        // 「15% increased Projectile Damage if you've dealt a Melee Hit in the past eight seconds」
        // 窗口 8s（非默认 4s）→ COND_COUNT_CAUSED_MELEE_HIT_8S 走 count + 显式窗口。
        map.put(StatIds.INC_PROJECTILE_DAMAGE_IF_YOU_VE_DEALT_A_MELEE_HIT_DAMAGE_IN_THE_PAST_EIGHT_SECONDS,
                self(StatConditionIds.COND_COUNT_CAUSED_MELEE_HIT_8S,
                        StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_PROJECTILE));
        // 「X% chance for Projectiles to Fork if you've dealt a Melee Hit in the past eight seconds」
        // 条件同上（8s 窗口）；效果走新增的 PROJECTILE_FORK_CHANCE 零基属性 {projectile}，
        // 读点 ProjectileDeliveryExecutor#chanceFork 在<b>发射时</b>掷骰（enterMerged 并入技能作用域，条件在施法者侧求值），
        // 命中时经 SkillArrow 的 supportFork>0 分支分裂出 1 个额外投射物。R3 真读，非死线。
        map.put(StatIds.PROJECTILES_HAVE_CHANCE_TO_FORK_IF_YOU_VE_DEALT_A_MELEE_HIT_DAMAGE_IN_THE_PAST_EIGHT_SECONDS,
                self(StatConditionIds.COND_COUNT_CAUSED_MELEE_HIT_8S,
                        StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_PROJECTILE_FORK_CHANCE_PROJECTILE));
        // 「Projectiles have {X}% chance for an additional Projectile when Forking」：
        // 与上一行同族不同语义——上者是「获得一次 Fork 的几率」，本条是「已经 Fork 时额外 +1 个投射物的几率」。
        // 读点 ProjectileDeliveryExecutor#chanceForkExtra（仅 fork>0 时掷骰），R3 真读，非死线。
        map.put(StatIds.PROJECTILES_HAVE_CHANCE_FOR_AN_ADDITIONAL_PROJECTILE_WHEN_FORKING,
                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_PROJECTILE_FORK_EXTRA_CHANCE_PROJECTILE));
        // 对称项：近 8 秒造成过弹射命中 → 近战伤害提升（COND_COUNT_CAUSED_PROJECTILE_HIT_8S + 近战伤害 EFF）
        map.put(StatIds.INC_MELEE_DAMAGE_IF_YOU_VE_DEALT_A_PROJECTILE_ATTACK_HIT_DAMAGE_IN_THE_PAST_EIGHT_SECONDS,
                self(StatConditionIds.COND_COUNT_CAUSED_PROJECTILE_HIT_8S,
                        StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_MELEE));

        // ===================== 近期未造成暴击 =====================
        // 「20% increased Critical Damage Bonus if you haven't dealt a Critical Hit Recently」
        // caused_crit 的计数通道走 DamagePipeline → Event.record：只写计时仓库会让
        // recently/count 查询恒 false。
        map.put(StatIds.INC_CRITICAL_DAMAGE_BONUS_IF_YOU_HAVEN_T_DEALT_A_CRITICAL_HIT_RECENTLY,
                self(StatConditionIds.COND_NOT_RECENTLY_CAUSED_CRIT,
                        StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_DAMAGE));

        // 「16% increased Critical Hit Chance if you haven't dealt a Critical Hit Recently」
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE_IF_YOU_HAVEN_T_DEALT_A_CRITICAL_HIT_RECENTLY,
                self(StatConditionIds.COND_NOT_RECENTLY_CAUSED_CRIT,
                        StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE));

        // ===================== 参数化时间窗口簇 =====================
        // 形状与上方「近 8 秒近战命中」完全一致，差异只在事件名与窗口长度（语料实测值）。
        // 三个 count 型 + 一对 recently/not_recently 型，共 5 条。

        // 「25% increased Damage if you've dealt a Critical Hit in the past 8 seconds」
        // 窗口 8s 由语料 second 参数给出（vals = [0.25, 8.0]）。
        // INC_ 前缀 → ADD_MULTIPLIED_BASE；DAMAGE base 非零，三定律 #3 合规。
        // 与上一条「近期<b>未</b>暴击 → 暴伤/暴率」恰好互补：本条是「近期<b>有</b>暴击 → 总伤害」。
        map.put(StatIds.INC_DAMAGE_IF_YOU_VE_DEALT_A_CRITICAL_HIT_IN_THE_PAST_SECONDS,
                self(StatConditionIds.COND_COUNT_CAUSED_CRIT_8S,
                        StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));

        // 「Regenerate 1% of maximum Life per Second if you've used a Life Flask in the past 10 seconds」
        // 窗口 10s（vals = [0.01, 10.0]）。落点 MAX_REGENERATION（{HEALTH, REGEN} 标签），
        // 语义 = 每秒回复最大生命的 1%——「Regenerate X% of maximum Life per Second」的既有落点。
        // 注：语料限定 Life Flask，而 USE_FLASK 发射时把 flask.resource 并入了 fireTags（{FLASK, <resource>}），
        // 但该标签集只作用于 StatEngine.fire 的触发匹配，不写分维计数队列（taggedTriggers）——
        // Event.record(e,name,int) 只写无标签队列，故无标签 count 是本引擎下唯一可读通道。
        // 「Life」限定在本引擎里由条目自身语义承载（MAX_REGENERATION 的 {HEALTH, REGEN} 标签即生命回复），
        // 不存在「用了魔力药剂却给生命回复」的误触发。
        map.put(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND_IF_YOU_VE_USED_A_HEALTH_FLASK_IN_THE_PAST_SECONDS,
                self(StatConditionIds.COND_COUNT_USE_FLASK_10S,
                        StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_HEALTH_REGEN));

        // 「+15 maximum Rage if you've used a Skill that Requires Glory in the past 20 seconds」
        // 窗口 20s（vals = [15.0, 20.0]）。「Requires Glory」= 消耗荣耀的技能 → CONSUME_GLORY 事件
        // （ResourceRegistry.consumeGlory 在实际消耗量 > 0 时发射，故「用了但最终没扣到荣耀」不会误触发）。
        // 落点 MAX_RAGE 走 ADD_VALUE（「+15 maximum」是绝对值而非百分比）。
        // 对照：RAGE_GAINED 是死键（RageHelper 只 refreshTimer，走 B 通道，count 恒 0），
        // 本条走 CONSUME_GLORY 属 A 通道计数队列，二者不可互换。
        map.put(StatIds.MAX_RAGE_IF_YOU_VE_USED_A_SKILL_THAT_REQUIRES_GLORY_IN_THE_PAST_SECONDS,
                self(StatConditionIds.COND_COUNT_CONSUME_GLORY_20S,
                        StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_RAGE));

        // ── heartstopper 基石一对：±50% DoT 承伤，1 秒窗口 ──
        // 「Take 50% less Damage over Time if you've started taking Damage over Time in the past second」（vals = [-0.5]）
        // 「Take 50% more Damage over Time if you haven't started taking Damage over Time in the past second」（vals = [0.5]）
        // 门控：TAKEN_DOT 由 AilmentHelper.applyDamageDoT 写 Event.record（且列在 MECHANICAL 恒记录），
        //      窗口 1s（语料 "in the past second"）——recently() 便捷构造固定 4s，故显式给 ticks(1.0)。
        // 落点：新建的 DoT 限定变体（GateExpr.of(DAMAGE_OVER_TIME)），而非通用 TAKEN_DAMAGE——
        //      语料明确是 "Damage over Time"，通用落点会把 ±50% 施加到<b>全部</b>承伤（含普通命中），
        //      属语义越界（R3）。标签由 DamageEvent.buildHitTags 对 IS_AILMENT 伤害源写入，精确命中。
        // op：MORE_/LESS_ 前缀 → ADD_MULTIPLIED_TOTAL；TAKEN_DAMAGE base=1.0 非零基，三定律 #3 合规。
        map.put(StatIds.TAKE_MORE_DAMAGE_OVER_TIME_IF_YOU_VE_STARTED_TAKING_DAMAGE_OVER_TIME_IN_THE_PAST_SECOND,
                self(StatConditionIds.COND_RECENTLY_TAKEN_DOT_1S,
                        StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_TAKEN_DAMAGE_DAMAGE_OVER_TIME));
        map.put(StatIds.TAKE_MORE_DAMAGE_OVER_TIME_IF_YOU_HAVEN_T_STARTED_TAKING_DAMAGE_OVER_TIME_IN_THE_PAST_SECOND,
                self(StatConditionIds.COND_NOT_RECENTLY_TAKEN_DOT_1S,
                        StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_TAKEN_DAMAGE_DAMAGE_OVER_TIME));

        // 「16% increased Attack Speed if you haven't Attacked Recently」
        // 门控事件 ATTACK_START（DamagePipeline:243，攻击发起即发射，不要求命中）——
        // 「haven't Attacked」问的是「有没有出手」而非「有没有打中」，故用 ATTACK_START 而非 CAUSED_HIT。
        // 落在 vanilla ATTACK_SPEED（base=4.0 非零），ADD_MULTIPLIED_BASE 合规。
        map.put(StatIds.INC_ATTACK_SPEED_IF_YOU_HAVEN_T_ATTACKED_RECENTLY,
                self(StatConditionIds.COND_NOT_RECENTLY_ATTACK_START,
                        StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_SPEED));

        // ===================== 灌入消耗门控 =====================
        // 「increased Spell Damage if you've Consumed an Elemental Infusion Recently」
        // 落点复用既有法术伤害 EFF（{DAMAGE, SPELL} 标签），门控为新增 COND_RECENTLY_CONSUME_INFUSION。
        // 窗口直接用 recently() 便捷构造的固定 4 秒——语料 "Recently" 正是 PoE2 的 4 秒口径，
        // 与本文件上方那批显式窗口（8/10/20 秒）不同，此处<b>不可</b>加 ticks(N)（会偏离语料原意）。
        map.put(StatIds.INC_SPELL_DAMAGE_IF_YOU_HAVE_CONSUMED_AN_ELEMENTAL_INFUSION_RECENTLY,
                self(StatConditionIds.COND_RECENTLY_CONSUME_INFUSION,
                        StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_SPELL));

        // ===================== 造成异常分类型近期（CAUSED_AILMENT 带具体异常标签）=====================
        // 本批关键突破：所有「造成异常」事件（轻晕 stun / 重晕 heavy_stun / 钉刺 pin / 点燃 ignite /
        // 冰缓 chill / 感电 shock / 中毒 poison / 流血 bleed）都经 AilmentHelper.fireAilment 真 fire
        // {caused_ailment, <ailmentId>}，由 GameEvent.fire → Event.record 写入攻击者 Event.State 的
        // taggedTriggers（与既有 CAUSED_HIT+melee 同通道）。故「造成 X 异常近期」可用纯数据 COND 实现，
        // 零引擎改动，R3 真实读点。COND_RECENTLY_CAUSED_STUN / _PIN 已落地于 StatConditionGen。
        // 注：轻晕标签 = "stun"，重晕标签 = "heavy_stun"（二者无共享标签，且条件集为逻辑与、
        // 引擎无 OR 节点），stun 条仅匹配轻晕——战斗中每次有效命中都可能轻晕，功能成立、非死线；
        // 重晕覆盖待 OR 原语（死线谱系 #23，非阻塞）。

        // 「X% increased Area of Effect if you've Stunned an Enemy Recently」
        // 门控 = 轻晕（CAUSED_AILMENT+"stun"，COND_RECENTLY_CAUSED_STUN，recently 固定 4s 窗口）。
        // 落点 EFFECT_AREA（base>0）→ ADD_MULTIPLIED_BASE，三定律 #3 合规。
        map.put(StatIds.INC_AREA_OF_EFFECT_IF_YOU_HAVE_STUNNED_AN_ENEMY_RECENTLY,
                self(StatConditionIds.COND_RECENTLY_CAUSED_STUN,
                        StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AREA_OF_EFFECT));

        // 「X% increased Movement Speed if you've Pinned an Enemy Recently」
        // 门控 = 钉刺（CAUSED_AILMENT+"pin"，COND_RECENTLY_CAUSED_PIN；pin 标签发射已由
        // WireDefense.ARMOR_BREAK_OF_ARMOR_ON_PINNING_AN_ENEMY 实证）。
        // 落点 MOVEMENT_SPEED（base>0）→ ADD_MULTIPLIED_BASE，三定律 #3 合规。
        map.put(StatIds.INC_MOVEMENT_SPEED_IF_YOU_VE_PINNED_AN_ENEMY_RECENTLY,
                self(StatConditionIds.COND_RECENTLY_CAUSED_PIN,
                        StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MOVEMENT_SPEED));

        // ===================== 召唤物死亡近期门控 =====================
        // 「{X}% inc Spell Damage if one of your Minions has died Recently」
        // 门控 = 新增 COND_RECENTLY_MINION_DIED（recently:minion_death，PoE2 "Recently" = 4 秒固定窗口）。
        // 发射点 DamageEvent.LivingDeathEvent：死亡统一发布点，记在<b>主人</b>名下（owner=主人），
        // 故本条用 self(...) 在主人 store 求值即可命中（不需要跨实体分支，与 #33/#37 无关）。
        // 覆盖全部召唤物（判据 OwnerHelper.getOwner），不限于可复生召唤物
        // ——SummonEvent.onMinionDeath 只处理 isRevivableMinion，不能作发射点。
        // 落点复用既有法术伤害 EFF（{DAMAGE, SPELL}，StatEffectGen:299），DAMAGE base=1.0 非零 → ADD_MULTIPLIED_BASE 合规。
        map.put(StatIds.INC_SPELL_DAMAGE_IF_ONE_OF_YOUR_MINIONS_HAS_DIED_RECENTLY,
                self(StatConditionIds.COND_RECENTLY_MINION_DIED,
                        StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_SPELL));
    }
}
