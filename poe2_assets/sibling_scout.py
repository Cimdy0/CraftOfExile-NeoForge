#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Sibling-completion scout.

Idea (high ROI / low risk): many unwired StatIds are *siblings* of already-wired
ones -- same template, differing by exactly one qualifier token
(e.g. INC_DAMAGE_WITH_BOWS vs INC_DAMAGE_WITH_CROSSBOWS).
A sibling is wireable with the SAME effect/op family, only the scope tag differs.

This script does NOT write any Java. It only emits candidate pairs with:
  * the sibling's existing wiring snippet (verbatim, from Wire*.java)
  * the corpus raw text of both sides (from node_stat_map.json)
so each pair can be verified by hand (R3: no batch guessing).

Usage: python poe2_assets/sibling_scout.py [--maxdiff N] [--top K] [--dump json]
"""
import os, re, json, glob, argparse, collections

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
SUP = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "codec", "support")


def read(p):
    with open(p, "r", encoding="utf-8") as f:
        return f.read()


# ---- StatIds vocabulary ----
sid_txt = read(os.path.join(SUP, "stat", "StatIds.java"))
statids = dict(re.findall(r'public static final Identifier (\w+) = id\("([^"]+)"\);', sid_txt))

# ---- wired set + verbatim wiring snippets ----
wire_files = sorted(glob.glob(os.path.join(GEN, "wirings", "*.java"))) + [os.path.join(GEN, "StatGen.java")]


def extract(p):
    """Extract map.put(StatIds.NAME, <body>) pairs with paren-balanced body.

    NOTE: wirings use several terminators -- `.build());`, `self(...));`,
    `onTarget(...));` -- so a `.build()`-anchored regex misses most of them.
    Walk the parens instead.
    """
    t = read(p)
    res = {}
    for m in re.finditer(r'map\.put\(\s*StatIds\.(\w+)\s*,', t):
        name = m.group(1)
        i = m.start() + len("map.put")
        if i >= len(t) or t[i] != '(':
            continue
        depth, j = 0, i
        while j < len(t):
            if t[j] == '(':
                depth += 1
            elif t[j] == ')':
                depth -= 1
                if depth == 0:
                    break
            j += 1
        body = t[m.end():j]
        res[name] = " ".join(body.split())
    return res


wired = {}
for p in wire_files:
    for name, body in extract(p).items():
        wired.setdefault(name, (os.path.basename(p), body))

# Wired KEYS: any `StatIds.X,` occurrence (matches classify_unwired.py口径 so the
# coverage number stays comparable across batches). Some wirings are written via
# helper factories without a literal `map.put(`.
wired_keys = set(wired)
for p in wire_files:
    wired_keys.update(re.findall(r'StatIds\.(\w+)\s*,', read(p)))

unwired = [n for n in statids if n not in wired_keys]

# ---- corpus raw text per constant ----
nsm_path = os.path.join(ROOT, "poe2_assets", "node_stat_map.json")
corpus = {}
if os.path.isfile(nsm_path):
    nsm = json.load(open(nsm_path, encoding="utf-8"))
    for _nid, entries in nsm.items():
        for e in entries:
            c = e.get("constant")
            raw = e.get("raw")
            if c and raw:
                corpus.setdefault(c, set()).add(raw)

# ---- token index over wired names ----
def toks(n):
    return [t for t in n.split("_") if t]


idx = collections.defaultdict(list)
for w in wired:
    for t_ in set(toks(w)):
        idx[t_].append(w)


def candidates(n):
    cnt = collections.Counter()
    for t_ in set(toks(n)):
        for w in idx.get(t_, ()):
            cnt[w] += 1
    return cnt


def symdiff(a, b):
    sa, sb = set(toks(a)), set(toks(b))
    return len(sa ^ sb)


ap = argparse.ArgumentParser()
ap.add_argument("--maxdiff", type=int, default=2)
ap.add_argument("--top", type=int, default=3)
ap.add_argument("--dump", default=None)
ap.add_argument("--minlen", type=int, default=2)
args = ap.parse_args()

pairs = []
for n in unwired:
    if len(toks(n)) < args.minlen:
        continue
    cand = candidates(n)
    scored = []
    for w, shared in cand.items():
        d = symdiff(n, w)
        if d <= args.maxdiff and w != n:
            scored.append((d, -shared, w))
    if not scored:
        continue
    scored.sort()
    best = scored[: args.top]
    pairs.append((n, best))

pairs.sort(key=lambda x: (x[1][0][0], x[0]))

print(f"StatIds={len(statids)}  wired={len(wired)}  unwired={len(unwired)}")
print(f"sibling candidates (symdiff<={args.maxdiff}): {len(pairs)}\n")

bydiff = collections.Counter(p[1][0][0] for p in pairs)
print("by symdiff:", dict(sorted(bydiff.items())), "\n")

out = []
for n, best in pairs:
    craw = sorted(corpus.get(n, []))[:2]
    print(f"### {n}")
    if craw:
        for c in craw:
            print(f"    corpus: {c}")
    for d, _neg, w in best:
        wraw = sorted(corpus.get(w, []))[:1]
        print(f"    ~{w}  (symdiff={d})")
        if wraw:
            print(f"        corpus: {wraw[0]}")
        f, snippet = wired[w]
        print(f"        [{f}] {snippet[:300]}")
    out.append({"name": n, "corpus": sorted(corpus.get(n, [])),
                "siblings": [{"name": w, "symdiff": d, "file": wired[w][0],
                              "snippet": wired[w][1],
                              "corpus": sorted(corpus.get(w, []))[:2]} for d, _n, w in best]})

if args.dump:
    json.dump(out, open(args.dump, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    print(f"\nwrote {args.dump}")
