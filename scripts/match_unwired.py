import re, os, json, collections

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
SID = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java")
EFF = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatEffectIds.java")
GEN = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatEffectGen.java")
ATTR = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/AttributeRegister.java")

def read(p):
    return open(p, encoding="utf-8").read()

# unwired
statids = dict(re.findall(r'public static final Identifier (\w+) = id\("([^"]+)"\)', read(SID)))
wired = set()
for fp in os.listdir(os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/wirings")):
    if fp.startswith("Wire") and fp.endswith(".java"):
        for m in re.finditer(r'map\.put\(StatIds\.(\w+)', read(os.path.join(ROOT,"src/main/java/com/cimdy/coe/data/gen/stat/wirings",fp))):
            wired.add(m.group(1))
unwired = [k for k in statids if k not in wired]

# EFF constants -> id string
eff = {}
for m in re.finditer(r'public static final Identifier (EFF_\w+) = id\("([^"]+)"\)', read(EFF)):
    eff[m.group(1)] = m.group(2)

# EFF -> (attribute, scope, op) from StatEffectGen. Pattern: lines like
# either("EFF_...", AttributeRegister.X, ...) or either("EFF_...", List.of(...), AttributeRegister.X, ...)
# We'll just capture AttributeRegister.X references near each EFF.
gen_txt = read(GEN)
# find Mapping.of(either("EFF_..",...) or look for "EFF_..." then nearest AttributeRegister.
eff_to_attr = {}
for m in re.finditer(r'(EFF_\w+)\b', gen_txt):
    pass
# Simpler: scan StatEffectGen for each EFF name and capture following AttributeRegister.<NAME> on same or nearby line
for ename in eff:
    # search window of 400 chars after first occurrence
    idx = gen_txt.find(ename)
    if idx == -1: continue
    window = gen_txt[idx:idx+600]
    am = re.search(r'AttributeRegister\.(\w+)', window)
    eff_to_attr[ename] = am.group(1) if am else "?"

STOP = set(["of","the","to","a","an","and","or","in","on","for","with","your","you","is","are","if","while","when","that","this","from","as","by","per","up","at","be","been","have","has","gain","grants","deal","deals","damage","hit","recently","seconds","metres","each","any","all","no","not","can","cannot","chance","_"])
def toks(s):
    return [t for t in re.split(r'[_\s]+', s.lower()) if t and t not in STOP and len(t)>2]

res = collections.defaultdict(list)
for u in unwired:
    ut = set(toks(u))
    if not ut: continue
    best = []
    for ename, estr in eff.items():
        et = set(toks(ename))
        inter = ut & et
        if len(inter) >= 3:
            best.append((len(inter), ename, estr, sorted(inter)))
    best.sort(reverse=True)
    if best:
        res[u] = best[:3]

out = []
for u in unwired:
    if u in res:
        for score, ename, estr, inter in res[u]:
            out.append((u, score, ename, eff_to_attr.get(ename,"?"), inter, statids[u]))
# print
for u, score, ename, attr, inter, sid in out:
    print(f"{score:2d}  {u}\n      -> {ename}  attr={attr}  shared={inter}\n      sid={sid}")
print("\nTOTAL unwired:", len(unwired), " with >=3-token EFF matches:", len(res))
