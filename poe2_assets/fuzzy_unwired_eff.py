#!/usr/bin/env python3
"""Fuzzy-match unwired StatIds to existing EFF identifiers in StatEffectIds.java.

Goal: surface "missed" pure-data wirings where an unwired StatId maps to an
ALREADY-REGISTERED effect (engine-true-read) but was never wired due to naming
mismatch. We do NOT invent new effects; we only surface candidates for manual
verification.
"""
import re, os, subprocess

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
STATIDS = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java")
EFFIDS = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatEffectIds.java")
WIRINGS = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/wirings")

def read(p):
    with open(p, encoding="utf-8") as f:
        return f.read()

def all_statids(src):
    return set(re.findall(r"public static final Identifier ([A-Z0-9_]+) = id\(", src))

def wired_statids():
    out = set()
    r = subprocess.run(["grep", "-rhoE", r"StatIds\.[A-Z0-9_]+", WIRINGS],
                      capture_output=True, text=True)
    for tok in r.stdout.split():
        if tok.startswith("StatIds."):
            out.add(tok[len("StatIds."):])
    return out

def all_effs(src):
    # EFF_..._COE_... = id(...)  -> capture constant name
    return re.findall(r"public static final Identifier (EFF_[A-Z0-9_]+) = id\(", src)

def tokens(name):
    # split on _ and drop generic prefixes
    parts = [p for p in name.split("_") if p]
    return parts

stat_src = read(STATIDS)
eff_src = read(EFFIDS)
all_ids = all_statids(stat_src)
wired = wired_statids()
unwired = sorted(all_ids - wired)

eff_consts = all_effs(eff_src)
eff_set = set(eff_consts)

# Build a keyword->eff index: for each eff, its token multiset
eff_tokens = {}
for e in eff_consts:
    eff_tokens[e] = tokens(e)

# Stop-words that are too generic to match on
STOP = {"INC","TO","ADD","MORE","GREATER","OF","THE","A","AN","AND","OR","WITH","FOR",
        "AGAINST","WHILE","WHEN","IF","YOU","YOUR","ENEMIES","ENEMY","RECENTLY","ON",
        "IN","BY","FROM","HAVE","HAS","TAKE","TAKEN","FROM","PER","SECONDS","CHANCE",
        "DAMAGE","STAT","EFF"}

def interesting_tokens(name):
    return [t for t in tokens(name) if t not in STOP and len(t) > 2]

results = []
for sid in unwired:
    it = interesting_tokens(sid)
    if not it:
        continue
    # require at least 2 interesting tokens shared with an eff (to avoid noise)
    best = []
    for e, et in eff_tokens.items():
        shared = set(it) & set(et)
        if len(shared) >= 2:
            best.append((len(shared), e, sorted(shared)))
    best.sort(reverse=True)
    if best:
        results.append((sid, best[:3]))

print(f"unwired total: {len(unwired)}")
print(f"eff total: {len(eff_set)}")
print(f"candidates (>=2 shared tokens): {len(results)}")
print("="*80)
for sid, best in results[:120]:
    print(f"\n{sid}")
    for score, e, shared in best:
        print(f"    [{score}] {e}  shared={shared}")
