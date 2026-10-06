#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Scan for DEAD EFFs: ADD_MULTIPLIED_BASE on a base==0 attribute.

Law 3: ADD_MULTIPLIED_BASE folds to base*Σmult. If the attribute's registered
default base is 0, the EFF contributes exactly 0 -> any wiring using it is a
SILENTLY DEAD stat entry (a real bug, not an auditor false positive).

Section 22 fixed the inc()/faceMod()/valueMod() helper paths in 2026-08-22;
this scans the EFF-registry layer (StatEffectIds/StatEffectGen), which is newer.
"""
import os, re, json
ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
A = os.path.join(ROOT, "poe2_assets")
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
def read(p):
    with open(p, "r", encoding="utf-8") as f: return f.read()

base_tbl = {k: v["base"] for k, v in json.load(open(os.path.join(A,"attr_base_table.json"), encoding="utf-8")).items()}
VANILLA_BASE = {"ARMOR":0.0,"ARMOR_TOUGHNESS":0.0,"ATTACK_DAMAGE":1.0,"ATTACK_SPEED":1.0,
                "MOVEMENT_SPEED":0.1,"MAX_HEALTH":20.0,"KNOCKBACK_RESISTANCE":0.0,"LUCK":0.0,
                "BLOCK_CHANCE":0.0,"MAX_ABSORPTION":0.0,"JUMP_STRENGTH":0.42,"SCALE":1.0,
                "GRAVITY":0.08,"SAFE_FALL_DISTANCE":3.0,"FALL_DAMAGE_MULTIPLIER":1.0}
def base_of(attr):
    if attr in base_tbl: return base_tbl[attr], "CoE"
    if attr in VANILLA_BASE: return VANILLA_BASE[attr], "vanilla"
    return None, "?"

eg = read(os.path.join(GEN, "StatEffectGen.java"))
effs = {}
for m in re.finditer(r'm\.put\(StatEffectIds\.(\w+),\s*new ModifierStatEffect\((.*?)\)\);', eg, re.S):
    n, b = m.group(1), m.group(2)
    am = re.search(r'(?:AttributeRegister|Attributes)\.(\w+)', b)
    om = re.search(r'AttributeModifier\.Operation\.(\w+)', b)
    if not (am and om): continue
    tags = frozenset() if "TagExpr.ALWAYS" in b else (
        frozenset(re.findall(r'StatTag\.(\w+)', tm.group(1))) if (tm:=re.search(r'TagExpr\.of\(Set\.of\((.*?)\)\)', b, re.S)) else None)
    effs[n] = dict(attr=am.group(1), op=om.group(1), tags=tags)

dead = {n: r for n, r in effs.items()
        if r["op"] == "ADD_MULTIPLIED_BASE" and base_of(r["attr"])[0] == 0}

# which wirings use them?
sg = read(os.path.join(GEN, "StatGen.java"))
users = {}
for m in re.finditer(r'map\.put\(StatIds\.(\w+),\s*(?:stat|self|onTarget)\((.*?)\)\);', sg, re.S):
    sid, body = m.group(1), m.group(2)
    e = next((p.split(".")[1].strip() for p in body.split(",") if p.strip().startswith("StatEffectIds.")), None)
    if e in dead: users.setdefault(e, []).append(sid)

print(f"ModifierStatEffect: {len(effs)}   ADD_MULTIPLIED_BASE: {sum(1 for r in effs.values() if r['op']=='ADD_MULTIPLIED_BASE')}")
print(f"\n=== DEAD EFFs (ADD_MULTIPLIED_BASE on base==0 attribute): {len(dead)} ===")
for n in sorted(dead):
    r = dead[n]; b, src = base_of(r["attr"])
    print(f"  {n}")
    print(f"      attr={r['attr']} ({src} base={b})  tags={sorted(r['tags']) if r['tags'] is not None else None}")
    if n in users:
        print(f"      !! USED BY {len(users[n])} wirings: {', '.join(sorted(users[n]))}")
    else:
        print(f"      (unused — latent trap, no wiring currently hits it)")

used_dead = {n: v for n, v in users.items()}
print(f"\n=== SUMMARY ===")
print(f"  dead EFFs: {len(dead)}")
print(f"  dead EFFs actually used by wirings: {len(used_dead)}")
print(f"  DEAD WIRINGS (stat entries that silently contribute 0): {sum(len(v) for v in used_dead.values())}")
for n, v in used_dead.items():
    for s in sorted(v): print(f"      {s}  <-  {n}")

json.dump({"dead_effs":{n:{**dead[n],"tags":sorted(dead[n]["tags"]) if dead[n]["tags"] is not None else None,
                           "base":base_of(dead[n]["attr"])[0],"base_src":base_of(dead[n]["attr"])[1],
                           "used_by":sorted(users.get(n,[]))} for n in dead},
           "dead_wirings":[{"sid":s,"eff":n} for n,v in users.items() for s in sorted(v)]},
          open(os.path.join(A,"dead_eff_scan.json"),"w"), indent=1)
print("\nwrote poe2_assets/dead_eff_scan.json")
