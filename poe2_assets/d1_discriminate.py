# -*- coding: utf-8 -*-
"""D1 判别：把 46 条 D1 旧常量分成「真实实现」与「机械占位实现」两类。

背景
  第 3 步的换行 bug 曾把「一个多行 stat 块」误当成「一条 stat」。旧生成器据此
  一次性造出：① 一个按合并文本命名的 StatIds 常量 ② 一个同名的占位 Attribute
  ③ 一行 inc/more/chance(map, 常量, 占位属性) 注册。这三者都是 bug 的产物，
  引擎里没有任何代码读取该占位属性。

判据（机械、可复算，无相似度）
  取该旧常量 StatGen 实现体中引用的 AttributeRegister.X / Attributes.X 符号，
  统计 X 在 src/main/java 中「AttributeRegister.java 声明」与「StatGen.java 注册」
  **之外**的引用数 = engine_reads。
    engine_reads == 0  且 实现为单行 helper 调用  -> DEAD 占位（删除）
    否则                                          -> REAL 真实实现（须挂到新常量）
  实现体使用专门 Effect 类（ImmuneStatEffect / DamageConvertStatEffect /
  LuckyStatEffect / ResourceConvertStatEffect / RecoupStatEffect /
  ChanceOnEventStatEffect / ChargeConvertStatEffect 等）者直接判 REAL。
"""
import json, re, importlib.util, os
from collections import defaultdict

_m = importlib.util.spec_from_file_location("m", "poe2_assets/map_old_stats.py")
m = importlib.util.module_from_spec(_m); _m.loader.exec_module(m)
_et = importlib.util.spec_from_file_location("et", "poe2_assets/extract_templates.py")
et = importlib.util.module_from_spec(_et); _et.loader.exec_module(et)

OLD = "poe2_assets/_backup/stat/StatIds.java.old"
NEW = "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"
STATGEN = "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java"
ATTRREG = "src/main/java/com/cimdy/coe/data/AttributeRegister.java"
OUT = "poe2_assets/_test/d1_discriminate.md"

CONSUMERS = [
    "src/main/java/com/cimdy/coe/data/codec/item/text/AdditionType.java",
    "src/main/java/com/cimdy/coe/data/codec/support/status/StatusBehavior.java",
    "src/main/java/com/cimdy/coe/data/codec/support/status/registry/buff/BuffStatusTypes.java",
    "src/main/java/com/cimdy/coe/data/codec/support/status/registry/charm/CharmStatusTypes.java",
    "src/main/java/com/cimdy/coe/data/codec/support/status/registry/form/FormStatusTypes.java",
    "src/main/java/com/cimdy/coe/data/gen/gem/skill/SkillFactory.java",
]
REF = re.compile(r"StatIds\.([A-Z][A-Z0-9_]*)")
non_statgen = set()
for p in CONSUMERS:
    non_statgen |= set(REF.findall(open(p, encoding="utf-8").read()))

# ---- 全项目 java 源码索引 ----
sources = {}
for root, _, files in os.walk("src/main/java"):
    for fn in files:
        if fn.endswith(".java"):
            p = os.path.join(root, fn).replace("\\", "/")
            sources[p] = open(p, encoding="utf-8").read()

EFFECT_CLASSES = re.compile(
    r"\b(ImmuneStatEffect|DamageConvertStatEffect|LuckyStatEffect|ResourceConvertStatEffect|"
    r"RecoupStatEffect|ChanceOnEventStatEffect|ChargeConvertStatEffect|ConditionStatEffect|"
    r"TriggerStatEffect|Conversions|MathParser)\b")

old = m.parse(OLD)
new = m.parse(NEW)
new_by_text = defaultdict(list)
for c, e, cm in new:
    new_by_text[cm].append(c)
new_text = set(new_by_text)

clean = json.load(open("poe2_assets/poe2_tree_clean.json", encoding="utf-8"))
joined2pieces = {}
for nid, n in clean["nodes"].items():
    for stat in (n.get("stats") or []):
        lines = [l.strip() for l in stat.split("\n") if l.strip()]
        if len(lines) > 1:
            pieces = [m.normalize(p) for p in et.split_stat(stat)]
            if len(pieces) > 1:
                joined2pieces.setdefault(m.normalize(" ".join(lines)), pieces)

d1 = []
for const, eid, comment in old:
    key = m.normalize(comment) if comment else None
    if key and key in new_text:
        continue
    if const in non_statgen:
        continue
    if key and key in joined2pieces:
        d1.append((const, eid, key))

sg = sources[STATGEN].split("\n")


def impl_of(const):
    pat = re.compile(r"(?:map\.put\(|\b\w+\(map,\s*)StatIds\." + const + r"\b")
    for i, line in enumerate(sg):
        if pat.search(line):
            j = i - 1
            while j >= 0 and sg[j].strip().startswith("//"):
                j -= 1
            head = j + 1
            k, depth = i, 0
            while k < len(sg):
                depth += sg[k].count("(") - sg[k].count(")")
                if depth <= 0 and sg[k].rstrip().endswith(";"):
                    break
                k += 1
            # 语句行（去注释）用于形态判定
            stmt = "\n".join(sg[i:k + 1])
            return head + 1, "\n".join(sg[head:k + 1]), stmt
    return None, None, None


ATTR = re.compile(r"\b(?:AttributeRegister|Attributes)\.([A-Z][A-Z0-9_]*)\b")


def engine_reads(sym):
    """该属性符号在「AttributeRegister 声明」与「StatGen 注册」之外的引用数。"""
    n = 0
    pat = re.compile(r"\b" + sym + r"\b")
    for p, txt in sources.items():
        if p.endswith("AttributeRegister.java") or p.endswith("StatGen.java"):
            continue
        n += len(pat.findall(txt))
    # 属性 id 字符串形式（registerPercentage("totem_limit_is_doubled", ...)）
    return n


REAL, DEAD = [], []
rows = []
for const, eid, key in d1:
    ln, body, stmt = impl_of(const)
    attrs = ATTR.findall(stmt or "")
    has_effect = bool(EFFECT_CLASSES.search(stmt or ""))
    is_oneliner = (stmt or "").count("\n") == 0 and not (stmt or "").strip().startswith("map.put")
    reads = {a: engine_reads(a) for a in attrs}
    total = sum(reads.values())
    if has_effect or not is_oneliner or total > 0:
        verdict = "REAL"
        REAL.append((const, eid, key, ln, body))
    else:
        verdict = "DEAD"
        DEAD.append((const, eid, key, ln, body, attrs))
    rows.append((const, key, verdict, ln, ",".join(f"{a}={v}" for a, v in reads.items()) or "-",
                 "Effect类" if has_effect else ("单行helper" if is_oneliner else "map.put块")))

with open(OUT, "w", encoding="utf-8") as f:
    w = f.write
    w("# D1 判别：真实实现 vs 机械占位实现\n\n")
    w(f"D1 共 **{len(d1)}** 条。判别结果：REAL **{len(REAL)}** / DEAD **{len(DEAD)}**\n\n")
    w("`engine_reads` = 该属性在 AttributeRegister 声明与 StatGen 注册之外的引用数。"
      "为 0 表示引擎从不读取，属 bug 期自动生成的占位属性。\n\n")
    w("| 旧常量 | 判定 | 实现形态 | 属性 engine_reads | StatGen 行 |\n|---|---|---|---|---|\n")
    for const, key, verdict, ln, reads, form in sorted(rows, key=lambda r: (r[2], r[0])):
        w(f"| `{const}` | **{verdict}** | {form} | {reads} | {ln} |\n")

    w("\n---\n\n## DEAD（机械占位，连同占位属性一并删除）\n\n")
    for const, eid, key, ln, body, attrs in sorted(DEAD):
        w(f"- `{const}` — 占位属性 {', '.join(attrs)}  (StatGen:{ln})\n")

    w("\n---\n\n## REAL（真实实现，须挂到新常量）\n\n")
    for const, eid, key, ln, body in sorted(REAL):
        w(f"**`{const}`**  (StatGen:{ln})\n\n合并原文：`{key}`\n\n```java\n{body}\n```\n\n")

with open("poe2_assets/_test/d1_dead.txt", "w", encoding="utf-8") as f:
    for const, eid, key, ln, body, attrs in sorted(DEAD):
        f.write(const + "\n")
with open("poe2_assets/_test/d1_dead_attrs.txt", "w", encoding="utf-8") as f:
    seen = set()
    for const, eid, key, ln, body, attrs in sorted(DEAD):
        for a in attrs:
            if a not in seen:
                seen.add(a)
                f.write(a + "\n")
with open("poe2_assets/_test/d1_real.txt", "w", encoding="utf-8") as f:
    for const, eid, key, ln, body in sorted(REAL):
        f.write(const + "\n")

print(f"D1 {len(d1)} -> REAL {len(REAL)} / DEAD {len(DEAD)}")
print(f"DEAD 占位属性 {len(set(a for *_, attrs in DEAD for a in attrs))} 个")
print(f"报告: {OUT}")
