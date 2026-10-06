#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Wire the Phase-13 condition-variant candidates (learned clause -> COND).

All 12 reuse an EXISTING cond + EXISTING eff (pure mirrors of their wired base),
so effect/condition counts stay unchanged; only entry grows.
"""
import os, re, json, collections

ROOT = r"E:/McModding/Modding/CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
WIR = os.path.join(GEN, "wirings")
def rd(p): return open(p, encoding="utf-8").read()

conds = set(re.findall(r'public static final Identifier (COND_\w+)', rd(os.path.join(GEN, "StatConditionIds.java"))))
effs = set(re.findall(r'public static final Identifier (EFF_\w+)', rd(os.path.join(GEN, "StatEffectIds.java"))))
eff_attr = {}
for m in re.finditer(r'm\.put\(StatEffectIds\.(\w+),\s*new ModifierStatEffect\((.*?)\)\);', rd(os.path.join(GEN, "StatEffectGen.java")), re.S):
    am = re.search(r'(?:AttributeRegister|Attributes)\.(\w+)', m.group(2))
    if am: eff_attr[m.group(1)] = am.group(1)

DOMAINS = {
 "WireDamage": {"DAMAGE","ATTACK_DAMAGE","CRIT_CHANCE","CRIT_DAMAGE","TAKEN_DAMAGE","TAKEN_CRIT_DAMAGE","TAKEN_CRIT_CHANCE","DAMAGE_OVER_TIME_MULTIPLIER","DAMAGE_TAKEN_BYPASSES_ENERGY"},
 "WireAilment":{"AILMENT_MAGNITUDE","AILMENT_DURATION","AILMENT_BUILDUP","AILMENT_CHANCE","AILMENT_THRESHOLD","AILMENT_THRESHOLD_ADD","AILMENT_THRESHOLD_ADAPTATION","AILMENT_DAMAGE_RATE","AILMENT_DAMAGE_CONTRIBUTION"},
 "WireDefense":{"ARMOR","EVASION","RESISTANCES","MAX_RESISTANCES","RESISTANCE_PENETRATION","BLOCK_CHANCE","MAX_BLOCK","DEFLECTION_RATING","ARMOR_BREAK","AVOID_AILMENT_CHANCE","DODGE_CHANCE","STUN_RECOVERY"},
 "WireSpeed":  {"CAST_SPEED","ATTACK_SPEED","MOVEMENT_SPEED","CROSS_BOW_RELOAD_TIME","COOLDOWN_RECOVERY_RATE"},
 "WireResource":{"MAX_REGENERATION","REGENERATION_RATE","RECOVERY_RATE","ENERGY_RECHARGE","BASE_ENERGY_SHIELD_RECHARGE","CURRENT_ENERGY_SHIELD_RECHARGE","MAX_ENERGY_SHIELD","MAX_MANA","MAX_HEALTH","MAX_RAGE","MAX_SPIRIT","MAX_VOLATILITY","META_ENERGY_MAX","LEECH","LEECH_RATE","AMOUNT_LEECHED","LEECHES_RESPONSE","FLASK_RECOVERY_RATE","FLASK_CHARGES_GAINED","FLASK_NO_CONSUME_CHANCE","ARCHON_RECOVERY_RATE","GUARD_RECOVERY"},
}
def domain_of(attr):
    for d, s in DOMAINS.items():
        if attr in s: return d
    return "WireMisc"

cands = json.load(open(os.path.join(ROOT, "poe2_assets", "harvest_v2.json"), encoding="utf-8"))
blocks = collections.defaultdict(list)
skipped = []
for c in cands:
    if c["cond"] not in conds: skipped.append((c["sid"], "missing COND " + c["cond"])); continue
    if c["eff"] not in effs:  skipped.append((c["sid"], "missing EFF " + c["eff"])); continue
    d = domain_of(eff_attr.get(c["eff"], ""))
    blocks[d].append(f'        // {c["clause"].strip("_")} —— 镜像 {c["base"]}（复用其 EFF 与 COND，零新增注册）\n'
                     f'        map.put(StatIds.{c["sid"]}, self(StatConditionIds.{c["cond"]}, StatEffectIds.{c["eff"]}));')

TAIL = re.compile(r"\n    \}\n\}\s*$")
total = 0
for d, lines in blocks.items():
    p = os.path.join(WIR, d + ".java")
    t = rd(p)
    body = "\n".join(lines)
    if lines and lines[0].split("\n")[-1] in t:
        print(f"  skip (present): {d}"); continue
    m = TAIL.search(t)
    assert m, f"tail not found in {d}"
    t = t[:m.start()] + "\n" + body + "\n" + t[m.start():]
    open(p, "w", encoding="utf-8").write(t)
    total += len(lines)
    print(f"  wired {len(lines)} into {d}")
print(f"\ntotal wired = {total}; skipped = {len(skipped)}")
for s, why in skipped: print("   SKIP", s, why)
