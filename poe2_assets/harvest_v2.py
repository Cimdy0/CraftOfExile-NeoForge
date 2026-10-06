#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Phase 13: condition-variant harvest against the EXPANDED truth table.

Truth table is now 885 wirings / 56 COND primitives. Phase 8 showed that a
HAND-WRITTEN COND_MAP silently misses clause spellings -- so instead of writing one,
LEARN the mapping from the existing wirings: for every wired conditional stat,
recover its base (longest wired stat name that is a strict prefix) and record
   clause -> COND.
That way all 56 COND (including the 4 added in Phase 12) are covered automatically.
"""
import os, re, json, collections

ROOT = r"E:/McModding/Modding/CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
SUP = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "codec", "support")
WIR = os.path.join(GEN, "wirings")
def rd(p): return open(p, encoding="utf-8").read()

# ---------- truth table ----------
wired = {}
for f in [os.path.join(GEN, "StatGen.java")] + [os.path.join(WIR, x) for x in sorted(os.listdir(WIR))]:
    for m in re.finditer(r'map\.put\(StatIds\.(\w+),\s*(?:stat|self|onTarget)\((.*?)\)\);', rd(f), re.S):
        sid, body = m.group(1), m.group(2)
        cond = tgt = eff = None
        for p in [x.strip() for x in body.split(",")]:
            if p.startswith("StatConditionIds."): cond = p.split(".")[1]
            elif p.startswith("StatTargetIds."): tgt = p.split(".")[1]
            elif p.startswith("StatEffectIds."): eff = p.split(".")[1]
        wired[sid] = dict(cond=cond, tgt=tgt, eff=eff)

statids = set(re.findall(r'public static final Identifier (\w+) =', rd(os.path.join(SUP, "stat", "StatIds.java"))))
unwired = sorted(statids - set(wired))
print(f"wired={len(wired)}  unwired={len(unwired)}")

META = {"YOU", "YOUR", "A", "AN", "THE", "OF", "WITH", "AND", "TO", "IS", "ARE", "THAT", "HAVE", "HAS", "IF", "WHILE", "ON", "IN", "AT", "FOR", "PER", "WHEN", "AGAINST", "VS", "FROM", "BEEN", "BE", "OR", "NOT"}

def clean(clause):
    toks = [t for t in clause.strip("_").split("_") if t and t not in META]
    return "_".join(toks)

# ---------- LEARN clause -> COND ----------
by_len = sorted(wired, key=len, reverse=True)
learned = collections.defaultdict(collections.Counter)
for sid, w in wired.items():
    if not w["cond"]: continue
    base = None
    for cand in by_len:
        if cand != sid and sid.startswith(cand + "_"):
            base = cand; break
    if not base: continue
    clause = sid[len(base):]
    learned[clause][w["cond"]] += 1
    c2 = clean(clause)
    if c2: learned.setdefault("~" + c2, collections.Counter())[w["cond"]] += 1

resolved = {c: v.most_common(1)[0][0] for c, v in learned.items() if len(v) == 1}
print(f"learned clause spellings: {len(learned)}  unambiguous: {len(resolved)}")

# ---------- match unwired ----------
cands = []
for u in unwired:
    base = None
    for cand in by_len:
        if u.startswith(cand + "_"):
            base = cand; break
    if not base: continue
    clause = u[len(base):]
    cond = resolved.get(clause) or resolved.get("~" + clean(clause))
    if not cond: continue
    bw = wired[base]
    if bw["cond"] is not None: continue      # base must be unconditional (pure mirror)
    cands.append(dict(sid=u, base=base, clause=clause, cond=cond, eff=bw["eff"], tgt=bw["tgt"]))

print(f"\n=== condition-variant candidates: {len(cands)} ===")
seen_cond = collections.Counter(c["cond"] for c in cands)
for c in cands[:40]:
    print(f"  {c['sid']}")
    print(f"      base={c['base']}  clause={c['clause']}")
    print(f"      -> cond={c['cond']}  tgt={c['tgt']}  eff={c['eff']}")
print("\nCOND usage among candidates:")
for k, v in seen_cond.most_common(20): print(f"   {v:4}  {k}")
json.dump(cands, open(os.path.join(ROOT, "poe2_assets", "harvest_v2.json"), "w"), indent=1)
print("\nwrote poe2_assets/harvest_v2.json")
