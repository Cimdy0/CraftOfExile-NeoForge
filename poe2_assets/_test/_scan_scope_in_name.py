#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""扫描属性字段名是否内嵌作用域(StatTag)。
判定：属性在 write(StatGen pctAdd/inc/chance/attrCond 等) 与 read(readInScope/getAttributeValue)
端实际注入的 StatTag 作用域集合，若字段名(大写拆分)含有其中任一标签常量名或标签值，即判为「字段名内嵌作用域」。
"""
import os, re, json, subprocess

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
SRC = os.path.join(ROOT, "src/main/java")

# 1) 解析 StatTag 常量名 + 标签值
tag_py = os.path.join(SRC, "com/cimdy/coe/data/codec/support/stat/StatTag.java")
tag_const, tag_val = set(), set()
with open(tag_py, encoding="utf-8") as f:
    for line in f:
        m = re.search(r'public static final String (\w+) = "([^"]+)";', line)
        if m:
            tag_const.add(m.group(1).upper())
            tag_val.add(m.group(2).upper())

# 2) 解析 AttributeRegister 声明 (NAME, key)
ar = os.path.join(SRC, "com/cimdy/coe/data/AttributeRegister.java")
attrs = []
with open(ar, encoding="utf-8") as f:
    for line in f:
        m = re.search(r'public static Holder<Attribute> (\w+) = register\w+\("([^"]+)"', line)
        if m:
            attrs.append((m.group(1), m.group(2)))

# 3) 对每个属性，扫描 src 中所有 AttributeRegister.NAME 出现处，提取同行 StatTag 标签
attr_names = [a[0] for a in attrs]
# 预编译：每行里所有 StatTag.XXX
statag_re = re.compile(r'StatTag\.([A-Z_]+)')

scope = {name: set() for name in attr_names}
# 也抓 attrCond 里以单独参数形式出现的 StatTag（同语句内即可）
for dp, _, files in os.walk(SRC):
    if ".new" in dp or ".pre_annotate" in dp:
        continue
    for fn in files:
        if not fn.endswith(".java"):
            continue
        if fn.endswith(".new") or ".pre_annotate" in fn:
            continue
        p = os.path.join(dp, fn)
        if ".new" in p or ".pre_annotate" in p:
            continue
        try:
            with open(p, encoding="utf-8") as f:
                for line in f:
                    if ".new" in p or ".pre_annotate" in p:
                        continue
                    for name in attr_names:
                        if f"AttributeRegister.{name}" in line:
                            for t in statag_re.findall(line):
                                scope[name].add(t)
        except Exception:
            pass

# 4) 比对
rows = []
for name, key in attrs:
    name_tokens = set(t for t in name.split("_") if t)
    embedded = set()
    for t in name_tokens:
        if t in tag_const or t in tag_val:
            embedded.add(t)
    # 仅当该属性确实在 write/read 端注入了作用域(否则维度载体本身即作用域，不算「重复」)
    has_scope_use = bool(scope[name])
    rows.append({
        "name": name, "key": key,
        "name_tokens": sorted(name_tokens),
        "scope_used": sorted(scope[name]),
        "embedded_in_name": sorted(embedded & ({t for t in tag_const} | set(tag_val))),
        "has_scope_use": has_scope_use,
    })

# 判定：字段名内嵌了「实际被注入」的作用域标签 => 真违规
violations = []
for r in rows:
    emb = set(r["embedded_in_name"])
    used = set(r["scope_used"])
    # 交集：名字里的标签 == 实际注入的作用域
    real = emb & used
    if real and r["has_scope_use"]:
        violations.append((r["name"], sorted(real), r["scope_used"], r["key"]))

print(f"属性总数={len(attrs)}；StatTag 常量={len(tag_const)}；字段名内嵌「实际注入作用域」的属性={len(violations)}")
print()
for name, real, used, key in violations:
    print(f"  [违规] {name}  (key={key})")
    print(f"          字段名内嵌作用域 = {real}")
    print(f"          write/read 实际注入作用域 = {used}")

# 输出 JSON 供后续裁定
out = {
    "total": len(attrs),
    "violations": [
        {"name": n, "embedded": e, "scope_used": u, "key": k} for n, e, u, k in violations
    ],
    "all": rows,
}
OUT = "E:/McModding/Modding/CraftOfExile-NeoForge/poe2_assets/_test/_scan_scope_in_name.json"
with open(OUT, "w", encoding="utf-8") as f:
    json.dump(out, f, ensure_ascii=False, indent=2)
print(f"\nJSON -> {OUT}")
