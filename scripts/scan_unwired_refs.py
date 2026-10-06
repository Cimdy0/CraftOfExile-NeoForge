import os, re

ROOT = r"E:/McModding/Modding/CraftOfExile-NeoForge"
SRC = os.path.join(ROOT, "src", "main", "java")
EXCLUDE = re.compile(r"(StatIds\.java|[/\\]wirings[/\\]|StatEffectGen\.java|StatConditionGen\.java|StatEffectIds\.java|StatConditionIds\.java|[/\\]gen[/\\]stat[/\\])")

# parse unwired list -> (const, snake)
cands = []
with open(os.path.join(ROOT, "unwired_true.txt"), encoding="utf-8") as f:
    for line in f:
        p = line.rstrip("\n").split("\t")
        if len(p) >= 2:
            cands.append((p[0], p[1]))

# build per-candidate regex
pat = {c: re.compile("StatIds\\." + re.escape(c[0]) + r"\b|\"" + re.escape(c[1]) + r"\"") for c in cands}

hits = {c: [] for c in cands}
for dp, _, files in os.walk(SRC):
    if EXCLUDE.search(dp):
        continue
    for fn in files:
        if not fn.endswith(".java"):
            continue
        fp = os.path.join(dp, fn)
        if EXCLUDE.search(fp):
            continue
        try:
            with open(fp, encoding="utf-8", errors="ignore") as fh:
                for i, ln in enumerate(fh, 1):
                    for c in cands:
                        if pat[c].search(ln):
                            hits[c].append(f"{fp}:{i}: {ln.strip()[:150]}")
        except Exception:
            pass

found = [(c, h) for c, h in hits.items() if h]
found.sort(key=lambda x: -len(x[1]))
print(f"UNWIRED with REAL code references: {len(found)}/{len(cands)}")
for c, h in found:
    print(f"\n### {c[0]}  ({len(h)} refs)")
    for r in h[:6]:
        print("   " + r)
