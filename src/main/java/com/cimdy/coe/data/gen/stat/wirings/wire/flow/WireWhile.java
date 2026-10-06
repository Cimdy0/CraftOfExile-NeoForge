package com.cimdy.coe.data.gen.stat.wirings.wire.flow;

import com.cimdy.coe.entity.custom.skill.totem.SkillTotem;
import com.cimdy.coe.data.gen.stat.StatGen;

import com.cimdy.coe.data.gen.stat.wirings.WiringDsl;

import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;
import com.cimdy.coe.data.codec.skill.phase.action.movement.SpawnAction;
import com.cimdy.coe.data.helper.hub.SpiritManager;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.core.HealthValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceCountValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.skill.UsingSkillValue;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.gen.stat.StatConditionGen;
import com.cimdy.coe.data.gen.stat.StatConditionIds;
import com.cimdy.coe.data.gen.stat.effect.StatEffectIds;
import com.cimdy.coe.data.gen.stat.StatTargetGen;
import com.cimdy.coe.data.gen.stat.StatTargetIds;
import com.cimdy.coe.event.combat.AttributeEvent;
import com.cimdy.coe.event.combat.GameEvent;
import com.cimdy.coe.event.skill.SkillEvent;
import com.mojang.datafixers.util.Either;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static com.cimdy.coe.data.gen.stat.wirings.WiringDsl.*;
/**
 * WireWhile 域接线：「while <持续状态>」条件簇。
 *
 * <p>与其余 Wire* 的区别：本类的条目<b>数值固定</b>（默认 {@code $value}），全部差异在<b>条件</b>上——
 * 即同一个 EFF 在不同「持续状态」下复用。故按条件维度分节，而非按属性维度。
 *
 * <p>条件来源三档：
 * <ol>
 *   <li><b>既有 COND</b>——LivingState / Status（如 {@code COND_FREEZE_STATUS}）直接复用；</li>
 *   <li><b>Presence 计数布尔化</b>（{@code COND_PRESENCE_TOTEM} / {@code COND_PRESENCE_ALLY}）——
 *       由 {@code PresenceCountValue} 计数节点与常量 0 比较得到；</li>
 *   <li><b>资源在场 / 满值</b>（{@code COND_HAS_ENERGY_SHIELD} / {@code COND_FULL_ENERGY_SHIELD}）——
 *       当前 ES > 0 / ES >= 上限，由 {@code HealthValue} 双侧比较得到。</li>
 * </ol>
 *
 * <h3>未接（缺口，不得以近似顶替——R3）</h3>
 * <ul>
 *   <li><s><b>Companion 类 4 条</b>（{@code INC_ATTACK_SPEED/DAMAGE/HEALTH_RECOVERY_RATE/
 *       ARMOR_EVASION_AND_ENERGY_SHIELD_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE}）</s>
 *       ——<b>已接</b>（见下方「伴侣在场」段）：PoE2 的 Companion 在本模组忠实映射为<b>盟友召唤物</b>，
 *       新增 {@code COND_PRESENCE_ALLY_SUMMONED}（{@code presence_count:ally.summoned > 0}）复用既有 presence
 *       全域扫描读点（Minion 由 {@code SpawnAction.spawnMinion} 写 {@code coe:summoned} 键，真计数非死线）。</li>
 *   <li><b>Movement Speed Penalty 4 条</b>（{@code INC_MOVEMENT_SPEED_PENALTY_*}）：
 *       惩罚 = 负向 {@code ModifierStatEffect} 落 {@code MOVEMENT_SPEED}（值已取负，范式见 WireMisc:686「reduced 值已取负」）；
 *       惩罚削减（削减）= 同一属性上的反向 counter，经既有 {@code ADD_MULTIPLIED_BASE} 聚合路径同源计算（「和削减一起计算」）。
 *       <ul>
 *         <li>{@code INC_MOVEMENT_SPEED_PENALTY_WHILE_ACTIVELY_BLOCKING} —— <b>已接</b>：
 *             {@code COND_BLOCKING_LIVING_STATE} + {@code EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MOVEMENT_SPEED}
 *             （引擎真读，R3 安全）。</li>
 *         <li>{@code INC_MOVEMENT_SPEED_PENALTY_FROM_USING_COLD/FIRE/SKILLS_WHILE_MOVING} 3 条 —— <b>已接</b>：
 *             新增引擎原语 {@code UsingSkillValue}（遍历 {@code SkillEvent.activeSkills} 中 caster==持有者 的技能实例、
 *             按技能定义递归判定含对应元素 DamageAction），以 {@code COND_USING_COLD/FIRE_SKILL}（element=cold/fire）
 *             与 {@code COND_USING_SKILL}（element=any）承接；与 {@code COND_MOVING_LIVING_STATE} 组成双条件 AND
 *             （moving AND using_X_skill）。R3 实时真读，非近似。</li>
 *       </ul></li>
 *   <li><s><b>Herald 5 条</b>（{@code INC_FIRE/COLD/LIGHTNING/CHAOS/PHYSICAL_DAMAGE_WHILE_AFFECTED_BY_HERALD_*} + {@code INC_DAMAGE_WHILE_AFFECTED_BY_A_HERALD}）</s>
 *       ——<b>已接</b>（见 {@code WireWhile}）：HeraldStatusTypes 已登记 5 个 kind=HERALD 存在性标记，
 *       {@code COND_HERALD_OF_ASH/ICE/THUNDER/PLAGUE/BLOOD} + {@code COND_ANY_HERALD} 即对应状态在身判定，复用既有元素伤害 EFF。</li>
 *   <li><b>Charm 3 条</b>（{@code INC_DAMAGE/HEALTH_AND_MANA_RECOVERY_..._ACTIVE_CHARM}）：Charm 机制未接入。</li>
 *   <li><s><b>复合自侧+目标侧 1 条</b>（{@code INC_DAMAGE_AGAINST_IMMOBILISED_ENEMIES_WHILE_SHAPESHIFTED}）</s>
 *       ——<b>已接</b>（见 {@code WireDamage}）：新增 {@code SourceScopeNode} 提供 root-relative 变体，
 *       把自侧门控切到 {@code StatContext.sourceEntity()}（命中结算注入的对侧实体）求值，
 *       条目 = onTarget + {@code COND_PIN_STATUS} + {@code COND_SOURCE_LIVING_STATE_SHAPESHIFTED}。</li>
 *   <li><b>机制/禁令类</b>（{@code CANNOT_BE_CRITICALLY_HIT_WHILE_PARRYING}、
 *       {@code UNAFFECTED_BY_CHILL_WHILE_MANA_LEECH_MANA}、
 *       {@code INVOCATION_SKILLS_CANNOT_GAIN_ENERGY_WHILE_TRIGGERING_SPELLS}、
 *       {@code DAMAGING_AILMENTS_CANNOT_BE_INFLICTED_ON_YOU_...} 等）：
 *       属免疫/禁令与技能机制，非 Stat 修饰接线。</li>
 * </ul>
 */
public final class WireWhile {
    private WireWhile() {
    }
    public static void build(Map<Identifier, StatEntry> map) {
        // ===================== Presence 在场：图腾 =====================
        // COND_PRESENCE_TOTEM = presence_count:ally.totem > 0（PresenceCountValue Kind.TOTEM）
        // ——图腾实体判据为 instanceof SkillTotem，与 PoE2 Summoned Totem 对应。
        // 「X% increased Damage while you have a Totem」
        map.put(StatIds.INC_DAMAGE_WHILE_YOU_HAVE_A_TOTEM,
                self(StatConditionIds.COND_PRESENCE_TOTEM, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE));
        // 「X% increased Area of Effect while you have a Totem」
        map.put(StatIds.INC_AREA_OF_EFFECT_WHILE_YOU_HAVE_A_TOTEM,
                self(StatConditionIds.COND_PRESENCE_TOTEM, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AREA_OF_EFFECT));
        // 「Regenerate X% of maximum Life per second while you have a Totem」
        // EFF 沿用 WireResource L92 的 REGENERATE_OF_MAX_HEALTH_PER_SECOND 同源（ADD_VALUE 速率，非倍率）。
        map.put(StatIds.REGENERATE_OF_MAX_HEALTH_PER_SECOND_WHILE_YOU_HAVE_A_TOTEM,
                self(StatConditionIds.COND_PRESENCE_TOTEM, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_HEALTH_REGEN));
        // ===================== Presence 在场：友军 =====================
        // COND_PRESENCE_ALLY = presence_count:ally.any > 0（同阵营或为持有者所有的任意成员）。
        // 「X% increased Attack Damage while you have an Ally in your Presence」
        map.put(StatIds.INC_ATTACK_DAMAGE_WHILE_YOU_HAVE_AN_ALLIES_IN_YOUR_PRESENCE,
                self(StatConditionIds.COND_PRESENCE_ALLY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_DAMAGE));
        // ===================== Presence 在场：伴侣（Companion 忠实映射为盟友召唤物）=====================
        // COND_PRESENCE_ALLY_SUMMONED = presence_count:ally.summoned > 0（minion 由 spawnMinion 写 coe:summoned 键）。
        // 「随从在场」即「盟友召唤物在场」，复用既有 presence 全域扫描读点，R3 安全。
        map.put(StatIds.INC_DAMAGE_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE,
                self(StatConditionIds.COND_PRESENCE_ALLY_SUMMONED, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_ATTACK_SPEED_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE,
                self(StatConditionIds.COND_PRESENCE_ALLY_SUMMONED, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_SPEED));
        map.put(StatIds.INC_HEALTH_RECOVERY_RATE_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE,
                self(StatConditionIds.COND_PRESENCE_ALLY_SUMMONED, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_REGENERATION_RATE_HEALTH_REGEN));
        // 「护甲+闪避+ES」复合：groupSelfWhen 把一条 stat 接到多个自身 EFF（范式见 WireDefense:215-219）。
        map.put(StatIds.INC_ARMOR_EVASION_AND_ENERGY_SHIELD_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE,
                groupSelfWhen(StatConditionIds.COND_PRESENCE_ALLY_SUMMONED,
                        StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_MINECRAFT_ARMOR,
                        StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION,
                        StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_MAX_ENERGY_SHIELD));
        // ===================== 状态 / 资源在场（既有 COND）=====================
        // 「+X to Armour while Frozen」——冻结状态在身时获得固定护甲加成（ADD_VALUE 绝对值）。
        map.put(StatIds.TO_ARMOR_WHILE_FROZEN,
                self(StatConditionIds.COND_FREEZE_STATUS, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_MINECRAFT_ARMOR));
        // 「X% increased Evasion Rating while you have Energy Shield」
        map.put(StatIds.INC_EVASION_WHILE_YOU_HAVE_ENERGY_SHIELD,
                self(StatConditionIds.COND_HAS_ENERGY_SHIELD, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION));
        // 「X% increased Movement Speed while you have Energy Shield」
        map.put(StatIds.INC_MOVEMENT_SPEED_WHILE_YOU_HAVE_ENERGY_SHIELD,
                self(StatConditionIds.COND_HAS_ENERGY_SHIELD, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MOVEMENT_SPEED));
        // 「X% increased Evasion Rating if Energy Shield Recharge has started in the past 2 seconds」
        // 门控 = 新增 COND_ES_RECHARGE_STARTED_2S（recently:es_recharge_started，2 秒窗口）。
        // 窗口取 2 秒的依据见 COND 注释：PoE2 官方 stat id 即 ..._in_past_2_seconds，秒数编死在 id 里，
        // 全游戏仅此一档（Immaterial notable 50% / Mastery 100% 同为 2 秒），故无需参数化窗口。
        // 读点链（R3 逐节点）：AttributeEvent ES 充能分支（边沿检测，timer 不活跃时 emit 一次）
        //   → GameEvent.emit → Event.record(owner, "es_recharge_started")
        //   → COND 读 recently → 落持有者 EVASION（base=53 非零）→ ADD_MULTIPLIED_BASE 合规。
        // 落点 EVASION 与上方兄弟条目 INC_EVASION_WHILE_YOU_HAVE_ENERGY_SHIELD 完全同构。
        map.put(StatIds.INC_EVASION_IF_ENERGY_SHIELD_RECHARGE_HAS_STARTED_IN_THE_PAST_SECONDS,
                self(StatConditionIds.COND_ES_RECHARGE_STARTED_2S, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION));
        // ===================== Movement Speed Penalty（负向 MOVEMENT_SPEED 修饰）=====================
        // 惩罚 = 负向 ModifierStatEffect 落 MOVEMENT_SPEED（值已取负，范式见 WireMisc:686「reduced 值已取负」）。
        // 引擎真读 MOVEMENT_SPEED（StatGen.modRead / GenEvent / StatusBehavior）→ 持盾时实际减速，R3 安全，
        // 非死线。下列 4 条统一打 PENALTY 标记，使其落入 StatEngine 的独立惩罚子池（penaltyMult），
        // 与正向 multBase 分池——未来接「削减惩罚」天赋（REDUCTION 因子）时只缩放惩罚子池，
        // 绝不会把削减泄漏成白送的正向增益（方案 A）。EFF 数值/属性/运算与共享
        // EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MOVEMENT_SPEED 完全一致，仅 penaltyRole 不同。
        // 「X% increased Movement Speed Penalty while actively Blocking」
        map.put(StatIds.INC_MOVEMENT_SPEED_PENALTY_WHILE_ACTIVELY_BLOCKING,
                penaltyMovementSpeed(StatConditionIds.COND_BLOCKING_LIVING_STATE));
        // ── 「使用某系技能时移动」惩罚削减：双条件 AND（moving AND using_X_skill）──
        // 第二条件经新增 COND_USING_*_SKILL（UsingSkillValue 遍历 SkillEvent.activeSkills 中 caster==持有者 的技能、
        // 按技能定义递归判定含对应元素 DamageAction，R3 实时真读，非近似）。
        // 「X% increased Movement Speed Penalty while using Cold Skills while moving」
        map.put(StatIds.INC_MOVEMENT_SPEED_PENALTY_FROM_USING_COLD_SKILLS_WHILE_MOVING,
                penaltyMovementSpeed(StatConditionIds.COND_MOVING_LIVING_STATE, StatConditionIds.COND_USING_COLD_SKILL));
        // 「X% increased Movement Speed Penalty while using Fire Skills while moving」
        map.put(StatIds.INC_MOVEMENT_SPEED_PENALTY_FROM_USING_FIRE_SKILLS_WHILE_MOVING,
                penaltyMovementSpeed(StatConditionIds.COND_MOVING_LIVING_STATE, StatConditionIds.COND_USING_FIRE_SKILL));
        // 「X% increased Movement Speed Penalty while using Skills while moving」
        map.put(StatIds.INC_MOVEMENT_SPEED_PENALTY_FROM_USING_SKILLS_WHILE_MOVING,
                penaltyMovementSpeed(StatConditionIds.COND_MOVING_LIVING_STATE, StatConditionIds.COND_USING_SKILL));
        // 「X% reduced Movement Speed Penalty with Command Skills」——Puppet Master 每层：
        // 复用 REDUCTION 角色落 MOVEMENT_SPEED（引擎独立惩罚子池，StatEngine 以 (1 - penaltyReduce) 二次缩放），
        // 削减移动速度惩罚子池；作用域由 Puppet Master buff 自身（命令技能触发）提供，故 EFF 无 command 标签。
        map.put(StatIds.REDUCED_MOVEMENT_SPEED_PENALTY_WITH_COMMAND_SKILLS, reduceMovementSpeedPenalty());
        // 「X% increased Spell Damage while on Full Energy Shield」
        // 满盾 = 当前 ES >= ES 上限（COND_FULL_ENERGY_SHIELD），与满血条件同构。
        map.put(StatIds.INC_SPELL_DAMAGE_WHILE_ON_FULL_ENERGY_SHIELD,
                self(StatConditionIds.COND_FULL_ENERGY_SHIELD, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_SPELL));
        // 「X% increased Spell Damage while wielding a Melee Weapon」
        // 主手持近战武器（剑/斧/锤，COND_WIELDING_MELEE_WEAPON 经 ItemTypeChecker.isSword/isAxe/isMace 判定）。
        map.put(StatIds.INC_SPELL_DAMAGE_WHILE_WIELDING_A_MELEE_WEAPON,
                self(StatConditionIds.COND_WIELDING_MELEE_WEAPON, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_SPELL));
        // ===================== 捷在身（HeraldStatusTypes 存在性标记）=====================
        // 门控读统一状态层 SpiritManager 每 tick 随精魂光环重放刷新的「受某捷影响」标记（R3 安全）。
        // 各条按对应元素落各自的增伤 EFF，与无条件版本（INC_FIRE_DAMAGE 等）同源。
        // 「X% increased Damage while affected by a Herald」→ 任意捷（kind=HERALD）。
        map.put(StatIds.INC_DAMAGE_WHILE_AFFECTED_BY_A_HERALD,
                self(StatConditionIds.COND_ANY_HERALD, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE));
        map.put(StatIds.INC_FIRE_DAMAGE_WHILE_AFFECTED_BY_HERALD_OF_ASH,
                self(StatConditionIds.COND_HERALD_OF_ASH, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_FIRE));
        map.put(StatIds.INC_COLD_DAMAGE_WHILE_AFFECTED_BY_HERALD_OF_ICE,
                self(StatConditionIds.COND_HERALD_OF_ICE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_COLD_DAMAGE));
        map.put(StatIds.INC_LIGHTNING_DAMAGE_WHILE_AFFECTED_BY_HERALD_OF_THUNDER,
                self(StatConditionIds.COND_HERALD_OF_THUNDER, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_LIGHTNING));
        map.put(StatIds.INC_CHAOS_DAMAGE_WHILE_AFFECTED_BY_HERALD_OF_PLAGUE,
                self(StatConditionIds.COND_HERALD_OF_PLAGUE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CHAOS_COE_DAMAGE_DAMAGE));
        map.put(StatIds.INC_PHYSICAL_DAMAGE_WHILE_AFFECTED_BY_HERALD_OF_BLOOD,
                self(StatConditionIds.COND_HERALD_OF_BLOOD, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_PHYSICAL));
    }
    /**
     * 惩罚型移动速度修饰：复用 {@code MOVEMENT_SPEED} 的 {@code ADD_MULTIPLIED_BASE} 运算（与共享
     * {@code EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MOVEMENT_SPEED} 同属性同运算同取值），
     * 但打 {@link ModifierStatEffect.PenaltyRole#PENALTY} 标记，使其落入 StatEngine 的独立惩罚子池，
     * 与正向 multBase 分池。数值本身由接线层取负（范式见 WireMisc「reduced 值已取负」）。
     * 条件（Cond）仍按 PoE 语料原样传入（持盾 / 移动 + 使用某系技能）。
     */
    private static StatRead penaltyMovementSpeed(Identifier... condRefs) {
        ModifierStatEffect eff = new ModifierStatEffect(
                Attributes.MOVEMENT_SPEED,
                Either.left(AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                ModifierStatEffect.Mode.GAIN,
                ModifierStatEffect.PenaltyRole.PENALTY,
                GateExpr.ALWAYS);
        List<ValueExpr> conds = condRefs.length == 0 ? List.of()
                : Arrays.stream(condRefs).flatMap(c -> StatConditionGen.conditions(c).stream()).toList();
        return new StatRead(ExpressionCodec.VALUE, conds, StatTargetGen.targets(StatTargetIds.TGT_SELF_ONLY), eff);
    }
    /**
     * 削减型移动速度惩罚：复用 {@code MOVEMENT_SPEED} 的 {@code ADD_MULTIPLIED_BASE} 运算，
     * 但打 {@link ModifierStatEffect.PenaltyRole#REDUCTION} 标记——不进入正向加值池，
     * 而在 StatEngine 聚合阶段累加为惩罚削减因子（penaltyReduce），仅缩放独立惩罚子池（penaltyMult），
     * 使移动速度惩罚变轻而绝不反向增益正向池（方案 A，详见 StatEngine#fold / aggregate）。
     * 数值由接线层取正（Puppet Master 每层 +10% × effect × stacks），代表「减少 X% 惩罚」；
     * 作用域由 Puppet Master buff 自身（命令技能触发）提供，故无 command 标签。
     */
    private static StatRead reduceMovementSpeedPenalty() {
        ModifierStatEffect eff = new ModifierStatEffect(
                Attributes.MOVEMENT_SPEED,
                Either.left(AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                ModifierStatEffect.Mode.GAIN,
                ModifierStatEffect.PenaltyRole.REDUCTION,
                GateExpr.ALWAYS);
        return new StatRead(ExpressionCodec.VALUE, List.of(), StatTargetGen.targets(StatTargetIds.TGT_SELF_ONLY), eff);
    }

}
