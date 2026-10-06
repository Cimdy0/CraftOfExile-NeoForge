#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Find COND primitives that can be built as PURE DATA from already-registered
StatusType / LivingStateModes (low risk, zero engine change), then count how many
unwired stats each would unlock.

Per skill §Phase4续: adding a COND primitive only needs a constant + one m.put(...)
in StatConditionGen.contents() — no engine work — PROVIDED the backing StatusType or
LivingStateMode is already registered.
"""
import os, re, json, collections

ROOT = r"E:/McModding/Modding/CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
SUP = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "codec", "support")
WIR = os.path.join(GEN, "wirings")

def rd(p): return open(p, encoding="utf-8").read()

# ---- exposed COND constants (52) ----
exposed = set(re.findall(r'public static final Identifier (COND_\w+)', rd(os.path.join(GEN, "StatConditionIds.java"))))

# ---- registered StatusType constants ----
status = {}
for f in ["status/registry/ailment/AilmentStatusTypes.java",
          "status/registry/debuff/DebuffStatusTypes.java"]:
    p = os.path.join(SUP, f)
    if os.path.isfile(p):
        for m in re.finditer(r'public static final \w+ (\w+) =', rd(p)):
            status[m.group(1)] = os.path.basename(f)
# ---- registered LivingStateModes ----
modes = {}
p = os.path.join(SUP, "math", "context", "nodes", "LivingStateModes.java")
if os.path.isfile(p):
    for m in re.finditer(r'public static final \w+ (\w+) =', rd(p)):
        modes[m.group(1)] = True

# ---- which are already exposed? infer from COND names ----
def covered(name):
    up = name.upper()
    return any(up in c for c in exposed)

missing_status = sorted(n for n in status if not covered(n))
missing_modes  = sorted(n for n in modes if not covered(n))

print(f"exposed COND primitives : {len(exposed)}")
print(f"registered StatusTypes  : {len(status)}   not exposed as COND: {len(missing_status)}")
print(f"registered LivingStateModes: {len(modes)}  not exposed as COND: {len(missing_modes)}")
print()
print("=== StatusTypes without a COND primitive (buildable as pure data) ===")
for n in missing_status: print(f"    {n}")
print()
print("=== LivingStateModes without a COND primitive ===")
for n in missing_modes: print(f"    {n}")

json.dump({"exposed": sorted(exposed), "status_all": sorted(status),
           "modes_all": sorted(modes),
           "missing_status": missing_status, "missing_modes": missing_modes},
          open(os.path.join(ROOT, "poe2_assets", "cond_frontier.json"), "w"), indent=1)
print("\nwrote poe2_assets/cond_frontier.json")
