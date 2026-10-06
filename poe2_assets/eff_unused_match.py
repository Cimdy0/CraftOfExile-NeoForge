#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Reverse scout: for each REGISTERED-BUT-UNUSED EFF, find unwired StatIds whose
(attr, op, scope) exactly matches it. R3-safe: the EFF already exists & is
correctly registered; we only bind a StatId to it (no new registration, no guess).

A match is valid only if the stat carries NO condition/per-count marker that the
EFF cannot express (EFFs are unconditional or scope-tagged only).
Read-only: writes eff_unused_match.json + prints; applies nothing.
"""
import io, re, os, json, glob

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
WIR = os.path.join(GEN, "wirings")
SUP = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "codec", "support")
def rd(p): return io.open(p, encoding="utf-8").read()

# true wired set
all_wired = set()
for f in [os.path.join(GEN, "StatGen.java")] + \
          [os.path.join(WIR, x) for x in sorted(os.listdir(WIR)) if x.startswith("Wire") and x.endswith(".java")]:
    for m in re.finditer(r'map\.put\(StatIds\.(\w+),', rd(f)):
        all_wired.add(m.group(1))

# EFFs
eff_text = rd(os.path.join(GEN, "StatEffectGen.java"))
effs = {}
for m in re.finditer(r'm\.put\(StatEffectIds\.(\w+),\s*new ModifierStatEffect\((.*?)\)\);', eff_text, re.S):
    am = re.search(r'(AttributeRegister|Attributes)\.(\w+)', m.group(2))
    om = re.search(r'AttributeModifier\.Operation\.(\w+)', m.group(2))
    md = re.search(r'ModifierStatEffect\.Mode\.(\w+)', m.group(2))
    if 'TagExpr.ALWAYS' in m.group(2):
        tg = frozenset()
    else:
        sm = re.search(r'TagExpr\.of\(Set\.of\((.*?)\)\)', m.group(2), re.S)
        tg = frozenset(re.findall(r'StatTag\.(\w+)', sm.group(1) if sm else ''))
    if am and om:
        effs[m.group(1)] = dict(attr=am.group(2), qual=am.group(1), op=om.group(1),
                                mode=(md.group(1) if md else 'GAIN'), tags=tg)

# used EFFs
used_eff = set()
for f in [os.path.join(GEN, "StatGen.java")] + \
          [os.path.join(WIR, x) for x in sorted(os.listdir(WIR)) if x.startswith("Wire") and x.endswith(".java")]:
    for m in re.finditer(r'StatEffectIds\.(\w+)', rd(f)):
        used_eff.add(m.group(1))
unused_effs = sorted(set(effs) - used_eff)

# token maps
tag_to_tokens = {
 'CROSSBOW': {'CROSSBOW','CROSSBOWS'}, 'BOW': {'BOW','BOWS'}, 'DAGGER': {'DAGGER','DAGGERS'},
 'SWORD': {'SWORD','SWORDS'}, 'AXE': {'AXE','AXES'}, 'MACE': {'MACE','MACES'},
 'FLAIL': {'FLAIL','FLAILS'}, 'QUARTERSTAFF': {'QUARTERSTAFF','QUARTERSTAVE','QUARTERSTAVES'},
 'SPEAR': {'SPEAR','SPEARS'}, 'ONE_HANDED': {'ONE_HANDED','ONEHANDED'}, 'TWO_HANDED': {'TWO_HANDED','TWOHANDED'},
 'MELEE': {'MELEE'}, 'PROJECTILE': {'PROJECTILE','PROJECTILES'}, 'SPELL': {'SPELL','SPELLS'},
 'ATTACK': {'ATTACK','ATTACKS'}, 'AREA': {'AREA'}, 'FIRE': {'FIRE'}, 'COLD': {'COLD','ICE'},
 'LIGHTNING': {'LIGHTNING'}, 'PHYSICAL': {'PHYSICAL'}, 'CHAOS': {'CHAOS'}, 'ELEMENTAL': {'ELEMENTAL'},
 'MINION': {'MINION','MINIONS'}, 'TOTEM': {'TOTEM','TOTEMS'}, 'HERALD': {'HERALD','HERALDS'},
 'COMPANION': {'COMPANION','COMPANIONS'}, 'TRAP': {'TRAP','TRAPS'}, 'MINE': {'MINE','MINES'},
 'BALLISTA': {'BALLISTA','BALLISTAS'}, 'WARCRY': {'WARCRY','WARCRIES'}, 'CURSE': {'CURSE','CURSES'},
 'MARK': {'MARK','MARKS'}, 'OFFERING': {'OFFERING','OFFERINGS'}, 'PARRY': {'PARRY'},
 'GRENADE': {'GRENADE','GRENADES'}, 'HAZARD': {'HAZARD','HAZARDS'}, 'BANNER': {'BANNER','BANNERS'},
 'EMPOWERED': {'EMPOWERED'}, 'UNARMED': {'UNARMED'}, 'POISON': {'POISON'}, 'BLEED': {'BLEED','BLEEDING'},
 'IGNITE': {'IGNITE','IGNITED'}, 'CHILL': {'CHILL','CHILLED'}, 'FREEZE': {'FREEZE','FROZEN'},
 'SHOCK': {'SHOCK','SHOCKED'}, 'BURN': {'BURN','BURNING'}, 'AILMENT': {'AILMENT','AILMENTS'},
 'DAMAGE': {'DAMAGE'}, 'COLD_RESISTANCE': {'COLD_RESISTANCE','COLD_RES'}, 'FIRE_RESISTANCE': {'FIRE_RESISTANCE','FIRE_RES'},
 'LIGHTNING_RESISTANCE': {'LIGHTNING_RESISTANCE','LIGHTNING_RES'}, 'CHAOS_RESISTANCE': {'CHAOS_RESISTANCE','CHAOS_RES'},
 'ARMOR': {'ARMOR'}, 'EVASION': {'EVASION'}, 'ENERGY_SHIELD': {'ENERGY_SHIELD','ENERGY'},
 'STUN': {'STUN'}, 'CRIT': {'CRIT','CRITICAL'}, 'ACCURACY': {'ACCURACY'},
}
token_to_tag = {}
for tag, toks in tag_to_tokens.items():
    for t in toks: token_to_tag[t] = tag

ATTR_TOKENS = {
 'DAMAGE': {'DAMAGE'}, 'ARMOR': {'ARMOR'}, 'EVASION': {'EVASION'},
 'ENERGY_SHIELD': {'ENERGY_SHIELD','ENERGY'}, 'MAX_LIFE': {'MAXIMUM_LIFE','MAX_LIFE','LIFE'},
 'MAX_MANA': {'MAXIMUM_MANA','MAX_MANA','MANA'}, 'STRENGTH': {'STRENGTH'}, 'DEXTERITY': {'DEXTERITY'},
 'INTELLIGENCE': {'INTELLIGENCE'}, 'ATTACK_SPEED': {'ATTACK_SPEED'}, 'CAST_SPEED': {'CAST_SPEED'},
 'CRIT_CHANCE': {'CRIT_CHANCE','CRITICAL_CHANCE','CRIT','CRITICAL'}, 'CRIT_DAMAGE': {'CRIT_DAMAGE','CRITICAL_DAMAGE'},
 'ACCURACY': {'ACCURACY'}, 'MOVEMENT_SPEED': {'MOVEMENT_SPEED','MOVE_SPEED'}, 'BLOCK_CHANCE': {'BLOCK_CHANCE','BLOCK'},
 'DODGE': {'DODGE','DODGE_CHANCE'}, 'RAGE': {'RAGE'}, 'SPIRIT': {'SPIRIT'},
 'RESERVATION': {'RESERVATION','RESERVED'}, 'ARMOR_BREAK': {'ARMOR_BREAK'},
 'AILMENT_BUILDUP': {'AILMENT_BUILDUP','STUN_BUILDUP','BUILDUP'}, 'AILMENT_MAGNITUDE': {'AILMENT_MAGNITUDE','MAGNITUDE'},
 'LAUNCH_ADDITIONAL_PROJECTILE': {'ADDITIONAL_PROJECTILE','PROJECTILES'}, 'TAKEN_CRIT_DAMAGE': {'CRIT_DAMAGE_TAKEN','TAKEN_CRIT'},
 'MAX_RAGE': {'MAX_RAGE','RAGE'}, 'SCALE': {'SCALE'}, 'CAST_SPEED': {'CAST_SPEED'},
 'ATTACK_DAMAGE': {'ATTACK_DAMAGE','MELEE_DAMAGE','DAMAGE'},
}
# attribute alias: EFF attr -> canonical ATTR_TOKENS key
ATTR_ALIAS = {
 'DAMAGE':'DAMAGE','COE_DAMAGE':'DAMAGE','COE_DAMAGE_DAMAGE':'DAMAGE','ARMOR':'ARMOR','EVASION':'EVASION',
 'MAX_RAGE':'MAX_RAGE','RAGE':'MAX_RAGE','SCALE':'SCALE','ARMOR_BREAK':'ARMOR_BREAK',
 'AILMENT_BUILDUP':'AILMENT_BUILDUP','AILMENT_MAGNITUDE':'AILMENT_MAGNITUDE',
 'CAST_SPEED':'CAST_SPEED','ATTACK_SPEED':'ATTACK_SPEED','MOVEMENT_SPEED':'MOVEMENT_SPEED',
 'CRIT_CHANCE':'CRIT_CHANCE','MAX_MANA':'MAX_MANA','LAUNCH_ADDITIONAL_PROJECTILE':'LAUNCH_ADDITIONAL_PROJECTILE',
 'TAKEN_CRIT_DAMAGE':'TAKEN_CRIT_DAMAGE','ATTACK_DAMAGE':'ATTACK_DAMAGE',
}

COND_MARK = ("IF_","WHILE_","WHEN_","RECENTLY","PER_","FOR_EACH_","AGAINST_","UP_TO","HAVEN",
             "EACH_","NOT_","ALSO_APPLY","REDUCTIONS","REFLECTED","TRANSFERRED","CONVERTED","DEALT_BY",
             "APPLIED_TO","SHARED","WITH_HIT","ON_","NEARBY","BELOW","ABOVE","KILLING","GAINED",
             "EQUIPPED","HELD","SURROUNDED","STATIONARY","CHANNELLING","LEECHING","DUAL","SHIELD",
             "COST_HEALTH","AFFECTED","WITHIN","CLOSE_RANGE","FURTHER","METRES","SECONDS","SOCKETED",
             "SUPPORT","GEMS","PRESENCE","IMMOBILISED","PINNED","HEAVY_STUN","FULL","LOW","FULLY")

def op_for_prefix(stat):
    if stat.startswith(("INC_","INCREASED_","REDUCED_")): return "ADD_MULTIPLIED_BASE"
    if stat.startswith(("MORE_","GREATER_","LESS_")): return "ADD_MULTIPLIED_TOTAL"
    if stat.startswith(("TO_","ADD_","ADDITIONAL_")): return "ADD_VALUE"
    return None

def scope_of(name):
    s = set()
    for t in name.split("_"):
        if t in token_to_tag: s.add(token_to_tag[t])
    return s

def refers_attr(name, attr):
    toks = set(name.split("_"))
    for at in ATTR_TOKENS.get(attr, {attr}):
        if any(at.lower() == t.lower() for t in toks): return True
    return False

def has_cond(name):
    return any(c in name for c in COND_MARK)

statids = set(re.findall(r'public static final Identifier (\w+) =', rd(os.path.join(SUP, "stat", "StatIds.java"))))
unwired = sorted(statids - all_wired)

matches = []
for eff in unused_effs:
    v = effs[eff]
    akey = ATTR_ALIAS.get(v['attr'], v['attr'])
    for u in unwired:
        if op_for_prefix(u) != v['op']: continue
        if not refers_attr(u, akey): continue
        # EFF tags include the attribute's own tag (e.g. DAMAGE EFFs tagged {DAMAGE,X});
        # strip it so we compare only the *scope* portion.
        eff_scope = frozenset(t for t in v['tags'] if t != v['attr'])
        if akey in ("DAMAGE",) and "DAMAGE" in eff_scope: eff_scope = eff_scope - {"DAMAGE"}
        # stat scope tokens == eff scope portion
        uscope = scope_of(u)
        if akey in ("DAMAGE",) and "DAMAGE" in uscope: uscope = uscope - {"DAMAGE"}
        # also drop the attribute's own tag if the stat happens to carry it
        uscope = frozenset(t for t in uscope if t != v['attr'])
        if uscope != eff_scope: continue
        if has_cond(u): continue   # EFF cannot express a condition
        matches.append(dict(eff=eff, sid=u, attr=v['attr'], op=v['op'], tags=sorted(v['tags'])))

# de-dup
seen=set(); out=[]
for m in matches:
    k=(m['eff'],m['sid'])
    if k in seen: continue
    seen.add(k); out.append(m)

print(f"unused EFFs = {len(unused_effs)}   unwired = {len(unwired)}")
print(f"\n=== EXACT-MATCH bindings (unused EFF <- unwired StatId): {len(out)} ===")
for m in out:
    print(f"  {m['sid']:50} <- {m['eff']}  [attr={m['attr']} op={m['op']} tags={m['tags']}]")

json.dump(out, open(os.path.join(ROOT, "poe2_assets", "eff_unused_match.json"), "w"), indent=1, ensure_ascii=False)
print("\nwrote poe2_assets/eff_unused_match.json")
