package com.cimdy.coe.data.codec.support.status.def;

import com.cimdy.coe.data.codec.support.status.runtime.StatusBehavior;
import com.cimdy.coe.entity.custom.skill.visual.SkillBanner;
import com.cimdy.coe.data.helper.resource.HealthHelper;
import com.cimdy.coe.data.codec.support.stat.effect.status.ImmuneStatEffect;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.support.status.def.StatusType.MagnitudeMeaning;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.entity.custom.skill.OfferingType;

import com.cimdy.coe.registry.AttributeRegistry;
import java.util.List;
import java.util.Set;

import net.minecraft.resources.Identifier;
import com.cimdy.coe.data.helper.engine.DamagePipeline;

/**
 * 状态定义数据持有者（全量收口）：合并原 8 个分类注册表
 * （Ailment / Debuff / Buff / Offering / Revive / Charge / Herald / Form）的全部
 * {@link StatusTypeDef} 声明 + 公开 {@link StatusType} 常量 + {@link #forType} 等辅助方法。
 *
 * <p>「全量推动新系统」：原 8 个注册类已完全删除，状态定义仅在此单点数据化声明，
 * 行为按 {@code tickBehaviorId} / {@code statBehaviorId} 引用 {@link StatusBehavior} 中有限命名的实现
 * {@link StatusType#register(StatusTypeDef)} 入库。
 *
 * <p>运行时动态状态（诅咒 / 标记等按技能 id 即时注册的）仍由 {@link StatusType#dynamic} 承载
 * （如诅咒 id = 技能 id，无法静态枚举）；其 id 由技能数据提供。地面 / 标记等<b>固定 id</b> 的状态
 * 在此静态声明（{@code *_GROUND} / {@code MARK}），不再走 {@code dynamic}。
 */
public final class StatusDefs {

    // ── 致残（Maim）真值常量（#MAIM-B1）：POE2 标准致残 = 30% reduced 移动速度 + 15% reduced 闪避值，持续 4 秒 ──
    /** 致残默认时长：POE2 真值 4 秒 = 80 ticks。 */
    public static final int MAIM_DEFAULT_DURATION_TICKS = 80;
    /** 致残默认强度：POE2 真值 30% reduced 移动速度；单位是<b>百分点</b>（经 maim 行为 /100 折算为比例）。 */
    public static final double MAIM_DEFAULT_MAGNITUDE = 30.0;

    // ───────────────────────────── Ailment（伤害性 / 控制性异常） ─────────────────────────────

    public static final StatusTypeDef IGNITE_DEF = StatusTypeDef.def("ignite", StatusKind.DAMAGING)
            .mag(MagnitudeMeaning.DAMAGE_PERCENT_PER_SECOND)
            .tick("dealSplitDoT").stat("none")
            .duration(80).ailment(AilmentSpec.dot("fire", AilmentSpec.BuildupMode.THRESHOLD, 0.20, 2, false, 4))
            .tags(GateTag.ELEMENTAL, GateTag.DAMAGE_OVER_TIME, "ailment").build();
    public static final StatusType IGNITE = StatusType.register(IGNITE_DEF);

    public static final StatusTypeDef POISON_DEF = StatusTypeDef.def("poison", StatusKind.DAMAGING)
            .mag(MagnitudeMeaning.DAMAGE_PERCENT_PER_SECOND).stackable(true).maxStacks(10)
            .tick("dealSplitDoT").stat("none")
            .duration(40).ailment(AilmentSpec.dot("chaos", AilmentSpec.BuildupMode.THRESHOLD, 0.20, 1, true, 2))
            .tags(GateTag.DAMAGE_OVER_TIME, "ailment").build();
    public static final StatusType POISON = StatusType.register(POISON_DEF);

    public static final StatusTypeDef BLEED_DEF = StatusTypeDef.def("bleed", StatusKind.DAMAGING)
            .mag(MagnitudeMeaning.DAMAGE_PERCENT_PER_SECOND)
            .tick("bleed").stat("none")
            .duration(100).ailment(AilmentSpec.dot("physical", AilmentSpec.BuildupMode.CHANCE, 0.15, 0, true, 5))
            .tags(GateTag.DAMAGE_OVER_TIME, "ailment").build();
    public static final StatusType BLEED = StatusType.register(BLEED_DEF);

    public static final StatusTypeDef CHILL_DEF = StatusTypeDef.def("chill", StatusKind.CONTROL)
            .mag(MagnitudeMeaning.SLOW_PERCENT)
            .tick("none").stat("actionSpeedFull")
            .duration(40).ailment(AilmentSpec.control("cold", AilmentSpec.BuildupMode.PARTIAL, 0.2, 2))
            .tags(GateTag.ELEMENTAL, GateTag.NON_DAMAGING, "ailment").build();
    public static final StatusType CHILL = StatusType.register(CHILL_DEF);

    public static final StatusTypeDef FREEZE_DEF = StatusTypeDef.def("freeze", StatusKind.CONTROL)
            .mag(MagnitudeMeaning.SLOW_PERCENT)
            .tick("none").stat("actionSpeedFull")
            .duration(80).ailment(AilmentSpec.control("cold", AilmentSpec.BuildupMode.THRESHOLD, 1.0, 4))
            .tags(GateTag.ELEMENTAL, GateTag.NON_DAMAGING, "ailment").build();
    public static final StatusType FREEZE = StatusType.register(FREEZE_DEF);

    public static final StatusTypeDef SHOCK_DEF = StatusTypeDef.def("shock", StatusKind.CONTROL)
            .mag(MagnitudeMeaning.MAGNITUDE_PERCENT)
            .tick("none").stat("shock")
            .duration(80).ailment(AilmentSpec.control("lightning", AilmentSpec.BuildupMode.THRESHOLD, 0.2, 4))
            .tags(GateTag.ELEMENTAL, GateTag.NON_DAMAGING, "ailment").build();
    public static final StatusType SHOCK = StatusType.register(SHOCK_DEF);

    public static final StatusTypeDef ELECTROCUTE_DEF = StatusTypeDef.def("electrocute", StatusKind.CONTROL)
            .mag(MagnitudeMeaning.SLOW_PERCENT)
            .tick("none").stat("actionSpeedFull")
            .duration(60).ailment(AilmentSpec.control("lightning", AilmentSpec.BuildupMode.THRESHOLD, 1.5, 1.0, 3))
            .tags(GateTag.ELEMENTAL, GateTag.NON_DAMAGING, "ailment").build();
    public static final StatusType ELECTROCUTE = StatusType.register(ELECTROCUTE_DEF);

    public static final StatusTypeDef PIN_DEF = StatusTypeDef.def("pin", StatusKind.CONTROL)
            .mag(MagnitudeMeaning.SLOW_PERCENT)
            .tick("none").stat("actionSpeedMag")
            .duration(40).ailment(AilmentSpec.NONE).tags("ailment").build();
    public static final StatusType PIN = StatusType.register(PIN_DEF);

    // ───────────────────────────── Debuff（减益） ─────────────────────────────

    /** 致盲：命中 -magnitude% 且闪避 -magnitude%（经 blind 行为）。 */
    public static final StatusTypeDef BLIND_DEF = StatusTypeDef.def("blind", StatusKind.DEBUFF)
            .mag(MagnitudeMeaning.MAGNITUDE_PERCENT).tick("none").stat("blind").build();
    public static final StatusType BLIND = StatusType.register(BLIND_DEF);

    /** 眩晕（Daze）：命中 -magnitude%（经 blind 行为）。 */
    public static final StatusTypeDef DAZE_DEF = StatusTypeDef.def("daze", StatusKind.DEBUFF)
            .mag(MagnitudeMeaning.MAGNITUDE_PERCENT).tick("none").stat("blind").build();
    public static final StatusType DAZE = StatusType.register(DAZE_DEF);

    /** 削弱（Debilitate）：动作速度 -magnitude%。 */
    public static final StatusTypeDef DEBILITATE_DEF = StatusTypeDef.def("debilitate", StatusKind.DEBUFF)
            .mag(MagnitudeMeaning.MAGNITUDE_PERCENT).tick("none").stat("actionSpeedMag").build();
    public static final StatusType DEBILITATE = StatusType.register(DEBILITATE_DEF);

    /**
     * 致残（#MAIM-B1 / poe2db 实证）：{@code 30% reduced Movement Speed + 15% reduced Evasion Rating}，持续 4 秒。
     * 减速维 = magnitude%（百分点单位，30.0 = 30% 减速）；闪避维为固定 15%（经 {@code maim} 行为一并给出，
     * 不随 magnitude 缩放）。默认时长 80 ticks = POE2 真值 4 秒。
     */
    public static final StatusTypeDef MAIM_DEF = StatusTypeDef.def("maim", StatusKind.DEBUFF)
            .mag(MagnitudeMeaning.SLOW_PERCENT).tick("none").stat("maim")
            .duration(MAIM_DEFAULT_DURATION_TICKS).build();
    public static final StatusType MAIM = StatusType.register(MAIM_DEF);

    /** 减速：通用减速（magnitude = 减速%）。 */
    public static final StatusTypeDef SLOW_DEF = StatusTypeDef.def("slow", StatusKind.DEBUFF)
            .mag(MagnitudeMeaning.SLOW_PERCENT).tick("none").stat("actionSpeedMag").build();
    public static final StatusType SLOW = StatusType.register(SLOW_DEF);

    /** 阻碍：减速 + 伤害减免（magnitude = 减速%）。 */
    public static final StatusTypeDef HINDER_DEF = StatusTypeDef.def("hinder", StatusKind.DEBUFF)
            .mag(MagnitudeMeaning.SLOW_PERCENT).tick("none").stat("actionSpeedMag").build();
    public static final StatusType HINDER = StatusType.register(HINDER_DEF);

    /** 火曝露。 */
    public static final StatusTypeDef EXPOSURE_FIRE_DEF = StatusTypeDef.def("exposure_fire", StatusKind.DEBUFF)
            .mag(MagnitudeMeaning.RESISTANCE_REDUCTION).tick("none").stat("exposure_fire")
            .duration(80).ailment(AilmentSpec.NONE).tags("exposure").build();
    public static final StatusType EXPOSURE_FIRE = StatusType.register(EXPOSURE_FIRE_DEF);

    /** 冰曝露。 */
    public static final StatusTypeDef EXPOSURE_COLD_DEF = StatusTypeDef.def("exposure_cold", StatusKind.DEBUFF)
            .mag(MagnitudeMeaning.RESISTANCE_REDUCTION).tick("none").stat("exposure_cold")
            .duration(80).ailment(AilmentSpec.NONE).tags("exposure").build();
    public static final StatusType EXPOSURE_COLD = StatusType.register(EXPOSURE_COLD_DEF);

    /** 雷曝露。 */
    public static final StatusTypeDef EXPOSURE_LIGHTNING_DEF = StatusTypeDef.def("exposure_lightning", StatusKind.DEBUFF)
            .mag(MagnitudeMeaning.RESISTANCE_REDUCTION).tick("none").stat("exposure_lightning")
            .duration(80).ailment(AilmentSpec.NONE).tags("exposure").build();
    public static final StatusType EXPOSURE_LIGHTNING = StatusType.register(EXPOSURE_LIGHTNING_DEF);

    /** 凋零：承受混沌伤害 +6%/层（上限 10 层）。 */
    public static final StatusTypeDef WITHER_DEF = StatusTypeDef.def("wither", StatusKind.DEBUFF)
            .stackable(true).maxStacks(10).mag(MagnitudeMeaning.STACK_BASED).tick("none").stat("wither").build();
    public static final StatusType WITHER = StatusType.register(WITHER_DEF);

    /** 腐化之血：物理持续伤害 DoT。 */
    public static final StatusTypeDef CORRUPTED_BLOOD_DEF = StatusTypeDef.def("corrupted_blood", StatusKind.DAMAGING)
            .stackable(true).maxStacks(10).mag(MagnitudeMeaning.DAMAGE_PERCENT_PER_SECOND)
            .tick("dealSplitDoT").stat("none")
            .duration(80).ailment(AilmentSpec.dot("physical", AilmentSpec.BuildupMode.THRESHOLD, 0.2, 0, false, 0))
            .tags(GateTag.DAMAGE_OVER_TIME, GateTag.PHYSICAL).build();
    public static final StatusType CORRUPTED_BLOOD = StatusType.register(CORRUPTED_BLOOD_DEF);

    /** 完全破甲禁再生：纯标记 debuff（由 HealthHelper 真读取并归零生命再生）。 */
    public static final StatusTypeDef CANNOT_REGEN_HEALTH_DEF = StatusTypeDef.def("cannot_regen_health", StatusKind.DEBUFF)
            .mag(MagnitudeMeaning.NONE).tick("none").stat("none").build();
    public static final StatusType CANNOT_REGEN_HEALTH = StatusType.register(CANNOT_REGEN_HEALTH_DEF);

    /** 危害标记（Hazard Mark）：纯标记 debuff（承载「敌人近期被你的危害物影响」门控）。 */
    public static final StatusTypeDef HAZARD_MARK_DEF = StatusTypeDef.def("hazard_mark", StatusKind.DEBUFF)
            .mag(MagnitudeMeaning.NONE).tick("none").stat("none").build();
    public static final StatusType HAZARD_MARK = StatusType.register(HAZARD_MARK_DEF);

    /** 破甲：受害者护甲 -magnitude%（完全破甲档由 armourBreak 行为处理）。 */
    public static final StatusTypeDef ARMOUR_BREAK_DEF = StatusTypeDef.def("armour_break", StatusKind.DEBUFF)
            .mag(MagnitudeMeaning.STACK_BASED).tick("none").stat("armourBreak").build();
    public static final StatusType ARMOUR_BREAK = StatusType.register(ARMOUR_BREAK_DEF);

    /** 嘲讽：强制攻击施加者（magnitude 无效）。 */
    public static final StatusTypeDef TAUNT_DEF = StatusTypeDef.def("taunt", StatusKind.DEBUFF)
            .mag(MagnitudeMeaning.NONE).tick("none").stat("none").build();
    public static final StatusType TAUNT = StatusType.register(TAUNT_DEF);

    /** 恐吓：目标承受伤害 +magnitude% 且造成伤害 -magnitude%。 */
    public static final StatusTypeDef INTIMIDATE_DEF = StatusTypeDef.def("intimidate", StatusKind.DEBUFF)
            .mag(MagnitudeMeaning.MAGNITUDE_PERCENT).tick("none").stat("intimidate").build();
    public static final StatusType INTIMIDATE = StatusType.register(INTIMIDATE_DEF);

    /** 眩晕（Stun）。 */
    public static final StatusTypeDef STUN_DEF = StatusTypeDef.def("stun", StatusKind.CONTROL)
            .mag(MagnitudeMeaning.NONE).tick("none").stat("none")
            .duration(80).ailment(AilmentSpec.of("physical", AilmentSpec.BuildupMode.THRESHOLD))
            .tags(GateTag.NON_DAMAGING).build();
    public static final StatusType STUN = StatusType.register(STUN_DEF);

    /** 重晕免疫窗口：标记状态，仅作积累抑制。 */
    public static final StatusTypeDef STUN_IMMUNE_DEF = StatusTypeDef.def("stun_immune", StatusKind.DEBUFF)
            .mag(MagnitudeMeaning.NONE).tick("none").stat("none").build();
    public static final StatusType STUN_IMMUNE = StatusType.register(STUN_IMMUNE_DEF);

    /** 暴击虚弱：被暴击几率 +magnitude%。 */
    public static final StatusTypeDef CRITICAL_WEAKNESS_DEF = StatusTypeDef.def("critical_weakness", StatusKind.DEBUFF)
            .mag(MagnitudeMeaning.MAGNITUDE_PERCENT).tick("none").stat("takenCritChance").build();
    public static final StatusType CRITICAL_WEAKNESS = StatusType.register(CRITICAL_WEAKNESS_DEF);

    /** 石化：完全瘫痪（=Frozen 行为）。 */
    public static final StatusTypeDef PETRIFY_DEF = StatusTypeDef.def("petrify", StatusKind.CONTROL)
            .mag(MagnitudeMeaning.SLOW_PERCENT).tick("none").stat("actionSpeedFull").build();
    public static final StatusType PETRIFY = StatusType.register(PETRIFY_DEF);

    /** 招架：攻击者被招架（短暂停顿）。 */
    public static final StatusTypeDef PARRIED_DEF = StatusTypeDef.def("parried", StatusKind.CONTROL)
            .mag(MagnitudeMeaning.SLOW_PERCENT).tick("none").stat("actionSpeedFull").build();
    public static final StatusType PARRIED = StatusType.register(PARRIED_DEF);

    /** 穿刺：可叠层；每层使受害者承受物理伤害 +IMPALE_TAKEN_PER_STACK% × magnitude。 */
    public static final StatusTypeDef IMPALE_DEF = StatusTypeDef.def("impale", StatusKind.DEBUFF)
            .stackable(true).maxStacks(10).mag(MagnitudeMeaning.STACK_BASED).tick("none").stat("impale").build();
    public static final StatusType IMPALE = StatusType.register(IMPALE_DEF);

    /**
     * 创口（Incision）：可叠层（上限 10 层），经 Stat 条目提供其对「击中者」流血几率的贡献 +
     * 施加方跨实体读取的「每层额外物理承伤」。
     */
    public static final StatusTypeDef INCISION_DEF = StatusTypeDef.def("incision", StatusKind.DEBUFF)
            .stackable(true).maxStacks(10).mag(MagnitudeMeaning.STACK_BASED).tick("none").stat("incision").build();
    public static final StatusType INCISION = StatusType.register(INCISION_DEF);

    /** 重晕档 HUD 图标（与轻晕 stun.png 区分，单 STUN 状态两档合一后由 iconOverride 使用）。 */
    public static final Identifier HEAVY_STUN_ICON = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "textures/gui/status/heavy_stun.png");

    // ───────────────────────────── Buff（增益） ─────────────────────────────

    /**
     * 猛攻（Onslaught）：<b>叠层</b>增益，<b>层数无上限</b>——强度由层数而非 magnitude 承载。
     * <ul>
     *   <li>每层：攻击速度 +1%、施法速度 +1%、移动速度 +0.5%
     *       （见 {@code StatusBehavior.onslaught}，读 {@code inst.scaledStacks()}）；</li>
     *   <li>默认授予 20 层 ⇒ 双速 +20%（0.01×20=0.20）、移速 +10%（0.005×20=0.10）——与改按层前的
     *       「magnitude 1.0 = 双速 20% / 移速 10%」完全等价，仅单位由 magnitude 换成 stacks；</li>
     *   <li>无上限：经 {@code uncapped()}（maxStacks=0）声明，StatusEngine 跳过夹紧，
     *       重复授予按 StatusEngine 叠层规则累加层数（不夹紧）。</li>
     * </ul>
     * 未声明 duration ⇒ 取 StatusTypeDef.Builder 默认值 80 ticks（4s）。
     */
    public static final StatusTypeDef ONSLAUGHT_DEF = StatusTypeDef.def("onslaught", StatusKind.BUFF)
            .uncapped().mag(MagnitudeMeaning.STACK_BASED).tick("none").stat("onslaught").build();
    public static final StatusType ONSLAUGHT = StatusType.register(ONSLAUGHT_DEF);

    /** 傀儡师：可叠层 buff（层数 = 玩家使用 [Command] 技能时按几率获得，上限动态）。 */
    public static final StatusTypeDef PUPPET_MASTER_DEF = StatusTypeDef.def("puppet_master", StatusKind.BUFF)
            .stackable(true).maxStacks(5).mag(MagnitudeMeaning.STACK_BASED).tick("none").stat("puppetMaster")
            .duration(160).ailment(AilmentSpec.NONE).build();
    public static final StatusType PUPPET_MASTER = StatusType.register(PUPPET_MASTER_DEF);

    /** 疾风：层数 = magnitude 倍率（默认 5 层上限，每层 +1% 攻速/移速）。 */
    public static final StatusTypeDef TAILWIND_DEF = StatusTypeDef.def("tailwind", StatusKind.BUFF)
            .stackable(true).maxStacks(10).mag(MagnitudeMeaning.STACK_BASED).tick("none").stat("tailwind").build();
    public static final StatusType TAILWIND = StatusType.register(TAILWIND_DEF);

    /** 噬魂者：层数 = 击杀叠加（默认 50 层上限，每层 +1% 攻速/施法速度）。 */
    public static final StatusTypeDef SOUL_EATER_DEF = StatusTypeDef.def("soul_eater", StatusKind.BUFF)
            .stackable(true).maxStacks(50).mag(MagnitudeMeaning.STACK_BASED).tick("none").stat("soulEater").build();
    public static final StatusType SOUL_EATER = StatusType.register(SOUL_EATER_DEF);

    /** 秘术增强：施法速度 +15%、魔力回复速度 +20%（二选一由 ARCANE_SURGE_GRANTS_HEALTH_REGEN 标记决定）。 */
    public static final StatusTypeDef ARCANE_SURGE_DEF = StatusTypeDef.def("arcane_surge", StatusKind.BUFF)
            .mag(MagnitudeMeaning.MAGNITUDE_PERCENT).tick("none").stat("arcaneSurge").build();
    public static final StatusType ARCANE_SURGE = StatusType.register(ARCANE_SURGE_DEF);

    /** 肾上腺素：造成伤害 +100%、攻速/施法/移速各 +25%、额外物理减伤 +10%（固定数值，无 magnitude 缩放）。 */
    public static final StatusTypeDef ADRENALINE_DEF = StatusTypeDef.def("adrenaline", StatusKind.BUFF)
            .mag(MagnitudeMeaning.NONE).tick("none").stat("adrenaline").build();
    public static final StatusType ADRENALINE = StatusType.register(ADRENALINE_DEF);

    /** 护体：持有者受到命中伤害 -magnitude%（仅命中，不作用于 DoT）。 */
    public static final StatusTypeDef FORTIFY_DEF = StatusTypeDef.def("fortify", StatusKind.BUFF)
            .mag(MagnitudeMeaning.MAGNITUDE_PERCENT).tick("none").stat("fortify").build();
    public static final StatusType FORTIFY = StatusType.register(FORTIFY_DEF);

    /** 相位：无可叠层纯标记（穿透/无法被命中）。 */
    public static final StatusTypeDef PHASING_DEF = StatusTypeDef.def("phasing", StatusKind.BUFF)
            .mag(MagnitudeMeaning.NONE).tick("none").stat("none").build();
    public static final StatusType PHASING = StatusType.register(PHASING_DEF);

    /** 康复：持有期间能量护盾立即开始充能且充能不可被打断（简化实现：精魂常驻即对自身施加本状态；
     * 真值经「子技能触发 3s 窗口 + 30s 冷却 + 满护盾移除」封装，见 docs/convalescence_wiring.md）。 */
    public static final StatusTypeDef CONVALASCENCE_DEF = StatusTypeDef.def("convalescence", StatusKind.BUFF)
            .mag(MagnitudeMeaning.NONE).tick("none").stat("none").duration(80).build();
    public static final StatusType CONVALASCENCE = StatusType.register(CONVALASCENCE_DEF);

    /** 不洁之力：造成所有伤害的 30% 作为额外混沌伤害（仅命中，不作用于 DoT）。 */
    public static final StatusTypeDef UNHOLY_MIGHT_DEF = StatusTypeDef.def("unholy_might", StatusKind.BUFF)
            .mag(MagnitudeMeaning.NONE).tick("none").stat("unholyMight").build();
    public static final StatusType UNHOLY_MIGHT = StatusType.register(UNHOLY_MIGHT_DEF);

    /** Archon 恢复冷却标记（无属性贡献，仅作冷却门控标记）。 */
    public static final StatusTypeDef ARCHON_RECOVERY_DEF = StatusTypeDef.def("archon_recovery", StatusKind.BUFF)
            .mag(MagnitudeMeaning.NONE).tick("none").stat("none").duration(400).ailment(AilmentSpec.NONE)
            .tags("archon_recovery").build();
    public static final StatusType ARCHON_RECOVERY = StatusType.register(ARCHON_RECOVERY_DEF);

    /** 拾取灌入残迹强化（Remnant Empowered）：窗口内按创建者残迹幅度加成注入 inc_infusion_magnitude。 */
    public static final StatusTypeDef REMNANT_EMPOWERED_DEF = StatusTypeDef.def("remnant_empowered", StatusKind.BUFF)
            .mag(MagnitudeMeaning.MAGNITUDE_PERCENT).tick("none").stat("remnantEmpowered")
            .duration(100).ailment(AilmentSpec.NONE).tags("remnant_empowered").build();
    public static final StatusType REMNANT_EMPOWERED = StatusType.register(REMNANT_EMPOWERED_DEF);

    /** 火元素灌入。 */
    public static final StatusTypeDef INFUSION_FIRE_DEF = StatusTypeDef.def("infusion_fire", StatusKind.BUFF)
            .stackable(true).maxStacks(100).mag(MagnitudeMeaning.NONE).tick("none").stat("none")
            .duration(100000000).ailment(AilmentSpec.NONE).tags("infusion", "fire").build();
    public static final StatusType INFUSION_FIRE = StatusType.register(INFUSION_FIRE_DEF);

    /** 冰元素灌入。 */
    public static final StatusTypeDef INFUSION_COLD_DEF = StatusTypeDef.def("infusion_cold", StatusKind.BUFF)
            .stackable(true).maxStacks(100).mag(MagnitudeMeaning.NONE).tick("none").stat("none")
            .duration(100000000).ailment(AilmentSpec.NONE).tags("infusion", "cold").build();
    public static final StatusType INFUSION_COLD = StatusType.register(INFUSION_COLD_DEF);

    /** 雷元素灌入。 */
    public static final StatusTypeDef INFUSION_LIGHTNING_DEF = StatusTypeDef.def("infusion_lightning", StatusKind.BUFF)
            .stackable(true).maxStacks(100).mag(MagnitudeMeaning.NONE).tick("none").stat("none")
            .duration(100000000).ailment(AilmentSpec.NONE).tags("infusion", "lightning").build();
    public static final StatusType INFUSION_LIGHTNING = StatusType.register(INFUSION_LIGHTNING_DEF);

    /** 维金/通配灌入（Vaal / Wildcard Infusion）。 */
    public static final StatusTypeDef INFUSION_VAAL_DEF = StatusTypeDef.def("infusion_vaal", StatusKind.BUFF)
            .stackable(true).maxStacks(100).mag(MagnitudeMeaning.NONE).tick("none").stat("none")
            .duration(400).ailment(AilmentSpec.NONE).tags("infusion", "vaal").build();
    public static final StatusType INFUSION_VAAL = StatusType.register(INFUSION_VAAL_DEF);

    /** 元素执政官（Elemental Archon）：核心效果经 Stat 实现（法术元素伤 +25% more 等），固定比值。 */
    public static final StatusTypeDef ELEMENTAL_ARCHON_DEF = StatusTypeDef.def("elemental_archon", StatusKind.BUFF)
            .mag(MagnitudeMeaning.NONE).tick("none").stat("elementalArchon")
            .duration(200).ailment(AilmentSpec.NONE).tags("archon").build();
    public static final StatusType ELEMENTAL_ARCHON = StatusType.register(ELEMENTAL_ARCHON_DEF);

    /** 亡灵执政官（Archon of Undeath）：标记型增益（与 ELEMENTAL_ARCHON 同族），经 command 事件真授予（R3 安全，非死线）。 */
    public static final StatusTypeDef ARCHON_OF_UNDEATH_DEF = StatusTypeDef.def("archon_of_undeath", StatusKind.BUFF)
            .mag(MagnitudeMeaning.NONE).tick("none").stat("none")
            .duration(200).ailment(AilmentSpec.NONE).tags("archon").build();
    public static final StatusType ARCHON_OF_UNDEATH = StatusType.register(ARCHON_OF_UNDEATH_DEF);

    /** 生命药剂授予 Guard（#14 / GAP-FIX#12）：生命药剂生效时授予限时 GUARD_RECOVERY 吸收层。
     * 强度 = 药剂每秒回血 ÷ 最大生命（对齐 DamagePipeline 注释「Flask grant Guard equal to X% of Life Recovery」），
     * 时长 = 药剂每秒回血（秒）；二者均取自 drink 时的 flask 数值。经 StatusStatBridge 注入 STAT_STORE，
     * 到期自动移除（无需事件回调，符合「buff 用 Stat 非事件」约束）。 */
    public static final StatusTypeDef GUARD_FROM_FLASK_DEF = StatusTypeDef.def("guard_from_flask", StatusKind.BUFF)
            .mag(MagnitudeMeaning.MAGNITUDE_PERCENT).tick("none").stat("guard_from_flask")
            .duration(1).ailment(AilmentSpec.NONE).tags("guard").build();
    public static final StatusType GUARD_FROM_FLASK = StatusType.register(GUARD_FROM_FLASK_DEF);

    /** 战旗光环（Banner Aura）：战旗区域内友方持续获得的速度增益。 */
    public static final StatusTypeDef BANNER_AURA_DEF = StatusTypeDef.def("banner_aura", StatusKind.BUFF)
            .mag(MagnitudeMeaning.MAGNITUDE_PERCENT).tick("none").stat("bannerAura").build();
    public static final StatusType BANNER_AURA = StatusType.register(BANNER_AURA_DEF);

    /** 战旗元素抗性（#389 / GAP-FIX#19）：战旗额外授予受影响目标的全部元素抗性 +X%（幅度=BANNER_ELE_RES 标记读值 0.25），
     * 经 StatusStatBridge 注入 STAT_STORE（三元素 ADD_VALUE 平加，复用 AttributeRegistry.RESISTANCES，不建新属性），
     * 由 SkillBanner 经 PeriodicDriver.drive + StatusStatEffect 对光环内友方施加，时长=bannerLingerTicks。 */
    public static final StatusTypeDef BANNER_ELEMENTAL_RES_DEF = StatusTypeDef.def("banner_elemental_res", StatusKind.BUFF)
            .mag(MagnitudeMeaning.MAGNITUDE_PERCENT).tick("none").stat("bannerElementalRes").tags("resistance").build();
    public static final StatusType BANNER_ELEMENTAL_RES = StatusType.register(BANNER_ELEMENTAL_RES_DEF);

    /** 变回人形按变形时长授予的限时「+X% 法术伤害」增益（#42 / GAP-FIX#13）：幅度=消费端计算（10%/秒，上限 80%，持续 8s），经 StatusStatBridge 注入 STAT_STORE。 */
    public static final StatusTypeDef HUMAN_FORM_SPELL_DMG_DEF = StatusTypeDef.def("human_form_spell_dmg", StatusKind.BUFF)
            .mag(MagnitudeMeaning.MAGNITUDE_PERCENT).tick("none").stat("human_form_spell_dmg")
            .duration(160).ailment(AilmentSpec.NONE).tags("spell").build();
    public static final StatusType HUMAN_FORM_SPELL_DMG = StatusType.register(HUMAN_FORM_SPELL_DMG_DEF);

    // ───────────────────────────── Offering（奉献增益） ─────────────────────────────

    /** 白骨奉献：你与召唤物获得防御增益。 */
    public static final StatusTypeDef BONE_OFFERING_DEF = StatusTypeDef.def("bone_offering", StatusKind.BUFF)
            .mag(MagnitudeMeaning.MAGNITUDE_PERCENT).tick("none").stat("boneOffering")
            .duration(200).ailment(AilmentSpec.NONE).tags("offering").build();
    public static final StatusType BONE_OFFERING = StatusType.register(BONE_OFFERING_DEF);

    /** 痛苦奉献：以尖刺为中心的 6 米光环，召唤物获得增伤。 */
    public static final StatusTypeDef PAIN_OFFERING_DEF = StatusTypeDef.def("pain_offering", StatusKind.BUFF)
            .mag(MagnitudeMeaning.MAGNITUDE_PERCENT).tick("none").stat("painOffering")
            .duration(200).ailment(AilmentSpec.NONE).tags("offering").build();
    public static final StatusType PAIN_OFFERING = StatusType.register(PAIN_OFFERING_DEF);

    /** 灵魂奉献：仅自身增益（施法速度 + 法术伤害 + 精魂）。 */
    public static final StatusTypeDef SOUL_OFFERING_DEF = StatusTypeDef.def("soul_offering", StatusKind.BUFF)
            .mag(MagnitudeMeaning.MAGNITUDE_PERCENT).tick("none").stat("soulOffering")
            .duration(200).ailment(AilmentSpec.NONE).tags("offering").build();
    public static final StatusType SOUL_OFFERING = StatusType.register(SOUL_OFFERING_DEF);

    /** 双奉献加伤：当你拥有 ≥2 种不同活跃奉献时，随从伤害额外 +50%（由 stat 承载 inc 值）。 */
    public static final StatusTypeDef OFFERING_MINION_DAMAGE_BONUS_DEF = StatusTypeDef.def(
            "offering_minion_damage_bonus", StatusKind.BUFF)
            .mag(MagnitudeMeaning.MAGNITUDE_PERCENT).tick("none").stat("offeringBonus")
            .duration(200).ailment(AilmentSpec.NONE).tags("offering").build();
    public static final StatusType OFFERING_MINION_DAMAGE_BONUS = StatusType.register(OFFERING_MINION_DAMAGE_BONUS_DEF);

    /** 按奉献类型取对应增益状态。 */
    public static StatusType forType(OfferingType type) {
        return switch (type) {
            case BONE -> BONE_OFFERING;
            case PAIN -> PAIN_OFFERING;
            case SOUL -> SOUL_OFFERING;
        };
    }

    // ───────────────────────────── Revive（复活标记） ─────────────────────────────

    /** 持有者待复活标记：队列非空期间常驻，清空即剥夺。 */
    public static final StatusTypeDef MINION_REVIVING_DEF = StatusTypeDef.def("minion_reviving", StatusKind.BUFF)
            .mag(MagnitudeMeaning.NONE).tick("none").stat("none")
            .duration(100000000).ailment(AilmentSpec.NONE)
            .display("status.coe.minion_reviving", false).tags("minion_reviving").build();
    public static final StatusType MINION_REVIVING = StatusType.register(MINION_REVIVING_DEF);

    // ───────────────────────────── Charge（充能球） ─────────────────────────────

    public static final StatusTypeDef POWER_CHARGE_DEF = StatusTypeDef.def("power_charge", StatusKind.CHARGED)
            .stackable(true).maxStacks(999).mag(MagnitudeMeaning.NONE)
            .tick("none").stat("charge_power").build();
    public static final StatusType POWER_CHARGE = StatusType.register(POWER_CHARGE_DEF);

    public static final StatusTypeDef FRENZY_CHARGE_DEF = StatusTypeDef.def("frenzy_charge", StatusKind.CHARGED)
            .stackable(true).maxStacks(999).mag(MagnitudeMeaning.NONE)
            .tick("none").stat("charge_frenzy").build();
    public static final StatusType FRENZY_CHARGE = StatusType.register(FRENZY_CHARGE_DEF);

    public static final StatusTypeDef ENDURANCE_CHARGE_DEF = StatusTypeDef.def("endurance_charge", StatusKind.CHARGED)
            .stackable(true).maxStacks(999).mag(MagnitudeMeaning.NONE)
            .tick("none").stat("charge_endurance").build();
    public static final StatusType ENDURANCE_CHARGE = StatusType.register(ENDURANCE_CHARGE_DEF);

    // ───────────────────────────── Herald（捷：存在性标记） ─────────────────────────────

    /** 存在性标记刷新时长（ticks）：光环每 tick 重放，1 秒时长使预留停止后约 1 秒失效。 */
    public static final int HERALD_PRESENCE_TICKS = 20;

    /** 受灰烬之捷影响（元素标签 {@code fire}）。 */
    public static final StatusType HERALD_OF_ASH = presence("herald_of_ash", GateTag.FIRE);

    /** 受寒冰之捷影响（元素标签 {@code cold}）。 */
    public static final StatusType HERALD_OF_ICE = presence("herald_of_ice", GateTag.COLD);

    /** 受雷之捷影响（元素标签 {@code lightning}）。 */
    public static final StatusType HERALD_OF_THUNDER = presence("herald_of_thunder", GateTag.LIGHTNING);

    /** 受瘟疫之捷影响（元素标签 {@code chaos}）。 */
    public static final StatusType HERALD_OF_PLAGUE = presence("herald_of_plague", GateTag.CHAOS);

    /** 受血之捷影响（元素标签 {@code physical}）。 */
    public static final StatusType HERALD_OF_BLOOD = presence("herald_of_blood", GateTag.PHYSICAL);

    /** 全族遍历表（供条件/校验按需枚举，新增捷只需在上面加一行并在此登记）。 */
    public static final List<StatusType> HERALD_ALL =
            List.of(HERALD_OF_ASH, HERALD_OF_ICE, HERALD_OF_THUNDER, HERALD_OF_PLAGUE, HERALD_OF_BLOOD);

    /**
     * 登记一个捷存在性标记：不可叠层、无 Stat 贡献、携带 {@code herald} 标签与元素标签。
     * 全族同构，新增捷类型只需在上方加一行并在 {@link #HERALD_ALL} 登记。
     */
    private static StatusType presence(String id, String elementTag) {
        return StatusType.register(StatusTypeDef.def(id, StatusKind.HERALD)
                .mag(MagnitudeMeaning.NONE).tick("none").stat("none")
                .duration(HERALD_PRESENCE_TICKS).ailment(AilmentSpec.NONE)
                .tags(GateTag.HERALD, elementTag).build());
    }

    // ───────────────────────────── Form（形态 Shapeshift） ─────────────────────────────

    /** 形态时长：POE2 形态是持续形态（无自动结束，直到切回人类/使用法术）。取大值近似持续。 */
    private static final int FORM_DURATION = 100000; // ticks（≈83 分钟，等效持续）

    /** 熊形态：慢速高伤 + Rage 联动。效果（{form, bear} 作用域）：物理/火伤害 +30%、护甲 +40%、攻速 -10%。 */
    public static final StatusTypeDef BEAR_DEF = StatusTypeDef.def("form_bear", StatusKind.FORM)
            .mag(MagnitudeMeaning.NONE).tick("none").stat("formBear")
            .duration(FORM_DURATION).ailment(AilmentSpec.NONE)
            .tags("form", "shapeshifted", "bear").build();
    public static final StatusType BEAR = StatusType.register(BEAR_DEF);

    /** 狼形态：高速 + 冰伤。效果（{form, wolf} 作用域）：冰/物理伤害 +20%、攻速 +25%、移速 +15%。 */
    public static final StatusTypeDef WOLF_DEF = StatusTypeDef.def("form_wolf", StatusKind.FORM)
            .mag(MagnitudeMeaning.NONE).tick("none").stat("formWolf")
            .duration(FORM_DURATION).ailment(AilmentSpec.NONE)
            .tags("form", "shapeshifted", "wolf").build();
    public static final StatusType WOLF = StatusType.register(WOLF_DEF);

    /** 龙形态：暴击/电伤。效果（{form, wyvern} 作用域）：电/混沌伤害 +25%、暴击率 +15%、暴击伤害 +20%。 */
    public static final StatusTypeDef WYVERN_DEF = StatusTypeDef.def("form_wyvern", StatusKind.FORM)
            .mag(MagnitudeMeaning.NONE).tick("none").stat("formWyvern")
            .duration(FORM_DURATION).ailment(AilmentSpec.NONE)
            .tags("form", "shapeshifted", "wyvern").build();
    public static final StatusType WYVERN = StatusType.register(WYVERN_DEF);

    // ───────────────────────────── Ground（地面：固定 id 状态，原经 dynamic 注册，现静态声明） ─────────────────────────────

    /** 锯齿地面：站在锯齿地面即获得该状态，供 COND 经 Status 节点读取（与 ignited/shocked/chilled 同构）。 */
    public static final StatusTypeDef JAGGED_GROUND_DEF = StatusTypeDef.def("jagged_ground", StatusKind.GROUND)
            .mag(MagnitudeMeaning.NONE).tick("none").stat("none")
            .duration(20).ailment(AilmentSpec.NONE).tags("jagged_ground").build();
    public static final StatusType JAGGED_GROUND = StatusType.register(JAGGED_GROUND_DEF);

    /**
     * 地面存在性标记（按 GroundTypes 的 id / 标签声明）：
     * 供「on X ground」类免疫按 id 身份匹配（{@code byId} 解析），不承载数值语义。
     */
    public static final StatusTypeDef ON_JAGGED_GROUND_DEF = StatusTypeDef.def("on_jagged_ground", StatusKind.GROUND)
            .mag(MagnitudeMeaning.NONE).tick("none").stat("none")
            .duration(20).ailment(AilmentSpec.NONE).tags("jagged_ground").build();
    public static final StatusType ON_JAGGED_GROUND = StatusType.register(ON_JAGGED_GROUND_DEF);

    public static final StatusTypeDef ON_IGNITED_GROUND_DEF = StatusTypeDef.def("on_ignited_ground", StatusKind.GROUND)
            .mag(MagnitudeMeaning.NONE).tick("none").stat("none")
            .duration(20).ailment(AilmentSpec.NONE).tags("ignited_ground").build();
    public static final StatusType ON_IGNITED_GROUND = StatusType.register(ON_IGNITED_GROUND_DEF);

    public static final StatusTypeDef ON_SHOCKED_GROUND_DEF = StatusTypeDef.def("on_shocked_ground", StatusKind.GROUND)
            .mag(MagnitudeMeaning.NONE).tick("none").stat("none")
            .duration(20).ailment(AilmentSpec.NONE).tags("shocked_ground").build();
    public static final StatusType ON_SHOCKED_GROUND = StatusType.register(ON_SHOCKED_GROUND_DEF);

    public static final StatusTypeDef ON_CHILLED_GROUND_DEF = StatusTypeDef.def("on_chilled_ground", StatusKind.GROUND)
            .mag(MagnitudeMeaning.NONE).tick("none").stat("none")
            .duration(20).ailment(AilmentSpec.NONE).tags("chilled_ground").build();
    public static final StatusType ON_CHILLED_GROUND = StatusType.register(ON_CHILLED_GROUND_DEF);

    // ───────────────────────────── Curse（诅咒：技能即状态，静态预注册） ─────────────────────────────

    /**
     * 诅咒缺省时长兜底（与 {@code SkillInstance#DEFAULT_CURSE_DURATION} 同口径）。
     * 运行时 {@link StatusEngine#applyCurseToNearby} 恒显式传 {@code durationTicks}，故此值仅作兜底；
     * 且 {@link StatusType#dynamic} 幂等（命中既有实例即返回），预注册不改变运行时行为。
     */
    private static final int CURSE_DEFAULT_DURATION = 100;

    /**
     * 静态预注册一个诅咒状态：id = {@code <MODID>:<path>}，与 {@link StatusType#dynamic} 收到的
     * {@code skillId.toString()}（{@code GemGen.id(path).toString()}）<b>逐字符一致</b>。
     *
     * <p>目的：诅咒本由技能运行时 {@code dynamic} 注册（无法静态枚举），但「免疫某诅咒」这类
     * 按 {@code StatusType.id} 身份匹配的效果（{@code ImmuneStatEffect} → {@link StatusEngine#apply}
     * → {@code StatEngine#isImmune}）需要在注册表中能解析到它，故在此按技能 path 预注册。
     * 构造与 {@code StatusType#dynamic} 的 CURSE 分支语义等价：{@code kind=CURSE}、
     * {@code tick("curseTick")}、{@code stat(StatusBehavior.curseStatProviderId(id))}。
     */
    private static StatusType curse(String path) {
        String id = CraftOfExile.MODID + ":" + path;
        return StatusType.register(StatusTypeDef.def(id, StatusKind.CURSE)
                .duration(CURSE_DEFAULT_DURATION)
                .tick("curseTick").stat(StatusBehavior.curseStatProviderId(id))
                .build());
    }

    public static final StatusType FLAMMABILITY_CURSE = curse("blue/flammability");
    public static final StatusType CONDUCTIVITY_CURSE = curse("blue/conductivity");
    public static final StatusType HYPOTHERMIA_CURSE = curse("blue/hypothermia");
    public static final StatusType ELEMENTAL_WEAKNESS_CURSE = curse("blue/elemental_weakness");
    public static final StatusType DESPAIR_CURSE = curse("blue/despair");
    public static final StatusType ENFEEBLE_CURSE = curse("blue/enfeeble");
    public static final StatusType VULNERABILITY_CURSE = curse("blue/vulnerability");
    public static final StatusType TEMPORAL_CHAINS_CURSE = curse("blue/temporal_chains");

    // ───────────────────────────── Mark（标记：固定 id，原经 dynamic 注册，现静态声明） ─────────────────────────────

    /** Mark 状态分类标签（供 Stat 作用域构建；运行时由 MarkHelper 施加到目标 StatusStore）。 */
    public static final String MARK_TAG = "mark";

    public static final StatusTypeDef MARK_DEF = StatusTypeDef.def("mark", StatusKind.MARK)
            .mag(MagnitudeMeaning.NONE).tick("none").stat("none")
            .duration(80).ailment(AilmentSpec.NONE).tags(MARK_TAG).build();
    public static final StatusType MARK = StatusType.register(MARK_DEF);

    private StatusDefs() {}
}
