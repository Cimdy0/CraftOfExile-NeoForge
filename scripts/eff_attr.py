import re, os, json, collections
ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatEffectGen.java")
txt = open(GEN, encoding="utf-8").read()

# Parse each either("EFF_..", ...) or either("EFF_..", List.of(...), AttributeRegister.X, ...) or either("EFF_..", AttributeRegister.X, Mode)
# Capture the EFF id (first quoted string starting with EFF_) and the AttributeRegister.X (and op/Mode if present).
# We'll split on "either(" calls.
pairs = []  # (eff_id, attr, mode)
for m in re.finditer(r'either\(\s*"([^"]+)"', txt):
    eff_id = m.group(1)
    # window until matching close paren is hard; instead take a generous window and find AttributeRegister + Mode/Operation
    start = m.end()
    window = txt[start:start+700]
    am = re.search(r'AttributeRegister\.(\w+)', window)
    attr = am.group(1) if am else "?"
    # find Mode (GAIN/REPLACE) or Operation
    mode = None
    mm = re.search(r'(GAIN|REPLACE)\b', window)
    if mm: mode = mm.group(1)
    op = None
    om = re.search(r'(ADD_VALUE|ADD_MULTIPLIED_BASE|ADD_MULTIPLIED_TOTAL|SET_MIN|SET_MAX|SET_VALUE)\b', window)
    if om: op = om.group(1)
    pairs.append((eff_id, attr, op, mode))

by_attr = collections.defaultdict(list)
for eff_id, attr, op, mode in pairs:
    by_attr[attr].append((eff_id, op, mode))

print("=== EFF ids per relevant attribute (attributes referenced in StatEffectGen) ===")
for attr in sorted(by_attr):
    print(f"\n{attr}:")
    for eff_id, op, mode in by_attr[attr]:
        print(f"    {eff_id}  op={op} mode={mode}")

# Save
json.dump({"pairs": pairs, "by_attr": {k:[list(x) for x in v] for k,v in by_attr.items()}},
          open(os.path.join(ROOT,"scripts/_eff_attr.json"),"w"), indent=0)
print("\nTotal either-mappings parsed:", len(pairs))
