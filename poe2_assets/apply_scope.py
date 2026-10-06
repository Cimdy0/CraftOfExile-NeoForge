#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Apply the reviewed scope-variant plan + a small hand-crafted batch.

Source A: poe2_assets/scope_auto.json -- produced by scope_auto.py, which enforces
  the corpus guard, the condition/per-count exclusion, the single-EFF base rule and
  the zero-base guard. Every entry was re-read against the corpus before applying.

Source B: hand-crafted entries whose TagExpr needs StatTag.not(...) or a non-
  ModifierStatEffect constructor. These cannot be generated mechanically without
  risk (rebuilding a not(...) expression silently changes which side of a stat it
  applies to), so they are written out explicitly.
"""
import io, re, os, json

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
WIR = os.path.join(GEN, "wirings")
def rd(p): return io.open(p, encoding="utf-8").read()
def wr(p, s): io.open(p, "w", encoding="utf-8").write(s)

eff_text = rd(os.path.join(GEN, "StatEffectGen.java"))
effs = {}
for m in re.finditer(r'm\.put\(StatEffectIds\.(\w+),\s*new ModifierStatEffect\((.*?)\)\);', eff_text, re.S):
    am = re.search(r'(AttributeRegister|Attributes)\.(\w+)', m.group(2))
    om = re.search(r'AttributeModifier\.Operation\.(\w+)', m.group(2))
    md = re.search(r'ModifierStatEffect\.Mode\.(\w+)', m.group(2))
    tg = re.search(r'TagExpr\.of\(Set\.of\((.*?)\)\)', m.group(2), re.S)
    tagset = frozenset() if not tg else frozenset(re.findall(r'StatTag\.(?:\w+\()?(\w+)', tg.group(1)))
    if am and om:
        effs[m.group(1)] = dict(attr=am.group(2), qual=am.group(1), op=om.group(1),
                                mode=(md.group(1) if md else 'GAIN'), tags=tagset)

DOMAIN = {
    "ACCURACY": "WireDefense", "ATTACK_SPEED": "WireSpeed", "CRIT_CHANCE": "WireDamage",
    "CRIT_DAMAGE": "WireDamage", "DAMAGE": "WireDamage", "EFFECT_AREA": "WireMisc",
    "PROJECTILE_SPEED": "WireMisc", "RESERVATION_EFFICIENCY": "WireResource",
    "AILMENT_BUILDUP": "WireAilment", "AILMENT_MAGNITUDE": "WireAilment",
    "AILMENT_DURATION": "WireAilment",
}

plan = []           # (statId, effName, domain, note)
new_effs = []       # dict(kind='modifier'|'raw', ...)

# ---------- Source A: auto plan ----------
auto = json.load(open(os.path.join(ROOT, "poe2_assets", "scope_auto.json"), encoding="utf-8"))
by_key = {}
for n, v in effs.items():
    by_key.setdefault((v['attr'], v['op'], v['tags']), n)

for it in auto["plan"]:
    if it["kind"] != "new":
        continue
    sid, eff = it["sid"], it["eff"]
    # reconstruct the new EFF from the plan name (attr/op/tags are encoded in it)
    m = re.match(r'EFF_(ADD_\w+?)_ATTRIBUTE_(.+)$', eff)
    assert m, eff
    op, rest = m.group(1), m.group(2)
    # attribute is the longest prefix that is a known attribute
    attr = None
    for cand in sorted({v['attr'] for v in effs.values()}, key=len, reverse=True):
        if rest.startswith(cand + "_") or rest == cand:
            attr = cand
            break
    assert attr, eff
    tagstr = rest[len(attr):].strip("_")
    tags = frozenset(t for t in tagstr.split("_") if t)
    base_eff = None
    for n, v in effs.items():
        if v['attr'] == attr and v['op'] == op and (v['tags'] == tags - {it["tag"]}):
            base_eff = n
            break
    assert base_eff, eff
    b = effs[base_eff]
    new_effs.append(dict(kind="modifier", name=eff, attr=attr, qual=b['qual'], op=op,
                         mode=b['mode'], tags=tags))
    plan.append((sid, eff, DOMAIN.get(attr, "WireMisc"), f'{it["base"]} 的 {it["tag"]} 作用域变体'))

for it in auto["plan"]:
    if it["kind"] == "reuse":
        attr = None
        for n, v in effs.items():
            if n == it["eff"]:
                attr = v['attr']
        plan.append((it["sid"], it["eff"], DOMAIN.get(attr, "WireMisc"),
                     f'{it["base"]} 的 {it["tag"]} 作用域变体（复用既有 EFF）'))

# ---------- Source B: hand-crafted ----------
def mod(name, attr, op, tagsexpr, note):
    new_effs.append(dict(kind="modifier_raw", name=name, attr=attr, op=op,
                         tagsexpr=tagsexpr, note=note))

mod("EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AILMENT_MAGNITUDE_AURA", "AILMENT_MAGNITUDE",
    "ADD_MULTIPLIED_BASE", "TagExpr.of(Set.of(StatTag.AURA, StatTag.not(StatTag.TAKEN)))",
    "光环技能效果（去掉 BANNER 限定）")
plan.append(("AURA_SKILLS_HAVE_INC_BUFF_MAGNITUDE",
             "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AILMENT_MAGNITUDE_AURA", "WireAilment",
             "光环技能效果提高（与 Banner 版本区分）"))

mod("EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AILMENT_MAGNITUDE_NON_DAMAGING", "AILMENT_MAGNITUDE",
    "ADD_MULTIPLIED_BASE", "TagExpr.of(Set.of(StatTag.not(StatTag.DAMAGE_OVER_TIME), StatTag.not(StatTag.TAKEN)))",
    "非伤害型异常效果")
plan.append(("INC_BUFF_MAGNITUDE_OF_NON_DAMAGING_AILMENTS_YOU_INFLICT",
             "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AILMENT_MAGNITUDE_NON_DAMAGING", "WireAilment",
             "非伤害型异常效果提高（与伤害型互补）"))

for ail, tag, sid, note in [
    ("POISON", "POISON", "INC_BUFF_MAGNITUDE_OF_POISON_YOU_INFLICT", "你造成的中毒效果提高"),
    ("SHOCK", "SHOCK", "INC_BUFF_MAGNITUDE_OF_SHOCK_YOU_INFLICT", "你造成的感电效果提高"),
]:
    nm = f"EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AILMENT_MAGNITUDE_{ail}"
    mod(nm, "AILMENT_MAGNITUDE", "ADD_MULTIPLIED_BASE",
        f"TagExpr.of(Set.of(StatTag.{tag}, StatTag.not(StatTag.TAKEN)))", note)
    plan.append((sid, nm, "WireAilment", note))

mod("EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AILMENT_DURATION_BEAST", "AILMENT_DURATION",
    "ADD_MULTIPLIED_BASE", "TagExpr.of(Set.of(StatTag.AILMENT, StatTag.BEAST, StatTag.not(StatTag.TAKEN)))",
    "对野兽施加的异常持续时间")
plan.append(("INC_DURATION_OF_AILMENTS_ON_BEASTS",
             "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AILMENT_DURATION_BEAST", "WireAilment",
             "野兽身上的异常持续时间提高"))

# DamageConvert scope variants: only the LAST TagExpr (effect scope) changes.
CONVERT = [
    ("SPELLS_GAIN_OF_DAMAGE_AS_EXTRA_CHAOS_DAMAGE", "EFF_CHAOS_DAMAGE_CONVERT_GAIN_SPELL",
     "StatTag.CHAOS", "StatTag.CHAOS", "StatTag.SPELL", "法术获得额外混沌伤害"),
    ("COMPANIONS_GAIN_DAMAGE_AS_EXTRA_CHAOS_DAMAGE", "EFF_CHAOS_DAMAGE_CONVERT_GAIN_COMPANION",
     "StatTag.CHAOS", "StatTag.CHAOS", "StatTag.COMPANION", "伙伴获得额外混沌伤害"),
    ("COMPANIONS_GAIN_DAMAGE_AS_EXTRA_COLD_DAMAGE", "EFF_COLD_DAMAGE_CONVERT_GAIN_COMPANION",
     "StatTag.COLD", "StatTag.COLD", "StatTag.COMPANION", "伙伴获得额外冰霜伤害"),
]
for sid, nm, frm, to, scope, note in CONVERT:
    new_effs.append(dict(kind="convert", name=nm, frm=frm, to=to, scope=scope, note=note))
    plan.append((sid, nm, "WireDamage", note))

print(f"new EFFs: {len(new_effs)}   wirings: {len(plan)}")

# ---------- ids ----------
p = os.path.join(GEN, "StatEffectIds.java"); t = rd(p)
add = '    // ── 作用域变体与手工标签变体（纯数据）──\n'
n_ids = 0
for e in new_effs:
    nm = e["name"]
    # word-boundary check, not substring: many EFF names share long prefixes and a
    # plain `in` test falsely reports a collision
    assert re.search(r'(?<![A-Za-z0-9_])' + re.escape(nm) + r'(?![A-Za-z0-9_])', t) is None, nm
    add += f'    public static final Identifier {nm} = id("{nm[len("EFF_"):].lower()}");\n'
    n_ids += 1
i = t.rstrip().rfind("}")
wr(p, t[:i] + add + t[i:])
print("ids +", n_ids)

# ---------- registrations ----------
p = os.path.join(GEN, "StatEffectGen.java"); t = rd(p)
add = '        // ── 作用域变体与手工标签变体（纯数据）──\n'
for e in new_effs:
    if e["kind"] == "modifier":
        tg = ("TagExpr.ALWAYS" if not e["tags"]
              else "TagExpr.of(Set.of(" + ", ".join(f"StatTag.{x}" for x in sorted(e["tags"])) + "))")
        add += (f'        m.put(StatEffectIds.{e["name"]}, new ModifierStatEffect({e["qual"]}.{e["attr"]}, '
                f'Either.left(AttributeModifier.Operation.{e["op"]}), ModifierStatEffect.Mode.{e["mode"]}, {tg}));\n')
    elif e["kind"] == "modifier_raw":
        add += (f'        m.put(StatEffectIds.{e["name"]}, new ModifierStatEffect(AttributeRegister.{e["attr"]}, '
                f'Either.left(AttributeModifier.Operation.{e["op"]}), ModifierStatEffect.Mode.GAIN, {e["tagsexpr"]}));\n')
    else:
        add += (f'        m.put(StatEffectIds.{e["name"]}, new DamageConvertStatEffect(Set.of({e["frm"]}), {e["to"]}, '
                f'DamageConvertStatEffect.Mode.GAIN, true, DamageConvertStatEffect.ToMode.FIXED, List.of(), '
                f'TagExpr.ALWAYS, TagExpr.of(Set.of({e["scope"]}))));\n')
i = t.rfind("        return m;")
wr(p, t[:i] + add + t[i:])

# ---------- wirings ----------
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

# ---------- gate ----------
p = os.path.join(ROOT, "scripts", "verify_gen.py"); t = rd(p)
cur = int(re.search(r'effect count (\d+)', t).group(1))
new = cur + len(new_effs)
t = t.replace(f'("effect count {cur}"', f'("effect count {new}"')
t = re.sub(rf'len\(effect_ids\)=={cur} and eff_n=={cur}', f'len(effect_ids)=={new} and eff_n=={new}', t)
wr(p, t)
print(f"verify_gen effect {cur} -> {new}")
