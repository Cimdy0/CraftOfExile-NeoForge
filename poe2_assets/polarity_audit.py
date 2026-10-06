#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""POLARITY AUDIT: does the corpus->StatId pipeline preserve increased/reduced direction?

Evidence found: node_stat_map.json gives value=+0.5 for "50% less Life Recovery"
and value=+0.2 for "20% reduced Life Regeneration rate" -> magnitude only, sign lost.
If a reduced/less wording shares a StatId with increased/more, wiring it to the same
EFF produces an INVERTED effect (a penalty becomes a bonus).

Quantify: how many stats are affected, and how many are already wired (= live bugs).
"""
import os, re, json
ROOT = r"E:/McModding/Modding/CraftOfExile-NeoForge"
A = os.path.join(ROOT, "poe2_assets")
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")

d = json.load(open(os.path.join(A, "node_stat_map.json"), encoding="utf-8"))
# constant -> set of (raw, value, polarity)
stat_rows = {}
for node, stats in d.items():
    for s in stats:
        c = s.get("constant")
        if not c: continue
        raw = s.get("raw", "")
        vals = s.get("values") or []
        v = vals[0].get("value") if vals else None
        low = raw.lower()
        neg = bool(re.search(r'\b(reduced|less|slower|lower|decreased|fewer)\b', low))
        pos = bool(re.search(r'\b(increased|more|faster|greater|higher|additional)\b', low))
        pol = "NEG" if neg and not pos else ("POS" if pos and not neg else ("MIXED" if (pos and neg) else "NONE"))
        stat_rows.setdefault(c, []).append((raw, v, pol))

sg = open(os.path.join(GEN, "StatGen.java"), encoding="utf-8").read()
wired = set(re.findall(r'map\.put\(StatIds\.(\w+),', sg))

conflict = {c: r for c, r in stat_rows.items()
            if any(p == "NEG" for _x, _v, p in r) and any(p == "POS" for _x, _v, p in r)}
neg_only = {c: r for c, r in stat_rows.items()
            if any(p == "NEG" for _x, _v, p in r) and not any(p == "POS" for _x, _v, p in r)}

def negval(c):
    return [(_r, _v) for _r, _v, p in stat_rows[c] if p == "NEG"]

print(f"corpus stats with a constant: {len(stat_rows)}")
print(f"\n=== POLARITY CONFLICT: same StatId carries BOTH increased/more AND reduced/less: {len(conflict)} ===")
wired_conf = sorted(c for c in conflict if c in wired)
unwired_conf = sorted(c for c in conflict if c not in wired)
print(f"    already WIRED (LIVE inverted-effect risk): {len(wired_conf)}")
print(f"    unwired (must resolve before wiring):      {len(unwired_conf)}")
print("\n  -- wired conflicts (top 25) --")
for c in wired_conf[:25]:
    ex = "; ".join(f"{r} [v={v}]" for r, v in negval(c)[:2])
    print(f"    {c:52} NEG: {ex}")
print("\n  -- unwired conflicts (top 15) --")
for c in unwired_conf[:15]:
    ex = "; ".join(f"{r} [v={v}]" for r, v in negval(c)[:2])
    print(f"    {c:52} NEG: {ex}")

print(f"\n=== NEGATIVE-ONLY StatIds (whole stat is a penalty): {len(neg_only)} ===")
print(f"    wired: {sum(1 for c in neg_only if c in wired)}")
for c in sorted(neg_only)[:10]:
    ex = "; ".join(f"{r} [v={v}]" for r, v in negval(c)[:2])
    print(f"    {c:52} {ex}")

json.dump({"conflict": {c: [{"raw": r, "value": v, "polarity": p} for r, v, p in rows]
                        for c, rows in conflict.items()},
           "wired_conflicts": wired_conf, "unwired_conflicts": unwired_conf,
           "neg_only": sorted(neg_only)},
          open(os.path.join(A, "polarity_audit.json"), "w"), indent=1)
print("\nwrote poe2_assets/polarity_audit.json")
