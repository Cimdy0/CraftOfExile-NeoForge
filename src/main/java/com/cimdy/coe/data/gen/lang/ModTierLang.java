package com.cimdy.coe.data.gen.lang;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 局部词缀阶名本地化缓存（离线烘焙）：全局词缀由 Stat 条目自带翻译，此处<b>只译局部词缀</b>。
 * key = 阶名（英文名小写下划线）；en_us = key 原样，zh_cn = name_cn。
 */
public final class ModTierLang {

    private ModTierLang() {
    }

    private static final Map<String, String> ZH = new LinkedHashMap<>();

    static {
        p0();
        pFlask();
    }

    private static void p0() {
        ZH.put("abating", "柔缓的");
        ZH.put("acrobat_s", "杂技的");
        ZH.put("adamant", "金刚的");
        ZH.put("adaptable", "灵敏的");
        ZH.put("adroit", "灵敏的");
        ZH.put("agile", "敏捷的");
        ZH.put("anointed", "杂技的");
        ZH.put("apparition_s", "灵敏的");
        ZH.put("ascendant", "幻迷的");
        ZH.put("banshee_s", "柔软的");
        ZH.put("baron_s", "男爵的");
        ZH.put("beatified", "气态的");
        ZH.put("blazing", "炽烈的");
        ZH.put("blessed", "敏捷的");
        ZH.put("blurred", "模糊的");
        ZH.put("brawler_s", "鬼魂的");
        ZH.put("buttressed", "支持的");
        ZH.put("carapaced", "装甲化的");
        ZH.put("cherub_s", "相位的");
        ZH.put("consecrated", "灵动的");
        ZH.put("count_s", "伯爵的");
        ZH.put("dancer_s", "舞者的");
        ZH.put("dauntless", "无畏的");
        ZH.put("dazzling", "眩目的");
        ZH.put("duelist_s", "幻象的");
        ZH.put("duke_s", "公爵的");
        ZH.put("durable", "相位的");
        ZH.put("eidolon_s", "灵动的");
        ZH.put("elusory", "灵动的");
        ZH.put("encased", "围绕的");
        ZH.put("enveloped", "包围的");
        ZH.put("ephemeral", "幽灵的");
        ZH.put("ethereal", "鬼魂的");
        ZH.put("evanescent", "幻象的");
        ZH.put("fearless", "无惧的");
        ZH.put("fencer_s", "幽魂的");
        ZH.put("fleet", "飘忽的");
        ZH.put("flexible", "模糊的");
        ZH.put("fortified", "强化的");
        ZH.put("fugitive", "");
        ZH.put("ghost_s", "鬼魂的");
        ZH.put("girded", "围城的");
        ZH.put("gladiator_s", "幽灵的");
        ZH.put("glimmering", "微光的");
        ZH.put("glittering", "闪闪发亮的");
        ZH.put("glowing", "泛光的");
        ZH.put("godly", "柔软的");
        ZH.put("hallowed", "相位的");
        ZH.put("hero_s", "梦魇的");
        ZH.put("illusion_s", "幻迷的");
        ZH.put("illusory", "魅影的");
        ZH.put("impenetrable", "幻迷的");
        ZH.put("impervious", "");
        ZH.put("impregnable", "坚不可摧的");
        ZH.put("incandescent", "炽焰的");
        ZH.put("incorporeal", "魅影的");
        ZH.put("inculcated", "幻象的");
        ZH.put("indomitable", "无法征服的");
        ZH.put("infixed", "阴暗的");
        ZH.put("infused", "幽灵的");
        ZH.put("ingrained", "鬼魂的");
        ZH.put("inspired", "魅影的");
        ZH.put("instilled", "幽魂的");
        ZH.put("interpermeated", "幻迷的");
        ZH.put("interpolated", "梦魇的");
        ZH.put("king_s", "国王的");
        ZH.put("lacquered", "上漆的");
        ZH.put("layered", "分层的");
        ZH.put("legend_s", "魅影的");
        ZH.put("lissome", "柔软的");
        ZH.put("lobstered", "甲壳的");
        ZH.put("lord_s", "领主的");
        ZH.put("marquess", "侯爵的");
        ZH.put("mirage_s", "魅影的");
        ZH.put("nightmare_s", "梦魇的");
        ZH.put("nymph_s", "杂技的");
        ZH.put("of_anger", "愤怒之");
        ZH.put("of_calamity", "灾难之");
        ZH.put("of_destruction", "毁灭之");
        ZH.put("of_disaster", "灾害之");
        ZH.put("of_ferocity", "凶暴之");
        ZH.put("of_fury", "狂暴之");
        ZH.put("of_havoc", "浩劫之");
        ZH.put("of_ire", "怒火之");
        ZH.put("of_menace", "威胁之");
        ZH.put("of_rage", "震怒之");
        ZH.put("of_ruin", "灭绝之");
        ZH.put("of_the_apt", "容易之");
        ZH.put("of_the_proficient", "精通之");
        ZH.put("of_the_skilled", "娴熟之");
        ZH.put("of_the_talented", "才华之");
        ZH.put("of_the_worthy", "价值之");
        ZH.put("of_unmaking", "解构之");
        ZH.put("phantasm_s", "幻象的");
        ZH.put("phased", "相位的");
        ZH.put("plated", "电镀的");
        ZH.put("pliant", "杂技的");
        ZH.put("prince_s", "亲王的");
        ZH.put("protective", "保护的");
        ZH.put("pulsing", "脉冲的");
        ZH.put("radiating", "辐射的");
        ZH.put("reinforced", "增强的");
        ZH.put("resilient", "灵动的");
        ZH.put("resolute", "坚决的");
        ZH.put("resplendent", "");
        ZH.put("ribbed", "螺纹的");
        ZH.put("saintly", "灵敏的");
        ZH.put("sanctified", "模糊的");
        ZH.put("scintillating", "夺目的");
        ZH.put("scrapper_s", "阴暗的");
        ZH.put("shade_s", "阴暗的");
        ZH.put("shadowy", "阴暗的");
        ZH.put("shining", "发光的");
        ZH.put("spectre_s", "幽魂的");
        ZH.put("spirit_s", "气态的");
        ZH.put("steadfast", "坚韧的");
        ZH.put("strong_willed", "意志坚强的");
        ZH.put("studded", "镶嵌的");
        ZH.put("sturdy", "气态的");
        ZH.put("supple", "敏捷的");
        ZH.put("sylph_s", "模糊的");
        ZH.put("thickened", "加厚的");
        ZH.put("unassailable", "坚不可摧的");
        ZH.put("unfaltering", "幻迷的");
        ZH.put("unmoving", "不动的");
        ZH.put("unreal", "梦魇的");
        ZH.put("unrelenting", "不屈的");
        ZH.put("unworldly", "幽魂的");
        ZH.put("vaporous", "气态的");
        ZH.put("versatile", "柔软的");
        ZH.put("victor_s", "幻迷的");
        ZH.put("viscount_s", "子爵的");
        ZH.put("will_o_wisp_s", "敏捷的");
        ZH.put("wraith_s", "幽灵的");
    }

    /** 药剂 / 护符词缀阶名（#215，中文取自 poe2db 语料 name_cn）。 */
    private static void pFlask() {
        ZH.put("abundant", "充裕的");
        ZH.put("analyst_s", "分析家的");
        ZH.put("aqueous", "苞蕾的");
        ZH.put("blooming", "繁花的");
        ZH.put("botanic", "植物学的");
        ZH.put("bright", "萌发的");
        ZH.put("catalysed", "催化的");
        ZH.put("clinician_s", "临床师的");
        ZH.put("compact", "紧凑的");
        ZH.put("concentrated", "浓缩的");
        ZH.put("condensed", "浓黏的");
        ZH.put("dawnlit", "晨辉的");
        ZH.put("dense", "浓稠的");
        ZH.put("drizzling", "药草的");
        ZH.put("evergreen", "常青的");
        ZH.put("examiner_s", "审查人的");
        ZH.put("experimenter_s", "实验家的");
        ZH.put("flooded", "植物学的");
        ZH.put("floral", "植被的");
        ZH.put("full_bodied", "全型的");
        ZH.put("hearty", "暖心的");
        ZH.put("herbal", "药草的");
        ZH.put("investigator_s", "调查者的");
        ZH.put("lustrous", "光辉的");
        ZH.put("mistbound", "繁花的");
        ZH.put("natural", "自然的");
        ZH.put("oceanic", "自然的");
        ZH.put("of_the_abundant", "充足之");
        ZH.put("of_the_ample", "丰沛之");
        ZH.put("of_the_apprentice", "学徒之");
        ZH.put("of_the_bottomless", "无底之");
        ZH.put("of_the_bountiful", "充裕之");
        ZH.put("of_the_brewer", "烹煮之");
        ZH.put("of_the_chemist", "药学之");
        ZH.put("of_the_constant", "恒常之");
        ZH.put("of_the_continuous", "恒续之");
        ZH.put("of_the_copious", "丰富之");
        ZH.put("of_the_distiller", "蒸馏之");
        ZH.put("of_the_doctor", "医生之");
        ZH.put("of_the_endless", "无穷之");
        ZH.put("of_the_eternal", "永恒之");
        ZH.put("of_the_foliage", "施肥之");
        ZH.put("of_the_medic", "救护之");
        ZH.put("of_the_mixologist", "调剂之");
        ZH.put("of_the_perpetual", "永久之");
        ZH.put("of_the_plentiful", "丰厚之");
        ZH.put("of_the_practitioner", "医者之");
        ZH.put("of_the_surgeon", "手术之");
        ZH.put("of_the_sylvan", "林荫之");
        ZH.put("of_the_verdant", "苍翠之");
        ZH.put("of_the_wide", "宽大之");
        ZH.put("opaque", "蔽光的");
        ZH.put("petaled", "苞蕾的");
        ZH.put("potent", "强力的");
        ZH.put("raindancer_s", "常青的");
        ZH.put("saturated", "饱和的");
        ZH.put("soaked", "植被的");
        ZH.put("sprouting", "萌发的");
        ZH.put("substantial", "重多的");
        ZH.put("sunburst", "艳阳的");
        ZH.put("sunny", "晴空的");
        ZH.put("tidebound", "萌发的");
        ZH.put("undiluted", "原汁的");
        ZH.put("vibrant", "鲜活的");
        ZH.put("viscous", "黏性的");
    }

    static void install(LangSupport L) {
        for (Map.Entry<String, String> e : ZH.entrySet()) {
            L.addPair(e.getKey(), e.getKey(), e.getValue());
        }
    }
}
