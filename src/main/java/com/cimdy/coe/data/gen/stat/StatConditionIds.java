/*

 * Decompiled with CFR 0.152.

 * 

 * Could not load the following classes:

 *  net.minecraft.resources.Identifier

 */

package com.cimdy.coe.data.gen.stat;



import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.EventHistoryValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceCountValue;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.LivingStateValue;

import net.minecraft.resources.Identifier;
import com.cimdy.coe.data.helper.engine.DamagePipeline;



public final class StatConditionIds {

    public static final Identifier COND_0_0_LIVING_STATE_SURROUNDED = id("0_0_living_state_surrounded");

    public static final Identifier COND_2_0_BLUE_COMPARE_COUNT_SOCKET_SUPPORT = id("2_0_blue_compare_count_socket_support");

    public static final Identifier COND_2_0_COMPARE_COUNT_GREEN_SOCKET_SUPPORT = id("2_0_compare_count_green_socket_support");

    public static final Identifier COND_2_0_COMPARE_COUNT_RED_SOCKET_SUPPORT = id("2_0_compare_count_red_socket_support");

    public static final Identifier COND_ARCHON_LIVING_STATE = id("archon_living_state");

    public static final Identifier COND_ARMOR_COMPARE_EQUIPMENT_EVASION_HEAD = id("armor_compare_equipment_evasion_head");

    public static final Identifier COND_ARMOR_COMPARE_EQUIPMENT_EVASION_HEAD_2 = id("armor_compare_equipment_evasion_head_2");

    public static final Identifier COND_BLEED_STATUS_AGE = id("bleed_status_age");

    public static final Identifier COND_BLOCKING_LIVING_STATE = id("blocking_living_state");

    public static final Identifier COND_CONSUME_POWER_COUNT_RECENTLY = id("consume_power_count_recently");

    public static final Identifier COND_COUNT_KILL_RECENTLY = id("count_kill_recently");

    public static final Identifier COND_COUNT_RECENTLY_TAKEN_HIT = id("count_recently_taken_hit");

    public static final Identifier COND_RECENTLY_RELOADED = id("recently_reloaded");

    public static final Identifier COND_COUNT_RECENTLY_TAKEN_STUN = id("count_recently_taken_stun");

    public static final Identifier COND_COUNT_RECENTLY_USE_ITEM = id("count_recently_use_item");

    public static final Identifier COND_COUNT_RECENTLY_USE_TOTEM = id("count_recently_use_totem");

    public static final Identifier COND_DAZE_STATUS = id("daze_status");

    public static final Identifier COND_ELECTROCUTE_STATUS = id("electrocute_status");

    public static final Identifier COND_EXPOSURE_COLD_STATUS = id("exposure_cold_status");

    public static final Identifier COND_EXPOSURE_FIRE_STATUS = id("exposure_fire_status");

    public static final Identifier COND_EXPOSURE_LIGHTNING_STATUS = id("exposure_lightning_status");

    public static final Identifier COND_ANY_EXPOSURE = id("any_exposure");

    public static final Identifier COND_ANY_AILMENT = id("any_ailment");

    public static final Identifier COND_ANY_ELEMENTAL_AILMENT = id("any_elemental_ailment");

    public static final Identifier COND_ANY_CURSE = id("any_curse");

    public static final Identifier COND_CURSE_DURATION_EXPIRED = id("curse_duration_expired");

    public static final Identifier COND_NO_ELEMENTAL_AILMENT = id("no_elemental_ailment");

    public static final Identifier COND_ANY_DAMAGING = id("any_damaging");

    public static final Identifier COND_FREEZE_STATUS = id("freeze_status");

    public static final Identifier COND_FULL_LIFE_LIVING_STATE = id("full_life_living_state");

    public static final Identifier COND_FULL_LIFE_LIVING_STATE_2 = id("full_life_living_state_2");

    public static final Identifier COND_HEAVY_STUN_STATUS = id("heavy_stun_status");

    public static final Identifier COND_STUN_STATUS = id("stun_status");

    public static final Identifier COND_ARMOUR_BROKEN_FULLY_STATUS = id("armour_broken_fully_status");

    public static final Identifier COND_SOURCE_ARMOUR_BROKEN_FULLY_STATUS = id("source_armour_broken_fully_status");

    public static final Identifier COND_LIVING_STATE_LOW_LIFE = id("living_state_low_life");

    public static final Identifier COND_LIVING_STATE_OFF_HAND_EMPTY_ONE_HANDED_MARTIAL = id("living_state_off_hand_empty_one_handed_martial");

    public static final Identifier COND_BOTH_HANDS_EMPTY = id("both_hands_empty");

    public static final Identifier COND_LIVING_STATE_PARRYING = id("living_state_parrying");

    public static final Identifier COND_LIVING_STATE_RECENTLY_TOOK_DOT = id("living_state_recently_took_dot");

    public static final Identifier COND_RECENTLY_GAINED_RAGE = id("recently_gained_rage");

    public static final Identifier COND_ALL_MINIONS_ARE_COMPANIONS = id("all_minions_are_companions");

    public static final Identifier COND_LIVING_STATE_SHAPESHIFTED = id("living_state_shapeshifted");

    public static final Identifier COND_SOURCE_LIVING_STATE_SHAPESHIFTED = id("source_living_state_shapeshifted");

    public static final Identifier COND_FORM_BEAR = id("form_bear");

    public static final Identifier COND_FORM_WOLF = id("form_wolf");

    public static final Identifier COND_FORM_WYVERN = id("form_wyvern");

    public static final Identifier COND_LIVING_STATE_STATIONARY = id("living_state_stationary");

    public static final Identifier COND_LIVING_STATE_SURROUNDED = id("living_state_surrounded");

    public static final Identifier COND_WIELDING_AXE = id("wielding_axe");

    public static final Identifier COND_WIELDING_QUARTERSTAFF = id("wielding_quarterstaff");

    public static final Identifier COND_WIELDING_MELEE_WEAPON = id("wielding_melee_weapon");

    public static final Identifier COND_WIELDING_DAGGER = id("wielding_dagger");

    public static final Identifier COND_MOVEMENT_STATIONARY = id("movement_stationary");

    public static final Identifier COND_ON_CHILLED_GROUND_STATUS = id("on_chilled_ground_status");

    public static final Identifier COND_ON_IGNITED_GROUND_STATUS = id("on_ignited_ground_status");

    public static final Identifier COND_ON_JAGGED_GROUND_STATUS = id("on_jagged_ground_status");

    public static final Identifier COND_ON_SHOCKED_GROUND_STATUS = id("on_shocked_ground_status");

    public static final Identifier COND_PIN_STATUS = id("pin_status");

    public static final Identifier COND_PRESENCE_RARITY_RARE_UNIQUE = id("presence_rarity_rare_unique");

    // 事件「触发目标」稀有度（真值门控「当你重晕稀有 / 传奇敌人时 X」）：读 caused_ailment+heavy_stun

    // 事件所作用的那一只敌人（trigger target）的 RARITY，取代 COND_PRESENCE_RARITY_RARE_UNIQUE 的近似。

    public static final Identifier COND_TARGET_RARITY_RARE_UNIQUE = id("target_rarity_rare_unique");

    public static final Identifier COND_SHOCK_STATUS = id("shock_status");

    public static final Identifier COND_ARCANE_SURGE_STATUS = id("arcane_surge_status");

    public static final Identifier COND_BLIND_STATUS = id("blind_status");

    public static final Identifier COND_CHILL_STATUS = id("chill_status");

    public static final Identifier COND_HINDER_STATUS = id("hinder_status");

    public static final Identifier COND_IGNITE_STATUS = id("ignite_status");

    public static final Identifier COND_MAIM_STATUS = id("maim_status");

    public static final Identifier COND_POISON_STATUS = id("poison_status");

    public static final Identifier COND_NOT_POISON_STATUS = id("not_poison_status");

    public static final Identifier COND_WITHERED_STATUS = id("withered_status");

    public static final Identifier COND_NOT_WITHERED_STATUS = id("not_withered_status");

    public static final Identifier COND_SLOW_STATUS = id("slow_status");

    public static final Identifier COND_CHANNELLING_LIVING_STATE = id("channelling_living_state");

    public static final Identifier COND_LEECHING_LIVING_STATE = id("leeching_living_state");

    public static final Identifier COND_LEECHING_MANA_LIVING_STATE = id("leeching_mana_living_state");

    public static final Identifier COND_MOVING_LIVING_STATE = id("moving_living_state");

    public static final Identifier COND_SPRINTING_LIVING_STATE = id("sprinting_living_state");

    public static final Identifier COND_DUAL_WIELD_LIVING_STATE = id("dual_wield_living_state");

    public static final Identifier COND_HOLDING_SHIELD_LIVING_STATE = id("holding_shield_living_state");

    public static final Identifier COND_LOW_MANA_LIVING_STATE = id("low_mana_living_state");

    public static final Identifier COND_NOT_LOW_MANA_LIVING_STATE = id("not_low_mana_living_state");

    public static final Identifier COND_FULL_MANA_LIVING_STATE = id("full_mana_living_state");

    public static final Identifier COND_DODGE_ROLLING_LIVING_STATE = id("dodge_rolling_living_state");

    public static final Identifier COND_WIELDING_TWO_HANDED_MELEE = id("wielding_two_handed_melee");

    public static final Identifier COND_INCISION_STATUS = id("incision_status");

    public static final Identifier COND_PRESENCE_MARK_STATUS = id("presence_mark_status");

    public static final Identifier COND_HAZARD_MARKED = id("hazard_marked");

    public static final Identifier COND_RECENTLY_ENTERED_PRESENCE = id("recently_entered_presence");

    public static final Identifier COND_RECENTLY_LEFT_PRESENCE = id("recently_left_presence");

    public static final Identifier COND_COUNT_RECENTLY_CAUSED_CRIT = id("count_recently_caused_crit");

    public static final Identifier COND_COUNT_RECENTLY_CAUSED_PARRY = id("count_recently_caused_parry");

    public static final Identifier COND_CONSUME_FRENZY_COUNT_RECENTLY = id("consume_frenzy_count_recently");

    public static final Identifier COND_CONSUME_ENDURANCE_COUNT_RECENTLY = id("consume_endurance_count_recently");

    public static final Identifier COND_COUNT_RECENTLY_CAUSED_BLOCK = id("count_recently_caused_block");

    public static final Identifier COND_COUNT_RECENTLY_CAUSED_AILMENT = id("count_recently_caused_ailment");

    public static final Identifier COND_COUNT_RECENTLY_META_SKILL_TRIGGERED = id("count_recently_meta_skill_triggered");

    public static final Identifier COND_RECENTLY_CAUSED_STUN = id("recently_caused_stun");

    public static final Identifier COND_RECENTLY_CAUSED_PIN = id("recently_caused_pin");

    public static final Identifier COND_NOT_RECENTLY_TAKEN_HIT = id("not_recently_taken_hit");

    public static final Identifier COND_NOT_RECENTLY_TAKEN_STUN = id("not_recently_taken_stun");

    // ── 近期（4s）被某元素伤害命中：per-element 扇出，供「每种近期承受过的元素伤害类型 +X% 最大抗性」──

    // 数据源已存在：DamagePipeline:860 recordTaggedHit(TAKEN_HIT, recencyTags)，RECENCY_TAGS 白名单含各元素。

    public static final Identifier COND_RECENTLY_TAKEN_HIT_FIRE = id("recently_taken_hit_fire");

    public static final Identifier COND_RECENTLY_TAKEN_HIT_COLD = id("recently_taken_hit_cold");

    public static final Identifier COND_RECENTLY_TAKEN_HIT_LIGHTNING = id("recently_taken_hit_lightning");

    public static final Identifier COND_COUNT_RECENTLY_FORM_CHANGED = id("count_recently_form_changed");

    public static final Identifier COND_COUNT_RECENTLY_GAIN_CHARGE = id("count_recently_gain_charge");

    public static final Identifier COND_COUNT_RECENTLY_ATTACK_START = id("count_recently_attack_start");

    public static final Identifier COND_CAST_SPELL_RECENTLY = id("cast_spell_recently");

    public static final Identifier COND_COUNT_RECENTLY_CAUSED_HIT = id("count_recently_caused_hit");

    public static final Identifier COND_COUNT_RECENTLY_FORM_CLEARED = id("count_recently_form_cleared");

    public static final Identifier COND_WITHIN_RANGE_OF_ENEMY = id("within_range_of_enemy");

    public static final Identifier COND_FURTHER_THAN_RANGE_OF_ENEMY = id("further_than_range_of_enemy");

    public static final Identifier COND_PRESENCE_TOTEM = id("presence_totem");

    public static final Identifier COND_PRESENCE_ALLY = id("presence_ally");

    public static final Identifier COND_PRESENCE_ALLY_SUMMONED = id("presence_ally_summoned");

    public static final Identifier COND_HAS_ENERGY_SHIELD = id("has_energy_shield");

    public static final Identifier COND_FULL_ENERGY_SHIELD = id("full_energy_shield");

    public static final Identifier COND_CRIT_HIT = id("crit_hit");

    public static final Identifier COND_SOCKET_RED_SUPPORT = id("socket_red_support");

    public static final Identifier COND_SOCKET_BLUE_SUPPORT = id("socket_blue_support");

    public static final Identifier COND_SOCKET_GREEN_SUPPORT = id("socket_green_support");

    public static final Identifier COND_COUNT_CAUSED_MELEE_HIT_8S = id("count_caused_melee_hit_8s");

    public static final Identifier COND_COUNT_RECENTLY_CAUSED_MELEE_HIT = id("count_recently_caused_melee_hit");

    public static final Identifier COND_COUNT_CAUSED_PROJECTILE_HIT_8S = id("count_caused_projectile_hit_8s");

    public static final Identifier COND_NOT_RECENTLY_CAUSED_CRIT = id("not_recently_caused_crit");

    public static final Identifier COND_COUNT_CAUSED_CRIT_8S = id("count_caused_crit_8s");

    public static final Identifier COND_COUNT_USE_FLASK_10S = id("count_use_flask_10s");

    public static final Identifier COND_COUNT_USE_MANA_FLASK_10S = id("count_use_mana_flask_10s");

    public static final Identifier COND_COUNT_CONSUME_GLORY_20S = id("count_consume_glory_20s");

    public static final Identifier COND_RECENTLY_TAKEN_DOT_1S = id("recently_taken_dot_1s");

    public static final Identifier COND_NOT_RECENTLY_TAKEN_DOT_1S = id("not_recently_taken_dot_1s");

    public static final Identifier COND_NOT_RECENTLY_ATTACK_START = id("not_recently_attack_start");

    public static final Identifier COND_RECENTLY_DODGE_ROLLED = id("recently_dodge_rolled");

    public static final Identifier COND_RECENTLY_CONSUME_INFUSION = id("recently_consume_infusion");

    public static final Identifier COND_RECENTLY_CONSUME_CORPSE = id("recently_consume_corpse");

    public static final Identifier COND_RECENTLY_USED_MARK = id("recently_used_mark");

    public static final Identifier COND_RECENTLY_MINION_DIED = id("recently_minion_died");

    public static final Identifier COND_ES_RECHARGE_STARTED_2S = id("es_recharge_started_2s");

    public static final Identifier COND_FLASK_ACTIVE_ANY = id("flask_active_any");

    public static final Identifier COND_FLASK_ACTIVE_MANA = id("flask_active_mana");

    public static final Identifier COND_FLASK_ACTIVE_HEALTH = id("flask_active_health");

    public static final Identifier COND_NO_HEALTH_FLASK_USES_LEFT = id("no_health_flask_uses_left");

    public static final Identifier COND_CHARM_ACTIVE_ANY = id("charm_active_any");

    public static final Identifier COND_ENEMY_TYPE_UNDEAD = id("enemy_type_undead");

    public static final Identifier COND_ENEMY_TYPE_HUMANOID = id("enemy_type_humanoid");

    public static final Identifier COND_ENEMY_TYPE_DEMON = id("enemy_type_demon");

    public static final Identifier COND_ENEMY_TYPE_CONSTRUCT = id("enemy_type_construct");

    public static final Identifier COND_ENEMY_TYPE_BEAST = id("enemy_type_beast");

    public static final Identifier COND_SOURCE_HAS_COMPANION_PRESENCE = id("source_has_companion_presence");

    public static final Identifier COND_ANY_HERALD = id("any_herald");

    public static final Identifier COND_HERALD_OF_ASH = id("herald_of_ash");

    public static final Identifier COND_HERALD_OF_ICE = id("herald_of_ice");

    public static final Identifier COND_HERALD_OF_THUNDER = id("herald_of_thunder");

    public static final Identifier COND_HERALD_OF_PLAGUE = id("herald_of_plague");

    public static final Identifier COND_HERALD_OF_BLOOD = id("herald_of_blood");

    public static final Identifier COND_USING_COLD_SKILL = id("using_cold_skill");

    public static final Identifier COND_USING_FIRE_SKILL = id("using_fire_skill");

    public static final Identifier COND_USING_SKILL = id("using_skill");

    public static final Identifier COND_TARGET_IGNITED_BY_YOU = id("target_ignited_by_you");

    // ── 归一 B 类：复用既有节点原语（LivingStateValue / EventHistoryValue），仅加数据条目 ──

    public static final Identifier COND_INFLICT_BLEED = id("inflict_bleed");



    private StatConditionIds() {

    }




    // —— 实体获取条件（StatCondition）具名常量——
    // 与上方 COND_*（谓词，机制 B：进 conditions 字段内联展开）正交，见 StatEntityConditionGen。
    public static final Identifier CAC_PRESENCE_SELF_ANY = id("presence_self_any");

    public static final Identifier CAC_PRESENCE_SELF_ALLY = id("presence_self_ally");

    public static final Identifier CAC_PRESENCE_SELF_ENEMY = id("presence_self_enemy");

    public static final Identifier CAC_RELATION_SELF_MINION = id("relation_self_minion");

    public static final Identifier CAC_RELATION_SELF_TOTEM = id("relation_self_totem");

    public static final Identifier CAC_RELATION_SELF_COMPANION = id("relation_self_companion");

    public static final Identifier CAC_RELATION_SELF_ALLY = id("relation_self_ally");

    public static final Identifier CAC_RELATION_SELF_OWNED_BY = id("relation_self_owned_by");

    /**
     * Presence 条件枚举 → 具名 id。穷尽 switch 无 {@code default}：枚举加值即编译失败，
     * 杜绝「新增取值忘了登记名字、运行期才抛未注册」。
     */
    public static Identifier presenceId(PresenceCountValue.Relation camp) {
        return switch (camp) {
            case ANY -> CAC_PRESENCE_SELF_ANY;
            case ALLY -> CAC_PRESENCE_SELF_ALLY;
            case ENEMY -> CAC_PRESENCE_SELF_ENEMY;
        };
    }

    private static Identifier id(String path) {

        return Identifier.fromNamespaceAndPath((String)"coe", (String)path);

    }

}

