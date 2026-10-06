package com.cimdy.coe.data.gen.gem.skill;

import com.cimdy.coe.data.gen.lang.LangProvider;

public final class SkillLang {
    private SkillLang() {
    }

    public static void addAll(LangProvider provider) {
    addAllPart1(provider);
    addAllPart2(provider);
    addAllPart3(provider);
    addAllPart4(provider);
}

    private static void addAllPart1(LangProvider provider) {

        // 形态攻击（POE2 Druid 实证）：使用即变身
        provider.addZh("gem.skill.name.coe.bear_maul", "熊形重击（Maul）");
        provider.addPair("gem.skill.description.coe.bear_maul", "Transform into Bear form and maul enemies, dealing Physical damage and granting 5 Rage per hit. Using this skill instantly switches you to Bear form; casting a spell reverts you to human.", "变身为熊形态并重击敌人，造成物理伤害，每次命中获得 5 怒气。使用该技能立即切换为熊形态；施放法术则变回人类。");
        provider.addZh("gem.skill.name.coe.wolf_rake", "狼形挥爪（Rake）");
        provider.addPair("gem.skill.description.coe.wolf_rake", "Transform into Wolf form and rake enemies, dealing Physical damage. Using this skill instantly switches you to Wolf form; casting a spell reverts you to human.", "变身为狼形态并挥爪攻击，造成物理伤害。使用该技能立即切换为狼形态；施放法术则变回人类。");
        provider.addZh("gem.skill.name.coe.wyvern_rend", "龙形撕裂（Rend）");
        provider.addPair("gem.skill.description.coe.wyvern_rend", "Transform into Wyvern form and rend enemies with claws, dealing Lightning damage. Using this skill instantly switches you to Wyvern form; casting a spell reverts you to human.", "变身为龙形态并以利爪撕裂敌人，造成闪电伤害。使用该技能立即切换为龙形态；施放法术则变回人类。");

        provider.addZh("gem.skill.name.coe.spark", "电弧");
        provider.addPair("gem.skill.description.coe.spark", "Release a jumping arc of lightning that deals Lightning damage and applies Shock on hit.", "释放一束跳跃的电弧，命中时造成闪电伤害并施加感电。");
        provider.addZh("gem.skill.name.coe.rolling_slam", "滚动猛击");
        provider.addPair("gem.skill.description.coe.rolling_slam", "Charge forward and slam the ground, dealing area Physical damage and building Stun.", "蓄力后向前猛冲砸地，造成范围物理伤害并累积晕眩。");
        provider.addZh("gem.skill.name.coe.shockwave_totem", "震波图腾");
        provider.addPair("gem.skill.description.coe.shockwave_totem", "Summon a totem that lasts 15s, striking the nearest enemy every second for Physical damage. Totem attacks inherit your support gem effects.", "立起一座图腾驻场 15 秒，每秒震击附近最近的敌人，造成物理伤害。图腾攻击继承你的辅助宝石效果。");
        provider.addZh("gem.skill.name.coe.ancestral_warrior_totem", "先祖战士图腾");
        provider.addPair("gem.skill.description.coe.ancestral_warrior_totem", "Summon an Ancestral totem that lasts 15s, rapidly striking nearby enemies for Physical damage. Totem attacks inherit your support gem effects.", "立起一座先祖图腾驻场 15 秒，快速打击近身敌人，造成物理伤害。图腾攻击继承你的辅助宝石效果。");
        provider.addZh("gem.skill.name.coe.raise_zombie", "唤起僵尸");
        provider.addPair("gem.skill.description.coe.raise_zombie", "Summon 2 Zombies to fight for you for 30s. Minions are hostile only to enemies and will not attack you.", "召唤 2 具僵尸为你作战 30 秒。召唤物只与敌对生物互相为敌，不会攻击你。");
        provider.addZh("gem.skill.name.coe.herald_of_ash", "灰烬之捷");
        provider.addPair("gem.skill.description.coe.herald_of_ash", "Continuously burn nearby enemies; kills trigger a Fire explosion.", "持续灼烧附近敌人，击杀时引发火焰爆炸。");
        provider.addZh("gem.skill.name.coe.incinerate", "火焰喷射");
        provider.addPair("gem.skill.description.coe.incinerate", "Channel a beam of fire that burns enemies in front of you.", "引导一道火焰光束持续灼烧前方敌人。");
        provider.addZh("gem.skill.name.coe.fireball", "火球");
        provider.addPair("gem.skill.description.coe.fireball", "Throw a fireball that deals Fire damage and Ignites on hit.", "投掷一枚火球，命中时造成火焰伤害并点燃。");
        provider.addZh("gem.skill.name.coe.lightning_arrow", "闪电箭矢");
        provider.addPair("gem.skill.description.coe.lightning_arrow", "Fire an arrow with added Lightning damage that applies Shock on hit.", "射出一箭附加闪电伤害，命中时造成感电。");
        provider.addZh("gem.skill.name.coe.frost_nova", "冰霜新星");
        provider.addPair("gem.skill.description.coe.frost_nova", "Burst ice from your location, dealing Cold damage and Chilling enemies.", "以自身为中心爆发寒冰，造成冰霜伤害并冰缓敌人。");
        provider.addZh("gem.skill.name.coe.leap_slam", "跃击");
        provider.addPair("gem.skill.description.coe.leap_slam", "Leap to a target and slam down, dealing area Physical damage.", "跃向目标点砸地，造成范围物理伤害。");
        provider.addZh("gem.skill.name.coe.frostbolt", "冰霜箭");
        provider.addPair("gem.skill.description.coe.frostbolt", "Release a homing ice shard that deals Cold damage and Chills, Freezing enemies on hit.", "释放追踪冰晶，命中造成冰霜伤害并冰缓、冻结敌人。");
        provider.addZh("gem.skill.name.coe.arc", "电弧连锁");
        provider.addPair("gem.skill.description.coe.arc", "Release a chaining arc of lightning that bounces between enemies and applies Shock.", "释放链跳电弧，在敌人之间弹射并施加感电。");
        provider.addZh("gem.skill.name.coe.lightning_strike", "闪电打击");
        provider.addPair("gem.skill.description.coe.lightning_strike", "Leap to a target and erupt lightning from your location, causing Stun and Knockback.", "跃向目标以自身为中心爆发闪电，造成眩晕与位移。");
        provider.addZh("gem.skill.name.coe.earthquake", "地震");
        provider.addPair("gem.skill.description.coe.earthquake", "Channel then slam the ground, causing ongoing tremors that deal area Physical damage.", "蓄力后猛砸地面，引发持续震荡造成范围物理伤害。");
        provider.addZh("gem.skill.name.coe.toxic_rain", "剧毒雨");
        provider.addPair("gem.skill.description.coe.toxic_rain", "Rain virulent poison over an area, dealing Chaos damage and applying Poison and Bleeding.", "在目标区域降下剧毒之雨，造成混沌伤害并施加中毒与流血。");
        provider.addZh("gem.skill.name.coe.cyclone", "旋风");
        provider.addPair("gem.skill.description.coe.cyclone", "Continuously spin and slash nearby enemies while moving forward.", "持续旋转挥砍周围敌人，并随移动向前推进。");
        provider.addZh("gem.skill.name.coe.shockwave", "冲击波");
        provider.addPair("gem.skill.description.coe.shockwave", "Release a shockwave that knocks back and damages enemies in front of you.", "释放一道冲击波，击退并伤害前方敌人。");
        provider.addZh("gem.skill.name.coe.aftershock", "余震");
        provider.addPair("gem.skill.description.coe.aftershock", "Trigger a secondary area Physical damage at the slam impact point.", "在猛击落点引发二次范围物理伤害。");
        provider.addZh("gem.skill.name.coe.firestorm", "火雨");
        provider.addPair("gem.skill.description.coe.firestorm", "Call down a firestorm at the kill location, continuously burning enemies in the area.", "在击杀处降下火焰风暴，持续灼烧范围内的敌人。");
        provider.addZh("gem.skill.name.coe.blink", "闪现");
        // 真值（poe2wiki / poe2db，0.3 Third Edict）：Blink 为 60 精魂的 Travel 精魂宝石，
        // "While active, replaces your dodge roll with a short-cooldown Spell that allows you to tunnel
        //  through space, instantly reappearing a medium distance away."
        // Teleports (4—4.9) metres / Cooldown (4.50—3.50)s / Reservation 60 Spirit。
        // **无落点范围伤害**（旧描述「对落点周围敌人造成物理伤害」系编造，已按真值订正）。
        provider.addPair("gem.skill.description.coe.blink", "While active, replaces your dodge roll with a very short-cooldown spell that lets you tunnel through space, instantly reappearing a medium distance away. Teleport 4m, Cooldown 4.5s.", "启动时，以一个冷却时间极短的法术取代你的闪避翻滚，让你穿越空间，立即现身于中距离外的一处。传送 4 米，冷却 4.5 秒。");
        provider.addZh("gem.skill.name.coe.volcanic_fissure", "火山裂隙");
        provider.addPair("gem.skill.description.coe.volcanic_fissure", "Slam the ground to open a lava fissure that burns and knocks back enemies.", "猛砸地面引发熔岩裂隙，持续灼烧并击退敌人。");
        provider.addZh("gem.skill.name.coe.ice_shard", "冰锥");
        provider.addPair("gem.skill.description.coe.ice_shard", "Fire an ice shard dealing Cold damage that Chills and Freezes enemies.", "射出冰锥，造成冰霜伤害并冰缓、冻结敌人。");
        provider.addZh("gem.skill.name.coe.boneshatter", "碎骨");
        provider.addPair("gem.skill.description.coe.boneshatter", "Strike enemies dealing area Physical damage and building Stun.", "猛击敌人造成范围物理伤害并累积晕眩。");
        provider.addZh("gem.skill.name.coe.sunder", "裂地");
        provider.addPair("gem.skill.description.coe.sunder", "Cleave the ground dealing area Physical damage.", "劈开地面造成范围物理伤害。");
        provider.addZh("gem.skill.name.coe.perforate", "穿刺");
        provider.addPair("gem.skill.description.coe.perforate", "Lunge forward dealing area Physical damage.", "向前突刺造成范围物理伤害。");
        provider.addZh("gem.skill.name.coe.ground_slam", "地裂斩");
        provider.addPair("gem.skill.description.coe.ground_slam", "Wide sweep dealing Physical damage.", "大范围横扫造成物理伤害。");
        provider.addZh("gem.skill.name.coe.rupture", "破裂");
        provider.addPair("gem.skill.description.coe.rupture", "Rend enemies dealing area Physical damage.", "撕裂敌人造成范围物理伤害。");
        provider.addZh("gem.skill.name.coe.perfect_strike", "完美打击");
        provider.addPair("gem.skill.description.coe.perfect_strike", "Area Physical strike with high Critical Chance.", "高暴击的范围物理打击。");
        provider.addZh("gem.skill.name.coe.stampede", "践踏");
        provider.addPair("gem.skill.description.coe.stampede", "Repeatedly stomp dealing area Physical damage.", "连续猛踩造成范围物理伤害。");
        provider.addZh("gem.skill.name.coe.galvanic_shards", "电花");
        provider.addPair("gem.skill.description.coe.galvanic_shards", "Fire multiple Lightning shards dealing Lightning damage.", "射出多枚闪电弹片造成闪电伤害。");
        provider.addZh("gem.skill.name.coe.lightning_spear", "闪电矛");
        provider.addPair("gem.skill.description.coe.lightning_spear", "Throw a Lightning spear dealing Lightning damage.", "掷出一杆闪电长矛造成闪电伤害。");
        provider.addZh("gem.skill.name.coe.puncture", "穿刺箭");
        provider.addPair("gem.skill.description.coe.puncture", "Fire a piercing arrow dealing Physical damage.", "射出穿刺箭造成物理伤害。");
        provider.addZh("gem.skill.name.coe.whirling_slash", "旋风斩");
        provider.addPair("gem.skill.description.coe.whirling_slash", "Continuously spin and slash nearby enemies.", "持续旋转挥砍周围敌人。");
        provider.addZh("gem.skill.name.coe.lacerate", "割裂");
        provider.addPair("gem.skill.description.coe.lacerate", "Slash dealing area Physical damage.", "挥砍造成范围物理伤害。");
        provider.addZh("gem.skill.name.coe.ball_lightning", "电球");
        provider.addPair("gem.skill.description.coe.ball_lightning", "Fire a Lightning ball that continually discharges, dealing Lightning damage.", "射出持续放电的闪电球造成闪电伤害。");
        provider.addZh("gem.skill.name.coe.comet", "彗星");
        provider.addPair("gem.skill.description.coe.comet", "Hurl a massive ice ball dealing high Cold damage.", "投掷巨大冰球造成高额冰霜伤害。");
        provider.addZh("gem.skill.name.coe.frost_bomb", "冰霜炸弹");
    }

    private static void addAllPart2(LangProvider provider) {

        provider.addPair("gem.skill.description.coe.frost_bomb", "Throw a Frost bomb dealing Cold damage.", "投掷冰霜炸弹造成冰霜伤害。");
        provider.addZh("gem.skill.name.coe.war_banner", "战旗");
        provider.addPair("gem.skill.description.coe.war_banner", "Plant a War Banner aura (radius 6m, 6s): nearby allies gain 25% more Attack Damage, 50% increased Accuracy, and 25% Attack Speed. The banner itself deals no damage.",
                "展开战旗光环（半径 6 米，持续 6 秒）：范围内友军获得 25% 更多攻击伤害、50% 命中值与 25% 攻击速度。战旗本身不造成伤害。");
        provider.addZh("gem.skill.name.coe.seismic_cry", "震地战吼");
        provider.addPair("gem.skill.description.coe.seismic_cry", "Let out a damaging warcry (radius 4m) that deals Physical damage to nearby enemies and Knocks them back.",
                "发出造成伤害的战吼（半径 4 米），对周围敌人造成物理伤害并将其击退。");
        provider.addZh("gem.skill.name.coe.blood_hunt", "猎血寻腥");
        provider.addPair("gem.skill.description.coe.blood_hunt", "Strike enemies dealing area Physical damage.", "猛击敌人造成范围物理伤害。");
        provider.addZh("gem.skill.name.coe.chaos_bolt", "混沌箭");
        provider.addPair("gem.skill.description.coe.chaos_bolt", "Fire a Chaos bolt dealing Chaos damage.", "射出混沌箭矢造成混沌伤害。");

        // <<<AUTO_START>>>
        provider.addZh("gem.skill.name.coe.armor_breaker", "摧甲重击");
        provider.addPair("gem.skill.description.coe.armor_breaker", "Area slam dealing Physical damage.", "范围猛击造成物理伤害。");
        provider.addZh("gem.skill.name.coe.berserk", "盛怒");
        provider.addPair("gem.skill.description.coe.berserk", "While active, empowers your Rage, increasing its effect by (40-59)%.", "激活时强化你的怒气，使你的怒气效果提高 (40—59)%。");
        provider.addZh("gem.skill.name.coe.earthshatter", "尖刺战吼");
        provider.addPair("gem.skill.description.coe.earthshatter", "Area slam dealing Physical damage.", "范围猛击造成物理伤害。");
        provider.addZh("gem.skill.name.coe.eternal_rage", "永恒狂怒");
        provider.addPair("gem.skill.description.coe.eternal_rage", "While active, continuously regenerates Rage, restoring (0.25-5) Rage per second (scales with level).",
                "激活时持续回复怒气，每秒回复 (0.25—5) 点怒气（随等级提升）。");
        provider.addZh("gem.skill.name.coe.forge_hammer", "锻造之锤");
        provider.addPair("gem.skill.description.coe.forge_hammer", "Area slam dealing Fire damage.", "范围猛击造成火焰伤害。");
        provider.addZh("gem.skill.name.coe.fortifying_cry", "坚韧战吼");
        provider.addPair("gem.skill.description.coe.fortifying_cry", "Perform a Warcry that grants Guard and triggers Shield Wave when your Shield Attacks deal damage. Bypass its cooldown by expending an Endurance Charge.", "发出战吼获得守护，并在你的盾牌攻击造成伤害时触发盾波。消耗一道忍耐充能可绕过其冷却。");
        provider.addZh("gem.skill.name.coe.hammer_of_the_gods", "诸天之锤");
        provider.addPair("gem.skill.description.coe.hammer_of_the_gods", "Area slam dealing Physical damage.", "范围猛击造成物理伤害。");
        provider.addZh("gem.skill.name.coe.infernal_cry", "炽焰战吼");
        provider.addPair("gem.skill.description.coe.infernal_cry", "Area slam dealing Fire damage.", "范围猛击造成火焰伤害。");
        provider.addZh("gem.skill.name.coe.iron_ward", "钢铁看守");
        provider.addPair("gem.skill.description.coe.iron_ward", "While active, gain (200-280) Evasion and (10-20)% reduced Damage taken.", "激活时，获得 (200—280) 闪避值，并减少承受伤害 (10—20)%。");
        provider.addZh("gem.skill.name.coe.execute", "处决");
        provider.addPair("gem.skill.description.coe.execute", "Area slam dealing Physical damage; delivers a killing blow to enemies below 35% Life.", "范围猛击造成物理伤害，对生命低于 35% 的敌人造成致命一击。");
        provider.addZh("gem.skill.name.coe.magma_barrier", "岩浆屏障");
        provider.addPair("gem.skill.description.coe.magma_barrier", "While active, gain 30% of Armour as Extra Evasion.", "激活时，将护甲的 30% 转化为额外闪避值。");
        provider.addZh("gem.skill.name.coe.molten_blast", "熔岩爆裂");
        provider.addPair("gem.skill.description.coe.molten_blast", "Weapon attack projectiles deal Fire damage.", "武器攻击投射物造成火焰伤害。");
        provider.addZh("gem.skill.name.coe.overwhelming_presence", "压迫气场");
        provider.addPair("gem.skill.description.coe.overwhelming_presence", "While active, radiates an aura that makes enemies in your Presence easier to Stun and inflict Elemental Ailments.\nEnemies in your Presence have (15-24)% reduced Ailment Threshold\nEnemies in your Presence have (15-25)% reduced Stun Threshold", "启动时，散发一个光环，使你存在范围内的敌人更容易被眩晕和造成元素异常状态。\n你存在范围内的敌人减少 (15—24)% 元素异常状态门槛\n你存在范围内的敌人减少 (15—25)% 晕眩门槛");
        provider.addZh("gem.skill.name.coe.scavenged_plating", "装甲回收");
        provider.addPair("gem.skill.description.coe.scavenged_plating", "While active, gain 15% of Energy Shield as Extra Armour.", "激活时，将能量护盾的 15% 转化为额外护甲。");
        provider.addZh("gem.skill.name.coe.shield_wall", "盾墙");
        provider.addPair("gem.skill.description.coe.shield_wall", "Ram your Shield into the ground, throwing up a wall of earth that holds back enemies. Shatter it with Slams, Warcries, or Shield Charge to explode and damage nearby enemies.", "将盾牌砸入地面筑起阻挡敌人的土墙。用猛击、战吼或盾击将其击碎，爆炸并伤害附近敌人。");
        provider.addZh("gem.skill.name.coe.time_of_need", "紧急时刻");
        provider.addPair("gem.skill.description.coe.time_of_need", "While active, periodically calls down a holy blessing that heals you and removes Curses and Elemental Ailments from you.", "持续期间，定期降下神圣祝福治疗你，并移除你身上的诅咒与元素异常。");
        provider.addZh("gem.skill.name.coe.alchemist_s_boon", "炼金师的恩典");
        provider.addPair("gem.skill.description.coe.alchemist_s_boon", "While active, you gain Flask charges passively.", "激活时被动获得药剂充能。");
        provider.addZh("gem.skill.name.coe.bloodhound_s_mark", "血犬印记");
        provider.addPair("gem.skill.description.coe.bloodhound_s_mark", "Mark a target, causing it to take increased Damage. The Marked enemy drops additional items when slain, and you gain Culling Strike against it.", "标记一个目标，使其受到更多伤害。被标记敌人死亡时掉落额外物品，且你对其获得斩杀。");
        provider.addZh("gem.skill.name.coe.combat_frenzy", "战斗狂怒");
        provider.addPair("gem.skill.description.coe.combat_frenzy", "While active, Freezing, Shocking or Pinning an enemy grants a Rage orb; this effect can trigger at most once every 8s.", "激活时，冻结、电击或钉住敌人会获得一枚狂怒球，此效果每 8 秒只能触发一次");
        provider.addZh("gem.skill.name.coe.cull_the_weak", "终结弱者");
        provider.addPair("gem.skill.description.coe.cull_the_weak", "Area slam dealing Physical damage.", "范围猛击造成物理伤害。");
        provider.addZh("gem.skill.name.coe.disengage", "退避三舍");
        provider.addPair("gem.skill.description.coe.disengage", "Blink and deal area Physical damage.", "位移并造成范围物理伤害。");
        provider.addZh("gem.skill.name.coe.electrocuting_arrow", "电殛箭");
        provider.addPair("gem.skill.description.coe.electrocuting_arrow", "Weapon attack projectiles deal Lightning damage.", "武器攻击投射物造成闪电伤害。");
        provider.addZh("gem.skill.name.coe.elemental_sundering", "元素碎裂");
        provider.addPair("gem.skill.description.coe.elemental_sundering", "Area slam dealing Fire damage.", "范围猛击造成火焰伤害。");
        provider.addZh("gem.skill.name.coe.escape_shot", "逃脱射击");
        provider.addPair("gem.skill.description.coe.escape_shot", "Weapon attack projectiles deal Cold damage.", "武器攻击投射物造成冰霜伤害。");
        provider.addZh("gem.skill.name.coe.explosive_spear", "爆破战矛");
        provider.addPair("gem.skill.description.coe.explosive_spear", "Weapon attack projectiles deal Fire damage.", "武器攻击投射物造成火焰伤害。");
        provider.addZh("gem.skill.name.coe.fangs_of_frost", "刺骨寒冰");
        provider.addPair("gem.skill.description.coe.fangs_of_frost", "Area slam dealing Cold damage.", "范围猛击造成冰霜伤害。");
        provider.addZh("gem.skill.name.coe.freezing_salvo", "冻结齐射");
        provider.addPair("gem.skill.description.coe.freezing_salvo", "Weapon attack projectiles deal Cold damage.", "武器攻击投射物造成冰霜伤害。");
        provider.addZh("gem.skill.name.coe.gas_arrow", "瓦斯之箭");
        provider.addPair("gem.skill.description.coe.gas_arrow", "Weapon attack projectiles deal Chaos damage.", "武器攻击投射物造成混沌伤害。");
        provider.addZh("gem.skill.name.coe.glacial_lance", "冰川长枪");
        provider.addPair("gem.skill.description.coe.glacial_lance", "Weapon attack projectiles deal Cold damage.", "武器攻击投射物造成冰霜伤害。");
        provider.addZh("gem.skill.name.coe.ice_shot", "冰霜射击");
        provider.addPair("gem.skill.description.coe.ice_shot", "Weapon attack projectiles deal Cold damage.", "武器攻击投射物造成冰霜伤害。");
        provider.addZh("gem.skill.name.coe.lightning_rod", "引雷针");
        provider.addPair("gem.skill.description.coe.lightning_rod", "Weapon attack projectiles deal Lightning damage.", "武器攻击投射物造成闪电伤害。");
        provider.addZh("gem.skill.name.coe.magnetic_salvo", "磁性齐射");
        provider.addPair("gem.skill.description.coe.magnetic_salvo", "Weapon attack projectiles deal Lightning damage.", "武器攻击投射物造成闪电伤害。");
        provider.addZh("gem.skill.name.coe.plague_bearer", "瘟疫使者");
        provider.addPair("gem.skill.description.coe.plague_bearer", "While active, you harbor a virulent Plague, greatly increasing the Damage of your Ailments.", "激活时，你身染剧毒瘟疫，大幅增强你的异常状态伤害。");
    }

    private static void addAllPart3(LangProvider provider) {

        provider.addZh("gem.skill.name.coe.poisonburst_arrow", "毒爆箭");
        provider.addPair("gem.skill.description.coe.poisonburst_arrow", "Weapon attack projectiles deal Chaos damage.", "武器攻击投射物造成混沌伤害。");
        provider.addZh("gem.skill.name.coe.primal_strikes", "原始打击");
        provider.addPair("gem.skill.description.coe.primal_strikes", "Area slam dealing Lightning damage.", "范围猛击造成闪电伤害。");
        provider.addZh("gem.skill.name.coe.rain_of_arrows", "箭雨");
        provider.addPair("gem.skill.description.coe.rain_of_arrows", "Weapon attack projectiles deal Physical damage.", "武器攻击投射物造成物理伤害。");
        provider.addZh("gem.skill.name.coe.rake", "斜掠");
        provider.addPair("gem.skill.description.coe.rake", "Area slam dealing Physical damage.", "范围猛击造成物理伤害。");
        provider.addZh("gem.skill.name.coe.rapid_assault", "极速进攻");
        provider.addPair("gem.skill.description.coe.rapid_assault", "Area slam dealing Physical damage.", "范围猛击造成物理伤害。");
        provider.addZh("gem.skill.name.coe.shockchain_arrow", "连环震击箭");
        provider.addPair("gem.skill.description.coe.shockchain_arrow", "Weapon attack projectiles deal Lightning damage.", "武器攻击投射物造成闪电伤害。");
        provider.addZh("gem.skill.name.coe.spear_of_solaris", "日耀之矛");
        provider.addPair("gem.skill.description.coe.spear_of_solaris", "Weapon attack projectiles deal Fire damage.", "武器攻击投射物造成火焰伤害。");
        provider.addZh("gem.skill.name.coe.spearfield", "枪林矛阵");
        provider.addPair("gem.skill.description.coe.spearfield", "Area slam dealing Physical damage.", "范围猛击造成物理伤害。");
        provider.addZh("gem.skill.name.coe.spiral_volley", "螺旋齐射");
        provider.addPair("gem.skill.description.coe.spiral_volley", "Weapon attack projectiles deal Physical damage.", "武器攻击投射物造成物理伤害。");
        provider.addZh("gem.skill.name.coe.storm_lance", "岚舞长枪");
        provider.addPair("gem.skill.description.coe.storm_lance", "Weapon attack projectiles deal Lightning damage.", "武器攻击投射物造成闪电伤害。");
        provider.addZh("gem.skill.name.coe.stormcaller_arrow", "唤雷箭");
        provider.addPair("gem.skill.description.coe.stormcaller_arrow", "Weapon attack projectiles deal Lightning damage.", "武器攻击投射物造成闪电伤害。");
        provider.addZh("gem.skill.name.coe.thunderous_leap", "雷鸣飞跃");
        provider.addPair("gem.skill.description.coe.thunderous_leap", "Blink and deal area Lightning damage.", "位移并造成范围闪电伤害。");
        provider.addZh("gem.skill.name.coe.tornado_shot", "龙卷射击");
        provider.addPair("gem.skill.description.coe.tornado_shot", "Weapon attack projectiles deal Physical damage.", "武器攻击投射物造成物理伤害。");
        provider.addZh("gem.skill.name.coe.toxic_domain", "毒性领域");
        provider.addPair("gem.skill.description.coe.toxic_domain", "Area slam dealing Chaos damage.", "范围猛击造成混沌伤害。");
        provider.addZh("gem.skill.name.coe.toxic_growth", "剧毒增生");
        provider.addPair("gem.skill.description.coe.toxic_growth", "Weapon attack projectiles deal Chaos damage.", "武器攻击投射物造成混沌伤害。");
        provider.addZh("gem.skill.name.coe.trail_of_caltrops", "铁蒺藜之路");
        provider.addPair("gem.skill.description.coe.trail_of_caltrops", "While active, your rolls scatter 3 caltrops behind you. When triggered by an enemy, caltrops deal attack-range Physical damage and Maim them (30% reduced Movement Speed for 4s); consumed on trigger, max 20 at once. Caltrop radius 0.5m, lasts 8s.", "激活时，翻滚会在你身后撒下 3 枚铁蒺藜。铁蒺藜被敌人触发时造成攻击范围物理伤害并使其残废（移动速度降低 30%，持续 4 秒）；触发后即销毁，最多同时存在 20 枚。铁蒺藜半径 0.5 米，持续 8 秒。");
        provider.addZh("gem.skill.name.coe.twister", "旋龙");
        provider.addPair("gem.skill.description.coe.twister", "Weapon attack projectiles deal Physical damage.", "武器攻击投射物造成物理伤害。");
        provider.addZh("gem.skill.name.coe.vine_arrow", "藤缠箭");
        provider.addPair("gem.skill.description.coe.vine_arrow", "Weapon attack projectiles deal Chaos damage.", "武器攻击投射物造成混沌伤害。");
        provider.addZh("gem.skill.name.coe.whirlwind_lance", "旋风投枪");
        provider.addPair("gem.skill.description.coe.whirlwind_lance", "Weapon attack projectiles deal Physical damage.", "武器攻击投射物造成物理伤害。");
        provider.addZh("gem.skill.name.coe.wind_serpent_s_fury", "腾蛇之怒");
        provider.addPair("gem.skill.description.coe.wind_serpent_s_fury", "Area slam dealing Physical damage.", "范围猛击造成物理伤害。");
        provider.addZh("gem.skill.name.coe.archmage", "大法师");
        provider.addPair("gem.skill.description.coe.archmage", "While active, your non-channeled spells gain added Lightning damage and added maximum Mana cost, both based on your maximum Mana. (Per 100 maximum Mana, non-channeled spells gain Lightning damage equal to 4% of damage; added Mana cost (8-6.1)% of maximum Mana.)", "激活时，你的非引导法术获得额外闪电伤害，并获得额外最大魔力消耗，两者均基于你的最大魔力而定。（每 100 点最大魔力，非引导法术获得等同 4% 伤害的额外闪电伤害；额外消耗最大魔力的 (8—6.1)%。）");
        provider.addZh("gem.skill.name.coe.bone_blast", "针刺爆发");
        provider.addPair("gem.skill.description.coe.bone_blast", "Area slam dealing Physical damage.", "范围猛击造成物理伤害。");
        provider.addZh("gem.skill.name.coe.bone_cage", "画地为牢");
        provider.addPair("gem.skill.description.coe.bone_cage", "Area slam dealing Physical damage.", "范围猛击造成物理伤害。");
        provider.addZh("gem.skill.name.coe.coiling_bolts", "盘绕魔弹");
        provider.addPair("gem.skill.description.coe.coiling_bolts", "Fire a spell projectile dealing Chaos damage.", "射出法术投射物造成混沌伤害。");
        provider.addZh("gem.skill.name.coe.conductivity", "导电");
        provider.addPair("gem.skill.description.coe.conductivity", "Curse: applies Conductivity to nearby enemies (reduced Lightning Resistance).", "诅咒：对周围敌人施加导电状态（降低闪电抗性）。");
        provider.addZh("gem.skill.name.coe.contagion", "瘟疫");
        provider.addPair("gem.skill.description.coe.contagion", "Area slam dealing Chaos damage.", "范围猛击造成混沌伤害。");
        provider.addZh("gem.skill.name.coe.convalescence", "康复");
        provider.addPair("gem.skill.description.coe.convalescence", "Persistent aura: Energy Shield begins recharging instantly and cannot be interrupted.", "常驻光环：能量护盾立即开始充能，且充能过程不可被打断。");
        provider.addZh("gem.skill.name.coe.decompose", "降解");
        provider.addPair("gem.skill.description.coe.decompose", "Area slam dealing Chaos damage.", "范围猛击造成混沌伤害。");
        provider.addZh("gem.skill.name.coe.despair", "绝望");
        provider.addPair("gem.skill.description.coe.despair", "Curse: applies Despair to nearby enemies (reduced Chaos Resistance).", "诅咒：对周围敌人施加绝望状态（降低混沌抗性）。");
        provider.addZh("gem.skill.name.coe.detonate_dead", "爆灵术");
        provider.addPair("gem.skill.description.coe.detonate_dead", "Area slam dealing Fire damage.", "范围猛击造成火焰伤害。");
        provider.addZh("gem.skill.name.coe.elemental_conflux", "元素汇流");
        provider.addPair("gem.skill.description.coe.elemental_conflux", "Tap into a current of raw Elemental power, greatly increasing your Damage while active.", "汲取原始元素之力，激活时大幅增强你的伤害。");
        provider.addZh("gem.skill.name.coe.elemental_weakness", "元素要害");
        provider.addPair("gem.skill.description.coe.elemental_weakness", "Curse: applies Elemental Weakness to nearby enemies.", "诅咒：对周围敌人施加元素要害状态。");
        provider.addZh("gem.skill.name.coe.ember_fusillade", "灰烬连射");
        provider.addPair("gem.skill.description.coe.ember_fusillade", "Fire a spell projectile dealing Fire damage.", "射出法术投射物造成火焰伤害。");
        provider.addZh("gem.skill.name.coe.enervating_nova", "衰颓新星");
        provider.addPair("gem.skill.description.coe.enervating_nova", "Area slam dealing Lightning damage.", "范围猛击造成闪电伤害。");
        provider.addZh("gem.skill.name.coe.enfeeble", "衰弱");
        provider.addPair("gem.skill.description.coe.enfeeble", "Curse: applies Enfeeble to nearby enemies (reduced Damage dealt).", "诅咒：对周围敌人施加衰弱状态（降低其造成伤害）。");
        provider.addZh("gem.skill.name.coe.essence_drain", "精华吸取");
        provider.addPair("gem.skill.description.coe.essence_drain", "Fire a spell projectile dealing Chaos damage.", "射出法术投射物造成混沌伤害。");
        provider.addZh("gem.skill.name.coe.eye_of_winter", "凛冬之眼");
        provider.addPair("gem.skill.description.coe.eye_of_winter", "Fire a spell projectile dealing Cold damage.", "射出法术投射物造成冰霜伤害。");
    }

    private static void addAllPart4(LangProvider provider) {

        provider.addZh("gem.skill.name.coe.feast_of_flesh", "血肉盛宴");
        provider.addPair("gem.skill.description.coe.feast_of_flesh", "Area slam dealing Physical damage.", "范围猛击造成物理伤害。");
        provider.addZh("gem.skill.name.coe.firebolt", "火焰弹");
        provider.addPair("gem.skill.description.coe.firebolt", "Fire a spell projectile dealing Fire damage.", "射出法术投射物造成火焰伤害。");
        provider.addZh("gem.skill.name.coe.flame_wall", "烈焰之墙");
        provider.addPair("gem.skill.description.coe.flame_wall", "Create a wall of fire that deals Fire Damage and Ignites enemies who touch it. Spells cast through the wall gain added Fire Damage.", "制造一道火焰墙造成火焰伤害并点燃触碰的敌人。穿过火墙施放的法术获得额外火焰伤害。");
        provider.addZh("gem.skill.name.coe.flammability", "易燃");
        provider.addPair("gem.skill.description.coe.flammability", "Curse: applies Flammability to nearby enemies (reduced Fire Resistance).", "诅咒：对周围敌人施加易燃状态（降低火焰抗性）。");
        provider.addZh("gem.skill.name.coe.freezing_shards", "急冻碎片");
        provider.addPair("gem.skill.description.coe.freezing_shards", "Fire a spell projectile dealing Cold damage.", "射出法术投射物造成冰霜伤害。");
        provider.addZh("gem.skill.name.coe.frost_darts", "冰霜飞镖");
        provider.addPair("gem.skill.description.coe.frost_darts", "Fire a spell projectile dealing Cold damage.", "射出法术投射物造成冰霜伤害。");
        provider.addZh("gem.skill.name.coe.frost_wall", "冰墙");
        provider.addPair("gem.skill.description.coe.frost_wall", "Create a wall of Ice Crystals which holds back enemies, dealing Cold Damage. The Crystals explode when destroyed, damaging nearby enemies.", "制造一道冰晶墙阻挡敌人并造成冰霜伤害。冰晶被摧毁时爆炸，对附近敌人造成伤害。");
        provider.addZh("gem.skill.name.coe.fulmination", "轰鸣烈焰");
        provider.addPair("gem.skill.description.coe.fulmination", "While active, Non-Channelling Lightning Spells deal 15% increased Lightning Damage.", "激活时，非引导闪电法术造成 15% 提高的闪电伤害。");
        provider.addZh("gem.skill.name.coe.galvanic_field", "电流场");
        provider.addPair("gem.skill.description.coe.galvanic_field", "Area slam dealing Lightning damage.", "范围猛击造成闪电伤害。");
        provider.addZh("gem.skill.name.coe.heart_of_ice", "冰雪之心");
        provider.add("gem.skill.description.coe.heart_of_ice", "Chills all enemies in your Presence as though dealing (26-888) to (39-1332) Cold damage");
        provider.addZh("gem.skill.name.coe.hexblast", "魔蛊爆炸");
        provider.addPair("gem.skill.description.coe.hexblast", "Area slam dealing Chaos damage.", "范围猛击造成混沌伤害。");
        provider.addZh("gem.skill.name.coe.hypothermia", "急冻");
        provider.addPair("gem.skill.description.coe.hypothermia", "Curse: applies Hypothermia to nearby enemies (reduced Cold Resistance).", "诅咒：对周围敌人施加急冻状态（降低冰霜抗性）。");
        provider.addZh("gem.skill.name.coe.ice_nova", "冰霜新星");
        provider.addPair("gem.skill.description.coe.ice_nova", "Area slam dealing Cold damage.", "范围猛击造成冰霜伤害。");
        provider.addZh("gem.skill.name.coe.icestorm", "冰风暴");
        provider.addPair("gem.skill.description.coe.icestorm", "Area slam dealing Cold damage.", "范围猛击造成冰霜伤害。");
        provider.addZh("gem.skill.name.coe.lightning_bolt", "闪电");
        provider.addPair("gem.skill.description.coe.lightning_bolt", "Area slam dealing Lightning damage.", "范围猛击造成闪电伤害。");
        provider.addZh("gem.skill.name.coe.lightning_conduit", "闪电通道");
        provider.addPair("gem.skill.description.coe.lightning_conduit", "Area slam dealing Lightning damage.", "范围猛击造成闪电伤害。");
        provider.addZh("gem.skill.name.coe.lightning_warp", "闪电传送");
        provider.addPair("gem.skill.description.coe.lightning_warp", "Area slam dealing Lightning damage.", "范围猛击造成闪电伤害。");
        provider.addZh("gem.skill.name.coe.living_bomb", "活动炸弹");
        provider.addPair("gem.skill.description.coe.living_bomb", "Area slam dealing Fire damage.", "范围猛击造成火焰伤害。");
        provider.addZh("gem.skill.name.coe.malice", "怨毒");
        provider.addPair("gem.skill.description.coe.malice", "While active, radiates an aura that periodically inflicts Critical Weakness on enemies in your Presence.\nCritical Weakness lasts 4s\nEnemies in your Presence suffer Critical Weakness every (0.7-0.51)s", "启动时，散发一个光环，周期性地使你存在范围内的敌人陷入暴击虚弱。\n暴击虚弱持续时间为 4 秒\n你存在范围内的敌人每 (0.7—0.51) 秒承受暴击虚弱");
        provider.addZh("gem.skill.name.coe.mana_remnants", "魔力残片");
        provider.addPair("gem.skill.description.coe.mana_remnants", "While active, enemies you kill affected by an Elemental Ailment have a 25% chance to drop a Mana Remnant; critical hits on Ailment-affected targets also drop one every 2s (remnants last 8s). Picking up a remnant restores Mana and can exceed your Mana cap.", "激活时，被你击杀且受元素异常影响的敌人有 25% 几率产生一枚魔力残片；暴击命中受元素异常影响的目标也会每 2 秒产生一枚（残片持续 8 秒）。拾取魔力残片回复魔力，且可超出魔力上限。");
        provider.addZh("gem.skill.name.coe.mirror_of_refraction", "折射之镜");
        provider.addPair("gem.skill.description.coe.mirror_of_refraction", "While active, take (20-25)% less Damage from Critical Strikes.", "激活时，受到暴击伤害降低 (20—25)%。");
        provider.addZh("gem.skill.name.coe.orb_of_storms", "风暴漩涡");
        provider.addPair("gem.skill.description.coe.orb_of_storms", "Create an Orb of electricity that fires Chaining Lightning bolts at nearby enemies. It leaves a Lightning Infusion Remnant when it expires.", "制造一颗电球，向附近敌人射出可连锁的闪电箭。其消失时留下闪电灌注残迹。");
        provider.addZh("gem.skill.name.coe.blight", "枯萎");
        provider.addPair("gem.skill.description.coe.blight", "Create a pool of Chaos ground at the target that deals ongoing Chaos damage to enemies standing in it.", "在落点生成一片混沌地面，踏入的敌人持续受到混沌伤害。");
        provider.addZh("gem.skill.name.coe.profane_ritual", "不敬之礼");
        provider.addPair("gem.skill.description.coe.profane_ritual", "Area slam dealing Chaos damage.", "范围猛击造成混沌伤害。");
        provider.addZh("gem.skill.name.coe.reap", "赤炼收割");
        provider.addPair("gem.skill.description.coe.reap", "Area slam dealing Physical damage.", "范围猛击造成物理伤害。");
        provider.addZh("gem.skill.name.coe.siphon_elements", "汲取元素");
        // 真值（poe2wiki，Tier 8 / 30 精魂）："While active, has a chance to create an Infusion Remnant
        //  when you Freeze, Shock or Ignite a target."；冰霜「每枚暴击球 5%」，火焰/闪电固定 5%，
        // 且火焰/闪电要求目标原本未处于该异常（non-Ignited / non-Shocked）。
        provider.addPair("gem.skill.description.coe.siphon_elements", "While active, Freezing, Shocking or Igniting an enemy has a chance to create a corresponding Infusion Remnant. Cold: 5% chance per Power Charge; Fire and Lightning: 5% chance, and only on targets not already suffering that Ailment.", "激活时，冻结、电击或点燃敌人时有几率生成一个对应元素的灌入残迹。冰霜：每枚暴击球 5% 几率；火焰与闪电：5% 几率，且仅对原本未处于该异常的目标生效。");
        provider.addZh("gem.skill.name.coe.snap", "激变");
        provider.addPair("gem.skill.description.coe.snap", "Area slam dealing Fire damage.", "范围猛击造成火焰伤害。");
        provider.addZh("gem.skill.name.coe.solar_orb", "日曜球");
        provider.addPair("gem.skill.description.coe.solar_orb", "Create a fiery Orb that periodically releases fiery pulses. Enemies very close to the Orb are Ignited.", "制造一颗火球，周期性释放火焰脉冲。极靠近火球的敌人被点燃。");
        provider.addZh("gem.skill.name.coe.soulrend", "裂魂术");
        provider.addPair("gem.skill.description.coe.soulrend", "Fire a spell projectile dealing Chaos damage.", "射出法术投射物造成混沌伤害。");
        provider.addZh("gem.skill.name.coe.temporal_chains", "时空锁链");
        provider.addPair("gem.skill.description.coe.temporal_chains", "Curse: Slows all nearby enemies.", "诅咒：减速周围所有敌人。");
        provider.add("gem.skill.name.coe.untether", "");
        provider.addPair("gem.skill.description.coe.untether", "Area slam dealing Physical damage.", "范围猛击造成物理伤害。");
        provider.addZh("gem.skill.name.coe.volatile_dead", "灵体火球");
        provider.addPair("gem.skill.description.coe.volatile_dead", "Area slam dealing Fire damage.", "范围猛击造成火焰伤害。");
        provider.addZh("gem.skill.name.coe.vulnerability", "脆弱");
        provider.addPair("gem.skill.description.coe.vulnerability", "Curse: applies Vulnerability to nearby enemies (hits ignore part of Armour).", "诅咒：对周围敌人施加脆弱状态（命中无视部分护甲）。");
        provider.addZh("gem.skill.name.coe.withering_presence", "凋零光环");
        provider.addPair("gem.skill.description.coe.withering_presence", "While active, radiates an aura that periodically Withers enemies in your Presence.\nWither lasts 6s\nEnemies in your Presence suffer Wither every (1.8-1.42)s", "启动时，散发一个光环，周期性地使你存在范围内的敌人凋零。\n凋零持续时间为 6 秒\n你存在范围内的敌人每 (1.8—1.42) 秒承受凋零");
        provider.addZh("gem.skill.name.coe.explosive_grenade", "爆裂手雷");
        provider.addPair("gem.skill.description.coe.explosive_grenade", "Throw a grenade that detonates after a 3s fuse, dealing Fire damage to enemies within 2m.", "投掷一枚手雷，引信（3 秒）到期后引爆，对半径 2 米内敌人造成火焰伤害。");
        provider.addZh("gem.skill.name.coe.voltaic_grenade", "伏雷手雷");
        provider.addPair("gem.skill.description.coe.voltaic_grenade", "Throw a grenade that detonates after a 3s fuse, dealing Lightning damage to enemies within 2m.", "投掷一枚手雷，引信（3 秒）到期后引爆，对半径 2 米内敌人造成闪电伤害。");
        // consume_corpse（权威 all_skills.json：id=consume_corpse，name_zh=null，故仅写英文原名）
        provider.add("gem.skill.name.coe.consume_corpse", "Consume Corpse");
        provider.add("gem.skill.description.coe.consume_corpse", "Consume Corpses in an area");
        // <<<AUTO_END>>>
    
    }

}
