#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""属性使用端 × Stat 通道错接审计。
解析 AttributeRegister 全部属性 + 全仓 AttributeRegister.X 读取点，
交叉 stat_entry JSON（哪些属性由 Stat 条目驱动），定位错接候选。
输出 JSON 到 poe2_assets/_test/attr_miswire_audit.json
"""
import os, re, json

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
SRC = os.path.join(ROOT, "src", "main", "java")
GEN = os.path.join(ROOT, "src", "generated", "resources", "data", "coe", "coe", "stat_entry")
REG = os.path.join(SRC, "com", "cimdy", "coe", "data", "AttributeRegister.java")
STAGEN = os.path.join(SRC, "com", "cimdy", "coe", "data", "gen", "stat", "StatGen.java")

# 1) 解析属性注册表
field_re = re.compile(r'^\s*public static Holder<Attribute>\s+(\w+)\s*=\s*register\w+\("([\w]+)"')
attrs = {}  # name -> key
order = []
with open(REG, encoding="utf-8") as f:
    for line in f:
        m = field_re.match(line)
        if m:
            attrs[m.group(1)] = m.group(2)
            order.append(m.group(1))

# 2) 哪些属性由 stat_entry JSON 驱动（ModifierStatEffect 的 "attribute" 字段）
stat_driven = {}  # key -> [json file names]
if os.path.isdir(GEN):
    for fn in os.listdir(GEN):
        if not fn.endswith(".json"):
            continue
        try:
            with open(os.path.join(GEN, fn), encoding="utf-8") as jf:
                txt = jf.read()
        except Exception:
            continue
        for km in re.finditer(r'"attribute"\s*:\s*"(coe:[\w]+)"', txt):
            key = km.group(1)[4:]
            stat_driven.setdefault(key, []).append(fn)

# 3) 全仓扫描 AttributeRegister.X 引用
ref_re = re.compile(r'AttributeRegister\.([A-Z][A-Z0-9_]+)')
readmethod_re = re.compile(r'(getAttributeValue|readInScope|readInScopePublic|StatEngine\.fold|DamageHelper\.readInScopePublic|fold)\s*\(')
consumers = {}  # name -> list of (rel, line, snippet, method)
wire = {}      # name -> list
for dirpath, _, files in os.walk(SRC):
    for fn in files:
        if not fn.endswith(".java"):
            continue
        full = os.path.join(dirpath, fn)
        rel = os.path.relpath(full, ROOT).replace("\\", "/")
        try:
            with open(full, encoding="utf-8") as f:
                lines = f.readlines()
        except Exception:
            continue
        for i, line in enumerate(lines, 1):
            for rm in ref_re.finditer(line):
                name = rm.group(1)
                if name not in attrs:
                    continue
                if fn == "AttributeRegister.java":
                    continue  # 声明，跳过
                # 判断读取方法
                method = "?"
                # 在同文件后续行找；这里用本行 + 前后 1 行
                ctx = line
                if i > 1:
                    ctx = lines[i-2] + line
                mm = readmethod_re.search(ctx)
                if mm:
                    method = mm.group(1)
                entry = (rel, i, line.strip(), method)
                if fn == "StatGen.java":
                    wire.setdefault(name, []).append(entry)
                else:
                    consumers.setdefault(name, []).append(entry)

# 4) 分类
result = {"attr_count": len(attrs), "attributes": {}}
for name in order:
    key = attrs[name]
    cons = consumers.get(name, [])
    w = wire.get(name, [])
    driven = stat_driven.get(key, [])
    # 消费端读取方法统计
    methods = {}
    for (rel, ln, sn, mth) in cons:
        methods[mth] = methods.get(mth, 0) + 1
    result["attributes"][name] = {
        "key": key,
        "stat_driven": driven,          # 由 Stat 条目驱动（有 ModifierStatEffect 落该属性）
        "wire_count": len(w),          # StatGen 接线数
        "consumer_count": len(cons),
        "consumer_methods": methods,
        "consumers": [{"file": rel, "line": ln, "snippet": sn, "method": mth} for (rel, ln, sn, mth) in cons],
    }

# 5) 错接候选判定
# 规则 A: 消费端用 getAttributeValue（无作用域，绕过 Stat scope 通道）
# 规则 B: 消费端读但 stat_driven 为空 且 wire 为空（纯孤儿，仅注释）
# 规则 C: 消费端在伤害应用(DamagePipeline)中带元素/状态标签但属性本身可折入基础属性
miswire = {"no_scope_read": [], "orphan_unwired": [], "damage_app_scoped": []}
for name in order:
    info = result["attributes"][name]
    cons = info["consumers"]
    noscope = [c for c in cons if c["method"] == "getAttributeValue"]
    if noscope:
        miswire["no_scope_read"].append({"name": name, "key": info["key"],
                                         "sites": [{"file": c["file"], "line": c["line"], "snippet": c["snippet"]} for c in noscope]})
    if not info["stat_driven"] and info["wire_count"] == 0 and info["consumer_count"] > 0:
        miswire["orphan_unwired"].append({"name": name, "key": info["key"], "consumer_count": info["consumer_count"]})
    dmg = [c for c in cons if "DamagePipeline" in c["file"]]
    if dmg:
        miswire["damage_app_scoped"].append({"name": name, "key": info["key"],
                                             "sites": [{"file": c["file"], "line": c["line"], "snippet": c["snippet"], "method": c["method"]} for c in dmg]})

result["miswire"] = miswire

out = os.path.join(ROOT, "poe2_assets", "_test", "attr_miswire_audit.json")
with open(out, "w", encoding="utf-8") as f:
    json.dump(result, f, ensure_ascii=False, indent=2)

# 控制台摘要
print(f"属性总数: {len(attrs)}")
print(f"无作用域 getAttributeValue 读取(绕过 Stat scope): {len(miswire['no_scope_read'])}")
for x in miswire["no_scope_read"]:
    for s in x["sites"]:
        print(f"  {x['name']:35s} {s['file'].split('/')[-1]}:{s['line']}")
print(f"孤儿(消费端有读但无 Stat 条目/无接线): {len(miswire['orphan_unwired'])}")
for x in miswire["orphan_unwired"]:
    print(f"  {x['name']:35s} (consumers={x['consumer_count']})")
print(f"伤害应用(DamagePipeline)作用域读取: {len(miswire['damage_app_scoped'])}")
for x in miswire["damage_app_scoped"]:
    for s in x["sites"]:
        print(f"  {x['name']:35s} {s['file'].split('/')[-1]}:{s['line']} [{s['method']}]")
print(f"\n写入 {out}")
