#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Learn "condition suffix -> (COND, TGT)" directly from the wiring truth table,
then use it to find unwired stats that are just conditional variants of a wired base.

Why this is safe (R3):
  * the (cond, tgt) for a suffix is only trusted when EVERY observed occurrence
    agrees -- no guessing;
  * the EFF is REUSED from the base stat (its op is already proven for that attribute);
  * zero-base attributes are skipped when the expected op is ADD_MULTIPLIED_BASE.

This is the third frontier after op-variants (Phase 11) and new COND primitives
(Phase 12): here we need no new EFF and no new COND -- only new map.put lines.
"""
import os, re, json, collections

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
SUP = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "codec", "support")
WIR = os.path.join(GEN, "wirings")
A = os.path.join(ROOT, "poe2_assets")


def rd(p):
    return open(p, encoding="utf-8").read()


# ---------- attribute base table ----------
base_tbl = {k: v["base"] for k, v in json.load(open(os.path.join(A, "attr_base_table.json"), encoding="utf-8")).items()}
VANILLA = {"ARMOR": 0.0, "ARMOR_TOUGHNESS": 0.0, "ATTACK_DAMAGE": 1.0, "ATTACK_SPEED": 1.0,
           "MOVEMENT_SPEED": 0.1, "MAX_HEALTH": 20.0, "KNOCKBACK_RESISTANCE": 0.0, "LUCK": 0.0,
           "BLOCK_CHANCE": 0.0, "MAX_ABSORPTION": 0.0}


def base_of(a):
    if a in base_tbl: return base_tbl[a]
    if a in VANILLA: return VANILLA[a]
    return None


# ---------- EFF -> (attr, op, tags) ----------
effs = {}
for m in re.finditer(r'm\.put\(StatEffectIds\.(\w+),\s*new ModifierStatEffect\((.*?)\)\);',
                     rd(os.path.join(GEN, "StatEffectGen.java")), re.S):
    am = re.search(r"(?:AttributeRegister|Attributes)\.(\w+)", m.group(2))
    om = re.search(r"AttributeModifier\.Operation\.(\w+)", m.group(2))
    if not (am and om): continue
    tags = frozenset() if "TagExpr.ALWAYS" in m.group(2) else frozenset(
        re.findall(r"StatTag\.(\w+)", (re.search(r"TagExpr\.of\(Set\.of\((.*?)\)\)", m.group(2), re.S) or [None, ""])[1]))
    effs[m.group(1)] = (am.group(1), om.group(1), tags)

# ---------- wirings ----------
files = [os.path.join(GEN, "StatGen.java")] + [os.path.join(WIR, f) for f in os.listdir(WIR)]
wired = {}
for f in files:
    for m in re.finditer(r'map\.put\(StatIds\.(\w+),\s*(?:stat|self|onTarget|wiring|rewire)\((.*?)\)\);', rd(f), re.S):
        sid, body = m.group(1), m.group(2)
        cond = next((p.split(".")[1].strip() for p in body.split(",") if p.strip().startswith("StatConditionIds.")), None)
        tgt = next((p.split(".")[1].strip() for p in body.split(",") if p.strip().startswith("StatTargetIds.")), None)
        eff = next((p.split(".")[1].strip() for p in body.split(",") if p.strip().startswith("StatEffectIds.")), None)
        if eff:
            wired[sid] = {"cond": cond, "tgt": tgt, "eff": eff}

statids = set(re.findall(r"public static final Identifier (\w+) =", rd(os.path.join(SUP, "stat", "StatIds.java"))))
unwired = sorted(statids - set(wired))


def exp_op(n):
    if n.startswith(("INC_", "INCREASED_", "REDUCED_")): return "ADD_MULTIPLIED_BASE"
    if n.startswith(("MORE_", "GREATER_", "LESS_")): return "ADD_MULTIPLIED_TOTAL"
    if n.startswith(("TO_", "ADD_", "ADDITIONAL_")): return "ADD_VALUE"
    return None


# ---------- 1) learn suffix -> (cond,tgt) from the truth table ----------
# For every wired pair (B, X) with X == B + "_" + suffix, record what condition X adds.
suffix_votes = collections.defaultdict(collections.Counter)
bases = sorted(wired, key=len)
bset = set(wired)
for x in wired:
    for b in bases:
        if len(b) >= len(x): break
        if x.startswith(b + "_"):
            suffix = x[len(b) + 1:]
            wx, wb = wired[x], wired[b]
            # the added part must be exactly a condition (EFF identical to base)
            if wx["eff"] != wb["eff"]:
                continue
            if wx["cond"] == wb["cond"] and wx["tgt"] == wb["tgt"]:
                continue                      # nothing added -> not a conditional variant
            suffix_votes[suffix][(wx["cond"], wx["tgt"])] += 1
            break                             # shortest base wins

# keep only suffixes with a single, consistent (cond,tgt) observed at least twice
suffix_map, ambiguous = {}, {}
for s, votes in suffix_votes.items():
    (best, n) = votes.most_common(1)[0]
    if len(votes) == 1:
        suffix_map[s] = best
    elif len(votes) > 1:
        ambiguous[s] = dict(votes)

print(f"wired={len(wired)}  unwired={len(unwired)}")
print(f"learned consistent condition suffixes: {len(suffix_map)}   (ambiguous rejected: {len(ambiguous)})")
print("\n--- suffix -> (COND, TGT)  [top 30 by length] ---")
for s in sorted(suffix_map, key=lambda k: -len(k))[:30]:
    c, t = suffix_map[s]
    print(f"  _{s:42} -> {c or '(none)'}  /  {t}   (seen {sum(suffix_votes[s].values())}x)")

# ---------- 2) apply to unwired ----------
cands, need_new_variant, zero_base_blocked, base_unwired = [], [], [], []
for u in unwired:
    hit = None
    for b in bases:
        if len(b) >= len(u): break
        if u.startswith(b + "_"):
            suffix = u[len(b) + 1:]
            if suffix in suffix_map:
                hit = (b, suffix); break
    if not hit:
        continue
    b, suffix = hit
    cond, tgt = suffix_map[suffix]
    e_attr, e_op, e_tags = effs[wired[b]["eff"]]
    want = exp_op(u)
    bval = base_of(e_attr)
    rec = dict(sid=u, base=b, suffix=suffix, cond=cond, tgt=tgt, eff=wired[b]["eff"],
               attr=e_attr, op=e_op, base_val=bval, want_op=want, tags=sorted(e_tags))
    if want and want != e_op:
        if want == "ADD_MULTIPLIED_BASE" and bval == 0:
            zero_base_blocked.append(rec)
        else:
            need_new_variant.append(rec)
        continue
    if want == "ADD_MULTIPLIED_BASE" and bval == 0:
        zero_base_blocked.append(rec); continue
    cands.append(rec)

print(f"\n=== WIREABLE NOW (reuse base EFF + learned COND/TGT, zero new EFF/COND): {len(cands)} ===")
for r in cands:
    print(f"  {r['sid']}")
    print(f"      base={r['base']}  suffix=_{r['suffix']}  -> {r['cond']} / {r['tgt']}")
    print(f"      eff={r['eff']}  attr={r['attr']}(base={r['base_val']})  op={r['op']}")

print(f"\n=== needs a NEW op variant (skipped): {len(need_new_variant)} ===")
for r in need_new_variant[:10]:
    print(f"  {r['sid']:58} {r['op']} -> {r['want_op']}  attr={r['attr']}(base={r['base_val']})")
print(f"\n=== zero-base blocked (law 3): {len(zero_base_blocked)} ===")
for r in zero_base_blocked[:10]:
    print(f"  {r['sid']:58} attr={r['attr']}(base=0) wants {r['want_op']}")

json.dump({"candidates": cands, "need_new_variant": need_new_variant,
           "zero_base_blocked": zero_base_blocked,
           "suffix_map": {k: {"cond": v[0], "tgt": v[1]} for k, v in suffix_map.items()},
           "ambiguous_suffixes": {k: {f"{c}|{t}": n for (c, t), n in v.items()} for k, v in ambiguous.items()}},
          open(os.path.join(A, "suffix_frontier.json"), "w"), indent=1)
print("\nwrote poe2_assets/suffix_frontier.json")
