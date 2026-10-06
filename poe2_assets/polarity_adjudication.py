#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Produce an adjudication table for the lost-polarity defect (task #333).

Corpus normalisation maps reduced->increased, less->more, remove->add, lose->gain
but never flips the sign, so values are always magnitudes. Fixing this by globally
negating would be WRONG because of double-negative phrasings such as
"30% reduced penalty to Accuracy Rating" (a *reduced penalty* is a BENEFIT).

This script classifies every corpus row as POS / NEG / AMBIGUOUS and emits:
  - the safe NEG list (can be negated mechanically)
  - the AMBIGUOUS list (requires human adjudication)
Analysis only -- it does not rewrite any data.
"""
import os, re, json, collections

ROOT = r"E:/McModding/Modding/CraftOfExile-NeoForge"
A = os.path.join(ROOT, "poe2_assets")

NEG_WORDS = r"(?:reduced|reduce|reduces|less|slower|lower|decreased|fewer|removed|removes|lose|loses|lost)"
POS_WORDS = r"(?:increased|increase|increases|more|faster|greater|higher|additional|gained|gain|adds|added)"

# Phrasings where a negative word actually yields a BENEFIT (or is otherwise unsafe
# to negate mechanically). These must be adjudicated by a human.
DOUBLE_NEG = [
    (r"reduced\s+penalty",            "reduced penalty = the penalty shrinks = a benefit"),
    (r"less\s+penalty",               "less penalty = a benefit"),
    (r"reduced\s+(?:.*\s)?cost",      "reduced cost = a benefit"),
    (r"less\s+(?:.*\s)?cost",         "less cost = a benefit"),
    (r"reduced\s+.*\s+taken",         "reduced X taken = a benefit"),
    (r"less\s+.*\s+taken",            "less X taken = a benefit"),
    (r"reduced\s+(?:loss|drain)",     "reduced loss = a benefit"),
    (r"slower\s+(?:expire|expires)",  "slower expiry = a benefit"),
    (r"reduced\s+.*\s+delay",         "reduced delay = usually a benefit"),
]


def classify(raw: str):
    low = raw.lower()
    has_neg = re.search(r"\b" + NEG_WORDS + r"\b", low) is not None
    has_pos = re.search(r"\b" + POS_WORDS + r"\b", low) is not None
    for pat, why in DOUBLE_NEG:
        if re.search(pat, low):
            return "AMBIGUOUS", why
    if has_neg and has_pos:
        return "AMBIGUOUS", "both positive and negative wording present"
    if has_neg:
        return "NEG", ""
    if has_pos:
        return "POS", ""
    return "NONE", ""


d = json.load(open(os.path.join(A, "node_stat_map.json"), encoding="utf-8"))

# (constant, raw) -> aggregated polarity
rows = collections.OrderedDict()
for node, stats in d.items():
    for s in stats:
        c = s.get("constant")
        raw = s.get("raw", "")
        if not c or not raw:
            continue
        vals = s.get("values") or []
        v = vals[0].get("value") if vals else None
        pol, why = classify(raw)
        key = (c, raw)
        if key not in rows:
            rows[key] = {"constant": c, "raw": raw, "value": v, "polarity": pol, "why": why}
        elif rows[key]["polarity"] == "NONE" and pol != "NONE":
            rows[key]["polarity"] = pol
            rows[key]["why"] = why

cnt = collections.Counter(r["polarity"] for r in rows.values())
print(f"corpus rows analysed : {len(rows)}")
print(f"  POS       : {cnt['POS']}")
print(f"  NEG       : {cnt['NEG']}   <- mechanically negatable")
print(f"  AMBIGUOUS : {cnt['AMBIGUOUS']}   <- NEEDS HUMAN ADJUDICATION")
print(f"  NONE      : {cnt['NONE']}   (no polarity word, e.g. flat '+X to Y')")

# per-StatId conflict: rows of the same constant disagree
by_const = collections.defaultdict(list)
for r in rows.values():
    by_const[r["constant"]].append(r)
conflict = {c: v for c, v in by_const.items()
            if any(x["polarity"] == "NEG" for x in v) and any(x["polarity"] == "POS" for x in v)}
print(f"\nStatIds with genuine POS/NEG conflict (cannot be fixed by wiring): {len(conflict)}")

print("\n=== AMBIGUOUS rows that must NOT be auto-negated (first 25) ===")
amb = [r for r in rows.values() if r["polarity"] == "AMBIGUOUS"]
for r in amb[:25]:
    print(f"  {r['constant']}")
    print(f"      {r['raw'][:95]}")
    print(f"      reason: {r['why']}")

out = {
    "counts": dict(cnt),
    "neg_rows": [r for r in rows.values() if r["polarity"] == "NEG"],
    "ambiguous_rows": amb,
    "conflict_statids": sorted(conflict),
}
json.dump(out, open(os.path.join(A, "polarity_adjudication.json"), "w"), indent=1, ensure_ascii=False)
print(f"\nwrote poe2_assets/polarity_adjudication.json  (neg={len(out['neg_rows'])}, amb={len(amb)})")
