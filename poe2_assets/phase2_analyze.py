# -*- coding: utf-8 -*-
"""Phase 2 分析：复用 build_wire_table 的 EFF 索引与分桶结果，
对 REVIEW / OP_GAP 项按失败原因细分，并针对每个概念族枚举可落地候选，
为「按族裁定」提供可验证的输入。只读分析，不改 StatGen。
"""
import os, re, json, collections, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import build_wire_table as B

ROOT = B.ROOT
EFF_GEN = B.EFF_GEN
WIRE = os.path.join(ROOT, "poe2_assets", "wire_table.json")

def parse_eff_index():
    return B.parse_eff_index()

def main():
    table = json.load(open(WIRE, encoding="utf-8"))
    eff = parse_eff_index()

    # ---- 按 bucket + 失败原因细分 ----
    by_bucket = collections.Counter(r["bucket"] for r in table)
    reason = collections.Counter()
    reason_examples = collections.defaultdict(list)
    for r in table:
        b = r["bucket"]
        note = r.get("note", "")
        if b == "CLEAN":
            reason["CLEAN"] += 1
            continue
        # 解析 note 中的 issue 标记
        issues = [p.split(":")[0] for p in note.split("; ") if p in (
            "multi-cand", "unresolved-scope", "variant-mismatch", "eff-overspecific") or p.startswith("anchor")]
        if not issues:
            issues = ["other:" + note[:40]]
        for it in issues:
            reason[it] += 1
            if len(reason_examples[it]) < 8:
                reason_examples[it].append(r["const"])

    print("=== 分桶计数 ===")
    for b, n in by_bucket.most_common():
        print(f"  {b:8s} {n}")
    print("\n=== REVIEW/OP_GAP 失败原因细分 ===")
    for k, n in reason.most_common():
        print(f"  {k:22s} {n:4d}  e.g. {reason_examples[k][:6]}")

    # ---- 按概念族聚合（来自 build_wire_table 的 family key）----
    fam = collections.defaultdict(lambda: collections.Counter())
    for r in table:
        if r["bucket"] in ("REVIEW", "OP_GAP"):
            famkey = "|".join(sorted(r["concept"])) if r["concept"] else ("MECH:" + (r["mech_class"] or "?"))
            fam[famkey][r["bucket"]] += 1
    print(f"\n=== 需裁定的概念族数量: {len(fam)} ===")
    print("Top 25 族（按 REVIEW+OP_GAP 总数）:")
    for k, c in sorted(fam.items(), key=lambda kv: -sum(kv[1].values()))[:25]:
        print(f"  {k:40s} REVIEW={c['REVIEW']:3d} OP_GAP={c['OP_GAP']:3d}")

    # ---- 针对 OP_GAP：概念有 EFF 但 op 不对 -> 找同概念且 op 正确的 EFF ----
    print("\n=== OP_GAP 可补 op 变体候选（同 concept 族，op 正确的 EFF 是否存在）===")
    opgap_fixable = 0
    opgap_need_new = 0
    for r in table:
        if r["bucket"] != "OP_GAP":
            continue
        concept = set(r["concept"])
        # 找同 concept 且 impl=ModifierStatEffect + op 正确 的 EFF
        op_want = r["op_kind"]
        fix = [ec for ec, e in eff.items()
               if concept and concept.issubset(set(e["concept"]))
               and e["impl"] == "ModifierStatEffect" and e["op"] == op_want]
        if fix:
            opgap_fixable += 1
        else:
            opgap_need_new += 1
    print(f"  OP_GAP 总数={by_bucket['OP_GAP']}  => 同族已有正确op变体(可直接补): {opgap_fixable}  需新设计op变体: {opgap_need_new}")

    # ---- 针对 REVIEW-anchor：找真正匹配的 EFF（放宽 concept 子集到交集）----
    print("\n=== REVIEW 中 anchor-match 项：核心锚点 token 命中的候选 EFF 数 ===")
    anchor_total = 0
    anchor_with_multi = 0
    for r in table:
        if r["bucket"] != "REVIEW":
            continue
        if not r["note"].startswith("anchor"):
            continue
        anchor_total += 1
        anc = [c for c in r["concept"] if len(c) >= 4]
        hits = [ec for ec, e in eff.items() if anc and set(anc).intersection(set(e["concept"]))]
        if len(hits) >= 1:
            anchor_with_multi += 1
    print(f"  anchor 项={anchor_total}  有>=1个锚点命中EFF={anchor_with_multi}")

    os.makedirs(os.path.join(ROOT, "poe2_assets"), exist_ok=True)
    json.dump({"by_bucket": dict(by_bucket), "reason": dict(reason)},
              open(os.path.join(ROOT, "poe2_assets", "phase2_summary.json"), "w", encoding="utf-8"),
              ensure_ascii=False, indent=1)
    print("\n已写出 phase2_summary.json")

if __name__ == "__main__":
    main()
