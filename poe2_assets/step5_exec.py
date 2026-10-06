# -*- coding: utf-8 -*-
"""第 5 步执行器：把「旧 1460 条 StatIds」与「新 1166 条 POE2 语料词表」对账并落地重构。

用户裁决（本轮据此实现）
  q-0  旧的移除，以新的为准                  -> 旧常量不再作为天赋树语料键；仅引擎自有条目保留
  q-1  不派生多余 Stat，扩建原 Effect 支持多项 -> 见 effect 层改造（本脚本不涉及）
  q-2  D1 误判按人工分析修正                  -> FORCE_KEEP / FORCE_DROP

判据（全确定性，无相似度）
  KEEP  被 StatGen 以外的消费文件引用（词缀/状态/技能子系统真实依赖）
  PARK  D1-REAL：手写关键石/引擎实现，注释被多行块合并污染，无法映射到单条语料
  A     注释规范化后与新表逐字符相等，**且该注释在旧表全库唯一、且该新常量只被它命中**
        -> 改名为新常量（把旧实现交给新语料条目）
  DROP  其余（语料已剔除 / 引擎占位重复 / 完全无引用）

为何 A 必须要求「1:1 且注释全库唯一」——旧注释已证实大面积损坏（审计：313 个重复注释串
覆盖 721/1460 条旧常量；231 个碰撞组中 181 组实现目标属性互相分歧）。典型反例：
  `ADD_ATTACK_DAMAGE`（通用攻击伤害词缀模板）的注释是 "Adds 1 to 37 Lightning damage to Attacks"。
凡注释非唯一，任何取舍都是猜测，一律不改名。

冲突组（同一注释/同一新常量被多个旧常量命中）按实现真实性分流；真实性 rank 的证据来自
AttributeRegister 的两个占位批次标记：
  L280 `批量落地（Wave-12 数据先行：551 剩余描述占位属性）`
  L716 `b3 追加（Wave-12）`
  之后声明的属性均为「数据先行占位」，其中 `_N` 数字后缀者是 b3 波次对同一 stat
  的**二次重复占位**。rank 越小越真：
    0  用具体 StatEffect 类，或目标属性为 vanilla Attributes.* / 占位标记之前的真实属性
    1  目标属性为占位属性（无 _N 后缀）
    2  目标属性为占位属性（带 _N 后缀）= b3 重复占位
    3  无 StatGen 实现
  rank=0 -> PARK（真实实现，仅注释被污染，进手工通道待第 6 步按语义重接）
  rank>=1 -> DROP（数据先行占位，无引擎读取，第 6 步可按新语料表精确重建）

用法
  python poe2_assets/step5_exec.py            # 仅分析，出报告
  python poe2_assets/step5_exec.py --apply     # 落地改写源码
"""
import importlib.util
import re
import sys
from collections import defaultdict

_m = importlib.util.spec_from_file_location("m", "poe2_assets/map_old_stats.py")
m = importlib.util.module_from_spec(_m)
_m.loader.exec_module(m)

OLD = "poe2_assets/_backup/stat/StatIds.java.old"
NEW = "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"
STATGEN = "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java"
ATTRREG = "src/main/java/com/cimdy/coe/data/AttributeRegister.java"

CONSUMERS = {
    "AdditionType": "src/main/java/com/cimdy/coe/data/codec/item/text/AdditionType.java",
    "StatusBehavior": "src/main/java/com/cimdy/coe/data/codec/support/status/StatusBehavior.java",
    "BuffStatusTypes": "src/main/java/com/cimdy/coe/data/codec/support/status/registry/buff/BuffStatusTypes.java",
    "CharmStatusTypes": "src/main/java/com/cimdy/coe/data/codec/support/status/registry/charm/CharmStatusTypes.java",
    "FormStatusTypes": "src/main/java/com/cimdy/coe/data/codec/support/status/registry/form/FormStatusTypes.java",
    "SkillFactory": "src/main/java/com/cimdy/coe/data/gen/gem/skill/SkillFactory.java",
    "StatGen": STATGEN,
}

# ---- q-2 人工修正 ----
# ADD_MAX_TOTEM：属通用 ADD_MAX_* limit 族（MAX_TOTEM 是真实属性），仅注释被多行块污染 -> 保留
FORCE_KEEP = {"ADD_MAX_TOTEM"}
# 语义错接且与手写关键石实现重复者 -> 删除
#   INC_ACCURACY_RATING_IS_DOUBLED(_CRIT): inc(精准%) != 「精准翻倍」，与 RESOLUTE_TECHNIQUE_ACCURACY 重复
#   FIRE_SPELLS_CONVERT_DAMAGE(_SPELL)   : inc(ADDED_DAMAGE,IGNITE) 与「火法术转混沌」无关，
#                                          与 BLACKFLAME_COVENANT_FIRE_TO_CHAOS 重复
FORCE_DROP = {
    "INC_ACCURACY_RATING_IS_DOUBLED", "INC_ACCURACY_RATING_IS_DOUBLED_CRIT",
    "FIRE_SPELLS_CONVERT_DAMAGE", "FIRE_SPELLS_CONVERT_DAMAGE_SPELL",
}

REF = re.compile(r"StatIds\.([A-Z][A-Z0-9_]*)")
ATTR_DECL = re.compile(r"public static Holder<Attribute> ([A-Z][A-Z0-9_]*) = register")
ATTR_REF = re.compile(r"(?:AttributeRegister|Attributes)\.([A-Z][A-Z0-9_]*)")
EFFECT_CLS = re.compile(r"\b([A-Z][A-Za-z]*StatEffect)\b")
PLACEHOLDER_MARK = "批量落地（Wave-12 数据先行"


# ══════════════════════ 解析 ══════════════════════
def read(p):
    return open(p, encoding="utf-8").read()


def refs_of(path):
    return set(REF.findall(read(path)))


def parse_attr_register():
    """-> (name -> (line, is_placeholder))"""
    lines = read(ATTRREG).splitlines()
    mark = next((i for i, l in enumerate(lines) if PLACEHOLDER_MARK in l), len(lines))
    out = {}
    for i, l in enumerate(lines):
        mm = ATTR_DECL.search(l)
        if mm:
            out[mm.group(1)] = (i + 1, i > mark)
    return out, mark + 1


def split_statements(src):
    """把 java 源码切成语句（在 paren/brace 深度 0 处遇 ';' 断开），返回 [(start,end,text)] 字符偏移。
    跳过字符串/字符字面量与注释内的括号与分号。"""
    out = []
    depth = 0
    start = 0
    i = 0
    n = len(src)
    while i < n:
        c = src[i]
        if c == '"' or c == "'":
            q = c
            i += 1
            while i < n:
                if src[i] == "\\":
                    i += 2
                    continue
                if src[i] == q:
                    break
                i += 1
        elif c == "/" and i + 1 < n and src[i + 1] == "/":
            while i < n and src[i] != "\n":
                i += 1
            continue
        elif c == "/" and i + 1 < n and src[i + 1] == "*":
            j = src.find("*/", i + 2)
            i = (j + 2) if j >= 0 else n
            continue
        elif c in "([":
            depth += 1
        elif c in ")]":
            depth -= 1
        elif c in "{}" and depth == 0:
            out.append((start, i + 1, src[start:i + 1]))
            start = i + 1
        elif c == ";" and depth == 0:
            out.append((start, i + 1, src[start:i + 1]))
            start = i + 1
        i += 1
    if start < n:
        out.append((start, n, src[start:n]))
    return out


def parse_statgen():
    """-> const -> dict(span=(s,e), text, consts=set, helper, attrs=set, effects=set)"""
    src = read(STATGEN)
    stmts = split_statements(src)
    by_const = {}
    for s, e, t in stmts:
        cs = set(REF.findall(t))
        if not cs:
            continue
        body = t
        helper = None
        hm = re.search(r"(\w+)\s*\(\s*map\s*,", body)
        if hm:
            helper = hm.group(1)
        elif "map.put" in body:
            helper = "map.put"
        info = dict(span=(s, e), text=t, consts=cs, helper=helper,
                    attrs=set(ATTR_REF.findall(body)),
                    effects=set(EFFECT_CLS.findall(body)))
        for c in cs:
            by_const.setdefault(c, info)
    return src, stmts, by_const


old = m.parse(OLD)
new = m.parse(NEW)
new_by_text = {}
for c, e, cm in new:
    new_by_text.setdefault(cm, c)

attr_info, attr_mark_line = parse_attr_register()
sg_src, sg_stmts, sg_by_const = parse_statgen()
by_file = {k: refs_of(v) for k, v in CONSUMERS.items()}
non_statgen = set().union(*(v for k, v in by_file.items() if k != "StatGen"))

d1_real = set(open("poe2_assets/_test/d1_real.txt", encoding="utf-8").read().split())
d1_dead = set(open("poe2_assets/_test/d1_dead.txt", encoding="utf-8").read().split())
d1_real -= FORCE_DROP
d1_dead -= FORCE_KEEP
d1_dead |= (FORCE_DROP & (d1_real | d1_dead | set()))
d1_dead |= FORCE_DROP


# ══════════════════════ rank ══════════════════════
def rank_of(const):
    info = sg_by_const.get(const)
    if not info:
        return 3, "无 StatGen 实现"
    if info["effects"] - {"ModifierStatEffect"}:
        return 0, "具体效果类 " + ",".join(sorted(info["effects"]))
    attrs = info["attrs"]
    if not attrs:
        return 0, "无属性符号（helper=%s）" % info["helper"]
    best, why = 2, ""
    for a in sorted(attrs):
        if a not in attr_info:
            r, w = 0, "vanilla Attributes.%s" % a
        else:
            line, ph = attr_info[a]
            if not ph:
                r, w = 0, "真实属性 %s(L%d)" % (a, line)
            elif re.search(r"_\d+$", a):
                r, w = 2, "b3 重复占位 %s(L%d)" % (a, line)
            else:
                r, w = 1, "占位属性 %s(L%d)" % (a, line)
        if r < best:
            best, why = r, w
    return best, why


def toks(s):
    return set(t for t in s.split("_") if t)


# ══════════════════════ 归因 ══════════════════════
# 旧注释在全库出现次数（用于「注释全库唯一」判据）
key_count = defaultdict(int)
for _c, _e, _cm in old:
    if _cm:
        key_count[m.normalize(_cm)] += 1

KEEP, PARK, A_cand, DROP = [], [], defaultdict(list), []
for const, eid, comment in old:
    if const in FORCE_DROP:
        DROP.append((const, eid, comment, "q-2 人工修正：语义错接且与手写实现重复"))
        continue
    if const in non_statgen or const in FORCE_KEEP:
        KEEP.append((const, eid, comment,
                     sorted(k for k, v in by_file.items() if const in v and k != "StatGen")))
        continue
    if const in d1_real:
        PARK.append((const, eid, "D1-REAL 手写实现：" + (comment or "")))
        continue
    if const in d1_dead:
        DROP.append((const, eid, comment, "D1-DEAD：占位实现，注释为多行块合并污染形"))
        continue
    key = m.normalize(comment) if comment else None
    if key and key in new_by_text:
        A_cand[new_by_text[key]].append((const, eid, key))
        continue
    DROP.append((const, eid, comment, "注释不匹配新语料表，且无 StatGen 以外引用"))

# 冲突裁决：只接受 1:1 且注释全库唯一；其余按 rank 分流（rank0 -> PARK，rank>=1 -> DROP）
A, AMB = [], []
for newc, cands in sorted(A_cand.items()):
    unique = len(cands) == 1 and key_count[cands[0][2]] == 1
    if unique:
        const, eid, key = cands[0]
        r, why = rank_of(const)
        A.append((const, eid, newc, why))
        continue
    reason = "冲突组（%d 条旧常量命中 %s）" % (len(cands), newc) if len(cands) > 1 \
        else "注释非全库唯一（该注释串被 %d 条旧常量共用）" % key_count[cands[0][2]]
    for const, eid, key in cands:
        r, why = rank_of(const)
        AMB.append((const, eid, newc, r, why, reason))

for const, eid, newc, r, why, reason in AMB:
    if r == 0:
        PARK.append((const, eid, "%s ／真实实现：%s ／%s" % (newc, why, reason)))
    else:
        DROP.append((const, eid, "", "%s；占位实现（%s）" % (reason, why)))

assert len(KEEP) + len(PARK) + len(A) + len(DROP) == len(old), \
    "收敛账不闭合 %d+%d+%d+%d != %d" % (len(KEEP), len(PARK), len(A), len(DROP), len(old))

keep_names = {c for c, *_ in KEEP}
park_names = {c for c, *_ in PARK}
rename = {c: n for c, _, n, _ in A if c != n}
drop_names = {c for c, *_ in DROP}
survivors = keep_names | park_names | {n for _, _, n, _ in A}
orphan_new = [c for c, e, cm in new if c not in {n for _, _, n, _ in A}]

# ══════════════════════ 删除安全性检查 ══════════════════════
# (1) 混合语句：同一 StatGen 语句同时引用 DROP 与存活常量 -> 不能整句删，必须人工处理
mixed = []
for s, e, t in sg_stmts:
    cs = set(REF.findall(t))
    if cs & drop_names and cs - drop_names:
        mixed.append((sorted(cs & drop_names), sorted(cs - drop_names)))

# (2) 删语句后是否仍残留对 DROP 常量的引用（应为 0）
del_covered = set()
for s, e, t in sg_stmts:
    cs = set(REF.findall(t))
    if cs and cs <= drop_names:
        del_covered |= cs
drop_residual = sorted((drop_names & set(REF.findall(read(STATGEN)))) - del_covered)

# ══════════════════════ 占位属性回收（仅报告；默认不删）══════════════════════
# 占位属性名编码了原始描述语义（如 BREAK_ARMOUR_HEAVY_STUNNING），第 6 步按新语料表
# 重建实现时可直接复用，故本轮只清点、不删除；需删除时加 --reap-attrs。
import glob as _glob

ALL_JAVA = [p for p in _glob.glob("src/main/java/**/*.java", recursive=True)]
attr_refs_outside = defaultdict(int)
for p in ALL_JAVA:
    if p.replace("\\", "/") in (ATTRREG, STATGEN):
        continue
    for a in ATTR_REF.findall(read(p)):
        attr_refs_outside[a] += 1

attr_users = defaultdict(set)
for c, info in sg_by_const.items():
    for a in info["attrs"]:
        attr_users[a].add(c)

dead_attrs = []
for a, (line, ph) in sorted(attr_info.items()):
    if not ph:
        continue
    if attr_refs_outside.get(a, 0):
        continue
    users = attr_users.get(a, set())
    if users - drop_names:
        continue
    dead_attrs.append((a, line, sorted(users)))


# ══════════════════════ 报告 ══════════════════════
def w_report():
    L = []
    W = L.append
    W("# 第 5 步 · 引用重构执行账（含冲突裁决）\n")
    W("- 旧 StatIds：**%d** / 新 StatIds：**%d**" % (len(old), len(new)))
    W("- 占位属性批次标记行：`AttributeRegister.java:%d`；其后声明的属性共 **%d** 条为数据先行占位"
      % (attr_mark_line, sum(1 for _, (l, ph) in attr_info.items() if ph)))
    W("")
    W("| 处置 | 数量 | 动作 |")
    W("|---|---|---|")
    W("| KEEP 引擎自有 | %d | 迁入 StatIds 手工通道（名/id/注释原样） |" % len(KEEP))
    W("| PARK 保留待接线 | %d | 迁入手工通道并标 TODO（真实实现，注释被污染无法定位语料） |" % len(PARK))
    W("| A 改名 | %d | StatGen 内改名为新语料常量（1:1 且注释全库唯一） |" % len(A))
    W("| DROP 删除 | %d | 删旧常量 + 删 StatGen 实现 |" % len(DROP))
    W("| **合计** | **%d** | |" % (len(KEEP) + len(PARK) + len(A) + len(DROP)))
    W("")
    amb_groups = sum(1 for v in A_cand.values() if len(v) > 1)
    W("- 注释命中新表的旧常量共 **%d** 条，其中 1:1 且注释全库唯一 **%d** 条（→A），"
      "歧义 **%d** 条（%d 个冲突组 + 注释非唯一），按 rank 分流为 PARK/DROP"
      % (sum(len(v) for v in A_cand.values()), len(A), len(AMB), amb_groups))
    W("- 新表仍待实现（无任何旧实现可继承）：**%d** / %d" % (len(orphan_new), len(new)))
    W("- 可回收的占位属性（仅被 DROP 常量使用）：**%d**（本轮仅清点，不删除）" % len(dead_attrs))
    W("")
    W("### 删除安全性检查\n")
    W("| 检查 | 结果 | 说明 |")
    W("|---|---|---|")
    W("| 混合语句（同句引用 DROP + 存活常量） | %d | 非 0 则该语句不可整句删，需人工拆分 |" % len(mixed))
    W("| 删语句后残留 DROP 引用 | %d | 非 0 则 StatGen 会出现未定义符号 |" % len(drop_residual))
    if mixed:
        W("")
        for d, s in mixed[:40]:
            W("- 混合：DROP=`%s` / 存活=`%s`" % (",".join(d), ",".join(s)))
    if drop_residual:
        W("")
        W("- 残留：`%s`" % ", ".join(drop_residual[:60]))
    W("")
    by_reason = defaultdict(int)
    for *_, r in DROP:
        by_reason[r.split("（")[0].split("；")[0].split("：")[0]] += 1
    W("### DROP 归因\n")
    W("| 原因 | 数量 |")
    W("|---|---|")
    for k, v in sorted(by_reason.items(), key=lambda x: -x[1]):
        W("| %s | %d |" % (k, v))
    W("")
    W("---\n\n## PARK 明细（真实实现，进手工通道待第 6 步按语义重接）\n")
    W("| 旧常量 | 旧 id | 说明 |")
    W("|---|---|---|")
    for c, e, note in sorted(PARK):
        W("| `%s` | `%s` | %s |" % (c, e, note))
    W("")
    W("---\n\n## 歧义组逐组分流（不改名）\n")
    seen = set()
    for newc, cands in sorted(A_cand.items()):
        if len(cands) == 1 and key_count[cands[0][2]] == 1:
            continue
        W("### `%s`\n" % newc)
        for const, eid, key in cands:
            r, why = rank_of(const)
            W("- %s rank=%d `%s` — %s" % ("PARK" if r == 0 else "DROP", r, const, why))
        W("")
    return "\n".join(L)


open("poe2_assets/_test/step5_exec.md", "w", encoding="utf-8").write(w_report())
with open("poe2_assets/_test/rename_final.tsv", "w", encoding="utf-8") as f:
    for a, b in sorted(rename.items()):
        f.write("%s\t%s\n" % (a, b))
with open("poe2_assets/_test/keep_final.txt", "w", encoding="utf-8") as f:
    f.write("\n".join(sorted(keep_names)) + "\n")
with open("poe2_assets/_test/park_final.txt", "w", encoding="utf-8") as f:
    f.write("\n".join(sorted(park_names)) + "\n")
with open("poe2_assets/_test/drop_final.txt", "w", encoding="utf-8") as f:
    f.write("\n".join(sorted(drop_names)) + "\n")
with open("poe2_assets/_test/dead_attrs_final.txt", "w", encoding="utf-8") as f:
    for a, line, users in dead_attrs:
        f.write("%s\tL%d\t%s\n" % (a, line, ",".join(users)))

print("旧 %d -> KEEP %d / PARK %d / A %d(实改 %d) / DROP %d  收敛 %d"
      % (len(old), len(KEEP), len(PARK), len(A), len(rename), len(DROP),
         len(KEEP) + len(PARK) + len(A) + len(DROP)))
print("歧义 %d 条（%d 个冲突组）→ PARK %d / DROP %d"
      % (len(AMB), sum(1 for v in A_cand.values() if len(v) > 1),
         sum(1 for *_x, r, _w, _rs in [(a[0], a[1], a[2], a[3], a[4], a[5]) for a in AMB] if r == 0),
         sum(1 for a in AMB if a[3] != 0)))
print("新表待实现 %d / %d；可回收占位属性 %d（仅清点）" % (len(orphan_new), len(new), len(dead_attrs)))
print("安全检查：混合语句 %d / 删后残留 DROP 引用 %d" % (len(mixed), len(drop_residual)))
print("报告 -> poe2_assets/_test/step5_exec.md")

if "--apply" not in sys.argv:
    sys.exit(0)

# ══════════════════════ 落地 ══════════════════════
import shutil, os

os.makedirs("poe2_assets/_backup/step5", exist_ok=True)
for p in [NEW, STATGEN, ATTRREG]:
    shutil.copy2(p, "poe2_assets/_backup/step5/" + os.path.basename(p))

# ── 1) StatGen：删 DROP 语句 + 改名 A 类 ──
src = sg_src
dels = []
for s, e, t in sg_stmts:
    cs = set(REF.findall(t))
    if cs and cs <= drop_names:
        dels.append((s, e))
for s, e in reversed(dels):
    # 连带吞掉紧邻其前的注释行
    head = src.rfind("\n", 0, s)
    src = src[:s] + src[e:]
for a, b in sorted(rename.items(), key=lambda x: -len(x[0])):
    src = re.sub(r"StatIds\.%s\b" % a, "StatIds." + b, src)
open(STATGEN, "w", encoding="utf-8").write(src)

# ── 2) StatIds：追加手工通道区块 ──
ids_src = read(NEW)
block = ["", "    // ═════════════════ 手工通道（引擎自有条目，非天赋树语料）═════════════════",
         "    // BEGIN MANUAL — 人工维护区，gen_stat_ids.py 重跑时原样保留。",
         "    // 这些条目不来自 poe2_tree.json 语料，而是词缀 / 状态 / 技能 / 关键石子系统的引擎构件；",
         "    // 名与 id 沿用重构前口径，避免 AdditionType 等消费方的语义命名漂移。", ""]
old_by_const = {c: (e, cm) for c, e, cm in old}
block.append("    // ---- 子系统引用（AdditionType / StatusBehavior / *StatusTypes / SkillFactory）----")
for c in sorted(keep_names):
    e, cm = old_by_const[c]
    if cm:
        block.append("    // %s" % cm)
    block.append('    public static final Identifier %s = id("%s");' % (c, e))
    block.append("")
block.append("    // ---- 手写关键石 / 引擎实现，待接线到 POE2 语料条目（TODO 第 6 步）----")
for c in sorted(park_names):
    e, cm = old_by_const[c]
    block.append("    // TODO(接线) %s" % (cm or ""))
    block.append('    public static final Identifier %s = id("%s");' % (c, e))
    block.append("")
block.append("    // END MANUAL")
tail = ids_src.rstrip()
assert tail.endswith("}")
cut = tail.rfind("\n", 0, tail.rfind("}"))
# 插入到最后一个方法 id(...) 之前：定位 static Identifier id(
anchor = ids_src.rfind("static Identifier id(")
assert anchor > 0, "未找到 id() 工厂方法锚点"
ids_src = ids_src[:anchor] + "\n".join(block) + "\n\n" + ids_src[anchor:]
open(NEW, "w", encoding="utf-8").write(ids_src)

# ── 3) AttributeRegister：占位属性回收（默认仅报告，须显式 --reap-attrs）──
# 占位属性名编码了原始描述语义，第 6 步按新语料重建实现时可直接复用，故默认保留。
reaped = 0
if "--reap-attrs" in sys.argv:
    ar_lines = read(ATTRREG).splitlines(keepends=True)
    dead_line_no = {line for _, line, _ in dead_attrs}
    ar_out = [l for i, l in enumerate(ar_lines, 1) if i not in dead_line_no]
    open(ATTRREG, "w", encoding="utf-8").write("".join(ar_out))
    reaped = len(dead_attrs)

print("已落地：StatGen 删 %d 语句 / 改名 %d；StatIds 追加手工通道 %d 条；AttributeRegister 删占位 %d（可回收 %d）"
      % (len(dels), len(rename), len(keep_names) + len(park_names), reaped, len(dead_attrs)))
