import re, os, glob

ROOT = "src/main/java/com/cimdy/coe"
STATTAG = os.path.join(ROOT, "data/codec/support/stat/StatTag.java")
EFFGEN = os.path.join(ROOT, "data/gen/stat/StatEffectGen.java")
EFFIDS = os.path.join(ROOT, "data/gen/stat/StatEffectIds.java")
WIRE_DIR = os.path.join(ROOT, "data/gen/stat/wirings")

# 1. StatTag constant -> string value
tag_val = {}
src_tag = open(STATTAG, encoding="utf-8").read()
for m in re.finditer(r'public static final String (\w+)\s*=\s*"([^"]+)"', src_tag):
    tag_val[m.group(1)] = m.group(2)

# 2. EFF -> set of tags from StatEffectGen TagExpr.of(Set.of(StatTag.X, ...))
eff_tags = {}
src_eff = open(EFFGEN, encoding="utf-8").read()
# split per m.put(StatEffectIds.EFF_..., new ...(..., TagExpr.of(Set.of(StatTag.A, StatTag.B))))
for m in re.finditer(r'(EFF_\w+).*?TagExpr\.of\(Set\.of\(([^)]*)\)', src_eff, re.S):
    eff, tags_raw = m.group(1), m.group(2)
    tags = re.findall(r'StatTag\.(\w+)', tags_raw)
    eff_tags[eff] = tags

# 3. Wired StatIds -> EFFs (from Wire*.java)
wired_effs = set()
for wf in glob.glob(os.path.join(WIRE_DIR, "Wire*.java")):
    txt = open(wf, encoding="utf-8").read()
    for m in re.finditer(r'StatEffectIds\.(EFF_\w+)', txt):
        wired_effs.add(m.group(1))

# 4. global required tags = union of tags of all WIRED effs
required = set()
for e in wired_effs:
    for t in eff_tags.get(e, []):
        required.add(t)

# 5. count StatTag.X references outside the 3 def files (defs = StatEffectGen/EffIds/StatTag)
def_files = {EFFGEN, EFFIDS, STATTAG}
counts = {t: 0 for t in tag_val}
for ext in ("**/*.java",):
    for path in glob.glob(os.path.join(ROOT, ext), recursive=True):
        if path in def_files:
            continue
        txt = open(path, encoding="utf-8").read()
        for t in tag_val:
            counts[t] += len(re.findall(r'StatTag\.' + t + r'\b', txt))

# 6. dead-line tags: required by a wired EFF but never referenced outside def files
dead = sorted([t for t in required if counts.get(t, 0) == 0])
print(f"StatTag consts={len(tag_val)}  wired EFFs={len(wired_effs)}  required tags={len(required)}")
print(f"\n=== REQUIRED-BUT-NEVER-EMITTED TAGS (silent dead-lines) = {len(dead)} ===")
for t in dead:
    # which wired EFFs use it
    users = [e for e in wired_effs if t in eff_tags.get(e, [])]
    print(f"\n  StatTag.{t}  (value=\"{tag_val.get(t)}\")  refs_outside_def={counts.get(t,0)}")
    print(f"    used by {len(users)} wired EFF(s): {users[:12]}")
