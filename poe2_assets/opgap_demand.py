#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Data-driven demand analysis for missing op-variants (Bucket E, non-zero-base subset).

Method (scout only — every candidate still needs manual R3 verification):
  1. Build a truth table from StatGen: wired stat -> (attr, tags, op, cond, tgt).
  2. For each UNWIRED stat, infer its (attr, tags) from its most similar WIRED
     sibling (token overlap), rather than guessing from the name (R3-safe).
  3. Compute the expected op from the StatId prefix (INC_/MORE_/TO_/ADD_).
  4. If (attr, tags, expected_op) does not exist as an EFF -> record as DEMAND.
  5. Mark whether the variant is actually buildable (law 3: BASE on base==0 is FATAL).
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
                "BLOCK_CHANCE":0.0,"MAX_ABSORPTION":0.0,"JUMP_STRENGTH":0.42,"SCALE":1.0}
def base_of(a):
    if a in base_tbl: return base_tbl[a], "CoE"
    if a in VANILLA_BASE: return VANILLA_BASE[a], "vanilla"
    return None, "?"

effs = {}
for m in re.finditer(r'm\.put\(StatEffectIds\.(\w+),\s*new ModifierStatEffect\((.*?)\)\);', read(os.path.join(GEN,"StatEffectGen.java")), re.S):
    n, b = m.group(1), m.group(2)
    am = re.search(r'(?:AttributeRegister|Attributes)\.(\w+)', b)
    om = re.search(r'AttributeModifier\.Operation\.(\w+)', b)
    if not (am and om): continue
    tags = frozenset() if "TagExpr.ALWAYS" in b else (
        frozenset(re.findall(r'StatTag\.(\w+)', tm.group(1))) if (tm:=re.search(r'TagExpr\.of\(Set\.of\((.*?)\)\)', b, re.S)) else None)
    effs[n] = dict(attr=am.group(1), op=om.group(1), tags=tags)

# (attr, tags, op) -> eff name
variant_index = {}
for n, r in effs.items():
    if r["tags"] is None: continue
    variant_index[(r["attr"], r["tags"], r["op"])] = n

sg = read(os.path.join(GEN, "StatGen.java"))
wired = {}
for m in re.finditer(r'map\.put\(StatIds\.(\w+),\s*(?:stat|self|onTarget)\((.*?)\)\);', sg, re.S):
    sid, body = m.group(1), m.group(2)
    e = next((p.split(".")[1].strip() for p in body.split(",") if p.strip().startswith("StatEffectIds.")), None)
    if e in effs: wired[sid] = effs[e]

sid_txt = read(os.path.join(ROOT,"src","main","java","com","cimdy","coe","data","codec","support","stat","StatIds.java"))
statids = dict(re.findall(r'public static final Identifier (\w+) = id\("([^"]+)"\);', sid_txt))
unwired = [n for n in statids if n not in wired]

def toks(n): return set(t for t in n.split("_") if t and t not in {"YOU","YOUR","A","AN","THE","OF","WITH","AND","TO"})
WT = {w: toks(w) for w in wired}

def exp_op(n):
    if n.startswith(("INC_","INCREASED_","REDUCED_")): return "ADD_MULTIPLIED_BASE"
    if n.startswith(("MORE_","GREATER_","LESS_")): return "ADD_MULTIPLIED_TOTAL"
    if n.startswith(("TO_","ADD_","ADDITIONAL_")): return "ADD_VALUE"
    return None

demand, unbuildable, no_sibling = {}, {}, []
for u in unwired:
    want = exp_op(u)
    if not want: continue
    ut = toks(u)
    if not ut: continue
    # nearest wired sibling by token overlap, filtered to ModifierStatEffect ones with known tags
    best, best_score = None, 0.0
    for w, wt in WT.items():
        if wired[w]["tags"] is None: continue
        inter = len(ut & wt)
        if inter < 2: continue
        score = inter / len(ut | wt)
        if score > best_score: best_score, best = score, w
    if not best or best_score < 0.5:
        no_sibling.append(u); continue
    r = wired[best]
    key = (r["attr"], r["tags"], want)
    if key in variant_index: continue          # variant already exists -> not our problem here
    b, src = base_of(r["attr"])
    rec = dict(sid=u, sibling=best, score=round(best_score,3), attr=r["attr"],
               tags=sorted(r["tags"]), want_op=want, base=b, base_src=src)
    if want == "ADD_MULTIPLIED_BASE" and b == 0:
        unbuildable.setdefault(key, []).append(rec)
    else:
        demand.setdefault(key, []).append(rec)

def show(title, d, limit=14):
    print(f"\n=== {title}: {len(d)} distinct missing variants, {sum(len(v) for v in d.values())} demanding stats ===")
    for (attr, tags, op), v in sorted(d.items(), key=lambda kv: -len(kv[1]))[:limit]:
        b, _ = base_of(attr)
        print(f"  attr={attr:26} op={op:22} tags={sorted(tags)}  base={b}")
        print(f"      demand={len(v)}  e.g. {', '.join(x['sid'] for x in v[:4])}")
        print(f"      inferred from sibling: {v[0]['sibling']} (score {v[0]['score']})")

show("BUILDABLE missing op-variants (non-zero-base, or op != BASE)", demand)
show("UNBUILDABLE (BASE on base==0 -> law 3, do NOT create)", unbuildable, limit=8)
print(f"\n(no confident sibling: {len(no_sibling)} unwired stats skipped)")
json.dump({"buildable":{f"{a}|{','.join(sorted(t))}|{o}":v for (a,t,o),v in demand.items()},
           "unbuildable":{f"{a}|{','.join(sorted(t))}|{o}":v for (a,t,o),v in unbuildable.items()},
           "no_sibling":no_sibling},
          open(os.path.join(A,"opgap_demand.json"),"w"), indent=1)
print("\nwrote poe2_assets/opgap_demand.json")
