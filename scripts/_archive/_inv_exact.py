import os, re

SRC = "E:/McModding/Modding/CraftOfExile-NeoForge/src/main/java"
def lines(p):
    with open(p, encoding="utf-8") as f: return f.read().split("\n")

def show(p, pat, before=1, after=1, maxn=4):
    ls = lines(p); rx=re.compile(pat)
    print(f"\n=== {os.path.basename(p)} :: {pat} ===")
    cnt=0
    for i,l in enumerate(ls):
        if rx.search(l):
            for j in range(max(0,i-before), min(len(ls), i+after+1)):
                print(f"  {j+1}: {ls[j]}")
            cnt+=1
            if cnt>=maxn: break

show(os.path.join(SRC,"com/cimdy/coe/data/gen/stat/StatEffectIds.java"), r"KNOCKBACK_CHANCE_KNOCKBACK")
show(os.path.join(SRC,"com/cimdy/coe/data/gen/stat/StatEffectGen.java"), r"KNOCKBACK_CHANCE_KNOCKBACK")
show(os.path.join(SRC,"com/cimdy/coe/data/gen/stat/wirings/WireMisc.java"), r"CHANCE_TO_KNOCK_BACK_BLEEDING_ENEMIES_WITH_HIT_DAMAGE")

# verify_gen.py hardcoded counts
vg = "E:/McModding/Modding/CraftOfExile-NeoForge/scripts/verify_gen.py"
with open(vg, encoding="utf-8") as f: vgt=f.read()
print("\n=== verify_gen.py counts ===")
for m in re.finditer(r'(effect|target|condition)[^\n]*=\s*\d+', vgt, re.IGNORECASE):
    print("  ", m.group(0).strip()[:120])
# also show lines containing '765' or 'EFFECT' count
for i,l in enumerate(vgt.split("\n")):
    if re.search(r'\b(765|764|766|EFFECT_COUNT|effect_count|gen_effect|expect)', l, re.IGNORECASE):
        print(f"  L{i+1}: {l.strip()[:140]}")
