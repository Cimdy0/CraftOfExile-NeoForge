#!/usr/bin/env python3
"""Widen the clean-wire harvest to ALL attributes (not just DAMAGE).

Discipline (R3): every candidate must pass manual review. This script only
*proposes* candidates via EXACT scope equality + op-family match + attribute
reference. It never auto-wires. Output is evidence for review.
"""
import re, json, os

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
GEN = ROOT + "/src/main/java/com/cimdy/coe/data/gen/stat"
SID = ROOT + "/src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"

eg = open(f"{GEN}/StatEffectGen.java", encoding="utf-8").read()
sg = open(f"{GEN}/StatGen.java", encoding="utf-8").read()
sid = open(SID, encoding="utf-8").read()

# --- parse all ModifierStatEffect EFFs ---
pat = re.compile(
    r'm\.put\(StatEffectIds\.(\w+),\s*new ModifierStatEffect\('
    r'AttributeRegister\.(\w+),\s*Either\.left\(AttributeModifier\.Operation\.(\w+)\),\s*'
    r'ModifierStatEffect\.Mode\.\w+,\s*TagExpr\.of\(Set\.of\((.*?)\)\)\)\);'
)
effs = {}
for m in pat.finditer(eg):
    name, attr, op, tagstr = m.groups()
    tags = frozenset(re.findall(r'StatTag\.(\w+)', tagstr))
    effs[name] = (attr, op, tags)

# wired StatIds in buildEntries
mb = re.search(r'buildEntries\(\)\s*\{(.*?)\n    \}', sg, re.S).group(1)
wired = set(re.findall(r'map\.put\(StatIds\.(\w+),', mb))
allids = set(re.findall(r'public static final Identifier (\w+) =', sid))
unwired = allids - wired

# --- token maps ---
# stat-name token -> StatTag (scope)
tag_to_tokens = {
 'CROSSBOW': {'CROSSBOW','CROSSBOWS'},
 'BOW': {'BOW','BOWS'},
 'DAGGER': {'DAGGER','DAGGERS'},
 'SWORD': {'SWORD','SWORDS'},
 'AXE': {'AXE','AXES'},
 'MACE': {'MACE','MACES'},
 'FLAIL': {'FLAIL','FLAILS'},
 'QUARTERSTAFF': {'QUARTERSTAFF','QUARTERSTAVE','QUARTERSTAVES'},
 'SPEAR': {'SPEAR','SPEARS'},
 'ONE_HANDED': {'ONE_HANDED','ONEHANDED'},
 'TWO_HANDED': {'TWO_HANDED','TWOHANDED'},
 'MELEE': {'MELEE'},
 'PROJECTILE': {'PROJECTILE','PROJECTILES'},
 'SPELL': {'SPELL','SPELLS'},
 'ATTACK': {'ATTACK','ATTACKS'},
 'AREA': {'AREA'},
 'FIRE': {'FIRE'},
 'COLD': {'COLD','ICE'},
 'LIGHTNING': {'LIGHTNING'},
 'PHYSICAL': {'PHYSICAL'},
 'CHAOS': {'CHAOS'},
 'ELEMENTAL': {'ELEMENTAL'},
 'MINION': {'MINION','MINIONS'},
 'TOTEM': {'TOTEM','TOTEMS'},
 'HERALD': {'HERALD','HERALDS'},
 'COMPANION': {'COMPANION','COMPANIONS'},
 'TRAP': {'TRAP','TRAPS'},
 'MINE': {'MINE','MINES'},
 'BALLISTA': {'BALLISTA','BALLISTAS'},
 'WARCRY': {'WARCRY','WARCRIES'},
 'CURSE': {'CURSE','CURSES'},
 'MARK': {'MARK','MARKS'},
 'OFFERING': {'OFFERING','OFFERINGS'},
 'PARRY': {'PARRY'},
 'GRENADE': {'GRENADE','GRENADES'},
 'HAZARD': {'HAZARD','HAZARDS'},
 'BANNER': {'BANNER','BANNERS'},
 'EMPOWERED': {'EMPOWERED'},
 'UNARMED': {'UNARMED'},
 'POISON': {'POISON'},
 'BLEED': {'BLEED','BLEEDING'},
 'IGNITE': {'IGNITE','IGNITED'},
 'CHILL': {'CHILL','CHILLED'},
 'FREEZE': {'FREEZE','FROZEN'},
 'SHOCK': {'SHOCK','SHOCKED'},
 'BURN': {'BURN','BURNING'},
 'AILMENT': {'AILMENT','AILMENTS'},
}
token_to_tag = {}
for tag, toks in tag_to_tokens.items():
    for t in toks:
        token_to_tag[t] = tag

# attribute -> stat subject tokens
ATTR_TOKENS = {
 'DAMAGE': {'DAMAGE'},
 'ARMOR': {'ARMOR'},
 'EVASION': {'EVASION'},
 'ENERGY_SHIELD': {'ENERGY_SHIELD','ENERGY'},
 'MAX_LIFE': {'MAXIMUM_LIFE','MAX_LIFE','LIFE'},
 'MAX_MANA': {'MAXIMUM_MANA','MAX_MANA','MANA'},
 'STRENGTH': {'STRENGTH'},
 'DEXTERITY': {'DEXTERITY'},
 'INTELLIGENCE': {'INTELLIGENCE'},
 'ATTACK_SPEED': {'ATTACK_SPEED'},
 'CAST_SPEED': {'CAST_SPEED'},
 'CRIT_CHANCE': {'CRIT_CHANCE','CRITICAL_CHANCE'},
 'CRIT_DAMAGE': {'CRIT_DAMAGE','CRITICAL_DAMAGE'},
 'ACCURACY': {'ACCURACY'},
 'MOVEMENT_SPEED': {'MOVEMENT_SPEED','MOVE_SPEED'},
 'BLOCK_CHANCE': {'BLOCK_CHANCE','BLOCK'},
 'DODGE': {'DODGE','DODGE_CHANCE'},
 'LIFE_REGEN': {'LIFE_REGEN','REGENERATION','REGEN'},
 'MANA_REGEN': {'MANA_REGEN'},
 'RAGE': {'RAGE'},
 'SPIRIT': {'SPIRIT'},
 'RESERVATION': {'RESERVATION','RESERVED'},
}

COND_WORDS = {"WHILE","WHEN","IF","AGAINST","RECENTLY","PER","FOR_EACH","UP_TO",
 "FURTHER","AT_CLOSE","CLOSE_RANGE","DURING","AFFECTED","NEAR","WITHIN","LOW_HEALTH",
 "FULL_HEALTH","SHAPESHIFTED","MOVING","STATIONARY","CHANNELLING","LEECHING","DUAL",
 "SHIELD","SURROUNDED","COST_HEALTH","ALSO_APPLY","REDUCTIONS","REFLECTED","TRANSFERRED",
 "CONVERTED","DEALT_BY","APPLIED_TO","SHARED","FROM_","TAKEN","OF_YOU","YOU_DEAL",
 "WITH_HIT","ON_","EACH","SECONDS","GAINED","EQUIPPED","HELD","WHEN_YOU","IF_YOU",
 "RECENTLY","NEARBY","BELOW","ABOVE","HIT","KILLING","FROM"}
META_WORDS = {"ALSO_APPLY","REDUCTIONS","REFLECTED","TRANSFERRED","CONVERTED",
 "DEALT_BY","APPLIED_TO","SHARED","NON_","ADDITIONAL","IS","ARE","THE","A_","OF_",
 "TO_YOU","YOU","YOUR"}

def has_cond(name):
    toks = name.split("_")
    return any(t in COND_WORDS or (t.startswith("ON") and t != "ONLY") for t in toks)

def op_for_prefix(stat):
    if stat.startswith("INC_") or stat.startswith("INCREASED_") or stat.startswith("REDUCED_"):
        return "ADD_MULTIPLIED_BASE"
    if stat.startswith("MORE_") or stat.startswith("GREATER_") or stat.startswith("LESS_"):
        return "ADD_MULTIPLIED_TOTAL"
    if stat.startswith("TO_") or stat.startswith("ADD_") or stat.startswith("ADDITIONAL_"):
        return "ADD_VALUE"
    return None

def scope_of(name):
    toks = name.split("_")
    s = set()
    for t in toks:
        if t in token_to_tag:
            s.add(token_to_tag[t])
    return s

def refers_attr(name, attr):
    toks = set(name.split("_"))
    for at in ATTR_TOKENS.get(attr, {attr}):
        # token match (case-insensitive)
        if any(at.lower() == t.lower() for t in toks):
            return True
    return False

# --- candidate generation ---
base_cands = []   # eff_scope empty
scoped_cands = [] # eff_scope non-empty, exact equality
for name, (attr, op, tags) in effs.items():
    eff_scope = set(tags) - {attr}
    if attr == "DAMAGE":
        eff_scope -= {"DAMAGE"}
    for s in unwired:
        if has_cond(s):
            continue
        exp = op_for_prefix(s)
        if exp is None or exp != op:
            continue
        if not refers_attr(s, attr):
            continue
        sscope = scope_of(s)
        if eff_scope == sscope:
            rec = (s, name, attr, tuple(sorted(eff_scope)), op)
            if not eff_scope:
                base_cands.append(rec)
            else:
                scoped_cands.append(rec)

# de-dup
base_cands = sorted(set(base_cands))
scoped_cands = sorted(set(scoped_cands))

print(f"unwired total: {len(unwired)}")
print(f"\n=== BASE (no scope) candidates: {len(base_cands)} ===")
for s, n, a, sc, op in base_cands:
    print(f"  {s} -> {n} [attr={a}, op={op}]")
print(f"\n=== SCOPED (exact-scope) candidates: {len(scoped_cands)} ===")
for s, n, a, sc, op in scoped_cands:
    print(f"  {s} -> {n} [attr={a}, scope={sc}, op={op}]")

json.dump({"base": base_cands, "scoped": scoped_cands},
          open("poe2_assets/harvest_widen_out.json", "w"), indent=1)
print("\nwrote poe2_assets/harvest_widen_out.json")
