# -*- coding: utf-8 -*-
"""StatIds 语义重复判定（只读审计，不改码、不自动合并）。

三层判定：
  L1 DUP_ID     : 两个不同常量名指向同一 id("...") 串 -> 硬重复（确定 bug）
  L2 SET_DUP    : 词序无关归一（缩写展开 + 算子同义词合一）后 token 集合相同的多常量
                  -> 强候选（高概率同一语义，需人裁定是否合并到单一 name）
  L3 SUBSET     : 某常量归一 token 集合是另一常量的严格子集 -> 弱候选
                  （不一定是重复，可能 scope/修饰不同，需人看）

纪律：仅输出候选 + 证据（成员/注释/引用数），绝不臆造合并。
同义词映射严格区分算子维度：flat(+X to / add / gain) != inc(additive%) != more(multiplicative%)，
不得把 increased 与 more 归一为同一 token。
"""
import json
import re
from collections import defaultdict
from pathlib import Path

ROOT = Path("E:/McModding/Modding/CraftOfExile-NeoForge")
STATIDS = ROOT / "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"
SRC = ROOT / "src/main/java"
OUT = ROOT / "poe2_assets/_test/semantic_dup_audit.json"

DECL = re.compile(r'public static final Identifier (\w+) = id\("([^"]+)"\);')

# 缩写展开（纯词形，不影响算子语义）
ABBREV = {
    "DMG": "DAMAGE", "RES": "RESISTANCE", "REGEN": "REGENERATION",
    "RARITY": "RARITY", "QTY": "QUANTITY", "QUANTITY": "QUANTITY",
    "ENCH": "ENCHANT", "SUFFIX": "SUFFIX", "PREFIX": "PREFIX",
    "MOB": "MOB", "HP": "HEALTH", "ES": "ENERGY_SHIELD",
    "DEF": "DEFENCE", "EXTRA": "EXTRA", "ADD": "ADD", "INC": "INC",
}

# 算子同义词 -> 合一 canonical（维度互不交叉）
OP_FLAT = {"TO", "ADD", "ADDS", "GAIN", "GAINS", "ADDITIONAL"}      # flat +X
OP_INC  = {"INC", "INCREASED", "INCR", "INCREASE"}                  # additive %
OP_MORE = {"MORE"}                                                  # multiplicative %
OP_LESS = {"LESS"}                                                  # multiplicative down
OP_RED  = {"REDUCED", "REDUCE", "REDUCES"}                          # additive down %
OP_PCT  = {"PCT", "PERCENT", "PERCENTAGE"}                          # 裸百分比容器

# 修饰语同义（保留维度，仅拼写为一）
SYN = {
    "AGAINST": "AGAINST", "VS": "AGAINST", "VERSUS": "AGAINST",
    "WHILE": "WHILE", "IF": "IF", "WHEN": "IF",
    "PER": "PER", "FOR": "FOR", "EACH": "PER",
    "FROM": "FROM", "ON": "ON", "OF": "OF",
    "WITH": "WITH", "USING": "USING",
}


def norm_token(t: str) -> str:
    """缩写展开 + 算子同义词合一（维度不交叉）。"""
    u = t.upper()
    # 算子维度优先（这些词一旦命中即归一为该维度 token，不再二次处理）
    if u in OP_FLAT:
        return "ADD"
    if u in OP_INC:
        return "INC"
    if u in OP_MORE:
        return "MORE"
    if u in OP_LESS:
        return "LESS"
    if u in OP_RED:
        return "REDUCED"
    if u in OP_PCT:
        return "PCT"
    if u in SYN:
        return SYN[u]
    if u in ABBREV:
        return ABBREV[u]
    return u


def tokens(name: str):
    return [t for t in name.split("_") if t]


def load_constants():
    lines = STATIDS.read_text(encoding="utf-8").splitlines()
    out = []
    for i, ln in enumerate(lines):
        m = DECL.search(ln)
        if not m:
            continue
        comment = ""
        for j in range(i - 1, max(-1, i - 4), -1):
            s = lines[j].strip()
            if s.startswith("//"):
                comment = s[2:].strip()
                break
            if s:
                break
        out.append((m.group(1), m.group(2), comment))
    return out


def collect_refs():
    refs = defaultdict(list)
    pat = re.compile(r"StatIds\.([A-Z][A-Z0-9_]*)")
    for p in SRC.rglob("*.java"):
        sp = str(p).replace("\\", "/")
        if "support/stat/StatIds.java" in sp:
            continue
        if ".step" in sp or ".bak" in sp or ".pre_xstrip" in sp:
            continue
        try:
            text = p.read_text(encoding="utf-8")
        except Exception:
            continue
        for n, ln in enumerate(text.splitlines(), 1):
            for mm in pat.finditer(ln):
                refs[mm.group(1)].append((p.name, n))
    return refs


def main():
    consts = load_constants()
    names = {n for n, _, _ in consts}
    refs = collect_refs()

    # ---- L1 DUP_ID : 同 id 串 ----
    by_id = defaultdict(list)
    for n, i, c in consts:
        by_id[i].append((n, c))
    dup_id = {i: grp for i, grp in by_id.items() if len(grp) > 1}

    # ---- L2/L3 : token 集合 ----
    raw_tok = {}      # name -> set(raw tokens)
    syn_tok = {}      # name -> frozenset(normalized tokens)
    for n, i, c in consts:
        rt = tokens(n)
        raw_tok[n] = frozenset(rt)
        syn_tok[n] = frozenset(norm_token(t) for t in rt)

    set_dup = defaultdict(list)
    for n, i, c in consts:
        set_dup[syn_tok[n]].append((n, i, c))

    l2 = {k: grp for k, grp in set_dup.items() if len(grp) > 1 and len(k) > 0}

    # ---- L3 SUBSET : strict subset ----
    l3 = []
    items = [(n, syn_tok[n]) for n, _, _ in consts]
    for a, sa in items:
        for b, sb in items:
            if a == b:
                continue
            if sa and sa < sb:
                # a 是 b 的严格子集
                l3.append((a, b, len(sa), len(sb)))

    def refc(n):
        return len(refs.get(n, []))

    # ---- 输出 ----
    print("=" * 80)
    print("StatIds 语义重复判定（只读）")
    print("=" * 80)
    print(f"常量总数            : {len(consts)}")
    print(f"L1 硬重复(id 串相同) : {len(dup_id)} 组 / {sum(len(v) for v in dup_id.values())} 常量")
    print(f"L2 强候选(归一集合相同): {len(l2)} 组 / {sum(len(v) for v in l2.values())} 常量")
    print(f"L3 弱候选(子集关系)  : {len(l3)} 对")
    print()

    print("--- L1 硬重复（id 串完全相同，确定需合并）---")
    for i, grp in sorted(dup_id.items(), key=lambda x: -len(x[1])):
        print(f"  id(\"{i}\") <- {len(grp)} 个常量")
        for n, c in grp:
            print(f"      {n}(ref={refc(n)})  // {c[:60]}")
    print()

    print("--- L2 强候选（同义词+词序归一后 token 集合相同，需裁定）---")
    for k, grp in sorted(l2.items(), key=lambda x: -len(x[1])):
        print(f"  [set={'+'.join(sorted(k))}]  {len(grp)} 个")
        for n, i, c in grp:
            print(f"      {n}(ref={refc(n)})  id={i}  // {c[:54]}")
    print()

    print("--- L3 弱候选（子集关系，前 40，可能是 scope 差异非重复）---")
    seen = set()
    cnt = 0
    for a, b, la, lb in sorted(l3, key=lambda x: (x[2], x[0])):
        key = (a, b)
        if key in seen:
            continue
        seen.add(key)
        ca = next(c for n, _, c in consts if n == a)
        cb = next(c for n, _, c in consts if n == b)
        print(f"  {a}(ref={refc(a)}) ⊂ {b}(ref={refc(b)})")
        print(f"      {a} // {ca[:50]}")
        print(f"      {b} // {cb[:50]}")
        cnt += 1
        if cnt >= 40:
            print(f"  ... 共 {len(l3)} 对，显示前 40")
            break
    if not l3:
        print("  (无)")

    payload = {
        "total": len(consts),
        "l1_dup_id": {i: [n for n, _ in grp] for i, grp in dup_id.items()},
        "l2_set_dup": {",".join(sorted(k)): [n for n, _, _ in grp] for k, grp in l2.items()},
        "l3_subset_count": len(l3),
        "l3_subset_sample": [[a, b] for a, b, _, _ in l3[:200]],
    }
    OUT.write_text(json.dumps(payload, ensure_ascii=False, indent=1), encoding="utf-8")
    print(f"\n[written] {OUT}")


if __name__ == "__main__":
    main()
