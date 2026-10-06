# -*- coding: utf-8 -*-
"""Phase 0 — 权威接线表构建（只读分析，不改 StatGen）。

对照对象 = 全量 545 Effects（StatEffectGen 真实注册），不是 AttributeRegister(109)。
Stat 被"实现" = 接到已注册 EFF_*，属性只是实现机制之一。

判定流程（修正模型，对齐用户纠正）：
  1. 解析 545 EFF：每条带 (impl_class, op_family, concept_tokens, scope_tokens)
       - ModifierStatEffect + ADD_MULTIPLIED_BASE  -> op=INC   (对应 INC_* / increased / faster / reduced)
       - ModifierStatEffect + ADD_VALUE            -> op=ADD   (对应 TO_* / ADD_* / GAIN / +X to)
       - ModifierStatEffect + ADD_MULTIPLIED_TOTAL -> op=MORE  (对应 MORE_* / less)
       - 其余 effect class                          -> op=MECH(clazz)（机制类，无 op 族）
  2. 对每个未接线节点常量：
       a. 由其命名（+模板兜底）判定所需实现类型：numeric{INC/ADD/MORE} 或 mechanism{class}
       b. 抽 concept_tokens / scope_tokens（归一 + 去 op/机制/结构/限定词 + 去作用域词 + 单数化）
       c. 候选 = EFF 满足 (node_concept ⊆ eff_concept) 且 实现类型对齐
            - numeric 节点：EFF.impl==ModifierStatEffect 且 EFF.op==node.op
            - mechanism 节点：EFF.impl==对应机制 class
  3. 分桶：
       - CLEAN  : 恰好 1 个候选 且 所有 scope 词可解析(cond/tgt/variant)  -> 可直接接线
       - REVIEW : 命中候选但 (多候选歧义) 或 (scope 无法干净解析) 或 (仅锚点命中需裁定)
                  -> 按概念族人工裁定(Phase 2)
       - OP_GAP : 概念有 EFF 但实现类型不对（如节点要 INC 却只有 ADD_VALUE） -> Phase 3 设计 op 变体
       - GAP    : 概念在 545 EFF 中零命中 -> Phase 4 设计全新概念
  4. 产出 wire_table.json（逐常量）、review_by_family.json（按 concept 族聚类）、并打印分桶计数。

注意：matcher 仅作 scout；异构词表下具体数字不可全信，REVIEW 必须由人工按族裁定，禁止批量臆造。
"""
import os, re, json, collections

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
GEN = f"{ROOT}/src/main/java/com/cimdy/coe/data/gen/stat"
SPEC = f"{ROOT}/poe2_assets/stat_entry_spec.json"
ST_GEN = f"{GEN}/StatGen.java"
EFF_IDS = f"{GEN}/StatEffectIds.java"
EFF_GEN = f"{GEN}/StatEffectGen.java"
OUT_TABLE = f"{ROOT}/poe2_assets/wire_table.json"
OUT_FAMILY = f"{ROOT}/poe2_assets/review_by_family.json"

# ---- MC 术语归一（与 gen_stat_ids.mc_replace 对齐）----
NORM = {
    "ARMOUR": "ARMOR", "LIFE": "HEALTH", "MAXIMUM": "MAX",
    "ACCURACY_RATING": "ACCURACY", "EVASION_RATING": "EVASION",
    "RESISTENCE": "RESISTANCE", "DEFENCE": "DEFENSE", "REGEN": "REGENERATION",
}
# 单数化（节点常量为复数、EFF 为单数，统一口径）
PLURALS = {
    "BOWS": "BOW", "ENEMIES": "ENEMY", "MINIONS": "MINION", "ATTACKS": "ATTACK",
    "SPELLS": "SPELL", "TOTEMS": "TOTEM", "AURAS": "AURA", "HERALDS": "HERALD",
    "WARCRIES": "WARCRY", "MARKS": "MARK", "OFFERINGS": "OFFERING",
    "PROJECTILES": "PROJECTILE", "COMPANIONS": "COMPANION", "SKILLS": "SKILL",
    "CHARGES": "CHARGE", "AILMENTS": "AILMENT", "DEBUFFS": "DEBUFF",
    "POISONS": "POISON", "BLEEDINGS": "BLEEDING", "BUILDUPS": "BUILDUP",
    "STATUSES": "STATUS", "INFUSIONS": "INFUSION", "REMNANTS": "REMNANT",
    "SHIELDS": "SHIELD", "QUIVERS": "QUIVER", "FOCUSES": "FOCUS",
    "FLAILS": "FLAIL", "TRAPS": "TRAP", "MINES": "MINE", "COMBOS": "COMBO",
    "ENEMY": "ENEMY", "ALLY": "ALLY",
}

# 纯结构/op/连接/限定词，一律丢弃（不参与 concept 命中）
DROP = {
    "add", "multiplied", "base", "total", "value", "attribute", "coe",
    "the", "a", "an", "of", "on", "to", "from", "with", "for", "your", "you",
    "and", "or", "gain", "gains", "gained", "increased", "increase", "adds",
    "added", "more", "reduced", "less", "max", "amount", "per", "have", "has",
    "equal", "deal", "dealt", "non", "rating", "inc", "reduce", "faster",
    "any", "all", "additional", "that", "are", "by", "against", "while",
    "when", "is", "as", "before", "after", "further", "than", "xm",
    "affected", "blinded", "recently", "killed", "kill", "this", "these",
    "their", "its", "other", "each", "both", "if", "haven", "t", "been",
    "stunned", "low", "full", "in", "your", "equipped",
}
# 机制触发词（从 concept 中剔除；机制类别由 MECH_FIRST 判定）
MECH_MARK = {
    "convert", "converts", "converted", "immune", "immunity", "grant",
    "grants", "recoup", "recouped", "leech", "lucky", "marker", "cull",
    "culling", "requirement", "requirements", "charge", "delivery", "hit",
    "resource", "heal", "damage", "taken",
}

# 作用域词（actor/type/状态触发）—— 解析到 cond/tgt/变体，而非新属性
SCOPE = {
    "minion", "minions", "companion", "companions", "totem", "totems", "aura", "auras",
    "herald", "heralds", "warcry", "warcries", "mark", "marks", "offering", "offerings",
    "projectile", "projectiles", "spell", "spells", "attack", "attacks", "melee", "cast",
    "curse", "curses", "fire", "cold", "lightning", "physical", "chaos", "elemental",
    "non_damaging", "over_time", "taken", "low", "enemy", "enemies", "while", "moving",
    "using", "skill", "skills", "presence", "banner", "bear_form", "mace", "knife",
    "spear", "crossbow", "staff", "wand", "bow", "trap", "mine", "recently", "kill",
    "kills", "ground", "chilled", "ignited", "jagged", "shocked", "rare", "unique",
    "full", "stationary", "surrounded", "blocking", "parrying", "shapeshifted", "bleed",
    "daze", "electrocute", "freeze", "heavy_stun", "pin", "shock", "exposure", "infusion",
}

SCOPE_TO_COND = {
    "low": "COND_LIVING_STATE_LOW_LIFE", "full": "COND_FULL_LIFE_LIVING_STATE",
    "stationary": "COND_MOVEMENT_STATIONARY", "surrounded": "COND_LIVING_STATE_SURROUNDED",
    "blocking": "COND_BLOCKING_LIVING_STATE", "parrying": "COND_LIVING_STATE_PARRYING",
    "shapeshifted": "COND_LIVING_STATE_SHAPESHIFTED", "bleed": "COND_BLEED_STATUS_AGE",
    "daze": "COND_DAZE_STATUS", "electrocute": "COND_ELECTROCUTE_STATUS",
    "freeze": "COND_FREEZE_STATUS", "heavy_stun": "COND_HEAVY_STUN_STATUS",
    "pin": "COND_PIN_STATUS", "shock": "COND_SHOCK_STATUS",
    "exposure": "COND_EXPOSURE_FIRE_STATUS", "chilled": "COND_ON_CHILLED_GROUND_STATUS",
    "ignited": "COND_ON_IGNITED_GROUND_STATUS", "jagged": "COND_ON_JAGGED_GROUND_STATUS",
    "shocked": "COND_ON_SHOCKED_GROUND_STATUS", "rare": "COND_PRESENCE_RARITY_RARE_UNIQUE",
    "unique": "COND_PRESENCE_RARITY_RARE_UNIQUE", "kill": "COND_COUNT_KILL_RECENTLY",
    "killed": "COND_COUNT_KILL_RECENTLY", "recently": "COND_COUNT_RECENTLY_TAKEN_HIT",
    "taken": "SCOPE_TAKEN_VARIANT",
}
SCOPE_TO_TGT = {
    "minion": "TGT_INLINE_MINION", "minions": "TGT_INLINE_MINION",
    "companion": "TGT_COMPANION_INLINE", "totem": "TGT_INLINE_TOTEM",
    "totems": "TGT_INLINE_TOTEM", "presence": "TGT_INLINE_PRESENCE_ALLIES",
    "enemy": "TGT_ALL_ON_TARGET", "enemies": "TGT_ALL_ON_TARGET",
    "you": "TGT_SELF_ONLY", "your": "TGT_SELF_ONLY",
    "ally": "TGT_INLINE_PRESENCE_ALLIES", "allies": "TGT_INLINE_PRESENCE_ALLIES",
}
VARIANT_KW = {
    "attack", "attacks", "spell", "spells", "melee", "cast", "curse", "curses", "fire",
    "cold", "lightning", "physical", "chaos", "elemental", "non_damaging", "over_time",
    "aura", "auras", "herald", "heralds", "warcry", "warcries", "mark", "marks",
    "offering", "offerings", "projectile", "projectiles", "mace", "knife", "spear",
    "crossbow", "staff", "wand", "bow", "trap", "mine", "infusion", "banner", "bear_form",
    "minion", "minions", "companion", "totem", "presence", "ally", "allies",
}

OP_FIRST = {
    "INC": "INC", "REDUCE": "INC", "FASTER": "INC", "INC_MAX": "INC",
    "TO": "ADD", "ADD": "ADD", "GAIN": "ADD", "GAINS": "ADD", "TO_MAX": "ADD",
    "MORE": "MORE", "LESS": "MORE",
}
MECH_FIRST = {
    "CONVERT": "convert", "CONVERTS": "convert", "CONVERTED": "convert",
    "IMMUNE": "immune", "IMMUNITY": "immune",
    "GRANT": "grant", "GRANTS": "grant",
    "RECOUP": "recoup", "RECOUPED": "recoup",
    "LEECH": "leech", "LUCKY": "lucky",
    "MARKER": "marker", "CULL": "culling", "CULLING": "culling",
    "REQUIREMENT": "requirement", "REQUIREMENTS": "requirement",
    "CHARGE": "charge_convert", "DELIVERY": "delivery",
    "HIT": "hitflag", "RESOURCE": "resource", "HEAL": "heal",
    "DAMAGE_TAKEN_CONVERT": "damage_taken_convert",
}
MECH_TO_EFFCLASS = {
    "convert": "DamageConvertStatEffect",
    "damage_taken_convert": "DamageTakenConvertStatEffect",
    "immune": "ImmuneStatEffect",
    "grant": "GrantOnEventStatEffect",
    "recoup": "RecoupStatEffect",
    "resource": "ResourceStatEffect",
    "charge_convert": "ChargeConvertStatEffect",
    "delivery": "DeliveryStatEffect",
    "lucky": "LuckyStatEffect",
    "culling": "CullingStatEffect",
    "leech": "LeechStatEffect",
    "marker": "MarkerStatEffect",
    "hitflag": "HitFlagStatEffect",
    "requirement": "RequirementStatEffect",
    "heal": "ResourceStatEffect",
    "damage": "DamageStatEffect",
}

EFFOP = {
    "ADD_MULTIPLIED_BASE": "INC",
    "ADD_VALUE": "ADD",
    "ADD_MULTIPLIED_TOTAL": "MORE",
}


def norm_tokens(toks):
    out = []
    for t in toks:
        t = NORM.get(t, t)
        t = PLURALS.get(t, t)
        out.append(t)
    return out


def split_node(name):
    """节点常量 -> (op_kind, mech_class, concept_tokens, scope_tokens)。"""
    raw = norm_tokens(name.split("_"))
    first = raw[0] if raw else ""
    op_kind = OP_FIRST.get(first)
    mech_class = None
    joined = "_".join(raw)
    for kw, mc in MECH_FIRST.items():
        if kw in joined:
            mech_class = mc
            break
    concept, scope = [], []
    for t in raw:
        tl = t.lower()
        if tl in DROP:
            continue
        if mech_class and tl in MECH_MARK:
            continue
        if tl in SCOPE:
            scope.append(tl)
            continue
        concept.append(tl)
    return op_kind, mech_class, concept, scope


def eff_tokens(name):
    s = name
    if s.startswith("EFF_"):
        s = s[4:]
    for op in ("ADD_MULTIPLIED_BASE", "ADD_MULTIPLIED_TOTAL", "ADD_VALUE"):
        if s.startswith(op + "_"):
            s = s[len(op) + 1:]
            break
    s = re.sub(r"^\d+_\d+_", "", s)
    toks = norm_tokens(s.split("_"))
    concept, scope = [], []
    for t in toks:
        tl = t.lower()
        if tl in DROP:
            continue
        if tl in SCOPE:
            scope.append(tl)
            continue
        concept.append(tl)
    return concept, scope


def parse_eff_index():
    src = open(EFF_GEN).read()
    reg = re.findall(r"m\.put\(StatEffectIds\.(EFF_[A-Z0-9_]+),\s*new\s+(\w+StatEffect)\((.*?)\)", src)
    idx = {}
    for const, clazz, rest in reg:
        op = None
        if clazz == "ModifierStatEffect":
            m = re.search(r"Operation\.(\w+)", rest)
            op = EFFOP.get(m.group(1)) if m else None
        concept, scope = eff_tokens(const)
        idx[const] = {"impl": clazz, "op": op, "concept": concept, "scope": scope}
    return idx


def resolve_scope(word):
    w = word.lower()
    if w in SCOPE_TO_TGT:
        return ("tgt", SCOPE_TO_TGT[w])
    if w in SCOPE_TO_COND:
        ref = SCOPE_TO_COND[w]
        return ("cond", ref) if ref != "SCOPE_TAKEN_VARIANT" else ("variant", "EFF_TAKEN_SUFFIX")
    if w in VARIANT_KW:
        return ("variant", w)
    return ("unresolved", w)


def scope_kind(word):
    return resolve_scope(word)[0]


def anchors(concept):
    """取概念中较可能为核心属性词的 token（长度>=4 且非纯限定词）。"""
    return [c for c in concept if len(c) >= 4]


def main():
    spec = json.load(open(SPEC, encoding="utf-8"))
    sg = open(ST_GEN, encoding="utf-8").read()
    wired = set(re.findall(r"map\.put\(StatIds\.(\w+),", sg))
    unwired = [t for t in spec if t.get("constant") not in wired]
    eff = parse_eff_index()

    buckets = collections.defaultdict(list)
    table = []
    family = collections.defaultdict(list)

    for t in unwired:
        const = t["constant"]
        tmpl = t.get("template", "")
        op_kind, mech_class, concept, scope = split_node(const)
        req_impl = MECH_TO_EFFCLASS.get(mech_class) if mech_class else "ModifierStatEffect"

        # --- 候选 1：concept ⊆ eff.concept 且 实现类型对齐 ---
        cands = []
        for ec, e in eff.items():
            if concept and not set(concept).issubset(set(e["concept"])):
                continue
            if mech_class:
                if e["impl"] != req_impl:
                    continue
            else:
                if e["impl"] != "ModifierStatEffect" or e["op"] != op_kind:
                    continue
            cands.append((ec, e))

        scope_res = [resolve_scope(w) for w in scope]
        unresolved = [w for w, r in zip(scope, scope_res) if r[0] == "unresolved"]
        variant_tokens = [w for w in scope if scope_kind(w) == "variant"]

        def _coverage(c):
            """节点请求的 variant token 被 EFF 的 (concept|scope) 覆盖数。"""
            e = c[1]
            return len(set(variant_tokens) & (set(e["concept"]) | set(e["scope"])))

        best = None
        if cands:
            # 优先选能覆盖节点全部 variant 的候选（如 fire 已内嵌于 EFF 名），
            # 其次 concept 最短，再次名字序。避免把 fire/cold/attack 等变体误判 mismatch。
            cands.sort(key=lambda c: (-_coverage(c), len(c[1]["concept"]), c[0]))
            best = cands[0]

        if cands:
            best_concept = set(best[1]["concept"])
            best_scope = best[1]["scope"]
            best_universe = best_concept | set(best_scope)
            # 作用域需可解析：COND/TGT 在接线时挂载；VARIANT 必须已内嵌于 EFF 的 (concept|scope)
            variant_mismatch = [w for w in variant_tokens if w not in best_universe]
            # EFF 自带节点未请求的 VARIANT -> 过度特化（如节点要 area，EFF 是 area_attack）
            eff_variant_extra = [t for t in (best_concept | set(best_scope))
                                 if t in VARIANT_KW and t not in set(scope)]
            issues = []
            if len(cands) > 1:
                cs = sorted(cands, key=lambda c: (-_coverage(c), len(c[1]["concept"]), c[0]))
                b0, b1 = cs[0], cs[1]
                # 仅在「最优候选不唯一」时才是真歧义：top 的 coverage 严格更高，
                # 或 coverage 持平但 concept 更短（更通用），均视为已确定 -> 不标 multi-cand。
                unambiguous = (_coverage(b0) > _coverage(b1)) or \
                              (_coverage(b0) == _coverage(b1) and len(b0[1]["concept"]) < len(b1[1]["concept"]))
                if not unambiguous:
                    issues.append(f"multi-cand({len(cands)})")
            if unresolved:
                issues.append("unresolved-scope:" + ",".join(unresolved))
            if variant_mismatch:
                issues.append("variant-mismatch:" + ",".join(variant_mismatch))
            if eff_variant_extra:
                issues.append("eff-overspecific:" + ",".join(eff_variant_extra))
            if issues:
                bucket = "REVIEW"
                note = "; ".join(issues) + f"; best={best[0]}"
            else:
                bucket = "CLEAN"
                note = f"cand={best[0]}"
        else:
            # --- 候选 2：概念有任意 EFF 命中（实现类型不对）-> OP_GAP ---
            any_c = [(ec, e) for ec, e in eff.items()
                     if concept and set(concept).issubset(set(e["concept"]))]
            if any_c:
                any_c.sort(key=lambda c: (len(c[1]["concept"]), c[0]))
                bucket = "OP_GAP"
                best = any_c[0]
                note = f"concept exists but wrong impl/op; e.g. {best[0]}"
            else:
                # --- 候选 3：锚点命中（概念核心 token 出现在某 EFF）-> REVIEW（需裁定） ---
                anc = anchors(concept)
                anchor_hits = []
                for ec, e in eff.items():
                    if anc and set(anc).intersection(set(e["concept"])):
                        anchor_hits.append((ec, e))
                if anchor_hits:
                    anchor_hits.sort(key=lambda c: (len(c[1]["concept"]), c[0]))
                    best = anchor_hits[0]
                    bucket = "REVIEW"
                    note = "anchor-match (adjudicate by family); best=" + best[0]
                else:
                    bucket = "GAP"
                    note = "concept not found in 545 EFF"

        rec = {
            "const": const, "template": tmpl,
            "op_kind": op_kind, "mech_class": mech_class,
            "concept": concept, "scope": scope,
            "bucket": bucket, "best_eff": best[0] if best else None,
            "scope_res": scope_res, "note": note,
        }
        table.append(rec)
        buckets[bucket].append(const)
        fam_key = "|".join(sorted(concept)) if concept else ("MECH:" + (mech_class or "?"))
        family[fam_key].append({
            "const": const, "template": tmpl, "bucket": bucket,
            "best_eff": best[0] if best else None, "note": note,
        })

    json.dump(table, open(OUT_TABLE, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    json.dump(
        {k: {"count": len(v), "items": v} for k, v in sorted(family.items(), key=lambda kv: -len(kv[1]))},
        open(OUT_FAMILY, "w", encoding="utf-8"), ensure_ascii=False, indent=1)

    total = len(unwired)
    print(f"unwired total: {total}")
    print(f"EFF indexed : {len(eff)}")
    print("=== 分桶（Phase 归属）===")
    for b in ("CLEAN", "REVIEW", "OP_GAP", "GAP"):
        n = len(buckets[b])
        print(f"  {b:8s} {n:4d}  ({100*n/total:.1f}%)  ->  "
              f"{'Phase1 接线' if b=='CLEAN' else 'Phase2 按族裁定' if b=='REVIEW' else 'Phase3 op变体' if b=='OP_GAP' else 'Phase4 新概念'}")
    print(f"\nCLEAN 样本（前 30）:")
    for r in table:
        if r["bucket"] == "CLEAN":
            print(f"  {r['const']:46s} -> {r['best_eff']}")
    print(f"\nOP_GAP 样本（前 25）:")
    for r in table:
        if r["bucket"] == "OP_GAP":
            print(f"  {r['const']:46s} {r['note']}")
    print(f"\nGAP 样本（前 30）:")
    for r in table:
        if r["bucket"] == "GAP":
            print(f"  {r['const']:46s} concept={r['concept']}  tpl='{r['template']}'")
    print(f"\nREVIEW 概念族数量: {len(family)}")
    print(f"产出: {OUT_TABLE}")
    print(f"产出: {OUT_FAMILY}")


if __name__ == "__main__":
    main()
