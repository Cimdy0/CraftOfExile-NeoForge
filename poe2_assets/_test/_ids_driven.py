"""Ids-driven normalization analysis.

Perspective: each StatIds (real data point) is the unit. For every StatIds we
record which attribute it maps to + the scope/condition tags on its writer.
Then classify attributes:
  - shared  : referenced by >1 StatIds  -> treated as a BASE (merge target)
  - 1:1     : referenced by exactly 1 StatIds -> a PLACEHOLDER (merge candidate)
For each 1:1 placeholder we attempt to detect a base+scope merge by matching the
StatIds' semantic id-string to a known base-attribute keyword.
"""
import re, json

ATTR = "src/main/java/com/cimdy/coe/data/AttributeRegister.java"
STGEN = "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java"
STATIDS = "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"
STAG = "src/main/java/com/cimdy/coe/data/codec/support/stat/StatTag.java"

attr_src = open(ATTR, encoding="utf-8").read()
stgen_src = open(STGEN, encoding="utf-8").read().splitlines()
statids_src = open(STATIDS, encoding="utf-8").read()
stag_src = open(STAG, encoding="utf-8").read()

# --- all registered attributes: const -> register string ---
attr_def = {}
for m in re.finditer(r'public static Holder<Attribute>\s+([A-Z][A-Z0-9_]*)\s*=\s*register\w+\(\s*"([a-z0-9_]+)"', attr_src):
    attr_def[m.group(1)] = m.group(2)
attr_const_by_str = {v: k for k, v in attr_def.items()}

# --- StatTag vocabulary: const -> value ---
stag = {c: v for c, v in re.findall(r'public static final String ([A-Z_]+) = "([a-z_]+)"', stag_src)}

# --- parse StatGen writers: StatIds.CONST -> (helper, attr_const, tags, raw) ---
writer_re = re.compile(r'\b(inc|chance|add|flat|valueMod|pctAdd|recoverPctOnEvent|esRechargeMod|map\.put)\s*\(\s*map\s*,\s*StatIds\.(\w+)', re.I)
writers = {}  # StatIds const -> dict
for i, l in enumerate(stgen_src):
    m = writer_re.search(l)
    if not m:
        continue
    helper, sid = m.group(1), m.group(2)
    rest = l[m.end():]
    am = re.search(r'AttributeRegister\.([A-Z][A-Z0-9_]*)', rest)
    attr_const = am.group(1) if am else None
    # tags: StatTag.X and bare "x"
    tag_consts = re.findall(r'StatTag\.([A-Z_]+)', rest)
    tag_strs = re.findall(r'"([a-z0-9_]+)"', rest)
    tags = set()
    for tc in tag_consts:
        tags.add(stag.get(tc, tc.lower()))
    tags |= set(tag_strs)
    writers[sid] = {
        "helper": helper,
        "attr_const": attr_const,
        "attr_str": attr_def.get(attr_const) if attr_const else None,
        "tags": sorted(tags),
        "line": i + 1,
        "raw": l.strip(),
    }

# --- all StatIds constants ---
statids = {}
for m in re.finditer(r'public static final Identifier\s+([A-Z][A-Z0-9_]*)\s*=\s*id\("([a-z0-9_]+)"\)', statids_src):
    statids[m.group(1)] = m.group(2)

# --- reverse map: attr_str -> [StatIds consts] ---
attr_to_sids = {}
for sid, w in writers.items():
    a = w["attr_str"]
    if a:
        attr_to_sids.setdefault(a, []).append(sid)

# --- classify attributes ---
shared = {a: sids for a, sids in attr_to_sids.items() if len(sids) > 1}
single = {a: sids[0] for a, sids in attr_to_sids.items() if len(sids) == 1}

# --- curated base-attribute keyword map (project's real base stats) ---
# maps a base-stat keyword (appears in StatIds id) -> registered attr register string
BASE_KW = {
    "crit_chance": "crit_chance",
    "critical_hit_chance": "crit_chance",
    "crit_damage": "taken_crit_damage",
    "cast_speed": "cast_speed",
    "attack_speed": "attack_speed",
    "movement_speed": "movement_speed",
    "damage": "damage",
    "added_damage": "added_damage",
    "life": "life",
    "max_life": "max_health",
    "max_health": "max_health",
    "mana": "mana",
    "max_mana": "max_mana",
    "energy_shield": "energy_shield",
    "armour": "defend_armour",
    "armor": "defend_armour",
    "evasion": "defend_evasion",
    "resistance": "resistances",
    "resist": "resistances",
    "thorns": "thorns",
    "knockback": "knockback",
    "stun_threshold": "stun_threshold",
    "stun_recovery": "stun_recovery",
    "recovery_rate": "recovery_rate",
    "cooldown_recovery": "cooldown_recovery_rate",
    "rage": "rage",
    "cost": "cost_efficiency",
    "aoe": "area_of_effect",
    "area_of_effect": "area_of_effect",
}

# scope/condition keywords -> StatTag value (only those that exist)
SCOPE_KW = {}
for v in stag.values():
    SCOPE_KW[v] = v
# extra alias mapping for scope words in id
SCOPE_ALIAS = {
    "minion": "minion", "minions": "minion",
    "arrow": "arrow", "arrows": "arrow",
    "grenade": "grenade",
    "ignite": "ignite",
    "bleed": "bleed",
    "poison": "poison",
    "chaos": "chaos",
    "cold": "cold",
    "fire": "fire",
    "lightning": "lightning",
    "physical": "physical",
    "elemental": "elemental",
    "spell": "spell",
    "attack": "attack",
    "melee": "melee",
    "projectile": "projectile",
    "warcry": "warcry",
    "totem": "totem",
    "trap": "trap",
    "mine": "mine",
    "summon": "summon",
    "brand": "brand",
    "aura": "aura",
    "channel": "channeling", "channelling": "channeling",
    "curse": "curse",
    "flask": "flask",
    "leech": "leeching",
    "blind": "blind",
    "stun": "stun",
    "knockback": "knockback",
    "impale": "impale",
    "withered": "withered",
    "ailment": "ailment",
    "plant": "plant",
    "remnant": "remnant",
    "infusion": "infusion",
    "shock": "shock",
    "freeze": "freeze",
    "chill": "chill",
    "burn": "burn",
    "parry": "parry",
    "dodge": "dodge",
    "shapeshift": "shapeshifted",
    "herald": "herald",
    "mark": "mark",
    "companion": "companion",
    "jewellery": "jewellery", "jewel": "jewellery",
    "charge": "charge",
    "pierce": "pierce",
    "crit": "crit",
}

print(f"StatIds total: {len(statids)}")
print(f"StatGen writers parsed: {len(writers)}")
print(f"Attributes referenced: {len(attr_to_sids)} (shared={len(shared)}, 1:1={len(single)})")

# --- For each 1:1 placeholder, propose base+scope merge ---
print("\n=== 1:1 PLACEHOLDER attributes -> proposed base+scope (Ids-driven) ===")
cands = []
for astr, sid in sorted(single.items(), key=lambda x: x[1]):
    w = writers.get(sid)
    if not w:
        continue
    sid_id = statids.get(sid, "")
    # detect base
    base_hit = None
    for kw, base in BASE_KW.items():
        if kw in sid_id and base in attr_def.values():
            base_hit = base
            break
    # detect scope tags present in id
    scope_tags = []
    for word, tag in SCOPE_ALIAS.items():
        if re.search(r'(^|_)' + re.escape(word) + r'(_|$)', sid_id) and tag in stag.values():
            if tag not in scope_tags:
                scope_tags.append(tag)
    already = set(w["tags"])
    missing_scope = [t for t in scope_tags if t not in already]
    note = []
    if base_hit and base_hit != astr:
        note.append(f"MERGE -> {base_hit}")
    if missing_scope:
        note.append(f"+scope {missing_scope}")
    if not note:
        continue
    cands.append((sid, sid_id, astr, w["helper"], sorted(already), note))

for sid, sid_id, astr, helper, tags, note in cands:
    print(f"  {sid}\n     id={sid_id}\n     now: {helper}({astr}) tags={tags}\n     => {'; '.join(note)}")

print(f"\n=== {len(cands)} placeholder candidates with a proposed action ===")
