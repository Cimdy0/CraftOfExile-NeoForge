#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
第三批清理：日期戳与「本批 / 此前」变更史叙述。

继 strip_batch_markers.py（Batch NN 标记）与 tidy_batch_leftovers.py（剥离残迹）之后的收尾。
依据同一条用户指令：代码里只留**必要语义**，变更过程与踩坑沉淀进记忆。

保留原则：
  · 保留 —— 对当前实现的**约束说明**（"必须写计数队列，否则恒 0"、"无 Companion 概念，按 R3 排除"）；
  · 删除 —— 变更过程的叙述（"此前…"、"本批…"、"已于 X 落地"、日期、Wave 号、对已删类的指代）。

用法：python scripts/tidy_change_history.py       （脚本对每条替换断言命中数，未命中即报错退出）
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent / "src" / "main" / "java"
COE = ROOT / "com" / "cimdy" / "coe"

# ── 整行删除：整句都是变更史的注释行，按子串定位后删整行（先于括号删除执行，
#    否则括号被剥掉后只剩一个孤零零的「// 。」，反而更难定位）──
LINE_DELETES = [
    ("data/AttachmentRegister.java", "旧 TimedModBuffTracker 外部追踪通道于 2026-08-23 删除"),
    ("data/codec/skill/phase/action/ModifierAction.java", "（2026-08-23 起统一临时条目通道"),
    ("data/codec/skill/phase/action/StatAction.java", "（2026-08-23 起替代旧 TimedModBuffTracker"),
    ("event/AttributeEvent.java", "旧 ENERGY 属性已迁移 ES"),
    ("event/AttributeEvent.java", "（旧 ENERGY「能量护盾」回复段已随 ENERGY 资源移除"),
    ("data/gen/talent/node/notable/NotableNodes07.java", "原 AURA 属性 + 承伤管道硬编码读已删除"),
]

# ── 全局：整括号删除（括号内除日期/Wave 外无实义）──
GLOBAL_DROP_PAREN = [
    "（2026-08-05）", "（2026-08-05 落地，L1）", "（2026-08-05 落地，L1 闭合）",
    "（2026-08-05 落地，替代注释中的占位设计）", "（2026-08-05 优化）", "（2026-08-05 已修）",
    "（2026-08-15）", "（2026-08-17 升阶子图整块移植批次）",
    "（2026-08-17 Mastery 选择机制批次）",
    "（2026-08-18 Wave-23）", "（2026-08-18 Wave-24）",
    "（2026-08-18 Wave-24/25）", "（2026-08-18 Wave-25 全面开放）",
    "（2026-08-18 Wave-30）",
    "（2026-08-18 Wave-30/31）", "（2026-08-18 Wave-31）",
    "（2026-08-18 Wave-32：旧 ENERGY 已迁移）", "（2026-08-18 Wave-32：旧 ENERGY 迁移 ES）",
    "（2026-08-22）", "（2026-08-22 归一）", "（2026-08-23）",
    "（2026-08-23 起替代旧 TimedModBuffTracker 外部追踪）",
    "（2026-08-23 起统一临时条目通道，旧 TimedModBuffTracker 外部追踪已移除）",
    "（2026-08-26）", "（2026-08-26：Effect 数据驱动路径化）", "（2026-08-28）",
]

# ── 全局：只剥日期/Wave，保留括号内实义 ──
GLOBAL_STRIP_IN_PAREN = [
    ("（2026-08-18 Wave-24，见 {@link com.cimdy.coe.data.helper.impl.PresenceAura}）",
     "（见 {@link com.cimdy.coe.data.helper.impl.PresenceAura}）"),
    ("（2026-08-18 Wave-27，与 {@link #damageConvertIndexed()} 同构）",
     "（与 {@link #damageConvertIndexed()} 同构）"),
    ("，2026-08-18 Wave-29 与 ENERGY「能量」区分）", "，与 ENERGY「能量」区分）"),
    ("（2026-08-18 Wave-29 从 ENERGY「能量」中区分独立）", "（从 ENERGY「能量」中区分独立）"),
    ("（2026-08-18 Wave-32，参考荣耀：按 metaSkillId 分槽；服务端权威、不同步）",
     "（参考荣耀：按 metaSkillId 分槽；服务端权威、不同步）"),
    ("（2026-08-18 Wave-32 参考荣耀：按技能 id 分槽，缺省 fallback \"default\"）",
     "（参考荣耀：按技能 id 分槽，缺省 fallback \"default\"）"),
    ("（2026-08-18 Wave-32 参考荣耀；无技能上下文 → \"default\" 槽）",
     "（参考荣耀；无技能上下文 → \"default\" 槽）"),
    ("（2026-08-18 Wave-32 参考荣耀：超过宽限期后逐 tick 回落）",
     "（参考荣耀：超过宽限期后逐 tick 回落）"),
    ("（Meta Skill Energy，2026-08-18 Wave-32）", "（Meta Skill Energy）"),
    ("（按需订阅的静态侧，2026-08-05）", "（按需订阅的静态侧）"),
    ("，Acolyte of Chayula 升阶，2026-08-18 Wave-28 新增）", "，Acolyte of Chayula 升阶）"),
    ("（41076 Embrace the Darkness，2026-08-18 Wave-28）", "（41076 Embrace the Darkness）"),
    ("（2026-08-17，严格还原；词缀 = Stat 实现）", "（严格还原；词缀 = Stat 实现）"),
    ("（tick，2026-08-18 Wave-35 语义修正）", "（tick）"),
    ("（2026-08-18 Wave-29 区分：ES 独立于", "（ES 独立于"),
]

# ── 定点：整行/整句删除或改写（多数为行首日期前缀、或对已删类的指代）──
LOCAL = [
    # AttachmentRegister：旧 ENERGY 附件已移除 —— 指涉已删字段，整段删
    ("data/AttachmentRegister.java",
     "     *  （EnergyShieldHelper.hurt）；旧 ENERGY「能量护盾」附件已移除（2026-08-18 Wave-32，元技能能量\n"
     "     *  走 META_ENERGY_TRACKER）。 */\n",
     "     *  （EnergyShieldHelper.hurt）。 */\n"),
    # 行首日期前缀：只留语义
    ("data/codec/skill/phase/action/meta/MetaSkillAction.java",
     "        // 2026-08-18 Wave-32：能量改为 MetaEnergyState（参考荣耀，持久资源：按技能 id 分槽 +\n",
     "        // 能量存于 MetaEnergyState（参考荣耀，持久资源：按技能 id 分槽 +\n"),
    ("data/codec/skill/phase/action/SpawnAction.java",
     "        // 2026-08-18 Wave-24：图腾 Living 化后会被 AttributeEvent 首次 tick 初始化——\n",
     "        // 图腾是 LivingEntity，会被 AttributeEvent 首次 tick 初始化——\n"),
    ("data/helper/impl/RecoupValue.java",
     "        // 2026-08-18 Wave-32：recoup energy 语义 = 能量护盾补给（旧 ENERGY「能量护盾」迁移 ES）\n",
     "        // recoup energy 语义 = 能量护盾补给\n"),
    ("data/helper/RemnantState.java",
     "        // 2026-08-18 机制推进：拾取范围乘玩家「更远收集」倍率\n",
     "        // 拾取范围乘玩家「更远收集」倍率\n"),
    ("event/AttributeEvent.java",
     "        // 2026-08-18 Wave-32：旧 ENERGY「能量护盾」迁移 ES——玩家初始化补满 ES\n",
     "        // 玩家初始化补满 ES\n"),
    ("event/DamagePipeline.java",
     "        // 2026-08-18 Wave-32：能量护盾承伤顺序统一在此阶段——\n",
     "        // 能量护盾承伤顺序统一在此阶段——\n"),
    ("event/DamagePipeline.java",
     "        // ── 承伤变动（总承伤 + 类型承伤）2026-08-18 Wave-25 统一通道 ──\n",
     "        // ── 承伤变动（总承伤 + 类型承伤）统一通道 ──\n"),
    ("event/DamagePipeline.java",
     "        // ── 图腾承伤光环（5580 Watchtowers）已移出承伤管道（2026-08-22 归一）──\n",
     "        // ── 图腾承伤光环（5580 Watchtowers）不在承伤管道内 ──\n"),
    ("event/data/GenEvent.java",
     "        // 2026-08-26 重构：effect / targets / conditions 三段各自独立 Provider 生成（内容寻址落盘），\n",
     "        // effect / targets / conditions 三段各自独立 Provider 生成（内容寻址落盘），\n"),
    ("data/codec/support/stat/StatEngine.java",
     "        // 2026-08-18 严格化（Minion/Totem Recoup 通道）：随从/图腾等召唤体受击时补充主人 store 中\n",
     "        // 随从/图腾等召唤体受击时补充主人 store 中\n"),
    ("event/GuiEvent.java",
     "        //最丑的能量护盾（2026-08-18 Wave-32：旧 ENERGY 迁移 ES）\n",
     "        // 能量护盾\n"),
    # 死代码说明：保留"为何不在此注册"，删日期与过程
    ("data/codec/support/stat/effect/StatEffectTypes.java",
     "        // 注：事件驱动横向拓展的 ailment_on_event / gain_charge 效果类（ApplyAilmentOnEventStatEffect /\n"
     "        //   GainChargeOnEventStatEffect）为死代码已删除（2026-08-15）：异常施加收敛 AilmentHelper /\n"
     "        //   StatusEngine.apply 中央入口，充能授予走 ChargedState，无需独立触发效果类型。\n",
     "        // 注：不在此注册 ailment_on_event / gain_charge 类效果——异常施加已收敛到 AilmentHelper /\n"
     "        //   StatusEngine.apply 中央入口，充能授予走 ChargedState，无需独立触发效果类型。\n"),
    # 用户裁定：保留裁定结论，删 Wave 指代
    ("data/codec/support/stat/effect/PresenceAuraStatEffect.java",
     " *       无任何元素标签 = 火/冰/电<b>全元素暴露</b>。「整句独立 Stat = 全元素暴露；可增加 tag 使一种或多种暴露生效」——\n"
     " *       2026-08-18 Wave-25 用户裁定，取代 Wave-24 的随机模式）。</li>\n",
     " *       无任何元素标签 = 火/冰/电<b>全元素暴露</b>。「整句独立 Stat = 全元素暴露；可增加 tag 使一种或多种暴露生效」）。</li>\n"),
    ("data/helper/impl/PresenceAura.java",
     "       火/冰/电全元素暴露（2026-08-18 Wave-25 用户裁定，取代随机模式）。</li>\n",
     "       火/冰/电全元素暴露。</li>\n"),
    ("data/helper/impl/PresenceAura.java",
     " * <p>注：Wave-24 的「图腾承伤光环」已按用户裁定重写为「承伤管道读攻击方 stat × 承伤方附近图腾数」\n"
     " * （见 {@code DamagePipeline.applyStatusDamageTakenModifiers}），不再走本光环通道。\n",
     " * <p>注：「图腾承伤光环」不走本通道，而是承伤管道读攻击方 stat × 承伤方附近图腾数\n"
     " * （见 {@code DamagePipeline.applyStatusDamageTakenModifiers}）。\n"),
    # SkillTotem / TargetSelector：Living 化的叙述改为现状陈述
    ("entity/custom/skill/SkillTotem.java",
     " * <p><b>Living 化（2026-08-18 Wave-23）</b>：图腾基类由 {@link Entity} 重构为 {@link LivingEntity}——\n",
     " * <p><b>Living 化</b>：图腾基类是 {@link LivingEntity} 而非 {@link Entity}——\n"),
    ("data/codec/support/stat/effect/TargetSelector.java",
     " * <p>2026-08-18 Wave-23：图腾实体基类重构为 {@link LivingEntity} 后本源正式启用——\n",
     " * <p>图腾基类为 {@link LivingEntity}，故本源适用——\n"),
    # 槽名：把"旧 X 导致恒 0"改成约束陈述
    ("data/codec/support/math/context/Conversions.java",
     "        // 槽名须用 EquipmentSlot.getName()（head），旧 \"helmet\" 非合法槽名导致该转化恒 0（2026-08-05 已修）\n",
     "        // 槽名必须用 EquipmentSlot.getName()（如 \"head\"）；\"helmet\" 不是合法槽名，会使该转化恒 0\n"),
    # ── 「本批」──
    ("data/gen/stat/StatConditionIds.java",
     "    // Event.record(e,name,int) 单参通道写的是无标签队列，故本批以无标签 count 读数为准。\n",
     "    // Event.record(e,name,int) 单参通道写的是无标签队列，故按无标签 count 读数。\n"),
    ("data/gen/stat/StatConditionIds.java",
     "    // 故本批 COND 一旦被条目引用，对应事件即进入记录（不存在「无发射点恒 0」的静默失败）。\n",
     "    // COND 一旦被条目引用，对应事件即进入记录（不存在「无发射点恒 0」的静默失败）。\n"),
    ("data/gen/stat/wirings/WireCrit.java",
     " * <p><b>为何不用 HitStateValue</b>（本批核过的关键判据，勿回退）：\n",
     " * <p><b>为何不用 HitStateValue</b>（关键判据，勿回退）：\n"),
    ("data/gen/stat/wirings/WireCrit.java",
     " * 本批只是给它加上暴击门控，非 {@code onTarget}（「你施加的」语义由 EFF 的 {@code not(TAKEN)} 标签承载，\n",
     " * 此处只加暴击门控，不走 {@code onTarget}（「你施加的」语义由 EFF 的 {@code not(TAKEN)} 标签承载，\n"),
    ("data/gen/stat/wirings/WireCrit.java",
     "        // 四条均为既有「you inflict」施加侧 EFF + 本批新增 COND_CRIT_HIT。\n",
     "        // 四条均为既有「you inflict」施加侧 EFF + COND_CRIT_HIT 门控。\n"),
    ("data/gen/stat/wirings/WireCrit.java",
     "        // MORE_ 前缀按 op 族走 ADD_MULTIPLIED_TOTAL（本批新增该 EFF：\n",
     "        // MORE_ 前缀按 op 族走 ADD_MULTIPLIED_TOTAL（该 EFF：\n"),
    ("data/gen/stat/wirings/WireGenerated.java",
     " * <p><b>本批来源不是语料模板，而是引擎自身的历史产物</b>：这些 id 不出现在\n",
     " * <p><b>来源不是语料模板，而是引擎自身的历史产物</b>：这些 id 不出现在\n"),
    ("data/gen/stat/wirings/WireGenerated.java",
     " * 本批逐条反查这些历史 JSON 的 {@code effect.ref} 与 {@code amount.value} 作为接线依据，\n",
     " * 逐条反查这些历史 JSON 的 {@code effect.ref} 与 {@code amount.value} 作为接线依据，\n"),
    ("data/gen/stat/wirings/WireMisc.java",
     "        // 事件 CONSUME_INFUSION 由 RemnantState.onConsume 发射（本批补齐的发射点）——\n",
     "        // 事件 CONSUME_INFUSION 由 RemnantState.onConsume 发射——\n"),
    ("data/gen/stat/wirings/WireMisc.java",
     "        // 该事件此前<b>无发射点</b>，令这三条接线集体静默失效；本批补齐后同时复活。\n",
     "        // 该事件缺发射点时，这三条接线会集体静默失效。\n"),
    ("data/gen/stat/wirings/WireMisc.java",
     "        // 事件发射点已在本批补齐（SkillEvent.tryGrantWarcry 记录无标签计数队列），\n"
     "        // 故本条与上方战吼回血同为 live（补齐前二者都不触发，属潜在静默失效）。\n",
     "        // 事件发射点为 SkillEvent.tryGrantWarcry（记录无标签计数队列），\n"
     "        // 故本条与上方战吼回血同为 live（该发射点缺失时二者都不触发）。\n"),
    ("data/gen/stat/wirings/WireMisc.java",
     "DAMAGE_WITH_HITS_IS_LUCKY_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH（本批上段）。\n",
     "DAMAGE_WITH_HITS_IS_LUCKY_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH（见上段）。\n"),
    ("data/gen/stat/wirings/WireMisc.java",
     "        // EFF：此前只有 MELEE+SPEAR 与 PROJECTILE 两个单侧变体，缺 PROJECTILE+SPEAR 组合，\n"
     "        // 本批按 MELEE_SPEAR 镜像新增（TagExpr{DAMAGE, PROJECTILE, SPEAR}），不退接较宽的 PROJECTILE（会放大全部投影物）。\n",
     "        // EFF 走 PROJECTILE+SPEAR 组合（TagExpr{DAMAGE, PROJECTILE, SPEAR}，镜像 MELEE_SPEAR），\n"
     "        // 不退接较宽的 PROJECTILE——那会放大全部投影物伤害。\n"),
    ("data/gen/stat/wirings/WirePerScaling.java",
     "        //   ② Event.State.countTagged(..., unique=true) —— 早已实现但此前<b>零消费者</b>的去重分支，本批启用；\n",
     "        //   ② Event.State.countTagged(..., unique=true) —— 去重分支；\n"),
    ("data/gen/stat/wirings/WirePerScaling.java",
     "        // 故本批对全部技能施放计数（",
     "        // 故对全部技能施放计数（"),
    ("data/gen/stat/wirings/WirePerScaling.java",
     "        // 语料写的是 per <b>Enemy</b>，故必须走后者；此前因引擎缺去重原语而整族 defer（R3：不以次数近似）。\n",
     "        // 语料写的是 per <b>Enemy</b>，故必须走后者（R3：不得以「施加次数」近似「敌人数」）。\n"),
    ("data/gen/stat/wirings/WireRecent.java",
     " * <p>本批两个 COND 的判据差异值得记录，是踩过的坑：\n",
     " * <p>两个 COND 的判据差异值得记录，是踩过的坑：\n"),
    ("data/gen/stat/wirings/WireRecent.java",
     "—— 即本批 {@code COND_COUNT_CAUSED_MELEE_HIT_8S}\n",
     "—— 即 {@code COND_COUNT_CAUSED_MELEE_HIT_8S}\n"),
    ("data/gen/stat/wirings/WireSpeed.java",
     "        // 复活机制本身亦在本批落地（SummonEvent）：永久召唤物（coe:summoned 且 MINION_EXPIRE==0）\n"
     "        // 濒死不真死，进入 100tick÷本倍率 的倒计时，归零时满血复归并发射 MINION_REVIVED。\n"
     "        // 故本条此前是「有属性无机制」的半死接线，现已贯通。\n",
     "        // 复活机制见 SummonEvent：永久召唤物（coe:summoned 且 MINION_EXPIRE==0）\n"
     "        // 濒死不真死，进入 100tick÷本倍率 的倒计时，归零时满血复归并发射 MINION_REVIVED。\n"),
    ("data/gen/stat/StatEffectGen.java",
     "        // （此前 EventName.USE_WARCRY 只有声明、无调用点，使本族与上方战吼回血恒不触发）。\n",
     "        // （USE_WARCRY 缺调用点时，本族与上方战吼回血恒不触发）。\n"),
    ("data/gen/stat/StatEffectIds.java",
     "此前缺 ADD_MULTIPLIED_TOTAL 变体。 */\n",
     "ADD_MULTIPLIED_TOTAL 变体。 */\n"),
    ("data/gen/stat/wirings/WireDamage.java",
     "        //   onTarget(COND_X_STATUS) 现可正确按目标实时状态求值；此前因 damageConvert(attacker) 不绑定目标而 parked）=====\n",
     "        //   onTarget(COND_X_STATUS) 可正确按目标实时状态求值；damageConvert(attacker) 因不绑定目标而不适用）=====\n"),
    ("data/codec/skill/SkillInstance.java",
     "        // autoRelease 结算；此前该置位丢失导致 isPress 恒 false，instance 首 tick 即被 isStop 移除，\n",
     "        // autoRelease 结算；该置位丢失会让 isPress 恒 false，instance 首 tick 即被 isStop 移除，\n"),
]

# ── 全局：「本批」通用句式 ──
GLOBAL_BENPI = [
    ("* <h3>本批未接（缺口，不得以近似顶替——R3）</h3>", "* <h3>未接（缺口，不得以近似顶替——R3）</h3>"),
    ("* <h3>本批排除（判据，勿以近似顶替——R3）</h3>", "* <h3>排除（判据，勿以近似顶替——R3）</h3>"),
    ("* <h3>本批唯一未接（有意排除，勿补）</h3>", "* <h3>唯一未接（有意排除，勿补）</h3>"),
    ("，本批不涉及。</li>", "，不在本文件范围。</li>"),
    ("本批新增 {@code ", "{@code "),
]


def adapt(text: str, eol: str) -> str:
    return text.replace("\n", eol) if eol != "\n" else text


def main() -> int:
    files = [p for p in ROOT.rglob("*.java")]
    cache = {p: p.read_text(encoding="utf-8", newline="") for p in files}
    eols = {p: ("\r\n" if "\r\n" in t else "\n") for p, t in cache.items()}

    applied = 0

    def bump(n: int) -> None:
        nonlocal applied
        applied += n

    # 0) 整行删除（先于括号删除，避免剥离后只剩孤立的「// 。」）
    for rel, marker in LINE_DELETES:
        p = COE / rel.replace("/", "\\")
        if p not in cache:
            print(f"!! NOFILE {rel}")
            return 1
        lines = cache[p].splitlines(keepends=True)
        keep = [ln for ln in lines if marker not in ln]
        n = len(lines) - len(keep)
        if n != 1:
            print(f"!! MISS (line delete, n={n}) {rel} marker={marker!r}")
            return 1
        cache[p] = "".join(keep)
        bump(1)

    # 1) 定点改写（先于全局：定点条目整句给定，可完整覆盖含日期/Wave 的行）
    for rel, old, new in LOCAL:
        p = COE / rel.replace("/", "\\")
        if p not in cache:
            print(f"!! NOFILE {rel}")
            return 1
        o, e = adapt(old, eols[p]), adapt(new, eols[p])
        c = cache[p].count(o)
        if c != 1:
            print(f"!! MISS (count={c}) {rel}\n   old={old[:80]!r}")
            return 1
        cache[p] = cache[p].replace(o, e, 1)
        bump(1)

    # 2) 全局整括号删除（count=0 只告警：可能已被上面的定点/整行删除消化）
    for drop in GLOBAL_DROP_PAREN:
        n = 0
        for p, text in cache.items():
            d = adapt(drop, eols[p])
            c = text.count(d)
            if c:
                cache[p] = text.replace(d, "")
                n += c
        print(f"  {'WARN absent' if n == 0 else 'ok':<12} drop {drop}  x{n}")
        bump(n)

    # 3) 全局括号内只剥日期
    for old, new in GLOBAL_STRIP_IN_PAREN:
        n = 0
        for p, text in cache.items():
            o, e = adapt(old, eols[p]), adapt(new, eols[p])
            c = text.count(o)
            if c:
                cache[p] = text.replace(o, e)
                n += c
        print(f"  {'WARN absent' if n == 0 else 'ok':<12} strip {old[:40]}  x{n}")
        bump(n)

    # 4) 全局「本批」句式
    for old, new in GLOBAL_BENPI:
        n = 0
        for p, text in cache.items():
            o, e = adapt(old, eols[p]), adapt(new, eols[p])
            c = text.count(o)
            if c:
                cache[p] = text.replace(o, e)
                n += c
        print(f"  {'WARN absent' if n == 0 else 'ok':<12} 本批 {old[:40]}  x{n}")
        bump(n)

    written = 0
    for p, text in cache.items():
        with open(p, "r", encoding="utf-8", newline="") as f:
            if f.read() == text:
                continue
        with open(p, "w", encoding="utf-8", newline="") as f:
            f.write(text)
        written += 1
    print(f"applied {applied} edits, wrote {written} files")
    return 0


if __name__ == "__main__":
    sys.exit(main())
