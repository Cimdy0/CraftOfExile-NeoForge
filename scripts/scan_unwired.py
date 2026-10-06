#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import os, re

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
SID = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "codec", "support", "stat", "StatIds.java")
WDIR = os.path.join(GEN, "wirings")

def read(p):
    with open(p, encoding="utf-8") as f:
        return f.read()

# all StatIds constants
sid_pat = re.compile(r'public static final Identifier (\w+) = id\("([^"]+)"\);')
statids = {}  # name -> path
for m in sid_pat.finditer(read(SID)):
    statids[m.group(1)] = m.group(2)

# all wired keys
wired = set()
put_pat = re.compile(r'map\.put\(StatIds\.(\w+),')
for f in sorted(os.listdir(WDIR)):
    if f.startswith("Wire") and f.endswith(".java"):
        for m in put_pat.finditer(read(os.path.join(WDIR, f))):
            wired.add(m.group(1))

unwired = [k for k in statids if k not in wired]
print(f"StatIds total={len(statids)}  wired={len(wired)}  unwired={len(unwired)}")

# categorize by keyword clusters
clusters = {
    "gloves": r"GLOVE|GLOVES",
    "boots/feet": r"BOOTS|FEET",
    "helmet/head": r"HELMET|HEAD",
    "chest/body": r"CHEST|BODY_ARMOUR|BODY_ARMOR",
    "shield/offhand": r"SHIELD|OFFHAND",
    "companion": r"COMPANION|MINION|SUMMON",
    "charm": r"CHARM",
    "herald": r"HERALD",
    "quarterstaff": r"QUARTERSTAFF|QUARTER_STAFF",
    "warcries/warcry": r"WARCRY|WAR_CRY",
    "totem": r"TOTEM",
    "curse": r"CURSE|HEX",
    "infusion": r"INFUSION",
    "dodge_roll": r"DODGE_ROLL",
    "rage": r"RAGE",
    "endurance": r"ENDURANCE",
    "frenzy": r"FRENZY",
    "power_charge": r"POWER_CHARGE",
    "spirit": r"SPIRIT",
    "banner": r"BANNER",
    "offering": r"OFFERING",
    "presence/aura": r"PRESENCE|AURA",
    "ailment generic": r"AILMENT",
    "skill effect dur": r"SKILL_EFFECT_DURATION",
    "energy shield": r"ENERGY_SHIELD",
    "exposure": r"EXPOSURE",
    "suppression": r"SUPPRESS",
    "armor": r"ARMOR",
    "evasion": r"EVASION",
    "resistance": r"RESISTANCE",
    "block": r"BLOCK",
    "stun": r"STUN",
    "knockback": r"KNOCKBACK",
    "projectile": r"PROJECTILE",
    "melee": r"MELEE",
    "attack": r"ATTACK",
    "cast/spell": r"SPELL|CAST",
    "damage": r"DAMAGE",
    "critical/crit": r"CRIT",
    "crit": r"CRIT",
    "movement": r"MOVEMENT|MOVE_SPEED",
    "flask": r"FLASK",
    "leech": r"LEECH",
    "life/health": r"LIFE|HEALTH",
    "mana": r"MANA",
    "marks/mark": r"MARK",
    "brand": r"BRAND",
    "trap/mine": r"TRAP|MINE",
}
import collections
hits = collections.Counter()
examples = collections.defaultdict(list)
for k in unwired:
    pid = statids[k]
    for label, pat in clusters.items():
        if re.search(pat, pid, re.I):
            hits[label] += 1
            if len(examples[label]) < 6:
                examples[label].append(pid)
print("\n=== unwired cluster histogram (a StatId may hit multiple) ===")
for label, c in hits.most_common():
    print(f"{c:4d}  {label:18s} e.g. {examples[label][:4]}")
