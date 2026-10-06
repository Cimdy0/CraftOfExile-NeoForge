#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""For each buildable COND primitive, find the unwired stats that actually need it.

Demand-driven: only build a COND if unwired stats genuinely reference that state.
Output is a SCOUT list -- every hit still needs manual R3 verification before wiring.
"""
import os, re, json

ROOT = r"E:/McModding/Modding/CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
SUP = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "codec", "support")
WIR = os.path.join(GEN, "wirings")
def rd(p): return open(p, encoding="utf-8").read()

# unwired stats
statids = set(re.findall(r'public static final Identifier (\w+) =',
                         rd(os.path.join(SUP, "stat", "StatIds.java"))))
wired = set()
for f in [os.path.join(GEN, "StatGen.java")] + [os.path.join(WIR, x) for x in os.listdir(WIR)]:
    for m in re.finditer(r'map\.put\(StatIds\.(\w+),', rd(f)): wired.add(m.group(1))
unwired = sorted(statids - wired)

# state -> token(s) that would appear in a stat name
KEYMAP = {
    "AGGRAVATED": ["AGGRAVATED"],
    "ARMOUR_BREAK": ["ARMOUR_BREAK", "ARMOUR_BROKEN"],
    "ARMOUR_BROKEN_FULLY": ["FULLY_ARMOUR_BROKEN", "ARMOUR_BROKEN_FULLY"],
    "CORRUPTED_BLOOD": ["CORRUPTED_BLOOD"],
    "CRITICAL_WEAKNESS": ["CRITICAL_WEAKNESS"],
    "DEBILITATE": ["DEBILITATED"],
    "IMPALE": ["IMPALED"],
    "INCISION": ["INCISION"],
    "INTIMIDATE": ["INTIMIDATED"],
    "PARRIED": ["PARRIED", "PARRYING"],
    "PETRIFY": ["PETRIFIED"],
    "PRESENCE_MARK": ["MARKED"],
    "TAUNT": ["TAUNTED"],
    "WITHER": ["WITHERED"],
    "DODGE_ROLLING": ["DODGE_ROLL", "DODGE_ROLLING"],
    "LOW_MANA": ["LOW_MANA"],
    "NOT_FULL_LIFE": ["NOT_FULL_LIFE", "NOT_ON_FULL_LIFE"],
}

hits = {}
for state, toks in KEYMAP.items():
    got = [u for u in unwired if any(t in u for t in toks)]
    if got: hits[state] = got

print(f"unwired total: {len(unwired)}\n")
print("=== COND candidates WITH demand (scout list, needs R3 verification) ===")
tot = 0
for state in sorted(hits, key=lambda s: -len(hits[s])):
    v = hits[state]
    tot += len(v)
    print(f"\n  {state}  -> {len(v)} stat(s)")
    for s in v: print(f"      {s}")
print(f"\ntotal demand: {tot} stats across {len(hits)} COND primitives")

no_demand = [s for s in KEYMAP if s not in hits]
print(f"\nno demand (do NOT build): {no_demand}")

json.dump({"demand": hits, "no_demand": no_demand},
          open(os.path.join(ROOT, "poe2_assets", "cond_demand.json"), "w"), indent=1)
print("wrote poe2_assets/cond_demand.json")
