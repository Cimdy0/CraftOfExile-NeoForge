import os, re, glob

SRC = "E:/McModding/Modding/CraftOfExile-NeoForge/src/main/java"
STATIDS = os.path.join(SRC,"com/cimdy/coe/data/codec/support/stat/StatIds.java")
WIRE_DIR = os.path.join(SRC,"com/cimdy/coe/data/gen/stat/wirings")

statids_text = open(STATIDS, encoding="utf-8").read()
defined = set(re.findall(r"\b([A-Z][A-Z0-9_]*)\s*=\s*id\(", statids_text))

wire_files = glob.glob(os.path.join(WIRE_DIR,"*.java"))
allwire = "\n".join(open(f,encoding="utf-8").read() for f in wire_files)
wired = set(re.findall(r"map\.put\(StatIds\.([A-Z][A-Z0-9_]*)\s*,", allwire))
unwired = sorted(defined - wired)

# skill-type qualifiers present in StatTag (from prior R3): AREA/MELEE/PROJECTILE/ATTACK/SPELL/ARROW/MINION/TOTEM/HERALD/CURSE/WARCRY
kw = re.compile(r"(AREA_SKILLS|_WITH_SPELLS|_WITH_ATTACKS|_ON_HIT_DAMAGE|_SKILLS_|ATTACK_SKILLS|PROJECTILE|MELEE|_SPELL_|CROSSBOW|PROJECTILE_ATTACKS|WITH_SPELL|WITH_ATTACK|SKILL_HITS|HIT_DAMAGE_WITH)")
hits = [u for u in unwired if kw.search(u)]
print(f"unwired total={len(unwired)}; skill-type-qualified candidates={len(hits)}")
for h in hits:
    print("  ", h)
