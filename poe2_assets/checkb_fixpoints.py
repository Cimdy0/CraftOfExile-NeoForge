#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""For each CHECK-B mismatch, find whether an ALREADY-EXISTING EFF provides the
correct op on the SAME attribute with a COMPATIBLE tag scope.

If yes -> pure rewiring fix (zero new EFF/COND, zero engine change).
If no  -> true op-variant gap (needs new EFF variant = design work).

Fixes the earlier inventory bug: also matches minecraft-native `Attributes.X`.
"""
import os, re, json
ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
def read(p):
    with open(p, "r", encoding="utf-8") as f: return f.read()

eg = read(os.path.join(GEN, "StatEffectGen.java"))
effs = {}
for m in re.finditer(r'm\.put\(StatEffectIds\.(\w+),\s*new ModifierStatEffect\((.*?)\)\);', eg, re.S):
    name, body = m.group(1), m.group(2)
    am = re.search(r'(?:AttributeRegister|Attributes)\.(\w+)', body)
    om = re.search(r'AttributeModifier\.Operation\.(\w+)', body)
    mm = re.search(r'ModifierStatEffect\.Mode\.(\w+)', body)
    if not am or not om: continue
    if "TagExpr.ALWAYS" in body: tags = frozenset()
    else:
        tm = re.search(r'TagExpr\.of\(Set\.of\((.*?)\)\)', body, re.S)
        tags = frozenset(re.findall(r'StatTag\.(\w+)', tm.group(1))) if tm else None
    effs[name] = dict(attr=am.group(1), op=om.group(1), tags=tags, mode=mm.group(1) if mm else None)
print(f"ModifierStatEffect parsed: {len(effs)}")

# --- parse CHECK B entries from the audit output ---
sg = read(os.path.join(GEN, "StatGen.java"))
wirings = {}
for m in re.finditer(r'map\.put\(StatIds\.(\w+),\s*(?:stat|self|onTarget)\((.*?)\)\);', sg, re.S):
    sid, body = m.group(1), m.group(2)
    parts = [p.strip() for p in body.split(",")]
    cond = None; tgt = None; effname = None
    for p in parts:
        if p.startswith("StatConditionIds."): cond = p.split(".")[1]
        elif p.startswith("StatTargetIds."): tgt = p.split(".")[1]
        elif p.startswith("StatEffectIds."): effname = p.split(".")[1]
    wirings[sid] = dict(cond=cond, tgt=tgt, eff=effname)

def exp_op(name):
    if name.startswith(("INC_","INCREASED_","REDUCED_")): return "ADD_MULTIPLIED_BASE"
    if name.startswith(("MORE_","GREATER_","LESS_")): return "ADD_MULTIPLIED_TOTAL"
    if name.startswith(("TO_","ADD_","ADDITIONAL_")): return "ADD_VALUE"
    return None

mismatch = []
for sid, w in wirings.items():
    e = effs.get(w["eff"])
    if not e: continue
    want = exp_op(sid)
    if want and e["op"] and e["op"] != want:
        mismatch.append((sid, w, e, want))
print(f"CHECK B mismatches: {len(mismatch)}\n")

fixable, gap = [], []
for sid, w, e, want in sorted(mismatch):
    cur = e
    # candidates: same attr, wanted op; tag scope COMPATIBLE
    cands = []
    for en, ee in effs.items():
        if ee["attr"] != cur["attr"]: continue
        if ee["op"] != want: continue
        if cur["tags"] is None or ee["tags"] is None: continue
        # ALWAYS(empty) is the generic variant. If current is ALWAYS -> need ALWAYS.
        # If current has tags -> prefer exact same tag set; also accept ALWAYS? No:
        # switching a scoped wiring to a generic ALWAYS EFF would CHANGE scope -> unsafe.
        if ee["tags"] == cur["tags"]:
            cands.append((en, "EXACT-TAGS"))
    if cands:
        fixable.append((sid, w["eff"], cands, cur["attr"], cur["op"], want, sorted(cur["tags"])))
    else:
        gap.append((sid, w["eff"], cur["attr"], cur["op"], want, sorted(cur["tags"]) if cur["tags"] is not None else None))

print(f"=== FIXABLE by pure rewiring (correct-op EFF already exists, exact same tag scope): {len(fixable)} ===")
for sid, old, cands, attr, oldop, want, tags in fixable:
    names = ", ".join(f"{n}[{k}]" for n,k in cands)
    print(f"  {sid}\n      {old} ({oldop}) -> {names}   attr={attr} tags={tags}")

print(f"\n=== TRUE OP-VARIANT GAP (no correct-op EFF with same tag scope): {len(gap)} ===")
for sid, old, attr, oldop, want, tags in gap:
    print(f"  {sid:52} attr={attr:28} {oldop} -> {want}  tags={tags}")

json.dump({"fixable":[{"sid":s,"old_eff":o,"new_eff":c[0][0],"attr":a,"from":f,"to":t,"tags":tg}
                      for s,o,c,a,f,t,tg in fixable],
           "gap":[{"sid":s,"eff":o,"attr":a,"from":f,"to":t,"tags":tg}
                  for s,o,a,f,t,tg in gap]},
          open(os.path.join(ROOT,"poe2_assets","checkb_fixpoints.json"),"w"), indent=1)
print("\nwrote poe2_assets/checkb_fixpoints.json")
