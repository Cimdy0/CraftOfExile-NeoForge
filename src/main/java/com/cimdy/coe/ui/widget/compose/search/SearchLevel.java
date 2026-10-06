package com.cimdy.coe.ui.widget.compose.search;

/**
 * 分层搜索匹配层（计划 979-991，唯一实现点）。
 *
 * <p>一条查询对一个条目按三层依次判定，命中层决定结果分组与行徽标：
 * <ul>
 *   <li>{@link #CATEGORY} 类别层：匹配分类键 / 源名（NavTree 一级 group、二级 subGroupKey、类型名）；</li>
 *   <li>{@link #ENTRY} 条目层：匹配条目名 / 列表行字段（物品名、词缀名、技能名……）；</li>
 *   <li>{@link #DETAIL} 内容层：匹配详情 / tooltip 文本（词缀效果、描述行、属性行、关键字标题）。</li>
 * </ul>
 *
 * <p>判定优先级（CodexBrowser 命中层计算）：CATEGORY → ENTRY → DETAIL，命中即返回（取最高层）；
 * 都不命中 = 该条目不入选。匹配范围为「仅条目层」时只判 ENTRY。
 */
public enum SearchLevel {
    /** 类别层：分类键 / 源名 / group / subGroupKey / 类型名。 */
    CATEGORY,
    /** 条目层：条目名 / 列表行字段。 */
    ENTRY,
    /** 内容层：详情 / tooltip 文本。 */
    DETAIL
}
