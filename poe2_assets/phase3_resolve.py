#!/usr/bin/env python3
# Phase 3 resolver v3 (R3-safe, tag/condition aware):
#  - EFF universe = ALL real EFF names parsed from StatEffectIds.java (ground truth)
#  - attribute resolution prefers SPECIFIC attr (ARMOR_BREAK > ARMOR, LEECH > MAX_HEALTH, ...)
#  - scope tag tokens (weapon/element/type) -> tagged EFF whose name contains the token
#  - scope condition tokens -> real COND + TGT (only those verified to exist)
#  - conditional templates without a resolvable COND -> skip
import json, re, os

ROOT = os.path.dirname(os.path.abspath(__file__))
wt = json.load(open(os.path.join(ROOT, "wire_table.json"), encoding="utf-8"))
EFFSRC = os.path.join(ROOT, "..", "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat", "StatEffectIds.java")

# --- ground-truth EFF universe ---
eff_names = set()
with open(EFFSRC, encoding="utf-8") as f:
    for line in f:
        m = re.search(r"(EFF_[A-Z0-9_]+)\s*=\s*id\(", line)
        if m:
            eff_names.add(m.group(1))
print("Real EFF universe size:", len(eff_names))

OP_TOKEN = {"INC": "ADD_MULTIPLIED_BASE", "ADD": "ADD_VALUE", "TO": "ADD_VALUE", "MORE": "ADD_MULTIPLIED_TOTAL"}

# concept token -> AttributeRegister key (specific first)
SYN = {
    "damage": "DAMAGE", "armor": "ARMOR", "armour": "ARMOR", "evasion": "EVASION",
    "accuracy": "ACCURACY", "ailment": "AILMENT_BUILDUP", "buildup": "AILMENT_BUILDUP",
    "ailments": "AILMENT_BUILDUP", "magnitude": "AILMENT_MAGNITUDE", "duration": "AILMENT_DURATION",
    "chance": "AILMENT_CHANCE", "threshold": "AILMENT_THRESHOLD",
    "crit": "CRIT_CHANCE", "critical": "CRIT_CHANCE", "cast": "CAST_SPEED",
    "attackspeed": "ATTACK_SPEED", "attackdamage": "ATTACK_DAMAGE", "attack": "ATTACK_DAMAGE",
    "movement": "MOVEMENT_SPEED", "cooldown": "COOLDOWN_RECOVERY_RATE",
    "health": "MAX_HEALTH", "life": "MAX_HEALTH", "mana": "MAX_MANA", "rage": "MAX_RAGE",
    "spirit": "MAX_SPIRIT", "strength": "STRENGTH", "dexterity": "DEXTERITY",
    "intelligence": "INTELLIGENCE", "deflection": "DEFLECTION_RATING", "block": "BLOCK_CHANCE",
    "energy": "MAX_ENERGY_SHIELD", "shield": "MAX_ENERGY_SHIELD",
    "resistance": "RESISTANCES", "resistances": "RESISTANCES",
    "regeneration": "REGENERATION_RATE", "recovery": "RECOVERY_RATE",
    "glory": "GLORY", "knockback": "KNOCKBACK", "flying": "FLYING_SPEED",
    "sneaking": "SNEAKING_SPEED", "scale": "SCALE", "volatility": "MAX_VOLATILITY",
    "stun": "STUN_RECOVERY", "armorbreak": "ARMOR_BREAK", "broken": "ARMOR_BREAK",
    "leech": "LEECH", "ailmentmagnitude": "AILMENT_MAGNITUDE", "ailmentduration": "AILMENT_DURATION",
    "ailmentbuildup": "AILMENT_BUILDUP", "ailmentchance": "AILMENT_CHANCE",
    "maxhealth": "MAX_HEALTH", "maxmana": "MAX_MANA", "maxrage": "MAX_RAGE",
    "maxenergy": "MAX_ENERGY_SHIELD", "maxspirit": "MAX_SPIRIT",
    "movementspeed": "MOVEMENT_SPEED", "castspeed": "CAST_SPEED", "rageloss": "RAGE_LOSS_RATE",
    "flaskcharges": "FLASK_CHARGES_GAINED", "charm": "CHARM_CHARGES_GAINED",
    "deflectionrating": "DEFLECTION_RATING", "ailmentthreshold": "AILMENT_THRESHOLD",
}

# scope token -> (kind, value) ; kind in {tag, cond}
# tag: matches an EFF name token (weapon/element/type). cond: (COND, TGT)
COND_MAP = {
    "shapeshifted": ("cond", ("COND_LIVING_STATE_SHAPESHIFTED", "TGT_SELF_ONLY")),
    "surrounded": ("cond", ("COND_LIVING_STATE_SURROUNDED", "TGT_SELF_ONLY")),
    "shock": ("cond", ("COND_SHOCK_STATUS", "TGT_COND_ON_TARGET")),
    "shocked": ("cond", ("COND_SHOCK_STATUS", "TGT_COND_ON_TARGET")),
    "freeze": ("cond", ("COND_FREEZE_STATUS", "TGT_COND_ON_TARGET")),
    "frozen": ("cond", ("COND_FREEZE_STATUS", "TGT_COND_ON_TARGET")),
    "chilled": ("cond", ("COND_ON_CHILLED_GROUND_STATUS", "TGT_SELF_ONLY")),
    "ignited": ("cond", ("COND_ON_IGNITED_GROUND_STATUS", "TGT_SELF_ONLY")),
    "parrying": ("cond", ("COND_LIVING_STATE_PARRYING", "TGT_SELF_ONLY")),
    "stationary": ("cond", ("COND_LIVING_STATE_STATIONARY", "TGT_SELF_ONLY")),
    "killrecently": ("cond", ("COND_COUNT_KILL_RECENTLY", "TGT_SELF_ONLY")),
    "killedrecently": ("cond", ("COND_COUNT_KILL_RECENTLY", "TGT_SELF_ONLY")),
    "daze": ("cond", ("COND_DAZE_STATUS", "TGT_COND_ON_TARGET")),
    "dazed": ("cond", ("COND_DAZE_STATUS", "TGT_COND_ON_TARGET")),
    "rare": ("cond", ("COND_PRESENCE_RARITY_RARE_UNIQUE", "TGT_SELF_ONLY")),
    "unique": ("cond", ("COND_PRESENCE_RARITY_RARE_UNIQUE", "TGT_SELF_ONLY")),
    "bleed": ("cond", ("COND_BLEED_STATUS_AGE", "TGT_COND_ON_TARGET")),
    "bleeding": ("cond", ("COND_BLEED_STATUS_AGE", "TGT_COND_ON_TARGET")),
    "electrocute": ("cond", ("COND_ELECTROCUTE_STATUS", "TGT_COND_ON_TARGET")),
    "electrocuted": ("cond", ("COND_ELECTROCUTE_STATUS", "TGT_COND_ON_TARGET")),
    "heavy_stun": ("cond", ("COND_HEAVY_STUN_STATUS", "TGT_COND_ON_TARGET")),
    "heavy_stunned": ("cond", ("COND_HEAVY_STUN_STATUS", "TGT_COND_ON_TARGET")),
    "pin": ("cond", ("COND_PIN_STATUS", "TGT_COND_ON_TARGET")),
    "pinned": ("cond", ("COND_PIN_STATUS", "TGT_COND_ON_TARGET")),
    "jagged": ("cond", ("COND_ON_JAGGED_GROUND_STATUS", "TGT_SELF_ONLY")),
    "exposure": ("cond", ("COND_EXPOSURE_COLD_STATUS", "TGT_COND_ON_TARGET")),
    "lowlife": ("cond", ("COND_LIVING_STATE_LOW_LIFE", "TGT_SELF_ONLY")),
    "full_life": ("cond", ("COND_FULL_LIFE_LIVING_STATE", "TGT_SELF_ONLY")),
}

# tag tokens -> EFF name word (uppercased). These are tags, not conditions.
TAG_MAP = {
    "sword": "SWORD", "axe": "AXE", "bow": "BOW", "crossbow": "CROSSBOW",
    "cross_bow": "CROSSBOW", "flail": "FLAIL", "spear": "SPEAR",
    "quarterstaff": "QUARTERSTAVE", "quarterstave": "QUARTERSTAVE",
    "dagger": "DAGGER", "wand": "WAND", "claw": "CLAW", "staff": "STAFF",
    "mace": "MACE", "one_handed": "ONE_HANDED", "two_handed": "TWO_HANDED",
    "melee": "MELEE", "ranged": "RANGED", "projectile": "PROJECTILE",
    "attack": "ATTACK", "spell": "SPELL", "cold": "COLD", "fire": "FIRE",
    "lightning": "LIGHTNING", "chaos": "CHAOS", "physical": "PHYSICAL",
    "elemental": "ELEMENTAL", "poison": "POISON", "bleed": "BLEED",
    "ignite": "IGNITE", "area": "AREA", "trap": "TRAP", "mine": "MINE",
    "totem": "TOTEM", "minion": "MINION", "herald": "HERALD",
    "warcry": "WARCRY", "offering": "OFFERING", "banner": "BANNER",
    "aura": "AURA", "mark": "MARK", "curse": "CURSE", "brand": "BRAND",
    "slam": "SLAM", "thrust": "THRUST", "swipe": "SWIPE", "grenade": "GRENADE",
    "hazard": "HAZARD", "parry": "PARRY", "meta": "META", "infusion": "INFUSION",
}

NARROW = set("""
WYVERN BLIND POISON IGNITE TAKEN AXE SWORD GRENADE HAZARD META ONE_HANDED TWO_HANDED
PARRY CURSE EXPOSURE INCISION INFUSION JAGGED WITHERED FIRE COLD LIGHTNING CHAOS
PHYSICAL ELEMENTAL SPELL ATTACK BOW CROSSBOW CROSS_BOW WARCRY HERALD OFFERING BANNER
AURA MARK TRAP FLAIL SPEAR QUARTERSTAVE QUARTERSTAVES DAGGER BEAST CHARGE FLASK GROUND
ARCHON ARCANE_SURGE FORM WOLF BEAR ICE_CRYSTAL NON_DAMAGING DAMAGE_OVER_TIME CONSTRUCT
HUMANOID DEMON ENERGY_REGEN MANA_REGEN HEALTH_REGEN SET_VALUE REPLACE SNEAK FLYING
LEECH RECOUP LUCKY PROJECTILE PIERCE ELECTROCUT IMMUNE CONVERT GLORY VOLATIL PUPPET
REMNANT SCALE KNOCKBACK AFTERSHOCK BLOCK DODGE DEFLECTION CRIT_FORM CRIT_DAMAGE
CHARM CORPSE SUBMERGED JUMP RANGE RARITY FOUND_ITEM ENDURANCE FRENZY POWER
""".split())

COND_WORDS = ["while", "when", "against", "if you", "if you've", "if you haven",
              "enemies", "nearby", "in your presence", "per ", "with ", "wielding",
              "dual wielding", "recently", "you have", "you've", "from equipped",
              "on hit", "on melee", "on kill", "on crit", "during", "if "]

def norm(t): return re.sub(r"[^a-z]", "", t.lower())

def matched_attrs(concept):
    out = set()
    norms = [norm(c) for c in (concept or [])]
    joined = "".join(norms)
    for n in norms + [joined]:
        if n in SYN:
            out.add(SYN[n])
    return out

def clean_base_eff(attr, op):
    tok = OP_TOKEN.get(op)
    if not tok: return None
    best = None
    for g in eff_names:
        if tok not in g: continue
        if attr not in g.upper(): continue   # MUST relate to the attribute
        rem = g.split(tok + "_", 1)[1] if (tok + "_") in g else ""
        if any(nt in rem.upper() for nt in NARROW): continue
        if best is None or len(rem) < len(best[1]):
            best = (g, rem)
    return best[0] if best else None

def tagged_eff(attr, op, tags):
    tok = OP_TOKEN.get(op)
    if not tok: return None
    cand = []
    for g in eff_names:
        if tok not in g: continue
        gu = g.upper()
        # must relate to attribute
        if attr not in gu: continue
        # must contain ALL tag words
        if all(t in gu for t in tags):
            cand.append(g)
    if not cand: return None
    # prefer the one with fewest extra tokens beyond attr+tags
    def score(g):
        gu = g.upper()
        extra = gu
        for t in tags + [attr, tok]:
            extra = extra.replace(t, "")
        return len(extra)
    cand.sort(key=score)
    return cand[0]

def resolve_scope(scope):
    """Return ('tag', [tags]) or ('cond', (COND,TGT)) or ('mixed', ...) or None."""
    tags, conds = [], []
    for s in (scope or []):
        ns = norm(s)
        if ns in TAG_MAP: tags.append(TAG_MAP[ns])
        elif ns in COND_MAP: conds.append(COND_MAP[ns])
        else: return None  # unmappable scope -> defer
    if tags and not conds: return ("tag", tags)
    if conds and not tags: return ("cond", conds[0])
    if tags and conds: return ("mixed", (tags, conds[0]))
    return None

candidates = []
for r in wt:
    op = r.get("op_kind"); const = r.get("const")
    concept = r.get("concept") or []; scope = r.get("scope") or []
    tmpl = (r.get("template") or "").lower()
    if op not in OP_TOKEN: continue
    if any(w in tmpl for w in COND_WORDS): continue
    attrs = matched_attrs(concept)
    if not attrs or len(attrs) > 1: continue
    attr = max(attrs, key=len)
    sc = resolve_scope(scope)
    if sc is None:
        # unconditional -> clean base
        eff = clean_base_eff(attr, op)
        if eff is None: continue
        candidates.append({"const": const, "op": op, "attr": attr, "eff": eff,
                           "cond": None, "tgt": "TGT_SELF_ONLY", "template": r.get("template")})
    elif sc[0] == "tag":
        eff = tagged_eff(attr, op, sc[1])
        if eff is None: continue
        candidates.append({"const": const, "op": op, "attr": attr, "eff": eff,
                           "cond": None, "tgt": "TGT_SELF_ONLY", "template": r.get("template")})
    elif sc[0] == "cond":
        cond, tgt = sc[1]
        eff = clean_base_eff(attr, op)
        if eff is None: continue
        candidates.append({"const": const, "op": op, "attr": attr, "eff": eff,
                           "cond": cond, "tgt": tgt, "template": r.get("template")})
    elif sc[0] == "mixed":
        tags, (cond, tgt) = sc[1]
        eff = tagged_eff(attr, op, tags)
        if eff is None: continue
        candidates.append({"const": const, "op": op, "attr": attr, "eff": eff,
                           "cond": cond, "tgt": tgt, "template": r.get("template")})

# verify EFF/cond/tgt exist in source & dedupe
def in_src(name, file_tokens): return name in file_tokens
seen = set(); uniq = []
COND_NAMES = set(re.findall(r"COND_[A-Z0-9_]+", open(
    os.path.join(ROOT, "..", "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat", "StatConditionIds.java"), encoding="utf-8").read()))
TGT_NAMES = set(re.findall(r"TGT_[A-Z0-9_]+", open(
    os.path.join(ROOT, "..", "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat", "StatTargetIds.java"), encoding="utf-8").read()))
for c in candidates:
    if c["const"] in seen: continue
    if c["eff"] not in eff_names: continue
    if c["cond"] and c["cond"] not in COND_NAMES: continue
    if c["tgt"] not in TGT_NAMES: continue
    seen.add(c["const"]); uniq.append(c)

print("VERIFIED CANDIDATES:", len(uniq))
for c in uniq:
    tag = f" [{c['cond']},{c['tgt']}]" if c["cond"] else ""
    print(f"  {c['const']} | {c['attr']} -> {c['eff']}{tag}")
json.dump(uniq, open(os.path.join(ROOT, "phase3_candidates_verified.json"), "w"), indent=1, ensure_ascii=False)
