#!/usr/bin/env python3
"""按描述严格去重 StatIds/StatGen 条目。
- 抽取 StatIds.java 每个常量的「描述注释」+ json id
- 抽取 StatGen.java 每个 map.put(StatIds.X, ...) 的底层签名（effect 类 + 关键参数归一）
- 扫描 src/main/java 所有 StatIds.X 引用计数（外部消费者）
- 聚类：① 按描述归一 ② 按底层签名归一
"""
import os, re, json
from collections import defaultdict, Counter

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
SRC = os.path.join(ROOT, "src", "main", "java")
STATIDS = os.path.join(SRC, "com", "cimdy", "coe", "data", "codec", "support", "stat", "StatIds.java")
STATGEN = os.path.join(SRC, "com", "cimdy", "coe", "data", "gen", "stat", "StatGen.java")

# ---------- 1. 解析 StatIds.java ----------
ids = {}  # name -> {json, comment}
const_re = re.compile(r'public static final Identifier (\w+) = id\("([^"]+)"\);')
with open(STATIDS, encoding="utf-8") as f:
    lines = f.readlines()

pending = []
for ln in lines:
    s = ln.strip()
    if s.startswith("//"):
        pending.append(s[2:].strip())
    else:
        m = const_re.search(ln)
        if m:
            name, jid = m.group(1), m.group(2)
            # 描述取最近的非空注释块；若无则空
            comment = " / ".join(c for c in pending if c) if pending else ""
            ids[name] = {"json": jid, "comment": comment}
        # 任何非空非注释行都清空 pending（注释必须紧贴常量上方）
        if s:
            pending = []

# ---------- 2. 解析 StatGen.java 的 map.put(StatIds.X, ...) ----------
gen_sig = {}  # name -> signature string (归一)
put_re = re.compile(r'map\.put\(StatIds\.(\w+),')
with open(STATGEN, encoding="utf-8") as f:
    gtxt = f.read()

# 直接在整串上 finditer，用绝对偏移平衡括号
for m in put_re.finditer(gtxt):
    name = m.group(1)
    # value 起点 = 逗号之后
    comma = gtxt.find(",", m.end() - 1)
    if comma == -1:
        continue
    j = comma + 1
    while j < len(gtxt) and gtxt[j] in " ,\t\n":
        j += 1
    # 平衡括号
    depth = 0
    k = j
    in_str = None
    while k < len(gtxt):
        c = gtxt[k]
        if in_str:
            if c == in_str and gtxt[k-1] != "\\":
                in_str = None
            k += 1
            continue
        # 注意：此分支占位
        if c in "\"'":
            in_str = c
        elif c == "(":
            depth += 1
        elif c == ")":
            depth -= 1
            if depth == 0:
                k += 1
                break
        k += 1
    value = gtxt[j:k]
    sig = re.sub(r'\s+', ' ', value).strip()
    gen_sig[name] = sig

# ---------- 3. 全工程 StatIds.X 引用计数 ----------
ref_counter = Counter()
for dp, _, fns in os.walk(SRC):
    if ".bak" in dp or dp.endswith(".bak"):
        continue
    for fn in fns:
        if not fn.endswith(".java") or ".bak" in fn:
            continue
        p = os.path.join(dp, fn)
        with open(p, encoding="utf-8", errors="ignore") as f:
            txt = f.read()
        for m in re.findall(r'StatIds\.(\w+)', txt):
            ref_counter[m] += 1

def external_refs(name):
    total = ref_counter.get(name, 0)
    # 减去 StatIds.java 内的定义(1) 与 StatGen.java 内的注册(通常1)
    ext = total - 1
    if name in gen_sig:
        ext -= 1
    return max(ext, 0), total

# ---------- 4. 聚类 ----------
# 4a 按描述归一
def norm_comment(c):
    c = c.lower()
    c = re.sub(r'\s+', ' ', c).strip()
    c = re.sub(r'[。.．；;，,、]', '', c)  # 去标点
    c = re.sub(r'\s+', ' ', c).strip()
    return c

by_comment = defaultdict(list)
for name, info in ids.items():
    key = norm_comment(info["comment"]) if info["comment"] else "<<NO_COMMENT>>"
    by_comment[key].append(name)

# 4b 按底层签名归一（去 StatIds 引用，仅留 effect 结构）
def norm_sig(sig):
    # 保留 AttributeRegister.X / StatTag.X 名字（区分不同 stat 的关键），
    # 仅归一数字与 StatIds 引用，使「真正同效应」的条目聚类。
    s = re.sub(r'StatIds\.', '', sig)
    s = re.sub(r'\d+\.\d+|\d+', '#', s)
    s = re.sub(r'\s+', ' ', s).strip()
    return s

by_sig = defaultdict(list)
for name, sig in gen_sig.items():
    by_sig[norm_sig(sig)].append(name)

# ---------- 5. 输出 ----------
out = []
out.append("# StatIds 按描述去重扫描\n")
out.append("总常量数: %d | StatGen 已注册: %d | 孤儿(无StatGen): %d\n" % (
    len(ids), sum(1 for n in ids if n in gen_sig),
    sum(1 for n in ids if n not in gen_sig)))

# 描述聚类（>1 成员）
out.append("\n## A. 描述相同聚类（>1 成员）\n")
dup_comment = {k: v for k, v in by_comment.items() if len(v) > 1 and k != "<<NO_COMMENT>>"}
# 按成员数降序
for key, members in sorted(dup_comment.items(), key=lambda kv: -len(kv[1])):
    out.append("\n### 「%s」 ×%d\n" % (key, len(members)))
    for name in members:
        info = ids[name]
        sig = gen_sig.get(name, "<<无StatGen>>")
        ext, total = external_refs(name)
        out.append("  - `%s` (json=%s) refs=%d(ext=%d)\n    sig=%s" % (
            name, info["json"], total, ext, sig[:160]))
        if len(sig) > 160:
            out.append(" …")

out.append("\n## B. 无描述注释的常量（需补/查）: %d\n" % sum(1 for n in ids if not ids[n]["comment"]))

# 签名聚类（真·功能重复，含不同描述）
out.append("\n## C. 底层签名相同聚类（功能重复，>1 成员）\n")
dup_sig = {k: v for k, v in by_sig.items() if len(v) > 1}
for key, members in sorted(dup_sig.items(), key=lambda kv: -len(kv[1])):
    out.append("\n### sig=%s ×%d\n" % (key[:90], len(members)))
    for name in members:
        info = ids.get(name, {})
        ext, total = external_refs(name)
        out.append("  - `%s` json=%s refs=%d(ext=%d) comment=%r" % (
            name, info.get("json", "?"), total, ext, (info.get("comment", "")[:50])))

report = "".join(out)
with open(os.path.join(ROOT, "poe2_assets", "_test", "dedup_scan.md"), "w", encoding="utf-8") as f:
    f.write(report)

# 控制台摘要
print("常量=%d 注册=%d 孤儿=%d" % (len(ids), sum(1 for n in ids if n in gen_sig),
      sum(1 for n in ids if n not in gen_sig)))
print("描述聚类(>1):", len(dup_comment), " 签名聚类(>1):", len(dup_sig))
print("无注释常量:", sum(1 for n in ids if not ids[n]["comment"]))
print("=== 描述聚类 TOP ===")
for key, members in sorted(dup_comment.items(), key=lambda kv: -len(kv[1]))[:25]:
    print("  [%d] %s" % (len(members), key[:70]))
print("=== 签名聚类 TOP ===")
for key, members in sorted(dup_sig.items(), key=lambda kv: -len(kv[1]))[:25]:
    print("  [%d] %s" % (len(members), key[:70]))
