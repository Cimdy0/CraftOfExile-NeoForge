"""扫描「EFF 作用域标签零发射点」死线族（#36 系统性排查）。

判据：
  1. 从 StatEffectGen.java 抽出所有 TagExpr.of(Set.of(...)) 里用到的 StatTag.XXX
  2. 对每个标签，在除 StatTag.java / StatEffectGen.java / StatConditionGen.java 之外的
     Java 源里找「发射点」：
       - StatTag.XXX 字面引用（引擎侧 enter/emit）
       - 字符串字面 "xxx"（数据侧 labels 注入）
  3. 零命中 = 该标签作用域下所有 EFF 恒不匹配 = 整族死线
"""
import re
import os
import sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                    '..', 'src', 'main', 'java', 'com', 'cimdy', 'coe')


def read(p):
    with open(p, encoding='utf-8') as f:
        return f.read()


# 1) StatTag 常量名 -> 字符串值
tag_java = read(os.path.join(ROOT, 'data', 'codec', 'support', 'stat', 'StatTag.java'))
tag_val = {}
for m in re.finditer(r'public\s+static\s+final\s+String\s+([A-Z0-9_]+)\s*=\s*"([a-z0-9_.:]+)"', tag_java):
    tag_val[m.group(1)] = m.group(2)
print(f"[StatTag] constants = {len(tag_val)}")

# 2) EFF 里用到的标签
gen = read(os.path.join(ROOT, 'data', 'gen', 'stat', 'StatEffectGen.java'))
used = {}
for m in re.finditer(r'TagExpr\.of\(Set\.of\(([^)]*)\)\)', gen):
    for c in re.findall(r'StatTag\.([A-Z0-9_]+)', m.group(1)):
        used.setdefault(c, 0)
        used[c] += 1
print(f"[EFF] 用到的标签种类 = {len(used)}")

# 3) 全仓 Java 文本（排除标签定义与效果定义文件）
skip = {'data/codec/support/stat/StatTag.java',
        'data/gen/stat/StatEffectGen.java',
        'data/gen/stat/StatConditionGen.java'}
corpus = []
for dp, _, fns in os.walk(ROOT):
    for fn in fns:
        if not fn.endswith('.java'):
            continue
        p = os.path.join(dp, fn)
        rel = os.path.relpath(p, ROOT).replace('\\', '/')
        if rel in skip:
            continue
        corpus.append((rel, read(p)))

orphans = []
for c, n in sorted(used.items(), key=lambda kv: -kv[1]):
    val = tag_val.get(c)
    if val is None:
        continue          # not() / 复合表达式，跳过
    hits = []
    for rel, txt in corpus:
        if re.search(r'StatTag\.' + c + r'\b', txt):
            hits.append(rel)
        elif re.search(r'"' + re.escape(val) + r'"', txt):
            hits.append(rel)
    if not hits:
        orphans.append((c, val, n))

print(f"\n=== 零发射点标签（整族死线）: {len(orphans)} ===")
for c, val, n in orphans:
    print(f"  StatTag.{c:<22} \"{val}\"    被 {n} 个 EFF 引用")
