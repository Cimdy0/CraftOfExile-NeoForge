#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Build implicit_stat_mapping.md from all_skills.json (read-only on source)."""
import json, re, os, collections

SRC = os.path.join(os.path.dirname(__file__), "all_skills.json")
OUT = os.path.join(os.path.dirname(__file__), "implicit_stat_mapping.md")

with open(SRC, encoding="utf-8") as f:
    data = json.load(f)

entries = data["entries"]

# --- collect unique strings from implicits + stats -------------------------
seen = collections.OrderedDict()  # string -> sources set
for e in entries:
    for key in ("implicits", "stats"):
        for s in (e.get(key) or []):
            s = s.strip()
            if not s:
                continue
            seen.setdefault(s, set()).add(key)

all_unique = list(seen.keys())
total = len(all_unique)

ENGINE_RE = re.compile(r"\[\s*\d+\s*\]")  # PoE2 engine tags like "is area damage [1]"
UNDERSCORE_STAT_RE = re.compile(r"^[a-z0-9_+%]+$")  # internal stat ids: fire_damage_+% , block_chance_+%
def is_engine(s):
    if ENGINE_RE.search(s):
        return True
    # lowercase underscore/percent stat identifiers (no spaces) are engine internals
    if "_" in s and UNDERSCORE_STAT_RE.match(s):
        return True
    return False
engine = [s for s in all_unique if is_engine(s)]
mech = [s for s in all_unique if not is_engine(s)]   # mechanism strings

# --- ordered keyword classifier (first match wins) -------------------------
# rule = (family, [substrings], verdict, target_mod_mechanism, notes)
RULES = [
    ("block_evade", ["block chance", "block", "evade", "evaded"], "MISSING",
     "New block / evasion mechanic",
     "No block chance or evasion/accuracy-evade mechanic in inventory."),
    ("max_stat", ["maximum life", "maximum energy", "maximum es", "max life",
                  "maximum life", "maximum energy shield"], "MISSING",
     "New maximum-stat modifier (max Life / ES)",
     "Maximum Life / Energy Shield values are not modelled as modifiable stats."),
    ("rage_berserk", ["rage", "berserk"], "MISSING", "New rage / berserk resource mechanic",
     "Rage and berserk resources & their effects are not in inventory."),
    ("skill_geometry", ["branch", "length is", "wave length", "creates", "fissures",
                        "wall segments", "segments have", "spikes", "width is",
                        "length cone", "metre length", "hits allowed", "number of hits"], "MISSING",
     "New skill-geometry stat (branch count / length / segments)",
     "Projectile/beam/fissure geometry (branch, length, segment count, width) not modelled."),
    ("curse", ["curse", "curses"], "MISSING", "New curse mechanic",
     "No curse application/removal mechanic in inventory."),
    ("life_recovery", ["regenerate", "recovers", "recovery", "life regen", "regen",
                       "life regeneration"], "MISSING", "New life-recovery / regen modifier",
     "Life regeneration & on-hit recovery not modelled as stats."),
    ("ailment_threshold", ["ailment threshold", "ailment threshold"], "MISSING",
     "New ailment-threshold modifier",
     "Ailment threshold (reduced/inc threshold) not in inventory."),
    ("dual_wield", ["dual wielding", "dual wield"], "MISSING",
     "New StatTag / conditional modifier for dual-wield state",
     "No dual-wield concept in inventory; needs a `dual_wield` condition flag + penalty/bonus modifier."),
    ("melee_range", ["melee strike range", "melee range", "strike range",
                     "step distance", "maximum action distance", "attack distance",
                     "weapon range"],
     "PARTIAL", "StatTag `melee_range` / `attack_distance`",
     "Range is not an explicit EffectMod subfield today; modelable via data-driven StatTag but needs a dedicated range stat."),
    ("stun_buildup", ["stun buildup", "stun", "heavy stun", "heavy stunned"], "COVERED",
     "StatusDefs `stun` (+ heavy-stun flag)",
     "Stun buildup and heavy-stun states map to existing stun status; magnitude scaled via status intensity."),
    ("knockback", ["knock back", "knocks back", "knockback"], "MISSING",
     "New knockback modifier / StatTag",
     "No knockback mechanic in inventory."),
    ("impale", ["impale"], "MISSING", "New impale mechanic",
     "No impale / projectile-impale behaviour in inventory."),
    ("pierce_chain_fork", ["pierce", "chain", "fork"], "MISSING",
     "New projectile-behaviour system",
     "No pierce/chain/fork handling in EffectMod or projectile model."),
    ("projectile_count", ["projectile", "fires", "nova", "additional projectile", "projectiles"], "MISSING",
     "New `projectile_count` / `nova` stat",
     "Projectile count and nova patterns are not represented in inventory."),
    ("minion_summon", ["minion", "summon", "ally", "companion", "spectre", "totem"], "MISSING",
     "New minion / summon system",
     "No minion or summoned-entity framework in inventory."),
    ("leech", ["leech", "life leech", "mana leech"], "MISSING",
     "New leech mechanic",
     "No life/mana leech modifier in inventory."),
    ("armour_break", ["break armour", "armour break", "sunder", "armour reduction",
                      "hits break", "break ("], "MISSING",
     "New armour-break / armour stat",
     "No armour or armour-break mechanic in inventory."),
    ("ground_effect", ["ground", "jagged", "burning ground", "patch", "chilled ground"],
     "MISSING", "New ground-effect / area-debuff system",
     "Area ground effects (Jagged Ground, Burning Ground) need a new ground-effect mechanic."),
    ("limit_count", ["limit (", "maximum (", "limit of", "can have up to", "limit:",
                     "limit ", "cannot have", "cannot have more than", "maximum "], "MISSING",
     "New entity-limit modifier",
     "Caps on simultaneous entities (ground patches, corpses, mirages) not modelled."),
    ("trigger", ["trigger", "detonate", "detonates", "triggered by"], "MISSING",
     "New trigger / detonate mechanic",
     "Skill-to-skill triggering / corpse detonation not in inventory."),
    ("thorns_reflect", ["reflected", "thorns", "reflect", "cannot be reflected"], "MISSING",
     "New reflect / thorns mechanic",
     "Damage reflection / thorns not represented."),
    ("stance", ["stance"], "MISSING", "New stance system",
     "No combat-stance switching in inventory."),
    ("damage_taken", ["damage taken", "more damage taken", "takes ", "damage dealt by enemies"], "MISSING",
     "New taken-multiplier modifier",
     "Incoming-damage multipliers (more/less damage taken) not in EffectMod."),
    ("movement_speed", ["movement speed", "movement speed +", "movement speed %"], "PARTIAL",
     "StatTag `movement_speed`",
     "Movement-speed modifiers appear often but no movement-speed subfield in EffectMod; needs a StatTag or new subfield."),
    ("reservation_spirit", ["reserve", "spirit", "reserves"], "PARTIAL",
     "cost field / StatTag `spirit_reservation`",
     "Spirit resource & reservation model absent from inventory; mana cost field exists but spirit needs extension."),
    ("life_cost", ["costs x life", "life cost", "costs life", "life as", "lifetap"], "PARTIAL",
     "cost field (currently mana-focused)",
     "Life-cost / life-tap variants need an extension beyond the mana cost field."),
    ("buff_empower", ["empower", "empowered", "empowers", "imbuement"], "PARTIAL",
     "StatusDefs buff / StatTag `buff`",
     "Generic empower / buff-grant behaviour not modelled as a distinct mechanism."),
    ("exposure", ["exposure"], "COVERED", "StatusDefs `exposure_*` + EffectMod.exposure",
     "Elemental exposure maps directly to exposure statuses and EffectMod.exposure."),
    ("penetration", ["penetrat", "penetrates", "penetration"], "COVERED", "EffectMod.penetration",
     "Damage penetration maps to EffectMod.penetration."),
    ("conversion", ["convert", "as extra", "of damage as extra", "gain", "added as"], "COVERED",
     "EffectMod.convert / added_damage",
     "Damage-type conversion and 'gain as extra X' map to EffectMod.convert / added_damage."),
    ("added_damage", ["adds", "added damage", "deals (", "to (", "damage to", "added "], "COVERED",
     "EffectMod.added_damage / DamageTypeRegistry",
     "Flat added damage & direct hits map to EffectMod.added_damage and the 5-element DamageTypeRegistry (+DoT splits)."),
    ("ailment_application", ["ignite", "poison", "bleed", "chill", "freeze",
                            "electrocute", "scorch", "plant", "shock ", "shock,",
                            "shocked", "shocks", "shock chance", "apply shock",
                            "applies shock", "shock enemies", "shock buildup"], "COVERED",
     "StatusDefs (ignite/poison/bleed/chill/freeze/shock/electrocute)",
     "Core ailments map directly to StatusDefs."),
    ("status_effect_utility", ["blind", "daze", "maim", "slow", "hinder", "wither", "fortify",
                               "taunt", "petrify", "pin", "onslaught", "intimidate", "unnerve"],
     "COVERED", "StatusDefs (blind/daze/maim/slow/hinder/wither/fortify/taunt/petrify/pin/onslaught/...)",
     "Utility statuses map to the 70-status StatusDefs inventory."),
    ("charges", ["charge", "power charge", "frenzy charge", "endurance charge"], "COVERED",
     "StatusDefs (power_charge/frenzy_charge/endurance_charge) + coe:charge_scale",
     "Charge generation/consumption maps to StatusDefs charges and the coe:charge_scale template."),
    ("forms", ["form", "avatar", "become", "metamorph"], "COVERED", "StatusDefs `form_*`",
     "Avatar/form transformations map to the form_* statuses."),
    ("suppress", ["suppress", "suppression"], "COVERED", "EffectMod.suppress",
     "Spell/ crit suppression maps to EffectMod.suppress."),
    ("crit", ["crit", "critical"], "COVERED", "EffectMod.crit_chance_multiplier / crit_damage_multiplier",
     "Crit chance & crit damage map directly to EffectMod crit subfields."),
    ("duration", ["duration", "lasts", "seconds", "for "], "COVERED",
     "duration modifier / StatTag duration",
     "Buff/debuff durations map to a duration modifier or StatTag; many use coe:per_level/quality templates."),
    ("attack_speed", ["attack speed", "attack time", "total attack time"], "COVERED",
     "EffectMod.more/less with `attack_speed` StatTag",
     "Attack-speed changes map via more/less with an attack_speed tag."),
    ("cast_speed", ["cast speed"], "COVERED", "EffectMod.more/less with `cast_speed` StatTag",
     "Cast-speed changes map via more/less with a cast_speed tag."),
    ("more_damage", ["more damage", "% more", "more %", "more dmg", "more fire damage",
                     "more damage with", "more physical"], "COVERED", "EffectMod.more",
     "Increased/more damage maps directly to EffectMod.more."),
    ("less_damage", ["less damage", "less %", "less dmg", "less damage while"], "COVERED",
     "EffectMod.less", "Reduced/less damage maps directly to EffectMod.less."),
    ("increased_reduced", ["increased", "reduced", "% increased", "% reduced"], "MISSING",
     "New additive `increased`/`reduced` layer (distinct from `more`/`less`)",
     "EffectMod only carries multiplicative `more`/`less`. The additive %increased/%reduced "
     "layer (the bulk of PoE2 buff wording) has no home — needs a new additive stat layer."),
    ("accuracy", ["accuracy", "accuracy rating"], "MISSING", "New accuracy / accuracy-rating stat",
     "Accuracy rating & accuracy scaling are not in the inventory."),
    ("area_radius_aoe", ["radius", "area of effect", "area damage", "area "], "PARTIAL",
     "StatTag `area_radius` / `aoe`",
     "Radii and AoE size appear constantly but EffectMod has no radius subfield; needs a StatTag or new subfield."),
    ("mana_cost", ["mana cost", "mana", "cost"], "COVERED", "cost field / StatTag mana_cost",
     "Mana cost maps to the cost field / mana_cost StatTag."),
    ("attribute_requirement", ["requires", "strength", "intelligence", "dexterity",
                               "attribute"], "COVERED", "req_attr / StatTag attribute_req",
     "Attribute requirements map to req_attr."),
    ("level_requirement", ["requires level", "level requirement", "level range"], "COVERED",
     "req_level / StatTag level_req", "Level requirements map to req_level."),
    ("cooldown", ["cooldown"], "COVERED", "cooldown field / StatTag cooldown",
     "Cooldown maps to the cooldown field."),
    ("cast_time", ["cast time", "casttime", "cast_time"], "COVERED",
     "cast_time field / StatTag cast_time", "Cast time maps to the cast_time field."),
]

RULE_INDEX = [(r[0], [k.lower() for k in r[1]], r[2], r[3], r[4]) for r in RULES]

def classify(s):
    low = s.lower()
    for fam, keys, verdict, target, notes in RULE_INDEX:
        for k in keys:
            if k in low:
                return fam
    return None

fam_counts = collections.Counter()
fam_examples = collections.defaultdict(list)
fam_sources = collections.defaultdict(set)
uncat = []
for s in mech:
    fam = classify(s)
    if fam is None:
        uncat.append(s)
        continue
    fam_counts[fam] += 1
    fam_sources[fam] |= seen[s]
    if len(fam_examples[fam]) < 4:
        fam_examples[fam].append(s)

# verdict tally from rules
verdict_of = {r[0]: r[2] for r in RULES}
fam_order = [r[0] for r in RULES]
# add any families that appeared but weren't in rule order (none expected)
for fam in fam_counts:
    if fam not in fam_order:
        fam_order.append(fam)

tally = collections.Counter(verdict_of.get(f, "??") for f in fam_counts)

# --- write markdown (CRLF preserved) ---------------------------------------
lines = []
def w(t=""):
    lines.append(t)

w("# Implicit / Stat String Mapping — Preparatory Blueprint (M3/M4)")
w()
w("> **Purpose.** This is a *mapping blueprint*, not data. The PoE2 crawl "
  "`implicits`/`stats` are English free text and cannot be consumed by the mod "
  "directly. They must be translated into CraftOfExile NeoForge stat / modifier / "
  "status definitions. This document clusters every unique string into **mechanism "
  "families** and judges, against the already-surveyed mod inventory, whether each "
  "family is **COVERED**, **PARTIAL**, or **MISSING** — so that the M3 (stat "
  "authoring) and M4 (modifier wiring) phases proceed from a complete plan rather "
  "than ad-hoc patches.")
w()
w("## Source & method")
w()
w(f"- **Source:** `design/refactor/skill-data/crawled/all_skills.json` "
  f"({len(entries)} entries).")
w(f"- **Unique strings analysed (implicits + stats, deduped):** {total}")
w(f"  - PoE2 engine-internal tags (e.g. `is area damage [1]`, "
  f"`display statset hide usage stats [1]`): **{len(engine)}** — excluded from "
  f"mechanism families (they are engine UI/internals, not ported mechanics).")
w(f"  - Mechanism strings (the focus of this artifact): **{len(mech)}**.")
w(f"- **Clustering:** rule-based keyword classification (leading-token / keyword), "
  f"first match wins, ordered most-specific-first. "
  f"**{len(fam_counts)} mechanism families** emerged covering the bulk of strings.")
w(f"- **Unmapped mechanism strings:** {len(uncat)} (long-tail / one-off phrasings; "
  f"see appendix).")
w(f"- **Mod inventory referenced:** `StatTag` (data-driven string tags), "
  f"`EffectMod` (delivery/damage_type/condition/more/less/"
  f"damage_multiplier_per_split/crit_chance_multiplier/crit_damage_multiplier/"
  f"suppress/added_damage/convert/exposure/penetration/slow), `StatusDefs` (70 "
  f"status types), `DamageTypeRegistry` (5 elements + DoT splits), expression "
  f"templates (coe:per_level_add_value / coe:per_quality_add_value / "
  f"coe:level_table / coe:charge_scale).")
w()
w("## Coverage legend")
w()
w("- **COVERED** — an existing mod stat / modifier / status clearly matches.")
w("- **PARTIAL** — exists but needs a data/stat extension (a new `StatTag` or "
  "EffectMod subfield) before it can carry the PoE2 value.")
w("- **MISSING** — no existing mechanism; requires a new stat / modifier / system "
  "to be built in the M-phase.")
w()
w("## Family table")
w()
w("| # | Family | Count | Coverage | Example strings (2–3) | Target mod mechanism / notes |")
w("|---|--------|------:|----------|------------------------|------------------------------|")
idx = 0
for fam in fam_order:
    if fam_counts.get(fam, 0) == 0:
        continue
    idx += 1
    v = verdict_of.get(fam, "??")
    ex = fam_examples[fam][:3]
    ex_str = "; ".join(ex)
    if len(ex_str) > 160:
        ex_str = ex_str[:157] + "..."
    note = ""
    for r in RULES:
        if r[0] == fam:
            note = f"{r[3]} — {r[4]}"
            break
    w(f"| {idx} | {fam} | {fam_counts[fam]} | **{v}** | {ex_str} | {note} |")
w()
w("## Summary tallies")
w()
w(f"- **Families:** {len(fam_counts)}")
w(f"- **Mechanism strings classified:** {sum(fam_counts.values())} of {len(mech)}")
w(f"- **COVERED:** {tally.get('COVERED',0)} families")
w(f"- **PARTIAL:** {tally.get('PARTIAL',0)} families")
w(f"- **MISSING:** {tally.get('MISSING',0)} families")
w()
w("### Most important MISSING families (build-first in M-phase)")
w()
missing = [f for f in fam_order if verdict_of.get(f) == "MISSING" and fam_counts.get(f,0) > 0]
missing.sort(key=lambda f: -fam_counts[f])
for f in missing:
    w(f"- **{f}** ({fam_counts[f]} strings) — needs: {next(r[3] for r in RULES if r[0]==f)}")
w()
w("### PARTIAL families (extend existing system before reuse)")
w()
partial = [f for f in fam_order if verdict_of.get(f) == "PARTIAL" and fam_counts.get(f,0) > 0]
partial.sort(key=lambda f: -fam_counts[f])
for f in partial:
    w(f"- **{f}** ({fam_counts[f]} strings) — extend: {next(r[3] for r in RULES if r[0]==f)}")
w()
w("## Appendix — unmapped mechanism strings (long tail)")
w()
for s in uncat[:120]:
    w(f"- {s}")
if len(uncat) > 120:
    w(f"- … and {len(uncat)-120} more.")
w()

text = "\r\n".join(lines) + "\r\n"
with open(OUT, "w", encoding="utf-8", newline="") as f:
    f.write(text)

# console report
print("total_unique:", total)
print("engine_tags:", len(engine))
print("mechanism_strings:", len(mech))
print("families:", len(fam_counts))
print("classified:", sum(fam_counts.values()), "unmapped:", len(uncat))
print("COVERED/PARTIAL/MISSING families:", dict(tally))
print("--- top families ---")
for fam, c in fam_counts.most_common(15):
    print(f"  {fam:22s} {c:4d}  {verdict_of.get(fam)}")
print("--- unmapped sample (first 40) ---")
for s in uncat[:40]:
    print("  ?", s)
