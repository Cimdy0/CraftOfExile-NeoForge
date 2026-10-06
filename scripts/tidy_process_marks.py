"""第四批注释清理：剥离过程记录（日期 / Wave / 缺陷编号 / 批次号缩写）。

保留对当前实现的约束说明与领域语义，删除「第几波 / 哪天改的 / 缺陷编号」这类
只在改动当时有意义、之后只剩噪音的过程标记。

用法：python scripts/tidy_process_marks.py [--dry]
"""
from __future__ import annotations

import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent / "src" / "main" / "java" / "com" / "cimdy" / "coe"

# (相对路径, 原文, 新文)。每条断言命中且仅命中一次。
REPLACEMENTS: list[tuple[str, str, str]] = [
    # ── A 组：日期 / Wave 波次号 ──────────────────────────────────────────
    ("data/gen/gem/skill/SkillBlueGen.java",
     "        // 元素要害（Elemental Weakness，诅咒，2026-08-18 Wave-31）：原为火焰范围法术（错位）——\n"
     "        // POE2 语义为诅咒（降低目标元素抗性）。改为诅咒技能：施放即对周围 24 格敌人施加\n",
     "        // 元素要害（Elemental Weakness，诅咒）：POE2 语义为降低目标元素抗性，\n"
     "        // 故按诅咒技能实现：施放即对周围 24 格敌人施加\n"),
    ("data/gen/gem/skill/SkillFactory.java",
     "（Wave-31：诅咒技能数据驱动施加范围/时长）",
     "（诅咒技能数据驱动施加范围/时长）"),
    ("data/codec/support/status/registry/buff/BuffStatusTypes.java",
     "     * 不作用于 DoT（POE2：「reduced damage taken from Hits... does not apply to damage over time」；\n"
     "     * Wave-26 修正：原无条件全局减伤偏离 POE2 语义）。",
     "     * 不作用于 DoT（POE2：「reduced damage taken from Hits... does not apply to damage over time」；\n"
     "     * 不可退化为无条件全局减伤，否则偏离 POE2 语义）。"),
    ("data/codec/support/status/registry/debuff/DebuffStatusTypes.java",
     "     * 不作用于普通命中（Wave-26 修正：原无条件总承伤偏离 POE2 语义）。",
     "     * 不作用于普通命中（不可退化为无条件总承伤，否则偏离 POE2 语义）。"),
    ("data/codec/support/status/StatusEngine.java",
     "（Wave-34 归一原则：基础维 × 作用域/条件）",
     "（归一原则：基础维 × 作用域/条件）"),
    ("data/helper/impl/PresenceAura.java",
     "                // 带 {fire} 等 tag 则仅对应元素暴露——用户裁定 Wave-25）",
     "                // 带 {fire} 等 tag 则仅对应元素暴露）"),
    ("event/DamageEvent.java",
     "                    // 组合命中，而不必为每种「击杀 × 异常」组合各立占位属性（Wave-34 归一）。",
     "                    // 组合命中，而不必为每种「击杀 × 异常」组合各立占位属性。"),
    ("event/DamagePipeline.java",
     "            // Darkness 预留 + ES 吸收已统一在 stageEnergy（Wave-32）；此处仅扣生命。",
     "            // Darkness 预留 + ES 吸收统一在 stageEnergy；此处仅扣生命。"),
    ("event/EventName.java",
     "     * 与属性 {@code REVIVE_SPEED}（复活速度，Wave-34 已归一）同属复活机制 backlog。",
     "     * 与属性 {@code REVIVE_SPEED}（复活速度）同属复活机制 backlog。"),

    # ── B 组：缺陷编号 / 重构编号 / 阶段编号 ──────────────────────────────
    ("data/codec/item/text/AdditionType.java",
     "    // ── 偷取（Leech）：触发开关 + 偷取量（D1 修复） ──",
     "    // ── 偷取（Leech）：触发开关 + 偷取量 ──"),
    ("data/gen/equipment/mod/SuffixMod.java",
     "        // ── 偷取（Leech，D1 修复）：命中触发 + 偷取量（Require.ANY，8 档 0.04–0.20 = 4%–20% 伤害偷取）──",
     "        // ── 偷取（Leech）：命中触发 + 偷取量（Require.ANY，8 档 0.04–0.20 = 4%–20% 伤害偷取）──"),
    ("data/codec/skill/phase/action/SpawnAction.java",
     " * 生成实体动作（P2-2 落地）— 在目标位置生成图腾或召唤物。",
     " * 生成实体动作 — 在目标位置生成图腾或召唤物。"),
    ("entity/custom/skill/SkillTotem.java",
     " * 技能图腾（P2-2 SpawnAction 落地）— 由 {@code SpawnAction} 生成的驻场施法体。",
     " * 技能图腾 — 由 {@code SpawnAction} 生成的驻场施法体。"),
    ("data/codec/support/stat/effect/StatEffect.java",
     "     * 效果索引归属（B-1.1 OCP 修复）：效果类型 → StatStore 归入哪个索引 / StatEngine 哪个通道消费。",
     "     * 效果索引归属（开闭原则）：效果类型 → StatStore 归入哪个索引 / StatEngine 哪个通道消费。"),
    ("data/codec/support/stat/StatStore.java",
     "        // 效果分类经 indexKind() 策略（B-1.1 OCP）：新增效果类型零改本构造器。",
     "        // 效果分类经 indexKind() 策略（开闭原则）：新增效果类型零改本构造器。"),
    ("data/codec/support/stat/entry/StatEntry.java",
     "     * 求值期统一经 {@link #bindParams} 注入；{@link StatRead} 覆写为 params⊕vars（H2 优先级修复），",
     "     * 求值期统一经 {@link #bindParams} 注入；{@link StatRead} 覆写为 params⊕vars（实例值优先于模板默认值），"),
    ("data/codec/support/stat/entry/StatEntry.java",
     "     * 求值路径（amount / conditions / describe）统一经此入口绑参 —— 修复 D-5",
     "     * 求值路径（amount / conditions / describe）统一经此入口绑参"),
    ("data/codec/support/stat/entry/StatEntry.java",
     "    /** 唯一取数入口：常量分支零开销，表达式分支经 vars 注入后求值（D-5：统一经 {@link #bindParams}）。 */",
     "    /** 唯一取数入口：常量分支零开销，表达式分支经 vars 注入后求值（统一经 {@link #bindParams}）。 */"),
    ("data/codec/support/stat/entry/StatEntry.java",
     "     * 条件是否通过（值非 0 即通过）。统一经 {@link #bindParams} 注入参数 —— D-5 修复：",
     "     * 条件是否通过（值非 0 即通过）。统一经 {@link #bindParams} 注入参数："),
    ("data/codec/support/stat/entry/StatEntry.java",
     "     * <p><b>D-7 修复：派发键为 {@code entry}，不再是 {@code type}</b>——后者与平铺的",
     "     * <p><b>派发键为 {@code entry} 而非 {@code type}</b>——后者与平铺的"),
    ("data/codec/support/stat/entry/StatRead.java",
     "     * 即「实例覆盖层」语义：模板定义默认值，数据包作者/roll 用 vars 覆写（H2 优先级反转修复）。",
     "     * 即「实例覆盖层」语义：模板定义默认值，数据包作者/roll 用 vars 覆写（实例值优先）。"),
    ("data/codec/support/stat/entry/StatRead.java",
     "    /** 条件判定：与单值路径同源注入（D-5 修复——此前 conditions 转发无 vars 通道）。 */",
     "    /** 条件判定：与单值路径同源注入（conditions 无独立 vars 通道）。 */"),
    ("data/codec/support/stat/StatEngine.java",
     "            // D-5：条件求值上下文经 e.bindParams 注入（$name 在条件里可用，此前无通道）",
     "            // 条件求值上下文经 e.bindParams 注入（$name 在条件里可用）"),
    ("data/codec/support/stat/StatEngine.java",
     "                // D-5：条件求值经 e.bindParams 注入参数；连通性修复：基座经 scope.evalBase 注入事件名/命中上下文",
     "                // 条件求值经 e.bindParams 注入参数；基座经 scope.evalBase 注入事件名/命中上下文"),
    ("data/codec/support/stat/StatEngine.java",
     "     * <p><b>修复的缺陷</b>：此前七条 fold 通道一律硬写 {@code StatEvalContext.self(x)}",
     "     * <p><b>约束</b>：七条 fold 通道不得一律硬写 {@code StatEvalContext.self(x)}"),
    ("data/codec/support/stat/StatEngine.java",
     "     * 跨实体条目安全地不生效，行为与修复前一致。",
     "     * 跨实体条目安全地不生效。"),
    ("data/codec/support/stat/StatTag.java",
     "        // 投射物：箭矢（arrow）+ 全部 proj:<id> 投射物实例（P1-1 修复：实例 id 不再硬编码在此表，",
     "        // 投射物：箭矢（arrow）+ 全部 proj:<id> 投射物实例（实例 id 不硬编码在此表，"),
    ("data/codec/support/stat/StatStore.java",
     "    /** 摊平：group 展开为 members，非 group 原样返回（H1：hybrid 词缀全量生效）。 */",
     "    /** 摊平：group 展开为 members，非 group 原样返回（hybrid 词缀全量生效）。 */"),
    ("data/codec/support/status/StatusStatBridge.java",
     " * <p>寻址语义（B-1.6 修复）：注入/移除按来源前缀 {@code \"status:<type.id>\"} 精确寻址，",
     " * <p>寻址语义：注入/移除按来源前缀 {@code \"status:<type.id>\"} 精确寻址，"),
    ("data/helper/AilmentHelper.java",
     "                // 电击独立标志（P3 修复：不再复用 causeStun，语义与眩晕分离）",
     "                // 电击独立标志（不复用 causeStun：电击与眩晕语义分离）"),
    ("event/Event.java",
     "         * 事件记录/读取不再依赖「每 tick 采样」这个副作用（D-1 修复）。",
     "         * 事件记录/读取不依赖「每 tick 采样」这个副作用。"),
    ("event/GameEvent.java",
     "        // 计数 + 计时（W1 通道：Event.record 一次写全；D-1 已修 tick 基准）",
     "        // 计数 + 计时（Event.record 一次写全；tick 基准）"),
    ("ui/screen/talent/AbstractTalentTreeScreen.java",
     "        // 节点（修复：不再过滤与专精节点相连的边）",
     "        // 节点（保留与专精节点相连的边）"),
    ("data/codec/skill/phase/action/DamageAction.java",
     "        // 2. 附加伤害（按各自伤害类型累加，修复原仅同类型的限制）",
     "        // 2. 附加伤害（按各自伤害类型独立累加）"),
    ("event/DamageEvent.java",
     "            // D-8：死亡受害侧事件——owner=死者。此前仅击杀者 KILL，死者自身无任何回调，",
     "            // 死亡受害侧事件——owner=死者。KILL 事件的 owner 为击杀者，死者自身无回调，"),
    ("event/DamageEvent.java",
     "        // CoE 武器类型以 ItemState 组件为准：覆盖 sword/axe（避免依赖 vanilla instanceof，修复静默死接线）",
     "        // CoE 武器类型以 ItemState 组件为准：覆盖 sword/axe（依赖 vanilla instanceof 会造成静默死接线）"),
    ("event/DamagePipeline.java",
     "        //   此前该维度缺失：攻击者增益型转换（Unholy Might / avatar_of_fire）完全不作用于外发命中伤害——\n"
     "        //   这是普遍 bug，本维度修复（见 STAT_SYSTEM.md §\"攻击者外发命中转换\"）。",
     "        //   缺此维度时，攻击者增益型转换（Unholy Might / avatar_of_fire）完全不作用于外发命中伤害\n"
     "        //   （见 STAT_SYSTEM.md §\"攻击者外发命中转换\"）。"),
    ("event/DamagePipeline.java",
     "            // （如「受到火焰伤害时触发」「被暴击时 X」），修复此前承受侧缺失元素维度的不对称。",
     "            // （如「受到火焰伤害时触发」「被暴击时 X」）——承受侧不可缺元素维度。"),
    ("event/DamagePipeline.java",
     "            // 方案 A 收敛（3-3）：每侧单次 fire 取代原先 hit + caused_hit + caused_crit 三次发射。",
     "            // 每侧单次 fire 取代 hit + caused_hit + caused_crit 三次发射。"),
    ("data/codec/support/stat/StatTagHierarchy.java",
     " * 标签层级条目：一个父类别标签及其子标签集合（重构 3-4）。",
     " * 标签层级条目：一个父类别标签及其子标签集合。"),
    ("data/gen/LangProvider.java",
     "        // 补齐既有站点的显示名翻译（此前缺失，会渲染为裸键）",
     "        // 站点显示名翻译（缺名会渲染为裸键）"),
    ("COEConfig.java",
     "    /** 解码期把 StatRead 引用展开为内联 Stat（重构 3-1）。默认关闭以保留 JSON 结构兼容；",
     "    /** 解码期把 StatRead 引用展开为内联 Stat。默认关闭以保留 JSON 结构兼容；"),

    # ── C 组：批次号缩写（B21 / B32 / B52 / B55 / B56 / B57 / B58 / B59 / B60）──
    ("data/codec/support/status/registry/ailment/AilmentStatusTypes.java",
     "                // 流血禁再生（B58，同源 B57 完全破甲禁再生）：流血施加方持 ENEMIES_YOU_INFLICT_BLEEDING_ON_CANNOT_REGENERATE_HEALTH 时，",
     "                // 流血禁再生（同「完全破甲禁再生」范式）：流血施加方持 ENEMIES_YOU_INFLICT_BLEEDING_ON_CANNOT_REGENERATE_HEALTH 时，"),
    ("data/gen/stat/wirings/WireAilment.java",
     "（镜像上条，加 onTarget(COND_NOT_POISON_STATUS) 门控；复用 B32 取反 COND，零新增注册）",
     "（镜像上条，加 onTarget(COND_NOT_POISON_STATUS) 门控；复用其取反 COND，零新增注册）"),
    ("data/gen/stat/wirings/WireDamage.java",
     "（同 INC_MINION_DAMAGE WireDamage:117 范式，B52 已立）。零新增 EFF/COND/TGT，R3 安全。",
     "（同 INC_MINION_DAMAGE WireDamage:117 范式）。零新增 EFF/COND/TGT，R3 安全。"),
    ("data/gen/stat/wirings/WireDefense.java",
     "        // 诅咒目标侧（B55）：对带 CURSE 状态的敌施加属性，同源 freeze/fire 敌抗性 onTarget 范式（R3 安全）",
     "        // 诅咒目标侧：对带 CURSE 状态的敌施加属性，同源 freeze/fire 敌抗性 onTarget 范式（R3 安全）"),
    ("data/gen/stat/wirings/WireDefense.java",
     "        // B60：诅咒目标侧·阻碍（R3 安全，纯数据）。复用 B55 同族 onTarget(COND_ANY_CURSE, EFF) 范式 +",
     "        // 诅咒目标侧·阻碍（R3 安全，纯数据）。复用同族 onTarget(COND_ANY_CURSE, EFF) 范式 +"),
    ("data/gen/stat/wirings/WireDefense.java",
     "        // 完全破甲致残（B56 机制）：攻击方持此 stat 时 flag 属性置 1.0；",
     "        // 完全破甲致残：攻击方持此 stat 时 flag 属性置 1.0；"),
    ("data/gen/stat/wirings/WireDefense.java",
     "        // 完全破甲禁再生（B57 机制）：攻击方持此 stat 时 flag 属性置 1.0；",
     "        // 完全破甲禁再生：攻击方持此 stat 时 flag 属性置 1.0；"),
    ("data/gen/stat/wirings/WireDefense.java",
     "        // 流血禁再生（B58 机制，同源 B57）：施加方持此 stat 时 flag 属性置 1.0；",
     "        // 流血禁再生（同「完全破甲禁再生」范式）：施加方持此 stat 时 flag 属性置 1.0；"),
    ("data/gen/stat/wirings/WireMisc.java",
     "故按 B56/B57/B58 的 flag 同款范式以固定 amount 承载常量语义，非近似。",
     "故按「完全破甲致残 / 禁再生」同款 flag 范式以固定 amount 承载常量语义，非近似。"),
    ("data/gen/stat/wirings/WireMisc.java",
     "        // B59：重型眩晕恐吓（变形门控变体）。复用 GRANT_INTIMIDATE_ON_HEAVY_STUN 同源 EFF（GrantOnEventStatEffect",
     "        // 重型眩晕恐吓（变形门控变体）。复用 GRANT_INTIMIDATE_ON_HEAVY_STUN 同源 EFF（GrantOnEventStatEffect"),
    ("data/gen/stat/wirings/WireMisc.java",
     "        // 范式同 B59 GRANT_INTIMIDATE_ON_HEAVY_STUN：",
     "        // 范式同 GRANT_INTIMIDATE_ON_HEAVY_STUN："),
    ("data/gen/stat/wirings/WireMisc.java",
     "（PoE2 源 \"further than 6m\"，B21 已验证 live）。",
     "（PoE2 源 \"further than 6m\"）。"),
    ("data/gen/stat/wirings/WireMisc.java",
     "距离中心取 SELF（玩家→最近敌人），与 B21 全簇一致；",
     "距离中心取 SELF（玩家→最近敌人），与全簇一致；"),
    ("data/gen/stat/wirings/WireMisc.java",
     "距离中心取 SELF，与 B21 全簇一致。",
     "距离中心取 SELF，与全簇一致。"),
]


def adapt(text: str, eol: str) -> str:
    """把字面量里的 \\n 折算成目标文件的实际换行符（本工程源码为 CRLF）。"""
    return text.replace("\n", eol) if eol != "\n" else text


def main() -> int:
    dry = "--dry" in sys.argv
    cache: dict[str, str] = {}
    eols: dict[str, str] = {}
    applied = 0
    for rel, old, new in REPLACEMENTS:
        path = ROOT / rel.replace("/", "\\")
        if rel not in cache:
            if not path.exists():
                print(f"!! NOFILE {rel}")
                return 1
            with open(path, "r", encoding="utf-8", newline="") as f:
                cache[rel] = f.read()
            eols[rel] = "\r\n" if "\r\n" in cache[rel] else "\n"
        eol = eols[rel]
        old, new = adapt(old, eol), adapt(new, eol)
        n = cache[rel].count(old)
        if n != 1:
            print(f"!! MISS (count={n}) {rel}\n   old={old[:80]!r}")
            return 1
        cache[rel] = cache[rel].replace(old, new, 1)
        applied += 1
        print(f"   ok  {rel}  {old.strip()[:56]}")
    if dry:
        print(f"\n[dry] would apply {applied} replacements")
        return 0
    for rel, text in cache.items():
        path = ROOT / rel.replace("/", "\\")
        with open(path, "w", encoding="utf-8", newline="") as f:
            f.write(text)
    print(f"\napplied {applied} replacements across {len(cache)} files")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
