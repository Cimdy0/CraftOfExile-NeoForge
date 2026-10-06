#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Bulk-wire scope-tag variants + direct-reuse candidates (Phase 20).

Two patterns, both pure data (no engine change):

A) SCOPE VARIANT   <WIRED_BASE>_WITH_<SCOPE>
   New EFF = base EFF's (attribute, operation, mode) with tags := base.tags | {SCOPE}.
   If an EFF with exactly that (attr, op, tags) already exists it is REUSED (never
   duplicated).

B) DIRECT REUSE
   An existing EFF already expresses the concept; the StatId was simply never bound.

Every entry below was verified against the corpus (raw game text) before being
added. R3 still applies: nothing here is guessed from the StatId name alone.
"""
import io, re, os

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
WIR = os.path.join(GEN, "wirings")
def rd(p): return io.open(p, encoding="utf-8").read()
def wr(p, s): io.open(p, "w", encoding="utf-8").write(s)

# ---------- parse existing EFFs ----------
eff_text = rd(os.path.join(GEN, "StatEffectGen.java"))
effs = {}
for m in re.finditer(r'm\.put\(StatEffectIds\.(\w+),\s*new ModifierStatEffect\((.*?)\)\);', eff_text, re.S):
    am = re.search(r'(AttributeRegister|Attributes)\.(\w+)', m.group(2))
    om = re.search(r'AttributeModifier\.Operation\.(\w+)', m.group(2))
    md = re.search(r'ModifierStatEffect\.Mode\.(\w+)', m.group(2))
    tg = (frozenset() if 'TagExpr.ALWAYS' in m.group(2)
          else frozenset(re.findall(r'StatTag\.(\w+)',
                         (re.search(r'TagExpr\.of\(Set\.of\((.*?)\)\)', m.group(2), re.S) or [None, ''])[1])))
    if am and om:
        # keep the qualifier: vanilla Minecraft attributes live on `Attributes`,
        # mod attributes on `AttributeRegister`. Assuming AttributeRegister for
        # everything produces code that does not compile.
        effs[m.group(1)] = dict(attr=am.group(2), qual=am.group(1), op=om.group(1),
                                mode=(md.group(1) if md else 'GAIN'), tags=tg)

by_key = {}
for n, v in effs.items():
    by_key.setdefault((v['attr'], v['op'], v['tags']), n)

# ---------- spec ----------
ATKSP = 'EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_SPEED'
CRITC = 'EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE'
CRITD = 'EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_CRIT_DAMAGE_CRIT'
ACCU  = 'EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ACCURACY'
STUNB = 'EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AILMENT_BUILDUP_STUN'
BLDUP = 'EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AILMENT_BUILDUP'
BOWDMG= 'EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_BOW_COE_DAMAGE_DAMAGE'
ATKDMG= 'EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_DAMAGE'
SPELLD= 'EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_SPELL'
CDR   = 'EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_COOLDOWN_RECOVERY_RATE'

# (statId, baseEFF, extraTags, domain, note)
SCOPE = [
 ("INC_ATTACK_SPEED_WITH_BOWS",        ATKSP, ("BOW",),        "WireSpeed",   "持弓时攻击速度"),
 ("INC_ATTACK_SPEED_WITH_AXES",        ATKSP, ("AXE",),        "WireSpeed",   "持斧时攻击速度"),
 ("INC_ATTACK_SPEED_WITH_CROSSBOWS",   ATKSP, ("CROSSBOW",),   "WireSpeed",   "持弩时攻击速度"),
 ("INC_ATTACK_SPEED_WITH_SPEARS",      ATKSP, ("SPEAR",),      "WireSpeed",   "持长矛时攻击速度"),
 ("INC_ATTACK_SPEED_WITH_SWORDS",      ATKSP, ("SWORD",),      "WireSpeed",   "持剑时攻击速度"),
 ("INC_MELEE_ATTACK_SPEED",            ATKSP, ("MELEE",),      "WireSpeed",   "近战攻击速度"),
 ("INC_CRITICAL_HIT_CHANCE_WITH_CROSSBOWS", CRITC, ("CROSSBOW",), "WireDamage", "持弩时暴击率"),
 ("INC_CRITICAL_HIT_CHANCE_WITH_SPEARS",    CRITC, ("SPEAR",),    "WireDamage", "持长矛时暴击率"),
 ("INC_CRITICAL_HIT_CHANCE_FOR_SPELLS",     CRITC, ("SPELL",),    "WireDamage", "法术暴击率"),
 ("INC_CRITICAL_HIT_CHANCE_WITH_TRAPS",     CRITC, ("TRAP",),     "WireDamage", "陷阱暴击率"),
 ("INC_CRITICAL_DAMAGE_BONUS_WITH_CROSSBOWS", CRITD, ("CROSSBOW",), "WireDamage", "持弩时暴击伤害加成"),
 ("INC_CRITICAL_DAMAGE_BONUS_WITH_SPEARS",    CRITD, ("SPEAR",),    "WireDamage", "持长矛时暴击伤害加成"),
 ("INC_ACCURACY_WITH_SPEARS",          ACCU,  ("SPEAR",),      "WireDefense", "持长矛时命中"),
 ("INC_STUN_BUILDUP_WITH_MACES",       STUNB, ("MACE",),       "WireAilment", "持锤时眩晕积累"),
 ("INC_PROJECTILE_STUN_BUILDUP",       STUNB, ("PROJECTILE",), "WireAilment", "抛射物眩晕积累"),
 ("INC_BALLISTA_IMMOBILISATION_BUILDUP", BLDUP, ("BALLISTA",), "WireAilment", "弩炮定身积累"),
 ("INC_HAZARD_IMMOBILISATION_BUILDUP",    BLDUP, ("HAZARD",),   "WireAilment", "危害定身积累"),
 ("INC_PHYSICAL_DAMAGE_WITH_BOWS",     BOWDMG, ("PHYSICAL",),  "WireDamage",  "弓类物理伤害"),
 ("INC_ATTACK_COLD_DAMAGE",            ATKDMG, ("COLD",),      "WireDamage",  "攻击冰霜伤害"),
 ("INC_SPELL_AREA_DAMAGE",             SPELLD, ("AREA",),      "WireDamage",  "法术范围伤害"),
 ("INC_WARCRY_COOLDOWN_RECOVERY_RATE", CDR,    ("WARCRY",),    "WireSpeed",   "战吼冷却回复"),
]
# AREA damage without ATTACK: base is the ATTACK+AREA variant, so drop ATTACK and
# reuse/create DAMAGE + {DAMAGE, AREA}.
AREA_ONLY = ("INC_AREA_DAMAGE", "EFF_ADD_MULTIPLIED_BASE_AREA_ATTACK_ATTRIBUTE_COE_DAMAGE_DAMAGE",
             ("AREA", "DAMAGE"), "WireDamage", "范围伤害（不限攻击）")

# (statId, existingEFF, domain, note)
DIRECT = [
 ("INC_CHILL_DURATION_ON_ENEMIES",  "EFF_ADD_MULTIPLIED_BASE_AILMENT_ATTRIBUTE_CHILL_COE_AILMENT_DURATION_TAKEN", "WireAilment", "敌人冰缓持续时间（施加侧）"),
 ("INC_FREEZE_DURATION_ON_ENEMIES", "EFF_ADD_MULTIPLIED_BASE_AILMENT_ATTRIBUTE_COE_AILMENT_DURATION_FREEZE_TAKEN", "WireAilment", "敌人冰冻持续时间（施加侧）"),
 ("INC_MANA_RECOVERY_FROM_FLASKS",  "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_RECOVERY_RATE_FLASK_MANA",        "WireResource","魔力药剂回复"),
 ("MINIONS_HAVE_INC_ATTACK_SPEED",  "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_SPEED_MINION",             "WireSpeed",   "召唤物攻击速度"),
 ("FASTER_START_OF_ENERGY_SHIELD_RECHARGE", "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ENERGY_RECHARGE_ENERGY",        "WireResource","能量护盾回复开始加快"),
 ("SLOWER_START_OF_ENERGY_SHIELD_RECHARGE", "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ENERGY_RECHARGE_ENERGY",        "WireResource","能量护盾回复开始减慢（值已取负）"),
 ("INC_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT", "EFF_ADD_MULTIPLIED_BASE_AILMENT_ATTRIBUTE_BLEED_COE_AILMENT_MAGNITUDE_TAKEN", "WireAilment", "你造成的流血效果（含 not 标签=施加侧）"),
 ("SKILL_MANA_COSTS_STAT_CONVERSION_TO_HEALTH_COSTS", "EFF_HEALTH_MANA_REPLACE_RESOURCE_CONVERT",                  "WireResource","技能魔力消耗转为生命消耗"),
]

# ---------- resolve / create ----------
new_effs = []       # (name, path, attr, op, mode, tags)
plan = []           # (statId, effName, domain, note)

for sid, base, extra, dom, note in SCOPE + [AREA_ONLY]:
    b = effs.get(base)
    assert b, f"base EFF missing: {base}"
    if AREA_ONLY[0] == sid:
        tags = frozenset(extra)                     # explicit tag set (drop ATTACK)
    else:
        tags = frozenset(set(b['tags']) | set(extra))
    key = (b['attr'], b['op'], tags)
    if key in by_key:
        plan.append((sid, by_key[key], dom, note))
        continue
    nm = "EFF_" + b['op'] + "_ATTRIBUTE_" + b['attr'] + "".join("_" + t for t in sorted(tags))
    assert nm not in effs, f"name collision {nm}"
    new_effs.append((nm, "_".join(nm.split("_")[1:]).lower(), b['attr'], b['op'], b['mode'], tags, b['qual']))
    by_key[key] = nm
    effs[nm] = dict(attr=b['attr'], qual=b['qual'], op=b['op'], mode=b['mode'], tags=tags)
    plan.append((sid, nm, dom, note))

for sid, eff, dom, note in DIRECT:
    # not all of these are ModifierStatEffect (e.g. resource-convert effects), so
    # assert on the declaration existing at all, not on the parsed modifier table
    assert re.search(r'StatEffectIds\.' + eff + r'\s*,', eff_text), f"EFF missing: {eff}"
    plan.append((sid, eff, dom, note))

print(f"new EFFs: {len(new_effs)}   wirings: {len(plan)}")

# ---------- write EFF ids ----------
if new_effs:
    p = os.path.join(GEN, "StatEffectIds.java"); t = rd(p)
    add = '    // ── 作用域变体（纯数据）：base EFF 的 attr/op/mode + 作用域标签 ──\n'
    for nm, path, *_ in new_effs:
        assert nm not in t, nm
        add += f'    public static final Identifier {nm} = id("{path}");\n'
    i = t.rstrip().rfind("}")
    wr(p, t[:i] + add + t[i:])

# ---------- write EFF registrations ----------
if new_effs:
    p = os.path.join(GEN, "StatEffectGen.java"); t = rd(p)
    add = '        // ── 作用域变体（纯数据）：与 base EFF 同 attr/op/mode，仅追加作用域标签 ──\n'
    for nm, path, attr, op, mode, tags, qual in new_effs:
        tg = ("TagExpr.ALWAYS" if not tags
              else "TagExpr.of(Set.of(" + ", ".join(f"StatTag.{x}" for x in sorted(tags)) + "))")
        add += (f'        m.put(StatEffectIds.{nm}, new ModifierStatEffect({qual}.{attr}, '
                f'Either.left(AttributeModifier.Operation.{op}), ModifierStatEffect.Mode.{mode}, {tg}));\n')
    i = t.rfind("        return m;")
    wr(p, t[:i] + add + t[i:])

# ---------- write wirings ----------
TAIL = re.compile(r"\n    \}\n\}\s*$")
by = {}
for sid, eff, dom, note in plan:
    by.setdefault(dom, []).append(
        f'        // {note}\n        map.put(StatIds.{sid}, self(StatEffectIds.{eff}));')

n = 0
for dom, lines in by.items():
    p = os.path.join(WIR, dom + ".java")
    t = rd(p)
    lines = [l for l in lines if l.split("\n")[1] not in t]
    if not lines:
        continue
    m = TAIL.search(t)
    assert m, dom
    wr(p, t[:m.start()] + "\n" + "\n".join(lines) + "\n" + t[m.start():])
    n += len(lines)
    print(f"  {dom}: +{len(lines)}")
print("wired =", n)

# ---------- gate count ----------
if new_effs:
    p = os.path.join(ROOT, "scripts", "verify_gen.py"); t = rd(p)
    cur = int(re.search(r'effect count (\d+)', t).group(1))
    new = cur + len(new_effs)
    t = t.replace(f'("effect count {cur}"', f'("effect count {new}"')
    t = re.sub(rf'len\(effect_ids\)=={cur} and eff_n=={cur}',
               f'len(effect_ids)=={new} and eff_n=={new}', t)
    wr(p, t)
    print(f"verify_gen effect {cur} -> {new}")
