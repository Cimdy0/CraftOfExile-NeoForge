import re, os
from collections import Counter

base = "E:/McModding/Modding/CraftOfExile-NeoForge"
sg_path = os.path.join(base, "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java")
ids_path = os.path.join(base, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java")

with open(sg_path, encoding="utf-8") as f:
    sg_text = f.read()
with open(ids_path, encoding="utf-8") as f:
    ids_text = f.read()

# 所有 StatIds 声明名
ids_names = re.findall(r'public static final Identifier\s+(\w+)\s*=\s*id\(', ids_text)
ids_set = set(ids_names)
print(f"StatIds 总声明: {len(ids_names)} (去重后 {len(ids_set)})")

# StatGen 中所有 StatIds.X 引用（含 map.put / ImmuneStatEffect / inc / add 等所有形式）
sg_ids_refs = re.findall(r'StatIds\.(\w+)', sg_text)
sg_ids_used = set(sg_ids_refs)
print(f"StatGen 中引用的 StatIds 去重数: {len(sg_ids_used)}")
print(f"StatGen 中 StatIds 引用总次数: {len(sg_ids_refs)}")

# 全工程 StatIds 引用（看是否被别处接线，如 AffixGen/EntityGen）
all_refs = []
for root, dirs, files in os.walk(os.path.join(base, "src/main/java")):
    for fn in files:
        if not fn.endswith(".java"):
            continue
        p = os.path.join(root, fn)
        with open(p, encoding="utf-8", errors="ignore") as f:
            txt = f.read()
        for m in re.finditer(r'StatIds\.(\w+)', txt):
            all_refs.append((m.group(1), p))
all_used = set(r[0] for r in all_refs)
print(f"全工程引用的 StatIds 去重数: {len(all_used)}")

# 未在任何地方被引用的 StatIds（纯孤儿）
orphan = ids_set - all_used
print(f"\n=== 孤儿 StatIds（声明但全工程零引用）: {len(orphan)} ===")

# 仅在 StatGen 外被引用（非 StatGen 接线的）
sg_only = sg_ids_used
non_sg = all_used - sg_ids_used
print(f"被 StatGen 接线的 StatIds: {len(sg_only)}")
print(f"被 StatGen 之外引用的 StatIds: {len(non_sg)}")

# 在 StatGen 被引用但不在 StatIds 声明的（悬空引用）
dangling = sg_ids_used - ids_set
print(f"StatGen 引用但 StatIds 未声明的（悬空）: {len(dangling)}")
if dangling:
    print("  ", sorted(dangling)[:20])

# 覆盖率
covered = len(sg_ids_used & ids_set)
print(f"\n=== 覆盖率 ===")
print(f"StatIds 已落地(StatGen 接线且声明存在): {covered}/{len(ids_set)} = {100*covered/len(ids_set):.1f}%")
print(f"StatIds 未落地(无 StatGen 写点): {len(ids_set - sg_ids_used)} = {100*(len(ids_set - sg_ids_used))/len(ids_set):.1f}%")
