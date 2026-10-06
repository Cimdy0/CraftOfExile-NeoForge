"""第五批注释清理：把「变更史叙述」改写为「对当前实现的约束说明」。

目标词汇：此前 / 原先 / 原来 / 旧版 / 旧实现 / 曾 / 已删除 / 已移除 / 已废弃 / 已弃用。
保留的是「为什么必须这样写」（约束），删除的是「过去曾经怎样、哪次改的」（过程）。

用法：python scripts/tidy_change_narrative.py [--dry]
"""
from __future__ import annotations

import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent / "src" / "main" / "java" / "com" / "cimdy" / "coe"

# 整行删除：纯「旧东西没了」的说明，删掉后不损失任何当前约束。
# (相对路径, 行内唯一子串)
LINE_DELETES: list[tuple[str, str]] = [
    ("data/helper/BlockHelper.java", "不再使用已删除的 {@code StunStory} 归一化专用条。"),
    ("event/AttributeEvent.java", "// 旧 AttributeData 平行通道已删除。"),
]

# 定点改写：(相对路径, 原文, 新文)。每条断言命中且仅命中一次。
REPLACEMENTS: list[tuple[str, str, str]] = [
    # ── D 组：此前 / 原先 / 原来 ──────────────────────────────────────────
    ("data/codec/stronghold/task/side/imp/RecipeSite.java",
     " * 取代此前「每种配方类型都注册一个专用站点类」的做法（{@code CraftSite}/{@code FurnaceSite}/",
     " * 无需为每种配方类型各注册一个专用站点类（{@code CraftSite}/{@code FurnaceSite}/"),
    ("data/codec/stronghold/task/side/RecipeSiteBlocks.java",
     " * 取代此前「每种配方类型都注册一个专用站点类」的做法：只需在数据包中声明",
     " * 无需为每种配方类型各注册一个专用站点类：只需在数据包中声明"),
    ("data/codec/support/math/context/ContextValueTypes.java",
     "     * 各键的 decode 只在 name 段查点号。此前 count 的实现把后缀拼在<b>末尾</b>",
     "     * 各键的 decode 只在 name 段查点号：后缀若拼在<b>末尾</b>"),
    ("data/codec/support/math/context/ContextValueTypes.java",
     "     * 「带标签+默认窗口」两种组合而侥幸未触发，本方法一并修正（对既有条目输出零变化）。",
     "     * 「带标签+默认窗口」两种组合才未暴露，本方法一并处理（对既有条目输出零变化）。"),
    ("data/codec/support/math/context/nodes/CompareValue.java",
     " * （此前机制无比较节点，此类条件无法表达，只能近似或留缺口；忠实接线原则下新增本节点收治缺口）。",
     " * （无比较节点时此类条件无法表达，只能近似或留缺口）。"),
    ("data/codec/support/stat/entry/StatEntry.java",
     "     * 此前条件求值走裸 {@code ExpressionContext.get()} 无 vars，{@code $name} 写进条件没有通道。",
     "     * 条件求值不可走裸 {@code ExpressionContext.get()}（无 vars，{@code $name} 无通道）。"),
    ("data/codec/support/stat/StatContext.java",
     "     * 闭合「event:/hit_state 在 Stat 通道恒 0」的断链（求值基座此前为裸 {@code ExpressionContext.get()}）。",
     "     * 求值基座须为完整 {@code ExpressionContext}；裸 {@code ExpressionContext.get()} 会让 event:/hit_state 在 Stat 通道恒 0。"),
    ("data/codec/support/stat/StatTag.java",
     "     * <p>此前 {@code parentsOf} 每次调用都全表遍历，而 fire 触发索引（2-7）会对每个事件标签调用它，",
     "     * <p>{@code parentsOf} 每次调用都全表遍历，而 fire 触发索引会对每个事件标签调用它，"),
    ("data/codec/support/stat/StatTagHierarchy.java",
     " * <p>此前 {@link StatTag} 的父子关系写死在静态表里，新增一个\"元素/异常\"类目必须改 Java 代码重新编译。\n"
     " * 现改为数据驱动：",
     " * <p>父子关系若写死在静态表里，新增一个\"元素/异常\"类目就必须改 Java 代码重新编译；\n"
     " * 故走数据驱动："),
    ("data/codec/support/stat/value/StatValue.java",
     " * <p>这是「模板生成 Stat → 部件生成」彻底解耦的最后一块拼图：此前 magnitude 以 {@code $value}\n"
     " * 变量留在 {@link com.cimdy.coe.data.codec.support.stat.entry.Stat} 层（经命名整 Stat + vars 注入），\n"
     " * 与已是路径引用的 effect / targets / conditions 互斥。现 magnitude 也下沉为内容寻址部件，",
     " * <p>这是「模板生成 Stat → 部件生成」彻底解耦的最后一块拼图：magnitude 若以 {@code $value}\n"
     " * 变量留在 {@link com.cimdy.coe.data.codec.support.stat.entry.Stat} 层（经命名整 Stat + vars 注入），\n"
     " * 就与已是路径引用的 effect / targets / conditions 互斥。故 magnitude 也下沉为内容寻址部件，"),
    ("event/DamagePipeline.java",
     "        /** 伤害数组已计算、暴击已定（{@code damages[]} 就绪）。此前命中判定已完成、作用域已激活。 */",
     "        /** 伤害数组已计算、暴击已定（{@code damages[]} 就绪）；此时命中判定已完成、作用域已激活。 */"),
    ("event/data/GenEvent.java",
     "        // 从数据包读取（取代此前纯内存 encode→decode 往返，数据包可覆盖/新增条目）。",
     "        // 从数据包读取（而非纯内存 encode→decode 往返），使数据包可覆盖/新增条目。"),
    ("event/data/LoadEvent.java",
     "     * 取代此前「纯内存 encode→decode 往返」——数据包可覆盖/新增条目，加载失败 fail-fast 暴露。",
     "     * 不走「纯内存 encode→decode 往返」——数据包可覆盖/新增条目，加载失败 fail-fast 暴露。"),
    ("event/EventName.java",
     "    // 消费端（StatGen 条目 tags）此前手写 \"consume_power\" 字面量，两端一旦改名即静默失配。",
     "    // 消费端（StatGen 条目 tags）不得手写 \"consume_power\" 字面量：两端一旦改名即静默失配。"),
    ("event/GameEvent.java",
     "        // 使 event:/event_tag:/hit_state:* 节点在 Stat 条件中可用（此前基座为裸单例、恒 0）",
     "        // 使 event:/event_tag:/hit_state:* 节点在 Stat 条件中可用（基座为裸单例时恒 0）"),
    ("event/SkillEvent.java",
     "        // （EventName.USE_WARCRY 此前只有声明、无任何调用点，导致既有的",
     "        // （USE_WARCRY 若只有声明、无任何调用点，则既有的"),
    ("event/SkillEvent.java",
     "        //  恒不触发）。此处补上计数队列记录，使「使用战吼时/近期使用过战吼」类条目可读。",
     "        //  恒不触发），故此处补上计数队列记录，使「使用战吼时/近期使用过战吼」类条目可读。"),
    ("ui/widget/component/OptionListPopup.java",
     " * <p>合并了旧库的 {@code TooltipPicker} 与 {@code ModalDialog}。这两个类此前各写了一遍",
     " * <p>合并了旧库的 {@code TooltipPicker} 与 {@code ModalDialog}。这两个类各写了一遍"),
    ("ui/widget/component/Stepper.java",
     " * <p>调数值这件事此前只有 {@link ValueBar} 的滑杆一条路：拖拽精度差，想精确到「刚好 7」很难。",
     " * <p>只靠 {@link ValueBar} 的滑杆调数值时：拖拽精度差，想精确到「刚好 7」很难。"),
    ("ui/widget/component/TabBar.java",
     " * <p>此前「切页」这件事各界面都是自己摆一排 {@code Button} 再手写「点一个就把其它的 active 清掉」，",
     " * <p>各界面若都自己摆一排 {@code Button} 再手写「点一个就把其它的 active 清掉」，"),
    ("ui/widget/compose/Collapsible.java",
     " * <p>「把一堆设置分组、默认收起来、点标题展开」是长界面的刚需，此前只能靠\n"
     " * {@code visible = false} 硬切",
     " * <p>「把一堆设置分组、默认收起来、点标题展开」是长界面的刚需，只靠\n"
     " * {@code visible = false} 硬切"),
    ("data/codec/skill/phase/action/ActionContext.java",
     " * 不再作为独立组件重复缓存（原先同一份数据在 record 中存了两份，且触发时需多次 with* 拷贝同步）。",
     " * 不作为独立组件重复缓存（否则同一份数据在 record 中存两份，且触发时需多次 with* 拷贝同步）。"),
    ("data/codec/skill/remoke/SkillDamageSource.java",
     "     * <p>原先在 {@code DamageEvent} / {@code DamagePipeline} / {@code AilmentHelper} 各有一份\n"
     "     * 逐字相同的 {@code skillIdsOf(DamageSource)}，此处收敛为单一来源。",
     "     * <p>{@code skillIdsOf(DamageSource)} 的逐字重复实现不散落在 {@code DamageEvent} /\n"
     "     * {@code DamagePipeline} / {@code AilmentHelper} 各处，而收敛于本类单一来源。"),
    ("data/codec/stronghold/task/WorkType.java",
     " * 取代原先分散在 {@link com.cimdy.coe.data.codec.stronghold.task.side.SiteTypes}、",
     " * 取代分散在 {@link com.cimdy.coe.data.codec.stronghold.task.side.SiteTypes}、"),
    ("data/codec/stronghold/task/WorkTypeRegistry.java",
     " * （目标注入）的唯一内建来源，消除原先三套平行清单需手动同步的问题。",
     " * （目标注入）的唯一内建来源，避免三套平行清单需手动同步。"),
    ("data/codec/support/math/context/EntityQuery.java",
     " * 原先「获取范围内实体 → 直接返回数值」的检测被拆为两段：",
     " * 「获取范围内实体 → 直接返回数值」被拆为两段："),
    ("data/codec/support/stat/effect/MarkerStatEffect.java",
     " * 统一惰性标记效果（INERT 型，不进 StatStore 索引）：吸收原先 4 个结构完全相同、仅 {@code type()} 字符串",
     " * 统一惰性标记效果（INERT 型，不进 StatStore 索引）：收敛 4 个结构完全相同、仅 {@code type()} 字符串"),
    ("data/codec/support/stat/effect/TargetSelector.java",
     " * 取代原先散布在 {@link AbstractStatEffect} 各子类里的硬编码 {@code resolveTargets} 布尔分支，",
     " * 取代散布在 {@link AbstractStatEffect} 各子类里的硬编码 {@code resolveTargets} 布尔分支，"),
    ("data/codec/support/stat/entry/StatEntry.java",
     "     * {@link StatRead} 覆写为「解析引用后构造内联 {@link Stat}」——取代原先在 withAmount/withEffect",
     "     * {@link StatRead} 覆写为「解析引用后构造内联 {@link Stat}」——取代在 withAmount/withEffect"),
    ("data/codec/support/stat/StatContext.java",
     "     * （原先散落在 {@code SkillStatScopes} / {@code DamageEvent} / {@code MetaSkillAction} 的逐字重复实现已收敛于此）。",
     "     * （{@code SkillStatScopes} / {@code DamageEvent} / {@code MetaSkillAction} 的逐字重复实现已收敛于此）。"),
    ("data/helper/impl/RecoupValue.java",
     "            // 不受「剩余量缩水为新 original」影响，累计回复 = 原始补给总量（避免原先仅回复约一半的衰减 bug）",
     "            // 不受「剩余量缩水为新 original」影响，累计回复 = 原始补给总量（否则会衰减到仅回复约一半）"),
    ("data/helper/StunMeter.java",
     " * 原先并行的 {@code StunStory} 与 {@code StaggerValue} 自有累积逻辑均已删除。",
     " * 本类是唯一的眩晕累积口径，不并行维护多套累积逻辑。"),
    ("entity/goal/hold/helper/TimedInteraction.java",
     " * 原先 {@code ContainerInteractionHandler}/{@code FetchQueueHandler}/\n"
     " * {@code SmartDepositHandler}/{@code ToolManager} 各有独立但几乎相同的 MOVING→OPENING→EXECUTING→CLOSING\n"
     " * 状态机实现，约 200 行重复代码。现在子类只需实现 {@link #execute(ServerLevel, BlockPos)}",
     " * {@code ContainerInteractionHandler}/{@code FetchQueueHandler}/\n"
     " * {@code SmartDepositHandler}/{@code ToolManager} 各有独立但几乎相同的 MOVING→OPENING→EXECUTING→CLOSING\n"
     " * 状态机实现，约 200 行重复代码；故收敛为基类，子类只需实现 {@link #execute(ServerLevel, BlockPos)}"),
    ("entity/goal/hold/imp/work/ConvertGoal.java",
     " * 取代原先的 {@code AbstractConvertGoal} + 4 个子类（Cook/Smelt/Composter/Mill）。本类不再自行判断需求，",
     " * 取代 {@code AbstractConvertGoal} + 4 个子类（Cook/Smelt/Composter/Mill）。本类不再自行判断需求，"),
    ("entity/goal/hold/imp/work/EnchantGoal.java",
     " * 取代原先设想的「附魔台随机附魔」，改为数据驱动的「使用附魔书给物品附魔」：",
     " * 不采用「附魔台随机附魔」，改为数据驱动的「使用附魔书给物品附魔」："),
    ("event/TalentPerkEvent.java",
     " * <p>原先的「能力(Perk)」机制已统一为属性系统：天赋节点通过属性修正写入这些属性，",
     " * <p>「能力(Perk)」机制统一走属性系统：天赋节点通过属性修正写入这些属性，"),
    ("ui/screen/progression/ProgressionTreeScreen.java",
     " * 这层外壳原先在两个类里各写了一遍（约 90 行），任何一处调整都要改两个文件且极易漂移。",
     " * 这层外壳若在两类里各写一遍（约 90 行），任何一处调整都要改两个文件且极易漂移。"),
    ("ui/widget/component/ValueBar.java",
     "        // 轨道等附加层原来整份丢失，克隆出的条只剩光秃秃的色带。",
     "        // 轨道等附加层须一并克隆，否则克隆出的条只剩光秃秃的色带。"),

    # ── E 组：旧版 / 旧实现 / 曾 ──────────────────────────────────────────
    ("data/codec/skill/phase/action/ModifierAction.java",
     " * 旧版直接对 {@code AttributeInstance} 写入 {@link AttributeModifier} + {@code BuffManager}\n"
     " * 已全面替换为通过 {@link StatStore} 写入 {@link ModifierStatEffect}，叠加结果由",
     " * 不对 {@code AttributeInstance} 直接写入 {@link AttributeModifier} + {@code BuffManager}，\n"
     " * 而通过 {@link StatStore} 写入 {@link ModifierStatEffect}，叠加结果由"),
    ("data/codec/stronghold/task/imp/work/ConvertTask.java",
     " * 不再像旧版那样作为独立工种「原地取料→转化→存回」，而是作为 DAG 链的一环：",
     " * 不作为独立工种「原地取料→转化→存回」，而是作为 DAG 链的一环："),
    ("data/codec/stronghold/task/imp/work/ConvertTask.java",
     "        // 产物需求消失则失效，避免无需求时继续转化导致过剩（与旧版需求门控一致）",
     "        // 产物需求消失则失效，避免无需求时继续转化导致过剩"),
    ("entity/goal/hold/helper/ContainerInteractionHandler.java",
     "    /** 兼容旧版 API：返回 {@link InteractionState} 枚举。 */",
     "    /** 兼容旧 API：返回 {@link InteractionState} 枚举。 */"),
    ("event/EventName.java",
     "     * 旧版裸键 {@code \"hit\"} 已统一为本键，造成/读取双通道同源。",
     "     * 裸键 {@code \"hit\"} 统一为本键，造成/读取双通道同源。"),
    ("ui/screen/gem/GemIconRender.java",
     " * 旧版 {@code GemIconRenderer} 通过 DataKey 每帧从 NBT 读纹理，这里只是一个\n"
     " * 不可变的固定纹理 —— 列表行的数据由 {@code ListView} 的 binder 在绑定时就确定了。",
     " * 这里只是一个不可变的固定纹理 —— 列表行的数据由 {@code ListView} 的 binder 在绑定时就确定，\n"
     " * 不通过 DataKey 每帧从 NBT 读纹理。"),
    ("ui/widget/render/imp/CornerTicksRenderer.java",
     " * 用来给面板加「取景框」质感。相比旧版增加了状态感知的颜色。",
     " * 用来给面板加「取景框」质感，并带状态感知的颜色。"),
    ("data/codec/support/math/context/Selector.java",
     " *       （旧实现比较器内反复 withEntity 重建 + 求值，O(n log n) 次）；</li>",
     " *       （比较器内反复 withEntity 重建 + 求值会达 O(n log n) 次）；</li>"),
    ("data/helper/DamageHelper.java",
     "     * <p>注：EXTRA_*_DAMAGE（额外附加伤害）为过时设计（被旧实现使用），随本次归一化一并移除，\n"
     "     * 正确范式为 DamageConvertStatEffect(gain)/词缀挂 DAMAGE 标签条目。",
     "     * <p>注：额外附加伤害不走 EXTRA_*_DAMAGE 独立属性，\n"
     "     * 正确范式为 DamageConvertStatEffect(gain)/词缀挂 DAMAGE 标签条目。"),
    ("ui/screen/hold/SiteScreen.java",
     " * <p>整个界面是一张<b>表单</b>。旧实现把每个控件的显示状态都塞进 NBT 标签，\n"
     " * 再在渲染帧里轮询「哪个按钮刚被按下」然后回写标签；这里控件直接持有回调，",
     " * <p>整个界面是一张<b>表单</b>。控件的显示状态不塞进 NBT 标签\n"
     " * 再在渲染帧里轮询回写，而是由控件直接持有回调，"),
    ("ui/screen/hold/TeleportScreen.java",
     " * <p>相比旧实现，这里没有任何 NBT 标签：目标的可用性直接写进节点的",
     " * <p>这里没有任何 NBT 标签：目标的可用性直接写进节点的"),
    ("ui/widget/graph/NodePainter.java",
     " * 不要自己再做一遍 world→screen（旧实现里两套坐标换算并存，缩放一变就对不齐）。",
     " * 不要自己再做一遍 world→screen（两套坐标换算并存时缩放一变就对不齐）。"),
    ("ui/widget/graph/WorldPainter.java",
     " * 旧实现把「珠宝辐射范围圈」直接写死在画布里，于是一个通用画布反过来依赖了具体业务类；",
     " * 把「珠宝辐射范围圈」写死在画布里会让通用画布反过来依赖具体业务类；"),
    ("ui/widget/render/skin/GuiSkin.java",
     " * <h3>与旧实现的区别</h3>",
     " * <h3>设计要点</h3>"),
    ("data/codec/support/stat/StatEvalContext.java",
     " * <p><b>不提供 {@code self(owner)} 式便捷构造</b>：曾存在的该工厂把 TRIGGER_TARGET 槽位硬写为",
     " * <p><b>不提供 {@code self(owner)} 式便捷构造</b>：该工厂会把 TRIGGER_TARGET 槽位硬写为"),
    ("data/gen/stat/wirings/WireDefense.java",
     "        // ===== 抗性族（RESISTANCES / MAX_RESISTANCES 真实属性；matcher 曾误接 RESISTANCE_PENETRATION）=====",
     "        // ===== 抗性族（RESISTANCES / MAX_RESISTANCES 真实属性；勿与 RESISTANCE_PENETRATION 混接）====="),
    ("data/gen/stat/wirings/WireDamage.java",
     "        // ===== 伤害/护甲/怒气 等（matcher CLEAN 中经人工核验正确的子集；排除误接 penetration/flask 的假阳性）=====",
     "        // ===== 伤害/护甲/怒气 等（matcher 结果中经人工核验正确的子集；penetration/flask 为假阳性）====="),

    # ── F 组：已删除 / 已移除 / 已废弃 / 已弃用（对死代码的指代）────────
    ("data/codec/entity/LivingMod.java",
     " * 旧 {@code predicate} / {@code convert} / {@code name} 字段已删除：\n"
     " * 条件判定并入 {@code conditions}，伤害转换由 {@code DamageConvertStatEffect} 条目表达，名称归入 {@link Mod}。",
     " * 不设 {@code predicate} / {@code convert} / {@code name} 字段：\n"
     " * 条件判定走 {@code conditions}，伤害转换由 {@code DamageConvertStatEffect} 条目表达，名称归入 {@link Mod}。"),
    ("data/codec/entity/LivingModifier.java",
     " * 旧 {@code predicate} / {@code convert} 字段已删除：\n"
     " * 条件判定并入 {@code conditions}，伤害转换由 {@code DamageConvertStatEffect} 条目表达（同走 Stat 通道）。",
     " * 不设 {@code predicate} / {@code convert} 字段：\n"
     " * 条件判定走 {@code conditions}，伤害转换由 {@code DamageConvertStatEffect} 条目表达（同走 Stat 通道）。"),
    ("data/helper/AilmentHelper.java",
     "    // 原「技能显式异常」的按状态分派方法（applyIgnite/Chill/Freeze/Shock/ElectrocuteAilment 的 skill 重载\n"
     "    // 与 chillAmplifier）在 AilmentAction 移除后无任何调用方，已删除——技能/Stat 触发施加统一走\n"
     "    // {@link #applyStatusOnEvent}（数据驱动路由）+ {@link #fireAilmentBySkill}（事件闭环）。",
     "    // 技能显式异常不按状态分派（不为 applyIgnite/Chill/Freeze/Shock/ElectrocuteAilment 各写一份 skill 重载\n"
     "    // 与 chillAmplifier），统一走 {@link #applyStatusOnEvent}（数据驱动路由）\n"
     "    // + {@link #fireAilmentBySkill}（事件闭环）。"),
    ("data/helper/DamageHelper.java",
     "        // getMixedDamage 以 ATTACK_DAMAGE 为基、元素比例由武器分布推导；逐元素 fold 通道（applyAddedDamages）已删除。",
     "        // getMixedDamage 以 ATTACK_DAMAGE 为基、元素比例由武器分布推导；不设逐元素 fold 通道。"),
    ("data/helper/DamageHelper.java",
     "     * 取代旧 PHYSICAL/CHAOS/FIRE/COLD/LIGHT_DAMAGE 5 独立属性（绝对性完全更改，不并存）。",
     "     * 与 PHYSICAL/CHAOS/FIRE/COLD/LIGHT_DAMAGE 5 独立属性互斥（不并存）。"),
    ("event/AttributeEvent.java",
     "                // 旧 RECENTLY_*/TAKEN_DAMAGE_TIME 衰减逻辑已删除：统一由 ExpressionContext 新 timer 接管",
     "                // 衰减统一由 ExpressionContext timer 接管"),
    ("event/AttributeEvent.java",
     "        // 纯 Stat 模型：词缀抽选不再做生成期谓词门控（旧的独立实体谓词通道已删除），",
     "        // 纯 Stat 模型：词缀抽选不做生成期谓词门控，"),
    ("item/charm/CharmEngine.java",
     "对齐 StatGen:873 INC_CHARM_DURATION 接线；POE2「X% increased Charm Effect Duration」）。原 CHARM_DURATION 死属性已删。",
     "对齐 StatGen:873 INC_CHARM_DURATION 接线；POE2「X% increased Charm Effect Duration」）。"),
    ("data/AttachmentRegister.java",
     "    // （已移除）技能 timed modifier buff 追踪器 TIMED_MOD_BUFF：临时条目到期统一走 StatStore\n"
     "    // 内建计时域（addModifier(key, entry, ticks) + StatStore.tick()，expiries 随附件序列化），\n"
     "\n"
     "    // 计时窗口（RECENTLY_*/TAKEN_DAMAGE_TIME/CAUSE_DAMAGE_TIME）已整体迁移至\n"
     "    // ExpressionContext 的共享 timer Map（kill / crit / taken_hit / cause_damage，统一事件名），\n"
     "    // 由 tickStore() 每 tick 递增、超 20s 存活上限移除，无旧 Attachment 兼容层（context_dsl_design.md §7.1 / M5）。",
     "    // 技能临时条目（timed modifier buff）到期统一走 StatStore 内建计时域\n"
     "    // （addModifier(key, entry, ticks) + StatStore.tick()，expiries 随附件序列化）；\n"
     "    // 计时窗口（kill / crit / taken_hit / cause_damage，统一事件名）走 ExpressionContext 的共享 timer Map，\n"
     "    // 由 tickStore() 每 tick 递增、超 20s 存活上限移除（context_dsl_design.md §7.1）。"),
    ("data/codec/support/stat/StatIds.java",
     " * <p>StatIds 为 Stat 路径的唯一缓存类，不再经「Ids -> StatPaths」第二层委托（StatPaths 已移除）。",
     " * <p>StatIds 为 Stat 路径的唯一缓存类，不经第二层委托。"),
    ("data/codec/support/status/StatusBehavior.java",
     "    // 已移除：原 allAttributes(STR/DEX/INT) 因本模组 STR/DEX/INT 仅作装备门槛（无战斗消费端）而无实际效果，\n"
     "    // ADRENALINE 现由 adrenaline() 直接写入真实战斗属性（DAMAGE/攻速/施法/移速/物理承伤）。",
     "    // 不提供 allAttributes(STR/DEX/INT)：本模组 STR/DEX/INT 仅作装备门槛（无战斗消费端）；\n"
     "    // ADRENALINE 由 adrenaline() 直接写入真实战斗属性（DAMAGE/攻速/施法/移速/物理承伤）。"),
    ("data/gen/StatDataIO.java",
     " * <p>本类不再持有索引构建逻辑（旧 {@code build*Index} 与 {@code Stat*Paths.byName} 反查已移除）：",
     " * <p>本类不持有索引构建逻辑："),
    ("event/DamagePipeline.java",
     "    // 命中资源获取（HitGain）机制已移除：怒气获取改由 ResourceStatEffect（Stat 条目 {\"type\":\"heal\",\"resource\":\"rage\"...}）",
     "    // 怒气获取走 ResourceStatEffect（Stat 条目 {\"type\":\"heal\",\"resource\":\"rage\"...}），不经 HitGain 机制"),
    ("event/TriggerContext.java",
     " * <p>字段仅保留效果真正读取的数值（其余身份/实体字段经全仓 grep 确认零读取，已移除）：",
     " * <p>字段仅保留效果真正读取的数值："),
    ("event/DamageEvent.java",
     "            // 空作用域保持：作用域激活本身用于支撑后续结算，这里不执行旧式属性读值（HIT_HEAL_* 已弃用）。",
     "            // 空作用域保持：作用域激活本身用于支撑后续结算，这里不执行属性读值。"),
    ("event/DamageEvent.java",
     "                    // （身份=ResourceType，数值=条目 amount）承载，属性 KILLED_GRANT_* 已弃用。",
     "                    // （身份=ResourceType，数值=条目 amount）承载。"),
    ("data/helper/DamageHelper.java",
     "     * 偷取计算入口：<b>资源键已归一</b>——偷取量不再按资源拆分属性（旧 HEALTH_LEECH 等 7 个独立属性\n"
     "     * 已废弃），统一由 {@code LEECH} 单属性 + {资源标签} 作用域承载，由 Stat 条目按标签聚合各资源偷取。",
     "     * 偷取计算入口：<b>资源键已归一</b>——偷取量不按资源拆分属性，\n"
     "     * 统一由 {@code LEECH} 单属性 + {资源标签} 作用域承载，由 Stat 条目按标签聚合各资源偷取。"),
    ("event/DamagePipeline.java",
     "         *  在抗性/护甲计算之前就地改写 {@code damages[]}。取代已废弃的 ARMOR_APPLIES_TO_* 系列属性。 */",
     "         *  在抗性/护甲计算之前就地改写 {@code damages[]}。 */"),
    ("data/gen/stat/wirings/WireDefense.java",
     "（复用 B57 HealthHelper 读点，R3 安全）",
     "（复用 HealthHelper 读点，R3 安全）"),
]


def adapt(text: str, eol: str) -> str:
    """把字面量里的 \\n 折算成目标文件的实际换行符（本工程源码为 CRLF）。"""
    return text.replace("\n", eol) if eol != "\n" else text


def main() -> int:
    dry = "--dry" in sys.argv
    cache: dict[str, str] = {}
    eols: dict[str, str] = {}

    def load(rel: str) -> str:
        if rel not in cache:
            path = ROOT / rel.replace("/", "\\")
            if not path.exists():
                print(f"!! NOFILE {rel}")
                raise SystemExit(1)
            with open(path, "r", encoding="utf-8", newline="") as f:
                cache[rel] = f.read()
            eols[rel] = "\r\n" if "\r\n" in cache[rel] else "\n"
        return cache[rel]

    # 0) 整行删除
    deleted = 0
    for rel, needle in LINE_DELETES:
        load(rel)
        eol = eols[rel]
        lines = cache[rel].splitlines(keepends=True)
        keep, hit = [], 0
        for line in lines:
            if needle in line:
                hit += 1
                continue
            keep.append(line)
        if hit != 1:
            print(f"!! MISS delete (count={hit}) {rel} :: {needle[:50]}")
            return 1
        cache[rel] = "".join(keep)
        deleted += hit
        print(f"   del  {rel}  {needle.strip()[:56]}")

    # 1) 定点改写
    applied = 0
    for rel, old, new in REPLACEMENTS:
        load(rel)
        eol = eols[rel]
        old, new = adapt(old, eol), adapt(new, eol)
        n = cache[rel].count(old)
        if n != 1:
            print(f"!! MISS (count={n}) {rel}\n   old={old[:90]!r}")
            return 1
        cache[rel] = cache[rel].replace(old, new, 1)
        applied += 1
        print(f"   ok   {rel}  {old.strip()[:56]}")

    if dry:
        print(f"\n[dry] would delete {deleted} lines and apply {applied} replacements")
        return 0
    for rel, text in cache.items():
        path = ROOT / rel.replace("/", "\\")
        with open(path, "w", encoding="utf-8", newline="") as f:
            f.write(text)
    print(f"\ndeleted {deleted} lines, applied {applied} replacements across {len(cache)} files")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
