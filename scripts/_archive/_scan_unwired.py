import re, glob, os, collections

root = "E:/McModding/Modding/CraftOfExile-NeoForge"
statids = os.path.join(root, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java")

# --- parse StatIds constants: NAME = id("name")
const = {}
with open(statids, encoding="utf-8") as f:
    for m in re.finditer(r'public static final Identifier (\w+)\s*=\s*id\("([^"]+)"\)', f.read()):
        const[m.group(2)] = m.group(1)

# --- parse wired StatIds from all Wire*.java map.put(StatIds.X
wired = set()
for path in glob.glob(os.path.join(root, "src/main/java/com/cimdy/coe/data/gen/stat/wirings/*.java")):
    txt = open(path, encoding="utf-8").read()
    for m in re.finditer(r'map\.put\(StatIds\.(\w+)', txt):
        wired.add(m.group(1))

# constant NAME -> id; cross map wired names to ids
wired_ids = set()
for cid, cname in const.items():
    if cname in wired:
        wired_ids.add(cid)

unwired = [cid for cid in const if cid not in wired_ids]
print(f"StatIds constants={len(const)}  wired={len(wired_ids)}  unwired={len(unwired)}")

# --- cluster by theme keywords (first matching wins)
themes = ["totem","companion","minion","summon","infusion","charge","herald","charm","warcry",
          "banner","offering","curse","mark","bleed","poison","ignite","shock","chill","freeze",
          "armor_break","stun","knock","ailment","crit","evasion","armor","energy_shield","shield",
          "mana","rage","frenzy","endurance","power","rage","resistance","damage","projectile","attack",
          "spell","elemental","fire","cold","lightning","physical","light","movement","speed","health",
          "regen","leech","flask","keystone","form","shapeshift","presence","ground","corpse","dodge",
          "block","parry","reflect","recoup","conversion","per","of_","gain","inc","chance","you","enemies",
          "ally","near","while","if_you","on","when","against"]
def theme_of(cid):
    for t in themes:
        if t in cid:
            return t
    return "_other"

cnt = collections.Counter(theme_of(c) for c in unwired)
print("\n=== theme counts (unwired) ===")
for t, n in cnt.most_common():
    print(f"{n:4d}  {t}")

print("\n=== full unwired id list ===")
for c in sorted(unwired):
    print(c)
