import re, glob, os, json, collections

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"

# 1. parse all StatIds constants
statids_path = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java")
txt = open(statids_path, encoding="utf-8").read()
consts = re.findall(r'public static final Identifier (\w+) = id\("([^"]+)"\)', txt)
all_keys = {name: sid for name, sid in consts}
print("total StatIds constants:", len(all_keys))

# 2. parse wired keys from wirings
wired = set()
pat = re.compile(r'map\.put\(StatIds\.(\w+)')
for fp in glob.glob(os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/wirings/*.java")):
    for line in open(fp, encoding="utf-8"):
        for m in pat.finditer(line):
            wired.add(m.group(1))

print("wired keys:", len(wired))

# 3. unwired
unwired = [k for k in all_keys if k not in wired]
print("unwired:", len(unwired))

# 4. categorize by keyword buckets (data-driven friendly first)
buckets = collections.OrderedDict()
def bucket(name):
    n = name.upper()
    if "CHANCE_TO" in n or "CHANCE_OF" in n or "_CHANCE_" in n: return "chance"
    if "PER_" in n or "FOR_EACH" in n or "FOR_EVERY" in n: return "per_count"
    if "RECENTLY" in n: return "recently"
    if "WHILE_" in n or "_WHILE_" in n or "WHEN_" in n: return "while_when"
    if "AGAINST" in n or "VS_" in n: return "against"
    if "CONVERT" in n or "AS_EXTRA" in n or "GAIN_OF" in n: return "convert"
    if "IMMUNE" in n or "IMMUNITY" in n: return "immune"
    if "MINION" in n or "COMPANION" in n: return "minion"
    if "FLASK" in n: return "flask"
    if "SKILL" in n: return "skill"
    if "ATTACK" in n or "SPELL" in n or "PROJECTILE" in n or "ABILITY" in n: return "attack_spell"
    if "DAMAGE" in n: return "damage"
    if "ARMOR" in n or "EVASION" in n or "ENERGY_SHIELD" in n or "ES_" in n: return "defense"
    if "LIFE" in n or "HEALTH" in n or "MANA" in n: return "vital"
    if "RESERV" in n: return "reservation"
    if "MOVEMENT" in n or "SPEED" in n: return "movement"
    if "RARITY" in n or "UNIQUE" in n or "MAGIC" in n or "RARE" in n: return "rarity"
    if "CURSE" in n or "MARK" in n: return "curse"
    if "BLEED" in n or "POISON" in n or "IGNITE" in n or "BURN" in n or "SHOCK" in n or "CHILL" in n or "FREEZE" in n or "ELECTROCUTE" in n or "SCORCH" in n or "BLOOD" in n: return "ailment"
    if "STUN" in n: return "stun"
    if "CHARGE" in n: return "charge"
    return "other"

for k in unwired:
    buckets.setdefault(bucket(k), []).append(k)

print("\n=== bucket sizes ===")
for b, ks in sorted(buckets.items(), key=lambda x: -len(x[1])):
    print(f"{b:14s} {len(ks)}")

# dump unwired with sid for inspection
out = {k: all_keys[k] for k in unwired}
json.dump(out, open(os.path.join(ROOT, "scripts/_unwired.json"), "w"), indent=0)
print("\nwrote scripts/_unwired.json")
