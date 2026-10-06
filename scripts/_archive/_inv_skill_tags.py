import os, re

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

def show(pat, label, ctx=100, maxh=8):
    print(f"\n=== {label} ===")
    rx = re.compile(pat, re.IGNORECASE)
    out=[]
    for f,t in texts.items():
        for m in rx.finditer(t):
            s=max(0,m.start()-ctx); e=min(len(t),m.end()+ctx)
            out.append(f.split("com/cimdy/coe/")[-1]+" :: "+t[s:e].replace("\n"," "))
            if len(out)>=maxh: break
        if len(out)>=maxh: break
    for o in out: print("   ", o[:300])

# SkillStatScopes enter + what tags pushed
show(r"class SkillStatScopes|SkillStatScopes\.enter", "SkillStatScopes")
# skill stat tags: how skills emit tags
show(r"getStatTags|withTags|StatTag\.ATTACK|StatTag\.SPELL|StatTag\.PROJECTILE|StatTag\.MELEE|StatTag\.AREA", "skill stat tags")
# StatTag constants list (attack/spell/area/melee/projectile)
show(r"public static final String (ATTACK|SPELL|PROJECTILE|MELEE|AREA|SKILL)\b|ATTACK =|SPELL =|AREA =", "StatTag constants")
# how sibling COND (bleeding) works - to mirror pattern
show(r"COND_.*BLEED|bleedingEnemies|isBleeding|recentlyBleeding", "bleeding COND pattern")
# KNOCKBACK_CHANCE EFF definition + its tag scope
show(r"EFF_ADD_VALUE_ATTRIBUTE_COE_KNOCKBACK_CHANCE_KNOCKBACK", "KNOCKBACK_CHANCE EFF def")
