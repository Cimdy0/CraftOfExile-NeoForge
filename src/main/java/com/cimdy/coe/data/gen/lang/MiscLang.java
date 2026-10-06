package com.cimdy.coe.data.gen.lang;

import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.modifier.impl.control.Slow;
import com.cimdy.coe.registry.world.BlockRegistry;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.data.codec.stronghold.task.site.SiteTypes;
import com.cimdy.coe.data.codec.stronghold.task.site.helper.SiteBindingFeedback;
import com.cimdy.coe.data.gen.gem.skill.SkillLang;
import com.cimdy.coe.data.gen.gem.spirit.SpiritLang;
import com.cimdy.coe.data.gen.gem.support.SupportLang;

import java.util.Map;
import com.cimdy.coe.data.codec.support.rarity.Rarity;

public final class MiscLang {
    private MiscLang() {
    }

    static void installHold(LangSupport L) {
        addHoldLang(L);
    }

    static void installSkill(LangSupport L) {
        addSkillLang(L);
    }

    static void installProgression(LangSupport L) {
        addProgressionLang(L);
        addRecoveredKeys(L);
    }

    static void installStatScope(LangSupport L) {
        addStatScopeLang(L);
    }

    /**
     * 条目层作用域外溢 / 转换的描述文本（{@code StatScopeDescriber} 的模板键与作用域标签值键）。
     *
     * <p>两个从句语义相反、措辞必须相反：spill 是「两侧各计一次」、convert 是「只计目标侧」。
     * MOVE 侧的措辞刻意带「instead / 改为」，避免读成两侧都计入。
     */
    private static void addStatScopeLang(LangSupport L) {
        L.addPair("stat.entry.spill_scope", "%1$s (also applies to %2$s)", "%1$s（同样适用于 %2$s）");
        L.addPair("stat.entry.convert_scope", "%1$s (counts as %2$s instead)", "%1$s（改为计入 %2$s）");

        // 作用域标签走 stat.value.scope_tag.<tag>，缺键时由 StatValueKey 回落为可见英文 literal。
        L.addPair("stat.value.scope_tag.fire", "Fire", "火焰");
        L.addPair("stat.value.scope_tag.cold", "Cold", "冰霜");
        L.addPair("stat.value.scope_tag.lightning", "Lightning", "闪电");
        L.addPair("stat.value.scope_tag.chaos", "Chaos", "混沌");
        L.addPair("stat.value.scope_tag.physical", "Physical", "物理");
        L.addPair("stat.value.scope_tag.elemental", "Elemental", "元素伤害");
        L.addPair("stat.value.scope_tag.attack", "Attack", "攻击");
        L.addPair("stat.value.scope_tag.spell", "Spell", "法术");
        L.addPair("stat.value.scope_tag.melee", "Melee", "近战");
        L.addPair("stat.value.scope_tag.ranged", "Ranged", "远程");
        L.addPair("stat.value.scope_tag.projectile", "Projectile", "投射物");
        L.addPair("stat.value.scope_tag.area", "Area", "区域");
        L.addPair("stat.value.scope_tag.aura", "Aura", "光环");
        L.addPair("stat.value.scope_tag.beam", "Beam", "光束");
        L.addPair("stat.value.scope_tag.totem", "Totem", "图腾");
        L.addPair("stat.value.scope_tag.minion", "Minion", "随从");
        L.addPair("stat.value.scope_tag.banner", "Banner", "旗帜");
        L.addPair("stat.value.scope_tag.skill", "Skill", "技能");
    }

    private static void addProgressionLang(LangSupport L) {
        L.addZh("block.coe." + BlockRegistry.STRONGHOLD_MARKER.getId().getPath(), "界桩");
        L.addZh("block.coe." + BlockRegistry.RESEARCH_BENCH.getId().getPath(), "研究台");
        L.addZh("block.coe." + BlockRegistry.WELL.getId().getPath(), "城镇水井");
        L.addPair("block.coe." + BlockRegistry.TERMINAL.getId().getPath(), "Terminal", "终端");
        L.addPair("block.coe.well.refilled", "Flask and Charm charges refilled", "药剂与护符充能已补满");

        L.addPair("hold.coe.bind.already_owned", "This chunk already belongs to another base", "该区块已属于其他基地");
        L.addPair("hold.coe.bind.not_adjacent", "Must connect to an existing base chunk; the base cannot be a scattered island", "必须与基地已有区块相接，基地不能是散落的孤岛");
        L.addPair("hold.coe.bind.no_permission", "You are not the owner of this base", "你不是这个基地的所有者");
        L.addPair("hold.coe.bind.no_budget", "Chunk budget exhausted (%s/%s); raise level or research related tech to expand", "区块预算已用尽（%s/%s），提升等级或研究相关科技以扩张");
        L.addPair("hold.coe.bind.failed", "Binding failed", "绑定失败");
        L.addPair("hold.coe.bind.success", "Merged into base (%s/%s)", "已并入基地（%s/%s）");
        L.addPair("hold.coe.bind.removed", "Chunk binding removed", "已解除该区块的绑定");
        L.addPair("hold.coe.marker.bound", "Chunk [%s, %s] belongs to the base", "区块 [%s, %s] 已属于基地");
        L.addPair("hold.coe.marker.unbound", "Chunk [%s, %s] is not yet merged into the base", "区块 [%s, %s] 尚未并入基地");
        L.addPair("hold.coe.marker.budget", "Chunks %s/%s (Level %s)", "区块 %s/%s（等级 %s）");
        L.addPair("hold.coe.site.locked", "Work type '%s' is not yet authorized", "工种「%s」尚未授权");
        L.addPair("hold.coe.site.cap", "Site count reached maximum (%s/%s)", "站点数已达上限（%s/%s）");

        L.addPair("tech.coe.unlocked", "Unlocked technology: %s", "已解锁科技：%s");
        L.addPair("tech.coe.section", "Level %s", "等级 %s");
        L.addPair("tech.coe.cell.unlocked", "Unlocked", "已解锁");
        L.addPair("tech.coe.cell.level", "Requires Level %s", "需要等级 %s");
        L.addPair("tech.coe.cell.cost", "Requires %s Tech Points", "需要 %s 科技点");
        L.addPair("tech.coe.tooltip.click_unlock", "Click to unlock (costs %s Tech Points)", "点击解锁（消耗 %s 科技点）");
        L.addPair("tech.coe.tooltip.need_level", "Insufficient level: %s / %s", "等级不足：%s / %s");
        L.addPair("tech.coe.tooltip.need_points", "Insufficient Tech Points: %s / %s", "科技点不足：%s / %s");
        L.addPair("tech.coe.effect.chunk_budget", "Base chunk limit +%s", "基地区块上限 +%s");
        L.addPair("tech.coe.effect.entity_cap", "Worker cap +%s", "工人上限 +%s");
        L.addPair("tech.coe.effect.site_cap", "Work site cap +%s", "工作站点上限 +%s");
        L.addPair("tech.coe.effect.work_type", "Unlocks work type: %s", "解锁工种：%s");
        L.addPair("tech.coe.effect.item", "Unlocks item: %s", "解锁物品：%s");
        L.addPair("tech.coe.effect.block", "Unlocks block: %s", "解锁方块：%s");
        L.addPair("tech.coe.effect.recipe", "Unlocks recipe: %s", "解锁配方：%s");
        L.addPair("tech.coe.effect.research", "Unlocks research: %s", "解锁研究：%s");
        L.addPair("tech.coe.effect.quest", "Unlocks quest: %s", "解锁任务：%s");

        L.addPair("research.coe.completed", "Research complete: %s", "研究完成：%s");
        L.addPair("research.coe.in_progress", "In progress: %s (%s%%)", "研究中：%s（%s%%）");
        L.addPair("research.coe.tooltip.cost", "Costs %s Research Points", "消耗 %s 研究点");
        L.addPair("research.coe.tooltip.duration", "Takes %s seconds", "耗时 %s 秒");
        L.addPair("research.coe.tooltip.efficiency", "%s Efficiency %s", "%s 效率 %s");
        L.addPair("research.coe.tooltip.priority", "Workers prioritize: %s", "工人优先处理：%s");
        L.addPair("research.coe.tooltip.allow", "Allowed work types: %s", "放行工种：%s");
        L.addPair("research.coe.tooltip.prerequisites", "Prerequisites: %s", "前置：%s");
        L.addPair("research.coe.bench.idle", "Research bench idle", "研究台空闲");
        L.addPair("research.coe.bench.running", "Researching: %s", "研究中：%s");
        L.addPair("research.coe.bench.remaining", "%s seconds remaining", "剩余 %s 秒");
        L.addPair("research.coe.bench.hint", "The research bench can only run one study at a time", "研究台一次只能进行一项研究");
        L.addPair("research.coe.bench.absent", "No research bench connected; view only", "未连接研究台，仅可查看");
        L.addPair("research.coe.tooltip.missing_prereq", "Missing prerequisite: %s", "前置缺失：%s");
        L.addPair("research.coe.panel.broken", "Data error: prerequisites missing for %s studies", "数据异常：%s 个研究的前置不存在");

        L.addZh("screen.coe.tech_tree", "科技树");
        L.addZh("screen.coe.research_tree", "研究树");
        L.addZh("screen.coe.tab.tech", "科技");
        L.addZh("screen.coe.tab.research", "研究");
        L.addPair("screen.coe.hint.pan_zoom", "Drag to pan - Scroll to zoom", "拖拽平移 · 滚轮缩放");
        L.addPair("screen.coe.hint.scroll", "Scroll to scroll", "滚轮滚动");

        L.addPair("progression.coe.level", "Level %s", "等级 %s");
        L.addPair("progression.coe.xp", "XP %s / %s", "经验 %s / %s");
        L.addPair("progression.coe.tech_points", "Tech Points %s", "科技点 %s");
        L.addPair("progression.coe.research_points", "Research Points %s", "研究点 %s");
        L.addPair("progression.coe.unlocked_tech", "Unlocked tech %s / %s", "已解锁科技 %s / %s");
        L.addPair("progression.coe.completed_research", "Completed research %s / %s", "已完成研究 %s / %s");
        L.addPair("progression.coe.chunk_budget", "Chunks %s / %s", "区块 %s / %s");
        L.addPair("progression.coe.entity_cap", "Worker cap %s", "工人上限 %s");
        L.addPair("progression.coe.site_cap", "Site cap %s", "站点上限 %s");

        L.addPair("quest.coe.completed", "Quest complete: %s", "任务完成：%s");

        L.addZh("work_type.coe.all", "全部工种");
        Map<String, String> names = Map.ofEntries(
                Map.entry("forestry", "伐木"),
                Map.entry("agriculture", "耕种"),
                Map.entry("mining", "采矿"),
                Map.entry("shear", "剪毛"),
                Map.entry("hunt", "狩猎"),
                Map.entry("milk", "挤奶"),
                Map.entry("fisher", "捕鱼"),
                Map.entry("ender_charge", "末影充能"),
                Map.entry("composter", "堆肥"),
                Map.entry("mill", "研磨"),
                Map.entry("crusher", "粉碎"),
                Map.entry("enchanter", "附魔"),
                Map.entry("recipe", "合成"));
        for (Site.Type<?> type : SiteTypes.REGISTRY) {
            String name = SiteTypes.workTypeName(type);
            if (name.isEmpty()) continue;
            L.addZh("work_type.coe." + name, names.getOrDefault(name, name));
        }
    }

    private static void addRecoveredKeys(LangSupport L) {
        L.addPair("charm.coe.charm_ruby_effect", "Grants +25% Fire Resistance (4s)", "获得 +25% 火焰抗性 (4秒)");
        L.addPair("charm.coe.charm_sapphire_effect", "Grants +25% Cold Resistance (4s)", "获得 +25% 冰霜抗性 (4秒)");
        L.addPair("charm.coe.charm_topaz_effect", "Grants +25% Lightning Resistance (4s)", "获得 +25% 闪电抗性 (4秒)");
        L.addPair("charm.coe.charm_amethyst_effect", "Grants +18% Chaos Resistance (4s)", "获得 +18% 混沌抗性 (4秒)");
        L.addPair("charm.coe.charm_dousing_effect", "Immunity to Ignite (3s)", "免疫点燃 (3秒)");
        L.addPair("charm.coe.charm_thawing_effect", "Immunity to Freeze (3s)", "免疫冻结 (3秒)");
        L.addPair("charm.coe.charm_grounding_effect", "Immunity to Shock (3s)", "免疫感电 (3秒)");
        L.addPair("charm.coe.charm_staunching_effect", "Immunity to Bleed (3s)", "免疫流血 (3秒)");
        L.addPair("charm.coe.charm_antidote_effect", "Immunity to Poison (3s)", "免疫中毒 (3秒)");
        L.addPair("charm.coe.charm_stone_effect", "Immunity to Stun (2.5s)", "免疫眩晕 (2.5秒)");
        L.addPair("charm.coe.charm_silver_effect", "Immunity to Slow (3s)", "免疫减速 (3秒)");
        L.addPair("charm.coe.charm_golden_effect", "+20% Item Rarity when killing Rare/Unique enemies (1s)", "击杀稀有/传奇敌人时 +20% 物品稀有度 (1秒)");

        L.addPair("charm.trigger.fire", "When you take Fire damage", "受到火焰伤害时");
        L.addPair("charm.trigger.cold", "When you take Cold damage", "受到冰霜伤害时");
        L.addPair("charm.trigger.lightning", "When you take Lightning damage", "受到闪电伤害时");
        L.addPair("charm.trigger.chaos", "When you take Chaos damage", "受到混沌伤害时");
        L.addPair("charm.trigger.ignite", "When you are Ignited", "被点燃时");
        L.addPair("charm.trigger.freeze", "When you are Frozen", "被冻结时");
        L.addPair("charm.trigger.shock", "When you are Shocked", "被感电时");
        L.addPair("charm.trigger.bleed", "When you are Bleeding", "被流血时");
        L.addPair("charm.trigger.poison", "When you are Poisoned", "被中毒时");
        L.addPair("charm.trigger.stun", "When you are Stunned", "被眩晕时");
        L.addPair("charm.trigger.slow", "When you are Slowed", "被减速时");
        L.addPair("charm.trigger.kill_rare_unique", "When you kill a Rare/Unique enemy", "击杀稀有/传奇敌人时");

        L.addPair("item.coe.sword", "Sword", "剑");
        L.addPair("item.coe.axe", "Axe", "斧");
        L.addPair("item.coe.bow", "Bow", "弓");
        L.addPair("item.coe.cross_bow", "Crossbow", "弩");
        L.addPair("item.coe.mace", "Mace", "锤");
        L.addPair("item.coe.shield", "Shield", "盾牌");
        L.addPair("item.coe.foci", "Focus", "法器");
        L.addPair("item.coe.head", "Helmet", "头盔");
        L.addPair("item.coe.chest", "Body Armour", "胸甲");
        L.addPair("item.coe.legs", "Leggings", "护腿");
        L.addPair("item.coe.feet", "Boots", "靴子");

        L.addPair("item.state.coe.mirror", "Mirrored", "已复制");
        L.addPair("item.state.coe.wisdom", "Identified", "已鉴定");
        // ---- 通货 / 词缀 操作的失败提示（缺键时游戏内会直接显示键名，故必须注册）----
        L.addPair("item.state.coe.un_wisdom", "Must be unidentified", "需要未鉴定的物品");
        L.addPair("item.state.coe.un_common", "Can only be used on normal items", "只能用于普通物品");
        L.addPair("item.state.coe.un_magic", "Can only be used on magic items", "只能用于魔法物品");
        L.addPair("item.state.coe.un_rare", "Can only be used on rare items", "只能用于稀有物品");
        L.addPair("item.state.coe.unique", "Cannot be used on unique items", "不能用于传奇物品");
        L.addPair("item.state.coe.below_magic", "Cannot be used on this item", "不能用于该物品");
        L.addPair("item.state.type.coe.unknown", "Unknown item type", "无法识别的物品类别");
        L.addPair("item.state.mods.coe.empty", "No explicit modifiers to remove", "没有可移除的词缀");
        L.addPair("item.mod.typecoe.all.maxed", "Modifier limit reached", "词缀数量已达上限");
        L.addPair("item.mod.typecoe.no.available", "No modifier available for this item", "该物品没有可用的词缀");
        L.addPair("item.mod.coe.veiled", "Veiled", "隐匿");
        L.addPair("item.mod.coe.entity.weakness", "Weakness", "虚弱");

        L.addPair("keymapping.coe.flask_1", "Use Life Flask Slot", "使用生命药剂槽");
        L.addPair("keymapping.coe.flask_2", "Use Mana Flask Slot", "使用魔力药剂槽");
        L.addPair("keymapping.coe.flask_all", "Use All Flasks", "使用全部药剂");

        L.addPair("radial.coe.skill_assembly", "Skill Assembly", "技能装配");
        L.addPair("radial.coe.talent", "Talent", "天赋");

        L.addPair("item.mod.coe.tier", "Tier", "等阶");
        L.addPair("item.mod.coe.base", "Base", "基底");

        // ---- 现字面量泄漏修复（UI/GUI/聊天消息：原 Component.literal / translatable(原始中文) 直写，en_us 无法去化）----
        L.addPair("msg.coe.spirit_aura_enable", "Spirit aura must be enabled in the assembly screen", "精魂光环请在装配界面开启");
        L.addPair("msg.coe.skill_data_missing", "Skill data missing", "技能数据缺失");
        L.addPair("msg.coe.attr_requirement", "Attribute requirement not met", "属性需求不足");
        L.addPair("msg.coe.cast_condition", "Cast conditions not met", "施法条件不满足");
        L.addPair("msg.coe.key_unbound", "This key is not bound to a skill", "该按键未绑定技能");
        L.addPair("msg.coe.slot_empty", "No skill gem installed in this slot", "该技能槽未装配技能石");
        L.addPair("msg.coe.on_cooldown", "Skill on cooldown", "技能冷却中");
        L.addPair("msg.coe.inv_full", "Inventory full, cannot replace skill", "背包已满，无法替换技能");
        L.addPair("ui.coe.skill_assembly", "Skill Assembly", "技能装配");
        L.addPair("ui.coe.panel_test", "Panel Test", "面板测试");
        L.addPair("ui.coe.flask_charm_trinket", "Flasks / Charms / Trinkets", "药剂 / 护符 / 饰品");
        L.addPair("item.coe.radiation_jewel", "Radiation Jewel", "辐射之珊宝");
        L.addPair("ui.coe.codex", "Item Codex", "物品图鉴");
        L.addPair("ui.coe.items", "Items", "物品");
        L.addPair("ui.coe.affixes", "Affixes", "词缀");
        L.addPair("ui.coe.stats", "Stats", "属性");
        L.addPair("ui.coe.desc_lang", "Description language: client ", "描述语言：客户端 ");
        L.addPair("ui.coe.flask", "Flasks", "药剂");
        L.addPair("ui.coe.charm", "Charms", "护符");
        L.addPair("ui.coe.trinket", "Trinkets", "饰品");
        L.addPair("ui.coe.back", "Back", "返回");
        L.addPair("ui.coe.close", "Close", "关闭");
        L.addPair("ui.coe.style_picker.title", "Style Presets", "风格预设");
        L.addPair("ui.coe.style_picker.active", "Active", "当前");
        L.addPair("keymapping.coe.cycle_style", "Cycle Style", "切换风格");
        L.addPair("keymapping.coe.open_style_picker", "Open Style Picker", "打开风格选择器");
        L.addPair("keymapping.coe.reload_style", "Reload Styles", "重新加载风格");
        L.addPair("ui.coe.info", "Info", "信息");
        L.addPair("talent.coe.choose_one", "Choose one: ", "选择其一: ");
        L.addPair("talent.coe.excludes", "Mutually exclusive with %s", "与 %s 互斥");
        L.addPair("talent.coe.links", "Links: ", "连接: ");
        L.addPair("jewel.coe.radius", "Radiation radius: %s (nodes within range can be lit without links)", "辐射半径: %s（范围内节点可无链接点亮）");
        L.addPair("jewel.coe.base", "Base jewel: provides only the above attributes", "基础珊宝：仅提供上述属性");
        L.addPair("ui.coe.search", "Search...", "搜索...");
        L.addPair("talent.coe.cancel_socket", "✖ Cancel socket lighting", "✖ 取消点亮插槽");
        L.addPair("talent.coe.stat_bonus", "Stat bonuses", "属性加成");
        L.addPair("talent.coe.create_set", "+ Create set", "+ 创建套装");
        L.addPair("ui.coe.none", "(None)", "(暂无)");

        // ---- 硬编码中文字面量泄漏修复（原 Component.literal 直写中文，en_us 无法去化）----
        L.addPair("item.coe.enchantments.header", "Enchantments", "附魔");
        L.addPair("item.coe.contrib.header", "Stat Contribution", "数值贡献");
        L.addPair("entity.coe.work_speed", "Work Speed: ", "工作速度: ");
        L.addPair("entity.coe.mutated", "(Mutated)", "(突变)");
        L.addPair("talent.coe.weapon_set_global", "Weapon Set: Global (always active)", "武器组：全局（恒点亮）");
        L.addPair("talent.coe.weapon_set_hotbar", "Weapon Set: Hotbar %s (%s)", "武器组：快捷栏 %s (%s)");
        L.addPair("talent.coe.points", "Points: %s / %s", "点: %s / %s");
        L.addPair("talent.coe.weapon_set", "Weapon Set", "武器套装");
        L.addPair("talent.coe.summary_header", "Total Stat Bonuses (%s)", "总属性加成（%s）");
        L.addPair("talent.coe.global", "Global", "全局");
        L.addPair("talent.coe.respec", "Reset", "重置");
        L.addPair("talent.coe.save", "Save", "保存");
        L.addPair("talent.coe.gold", "Gold: %s", "金币：%s");
        L.addPair("talent.coe.respec_cost", "Cost: %s (%s pts)", "花费：%s（%s 点）");
        L.addPair("talent.coe.unsaved_allocations", "Unsaved points", "有未保存加点");
        L.addPair("talent.coe.reject_reset_during_allocate", "Save pending points first", "请先保存加点改动");
        L.addPair("talent.coe.reject_alloc_during_reset", "Save or cancel the pending reset first", "请先保存或取消重置");
        L.addPair("talent.coe.respec_insufficient", "Not enough gold", "金币不足");
        L.addPair("talent.coe.respec_nothing", "No points to reset", "没有可重置的点数");
        L.addPair("talent.coe.commit_sent", "Saving...", "正在保存……");
        L.addPair("talent.coe.reverted", "Changes discarded", "改动已放弃");
        L.addPair("talent.coe.exit_message", "You have unsaved talent changes.", "你有未保存的天赋改动。");
        L.addPair("talent.coe.exit_save", "Save and exit", "保存并退出");
        L.addPair("talent.coe.exit_discard", "Discard and exit", "放弃改动并退出");
        L.addPair("talent.coe.exit_cancel", "Cancel", "取消");
        L.addPair("status.coe.minion_reviving", "Reviving", "复活中");
        L.addPair("talent.type.coe.travel", "Travel", "旅行");
        L.addPair("talent.type.coe.minor", "Minor", "小型");
        L.addPair("talent.type.coe.notable", "Notable", "显著");
        L.addPair("talent.type.coe.keystone", "Keystone", "基石");
        L.addPair("talent.type.coe.jewel", "Jewel Socket", "珠宝插槽");
        L.addPair("talent.type.coe.ascendancy", "Ascendancy", "升阶");
        L.addPair("talent.type.coe.mastery", "Mastery", "掌握");

        // ---- 天赋提交保存 / 金币重置（POE2：无后悔石，重置按等级单价付金币）----
        L.addPair("talent.coe.commit.save", "Save", "保存");
        L.addPair("talent.coe.commit.reset", "Reset", "重置");
        L.addPair("talent.coe.commit.reset_cost", "Reset cost: %s gold (%s points \u00d7 %s per point)", "重置花费：%s 金币（%s 点 \u00d7 每点 %s）");
        L.addPair("talent.coe.commit.reset_confirm", "Reset all %s allocated points for %s gold?", "花费 %2$s 金币重置全部 %1$s 点天赋？");
        L.addPair("talent.coe.commit.saved", "Talent changes saved (%s points)", "天赋改动已保存（%s 点）");
        L.addPair("talent.coe.commit.reset_done", "Talent tree reset for %s gold", "已花费 %s 金币重置天赋树");
        L.addPair("talent.coe.commit.fail.not_enough_gold", "Not enough gold: need %s, have %s", "金币不足：需要 %s，持有 %s");
        L.addPair("talent.coe.commit.fail.too_many_points", "Too many points allocated: %s / %s", "已分配点数超限：%s / %s");
        L.addPair("talent.coe.commit.fail.unknown_node", "Talent node no longer exists", "该天赋节点已不存在");
        L.addPair("talent.coe.commit.fail.unreachable_node", "Some allocated nodes are not connected to the tree", "部分已分配节点与天赋树不连通");
        L.addPair("talent.coe.commit.fail.bad_mode", "Invalid talent request", "非法的天赋请求");
        L.addPair("talent.coe.commit.unsaved", "Unsaved talent changes", "存在未保存的天赋改动");
        L.addPair("talent.coe.commit.unsaved_body", "You have unsaved talent changes. Save them, or discard them?", "你有未保存的天赋改动。要保存还是丢弃？");
        L.addPair("talent.coe.commit.discard", "Discard", "丢弃");
    }

    private static void addHoldLang(LangSupport L) {
        L.addPair("hold.coe.description", "Stronghold", "据点");
        L.addPair("hold.coe.name", "Name", "名称");
        L.addPair("hold.coe.team", "Owner", "所属");
        L.addPair("hold.coe.level", "Tier", "等阶");
        L.addPair("hold.coe.energy", "Energy", "能量");
        L.addPair("hold.coe.terminal_set", "View", "查看");
        L.addPair("hold.terminal.coe.player_entity", "Storage", "仓库");
        L.addPair("hold.terminal.coe.work_entity", "Work", "工作");
        L.addPair("hold.terminal.coe.stock", "Stock", "库存");
        L.addPair("hold.terminal.coe.task", "Tasks", "任务");
        L.addPair("hold.terminal.coe.ally", "Ally", "友");
        L.addPair("hold.terminal.coe.ally_no_work", "Does not participate in stronghold work", "不参与据点工作");
        L.addPair("hold.coe.terminal_teleport", "Teleport", "传送");
        L.addPair("hold.coe.stock_set", "Stock Settings", "存量设定");
        // ---- 终端不可逆操作的确认对话框文案 ----
        L.addPair("ui.coe.confirm.title", "Confirm", "确认");
        L.addPair("ui.coe.confirm.spawn_entity", "Spawn this entity into the stronghold?", "确认将该实体放出到据点？");
        L.addPair("ui.coe.confirm.recycle_entity", "Recycle this entity back to storage?", "确认回收该实体？");
        L.addZh("hold.stock.coe.range." + "global", "全局");
        L.addZh("hold.stock.coe.range." + "dimension", "维度");
        L.addZh("hold.stock.coe.range." + "local", "本地");
        L.addZh("hold.stock.coe.mode." + "supply", "供应");
        L.addZh("hold.stock.coe.mode." + "demand", "需求");
        L.addZh("hold.stock.coe.mode." + "storage", "仓储");
        L.addZh("hold.stock.coe.has", "当前");
        L.addZh("hold.stock.coe.transit", "在途");
        L.addZh("hold.stock.coe.max", "最大");
        L.addZh("hold.stock.coe.unset", "未配置");
        L.addZh("hold.stock.coe.priority", "优先级");
        L.addZh("hold.stock.coe.min_transfer", "最小量");
        L.addPair("hold.site.coe.recipe", "Recipe Site", "配方站点");
        L.addPair("hold.site.coe.recipe.description", "Executes all recipes of a given type at the corresponding vanilla workstation, based on a data-driven 'recipe type to block' mapping.", "依据数据驱动的「配方类型→方块」映射，在对应原版工作站执行该类型的全部配方。");
        L.addPair("hold.site.coe.crusher", "Crusher Site", "粉碎站点");
        L.addPair("hold.site.coe.crusher.description", "Crushes cobblestone, sand and stone down into finer grains, or recycles them into constituents (no vanilla recipe; handled by the conversion pipeline).", "将圆石、沙子、石料逐级粉碎为更细颗粒，或回收为构成物（无原版配方，由转化管线处理）。");
        L.addPair("hold.site.coe.enchanter", "Enchanter Site", "附魔站点");
        L.addPair("hold.site.coe.enchanter.description", "Uses an Enchanting Table as the marker block to enchant tools/gear with enchanted books, consuming Lapis Lazuli and Ender energy (no vanilla recipe; handled by the enchantment pipeline).", "以附魔台为标记方块，使用附魔书为工具/装备附魔，消耗青金石与末影能量（无原版配方，由附魔管线处理）。");
        L.addPair("hold.site.coe.compost", "Compost Site", "堆肥站点");
        L.addPair("hold.site.coe.compost.description", "Composts organic waste into Bone Meal (no vanilla recipe; handled by the conversion pipeline).", "将有机废料堆肥为骨粉（无原版配方，由转化管线处理）。");
        L.addPair("hold.site.coe.mill", "Mill Site", "研磨站点");
        L.addPair("hold.site.coe.mill.description", "Grinds flowers/dye ingredients into the corresponding dye (no vanilla recipe; handled by the conversion pipeline).", "将花卉/染料原料研磨为对应染料（无原版配方，由转化管线处理）。");
        SiteBindingFeedback.getTranslationKeys().forEach((k, v) -> L.addPair(k, v[0], v[1]));
    }

    private static void addSkillLang(LangSupport L) {
        SkillLang.addAll(L.provider());
        SupportLang.addAll(L.provider());
        SpiritLang.addAll(L.provider());
    }

}
