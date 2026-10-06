package com.cimdy.coe.data.gen.lang;

import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.Convert;
import com.cimdy.coe.data.gen.equipment.EquipmentLang;
import com.cimdy.coe.data.gen.talent.TalentNodeLang;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.cimdy.coe.data.gen.stat.effect.StatEffectKeys;
import com.cimdy.coe.data.codec.item.essence.EssenceRegistry;
import com.cimdy.coe.data.codec.item.essence.EssenceState;
import com.cimdy.coe.data.codec.item.RuneRegistry;
import com.cimdy.coe.data.codec.item.state.RuneState;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class LangProvider extends LanguageProvider {
    private final String locale;

    private final LangSupport L;

    public LangProvider(PackOutput output, String locale) {
        super(output, CraftOfExile.MODID, locale);
        this.locale = locale;
        this.L = new LangSupport(this, this.locale);
    }

    public void addZh(String key, String zh) {
        if (!"zh_cn".equals(this.locale)) {
            this.add(key, englishFor(key));
        } else {
            this.add(key, zh);
        }
    }

    private static String englishFor(String key) {
        int dot = key.lastIndexOf('.');
        return ValueExpr.humanize(dot < 0 ? key : key.substring(dot + 1));
    }

    @Override
    protected void addTranslations() {
        addBaseItemKeys();
        addEssenceRuneLang();

        MiscLang.installHold(L);
        ModLang.install(L);
        MiscLang.installSkill(L);
        LevelLang.install(L);
        EquipmentLang.installModNames(L);
        EquipmentLang.installEquipment(L);
        TalentNodeLang.populate(this, locale);
        MiscLang.installProgression(L);
        MiscLang.installStatScope(L);

        addStatPresentationEn();

        addStatPresentationZh();

        AttributeLang.install(L);
        KeywordLang.install(L);
        addGapKeys();
    }

    private void addBaseItemKeys() {
        this.addPair("input_box.coe.input", "Please enter...", "请输入...");
        this.addPair("ui.coe.empty", "Nothing here", "暂无内容");
        this.addPair("ui.coe.on", "On", "开");
        this.addPair("ui.coe.off", "Off", "关");
        this.addPair("keymapping.coe.test_panel", "Test Panel", "测试面板");
        this.addPair("keymapping.coe.open_codex", "Codex", "物品图鉴");
        this.addPair("keymapping.coe.open_keyword", "Keyword Reference", "关键字参考");
        this.addPair("keymapping.coe.open_gallery", "UI Gallery", "界面库");
        this.addPair("ui.coe.gallery", "UI Component Gallery", "界面组件库");
        this.addPair("ui.coe.ok", "OK", "确定");
        this.addPair("ui.coe.cancel", "Cancel", "取消");
        this.addPair("screen.coe.gem_cut.title", "%s Gem Cutting", "%s 宝石切割");
        this.addPair("screen.coe.gem_cut.cut", "Cut", "切割");
        this.addZh("gem.category.skill", "技能");
        this.addZh("gem.category.support", "辅助");
        this.addZh("gem.category.spirit", "精魂");
        this.addZh("item.coe.uncut_gem", "未切割的技能石");
        this.addZh("item.coe.uncut_support_gem", "未切割的辅助宝石");
        this.addZh("item.coe.uncut_spirit_gem", "未切割的精魂宝石");
        this.addZh("item.coe.uncut_gem.tier", "(等阶 %s)");
        this.addZh("item.coe.skill_gem", "技能石");
        this.addZh("item.coe.support_gem", "辅助宝石");
        this.addZh("item.coe.spirit_gem", "精魂宝石");
        this.addZh("item.coe.life_flask", "生命药剂");
        this.addZh("item.coe.mana_flask", "魔力药剂");
        this.addZh("item.coe.flask.charges", "充能: %s");
        this.addZh("item.coe.flask.recovery", "%s 秒内回复 %s 点%s");
        this.addZh("item.coe.flask.per_use", "每次使用消耗 %s 充能");
        this.addZh("item.coe.ruby_charm", "红宝石护符");
        this.addZh("item.coe.sapphire_charm", "蓝宝石护符");
        this.addZh("item.coe.topaz_charm", "黄玉护符");
        this.addZh("item.coe.amethyst_charm", "紫水晶护符");
        this.addZh("item.coe.dousing_charm", "灭火护符");
        this.addZh("item.coe.thawing_charm", "解冻护符");
        this.addZh("item.coe.grounding_charm", "接地护符");
        this.addZh("item.coe.staunching_charm", "止血护符");
        this.addZh("item.coe.antidote_charm", "解毒护符");
        this.addZh("item.coe.stone_charm", "磐石护符");
        this.addZh("item.coe.silver_charm", "白银护符");
        this.addZh("item.coe.golden_charm", "黄金护符");
        this.addZh("item.coe.charm.trigger", "触发: %s");
        this.addPair("item.coe.charm.effect", "Effect: %s", "效果: %s");
        this.addZh("item.coe.charm.charges", "充能: %s");
        this.addZh("item.coe.charm.used", "每次消耗: %s");
        // ---- 通货：基础阶 ----
        this.addZh("item.coe.wisdom_scroll", "知识卷轴");
        this.addZh("item.coe.portal_scroll", "传送卷轴");
        this.addZh("item.coe.kalandra_mirror", "卡兰德拉之镜");
        this.addZh("item.coe.chance_orb", "机会石");
        this.addZh("item.coe.scouring_orb", "重铸石");
        this.addZh("item.coe.alchemy_orb", "点金石");
        this.addZh("item.coe.alteration_orb", "改造石");
        this.addZh("item.coe.transmutation_orb", "蜕变石");
        this.addZh("item.coe.chaos_orb", "混沌石");
        this.addZh("item.coe.regal_orb", "富豪石");
        this.addZh("item.coe.augmentation_orb", "增幅石");
        this.addZh("item.coe.exalted_orb", "崇高石");
        this.addZh("item.coe.divine_orb", "神圣石");
        this.addZh("item.coe.annulment_orb", "无效石");
        this.addZh("item.coe.jeweller_orb", "工匠石");
        this.addZh("item.coe.blessed_orb", "祝福石");
        // ---- 通货：分阶（Greater / Perfect，动作同基础阶、仅新增词缀有等级下限）----
        this.addZh("item.coe.greater_transmutation_orb", "高阶蜕变石");
        this.addZh("item.coe.perfect_transmutation_orb", "完美蜕变石");
        this.addZh("item.coe.greater_augmentation_orb", "高阶增幅石");
        this.addZh("item.coe.perfect_augmentation_orb", "完美增幅石");
        this.addZh("item.coe.greater_alteration_orb", "高阶改造石");
        this.addZh("item.coe.perfect_alteration_orb", "完美改造石");
        this.addZh("item.coe.greater_regal_orb", "高阶富豪石");
        this.addZh("item.coe.perfect_regal_orb", "完美富豪石");
        this.addZh("item.coe.greater_exalted_orb", "高阶崇高石");
        this.addZh("item.coe.perfect_exalted_orb", "完美崇高石");
        this.addZh("item.coe.greater_chaos_orb", "高阶混沌石");
        this.addZh("item.coe.perfect_chaos_orb", "完美混沌石");
        // ---- 技能工匠石分阶（Lesser / Greater / Perfect，POE2 无基准阶）----
        this.addZh("item.coe.lesser_jeweller_orb", "次级工匠石");
        this.addZh("item.coe.greater_jeweller_orb", "高阶工匠石");
        this.addZh("item.coe.perfect_jeweller_orb", "完美工匠石");
        // ---- 品质通货（磨刀石 / 护甲碎片 / 玻璃珠 / 棱光石）----
        this.addZh("item.coe.blacksmith_whetstone", "磨刀石");
        this.addZh("item.coe.armourer_scrap", "护甲碎片");
        this.addZh("item.coe.glassblower_bauble", "玻璃珠");
        this.addZh("item.coe.gemcutter_prism", "棱光石");
        // ---- 装备孔通货（巧匠石：为符文 / 魂核 / 增幅物开孔）----
        this.addZh("item.coe.artificer_orb", "巧匠石");
        // ---- 品质 / 孔 通用键 ----
        this.addZh("item.coe.quality", "品质");
        this.addZh("item.coe.sockets", "物品插槽");
        this.addPair("item.currency.coe.domain.equipment", "Can only be used on equipment", "只能用于装备");
        this.addPair("item.currency.coe.domain.weapon", "Can only be used on weapons", "只能用于武器");
        this.addPair("item.currency.coe.domain.armour", "Can only be used on armour", "只能用于护甲");
        this.addPair("item.currency.coe.domain.weapon_or_armour",
                "Can only be used on weapons or armour", "只能用于武器或护甲");
        this.addPair("item.currency.coe.domain.flask", "Can only be used on flasks", "只能用于药剂");
        this.addPair("item.currency.coe.domain.affix_item",
                "Can only be used on equipment, flasks or charms", "只能用于装备、药剂或护符");
        this.addPair("item.currency.coe.domain.skill_gem", "Can only be used on skill gems", "只能用于技能石");
        this.addPair("item.currency.coe.quality.maxed", "Quality is at maximum", "品质已达上限");
        this.addPair("item.currency.coe.equipped", "Equipped items cannot be modified; unequip first", "已装备的物品无法修改，请先卸下");
        this.addPair("item.currency.coe.socket.maxed", "Item sockets are at maximum", "物品插槽已达上限");
        this.addPair("item.currency.coe.sockets.invalid", "This skill gem's support socket count has reached or exceeded the target", "该技能石的辅助槽数已达或超过目标值");
        this.addPair("item.currency.coe.generate.empty", "No equipment available to generate", "当前没有可生成的装备");
    }

    /**
     * 符文 / 精髓名称由翻译键拼接而成（见 {@code RuneState#displayName} / {@code EssenceState#displayName}）：
     * <b>{@code T<阶号>}（字面量，深灰）+ {@code item.coe.rune|essence.<name>} + {@code item.coe.rune|essence}</b>，
     * 精髓再插一段 {@code item.coe.essence.<类名>}；符文<b>不注册类名段</b>（不渲染名称）。
     * 故此处只注册「名称 + 种类」两段子键，物品名由前缀现拼、不注册扁平名。
     * 英文把分隔空格烘进子键值，中文无需分隔。
     */
    private void addEssenceRuneLang() {
        EssenceRegistry.init();
        RuneRegistry.init();
        boolean en = "en_us".equals(this.locale);

        for (RuneState s : RuneRegistry.all()) {
            this.add("item.coe.rune." + s.name().getPath(),
                    familyName(RUNE_FAMILY_ZH, s.name().getPath(), en));
        }
        for (EssenceState s : EssenceRegistry.all()) {
            this.add("item.coe.essence." + s.name().getPath(),
                    familyName(ESSENCE_FAMILY_ZH, s.name().getPath(), en));
        }
        this.add("item.coe.rune", en ? "Rune" : "符文");
        this.add("item.coe.essence", en ? "Essence" : "精髓");
    }

    /** 族名：中文取语料译名表；英文即族名本身（用于与前后段拼接的尾随空格）。 */
    private static String familyName(Map<String, String> zh, String family, boolean en) {
        if (en) {
            return family + " ";
        }
        return zh.getOrDefault(family.toLowerCase(Locale.ROOT), family);
    }

    /** 符文族译名（键为族名小写）。 */
    private static final Map<String, String> RUNE_FAMILY_ZH = Map.ofEntries(
            Map.entry("adept", "嫻熟"), Map.entry("body", "肉體"), Map.entry("charging", "充能"),
            Map.entry("desert", "沙漠"), Map.entry("glacial", "冰川"), Map.entry("inspiration", "啟發"),
            Map.entry("iron", "鍛鐵"), Map.entry("mind", "心靈"), Map.entry("rebirth", "重生"),
            Map.entry("resolve", "決心"), Map.entry("robust", "堅實"), Map.entry("stone", "岩石"),
            Map.entry("storm", "暴風"), Map.entry("tempered", "鍛煉"), Map.entry("vision", "遠見"),
            Map.entry("ward", "保護"), Map.entry("masterwork", "匠鍛"));

    /** 精髓族译名（键为族名小写）。 */
    private static final Map<String, String> ESSENCE_FAMILY_ZH = Map.ofEntries(
            Map.entry("abrasion", "磨礎"), Map.entry("battle", "戰鬥"), Map.entry("body", "肉體"),
            Map.entry("delirium", "诟忘"), Map.entry("electricity", "電能"), Map.entry("enhancement", "強化"),
            Map.entry("flames", "烈焰"), Map.entry("horror", "極恐"), Map.entry("hysteria", "浪貴"),
            Map.entry("insanity", "錯亂"), Map.entry("ice", "寧冰"), Map.entry("infinite", "無限"),
            Map.entry("haste", "捻捻"), Map.entry("mind", "心智"), Map.entry("ruin", "毀滅"),
            Map.entry("sorcery", "導幃"));

    private void addStatPresentationEn() {
        if ("en_us".equals(this.locale)) {
            this.add("item.coe.level", "Item Level");
            this.add("rarity.coe.none", "None");
            this.add("rarity.coe.common", "Common");
            this.add("rarity.coe.magic", "Magic");
            this.add("rarity.coe.rare", "Rare");
            this.add("rarity.coe.unique", "Unique");
            this.add("stat.op.add_value", "%1$s %2$s");
            this.add("stat.op.add_multiplied_base", "%1$s%% %2$s");
            this.add("stat.op.add_multiplied_total", "%1$s%% more %2$s");
            this.add("stat.op.replace", "replaces %1$s (%2$s)");
            this.add("stat.op.limiter", "%1$s (%2$s)");
            this.add("stat.op.reduced_penalty", "reduced penalty: %s");

            // Puppet Master 每层新增「with Command Skills」词缀的显式英文名（否则回落 humanize）。
            this.add("stat.inc_skill_speed_with_command_skills", "Skill Speed with Command Skills");
            this.add("stat.reduced_movement_speed_penalty_with_command_skills", "Reduced Movement Speed Penalty with Command Skills");
            this.add("stat.fmt.modifier", "%1$s %2$s");
            this.add("stat_effect.deal", "deal %s");
            this.add("stat.amount.per", "per %s %s");
            this.add("stat.amount.up_to", "up to %s");
            this.add("stat.amount.unknown", "[!] amount");
            this.add("stat.targets.fmt", "to %s");
            this.add("stat.target.self", "Self");
            this.add("stat.target.trigger_target", "Trigger Target");
            this.add("stat.target.presence_targets", "Presence Targets");
            this.add("stat.target.presence_allies", "Presence Allies");
            this.add("stat.target.presence_enemies", "Presence Enemies");
            this.add("stat.target.minion", "Minions");
            this.add("stat.target.totem", "Totems");
            this.add("stat.target.companion", "Companions");
            this.add("stat.target.ballista", "Ballistas");
            this.add("stat.target.mine", "Mines");
            this.add("stat.target.golem", "Golems");
            this.add("stat.target.brand", "Brands");
            this.add("stat.target.trap", "Traps");
            this.add("stat.target.ally", "Allies");
            this.add("stat.condition.unknown", "[!] condition");
            this.add("stat.condition.join_and", " and ");
            this.add("stat.condition.compare", "%1$s %2$s %3$s");
            this.add("stat.quantity.unknown", "[?]");
            this.add("stat.condition.living_state", "while %s");
            this.add("stat.condition.status", "while affected by %s");
            this.add("stat.condition.event", "if you've recently %s");
            this.add("stat.condition.event.not_recently", "if you haven't recently %s");
            this.add("stat.condition.entity_type", "against %s");
            this.add("stat.condition.accessory", "while %s is equipped");
            this.add("stat.condition.biome", "in %s");
            this.add("stat.condition.dimension", "in %s");
            this.add("stat.condition.flag", "if %s");
            this.add("stat.condition.status_tag", "while affected by %s");
            this.add("stat.condition.status_kind", "while affected by a %s");
            this.add("stat.condition.movement", "while %s");
            this.add("stat.condition.movement.stationary", "while Stationary");
            this.add("stat.condition.movement.moving", "while Moving");
            this.add("stat.condition.flask_active.any", "while a Flask is active");
            this.add("stat.condition.flask_active.mana", "while a Mana Flask is active");
            this.add("stat.condition.flask_active.health", "while a Life Flask is active");
            this.add("stat.condition.charm_active", "while you have an active Charm");
            this.add("stat.condition.heavy_stun", "while Heavily Stunned");
            this.add("stat.condition.fully_broken_armor", "while Armour is Fully Broken");
            this.add("stat.condition.status_age", "%1$s for %2$s");
            this.add("stat.condition.current_event", "if current event is %s");
            this.add("stat.condition.current_event_tag", "%1$s (tagged %2$s)");
            this.add("stat.condition.damage", "when dealing %s");
            this.add("stat.condition.charge", "while you have %s");
            this.add("stat.condition.gem", "when socketed with %s");
            this.add("stat.condition.hit_state", "on %s");
            this.add("stat.condition.presence.entered", "when an enemy enters your presence");
            this.add("stat.condition.presence.left", "when an enemy leaves your presence");
            this.add("stat.condition.effect_level", "at %s level");
            this.add("stat.condition.effect_duration", "while %s is active");
            this.add("stat.condition.using_skill", "while using %s");
            this.add("stat.condition.target_rarity", "the target is %s");
            this.add("stat.condition.flask_uses", "while you have no %s left");
            this.add("stat.condition.curse_duration", "once a curse has expired");
            this.add("stat.condition.selector", "for %s");
            addStatValueKeys();
            addStatEffectKeys();
        }
    }

    private void addStatPresentationZh() {
        if ("zh_cn".equals(this.locale)) {
            this.addZh("item.coe.level", "物品等级");
            this.add("rarity.coe.none", "无");
            this.add("rarity.coe.common", "普通");
            this.add("rarity.coe.magic", "魔法");
            this.add("rarity.coe.rare", "稀有");
            this.add("rarity.coe.unique", "传奇");
            this.add("stat.op.add_value", "%2$s %1$s");
            this.add("stat.op.add_multiplied_base", "%2$s %1$s%%");
            this.add("stat.op.add_multiplied_total", "%2$s 总增 %1$s%%");
            this.add("stat.op.replace", "将 %1$s 替换为 %2$s");
            this.add("stat.op.limiter", "%1$s（%2$s）");
            this.add("stat.op.reduced_penalty", "惩罚降低：%s");
            this.add("stat.inc_skill_speed_with_command_skills", "命令技能技能速度");
            this.add("stat.reduced_movement_speed_penalty_with_command_skills", "命令技能移动速度惩罚降低");
            this.add("stat.fmt.modifier", "%2$s，%1$s");
            this.add("stat_effect.deal", "造成 %s");
            this.add("stat.amount.per", "每 %1$s %2$s");
            this.add("stat.amount.up_to", "最多 %s");
            this.add("stat.amount.unknown", "[!] 数值");
            this.add("stat.targets.fmt", "对 %s");
            this.add("stat.target.self", "自身");
            this.add("stat.target.trigger_target", "触发目标");
            this.add("stat.target.presence_targets", "在场目标");
            this.add("stat.target.presence_allies", "在场友方");
            this.add("stat.target.presence_enemies", "在场敌人");
            this.add("stat.target.minion", "召唤物");
            this.add("stat.target.totem", "图腾");
            this.add("stat.target.companion", "同伴");
            this.add("stat.target.ballista", "弩炮");
            this.add("stat.target.mine", "地雷");
            this.add("stat.target.golem", "魔像");
            this.add("stat.target.brand", "烙印");
            this.add("stat.target.trap", "陷阱");
            this.add("stat.target.ally", "友方");
            this.add("stat.condition.unknown", "[!] 条件");
            this.add("stat.condition.join_and", " 且 ");
            this.add("stat.condition.compare", "当 %1$s %2$s %3$s 时");
            this.add("stat.quantity.unknown", "[?]");
            this.add("stat.condition.living_state", "%s 时");
            this.add("stat.condition.status", "处于 %s 状态时");
            this.add("stat.condition.event", "若你近期 %s");
            this.add("stat.condition.event.not_recently", "若你近期未 %s");
            this.add("stat.condition.entity_type", "对 %s");
            this.add("stat.condition.accessory", "装备%s时");
            this.add("stat.condition.biome", "在%s中");
            this.add("stat.condition.dimension", "在%s中");
            this.add("stat.condition.flag", "当%s时");
            this.add("stat.condition.status_tag", "处于 %s 状态时");
            this.add("stat.condition.status_kind", "处于%s状态时");
            this.add("stat.condition.movement", "%s 时");
            this.add("stat.condition.movement.stationary", "静止时");
            this.add("stat.condition.movement.moving", "移动时");
            this.add("stat.condition.flask_active.any", "药剂生效时");
            this.add("stat.condition.flask_active.mana", "魔力药剂生效时");
            this.add("stat.condition.flask_active.health", "生命药剂生效时");
            this.add("stat.condition.charm_active", "护符生效时");
            this.add("stat.condition.heavy_stun", "重度眩晕时");
            this.add("stat.condition.fully_broken_armor", "护甲完全破损时");
            this.add("stat.condition.status_age", "%1$s 持续 %2$s");
            this.add("stat.condition.current_event", "若当前事件为 %s");
            this.add("stat.condition.current_event_tag", "%1$s（标签 %2$s）");
            this.add("stat.condition.damage", "造成 %s 时");
            this.add("stat.condition.charge", "持有 %s 时");
            this.add("stat.condition.gem", "镶嵌 %s 时");
            this.add("stat.condition.hit_state", "在 %s 时");
            this.add("stat.condition.presence.entered", "敌人进入范围时");
            this.add("stat.condition.presence.left", "敌人离开范围时");
            this.add("stat.condition.effect_level", "处于 %s 等级时");
            this.add("stat.condition.effect_duration", "%s 生效时");
            this.add("stat.condition.using_skill", "使用 %s 时");
            this.add("stat.condition.target_rarity", "目标为 %s");
            this.add("stat.condition.ignited_by_you", "被你点燃");
            this.add("stat.condition.flask_uses", "当 %s 可用次数耗尽时");
            this.add("stat.condition.curse_duration", "诅咒已过期时");
            this.add("stat.condition.selector", "对 %s");
            addStatValueKeys();
            addStatEffectKeys();
        }
    }

    private static final String[][] STAT_VALUE_ROWS = {
            {"status", "ignite", "点燃"}, {"status", "ignited", "点燃"}, {"status", "burning", "燃烧"},
            {"status", "chill", "冰缓"}, {"status", "chilled", "冰缓"},
            {"status", "freeze", "冰冻"}, {"status", "frozen", "冰冻"},
            {"status", "shock", "感电"}, {"status", "shocked", "感电"},
            {"status", "electrocute", "触电"},
            {"status", "poison", "中毒"}, {"status", "poisoned", "中毒"},
            {"status", "bleed", "流血"}, {"status", "bleeding", "流血"},
            {"status", "blind", "致盲"}, {"status", "daze", "目眩"}, {"status", "debilitate", "衰弱"},
            {"status", "maim", "致残"}, {"status", "slow", "减速"}, {"status", "hinder", "阻碍"},
            {"status", "pin", "钉身"}, {"status", "stun", "眩晕"}, {"status", "heavy_stun", "重度眩晕"},
            {"status", "impale", "穿刺"}, {"status", "incision", "裂伤"},
            {"status", "wither", "凋零"}, {"status", "corrupted_blood", "腐化之血"},
            {"status", "armour_break", "破甲"}, {"status", "taunt", "嘲讽"}, {"status", "intimidate", "恐吓"},
            {"status", "petrify", "石化"}, {"status", "parried", "被招架"},
            {"status", "critical_weakness", "暴击弱点"}, {"status", "cannot_regen_health", "无法回复生命"},
            {"status", "hazard_mark", "危险标记"},
            {"status", "exposure_fire", "火焰曝露"}, {"status", "exposure_cold", "冰霜曝露"},
            {"status", "exposure_lightning", "闪电曝露"},
            {"status", "curse", "诅咒"}, {"status", "cursed", "诅咒"},
            {"status", "onslaught", "猛攻"}, {"status", "tailwind", "顺风"}, {"status", "adrenaline", "肾上腺素"},
            {"status", "fortify", "巩固"}, {"status", "phasing", "相位"}, {"status", "unholy_might", "邪恶之力"},
            {"status", "arcane_surge", "奥术涌流"}, {"status", "soul_eater", "噬魂"},
            {"status", "convalascence", "恢复期"},
            {"status", "infusion_fire", "火焰灌注"}, {"status", "infusion_cold", "冰霜灌注"},
            {"status", "infusion_lightning", "闪电灌注"}, {"status", "infusion_vaal", "瓦尔灌注"},
            {"status", "elemental_archon", "元素执政官"}, {"status", "archon_of_undeath", "不死执政官"},
            {"status", "archon_recovery", "执政官恢复"}, {"status", "remnant_empowered", "残痕强化"},
            {"status", "guard_from_flask", "药剂守护"},
            {"status", "banner_aura", "旗帜光环"}, {"status", "banner_elemental_res", "旗帜元素抗性"},
            {"status", "bone_offering", "骸骨献祭"}, {"status", "pain_offering", "痛苦献祭"},
            {"status", "soul_offering", "灵魂献祭"},
            {"status", "minion_reviving", "召唤物复活中"},
            {"status", "energy", "能量"}, {"status", "rage", "怒气"}, {"status", "mana", "魔力"},
            {"status", "spirit", "精魂"}, {"status", "charges", "充能球"}, {"status", "combo", "连击"},
            {"status", "glory", "荣耀"}, {"status", "mark", "标记"},
            {"status_tag", "exposure", "曝露"}, {"status_tag", "ailment", "异常"},
            {"status_tag", "elemental", "元素"}, {"status_tag", "physical", "物理"},
            {"status_tag", "chaos", "混沌"}, {"status_tag", "fire", "火焰"}, {"status_tag", "cold", "冰霜"},
            {"status_tag", "lightning", "闪电"}, {"status_tag", "curse", "诅咒"},
            {"status_tag", "archon", "执政官"}, {"status_tag", "infusion", "灌注"},
            {"status_tag", "charge", "充能球"}, {"status_tag", "offering", "献祭"},
            {"status_tag", "mark", "标记"}, {"status_tag", "form", "形态"},
            {"status_tag", "buff", "增益"}, {"status_tag", "debuff", "减益"},
            {"status_kind", "damaging", "伤害类"}, {"status_kind", "control", "控制类"},
            {"status_kind", "buff", "增益类"}, {"status_kind", "debuff", "减益类"},
            {"status_kind", "curse", "诅咒类"}, {"status_kind", "aura", "光环类"},
            {"status_kind", "mark", "标记类"}, {"status_kind", "herald", "先驱类"},
            {"status_kind", "shrine", "神龛类"}, {"status_kind", "charged", "充能类"},
            {"status_kind", "form", "形态类"},
            {"entity_type", "undead", "亡灵"}, {"entity_type", "demon", "恶魔"},
            {"entity_type", "beast", "野兽"}, {"entity_type", "humanoid", "人形"},
            {"entity_type", "construct", "构装体"}, {"entity_type", "elemental", "元素"},
            {"entity_type", "boss", "首领"}, {"entity_type", "minion", "召唤物"},
            {"entity_type", "player", "玩家"}, {"entity_type", "animal", "动物"},
            {"living_state", "channelling", "引导中"}, {"living_state", "parrying", "招架中"},
            {"living_state", "leeching", "偷取中"}, {"living_state", "leeching_mana", "偷取魔力中"},
            {"living_state", "dual_wield", "双持时"}, {"living_state", "holding_shield", "持盾时"},
            {"living_state", "sprinting", "冲刺时"}, {"living_state", "low_life", "低血"},
            {"living_state", "low_mana", "低魔力"}, {"living_state", "full_mana", "满魔力"},
            {"living_state", "full_life", "满血"}, {"living_state", "blocking", "格挡时"},
            {"living_state", "shapeshifted", "变形时"}, {"living_state", "archon", "执政官形态下"},
            {"living_state", "stationary", "静止"}, {"living_state", "moving", "移动中"},
            {"living_state", "surrounded", "被包围时"}, {"living_state", "off_hand_empty", "副手为空时"},
            {"living_state", "both_hands_empty", "双手皆空时"},
            {"living_state", "one_handed_martial", "持单手近战武器时"},
            {"living_state", "wielding_axe", "持斧时"},
            {"living_state", "wielding_quarterstaff", "持长杖时"},
            {"living_state", "wielding_melee_weapon", "持近战武器时"},
            {"living_state", "wielding_dagger", "持匕首时"},
            {"living_state", "wielding_two_handed_melee", "持双手近战武器时"},
            {"living_state", "dodge_rolling", "翻滚中"},
            {"living_state", "recently_took_dot", "近期承受持续伤害"},
            {"living_state", "recently_gained_rage", "近期获得怒气"},
            {"living_state", "all_minions_are_companions", "全部召唤物为同伴时"},
            {"event", "hit", "命中"}, {"event", "crit", "暴击"}, {"event", "kill", "击杀"},
            {"event", "parry", "招架"}, {"event", "block", "格挡"}, {"event", "dodge", "闪避"},
            {"event", "evade", "闪避"}, {"event", "leech", "偷取"},
            {"event", "taken_dot", "承受持续伤害"}, {"event", "rage_gained", "获得怒气"},
            {"event", "rage_spent", "消耗怒气"},
            {"event", "caused_hit", "造成命中"}, {"event", "taken_hit", "被击中"},
            {"event", "caused_crit", "造成暴击"}, {"event", "taken_crit", "被暴击"},
            {"event", "caused_ailment", "造成异常"}, {"event", "taken_ailment", "承受异常"},
            {"event", "caused_block", "造成格挡"}, {"event", "taken_block", "被格挡"},
            {"event", "caused_parry", "造成招架"}, {"event", "taken_parry", "被招架"},
            {"event", "caused_dodge", "造成闪避"}, {"event", "consumed_ailment", "消耗异常"},
            {"event", "attack_start", "开始攻击"}, {"event", "resource_gain", "获得资源"},
            {"event", "resource_spend", "消耗资源"}, {"event", "recoup", "补偿"},
            {"event", "shield_block", "盾牌格挡"}, {"event", "taken_death", "死亡"},
            {"event", "flask_used", "使用药剂"}, {"event", "use_item", "使用物品"},
            {"event", "meta_skill_triggered", "触发元技能"}, {"event", "volatility_detonated", "引爆不稳定"},
            {"event", "enter_area", "进入区域"}, {"event", "join_level", "进入世界"},
            {"event", "leave_level", "离开世界"}, {"event", "mount", "骑乘"},
            {"event", "dismount", "下骑"}, {"event", "teleport", "传送"},
            {"event", "jump", "跳跃"}, {"event", "fall", "坠落"}, {"event", "knockback", "击退"},
            {"event", "heal", "治疗"}, {"event", "reloaded", "装填"},
            {"event", "fired_ammunition", "发射弹药"}, {"event", "swap_items", "交换物品"},
            {"event", "change_target", "切换目标"}, {"event", "equipment_change", "更换装备"},
            {"event", "effect_added", "获得效果"}, {"event", "effect_removed", "移除效果"},
            {"event", "effect_expired", "效果到期"},
            {"movement", "stationary", "静止"}, {"movement", "moving", "移动"},
            {"movement", "distance", "移动距离"}, {"movement", "speed", "移动速度"},
            {"src", "nearby_enemy_count", "附近敌人数量"},
            {"src", "dist_nearest_enemy", "最近敌人", "Nearest Enemy"},
            {"src", "elemental_ailment_type_count", "元素异常种类数"},
            {"src", "count", "数量"}, {"src", "distinct", "去重数量"}, {"src", "sum", "总和"},
            {"src", "presence", "在场实体"},
            {"src", "equipment", "装备"}, {"src", "accessory", "饰品"},
            {"src", "equipment_stat", "装备数值"}, {"src", "accessory_stat", "饰品数值"},
            {"src", "rage", "怒气"},
            {"equip_slot", "head", "头部"}, {"equip_slot", "chest", "胸部"},
            {"equip_slot", "legs", "腿部"}, {"equip_slot", "feet", "足部"},
            {"equip_slot", "mainhand", "主手"}, {"equip_slot", "offhand", "副手"},
            {"equip_slot", "body", "身体"},
            {"equip_out", "count", "数量"}, {"equip_out", "level", "等级"},
            {"equip_out", "mods", "词缀"},
            {"equip_out", "armor", "护甲"},
            {"equip_out", "evasion", "闪避值"}, {"equip_out", "energy", "能量护盾"},
            {"equip_out", "energy_shield", "能量护盾"},
            {"accessory_group", "legs", "腿甲"}, {"accessory_group", "quiver", "箭袋"},
            {"accessory_group", "ring", "戒指"}, {"accessory_group", "amulet", "护身符"},
            {"accessory_group", "belt", "腰带"},
            {"presence_kind", "summoned", "召唤物"}, {"presence_kind", "totem", "图腾"},
            {"presence_kind", "companion", "同伴"}, {"presence_kind", "persistent_minion", "持久召唤物"},
            {"presence_relation", "ally", "友方"}, {"presence_relation", "enemy", "敌方"},
            {"resource", "health", "生命", "Life"},
            {"resource", "max_health", "最大生命", "Maximum Life"},
            {"resource", "mana", "魔力", "Mana"},
            {"resource", "max_mana", "最大魔力", "Maximum Mana"},
            {"resource", "energy", "能量护盾", "Energy Shield"},
            {"resource", "max_energy", "最大能量护盾", "Maximum Energy Shield"},
            {"resource", "spirit", "精魂", "Spirit"},
            {"resource", "max_spirit", "最大精魂", "Maximum Spirit"},
            {"resource", "rage", "怒气", "Rage"},
            {"cmp", "eq", "=", "="},
            {"cmp", "ne", "≠", "!="},
            {"cmp", "lt", "<", "<"},
            {"cmp", "le", "≤", "<="},
            {"cmp", "gt", ">", ">"},
            {"cmp", "ge", "≥", ">="},
            {"var", "stat_range_m", "射程", "Range"},
            {"time", "day", "天数"},
            {"hit_state", "is_entity", "命中实体", "hit an entity"},
            {"hit_state", "is_block", "击中方块", "hit a block"},
            {"hit_state", "is_miss", "落空", "miss"},
            {"damage", "physical", "物理伤害", "Physical Damage"},
            {"damage", "fire", "火焰伤害", "Fire Damage"},
            {"damage", "cold", "冰霜伤害", "Cold Damage"},
            {"damage", "light", "闪电伤害", "Lightning Damage"},
            {"damage", "chaos", "混沌伤害", "Chaos Damage"},
            {"damage", "total", "总伤害", "Total Damage"},
            {"damage", "energy", "能量护盾伤害", "Energy Shield Damage"},
            {"damage", "health", "生命伤害", "Life Damage"},
            {"damage", "overflow", "溢出伤害", "Overflow Damage"},
            {"damage", "mana", "魔力伤害", "Mana Damage"},
            {"damage", "hit", "命中", "Hit"},
            {"damage", "crit", "暴击", "Critical Hit"},
            {"damage", "chill", "冰缓", "Chill"},
            {"damage", "freeze", "冰冻", "Freeze"},
            {"damage", "shock", "感电", "Shock"},
            {"damage", "bleeding", "流血", "Bleeding"},
            {"damage", "ignite", "点燃", "Ignite"},
            {"damage", "poison", "中毒", "Poison"},
            {"damage", "stun", "眩晕", "Stun"},
            {"damage", "evasion", "闪避", "Evasion"},
            {"damage", "block", "格挡", "Block"},
            {"damage", "parry", "招架", "Parry"},
            {"presence", "entered", "进入", "entered"},
            {"presence", "left", "离开", "left"},
            {"gem", "any", "已镶嵌宝石", "Socketed Gem"},
            {"neg", "not", "非", "not"},
            {"flag", "summoned", "召唤物", "Summoned"},
            {"flag", "owner", "拥有者", "Owner"},
            {"flag", "level", "等级", "Level"},
            {"charge", "power_charge", "暴击球"}, {"charge", "frenzy_charge", "狂怒球"}, {"charge", "endurance_charge", "耐力球"},
            {"skill_state", "skill.level", "技能等级", "Skill Level"}, {"skill_state", "quality", "品质", "Quality"},
            {"skill_state", "charge", "充能", "Charge"}, {"skill_state", "press", "按下", "Press"},
            {"skill_state", "released", "释放", "Released"}, {"skill_state", "press.time", "按压时间", "Press Time"},
            {"skill_state", "bind", "绑定", "Bind"},
            {"presence_count.rel", "any", "在场", "Present"}, {"presence_count.rel", "ally", "友方", "Ally"}, {"presence_count.rel", "enemy", "敌方", "Enemy"},
            {"presence_count.kind", "any", "实体", "Entity"}, {"presence_count.kind", "summoned", "召唤物", "Summon"},
            {"presence_count.kind", "totem", "图腾", "Totem"}, {"presence_count.kind", "companion", "同伴", "Companion"},
            {"presence_count.kind", "persistent_minion", "持久召唤物", "Persistent Minion"},
            {"presence_count.measure", "count", "数量", "Count"}, {"presence_count.measure", "distinct", "种类数", "Distinct Types"},
            {"presence_rarity.rarity", "rare", "稀有", "Rare"}, {"presence_rarity.rarity", "unique", "传奇", "Unique"},
            {"presence_rarity.rarity", "rare_unique", "稀有或传奇", "Rare or Unique"},
            {"presence_rarity.scope", "enemy", "敌人", "Enemy"},
            {"distance", "to_anchor", "距离", "Distance"}, {"distance", "referrer_to_candidate", "相对距离", "Relative Distance"},
            {"distance", "hit", "命中距离", "Hit Distance"},
            {"selector", "count", "匹配数", "Count"}, {"selector", "sum", "累加值", "Sum"},
            {"selector", "pick", "选中目标", "Picked Target"}, {"selector", "each", "每个目标", "Each Target"},
            {"using_skill", "any", "任意技能", "Any Skill"}, {"using_skill", "fire", "火属性技能", "Fire Skill"},
            {"using_skill", "cold", "冰属性技能", "Cold Skill"}, {"using_skill", "lightning", "电属性技能", "Lightning Skill"},
            {"using_skill", "chaos", "混沌属性技能", "Chaos Skill"}, {"using_skill", "physical", "物理属性技能", "Physical Skill"},
            {"target_rarity.rarity", "rare", "稀有", "Rare"}, {"target_rarity.rarity", "unique", "传奇", "Unique"},
            {"target_rarity.rarity", "rare_unique", "稀有或传奇", "Rare or Unique"},
            {"flask_uses.resource", "all", "所有", "All"}, {"flask_uses.resource", "health", "生命", "Health"}, {"flask_uses.resource", "mana", "魔力", "Mana"},
            {"flask_uses.unit", "flask", "药剂", "Flask"},
            {"curse_duration_expired", "expired", "诅咒已过期", "Curse Duration Expired"},
            {"elemental_ailment_type_count", "types", "元素异常种类数", "Elemental Ailment Types"},
            {"timer", "burn", "燃烧"},
            {"ground", "on_jagged_ground", "锯齿地面"}, {"ground", "on_ignited_ground", "燃烧地面"},
            {"ground", "on_shocked_ground", "感电地面"}, {"ground", "on_chilled_ground", "冰缓地面"},
            {"delivery", "projectile", "投射物"}, {"delivery", "area", "范围"},
            {"delivery", "hazard", "危害区域"}, {"delivery", "remnant", "残痕"},
            {"delivery", "thrown", "投掷"}, {"delivery", "ball", "球体"},
            {"delivery", "orb", "法球"}, {"delivery", "beam", "光束"},
            {"delivery", "aura", "光环"}, {"delivery", "self", "自身"},
            {"meta_energy", "meta", "元技能能量"},
            {"event_tag", "ignite", "点燃"}, {"event_tag", "chill", "冰缓"},
            {"event_tag", "shock", "感电"}, {"event_tag", "freeze", "冰冻"},
            {"event_tag", "bleed", "流血"}, {"event_tag", "poison", "中毒"},
            {"biome", "is_hot", "炎热群系"}, {"biome", "is_cold", "寒冷群系"},
            {"biome", "is_swamp", "沼泽群系"}, {"biome", "is_forest", "森林群系"},
            {"biome", "is_cave", "洞穴群系"},
            // 扁平键补全（不含 marker：marker 走单词键拼装，见 ModLang / LangSupport 的 stat.<token>）
            // charge_mode
            {"charge_mode", "convert", "转换", "Convert"},
            {"charge_mode", "to_max", "补满", "To Maximum"},
            {"charge_mode", "ally_grant", "授予盟友", "Grant to Ally"},
            // roll_subject
            {"roll_subject", "damage", "伤害", "Damage"},
            {"roll_subject", "crit", "暴击", "Crit"},
            {"roll_subject", "evade", "闪避", "Evade"},
            {"roll_subject", "deflect", "格挡", "Deflect"},
            {"roll_subject", "victim_damage", "受害方伤害", "Victim Damage"},
            // roll_mode
            {"roll_mode", "lucky", "幸运", "Lucky"},
            {"roll_mode", "unlucky", "不幸", "Unlucky"},
            // hit_flag
            {"hit_flag", "unevadable", "不可闪避", "Unevadable"},
            {"hit_flag", "uncrittable", "不可暴击", "Uncrittable"},
            // reduction
            {"reduction", "armor", "护甲", "Armor"},
            {"reduction", "resistance", "抗性", "Resistance"},
            {"reduction", "fire", "火焰", "Fire"},
            {"reduction", "cold", "冰霜", "Cold"},
            {"reduction", "lightning", "闪电", "Lightning"},
            {"reduction", "chaos", "混沌", "Chaos"},
            // scope
            {"scope", "all", "所有", "All"},
    };

    private void addStatValueKeys() {
        boolean en = "en_us".equals(this.locale);
        for (String[] row : STAT_VALUE_ROWS) {
            String ns = row[0];
            String id = row[1];
            String zh = row[2];
            String key = StatValueKey.key(ns, id);
            if (key == null) continue;
            String enText = row.length >= 4 && !row[3].isEmpty() ? row[3] : ValueExpr.humanize(id);
            this.add(key, (en || zh == null || zh.isEmpty()) ? enText : zh);
        }
    }

    private void addStatEffectKeys() {
        boolean en = "en_us".equals(this.locale);
        for (String[] row : StatEffectKeys.TEMPLATE_ROWS) {
            this.add(row[0], en ? row[1] : row[2]);
        }
        for (String[] row : StatEffectKeys.VALUE_ROWS) {
            String key = row[0];
            String enText = row[1];
            String zh = row[2];
            this.add(key, (en || zh == null || zh.isEmpty()) ? enText : zh);
        }
    }

    private final Set<String> definedKeys = new HashSet<>();

    @Override
    public void add(String key, String value) {
        if (!definedKeys.add(key)) return;
        super.add(key, value);
    }

    public void addPair(String key, String en, String zh) {
        this.L.addPair(key, en, zh);
    }

    /**
     * 补齐「代码中已引用、但 datagen 从未注册」的语言键 —— 缺键时游戏内会直接显示原始键名。
     * 清单来自 loc_gap_inventory.md 的缺口清点（49 条）；统一走 addPair 提供 en/zh 双语，
     * 避免像早期键那样把中文写进 en_us。
     */
    private void addGapKeys() {
        // ---- 按键绑定 ----
        this.addPair("keymapping.coe.skill_0", "Use Skill Slot 1", "使用技能槽 1");
        this.addPair("keymapping.coe.skill_1", "Use Skill Slot 2", "使用技能槽 2");
        this.addPair("keymapping.coe.skill_2", "Use Skill Slot 3", "使用技能槽 3");
        this.addPair("keymapping.coe.skill_3", "Use Skill Slot 4", "使用技能槽 4");
        this.addPair("keymapping.coe.skill_4", "Use Skill Slot 5", "使用技能槽 5");
        this.addPair("keymapping.coe.skill_aux", "Skill Slot Modifier", "技能槽辅助键");
        this.addPair("keymapping.coe.interact_0", "Interact 1", "交互 1");
        this.addPair("keymapping.coe.interact_1", "Interact 2", "交互 2");
        this.addPair("keymapping.coe.interact_2", "Interact 3", "交互 3");
        this.addPair("keymapping.coe.interact_3", "Interact 4", "交互 4");
        this.addPair("keymapping.coe.open_skill", "Open Skill Assembly", "打开技能装配");
        this.addPair("keymapping.coe.open_talent", "Open Talent Tree", "打开天赋");
        this.addPair("keymapping.coe.open_menu", "Open Radial Menu", "打开轮盘菜单");
        this.addPair("keymapping.coe.dodge_roll", "Dodge Roll", "翻滚");
        // ---- 实体信息 tooltip 标签 ----
        this.addPair("entity.coe.camp", "Camp", "营地");
        this.addPair("entity.coe.element", "Elemental", "元素");
        this.addPair("entity.coe.energy", "Energy", "能量");
        this.addPair("entity.coe.owner", "Owner", "所有者");
        // ---- 创造模式物品栏 ----
        this.addPair("group.item.coe.def", "Craft of Exile", "流亡工艺");
        // ---- 物品 tooltip ----
        this.addPair("item.coe.currency", "Currency", "通货");
        this.addPair("item.coe.stack_size", "Stack Size", "堆叠数量");
        this.addPair("item.coe.jewel", "Jewel", "珠宝");
        this.addPair("item.coe.jewel.hint", "Socket it into a jewel socket on the talent tree to take effect", "镶嵌到天赋树的珠宝插槽后生效");
        // ---- 据点：站点界面 ----
        this.addPair("hold.coe.assigned_workers", "Assigned Workers: %s", "已分配工人：%s");
        this.addPair("hold.coe.assigned_storage", "Assigned Storage: %s", "已绑定容器：%s");
        this.addPair("hold.coe.auto_assign", "Auto Assign", "自动分配");
        this.addPair("hold.coe.max_workers", "Max Workers", "工人上限");
        this.addPair("hold.coe.priority", "Priority", "优先级");
        this.addPair("hold.coe.site.not_owner", "You do not own this site", "你不是该据点的所有者");
        // ---- 据点：站点类型 ----
        this.addPair("hold.site.coe.agriculture.description", "Farms the designated area: tills, plants and harvests the matching crops.", "在划定区域内耕作、种植并收获对应作物。");
        this.addPair("hold.site.coe.ender_charge", "Ender Charge Site", "末影充能站点");
        this.addPair("hold.site.coe.ender_charge.description", "Endermen and endermites convert Ender energy at the dragon head to replenish the stronghold's energy reserves (occupies no item storage).", "由末影人/末螨在龙首处转化末影能量，为据点补充能量储备（不占用物品容器）。");
        this.addPair("hold.site.coe.fish", "Fishing Site", "捕鱼站点");
        this.addPair("hold.site.coe.fish.description", "Gathers fish within the designated water area.", "在划定水域内获取鱼类。");
        this.addPair("hold.site.coe.forestry.description", "Fells trees in the designated area, replants saplings and clears leaf litter and vines.", "在划定区域内伐木、补种树苗并清理落叶与藤蔓。");
        this.addPair("hold.site.coe.hunt", "Hunting Site", "狩猎站点");
        this.addPair("hold.site.coe.hunt.description", "Hunts non-player mobs in the designated area; drops go to the bound container.", "在划定区域内追猎非玩家生物，掉落物存入绑定容器。");
        this.addPair("hold.site.coe.milk", "Milking Site", "挤奶站点");
        this.addPair("hold.site.coe.milk.description", "Milks the target creatures with a bucket in the designated area.", "在划定区域内手持桶为指定生物挤奶。");
        this.addPair("hold.site.coe.mining", "Mining Site", "采矿站点");
        this.addPair("hold.site.coe.mining.description", "Mines ores in the area using the cartography table as the marker block.", "以制图台为标记方块开采区域内矿石。");
        this.addPair("hold.site.coe.shear", "Shearing Site", "剪毛站点");
        this.addPair("hold.site.coe.shear.description", "Shears sheep with shears in the designated area.", "在划定区域内用剪刀剪取绵羊羊毛。");
        this.addPair("hold.site.coe.site_set", "Site Settings", "站点设置");
        // ---- 据点：库存 ----
        this.addPair("hold.stock.coe.stored", "Stored", "已存");
        // C.2：DSP 三行小号第三行——在途未取（已承诺取走、尚未拾取的量，橙）。
        this.addPair("hold.stock.coe.pending", "Pending", "待取");
        // ---- 据点：终端 ----
        this.addPair("hold.terminal.coe.combatant", "Combat", "参战");
        this.addPair("hold.terminal.coe.combatant_on", "On", "开");
        this.addPair("hold.terminal.coe.combatant_off", "Off", "关");
        this.addPair("hold.terminal.coe.combatant_work_only", "Work Only", "专注工作");
        // ---- 背包浮窗（WidgetScreen 系，B 键弹出；计划 §3.5b）----
        this.addPair("ui.coe.inventory.title", "Backpack", "背包");
        // ---- 附录 D.3：音效开关（总开关 + 悬停开关；不新增配置菜单，键位先行供后续选项页复用）----
        this.addPair("ui.coe.sfx.master", "UI Sounds", "界面音效");
        this.addPair("ui.coe.sfx.hover", "Hover Sounds", "悬停音效");
    }
}
