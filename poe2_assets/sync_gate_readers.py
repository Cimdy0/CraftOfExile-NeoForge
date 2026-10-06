#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Point the gate scripts at the partitioned wirings.

After the split, StatGen.java no longer contains `map.put(StatIds....)` lines — the
881 wirings live in `stat/wirings/Wire*.java`. Any script that reads StatGen.java to
parse wirings would now silently see ZERO wirings, which would make the gate report
a false PASS. This rewrites those readers to concatenate StatGen.java with the
wirings directory.
"""
import os, re

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"

SNIPPET = '''

def _statgen_sources():
    """StatGen.java plus every wirings/Wire*.java partition (wirings were split out
    of StatGen, so parsing only StatGen.java would silently yield zero wirings)."""
    base = os.path.join(GEN, "StatGen.java")
    parts = [read(base)]
    wdir = os.path.join(GEN, "wirings")
    if os.path.isdir(wdir):
        for f in sorted(os.listdir(wdir)):
            if f.startswith("Wire") and f.endswith(".java"):
                parts.append(read(os.path.join(wdir, f)))
    return "\\n".join(parts)
'''

OLD = 'sg = read(os.path.join(GEN, "StatGen.java"))'
NEW = 'sg = _statgen_sources()'

FILES = [
    os.path.join(ROOT, "scripts", "verify_gen.py"),
    os.path.join(ROOT, "poe2_assets", "audit_opfamily.py"),
    os.path.join(ROOT, "poe2_assets", "audit_session.py"),
]

for p in FILES:
    src = open(p, encoding="utf-8").read()
    if OLD not in src:
        print(f"  skip (pattern absent): {os.path.basename(p)}")
        continue
    if "_statgen_sources" in src:
        print(f"  already patched: {os.path.basename(p)}")
        continue
    src = src.replace(OLD, NEW, 1)
    # inject the helper right after GEN is defined
    m = re.search(r'^GEN\s*=.*$', src, re.M)
    if not m:
        print(f"  ABORT (no GEN definition): {os.path.basename(p)}")
        continue
    src = src[:m.end()] + SNIPPET + src[m.end():]
    open(p, "w", encoding="utf-8").write(src)
    print(f"  patched: {os.path.basename(p)}")
print("done")
