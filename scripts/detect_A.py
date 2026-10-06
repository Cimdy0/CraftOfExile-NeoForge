import re, os, json, collections
ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
SID = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java")
EFFID = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatEffectIds.java")
GEN = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatEffectGen.java")
WIR = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/wirings")

statids = dict(re.findall(r'public static final Identifier (\w+) = id\("([^"]+)"\)', open(SID, encoding="utf-8").read()))
wired = set()
for fp in os.listdir(WIR):
    if fp.startswith("Wire") and fp.endswith(".java"):
        for m in re.finditer(r'map\.put\(StatIds\.(\w+)', open(os.path.join(WIR,fp), encoding="utf-8").read()):
            wired.add(m.group(1))
unwired = [k for k in statids if k not in wired]

# parse StatEffectGen: (eff_const_name, eff_id_string, attribute, op, mode, tags)
gen = open(GEN, encoding="utf-8").read()
effrec = {}  # const name -> dict
for m in re.finditer(r'm\.put\(StatEffectIds\.(\w+),\s*new\s+(\w+)\((.*?)\)\s*\)', gen, re.S):
    const, cls, args = m.group(1), m.group(2), m.group(3)
    am = re.search(r'AttributeRegister\.(\w+)', args)
    attr = am.group(1) if am else None
    om = re.search(r'Operation\.(\w+)', args)
    op = om.group(1) if om else None
    mm = re.search(r'Mode\.(\w+)', args)
    mode = mm.group(1) if mm else None
    tags = set(re.findall(r'StatTag\.(\w+)', args))
    effrec[const] = {"attr":attr,"op":op,"mode":mode,"tags":tags}
# also map const name -> id string from StatEffectIds
idstr = dict(re.findall(r'public static final Identifier (\w+) = id\("([^"]+)"\)', open(EFFID, encoding="utf-8").read()))

# keyword -> attribute
ATTRKW = [
 ("LAUNCH_ADDITIONAL_PROJECTILE",["additional_projectile","fire_an_additional","additional_arrow","additional_projectile"]),
 ("PROJECTILE_PIERCE",["projectile_pierce","pierce"]),
 ("PROJECTILE_PIERCE_CHANCE",["pierce_chance"]),
 ("DEFLECTION_RATING",["deflection"]),
 ("MAX_RESISTANCES",["max_resistances","resistances"]),
 ("RESISTANCES",["resistances"]),
 ("EVASION",["evasion"]),
 ("ACCURACY",["accuracy"]),
 ("CRIT_CHANCE",["crit_chance","critical_hit_chance"]),
 ("CRIT_DAMAGE",["crit_damage","critical_damage"]),
 ("EFFECT_AREA",["area_of_effect","area_of_effect"]),
 ("DAMAGE",["inc_damage","more_damage","deal_inc_damage","damage_with","damage_for_each"]),
 ("KNOCKBACK",["knockback_distance","knockback"]),
 ("KNOCKBACK_CHANCE",["knockback_chance","chance_to_knock"]),
 ("BLOCK_CHANCE",["block_chance","block_recovery"]),
 ("DODGE_CHANCE",["dodge"]),
 ("CAST_SPEED",["cast_speed"]),
 ("MAX_RAGE",["max_rage","to_max_rage"]),
 ("MAX_VOLATILITY",["max_volatility"]),
 ("MAX_ENERGY_SHIELD",["max_energy_shield"]),
 ("MAX_MANA",["max_mana"]),
 ("MAX_SPIRIT",["max_spirit"]),
 ("MAX_CURSES",["max_curses"]),
 ("MAX_INFUSION",["max_infusion","infusion"]),
 ("RESERVATION_EFFICIENCY",["reservation_efficiency"]),
 ("MANA_COST_EFFICIENCY",["cost_efficiency","mana_cost"]),
 ("LEECH",["leech"]),
 ("AMOUNT_LEECHED",["amount_leech","leech_the_same","leech_health"]),
 ("LEECH_RATE",["leech_rate","leech_health_faster","leech_health_slower"]),
 ("REGENERATION_RATE",["regeneration_rate","regen_rate"]),
 ("RECOVERY_RATE",["recovery_rate","recover_of","recoup"]),
 ("MAX_REGENERATION",["max_regeneration","regenerate_of_max"]),
 ("ENERGY_RECHARGE",["energy_recharge","energy_shield_recharge"]),
 ("AILMENT_DURATION",["ailment_duration","bleeding_duration","blind_duration","hinder_duration","ailments_ailment_application"]),
 ("AILMENT_MAGNITUDE",["ailment_magnitude","impales_inflicted","shock_magnitude","ailments","incision"]),
 ("AILMENT_BUILDUP",["ailment_buildup"]),
 ("STUN_RECOVERY",["stun_recovery","heavy_stun_buildup","stun"]),
 ("ARMOR_BREAK",["armor_break"]),
 ("GUARD_RECOVERY",["guard"]),
 ("BANNER_LINGER",["banner_linger","banner"]),
 ("PRESENCE_RANGE",["presence"]),
 ("STATUS_MAX_STACKS",["status_max_stacks","stacks_of_puppet"]),
 ("CHARM_EFFECT",["charm"]),
 ("MINECRAFT_MAX_HEALTH",["max_health"]),
 ("VOLATILITY_GENERATION",["volatility_generation"]),
 ("EFFECT_AREA",["area_of_effect"]),
]
OPKW = [("ADD_VALUE",["to_max_","to_level_","add_","to_","gain_","of_max_","max_health_is","max_volatility_is","surpass"]),
        ("ADD_MULTIPLIED_BASE",["inc_","inc_damage","inc_crit","inc_accuracy","inc_evasion","inc_area","inc_cast","inc_block","inc_armor","inc_movement","inc_mana","inc_health","inc_lightning","inc_resistances"]),
        ("ADD_MULTIPLIED_TOTAL",["more_","more_damage","more_max","more_reservation","more_knockback","more_max_lightning"])]

def find_attr(u):
    ul = u.lower()
    for attr, kws in ATTRKW:
        for kw in kws:
            if kw in ul: return attr
    return None

def find_op(u):
    ul = u.lower()
    for op, kws in OPKW:
        for kw in kws:
            if kw in ul: return op
    return None

results = []
for u in unwired:
    attr = find_attr(u)
    if not attr: continue
    op = find_op(u)
    # find EFFs on this attr
    cand = [c for c,d in effrec.items() if d["attr"]==attr]
    if not cand: 
        results.append((u, attr, op, None, "NO EFF on attr (B/C)"))
        continue
    # prefer op match; then unscoped
    best = None
    for c in cand:
        d = effrec[c]
        if d["op"]==op:
            best = c; break
    if best is None:
        # pick unscoped general
        for c in cand:
            if not effrec[c]["tags"]:
                best = c; break
        if best is None: best = cand[0]
    results.append((u, attr, op, best, idstr.get(best,"?")))

print(f"{'UNWIRED':55s} ATTR -> EFF (op match)  tags")
for u, attr, op, best, sidstr in results:
    if best:
        d = effrec[best]
        print(f"{u:55s} {attr:22s} {best}\n      op_req={op} eff_op={d['op']} mode={d['mode']} tags={sorted(d['tags'])}")
    else:
        print(f"{u:55s} {attr:22s} -- {best}")
print("\nTotal unwired with attribute match:", len(results), " / ", len(unwired))
