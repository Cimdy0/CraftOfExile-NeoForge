#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""全量扫描属性：属性声明 -> StatGen 接线(op/tags) -> 运行时消费端分布。
输出原始数据供 FOLD-vs-FIRE 架构轴裁定。纯分析，不改动源码。"""
import re, os, subprocess, json

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
ATTR = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/AttributeRegister.java")
STGEN = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java")
SRC = os.path.join(ROOT, "src/main/java")

# 1) 属性声明
decl_re = re.compile(
    r'public static Holder<Attribute>\s+(\w+)\s*=\s*register\w+\("([^"]+)"\s*,\s*([^;]+)\);'
)
attrs = {}  # name -> {key, base, reg}
with open(ATTR, encoding="utf-8") as f:
    for line in f:
        m = decl_re.search(line)
        if m:
            name, key, args = m.group(1), m.group(2), m.group(3)
            nums = re.findall(r'-?\d+(?:\.\d+)?', args)
            base = nums[0] if nums else ""
            attrs[name] = {"key": key, "base": base}

# 2) StatGen 接线： op(map, [StatIds.X,] AttributeRegister.Y, [tags...])
#    匹配行首 op( 且含 AttributeRegister.
wire_re = re.compile(
    r'^\s*(\w+)\(\s*map\s*,\s*(?:StatIds\.\w+\s*,\s*)?AttributeRegister\.(\w+)(?:,\s*(.*))?\)\s*;'
)
# 提取 tags：行内 StatTag.XXX / "quoted" / plain tag 词
tag_re = re.compile(r'StatTag\.(\w+)|"([a-z_]+)"')
wiring = {}  # name -> list of (op, tags)
with open(STGEN, encoding="utf-8") as f:
    for line in f:
        m = wire_re.search(line)
        if m:
            op, name, rest = m.group(1), m.group(2), m.group(3) or ""
            tags = []
            for tm in tag_re.finditer(rest):
                tags.append(tm.group(1) or tm.group(2))
            # 还原 not(...) 表达
            if "not(" in rest:
                tags = [("!" + t if False else t) for t in tags]
            wiring.setdefault(name, []).append((op, tags))

# 3) 运行时消费端分布（排除 AttributeRegister.java / StatGen.java）
def consumer_files(name):
    pat = f"AttributeRegister\\.{name}"
    # 用 grep -rl 等效：遍历文件
    res = subprocess.run(
        ["grep", "-rl", pat, "--include=*.java", SRC],
        capture_output=True, text=True
    )
    files = [p for p in res.stdout.splitlines() if p and "AttributeRegister.java" not in p and "StatGen.java" not in p]
    return files

rows = []
for name in sorted(attrs):
    a = attrs[name]
    wires = wiring.get(name, [])
    ops = sorted({w[0] for w in wires})
    tagset = set()
    for _, ts in wires:
        tagset.update(ts)
    cf = consumer_files(name)
    rows.append({
        "name": name,
        "key": a["key"],
        "base": a["base"],
        "ops": ",".join(ops) if ops else "(无接线)",
        "tags": ",".join(sorted(tagset)) if tagset else "",
        "n_wires": len(wires),
        "n_consumers": len(cf),
        "consumers": ";".join(os.path.basename(p).replace(".java","") for p in cf),
    })

out = os.path.join(os.path.dirname(ATTR), "_scan_fold_vs_fire.json")
with open(out, "w", encoding="utf-8") as f:
    json.dump(rows, f, ensure_ascii=False, indent=1)

# 控制台摘要
print(f"属性总数={len(rows)}")
no_wire = [r for r in rows if r["n_wires"] == 0]
orphan = [r for r in rows if r["n_consumers"] == 0]
print(f"无 StatGen 接线={len(no_wire)}: " + ", ".join(r['name'] for r in no_wire))
print(f"无运行时消费端(孤儿)={len(orphan)}: " + ", ".join(r['name'] for r in orphan))
print(f"\nJSON -> {out}")
