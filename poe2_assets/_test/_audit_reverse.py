import re, os
from collections import Counter

base = "E:/McModding/Modding/CraftOfExile-NeoForge"
src = os.path.join(base, "src/main/java")
sg_path = os.path.join(base, "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java")

# ── 1. 提取所有 scope 读取现场标签（StatContext.of 参数中的字面标签）──
scope_tags = Counter()
ctx_of_re = re.compile(r'StatContext\.of\(')
for root, dirs, files in os.walk(src):
    for fn in files:
        if not fn.endswith(".java"):
            continue
        p = os.path.join(root, fn)
        with open(p, encoding="utf-8", errors="ignore") as f:
            txt = f.read()
        for m in ctx_of_re.finditer(txt):
            seg = txt[m.end(): m.end()+260]  # 取调用参数片段
            for s in re.findall(r'"([^"]+)"', seg):
                scope_tags[s] += 1
            for s in re.findall(r'StatTag\.(\w+)', seg):
                scope_tags[s] += 1

print(f"=== scope 读取现场字面标签字典（去重 {len(scope_tags)} 个，按频次）===")
for t, c in scope_tags.most_common():
    print(f"  {t:32s} x{c}")

# ── 2. StatGen 写点 tags 全集 ──
with open(sg_path, encoding="utf-8") as f:
    sg_text = f.read()
call_re = re.compile(
    r'(inc|add|chance|valueMod|pctAdd|pct|sbMod|esRechargeMod|more|entityMod)\s*\(\s*map\s*,\s*(StatIds\.\w+)\s*,\s*(AttributeRegister\.\w+)\s*(?:,\s*(.*?))?\s*\)\s*;'
)
write_tags = Counter()
write_tag_to_ids = {}
for m in call_re.finditer(sg_text):
    rest = m.group(4)
    if not rest:
        continue
    tags = re.findall(r'StatTag\.(\w+)|"([^"]+)"', rest)
    for a, b in tags:
        t = a or b
        write_tags[t] += 1
        write_tag_to_ids.setdefault(t, set()).add(m.group(2))

print(f"\n=== StatGen 写点所用标签全集（去重 {len(write_tags)} 个）===")
for t, c in write_tags.most_common():
    print(f"  {t:32s} x{c}")

# ── 3. 孤儿检测：写点标签不在任何 scope 现场字典 ──
scope_set = set(scope_tags.keys())
orphan = [t for t in write_tags if t not in scope_set]
print(f"\n=== 孤儿标签（写点用到但无任何 scope 现场）：{len(orphan)} 个 ===")
for t in sorted(orphan):
    ids = sorted(write_tag_to_ids.get(t, []))
    print(f"  {t:32s} 用于 {len(ids)} 写点: {ids[:6]}{'...' if len(ids)>6 else ''}")

# ── 4. 已知 scope 但写点零使用（潜在缺失方向，仅供参考）──
unused_scope = [t for t in scope_set if t not in write_tags]
print(f"\n=== scope 现场存在但 StatGen 写点零使用（仅供参考，可能经其他 gen 或动态标签）：{len(unused_scope)} 个 ===")
print("  ", sorted(unused_scope)[:40])
