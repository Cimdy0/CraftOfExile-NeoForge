# 描述覆盖率审计（stat/note 接线 + 描述覆盖）
#
# 背景：StatIds.java 每条目上方的 Java 注释即规范化的 PoE2 stat 描述语料
#       （如 `// +{X} to Armor`），共 602 条 canonical 描述，但仅作注释、
#       未接到运行时渲染。运行时描述由 StatEffect.describe() 重建，名字来自
#       `attribute.name.<key>` 翻译键（en_us.json）。
#
# 三轴度量（均静态分析，不依赖运行时）：
#   轴1 接线覆盖   ：StatIds(声明) -> StatGen(写点) 的落地率。
#   轴2 翻译覆盖   ：stat_effect 引用的去重属性 coe:<attr> 是否都有 attribute.name.<attr> 键。
#   轴3 孤儿描述   ：StatIds 带规范化描述注释、但全工程零引用（描述永不现身）。
#
# 复合「描述覆盖率」= 已接线且属性名可翻译的 StatIds / 全部带描述注释的 StatIds。

import re, os, json, glob
from collections import Counter

BASE = "E:/McModding/Modding/CraftOfExile-NeoForge"
SG_PATH = os.path.join(BASE, "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java")
IDS_PATH = os.path.join(BASE, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java")
LANG_PATH = os.path.join(BASE, "src/generated/resources/assets/coe/lang/en_us.json")
EFFECT_DIR = os.path.join(BASE, "src/generated/resources/data/coe/coe/stat_effect")

# ---------------------------------------------------------------- 轴1+轴3：StatIds / StatGen
with open(IDS_PATH, encoding="utf-8") as f:
    ids_text = f.read()
ids_names = re.findall(r'public static final Identifier\s+(\w+)\s*=\s*id\(', ids_text)
ids_set = set(ids_names)

with open(SG_PATH, encoding="utf-8") as f:
    sg_text = f.read()
sg_ids_used = set(re.findall(r'StatIds\.(\w+)', sg_text))

# 全工程 StatIds 引用（捕捉 AffixGen/EntityGen/天赋节点等接线方）
all_used = set()
for root, dirs, files in os.walk(os.path.join(BASE, "src/main/java")):
    for fn in files:
        if not fn.endswith(".java"):
            continue
        with open(os.path.join(root, fn), encoding="utf-8", errors="ignore") as f:
            txt = f.read()
        for m in re.finditer(r'StatIds\.(\w+)', txt):
            all_used.add(m.group(1))

wired = ids_set & all_used
orphan = ids_set - all_used
dangling = all_used - ids_set  # 引用了但未声明的（悬空）

print("=== 轴1 接线覆盖 (StatIds -> 全工程写点) ===")
print(f"StatIds 总声明 (canonical 描述池): {len(ids_set)}")
print(f"已接线 (全工程有引用): {len(wired)}  ->  {100*len(wired)/len(ids_set):.1f}%")
print(f"孤儿 (声明但零引用, 描述永不现身): {len(orphan)}  ->  {100*len(orphan)/len(ids_set):.1f}%")
print(f"悬空引用 (引用但未声明): {len(dangling)}")
if dangling:
    print("   ", sorted(dangling)[:20])

# ---------------------------------------------------------------- 轴2：属性名翻译覆盖
# 关键：describe() 用 Holder<Attribute>.getKey() 拼键 -> 带命名空间 `attribute.name.coe:<path>`；
#       而 lang 仅定义无命名空间 `attribute.name.<path>`。两种方案必须分别核对。
with open(LANG_PATH, encoding="utf-8") as f:
    lang = json.load(f)
lang_ns = set(k for k in lang if k.startswith("attribute.name.coe:"))      # 命名空间方案
lang_path = set(k for k in lang if k.startswith("attribute.name.")
                and not k.startswith("attribute.name.coe:"))

attr_paths = set()   # path-only，如 armor
for f in glob.glob(os.path.join(EFFECT_DIR, "*.json")):
    t = open(f, encoding="utf-8").read()
    for m in re.findall(r'"attribute"\s*:\s*"([^"]+)"', t):
        path = m.split(":", 1)[1] if ":" in m else m
        attr_paths.add(path)

# 真实键方案（describe 生成）= 命名空间；核对 lang 命中
covered_ns = {p for p in attr_paths if f"attribute.name.coe:{p}" in lang_ns}
covered_path = {p for p in attr_paths if f"attribute.name.{p}" in lang_path}
# 兼容：任一方案命中即视为「键存在」（用于区分「纯缺失键」与「方案不匹配」）
covered_either = covered_ns | covered_path
missing_keys = attr_paths - covered_either

print("\n=== 轴2 属性名翻译覆盖 (stat_effect 引用属性 -> lang 键) ===")
print(f"stat_effect 引用去重属性(path): {len(attr_paths)}")
print(f"  lang 命名空间键(attribute.name.coe:*) 数: {len(lang_ns)}")
print(f"  lang 无命名空间键(attribute.name.*) 数: {len(lang_path)}")
print(f"[真实方案=命名空间] 命中: {len(covered_ns)}/{len(attr_paths)} = {100*len(covered_ns)/len(attr_paths):.1f}%")
print(f"[兼容方案=path]      命中: {len(covered_path)}/{len(attr_paths)} = {100*len(covered_path)/len(attr_paths):.1f}%")
print(f"纯缺失翻译键(两方案皆无): {len(missing_keys)}")
if missing_keys:
    print("   ", sorted(missing_keys)[:30])
print(">> 结论：describe() 生成命名空间键，lang 仅有无命名空间键 -> 方案不匹配，")
print("   真实渲染覆盖趋近于 0%（所有属性名将泄漏为原始键，除非存在未发现的 stripping 路径）。")

# ---------------------------------------------------------------- 复合描述覆盖率
# 复合 = 已接线且真实键方案可翻译的 StatIds / 全部带描述注释的 StatIds。
# 因 describe() 生成命名空间键而 lang 无此方案，真实翻译率取 covered_ns 比例。
print("\n=== 复合描述覆盖率 ===")
real_trans_rate = len(covered_ns) / len(attr_paths) if attr_paths else 0
composite = 100 * (len(wired) / len(ids_set)) * real_trans_rate
print(f"接线率 {100*len(wired)/len(ids_set):.1f}%  ×  真实属性翻译率 {100*real_trans_rate:.1f}%")
print(f"近似复合描述覆盖率(命名空间方案): {composite:.1f}%")

print("\n=== 基线汇总 ===")
print(f"canonical 描述池(StatIds): {len(ids_set)}")
print(f"已接线: {len(wired)} | 孤儿: {len(orphan)} | 悬空: {len(dangling)}")
print(f"属性引用(path): {len(attr_paths)} | 真实键命中: {len(covered_ns)} | 纯缺失键: {len(missing_keys)}")
