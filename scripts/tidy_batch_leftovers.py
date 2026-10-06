#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Batch 标记剥离后的收尾精修（配合 strip_batch_markers.py 使用）。

机械剥离会留下两类残迹，本脚本定点修复：
  A. 语义空洞 —— 「由 修复」「于 落地」「（，FLASK…」「（，）」等被吃空的表述；
  B. 纯变更史叙述 —— 「此前…」「本批…」「原接线取 X 为估值」等应移出代码、沉淀进记忆的内容，
     改写成对当前实现的**约束说明**（仍解释「为什么不能那样写」，但不复述变更过程）。

每条替换均断言命中且仅命中一次；未命中即报错退出，防止静默漏改。
"""
import pathlib
import sys

# 下列相对路径均以主包根为基准
ROOT = pathlib.Path(__file__).resolve().parent.parent / "src" / "main" / "java" / "com" / "cimdy" / "coe"

# (相对路径, 原文, 新文)
REPLACEMENTS = [
    # ── StatConditionIds：吃掉「由 修复」空洞 ──
    ("data/gen/stat/StatConditionIds.java",
     "    // caused_crit 的计数通道由 修复（DamagePipeline 由 recordTimer 改 Event.record）。\n",
     "    // caused_crit 的计数通道走 DamagePipeline → Event.record。\n"),

    # ── StatEffectIds：括注里的变更动词 ──
    ("data/gen/stat/StatEffectIds.java",
     "     * 事件发射点见 {@code RemnantState.onConsume}（补齐）。\n",
     "     * 事件发射点见 {@code RemnantState.onConsume}。\n"),
    ("data/gen/stat/StatEffectIds.java",
     "     * 事件发射点为 {@code SkillEvent.tryGrantWarcry}（补齐，此前该事件名只有声明无调用点）。\n",
     "     * 事件发射点为 {@code SkillEvent.tryGrantWarcry}。\n"),

    # ── WireDamage：括注内残留逗号 ──
    ("data/gen/stat/wirings/WireDamage.java",
     "        // 电击持续时间（bare 别名，）：与 INC_SHOCK_DURATION_ON_YOU 同节点",
     "        // 电击持续时间（bare 别名）：与 INC_SHOCK_DURATION_ON_YOU 同节点"),

    # ── WirePerScaling ──
    ("data/gen/stat/wirings/WirePerScaling.java",
     "        // 已于 落地 distinct 原语，见下方「去重敌计数缩放」小节。\n",
     "        // 见下方「去重敌计数缩放」小节。\n"),
    ("data/gen/stat/wirings/WirePerScaling.java",
     "        // 【通道修复】此前 fire 通道（GameEvent.fireCaused/fireTaken/firePair）只写\n"
     "        // ExpressionContext timer + StatEngine.fire，**不写 Event.State 计数队列**，致本族读值源恒为 0。\n"
     "        // 已在 GameEvent.fire 补 Event.record(owner, eventName, tags)（三参双写 triggers + taggedTriggers），\n"
     "        // 并在 Event.record(e, name, Set) 三参重载补无标签队列写入——见两处 注释。以下两条据此复活。\n",
     "        // fire 通道（GameEvent.fireCaused/fireTaken/firePair）除 timer + StatEngine.fire 外还须写\n"
     "        // Event.State 计数队列，否则本族读值源恒为 0：GameEvent.fire 已写 Event.record(owner, eventName, tags)\n"
     "        // （三参双写 triggers + taggedTriggers），Event.record(e, name, Set) 三参重载补无标签队列写入。\n"),
    ("data/gen/stat/wirings/WirePerScaling.java",
     "        // 读值源 = EventHistoryValue mode=distinct（新增）：窗口内匹配标签<b>去重后</b>的数量。\n",
     "        // 读值源 = EventHistoryValue mode=distinct：窗口内匹配标签<b>去重后</b>的数量。\n"),
    ("data/gen/stat/wirings/WirePerScaling.java",
     "        // ===================== 施放技能族（CAST_SKILL 事件，）=====================\n",
     "        // ===================== 施放技能族（CAST_SKILL 事件）=====================\n"),
    ("data/gen/stat/wirings/WirePerScaling.java",
     "        // 解除卡点：EventName.USE_WARCRY 此前「只有声明、无调用点」，本批已在\n"
     "        // SkillEvent.tryGrantWarcry（全工程唯一的战吼类技能施放判定点）补上\n"
     "        // Event.record(player, USE_WARCRY)，故 count:use_warcry 现在可读——本条按本文件\n",
     "        // USE_WARCRY 由 SkillEvent.tryGrantWarcry（全工程唯一的战吼类技能施放判定点）\n"
     "        // 经 Event.record(player, USE_WARCRY) 写入，故 count:use_warcry 可读——本条按本文件\n"),
    ("data/gen/stat/wirings/WirePerScaling.java",
     "        // 缩放基数同为 presence_count:ally.totem（PresenceCountValue，）。\n",
     "        // 缩放基数同为 presence_count:ally.totem（PresenceCountValue）。\n"),
    ("data/gen/stat/wirings/WirePerScaling.java",
     "        // ===================== 近敌计数缩放（nearby_enemy_count:<radius>，）=====================\n",
     "        // ===================== 近敌计数缩放（nearby_enemy_count:<radius>）=====================\n"),
    ("data/gen/stat/wirings/WirePerScaling.java",
     "        // 故本节正是开头注释所指「留待计数原语落地」的那类去重实体计数——原语已于 落地。\n",
     "        // 故本节正是开头注释所指「留待计数原语落地」的那类去重实体计数。\n"),

    # ── WireRecent ──
    ("data/gen/stat/wirings/WireRecent.java",
     " * <h3>：参数化时间窗口簇（本批落地 6 条）</h3>\n",
     " * <h3>参数化时间窗口簇</h3>\n"),
    ("data/gen/stat/wirings/WireRecent.java",
     " *       <b>注（更正）</b>——{@code USE_WARCRY} 已<b>有</b>发射点\n"
     " *       （{@code SkillEvent.tryGrantWarcry} 记录无标签计数队列，本批补齐；此前该事件名只有声明、无调用点）。\n",
     " *       <b>注</b>——{@code USE_WARCRY} 已<b>有</b>发射点\n"
     " *       （{@code SkillEvent.tryGrantWarcry} 记录无标签计数队列）。\n"),
    ("data/gen/stat/wirings/WireRecent.java",
     "：<b>已于 接入</b>\n",
     "：<b>已接入</b>\n"),
    ("data/gen/stat/wirings/WireRecent.java",
     "        // caused_crit 的计数通道由 修复（DamagePipeline：recordTimer → Event.record），\n"
     "        // 此前该键只写计时仓库，recently/count 查询恒 false。\n",
     "        // caused_crit 的计数通道走 DamagePipeline → Event.record：只写计时仓库会让\n"
     "        // recently/count 查询恒 false。\n"),

    # ── WireResource ──
    ("data/gen/stat/wirings/WireResource.java",
     "        // 1) 药剂回复的 more/less 分支 —— 复用 INC_HEALTH_RECOVERY_FROM_FLASKS 同源（，FLASK_RECOVERY_RATE\n",
     "        // 1) 药剂回复的 more/less 分支 —— 复用 INC_HEALTH_RECOVERY_FROM_FLASKS 同源（FLASK_RECOVERY_RATE\n"),
    ("data/gen/stat/wirings/WireResource.java",
     "        // low_mana 经 COND_LOW_MANA_LIVING_STATE 门控。flask-effect 变体由 经新建 COND_FLASK_ACTIVE_MANA\n",
     "        // low_mana 经 COND_LOW_MANA_LIVING_STATE 门控。flask-effect 变体经 COND_FLASK_ACTIVE_MANA\n"),

    # ── WireWhile ──
    ("data/gen/stat/wirings/WireWhile.java",
     " *       由 的 {@code PresenceCountValue} 计数节点与常量 0 比较得到；</li>\n",
     " *       由 {@code PresenceCountValue} 计数节点与常量 0 比较得到；</li>\n"),

    # ── RemnantState ──
    ("data/helper/RemnantState.java",
     "     * <p>：本方法是灌入层数的<b>唯一</b>消耗入口，故在此发射 {@code CONSUME_INFUSION}\n",
     "     * <p>本方法是灌入层数的<b>唯一</b>消耗入口，故在此发射 {@code CONSUME_INFUSION}\n"),
    ("data/helper/RemnantState.java",
     "     * <p>补齐事件发射：本方法此前已是完整的拾取逻辑，唯独未发 {@code COLLECT_REMNANT}，\n"
     "     * 令 2 条<b>早已接线</b>的条目（拾取残迹回血 / 回魔）恒不触发。补一行即复活。\n",
     "     * <p>本方法同时发射 {@code COLLECT_REMNANT}，驱动「拾取残迹回血 / 回魔」一族条目。\n"),

    # ── Event：双写约束（保留「为什么」，去掉变更过程）──
    ("event/Event.java",
     "     * <p><b>双写（修）</b>：本重载既写 {@code taggedTriggers}（分维 {@code recently:<k>:<tag>}），\n"
     "     * 也写 {@code triggers} 无标签队列（{@code count:<k>} / {@code recently:<k>}）。\n"
     "     * 此前只写带标签队列，导致「带标签记录、无标签读取」的接线恒返 0 且无报错\n"
     "     * （典型：<code>Event.record(target, FORM_CHANGED, form.tags())</code> 配\n"
     "     * {@code recently:form_changed}）——与 {@link GameEvent#recordTaggedHit} 既有的双写意图对齐。\n",
     "     * <p><b>双写</b>：本重载既写 {@code taggedTriggers}（分维 {@code recently:<k>:<tag>}），\n"
     "     * 也写 {@code triggers} 无标签队列（{@code count:<k>} / {@code recently:<k>}）。\n"
     "     * 只写带标签队列会让「带标签记录、无标签读取」的接线恒返 0 且无报错\n"
     "     * （典型：<code>Event.record(target, FORM_CHANGED, form.tags())</code> 配\n"
     "     * {@code recently:form_changed}）——与 {@link GameEvent#recordTaggedHit} 的双写对齐。\n"),

    # ── EventName ──
    ("event/EventName.java",
     "     * 发射点：{@code RemnantState.collect}（补齐）——残迹机制早已完全实现（生产/衰减/拾取/强化），\n"
     "     * 此前唯缺本行发射，导致 2 条既有接线（拾取残迹回血/回魔）<b>静默失效</b>。附带 {@code {remnant}} 标签。\n",
     "     * 发射点：{@code RemnantState.collect}。附带 {@code {remnant}} 标签。\n"),
    ("event/EventName.java",
     "     * <p>发射点：{@code RemnantState.onConsume}（新增）——该方法是灌入层数的<b>唯一</b>消耗入口\n",
     "     * <p>发射点：{@code RemnantState.onConsume}——该方法是灌入层数的<b>唯一</b>消耗入口\n"),

    # ── GameEvent ──
    ("event/GameEvent.java",
     "        // 计数队列（修）：此前 fire 通道只写 timer + StatEngine.fire，\n"
     "        // 导致所有走 fireCaused/fireTaken 的事件键（caused_ailment / caused_block / caused_parry /\n"
     "        // taken_* 等一整族）在 count:<k> / recently:<k> 查询下恒返 0 且无报错。\n",
     "        // 计数队列：fire 通道必须同时写 Event.State 计数队列，否则所有走 fireCaused/fireTaken\n"
     "        // 的事件键（caused_ailment / caused_block / caused_parry / taken_* 等一整族）\n"
     "        // 在 count:<k> / recently:<k> 查询下恒返 0 且无报错。\n"),

    # ── SpawnAction：召唤物标记写入点（保留「唯一写入点」这一 R3 关键信息）──
    ("data/codec/skill/phase/action/SpawnAction.java",
     "        // 召唤物标记（补齐的唯一写入点）：此前本键全工程无人写入，\n"
     "        // 令 EntityFlagValue.Flag.SUMMONED 与 presence_count:ally.summoned 恒为假/0（死读值）。\n"
     "        // 本方法是召唤物生成的单一权威路径，故在此写入即可覆盖全部召唤来源。\n",
     "        // 召唤物标记：本方法是召唤物生成的单一权威路径，故在此写入即可覆盖全部召唤来源。\n"
     "        // 缺此写入会让 EntityFlagValue.Flag.SUMMONED 与 presence_count:ally.summoned 恒为假/0（死读值）。\n"),

    # ── EntityFlagValue ──
    ("data/codec/support/math/context/nodes/EntityFlagValue.java",
     "     * 召唤物标记键（修正）：此前<b>全工程只有读取点、无写入点</b>，\n"
     "     * 令 {@code Flag.SUMMONED} 与 {@code presence_count:ally.summoned} 恒为假/0（死读值）。\n"
     "     * 唯一写入点为 {@code SpawnAction.spawnMinion}（召唤物生成的单一权威路径）。\n"
     "     * 提升为 public 以便写入侧与 {@code PresenceCountValue} 共用同一常量（原先两处各持私有副本）。\n",
     "     * 召唤物标记键。唯一写入点为 {@code SpawnAction.spawnMinion}（召唤物生成的单一权威路径），\n"
     "     * 缺此写入会让 {@code Flag.SUMMONED} 与 {@code presence_count:ally.summoned} 恒为假/0（死读值）。\n"
     "     * public 以便写入侧与 {@code PresenceCountValue} 共用同一常量。\n"),

    # ── LivingStateValue：把「历史修复」改写成对实现的约束 ──
    ("data/codec/support/math/context/nodes/LivingStateValue.java",
     "     * 历史修复：旧实现只遍历 {@code level.players()} 且阈值为 1 个，\n"
     "     * 在单人存档下恒为 false —— 全部 {@code WHILE_SURROUNDED} 词条虽已接线但永不触发（静默死线）。\n",
     "     * 敌对扫描须覆盖<b>全部</b>实体而非仅 {@code level.players()}：只扫玩家时单人存档下恒无敌人，\n"
     "     * 全部 {@code WHILE_SURROUNDED} 词条会虽已接线却永不触发（静默死线）。\n"),

    # ── ContextValueTypes ──
    ("data/codec/support/math/context/ContextValueTypes.java",
     "        // 实体类别判定（替代早期的硬编码实体枚举）：entity_type:<tag id>，\n",
     "        // 实体类别判定：entity_type:<tag id>,\n"),
    ("data/codec/support/math/context/ContextValueTypes.java",
     "        // 补一个聚焦简写 presence_rarity:count = 范围内稀有/传奇敌人计数（用于「for each rare/unique monster +X」线性缩放）。\n",
     "        // 聚焦简写 presence_rarity:count = 范围内稀有/传奇敌人计数（用于「for each rare/unique monster +X」线性缩放）。\n"),
    ("data/codec/support/math/context/ContextValueTypes.java",
     "        // presence 实体进/出范围读值（2026-08-05 落地）：recently_entered:<window> / recently_left:<window>\n",
     "        // presence 实体进/出范围读值：recently_entered:<window> / recently_left:<window>\n"),

    # ── WireMisc ──
    ("data/gen/stat/wirings/WireMisc.java",
     "        // CHARM_IMMUNE_GROUNDING（护符 Grounding = 免疫感电地面）→ 免疫 ON_SHOCKED_GROUND 标记。\n"
     "        // ⚠️ 修正：此前误接 EFF_IMMUNE_SHOCK（免疫**感电状态**）—— 语义错误：\n"
     "        // 感电地面是独立状态（ON_SHOCKED_GROUND），免疫感电并不能阻止被打上地面标记，反之亦然。\n",
     "        // 护符 Grounding = 免疫感电地面 → 免疫 ON_SHOCKED_GROUND 标记。\n"
     "        // 不可复用 EFF_IMMUNE_SHOCK：感电地面是<b>独立于感电</b>的状态，\n"
     "        // 免疫感电挡不住地面标记，反之亦然（近似即错义）。\n"),
    ("data/gen/stat/wirings/WireMisc.java",
     "        // 复活：残迹机制（RemnantState 生产/衰减/拾取）早已完整，唯独 collect() 未发事件，\n"
     "        // 令本条与下方回魔条<b>恒不触发</b>（潜在静默失效，与战吼同型）。已在 RemnantState.collect 补发射点。\n",
     "        // 依赖 {@code RemnantState.collect} 发射 COLLECT_REMNANT；缺该发射点时本条与下方回魔条恒不触发。\n"),
    ("data/gen/stat/wirings/WireMisc.java",
     "        // 校准：PoE2 官方关键词 \"Close Range\" = 「距角色 2 米内」（原接线取 3.0 为估值），\n"
     "        // 与本簇 within/further 的字面阈值统一改按官方口径 2.0；同一口径下 nearby_enemy_count:2 的\n"
     "        // 「每个近距离敌人」缩放族才与门控自洽。\n",
     "        // PoE2 官方关键词 \"Close Range\" = 「距角色 2 米内」。\n"
     "        // 本簇 within/further 字面阈值须与此统一取 2.0——否则 nearby_enemy_count:2 的\n"
     "        // 「每个近距离敌人」缩放族与门控口径不自洽。\n"),
    ("data/gen/stat/wirings/WireMisc.java",
     "        // 眩晕蓄积（通用 STUN 标签，base=1）：within 2m —— 按语料字面校准\n"
     "        //（PoE2 源 \"40% increased Stun Buildup against enemies within 2 metres\"，原 3.0 为估值）。\n",
     "        // 眩晕蓄积（通用 STUN 标签，base=1）：within 2m\n"
     "        //（PoE2 源 \"40% increased Stun Buildup against enemies within 2 metres\"）。\n"),
]


def adapt(text: str, eol: str) -> str:
    """把字面量里的 \\n 折算成目标文件的实际换行符（本工程源码为 CRLF）。"""
    return text.replace("\n", eol) if eol != "\n" else text


def main() -> int:
    cache: dict[str, str] = {}
    eols: dict[str, str] = {}
    for rel, old, new in REPLACEMENTS:
        path = ROOT / rel.replace("/", "\\")
        if rel not in cache:
            with open(path, "r", encoding="utf-8", newline="") as f:
                cache[rel] = f.read()
            eols[rel] = "\r\n" if "\r\n" in cache[rel] else "\n"
        eol = eols[rel]
        old, new = adapt(old, eol), adapt(new, eol)
        src = cache[rel]
        n = src.count(old)
        if n != 1:
            print(f"!! MISS (count={n}) {rel}\n   old={old[:70]!r}")
            return 1
        cache[rel] = src.replace(old, new, 1)

    for rel, text in cache.items():
        path = ROOT / rel.replace("/", "\\")
        with open(path, "r", encoding="utf-8", newline="") as f:
            if f.read() == text:
                continue
        with open(path, "w", encoding="utf-8", newline="") as f:
            f.write(text)
        print(f"  updated  {rel}")
    print(f"\napplied {len(REPLACEMENTS)} replacements across {len(cache)} files")
    return 0


if __name__ == "__main__":
    sys.exit(main())
