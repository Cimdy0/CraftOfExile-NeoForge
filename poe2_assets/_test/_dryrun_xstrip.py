# -*- coding: utf-8 -*-
"""Phase 1 干跑：量化「移除数值 X」后的改名 / 冲突 / 合并规模。

纯静态读 .java，不读 JSON，不改任何文件。
输出三类：
  RENAME_CLEAN  去 X 后新名不与任何现存常量冲突   -> 机械改名
  MERGE_LEGACY  去 X 后新名 == 现存无 X 常量      -> 合并到旧族（旧名优先）
  COLLIDE_XX    多个带 X 常量去 X 后彼此同名      -> 需人工/规则定夺
并交叉 826 个引用点，给出每类的真实工作量。
"""
import json
import re
from pathlib import Path

ROOT = Path("E:/McModding/Modding/CraftOfExile-NeoForge")
STATIDS = ROOT / "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"
SRC = ROOT / "src/main/java"
OUT = ROOT / "poe2_assets/_test/xstrip_dryrun.json"

DECL = re.compile(r'public static final Identifier (\w+) = id\("([^"]+)"\);')


def load_constants():
    """返回 [(name, id, comment)]，注释取声明行上方最近的 // 行。"""
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


def strip_x_name(name):
    """按 token 移除独立的 X（可能多处），合并下划线。"""
    toks = [t for t in name.split("_") if t != "X"]
    return "_".join(toks)


def strip_x_id(sid):
    toks = [t for t in sid.split("_") if t != "x"]
    return "_".join(toks)


def collect_refs():
    """全仓引用点：constant -> [(file, line)]；排除 StatIds.java 自身与备份噪音。"""
    refs = {}
    pat = re.compile(r"StatIds\.([A-Z][A-Z0-9_]*)")
    for p in SRC.rglob("*.java"):
        sp = str(p).replace("\\", "/")
        if "support/stat/StatIds.java" in sp:
            continue
        if ".step" in sp or ".bak" in sp:
            continue
        try:
            text = p.read_text(encoding="utf-8")
        except Exception:
            continue
        for n, ln in enumerate(text.splitlines(), 1):
            for m in pat.finditer(ln):
                refs.setdefault(m.group(1), []).append((p.name, n))
    return refs


def main():
    consts = load_constants()
    names = {n for n, _, _ in consts}
    refs = collect_refs()

    with_x = [(n, i, c) for n, i, c in consts if "X" in n.split("_")]
    no_x = [(n, i, c) for n, i, c in consts if "X" not in n.split("_")]

    # 目标名归组，检出 X 常量间互撞
    target_groups = {}
    for n, i, c in with_x:
        target_groups.setdefault(strip_x_name(n), []).append((n, i, c))

    rename_clean, merge_legacy, collide_xx = [], [], []
    for tgt, group in target_groups.items():
        hits_legacy = tgt in names
        if len(group) > 1:
            collide_xx.append((tgt, group, hits_legacy))
        elif hits_legacy:
            merge_legacy.append((tgt, group[0]))
        else:
            rename_clean.append((tgt, group[0]))

    def refcount(name):
        return len(refs.get(name, []))

    def bucket_stat(items, getnames):
        tot_c = tot_r = 0
        for it in items:
            for nm in getnames(it):
                tot_c += 1
                tot_r += refcount(nm)
        return tot_c, tot_r

    c1, r1 = bucket_stat(rename_clean, lambda it: [it[1][0]])
    c2, r2 = bucket_stat(merge_legacy, lambda it: [it[1][0]])
    c3, r3 = bucket_stat(collide_xx, lambda it: [g[0] for g in it[1]])

    referenced = {n for n in refs if n in names}
    unref = names - referenced
    dangling = set(refs) - names

    print("=" * 78)
    print("Phase 1 干跑 — 移除数值 X 的改名 / 冲突规模")
    print("=" * 78)
    print(f"StatIds 常量总数            : {len(consts)}")
    print(f"  带 X 参数标记             : {len(with_x)}")
    print(f"  无 X                      : {len(no_x)}")
    print(f"全仓引用点总数              : {sum(len(v) for v in refs.values())}")
    print(f"  被引用的 unique 常量      : {len(referenced)}")
    print(f"  零引用常量(reserved 候选) : {len(unref)}")
    print(f"  引用了不存在的常量(断裂)  : {len(dangling)}")
    print()
    print("--- 去 X 分类（按目标名归组）---")
    print(f"A. RENAME_CLEAN 机械改名     : {c1} 常量 / {r1} 引用点")
    print(f"B. MERGE_LEGACY 并入旧族     : {c2} 常量 / {r2} 引用点")
    print(f"C. COLLIDE_XX  X 常量间互撞  : {c3} 常量 / {r3} 引用点  (目标名 {len(collide_xx)} 组)")
    print()

    print("--- B. MERGE_LEGACY 中「有引用」的（真实工作量，最多 30）---")
    hot_b = [(t, g) for t, g in merge_legacy if refcount(g[0]) or refcount(t)]
    hot_b.sort(key=lambda x: -(refcount(x[1][0]) + refcount(x[0])))
    for t, g in hot_b[:30]:
        print(f"  {g[0]}({refcount(g[0])}) -> {t}({refcount(t)})")
    print(f"  ... 共 {len(hot_b)} 组有引用")
    print()

    print("--- C. COLLIDE_XX 中「有引用」的组（需定夺，最多 20）---")
    hot_c = [(t, g, hl) for t, g, hl in collide_xx if any(refcount(x[0]) for x in g) or hl]
    hot_c.sort(key=lambda x: -sum(refcount(y[0]) for y in x[1]))
    for t, g, hl in hot_c[:20]:
        tag = " [+legacy同名]" if hl else ""
        print(f"  => {t}{tag}")
        for nm, sid, cm in g:
            print(f"       {nm}({refcount(nm)})   // {cm[:66]}")
    print(f"  ... 共 {len(hot_c)} 组有引用/撞旧族；总 {len(collide_xx)} 组")
    print()

    print("--- 断裂引用（引用了 StatIds 里不存在的常量）---")
    for d in sorted(dangling)[:20]:
        print(f"  {d}  <- {refs[d][:2]}")
    if not dangling:
        print("  (无)")

    payload = {
        "total": len(consts),
        "with_x": len(with_x),
        "no_x": len(no_x),
        "ref_points": sum(len(v) for v in refs.values()),
        "referenced_unique": len(referenced),
        "unreferenced": len(unref),
        "dangling": sorted(dangling),
        "rename_clean": {g[0]: t for t, g in rename_clean},
        "merge_legacy": {g[0]: t for t, g in merge_legacy},
        "collide_xx": {t: [x[0] for x in g] for t, g, _ in collide_xx},
        "counts": {
            "rename_clean": [c1, r1],
            "merge_legacy": [c2, r2],
            "collide_xx": [c3, r3],
        },
    }
    OUT.write_text(json.dumps(payload, ensure_ascii=False, indent=1), encoding="utf-8")
    print(f"\n[written] {OUT}")


if __name__ == "__main__":
    main()
