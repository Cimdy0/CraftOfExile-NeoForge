import os, re, glob

SRC = "E:/McModding/Modding/CraftOfExile-NeoForge/src/main/java"
WIRE_DIR = os.path.join(SRC, "com/cimdy/coe/data/gen/stat/wirings")
STATIDS = os.path.join(SRC, "com/cimdy/coe/data/codec/support/stat/StatIds.java")

pairs = [
 ("ALLIES_IN_YOUR_PRESENCE_REGENERATE_RAGE_PER_SECOND_IF_YOU_HAVE_GAIN_RAGE_RECENTLY",
  "ALLIES_IN_YOUR_PRESENCE_REGENERATE_OF_YOUR_MAX_HEALTH_PER_SECOND"),
 ("AREA_SKILLS_HAVE_CHANCE_TO_KNOCK_ENEMIES_BACK_ON_HIT_DAMAGE",
  "CHANCE_TO_KNOCK_BACK_BLEEDING_ENEMIES_WITH_HIT_DAMAGE"),
 ("ARMOR_BREAK_ARMOR_ON_CRITICAL_HIT_WITH_SPELLS_EQUAL_TO_OF_PHYSICAL_DAMAGE_DEALT",
  "MINIONS_BREAK_ARMOR_EQUAL_TO_OF_PHYSICAL_DAMAGE_DEALT"),
 ("BASE_BLEEDING_DURATION_IS_SECOND",
  "INC_BLEEDING_DURATION"),
 ("CANNOT_RECOVER_HEALTH_OTHER_THAN_FROM_HEALTH_LEECH",
  "INC_LEECH_HEALTH"),
 ("CHANCE_THAT_IF_YOU_WOULD_GAIN_RAGE_ON_HIT_DAMAGE_YOU_INSTEAD_GAIN_UP_TO_YOUR_MAX_RAGE",
  "CHANCE_THAT_IF_YOU_WOULD_GAIN_ENDURANCE_CHARGES_YOU_INSTEAD_GAIN_UP_TO_MAX_ENDURANCE_CHARGES"),
 ("DEFLECTED_HIT_DAMAGE_CANNOT_INFLICT_BLEEDING_ON_YOU",
  "CHANCE_TO_INFLICT_BLEEDING_ON_HIT_DAMAGE"),
 ("HEALTH_RECOVERY_FROM_YOUR_FLASKS_ALSO_APPLIES_TO_YOUR_COMPANIONS",
  "INC_HEALTH_RECOVERY_FROM_FLASKS"),
 ("NO_RAGE_EFFECT",
  "RAGE_ON_HIT"),
 ("REGENERATE_RAGE_PER_SECOND_PER_RAGE_SPENT_RECENTLY",
  "REGENERATE_OF_MAX_HEALTH_PER_SECOND_IF_YOU_HAVE_BEEN_HIT_RECENTLY"),
 ("SKILLS_GAIN_COMBO_SLOWER",
  "GRANT_COMBO_ON_HIT"),
 ("WARCRIES_HAVE_A_MINIMUM_OF_POWER",
  "INC_TOTAL_POWER_COUNTED_BY_WARCRIES"),
]

# gather all wirings text
wire_files = glob.glob(os.path.join(WIRE_DIR, "*.java"))
wire_text = {}
for f in wire_files:
    with open(f, encoding="utf-8") as fh:
        wire_text[f] = fh.read()
allwire = "\n".join(wire_text.values())

statids_text = open(STATIDS, encoding="utf-8").read()

def is_defined(name):
    # constant declared as NAME = id("...")
    return re.search(r"\b"+re.escape(name)+r"\s*=\s*id\(", statids_text) is not None

def wiring_line(name):
    m = re.search(r"map\.put\(StatIds\."+re.escape(name)+r",\s*([^\n;]*)\)", allwire)
    if not m:
        return None
    return m.group(1).strip()

def eff_of(line):
    if not line:
        return None
    m = re.search(r"StatEffectIds\.(\w+)", line)
    return m.group(1) if m else None

print("ITEM | ITEM_DEFINED | ITEM_WIRED | ITEM_EFF || SIBLING | SIB_DEFINED | SIB_WIRED | SIB_EFF")
for item, sib in pairs:
    il = wiring_line(item)
    sl = wiring_line(sib)
    row = [
        item,
        "Y" if is_defined(item) else "N",
        "Y" if il else "N",
        eff_of(il) or "-",
        "||",
        sib,
        "Y" if is_defined(sib) else "N",
        "Y" if sl else "N",
        eff_of(sl) or "-",
    ]
    print(" | ".join(row))
