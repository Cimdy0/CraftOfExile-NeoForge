import os, re, glob

SRC = "E:/McModding/Modding/CraftOfExile-NeoForge/src/main/java"
files = []
for root, _, fs in os.walk(SRC):
    for f in fs:
        if f.endswith(".java"):
            files.append(os.path.join(root, f))

texts = {}
for f in files:
    try:
        with open(f, encoding="utf-8") as fh:
            texts[f] = fh.read()
    except Exception:
        pass

def find(patterns, label):
    print(f"\n=== {label} ===")
    for pat in patterns:
        rx = re.compile(pat, re.IGNORECASE)
        hits = []
        for f, t in texts.items():
            for m in rx.finditer(t):
                # show a short window
                s = max(0, m.start()-40); e = min(len(t), m.end()+40)
                hits.append(f.split("com/cimdy/coe/")[-1] + " :: " + t[s:e].replace("\n"," "))
                if len(hits) >= 3: break
            if len(hits) >= 3: break
        print(f"  [{pat}] -> {len(hits)} files-hits (showing up to 3)")
        for h in hits[:3]:
            print("     ", h[:160])

# 1) rage regen attribute / read
find([r"RAGE_REGEN", r"rage_regen", r"REGENERATE_RAGE", r"rageRegen", r"MAX_RAGE_REGEN"], "RAGE REGEN attribute/read")
# 2) knockback chance read + area skill tag scoping
find([r"KNOCKBACK_CHANCE", r"knockback", r"SKILL_AREA", r"\"area\"", r"AREA_SKILL", r"TagExpr.*area"], "KNOCKBACK + AREA skill tag")
# 3) armor break attributes (non-minion) + spell crit condition
find([r"ARMOR_BREAK_[A-Z]", r"ARMOR_BREAK_SELF", r"ARMOR_BREAK_PLAYER", r"spellCrit", r"SPELL_CRIT", r"isSpellCrit", r"CAUSED_CRIT"], "ARMOR BREAK variants + spell-crit hook")
# 4) base bleed/ailment duration read (not just multiplier)
find([r"BASE_BLEED", r"baseBleed", r"BLEED_DURATION_BASE", r"ailmentDurationBase", r"baseDuration", r"AILMENT_BASE_DURATION"], "BASE bleed/ailment duration read")
# 5) rage charge-convert / generic charge convert
find([r"CHARGE_CONVERT", r"chargeConvert", r"GAIN_TO_MAX", r"gainToMax", r"RAGE_TO_MAX"], "CHARGE CONVERT (rage) mechanism")
# 6) combo gain rate attribute
find([r"COMBO_GAIN_RATE", r"comboGainRate", r"combo_rate", r"COMBO_RATE", r"comboSpeed", r"COMBO_SPEED"], "COMBO GAIN RATE attribute")
# 7) warcry min power / power floor
find([r"WARCRY.*MIN", r"MIN_POWER", r"minPower", r"powerFloor", r"POWER_FLOOR", r"WARCRY_POWER"], "WARCRY min power / floor")
# 8) recently-gained-rage condition
find([r"RECENTLY_GAINED_RAGE", r"recentlyGainedRage", r"GAIN_RAGE_RECENTLY", r"RAGE_RECENTLY", r"COND_RECENTLY.*RAGE"], "RECENTLY GAINED RAGE condition")
# 9) companion flask heal redirect
find([r"COMPANION.*FLASK", r"flask.*companion", r"COMPANION_HEAL", r"companionHeal", r"FLASK.*COMPANION"], "COMPANION flask heal redirect")
# 10) deflect bleed prevention
find([r"DEFLECT.*BLEED", r"deflect.*bleed", r"CANNOT_INFLICT", r"cannotInflictBleed", r"PREVENT_BLEED"], "DEFLECT->BLEED prevention")
