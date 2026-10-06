package com.cimdy.coe.data.gen.stat.wirings.wire.attribute;

import com.cimdy.coe.data.gen.stat.wirings.WiringDsl;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.damage.DamageValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.HitStateValue;
import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.gen.stat.StatConditionIds;
import com.cimdy.coe.data.gen.stat.effect.StatEffectIds;
import net.minecraft.resources.Identifier;

import java.util.Map;

import static com.cimdy.coe.data.gen.stat.wirings.WiringDsl.*;
import com.cimdy.coe.data.helper.engine.DamagePipeline;

/**
 * WireCrit 域接线：「with Critical Hits」暴击命中条件簇。
 *
 * <p><b>条件来源</b>：{@code COND_CRIT_HIT} = {@code damage.crit > 0}，
 * 读 {@code DamageValue} 的 <b>CONTEXT</b> 源（中缀 {@code damage.crit} → {@code DamageContext.isCrit()}）。
 *
 * <p><b>为何不用 HitStateValue</b>（关键判据，勿回退）：
 * {@code HitStateValue} 读的是<b>投递阶段</b>（delivery）的 {@code HitContext}，只有
 * {@code is_entity / is_block / is_miss} 三个模式；而暴击是在<b>伤害管线</b>（DamagePipeline）里判定的，
 * 时间上晚于投递。给 {@code HitContext} 加 {@code is_crit} 字段语义不成立——投递时根本无从得知是否暴击。
 * 正确答案是从伤害管线写入的 {@code DamageContext} 读，即 {@code damage.crit}。
 *
 * <p><b>作用域</b>：全部走 {@code self}（施加侧）。这与兄弟条目一致——
 * 例如 {@code INC_BUFF_MAGNITUDE_OF_SHOCK_YOU_INFLICT} 亦为 {@code self(...)} 无条件版，
 * 此处只加暴击门控，不走 {@code onTarget}（「你施加的」语义由 EFF 的 {@code not(TAKEN)} 标签承载，
 * 而非由作用域承载）。
 *
 * <h3>未接（缺口，不得以近似顶替——R3）</h3>
 * <ul>
 *   <li>{@code ARMOR_BREAK_ARMOR_ON_CRITICAL_HIT_WITH_SPELLS_EQUAL_TO_OF_PHYSICAL_DAMAGE_DEALT}
 *       （「Break Armour on Critical Hit with Spells equal to 10% of Physical Damage dealt」）：
 *       候选 EFF {@code EFF_ADD_VALUE_ARMOR_BREAK_ATTRIBUTE_COE_ARMOR_BREAK} 已注册且未被引用，
 *       但接线需把量写成 {@code $value * damage.physical / $per}，其正确性取决于
 *       <b>ARMOR_BREAK 属性被读取的时刻 DamageContext 是否仍在作用域内</b>——
 *       该读取时机尚未核验，按 R3「实现 = 引擎真读」暂不接，待核验后补。</li>
 *   <li>{@code CHANCE_TO_INFLICT_BLEEDING_ON_CRITICAL_HIT_WITH_ATTACKS}：
 *       需「暴击时按几率施加 Bleeding」的 chance-effect 组合，属引擎 chance 通道，不在本文件范围。</li>
 *   <li>{@code INC_CRITICAL_DAMAGE_BONUS_IF_YOU_VE_DEALT_A_NON_CRITICAL_HIT_RECENTLY}：
 *       需「最近造成过<b>非</b>暴击」——即 {@code recently:caused_hit} 与 crit 取反的交集，
 *       现有事件队列不支持「带属性过滤的取反」，属引擎扩展。</li>
 * </ul>
 */
public final class WireCrit {

    private WireCrit() {
    }

    public static void build(Map<Identifier, StatEntry> map) {
        // ===================== 异常强度：暴击命中时施加的更强 =====================
        // 四条均为既有「you inflict」施加侧 EFF + COND_CRIT_HIT 门控。
        // EFF 选取一律对齐各自的<b>无暴击版兄弟条目</b>（WireAilment 已接线），保证施加侧语义一致。

        // 「X% increased Magnitude of Bleeding you inflict with Critical Hits」
        // 兄弟：INC_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT → 同 EFF（BLEED + not TAKEN）
        map.put(StatIds.INC_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT_WITH_CRITICAL_HITS,
                self(StatConditionIds.COND_CRIT_HIT, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_AILMENT_ATTRIBUTE_BLEED_COE_AILMENT_MAGNITUDE_TAKEN));

        // 「X% increased Magnitude of Damaging Ailments you inflict with Critical Hits」
        // 兄弟：INC_BUFF_MAGNITUDE_OF_DAMAGING_AILMENTS_YOU_INFLICT（DAMAGE_OVER_TIME + not TAKEN）
        map.put(StatIds.INC_BUFF_MAGNITUDE_OF_DAMAGING_AILMENTS_YOU_INFLICT_WITH_CRITICAL_HITS,
                self(StatConditionIds.COND_CRIT_HIT, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AILMENT_MAGNITUDE_DAMAGE_OVER_TIME_TAKEN));

        // 「X% increased Magnitude of Non-Damaging Ailments you inflict with Critical Hits」
        // 兄弟：INC_BUFF_MAGNITUDE_OF_NON_DAMAGING_AILMENTS_YOU_INFLICT（NON_DAMAGING，无 TAKEN 后缀）
        map.put(StatIds.INC_BUFF_MAGNITUDE_OF_NON_DAMAGING_AILMENTS_YOU_INFLICT_WITH_CRITICAL_HITS,
                self(StatConditionIds.COND_CRIT_HIT, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AILMENT_MAGNITUDE_NON_DAMAGING));

        // 「X% increased Magnitude of Shock you inflict with Critical Hits」
        // 兄弟：INC_BUFF_MAGNITUDE_OF_SHOCK_YOU_INFLICT（SHOCK，无 TAKEN 后缀）
        map.put(StatIds.INC_BUFF_MAGNITUDE_OF_SHOCK_YOU_INFLICT_WITH_CRITICAL_HITS,
                self(StatConditionIds.COND_CRIT_HIT, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AILMENT_MAGNITUDE_SHOCK_TAKEN));

        // ===================== 眩晕积累：暴击命中时 more =====================
        // 「X% more Stun Buildup with Critical Hits」
        // MORE_ 前缀按 op 族走 ADD_MULTIPLIED_TOTAL（该 EFF：
        // 既有 AILMENT_BUILDUP+STUN 只有 ADD_MULTIPLIED_BASE 与 ADD_VALUE 两个变体）。
        // AILMENT_BUILDUP 的 base = 1.0 非零，故 TOTAL 不违反「零基禁 inc」（三定律 #3）。
        map.put(StatIds.MORE_STUN_BUILDUP_WITH_CRITICAL_HITS,
                self(StatConditionIds.COND_CRIT_HIT, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_AILMENT_BUILDUP_STUN));
    }
}
