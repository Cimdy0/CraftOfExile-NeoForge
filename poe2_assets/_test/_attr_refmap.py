# -*- coding: utf-8 -*-
"""step12: AttributeRegister 全量引用面审计。
- 提取全部注册属性
- 对每个属性统计全工程引用（排除 AttributeRegister 自身定义与注释）
- 分类：runtime(运行时读) / datagen(数据写点) / equip(装备侧) / other / dead(零引用)
- 输出 Wave-12 占位属性的引用明细，供归一聚类判定
"""
import re, os, json, collections

ROOT = r"E:/McModding/Modding/CraftOfExile-NeoForge/src/main/java"
AR = os.path.join(ROOT, "com/cimdy/coe/data/AttributeRegister.java")

src = open(AR, encoding="utf-8").read()
attrs = []  # (NAME, reg_call)
for m in re.finditer(r'public static Holder<Attribute> (\w+) = (register\w+)\(', src):
    attrs.append((m.group(1), m.group(2)))
print(f"total registered attributes: {len(attrs)}")

# 收集全部 java 文件（排除备份）
files = []
for dirpath, dirnames, filenames in os.walk(ROOT):
    if "_backup" in dirpath or "bak" in dirpath:
        continue
    for fn in filenames:
        if fn.endswith(".java") and fn != "AttributeRegister.java":
            files.append(os.path.join(dirpath, fn))

# 每属性引用分类
def classify(path):
    p = path.replace("\\", "/")
    if "/data/gen/stat/StatGen.java" in p: return "datagen"
    if "/data/gen/" in p: return "equip"
    if "/helper/" in p or "/pipeline/" in p or "/engine/" in p or "/status/" in p \
       or "/codec/support/stat/" in p or "/skill/" in p or "/entity/" in p or "/item/" in p:
        return "runtime"
    return "other"

report = {}
for name, reg in attrs:
    pat = re.compile(r'AttributeRegister\.' + re.escape(name) + r'\b')
    cats = collections.Counter()
    detail = []
    for f in files:
        try:
            txt = open(f, encoding="utf-8").read()
        except Exception:
            continue
        if pat.search(txt):
            c = classify(f)
            cats[c] += 1
            detail.append((c, os.path.relpath(f, ROOT)))
    report[name] = {"reg": reg, "cats": dict(cats), "detail": detail}

# 汇总
dead = [n for n, r in report.items() if not r["cats"]]
runtime_only = [n for n, r in report.items() if "runtime" in r["cats"]]
print(f"zero-reference (dead): {len(dead)}")
print(f"has runtime read: {len(runtime_only)}")
print(f"datagen-written: {sum(1 for r in report.values() if 'datagen' in r['cats'])}")
print(f"equip-referenced: {sum(1 for r in report.values() if 'equip' in r['cats'])}")

json.dump(report, open(os.path.join(os.path.dirname(__file__), "attr_refmap.json"), "w", encoding="utf-8"),
          ensure_ascii=False, indent=1)
print("refmap -> attr_refmap.json")

# Wave-12 区段（占位属性批量落地段）明细
m = re.search(r'批量落地（Wave-12.*?b3 追加（Wave-12）──\n(.*?)\n', src, re.S)
wave12 = []
for line in src.splitlines():
    mm = re.match(r'\s*public static Holder<Attribute> (\w+) = ', line)
    if mm:
        wave12.append(mm.group(1))
# 用行号界定 Wave-12 区段
lines = src.splitlines()
start = next(i for i, l in enumerate(lines) if "批量落地（Wave-12" in l)
end = next(i for i, l in enumerate(lines) if "b3 追加（Wave-12）──" in l)
w12_names = []
for l in lines[start:end + 25]:
    mm = re.match(r'\s*public static Holder<Attribute> (\w+) = ', l)
    if mm:
        w12_names.append(mm.group(1))
# b3 段结束于 DEFLECTION_RATING 前
w12_names = [n for n in w12_names if n not in ("DEFLECTION_RATING",)]
print(f"\nwave12 placeholder block: {len(w12_names)} attrs")

print("\n=== Wave-12 占位属性引用明细（非 datagen 引用 = 有消费者） ===")
for n in w12_names:
    r = report.get(n, {"cats": {}, "detail": []})
    non_gen = [d for c, d in r["detail"] if c != "datagen"]
    flag = "DEAD" if not r["cats"] else ("GEN-ONLY" if not non_gen else "CONSUMED")
    print(f"[{flag:9}] {n:45} {r['cats']}")
    for c, d in r["detail"]:
        if c != "datagen":
            print(f"             {c}: {d}")

print("\n=== 非 Wave-12 属性中的零引用 / 仅数据写点属性 ===")
for n, r in report.items():
    if n in w12_names:
        continue
    non_gen = [d for c, d in r["detail"] if c != "datagen"]
    if not r["cats"] or not non_gen:
        print(f"[{'DEAD' if not r['cats'] else 'GEN-ONLY':9}] {n:45} {r['cats']}")
