import re, os
from collections import Counter

base = "E:/McModding/Modding/CraftOfExile-NeoForge"
sg_path = os.path.join(base, "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java")
ids_path = os.path.join(base, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java")

with open(sg_path, encoding="utf-8") as f:
    sg_text = f.read()
with open(ids_path, encoding="utf-8") as f:
    ids_text = f.read()

# StatIds 总 id 数
ids_all = re.findall(r'public static final String\s+(\w+)\s*=', ids_text)
ids_total = len(ids_all)
print(f"StatIds 总 id 数: {ids_total}")

# 解析 StatGen 主写点调用：inc/add/chance/valueMod/pctAdd/pct
call_re = re.compile(
    r'(inc|add|chance|valueMod|pctAdd|pct)\s*\(\s*map\s*,\s*(StatIds\.\w+)\s*,\s*(AttributeRegister\.\w+)\s*(?:,\s*(.*?))?\s*\)\s*;'
)
calls = []
for m in call_re.finditer(sg_text):
    helper, ids, attr, rest = m.group(1), m.group(2), m.group(3), m.group(4)
    tags = []
    if rest:
        # 提取 StatTag.X 或 "x" 字符串字面量
        tags = re.findall(r'StatTag\.(\w+)|"([^"]+)"', rest)
        tags = [a or b for a, b in tags]
    calls.append({"helper": helper, "ids": ids, "attr": attr, "tags": tags})

print(f"StatGen 主写点调用总数: {len(calls)}")

by_tagcount = Counter(len(c["tags"]) for c in calls)
print(f"按作用域标签数分布: {dict(sorted(by_tagcount.items()))}")
print(f"  裸调用(0标签): {by_tagcount.get(0,0)}")
print(f"  1标签: {by_tagcount.get(1,0)}")
print(f"  2标签: {by_tagcount.get(2,0)}")
print(f"  3+标签: {sum(v for k,v in by_tagcount.items() if k>=3)}")
print()

# 已知作用域词（来自 StatIds/描述语义）
scope_words = [
    "MINION","MINIONS","ATTACK","ATTACKS","SPELL","SPELLS","CURSE","CURSES","FLASK","FLASKS",
    "CHANNEL","CHANNELING","PROJECTILE","PROJECTILES","MELEE","AURA","AURAS","SHIELD","SHIELDS",
    "ARMOR","ARMOUR","ELEMENTAL","FIRE","COLD","ICE","CHAOS","POISON","BLEED","BLEEDING","IGNITE",
    "IGNITION","STUN","STUNS","PARRY","PARRYING","DODGE","LEECH","LEECHING","BLOCK","BLOCKS",
    "CHARM","CHARMS","WARCRY","WARCRIES","TOTEM","TOTEMS","COMPANION","COMPANIONS","JEWEL",
    "JEWELLERY","JEWELRY","SHAPESHIFT","SHAPESHIFTED","METEOR","METEORS","ARROW","ARROWS",
    "GRENADE","GRENADES","HERALD","HERALDS","SKILL","SKILLS","TRAP","TRAPS","MINE","MINES",
    "BOW","POWER","SUMMON","SUMMONS","DEMON","DEMONS","WITCH","RANGER","MARAUDER","DUELIST",
    "SHAMAN","WARRIOR","MONK","ARCHMAGE","CHAMPION","SLAYER","ASSASSIN","SABOTEUR","TRICKSTER",
    "AVATAR","GLADIATOR","JUGGERNAUT","BERSEKER","CHIEFTAIN","GUARDIAN","OCCULTIST","ELEMENTALIST",
    "NECROMANCER","HIEROPHANT","INQUISITOR","WARDEN","WARBRINGER","BLOOD","BLOODY","HASTE",
    "BLIND","BLINDED","MAIM","MAIMED","TAUNT","TAUNTED","EXPOSE","EXPOSED","MARK","MARKED",
    "VULNERABILITY","VULNERABLE","SLOW","SLOWED","CHILL","CHILLED","FREEZE","FROZEN","SHOCK",
    "SHOCKED","BURN","BURNING","POISONED","WITHER","WITHERED","CORRUPT","CORRUPTED","RAGE",
    "RAGED","FRENZY","FRENZIED","ENDURANCE","POWER_CHARGE","FRENZY_CHARGE","LIGHTNING",
    "PHYSICAL","MOVEMENT","MOVING","STATIONARY","STANDING","UNARMED","ONE_HANDED","TWO_HANDED",
    "STAFF","WAND","DAGGER","CLAW","SCEPTRE","MACE","SWORD","AXE","SPEAR","QUIVER","HELMET",
    "CHEST","GLOVES","BOOTS","BELT","RING","AMULET","BODY_ARMOUR","WEAPON","WEAPONS","ARMOUR",
    "FULL_LIFE","LOW_LIFE","LOWER_ENERGY","FULL_ENERGY","RECENT","RECENTLY","KILL","KILLS",
    "HIT","HITS","WHILE","WHEN","IF","FROM","TO","NEARBY","NEAR","DISTANT","FAR","GROUND",
    "GROUND_FIRE","CHILLED_GROUND","SHOCKED_GROUND","BURNING_GROUND","MANA","LIFE","HEALTH",
    "ENERGY","RESERVATION","RESERVED","COST","COSTS","CORPSE","CORPSES","VOLATILITY","META",
    "ENERGY_SHIELD","ES","DEFLECT","DEFLECTION","SURROUNDED","CAST","CASTING","SUMMONED",
]
sw = set(scope_words)

# 裸调用中，StatIds 名含作用域词 -> 疑似需补标
naked = [c for c in calls if len(c["tags"]) == 0]
suspect = []
for c in naked:
    name = c["ids"].replace("StatIds.", "")
    toks = set(re.findall(r'[A-Z]+[a-z]*', name))
    hits = toks & sw
    if hits:
        suspect.append((name, c["attr"].replace("AttributeRegister.",""), sorted(hits)))

print(f"=== 裸写点总数: {len(naked)} ===")
print(f"其中 StatIds 名含作用域词(疑似应补标): {len(suspect)} ===")
for name, attr, hits in sorted(suspect, key=lambda x: x[0]):
    print(f"  {name:58s} -> {attr:40s} 命中作用域词={hits}")

# 已带标签的写点（视为已完成归一）
done = [c for c in calls if len(c["tags"]) >= 1]
print(f"\n=== 已带至少 1 个作用域标签(已归一): {len(done)} ===")
