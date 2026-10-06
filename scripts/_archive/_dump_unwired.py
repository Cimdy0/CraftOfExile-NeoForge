import re, os, glob
ROOT = "src/main/java/com/cimdy/coe"
STATIDS = os.path.join(ROOT, "data/codec/support/stat/StatIds.java")
WIRE_DIR = os.path.join(ROOT, "data/gen/stat/wirings")
fields = {}
src = open(STATIDS, encoding="utf-8").read()
pat = re.compile(r'(?:^|\n)\s*//\s*(.*?)\n\s*public static final Identifier (\w+) = id\("([^"]+)"\)', re.M)
for m in pat.finditer(src):
    desc, name, path = m.group(1).strip(), m.group(2), m.group(3)
    fields[name] = (path, desc)
pat2 = re.compile(r'public static final Identifier (\w+) = id\("([^"]+)"\)', re.M)
for m in pat2.finditer(src):
    name, path = m.group(1), m.group(2)
    fields.setdefault(name, (path, ""))
all_names = set(fields.keys())
wired = set()
for wf in glob.glob(os.path.join(WIRE_DIR, "Wire*.java")):
    txt = open(wf, encoding="utf-8").read()
    for m in re.finditer(r'StatIds\.(\w+)', txt):
        wired.add(m.group(1))
unwired = sorted(all_names - wired, key=lambda n: fields[n][0])
with open("unwired_now.txt", "w", encoding="utf-8") as f:
    for n in unwired:
        f.write(f"{n}\t{fields[n][0]}\t{fields[n][1][:120]}\n")
print("UNWIRED", len(unwired))
