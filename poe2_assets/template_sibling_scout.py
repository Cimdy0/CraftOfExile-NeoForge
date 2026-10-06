#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Template-level sibling scout (higher precision than constant-name matching).

`node_stat_map.json` carries a normalised `template` per stat, e.g.
    "{X}% increased Damage with Bows"
    "{X}% increased Damage with Crossbows"

Two stats whose templates differ by exactly ONE qualifier word are siblings:
the same underlying attribute+op, differing only by one scope tag. If the
narrower one is already wired, the other usually only needs the tag variant.

This is more precise than matching on constant names (sibling_scout.py) because
it compares the actual human-readable corpus text, which is what the wiring was
derived from in the first place.

Output is a CANDIDATE list with the sibling's verbatim wiring snippet, so every
pair can be hand-verified (R3). Nothing is written to Java sources.
"""
import os, re, json, glob, collections, argparse

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
SUP = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "codec", "support")


def read(p):
    with open(p, "r", encoding="utf-8") as f:
        return f.read()


statids = dict(re.findall(r'public static final Identifier (\w+) = id\("([^"]+)"\);',
                          read(os.path.join(SUP, "stat", "StatIds.java"))))


def extract(p):
    """map.put(StatIds.NAME, <body>) with paren-balanced body."""
    t = read(p)
    res = {}
    for m in re.finditer(r'map\.put\(\s*StatIds\.(\w+)\s*,', t):
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
        res[m.group(1)] = " ".join(t[m.end():j].split())
    return res


wire_files = sorted(glob.glob(os.path.join(GEN, "wirings", "*.java"))) + [os.path.join(GEN, "StatGen.java")]
wired = {}
for p in wire_files:
    for k, v in extract(p).items():
        wired.setdefault(k, (os.path.basename(p), v))
wired_keys = set(wired)
for p in wire_files:
    wired_keys.update(re.findall(r'StatIds\.(\w+)\s*,', read(p)))
unwired = [n for n in statids if n not in wired_keys]

# ---- corpus: constant -> templates / raws ----
nsm = json.load(open(os.path.join(ROOT, "poe2_assets", "node_stat_map.json"), encoding="utf-8"))
tmpl = collections.defaultdict(set)
raws = collections.defaultdict(set)
for _nid, es in nsm.items():
    for e in es:
        c = e.get("constant")
        if not c:
            continue
        if e.get("template"):
            tmpl[c].add(e["template"])
        if e.get("raw"):
            raws[c].add(e["raw"])


def words(t):
    # 去占位符与标点；保留方括号内的规范词（语料已把同义归一为 [X|Y]）
    t = re.sub(r"\{[^}]*\}", "", t)
    t = re.sub(r"[\[\]|]", " ", t)
    t = re.sub(r"[^A-Za-z ]", " ", t)
    return [w for w in t.split() if w]


ap = argparse.ArgumentParser()
ap.add_argument("--top", type=int, default=3)
ap.add_argument("--dump", default=None)
args = ap.parse_args()

# 已接线模板索引：按词集长度分桶，减少比较
wired_templates = []
for w in wired:
    for t in tmpl.get(w, ()):
        wired_templates.append((w, t, words(t)))

rows = []
for n in unwired:
    ts = tmpl.get(n)
    if not ts:
        continue
    for t in ts:
        tw = words(t)
        if not tw:
            continue
        best = []
        for (w, wt, ww) in wired_templates:
            if w == n or abs(len(ww) - len(tw)) != 1:
                continue
            # 差一个词：短的一方是长的一方去掉某词后的子序列
            lo, hi = (ww, tw) if len(ww) < len(tw) else (tw, ww)
            # 逐位对齐找单插入
            i = j = 0
            diffs = 0
            extra = None
            while i < len(lo) and j < len(hi):
                if lo[i] == hi[j]:
                    i += 1
                    j += 1
                else:
                    diffs += 1
                    extra = hi[j]
                    j += 1
                    if diffs > 1:
                        break
            if i < len(lo) or j < len(hi):
                diffs += 1
                extra = hi[j] if j < len(hi) else None
            if diffs == 1:
                best.append((extra, w, wt))
        if best:
            # 同一 extra 去重
            seen = set()
            uniq = []
            for extra, w, wt in best:
                if w in seen:
                    continue
                seen.add(w)
                uniq.append((extra, w, wt))
            rows.append((n, t, uniq[: args.top]))

print(f"wired={len(wired_keys)}  unwired={len(unwired)}")
print(f"unwired stats whose template is one word away from a wired template: {len(rows)}\n")

out = []
for n, t, best in rows:
    print(f"### {n}")
    print(f"    corpus: {t}")
    for extra, w, wt in best:
        f, snip = wired[w]
        print(f"    ~ {w}   (diff word: {extra!r})")
        print(f"        sibling corpus: {wt}")
        print(f"        [{f}] {snip[:220]}")
    out.append({"name": n, "template": t,
                "siblings": [{"name": w, "diff": extra, "template": wt,
                              "file": wired[w][0], "snippet": wired[w][1]} for extra, w, wt in best]})

if args.dump:
    json.dump(out, open(args.dump, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    print(f"\nwrote {args.dump}")
