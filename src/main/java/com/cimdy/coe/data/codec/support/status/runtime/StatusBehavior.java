package com.cimdy.coe.data.codec.support.status.runtime;

import com.cimdy.coe.data.codec.support.status.def.AilmentSpec;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.data.helper.combat.DamageHelper;
import com.cimdy.coe.data.codec.support.stat.effect.grant.GrantOnEventStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.status.ImmuneStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.event.combat.DamageEvent;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.registry.DamageTypeRegistry;
import com.cimdy.coe.data.codec.support.damage.DamageTypes;
import com.cimdy.coe.data.codec.skill.context.SkillDamageSource;
import com.cimdy.coe.data.codec.support.attribute.AttributeLimiters;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.convert.DamageConvertStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;
import com.cimdy.coe.data.gen.stat.StatGen;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import com.cimdy.coe.data.codec.support.status.def.StatusType.StatusTickLogic;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.*;
import com.cimdy.coe.data.helper.engine.DamagePipeline;

/**
 * Status 行为落点工具：把原本由 MobEffect 驱动的状态效果，统一经 Status 系统的
 * {@code statProvider}（属性层）与 {@code tickLogic}（DoT / 控制衰减）实现。
 * 机制数值对齐 poe2db（Ailments）。
 *
 * <p>全量重构（用户指令「写数据生成用 Read，具体条目写入 StatGen」）：本类的每个工厂方法不再内联
 * {@code new Stat(...)} / {@code ModifierStatEffect.entry(...)}，而是返回引用 {@link StatGen} 中
 * 具体条目的 {@link StatRead}。幅度可变的输出（actionSpeed / exposure / speedPerStack / damageTaken /
 * damageDealt / accuracy / evasion / armorReduction / takenCritChance / elementalArchon 的 effMul 缩放项）
 * 引用 StatGen 中的 {@code $value} 模板，实际幅度经 {@code StatRead.vars} 注入；固定输出（unholyMight /
 * adrenaline / armorBreakFully / archon NULLIFY）引用 StatGen 中已烘进数值的常量具体条目。
 */
public final class StatusBehavior {

    private StatusBehavior() {
    }

    /** 引用 StatGen 中的固定输出条目（无幅度注入）。 */
    private static StatEntry sb(Identifier id) {
        return new StatRead(id);
    }

    /** 引用 StatGen 中的 $value 模板条目，并注入实际幅度（经 vars 的 "value" 键）。 */
    private static StatEntry sb(Identifier id, double value) {
        return new StatRead(id,
                Map.of("value", ExpressionCodec.of(value)));
    }

    // speedPerStack 属性 → StatGen 模板条目映射（仅攻击/移速/施法速度三种）
    // 直接映射 StatIds 常量：拼串 id 一旦与 StatGen 注册漂移即静默悬空（StatRead 解析失败 = no-op），
    // 故此处与 exposure / damageDealt 一律走常量引用。
    private static final Map<Holder<Attribute>, Identifier> SPEED_ATTR_ENTRY = Map.of(
            Attributes.ATTACK_SPEED, StatIds.SPEED_PER_STACK_ATTACK_SPEED,
            Attributes.MOVEMENT_SPEED, StatIds.SPEED_PER_STACK_MOVEMENT_SPEED,
            AttributeRegistry.CAST_SPEED, StatIds.SPEED_PER_STACK_CAST_SPEED);

    // 元素曝露 标签 → StatGen 模板条目映射（RESISTANCES + 元素标签作用域，ADD_VALUE）
    private static final Map<String, Identifier> EXPOSURE_ENTRY_BY_TAG = Map.of(
            GateTag.FIRE, StatIds.EXPOSURE_FIRE,
            GateTag.COLD, StatIds.EXPOSURE_COLD,
            GateTag.LIGHTNING, StatIds.EXPOSURE_LIGHTNING);

    // DoT 每秒伤害系数（bleed 0.15 / ignite 0.20 / poison 0.20）与默认来源槽位（bleed 物理 / ignite 火 / poison 混沌）、
    // 是否攻击类（bleed/poison）均已迁入 {@link AilmentSpec}（注册时声明），不再在此保留代码常量。

    /**
     * 行动速度削减：统一作用于 cast / attack / movement / sneak / fly / mining / block / jump。
     * fraction：1.0 = 完全停止（冰冻 / 触电 / 石化）。
     */
    public static List<StatEntry> actionSpeed(double fraction) {
        if (fraction <= 0) return List.of();
        double v = -Math.min(fraction, 1.0);
        return List.of(
                sb(StatIds.ACTION_SPEED_CAST_SPEED, v),
                sb(StatIds.ACTION_SPEED_ATTACK_SPEED, v),
                sb(StatIds.ACTION_SPEED_MOVEMENT_SPEED, v),
                sb(StatIds.ACTION_SPEED_SNEAKING_SPEED, v),
                sb(StatIds.ACTION_SPEED_SUBMERGED_MINING_SPEED, v),
                sb(StatIds.ACTION_SPEED_FLYING_SPEED, v),
                sb(StatIds.ACTION_SPEED_BLOCK_BREAK_SPEED, v),
                sb(StatIds.ACTION_SPEED_JUMP_STRENGTH, v));
    }

    /** 元素抗性曝露：削减目标对应元素抗性（fraction：1.0 = 削 100%）。归一为 RESISTANCES + 元素标签作用域。 */
    public static List<StatEntry> exposure(Holder<Attribute> resist, Set<String> tags, double fraction) {
        if (fraction <= 0) return List.of();
        Either<AttributeModifier.Operation, AttributeLimiters.Operation> op =
                Either.left(AttributeModifier.Operation.ADD_VALUE);
        double v = -Math.min(fraction, 1.0);
        if (tags.size() == 1) {
            Identifier entry = EXPOSURE_ENTRY_BY_TAG.get(tags.iterator().next());
            if (entry != null) return List.of(sb(entry, v));
        }
        // 非预期组合（多标签 / 非元素标签）：退回内联（行为保持）
        return List.of(ModifierStatEffect.entry(resist, new ExpressionCodec.Value(Either.left(v)), op, tags, List.of()));
    }

    /**
     * 按层加速：每层 +perStack 的指定速度属性（ADD_MULTIPLIED_BASE）。
     * 用于疾风（每层 +1% 攻速/移速）、噬魂者（每层 +1% 攻速/施法速度）等可叠层增益。
     *
     * @param stacks   当前层数
     * @param perStack 每层提供的比例（0.01 = 1%）
     * @param attrs    受影响的速度属性（如 ATTACK_SPEED / MOVEMENT_SPEED / CAST_SPEED）
     */
    public static List<StatEntry> speedPerStack(int stacks, double perStack, Holder<Attribute>... attrs) {
        if (stacks <= 0) return List.of();
        Either<AttributeModifier.Operation, AttributeLimiters.Operation> op =
                Either.left(AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        double v = stacks * perStack;
        List<StatEntry> out = new ArrayList<>();
        for (Holder<Attribute> a : attrs) {
            Identifier entry = SPEED_ATTR_ENTRY.get(a);
            if (entry != null) {
                out.add(sb(entry, v));
            } else {
                // 非预期属性：退回内联（行为保持）
                out.add(ModifierStatEffect.entry(a, new ExpressionCodec.Value(Either.left(v)), op, Set.of(), List.of()));
            }
        }
        return out;
    }

    /**
     * 受伤倍率变动（ADD_MULTIPLIED_BASE）：fraction &gt; 0 受伤更多（如感电），fraction &lt; 0 受伤更少（如护体）。
     * 落点统一为 {@code TAKEN_DAMAGE} 单属性（默认 1.0），由伤害管线在受击方作用域读取。
     * 无标签作用域 = 对所有伤害类型生效（感电/护体）。
     */
    public static List<StatEntry> damageTaken(double fraction) {
        return damageTaken(fraction, Set.of());
    }

    /**
     * 受伤倍率变动（ADD_MULTIPLIED_BASE），限定于指定伤害类型作用域。
     * 仅当伤害管线在受击方推入的命中作用域含 {@code tags} 中任一标签时该条目才被聚合——
     * 例：{@code damageTaken(0.05 * stacks, Set.of(GateTag.CHAOS))} 即「承受混沌伤害 +5%/层」（凋零），
     * 只对混沌成分 &gt; 0 的命中 / 中毒等混沌 DoT 生效，不影响火/物理等其它成分。
     *
     * @param fraction 伤更多为正、伤更少为负
     * @param tags     伤害类型作用域标签（空集合 = 全局生效）；支持父类别标签（如 {@code elemental}）
     */
    public static List<StatEntry> damageTaken(double fraction, Set<String> tags) {
        if (fraction == 0) return List.of();
        Either<AttributeModifier.Operation, AttributeLimiters.Operation> op =
                Either.left(AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        if (tags.isEmpty()) return List.of(sb(StatIds.DAMAGE_TAKEN, fraction));
        if (tags.size() == 1) {
            String t = tags.iterator().next();
            if (t.equals(GateTag.CHAOS)) return List.of(sb(StatIds.DAMAGE_TAKEN_CHAOS, fraction));
            if (t.equals(GateTag.PHYSICAL)) return List.of(sb(StatIds.DAMAGE_TAKEN_PHYSICAL, fraction));
        }
        // 非预期组合：退回内联（行为保持）
        return List.of(ModifierStatEffect.entry(AttributeRegistry.TAKEN_DAMAGE,
                new ExpressionCodec.Value(Either.left(fraction)), op, tags, List.of()));
    }

    /**
     * 造成 / 输出伤害倍率变动（ADD_MULTIPLIED_BASE）：fraction &gt; 0 造成更多伤害（如不洁之力），
     * fraction &lt; 0 造成更少（如恐吓）。落点统一为 {@code DAMAGE} 单属性（默认 1.0），
     * 由伤害管线在攻击方作用域按元素标签分别读取。无标签作用域 = 对所有元素生效。
     */
    public static List<StatEntry> damageDealt(double fraction) {
        return damageDealt(fraction, Set.of());
    }

    /**
     * 造成 / 输出伤害倍率变动（ADD_MULTIPLIED_BASE），限定于指定伤害类型作用域。
     * 例：{@code damageDealt(0.3, Set.of(GateTag.CHAOS))} 即「造成混沌伤害 +30%」（不洁之力）。
     */
    public static List<StatEntry> damageDealt(double fraction, Set<String> tags) {
        if (fraction == 0) return List.of();
        Identifier key = damageDealtKey(tags);
        if (key != null) return List.of(sb(key, fraction));
        // 非预期组合：退回内联（行为保持）
        Either<AttributeModifier.Operation, AttributeLimiters.Operation> op =
                Either.left(AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        return List.of(ModifierStatEffect.entry(AttributeRegistry.DAMAGE,
                new ExpressionCodec.Value(Either.left(fraction)), op, tags, List.of()));
    }

    /** 已知标签组合 → StatGen 模板条目（无标签 / {elemental,spell} / 形态×元素组合）；未知返回 null。 */
    private static Identifier damageDealtKey(Set<String> tags) {
        if (tags.isEmpty()) return StatIds.DAMAGE_DEALT;
        if (tags.equals(Set.of(GateTag.ELEMENTAL, GateTag.SPELL))) return StatIds.DAMAGE_DEALT_ELEMENTAL_SPELL;
        if (tags.contains(GateTag.FORM)) {
            String form = tags.contains(GateTag.BEAR) ? "bear"
                    : tags.contains(GateTag.WOLF) ? "wolf"
                    : tags.contains(GateTag.WYVERN) ? "wyvern" : null;
            if (form != null) {
                for (String el : List.of(GateTag.PHYSICAL, GateTag.FIRE, GateTag.COLD, GateTag.LIGHTNING, GateTag.CHAOS)) {
                    if (tags.contains(el)) return StatIds.byName("damage_dealt_" + form + "_" + el);
                }
            }
        }
        return null;
    }

    /** 命中几率：降低持有状态实体的命中（致盲）。fraction = -magnitude%（负）。落点 ACCURACY。 */
    public static List<StatEntry> accuracy(double fraction) {
        if (fraction == 0) return List.of();
        return List.of(sb(StatIds.ACCURACY, fraction));
    }

    /** 闪避：降低持有状态实体的闪避（致盲）。fraction = -magnitude%（负）。落点 EVASION（MC 原版属性）。 */
    public static List<StatEntry> evasion(double fraction) {
        if (fraction == 0) return List.of();
        return List.of(sb(StatIds.EVASION, fraction));
    }

    /**
     * 致盲（poe2db 实证）：同时削减命中与闪避（POE2 各 -20%）。fraction = -magnitude%（负）。
     * 落点 ACCURACY + EVASION（二者均为被致盲实体自身的命中/闪避能力）。
     */
    public static List<StatEntry> blind(double fraction) {
        if (fraction == 0) return List.of();
        List<StatEntry> out = new ArrayList<>();
        out.addAll(accuracy(fraction));
        out.addAll(evasion(fraction));
        return out;
    }

    /**
     * 恐吓（poe2db 实证）：同时使目标「承受伤害 +fraction」与「造成伤害 -fraction」（POE2 各 10%）。
     * 二者均作用于被施加此减益的同一实体：damageTaken 提升其承伤，damageDealt 削减其输出。
     */
    public static List<StatEntry> intimidate(double fraction) {
        if (fraction == 0) return List.of();
        List<StatEntry> out = new ArrayList<>();
        out.addAll(damageDealt(-fraction));
        out.addAll(damageTaken(fraction));
        return out;
    }

    /** 护甲削减（破甲）：降低持有状态实体的护甲。fraction = -magnitude%（负）。落点 Attributes.ARMOR。 */
    public static List<StatEntry> armorReduction(double fraction) {
        if (fraction == 0) return List.of();
        return List.of(sb(StatIds.ARMOR_REDUCTION, fraction));
    }

    /** 护甲清零（完全破甲）：将持有状态实体的护甲乘算至 0（ADD_MULTIPLIED_TOTAL = -1.0）。 */
    public static List<StatEntry> armorBreakFully() {
        return List.of(sb(StatIds.ARMOR_BREAK_FULLY));
    }

    // 不提供 allAttributes(STR/DEX/INT)：本模组 STR/DEX/INT 仅作装备门槛（无战斗消费端）；
    // ADRENALINE 由 adrenaline() 直接写入真实战斗属性（DAMAGE/攻速/施法/移速/物理承伤）。

    /**
     * 不洁之力（poe2db/poe2wiki 实证，严格转换语义）：你造成的所有伤害有 30% 作为「额外」混沌伤害。
     * 基础伤害本身不转换（保留），仅在其上额外加 30% 等量混沌——对应 {@link DamageConvertStatEffect} 的
     * {@code GAIN} 模式（保留 from、额外按 ratio 给 to 加伤，与"转换/替代"的 REPLACE 不同）。
     * <b>仅命中(hits)生效、不作用于 DoT</b>（POE2 严格语义）：经 {@code hitsOnly=true} 门控，
     * 由 DamagePipeline 维度A(DoT) 跳过、维度C(命中) 应用。对五种基础伤害类型各做一份 GAIN 转换
     * （physical/fire/cold/lightning/chaos → chaos，ratio=0.30），作用域 ALWAYS（攻击与法术命中均生效）。
     */
    public static List<StatEntry> unholyMight() {
        return List.of(
                sb(StatIds.UNHOLY_MIGHT_PHYSICAL),
                sb(StatIds.UNHOLY_MIGHT_FIRE),
                sb(StatIds.UNHOLY_MIGHT_COLD),
                sb(StatIds.UNHOLY_MIGHT_LIGHTNING),
                sb(StatIds.UNHOLY_MIGHT_CHAOS));
    }

    /**
     * 元素执政官（Elemental Archon，poe2db/poe2wiki 实证，严格五效）：
     * ① 法术造成元素伤害 +25% more（DAMAGE，标签 {elemental, spell}）；
     * ② 无法以法术造成非元素伤害：法术命中的物理/混沌伤**归零**（NULLIFY 模式，hitsOnly=true, tags={spell}），
     *    非转换为元素（POE2 实证无"转换"语义，仅禁止造成非元素法术伤，见 DamageConvertStatEffect.Mode.NULLIFY）；
     * ③ 法术命中 +100% 冰冻积蓄（AILMENT_BUILDUP，{ailment, freeze, spell}）；
     * ④ 法术 +100% 点燃几率（AILMENT_BUILDUP，{ailment, ignite, spell}）；
     * ⑤ 法术命中 +100% 感电几率（AILMENT_MAGNITUDE，{ailment, shock, spell}）。
     * ②③⑤ 的「法术命中」门控经 hitsOnly=true + spell 作用域（DamagePipeline 维度C 读命中作用域；
     * 异常积蓄/强度读取经 {@code StatScopes.enterMerged} 并入当前栈保留 spell 标签，取代原 isSpellSource 谓词）精确生效；
     * ③④⑤ 的 spell 感知经 AilmentHelper 的 isSpell 重载（命中阶段攻击者作用域含 spell）落地，见 STAT_SYSTEM.md。
     */
    public static List<StatEntry> elementalArchon() {
        return elementalArchon(null);
    }

    /**
     * 元素执政官五效（带持有者效果倍率）：{@code effMul = archonBuffEffectMultiplier(living)}
     * （语料「X% increased effect of Archon Buffs on you」，{archon} 作用域读 AILMENT_MAGNITUDE，默认 1.0），
     * 作用于 ①（+25% more 元素法术伤）与 ③④⑤（+100% 冰冻积蓄/点燃几率/感电几率）的固定系数。
     * {@code living} 为 null（纯静态查询路径）时倍率取 1.0。
     */
    public static List<StatEntry> elementalArchon(LivingEntity living) {
        List<StatEntry> out = new ArrayList<>();
        double effMul = living != null ? StatusEngine.archonBuffEffectMultiplier(living) : 1.0;
        // ① 25% more Elemental Damage with Spells
        out.add(sb(StatIds.DAMAGE_DEALT_ELEMENTAL_SPELL, 0.25 * effMul));
        // ② Cannot deal Non-Elemental Damage with Spells：法术命中把物理/混沌伤**归零**（NULLIFY，非转换）。
        //    PoE2 实证该效果无"转换"语义——非元素法术伤直接不造成，而非转成元素；故用 NULLIFY 模式（hitsOnly=true, tags={spell}）。
        out.add(sb(StatIds.ARCHON_NULLIFY_PHYSICAL));
        out.add(sb(StatIds.ARCHON_NULLIFY_CHAOS));
        // ③ Hits with Spells cause 100% more Freeze Buildup
        out.add(sb(StatIds.ARCHON_FREEZE_BUILDUP, 1.0 * effMul));
        // ④ Spells have 100% more Flammability Magnitude / Ignite chance
        out.add(sb(StatIds.ARCHON_IGNITE_BUILDUP, 1.0 * effMul));
        // ⑤ Hits with Spells have 100% more Shock chance
        out.add(sb(StatIds.ARCHON_SHOCK_MAGNITUDE, 1.0 * effMul));
        return out;
    }

    /**
     * 创口（Incision）对<b>击中者</b>流血几率的贡献，走 Stat 条目而非 Helper 硬编码。
     *
     * <p>本方法由 {@code StatusDefs.INCISION} 的 statProvider 调用，产出的条目经
     * {@link StatusStatBridge} 注入<b>受害者</b>（创口携带者）的 STAT_STORE；条目接线到
     * {@code TGT_INLINE_TRIGGER_TARGET}（target=EVENT_TARGET）+ {@code AILMENT_CHANCE}{bleed} ADD_VALUE，
     * 故只有「正在击中该受害者的那个实体」在读流血几率时才会聚合到它
     * （{@code StatEngine.compute} 跨实体分支：作用域 sourceEntity=受害者 → 取 target 解析结果含 reader 的条目）。
     *
     * <p>贡献 = {@code stacks × magnitude × INCISION_BLEED_CHANCE_PER_STACK}（每层 10%）；
     * 「创口效果提高 X%」由施加侧缩放 {@code inst.magnitude} 表达（见 GrantOnEventStatEffect），
     * 不在此处重复读取属性，避免与施加时点语义不一致。
     */
    public static List<StatEntry> incisionBleedChance(StatusInstance inst) {
        if (inst == null || inst.stacks <= 0) return List.of();
        return List.of(sb(StatIds.INCISION_BLEED_CHANCE_ON_HITTER,
                inst.stacks * inst.magnitude * INCISION_BLEED_CHANCE_PER_STACK));
    }

    /** 被暴击几率（暴击虚弱）：提升持有状态实体的被暴击几率。fraction = +magnitude%。落点 TAKEN_CRIT_CHANCE。 */
    public static List<StatEntry> takenCritChance(double fraction) {
        if (fraction == 0) return List.of();
        return List.of(sb(StatIds.TAKEN_CRIT, fraction));
    }

    /**
     * 肾上腺素（poe2db 实证，固定数值增益、非属性增益、非 magnitude 缩放）：
     * 造成伤害 +100%、攻击速度 +25%、施法速度 +25%、移动速度 +25%、额外物理伤害减免 +10%。
     * 注意：POE2 肾上腺素的 5 项数值彼此独立且固定，与「+属性」无关——本模组 STR/DEX/INT 仅作装备门槛，
     * 无战斗消费端——故此处直接写入真实战斗属性（DAMAGE / ATTACK_SPEED / CAST_SPEED / MOVEMENT_SPEED / 物理承伤），
     * 而非属性本身。额外物理减伤以「承受物理伤害 -10%」表达。
     */
    public static List<StatEntry> adrenaline() {
        return List.of(
                sb(StatIds.ADRENALINE_DAMAGE),
                sb(StatIds.ADRENALINE_ATTACK_SPEED),
                sb(StatIds.ADRENALINE_CAST_SPEED),
                sb(StatIds.ADRENALINE_MOVEMENT_SPEED),
                sb(StatIds.ADRENALINE_TAKEN_PHYSICAL));
    }

    /**
     * 经 Status 施加的 DoT：每 tick 造成每秒伤害（按伤害类型；来源取实例 source UUID）。
     * <p>
     * 持续伤害 / 异常状态伤害的多重分类（点燃=火+元素+异常、流血=物理+攻击+异常、中毒=混沌+攻击+异常）
     * 交由此处的「Stat 系统 + 标签聚合」统一处理，而非在代码里手写层级乘积：
     * <ol>
     *   <li>在<b>施法者</b>上进入一个携带本次 DoT 事件全部分类标签的作用域
     *       （{@code damage_over_time} + {@code ailment} + 具体异常名 + 元素 + 攻击）；</li>
     *   <li>在该作用域内读取 {@code damage_over_time_multiplier} / 对应元素伤害属性
     *       （攻击维度由作用域 {@code ATTACK} 标签承载，攻击 DoT 加成挂 tags={damage_over_time, attack} 条目）；</li>
     *   <li>Stat 系统按「标签子集包含」自动聚合匹配的 stat entry —— 数据侧只需用标签组合表达，例如
     *       {@code [damage_over_time]}（持续伤加成）、{@code [damage_over_time, ailment]}（伤害性异常持续伤加成）、
     *       {@code [damage_over_time, elemental]}（元素持续伤加成）、{@code [damage_over_time, fire]}（火焰持续伤加成）、
     *       {@code [damage_over_time, ignite]}（点燃持续伤加成）、{@code [damage_over_time, attack]}（攻击持续伤加成）……
     *       因而 poe2db 中「伤害性异常既属持续伤害、又属异常、又属元素」的多重分类关系，由标签组合自然表达，
     *       无需在代码里写死层级。</li>
     * </ol>
     * 点燃额外吃 {@code fire_damage}（命中「火伤」与「火焰持续伤」两类 entry）；流血吃 {@code physical_damage}；中毒吃 {@code chaos_damage}。
     * 其它状态效果（冰缓/冰冻/感电等控制类）也应沿用此「进作用域 + 标签 + 读属性」的同一流程处理其 magnitude 加成。
     */
    /**
     * 拆分 DoT 结算（用户机制：以来源伤害类型分别输出，各自被对应抗性/护甲减免）。
     * 对 StatusInstance 的每份额外来源伤害（dotComposition）分别造成<b>来源伤害类型</b>（dot_&lt;src&gt;）持续伤害，
     * 各走一次 IS_AILMENT 伤害源（携带异常名）→ 在 {@code LivingDamageEventPre} 的预减免阶段，
     * 按施法者 {@code ailment_output_convert} 经 Stat 将 damages[] 槽位重路由到目标类型后，再由对应抗性/护甲减免，
     * 并享受感电增幅/凋零与生命/能量结算。
     * <p>定义性数值（每秒系数 / 默认来源槽位 / 是否攻击类 / 异常名）统一取自 {@code inst.type.ailment}
     * （{@link AilmentSpec} 数据，注册时声明），不再按异常名 switch 硬编码。
     */
    public static void dealSplitDoT(LivingEntity target, StatusInstance inst) {
        if (target.level().isClientSide() || target.isDeadOrDying()) return;
        AilmentSpec spec = inst.type.ailment;
        String ailmentTag = inst.type.id;
        boolean isAttackAilment = spec.attackAilment();
        double perSecondCoeff = spec.perSecondCoeff();
        // 兼容旧档：无拆分数组时按异常声明默认来源槽位整段输出
        double[] comp = inst.dotComposition;
        if (comp == null) {
            comp = new double[DamageTypes.SLOT_CAPACITY];
            int slot = spec.defaultSourceSlot();
            comp[slot >= 0 && slot < DamageTypes.width() ? slot : 0] = inst.magnitude;
        }
        LivingEntity caster = resolveCaster(target.level(), inst.source);
        // 「你造成的点燃被反射回你自身」：累计本次结算施加给目标的点燃每 tick 总量，循环结束后按标记比例反射给施加者。
        double tickTotal = 0;
        for (int t = 0; t < DamageTypes.width(); t++) {
            double c = comp[t];
            if (c <= 0) continue;
            // 输出伤害类型（o）即来源伤害类型（t）：ailment_output_convert 的修正不在 tick 处冻结，
            // 而是在伤害事件（LivingDamageEventPre）的预减免阶段、由 DamagePipeline#stageOutputConvert 维度A
            // 经施法者 Stat 动态重路由 damages[] 槽位（见该类 维度A 注释），故此处恒 o = t。
            // #18 IGNITE_INFLICTED_WITH_FIRE_SPELLS_DEALS_CHAOS_DAMAGE_INSTEAD_OF_FIRE_DAMAGE（GAP-FIX#18）
            // 已由该通用通道承载（WireAilment 接 EFF_AILMENT_OUTPUT_CONVERT_IGNITE_FIRE_TO_CHAOS）。
            int o = t;
            double perSecond = c * perSecondCoeff * inst.stacks * (1.0 + inst.aggravatedFraction);
            double mult = 1.0;
            if (caster != null) {
                // 状态效果基于来源伤害效果：caster 侧加成作用域取「来源伤害类型」标签（如 fire→FIRE_DAMAGE），
                // 而输出/减免由 outType（o）决定（见下方 DamageSource）。
                Set<String> tags = new HashSet<>();
                tags.add(GateTag.DAMAGE_OVER_TIME);
                tags.add(GateTag.AILMENT);
                tags.add(ailmentTag);
                String srcTag = DamageTypeRegistry.DAMAGE_TYPE_TAGS[t];
                tags.add(srcTag);
                if (isAttackAilment) tags.add(GateTag.ATTACK);
                if (srcTag.equals(GateTag.FIRE) || srcTag.equals(GateTag.COLD) || srcTag.equals(GateTag.LIGHTNING))
                    tags.add(GateTag.ELEMENTAL);
                AutoCloseable scope = StatScopes.enterMerged(caster, StatContext.of(tags, null));
                try {
                    // 归一：持续伤倍率单属性（作用域已带 damage_over_time + ailment + ailmentTag + 来源元素标签，
                    // 中毒/流血伤害倍率由数据侧挂 tags={damage_over_time, ailment, poison/bleed, damage} 的 Stat 条目表达，
                    // 无需 POISON_DAMAGE/BLEEDING_DAMAGE 独立属性；攻击维度由 ATTACK 标签承载）
                    mult = caster.getAttributeValue(AttributeRegistry.DAMAGE_OVER_TIME_MULTIPLIER);
                    mult *= elementDamageMult(caster, srcTag);
                    // 统一异常强度加成（攻方 [ailment, 异常名, magnitude] 作用域，默认 1.0）
                    mult *= caster.getAttributeValue(AttributeRegistry.AILMENT_MAGNITUDE);
                } finally {
                    try {
                        scope.close();
                    } catch (Exception ignored) {
                    }
                }
            }
            float perTick = (float) (perSecond * mult / 20.0);
            if (perTick <= 0) continue;
            tickTotal += perTick;
            Holder<DamageType> typeHolder = target.level().registryAccess()
                    .lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypeRegistry.dotDamageType(o));
            // 携带异常名，供伤害事件预减免阶段（applyAilmentOutputConversion）经施法者 Stat 重路由输出类型
            DamageSource source = new SkillDamageSource(typeHolder, null, caster,
                    caster != null ? caster.position() : target.position(), ailmentTag);
            target.hurt(source, perTick);
        }
        // 「你造成的点燃被反射回你自身」：点燃 DoT 的每 tick 总量按标记比例反射给施加者（本人承伤）。
        // 反射用非异常伤害类型（FIRE 不在 AILMENT_TYPE 内）→ 不触发再施加异常，避免循环。
        if (caster != null && caster != target && inst.type == StatusDefs.IGNITE && tickTotal > 0.0) {
            double reflectRatio = StatEngine.magnitudeOf(caster, "ignite_reflect", Set.of());
            if (reflectRatio > 0.0) {
                Holder<DamageType> reflectType = target.level().registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypeRegistry.FIRE);
                DamageSource reflectSource = new SkillDamageSource(reflectType, null, caster, caster.position(), null);
                caster.hurt(reflectSource, (float) (tickTotal * reflectRatio));
            }
        }
        // 「被你点燃过的敌人」累计点燃秒数（node 43939）：点燃 DoT 每 tick 计 0.05 秒，按施加者 UUID 累计到受害方。
        // 消费端 DamagePipeline#applyStatusDamageTakenModifiers 读「当前攻击者」的累计秒数放大火焰成分（仅 IGNITE 状态的 tick 累计）。
        if (caster != null && caster != target && inst.type == StatusDefs.IGNITE) {
            java.util.Map<UUID, Float> hist = new java.util.HashMap<>(target.getData(AttachmentRegistry.IGNITE_HISTORY));
            hist.merge(caster.getUUID(), 0.05f, Float::sum);
            target.setData(AttachmentRegistry.IGNITE_HISTORY, java.util.Map.copyOf(hist));
        }
    }
    /** DoT 元素伤害倍率：统一 DAMAGE 单属性 + 元素标签作用域读取（替代旧 5 个 *_DAMAGE 独立属性）。 */
    private static double elementDamageMult(LivingEntity caster, String tag) {
        if (caster == null) return 1.0;
        Set<String> tags = Set.of(tag, GateTag.DAMAGE);
        return com.cimdy.coe.data.helper.combat.DamageHelper.readInScopePublic(caster, tags, AttributeRegistry.DAMAGE);
    }

    private static LivingEntity resolveCaster(Level level, java.util.UUID uuid) {
        if (uuid == null || !(level instanceof ServerLevel sl)) return null;
        Entity e = sl.getEntity(uuid);
        return e instanceof LivingEntity le ? le : null;
    }

    // ───────────────────────────── 行为注册表（有限命名，数据定义按 id 引用） ─────────────────────────────

    /** 诅咒存续比例跨过此阈值后重注入 stat 贡献（原 StatusType.CURSE_DURATION_EXPIRED_FRACTION）。 */
    private static final double CURSE_DURATION_EXPIRED_FRACTION = 0.5;

    /** 创口每层提供的流血几率系数（每层 10%）：叠加到击中者流血几率。 */
    private static final double INCISION_BLEED_CHANCE_PER_STACK = 0.10;

    /** 每 tick 诅咒逻辑：存续比例跨过阈值时重注入一次 stat 贡献（原 CURSE_TICK）。 */
    public static void curseTick(StatusInstance inst, LivingEntity target) {
        if (inst.buildup >= 1.0) return;
        long age = target.level().getGameTime() - inst.appliedTick;
        double total = age + inst.durationTicks;
        if (total > 0 && age >= CURSE_DURATION_EXPIRED_FRACTION * total) {
            StatusStatBridge.remove(target, inst);
            StatusStatBridge.add(target, inst);
            inst.buildup = 1.0;
        }
    }

    /**
     * 诅咒 stat 贡献通用骨架：按施法者 AILMENT_MAGNITUDE 缩放，对给定元素施加抗性削减，
     * 并叠加施法者侧诅咒增强（禁用 ES 回复 / 生命再生倍率）。
     * 各诅咒按资料库「仅削减对应元素抗性」忠实实现，不再对所有诅咒硬编码全四元素抗削减（避免近似接线）。
     */
    private static List<StatEntry> curseResist(LivingEntity living, StatusInstance inst,
                                               double base, String... elements) {
        if (inst.source == null) return List.of();
        Entity e = living.level().getEntity(inst.source);
        if (!(e instanceof LivingEntity caster)) return List.of();
        double mag;
        double esDisable;
        double regenMult;
        try (AutoCloseable s = StatScopes.enter(caster,
                StatContext.of(Set.of(GateTag.CURSE, GateTag.not(GateTag.TAKEN)), null)
                        .withSourceEntity(living))) {
            mag = caster.getAttributeValue(AttributeRegistry.AILMENT_MAGNITUDE);
            esDisable = StatEngine.gateOf(caster, "curse_target_es_recharge_disabled", Set.of("curse")) ? 1.0 : 0.0;
            regenMult = caster.getAttributeValue(AttributeRegistry.CURSE_TARGET_HEALTH_REGEN_MULT);
        } catch (Exception ignored) {
            return List.of();
        }
        double reduction = base * mag;
        List<StatEntry> entries = new ArrayList<>();
        for (String element : elements) {
            entries.add(ModifierStatEffect.entry(AttributeRegistry.RESISTANCES, -reduction,
                    AttributeModifier.Operation.ADD_VALUE, Set.of(element)));
        }
        if (esDisable >= 1.0) {
            entries.add(MarkerStatEffect.entry("energy_shield_recharge_disabled", Set.of()));
        }
        if (regenMult > 1.0) {
            entries.add(ModifierStatEffect.entry(AttributeRegistry.REGENERATION_RATE, regenMult - 1.0,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE, Set.of(GateTag.HEALTH, GateTag.REGEN)));
        }
        return entries;
    }

    // ── 每诅咒独立 stat 提供器（资料库真值；base = L1 削减比例，按 AILMENT_MAGNITUDE 缩放）──
    /** Flammability：仅火焰抗性 -23%（L1，L20 -29%）。 */
    public static List<StatEntry> curseFireResist(LivingEntity l, StatusInstance i) {
        return curseResist(l, i, 0.23, GateTag.FIRE);
    }
    /** Conductivity：仅闪电抗性 -23%（L1，L20 -29%）。 */
    public static List<StatEntry> curseLightningResist(LivingEntity l, StatusInstance i) {
        return curseResist(l, i, 0.23, GateTag.LIGHTNING);
    }
    /** Hypothermia：仅冰霜抗性 -23%（L1，L20 -29%）。 */
    public static List<StatEntry> curseColdResist(LivingEntity l, StatusInstance i) {
        return curseResist(l, i, 0.23, GateTag.COLD);
    }
    /** Elemental Weakness：火/冰/电三元素抗性 -40%（L1，L20 -59%），不含混沌。
     * 免疫不在此处判定：「Unaffected by Elemental Weakness」经 ImmuneStatEffect 在
     * {@link StatusEngine#apply} 入口中央短路（归一铁律：离散机制修饰转 StatEffect，不建属性）。 */
    public static List<StatEntry> curseElementalResist(LivingEntity l, StatusInstance i) {
        return curseResist(l, i, 0.40, GateTag.FIRE, GateTag.COLD, GateTag.LIGHTNING);
    }
    /** Despair：仅混沌抗性 -35%（L1，L20 -49%）。 */
    public static List<StatEntry> curseChaosResist(LivingEntity l, StatusInstance i) {
        return curseResist(l, i, 0.35, GateTag.CHAOS);
    }
    /** Enfeeble：目标造成 20%（L1，L20 29%）更少伤害（削减其 DAMAGE 输出）。 */
    public static List<StatEntry> curseEnfeeble(LivingEntity l, StatusInstance i) {
        if (i.source == null) return List.of();
        Entity e = l.level().getEntity(i.source);
        if (!(e instanceof LivingEntity caster)) return List.of();
        double mag;
        try (AutoCloseable s = StatScopes.enter(caster,
                StatContext.of(Set.of(GateTag.CURSE, GateTag.not(GateTag.TAKEN)), null)
                        .withSourceEntity(l))) {
            mag = caster.getAttributeValue(AttributeRegistry.AILMENT_MAGNITUDE);
        } catch (Exception ignored) {
            return List.of();
        }
        return List.of(ModifierStatEffect.entry(AttributeRegistry.DAMAGE, -0.20 * mag,
                AttributeModifier.Operation.ADD_VALUE, Set.of()));
    }
    /** Vulnerability：命中无视部分护甲（受害方承受破甲倍率 +20% L1，L20 更高）。 */
    public static List<StatEntry> curseVulnerability(LivingEntity l, StatusInstance i) {
        if (i.source == null) return List.of();
        Entity e = l.level().getEntity(i.source);
        if (!(e instanceof LivingEntity caster)) return List.of();
        double mag;
        try (AutoCloseable s = StatScopes.enter(caster,
                StatContext.of(Set.of(GateTag.CURSE, GateTag.not(GateTag.TAKEN)), null)
                        .withSourceEntity(l))) {
            mag = caster.getAttributeValue(AttributeRegistry.AILMENT_MAGNITUDE);
        } catch (Exception ignored) {
            return List.of();
        }
        return List.of(ModifierStatEffect.entry(AttributeRegistry.ARMOR_BREAK, 0.20 * mag,
                AttributeModifier.Operation.ADD_VALUE, Set.of(GateTag.TAKEN)));
    }

    /**
     * Temporal Chains：诅咒敌人行动速度（施法 / 攻击 / 移动 / 潜行 / 飞行 / 采矿 / 破障 / 跳跃）降低，
     * 按施法者 {@link AttributeRegistry#AILMENT_MAGNITUDE} 缩放（base = L1 减速比例，0.30 = 30% less Action Speed，对齐 poe2wiki）。
     * 与 {@link #curseResist} 同源取施法者侧效果幅度，不读 inst.magnitude——
     * 诅咒状态经 StatusEngine.applyCurseToNearby 施加时 magnitude 恒为 1.0（见 StatusEngine:124），故必须走施法者 AILMENT_MAGNITUDE 而非 actionSpeedMag。
     * 复用已接线且被 MAIM/HINDER 真消费的 actionSpeed 助手，零新增属性、零死线。
     */
    public static List<StatEntry> curseSlow(LivingEntity l, StatusInstance i) {
        if (i.source == null) return List.of();
        Entity e = l.level().getEntity(i.source);
        if (!(e instanceof LivingEntity caster)) return List.of();
        double mag;
        try (AutoCloseable s = StatScopes.enter(caster,
                StatContext.of(Set.of(GateTag.CURSE, GateTag.not(GateTag.TAKEN)), null)
                        .withSourceEntity(l))) {
            mag = caster.getAttributeValue(AttributeRegistry.AILMENT_MAGNITUDE);
        } catch (Exception ignored) {
            return List.of();
        }
        return actionSpeed(0.30 * mag);
    }

    /**
     * 诅咒技能 id → stat 提供器 id 映射（数据驱动，按技能 id 取独立效果）。
     * 未登记者回退 "none"（仅标记，无虚构效果）。
     * 注：repulsion 是被动天赋节点（passives_areaofeffect，stats 当前为空桩），非 CURSE 类技能，不在本表范围。
     */
    private static final Map<String, String> CURSE_PROVIDER_BY_ID = new HashMap<>();
    static {
        CURSE_PROVIDER_BY_ID.put("blue/flammability", "curseFireResist");
        CURSE_PROVIDER_BY_ID.put("blue/conductivity", "curseLightningResist");
        CURSE_PROVIDER_BY_ID.put("blue/hypothermia", "curseColdResist");
        CURSE_PROVIDER_BY_ID.put("blue/elemental_weakness", "curseElementalResist");
        CURSE_PROVIDER_BY_ID.put("blue/despair", "curseChaosResist");
        CURSE_PROVIDER_BY_ID.put("blue/enfeeble", "curseEnfeeble");
        CURSE_PROVIDER_BY_ID.put("blue/vulnerability", "curseVulnerability");
        CURSE_PROVIDER_BY_ID.put("blue/temporal_chains", "curseSlow");
    }

    /**
     * 取动态诅咒状态的 stat 提供器 id（按技能 id）；缺失回退 "none"（空贡献）。
     *
     * <p>命名空间容错：动态诅咒状态的 id 是 {@code skillId.toString()}（<b>带</b>命名空间，
     * 如 {@code coe:blue/elemental_weakness}），而 {@link #CURSE_PROVIDER_BY_ID} 按技能 <b>path</b>
     * 键控（{@code blue/elemental_weakness}）——二者口径不同，故先原样查，未命中再剥掉
     * {@code namespace:} 前缀按 path 查一次。（此前缺此回退 = 全表 miss = 8 个诅咒的 stat 效果全线失效。）
     */
    public static String curseStatProviderId(String skillId) {
        if (skillId == null) return "none";
        String hit = CURSE_PROVIDER_BY_ID.get(skillId);
        if (hit != null) return hit;
        int colon = skillId.indexOf(':');
        if (colon >= 0 && colon + 1 < skillId.length()) {
            String path = skillId.substring(colon + 1);
            hit = CURSE_PROVIDER_BY_ID.get(path);
            if (hit != null) return hit;
        }
        return "none";
    }

    // ── 精魂光环（Aura / Herald）stat 提供器 ─────────────────────────────
    // 真值对齐各宝石注释中 poe2wiki 幅度；经 ModifierStatEffect.entry 直落真实属性+作用域，
    // 由伤害/属性管线真读（非死线）。op 语义：
    //   · 伤害类用 ADD_MULTIPLIED_TOTAL（more，与旧 attrBuff op=1 一致）；
    //   · 抗性类用 ADD_VALUE（RESISTANCES base=0，零基禁 inc，须平加，见 §三定律 #3）；
    //   · 魔力回复 / 闪避用 ADD_MULTIPLIED_TOTAL（base 非 0，more 安全）。
    // 此处改走已注册的真实属性，使 13 个光环/捷从「每宝石死线预设」收敛为单一 StatusBehavior 按 id 分发。
    private static final Map<String, String> AURA_PROVIDER_BY_ID = new HashMap<>();
    static {
        AURA_PROVIDER_BY_ID.put("blue/herald_of_ice", "auraColdDamage");
        AURA_PROVIDER_BY_ID.put("blue/purity_of_fire", "auraFireResist");
        AURA_PROVIDER_BY_ID.put("blue/purity_of_ice", "auraColdResist");
        AURA_PROVIDER_BY_ID.put("blue/purity_of_lightning", "auraLightningResist");
        AURA_PROVIDER_BY_ID.put("blue/impurity", "auraChaosResist");
        AURA_PROVIDER_BY_ID.put("blue/clarity", "auraManaRegen");
        AURA_PROVIDER_BY_ID.put("blue/wrath", "auraLightningDamage");
        AURA_PROVIDER_BY_ID.put("red/herald_of_ash", "auraFireDamage");
        AURA_PROVIDER_BY_ID.put("red/herald_of_blood", "auraPhysicalDamage");
        AURA_PROVIDER_BY_ID.put("green/wind_dancer", "auraEvasion10");
        AURA_PROVIDER_BY_ID.put("green/herald_of_thunder", "auraLightningDamage");
        AURA_PROVIDER_BY_ID.put("green/herald_of_plague", "auraChaosDamage");
        AURA_PROVIDER_BY_ID.put("green/grace", "auraEvasion25");
    }

    /** 取动态光环状态的 stat 提供器 id（按技能 id）；缺失回退 "none"（空贡献）。 */
    public static String auraStatProviderId(String skillId) {
        return AURA_PROVIDER_BY_ID.getOrDefault(skillId, "none");
    }

    // 伤害类：more（ADD_MULTIPLIED_TOTAL）作用于 DAMAGE + 元素作用域
    public static List<StatEntry> auraFireDamage(LivingEntity l, StatusInstance i) {
        return List.of(ModifierStatEffect.entry(AttributeRegistry.DAMAGE, 0.25 * i.scaledMagnitude(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, Set.of(GateTag.FIRE)));
    }
    public static List<StatEntry> auraColdDamage(LivingEntity l, StatusInstance i) {
        return List.of(ModifierStatEffect.entry(AttributeRegistry.DAMAGE, 0.25 * i.scaledMagnitude(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, Set.of(GateTag.COLD)));
    }
    public static List<StatEntry> auraLightningDamage(LivingEntity l, StatusInstance i) {
        return List.of(ModifierStatEffect.entry(AttributeRegistry.DAMAGE, 0.25 * i.scaledMagnitude(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, Set.of(GateTag.LIGHTNING)));
    }
    public static List<StatEntry> auraPhysicalDamage(LivingEntity l, StatusInstance i) {
        return List.of(ModifierStatEffect.entry(AttributeRegistry.DAMAGE, 0.25 * i.scaledMagnitude(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, Set.of(GateTag.PHYSICAL)));
    }
    public static List<StatEntry> auraChaosDamage(LivingEntity l, StatusInstance i) {
        return List.of(ModifierStatEffect.entry(AttributeRegistry.DAMAGE, 0.25 * i.scaledMagnitude(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, Set.of(GateTag.CHAOS)));
    }
    // 抗性类：平加（ADD_VALUE，base=0 零基）
    public static List<StatEntry> auraFireResist(LivingEntity l, StatusInstance i) {
        return List.of(ModifierStatEffect.entry(AttributeRegistry.RESISTANCES, 0.22 * i.scaledMagnitude(),
                AttributeModifier.Operation.ADD_VALUE, Set.of(GateTag.FIRE)));
    }
    public static List<StatEntry> auraColdResist(LivingEntity l, StatusInstance i) {
        return List.of(ModifierStatEffect.entry(AttributeRegistry.RESISTANCES, 0.22 * i.scaledMagnitude(),
                AttributeModifier.Operation.ADD_VALUE, Set.of(GateTag.COLD)));
    }
    public static List<StatEntry> auraLightningResist(LivingEntity l, StatusInstance i) {
        return List.of(ModifierStatEffect.entry(AttributeRegistry.RESISTANCES, 0.22 * i.scaledMagnitude(),
                AttributeModifier.Operation.ADD_VALUE, Set.of(GateTag.LIGHTNING)));
    }
    public static List<StatEntry> auraChaosResist(LivingEntity l, StatusInstance i) {
        return List.of(ModifierStatEffect.entry(AttributeRegistry.RESISTANCES, 0.10 * i.scaledMagnitude(),
                AttributeModifier.Operation.ADD_VALUE, Set.of(GateTag.CHAOS)));
    }
    // 战旗元素抗性（#389 / GAP-FIX#19）：三元素抗性平加（ADD_VALUE，base=0 零基），复用 AttributeRegistry.RESISTANCES。
    // 与 aura*Resist 同形状，但不乘 0.22 光环常数——传入的 i.scaledMagnitude() 已是节点值（规范 0.25）。
    public static List<StatEntry> bannerElementalRes(LivingEntity l, StatusInstance i) {
        return List.of(
                ModifierStatEffect.entry(AttributeRegistry.RESISTANCES, i.scaledMagnitude(),
                        AttributeModifier.Operation.ADD_VALUE, Set.of(GateTag.FIRE)),
                ModifierStatEffect.entry(AttributeRegistry.RESISTANCES, i.scaledMagnitude(),
                        AttributeModifier.Operation.ADD_VALUE, Set.of(GateTag.COLD)),
                ModifierStatEffect.entry(AttributeRegistry.RESISTANCES, i.scaledMagnitude(),
                        AttributeModifier.Operation.ADD_VALUE, Set.of(GateTag.LIGHTNING)));
    }
    // 魔力回复 / 闪避：more（ADD_MULTIPLIED_TOTAL）
    public static List<StatEntry> auraManaRegen(LivingEntity l, StatusInstance i) {
        return List.of(ModifierStatEffect.entry(AttributeRegistry.REGENERATION_RATE, 0.25 * i.scaledMagnitude(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, Set.of()));
    }
    public static List<StatEntry> auraEvasion10(LivingEntity l, StatusInstance i) {
        return List.of(ModifierStatEffect.entry(AttributeRegistry.EVASION, 0.10 * i.scaledMagnitude(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, Set.of()));
    }
    public static List<StatEntry> auraEvasion25(LivingEntity l, StatusInstance i) {
        return List.of(ModifierStatEffect.entry(AttributeRegistry.EVASION, 0.25 * i.scaledMagnitude(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, Set.of()));
    }

    /** 生命药剂授予 Guard（#14 / GAP-FIX#12）：激活期授予 GUARD_RECOVERY（无作用域门控，base=0 平加 ADD_VALUE，
     * 见 §三定律 #3），强度 = 实例 magnitude（drink 时传入的「每秒回血 ÷ 最大生命」）。
     * 经 StatusStatBridge 在状态激活期注入 STAT_STORE，到期自动移除（无需事件回调）。
     * <p><b>门控说明</b>：原 {@code {guard}} 作用域标记已移除——guard_from_flask 状态本身就是 Guard 的唯一来源，
     * 且状态 tag 经 {@code StatusEngine.activeTags} 仅进事件作用域、<b>不</b>进 Stat 折叠作用域（archon 模式亦靠显式
     * {@code StatScopes.enter} 注入），故 {@code {guard}} 门控恒不匹配、GUARD_RECOVERY 恒为 0（死线）。
     * 去掉门控后：状态激活期即贡献 GUARD_RECOVERY，读点 DamagePipeline:783 自带 {@code {guard}} scope 取用（无害）。 */
    public static List<StatEntry> guardFromFlask(LivingEntity l, StatusInstance i) {
        return List.of(ModifierStatEffect.entry(AttributeRegistry.GUARD_RECOVERY, i.scaledMagnitude(),
                AttributeModifier.Operation.ADD_VALUE, Set.of()));
    }

    /** 流血 tick：DoT 结算（「流血禁再生」已归一 #56 为 GrantOnEvent，事件 {caused_ailment, bleed}）。 */
    public static void bleedTick(StatusInstance inst, LivingEntity target) {
        // 归一 #56：「流血禁再生」改由 EFF_GRANT_ON_EVENT_NO_REGEN_ON_BLEED（事件 {caused_ailment, bleed}）承载，
        // 此处的施加方标记硬判已移除（原每 tick 重复施加，改为事件触发一次）。
        dealSplitDoT(target, inst);
    }

    /** 破甲 stat 贡献（含完全破甲档；致残/禁再生已归一 #56 为 GrantOnEvent，事件 FULLY_BROKE_ARMOUR）。 */
    public static List<StatEntry> armourBreak(LivingEntity living, StatusInstance inst) {
        List<StatEntry> out = new ArrayList<>(armorReduction(-inst.scaledMagnitude() / 100.0));
        if (inst.fullyBroken) {
            // 归一 #56：「完全破甲致残 / 禁再生」改由 EFF_GRANT_ON_EVENT_*（事件 FULLY_BROKE_ARMOUR）承载，
            // 此处的施加方标记硬判已移除。
            out.addAll(armorBreakFully());
        }
        return out;
    }

    /** 创口：击中者流血几率贡献 + 施加方跨实体读取的「每层额外物理承伤」。 */
    public static List<StatEntry> incision(LivingEntity living, StatusInstance inst) {
        List<StatEntry> out = new ArrayList<>(incisionBleedChance(inst));
        if (inst.source != null && inst.stacks > 0) {
            Entity e = living.level().getEntity(inst.source);
            if (e instanceof LivingEntity caster) {
                double per = caster.getAttributeValue(AttributeRegistry.INCISION_TAKEN_PHYSICAL_PER_INCISION);
                out.addAll(damageTaken(per * inst.scaledStacks(), Set.of(GateTag.PHYSICAL)));
            }
        }
        return out;
    }

    public static List<StatEntry> puppetMaster(LivingEntity living, StatusInstance inst) {
        double effect = 1.0 + com.cimdy.coe.data.codec.support.stat.engine.StatEngine.foldDelta(living, AttributeRegistry.PUPPET_MASTER_EFFECT);
        // PoE2 Puppet Master（每层）：命令技能 10% 增技能速度 / 10% 减移动速度惩罚；
        // 召唤物 10% 增伤（命令）、2% 移速、3% 攻速/施法速度。倍率 = poe2PerStack × effect × stacks。
        return List.of(
                StatGen.ref(StatIds.MINIONS_DEAL_INC_DAMAGE_WITH_COMMAND_SKILLS, 0.10 * effect * inst.scaledStacks()),
                StatGen.ref(StatIds.INC_MINION_MOVEMENT_SPEED, 0.02 * effect * inst.scaledStacks()),
                StatGen.ref(StatIds.INC_MINIONS_ATTACK_CAST_SPEED, 0.03 * effect * inst.scaledStacks()),
                StatGen.ref(StatIds.INC_SKILL_SPEED_WITH_COMMAND_SKILLS, 0.10 * effect * inst.scaledStacks()),
                StatGen.ref(StatIds.REDUCED_MOVEMENT_SPEED_PENALTY_WITH_COMMAND_SKILLS, 0.10 * effect * inst.scaledStacks())
        );
    }

    public static List<StatEntry> arcaneSurge(LivingEntity living, StatusInstance inst) {
        List<StatEntry> refs = new ArrayList<>(List.of(
                StatGen.ref(StatIds.ARCANE_SURGE_CAST_SPEED, 0.15 * inst.scaledMagnitude())));
        if (StatEngine.gateOf(living, "arcane_surge_grants_health_regen", Set.of())) {
            refs.add(StatGen.ref(StatIds.INC_HEALTH_REGENERATION_RATE, 0.2 * inst.scaledMagnitude()));
        } else {
            refs.add(StatGen.ref(StatIds.ARCANE_SURGE_REGEN, 0.2 * inst.scaledMagnitude()));
        }
        return refs;
    }

    public static List<StatEntry> remnantEmpowered(LivingEntity living, StatusInstance inst) {
        double m = inst.scaledMagnitude();
        if (m <= 0) return List.of();
        return List.of(StatGen.ref(StatIds.INC_INFUSION_MAGNITUDE, m));
    }

    /**
     * 猛攻（Onslaught）：<b>按层数</b>提供双速 + 移速，层数无上限。
     * <ul>
     *   <li>{@link StatIds#ONSLAUGHT_ATTACK_SPEED}：每层 +1% ⇒ {@code 0.01 * stacks}</li>
     *   <li>{@link StatIds#ONSLAUGHT_CAST_SPEED}：每层 +1% ⇒ {@code 0.01 * stacks}</li>
     *   <li>{@link StatIds#ONSLAUGHT_MOVEMENT_SPEED}：每层 +0.5% ⇒ {@code 0.005 * stacks}</li>
     * </ul>
     * 算学校验（默认授予量 20 层）：0.01 × 20 = 0.20 ⇒ 双速 +20%；0.005 × 20 = 0.10 ⇒ 移速 +10%，
     * 与改按层前的 {@code 0.2 * magnitude} / {@code 0.1 * magnitude}（magnitude 1.0 = 满猛攻）完全等价；
     * magnitude 自此不再决定猛攻基础收益（保留给来源侧缩放），改为读 {@code inst.stacks}
     * （与 witherProv / impaleProv 等 STACK_BASED 行为同口径）。
     */
    public static List<StatEntry> onslaught(LivingEntity living, StatusInstance inst) {
        return List.of(
                StatGen.ref(StatIds.ONSLAUGHT_ATTACK_SPEED, 0.01 * inst.scaledStacks()),
                StatGen.ref(StatIds.ONSLAUGHT_CAST_SPEED, 0.01 * inst.scaledStacks()),
                StatGen.ref(StatIds.ONSLAUGHT_MOVEMENT_SPEED, 0.005 * inst.scaledStacks()));
    }

    /**
     * 战旗光环（War Banner）— 严格对齐 poe2db「War Banner」Implicit 三项：
     * <ul>
     *   <li>Aura grants 25% more Attack damage → {@link StatIds#INC_BANNER_DAMAGE}（{banner} 作用域增伤）</li>
     *   <li>Aura grants 50% increased Accuracy Rating → {@link StatIds#INC_ACCURACY}</li>
     *   <li>Aura grants 25% increased Attack Speed → {@link StatIds#BANNER_ATTACK_SPEED}</li>
     * </ul>
     * 原实现附带的 cast speed / movement speed 非 War Banner 语义（属近似接线），已移除。
     */
    public static List<StatEntry> bannerAura(LivingEntity living, StatusInstance inst) {
        return List.of(
                StatGen.ref(StatIds.INC_BANNER_DAMAGE, 0.25 * inst.scaledMagnitude()),
                StatGen.ref(StatIds.INC_ACCURACY, 0.50 * inst.scaledMagnitude()),
                StatGen.ref(StatIds.BANNER_ATTACK_SPEED, 0.25 * inst.scaledMagnitude()));
    }

    public static List<StatEntry> boneOffering(LivingEntity living, StatusInstance inst) {
        return List.of(
                StatGen.ref(StatIds.INC_BLOCK_CHANCE, 0.30 * inst.scaledMagnitude()),
                StatGen.ref(StatIds.RECOVER_HEALTH_WHEN_YOU_BLOCK, 0.15 * inst.scaledMagnitude()),
                StatGen.ref(StatIds.MINIONS_HAVE_ADDITIONAL_PHYSICAL_DAMAGE_REDUCTION, 0.20 * inst.scaledMagnitude()),
                StatGen.ref(StatIds.MINIONS_HAVE_INC_MAX_HEALTH, 0.20 * inst.scaledMagnitude()));
    }

    public static List<StatEntry> painOffering(LivingEntity living, StatusInstance inst) {
        return List.of(
                StatGen.ref(StatIds.INC_MINION_DAMAGE, 0.30 * inst.scaledMagnitude()),
                StatGen.ref(StatIds.MINIONS_DEAL_INC_DAMAGE, 0.30 * inst.scaledMagnitude()));
    }

    public static List<StatEntry> soulOffering(LivingEntity living, StatusInstance inst) {
        return List.of(
                StatGen.ref(StatIds.INC_CAST_SPEED, 0.30 * inst.scaledMagnitude()),
                StatGen.ref(StatIds.INC_SPELL_DAMAGE, 0.30 * inst.scaledMagnitude()),
                StatGen.ref(StatIds.INC_SPIRIT, 0.30 * inst.scaledMagnitude()));
    }

    /** 变回人形按变形时长授予的限时「+X% 法术伤害」增益（#42 / GAP-FIX#13）：幅度由 StatusEngine#grantHumanFormSpellDamage 按变形秒数计算（10%/秒，上限 80%）。 */
    public static List<StatEntry> humanFormSpellDamage(LivingEntity l, StatusInstance i) {
        return List.of(StatGen.ref(StatIds.INC_SPELL_DAMAGE, i.scaledMagnitude()));
    }

    public static List<StatEntry> offeringBonus(LivingEntity living, StatusInstance inst) {
        return List.of(StatGen.ref(StatIds.INC_MINION_DAMAGE, inst.scaledMagnitude()));
    }

    /**
     * 熊形态加成。
     * FORM_BEAR_ARMOR / FORM_BEAR_ATTACK_SPEED 数值依据取自 mod 原始 PoE2 派生设计源
     * （poe2_assets/_backup/step5/StatGen.java：ARMOR 0.40 / ATTACK_SPEED -0.10，ADD_MULTIPLIED_BASE）。
     */
    public static List<StatEntry> formBear() {
        return List.of(
                StatGen.ref(StatIds.DAMAGE_DEALT_BEAR_PHYSICAL, 0.30),
                StatGen.ref(StatIds.DAMAGE_DEALT_BEAR_FIRE, 0.30),
                StatGen.ref(StatIds.FORM_BEAR_ARMOR, 0.40),
                StatGen.ref(StatIds.FORM_BEAR_ATTACK_SPEED, -0.10));
    }

    /**
     * 狼形态加成。
     * FORM_WOLF_ATTACK_SPEED / FORM_WOLF_MOVEMENT_SPEED 数值依据取自原始设计源
     * （poe2_assets/_backup/step5/StatGen.java：ATTACK_SPEED 0.25 / MOVEMENT_SPEED 0.15）。
     */
    public static List<StatEntry> formWolf() {
        return List.of(
                StatGen.ref(StatIds.DAMAGE_DEALT_WOLF_COLD, 0.20),
                StatGen.ref(StatIds.DAMAGE_DEALT_WOLF_PHYSICAL, 0.20),
                StatGen.ref(StatIds.FORM_WOLF_ATTACK_SPEED, 0.25),
                StatGen.ref(StatIds.FORM_WOLF_MOVEMENT_SPEED, 0.15));
    }

    /**
     * 飞龙形态加成。
     * FORM_WYVERN_CRIT_CHANCE / FORM_WYVERN_CRIT_DAMAGE 数值依据取自原始设计源
     * （poe2_assets/_backup/step5/StatGen.java：CRIT_CHANCE 0.15 / CRIT_DAMAGE 0.20）。
     */
    public static List<StatEntry> formWyvern() {
        return List.of(
                StatGen.ref(StatIds.DAMAGE_DEALT_WYVERN_LIGHTNING, 0.25),
                StatGen.ref(StatIds.DAMAGE_DEALT_WYVERN_CHAOS, 0.25),
                StatGen.ref(StatIds.FORM_WYVERN_CRIT_CHANCE, 0.15),
                StatGen.ref(StatIds.FORM_WYVERN_CRIT_DAMAGE, 0.20));
    }

    // ── 参数化行为（从实例 magnitude / stacks / 标签推导） ──

    public static List<StatEntry> blindProv(LivingEntity living, StatusInstance inst) {
        return blind(-inst.magnitude / 100.0);
    }

    public static List<StatEntry> intimidateProv(LivingEntity living, StatusInstance inst) {
        return intimidate(inst.scaledMagnitude() / 100.0);
    }

    public static List<StatEntry> takenCritChanceProv(LivingEntity living, StatusInstance inst) {
        return takenCritChance(inst.scaledMagnitude() / 100.0);
    }

    public static List<StatEntry> shockProv(LivingEntity living, StatusInstance inst) {
        return damageTaken(inst.scaledMagnitude() / 100.0);
    }

    public static List<StatEntry> actionSpeedMag(LivingEntity living, StatusInstance inst) {
        return actionSpeed(inst.scaledMagnitude() / 100.0);
    }

    /** 致残的闪避维（#MAIM-B1 / POE2 真值）：固定 15% reduced Evasion Rating。
     * POE2 该维不随致残强度缩放（亦无「致残效果提高」类词缀），故为常量而非 magnitude 倍率。 */
    private static final double MAIM_EVASION_REDUCED = 0.15;

    /**
     * 致残（#MAIM-B1 / poe2db 实证）：{@code 30% reduced Movement Speed + 15% reduced Evasion Rating}，默认 4 秒。
     *
     * <p>减速维沿用 {@link #actionSpeedMag} 口径：{@code inst.magnitude} 的单位是<b>百分点</b>
     * （30.0 → 30% 减速，经 {@link #actionSpeed} 的 /100 折算），故强度由施加点传入（POE2 真值 30.0）。
     *
     * <p>闪避维为固定 0.15（负向 = reduced），不乘 magnitude；落点复用 {@link #evasion}
     * （= {@code coe:evasion}，ADD_MULTIPLIED_BASE），与致盲「reduced 闪避」同桶——POE2 的 reduced 与 increased
     * 同属加性桶，只有 more / less 才是乘算（ADD_MULTIPLIED_TOTAL），故此处不取 TOTAL。
     */
    public static List<StatEntry> maim(LivingEntity living, StatusInstance inst) {
        List<StatEntry> out = new ArrayList<>();
        // 减速维（#MAIM-B1 / POE2 真值）：仅移动速度 -magnitude%（30.0 百分点 = 30% reduced Movement Speed），
        // 不借通用 actionSpeed 降幅（那会波及攻速/施法/潜行/挖掘，非 PoE2 致残维度）。
        double ms = -Math.min(inst.scaledMagnitude() / 100.0, 1.0);
        if (ms < 0) out.add(sb(StatIds.ACTION_SPEED_MOVEMENT_SPEED, ms));
        // 闪避维：固定 15% reduced Evasion Rating（不随致残强度缩放，POE2 无「致残效果提高」词缀）。
        out.addAll(evasion(-MAIM_EVASION_REDUCED));
        return out;
    }

    public static List<StatEntry> actionSpeedFull(LivingEntity living, StatusInstance inst) {
        return actionSpeed(1.0);
    }

    public static List<StatEntry> fortifyProv(LivingEntity living, StatusInstance inst) {
        return damageTaken(-inst.scaledMagnitude() / 100.0, Set.of(GateTag.ATTACK));
    }

    public static List<StatEntry> witherProv(LivingEntity living, StatusInstance inst) {
        return damageTaken(inst.scaledStacks() * 0.05, Set.of(GateTag.CHAOS));
    }

    public static List<StatEntry> impaleProv(LivingEntity living, StatusInstance inst) {
        return damageTaken(inst.stacks * 0.04 * inst.magnitude, Set.of(GateTag.PHYSICAL));
    }

    public static List<StatEntry> exposureFire(LivingEntity living, StatusInstance inst) {
        return exposure(AttributeRegistry.RESISTANCES, Set.of(GateTag.FIRE), inst.scaledMagnitude());
    }

    public static List<StatEntry> exposureCold(LivingEntity living, StatusInstance inst) {
        return exposure(AttributeRegistry.RESISTANCES, Set.of(GateTag.COLD), inst.scaledMagnitude());
    }

    public static List<StatEntry> exposureLightning(LivingEntity living, StatusInstance inst) {
        return exposure(AttributeRegistry.RESISTANCES, Set.of(GateTag.LIGHTNING), inst.scaledMagnitude());
    }

    public static List<StatEntry> tailwindProv(LivingEntity living, StatusInstance inst) {
        return speedPerStack(inst.scaledStacks(), 0.01, Attributes.ATTACK_SPEED, Attributes.MOVEMENT_SPEED);
    }

    public static List<StatEntry> soulEaterProv(LivingEntity living, StatusInstance inst) {
        return speedPerStack(inst.scaledStacks(), 0.01, Attributes.ATTACK_SPEED, AttributeRegistry.CAST_SPEED);
    }

    public static List<StatEntry> elementalArchonProv(LivingEntity living, StatusInstance inst) {
        return elementalArchon(living);
    }

    // ───────────────────────────── Charge（充能球持有被动） ─────────────────────────────
    // 充能球以 Status 承载，stacks = 当前球数；持有即按球数缩放授予 POE2 式被动。
    // 数值为 POE2 每球基准（final baseline，无需微调）；作用域均为持有者自身（无标签门控）。

    /** 耐力球：每球 -15% 物理伤害承受 + 15% 全元素抗性。 */
    public static List<StatEntry> chargeEndurance(LivingEntity living, StatusInstance inst) {
        int s = inst.scaledStacks();
        if (s <= 0) return List.of();
        return List.of(
                ModifierStatEffect.entry(AttributeRegistry.TAKEN_DAMAGE, -0.15 * s,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE, Set.of(GateTag.PHYSICAL)),
                ModifierStatEffect.entry(AttributeRegistry.RESISTANCES, 0.15 * s,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE, Set.of()));
    }

    /** 狂怒球：每球 +4% 攻速 +4% 施法速度 +4% more 伤害。 */
    public static List<StatEntry> chargeFrenzy(LivingEntity living, StatusInstance inst) {
        int s = inst.scaledStacks();
        if (s <= 0) return List.of();
        return List.of(
                ModifierStatEffect.entry(Attributes.ATTACK_SPEED, 0.04 * s,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE, Set.of()),
                ModifierStatEffect.entry(AttributeRegistry.CAST_SPEED, 0.04 * s,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE, Set.of()),
                ModifierStatEffect.entry(AttributeRegistry.DAMAGE, 0.04 * s,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, Set.of()));
    }

    /** 力量球：每球 +40% 暴击率 +40% 暴击伤害。 */
    public static List<StatEntry> chargePower(LivingEntity living, StatusInstance inst) {
        int s = inst.scaledStacks();
        if (s <= 0) return List.of();
        return List.of(
                ModifierStatEffect.entry(AttributeRegistry.CRIT_CHANCE, 0.40 * s,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE, Set.of()),
                ModifierStatEffect.entry(AttributeRegistry.CRIT_DAMAGE, 0.40 * s,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE, Set.of()));
    }

    /** stat 行为注册表：id → 提供器。未知 id 退化为空贡献。 */
    public static final Map<String, StatusStatProvider> STAT_PROVIDERS = new HashMap<>();
    /** tick 行为注册表：id → 逻辑。未知 id 退化为空操作。 */
    public static final Map<String, StatusTickLogic> TICK_PROVIDERS = new HashMap<>();

    static {
        STAT_PROVIDERS.put("none", (l, i) -> List.of());
        STAT_PROVIDERS.put("curseSlow", StatusBehavior::curseSlow);
        STAT_PROVIDERS.put("dealSplitDoT", (l, i) -> List.of());
        STAT_PROVIDERS.put("blind", StatusBehavior::blindProv);
        STAT_PROVIDERS.put("daze", StatusBehavior::blindProv);
        STAT_PROVIDERS.put("actionSpeedMag", StatusBehavior::actionSpeedMag);
        STAT_PROVIDERS.put("actionSpeedFull", StatusBehavior::actionSpeedFull);
        // 致残（#MAIM-B1）：减速（magnitude 百分点驱动）+ 固定 15% reduced 闪避，二维合一
        STAT_PROVIDERS.put("maim", StatusBehavior::maim);
        STAT_PROVIDERS.put("exposure_fire", StatusBehavior::exposureFire);
        STAT_PROVIDERS.put("exposure_cold", StatusBehavior::exposureCold);
        STAT_PROVIDERS.put("exposure_lightning", StatusBehavior::exposureLightning);
        STAT_PROVIDERS.put("wither", StatusBehavior::witherProv);
        STAT_PROVIDERS.put("intimidate", StatusBehavior::intimidateProv);
        STAT_PROVIDERS.put("takenCritChance", StatusBehavior::takenCritChanceProv);
        STAT_PROVIDERS.put("shock", StatusBehavior::shockProv);
        STAT_PROVIDERS.put("impale", StatusBehavior::impaleProv);
        STAT_PROVIDERS.put("incision", StatusBehavior::incision);
        STAT_PROVIDERS.put("puppetMaster", StatusBehavior::puppetMaster);
        STAT_PROVIDERS.put("arcaneSurge", StatusBehavior::arcaneSurge);
        STAT_PROVIDERS.put("adrenaline", (l, i) -> adrenaline());
        STAT_PROVIDERS.put("fortify", StatusBehavior::fortifyProv);
        STAT_PROVIDERS.put("unholyMight", (l, i) -> unholyMight());
        STAT_PROVIDERS.put("elementalArchon", StatusBehavior::elementalArchonProv);
        STAT_PROVIDERS.put("onslaught", StatusBehavior::onslaught);
        STAT_PROVIDERS.put("bannerAura", StatusBehavior::bannerAura);
        STAT_PROVIDERS.put("tailwind", StatusBehavior::tailwindProv);
        STAT_PROVIDERS.put("soulEater", StatusBehavior::soulEaterProv);
        STAT_PROVIDERS.put("boneOffering", StatusBehavior::boneOffering);
        STAT_PROVIDERS.put("painOffering", StatusBehavior::painOffering);
        STAT_PROVIDERS.put("soulOffering", StatusBehavior::soulOffering);
        STAT_PROVIDERS.put("offeringBonus", StatusBehavior::offeringBonus);
        STAT_PROVIDERS.put("remnantEmpowered", StatusBehavior::remnantEmpowered);
        STAT_PROVIDERS.put("armourBreak", StatusBehavior::armourBreak);
        STAT_PROVIDERS.put("formBear", (l, i) -> formBear());
        STAT_PROVIDERS.put("formWolf", (l, i) -> formWolf());
        STAT_PROVIDERS.put("formWyvern", (l, i) -> formWyvern());
        STAT_PROVIDERS.put("curseFireResist", StatusBehavior::curseFireResist);
        STAT_PROVIDERS.put("curseLightningResist", StatusBehavior::curseLightningResist);
        STAT_PROVIDERS.put("curseColdResist", StatusBehavior::curseColdResist);
        STAT_PROVIDERS.put("curseElementalResist", StatusBehavior::curseElementalResist);
        STAT_PROVIDERS.put("curseChaosResist", StatusBehavior::curseChaosResist);
        STAT_PROVIDERS.put("curseEnfeeble", StatusBehavior::curseEnfeeble);
        STAT_PROVIDERS.put("curseVulnerability", StatusBehavior::curseVulnerability);
        // 精魂光环（Aura / Herald）stat 提供器：按 gem id 经 AURA_PROVIDER_BY_ID 分发
        STAT_PROVIDERS.put("auraFireDamage", StatusBehavior::auraFireDamage);
        STAT_PROVIDERS.put("auraColdDamage", StatusBehavior::auraColdDamage);
        STAT_PROVIDERS.put("auraLightningDamage", StatusBehavior::auraLightningDamage);
        STAT_PROVIDERS.put("auraPhysicalDamage", StatusBehavior::auraPhysicalDamage);
        STAT_PROVIDERS.put("auraChaosDamage", StatusBehavior::auraChaosDamage);
        STAT_PROVIDERS.put("auraFireResist", StatusBehavior::auraFireResist);
        STAT_PROVIDERS.put("auraColdResist", StatusBehavior::auraColdResist);
        STAT_PROVIDERS.put("auraLightningResist", StatusBehavior::auraLightningResist);
        STAT_PROVIDERS.put("auraChaosResist", StatusBehavior::auraChaosResist);
        STAT_PROVIDERS.put("auraManaRegen", StatusBehavior::auraManaRegen);
        STAT_PROVIDERS.put("auraEvasion10", StatusBehavior::auraEvasion10);
        STAT_PROVIDERS.put("auraEvasion25", StatusBehavior::auraEvasion25);
        // 充能球持有被动（stacks = 球数，按球数缩放；数值 POE2 基准值，无需微调）
        STAT_PROVIDERS.put("charge_endurance", StatusBehavior::chargeEndurance);
        STAT_PROVIDERS.put("charge_frenzy", StatusBehavior::chargeFrenzy);
        STAT_PROVIDERS.put("charge_power", StatusBehavior::chargePower);
        // 生命药剂授予 Guard（#14）：激活期注入 GUARD_RECOVERY（{guard} 作用域）。
        STAT_PROVIDERS.put("guard_from_flask", StatusBehavior::guardFromFlask);
        // 变回人形按变形时长授予的限时「+X% 法术伤害」（#42 / GAP-FIX#13）
        STAT_PROVIDERS.put("human_form_spell_dmg", StatusBehavior::humanFormSpellDamage);
        // 战旗元素抗性（#389 / GAP-FIX#19）：三元素抗性平加
        STAT_PROVIDERS.put("bannerElementalRes", StatusBehavior::bannerElementalRes);

        TICK_PROVIDERS.put("none", (i, e) -> {});
        TICK_PROVIDERS.put("dealSplitDoT", (i, e) -> dealSplitDoT(e, i));
        TICK_PROVIDERS.put("bleed", StatusBehavior::bleedTick);
        TICK_PROVIDERS.put("curseTick", StatusBehavior::curseTick);
    }

    /** 按 id 取 stat 提供器（未知 id → 空贡献）。 */
    public static StatusStatProvider statProvider(String id) {
        return STAT_PROVIDERS.getOrDefault(id, (l, i) -> List.of());
    }

    /** 按 id 取 tick 逻辑（未知 id → 空操作）。 */
    public static StatusTickLogic tickProvider(String id) {
        return TICK_PROVIDERS.getOrDefault(id, (i, e) -> {});
    }
}
