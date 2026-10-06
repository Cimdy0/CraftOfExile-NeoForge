package com.cimdy.coe.data.gen.lang;

import com.cimdy.coe.data.gen.source.EntityGen;
import com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect;

public final class ModLang {
    private ModLang() {
    }

    static void install(LangSupport L) {
        // 词缀阶名（离线烘焙：英文 Name 原样 → 中文），键前缀 mod.coe.name.
        ModTierLang.install(L);
        addModLangPart1(L);
        addModLangPart2(L);
        addModLangPart3(L);
        addEntityModLang(L);
        addMissingEntityKeys(L);
    }

    private static void addEntityModLang(LangSupport L) {
        L.addZh("item.mod.coe.entity.increased_stun_threshold", "晕眩门槛提高");
        L.addZh("item.mod.coe.entity.fire_resistant", "火焰抵抗");
        L.addZh("item.mod.coe.entity.cold_resistant", "冰霜抵抗");
        // ⚠️ light 语义待裁定（Lightning vs Light）：暂留无条件写（含中文入 en_us），勿改。
        L.addZh("item.mod.coe.entity.light_resistant", "闪电抵抗");
        L.addZh("item.mod.coe.entity.chaos_resistant", "混沌抵抗");
        L.addZh("item.mod.coe.entity.extra_fire_damage", "额外火焰伤害");
        L.addZh("item.mod.coe.entity.extra_cold_damage", "额外冰霜伤害");
        // ⚠️ light 语义待裁定（Lightning vs Light）：暂留无条件写（含中文入 en_us），勿改。
        L.addZh("item.mod.coe.entity.extra_light_damage", "额外闪电伤害");
        L.addZh("item.mod.coe.entity.extra_chaos_damage", "额外混沌伤害");
        L.addZh("item.mod.coe.entity.always_bleeds", "总是造成流血");
        L.addZh("item.mod.coe.entity.all_damage_ignites", "所有伤害点燃");
        L.addZh("item.mod.coe.entity.all_damage_chill", "所有伤害冰缓");
        L.addZh("item.mod.coe.entity.all_damage_shocks", "所有伤害感电");
        L.addZh("item.mod.coe.entity.always_poisons", "总是造成中毒");
        L.addZh("item.mod.coe.entity.breaks_armor", "护甲破坏");
        L.addZh("item.mod.coe.entity.stuns", "造成晕眩");
        L.addZh("item.mod.coe.entity.extra_crits", "额外暴击");
        L.addZh("item.mod.coe.entity.accurate", "精准");
        L.addZh("item.mod.coe.entity.hasted", "快速");
        L.addZh("item.mod.coe.entity.additional_projectiles", "额外投射物");
        L.addZh("item.mod.coe.entity.armored", "护甲");
        L.addZh("item.mod.coe.entity.increased_life", "生命提高");
        L.addZh("item.mod.coe.entity.regenerates_life", "生命再生");
        L.addZh("item.mod.coe.entity.crit_resistant", "暴击抵抗");
        L.addZh("item.mod.coe.entity.stun_resistant", "晕眩抵抗");
    }

    /**
     * 补齐怪物词缀侧「从未注册」的键（运行时会显示原始键串，见 compat/jade/MobsInfo）。
     * <p>键集来源：{@code item.mod.coe.entity.weakness}（MobsInfo 字面量，抗性面板「弱点」前缀）；
     * 其余为 {@code data/gen/entity/EntityGen} 以 {@code registerMod/registerTaggedMod/registerSkillMod}
     * 注册的 LivingMod 名（MobsInfo 拼 {@code "item.mod.coe.entity." + name} 直接 translatable 无回落）。
     */
    private static void addMissingEntityKeys(LangSupport L) {
        L.addZh("item.mod.coe.entity.weakness", "弱点");
        L.addPair("item.mod.coe.entity.mind_over_matter", "Mind over Matter", "心胜于物");
        L.addZh("item.mod.coe.entity.pyromancer", "火焰法师");
        L.addZh("item.mod.coe.entity.feral_slasher", "野性撕裂者");
        L.addZh("item.mod.coe.entity.elemental_adept", "元素亲和");
        L.addZh("item.mod.coe.entity.warlock", "术士");
    }

    private static void addModLangPart1(LangSupport L) {

        L.addZh("stat.level", "等级");
        L.addZh("stat.require", "需求");
        L.addZh("stat.low", "低");
        L.addZh("stat.high", "高");
        L.addZh("stat.full", "满");
        L.addZh("stat.support", "辅助");
        L.addZh("stat.aura", "光环");
        L.addZh("stat.base", "基础");
        L.addZh("stat.s", "的");
        L.addZh("stat.critical", "暴击");
        L.addZh("stat.spell", "施法");
        L.addZh("stat.skill", "技能");
        L.addZh("stat.skill.title", "技能装配");
        L.addZh("stat.skill.aura_on", "光环");
        L.addZh("stat.skill.section.assembly", "技能装配");
        L.addZh("stat.skill.section.inventory", "物品栏");
        L.addZh("stat.aoe", "范围");
        L.addZh("stat.hit", "击中");
        L.addZh("stat.per", "每");
        L.addZh("stat.single", "只");
        L.addZh("stat.an", "一个");
        L.addZh("stat.killed", "击败");
        L.addZh("stat.enemy", "敌人");
        L.addZh("stat.attribute", "属性");
        L.addZh("stat.element", "元素");
        L.addZh("stat.local", "该装备");
        L.addZh("stat.apply", "附加");
        L.addZh("stat.attack", "攻击");
        L.addZh("stat.on_hit", "击中时");
        L.addZh("stat.chance", "几率");
        L.addZh("stat.to_poison", "导致中毒");
        L.addZh("stat.to_maim", "造成瘫痪");
        L.addZh("stat.to_bleeding", "造成流血");
        L.addZh("stat.max", "最大");
        L.addZh("stat.have", "有");
        L.addZh("stat.grants", "获得");
        L.addZh("stat.health", "生命");
        L.addZh("stat.energy", "护盾");
        L.addZh("stat.mana", "魔力");
        L.addZh("stat.spirit", "精魂");
        L.addZh("stat.weakness", "弱点");
        L.addZh("stat.physical", "物理");
        L.addZh("stat.fire", "火焰");
        L.addZh("stat.cold", "冰霜");
        L.addZh("stat.light", "闪电");
        L.addZh("stat.chaos", "混沌");
        L.addZh("stat.damage", "伤害");
        L.addZh("stat.resistance", "抗性");
        // 单词拼接分隔符：en 用空格（Fire Resistance），zh 用空串（火焰抗性）；两 locale 都不退化。
        L.addPair("stat.concat_sep", " ", "");
        L.addZh("stat.inc", "提高");
        L.addZh("stat.reduced", "降低");
        L.addZh("stat.strength", "力量");
        L.addZh("stat.dexterity", "敏捷");
        L.addZh("stat.intelligence", "智慧");
        L.addZh("stat.accuracy", "命中");
        L.addZh("stat.armor", "护甲");
        L.addZh("stat.evasion", "闪避");
        L.addZh("stat.block", "格挡");
        L.addZh("stat.global", "全域");
        L.addZh("stat.elemental", "元素");
        L.addZh("stat.melee", "近战");
        L.addZh("stat.crit", "暴击");
        L.addZh("stat.speed", "速度");
        L.addZh("stat.bow", "弓类");
        L.addZh("stat.projectile", "投射物");
        L.addZh("stat.arrow", "箭矢");
        L.addZh("stat.poison", "中毒");
        L.addZh("stat.movement", "移动");
        L.addZh("stat.stun", "晕眩");
        L.addZh("stat.duration", "持续时间");
        L.addZh("stat.leeches", "偷取");

        L.addZh("attribute.name.strength", "力量");
        L.addZh("attribute.name.intelligence", "智慧");
        L.addZh("attribute.name.dexterity", "敏捷");
        L.addZh("attribute.name.max_mana", "最大魔力");
    }

    private static void addModLangPart2(LangSupport L) {

        L.addZh("attribute.name.ally_damage_applies_to_self", "召唤物伤害加成作用于自身");
        L.addZh("attribute.name.knockback", "击退距离");
        L.addZh("attribute.name.knockback_chance", "近距击退几率");
        L.addZh("attribute.name.evasion", "闪避");
        L.addZh("attribute.name.accuracy", "命中");
    }

    private static void addModLangPart3(LangSupport L) {

        L.addZh("attribute.name.cast_speed", "施法速度");
        L.addZh("attribute.name.block_chance", "格挡率");
        L.addZh("attribute.name.max_block", "最大格挡");
        L.addZh("attribute.name.taken_damage", "承受伤害");
        L.addZh("attribute.name.launch_additional_projectile", "额外投射物");
        L.addZh("attribute.name.additional_projectile_chance", "额外投射物几率");
        L.addZh("attribute.name.additional_projectile_count", "额外投射物数量");
        L.addZh("attribute.name.projectile_speed", "投射物速度");
        L.addZh("attribute.name.talent_looting", "战利品");
        L.addZh("attribute.name.talent_mending", "修复");
        L.addZh("attribute.name.talent_fortune", "幸运");
        L.addZh("attribute.name.talent_auto_smelt", "自动熔炼");
        L.addZh("attribute.name.talent_extra_xp", "额外经验");
        L.addPair("attribute.name.talent_magnet", "Magnet (Auto Pickup)", "磁铁（自动拾取）");
        L.addZh("attribute.name.physical_damage_reduction", "物理伤害减免");
        L.addZh("attribute.name.hazard_rearm_chance", "危害物触发后重新布置的几率");
        L.addZh("stat.causes", "造成");
        L.addZh("stat.buildup", "累积");
        L.addZh("stat.thorns", "反射");
        L.addZh("stat.defences", "防御");
        L.addZh("stat.found", "发现");
        L.addZh("stat.item", "物品");
        L.addZh("stat.rarity", "稀有度");
        L.addZh("stat.number", "数量");
        L.addZh("stat.reload", "装填");
        L.addZh("stat.time", "时间");
        L.addZh("stat.addition", "额外");
        L.addZh("stat.additional", "额外");
        L.addZh("stat.launch", "发射");
        L.addZh("stat.area", "范围");
        L.addZh("stat.buff", "增益效果");
        L.addZh("stat.cooldown", "冷却");
        L.addZh("stat.cost", "消耗");
        L.addZh("stat.cross_bow", "弩");
        L.addZh("stat.bolt", "弩矢");
        L.addZh("stat.second", "秒");
        L.addZh("stat.regeneration", "再生");
        L.addZh("stat.rate", "速率");
        L.addZh("stat.recharge", "充能");
        L.addZh("stat.faster", "更快");
        L.addZh("stat.start", "起始");
        L.addZh("stat.taken", "受到");
        L.addZh("stat.shock", "感电");
        L.addZh("stat.chill", "冰缓");
        L.addZh("stat.freeze", "冻结");
        L.addZh("stat.bleed", "流血");
        L.addZh("stat.ignite", "点燃");
        L.addZh("stat.pierce", "穿透");
        L.addZh("stat.threshold", "门槛");
        L.addZh("stat.ailment", "异常");
        L.addZh("stat.preserve", "保留");
            // marker word-key lexicon: stat.<token> assembled from MarkerStatEffect.Kind ids; reuses existing keys, no twins
            L.addZh("stat.activation", "激活");
            L.addZh("stat.advance", "推进");
            L.addZh("stat.ammo", "弹药");
            L.addZh("stat.as", "为");
            L.addZh("stat.at", "在");
            L.addZh("stat.avoid", "避免");
            L.addZh("stat.banner", "战旗");
            L.addZh("stat.bonus", "加成");
            L.addZh("stat.both", "双");
            L.addZh("stat.category", "类别");
            L.addZh("stat.cause", "造成");
            L.addZh("stat.charge", "充能");
            L.addZh("stat.consume", "消耗");
            L.addZh("stat.consumed", "消耗");
            L.addZh("stat.convert", "转换");
            L.addZh("stat.corpse", "尸体");
            L.addZh("stat.corruption", "腐蚀");
            L.addZh("stat.count", "数量");
            L.addZh("stat.debilitate", "虚弱");
            L.addZh("stat.defend", "防御");
            L.addZh("stat.dmg", "伤害");
            L.addZh("stat.ele", "元素");
            L.addZh("stat.empty", "空");
            L.addZh("stat.endures", "持久");
            L.addZh("stat.entity", "实体");
            L.addZh("stat.equip", "装备");
            L.addZh("stat.equipment", "装备");
            L.addZh("stat.equipped", "装备");
            L.addZh("stat.es", "能量护盾");
            L.addZh("stat.exempt", "豁免");
            L.addZh("stat.expended", "消耗");
            L.addZh("stat.expire", "消散");
            L.addZh("stat.extra", "额外");
            L.addZh("stat.flask", "药剂");
            L.addZh("stat.form", "形态");
            L.addZh("stat.front", "前");
            L.addZh("stat.gate", "门槛");
            L.addZh("stat.glory", "荣耀");
            L.addZh("stat.grenade", "榴弹");
            L.addZh("stat.guard", "守卫");
            L.addZh("stat.hands", "手");
            L.addZh("stat.history", "历史");
            L.addZh("stat.human", "人形");
            L.addZh("stat.ignore", "无视");
            L.addZh("stat.immune", "免疫");
            L.addZh("stat.inside", "内");
            L.addZh("stat.interval", "间隔");
            L.addZh("stat.invocation", "召唤");
            L.addZh("stat.leech", "偷取");
            L.addZh("stat.leeched", "偷取");
            L.addZh("stat.life", "生命");
            L.addZh("stat.limit", "上限");
            L.addZh("stat.loss", "损耗");
            L.addZh("stat.magnitude", "幅度");
            L.addZh("stat.martial", "武术");
            L.addZh("stat.min", "最小");
            L.addZh("stat.minions", "随从");
            L.addZh("stat.more", "更多");
            L.addZh("stat.no", "无");
            L.addZh("stat.not", "不");
            L.addZh("stat.offering", "献祭");
            L.addZh("stat.on", "上");
            L.addZh("stat.outside", "外");
            L.addZh("stat.penalty", "惩罚");
            L.addZh("stat.quarterstaff", "长杖");
            L.addZh("stat.range", "范围");
            L.addZh("stat.reapply", "重新施加");
            L.addZh("stat.recent", "近期");
            L.addZh("stat.recover", "恢复");
            L.addZh("stat.reflect", "反射");
            L.addZh("stat.regen", "再生");
            L.addZh("stat.requirements", "需求");
            L.addZh("stat.res", "抗性");
            L.addZh("stat.retain", "保留");
            L.addZh("stat.rule", "规则");
            L.addZh("stat.shapeshift", "变形");
            L.addZh("stat.shield", "盾");
            L.addZh("stat.skip", "跳过");
            L.addZh("stat.spawn", "生成");
            L.addZh("stat.stage", "阶段");
            L.addZh("stat.take", "承受");
            L.addZh("stat.to", "给");
            L.addZh("stat.travel", "飞行");
            L.addZh("stat.trigger", "触发");
            L.addZh("stat.tripled", "三倍");
            L.addZh("stat.unarmed", "空手");
            L.addZh("stat.unreserved", "未保留");
            L.addZh("stat.volatility", "动荡");
            L.addZh("stat.while", "当");

    }

}
