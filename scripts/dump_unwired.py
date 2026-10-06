import re, os, glob

ROOT = "src/main/java/com/cimdy/coe"
STATIDS = os.path.join(ROOT, "data/codec/support/stat/StatIds.java")
WIRE_DIR = os.path.join(ROOT, "data/gen/stat/wirings")

with open(STATIDS, encoding="utf-8") as f:
    src = f.read()
fields = {}
pat = re.compile(r'(?:^|\n)\s*//\s*(.*?)\n\s*public static final Identifier (\w+) = id\("([^"]+)"\)', re.M)
for m in pat.finditer(src):
    desc, name, path = m.group(1).strip(), m.group(2), m.group(3)
    fields[name] = (path, desc)
pat2 = re.compile(r'public static final Identifier (\w+) = id\("([^"]+)"\)', re.M)
for m in pat2.finditer(src):
    name, path = m.group(1), m.group(2)
    fields.setdefault(name, (path, ""))

wired = set()
for wf in glob.glob(os.path.join(WIRE_DIR, "Wire*.java")):
    txt = open(wf, encoding="utf-8").read()
    for m in re.finditer(r'StatIds\.(\w+)', txt):
        wired.add(m.group(1))

unwired = sorted(set(fields) - wired, key=lambda n: fields[n][0])
with open("unwired_103.txt", "w", encoding="utf-8") as out:
    out.write(f"UNWIRED={len(unwired)}\n")
    for i, name in enumerate(unwired, 1):
        path, desc = fields[name]
        out.write(f"{i:3d}. {name}\n    path={path}\n    desc={desc}\n")
print("wrote unwired_103.txt with", len(unwired), "entries")
