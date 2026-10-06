import re, os
from collections import Counter

base = "E:/McModding/Modding/CraftOfExile-NeoForge"
sg_path = os.path.join(base, "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java")
ids_path = os.path.join(base, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java")

with open(sg_path, encoding="utf-8") as f:
    sg_text = f.read()
with open(ids_path, encoding="utf-8") as f:
    ids_text = f.read()

ids_total = len(re.findall(r'public static final Identifier\s+\w+\s*=\s*id\(', ids_text))
print(f"StatIds 总 id 数: {ids_total}")

call_re = re.compile(
    r'(inc|add|chance|valueMod|pctAdd|pct)\s*\(\s*map\s*,\s*(StatIds\.\w+)\s*,\s*(AttributeRegister\.\w+)\s*(?:,\s*(.*?))?\s*\)\s*;'
)
calls = []
for m in call_re.finditer(sg_text):
    helper, ids, attr, rest = m.group(1), m.group(2), m.group(3), m.group(4)
    stat_tags, str_tags = [], []
    if rest:
        for sm in re.finditer(r'StatTag\.(\w+)|"([^"]+)"', rest):
            if sm.group(1):
                stat_tags.append(sm.group(1))
            elif sm.group(2):
                str_tags.append(sm.group(2))
    calls.append({"helper": helper, "ids": ids.replace("StatIds.",""),
                  "attr": attr.replace("AttributeRegister.",""),
                  "st": stat_tags, "str": str_tags})

n = len(calls)
no_st = [c for c in calls if not c["st"]]
with_cond = [c for c in calls if c["str"]]
print(f"StatGen 主写点调用总数: {n}")
print(f"  带 >=1 StatTag 作用域标签: {n-len(no_st)}")
print(f"  零 StatTag(裸/仅靠条件): {len(no_st)}")
print(f"  带 >=1 条件字符串标签: {len(with_cond)}")
print()

# 严格作用域词（排除 TO/FROM/CAST/SKILL/HIT/HEALTH/MANA/DEFLECT 等通用/资源词）
SCOPE = set("""MINION MINIONS ATTACK ATTACKS SPELL SPELLS CURSE CURSES FLASK FLASKS CHANNEL CHANNELING
PROJECTILE PROJECTILES MELEE AURA AURAS SHIELD SHIELDS ARMOR ARMOUR ELEMENTAL FIRE COLD ICE
CHAOS POISON BLEED BLEEDING IGNITE IGNITION STUN STUNS PARRY PARRYING DODGE LEECH LEECHING BLOCK
BLOCKS CHARM CHARMS WARCRY WARCRIES TOTEM TOTEMS COMPANION COMPANIONS JEWEL JEWELLERY JEWELRY
SHAPESHIFT SHAPESHIFTED METEOR METEORS ARROW ARROWS GRENADE GRENADES HERALD HERALDS TRAP TRAPS
MINE MINES BOW RAGE BLOOD BLOODY SUMMON SUMMONS DEMON DEMONS TRAP WARDEN ARCHMAGE BERSERKER
JUGGERNAUT GLADIATOR CHAMPION SLAYER ASSASSIN SABOTEUR TRICKSTER OCCULTIST ELEMENTALIST
NECROMANCER HIEROPHANT INQUISITOR SHAMAN WARRIOR MONK DUELIST MARAUDER RANGER WITCH""".split())

# 条件触发词（StatIds 名含这些 => 写点应带条件字符串标签）
COND = set(["WHILE","WHEN","IF","RECENT","PAST","NEARBY","SURROUNDED","FULL_LIFE","LOW_LIFE","LOWER_","STANDING","MOVING","STATIONARY","UNARMED"])

def toks(name): return set(re.findall(r'[A-Z]+[a-z]*', name))

# 疑似缺作用域：零 StatTag 且 StatIds 名含作用域词
miss_scope = []
for c in no_st:
    hits = toks(c["ids"]) & SCOPE
    if hits:
        miss_scope.append((c["ids"], c["attr"], sorted(hits)))

# 疑似缺条件：StatIds 名含条件触发词，但写点无字符串条件标签
miss_cond = []
for c in calls:
    if c["str"]:
        continue
    hits = toks(c["ids"]) & COND
    if hits:
        miss_cond.append((c["ids"], c["attr"], sorted(hits)))

print(f"=== 疑似缺作用域标签 (裸且 StatIds 含作用域词): {len(miss_scope)} ===")
for n_,a,h in sorted(miss_scope):
    print(f"  {n_:60s} -> {a:38s} 作用域词={h}")

print(f"\n=== 疑似缺条件标签 (StatIds 含条件词但无字符串条件标签): {len(miss_cond)} ===")
for n_,a,h in sorted(miss_cond):
    print(f"  {n_:70s} -> {a:34s} 条件词={h}")
