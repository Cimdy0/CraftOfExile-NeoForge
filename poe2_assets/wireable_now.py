#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Find unwired stats whose required EFF VARIANT ALREADY EXISTS -> wireable with
zero new EFF and zero new COND (highest safety class).

Method: infer (attr, tags) from the most similar already-wired sibling (token
Jaccard) instead of guessing from the name, then look up whether
(attr, tags, expected_op) is already registered.

Contrast with opgap_demand.py which looks for MISSING variants (needs new EFF);
this one looks for EXISTING variants (needs only a map.put line).
"""
import os, re, json, collections

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
SUP = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "codec", "support")
WIR = os.path.join(GEN, "wirings")
A = os.path.join(ROOT, "poe2_assets")


def rd(p):
    return open(p, encoding="utf-8").read()


base_tbl = {k: v["base"] for k, v in json.load(open(os.path.join(A, "attr_base_table.json"), encoding="utf-8")).items()}
VANILLA = {"ARMOR": 0.0, "ARMOR_TOUGHNESS": 0.0, "ATTACK_DAMAGE": 1.0, "ATTACK_SPEED": 1.0,
           "MOVEMENT_SPEED": 0.1, "MAX_HEALTH": 20.0, "KNOCKBACK_RESISTANCE": 0.0, "LUCK": 0.0,
           "BLOCK_CHANCE": 0.0, "MAX_ABSORPTION": 0.0}


def base_of(a):
    return base_tbl.get(a, VANILLA.get(a))


effs = {}
for m in re.finditer(r'm\.put\(StatEffectIds\.(\w+),\s*new ModifierStatEffect\((.*?)\)\);',
                     rd(os.path.join(GEN, "StatEffectGen.java")), re.S):
    am = re.search(r"(?:AttributeRegister|Attributes)\.(\w+)", m.group(2))
    om = re.search(r"AttributeModifier\.Operation\.(\w+)", m.group(2))
    if not (am and om): continue
    tags = frozenset() if "TagExpr.ALWAYS" in m.group(2) else frozenset(
        re.findall(r"StatTag\.(\w+)", (re.search(r"TagExpr\.of\(Set\.of\((.*?)\)\)", m.group(2), re.S) or [None, ""])[1]))
    effs[m.group(1)] = (am.group(1), om.group(1), tags)

variant_index = {(a, t, o): n for n, (a, o, t) in effs.items()}

files = [os.path.join(GEN, "StatGen.java")] + [os.path.join(WIR, f) for f in os.listdir(WIR)]
wired = {}          # sid -> wiring whose EFF is a ModifierStatEffect (used for sibling inference)
wired_all = set()   # EVERY wired sid, regardless of effect class -- must match the gate's 885
for f in files:
    for m in re.finditer(r'map\.put\(StatIds\.(\w+),\s*(?:stat|self|onTarget|wiring|rewire)\((.*?)\)\);', rd(f), re.S):
        sid, body = m.group(1), m.group(2)
        wired_all.add(sid)
        eff = next((p.split(".")[1].strip() for p in body.split(",") if p.strip().startswith("StatEffectIds.")), None)
        cond = next((p.split(".")[1].strip() for p in body.split(",") if p.strip().startswith("StatConditionIds.")), None)
        if eff in effs:
            wired[sid] = {"eff": eff, "cond": cond}

statids = set(re.findall(r'public static final Identifier (\w+) = id\(', rd(os.path.join(SUP, "stat", "StatIds.java"))))
unwired = sorted(statids - wired_all)

# corpus for provenance check
corpus = json.load(open(os.path.join(A, "node_stat_map.json"), encoding="utf-8"))
raw_by_const = collections.defaultdict(set)
for node, stats in corpus.items():
    for s in stats:
        if s.get("constant"):
            raw_by_const[s["constant"]].add(s.get("raw", ""))

STOP = {"YOU", "YOUR", "A", "AN", "THE", "OF", "WITH", "AND", "TO", "HAVE", "HAS", "ARE",
        "ENEMIES", "ENEMY", "IF", "WHILE", "WHEN", "PER", "EACH", "AT", "ON", "IN", "FROM"}


def toks(n):
    return set(t for t in n.split("_") if t and t not in STOP)


WT = {w: toks(w) for w in wired}


def exp_op(n):
    if n.startswith(("INC_", "INCREASED_", "REDUCED_")): return "ADD_MULTIPLIED_BASE"
    if n.startswith(("MORE_", "GREATER_", "LESS_")): return "ADD_MULTIPLIED_TOTAL"
    if n.startswith(("TO_", "ADD_", "ADDITIONAL_")): return "ADD_VALUE"
    return None


ready, needs_cond, no_sibling = [], [], []
for u in unwired:
    want = exp_op(u)
    if not want: continue
    ut = toks(u)
    if not ut: continue
    best, bs = None, 0.0
    for w, wt in WT.items():
        if len(ut & wt) < 2: continue
        sc = len(ut & wt) / len(ut | wt)
        if sc > bs: bs, best = sc, w
    if not best or bs < 0.5:
        no_sibling.append(u); continue
    a, o, t = effs[wired[best]["eff"]]
    key = (a, t, want)
    rec = dict(sid=u, sibling=best, score=round(bs, 3), attr=a, tags=sorted(t),
               want_op=want, base=base_of(a),
               raw=sorted(raw_by_const.get(u, []))[:2])
    if key in variant_index:
        rec["eff"] = variant_index[key]
        ready.append(rec)
    else:
        needs_cond.append(rec)

print(f"unwired={len(unwired)}  wired={len(wired)}")
print(f"\n=== WIREABLE NOW (variant exists, zero new EFF/COND): {len(ready)} ===")
for r in sorted(ready, key=lambda x: -x["score"]):
    print(f"  {r['sid']}")
    print(f"      sibling={r['sibling']} (score {r['score']})  attr={r['attr']}(base={r['base']})")
    print(f"      tags={r['tags']}  op={r['want_op']}  eff={r['eff']}")
    if r["raw"]:
        print(f"      corpus: {r['raw'][0][:88]}")

print(f"\n=== variant missing (would need new EFF): {len(needs_cond)} ===")
cnt = collections.Counter((r["attr"], r["want_op"]) for r in needs_cond)
for (a, o), n in cnt.most_common(12):
    b = base_of(a)
    blocked = " ZERO-BASE BLOCKED" if (o == "ADD_MULTIPLIED_BASE" and b == 0) else ""
    print(f"  {n:4}  attr={a:26} op={o:22} base={b}{blocked}")
print(f"\n(no confident sibling: {len(no_sibling)})")

json.dump({"ready": ready, "needs_new_variant": needs_cond},
          open(os.path.join(A, "wireable_now.json"), "w"), indent=1)
print("wrote poe2_assets/wireable_now.json")
