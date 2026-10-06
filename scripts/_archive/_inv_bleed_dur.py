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

def show(pat, label, ctx=140, maxh=6):
    print(f"\n=== {label} ===")
    rx = re.compile(pat, re.IGNORECASE)
    out=[]
    for f,t in texts.items():
        for m in rx.finditer(t):
            s=max(0,m.start()-ctx); e=min(len(t),m.end()+ctx)
            out.append(f.split("com/cimdy/coe/")[-1]+" :: "+t[s:e].replace("\n"," "))
            if len(out)>=maxh: break
        if len(out)>=maxh: break
    for o in out: print("   ", o[:340])

# bleed status type / base duration
show(r"class Bleed|BLEED\.id|BleedStatusTypes|registerAilment.*bleed|bleed.*baseDuration|BLEED_DURATION", "bleed status def + base duration")
# where duration is computed on apply
show(r"applyBleed|\.duration\(|setDuration|getDuration|ailmentDuration|durationTaken|DurationTaken", "ailment duration usage")
# existing AILMENT_DURATION_TAKEN read site (the multiplier)
show(r"AILMENT_DURATION_TAKEN|ailment_duration_taken|DurationTaken", "AILMENT_DURATION_TAKEN read sites")
# SET_VALUE EFF pattern (MAX_HEALTH_IS / MAX_VOLATILITY_IS) for reference
show(r"EFF_ATTRIBUTE_COE_MAX_HEALTH_SET_VALUE|EFF_ATTRIBUTE_COE_MAX_VOLATILITY_SET_VALUE|SET_VALUE", "SET_VALUE EFF pattern")
# how StatusEngine/StatusType computes duration for bleed specifically
show(r"StatusType.*duration|statusDuration|computeDuration|durationOf|baseDuration\(\)", "status duration compute")
