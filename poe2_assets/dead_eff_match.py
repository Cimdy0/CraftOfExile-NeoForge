#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Dead-EFF matcher -- harvest already-registered effects that NO stat wiring uses.

Why this is the highest ROI / lowest risk remaining move:
  * 661 effects are registered in StatEffectGen.
  * An effect that is registered but never referenced by any Wire*.java is a
    CAPABILITY THAT ALREADY EXISTS and is simply unused.
  * Wiring an unwired StatId to it requires NO new attribute, NO new engine
    node, NO new condition -- zero new surface, so R3 risk is minimal.

The script parses:
  - StatEffectGen.java   -> every registered EFF (op, attribute, scope tags)
  - Wire*.java/StatGen   -> every EFF actually referenced
  - StatIds.java + wirings -> the unwired constant set
  - node_stat_map.json   -> corpus raw text for hand verification

and emits, for each dead EFF, candidate unwired StatIds whose name tokens cover
the EFF's semantic tokens. Everything is a CANDIDATE -- each pair must still be
verified by hand (R3).
"""
import os, re, json, glob, collections, argparse

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
SUP = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "codec", "support")


def read(p):
    with open(p, "r", encoding="utf-8") as f:
        return f.read()


# ---- StatIds ----
statids = dict(re.findall(r'public static final Identifier (\w+) = id\("([^"]+)"\);',
                          read(os.path.join(SUP, "stat", "StatIds.java"))))

# ---- wirings ----
wire_files = sorted(glob.glob(os.path.join(GEN, "wirings", "*.java"))) + [os.path.join(GEN, "StatGen.java")]
wire_txt = "\n".join(read(p) for p in wire_files)
wired_keys = set(re.findall(r'StatIds\.(\w+)\s*,', wire_txt))
unwired = [n for n in statids if n not in wired_keys]

# ---- registered effects ----
eg = read(os.path.join(GEN, "StatEffectGen.java"))
reg = {}
for m in re.finditer(r'm\.put\(StatEffectIds\.(\w+),\s*new\s+(\w+)\((.*?)\)\);', eg, re.DOTALL):
    name, cls, body = m.group(1), m.group(2), m.group(3)
    am = re.search(r'AttributeRegister\.(\w+)', body)
    opm = re.search(r'AttributeModifier\.Operation\.(\w+)', body)
    tags = ["ALWAYS"] if "TagExpr.ALWAYS" in body else re.findall(r'StatTag\.(\w+)', body)
    reg[name] = {"cls": cls, "attr": am.group(1) if am else None,
                 "op": opm.group(1) if opm else None, "tags": tags}

referenced = set(re.findall(r'StatEffectIds\.(\w+)', wire_txt))
dead = {n: v for n, v in reg.items() if n not in referenced}

# ---- corpus ----
corpus = {}
nsm_path = os.path.join(ROOT, "poe2_assets", "node_stat_map.json")
if os.path.isfile(nsm_path):
    for _nid, entries in json.load(open(nsm_path, encoding="utf-8")).items():
        for e in entries:
            if e.get("constant") and e.get("raw"):
                corpus.setdefault(e["constant"], set()).add(e["raw"])

OP_PREFIX = re.compile(r'^EFF_(?:ADD_MULTIPLIED_BASE|ADD_MULTIPLIED_TOTAL|ADD_VALUE|SET)_')
STOP = {"ATTRIBUTE", "COE", "MINECRAFT", "TRUE", "DAMAGE"}


def sem_tokens(eff):
    s = OP_PREFIX.sub("", eff)
    return set(t for t in s.split("_") if t and t not in STOP)


def toks(n):
    return set(t for t in n.split("_") if t)


ap = argparse.ArgumentParser()
ap.add_argument("--mincover", type=float, default=0.6, help="fraction of EFF semantic tokens the StatId must cover")
ap.add_argument("--dump", default=None)
ap.add_argument("--attr", default=None, help="only show dead EFFs on this attribute")
args = ap.parse_args()

print(f"registered effects={len(reg)}  referenced={len(referenced)}  DEAD(unreferenced)={len(dead)}")
print(f"StatIds={len(statids)}  wired={len(wired_keys)}  unwired={len(unwired)}\n")

# group dead by attribute
by_attr = collections.Counter(v["attr"] for v in dead.values())
print("dead effects by attribute (top 25):")
for a, c in by_attr.most_common(25):
    print(f"   {str(a):45} {c}")
print()

rows = []
for eff in sorted(dead):
    if args.attr and dead[eff]["attr"] != args.attr:
        continue
    st = sem_tokens(eff)
    if not st:
        continue
    hits = []
    for n in unwired:
        nt = toks(n)
        cover = len(st & nt) / len(st)
        if cover >= args.mincover:
            hits.append((round(cover, 2), n))
    hits.sort(key=lambda x: (-x[0], x[1]))
    if hits:
        rows.append((eff, dead[eff], hits[:5]))

print(f"dead effects with >=1 name-similar unwired StatId: {len(rows)}\n")
out = []
for eff, meta, hits in rows:
    print(f"### {eff}")
    print(f"    attr={meta['attr']}  op={meta['op']}  tags={meta['tags']}")
    for cover, n in hits:
        craw = sorted(corpus.get(n, []))[:2]
        print(f"    -> {n}  (cover={cover})")
        for c in craw:
            print(f"         corpus: {c}")
    out.append({"eff": eff, "attr": meta["attr"], "op": meta["op"], "tags": meta["tags"],
                "candidates": [{"name": n, "cover": cover, "corpus": sorted(corpus.get(n, []))} for cover, n in hits]})

if args.dump:
    json.dump(out, open(args.dump, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    print(f"\nwrote {args.dump}")
