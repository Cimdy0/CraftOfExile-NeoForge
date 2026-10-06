"""Strict word-family consistency audit for the Wave-12 block.

Groups every Wave-12 attribute by the scope/subject word that appears in its
register STRING name, then reports, for each family, the set of StatTag scope
labels currently applied across its StatGen writers. Goal: detect
inconsistent tagging inside a family (e.g. one member tagged {arrow} but
another arrow-related member not).
"""
import re

ATTR = "src/main/java/com/cimdy/coe/data/AttributeRegister.java"
STGEN = "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java"
STAG = "src/main/java/com/cimdy/coe/data/codec/support/stat/StatTag.java"

attr_src = open(ATTR, encoding="utf-8").read().splitlines()
stgen_src = open(STGEN, encoding="utf-8").read().splitlines()
stag_src = open(STAG, encoding="utf-8").read()

# --- locate Wave-12 block ---
start = next(i for i, l in enumerate(attr_src) if ("批量落地" in l or "Wave-12" in l))
block = []
for i in range(start, min(start + 300, len(attr_src))):
    l = attr_src[i]
    m = re.search(r'register\w+\(\s*"([a-z0-9_]+)"', l)
    if m and ("public static Holder<Attribute>" in l or "= register" in l):
        block.append((i + 1, m.group(1)))
names = {n for _, n in block}

# --- parse StatGen writers: attribute const -> tags ---
stag_vals = set(v for _, v in re.findall(r'public static final String ([A-Z_]+) = "([a-z_]+)"', stag_src))
writer_re = re.compile(r'(inc|chance|add|flat|valueMod|map\.put)\s*\(\s*map\s*,\s*StatIds\.(\w+)\s*,', re.I)
attr_const_to_tags = {}
for i, l in enumerate(stgen_src):
    m = writer_re.search(l)
    if not m:
        continue
    rest = l[m.end():]
    am = re.search(r'AttributeRegister\.(\w+)', rest)
    if not am:
        # ImmuneStatEffect style: map.put(StatIds.X, ImmuneStatEffect.entry(...))
        continue
    const = am.group(1)
    toks = re.findall(r'StatTag\.([A-Z_]+)', l)
    strs = re.findall(r'"([a-z0-9_]+)"', l)
    resolved = set()
    for t in toks:
        mm = re.search(r'public static final String %s = "([a-z_]+)"' % t, stag_src)
        if mm:
            resolved.add(mm.group(1))
    resolved |= set(strs)
    attr_const_to_tags.setdefault(const, set()).update(resolved)

# map attribute STRING name -> const (they usually share the same identifier casing)
def const_of(name):
    # AttributeRegister.CONST = register("name", ...)  -> CONST derived from name
    # heuristic: const is the Java identifier; find it in AttributeRegister lines
    for l in attr_src:
        m = re.search(r'([A-Z][A-Z0-9_]*)\s*=\s*register\w+\(\s*"' + re.escape(name) + r'"', l)
        if m:
            return m.group(1)
    return None

# scope words to group families (subset of StatTag vocabulary + common subjects)
SCOPE_WORDS = [
    "arrow", "pierce", "grenade", "ignite", "bleed", "energy_shield", "life", "mana",
    "health", "chaos", "cold", "lightning", "fire", "physical", "elemental", "spell",
    "plant", "kill", "remnant", "warcry", "debilitate", "stun", "resistance", "armor",
    "armour", "shield", "minion", "curse", "flask", "leech", "impale", "infusion",
    "projectile", "melee", "attack", "crit", "thorns", "knockback", "movement", "cost",
    "culling", "stun_recovery", "companion", "jewellery", "charge", "ailment", "poison",
    "freeze", "shock", "chill", "burn", "brand", "bolt", "totem", "trap", "mine",
]

print("=== Wave-12 family tagging consistency (only multi-member families shown) ===")
families = {}
for ln, name in block:
    const = const_of(name)
    tags = attr_const_to_tags.get(const, set())
    for w in SCOPE_WORDS:
        if w in name:
            families.setdefault(w, []).append((name, sorted(tags)))

shown = 0
for w in sorted(families):
    members = families[w]
    if len(members) < 2:
        continue
    # collect distinct tag-sets
    distinct = {}
    for name, tags in members:
        distinct.setdefault(tuple(tags), []).append(name)
    if len(distinct) > 1:
        flag = "  <-- INCONSISTENT"
    else:
        flag = ""
    print(f"\n[{w}] ({len(members)} members){flag}")
    for tags, nms in distinct.items():
        print(f"   tags={list(tags)}: {nms}")
    shown += 1
print(f"\n=== {shown} multi-member families inspected ===")
