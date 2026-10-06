#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""协同删除 72 死属性 + 78 悬空 StatIds（连同紧邻注释块）。安全前提：_plan_deletions 已确认 0 外部引用。"""
import os, re, json, shutil

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
SRC = os.path.join(ROOT, "src", "main", "java")
ATTR_FILE = os.path.join(SRC, "com", "cimdy", "coe", "data", "AttributeRegister.java")
SID_FILE = os.path.join(SRC, "com", "cimdy", "coe", "data", "codec", "support", "stat", "StatIds.java")

lst = json.load(open(os.path.join(ROOT, "poe2_assets/_test/delete_lists.json"), encoding="utf-8"))
dead_attrs = set(lst["dead_attributes"])
dangling_sids = set(lst["dangling_statids"])

def is_comment(s):
    return s.startswith("//") or s.startswith("*") or s.startswith("/*") or s.startswith("/**") or s == "*/"

def remove_decls(path, names, kind):
    lines = open(path, encoding="utf-8", errors="ignore").read().split("\n")
    removed = 0
    # iterate over a copy of indices; process from bottom to keep indices stable
    for i in range(len(lines) - 1, -1, -1):
        line = lines[i]
        m = re.match(rf"^\s*public (?:static Holder<Attribute>|static final Identifier)\s+([A-Z_][A-Z0-9_]*)\b", line)
        if not m:
            continue
        name = m.group(1)
        if name not in names:
            continue
        to_del = {i}
        j = i - 1
        while j >= 0:
            s = lines[j].strip()
            if s == "":
                break
            if is_comment(s):
                to_del.add(j)
                j -= 1
            else:
                break
        for k in sorted(to_del, reverse=True):
            del lines[k]
        removed += 1
        names.discard(name)
    open(path, "w", encoding="utf-8").write("\n".join(lines))
    return removed, len(names)

# backup
shutil.copy2(ATTR_FILE, ATTR_FILE + ".coherebak")
shutil.copy2(SID_FILE, SID_FILE + ".coherebak")

ra, left_a = remove_decls(ATTR_FILE, set(dead_attrs), "attr")
rs, left_s = remove_decls(SID_FILE, set(dangling_sids), "sid")
print(f"属性删除: {ra} (剩余未匹配 {left_a})")
print(f"StatIds删除: {rs} (剩余未匹配 {left_s})")
