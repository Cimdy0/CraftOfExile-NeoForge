#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Refine the 207 "wireable now" scout candidates into action classes.

The scout only checks that SOME (attr, tags, op) variant exists. Two ways that can
still be wrong:
  1. SCOPE MISMATCH -- the stat name carries a scope qualifier (e.g. BALLISTA, BOW,
     MINION) that the sibling's EFF tags do NOT contain; reusing it would silently
     drop the scope (the exact failure documented in GLOBAL_PLAN 5.2).
  2. UNRESOLVED CONDITION -- the name has a conditional clause with no known COND.

Only SAFE / COND_OK are emitted as ready-to-paste wiring lines; everything else is
reported for manual adjudication.
"""
import os, re, json, collections

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
A = os.path.join(ROOT, "poe2_assets")
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")

STATTAGS = set(re.findall(r"public static final String ([A-Z0-9_]+)",
                          open(os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe",
                                            "data", "codec", "support", "stat", "StatTag.java"),
                               encoding="utf-8").read()))

ready = json.load(open(os.path.join(A, "wireable_now.json"), encoding="utf-8"))["ready"]
suffix_map = json.load(open(os.path.join(A, "suffix_frontier.json"), encoding="utf-8"))["suffix_map"]

# condition clause markers
COND_MARKERS = ("_WHILE_", "_IF_", "_WHEN_", "_AGAINST_", "_VS_", "_RECENTLY", "_ON_",
                "_DURING_", "_PER_", "_FOR_EACH", "_HAVE_", "_BEEN_")


def has_cond(sid):
    return any(m in sid for m in COND_MARKERS) or sid.endswith(("_RECENTLY", "_SHAPESHIFTED", "_MOVING",
                                                               "_IGNITED", "_CHILLED", "_SHOCKED", "_STATIONARY"))


def tag_for_token(t):
    """Map a stat-name token to a StatTag, handling plurals (BOWS->BOW, AXES->AXE).

    Without this, scope qualifiers written in the plural silently escape detection
    and a scoped stat would be wired to an untagged ALWAYS effect -- losing its scope.
    """
    if t in STATTAGS:
        return t
    n = t[:-1] if (t.endswith("S") and len(t) > 2) else t
    if n in STATTAGS:
        return n
    if n + "E" in STATTAGS:          # AX -> AXE
        return n + "E"
    return None


def scope_tokens(sid):
    """Tokens that look like a scope qualifier in a stat name."""
    out = []
    m = re.search(r"_WITH_([A-Z0-9_]+)", sid)
    if m:
        out += m.group(1).split("_")
    m = re.search(r"_OF_([A-Z0-9_]+)_SKILLS", sid)
    if m:
        out += m.group(1).split("_")
    return out


safe, cond_ok, scope_bad, cond_unknown, unknown_scope = [], [], [], [], []

for r in ready:
    sid = r["sid"]
    tags = set(r["tags"])
    attr_tokens = set(r["attr"].split("_"))
    sid_tokens = set(sid.split("_"))
    # (a) qualifiers that ARE tags but missing from the EFF -> silent scope loss
    unmatched = sorted({tg for tk in sid_tokens
                        if (tg := tag_for_token(tk)) and tg not in tags and tg not in attr_tokens})
    # (b) qualifiers that match NO tag at all -> no scoped EFF variant can express them
    unknown = sorted({tk for tk in scope_tokens(sid) if tag_for_token(tk) is None})
    if unmatched or unknown:
        r["unmatched_scope"] = unmatched
        r["unknown_scope"] = unknown
        (scope_bad if unmatched else unknown_scope).append(r)
        continue
    if not has_cond(sid):
        safe.append(r)
        continue
    # try to resolve the conditional suffix against the learned map
    hit = None
    for s, v in suffix_map.items():
        if sid.endswith("_" + s):
            hit = (s, v); break
    if hit:
        s, v = hit
        r["suffix"], r["cond"], r["tgt"] = s, v["cond"], v["tgt"]
        cond_ok.append(r)
    else:
        cond_unknown.append(r)

print(f"candidates examined : {len(ready)}")
print(f"  SAFE      (no condition, scope matches): {len(safe)}")
print(f"  COND_OK   (condition resolved via learned suffix): {len(cond_ok)}")
print(f"  SCOPE_BAD (scope qualifier missing from EFF tags -> DO NOT WIRE): {len(scope_bad)}")
print(f"  UNKNOWN_SCOPE (qualifier matches no StatTag -> no variant can express it): {len(unknown_scope)}")
print(f"  COND_UNKNOWN (needs manual COND mapping): {len(cond_unknown)}")

print("\n=== SCOPE_BAD examples (why blind reuse is dangerous) ===")
for r in scope_bad[:12]:
    print(f"  {r['sid']:56} missing={r['unmatched_scope']}  (sibling {r['sibling']} tags={r['tags']})")

print("\n=== SAFE: ready-to-paste wiring lines ===")
lines = []
for r in sorted(safe, key=lambda x: x["sid"]):
    ln = f'        map.put(StatIds.{r["sid"]}, self(StatEffectIds.{r["eff"]}));'
    lines.append(ln)
    print(ln)

print("\n=== COND_OK: ready-to-paste wiring lines ===")
for r in sorted(cond_ok, key=lambda x: x["sid"]):
    if r["tgt"] and r["tgt"] != "None":
        ln = f'        map.put(StatIds.{r["sid"]}, stat(StatConditionIds.{r["cond"]}, StatTargetIds.{r["tgt"]}, StatEffectIds.{r["eff"]}));'
    else:
        ln = f'        map.put(StatIds.{r["sid"]}, self(StatConditionIds.{r["cond"]}, StatEffectIds.{r["eff"]}));'
    lines.append(ln)
    print(ln)

print(f"\n=== COND_UNKNOWN samples ({len(cond_unknown)}) ===")
for r in cond_unknown[:12]:
    print(f"  {r['sid']}")

json.dump({"safe": safe, "cond_ok": cond_ok, "scope_bad": scope_bad, "unknown_scope": unknown_scope, "cond_unknown": cond_unknown,
           "lines": lines},
          open(os.path.join(A, "refined_candidates.json"), "w"), indent=1)
print(f"\nwrote poe2_assets/refined_candidates.json  ({len(lines)} paste-ready lines)")
