#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Strict exact-scope matcher: a candidate is wireable ONLY if an EFF exists
whose name contains ALL of the stat's scope qualifiers (weapon/element/taken/
minion/etc). This yields the genuine safe subset and prevents scope-mismatch
mis-wiring (the loop trap)."""
import os, re

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
SUP = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "codec", "support")
def read(p):
    with open(p, "r", encoding="utf-8") as f: return f.read()

# EFF registry: name -> op
eg = read(os.path.join(GEN, "StatEffectGen.java"))
effs = {}
for m in re.finditer(r'm\.put\(StatEffectIds\.(\w+),\s*new\s+(\w+)\((.*?)\)\);', eg):
    name, cls, body = m.group(1), m.group(2), m.group(3)
    op = None
    if cls == "ModifierStatEffect":
        om = re.search(r'AttributeModifier\.Operation\.(\w+)', body)
        if om: op = om.group(1)
    effs[name] = op

# subject -> attribute keyword set (for attr routing)
SUBJ = {
 "DAMAGE":["DAMAGE"], "CRIT_CHANCE":["CRIT_CHANCE"], "CRIT_DAMAGE":["CRIT_DAMAGE"],
 "ACCURACY":["ACCURACY"], "EVASION":["EVASION"], "ARMOR":["ARMOR"],
 "MOVEMENT_SPEED":["MOVEMENT_SPEED"], "CAST_SPEED":["CAST_SPEED"], "ATTACK_SPEED":["ATTACK_SPEED"],
 "MAX_MANA":["MAX_MANA"], "MANA":["MAX_MANA"], "ENERGY_SHIELD":["MAX_ENERGY_SHIELD","ENERGY_SHIELD"],
 "MAX_HEALTH":["MAX_HEALTH","HEALTH"], "LIGHT_RADIUS":["LIGHT_RADIUS"],
 "FLASK_CHARGES":["FLASK_CHARGES"], "CHARM":["CHARM"], "BLEED":["BLEED"],
 "DURATION":["DURATION"], "RAGE":["RAGE"], "SPIRIT":["SPIRIT"], "RESERVATION":["RESERVATION"],
}
# scope qualifiers that MUST appear in the EFF name (token form)
SCOPE_TOK = {
 "MELEE":"MELEE", "SPELL":"SPELL", "PROJECTILE":"PROJECTILE", "ATTACK":"ATTACK",
 "BOW":"BOW", "CROSSBOW":"CROSSBOW", "DAGGER":"DAGGER", "FLAIL":"FLAIL",
 "QUARTERSTAVE":"QUARTERSTAVE", "FIRE":"FIRE", "COLD":"COLD", "LIGHTNING":"LIGHTNING",
 "CHAOS":"CHAOS", "PHYSICAL":"PHYSICAL", "ELEMENTAL":"ELEMENTAL", "MINION":"MINION",
 "TOTEM":"TOTEM", "WARCRY":"WARCRY", "BANNER":"BANNER", "AREA":"AREA",
 "TAKEN":"TAKEN", "CURSE":"CURSE", "TRAP":"TRAP", "MINE":"MINE", "HERALD":"HERALD",
 "MOVEMENT":"MOVEMENT", "CHARGE":"CHARGE", "ENDURANCE":"ENDURANCE", "FRENZY":"FRENZY",
 "POWER":"POWER", "FLASK":"FLASK", "POISON":"POISON", "BLEED":"BLEED", "CHILL":"CHILL",
 "IGNITE":"IGNITE", "SHOCK":"SHOCK", "FREEZE":"FREEZE", "BLIND":"BLIND", "STUN":"STUN",
 "PIN":"PIN", "MARK":"MARK", "SEAL":"SEAL", "OFFERING":"OFFERING", "PARRY":"PARRY",
 "INFUSION":"INFUSION", "GRENADE":"GRENADE", "HAZARD":"HAZARD", "BEAST":"BEAST",
 "ARCHON":"ARCHON", "COMPANION":"COMPANION", "FORM":"FORM", "WOLF":"WOLF",
}
def scope_tokens(name):
    return [t for t,v in SCOPE_TOK.items() if t in name]

def exp_op(name):
    if name.startswith(("INC_","INCREASED_","REDUCED_")): return "ADD_MULTIPLIED_BASE"
    if name.startswith(("MORE_","GREATER_","LESS_")): return "ADD_MULTIPLIED_TOTAL"
    if name.startswith(("TO_","ADD_","ADDITIONAL_")): return "ADD_VALUE"
    return None

# unwired
sid = dict(re.findall(r'public static final Identifier (\w+) = id\("([^"]+)"\);', read(os.path.join(SUP,"stat","StatIds.java"))))
wired = set(re.findall(r'map\.put\(StatIds\.(\w+),', read(os.path.join(GEN,"StatGen.java"))))
unwired = [n for n in sid if n not in wired]

QUAL=("WITH","_AT_","_ON_","_FROM_","AGAINST","WHILE","_PER_","_VS_","_BY_","_FOR_","NEAR","WITHIN",
 "_DURING_","HAS_","RECENTLY","IF_","WHEN_","_TO_","_OF_","PERCENT","_IN_","_VIA_","_WITH_",
 "RANGE","EQUIP","SOCKET","_USING_")
# candidate = op-prefixed, single subject, but may carry scope tokens
cands = [n for n in unwired if any(n.startswith(p) for p in ("INC_","MORE_","TO_","ADD_")) and "_" in n[4:]]

verified=[]   # (stat, eff, note)
ambiguous=[]  # (stat, [candidate effs])  -> needs human design
for n in cands:
    exp = exp_op(n)
    scopes = scope_tokens(n)
    # find subject attr
    attr=None
    for k,v in SUBJ.items():
        if k in n: attr=v; break
    if not attr: 
        ambiguous.append((n,[])); continue
    # candidate EFFs: name contains attr token(s) AND all scope tokens, op matches
    best=[]
    for en,op in effs.items():
        if op!=exp: continue
        if not any(a.replace("MAX_","") in en or a in en or en.endswith(a) for a in attr): 
            # require attr token present
            if not any(a in en for a in attr): continue
        if not all(s in en for s in scopes): continue
        best.append(en)
    if len(best)==1:
        verified.append((n,best[0],"exact"))
    elif len(best)>1:
        verified.append((n,best[0],f"exact-multi:{len(best)}"))
    else:
        ambiguous.append((n,[]))

print(f"Candidates scanned: {len(cands)}")
print(f"VERIFIED exact-scope (wireable, single match): {len(verified)}")
print(f"AMBIGUOUS / no exact EFF (needs design): {len(ambiguous)}")
print("\n--- VERIFIED (safe to wire) ---")
for s,e,note in verified:
    print(f"  {s}  ->  {e}   [{note}]")
print("\n--- sample AMBIGUOUS (first 40) ---")
for s,_ in ambiguous[:40]:
    print(f"  {s}")
import json
json.dump({"verified":verified,"ambiguous_count":len(ambiguous)},
          open(os.path.join(ROOT,"poe2_assets","strict_matched.json"),"w"),indent=1)
print("\nwrote poe2_assets/strict_matched.json")
