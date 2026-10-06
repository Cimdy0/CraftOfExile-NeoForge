import os, re

SRC = "E:/McModding/Modding/CraftOfExile-NeoForge/src/main/java"
def read(p):
    with open(p, encoding="utf-8") as f: return f.read()

# 1) StatTag.AREA exists?
statag = read(os.path.join(SRC,"com/cimdy/coe/data/codec/support/stat/StatTag.java"))
print("=== StatTag AREA/MELEE/PROJECTILE/ATTACK/ARROW ===")
for t in ["AREA","MELEE","PROJECTILE","ATTACK","ARROW","KNOCKBACK","SPELL"]:
    print(f"  {t}: {'FOUND' if re.search(r'\b'+t+r'\b\s*=', statag) or (t.lower()+' ') in statag.lower() else '??'}")

# 2) KNOCKBACK_CHANCE EFF def line in StatEffectGen
seg = read(os.path.join(SRC,"com/cimdy/coe/data/gen/stat/StatEffectGen.java"))
for m in re.finditer(r".*KNOCKBACK_CHANCE_KNOCKBACK.*", seg):
    print("EFF_GEN:", m.group(0).strip()[:260])

# 3) WireMisc knockback wirings (exact lines)
wm = read(os.path.join(SRC,"com/cimdy/coe/data/gen/stat/wirings/WireMisc.java"))
for m in re.finditer(r".*(KNOCKBACK_CHANCE_KNOCKBACK|KNOCK_BACK|KNOCK_ENEMIES).*", wm):
    print("WIREMISC:", m.group(0).strip()[:300])

# 4) StatEffectIds KNOCKBACK_CHANCE line
sei = read(os.path.join(SRC,"com/cimdy/coe/data/gen/stat/StatEffectIds.java"))
for m in re.finditer(r".*KNOCKBACK_CHANCE_KNOCKBACK.*", sei):
    print("EFF_IDS:", m.group(0).strip()[:200])

# 5) confirm SkillStatScopes enterAction pushes delivery tag
sss = read(os.path.join(SRC,"com/cimdy/coe/data/codec/skill/SkillStatScopes.java"))
for m in re.finditer(r".*(enterAction|delivery|getStatTags|tags).*", sss):
    line=m.group(0).strip()
    if any(k in line for k in ["enterAction","delivery","getStatTags","tags.add","tags ="]):
        print("SKILLSCOPE:", line[:200])
