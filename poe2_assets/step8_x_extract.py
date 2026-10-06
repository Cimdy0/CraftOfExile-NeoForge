#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Step 8: 全盘提取「独立的 X」token。
覆盖：Java 字段名（大写 X）、Java 字符串字面量（id path 等小写 x）、
JSON/资源文件字符串、stat_templates.txt 源语料 {X}。
按 X 在 `_` 切分序列中的「前/中/末」位置分类。
"""
import os, re, json

ROOT = "."
JAVA_ROOT = "src/main/java"
RES_ROOT = "src/main/resources"
POE_TREE = "poe2_assets/poe2_tree.json"
TEMPLATES = "poe2_assets/stat_templates.txt"

# 段 = 独立 X/x（长度为1，仅字母 X/x），按下划线切分
def classify(text):
    """返回 (front, middle, end) 三个布尔，表示 X 出现在 _ 切分序列的 前/中/末。"""
    segs = [s for s in re.split(r"_", text) if s != ""]
    front = middle = end = False
    for i, s in enumerate(segs):
        if s.upper() == "X" and len(s) == 1:
            if len(segs) == 1:
                front = middle = end = True
            elif i == 0:
                front = True
            elif i == len(segs) - 1:
                end = True
            else:
                middle = True
    return front, middle, end

# 收集结构
data = {
    "java_field":   {"front": [], "middle": [], "end": []},   # 字段名（大写 X）
    "java_string":  {"front": [], "middle": [], "end": []},   # 字符串字面量（含 id path 小写 x）
    "json_string":  {"front": [], "middle": [], "end": []},   # JSON/资源字符串
    "template_X":   [],                                        # {X} 模板
}

# ---------- Java 文件 ----------
IDENT_DECL = re.compile(r"public static final Identifier\s+([A-Z][A-Z0-9_]*)\s*=\s*id\(\s*\"([^\"]*)\"\s*\)")
ATTR_DECL  = re.compile(r"public static (?:final )?Holder<Attribute>\s+([A-Z][A-Z0-9_]*)\s*=")
STR_LIT    = re.compile(r"\"([^\"]*)\"")
USAGE      = re.compile(r"(?:StatIds|AttributeRegister|AdditionType)\.([A-Z][A-Z0-9_]*)")

field_names = set()
for dp, _, fs in os.walk(JAVA_ROOT):
    for f in fs:
        if not f.endswith(".java"):
            continue
        p = os.path.join(dp, f)
        txt = open(p, encoding="utf-8", errors="ignore").read()
        for m in IDENT_DECL.finditer(txt):
            name, path = m.group(1), m.group(2)
            field_names.add(name)
            fr, mi, en = classify(name)
            if fr: data["java_field"]["front"].append(name)
            if mi: data["java_field"]["middle"].append(name)
            if en: data["java_field"]["end"].append(name)
            # id path 字符串（小写 x）
            fr2, mi2, en2 = classify(path)
            if fr2: data["java_string"]["front"].append("%s  (%s)" % (path, name))
            if mi2: data["java_string"]["middle"].append("%s  (%s)" % (path, name))
            if en2: data["java_string"]["end"].append("%s  (%s)" % (path, name))
        for m in ATTR_DECL.finditer(txt):
            name = m.group(1)
            field_names.add(name)
            fr, mi, en = classify(name)
            if fr: data["java_field"]["front"].append(name)
            if mi: data["java_field"]["middle"].append(name)
            if en: data["java_field"]["end"].append(name)
        # 其余字符串字面量（排除已处理的 id path）
        for m in STR_LIT.finditer(txt):
            s = m.group(1)
            if not re.search(r"[Xx]", s):
                continue
            fr, mi, en = classify(s)
            if fr or mi or en:
                tag = "  [%s]" % p.split("/")[-1]
                if fr: data["java_string"]["front"].append(s + tag)
                if mi: data["java_string"]["middle"].append(s + tag)
                if en: data["java_string"]["end"].append(s + tag)
        # 用法引用名
        for m in USAGE.finditer(txt):
            name = m.group(1)
            if name in field_names:
                continue
            fr, mi, en = classify(name)
            if fr: data["java_field"]["front"].append(name)
            if mi: data["java_field"]["middle"].append(name)
            if en: data["java_field"]["end"].append(name)

# ---------- JSON / 资源文件 ----------
def scan_json_strings(obj, out):
    if isinstance(obj, dict):
        for k, v in obj.items():
            scan_json_strings(k, out)
            scan_json_strings(v, out)
    elif isinstance(obj, list):
        for v in obj:
            scan_json_strings(v, out)
    elif isinstance(obj, str):
        out.append(obj)

json_strings = []
if os.path.exists(POE_TREE):
    try:
        scan_json_strings(json.load(open(POE_TREE, encoding="utf-8")), json_strings)
    except Exception as e:
        json_strings.append("__PARSE_ERROR__" + str(e))
for dp, _, fs in os.walk(RES_ROOT):
    for f in fs:
        if f.endswith(".json"):
            try:
                scan_json_strings(json.load(open(os.path.join(dp, f), encoding="utf-8")), json_strings)
            except Exception:
                pass
for s in json_strings:
    fr, mi, en = classify(s)
    if fr: data["json_string"]["front"].append(s)
    if mi: data["json_string"]["middle"].append(s)
    if en: data["json_string"]["end"].append(s)

# ---------- 源语料 {X} ----------
if os.path.exists(TEMPLATES):
    for line in open(TEMPLATES, encoding="utf-8"):
        if "{X}" in line or "{x}" in line:
            data["template_X"].append(line.rstrip("\n"))

# ---------- 去重排序 ----------
for k in data:
    if isinstance(data[k], dict):
        for pos in data[k]:
            data[k][pos] = sorted(set(data[k][pos]))
    else:
        data[k] = sorted(set(data[k]))

# ---------- 输出 ----------
def md_list(items, cap=400):
    if not items:
        return "  _(无)_"
    if len(items) > cap:
        head = "\n".join("  - `%s`" % i for i in items[:cap])
        return head + "\n  - … 共 %d 条（已截断）" % len(items)
    return "\n".join("  - `%s`" % i for i in items)

L = []
L.append("# Step 8 — 独立 X 全盘提取（字段 + 字符串，前/中/末）\n")
L.append("## 1. 扫描范围\n")
L.append("- Java 源码：`%s`（字段名 + 字符串字面量 + 用法引用）" % JAVA_ROOT)
L.append("- 天赋树数据：`%s`" % POE_TREE)
L.append("- 资源文件：`%s/**/*.json`" % RES_ROOT)
L.append("- 源语料：`%s`（提取 `{X}` 模板）\n")

L.append("## 2. 汇总（X 在 `_` 切分序列中的位置）\n")
L.append("| 位置 | Java 字段名(大写X) | Java 字符串(小写x/id) | JSON/数据字符串 | 源语料 `{X}` |")
L.append("| --- | ---: | ---: | ---: | ---: |")
jf = data["java_field"]; js = data["java_string"]; jn = data["json_string"]
L.append("| 前 (X_/x_) | %d | %d | %d | — |" % (len(jf["front"]), len(js["front"]), len(jn["front"])))
L.append("| 中 (_X_/_x_) | %d | %d | %d | — |" % (len(jf["middle"]), len(js["middle"]), len(jn["middle"])))
L.append("| 末 (_X/_x) | %d | %d | %d | — |" % (len(jf["end"]), len(js["end"]), len(jn["end"])))
L.append("| **合计** | **%d** | **%d** | **%d** | **%d** |" % (
    len(jf["front"])+len(jf["middle"])+len(jf["end"]),
    len(js["front"])+len(js["middle"])+len(js["end"]),
    len(jn["front"])+len(jn["middle"])+len(jn["end"]),
    len(data["template_X"])))
L.append("")

L.append("## 3. Java 字段名中的独立 X（Identifier 常量 + Attribute 属性 + 用法引用）\n")
L.append("### 3.1 前缀 `X_`（前）— %d 条\n%s\n" % (len(jf["front"]), md_list(jf["front"])))
L.append("### 3.2 中缀 `_X_`（中）— %d 条\n%s\n" % (len(jf["middle"]), md_list(jf["middle"])))
L.append("### 3.3 后缀 `_X`（末）— %d 条\n%s\n" % (len(jf["end"]), md_list(jf["end"])))

L.append("## 4. Java 字符串字面量中的独立 x（id path 等）\n")
L.append("### 4.1 前缀 `x_`（前）— %d 条\n%s\n" % (len(js["front"]), md_list(js["front"])))
L.append("### 4.2 中缀 `_x_`（中）— %d 条\n%s\n" % (len(js["middle"]), md_list(js["middle"])))
L.append("### 4.3 后缀 `_x`（末）— %d 条\n%s\n" % (len(js["end"]), md_list(js["end"])))

L.append("## 5. JSON / 资源文件字符串中的独立 x\n")
L.append("### 5.1 前缀 `x_`（前）— %d 条\n%s\n" % (len(jn["front"]), md_list(jn["front"])))
L.append("### 5.2 中缀 `_x_`（中）— %d 条\n%s\n" % (len(jn["middle"]), md_list(jn["middle"])))
L.append("### 5.3 后缀 `_x`（末）— %d 条\n%s\n" % (len(jn["end"]), md_list(jn["end"])))

L.append("## 6. 源语料 `{X}` 模板 — %d 条\n%s\n" % (len(data["template_X"]), md_list(data["template_X"], cap=600)))

L.append("## 7. 关键发现\n")
L.append("- 字段名中 X 为大写（`X_...`），其对应 `id(\"...\")` 字符串中 X 为小写（`x_...`）——同一 token 两形态。")
L.append("- 「前」类（X_ 前缀）是主体；「中」类（`_X_`）多为多数值占位（如 `..._PER_X_...`、`..._X_OF_...`）。")
L.append("- 「末」类（_X 后缀）在当前代码库中几乎不存在（命名约定未采用后缀形式）。")
L.append("- `{X}` 仅存于源语料 `stat_templates.txt`，是生成 X_ 常量名的源头；落地后 Java 层已无 `{X}` 字面量。")

out = "\n".join(L)
open("poe2_assets/_test/step8_x_extract.md", "w", encoding="utf-8").write(out)
print("已写入 poe2_assets/_test/step8_x_extract.md")
print("字段名 X: 前=%d 中=%d 末=%d" % (len(jf["front"]), len(jf["middle"]), len(jf["end"])))
print("字符串 x: 前=%d 中=%d 末=%d" % (len(js["front"]), len(js["middle"]), len(js["end"])))
print("JSON x:   前=%d 中=%d 末=%d" % (len(jn["front"]), len(jn["middle"]), len(jn["end"])))
print("{X} 模板: %d" % len(data["template_X"]))
