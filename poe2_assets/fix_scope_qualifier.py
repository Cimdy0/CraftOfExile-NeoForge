#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Repair the scope-variant EFFs written by bulk_scope_wire.py.

Some attributes live on vanilla `Attributes` (e.g. ATTACK_SPEED, MAX_HEALTH)
while mod attributes live on `AttributeRegister`. The generator assumed
AttributeRegister for everything, which does not compile. Resolve each attribute
against how it is actually referenced elsewhere in StatEffectGen.java.
"""
import io, re, os

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
p = os.path.join(GEN, "StatEffectGen.java")
t = io.open(p, encoding="utf-8").read()

# how each attribute is referenced in the pre-existing (trusted) declarations
ref = {}
for m in re.finditer(r'\b(AttributeRegister|Attributes)\.(\w+)\b', t):
    ref.setdefault(m.group(2), m.group(1))

# pull in AttributeRegister/Attributes declaration sites too, to resolve names that
# only ever appear in the broken lines
def scan(path, qual):
    try:
        s = io.open(path, encoding="utf-8").read()
    except OSError:
        return
    for m in re.finditer(r'\b(?:Holder<Attribute>|public static final \w+)\s+(\w+)\s*=', s):
        ref.setdefault(m.group(1), qual)

import glob
for f in glob.glob(os.path.join(ROOT, "src", "main", "java", "**", "AttributeRegister.java"), recursive=True):
    scan(f, "AttributeRegister")

fixed = 0
out = []
for line in t.split("\n"):
    m = re.search(r'AttributeRegister\.(\w+)', line)
    if m and "作用域变体" not in line:
        pass
    # only touch the block we just generated
    out.append(line)
t = "\n".join(out)

# targeted repair: within the scope-variant block only
start = t.index("// ── 作用域变体（纯数据）：与 base EFF 同 attr/op/mode，仅追加作用域标签 ──")
end = t.index("        return m;", start)
block = t[start:end]
newblock = block
for m in list(re.finditer(r'AttributeRegister\.(\w+)', block)):
    attr = m.group(1)
    want = ref.get(attr)
    if want is None:
        # fall back: vanilla Minecraft attributes live on Attributes
        want = "Attributes"
    if want != "AttributeRegister":
        newblock = newblock.replace(f"AttributeRegister.{attr}", f"{want}.{attr}")
        fixed += 1
        print(f"  {attr}: AttributeRegister -> {want}")
t = t[:start] + newblock + t[end:]
io.open(p, "w", encoding="utf-8").write(t)
print("fixed refs =", fixed)
