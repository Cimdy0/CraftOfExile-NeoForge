#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Step 8: 去除独立 X —— 确定性重命名。
规则：去掉名称中独立的 X/x 段（长度1，仅字母X），合并下划线。
  X_INCREASED_INTELLIGENCE            -> INCREASED_INTELLIGENCE
  X_HEALTH_PER_X_DEXTERITY           -> HEALTH_PER_DEXTERITY
  ATTACK_DAMAGE_PENETRATES_X_OF_...  -> ATTACK_DAMAGE_PENETRATES_OF_...
同时重命名：常量声明名 + id("path") 字符串 + 全仓用法引用（StatIds./AttributeRegister./AdditionType.）。
坐标轴 x（max_x/image_offset_x 等）不含独立 X 段（x 为段尾但属坐标命名，经长度/位置规则已由 extract 标注为末位，这里仅处理 _ 切分中存在独立 X 段的名称）。
"""
import os, re, sys

STATIDS = "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"
ATTRREG = "src/main/java/com/cimdy/coe/data/AttributeRegister.java"
JAVA_ROOT = "src/main/java"

# 仅当段为独立 X（长度1 且全大写X/x）才去除
def has_x_seg(name):
    return any(s.upper() == "X" and len(s) == 1 for s in name.split("_") if s)

def transform(name):
    segs = [s for s in name.split("_") if s != ""]
    new = [s for s in segs if not (s.upper() == "X" and len(s) == 1)]
    return "_".join(new)

# ---- 收集所有已定义常量名（避免新名与现存常量冲突）----
def collect_defined(path, pat):
    out = {}
    if not os.path.exists(path):
        return out
    for line in open(path, encoding="utf-8"):
        m = re.search(pat, line)
        if m:
            out[m.group(1)] = m.group(2) if m.lastindex and m.lastindex >= 2 else None
    return out

CONST_PAT = r"public static final Identifier\s+([A-Z][A-Z0-9_]*)\s*=\s*id\(\s*\"([^\"]*)\"\s*\)"
ATTR_PAT  = r"public static (?:final )?Holder<Attribute>\s+([A-Z][A-Z0-9_]*)\s*=\s*\w+\(\s*\"([^\"]*)\""
# 所有 java 文件中的常量/属性声明（用于冲突检测）
all_const = {}
all_paths = set()
for dp, _, fs in os.walk(JAVA_ROOT):
    for f in fs:
        if not f.endswith(".java"):
            continue
        p = os.path.join(dp, f)
        for line in open(p, encoding="utf-8", errors="ignore"):
            for pat in (CONST_PAT, ATTR_PAT):
                m = re.search(pat, line)
                if m:
                    all_const[m.group(1)] = m.group(2)
                    if m.group(2):
                        all_paths.add(m.group(2))

# ---- 构建重命名映射（仅 StatIds + AttributeRegister 的声明）----
rename = {}  # OLD_NAME -> (NEW_NAME, OLD_PATH, NEW_PATH)
src_files = []  # (path, text) for declaration update
for path, pat in ((STATIDS, CONST_PAT), (ATTRREG, ATTR_PAT)):
    for line in open(path, encoding="utf-8"):
        m = re.search(pat, line)
        if not m:
            continue
        name, path_str = m.group(1), m.group(2)
        if not has_x_seg(name):
            continue
        new_name = transform(name)
        new_path = transform(path_str) if path_str else None
        rename[name] = (new_name, path_str, new_path)

# ---- 碰撞预检：冲突项从重命名集中排除（单独标记，交人工裁决）----
collisions = []
safe_rename = {}
for old, (new, op, np) in rename.items():
    conflict = False
    if new in all_const and all_const[new] != op:
        collisions.append(("NAME_EXISTS", old, new))
        conflict = True
    if np and np in all_paths and all_const.get(new) != op and np != op:
        owner = [k for k, v in all_const.items() if v == np]
        if owner:
            collisions.append(("PATH_EXISTS", old, np, owner[0]))
            conflict = True
    if not conflict:
        safe_rename[old] = (new, op, np)
rename = safe_rename

print("= 重命名映射统计 =")
print("待重命名常量/属性数: %d" % len(rename))
print("碰撞数: %d" % len(collisions))
for c in collisions[:50]:
    print("  碰撞:", c)
print()
print("= 样本映射（前 25）=")
for i, (old, (new, op, np)) in enumerate(sorted(rename.items())):
    if i >= 25:
        break
    print("  %s -> %s   (path: %s -> %s)" % (old, new, op, np))

if "--apply" in sys.argv:
    print("\n[APPLY] 执行重命名…")
    # 1) 声明文件：替换 名字 与 path（带单词边界）
    for path, _ in ((STATIDS, None), (ATTRREG, None)):
        text = open(path, encoding="utf-8").read()
        for old, (new, op, np) in rename.items():
            text = re.sub(r"\b%s\b" % re.escape(old), new, text)
            if op and np:
                text = re.sub(r"\b%s\b" % re.escape(op), np, text)
        open(path, "w", encoding="utf-8").write(text)
    # 2) 全仓其他 java：替换 用法引用（前缀 + 名字）
    for dp, _, fs in os.walk(JAVA_ROOT):
        for f in fs:
            if not f.endswith(".java"):
                continue
            p = os.path.join(dp, f)
            if p == STATIDS or p == ATTRREG:
                continue
            text = open(p, encoding="utf-8", errors="ignore").read()
            orig = text
            for old, (new, op, np) in rename.items():
                text = re.sub(r"\b%s\b" % re.escape(old), new, text)
            if text != orig:
                open(p, "w", encoding="utf-8").write(text)
    print("已落地重命名。")
