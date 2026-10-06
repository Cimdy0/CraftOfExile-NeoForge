# -*- coding: utf-8 -*-
"""Phase 2 精确接线匹配器 v2（payload 驱动，非名字子串）。

关键纪律（R3）：matcher 仅作 scout。本脚本对照 StatEffectGen 的 *真实 payload*
（AttributeRegister.X + Operation + StatTag 集合）做匹配，避免名字子串陷阱
（如 RESISTANCE⊂RESISTANCE_PENETRATION 误接）。每条候选输出 const->EFF + 置信度 +
理由；CLEAN 仅当 op/attr/tags 全部精确命中且唯一。最终接线仍须人工核验 EFF 名存在。
"""
import os, re, json, collections

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
GEN = f"{ROOT}/src/main/java/com/cimdy/coe/data/gen/stat"
SPEC = f"{ROOT}/poe2_assets/stat_entry_spec.json"
ST_GEN = f"{GEN}/StatGen.java"
EFF_GEN = f"{GEN}/StatEffectGen.java"
OUT = f"{ROOT}/poe2_assets/proposed_wiring.json"

# ---- 归一 ----
NORM = {
    "ARMOUR": "ARMOR", "LIFE": "HEALTH", "MAXIMUM": "MAX",
    "ACCURACY_RATING": "ACCURACY", "EVASION_RATING": "EVASION",
    "RESISTENCE": "RESISTANCE", "DEFENCE": "DEFENSE", "REGEN": "REGENERATION",
    "RESISTANCES": "RESISTANCE", "MAX_RESISTANCES": "MAX_RESISTANCE",
}
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
    "ENEMY": "ENEMY", "ALLY": "ALLY", "RESISTANCES": "RESISTANCE",
    "MAX_RESISTANCES": "MAX_RESISTANCE",
}
DROP = {
    "add", "multiplied", "base", "total", "value", "attribute", "coe",
    "the", "a", "an", "of", "on", "to", "from", "with", "for", "your", "you",
    "and", "or", "gain", "gains", "gained", "increased", "increase", "adds",
    "added", "more", "reduced", "less", "max", "amount", "per", "have", "has",
    "equal", "deal", "dealt", "non", "rating", "inc", "reduce", "faster",
    "any", "all", "additional", "that", "are", "by", "against", "while",
    "when", "is", "as", "before", "after", "further", "than",
    "affected", "blinded", "recently", "killed", "kill", "this", "these",
    "their", "its", "other", "each", "both", "if", "haven", "t", "been",
    "stunned", "low", "full", "in", "equipped", "also",
}
MECH_MARK = {
    "convert", "converts", "converted", "immune", "immunity", "grant",
    "grants", "recoup", "recouped", "leech", "lucky", "marker", "cull",
    "culling", "requirement", "requirements", "charge", "delivery", "hit",
    "resource", "heal", "damage", "taken",
}
# scope 词（解析到 cond/tgt/变体/标签）
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
    "poison", "bleeding", "ailment",
}
OP_FIRST = {
    "INC": "ADD_MULTIPLIED_BASE", "REDUCE": "ADD_MULTIPLIED_BASE", "FASTER": "ADD_MULTIPLIED_BASE",
    "TO": "ADD_VALUE", "ADD": "ADD_VALUE", "GAIN": "ADD_VALUE", "GAINS": "ADD_VALUE", "TO_MAX": "ADD_VALUE",
    "MORE": "ADD_MULTIPLIED_TOTAL", "LESS": "ADD_MULTIPLIED_TOTAL",
}
MECH_FIRST = {
    "CONVERT": "convert", "CONVERTS": "convert", "CONVERTED": "convert",
    "IMMUNE": "immune", "IMMUNITY": "immune", "GRANT": "grant", "GRANTS": "grant",
    "RECOUP": "recoup", "RECOUPED": "recoup", "LEECH": "leech", "LUCKY": "lucky",
    "MARKER": "marker", "CULL": "culling", "CULLING": "culling",
    "REQUIREMENT": "requirement", "REQUIREMENTS": "requirement",
    "CHARGE": "charge_convert", "DELIVERY": "delivery", "HIT": "hitflag",
    "RESOURCE": "resource", "HEAL": "heal",
}
MECH_TO_EFFCLASS = {
    "convert": "DamageConvertStatEffect", "immune": "ImmuneStatEffect",
    "grant": "GrantOnEventStatEffect", "recoup": "RecoupStatEffect",
    "resource": "ResourceStatEffect", "charge_convert": "ChargeConvertStatEffect",
    "delivery": "DeliveryStatEffect", "lucky": "LuckyStatEffect",
    "culling": "CullingStatEffect", "leech": "LeechStatEffect",
    "marker": "MarkerStatEffect", "hitflag": "HitFlagStatEffect",
    "requirement": "RequirementStatEffect", "heal": "ResourceStatEffect",
}

# scope 词 -> StatTag 期望（用于 payload 标签匹配）
SCOPE_TAG = {
    "fire": "FIRE", "cold": "COLD", "lightning": "LIGHTNING", "physical": "PHYSICAL",
    "chaos": "CHAOS", "elemental": "ELEMENTAL", "attack": "ATTACK", "spell": "SPELL",
    "melee": "MELEE", "curse": "CURSE", "banner": "BANNER", "aura": "AURA",
    "herald": "HERALD", "warcry": "WARCRY", "mark": "MARK", "offering": "OFFERING",
    "projectile": "PROJECTILE", "bolt": "BOLT", "trap": "TRAP", "mine": "MINE",
    "minion": "MINION", "companion": "COMPANION", "totem": "TOTEM",
    "presence": "PRESENCE", "bleed": "BLEED", "poison": "POISON", "ignite": "IGNITE",
    "freeze": "FREEZE", "shock": "SHOCK", "electrocute": "ELECTROCUTE",
    "pin": "PIN", "stun": "STUN", "daze": "DAZE", "charm": "CHARM", "charge": "CHARGE",
    "over_time": "DAMAGE_OVER_TIME", "non_damaging": "NON_DAMAGING",
    "taken": "TAKEN", "enemy": "TAKEN", "enemies": "TAKEN",
}
# scope 词 -> cond/tgt（接线时挂载）
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
}
SCOPE_TO_TGT = {
    "minion": "TGT_INLINE_MINION", "minions": "TGT_INLINE_MINION",
    "companion": "TGT_COMPANION_INLINE", "totem": "TGT_INLINE_TOTEM",
    "totems": "TGT_INLINE_TOTEM", "presence": "TGT_INLINE_PRESENCE_ALLIES",
    "enemy": "TGT_ALL_ON_TARGET", "enemies": "TGT_ALL_ON_TARGET",
    "ally": "TGT_INLINE_PRESENCE_ALLIES", "allies": "TGT_INLINE_PRESENCE_ALLIES",
}
# 变体词（内嵌于 EFF 名/标签；不挂载 cond/tgt）
VARIANT_KW = {
    "attack", "spell", "melee", "cast", "curse", "fire", "cold", "lightning",
    "physical", "chaos", "elemental", "non_damaging", "over_time", "aura", "herald",
    "warcry", "mark", "offering", "projectile", "mace", "knife", "spear",
    "crossbow", "staff", "wand", "bow", "trap", "mine", "infusion", "banner",
    "bear_form", "minion", "companion", "totem", "presence", "ally", "bleed", "poison",
}


def norm_tokens(toks):
    out = []
    for t in toks:
        t = NORM.get(t, t)
        t = PLURALS.get(t, t)
        out.append(t)
    return out


def split_node(name):
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


def _balanced(src, start):
    """从 src[start]（应为 '('）起，返回匹配的右括号索引。"""
    depth = 0
    i = start
    while i < len(src):
        if src[i] == '(':
            depth += 1
        elif src[i] == ')':
            depth -= 1
            if depth == 0:
                return i
        i += 1
    return -1


def parse_eff_index():
    """返回 {EFF_NAME: {impl, op, attribute, tags:set, mode}}，attribute 取
    AttributeRegister.X / Attributes.X 的最后一段（真实属性名）；tags 取自 StatTag 集合。
    """
    src = open(EFF_GEN, encoding="utf-8").read()
    idx = {}
    # 找每个 m.put(StatEffectIds.EFF_XXX, new <Class>( ... ))
    for mobj in re.finditer(
            r"m\.put\(StatEffectIds\.(EFF_[A-Z0-9_]+),\s*new\s+(\w+StatEffect)\(", src):
        const = mobj.group(1)
        clazz = mobj.group(2)
        p_start = mobj.end() - 1  # '(' 位置
        p_end = _balanced(src, p_start)
        if p_end < 0:
            continue
        rest = src[mobj.end():p_end]
        rec = {"impl": clazz, "op": None, "attribute": None, "tags": [], "mode": None}
        if clazz == "ModifierStatEffect":
            m = re.search(r"Operation\.(\w+)", rest)
            rec["op"] = m.group(1) if m else None
            am = re.search(r"(AttributeRegister|Attributes)\.([A-Za-z0-9_]+)", rest)
            if am:
                rec["attribute"] = am.group(2)
            mm = re.search(r"Mode\.(\w+)", rest)
            rec["mode"] = mm.group(1) if mm else None
            tm = re.search(r"StatTag\.((?:\w+|\bnot\b\(\w+\))(?:\s*,\s*StatTag\.(?:\w+|\bnot\b\(\w+\)))*)", rest)
            tags = []
            if tm:
                for piece in re.split(r",\s*StatTag\.", tm.group(1)):
                    piece = piece.strip()
                    if piece.startswith("not("):
                        inner = piece[4:-1]
                        tags.append("!" + inner)
                    else:
                        tags.append(piece)
            rec["tags"] = tags
        idx[const] = rec
    return idx


# 窄作用域标签组：组内互斥——候选要求的必须命中，EFF 不得引入候选未要求的窄标签。
NARROW_GROUPS = [
    {"FIRE", "COLD", "LIGHTNING", "PHYSICAL", "CHAOS", "ELEMENTAL"},
    {"ATTACK", "SPELL", "MELEE", "BOW", "MACE", "KNIFE", "SPEAR", "CROSSBOW",
     "STAFF", "WAND", "TRAP", "MINE", "BALLISTA", "BOLT", "CURSE", "BANNER",
     "AURA", "HERALD", "WARCRY", "MARK", "OFFERING", "PROJECTILE", "AREA", "CAST"},
    {"BLEED", "POISON", "IGNITE", "FREEZE", "SHOCK", "ELECTROCUTE", "PIN",
     "STUN", "DAZE", "CHARM", "CHILL", "SLOW"},
    {"MINION", "COMPANION", "TOTEM", "PRESENCE", "ALLY"},
]
# 元素组的超集宽松：FIRE/COLD/LIGHTNING 可由 ELEMENTAL 覆盖
ELEMENT_SUPERSET = {"FIRE": "ELEMENTAL", "COLD": "ELEMENTAL", "LIGHTNING": "ELEMENTAL"}


def req_tags(scope):
    want, forbid = set(), set()
    for w in scope:
        tag = SCOPE_TAG.get(w)
        if tag == "TAKEN":
            want.add("TAKEN")
        elif tag:
            want.add(tag)
    # 未显式要求 TAKEN 的，视为期望「非 TAKEN」（dealt by you）
    if "TAKEN" not in want:
        forbid.add("TAKEN")
    return want, forbid


def _tag_present(want_tag, eff_pos):
    if want_tag in eff_pos:
        return True
    if want_tag in ELEMENT_SUPERSET and ELEMENT_SUPERSET[want_tag] in eff_pos:
        return True
    return False


def score_eff(op_kind, concept, want_tags, forbid_tags, e):
    """返回 (score, reasons, strict_ok)。strict_ok=True 表示 op/attr/窄标签全部精确命中，可作 CLEAN。"""
    reasons = []
    score = 0
    strict = True
    # 1. op 匹配
    if e["op"] != op_kind:
        return -1, ["op-mismatch"], False
    score += 40
    reasons.append("op=ok")
    # 2. 属性匹配：候选「最长」概念令牌（最具特异性的属性词）必须是 EFF attribute 名的子串。
    #    仅要求最长词命中，可避免 "CHANCE" 误接 CRIT_CHANCE、"MANA" 误接一般性属性等假阳性；
    #    若最长词都不在属性名中，强拒（matcher 仅作 scout，绝不放行疑似误接）。
    attr_tokens = [c.upper() for c in concept if c.lower() not in VARIANT_KW]
    if not attr_tokens:
        reasons.append("attr=neutral")
        strict = False
    else:
        longest = max(attr_tokens, key=len)
        if e["attribute"] and longest in e["attribute"].upper():
            score += 50
            reasons.append("attr=hit:" + longest)
        else:
            return -1, ["attr-miss:" + longest], False
    # 3. 窄标签精确规则
    eff_pos = {t for t in e["tags"] if not t.startswith("!")}
    eff_neg = {t[1:] for t in e["tags"] if t.startswith("!")}
    for wt in want_tags:
        if _tag_present(wt, eff_pos):
            score += 8
        else:
            # 要求的窄标签 EFF 没有 -> 不能接线
            strict = False
            reasons.append("tag-miss:" + wt)
    for ft in forbid_tags:
        if ft in eff_pos:
            strict = False
            score -= 15
            reasons.append("tag-unwant:" + ft)
    # 组冲突：EFF 引入了候选未要求的窄标签
    for grp in NARROW_GROUPS:
        req_in_grp = {t for t in want_tags if t in grp}
        eff_in_grp = {t for t in eff_pos if t in grp}
        # 元素超集宽松：EFF 的 ELEMENTAL 视为覆盖候选的 FIRE/COLD/LIGHTNING 请求
        if "ELEMENTAL" in eff_in_grp and req_in_grp <= {"FIRE", "COLD", "LIGHTNING"}:
            eff_in_grp.discard("ELEMENTAL")
        extra = eff_in_grp - req_in_grp
        if extra:
            strict = False
            score -= 12
            reasons.append("tag-extra:" + ",".join(sorted(extra)))
    return score, reasons, strict


def main():
    spec = json.load(open(SPEC, encoding="utf-8"))
    sg = open(ST_GEN, encoding="utf-8").read()
    wired = set(re.findall(r"map\.put\(StatIds\.(\w+),", sg))
    unwired = [t for t in spec if t.get("constant") not in wired]
    eff = parse_eff_index()
    print(f"EFF indexed: {len(eff)}  unwired: {len(unwired)}")

    proposed = []
    buckets = collections.Counter()
    for t in unwired:
        const = t["constant"]
        tmpl = t.get("template", "")
        op_kind, mech_class, concept, scope = split_node(const)
        if mech_class:
            # 机制类：仅匹配 impl class 相同者（粗略）
            req_impl = MECH_TO_EFFCLASS.get(mech_class)
            cands = [(c, e) for c, e in eff.items() if e["impl"] == req_impl]
            if cands:
                cands.sort(key=lambda kv: len(kv[0]))
                best = cands[0]
                proposed.append({"const": const, "eff": best[0], "conf": "REVIEW_MECH",
                                 "note": "mech class match; verify payload", "tmpl": tmpl})
                buckets["MECH"] += 1
            else:
                buckets["MECH_GAP"] += 1
            continue

        want, forbid = req_tags(scope)
        scored = []
        for c, e in eff.items():
            if e["impl"] != "ModifierStatEffect":
                continue
            s, r, st = score_eff(op_kind, concept, want, forbid, e)
            if s >= 0:
                scored.append((s, c, r, st))
        scored.sort(key=lambda x: (-x[0], x[1]))
        if not scored:
            buckets["NOMATCH"] += 1
            continue
        top_s, top_c, top_r, top_st = scored[0]
        second_s = scored[1][0] if len(scored) > 1 else -999
        if top_st and top_s >= 90 and top_s > second_s + 10:
            conf = "CLEAN"
        elif top_s >= 50:
            conf = "REVIEW"
        else:
            conf = "WEAK"
        buckets[conf] += 1
        proposed.append({
            "const": const, "eff": top_c, "conf": conf,
            "score": top_s, "note": ";".join(top_r),
            "op": op_kind, "concept": concept, "scope": scope,
            "want_tags": sorted(want), "forbid_tags": sorted(forbid),
            "tmpl": tmpl,
        })

    json.dump(proposed, open(OUT, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    print("=== 置信度分桶 ===")
    for k, v in buckets.most_common():
        print(f"  {k:10s} {v}")
    print(f"写出: {OUT}")
    # 打印 CLEAN 样本
    print("\n=== CLEAN 候选（前 60）===")
    n = 0
    for p in proposed:
        if p["conf"] == "CLEAN":
            print(f"  {p['const']:50s} -> {p['eff']:60s}  [{p['score']}] {p['note']}")
            n += 1
            if n >= 60:
                break


if __name__ == "__main__":
    main()
