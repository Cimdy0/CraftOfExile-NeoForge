import re, os, sys

STGEN = "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java"
lines = open(STGEN, encoding="utf-8").read().splitlines()

# expected scope tags per StatIds (the 27 batch-4 targets)
targets = {
    "ARROWS_PIERCE_AN_ADDITIONAL_TARGET": ["ARROW"],
    "ARROWS_HAVE_CHANCE_TO_PIERCE_AN_ADDITIONAL_TARGET": ["ARROW"],
    "GRENADES_HAVE_CHANCE_TO_EXPLODE_A_SECOND_TIME": ["GRENADE"],
    "IGNITE_DURATION": ["IGNITE"],
    "BLEED_DURATION": ["BLEED"],
    "ENERGY_SHIELD_RECHARGE_RATE": ["ENERGY_SHIELD"],
    "LEECH_RATE_OF_LIFE": ["LEECHING"],
    "LEECH_RATE_OF_ENERGY_SHIELD": ["LEECHING", "ENERGY_SHIELD"],
    "SKILLS_COST_LIFE_INSTEAD_OF_MANA": ["LIFE", "MANA", "COST"],
    "CHAOS_DAMAGE_OVER_TIME_APPLIED_TO_ENEMIES_IS_100_MORE_EFFECTIVE": ["CHAOS"],
    "COLD_DAMAGE_OVER_TIME_APPLIED_TO_ENEMIES_IS_100_MORE_EFFECTIVE": ["COLD"],
    "HEALTH_REGENERATION_RATE": ["HEALTH"],
    "ENERGY_SHIELD_REGENERATION_RATE": ["ENERGY_SHIELD"],
    "FLASK_CHARGES_GENERATION_RATE": ["FLASK"],
    "MANA_REGENERATION_RATE": ["MANA"],
    "LIGHTNING_DAMAGE_DEALT_IS_UNAFFECTED_BY_DEGENERATION": ["LIGHTNING"],
    "INFUSION_EFFECTIVENESS": ["INFUSION"],
    "IMPALE_EFFECTIVENESS": ["IMPALE"],
    "SPELLS_DEAL_ADDITIONAL_PHYSICAL_DAMAGE": ["SPELL"],
    "PLANT_ALLIES_DAMAGE": ["PLANT"],
    "KILLING_BLOWS_AFFECT_ALL_ENEMIES_IN_A_RADIUS": ["KILL"],
    "REMNANTS_LINGER_FOR_LONGER": ["REMNANT"],
    "ELEMENTAL_DAMAGE_WITH_ATTACKS": ["ELEMENTAL", "ATTACK"],
    "WARCRIES_ARE_MORE_EFFECTIVE": ["WARCRY"],
    "DEBILITATE_DURATION": ["DEBILITATE"],
    "STUN_DURATION": ["STUN"],
    "ELEMENTAL_RESISTANCES": ["ELEMENTAL", "RESISTANCE"],
}

writer_re = re.compile(r'(inc|chance|add|flat|valueMod|map\.put)\s*\(\s*map\s*,\s*StatIds\.(\w+)\s*,', re.I)
stag_vals = set(v for _, v in re.findall(r'public static final String ([A-Z_]+) = "([a-z_]+)"',
                                         open("src/main/java/com/cimdy/coe/data/codec/support/stat/StatTag.java", encoding="utf-8").read()))

# map each writer line to its StatIds, collect tags
bad = []
for i, ln in enumerate(lines):
    m = writer_re.search(ln)
    if not m:
        continue
    sid = m.group(2)
    if sid not in targets:
        continue
    # collect tags on this line (StatTag.X or "x")
    toks = re.findall(r'StatTag\.([A-Z_]+)', ln)
    strs = re.findall(r'"([a-z0-9_]+)"', ln)
    # resolve StatTag constant -> value
    resolved = set()
    src = open("src/main/java/com/cimdy/coe/data/codec/support/stat/StatTag.java", encoding="utf-8").read()
    for t in toks:
        mm = re.search(r'public static final String %s = "([a-z_]+)"' % t, src)
        if mm:
            resolved.add(mm.group(1))
    resolved |= set(strs)
    need = set(t.lower() for t in targets[sid])
    missing = need - resolved
    if missing:
        bad.append((i + 1, sid, sorted(missing), ln.strip()))

if bad:
    print("FAIL — %d writers missing tags:" % len(bad))
    for ln_no, sid, miss, text in bad:
        print(f"  StatGen:{ln_no} {sid} missing={miss}")
        print(f"    {text}")
    sys.exit(1)
print("PASS — all %d batch-4 writers carry their expected scope tags" % len(targets))
