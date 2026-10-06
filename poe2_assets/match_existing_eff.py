#!/usr/bin/env python3
"""Conservative matcher: map unwired StatIds to EXISTING EFFs in StatEffectGen.

Strategy (R3-safe):
- Parse every EFF registration: name, op, attribute, positive tags, negative (not()) tags.
- For each unwired StatId: derive op from prefix; derive requested tags from the
  StatTag string-values that appear as whole tokens in the StatId name.
- Match rule (conservative):
    1. op matches (INC->ADD_MULTIPLIED_BASE, MORE/GREATER->ADD_MULTIPLIED_TOTAL, TO/ADD->ADD_VALUE)
    2. requested tags  ⊆  EFF positive tags
    3. EFF positive tags \ requested  ⊆  {damage}        # allow a generic DAMAGE base
    4. EFF negative tags must not be contradicted by a requested tag that equals them
       (i.e. a stat about 'taken' won't match an EFF carrying not(taken) -- but offensive
        stats are fine because they don't request 'taken')
- Output exact matches (extra=0) as primary wiring candidates; near (extra<=2) for review.
"""
import re, json, collections

ROOT = "src/main/java/com/cimdy/coe"
SID = f"{ROOT}/data/codec/support/stat/StatIds.java"
GEN = f"{ROOT}/data/gen/stat/StatGen.java"
EFFGEN = f"{ROOT}/data/gen/stat/StatEffectGen.java"
STAG = f"{ROOT}/data/codec/support/stat/StatTag.java"

# ---- StatTag values ----
stext = open(STAG, encoding="utf-8").read()
kv = dict(re.findall(r'public static final String (\w+) = "([^"]+)";', stext))
val2name = {v: k for k, v in kv.items()}      # "spell" -> SPELL
all_values = set(val2name)

# ---- StatIds ----
consts = dict(re.findall(r'public static final Identifier (\w+) = id\("([^"]+)"\);', open(SID, encoding="utf-8").read()))
wired = set(re.findall(r'map\.put\(StatIds\.(\w+),', open(GEN, encoding="utf-8").read()))
unwired = [n for n in consts if n not in wired]

# ---- EFFs ----
# StatTag constant NAME (e.g. DAMAGE) -> string value (e.g. "damage")
name2val = {k: v for k, v in kv.items()}
effs = []
blob = open(EFFGEN, encoding="utf-8").read()
for m in re.finditer(r'm\.put\(StatEffectIds\.(\w+),\s*new ModifierStatEffect\((.*?)\)\)\;', blob, re.S):
    name = m.group(1); body = m.group(2)
    am = re.search(r'AttributeRegister\.(\w+)', body)
    om = re.search(r'AttributeModifier\.Operation\.(\w+)', body)
    tm = re.search(r'TagExpr\.(?:of|ALWAYS)\((.*?)\)', body, re.S)
    pos, neg = [], []
    if tm:
        seg = tm.group(1)
        neg_names = set(re.findall(r'not\(StatTag\.(\w+)\)', seg))
        for pm in re.finditer(r'StatTag\.(\w+)', seg):
            if pm.group(1) in neg_names:
                neg.append(name2val.get(pm.group(1), pm.group(1).lower()))
            else:
                pos.append(name2val.get(pm.group(1), pm.group(1).lower()))
    effs.append({"name": name, "attr": am.group(1) if am else None,
                 "op": om.group(1) if om else None,
                 "pos": pos, "neg": neg})

print(f"consts={len(consts)} wired={len(wired)} unwired={len(unwired)} effs={len(effs)}")

OP = {"INC": "ADD_MULTIPLIED_BASE", "MORE": "ADD_MULTIPLIED_TOTAL",
      "GREATER": "ADD_MULTIPLIED_TOTAL", "TO": "ADD_VALUE", "ADD": "ADD_VALUE"}

def stat_op(name):
    for k, v in OP.items():
        if name.startswith(k + "_") or name == k:
            return v
    return None

def requested_tags(name):
    toks = name.lower().split("_")
    out = set()
    for t in toks:
        if t in all_values:
            out.add(t)
    return out

exact, near = [], []
for n in unwired:
    op = stat_op(n)
    if not op:
        continue
    req = requested_tags(n)
    if not req:
        continue
    # conditional words => skip (needs new condition primitive)
    if any(w in n for w in ("WHILE", "IF_", "AGAINST", "WHEN", "RECENTLY", "PER_",
                            "FOR_EACH", "WITH_", "YOU_", "ENEMIES", "FROM_", "CAN_")):
        continue
    for e in effs:
        if e["op"] != op:
            continue
        if e["neg"] and any(rt in e["neg"] for rt in req):
            continue
        epos = set(e["pos"])
        if not req.issubset(epos):
            continue
        extra = epos - req
        if extra and extra != {"damage"}:
            if len(extra) <= 2:
                near.append((n, e["name"], sorted(req), sorted(epos), sorted(extra), e["attr"]))
            continue
        exact.append((n, e["name"], sorted(req), sorted(epos), e["attr"]))

print(f"\nEXACT candidates (extra==0 or =={{damage}}): {len(exact)}")
print(f"NEAR  candidates (extra 1-2): {len(near)}")

# de-dup: keep first best EFF per stat (prefer fewer extra)
seen = {}
for c in exact:
    seen.setdefault(c[0], c)
exact = list(seen.values())

# print exact
with open("poe2_assets/exact_candidates.json", "w") as f:
    json.dump([{"stat": c[0], "eff": c[1], "req": c[2], "efftags": c[3], "attr": c[4]} for c in exact], f, indent=1)
with open("poe2_assets/near_candidates.json", "w") as f:
    json.dump([{"stat": c[0], "eff": c[1], "req": c[2], "efftags": c[3], "extra": c[4], "attr": c[5]} for c in near], f, indent=1)

print("\n=== EXACT (sample first 80) ===")
for c in exact[:80]:
    print(f"{c[0]}  ->  {c[1]}  [req={c[2]} attr={c[4]}]")
if len(exact) > 80:
    print(f"... +{len(exact)-80} more (see exact_candidates.json)")
