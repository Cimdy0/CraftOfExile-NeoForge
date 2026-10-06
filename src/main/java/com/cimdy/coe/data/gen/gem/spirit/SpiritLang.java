package com.cimdy.coe.data.gen.gem.spirit;

import com.cimdy.coe.data.gen.lang.LangProvider;

public final class SpiritLang {
    private SpiritLang() {
    }

    public static void addAll(LangProvider provider) {
        provider.addZh("gem.spirit.name.coe.wind_dancer", "风舞者");
        provider.addPair("gem.spirit.description.coe.wind_dancer", "Spirit aura: gain increased Evasion, stacking each time you avoid being hit consecutively.", "精魂光环：获得更多闪避，连续未被击中时逐层叠加。");
        provider.addZh("gem.spirit.name.coe.herald_of_ice", "寒冰之捷");
        provider.addPair("gem.spirit.description.coe.herald_of_ice", "Spirit aura: your hits gain Cold damage and Chill and Shatter enemies.", "精魂光环：为你的打击附加冰霜伤害并冰缓、碎裂敌人。");
        provider.addZh("gem.spirit.name.coe.herald_of_thunder", "雷之捷");
        provider.addPair("gem.spirit.description.coe.herald_of_thunder", "Spirit aura: your hits gain Lightning damage and Shock.", "精魂光环：为你的打击附加闪电伤害并感电。");
        provider.addZh("gem.spirit.name.coe.herald_of_plague", "瘟疫之捷");
        provider.addPair("gem.spirit.description.coe.herald_of_plague", "Spirit aura: your hits gain Chaos damage and inflict Poison.", "精魂光环：为你的打击附加混沌伤害并施加中毒。");
        provider.addZh("gem.spirit.name.coe.grace", "优雅");
        provider.addPair("gem.spirit.description.coe.grace", "Spirit aura: increases your Evasion.", "精魂光环：提升你的闪避值。");
        provider.addZh("gem.spirit.name.coe.purity_of_fire", "纯净火焰");
        provider.addPair("gem.spirit.description.coe.purity_of_fire", "Spirit aura: increases your Fire Resistance.", "精魂光环：提升你的火焰抗性。");
        provider.addZh("gem.spirit.name.coe.purity_of_ice", "纯净冰霜");
        provider.addPair("gem.spirit.description.coe.purity_of_ice", "Spirit aura: increases your Cold Resistance.", "精魂光环：提升你的冰霜抗性。");
        provider.addZh("gem.spirit.name.coe.purity_of_lightning", "纯净闪电");
        provider.addPair("gem.spirit.description.coe.purity_of_lightning", "Spirit aura: increases your Lightning Resistance.", "精魂光环：提升你的闪电抗性。");
        provider.addZh("gem.spirit.name.coe.impurity", "纯净混沌");
        provider.addPair("gem.spirit.description.coe.impurity", "Spirit aura: increases your Chaos Resistance.", "精魂光环：提升你的混沌抗性。");
        provider.addZh("gem.spirit.name.coe.clarity", "清晰");
        provider.addPair("gem.spirit.description.coe.clarity", "Spirit aura: increases your Mana Regeneration Rate.", "精魂光环：提升你的魔力回复速率。");
        provider.addZh("gem.spirit.name.coe.wrath", "狂怒");
        provider.addPair("gem.spirit.description.coe.wrath", "Spirit aura: your Attacks gain added Lightning damage.", "精魂光环：为你的攻击附加闪电伤害。");
        provider.addZh("gem.spirit.name.coe.herald_of_blood", "血之捷");
        provider.addPair("gem.spirit.description.coe.herald_of_blood", "Spirit aura: your hits gain Physical damage; kills trigger a Physical explosion.", "精魂光环：为你的打击附加物理伤害，击杀引发物理爆发。");
        // herald_of_ash 同时是技能宝石与精魂宝石（共用 id red/herald_of_ash），原 gem.skill.* 键致译文碰撞。
        // 按「去重且独立键」裁定：技能宝石保留 gem.skill.*，此处精魂宝石改用 gem.spirit.* 独立键并填回专属精魂版译文。
        provider.addZh("gem.spirit.name.coe.herald_of_ash", "灰烬之捷");
        provider.addPair("gem.spirit.description.coe.herald_of_ash", "Continuously burn nearby enemies; kills trigger a Fire explosion.", "精魂光环：为你的打击附加火焰伤害并点燃，击杀引发火焰爆发。");
        // blood_pact：poe2db 权威数据（all_skills.json / Spirit_Gems 页）中无对应条目，
        // 按本地 id 写英文原名，不臆造中文名与描述。
        provider.add("gem.spirit.name.coe.blood_pact", "Blood Pact");
    }
}
