package com.cimdy.coe.data;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.common.BooleanAttribute;
import net.neoforged.neoforge.common.PercentageAttribute;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public class AttributeRegister {
    /**
     * ===== 属性注册层（scope 体系 + 三定律 + 图例）=====
     *
     * 【读取机制】getAttributeValue / readInScopePublic → LivingAttributeValue mixin →
     * StatEngine.fold 按当前作用域（ExpressionContext 栈顶 StatContext）即时聚合 Stat 贡献。
     * 作用域是「栈」：同一时刻仅栈顶生效；嵌套（引导→命中）内层 merge 外层 tags 入栈，避免遮蔽。
     * 取特定场景值 = 先 StatScopes.enter(实体, 对应标签作用域) 再读，退出后版本失效回落。
     *
     * 【三定律】（属性层归一纪律，逐条封死轮回）
     * 1. scope 通道：StatGen 带 scope 标签的条目，消费端必须 StatScopes.enter(scope) 读取，
     *    否则 EMPTY 下恒不命中 → 读注册默认值（等价绕过 Stat）。
     * 2. 触发型走 fire：一次性资源/状态/伤害/几率授予必须走 StatEffect.fire，
     *    禁「裸属性 + 硬编码消费端」（本类越界见 GLORY_GENERATION / PUPPET_MASTER_CHANCE / REMNANT_*）。
     * 3. 零基禁 inc：base=0 属性只能 pctAdd(ADD_VALUE)/ADD_MULTIPLIED_TOTAL，绝不可 inc
     *    （fold 式 ADD_MULTIPLY_BASE ≡ base*ΣmultBase ≡ 0 静默死，见 [零基] 标注属性）。
     *
     * 【图例】每行备注格式： [tag] 用途 / 作用域 / 状态
     *   [核心]   战斗/系统核心属性，消费端直接读取（Helper / getAttributeValue）
     *   [fold]   读时折叠数值，StatGen 经 ModifierStatEffect 接线，scope fold 聚合
     *   [legacy] 原版 AttributeModifier 直挂（合法，非 Stat 数据驱动）
     *   [reserved] 预留/规划，暂无消费端
     *   [待删]   过时设计，待词缀 Stat 化后移除（勿新接）
     *   [数据先行] 数据已落地，运行时消费端待接（reserved-empty，不删常量）
     *   [悬空]   当前无消费端（注释声称的读取不实），待裁定
     *   [零基]   base=0，仅可 pctAdd/ADD_VALUE，禁 inc
     *   [scope X] 须 StatScopes.enter(X) 后读取，否则 EMPTY 下恒读默认
     *
     * 拆分维度：事件（hit/kill/crit…）与资源（health/mana/energy…）正交；资源按量/基数/指定倍率拆，
     * 全局乘数归一单属性；承受侧统一走 *TAKEN* + {taken} 作用域，不建独立 _TAKEN 属性。
     */
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, CraftOfExile.MODID);

    public static List<Holder<Attribute>> ALL_ATTRIBUTE = new ArrayList<>();
    // ===== 工作速度 / 工作适应度（Stat 词缀系统介入点） =====
    /** [legacy] 全局工作速度倍率；WorkSpeed 直接读取（EntityAttributeModificationEvent 挂载全体） */
    public static Holder<Attribute> WORK_SPEED = registerDouble("work_speed", 1.0);
    /** [legacy] 全工种适应度加成；WorkSpeed 直接读取 */
    public static Holder<Attribute> WORK_APTITUDE = registerDouble("work_aptitude", 0.0);
    /** [reserved] 天赋：自动熔炼等级（预留，无消费端） */
    public static Holder<Attribute> TALENT_AUTO_SMELT = registerDouble("talent_auto_smelt", 0, 0);
    /** [reserved] 天赋：幸运（预留，无消费端） */
    public static Holder<Attribute> TALENT_FORTUNE = registerDouble("talent_fortune", 0, 0);
    /** [reserved] 天赋：掠夺（预留，无消费端） */
    public static Holder<Attribute> TALENT_LOOTING = registerDouble("talent_looting", 0, 0);
    /** [reserved] 天赋：修复（预留，无消费端） */
    public static Holder<Attribute> TALENT_MENDING = registerDouble("talent_mending", 0, 0);
    /** [reserved] 天赋：额外经验（预留，无消费端） */
    public static Holder<Attribute> TALENT_EXTRA_XP = registerDouble("talent_extra_xp", 0, 0);
    /** [reserved] 天赋：磁吸（预留，无消费端） */
    public static Holder<Attribute> TALENT_MAGNET = registerDouble("talent_magnet", 0, 0);
    /** [核心] 技能槽数量上限；SkillScreen 读取 */
    public static Holder<Attribute> SKILL_SLOT_COUNT = registerDouble("skill_slot_count", 9, 0, 10);
    /** [数据先行·待核] 战旗离开后增益残留秒数；消费端待接 */
    public static Holder<Attribute> BANNER_LINGER = registerDouble("banner_linger", 0, 0);
    /** [零基·禁inc] 战吼强化额外攻击次数；StatGen 以 inc 接 base=0 静默死，须改 pctAdd */
    public static Holder<Attribute> ADD_EMPOWERED_ATTACKS = registerDouble("empowered_attacks", 0, 0);
    /** [数据先行·待核] 近期消耗尸体攻击增伤点数；消费端待接 */
    public static Holder<Attribute> RECENTLY_CONSUMED_DAMAGE = registerDouble("recently_consumed_damage", 0, 0);
    /** [fold·scope corpse] 消耗尸体不摧毁几率；ConsumeCorpseAction 读取 */
    public static Holder<Attribute> CORPSE_RETAIN_CHANCE = registerDouble("corpse_retain_chance", 0, 0);
    /** [核心] 物品容量；CapacityHelper 读取 */
    public static Holder<Attribute> ITEM_CAPACITY = registerDouble("item_capacity", 16, 0);
    /** [零基·禁inc] 掉落物稀有度；StatGen 以 inc 接 base=0 静默死，须改 pctAdd */
    public static Holder<Attribute> FOUND_ITEM_RARITY = registerPercentage("found_item_rarity", 0);
    /** [零基·禁inc] 掉落物数量；StatGen 以 inc 接 base=0 静默死，须改 pctAdd */
    public static Holder<Attribute> FOUND_ITEM_NUMBER = registerPercentage("found_item_number", 0);
    /** [核心·scope attack] 命中准确率；命中判定读取（攻方，{attack} 作用域） */
    public static Holder<Attribute> ACCURACY = registerDouble("accuracy", 15, 0);
    /** [fold·scope ailment] 异常积蓄倍率；StatGen 接线 */
    public static Holder<Attribute> AILMENT_BUILDUP = registerPercentage("ailment_buildup", 1.0);
    /** [fold·scope] 范围效果倍率；技能投送范围读取 */
    public static Holder<Attribute> AREA_OF_EFFECT = registerPercentage("area_of_effect", 1.0);
    /** [fold] 冷却回复速率倍率 */
    public static Holder<Attribute> COOLDOWN_RECOVERY_RATE = registerPercentage("cooldown_recovery_rate", 1.0);
    /** [fold·scope ailment] 异常触发几率；StatGen 接线 */
    public static Holder<Attribute> AILMENT_CHANCE = registerPercentage("ailment_chance", 0.0);
    /** [fold·scope ailment] 异常伤害贡献 */
    public static Holder<Attribute> AILMENT_DAMAGE_CONTRIBUTION = registerPercentage("ailment_damage_contribution", 0.0);
    /** [fold·scope ailment] 异常伤害速率 */
    public static Holder<Attribute> AILMENT_DAMAGE_RATE = registerPercentage("ailment_damage_rate", 1.0);
    /** [fold·scope ailment] 异常时长倍率（归一属性） */
    public static Holder<Attribute> AILMENT_DURATION = registerPercentage("ailment_duration", 1.0);
    /** [fold·scope ailment] 异常幅度倍率（归一属性，按异常名作用域） */
    public static Holder<Attribute> AILMENT_MAGNITUDE = registerPercentage("ailment_magnitude", 1.0);
    /** [fold·scope ailment,threshold] 异常门槛倍率（归一属性） */
    public static Holder<Attribute> AILMENT_THRESHOLD = registerDouble("ailment_threshold", 0.0);
    /** [fold·scope ailment,threshold] 异常门槛加性（归一属性，所有异常共有） */
    public static Holder<Attribute> AILMENT_THRESHOLD_ADD = registerDouble("ailment_threshold_add", 0.0);
    /** [核心] 异常门槛适应倍率；StatusEngine:470 读取 */
    public static Holder<Attribute> AILMENT_THRESHOLD_ADAPTATION = registerPercentage("ailment_threshold_adaptation", 1.0);
    /** [fold·scope resource] 偷取量倍率（按资源作用域） */
    public static Holder<Attribute> AMOUNT_LEECHED = registerPercentage("amount_leeched", 1.0);
    /** [fold·scope ailment] 异常回避几率 */
    public static Holder<Attribute> AVOID_AILMENT_CHANCE = registerPercentage("avoid_ailment_chance", 0);
    /** [fold·scope block] 格挡几率（max 0.75） */
    public static Holder<Attribute> BLOCK_CHANCE = registerPercentage("block_chance", 0, 0, 0.75);
    /** [核心] 施法速度倍率（与 ATTACK_SPEED 正交） */
    public static Holder<Attribute> CAST_SPEED = registerDouble("cast_speed", 1, 0, 1024);
    /** [fold] 护甲破坏倍率 */
    public static Holder<Attribute> ARMOR_BREAK = registerPercentage("armor_break", 0.0);
    /** [fold·scope crossbow] 弩装填时间倍率 */
    public static Holder<Attribute> CROSS_BOW_RELOAD_TIME = registerPercentage("cross_bow_reload_time", 1.0);
    /** [fold·scope crit] 暴击几率 */
    public static Holder<Attribute> CRIT_CHANCE = registerPercentage("crit_chance", 1.0);
    /** [fold·scope crit] 暴击伤害倍率 */
    public static Holder<Attribute> CRIT_DAMAGE = registerPercentage("crit_damage", 1.0);
    /** [fold·scope] 伤害倍率（归一属性，按 scope 区分 attack/spell/...） */
    public static Holder<Attribute> DAMAGE = registerPercentage("damage", 1.0);
    /** [fold] 持续伤害倍率 */
    public static Holder<Attribute> DAMAGE_OVER_TIME_MULTIPLIER = registerPercentage("damage_over_time_multiplier", 1.0);
    /** [零基·禁inc] 敏捷（属性点）；base=0，仅 valueMod/pctAdd 安全 */
    public static Holder<Attribute> DEXTERITY = registerDouble("dexterity", 0, 0);
    /** [fold] 能量（施法资源）回复速率倍率 */
    public static Holder<Attribute> ENERGY_RECHARGE = registerPercentage("energy_recharge", 1.0);
    /** [fold·scope] 受伤转魔力比例 */
    public static Holder<Attribute> MANA_TAKE_OF_DAMAGE = registerPercentage("mana_take_of_damage", 0.0);
    /** [核心] 闪避值 */
    public static Holder<Attribute> EVASION = registerDouble("evasion", 53, 0);
    /** [零基·禁inc] 智力（属性点）；StatGen 以 inc(INC_INTELLIGENCE_2) 接 base=0 静默死，须改 pctAdd */
    public static Holder<Attribute> INTELLIGENCE = registerDouble("intelligence", 0, 0);
    /** [fold·scope projectile] 额外投射物数量 */
    public static Holder<Attribute> LAUNCH_ADDITIONAL_PROJECTILE = registerDouble("launch_additional_projectile", 0);
    /** [fold·scope resource] 偷取率（按资源作用域） */
    public static Holder<Attribute> LEECH = registerPercentage("leech", 0);
    /** [核心] 偷取速率倍率（归一）；DamageHelper:295 读取 */
    public static Holder<Attribute> LEECH_RATE = registerPercentage("leech_rate", 1.0);
    /** [fold·scope resource] 偷取响应（按资源作用域） */
    public static Holder<Attribute> LEECHES_RESPONSE = registerPercentage("leeches_response", 0.20);
    /** [核心] 最大耐力球数 */
    public static Holder<Attribute> MAX_ENDURANCE_CHARGES = registerDouble("max_endurance_charges", 3);
    /** [核心] 元技能能量上限（base 100） */
    public static Holder<Attribute> META_ENERGY_MAX = registerDouble("meta_energy_max", 100, 0);
    /** [核心] 最大能量护盾（ES）；StateCalculator 读取，承伤吸收层消费 */
    public static Holder<Attribute> MAX_ENERGY_SHIELD = registerDouble("max_energy_shield", 0, 0);
    /** [核心] 最大狂怒球 */
    public static Holder<Attribute> MAX_FRENZY_CHARGES = registerDouble("max_frenzy_charges", 3);
    /** [核心] 最大魔力 */
    public static Holder<Attribute> MAX_MANA = registerDouble("max_mana", 40, 0);
    /** [核心] 最大能量球 */
    public static Holder<Attribute> MAX_POWER_CHARGES = registerDouble("max_power_charges", 3);
    /** [核心] 最大怒气 */
    public static Holder<Attribute> MAX_RAGE = registerDouble("max_rage", 50);
    /** [核心] 怒气固有流失速率倍率；RageHelper 读取 */
    public static Holder<Attribute> RAGE_LOSS_RATE = registerPercentage("rage_loss_rate", 1.0);
    /** [核心] 怒气固有流失宽限期（tick）；RageHelper 读取 */
    public static Holder<Attribute> RAGE_LOSS_GRACE = registerDouble("rage_loss_grace", 0, 0, 4096);
    /** [fold] 最大再生率 */
    public static Holder<Attribute> MAX_REGENERATION = registerDouble("max_regeneration", 0.05);
    /** [核心] 最大抗性上限（0.75） */
    public static Holder<Attribute> MAX_RESISTANCES = registerPercentage("max_resistances", 0.75);
    /** [核心] 最大精魂 */
    public static Holder<Attribute> MAX_SPIRIT = registerDouble("max_spirit", 0, 0);
    /** [fold·scope parry] 招架范围倍率 */
    public static Holder<Attribute> PARRY_RANGE = registerPercentage("parry_range", 1.0);
    /** [核心·scope presence] 光环/威势范围；EntityOperationEvent 读取 */
    public static Holder<Attribute> PRESENCE_RANGE = registerDouble("presence_range", 6.0, 0);
    /** [fold·scope projectile] 投射物穿透次数 */
    public static Holder<Attribute> PROJECTILE_PIERCE = registerDouble("projectile_pierce", 0);
    /** [fold·scope projectile] 投射物穿透几率 */
    public static Holder<Attribute> PROJECTILE_PIERCE_CHANCE = registerPercentage("projectile_pierce_chance", 0);
    /** [fold·scope projectile] 投射物随机发射（布尔） */
    public static Holder<Attribute> PROJECTILE_RANDOM_FIRE = registerBoolean("projectile_random_fire", false);
    /** [fold·scope projectile] 投射物速度倍率 */
    public static Holder<Attribute> PROJECTILE_SPEED = registerPercentage("projectile_speed", 1.0);
    /** [fold] 跨来源恢复率倍率（Regen/Leech/Flask/Recoup 同乘） */
    public static Holder<Attribute> RECOVERY_RATE = registerPercentage("recovery_rate", 1.0);
    /** [fold·scope flask] Flask 回复量倍率 */
    public static Holder<Attribute> FLASK_RECOVERY_RATE = registerPercentage("flask_recovery_rate", 1.0);
    /** [fold·scope flask] Flask 充能获得量倍率 */
    public static Holder<Attribute> FLASK_CHARGES_GAINED = registerPercentage("flask_charges_gained", 1.0);
    /** [fold·scope flask] Flask 不消耗充能几率 */
    public static Holder<Attribute> FLASK_NO_CONSUME_CHANCE = registerPercentage("flask_no_consume_chance", 0.0);

    // ───────── Charm（护符，缺口 F 后半：物品机制本体）─────────
    /** [fold·scope charm] 护符充能获得量倍率 */
    public static Holder<Attribute> CHARM_CHARGES_GAINED = registerPercentage("charm_charges_gained", 1.0);
    /** [fold·scope charm] 护符连锁触发几率 */
    public static Holder<Attribute> CHARM_ACTIVATION_CHANCE = registerPercentage("charm_activation_chance", 0.0);
    /** [fold·scope charm] 护符充能消耗倍率（<1 减耗） */
    public static Holder<Attribute> CHARM_CHARGES_USED = registerPercentage("charm_charges_used", 1.0);
    /** [fold·scope charm] 护符效果时长倍率 */
    public static Holder<Attribute> CHARM_DURATION = registerPercentage("charm_duration", 1.0);
    /** [fold·scope charm] 护符效果强度倍率 */
    public static Holder<Attribute> CHARM_EFFECT = registerPercentage("charm_effect", 1.0);

    // ───────── 效果倍率族（Wave-9 数据先行：条目先建、消费端后补）─────────
    /** [fold·scope aura] 光环幅度倍率（归一属性） */
    public static Holder<Attribute> AURA_MAGNITUDE = registerPercentage("aura_magnitude", 1.0);
    /** [核心·scope darkness] 最大黑暗值；DarknessHelper 读取 */
    public static Holder<Attribute> MAX_DARKNESS = registerPercentage("max_darkness", 1.0);
    /** [核心] ES 基础回复倍率；EnergyShieldHelper 读取 */
    public static Holder<Attribute> BASE_ENERGY_SHIELD_RECHARGE = registerPercentage("base_energy_shield_recharge", 1.0);
    /** [核心] ES 当前回复倍率；EnergyShieldHelper 读取 */
    public static Holder<Attribute> CURRENT_ENERGY_SHIELD_RECHARGE = registerPercentage("current_energy_shield_recharge", 1.0);
    /** [fold] 承伤绕过 ES 比例 */
    public static Holder<Attribute> DAMAGE_TAKEN_BYPASSES_ENERGY = registerPercentage("damage_taken_bypasses_energy", 1.0);
    /** [核心·scope darkness] 黑暗保留持续；DarknessHelper 读取 */
    public static Holder<Attribute> DARKNESS_RESERVATION_LASTS = registerPercentage("darkness_reservation_lasts", 1.0);
    /** [数据先行·待核] 护甲招架几率；当前无消费者（{crit} 作用域） */
    public static Holder<Attribute> DEFEND_ARMOUR = registerPercentage("defend_armour", 0.0);
    /** [fold·scope bleeding] 元素伤害贡献流血比例 */
    public static Holder<Attribute> ELEMENTAL_DAMAGE_CONTRIBUTES_BLEEDING = registerPercentage("elemental_damage_contributes_bleeding", 1.0);
    /** [核心] 荣耀资源（ResourceType.GLORY） */
    public static Holder<Attribute> GLORY = registerPercentage("glory", 0.0);
    /** [fold] 禁用固有魔力再生 */
    public static Holder<Attribute> NO_INHERENT_MANA_REGENERATION = registerPercentage("no_inherent_mana_regeneration", 1.0);
    /** [核心·scope darkness] 无精魂黑暗基底；DarknessHelper/ManaHelper 读取 */
    public static Holder<Attribute> NO_SPIRIT_BASE_DARKNESS = registerPercentage("no_spirit_base_darkness", 1.0);
    /** [fold] 魔力再生等于生命比例 */
    public static Holder<Attribute> REGENERATE_MANA_EQUAL_LIFE = registerPercentage("regenerate_mana_equal_life", 1.0);
    /** [fold·scope remnant] 残迹收集距离倍率 */
    public static Holder<Attribute> REMNANTS_COLLECTED_FURTHER_AWAY = registerPercentage("remnants_collected_further_away", 1.0);
    /** [核心·scope volatility] 波动免伤；VolatilityDetonationManager 读取 */
    public static Holder<Attribute> TAKE_NO_DAMAGE_VOLATILITY = registerPercentage("take_no_damage_volatility", 1.0);
    /** [fold·scope deflect] 招架判定强度（格挡判定读取） */
    public static Holder<Attribute> DEFLECTION_RATING = registerPercentage("deflection_rating", 1.0);
    /** [fold·scope dodge] 闪避几率（格挡判定读取） */
    public static Holder<Attribute> DODGE_CHANCE = registerPercentage("dodge_chance", 0.0);
    /** [核心] 免死几率（corruption_endures keystone）；DamagePipeline:552 读取 */
    public static Holder<Attribute> CORRUPTION_ENDURES_CHANCE = registerPercentage("corruption_endures_chance", 0.0);
    /** [fold·scope guard] Guard 护盾回复量 */
    public static Holder<Attribute> GUARD_RECOVERY = registerPercentage("guard_recovery", 0.0);
    /** [fold·scope archon] Archon 恢复速率倍率 */
    public static Holder<Attribute> ARCHON_RECOVERY_RATE = registerPercentage("archon_recovery_rate", 1.0);

    // ===== 增益幅度 / 承益效果 / 荣耀计量（词缀 Stat 系统介入点） =====
    /** [悬空·待裁定] 荣耀生成速率；当前无消费端（注释称 Banner 读取不实），待接 fire 通道或删 */
    public static Holder<Attribute> GLORY_GENERATION = registerPercentage("glory_generation", 1.0);

    // ===== 波动 / 元素灌入 资源（组B 资源系统：词缀 Stat 介入点） =====
    /** [核心·scope volatility] 波动上限；VolatilityHelper 读取 */
    public static Holder<Attribute> MAX_VOLATILITY = registerDouble("max_volatility", 100.0);
    /** [fold·scope volatility] 波动获取速率倍率 */
    public static Holder<Attribute> VOLATILITY_GENERATION = registerPercentage("volatility_generation", 1.0);
    /** [fold·scope volatility] 波动爆炸延迟（秒） */
    public static Holder<Attribute> VOLATILITY_DETONATION_TIME = registerDouble("volatility_detonation_time", 5.0);
    /** [核心·scope infusion] 元素灌入层数上限；InfusionHelper 读取 */
    public static Holder<Attribute> MAX_INFUSION = registerDouble("max_infusion", 1.0);
    /** [fold·scope aura] 保留效率（降低精魂保留量） */
    public static Holder<Attribute> RESERVATION_EFFICIENCY = registerPercentage("reservation_efficiency", 0.0);
    /** [fold] 荆棘反伤倍率（受击方） */
    public static Holder<Attribute> THORNS = registerPercentage("thorns", 1.0);
    /** [fold·scope stun] 眩晕恢复速率倍率 */
    public static Holder<Attribute> STUN_RECOVERY = registerPercentage("stun_recovery", 1.0);
    /** [fold] 击退距离倍率 */
    public static Holder<Attribute> KNOCKBACK = registerPercentage("knockback", 1.0);
    /** [fold] 斩杀阈值倍率 */
    public static Holder<Attribute> CULLING_STRIKE_THRESHOLD = registerPercentage("culling_strike_threshold", 1.0);
    /** [fold] 再生速率倍率 */
        public static Holder<Attribute> REGENERATION_RATE = registerDouble("regeneration_rate", 1);
    /** [fold·scope element] 抗性（归一属性，按元素作用域读取） */
    public static Holder<Attribute> RESISTANCES = registerPercentage("resistances", 0.0);
    /** [fold·scope element] 抗性穿透（攻击方，全局乘数） */
    public static Holder<Attribute> RESISTANCE_PENETRATION = registerPercentage("resistance_penetration", 0.0);
    /** [fold·scope status] 状态额外层数 */
    public static Holder<Attribute> STATUS_EXTRA_STACKS = registerPercentage("status_extra_stacks", 0.0);
    /** [fold·scope status] 状态最大层数 */
    public static Holder<Attribute> STATUS_MAX_STACKS = registerPercentage("status_max_stacks", 0.0);
    /** [核心·scope remnant] 额外残迹几率；RemnantState 读取 */
    public static Holder<Attribute> REMNANT_EXTRA_CHANCE = registerPercentage("remnant_extra_chance", 0.0);
    /** [核心·scope remnant] 残迹双倍几率；RemnantState:53 读取 */
    public static Holder<Attribute> REMNANT_DOUBLE_CHANCE = registerPercentage("remnant_double_chance", 0.0);
    /** [核心·scope remnant] 残迹幅度；RemnantState:54 读取 */
    public static Holder<Attribute> REMNANT_MAGNITUDE = registerDouble("remnant_magnitude", 1.0);
    /** [fold·scope slam] 余震几率（归一 {slam}/{mace,slam}/{form,slam}）；AreaDeliveryExecutor 读取 */
    public static Holder<Attribute> AFTERSHOCK_CHANCE = registerPercentage("aftershock_chance", 0.0);
    /** [fold·scope slam] 余震范围效果倍率 */
    public static Holder<Attribute> AFTERSHOCK_AREA = registerPercentage("aftershock_area", 1.0);
    /** [核心·待归一] 傀儡师获得层数几率；SkillEvent 读取（应走 fire 通道 grant_on_event） */
    public static Holder<Attribute> PUPPET_MASTER_CHANCE = registerPercentage("puppet_master_chance", 0.0);
    /** [fold·scope puppet] 傀儡师效果强度（零基累积） */
    public static Holder<Attribute> PUPPET_MASTER_EFFECT = registerPercentage("puppet_master_effect", 0.0);
    /** [fold·scope puppet] 傀儡师持续（零基累积） */
    public static Holder<Attribute> PUPPET_MASTER_DURATION = registerPercentage("puppet_master_duration", 0.0);
    /** [零基·禁inc] 力量（属性点）；base=0，仅 valueMod/pctAdd 安全 */
    public static Holder<Attribute> STRENGTH = registerDouble("strength", 0, 0);
    /** [fold·scope taken,crit] 承暴击伤害倍率 */
    public static Holder<Attribute> TAKEN_CRIT_DAMAGE = registerPercentage("taken_crit_damage", 1.0);
    /** [fold·scope taken,crit] 被暴击几率（受害者侧） */
    public static Holder<Attribute> TAKEN_CRIT_CHANCE = registerPercentage("taken_crit_chance", 0.0);
    /** [fold·scope taken] 承伤倍率 */
    public static Holder<Attribute> TAKEN_DAMAGE = registerPercentage("taken_damage", 1.0);
    /** [fold·scope totem,taken] 图腾承伤光环（攻击方读取附近图腾数） */
    public static Holder<Attribute> TOTEM_TAKEN_AURA = registerPercentage("totem_taken_aura", 0.0);
    /** [零基·禁inc] 附加伤害（按元素 valueMod 接）；base=0，禁 inc */
    public static Holder<Attribute> ADDED_DAMAGE = registerDouble("added_damage", 0);

// ===== AUTO-GEN POE2 TALENT ATTRIBUTES END =====

    public static Holder<Attribute> registerDouble(String name) {
        Holder<Attribute> attributeHolder = ATTRIBUTES.register(name, () ->
                new RangedAttribute("attribute.name." + name, 0.0, -999999.0, 999999.0).setSyncable(true));
        ALL_ATTRIBUTE.add(attributeHolder);
        return attributeHolder;
    }

    public static Holder<Attribute> registerDouble(String name, double defaultValue) {
        Holder<Attribute> attributeHolder = ATTRIBUTES.register(name, () ->
                new RangedAttribute("attribute.name." + name, defaultValue, -999999.0, 999999.0).setSyncable(true));
        ALL_ATTRIBUTE.add(attributeHolder);
        return attributeHolder;
    }

    public static Holder<Attribute> registerDouble(String name, double defaultValue, double min) {
        Holder<Attribute> attributeHolder = ATTRIBUTES.register(name, () ->
                new RangedAttribute("attribute.name." + name, defaultValue, min, 999999.0).setSyncable(true));
        ALL_ATTRIBUTE.add(attributeHolder);
        return attributeHolder;
    }

    public static Holder<Attribute> registerDouble(String name, double defaultValue, int min, int max) {
        Holder<Attribute> attributeHolder = ATTRIBUTES.register(name, () ->
                new RangedAttribute("attribute.name." + name, defaultValue, min, max).setSyncable(true));
        ALL_ATTRIBUTE.add(attributeHolder);
        return attributeHolder;
    }

    public static Holder<Attribute> registerBoolean(String name) {
        Holder<Attribute> attributeHolder = ATTRIBUTES.register(name, () ->
                new BooleanAttribute("attribute.name." + name, false).setSyncable(true));
        ALL_ATTRIBUTE.add(attributeHolder);
        return attributeHolder;
    }

    public static Holder<Attribute> registerBoolean(String name, boolean defaultValue) {
        Holder<Attribute> attributeHolder = ATTRIBUTES.register(name, () ->
                new BooleanAttribute("attribute.name." + name, defaultValue).setSyncable(true));
        ALL_ATTRIBUTE.add(attributeHolder);
        return attributeHolder;
    }

    public static Holder<Attribute> registerPercentage(String name) {
        Holder<Attribute> attributeHolder = ATTRIBUTES.register(name, () ->
                new PercentageAttribute("attribute.name." + name, 0.0, -999999.0, 999999.0).setSyncable(true));
        ALL_ATTRIBUTE.add(attributeHolder);
        return attributeHolder;
    }

    public static Holder<Attribute> registerPercentage(String name, double defaultValue) {
        Holder<Attribute> attributeHolder = ATTRIBUTES.register(name, () ->
                new PercentageAttribute("attribute.name." + name, defaultValue, -999999.0, 999999.0).setSyncable(true));
        ALL_ATTRIBUTE.add(attributeHolder);
        return attributeHolder;
    }

    public static Holder<Attribute> registerPercentage(String name, double defaultValue, double min) {
        Holder<Attribute> attributeHolder = ATTRIBUTES.register(name, () ->
                new PercentageAttribute("attribute.name." + name, defaultValue, min, 999999.0).setSyncable(true));
        ALL_ATTRIBUTE.add(attributeHolder);
        return attributeHolder;
    }

    public static Holder<Attribute> registerPercentage(String name, double defaultValue, double min, double max) {
        Holder<Attribute> attributeHolder = ATTRIBUTES.register(name, () ->
                new PercentageAttribute("attribute.name." + name, defaultValue, min, max).setSyncable(true));
        ALL_ATTRIBUTE.add(attributeHolder);
        return attributeHolder;
    }
}

