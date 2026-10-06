#!/usr/bin/env python3
"""Generate AGAINST_<STATUS>_ENEMIES wiring candidates using CURATED maps.

Only uses:
- COND primitives that actually exist in StatConditionIds (verified list below).
- Base EFF families that are simple & unambiguous (generic/melee/attack/projectile
  DAMAGE, CRIT_CHANCE, ACCURACY, convert-gain cold/fire/lightning/physical).
Template: stat(StatConditionIds.COND_X, StatTargetIds.TGT_COND_ON_TARGET, StatEffectIds.EFF_Y)

Status keyword (in the AGAINST clause) -> COND constant name (must exist).
"""
import re, json

ROOT = "src/main/java/com/cimdy/coe"
SID = f"{ROOT}/data/codec/support/stat/StatIds.java"
GEN = f"{ROOT}/data/gen/stat/StatGen.java"
CONDS = f"{ROOT}/data/gen/stat/StatConditionIds.java"

consts = dict(re.findall(r'public static final Identifier (\w+) = id\("([^"]+)"\);', open(SID, encoding="utf-8").read()))
wired = set(re.findall(r'map\.put\(StatIds\.(\w+),', open(GEN, encoding="utf-8").read()))
cond_consts = set(re.findall(r'public static final Identifier (COND_\w+) =', open(CONDS, encoding="utf-8").read()))
unwired = [n for n in consts if n not in wired]

# ---- CURATED status -> COND (only existing primitives) ----
STATUS_COND = {
    "SHOCKED": "COND_SHOCK_STATUS",
    "FROZEN": "COND_FREEZE_STATUS",
    "DAZED": "COND_DAZE_STATUS",
    "HEAVY_STUNNED": "COND_HEAVY_STUN_STATUS",
    "ELECTROCUTED": "COND_ELECTROCUTE_STATUS",
    "PINNED": "COND_PIN_STATUS",
    "BLEEDING": "COND_BLEED_STATUS_AGE",
    "EXPOSURE": "COND_EXPOSURE_FIRE_STATUS",   # generic exposure (fire variant used as catch-all)
}

# ---- CURATED base noun -> EFF constant name (must exist) ----
# keyed by a normalized base signature token
BASE_EFF = {
    "DAMAGE_GEN": "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE",
    "DAMAGE_MELEE": "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_MELEE",
    "DAMAGE_ATTACK": "EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_DAMAGE_DAMAGE",
    "DAMAGE_PROJECTILE": "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_PROJECTILE",
    "CRIT_CHANCE": "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE_CRIT",
    "CRIT_DAMAGE": "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE",  # crit damage = generic damage mult
    "ACCURACY": "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ACCURACY_ACCURACY",
    "CONV_COLD": "EFF_COLD_DAMAGE_CONVERT_GAIN_TRUE",
    "CONV_FIRE": "EFF_DAMAGE_CONVERT_FIRE_GAIN_TRUE",
    "CONV_LIGHTNING": "EFF_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE",
    "CONV_PHYSICAL_COLD": "EFF_DAMAGE_CONVERT_GAIN_COLD_TRUE",
    "CONV_PHYSICAL_FIRE": "EFF_DAMAGE_CONVERT_GAIN_FIRE_TRUE",
    "CONV_PHYSICAL_LIGHTNING": "EFF_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE",
}

def norm_base(name):
    """Strip the AGAINST clause and qualifiers, return a base signature."""
    n = name
    # remove AGAINST ... ENEMIES (greedy up to ENEMIES)
    n = re.sub(r'_AGAINST_[A-Z_]*ENEMIES', '', n)
    n = re.sub(r'_ENEMIES', '', n)
    n = n.replace("WITH_HITS", "").replace("WITH_HIT_DAMAGE", "")
    n = n.replace("DEAL_INC_DAMAGE", "DAMAGE").replace("DEAL_INC", "INC")
    n = n.replace("PROJECTILES_", "").replace("PROJECTILE_", "")
    n = n.replace("INC_", "")
    # normalize
    toks = [t for t in n.split("_") if t]
    return toks

def base_sig(toks):
    s = set(toks)
    if "MELEE" in s and "DAMAGE" in s: return "DAMAGE_MELEE"
    if "ATTACK" in s and "DAMAGE" in s: return "DAMAGE_ATTACK"
    if "PROJECTILE" in s and "DAMAGE" in s: return "DAMAGE_PROJECTILE"
    if "CRITICAL" in s and "HIT" in s and "CHANCE" in s: return "CRIT_CHANCE"
    if "CRITICAL" in s and "DAMAGE" in s: return "CRIT_DAMAGE"
    if "ACCURACY" in s: return "ACCURACY"
    # convert-gain forms
    if "GAIN_OF_COLD_DAMAGE_AS_EXTRA" in "_".join(toks) or (("COLD" in s) and ("AS_EXTRA" in s) and ("DAMAGE" in s)):
        return "CONV_COLD"
    if ("FIRE" in s) and ("AS_EXTRA" in s): return "CONV_FIRE"
    if ("LIGHTNING" in s) and ("AS_EXTRA" in s): return "CONV_LIGHTNING"
    if "DAMAGE" in s: return "DAMAGE_GEN"
    return None

cands = []
for stat in unwired:
    if "AGAINST" not in stat or "ENEMIES" not in stat:
        continue
    # find status keyword in AGAINST clause
    m = re.search(r'AGAINST_([A-Z_]+?)_ENEMIES', stat)
    if not m:
        m = re.search(r'AGAINST_([A-Z_]+?ENEMIES)', stat)
    if not m:
        continue
    clause = m.group(1)
    # map clause to a status we support
    cond = None
    for kw, c in STATUS_COND.items():
        if kw in clause:
            cond = c
            break
    if cond is None:
        continue
    toks = norm_base(stat)
    sig = base_sig(toks)
    if sig is None:
        continue
    eff = BASE_EFF.get(sig)
    if eff is None:
        continue
    if cond not in cond_consts:   # safety: only if COND really defined
        continue
    cands.append({"stat": stat, "cond": cond, "eff": eff, "sig": sig})

# de-dup by stat
seen = {}
for c in cands:
    seen.setdefault(c["stat"], c)
cands = list(seen.values())

# verify all referenced EFF constants exist in StatEffectIds
effids = set(re.findall(r'public static final Identifier (EFF_\w+) =', open(f"{ROOT}/data/gen/stat/StatEffectIds.java", encoding="utf-8").read()))
missing_eff = sorted({c["eff"] for c in cands if c["eff"] not in effids})
missing_cond = sorted({c["cond"] for c in cands if c["cond"] not in cond_consts})
print(f"Candidates: {len(cands)}")
print("Missing EFF constants (must fix before wiring):", missing_eff)
print("Missing COND constants:", missing_cond)
for c in cands:
    ok = "OK" if (c["eff"] in effids and c["cond"] in cond_consts) else "MISSING"
    print(f"  [{ok}] {c['stat']}  cond={c['cond']}  eff={c['eff']}  sig={c['sig']}")
json.dump(cands, open("poe2_assets/against_candidates.json", "w"), indent=1)
