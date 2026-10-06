#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Precise gap classifier: for each UNWIRED stat, derive (attribute, op, inferred-tags)
and find (a) EXACT existing EFF -> SAFE WIRE (b) attribute exists + same-attr same-op
EFF exists but missing this tag combo -> DESIGN new EFF variant (feasible, pure data).

Subject->attribute keyword map (only existing AttributeRegister attrs).
Tag keyword map restricted to ENGINE-INFERRED StatTag constants (no dagger/flail/qstaff).
"""
import re, glob, os, json

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
SUP = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "codec", "support")

def read(p):
    with open(p, "r", encoding="utf-8") as f: return f.read()

# ---- StatIds ----
sid = read(os.path.join(SUP, "stat", "StatIds.java"))
statids = dict(re.findall(r'public static final Identifier (\w+) = id\("([^"]+)"\);', sid))

# ---- true wired set ----
wired = set()
for p in glob.glob(os.path.join(GEN, "wirings", "*.java")) + [os.path.join(GEN, "StatGen.java")]:
    wired.update(re.findall(r'StatIds\.(\w+),', read(p)))
unwired = [n for n in statids if n not in wired]

# ---- EFF registry: name -> (op, attr, frozenset(tags), negated_tags) ----
eg = read(os.path.join(GEN, "StatEffectGen.java"))
eff = {}
for m in re.finditer(r'm\.put\(StatEffectIds\.(\w+),\s*new\s+ModifierStatEffect\((.*?)\)\);', eg):
    name, body = m.group(1), m.group(2)
    am = re.search(r'AttributeRegister\.(\w+)', body)
    opm = re.search(r'AttributeModifier\.Operation\.(\w+)', body)
    if not am or not opm: continue
    attr, op = am.group(1), opm.group(1)
    if "TagExpr.ALWAYS" in body:
        pos, neg = ["ALWAYS"], set()
    else:
        tm = re.search(r'TagExpr\.of\(Set\.of\((.*?)\)\)', body, re.S)
        if not tm: continue
        inner = tm.group(1)
        pos = re.findall(r'StatTag\.(\w+)', inner)
        neg = set(re.findall(r'StatTag\.not\(StatTag\.(\w+)\)', inner))
    eff[name] = (op, attr, frozenset(pos), neg)

by_attr = {}
for n,(op,attr,pos,neg) in eff.items():
    by_attr.setdefault(attr, []).append((n,op,pos,neg))

# ---- registered attributes (names that appear as AttributeRegister.X in EFF) ----
attrs = set(re.findall(r'AttributeRegister\.(\w+)\b', eg))

# ---- engine-inferred StatTag constants ----
stag = read(os.path.join(SUP, "stat", "StatTag.java"))
INFERRED = set(re.findall(r'public static final String (\w+) = ', stag))

# ---- subject -> attribute keyword map (only attrs that exist) ----
SUBJ = {}
def add(sub, attr):
    if attr in attrs: SUBJ[sub] = attr
# damage family
for k in ["DAMAGE"]: add(k,"DAMAGE")
add("CRIT_CHANCE","CRIT_CHANCE"); add("CRIT_DAMAGE","CRIT_DAMAGE"); add("CRITICAL_DAMAGE","CRIT_DAMAGE")
add("CRITICAL_HIT_CHANCE","CRIT_CHANCE"); add("ACCURACY","ACCURACY"); add("EVASION","EVASION")
add("ARMOR","ARMOR"); add("MOVEMENT_SPEED","MOVEMENT_SPEED"); add("CAST_SPEED","CAST_SPEED")
add("ATTACK_SPEED","ATTACK_SPEED"); add("MANA","MAX_MANA"); add("ENERGY_SHIELD","MAX_ENERGY_SHIELD")
add("HEALTH","MAX_HEALTH"); add("LIFE","MAX_HEALTH"); add("RAGE","MAX_RAGE"); add("SPIRIT","MAX_SPIRIT")
add("RESISTANCE","RESISTANCES"); add("RESISTANCES","RESISTANCES")
add("FIRE_DAMAGE","DAMAGE"); add("COLD_DAMAGE","DAMAGE"); add("LIGHTNING_DAMAGE","DAMAGE")
add("CHAOS_DAMAGE","DAMAGE"); add("PHYSICAL_DAMAGE","DAMAGE"); add("ELEMENTAL","DAMAGE")
add("BLOCK","BLOCK_CHANCE"); add("STUN","STUN_THRESHOLD"); add("DEFLECTION","DEFLECTION_RATING")
add("RESERVATION","RESERVATION_EFFICIENCY"); add("FLASK","FLASK_RECOVERY_RATE")
add("LEECH","LEECH"); add("REGEN","REGENERATION_RATE"); add("REGENERATION","REGENERATION_RATE")
add("HEALING","REGENERATION_RATE"); add("PIERCE","PROJECTILE_PIERCE"); add("PROJECTILE","DAMAGE")
add("AILMENT","AILMENT_MAGNITUDE"); add("BLEED","AILMENT_MAGNITUDE"); add("POISON","AILMENT_MAGNITUDE")
add("IGNITE","AILMENT_MAGNITUDE"); add("CHILL","AILMENT_MAGNITUDE"); add("SHOCK","AILMENT_MAGNITUDE")
add("FREEZE","AILMENT_MAGNITUDE"); add("BURN","AILMENT_MAGNITUDE")
add("EFFECT_AREA","EFFECT_AREA"); add("AREA_OF_EFFECT","EFFECT_AREA")
add("KNOCKBACK","KNOCKBACK"); add("DODGE","DODGE_CHANCE")
add("MAXIMUM_LIFE","MAX_HEALTH"); add("MAXIMUM_MANA","MAX_MANA"); add("MAXIMUM_ENERGY_SHIELD","MAX_ENERGY_SHIELD")
add("MAXIMUM_RAGE","MAX_RAGE"); add("MAXIMUM_SPIRIT","MAX_SPIRIT")

# ---- tag keyword map (only inferred tags) ----
TAGKW = {}
for t in ["ATTACK","SPELL","MELEE","RANGED","PROJECTILE","AREA","AURA","BEAM","SELF","FIRE","COLD",
          "LIGHTNING","CHAOS","PHYSICAL","ELEMENTAL","ONE_HANDED","TWO_HANDED","SWORD","MACE","AXE",
          "SPEAR","BOW","CROSSBOW","SLAM","MINION","TOTEM","TRAP","MINE","CURSE","MARK","WARCRY",
          "POISON","BLEED","IGNITE","CHILL","SHOCK","FREEZE","BURN","STUN","PIN","BLIND","MAIM",
          "HINDER","SLOW","EXPOSURE","ARMOR_BREAK","WOUNDED","HEAVY_STUN"]:
    if t in INFERRED: TAGKW[t]=t
# extras present in INFERRED
for t in ["EMPOWERED","HERALD","BANNER","BRAND","GOLEM","COMPANION","ONSLAUGHT","INFUSION","STORM",
          "PLANT","BALLISTA","GRENADE","HAZARD","OFFERING","SEAL","META","ARCANE_SURGE","ARCHON",
          "CHARGE","FRENZY","ENDURANCE","POWER","GLORY","VOLATILITY","CORPSE","RECENTLY_CONSUMED",
          "COMMAND","PUPPET_MASTER","ICE_CRYSTAL","CHILLED_GROUND","SHOCKED_GROUND","IGNITED_GROUND",
          "JAGGED_GROUND","WITHERED","DEBILITATE","AGGRAVATED","IMMOBILISE","INCISION","DAZE","FORM",
          "BEAST","WOLF","BEAR","WYVERN","FOCUS","QUIVER","HELMET","JEWELLERY","JEWEL","RARITY","BOLT",
          "ALLIES","STATUS","AILMENT","NON_DAMAGING","CRIT","KILL","LEECHING","SHAPESHIFTED","MOVEMENT",
          "RECOUP","CULLING","IMPALE","ENERGY","ENERGY_SHIELD","MANA_TAKE","HEALTH","MANA","RAGE","SPIRIT"]:
    if t in INFERRED and t not in TAGKW: TAGKW[t]=t

ZERO_BASE = set(json.load(open(os.path.join(ROOT,"poe2_assets","attr_base_table.json"))).keys())  # names with base==0

def exp_op(name):
    if name.startswith(("INC_","INCREASED_","REDUCED_")): return "ADD_MULTIPLIED_BASE"
    if name.startswith(("MORE_","GREATER_","LESS_")): return "ADD_MULTIPLIED_TOTAL"
    if name.startswith(("TO_","ADD_","ADDITIONAL_","EXTRA_")): return "ADD_VALUE"
    return None

# ---- condition / per-count / meta markers (skip; not pure-data simple) ----
SKIP = ("_AND_","AND_","PENALTY","CONVERT","IMMUNE_TO","CANNOT_","CULLING","APPLY_","GAIN_OF",
 "CHANCE_TO","HAS_A_CHANCE","WIELDING_","EQUIP_","AFFECTED_BY","COMPANIONS_","YOUR_COMPANION",
 "INVOCATION","OFF_HAND_EMPTY","RECENTLY_TOOK","YOU_CAN_","ENEMIES_YOU","SKILL_SPEED",
 "MANA_COST_EFFICIENCY","ENERGY_SHIELD_RECHARGE","HEALTH_REGEN","HEALTH_REGENERATION",
 "HEALTH_RECOVERY","MANA_REGEN","REGENERATION_RATE","SOCKET","RARITY","DURATION_OF",
 "MAGNITUDE_OF","EFFECT_OF","PER_","FOR_EACH","WITH_","AGAINST","_WHILE_","_IF_","_ON_","_WHEN_",
 "RECENTLY","_DURING_","HAS_","_BY_","_FROM_","IF_YOU","WHILE_","WHEN_","VS_","NEAR_","WITHIN_",
 "_FOR_","METRES","SECONDS","_AFTER_")
def is_skip(name):
    return any(s in name for s in SKIP)

D_safe=[]; E_design=[]
for n in unwired:
    if is_skip(n): continue
    op = exp_op(n)
    if op is None: continue
    # derive attribute
    attr=None
    for k,v in SUBJ.items():
        if k in n: attr=v; break
    if attr is None: continue
    if attr not in attrs: continue
    # derive tags from name (inferred only)
    tags=set()
    for k,t in TAGKW.items():
        if k in n: tags.add(t)
    # exact EFF match?
    for (en,eop,epos,eneg) in by_attr.get(attr,[]):
        if eop!=op: continue
        if epost_pos := epos - {"ALWAYS"}:
            if epost_pos == (tags | set()) or epost_pos == (tags) or (tags and epost_pos==tags):
                # require DAMAGE/attr self-tag handled: check pos matches derived tags exactly or pos is subset of derived+attr
                if epost_pos == (tags | ({"DAMAGE"} if attr=="DAMAGE" and "DAMAGE" in epost_pos else set())):
                    D_safe.append((n, en, attr, op, sorted(epost_pos), sorted(tags))); break
    else:
        # design: attribute exists, same-attr same-op EFF exists, tags all valid inferred -> feasible new variant
        has_same_op = any(eop==op for (_,eop,_,_) in by_attr.get(attr,[]))
        if has_same_op and tags and tags.issubset(INFERRED):
            # avoid base=0 + ADD_MULTIPLIED_BASE (three-laws #3)
            if not (attr in ZERO_BASE and op=="ADD_MULTIPLIED_BASE"):
                E_design.append((n, attr, op, sorted(tags)))

print(f"Total unwired={len(unwired)}  analyzed")
print(f"\n[D] EXACT EFF match (SAFE WIRE): {len(D_safe)}")
for x in D_safe: print("   ", x[0], "->", x[1])
print(f"\n[E] DESIGN new EFF variant (attr exists, op exists, tags valid): {len(E_design)}")
for x in E_design[:60]: print("   ", x[0], "| attr=",x[1], "op=",x[2], "tags=",x[3])

json.dump({"D":D_safe,"E":E_design}, open(os.path.join(ROOT,"poe2_assets","precise_gap.json"),"w"), indent=1)
print("\nwrote poe2_assets/precise_gap.json")
