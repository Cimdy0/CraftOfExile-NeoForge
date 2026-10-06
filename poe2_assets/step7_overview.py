# -*- coding: utf-8 -*-
"""Step 7 总览（修正版）：StatIds / StatGen / AttributeRegister 三层引用与 TODO 现状。"""
import re, os

ROOT = "src/main/java/com/cimdy/coe"
STATIDS = os.path.join(ROOT, "data/codec/support/stat/StatIds.java")
STATGEN = os.path.join(ROOT, "data/gen/stat/StatGen.java")
ATTRREG = os.path.join(ROOT, "data/AttributeRegister.java")


def java_files():
    out = []
    for dp, _, fs in os.walk("src"):
        for f in fs:
            if f.endswith(".java"):
                out.append(os.path.join(dp, f))
    return out


ALL_JAVA = java_files()


def collect_refs(pattern, exclude=()):
    refs = {}
    for p in ALL_JAVA:
        if any(e in p for e in exclude):
            continue
        t = open(p, encoding="utf-8", errors="ignore").read()
        for m in re.finditer(pattern, t):
            refs.setdefault(m.group(1), set()).add(p)
    return refs


# ── Layer 1: StatIds ──
sid_text = open(STATIDS, encoding="utf-8").read()
const_re = re.compile(r"//\s*(.*?)\n\s*public static final Identifier ([A-Z][A-Z0-9_]+) = id\(\"([^\"]+)\"\);")
consts = {}
for m in const_re.finditer(sid_text):
    consts[m.group(2)] = {"id": m.group(3), "line": sid_text[:m.start()].count("\n") + 1, "comment": m.group(1).strip()}

sid_self = set(re.findall(r"StatIds\.([A-Z][A-Z0-9_]+)", sid_text))
sid_refs = collect_refs(r"StatIds\.([A-Z][A-Z0-9_]+)", exclude=(STATIDS,))
for n in sid_self:
    sid_refs.setdefault(n, set()).add(STATIDS)

total = len(consts)
referenced = {n for n in consts if n in sid_refs}
dead = sorted(set(consts) - referenced)
x_total = [n for n in consts if n.startswith("X_")]
x_ref = [n for n in x_total if n in referenced]
x_dead = [n for n in x_total if n not in referenced]
todo_consts = {n: c for n, c in consts.items() if "TODO" in c["comment"].upper()}
gen_refs = set(re.findall(r"StatIds\.([A-Z][A-Z0-9_]+)", open(STATGEN, encoding="utf-8").read()))
todo_but_impl = sorted(set(todo_consts) & gen_refs)
todo_not_impl = sorted(set(todo_consts) - gen_refs)

# 死常量是否在非 java 资源（json/txt）中以 id path 被引用
res_hits = {}
for dp, _, fs in os.walk("."):
    if "/build/" in dp or "\\build\\" in dp:
        continue
    for f in fs:
        if f.endswith((".json", ".txt", ".snbt", ".mcmeta")):
            p = os.path.join(dp, f)
            try:
                t = open(p, encoding="utf-8", errors="ignore").read()
            except Exception:
                continue
            for n in dead:
                if '"%s"' % consts[n]["id"] in t or "'%s'" % consts[n]["id"] in t:
                    res_hits.setdefault(n, []).append(p)
res_referenced = {n for n in dead if n in res_hits}

# ── Layer 3: AttributeRegister ──
attr_text = open(ATTRREG, encoding="utf-8").read()
attr_re = re.compile(r"public static (?:final )?Holder<Attribute>\s+([A-Z][A-Z0-9_]+)\s*=")
attrs = attr_re.findall(attr_text)
attr_dups = sorted({a for a in attrs if attrs.count(a) > 1})
attr_set = set(attrs)
attr_refs = collect_refs(r"AttributeRegister\.([A-Z][A-Z0-9_]+)", exclude=(ATTRREG,))
referenced_attrs = {a for a in attr_set if a in attr_refs}
dead_attrs = sorted(attr_set - referenced_attrs)
placeholder_attrs = [a for a in attr_set if re.search(r"_\d+$", a)]

# ── 输出 ──
print("=" * 60); print("StatIds 层"); print("=" * 60)
print("常量总数            : %d" % total)
print("被引用（全仓.java）  : %d" % len(referenced))
print("死常量（零引用）    : %d  (其中资源引用 %d)" % (len(dead), len(res_referenced)))
print("  ├ X_ 前缀死常量   : %d" % len(x_dead))
print("  └ 非X_ 死常量     : %d" % (len(dead) - len(x_dead) - len(res_referenced)))
print("X_ 常量总数         : %d (被引用 %d / 死 %d)" % (len(x_total), len(x_ref), len(x_dead)))
print("带 TODO 注释常量    : %d  (TODO但已实现 %d / TODO且未实现 %d)" % (len(todo_consts), len(todo_but_impl), len(todo_not_impl)))

print(""); print("=" * 60); print("StatGen 层"); print("=" * 60)
print("引用常量去重数      : %d" % len(gen_refs))

print(""); print("=" * 60); print("AttributeRegister 层"); print("=" * 60)
print("属性声明总数        : %d" % len(attr_set))
print("重复声明（同名）    : %d  %s" % (len(attr_dups), attr_dups[:10]))
print("被引用（全仓.java）  : %d" % len(referenced_attrs))
print("死属性（零引用）    : %d" % len(dead_attrs))
print("编号/占位属性(_N)   : %d" % len(placeholder_attrs))

os.makedirs("poe2_assets/_test", exist_ok=True)
with open("poe2_assets/_test/step7_overview.md", "w", encoding="utf-8") as f:
    f.write("# Step 7 总览 — StatIds / StatGen / AttributeRegister 三层现状（修正版）\n\n")
    f.write("## 1. StatIds 层\n| 指标 | 数量 |\n| --- | --- |\n")
    f.write("| 常量总数 | %d |\n" % total)
    f.write("| 被引用（全仓 .java） | %d |\n" % len(referenced))
    f.write("| 死常量（零引用，含资源引用 %d） | %d |\n" % (len(res_referenced), len(dead)))
    f.write("| 　├ X_ 前缀死常量 | %d |\n" % len(x_dead))
    f.write("| 　└ 非 X_ 死常量 | %d |\n" % (len(dead) - len(x_dead) - len(res_referenced)))
    f.write("| X_ 常量总数（被引用/死） | %d (%d/%d) |\n" % (len(x_total), len(x_ref), len(x_dead)))
    f.write("| 带 TODO 注释常量（已实现/未实现） | %d (%d/%d) |\n" % (len(todo_consts), len(todo_but_impl), len(todo_not_impl)))
    f.write("\n## 2. StatGen 层\n| 指标 | 数量 |\n| --- | --- |\n| 引用常量去重数 | %d |\n" % len(gen_refs))
    f.write("\n## 3. AttributeRegister 层\n| 指标 | 数量 |\n| --- | --- |\n")
    f.write("| 属性声明总数 | %d |\n" % len(attr_set))
    f.write("| 重复声明（同名） | %d %s |\n" % (len(attr_dups), attr_dups[:10]))
    f.write("| 被引用（全仓 .java） | %d |\n" % len(referenced_attrs))
    f.write("| 死属性（零引用） | %d |\n" % len(dead_attrs))
    f.write("| 编号/占位属性(_N) | %d |\n\n" % len(placeholder_attrs))
    f.write("## 4. TODO 但已实现（应去 TODO，共 %d）样例\n" % len(todo_but_impl))
    for n in todo_but_impl[:50]:
        f.write("- `%s`  L%d  // %s\n" % (n, consts[n]["line"], consts[n]["comment"][:60]))
    f.write("\n## 5. 死常量（零引用，共 %d）样例（前 80）\n" % len(dead))
    for n in dead[:80]:
        f.write("- `%s`  // %s\n" % (n, consts[n]["comment"][:60]))
    f.write("\n## 6. 死属性（零引用，共 %d）样例（前 80）\n" % len(dead_attrs))
    for n in dead_attrs[:80]:
        f.write("- `%s`\n" % n)
    if len(dead_attrs) > 80:
        f.write("- ... 共 %d\n" % len(dead_attrs))
    if attr_dups:
        f.write("\n## 7. 重复属性声明（同名，共 %d）\n" % len(attr_dups))
        for n in attr_dups:
            f.write("- `%s`\n" % n)

print("\n报告已写 poe2_assets/_test/step7_overview.md")
