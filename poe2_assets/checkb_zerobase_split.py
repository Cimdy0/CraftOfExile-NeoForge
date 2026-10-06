#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Split CHECK-B mismatches into TRUE MISWIRES vs ZERO-BASE FALSE POSITIVES.

Law 3 (零基禁 inc): ADD_MULTIPLIED_BASE folds to base*sum -> if the attribute's
registered default base is 0, switching to BASE makes the entry silently dead.
Those reports are auditor false positives: current wiring is the only viable one.

Also handles vanilla `Attributes.*` (not in AttributeRegister) via a known table.
"""
import os, re, json
ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
A = os.path.join(ROOT, "poe2_assets")
base_tbl = json.load(open(os.path.join(A, "attr_base_table.json")))

# vanilla Minecraft attributes used by StatEffectGen: their registered defaults
VANILLA = {"ARMOR": 0.0, "ARMOR_TOUGHNESS": 0.0, "ATTACK_DAMAGE": 1.0,
           "ATTACK_SPEED": 1.0, "MOVEMENT_SPEED": 0.1, "MAX_HEALTH": 20.0,
           "KNOCKBACK_RESISTANCE": 0.0, "LUCK": 0.0, "BLOCK_CHANCE": 0.0}

def base_of(attr):
    if attr in base_tbl: return base_tbl[attr]["base"], "CoE"
    if attr in VANILLA:  return VANILLA[attr], "vanilla"
    return None, "?"

GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
def read(p):
    with open(p, "r", encoding="utf-8") as f: return f.read()

effs = {}
for m in re.finditer(r'm\.put\(StatEffectIds\.(\w+),\s*new ModifierStatEffect\((.*?)\)\);', read(os.path.join(GEN,"StatEffectGen.java")), re.S):
    n, b = m.group(1), m.group(2)
    am = re.search(r'(?:AttributeRegister|Attributes)\.(\w+)', b)
    om = re.search(r'AttributeModifier\.Operation\.(\w+)', b)
    if not (am and om): continue
    tags = frozenset() if "TagExpr.ALWAYS" in b else (
        frozenset(re.findall(r'StatTag\.(\w+)', tm.group(1))) if (tm:=re.search(r'TagExpr\.of\(Set\.of\((.*?)\)\)', b, re.S)) else None)
    effs[n] = dict(attr=am.group(1), op=om.group(1), tags=tags)

sg = read(os.path.join(GEN, "StatGen.java"))
wirings = {}
for m in re.finditer(r'map\.put\(StatIds\.(\w+),\s*(?:stat|self|onTarget)\((.*?)\)\);', sg, re.S):
    sid, body = m.group(1), m.group(2)
    wirings[sid] = dict(
        cond=next((p.split(".")[1] for p in (x.strip() for x in body.split(",")) if p.startswith("StatConditionIds.")), None),
        tgt =next((p.split(".")[1] for p in (x.strip() for x in body.split(",")) if p.startswith("StatTargetIds.")), None),
        eff =next((p.split(".")[1] for p in (x.strip() for x in body.split(",")) if p.startswith("StatEffectIds.")), None))

def exp_op(n):
    if n.startswith(("INC_","INCREASED_","REDUCED_")): return "ADD_MULTIPLIED_BASE"
    if n.startswith(("MORE_","GREATER_","LESS_")): return "ADD_MULTIPLIED_TOTAL"
    if n.startswith(("TO_","ADD_","ADDITIONAL_")): return "ADD_VALUE"
    return None

true_bad, false_pos = [], []
for sid, w in sorted(wirings.items()):
    e = effs.get(w["eff"])
    if not e: continue
    want = exp_op(sid)
    if not want or e["op"] == want: continue
    b, src = base_of(e["attr"])
    rec = dict(sid=sid, eff=w["eff"], attr=e["attr"], base=b, base_src=src,
               cur_op=e["op"], want_op=want,
               tags=sorted(e["tags"]) if e["tags"] is not None else None,
               cond=w["cond"], tgt=w["tgt"])
    # zero-base + wants BASE  -> switching would kill the entry => false positive
    if want == "ADD_MULTIPLIED_BASE" and b == 0:
        false_pos.append(rec)
    else:
        true_bad.append(rec)

print(f"CHECK-B total {len(true_bad)+len(false_pos)}")
print(f"\n=== ZERO-BASE FALSE POSITIVES ({len(false_pos)}) — do NOT change (base=0, inc would silently die) ===")
for r in false_pos:
    print(f"    {r['sid']:48} attr={r['attr']:24}({r['base_src']} base=0)  {r['cur_op']}  tags={r['tags']}")
print(f"\n=== TRUE MISWIRES ({len(true_bad)}) — actionable ===")
for r in true_bad:
    print(f"    {r['sid']:48} attr={r['attr']:24} base={r['base']}  {r['cur_op']} -> {r['want_op']}  tags={r['tags']}")

json.dump({"true_miswires":true_bad,"zero_base_false_positives":false_pos},
          open(os.path.join(A,"checkb_zerobase_split.json"),"w"), indent=1)
print("\nwrote poe2_assets/checkb_zerobase_split.json")
