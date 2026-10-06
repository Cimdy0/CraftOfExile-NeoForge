# -*- coding: utf-8 -*-
"""StatIds 安全合并：恢复到 corpus 真值（~1166），只增不删、不改名。

方法：
- 复用 gen_stat_ids.py 的 EXACT normalize/to_name/to_id（import 模块，保证命名与生成器一致）。
- 生成 corpus 权威 1166 条目 (name, id, comment)。
- 解析当前 StatIds.java，提取现有 (name, id)。
- add-only：仅追加 corpus 有而当前无的条目；若 name 与现有冲突则追加 _N。
- 不删除、不重命名任何现有常量（保护 StatGen/引擎引用）。
"""
import os, re, sys

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
SID = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java")
sys.path.insert(0, os.path.join(ROOT, "poe2_assets"))
import gen_stat_ids as g

# ---- 1. 生成 corpus 权威条目 ----
raw = [l.rstrip("\n") for l in open(g.TEMPLATES, encoding="utf-8") if l.strip()]
templ = [g.normalize(t) for t in raw]
corpus = []
name_used, id_used = {}, {}
for t in templ:
    base_name = g.to_name(t)
    base_id = g.to_id(t)
    name, eid, k = base_name, base_id, 2
    while name in name_used or eid in id_used:
        name, eid = f"{base_name}_{k}", f"{base_id}_{k}"
        k += 1
    name_used[name] = True
    id_used[eid] = True
    corpus.append((name, eid, t))
print(f"corpus 权威条目数: {len(corpus)}")

# ---- 2. 解析当前 StatIds.java ----
src = open(SID, encoding="utf-8", errors="ignore").read()
m = re.search(r"(public final class StatIds \{)(.*?)(\n    private static Identifier id)", src, re.S)
assert m, "StatIds.java 结构异常"
header_marker = m.group(1)
body = m.group(2)
footer_marker = m.group(3)
existing_ids = set(re.findall(r'id\("([a-z0-9_]+)"\)', body))
existing_names = set(re.findall(r"public static final Identifier\s+([A-Z_][A-Z0-9_]*)\s*=\s*id\(", body))
print(f"当前 StatIds 条目数: {len(existing_ids)}")

# ---- 3. add-only 合并 ----
added = []
new_name_used = set(existing_names)
for name, eid, comment in corpus:
    if eid in existing_ids:
        continue  # 已存在（按 id 去重），保留现有（含手改命名）
    nm = name
    kk = 2
    while nm in new_name_used:
        nm = f"{name}_{kk}"
        kk += 1
    new_name_used.add(nm)
    added.append((nm, eid, comment))

print(f"将追加（恢复+补齐）: {len(added)} 条")

# ---- 4. 重写：保留现有 body，尾部追加新增 ----
# 现有 body 以若干空行/条目结尾；归一化尾部空白后追加
new_body = body.rstrip("\n")
if not new_body.endswith("\n"):
    new_body += "\n"
# 确保现有条目与新增之间有一个空行分隔
if not new_body.endswith("\n\n"):
    new_body += "\n"
for nm, eid, comment in added:
    new_body += f"    // {comment}\n"
    new_body += f"    public static final Identifier {nm} = id(\"{eid}\");\n\n"

new_src = header_marker + "\n" + new_body + footer_marker + "\n}\n"
open(SID, "w", encoding="utf-8").write(new_src)
print(f"写入完成。新 StatIds 条目数: {len(existing_ids) + len(added)}")
print("新增样例(前15):")
for nm, eid, c in added[:15]:
    print(f"  + {nm} = id(\"{eid}\")  // {c}")
