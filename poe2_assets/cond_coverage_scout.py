#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Reverse COND coverage scout.

Forward matching (unwired StatId -> find an EFF) plateaus because the EFF space
is already well covered. This script flips it around:

  for every COND that already exists, which UNWIRED StatIds *look like they need
  exactly this condition*?

A hit means: the condition is available, so the only thing missing is the
subject EFF -- and if the subject already has a wired sibling, the wiring is
essentially free.

Matching is deliberately coarse (token overlap between the COND name and the
StatId name) -- output is a CANDIDATE list for hand verification (R3), never a
batch wiring instruction.
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
wire_files = sorted(glob.glob(os.path.join(GEN, "wirings", "*.java"))) + [os.path.join(GEN, "StatGen.java")]
wire_txt = "\n".join(read(p) for p in wire_files)
wired = set(re.findall(r'StatIds\.(\w+)\s*,', wire_txt))
unwired = [n for n in statids if n not in wired]

conds = re.findall(r'public static final Identifier COND_(\w+) = id\("([^"]+)"\);',
                   read(os.path.join(GEN, "StatConditionIds.java")))

corpus = {}
nsm = os.path.join(ROOT, "poe2_assets", "node_stat_map.json")
if os.path.isfile(nsm):
    for _nid, es in json.load(open(nsm, encoding="utf-8")).items():
        for e in es:
            if e.get("constant") and e.get("raw"):
                corpus.setdefault(e["constant"], set()).add(e["raw"])

# COND 常量名 -> 语义 token（去掉无区分度的骨架词）
SKELETON = {"COND", "COUNT", "RECENTLY", "LIVING", "STATE", "STATUS", "HAS", "IS", "OF", "THE"}
STOP_TOK = {"AND", "OR", "WITH", "ALL", "ANY"}


def cond_tokens(const):
    ts = [t for t in const.split("_") if t and t not in SKELETON and t not in STOP_TOK]
    return set(ts)


ap = argparse.ArgumentParser()
ap.add_argument("--minhits", type=int, default=1, help="minimum shared semantic tokens")
ap.add_argument("--dump", default=None)
args = ap.parse_args()

rows = []
for const, path in conds:
    ct = cond_tokens(const)
    if not ct:
        continue
    hits = []
    for n in unwired:
        nt = set(t for t in n.split("_") if t)
        shared = ct & nt
        if len(shared) >= args.minhits and len(shared) >= max(1, len(ct) // 2):
            hits.append((len(shared), n))
    if hits:
        hits.sort(key=lambda x: (-x[0], x[1]))
        rows.append((const, path, ct, hits))

rows.sort(key=lambda r: -len(r[3]))
print(f"COND={len(conds)}  wired={len(wired)}  unwired={len(unwired)}")
print(f"conditions with >=1 unwired candidate: {len(rows)}\n")

out = []
for const, path, ct, hits in rows:
    print(f"### COND_{const}   ({path})  tokens={{{','.join(sorted(ct))}}}  candidates={len(hits)}")
    for sh, n in hits[:6]:
        c = sorted(corpus.get(n, []))[:1]
        print(f"    {n}" + (f"\n        {c[0]}" if c else ""))
    out.append({"cond": "COND_" + const, "path": path, "tokens": sorted(ct),
                "candidates": [{"name": n, "shared": sh, "corpus": sorted(corpus.get(n, []))[:2]} for sh, n in hits]})

if args.dump:
    json.dump(out, open(args.dump, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    print(f"\nwrote {args.dump}")
