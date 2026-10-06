import re, os, collections
ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
ATTR = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/AttributeRegister.java")
SRC = os.path.join(ROOT, "src/main/java")
atx = re.findall(r'public static Holder<Attribute> (\w+) =', open(ATTR, encoding="utf-8").read())

# genuine engine read = file NOT under data/gen/ and not AttributeRegister.java
def is_gen(p):
    p = p.replace("\\","/")
    return "/data/gen/" in p or p.endswith("AttributeRegister.java") or "StatIds.java" in p

cands = set(("EVASION","MAX_RESISTANCES","DEFLECTION_RATING","MINECRAFT_MAX_HEALTH","MAX_VOLATILITY",
"MAX_RAGE","DAMAGE","KNOCKBACK","KNOCKBACK_CHANCE","ACCURACY","CRIT_CHANCE","CRIT_DAMAGE","EFFECT_AREA",
"CAST_SPEED","LAUNCH_ADDITIONAL_PROJECTILE","PROJECTILE_PIERCE","PROJECTILE_PIERCE_CHANCE","BLOCK_CHANCE",
"DODGE_CHANCE","STUN_RECOVERY","MAX_ENERGY_SHIELD","MAX_MANA","MAX_SPIRIT","MAX_CURSES","MAX_INFUSION",
"RESERVATION_EFFICIENCY","MANA_COST_EFFICIENCY","AILMENT_DURATION","AILMENT_MAGNITUDE","AILMENT_BUILDUP",
"GUARD_RECOVERY","ARMOR_BREAK","RECOVERY_RATE","REGENERATION_RATE","ENERGY_RECHARGE","CHARM_EFFECT",
"STATUS_MAX_STACKS","PRESENCE_RANGE","BANNER_LINGER","VOLATILITY_GENERATION"))
for name in atx:
    if name not in cands: continue
    pattern = re.compile(r'AttributeRegister\.' + re.escape(name) + r'\b')
    files = []
    for dp,_,fs in os.walk(SRC):
        for f in fs:
            if not f.endswith(".java"): continue
            fp = os.path.join(dp,f)
            if is_gen(fp): continue
            try: t = open(fp, encoding="utf-8").read()
            except: continue
            m = pattern.search(t)
            if m:
                ln = t[:m.start()].count("\n")+1
                files.append(fp.split('src/main/java/')[-1]+":"+str(ln))
    print(f"\n### {name}  (genuine engine reads: {len(files)})")
    for fl in files[:6]:
        print("   ", fl)
