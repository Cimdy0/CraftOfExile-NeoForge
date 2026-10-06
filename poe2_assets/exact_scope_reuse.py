#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Corrected exact-scope scout (Phase 21).

Fixes the silent bug in harvest_widen.py: that script parsed ONLY StatGen.java,
but all wirings now live in wirings/Wire*.java -- so it reported every stat as
unwired and its candidate list was ~80% already wired.

This scout:
  * parses the TRUE wired set (StatGen.java + wirings/Wire*.java)
  * for each genuinely-unwired StatId, derives (attr, op, scope) from the name
  * strategy A  EXACT-SCOPE REUSE  -> existing EFF with (attr, op, tags)==scope
  * strategy B  NEW-EFF SUPERSET    -> existing EFF with (attr, op, tags) C scope,
                                      build a new EFF with tags:=scope (safe if
                                      op != ADD_MULTIPLIED_BASE on a base==0 attr)
Guards (R3 discipline):
  * corpus guard: every scope token must appear in the stat's game text
  * zero-base guard: no ADD_MULTIPLIED_BASE on a base==0 attribute
  * not(...) guard: never rebuild an EFF whose TagExpr uses not(...)
Read-only: writes exact_scope_reuse.json + prints; applies nothing.
"""
import io, re, os, json, glob

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
WIR = os.path.join(GEN, "wirings")
SUP = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "codec", "support")
def rd(p): return io.open(p, encoding="utf-8").read()

# ---------- true wired set ----------
all_wired = set()
for f in [os.path.join(GEN, "StatGen.java")] + \
          [os.path.join(WIR, x) for x in sorted(os.listdir(WIR)) if x.startswith("Wire") and x.endswith(".java")]:
    for m in re.finditer(r'map\.put\(StatIds\.(\w+),', rd(f)):
        all_wired.add(m.group(1))

# ---------- EFFs (both AttributeRegister and vanilla Attributes; ALWAYS + Set.of) ----------
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
by_key = {}
for n, v in effs.items():
    by_key.setdefault((v['attr'], v['op'], v['tags']), n)

# ---------- attribute base table (zero-base guard) ----------
base_tbl = json.load(open(os.path.join(ROOT, "poe2_assets", "attr_base_table.json"), encoding="utf-8"))
rows = base_tbl if isinstance(base_tbl, dict) else {r['attribute']: r for r in base_tbl}
def base_of(attr):
    v = rows.get(attr)
    return v.get('base') if isinstance(v, dict) else None

# ---------- corpus (corpus guard) ----------
corpus = json.load(open(os.path.join(ROOT, "poe2_assets", "node_stat_map.json"), encoding="utf-8"))
tmpl = {}
for node, stats in corpus.items():
    for s in stats:
        if s.get("constant") and s.get("template"):
            tmpl.setdefault(s["constant"], set()).add(s["template"])

# ---------- token maps (from harvest_widen) ----------
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
}
token_to_tag = {}
for tag, toks in tag_to_tokens.items():
    for t in toks: token_to_tag[t] = tag

ATTR_TOKENS = {
 'DAMAGE': {'DAMAGE'}, 'ARMOR': {'ARMOR'}, 'EVASION': {'EVASION'},
 'ENERGY_SHIELD': {'ENERGY_SHIELD','ENERGY'}, 'MAX_LIFE': {'MAXIMUM_LIFE','MAX_LIFE','LIFE'},
 'MAX_MANA': {'MAXIMUM_MANA','MAX_MANA','MANA'}, 'STRENGTH': {'STRENGTH'}, 'DEXTERITY': {'DEXTERITY'},
 'INTELLIGENCE': {'INTELLIGENCE'}, 'ATTACK_SPEED': {'ATTACK_SPEED'}, 'CAST_SPEED': {'CAST_SPEED'},
 'CRIT_CHANCE': {'CRIT_CHANCE','CRITICAL_CHANCE'}, 'CRIT_DAMAGE': {'CRIT_DAMAGE','CRITICAL_DAMAGE'},
 'ACCURACY': {'ACCURACY'}, 'MOVEMENT_SPEED': {'MOVEMENT_SPEED','MOVE_SPEED'}, 'BLOCK_CHANCE': {'BLOCK_CHANCE','BLOCK'},
 'DODGE': {'DODGE','DODGE_CHANCE'}, 'LIFE_REGEN': {'LIFE_REGEN','REGENERATION','REGEN'},
 'MANA_REGEN': {'MANA_REGEN'}, 'RAGE': {'RAGE'}, 'SPIRIT': {'SPIRIT'},
 'RESERVATION': {'RESERVATION','RESERVED'}, 'RESISTANCES': {'RESISTANCES','RESISTANCE'},
 'COLD_RESISTANCE': {'COLD_RESISTANCE','COLD_RES'}, 'FIRE_RESISTANCE': {'FIRE_RESISTANCE','FIRE_RES'},
 'LIGHTNING_RESISTANCE': {'LIGHTNING_RESISTANCE','LIGHTNING_RES'}, 'CHAOS_RESISTANCE': {'CHAOS_RESISTANCE','CHAOS_RES'},
}

NOT_EXPR = re.compile(r'StatTag\s*\.\s*not\s*\(')
FILLER = {"HAVE","HAS","DEAL","DEALS","IS","ARE","A","AN","THE","OF","TO","AND","OR","WITH","FOR",
          "IN","ON","AT","YOU","YOUR","USED","BY","SKILLS","SKILL","ALL","ANY","GAIN","GAINS","THAT",
          "WHICH","FROM","THEY","THEIR","WHEN","WHILE","IF","RECENTLY","LOW","FULL","EACH","PER","UP"}

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

def domain_of(attr):
    if attr in ("DAMAGE",) or attr.startswith("COE_DAMAGE") or attr in ("CRIT_CHANCE","CRIT_DAMAGE"): return "WireDamage"
    if attr in ("ARMOR","EVASION","ENERGY_SHIELD","BLOCK_CHANCE","DODGE","ACCURACY") or attr.startswith("COE_") and "RESISTANCE" in attr: return "WireDefense"
    if attr in ("ATTACK_SPEED","CAST_SPEED","MOVEMENT_SPEED"): return "WireSpeed"
    if attr in ("MAX_LIFE","MAX_MANA","STRENGTH","DEXTERITY","INTELLIGENCE","RAGE","SPIRIT","RESERVATION",
                "LIFE_REGEN","MANA_REGEN") or "REGEN" in attr or "RESERV" in attr: return "WireResource"
    if "AILMENT" in attr or attr in ("BLEED","POISON","IGNITE","CHILL","FREEZE","SHOCK","BURN"): return "WireAilment"
    return "WireMisc"

statids = set(re.findall(r'public static final Identifier (\w+) =', rd(os.path.join(SUP, "stat", "StatIds.java"))))
unwired = sorted(statids - all_wired)

reuse, new_eff, skipped = [], [], []
for u in unwired:
    op = op_for_prefix(u)
    if op is None: continue
    # find candidate attribute: any ATTR_TOKENS key whose tokens appear in u
    attrs = [a for a in ATTR_TOKENS if refers_attr(u, a)]
    if not attrs:
        # also allow raw EFF attr names that are single-word in the stat
        continue
    uscope = scope_of(u)
    if not uscope: continue  # pure base stat with no scope -> not a scope variant
    ut = " ".join(tmpl.get(u, ())).lower()
    for attr in attrs:
        # exact-scope reuse
        key = (attr, op, frozenset(uscope))
        if key in by_key:
            eff = by_key[key]
            # corpus guard
            if not all(re.search(r'\b'+re.escape(t.lower())+r'\b', ut) for t in uscope) and tmpl.get(u):
                skipped.append((u, eff, "corpus guard: scope tokens absent from game text"))
                continue
            reuse.append(dict(sid=u, eff=eff, domain=domain_of(attr), scope=sorted(uscope), note="exact-scope reuse"))
            break
        # new-eff superset: existing EFF with same (attr,op) and tags C uscope (strict)
        cands = [(n, v) for n, v in effs.items() if v['attr']==attr and v['op']==op and v['tags'] < frozenset(uscope)]
        cands = [(n, v) for n, v in cands if not NOT_EXPR.search(
            next((ln for ln in eff_text.split("\n") if re.search(r'StatEffectIds\.'+re.escape(n)+r'\s*,', ln)), ""))]
        if cands:
            # pick the one whose tags are largest subset (closest)
            base_n, base_v = max(cands, key=lambda cv: len(cv[1]['tags']))
            if op == "ADD_MULTIPLIED_BASE" and (base_of(attr) == 0.0 or base_of(attr) is None and base_of(base_v['attr'])==0.0):
                skipped.append((u, base_n, f"zero-base guard: {attr} base==0"))
                continue
            # corpus guard
            if tmpl.get(u) and not all(re.search(r'\b'+re.escape(t.lower())+r'\b', ut) for t in uscope):
                skipped.append((u, base_n, "corpus guard: scope tokens absent from game text"))
                continue
            new_eff.append(dict(sid=u, attr=base_v['attr'], qual=base_v['qual'], op=op,
                                mode=base_v['mode'], tags=sorted(uscope), domain=domain_of(attr),
                                base=base_n, note="new EFF (superset scope)"))
            break

print(f"true wired = {len(all_wired)}   unwired = {len(unwired)}")
print(f"\n=== EXACT-SCOPE REUSE: {len(reuse)} ===")
for r in reuse: print(f"  {r['sid']:55} -> {r['eff']}  [{r['domain']}]")
print(f"\n=== NEW-EFF SUPERSET: {len(new_eff)} ===")
for r in new_eff: print(f"  {r['sid']:45} attr={r['attr']} op={r['op']} tags={r['tags']}  [{r['domain']}]  base={r['base']}")
print(f"\n=== SKIPPED sample ({len(skipped)}) ===")
seen=set()
for u, e, why in skipped:
    k=why.split(":")[0]
    if k in seen: continue
    seen.add(k); print(f"  {k}  e.g. {u}")

json.dump({"reuse": reuse, "new_eff": new_eff,
           "skipped": [dict(sid=u, base=e, why=w) for u, e, w in skipped]},
          open(os.path.join(ROOT, "poe2_assets", "exact_scope_reuse.json"), "w"), indent=1, ensure_ascii=False)
print("\nwrote poe2_assets/exact_scope_reuse.json")
