package com.cimdy.coe.data.gen.source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 关键字系统的「唯一规范数据源」（PoE2 化）。
 *
 * <p>历史上 {@code keywords.json} 是手改的静态资源，新增/修正机制知识必须改 JSON、且极易与
 * 翻译键漂移。现改为：<b>代码即数据源</b>，本类内嵌全部关键字（id / 标题 / 别名 / 描述），
 * 由 {@code KeywordProvider} 按 id 分文件写出 {@code keywords/<id>.json}、由 {@code KeywordLang} 注册双语翻译键，
 * 二者共用同一份清单，从根本上杜绝键漂移。
 *
 * <p>约定：
 * <ul>
 *   <li>{@code id} 大小写敏感，同时充当翻译键 {@code coe:keyword.<id>}（{@code KeywordRegistry} 自动注入）。</li>
 *   <li>{@code title} 为展示标题（默认中文；英文标题由 {@code KeywordLang} 从别名推导，避免重复）。</li>
 *   <li>{@code aliases} 第一个纯 ASCII 项通常即英文标题，供 {@code KeywordLang} 取 en 用。</li>
 *   <li>{@code description} 每行一条；可含 {@code $[id]} 占位符，运行时由 {@code KeywordRegistry#resolveText}
 *       替换为该关键字的<b>本地化</b>标题，因而「解释里可以继续点进下一个关键字」是语言无关的。</li>
 * </ul>
 *
 * <p>{@code $[id]} 已全面激活：全部 49 条 description 都至少引用了一个其它（或自身）关键字，
 * 占位符内 id 必须存在于本清单，否则会原样显示在屏幕上。
 */
public final class KeywordSource {

    private KeywordSource() {
    }

    /** 单个关键字的规范定义。 */
    public static final class Entry {
        public final String id;
        public final String title;
        public final List<String> aliases;
        public final List<String> description;

        Entry(String id, String title, List<String> aliases, List<String> description) {
            this.id = id;
            this.title = title;
            this.aliases = List.copyOf(aliases);
            this.description = List.copyOf(description);
        }
    }

    private static Entry e(String id, String title, String[] aliases, String[] desc) {
        List<String> a = new ArrayList<>();
        for (String s : aliases) a.add(s);
        List<String> d = new ArrayList<>();
        for (String s : desc) d.add(s);
        return new Entry(id, title, a, d);
    }

    private static final Entry[] ENTRIES = {
            e("ailment", "异常状态",
                    new String[]{"Ailment", "Ailments", "异常状态", "异常"},
                    new String[]{
                            "异常状态是施加在敌人与玩家身上的负面效果（$[debuff]）。",
                            "异常状态分为两类：$[elemental_ailment]（$[freeze]、$[shock]、$[ignite]、$[chill]、$[scorch]、$[brittle]、$[sap]）与$[damage_over_time]异常（$[bleed]、$[poison]）。",
                            "$[stun]是一种独立、非伤害类的异常。异常可通过$[ailment_threshold]来削弱，其持续时间也可被缩短。"
                    }),
            e("elemental_ailment", "元素异常",
                    new String[]{"Elemental Ailment", "Elemental Ailments", "元素异常"},
                    new String[]{
                            "$[elemental_ailment]由特定元素的$[damage]施加。",
                            "$[freeze]与$[chill]来自$[cold_damage]，$[shock]来自$[lightning_damage]，$[ignite]来自$[fire_damage]。",
                            "它们的特殊变体分别为$[scorch]（火焰）、$[brittle]（冰霜）与$[sap]（闪电）。"
                    }),
            e("freeze", "冰冻",
                    new String[]{"Freeze", "Frozen", "冰冻", "冻结"},
                    new String[]{
                            "$[freeze]是一种$[elemental_ailment]，会使目标无法移动或行动。",
                            "$[freeze]值累积到冰冻阈值时会触发冰冻；其持续时间随超出阈值的幅度而增加。",
                            "$[freeze]同时也会施加$[chill]。另见碎裂与$[stun]。"
                    }),
            e("chill", "冰缓",
                    new String[]{"Chill", "Chilled", "冰缓", "冰冷"},
                    new String[]{
                            "$[chill]是一种来自$[cold_damage]的$[elemental_ailment]，会降低目标的行动速度。",
                            "敌人被$[freeze]时会自动附加$[chill]，也可以独立施加。"
                    }),
            e("shock", "感电",
                    new String[]{"Shock", "Shocked", "感电"},
                    new String[]{
                            "$[shock]是一种来自$[lightning_damage]的$[elemental_ailment]，会提高目标所受的$[damage]。",
                            "$[shock]的强度取决于该次命中移除的敌人$[life]或$[energy_shield]的比例。"
                    }),
            e("ignite", "点燃",
                    new String[]{"Ignite", "Ignited", "点燃"},
                    new String[]{
                            "$[ignite]是一种来自$[fire_damage]的$[elemental_ailment]，会造成火焰$[damage_over_time]。",
                            "$[ignite]的持续时间和$[damage]会随点燃该次命中的$[fire_damage]而提升。"
                    }),
            e("scorch", "灼烧",
                    new String[]{"Scorch", "Scorched", "灼热", "焚烧"},
                    new String[]{
                            "$[scorch]是一种强力的火焰异常，会降低目标的火焰$[resistance]。",
                            "它对应$[brittle]（冰霜）与$[sap]（闪电）。"
                    }),
            e("brittle", "脆弱",
                    new String[]{"Brittle", "脆弱"},
                    new String[]{
                            "$[brittle]是一种强力的冰霜异常，会为攻击者的暴击率提供加成。",
                            "它对应$[scorch]（火焰）与$[sap]（闪电）。"
                    }),
            e("sap", "衰弱",
                    new String[]{"Sap", "Sapped", "衰弱"},
                    new String[]{
                            "$[sap]是一种强力的闪电异常，会降低目标的闪避值。",
                            "它对应$[scorch]（火焰）与$[brittle]（冰霜）。"
                    }),
            e("bleed", "流血",
                    new String[]{"Bleed", "Bleeding", "流血"},
                    new String[]{
                            "$[bleed]是一种物理$[damage_over_time]异常，造成的$[damage]基于该次物理命中。",
                            "$[bleed]对移动中的目标造成更高$[damage]。"
                    }),
            e("poison", "中毒",
                    new String[]{"Poison", "Poisoned", "中毒"},
                    new String[]{
                            "$[poison]是一种混沌$[damage_over_time]异常，每次施加都会叠加。",
                            "$[poison]不受$[ailment_threshold]影响，并能部分无视敌人$[resistance]。"
                    }),
            e("stun", "眩晕",
                    new String[]{"Stun", "Stunned", "眩晕"},
                    new String[]{
                            "$[stun]会打断目标的行动。$[stun]蓄力会在眩晕阈值下进行累积。",
                            "越重的命中累积越多$[stun]；$[stun]可被重眩晕等效果延长。"
                    }),
            e("critical", "暴击",
                    new String[]{"Critical Hit", "Critical", "Crit", "暴击", "暴击率"},
                    new String[]{
                            "$[critical]会将该次命中的$[damage]乘以暴击伤害加成。",
                            "暴击率与$[brittle]都会提高打出$[critical]的几率。"
                    }),
            e("chaos", "混沌伤害",
                    new String[]{"Chaos Damage", "Chaos", "混沌伤害"},
                    new String[]{
                            "$[chaos]是五种$[damage]类型之一，也是$[poison]的基础。",
                            "与其他元素不同，它无法造成$[freeze]、$[shock]或$[ignite]。"
                    }),
            e("physical", "物理伤害",
                    new String[]{"Physical Damage", "Physical", "物理伤害"},
                    new String[]{
                            "$[physical]由$[armour]减免，也是$[bleed]的基础。",
                            "它可被转化为火焰、冰霜、闪电或$[chaos]。"
                    }),
            e("armour", "护甲",
                    new String[]{"Armour", "护甲"},
                    new String[]{
                            "$[armour]可减免命中造成的$[physical]，对较小$[damage]的效果更佳。",
                            "它不提供对$[chaos]的保护。"
                    }),
            e("evasion", "闪避",
                    new String[]{"Evasion", "闪避", "闪避值"},
                    new String[]{
                            "$[evasion]提供完全避开敌人命中的几率。",
                            "会被$[sap]降低。以$[evasion]为基础的防御与$[energy_shield]、$[ward]相配合。"
                    }),
            e("energy_shield", "能量护盾",
                    new String[]{"Energy Shield", "ES", "能量护盾", "护盾"},
                    new String[]{
                            "$[energy_shield]会在$[life]之前吸收$[damage]，并在一段时间未受$[damage]后再生。",
                            "常会与$[ward]、$[evasion]搭配。"
                    }),
            e("ward", "守护",
                    new String[]{"Ward", "守护", "精魄"},
                    new String[]{
                            "$[ward]是一层临时的吸收层，可吸收命中并随时间衰减。",
                            "它只能由特定效果补充，并可配合$[energy_shield]使用。"
                    }),
            e("aoe", "范围效果",
                    new String[]{"Area of Effect", "AoE", "范围效果", "范围"},
                    new String[]{
                            "$[aoe]$[skill]会在落点周围一定半径内造成$[damage]或施加异常。",
                            "$[aoe]受$[aoe]词缀提升，并可覆盖多个敌人。"
                    }),
            e("chaining", "连锁",
                    new String[]{"Chaining", "Chain", "连锁"},
                    new String[]{
                            "$[chaining]使$[projectile]或光束在命中目标后跃向附近的敌人。",
                            "每次$[chaining]会重复一次命中，通常$[damage]会降低。"
                    }),
            e("channeling", "引导",
                    new String[]{"Channeling", "Channel", "引导"},
                    new String[]{
                            "$[channeling]$[skill]在按住攻击键时持续释放，反复造成效果。",
                            "$[channeling]时移动速度通常会被降低。"
                    }),
            e("curse", "诅咒",
                    new String[]{"Curse", "Curses", "诅咒"},
                    new String[]{
                            "$[curse]是施加在敌人身上、使其状态恶化的咒术（例如提高所受$[damage]）。",
                            "通常只能施加一个$[curse]，除非获得更高的诅咒上限。"
                    }),
            e("leech", "偷取",
                    new String[]{"Leech", "Leeching", "偷取", "汲取"},
                    new String[]{
                            "$[leech]会根据造成$[damage]的一部分来恢复$[life]、$[mana]或$[energy_shield]。",
                            "$[leech]的量会在短时间内持续恢复，而非瞬间恢复。"
                    }),
            e("reflect", "反伤",
                    new String[]{"Reflect", "Reflection", "反伤", "反射"},
                    new String[]{
                            "$[reflect]会将所承受$[damage]的一部分返还给攻击者。",
                            "$[reflect]常出现在某些地图词缀上，可为物理、元素或混沌类型。"
                    }),
            e("damage", "伤害",
                    new String[]{"Damage", "伤害"},
                    new String[]{
                            "$[damage]由$[skill]造成，并从$[life]、$[energy_shield]、$[ward]或$[armour]中扣除。",
                            "五种$[damage]类型分别为物理、冰霜、火焰、闪电与$[chaos]。",
                            "许多效果会随$[aoe]、$[critical]或$[damage_over_time]而缩放。"
                    }),
            e("cold_damage", "冰霜伤害",
                    new String[]{"Cold Damage", "Cold", "冰霜伤害", "冰冷伤害"},
                    new String[]{
                            "$[cold_damage]是一种元素伤害类型，也是$[chill]与$[freeze]的基础。",
                            "冰霜$[resistance]可降低所受的$[cold_damage]。"
                    }),
            e("fire_damage", "火焰伤害",
                    new String[]{"Fire Damage", "Fire", "火焰伤害"},
                    new String[]{
                            "$[fire_damage]是一种元素伤害类型，也是$[ignite]与$[scorch]的基础。",
                            "火焰$[resistance]可降低所受的$[fire_damage]。"
                    }),
            e("lightning_damage", "闪电伤害",
                    new String[]{"Lightning Damage", "Lightning", "闪电伤害"},
                    new String[]{
                            "$[lightning_damage]是一种元素伤害类型，也是$[shock]与$[sap]的基础。",
                            "闪电$[resistance]可降低所受的$[lightning_damage]。"
                    }),
            e("resistance", "抗性",
                    new String[]{"Resistance", "Resistances", "抗性"},
                    new String[]{
                            "$[resistance]可降低所受的对应伤害类型（冰霜抗性、火焰抗性、闪电抗性、混沌抗性）。",
                            "混沌抗性尤为重要，因为$[armour]与$[energy_shield]都无法抵御$[chaos]。"
                    }),
            e("block", "格挡",
                    new String[]{"Block", "Blocking", "格挡"},
                    new String[]{
                            "$[block]提供完全避开命中$[damage]的几率。",
                            "法术格挡可抵御法术命中；二者均与$[evasion]和$[dodge]不同。"
                    }),
            e("dodge", "闪躲",
                    new String[]{"Dodge", "Dodging", "闪避"},
                    new String[]{
                            "$[dodge]是一种避开敌人命中的几率，来源于闪避值及其他来源。",
                            "与$[block]不同，$[dodge]不会减少已命中$[damage]的数值。"
                    }),
            e("attribute", "属性",
                    new String[]{"Attribute", "Attributes", "属性"},
                    new String[]{
                            "$[attribute]分为$[strength]、$[dexterity]与$[intelligence]，可提供防御与进攻属性。",
                            "$[strength]提供$[life]，$[dexterity]提供$[evasion]，$[intelligence]提供$[energy_shield]与$[mana]。"
                    }),
            e("strength", "力量",
                    new String[]{"Strength", "力量"},
                    new String[]{
                            "$[strength]是一种提供$[life]并提高近战$[damage]的$[attribute]。"
                    }),
            e("dexterity", "敏捷",
                    new String[]{"Dexterity", "敏捷"},
                    new String[]{
                            "$[dexterity]是一种提供$[evasion]与命中（准确率）的$[attribute]。"
                    }),
            e("intelligence", "智慧",
                    new String[]{"Intelligence", "智慧", "智力"},
                    new String[]{
                            "$[intelligence]是一种提供$[energy_shield]与$[mana]的$[attribute]。"
                    }),
            e("skill", "技能",
                    new String[]{"Skill", "Skills", "技能"},
                    new String[]{
                            "$[skill]是造成$[damage]或施加异常的主动能力。",
                            "$[skill]可由$[support_gem]修改，并可能保留$[spirit]或消耗$[mana]。"
                    }),
            e("support_gem", "辅助宝石",
                    new String[]{"Support Gem", "Support", "辅助宝石", "辅助"},
                    new String[]{
                            "$[support_gem]会修改其所连接$[skill]的行为。",
                            "$[support_gem]本身不会造成$[damage]。"
                    }),
            e("spirit", "精魂",
                    new String[]{"Spirit", "精魂"},
                    new String[]{
                            "$[spirit]是一种由特定$[skill]保留的资源，类似于持续的$[mana]保留。"
                    }),
            e("mana", "魔力",
                    new String[]{"Mana", "魔力"},
                    new String[]{
                            "$[mana]用于施放$[skill]，并可通过$[leech]与再生来恢复。"
                    }),
            e("life", "生命",
                    new String[]{"Life", "生命"},
                    new String[]{
                            "$[life]是主要的生命值池；对$[life]的$[damage]可由$[energy_shield]、$[armour]与$[ward]来避免。"
                    }),
            e("flask", "药剂",
                    new String[]{"Flask", "Flasks", "药剂"},
                    new String[]{
                            "$[flask]是可消耗品，可恢复$[life]或$[mana]，并赋予临时$[buff]。"
                    }),
            e("buff", "增益",
                    new String[]{"Buff", "Buffs", "增益"},
                    new String[]{
                            "$[buff]是一种正面临时效果，与$[debuff]相反。",
                            "$[buff]可由$[flask]或$[skill]赋予。"
                    }),
            e("debuff", "减益",
                    new String[]{"Debuff", "Debuffs", "减益", "负面"},
                    new String[]{
                            "$[debuff]是一种负面临时效果；异常与$[curse]都属于$[debuff]。"
                    }),
            e("ailment_threshold", "异常阈值",
                    new String[]{"Ailment Threshold", "异常阈值"},
                    new String[]{
                            "$[ailment_threshold]是异常（$[freeze]、$[shock]、$[ignite]、$[bleed]、$[poison]）累积所针对的池。",
                            "阈值越高，需要越重的命中才能施加该异常。"
                    }),
            e("exposure", "曝露",
                    new String[]{"Exposure", "曝露", "曝光"},
                    new String[]{
                            "$[exposure]会降低敌人针对特定元素的$[resistance]，从而放大该类型$[damage]。"
                    }),
            e("magnitude", "效果强度",
                    new String[]{"Magnitude", "效果数值", "强度"},
                    new String[]{
                            "$[magnitude]是效果的规模；它会缩放异常强度、$[leech]量及类似词缀。"
                    }),
            e("projectile", "投射物",
                    new String[]{"Projectile", "Projectiles", "投射物", "弹射物"},
                    new String[]{
                            "$[projectile]是一种可$[chaining]、穿透或分叉的飞行命中。",
                            "$[projectile]行为在闪电与冰霜技能上较为常见。"
                    }),
            e("damage_over_time", "持续伤害",
                    new String[]{"Damage over Time", "DoT", "持续伤害", "持续"},
                    new String[]{
                            "$[damage_over_time]会逐步造成$[damage]；$[bleed]、$[poison]与$[ignite]都属于$[damage_over_time]效果。",
                            "$[damage_over_time]会无视部分基于命中的减伤手段，例如$[armour]。"
                    }),
    };

    /** 全部关键字的规范定义（顺序即生成顺序）。 */
    public static List<Entry> all() {
        return Arrays.asList(ENTRIES);
    }

    /**
     * C.9：把一行原始描述（内含 {@code $[id]} 占位符）拆成<b>片段串数组</b>，供 datagen 写出片段行。
     *
     * <p>例如 {@code "异常状态是...（$[debuff]）。"} → {@code ["异常状态是...（", "$[debuff]", "）。"]}。
     * 运行时 {@code KeywordFragment.of} 把片段串解析为 字面 / {@code @key} / {@code $[id]}引用 三类；
     * 这样 49 条既有描述<b>无需逐句重写</b>即可平滑迁入片段数组 schema。
     */
    public static List<String> fragmentsOf(String line) {
        List<String> out = new ArrayList<>();
        if (line == null || line.isEmpty()) return out;
        Matcher m = KW_REF.matcher(line);
        int last = 0;
        while (m.find()) {
            if (m.start() > last) out.add(line.substring(last, m.start()));
            out.add(m.group(0));            // 整段 "$[id]" 作为引用片段
            last = m.end();
        }
        if (last < line.length()) out.add(line.substring(last));
        return out;
    }

    private static final Pattern KW_REF = Pattern.compile("\\$\\[([^\\]]+)\\]");
}
