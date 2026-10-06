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

def show(pat, label, ctx=70):
    print(f"\n=== {label} ===")
    rx = re.compile(pat)
    out=[]
    for f,t in texts.items():
        for m in rx.finditer(t):
            s=max(0,m.start()-ctx); e=min(len(t),m.end()+ctx)
            out.append(f.split("com/cimdy/coe/")[-1]+" :: "+t[s:e].replace("\n"," "))
            if len(out)>=4: break
        if len(out)>=4: break
    for o in out: print("   ", o[:230])

# knockback read sites
show(r"KNOCKBACK_CHANCE", "KNOCKBACK_CHANCE read sites")
# how knockback chance consumed (is it tag/skill scoped?)
show(r"knockback_chance|KnockbackChance|getKnockback|applyKnockback|onKnockBack", "knockback consumption")
# bleed / ailment duration read (taken multiplier vs base)
show(r"AILMENT_DURATION_TAKEN|ailmentDurationTaken|BleedStatusTypes|BLEED.*DURATION|durationTaken", "BLEED/ailment duration read")
# base duration of bleed status definition
show(r"new .*Bleed|class Bleed|registerAilment.*bleed|bleed.*baseDuration|STATUS_BLEED", "bleed status base duration def")
