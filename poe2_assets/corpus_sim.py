#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Corpus-template similarity scout.

Instead of guessing from StatId NAMES (which are noisy: same concept gets
different spellings, different concepts get similar spellings), compare the
actual game-text TEMPLATE (numbers already normalised to {X}) of every unwired
stat against every wired stat. A high Jaccard overlap means the two lines are
near-identical in wording, so the wired one's EFF is a strong landing point.

Output is a SCOUT list only -- every hit still needs R3 manual verification.
"""
import os, re, json, collections

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
SUP = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "codec", "support")
WIR = os.path.join(GEN, "wirings")
def rd(p): return open(p, encoding="utf-8").read()

wired = {}
for f in [os.path.join(GEN, "StatGen.java")] + [os.path.join(WIR, x) for x in sorted(os.listdir(WIR))]:
    for m in re.finditer(r'map\.put\(StatIds\.(\w+),\s*(?:\w+)\((.*?)\)\);', rd(f), re.S):
        effs = re.findall(r'StatEffectIds\.(\w+)', m.group(2))
        cond = re.findall(r'StatConditionIds\.(\w+)', m.group(2))
        tgt = re.findall(r'StatTargetIds\.(\w+)', m.group(2))
        wired[m.group(1)] = dict(effs=effs, cond=cond[0] if cond else None, tgt=tgt[0] if tgt else None)

statids = set(re.findall(r'public static final Identifier (\w+) =', rd(os.path.join(SUP, "stat", "StatIds.java"))))
unwired = sorted(statids - set(wired))
print(f"wired={len(wired)}  unwired={len(unwired)}")

corpus = json.load(open(os.path.join(ROOT, "poe2_assets", "node_stat_map.json"), encoding="utf-8"))
tmpl = collections.defaultdict(set)
for node, stats in corpus.items():
    for s in stats:
        c, t = s.get("constant"), s.get("template")
        if c and t:
            tmpl[c].add(t)

def norm(t):
    t = re.sub(r'\[[^\]|]+(?:\|[^\]]+)?\]', lambda m: m.group(0).split('|')[0][1:], t)
    t = re.sub(r'[^a-z0-9{} ]', ' ', t.lower())
    return t.split()

def toks(c):
    out = set()
    for t in tmpl.get(c, ()):
        out |= set(norm(t))
    return out

wt = {w: toks(w) for w in wired if toks(w)}
STOP = {"a", "an", "the", "of", "to", "and", "or", "with", "for", "in", "on", "at", "is", "are", "you", "your", "x"}

hits = []
for u in unwired:
    ut = toks(u)
    if not ut:
        continue
    utc = ut - STOP
    best = (0.0, None)
    for w, wtk in wt.items():
        wc = wtk - STOP
        if not wc:
            continue
        inter = len(utc & wc)
        union = len(utc | wc)
        if not union:
            continue
        j = inter / union
        if j > best[0]:
            best = (j, w)
    if best[0] >= 0.75 and best[1]:
        hits.append((u, best[1], round(best[0], 2), wired[best[1]]))

hits.sort(key=lambda h: -h[2])
print(f"\n=== corpus-template similarity >= 0.75 : {len(hits)} ===")
for u, w, j, info in hits[:45]:
    src = sorted(tmpl.get(u, ()))[0][:80] if tmpl.get(u) else "<no corpus>"
    dst = sorted(tmpl.get(w, ()))[0][:80] if tmpl.get(w) else "<no corpus>"
    print(f"  J={j}  {u}")
    print(f"        unwired: {src}")
    print(f"        ~wired : {dst}")
    print(f"        eff={info['effs']} cond={info['cond']} tgt={info['tgt']}")

json.dump([{"sid": u, "like": w, "j": j, "effs": i["effs"], "cond": i["cond"], "tgt": i["tgt"],
            "src": sorted(tmpl.get(u, ()))[:2], "dst": sorted(tmpl.get(w, ()))[:2]} for u, w, j, i in hits],
          open(os.path.join(ROOT, "poe2_assets", "corpus_sim.json"), "w"), indent=1, ensure_ascii=False)
print("\nwrote poe2_assets/corpus_sim.json")
