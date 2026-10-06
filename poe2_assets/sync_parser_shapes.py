#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Teach the parser scripts about the new factory call shapes.

StatGen wirings now use self(eff) / self(cond, eff) / onTarget(cond, eff) instead of
always spelling out stat(...). Any script that parses wirings must accept all shapes,
otherwise the GATE scripts silently see zero wirings and report bogus results.
"""
import os, re, sys

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"

TARGETS = [
    os.path.join(ROOT, "scripts", "verify_gen.py"),
    os.path.join(ROOT, "poe2_assets", "audit_opfamily.py"),
    os.path.join(ROOT, "poe2_assets", "audit_session.py"),
    os.path.join(ROOT, "poe2_assets", "checkb_fixpoints.py"),
    os.path.join(ROOT, "poe2_assets", "checkb_zerobase_split.py"),
    os.path.join(ROOT, "poe2_assets", "dead_eff_scan.py"),
    os.path.join(ROOT, "poe2_assets", "gen_talent_entries.py"),
    os.path.join(ROOT, "poe2_assets", "gen_talent_entries_v3.py"),
    os.path.join(ROOT, "poe2_assets", "harvest_conditions.py"),
    os.path.join(ROOT, "poe2_assets", "opgap_demand.py"),
    os.path.join(ROOT, "poe2_assets", "classify_unwired.py"),
]

# `stat\(` used as a REGEX token inside these python files -> widen to all shapes
PAT_OLD = re.compile(r'(\\s\*)stat\\\(')
PAT_NEW = r'\1(?:stat|self|onTarget)\\('

total = 0
for p in TARGETS:
    if not os.path.isfile(p):
        print(f"  skip (missing): {os.path.basename(p)}")
        continue
    src = open(p, encoding="utf-8").read()
    new, n = PAT_OLD.subn(PAT_NEW, src)
    if n:
        open(p, "w", encoding="utf-8").write(new)
        print(f"  {os.path.basename(p):32} {n} pattern(s) widened")
        total += n
    else:
        print(f"  {os.path.basename(p):32} (no match)")
print(f"\ntotal patterns widened: {total}")
