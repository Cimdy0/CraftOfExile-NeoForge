package com.cimdy.coe.data.gen.gem;

import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.data.gen.asset.CoeModelProvider;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.core.SkillEntry;
import com.cimdy.coe.data.codec.skill.gem.SupportRequire;
import com.cimdy.coe.data.codec.skill.support.SupportGem;
import com.cimdy.coe.data.codec.support.require.Require;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.gen.gem.skill.SkillBlueGen;
import com.cimdy.coe.data.gen.gem.skill.SkillGreenGen;
import com.cimdy.coe.data.gen.gem.skill.SkillRedGen;
import com.cimdy.coe.data.gen.gem.spirit.LifeGen;
import com.cimdy.coe.data.gen.gem.spirit.SpiritBlueGen;
import com.cimdy.coe.data.gen.gem.spirit.SpiritGreenGen;
import com.cimdy.coe.data.gen.gem.spirit.SpiritRedGen;
import com.cimdy.coe.data.gen.gem.support.SupportBlueGen;
import com.cimdy.coe.data.gen.gem.support.SupportGreenGen;
import com.cimdy.coe.data.gen.gem.support.SupportRedGen;
import com.cimdy.coe.event.data.LoadEvent;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;

import java.util.List;

public class GemGen {

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(CraftOfExile.MODID, path);
    }

    /**
     * 全部已注册主动技能 ID（按颜色前缀分组：red=力量 / green=敏捷 / blue=智力）。
     * 颜色段同时作为数据路径 {@code data/<mod>/item/gem/skill/<color>/<name>.json}，
     * 运行时由 {@code LoadEvent.GenEvent}（该嵌套类型已删除） 解析并缓存 {@code GemColor}。
     * 供 CoeModelProvider 为每个技能生成物品模型。
     */
    public static final List<Identifier> SKILL_IDS = List.of(
            // 红色（力量）— 近战猛击 / 位移 / 范围
            id("red/rolling_slam"),
            id("red/earthquake"),
            id("red/leap_slam"),
            id("red/volcanic_fissure"),
            id("red/shockwave"),
            id("red/aftershock"),
            id("red/boneshatter"),
            id("red/sunder"),
            id("red/perforate"),
            id("red/ground_slam"),
            id("red/rupture"),
            id("red/perfect_strike"),
            id("red/stampede"),
            id("red/war_banner"),
            id("red/seismic_cry"),
            id("red/shockwave_totem"),
            id("red/ancestral_warrior_totem"),
            id("red/execute"),
            id("blue/raise_zombie"),
            // 绿色（敏捷）— 武器攻击 / 投射 / 位移
            id("green/lightning_arrow"),
            id("green/toxic_rain"),
            id("green/cyclone"),
            id("green/lightning_strike"),
            id("green/galvanic_shards"),
            id("green/lightning_spear"),
            id("green/puncture"),
            id("green/whirling_slash"),
            id("green/lacerate"),
            id("green/blood_hunt"),
            // 蓝色（智力）— 法术 / 光束 / 自身增益
            id("blue/fireball"),
            id("blue/frostbolt"),
            id("blue/spark"),
            id("blue/arc"),
            id("blue/incinerate"),
            id("blue/frost_nova"),
            id("blue/firestorm"),
            id("blue/ice_shard"),
            id("blue/ball_lightning"),
            id("blue/blight"),
            id("blue/comet"),
            id("blue/frost_bomb"),
            id("blue/chaos_bolt"),
            // <<<AUTO_START>>>
            id("red/armor_breaker"),
            id("red/bear_maul"),
            id("red/consume_corpse"),
            id("red/earthshatter"),
            id("red/forge_hammer"),
            id("red/fortifying_cry"),
            id("red/hammer_of_the_gods"),
            id("red/infernal_cry"),
            id("red/iron_ward"),
            id("red/magma_barrier"),
            id("red/molten_blast"),
            id("red/scavenged_plating"),
            id("red/shield_wall"),
            id("red/wyvern_rend"),
            id("green/alchemist_s_boon"),
            id("green/bloodhound_s_mark"),
            id("green/cull_the_weak"),
            id("green/disengage"),
            id("green/electrocuting_arrow"),
            id("green/elemental_sundering"),
            id("green/escape_shot"),
            id("green/explosive_spear"),
            id("green/fangs_of_frost"),
            id("green/freezing_salvo"),
            id("green/gas_arrow"),
            id("green/glacial_lance"),
            id("green/ice_shot"),
            id("green/lightning_rod"),
            id("green/magnetic_salvo"),
            id("green/plague_bearer"),
            id("green/poisonburst_arrow"),
            id("green/primal_strikes"),
            id("green/rain_of_arrows"),
            id("green/rake"),
            id("green/rapid_assault"),
            id("green/shockchain_arrow"),
            id("green/spear_of_solaris"),
            id("green/spearfield"),
            id("green/spiral_volley"),
            id("green/storm_lance"),
            id("green/stormcaller_arrow"),
            id("green/thunderous_leap"),
            id("green/tornado_shot"),
            id("green/toxic_domain"),
            id("green/toxic_growth"),
            id("green/twister"),
            id("green/vine_arrow"),
            id("green/whirlwind_lance"),
            id("green/wind_serpent_s_fury"),
            id("green/wolf_rake"),
            id("green/explosive_grenade"),
            id("green/voltaic_grenade"),
            id("blue/bone_blast"),
            id("blue/bone_cage"),
            id("blue/coiling_bolts"),
            id("blue/conductivity"),
            id("blue/contagion"),
            id("blue/decompose"),
            id("blue/despair"),
            id("blue/detonate_dead"),
            id("blue/elemental_conflux"),
            id("blue/elemental_weakness"),
            id("blue/ember_fusillade"),
            id("blue/enervating_nova"),
            id("blue/enfeeble"),
            id("blue/essence_drain"),
            id("blue/eye_of_winter"),
            id("blue/feast_of_flesh"),
            id("blue/firebolt"),
            id("blue/flame_wall"),
            id("blue/flammability"),
            id("blue/freezing_shards"),
            id("blue/frost_darts"),
            id("blue/frost_wall"),
            id("blue/fulmination"),
            id("blue/galvanic_field"),
            id("blue/hexblast"),
            id("blue/hypothermia"),
            id("blue/ice_nova"),
            id("blue/icestorm"),
            id("blue/lightning_bolt"),
            id("blue/lightning_conduit"),
            id("blue/lightning_warp"),
            id("blue/living_bomb"),
            id("blue/mirror_of_refraction"),
            id("blue/orb_of_storms"),
            id("blue/profane_ritual"),
            id("blue/reap"),
            id("blue/snap"),
            id("blue/solar_orb"),
            id("blue/soulrend"),
            id("blue/temporal_chains"),
            id("blue/untether"),
            id("blue/volatile_dead"),
            id("blue/vulnerability")
            // <<<AUTO_END>>>
    );

    /**
     * 辅助宝石 ID。运行时修正（effects）与技能自身 modifiers 共用同一套 SkillModifier 形状，
     * 由 SupportRed / Green / BlueGen 注册。此处列出供 CoeModelProvider 生成物品模型。
     */
    public static final List<Identifier> SUPPORT_GEM_IDS = List.of(
            // 红色（力量）+5 力量
            id("red/brutality"),
            id("red/fire_infusion"),
            id("red/heavy_swing"),
            // 绿色（敏捷）+5 敏捷

            id("green/fork"),
            id("green/chain"),
            // 蓝色（智力）+5 智慧
            id("blue/culling_strike"),
            // 元宝石（Cast on X，P2-3）
            id("blue/cast_on_crit"),
            id("blue/cast_on_ignite"),
            id("blue/cast_on_shock"),
            // <<<AUTO_SUPPORTS_START>>>
            id("blue/added_chaos_damage"),
            id("blue/added_cold_damage"),
            id("blue/added_fire_damage"),
            id("blue/added_lightning_damage"),
            id("green/adhesive_grenades"),
            id("green/alignment"),
            id("blue/ambush"),
            id("blue/arbiter_ignition"),
            id("blue/astral_projection"),
            id("red/battershout"),
            id("blue/biting_exposure"),
            id("green/blindside"),
            id("red/branching_fissures"),
            id("green/bursting_plague"),
            id("red/cirels_cultivation"),
            id("red/clash"),
            id("green/close_combat"),
            id("blue/cold_penetration"),
            id("blue/concentrated_area"),
            id("blue/conductive_exposure"),
            id("red/corrupting_cry"),
            id("green/deadly_poison"),
            id("red/deep_cuts"),
            id("red/echoing_cry"),
            id("blue/elemental_focus"),
            id("blue/encroaching_ground"),
            id("blue/expand"),
            id("blue/expanse"),
            id("blue/fire_penetration"),
            id("blue/fortress"),
            id("blue/freezefork"),
            id("blue/gambleshot"),
            id("green/garukhans_resolve"),
            id("blue/inexorable_critical"),
            id("blue/ixchels_torment"),
            id("red/kaoms_madness"),
            id("blue/lightning_penetration"),
            id("green/longshot"),
            id("blue/magnified_area"),
            id("green/martial_projectiles"),
            id("blue/morganas_tempest"),
            id("green/multishot"),
            id("green/murderous_intent"),
            id("green/overextend"),
            id("green/overreach"),
            id("green/pierce"),
            id("blue/pinpoint_critical"),
            id("blue/profanity"),
            id("green/pursuit"),
            id("green/retreat"),
            id("green/rigwalds_ferocity"),
            id("blue/rising_tempest"),
            id("blue/ritualistic_curse"),
            id("red/searing_flame"),
            id("blue/scorching_exposure"),
            id("green/spectral_volley"),
            id("blue/spell_cascade"),
            id("blue/spell_echo"),
            id("red/splinter_totem"),
            id("green/streamlined_rounds"),
            id("blue/supercritical"),
            id("red/tectonic_slams"),
            id("blue/unleash"),
            id("red/upheaval"),
            id("red/vruuns_inevitability"),
            id("blue/withering_touch"),
            id("blue/zarokhs_refrain"),
            id("blue/zarokhs_revolt"),
            id("red/zerphis_infamy")
            // <<<AUTO_SUPPORTS_END>>>
    );

    /**
     * 精魂宝石 ID — 对齐 poe2wiki（0.3 / Third Edict），各预留 30 精魂。
     * 红色（力量）：灰烬之捷；绿色（敏捷）：风舞者；蓝色（智力）：寒冰之捷。
     */
    public static final List<Identifier> SPIRIT_IDS = List.of(
            // 红色（力量）+30 精魂
            id("red/herald_of_ash"),
            id("red/herald_of_blood"),
            id("red/overwhelming_presence"),
            id("red/time_of_need"),
            id("red/eternal_rage"),
            id("red/berserk"),
            // 绿色（敏捷）+30 精魂
            id("green/combat_frenzy"),
            id("green/wind_dancer"),
            id("green/herald_of_thunder"),
            id("green/herald_of_plague"),
            id("green/grace"),
            id("green/trail_of_caltrops"),
            // 蓝色（智力）+30 精魂
            id("blue/blink"),
            id("blue/herald_of_ice"),
            id("blue/purity_of_fire"),
            id("blue/purity_of_ice"),
            id("blue/purity_of_lightning"),
            id("blue/impurity"),
            id("blue/clarity"),
            id("blue/wrath"),
            id("blue/siphon_elements"),
            id("blue/withering_presence"),
            id("blue/mana_remnants"),
            id("blue/malice"),
            id("blue/heart_of_ice"),
            id("blue/convalescence"),
            id("blue/archmage"),
            // 红色（力量）— 生命预留类常驻光环（原 life 分类，并入 red 消除伪 life 类别）
            id("red/blood_pact")
    );

    // ---------- 公共数据 ----------

    // ========== Require 辅助方法 ==========
    // PoE2 Uncut Gem 等级体系: 技能LV1→角色LV1, LV5→LV21, LV10→LV41, LV20→LV81
    // 属性需求: 纯属性技能14+2/LV, 混合属性技能10+1.5/LV

    private static ExpressionCodec.Value levelReq() {
        return ExpressionCodec.ofSkillLevel(1, 1, 4);
    }

    public static Require intReq() {
        return Require.builder().level(levelReq())
                .intelligence(ExpressionCodec.ofSkillLevel(14, 1, 2)).build();
    }

    public static Require strReq() {
        return Require.builder().level(levelReq())
                .strength(ExpressionCodec.ofSkillLevel(14, 1, 2)).build();
    }

    public static Require dexReq() {
        return Require.builder().level(levelReq())
                .dexterity(ExpressionCodec.ofSkillLevel(14, 1, 2)).build();
    }

    public static Require strIntReq() {
        return Require.builder().level(levelReq())
                .strength(ExpressionCodec.ofSkillLevel(10, 1, 1.5))
                .intelligence(ExpressionCodec.ofSkillLevel(10, 1, 1.5)).build();
    }

    /**
     * 辅助宝石的「额外需求」构造器：辅助宝石链接到技能时，会把这里的属性累加到技能需求上。
     * 三个参数分别为 力量 / 敏捷 / 智慧 的额外加成（PoE2 通常为 +5）。
     */
    public static SupportRequire supportReq(int strength, int dexterity, int intelligence) {
        return SupportRequire.builder()
                .strength(ExpressionCodec.of((double) strength))
                .dexterity(ExpressionCodec.of((double) dexterity))
                .intelligence(ExpressionCodec.of((double) intelligence))
                .build();
    }

    public static void registerSkill(BootstrapContext<SkillEntry> bootstrap) {
        SkillRedGen.register(bootstrap);
        SkillGreenGen.register(bootstrap);
        SkillBlueGen.register(bootstrap);
    }

    public static void registerSpirit(BootstrapContext<SkillEntry> bootstrap) {
        SpiritRedGen.register(bootstrap);
        SpiritGreenGen.register(bootstrap);
        SpiritBlueGen.register(bootstrap);
        LifeGen.register(bootstrap);
    }

    public static void registerSupportGem(BootstrapContext<SupportGem> bootstrap) {
        SupportRedGen.register(bootstrap);
        SupportGreenGen.register(bootstrap);
        SupportBlueGen.register(bootstrap);
    }
}
