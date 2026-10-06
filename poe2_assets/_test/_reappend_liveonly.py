# -*- coding: utf-8 -*-
"""把 421 个 corpus 外手写 live stat（rename_map.json 的 unmapped）从 .preregen 抽取完整块，
补回再生后的 StatIds.java（footer 之前）。保留原注释，不删真实 stat。
"""
import os, re, json

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
SID = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java")
PREREGEN = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java.preregen")

m = json.load(open(os.path.join(ROOT, "poe2_assets/_test/rename_map.json")))
unmapped = m["unmapped"]  # list of [name, id]
unmapped_names = {n for n, _ in unmapped}

# 从 preregen 抽取每个 unmapped 常量的完整块（含前导 // 注释行）
pre = open(PREREGEN, encoding="utf-8", errors="ignore").read()
# 匹配: 可选若干 // 注释行 + public static final Identifier NAME = id("ID");
pattern = re.compile(
    r"((?://[^\n]*\n)*?)\s*public static final Identifier (" +
    "|".join(re.escape(n) for n in unmapped_names) +
    r")\s*=\s*id\(\"([a-z0-9_]+)\"\);",
    re.S)
blocks = {}
for mm in pattern.finditer(pre):
    comment = mm.group(1)
    name = mm.group(2)
    eid = mm.group(3)
    blocks[name] = f"{comment.strip()}\n    public static final Identifier {name} = id(\"{eid}\");\n"

missing = unmapped_names - set(blocks)
print(f"unmapped 总数: {len(unmapped_names)}, 成功抽取块: {len(blocks)}, 未抽到: {len(missing)}")
if missing:
    print("  未抽到(前10):", list(missing)[:10])

# 插入再生文件 footer 之前
cur = open(SID, encoding="utf-8", errors="ignore").read()
footer_idx = cur.rfind("\n    private static Identifier id")
assert footer_idx != -1, "footer 未找到"
head = cur[:footer_idx]
tail = cur[footer_idx:]
append_text = "\n".join(blocks[n] for n, _ in unmapped if n in blocks)
if not append_text.endswith("\n"):
    append_text += "\n"
new = head.rstrip("\n") + "\n\n" + append_text + tail
open(SID, "w", encoding="utf-8").write(new)
print(f"补回后 StatIds 条目数: {len(re.findall(r'public static final Identifier [A-Z_0-9]+ = id\\(', new))}")
