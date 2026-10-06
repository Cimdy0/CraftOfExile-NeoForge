import re, os, json, subprocess

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
ATTR = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/AttributeRegister.java")
STGEN = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java")
STAG = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatTag.java")

attr_src = open(ATTR, encoding="utf-8").read().splitlines()
stgen_src = open(STGEN, encoding="utf-8").read().splitlines()
stag_src = open(STAG, encoding="utf-8").read()

stag_vals = set(v for _, v in re.findall(r'public static final String ([A-Z_]+) = "([a-z_]+)"', stag_src))

# parse StatGen writers: each helper(map, StatIds.X, AttributeRegister.Y, [tags]) is ONE entry
writer_re = re.compile(r'([a-zA-Z_]+)\s*\(\s*map\s*,\s*StatIds\.(\w+)\s*,\s*([^;]*)\)')
writers = []  # list of dict(line, helper, sid, const, tags)
for i, l in enumerate(stgen_src):
    m = writer_re.search(l)
    if m and m.group(2).isupper():
        rest = m.group(3)
        am = re.search(r'AttributeRegister\.(\w+)', rest)
        if not am:
            continue
        const = am.group(1)
        tags = re.findall(r'StatTag\.(\w+)', rest) + re.findall(r'"([a-z0-9_]+)"', rest)
        writers.append({"line": i + 1, "helper": m.group(1), "sid": m.group(2),
                        "const": const, "tags": tags, "raw": l.strip()})

# runtime readers per const (for merge-safety, not for tag-add safety)
def runtime_readers(const):
    r = subprocess.run(["grep", "-rnE", f"AttributeRegister\\.{const}\\b",
                        os.path.join(ROOT, "src/main/java")],
                       capture_output=True, text=True)
    out = []
    for line in r.stdout.splitlines():
        if ".bak" in line: continue
        if "/data/gen/stat/StatGen.java:" in line: continue
        if "/data/AttributeRegister.java:" in line: continue
        out.append(line.strip())
    return out

# build per-const writer count
from collections import Counter, defaultdict
const_wc = Counter(w["const"] for w in writers)

records = []
for w in writers:
    readers = runtime_readers(w["const"]) if const_wc[w["const"]] == 1 else ["(multi-writer, skip reader check)"]
    records.append({**w, "const_wc": const_wc[w["const"]],
                    "has_reader": bool(readers) and readers[0] != "(multi-writer, skip reader check)",
                    "readers": readers[:2]})

# Dump: single-writer entries, show sid + current tags + const + reader presence
single = [r for r in records if r["const_wc"] == 1]
print(f"Total writer lines: {len(writers)}")
print(f"Distinct attrs: {len(const_wc)}")
print(f"Single-writer attrs: {len(single)}")
print(f"  of which HAVE runtime readers: {sum(1 for r in single if r['has_reader'])}")
print(f"  of which NO runtime readers:   {sum(1 for r in single if not r['has_reader'])}")

with open(os.path.join(ROOT, "poe2_assets/_test/_ids_all_writers.json"), "w", encoding="utf-8") as f:
    json.dump(records, f, indent=1, ensure_ascii=False)

print("\n=== SINGLE-WRITER entries (sid | const | tags | reader?) ===")
for r in sorted(single, key=lambda x: x["sid"]):
    rd = "R" if r["has_reader"] else "-"
    print(f"  {rd} {r['sid']:58s} {r['const']:38s} tags={r['tags']}")
