package com.cimdy.coe.data.gen.lang;
import com.cimdy.coe.registry.AttributeRegistry;

/**
 * {@link com.cimdy.coe.registry.AttributeRegistry} 自有属性的中文名（{@code attribute.name.*}）。
 *
 * <p>每个属性由 {@code AttributeRegistry} 以 {@code new RangedAttribute("attribute.name." + name, ...)}
 * 注册，{@link net.minecraft.world.entity.ai.attributes.Attribute#getDescriptionId()} 即
 * {@code attribute.name.<注册名>}。本类为这些属性注册中文名（文案取自 loc_copy_table.md §C），
 * 避免中文环境下回落英文。
 *
 * <p>一律走 {@link LangSupport#addZh}：zh_cn 写中文，en_us 写 {@code englishFor(key)}
 * （{@link LangSupport} 的 {@code STAT_EN} 显式覆盖优先，否则 humanize 注册名尾段）。
 */
public final class AttributeLang {
    private AttributeLang() {
    }

    static void install(LangSupport L) {
        addResourceCaps(L);
        addResourceMechanics(L);
        addAilmentDimensions(L);
        addEffectDimensions(L);
        addCoreCombat(L);
        addTakenSide(L);
        addResistAndStatus(L);
        addLeechAndRecovery(L);
        addProjectile(L);
        addReservedAndPending(L);
    }

    /** §B 资源上限（27 条）。 */
    private static void addResourceCaps(LangSupport L) {
        L.addZh("attribute.name.work_aptitude", "工作适应度");
        L.addZh("attribute.name.work_speed", "工作速度");
        L.addZh("attribute.name.max_darkness", "最大黑暗值");
        L.addZh("attribute.name.max_endurance_charges", "最大耐力球数");
        L.addZh("attribute.name.max_energy_shield", "最大能量护盾");
        L.addZh("attribute.name.max_frenzy_charges", "最大狂怒球数");
        L.addZh("attribute.name.max_banners", "战旗上限");
        L.addZh("attribute.name.max_curses", "诅咒上限");
        L.addZh("attribute.name.max_totems", "图腾上限");
        L.addZh("attribute.name.max_orb", "法球技能上限");
        L.addZh("attribute.name.max_fissure", "裂隙上限");
        L.addZh("attribute.name.max_companions", "同伴上限");
        L.addZh("attribute.name.minion_limit", "召唤物上限");
        L.addZh("attribute.name.minion_duration", "召唤物持续时间");
        L.addZh("attribute.name.stun_threshold_from_es", "以护盾计算眩晕门槛");
        L.addZh("attribute.name.max_infusion", "元素灌入上限");
        L.addZh("attribute.name.max_total_elemental_infusions", "元素灌入总上限");
        L.addZh("attribute.name.archmage_lightning_gain", "大法师闪电伤害转化");
        L.addZh("attribute.name.max_power_charges", "最大暴击球数");
        L.addZh("attribute.name.max_rage", "最大怒气");
        L.addZh("attribute.name.max_regeneration", "最大再生率");
        L.addZh("attribute.name.max_resistances", "最大抗性上限");
        L.addZh("attribute.name.max_spirit", "最大精魂");
        L.addZh("attribute.name.max_volatility", "最大波动");
        L.addZh("attribute.name.skill_level_bonus", "技能等级加成");
        L.addZh("attribute.name.meta_energy_max", "元技能能量上限");
    }

    /** §C 资源机制（26 条）。 */
    private static void addResourceMechanics(LangSupport L) {
        L.addZh("attribute.name.base_energy_shield_recharge", "能量护盾基础回复率");
        L.addZh("attribute.name.current_energy_shield_recharge", "能量护盾当前回复率");
        L.addZh("attribute.name.darkness_reservation_lasts", "黑暗保留持续");
        L.addZh("attribute.name.energy_recharge", "能量回复速率");
        L.addZh("attribute.name.curse_target_es_recharge_disabled", "诅咒目标禁用护盾充能");
        L.addZh("attribute.name.deflected_cannot_inflict_bleeding", "偏转命中不造成流血");
        L.addZh("attribute.name.deflected_cannot_inflict_maim", "偏转命中不造成致残");
        L.addZh("attribute.name.add_a_curse_after_channelling_for_seconds", "引导后施加诅咒的秒数");
        L.addZh("attribute.name.curse_target_health_regen_mult", "诅咒目标生命再生倍率");
        L.addZh("attribute.name.glory_consume_skip_chance", "不消耗荣耀几率");
        L.addZh("attribute.name.glory_generation", "荣耀生成速率");
        L.addZh("attribute.name.rage_loss_grace", "怒气流失宽限期");
        L.addZh("attribute.name.rage_loss_rate", "怒气流失速率");
        L.addZh("attribute.name.rage_regen_per_rage_spent", "每点消耗怒气的回复");
        L.addZh("attribute.name.rage_regen_per_second", "每秒怒气回复");
        L.addZh("attribute.name.reservation_efficiency", "保留效率");
        L.addZh("attribute.name.mana_cost_efficiency", "魔力消耗效率");
        L.addZh("attribute.name.health_cost_efficiency", "生命消耗效率");
        L.addZh("attribute.name.invoked_spells_consume_more_energy", "被调用法术消耗更多能量");
        L.addZh("attribute.name.volatility_detonation_time", "波动爆炸延迟");
        L.addZh("attribute.name.volatility_generation", "波动获取速率");
    }

    /** §E 异常维度（11 条）。 */
    private static void addAilmentDimensions(LangSupport L) {
        L.addZh("attribute.name.ailment_buildup", "异常积蓄");
        L.addZh("attribute.name.ailment_chance", "异常几率");
        L.addZh("attribute.name.ailment_damage_contribution", "异常伤害贡献");
        L.addZh("attribute.name.ailment_damage_rate", "异常伤害速率");
        L.addZh("attribute.name.ailment_duration", "异常持续时间");
        L.addZh("attribute.name.ailment_magnitude", "异常强度");
        L.addZh("attribute.name.impale_magnitude", "穿刺强度");
        L.addZh("attribute.name.incision_taken_physical_per_incision", "每层创口额外物理承伤");
        L.addZh("attribute.name.ailment_threshold", "异常门槛");
        L.addZh("attribute.name.ailment_threshold_adaptation", "异常门槛适应");
        L.addZh("attribute.name.ailment_threshold_add", "异常门槛加值");
    }

    /** §F 效果维度（33 条）。 */
    private static void addEffectDimensions(LangSupport L) {
        L.addZh("attribute.name.aftershock_area", "余震范围");
        L.addZh("attribute.name.aftershock_chance", "余震几率");
        L.addZh("attribute.name.archon_recovery_rate", "执政官恢复速率");
        L.addZh("attribute.name.archon_recovery_period_mult", "执政官恢复周期倍率");
        L.addZh("attribute.name.buff_duration_mult", "增益持续时间倍率");
        L.addZh("attribute.name.debuff_duration_mult", "减益持续时间倍率");
        L.addZh("attribute.name.duration_mult", "持续时间倍率");
        L.addZh("attribute.name.aura_duration", "光环持续时间");
        L.addZh("attribute.name.banner_duration", "战旗持续时间");
        L.addZh("attribute.name.ground_duration", "地面效果持续时间");
        L.addZh("attribute.name.charge_duration", "蓄力效果持续时间");
        L.addZh("attribute.name.charm_duration", "护符持续时间");
        L.addZh("attribute.name.parry_duration", "格挡持续时间");
        L.addZh("attribute.name.archon_duration", "执政官持续时间");
        L.addZh("attribute.name.totem_duration", "图腾持续时间");
        L.addZh("attribute.name.mark_duration", "印记持续时间");
        L.addZh("attribute.name.offering_duration", "供奉持续时间");
        L.addZh("attribute.name.curse_duration", "诅咒持续时间");
        L.addZh("attribute.name.beast_duration", "野兽持续时间");
        L.addZh("attribute.name.stun_duration", "眩晕持续时间");
        L.addZh("attribute.name.daze_duration", "迷醉持续时间");
        L.addZh("attribute.name.debuff_duration", "减益持续时间");

        L.addZh("attribute.name.charm_activation_chance", "护符触发几率");
        L.addZh("attribute.name.charm_charges_gained", "护符充能获得");
        L.addZh("attribute.name.charm_charges_used", "护符充能消耗");
        L.addZh("attribute.name.charm_charge_rate", "护符每秒充能");
        L.addZh("attribute.name.charges_gained", "充能获得");
        L.addZh("attribute.name.charges_used", "充能消耗");
        L.addZh("attribute.name.charge_rate", "每秒充能");

        L.addZh("attribute.name.charm_effect", "护符效果");
        L.addZh("attribute.name.charm_slot", "护符槽位");
        L.addZh("attribute.name.guard_recovery", "守护回复");
        L.addZh("attribute.name.quiver_bonus_inc", "箭袋加成提高");
        L.addZh("attribute.name.puppet_master_duration", "傀儡师持续时间");
        L.addZh("attribute.name.puppet_master_effect", "傀儡师效果");
        L.addZh("attribute.name.remnants_collected_further_away", "残迹收集距离");
        L.addZh("attribute.name.remnant_double_chance", "残迹翻倍几率");
        L.addZh("attribute.name.remnant_extra_chance", "额外残迹几率");
        L.addZh("attribute.name.remnant_magnitude", "残迹强度");
        L.addZh("attribute.name.remnant_conversion", "残迹元素重映射");
        L.addZh("attribute.name.burning_explode_on_kill_chance", "击杀燃烧敌人爆炸几率");
        L.addZh("attribute.name.empowered_explode_on_kill_chance", "强化攻击击杀爆炸几率");
        L.addZh("attribute.name.additional_infusion_chance", "额外灌入几率");
        L.addZh("attribute.name.crossbow_immediate_reload_chance", "弩立即装填几率");
        L.addZh("attribute.name.charge_consume_skip_chance", "不消耗充能球几率");
        L.addZh("attribute.name.infusion_consume_skip_chance", "不消耗灌入几率");
        L.addZh("attribute.name.invoked_half_energy_chance", "被调用法术半耗能量几率");
        L.addZh("attribute.name.trigger_energy_refund_chance", "触发技能返还能量几率");
        L.addZh("attribute.name.minion_revive_speed", "召唤物复活速度");
    }

    /** §G 核心战斗（30 条）。 */
    private static void addCoreCombat(LangSupport L) {
        L.addZh("attribute.name.area_of_effect", "范围效果");
        L.addZh("attribute.name.echo_count", "回响次数");
        L.addZh("attribute.name.armor_break", "护甲破坏");
        L.addZh("attribute.name.armor_break_taken", "承受破甲");
        // 归一：armor_break_spell_crit_magnitude 已并入 armor_break（见 AttributeRegistry）
        L.addZh("attribute.name.maim_on_fully_break", "完全破甲致残");
        L.addZh("attribute.name.no_regen_on_fully_break", "完全破甲禁再生");
        L.addZh("attribute.name.no_regen_on_bleed", "流血禁再生");
        L.addZh("attribute.name.base_bleeding_duration", "流血基础持续时间");
        L.addZh("attribute.name.combo_slowdown", "连击减速");
        L.addZh("attribute.name.cooldown_recovery_rate", "冷却回复速率");
        L.addZh("attribute.name.crit_chance", "暴击几率");
        L.addZh("attribute.name.crit_damage", "暴击伤害");
        L.addZh("attribute.name.cross_bow_reload_time", "弩装填时间");
        L.addZh("attribute.name.damage", "伤害");
        L.addZh("attribute.name.damage_over_time_multiplier", "持续伤害倍率");
        L.addZh("attribute.name.deflection_rating", "招架强度");
        L.addZh("attribute.name.dodge_chance", "闪避几率");
        L.addZh("attribute.name.dodge_roll_cannot_avoid_damage", "翻滚无法免伤");
        L.addZh("attribute.name.rage_effect", "怒气效果");
        L.addZh("attribute.name.aura_buffs_exclude_allies", "光环不影响盟友");
        L.addZh("attribute.name.dodge_roll_distance", "翻滚距离");
        L.addZh("attribute.name.chance_to_avoid_death", "免死几率");
        L.addZh("attribute.name.parry_range", "招架范围");
        L.addZh("attribute.name.stun_recovery", "眩晕恢复速率");
    }

    /** §H 承伤侧（3 条）。 */
    private static void addTakenSide(LangSupport L) {
        L.addZh("attribute.name.damage_taken_bypasses_energy", "承伤绕过能量护盾");
        L.addZh("attribute.name.taken_crit_chance", "被暴击几率");
        L.addZh("attribute.name.taken_crit_damage", "承受暴击伤害");
    }

    /** §I 抗性与状态（7 条）。 */
    private static void addResistAndStatus(LangSupport L) {
        L.addZh("attribute.name.avoid_ailment_chance", "异常回避几率");
        L.addZh("attribute.name.resistances", "抗性");
        L.addZh("attribute.name.resistance_penetration", "抗性穿透");
        L.addZh("attribute.name.ignore_non_negative_elemental_resistance", "无视非负元素抗性");
        L.addZh("attribute.name.invert_elemental_resistance_chance", "元素抗性取反几率");
        L.addZh("attribute.name.status_extra_stacks", "状态额外层数");
        L.addZh("attribute.name.status_max_stacks", "状态最大层数");
    }

    /** §J 偷取与回复（17 条）。 */
    private static void addLeechAndRecovery(LangSupport L) {
        L.addZh("attribute.name.amount_leeched", "偷取量");
        L.addZh("attribute.name.leech_share", "同伴共享偷取");
        L.addZh("attribute.name.offering_damage_bonus", "献祭同伴伤害加成");
        L.addZh("attribute.name.totem_spirit_reservation", "每图腾保留精魂");
        L.addZh("attribute.name.flask_charges_gained", "药剂充能获得");
        L.addZh("attribute.name.flask_charge_rate", "药剂每秒充能");
        L.addZh("attribute.name.flask_charges_used", "药剂充能消耗");
        L.addZh("attribute.name.flask_no_consume_chance", "药剂不消耗充能几率");
        L.addZh("attribute.name.flask_recovery_rate", "药剂回复量");
        L.addZh("attribute.name.leech", "偷取");
        L.addZh("attribute.name.leeches_response", "偷取响应");
        L.addZh("attribute.name.leech_rate", "偷取速率");
        L.addZh("attribute.name.mana_take_of_damage", "受伤转移魔力");
        L.addZh("attribute.name.damage_take_share", "盟友承伤比例");
        L.addZh("attribute.name.attribute_requirements", "属性需求");
        L.addZh("attribute.name.recovery_rate", "恢复速率");
        L.addZh("attribute.name.regeneration_rate", "再生速率");
    }

    /** §K 投射物（11 条）。 */
    private static void addProjectile(LangSupport L) {
        L.addZh("attribute.name.moving_additional_projectile_chance", "移动时额外投射物几率");
        L.addZh("attribute.name.surpass_additional_projectile_chance", "超越额外投射物几率");
        L.addZh("attribute.name.mark_blinded", "标记致盲");
        L.addZh("attribute.name.curse_activation_delay_more", "诅咒激活延迟增加");
        L.addZh("attribute.name.curse_activation_frequency", "诅咒激活频率");
        L.addZh("attribute.name.projectile_pierce", "投射物穿透次数");
        L.addZh("attribute.name.projectile_pierce_chance", "投射物穿透几率");
        L.addZh("attribute.name.projectile_fork_chance", "投射物分裂几率");
        L.addZh("attribute.name.projectile_fork_extra_chance", "分裂时额外投射物几率");
        L.addZh("attribute.name.chain_chance", "额外连锁几率");
        
    }

    /** §L/M 天赋预留 · 数据先行（11 条）。 */
    private static void addReservedAndPending(LangSupport L) {
        L.addZh("attribute.name.empowered_attacks", "强化攻击次数");
        L.addZh("attribute.name.warcry_min_power", "战吼最低威力");
        L.addZh("attribute.name.flask_life_recovery_share", "药剂生命回复给同伴");
        L.addZh("attribute.name.banner_linger", "战旗残留时间");
        L.addZh("attribute.name.corpse_retain_chance", "尸体不摧毁几率");
        L.addZh("attribute.name.found_item_number", "掉落物数量");
        L.addZh("attribute.name.found_item_rarity", "掉落物稀有度");
        L.addZh("attribute.name.item_capacity", "物品容量");
        L.addZh("attribute.name.presence_range", "存在范围");
        L.addZh("attribute.name.light_radius", "光照半径");
        L.addZh("attribute.name.skill_slot_count", "技能槽数量");
    }

}
