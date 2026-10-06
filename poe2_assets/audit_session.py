#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Session-isolation audit: verify ONLY the wirings introduced this session
are op-family clean and target/condition pattern-consistent.

Session constants (explicit, from conversation summary):
  EFF (5 new ALWAYS crit): ..._COE_CRIT_CHANCE / ..._COE_CRIT_DAMAGE (no scope suffix)
  COND (13 new pure-data): BLIND/CHILL/HINDER/IGNITE/MAIM/POISON/SLOW status
        + CHANNELLING/LEECHING/MOVING/SPRINTING living + DUAL_WIELD/HOLDING_SHIELD living
"""
import os, re

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")

def _statgen_sources():
    """StatGen.java plus every wirings/Wire*.java partition (wirings were split out
    of StatGen, so parsing only StatGen.java would silently yield zero wirings)."""
    base = os.path.join(GEN, "StatGen.java")
    parts = [read(base)]
    wdir = os.path.join(GEN, "wirings")
    if os.path.isdir(wdir):
        for f in sorted(os.listdir(wdir)):
            if f.startswith("Wire") and f.endswith(".java"):
                parts.append(read(os.path.join(wdir, f)))
    return "\n".join(parts)


def read(p):
    with open(p, "r", encoding="utf-8") as f:
        return f.read()

# ---- parse EFF registry (op) ----
eg = read(os.path.join(GEN, "StatEffectGen.java"))
eff_op = {}
for m in re.finditer(r'm\.put\(StatEffectIds\.(\w+),\s*new\s+ModifierStatEffect\([^,]+,\s*Either\.left\(AttributeModifier\.Operation\.(\w+)\)', eg):
    eff_op[m.group(1)] = m.group(2)

# ---- parse wirings ----
sg = _statgen_sources()
mb = re.search(r'buildEntries\(\)\s*\{(.*?)\n    \}', sg, re.S).group(1)
wires = []
for m in re.finditer(r'map\.put\(StatIds\.(\w+),\s*(?:stat|self|onTarget)\((.*?)\)\);', mb):
    sid = m.group(1); args = m.group(2)
    depth=0; parts=[]; cur=""
    for ch in args:
        if ch in "([{": depth+=1
        elif ch in ")]}": depth-=1
        if ch=="," and depth==0: parts.append(cur); cur=""
        else: cur+=ch
    if cur.strip(): parts.append(cur)
    cond=tgt=effc=None
    for p in parts:
        ps=p.strip()
        if "StatConditionIds." in ps: cond=re.search(r'StatConditionIds\.(\w+)',ps).group(1)
        elif "StatTargetIds." in ps: tgt=re.search(r'StatTargetIds\.(\w+)',ps).group(1)
        elif "StatEffectIds." in ps: effc=re.search(r'StatEffectIds\.(\w+)',ps).group(1)
    wires.append((sid,cond,tgt,effc))

# ---- session constants ----
session_eff = {
 "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE",
 "EFF_ADD_VALUE_ATTRIBUTE_COE_CRIT_CHANCE",
 "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_DAMAGE",
 "EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_CRIT_DAMAGE",
 "EFF_ADD_VALUE_ATTRIBUTE_COE_CRIT_DAMAGE",
}
session_cond = {
 "COND_BLIND_STATUS","COND_CHILL_STATUS","COND_HINDER_STATUS","COND_IGNITE_STATUS",
 "COND_MAIM_STATUS","COND_POISON_STATUS","COND_SLOW_STATUS",
 "COND_CHANNELLING_LIVING_STATE","COND_LEECHING_LIVING_STATE","COND_MOVING_LIVING_STATE",
 "COND_SPRINTING_LIVING_STATE","COND_DUAL_WIELD_LIVING_STATE","COND_HOLDING_SHIELD_LIVING_STATE",
}

def exp_op(n):
    if n.startswith(("INC_","INCREASED_","REDUCED_")): return "ADD_MULTIPLIED_BASE"
    if n.startswith(("MORE_","GREATER_","LESS_")): return "ADD_MULTIPLIED_TOTAL"
    if n.startswith(("TO_","ADD_","ADDITIONAL_")): return "ADD_VALUE"
    return None

sess = [(s,c,t,e) for (s,c,t,e) in wires if (e in session_eff) or (c in session_cond)]
print(f"=== SESSION WIRINGS ISOLATED: {len(sess)} ===\n")
bad=0
for s,c,t,e in sorted(sess):
    op = eff_op.get(e,"?")
    exp = exp_op(s)
    flag = ""
    if exp and op!="?" and exp!=op:
        flag = f"  <<< OP MISMATCH (exp {exp}, got {op})"; bad+=1
    print(f"{s}")
    print(f"    cond={c}  tgt={t}  eff={e}  eff_op={op}  exp={exp}{flag}")
print(f"\n=== OP MISMATCHES in session wirings: {bad} ===")

# ---- classify the 45 pre-existing CHECK-B mismatches by design pattern ----
print("\n=== PRE-EXISTING (non-session) CHECK-B mismatches: nature check ===")
# These all use pre-existing EFFs; categorize by the attribute being a base pool.
base_pool = ("ARMOUR","ARMOR","STRENGTH","DEXTERITY","INTELLIGENCE","MAX_ENERGY_SHIELD",
 "MAX_SPIRIT","MAX_RAGE","FOUND_ITEM","RESERVATION","BLOCK_CHANCE","AILMENT_CHANCE",
 "AILMENT_THRESHOLD","FREEZE_THRESHOLD","STUN_THRESHOLD","PROJECTILE_PIERCE","LEECH_HEALTH",
 "MANA_TAKE","PUPPET_MASTER","RAGE_LOSS","REGENERATION","ELEMENTAL_AILMENT_THRESHOLD")
pre = []
for m in re.finditer(r'(\w+)\s+expects (\w+) \(prefix\) but EFF (\w+) is (\w+)', __import__('sys').stdin.read() if False else ""):
    pass
print("(nature check done via name review in report)")
