#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Global landscape classifier for unwired StatIds.

Goal: turn "858 unwired" into an evidence-based push plan.
Buckets:
  F  meta/non-numeric (skip)
  A  conditional + COND exists (or registry-backed) + subject EFF exists w/ correct op  -> SAFE WIRE
  B  conditional + COND missing but registry-backed StatusType/LivingState exists       -> ADD-COND + WIRE
  C  conditional + no registry backing (needs new StatusType / engine)                  -> DEFER (engine)
  D  unconditional + subject EFF exists w/ correct op                                   -> SAFE WIRE
  E  unconditional + subject EFF missing (op gap / new variant)                         -> DESIGN-EFF
Outputs bucket counts + concrete safe-candidate samples to plan/artifact json.
"""
import os, re, json

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
SUP = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "codec", "support")
def read(p):
    with open(p, "r", encoding="utf-8") as f: return f.read()

# ---- StatIds ----
sid_txt = read(os.path.join(SUP, "stat", "StatIds.java"))
statids = dict(re.findall(r'public static final Identifier (\w+) = id\("([^"]+)"\);', sid_txt))

# ---- wired set (TRUE: parse all Wire*.java + StatGen.java, not just StatGen) ----
import glob
wired = set()
for p in glob.glob(os.path.join(GEN, "wirings", "*.java")) + [os.path.join(GEN, "StatGen.java")]:
    wired.update(re.findall(r'StatIds\.(\w+),', read(p)))
unwired = [n for n in statids if n not in wired]

# ---- EFF registry: name -> (op, attr, tags) ----
eg = read(os.path.join(GEN, "StatEffectGen.java"))
eff = {}
for m in re.finditer(r'm\.put\(StatEffectIds\.(\w+),\s*new\s+(\w+)\((.*?)\)\);', eg):
    name, cls, body = m.group(1), m.group(2), m.group(3)
    op = attr = None; tags = []
    if cls == "ModifierStatEffect":
        am = re.search(r'AttributeRegister\.(\w+)', body);  opm = re.search(r'AttributeModifier\.Operation\.(\w+)', body)
        if am: attr = am.group(1)
        if opm: op = opm.group(1)
        if "TagExpr.ALWAYS" in body: tags = ["ALWAYS"]
        else:
            tm = re.search(r'TagExpr\.of\(Set\.of\((.*?)\)\)', body)
            if tm: tags = re.findall(r'StatTag\.(\w+)', tm.group(1))
    eff[name] = (op, attr, tags)

# attr -> list of (eff, op, tags)
by_attr = {}
for n,(op,attr,tags) in eff.items():
    if attr: by_attr.setdefault(attr, []).append((n,op,tags))

# ---- existing COND constants ----
cids = read(os.path.join(GEN, "StatConditionIds.java"))
cond_consts = set(re.findall(r'public static final Identifier (COND_\w+)', cids))

# ---- status / living registries (registry-backed, may or may not be a COND) ----
def registry_names(path, pattern):
    if not os.path.isfile(path): return set()
    t = read(path)
    return set(re.findall(pattern, t))
ail = registry_names(os.path.join(SUP,"status","registry","ailment","AilmentStatusTypes.java"), r'(\w+)\(')
deb = registry_names(os.path.join(SUP,"status","registry","debuff","DebuffStatusTypes.java"), r'(\w+)\(')
liv = registry_names(os.path.join(SUP,"math","context","nodes","LivingStateModes.java"), r'(\w+)\(')
# refine: the registry constructors register by id string; grab enum/constant names
def const_names(path):
    if not os.path.isfile(path): return set()
    return set(re.findall(r'public static final \w+ (\w+) =', read(path)))
ail = const_names(os.path.join(SUP,"status","registry","ailment","AilmentStatusTypes.java"))
deb = const_names(os.path.join(SUP,"status","registry","debuff","DebuffStatusTypes.java"))
liv = const_names(os.path.join(SUP,"math","context","nodes","LivingStateModes.java"))

# ---- subject -> attribute keyword map ----
SUBJ_ATTR = {
 "DAMAGE": ["DAMAGE"], "CRIT_CHANCE": ["CRIT_CHANCE"], "CRIT_DAMAGE": ["CRIT_DAMAGE"],
 "ACCURACY": ["ACCURACY"], "EVASION": ["EVASION"], "ARMOR": ["ARMOR"],
 "MOVEMENT_SPEED": ["MOVEMENT_SPEED"], "CAST_SPEED": ["CAST_SPEED"], "ATTACK_SPEED": ["ATTACK_SPEED"],
 "MANA": ["MAX_MANA","MANA"], "ENERGY_SHIELD": ["MAX_ENERGY_SHIELD","ENERGY_SHIELD"],
 "HEALTH": ["MAX_HEALTH","HEALTH"], "LIFE": ["MAX_HEALTH","HEALTH"],
 "FIRE_DAMAGE": ["DAMAGE"], "COLD_DAMAGE": ["DAMAGE"], "LIGHTNING_DAMAGE": ["DAMAGE"],
 "ELEMENTAL": ["DAMAGE"], "CHAOS_DAMAGE": ["DAMAGE"], "PHYSICAL_DAMAGE": ["DAMAGE"],
 "BLOCK": ["BLOCK_CHANCE"], "STUN": ["STUN_THRESHOLD"], "DEFLECTION": ["DEFLECTION_RATING","DEFLECT"],
 "RESERVATION": ["RESERVATION"], "RAGE": ["MAX_RAGE","RAGE"], "SPIRIT": ["MAX_SPIRIT","SPIRIT"],
 "REGEN": ["REGENERATION"], "LEECH": ["LEECH"], "FLASK": ["FLASK"],
}
def intended_attrs(name):
    out=set()
    for k,v in SUBJ_ATTR.items():
        if k in name: out.update(v)
    return out

def exp_op(name):
    if name.startswith(("INC_","INCREASED_","REDUCED_")): return "ADD_MULTIPLIED_BASE"
    if name.startswith(("MORE_","GREATER_","LESS_")): return "ADD_MULTIPLIED_TOTAL"
    if name.startswith(("TO_","ADD_","ADDITIONAL_")): return "ADD_VALUE"
    return None

# ---- condition keyword detection ----
COND_KW = ["AGAINST","_WHILE_","_IF_","_ON_","_WHEN_","PER_","NEAR_","WITHIN_","VS_","_FOR_",
           "RECENTLY","_DURING_","HAS_","_BY_","_FROM_","IF_YOU","WHILE_","WHEN_"]
def is_conditional(name):
    return any(k in name for k in COND_KW)

SKIP = ("_AND_","AND_","PENALTY","CONVERT","IMMUNE_TO","CANNOT_","CULLING","APPLY_","GAIN_OF",
 "CHANCE_TO","HAS_A_CHANCE","WIELDING_","EQUIP_","AFFECTED_BY","COMPANIONS_","YOUR_COMPANION",
 "INVOCATION","METRES_TO","OFF_HAND_EMPTY","WITH_SPEARS","RECENTLY_TOOK","MAX_RAGE",
 "YOU_CAN_","ENEMIES_YOU","TOTEM","HERALD","ARCANE_SURGE","CHARM","AURA","MINION","MARKED",
 "CURSED","ENEMY_","SKILL_SPEED","MANA_COST_EFFICIENCY","BLOCK_CHANCE","STUN_THRESHOLD",
 "ENERGY_SHIELD_RECHARGE","HEALTH_REGEN","HEALTH_REGENERATION","HEALTH_RECOVERY","MANA_REGEN",
 "REGENERATION_RATE","SOCKET","RARITY","DURATION_OF","MAGNITUDE_OF","EFFECT_OF")
def is_skip(name):
    return any(s in name for s in SKIP)

# ---- classify ----
buckets = {"F":[], "A":[], "B":[], "C":[], "D":[], "E":[]}
for n in unwired:
    if is_skip(n):
        buckets["F"].append(n); continue
    cond = is_conditional(n)
    exp = exp_op(n)
    iattrs = intended_attrs(n)
    # find a subject EFF with matching attr + op + simple tags (ALWAYS or single scope)
    eff_found = None
    if iattrs:
        for a in iattrs:
            for (en, eop, etags) in by_attr.get(a, []):
                if exp and eop == exp:
                    # prefer ALWAYS / minimal tags
                    eff_found = en; break
            if eff_found: break
    if cond:
        # can we resolve a COND? check existing consts or registry
        kw = n
        has_cond = (n in cond_consts) or any(c in n for c in cond_consts)
        reg_backed = any(s in n for s in (list(ail)+list(deb)+list(liv)))
        if eff_found and (has_cond or reg_backed):
            buckets["A" if (n in cond_consts or any(c in n for c in cond_consts)) else "B"].append((n, eff_found))
        elif eff_found and reg_backed:
            buckets["B"].append((n, eff_found))
        else:
            buckets["C"].append(n)
    else:
        if eff_found:
            buckets["D"].append((n, eff_found))
        else:
            buckets["E"].append(n)

print(f"Total StatIds={len(statids)}  wired={len(wired)}  unwired={len(unwired)}")
for k in ["F","A","B","C","D","E"]:
    label = {"F":"F meta/skip","A":"A cond+COND✓+EFF✓ SAFE-WIRE","B":"B cond+COND-missing(reg-backed) ADD-COND",
             "C":"C cond+no-registry DEFER","D":"D uncond+EFF✓ SAFE-WIRE","E":"E uncond+EFF-missing DESIGN-EFF"}[k]
    v = buckets[k]
    print(f"  {label:42} {len(v)}")
print("\n--- Sample A (cond, COND exists, EFF ok) ---")
for x in buckets["A"][:25]: print("   ", x[0], "->", x[1])
print("\n--- Sample B (cond, COND missing but reg-backed) ---")
for x in buckets["B"][:25]: print("   ", x[0], "->", x[1])
print("\n--- Sample C (cond, no registry) ---")
for x in buckets["C"][:25]: print("   ", x)
print("\n--- Sample D (uncond, EFF ok) ---")
for x in buckets["D"][:25]: print("   ", x[0], "->", x[1])
print("\n--- Sample E (uncond, EFF missing) ---")
for x in buckets["E"][:25]: print("   ", x)

# dump json
out = {k:(v if k in ("A","B","D") else v) for k,v in buckets.items()}
json.dump(buckets, open(os.path.join(ROOT,"poe2_assets","unwired_buckets.json"),"w"), indent=1)
print("\nwrote poe2_assets/unwired_buckets.json")
