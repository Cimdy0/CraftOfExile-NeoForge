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

def show(pat, label, ctx=120, maxh=6):
    print(f"\n=== {label} ===")
    rx = re.compile(pat, re.IGNORECASE)
    out=[]
    for f,t in texts.items():
        for m in rx.finditer(t):
            s=max(0,m.start()-ctx); e=min(len(t),m.end()+ctx)
            out.append(f.split("com/cimdy/coe/")[-1]+" :: "+t[s:e].replace("\n"," "))
            if len(out)>=maxh: break
        if len(out)>=maxh: break
    for o in out: print("   ", o[:320])

# readKnockbackChance definition
show(r"readKnockbackChance", "readKnockbackChance def + call sites")
# how skill tags pushed onto scope during hit
show(r"enterHitScopes|StatScopes\.enter|withHitScopes|hitScopes", "scope entry on hit")
# area tag emission from skills
show(r"StatTag\.AREA|\"area\"|AREA\b|getStatTags", "area stat tag emission")
# the knockback read context method body
show(r"KNOCKBACK_CHANCE|knockback_chance", "KNOCKBACK_CHANCE usages", ctx=80, maxh=8)
