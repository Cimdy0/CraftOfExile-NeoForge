#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
属性消费端调用扫描（只读、静态）。
目标：对 AttributeRegister 中全部属性，统计其在 src 中的引用分布，
区分 StatGen 接线 vs 消费端读取，标记死属性/孤儿属性，补充清理范围。

分类：
  WIRE        StatGen.java 中的接线引用（AttributeRegister.X 出现在 gen/stat/StatGen）
  CONSUMER    其它文件中的读取引用（getAttributeValue / readInScope / StatEngine.fold / DamageHelper 等）
  DEF         声明本身（AttributeRegister.java 内的字段定义，不计为使用）

属性状态判定：
  DEAD_ZERO        无 WIRE 且无 CONSUMER（完全未用）
  ORPHAN_WIRED     有 WIRE 但无 CONSUMER（接线孤儿，安全可清候选）
  CONSUMED_NO_WIRE 有 CONSUMER 但无 WIRE（直读属性，如 REMNANT_MAGNITUDE）
  NORMAL           两者皆有
"""
import json, re, os, glob

SRC = "E:/McModding/Modding/CraftOfExile-NeoForge/src"
REG = os.path.join(SRC, "main/java/com/cimdy/coe/data/AttributeRegister.java")
STAGEN = os.path.join(SRC, "main/java/com/cimdy/coe/data/gen/stat/StatGen.java")

# 1) 提取全部属性字段名
fields = []
with open(REG, encoding="utf-8") as f:
    for line in f:
        m = re.search(r"public static Holder<Attribute>\s+(\w+)\s*=\s*register", line)
        if m:
            fields.append(m.group(1))
print(f"parsed {len(fields)} attribute fields")

# 2) 收集 src 全部 java 行中的 AttributeRegister.X 引用
ref_re = re.compile(r"AttributeRegister\.(\w+)")
refs = {}  # name -> list of (relpath, line, snippet)
for path in glob.glob(SRC + "/**/*.java", recursive=True):
    rel = os.path.relpath(path, SRC)
    try:
        with open(path, encoding="utf-8") as fh:
            for i, line in enumerate(fh, 1):
                for mm in ref_re.finditer(line):
                    name = mm.group(1)
                    refs.setdefault(name, []).append((rel.replace("\\", "/"), i, line.strip()))
    except Exception as e:
        print("ERR", path, e)

# 3) 分类
WIRE_FILE = "main/java/com/cimdy/coe/data/gen/stat/StatGen.java"
DEF_FILE = "main/java/com/cimdy/coe/data/AttributeRegister.java"

READ_HINTS = ("getAttributeValue", "readInScope", "StatEngine.fold", "DamageHelper",
              "LivingAttributeValue", "getModifiedValue", "fold(", "StatScopes")

def classify(refs_for_name):
    wire = []
    consumer = []
    for rel, ln, snip in refs_for_name:
        if rel == WIRE_FILE:
            wire.append((rel, ln, snip))
        elif rel == DEF_FILE:
            continue  # 声明
        else:
            consumer.append((rel, ln, snip))
    return wire, consumer

results = {}
dead, orphan, consumed_no_wire, normal = [], [], [], []
for name in fields:
    r = refs.get(name, [])
    wire, consumer = classify(r)
    status = None
    if not wire and not consumer:
        status = "DEAD_ZERO"
        dead.append(name)
    elif wire and not consumer:
        status = "ORPHAN_WIRED"
        orphan.append(name)
    elif consumer and not wire:
        status = "CONSUMED_NO_WIRE"
        consumed_no_wire.append(name)
    else:
        status = "NORMAL"
        normal.append(name)
    results[name] = {
        "status": status,
        "wire_count": len(wire),
        "consumer_count": len(consumer),
        "wire": [{"file": a, "line": b, "snippet": c} for a, b, c in wire],
        "consumer": [{"file": a, "line": b, "snippet": c} for a, b, c in consumer],
    }

out = {
    "totals": {
        "attributes": len(fields),
        "dead_zero": len(dead),
        "orphan_wired": len(orphan),
        "consumed_no_wire": len(consumed_no_wire),
        "normal": len(normal),
    },
    "dead_zero": dead,
    "orphan_wired": orphan,
    "consumed_no_wire": consumed_no_wire,
    "normal": normal,
    "details": results,
}
with open(os.path.join(SRC, "../poe2_assets/_test/attr_consumer_audit.json"), "w", encoding="utf-8") as f:
    json.dump(out, f, indent=1, ensure_ascii=False)

print("DEAD_ZERO(%d): %s" % (len(dead), dead))
print()
print("ORPHAN_WIRED(%d): %s" % (len(orphan), orphan))
print()
print("CONSUMED_NO_WIRE(%d): %s" % (len(consumed_no_wire), consumed_no_wire))
print()
print("NORMAL count=%d" % len(normal))
