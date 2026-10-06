#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Wave-12 块属性引用面审计。
解析 AttributeRegister 中 'Wave-12 数据先行' 注释后到 'AUTO-GEN POE2 TALENT ATTRIBUTES END'
之间的全部 public static Holder<Attribute> 字段，扫描整个 src 与生成物，按引用面分类。
"""
import os, re, json, sys

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
SRC = os.path.join(ROOT, "src", "main", "java")
GEN = os.path.join(ROOT, "src", "generated", "resources", "data", "coe", "coe", "stat_entry")
RES = os.path.join(ROOT, "src", "main", "resources")

ATTR_FILE = os.path.join(SRC, "com", "cimdy", "coe", "data", "AttributeRegister.java")

text = open(ATTR_FILE, encoding="utf-8").read()

# 定位 Wave-12 块
start = text.index("批量落地（Wave-12")
end = text.index("AUTO-GEN POE2 TALENT ATTRIBUTES END")
block = text[start:end]

# 提取字段名 + 注册名(snake)
fields = []
for m in re.finditer(r"public static Holder<Attribute>\s+(\w+)\s*=\s*register\w+\(\s*\"([^\"]+)\"", block):
    fields.append((m.group(1), m.group(2)))

print(f"WAVE-12 FIELDS: {len(fields)}")

# 收集 src 全部 .java 文本
java_files = []
for dp, _, fns in os.walk(SRC):
    for f in fns:
        if f.endswith(".java"):
            java_files.append(os.path.join(dp, f))
java_text = {}
for f in java_files:
    try:
        java_text[f] = open(f, encoding="utf-8", errors="ignore").read()
    except Exception:
        pass

# 收集生成物 stat_entry json 文本
gen_files = []
if os.path.isdir(GEN):
    for f in os.listdir(GEN):
        if f.endswith(".json"):
            gen_files.append(f)
gen_text = {}
for f in gen_files:
    try:
        gen_text[f] = open(os.path.join(GEN, f), encoding="utf-8", errors="ignore").read()
    except Exception:
        pass

# 收集资源 json（天赋数据）
res_text = {}
if os.path.isdir(RES):
    for dp, _, fns in os.walk(RES):
        for f in fns:
            if f.endswith(".json"):
                try:
                    res_text[os.path.join(dp, f)] = open(os.path.join(dp, f), encoding="utf-8", errors="ignore").read()
                except Exception:
                    pass

# 收集 StatIds.java 文本
statids_text = ""
for f in java_files:
    if f.endswith("StatIds.java"):
        statids_text = java_text[f]
        break

def count_refs(name_const, snake):
    """返回各类引用计数。"""
    attr_ref = 0          # AttributeRegister.NAME
    statids_ref = 0       # StatIds.NAME 或 id("snake")
    gen_ref = 0           # 生成物 json 含 "coe:snake"
    res_ref = 0           # 资源 json 含 snake
    attr_files = set()
    for f, t in java_text.items():
        c = len(re.findall(r"\bAttributeRegister\." + re.escape(name_const) + r"\b", t))
        if c:
            attr_ref += c
            attr_files.add(os.path.basename(f))
    # StatIds 引用：常量名或 snake id
    if re.search(r"\b" + re.escape(name_const) + r"\b", statids_text):
        statids_ref += 1
    if re.search(r"id\(\"" + re.escape(snake) + r"\"\)", statids_text):
        statids_ref += 1
    for f, t in gen_text.items():
        if '"coe:%s"' % snake in t or 'coe:%s' % snake in t:
            gen_ref += 1
    for f, t in res_text.items():
        if '"%s"' % snake in t or snake in t:
            res_ref += 1
    return attr_ref, statids_ref, gen_ref, res_ref, sorted(attr_files)

rows = []
for name_const, snake in fields:
    ar, sr, gr, rr, afiles = count_refs(name_const, snake)
    rows.append((name_const, snake, ar, sr, gr, rr, afiles))

# 分类
ZERO = []      # 零引用（含 StatGen 写点也无）
GEN_ONLY = []  # 有生成物但无 java 除 AttributeRegister 外引用
WITH_WRITER = []  # 有 StatGen 或其它 java 写点
OTHER = []

for name_const, snake, ar, sr, gr, rr, afiles in rows:
    # 去掉 AttributeRegister 自身
    real_java = [a for a in afiles if a != "AttributeRegister.java"]
    if ar == 0 and sr == 0 and gr == 0 and rr == 0:
        ZERO.append((name_const, snake))
    elif gr > 0 and not real_java and sr == 0:
        GEN_ONLY.append((name_const, snake, gr))
    elif real_java or sr > 0:
        WITH_WRITER.append((name_const, snake, ar, sr, gr, rr, real_java))
    else:
        OTHER.append((name_const, snake, ar, sr, gr, rr, real_java))

print("\n=== ZERO REFERENCE (dead registration, no writer/statid/gen/res) ===")
for n, s in ZERO:
    print(f"  {n}  ({s})")

print(f"\n=== GEN-ONLY (has stat_entry json, no StatIds/extra java; data-first writers) === ({len(GEN_ONLY)})")
for n, s, g in GEN_ONLY:
    print(f"  {n}  ({s})  gen={g}")

print(f"\n=== WITH WRITER / EXTRA JAVA REF === ({len(WITH_WRITER)})")
for n, s, ar, sr, gr, rr, real_java in WITH_WRITER:
    print(f"  {n}  ({s})  attrRef={ar} statids={sr} gen={gr} res={rr} files={real_java}")

if OTHER:
    print(f"\n=== OTHER === ({len(OTHER)})")
    for n, s, ar, sr, gr, rr, real_java in OTHER:
        print(f"  {n}  ({s})  attrRef={ar} statids={sr} gen={gr} res={rr} files={real_java}")

# 输出 json 供后续使用
out = {
    "zero": [{"const": n, "snake": s} for n, s in ZERO],
    "gen_only": [{"const": n, "snake": s, "gen": g} for n, s, g in GEN_ONLY],
    "with_writer": [{"const": n, "snake": s, "attrRef": ar, "statids": sr, "gen": gr, "res": rr, "files": real_java} for n, s, ar, sr, gr, rr, real_java in WITH_WRITER],
}
with open(os.path.join(ROOT, "poe2_assets", "_test", "wave12_audit.json"), "w", encoding="utf-8") as f:
    json.dump(out, f, ensure_ascii=False, indent=2)
print(f"\nTOTAL fields={len(fields)} ZERO={len(ZERO)} GEN_ONLY={len(GEN_ONLY)} WITH_WRITER={len(WITH_WRITER)} OTHER={len(OTHER)}")
