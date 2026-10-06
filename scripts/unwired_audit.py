import re, os, glob

ROOT = "src/main/java/com/cimdy/coe"
STATIDS = os.path.join(ROOT, "data/codec/support/stat/StatIds.java")
WIRE_DIR = os.path.join(ROOT, "data/gen/stat/wirings")
EFFGEN = os.path.join(ROOT, "data/gen/stat/StatEffectGen.java")

# 1. collect all StatId field names
with open(STATIDS, encoding="utf-8") as f:
    src = f.read()

fields = {}
# match: public static final Identifier NAME = id("path");  plus preceding comment line
pat = re.compile(r'(?:^|\n)\s*//\s*(.*?)\n\s*public static final Identifier (\w+) = id\("([^"]+)"\)', re.M)
for m in pat.finditer(src):
    desc, name, path = m.group(1).strip(), m.group(2), m.group(3)
    fields[name] = (path, desc)
# also catch fields without comment
pat2 = re.compile(r'public static final Identifier (\w+) = id\("([^"]+)"\)', re.M)
for m in pat2.finditer(src):
    name, path = m.group(1), m.group(2)
    fields.setdefault(name, (path, ""))

all_names = set(fields.keys())

# 2. collect wired names from Wire*.java
wired = set()
for wf in glob.glob(os.path.join(WIRE_DIR, "Wire*.java")):
    with open(wf, encoding="utf-8") as f:
        txt = f.read()
    for m in re.finditer(r'StatIds\.(\w+)', txt):
        wired.add(m.group(1))

unwired = sorted(all_names - wired, key=lambda n: fields[n][0])
print(f"total StatIds={len(all_names)}  wired={len(wired)}  UNWIRED={len(unwired)}")

# 3. for each unwired, look for a candidate EFF in StatEffectGen by keyword tokens
with open(EFFGEN, encoding="utf-8") as f:
    eff_src = f.read()
eff_names = set(re.findall(r'(EFF_\w+)', eff_src))

def tokens(name):
    # split camel/snake into lower tokens
    s = re.sub(r'(?<!^)(?=[A-Z])', '_', name).lower()
    parts = re.split(r'[_\s]+', s)
    return [p for p in parts if len(p) > 2]

candidates = []
for name in unwired:
    path, desc = fields[name]
    toks = tokens(name)
    # search EFF names containing most of the tokens
    best = []
    for en in eff_names:
        et = tokens(en)
        if et and sum(1 for t in toks if t in et) >= max(2, len(toks)//2):
            best.append(en)
    if best:
        candidates.append((name, path, desc, best[:6]))

print(f"\n=== UNWIRED WITH POSSIBLE EXISTING EFF ({len(candidates)}) ===")
for name, path, desc, best in candidates:
    print(f"\n{name}  [{path}]")
    print(f"   desc: {desc[:120]}")
    print(f"   EFF? : {best}")
