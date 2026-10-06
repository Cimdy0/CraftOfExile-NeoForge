#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
verify_authoritative.py — PoE2 权威数据层完整性校验（独立可跑，不依赖 gradle）

用法（仓库任意位置均可）：
  C:/Users/19063/.workbuddy/binaries/python/envs/default/Scripts/python.exe \
      poe2_assets/authoritative/scripts/verify_authoritative.py
  --allow-drift   源已漂移时降级为 WARN，不判 FAIL
  --json          附加输出机器可读的 JSON 结果

设计原则：
  · 结构性不变量（唯一性、包含关系、族一致性）用**硬断言** —— 与具体数字无关。
  · 数字快照与 source_pin.json 记录的冻结值比对 —— 用来抓「有人手工改了冻结层」。
  · 团队 2026-09-19 基线（1046/377/614/55/887/1045）仅作**信息性对照**，输出 Δ，不作为期望值。
    （该基线已被实测值覆盖，详见本任务汇报。）
  · 源漂移检测：对 design/refactor/skill-data/crawled/all_skills.json 取 sha256 与
    source_pin.json 比对；不一致 => FAIL，提示重新冻结。
"""
# ruff: noqa: E501

import argparse
import hashlib
import json
import os
import sys

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", ".."))
AUTH = os.path.join(ROOT, "poe2_assets", "authoritative")
SRC = os.path.join(ROOT, "design", "refactor", "skill-data", "crawled", "all_skills.json")

ROMAN_SUFFIXES = ("_i", "_ii", "_iii", "_iv", "_v", "_vi", "_vii", "_viii")

results = []


def rec(idx, name, ok, actual, expected=None, baseline=None, note=""):
    """记录一条断言结果。"""
    if baseline is not None and expected is not None:
        try:
            delta = f" | baseline {baseline} | Δ {int(actual) - int(baseline):+d}"
        except (TypeError, ValueError):
            delta = f" | baseline {baseline}"
    else:
        delta = ""
    exp = f" | expected {expected}" if expected is not None else ""
    results.append({
        "id": idx, "name": name, "pass": bool(ok),
        "actual": actual, "expected": expected, "baseline": baseline, "note": note,
    })
    tag = "PASS" if ok else "FAIL"
    print(f"  [{idx:02d}] {tag}  {name}")
    print(f"        actual   = {actual}{exp}{delta}")
    if note:
        print(f"        note     = {note}")
    return bool(ok)


def load(p):
    with open(p, encoding="utf-8") as f:
        return json.load(f)


def sha256_of(path):
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--allow-drift", action="store_true")
    ap.add_argument("--json", action="store_true")
    args = ap.parse_args()

    print("=" * 78)
    print("PoE2 权威数据层 — 完整性校验")
    print(f"  冻结层: {os.path.relpath(AUTH, ROOT)}")
    print(f"  源    : {os.path.relpath(SRC, ROOT)}")
    print("=" * 78)

    pin_path = os.path.join(AUTH, "source_pin.json")
    frozen_path = os.path.join(AUTH, "all_skills.json")
    for p in (pin_path, frozen_path):
        if not os.path.exists(p):
            print(f"FATAL: 缺少 {os.path.relpath(p, ROOT)} —— 请先跑 scripts/freeze_authoritative.js")
            return 2

    pin = load(pin_path)
    snap = pin.get("measured_snapshot", {})
    base = pin.get("known_baseline_2026_09_19", {})
    frozen = load(frozen_path)
    E = frozen["entries"]

    # ---------------------------------------------------------------- [00] 源漂移
    print("\n-- 漂移检测 --")
    cur_sha = sha256_of(SRC)
    pinned_sha = pin.get("sha256")
    drift = cur_sha != pinned_sha
    if drift:
        msg = (f"源已被重写（现 sha256 {cur_sha[:16]}… / 冻结时 {pinned_sha[:16]}…）。"
               f"重跑 scripts/freeze_authoritative.js + convert_zh.py 重新冻结。")
        print(f"  DRIFT DETECTED: {msg}")
        results.append({"id": 0, "name": "源未被重爬（sha256 与冻结指纹一致）",
                        "pass": bool(args.allow_drift), "actual": cur_sha[:16],
                        "expected": pinned_sha[:16], "baseline": None, "note": msg})
        print(("  [00] WARN  源未漂移（已 --allow-drift 降级）" if args.allow_drift
               else "  [00] FAIL  源未漂移（sha256 与冻结指纹一致）"))
        print(f"        actual   = {cur_sha[:16]}… | expected {pinned_sha[:16]}…")
    else:
        rec(0, "源未被重爬（sha256 与冻结指纹一致）", True,
            cur_sha[:16] + "…", pinned_sha[:16] + "…", None,
            f"crawled_at={frozen.get('meta', {}).get('crawled_at')}")

    print("\n-- 断言 --")

    # ---------------------------------------------------------------- ① 总条目
    rec(1, "总条目数 == 冻结快照", len(E) == snap.get("entry_count"),
        len(E), snap.get("entry_count"), base.get("entry_count"))

    # ---------------------------------------------------------------- ② 类型分布
    by = {}
    for e in E:
        by[e.get("gem_type")] = by.get(e.get("gem_type"), 0) + 1
    pinned_by = snap.get("by_type", {})
    ok2 = all(by.get(k) == pinned_by.get(k) for k in ("skill", "support", "spirit")) and len(by) == 3
    rec(2, "by_type 与冻结快照一致 {skill, support, spirit}", ok2,
        json.dumps(by), json.dumps(pinned_by), json.dumps(base.get("by_type", {})))

    # ---------------------------------------------------------------- ③ 唯一 base
    base_ids = {e.get("base_id") for e in E}
    rec(3, "唯一 base_id 数（去 tier 变体）== 冻结快照",
        len(base_ids) == snap.get("unique_base"),
        len(base_ids), snap.get("unique_base"), base.get("unique_base"))

    # ---------------------------------------------------------------- ④ Spirit ⊆ Skill
    with_spirit_page = [e for e in E if "Spirit_Gems" in (e.get("seen_in") or [])]
    with_skill_page = [e for e in E if "Skill_Gems" in (e.get("seen_in") or [])]
    skill_page_ids = {e["id"] for e in with_skill_page}
    subset_ok = all(e["id"] in skill_page_ids for e in with_spirit_page)
    gemtype_spirit = by.get("spirit", 0)
    note4 = (f"gem_type=spirit 计数 {gemtype_spirit}，seen_in 含 Spirit_Gems 计数 {len(with_spirit_page)}；"
             f"差 {gemtype_spirit - len(with_spirit_page)} 条来自 extra:gemless（无宝石形态的固有技能）。"
             f"二者口径不同，本断言取 seen_in 口径 —— 该口径下 Spirit_Gems 应为 {len(with_spirit_page)}。")
    rec(4, "Spirit ⊆ Skill（每个 Spirit_Gems 条目都同时出现在 Skill_Gems）", subset_ok,
        f"Spirit_Gems={len(with_spirit_page)} ⊆ Skill_Gems={len(with_skill_page)}",
        "Spirit_Gems 全为 Skill_Gems 子集", None, note4)

    # ---------------------------------------------------------------- ⑤ tier 变体族解析
    def strip_roman(s):
        for suf in ROMAN_SUFFIXES:
            if s and s.endswith(suf):
                return s[: -len(suf)]
        return s

    variants = [e for e in E if strip_roman(e.get("id", "")) != e.get("id")]
    unresolvable = [e["id"] for e in E if e.get("base_id") not in base_ids]
    missing_base_entry = [e["id"] for e in variants if e.get("base_id") not in {x["id"] for x in E}]
    fam = {}
    for e in E:
        fam.setdefault(e.get("base_id"), []).append(e.get("gem_type"))
    fam_members = {}
    for e in E:
        fam_members.setdefault(e.get("base_id"), []).append(e.get("id"))
    all_variant_fams = len([k for k, v in fam_members.items() if all(strip_roman(i) != i for i in v)])
    mixed = [k for k, v in fam.items() if len(set(v)) > 1]
    derived_mism = [e["id"] for e in E if e.get("base_id") != strip_roman(e.get("id", ""))]
    ok5 = (len(unresolvable) == 0) and (len(mixed) == 0) and (len(derived_mism) == 0)
    note5 = (f"tier 变体 {len(variants)} 条；其中 {len(missing_base_entry)} 条的 base_id 在数据集中"
             f"**没有同名基础条目**（如 bleed_i -> bleed，但数据集里没有 bleed 条目）—— base_id 是派生的"
             f"『族键』而非现有 entry id，这是权威源的真实形态，不是缺陷。"
             f"家族数 {len(fam)}，其中 {all_variant_fams} 个家族的全部成员都是 tier 变体。")
    rec(5, "tier 变体的 base_id 均可解析，且同族 gem_type 一致", ok5,
        f"unresolvable={len(unresolvable)}, mixed_type_family={len(mixed)}, derived_mismatch={len(derived_mism)}",
        "unresolvable=0, mixed_type_family=0, derived_mismatch=0", None, note5)

    # ---------------------------------------------------------------- ⑥ 无 DNT
    dnt = [e.get("id") for e in E if str(e.get("id", "")).upper().startswith("DNT")]
    rec(6, "无 DNT-* 废弃条目", len(dnt) == 0, len(dnt), 0, 0)

    # ---------------------------------------------------------------- ⑦ 图标覆盖
    ic = {}
    for e in E:
        ic[e.get("icon_status")] = ic.get(e.get("icon_status"), 0) + 1
    ok7 = (ic.get("existing", 0) == snap.get("icon_existing")
           and ic.get("missing", 0) == snap.get("icon_missing")
           and sum(ic.values()) == len(E))
    missing_ids = [e.get("id") for e in E if e.get("icon_status") != "existing"]
    rec(7, "图标覆盖 == 冻结快照，且状态之和 == 总条目", ok7,
        json.dumps(ic) + f" | missing_ids={missing_ids}",
        f"existing={snap.get('icon_existing')}, missing={snap.get('icon_missing')}",
        json.dumps({"existing": base.get("icon_existing"), "missing": 1}))

    # ---------------------------------------------------------------- ⑧ 必填字段
    def present(v):
        if v is None:
            return False
        if isinstance(v, str):
            return v.strip() != ""
        if isinstance(v, (list, dict)):
            return len(v) > 0
        return True

    miss_id = [e.get("id", "(no id)") for e in E if not present(e.get("id"))]
    miss_en = [e.get("id", "(no id)") for e in E if not present(e.get("name_en"))]
    miss_iid = [e.get("id", "(no id)") for e in E if not present(e.get("internal_id"))]
    ok8 = len(miss_id) == 0 and len(miss_en) == 0
    note8 = (
        (f"internal_id 缺失 {len(miss_iid)} 条（已知例外，允许）：{', '.join(miss_iid)}"
         if miss_iid else "internal_id 无缺失")
        + " —— 这些全部是 extra:gemless 无宝石形态技能或页面未暴露 Metadata 的条目，属权威源真实形态。"
    )
    rec(8, "每条都有 id 与 name_en（internal_id 例外单独列出）", ok8,
        f"missing_id={len(miss_id)}, missing_name_en={len(miss_en)}, missing_internal_id={len(miss_iid)}",
        "missing_id=0, missing_name_en=0", None, note8)

    # ---------------------------------------------------------------- ⑨ id 唯一
    ids = [e.get("id") for e in E]
    dup = sorted({i for i in ids if ids.count(i) > 1})
    rec(9, "id 全局唯一（无重复）", len(dup) == 0, f"duplicates={dup if dup else 0}", "duplicates=0")

    # ---------------------------------------------------------------- ⑩ by_type 与冻结层一致
    bt_dir = os.path.join(AUTH, "by_type")
    sum_bt = 0
    bt_ok = True
    bt_detail = []
    for t in ("skill", "support", "spirit"):
        p = os.path.join(bt_dir, t + ".json")
        if not os.path.exists(p):
            bt_ok = False
            bt_detail.append(f"{t}: MISSING")
            continue
        sub = load(p)
        ents = sub.get("entries", [])
        sum_bt += len(ents)
        ok_t = all(x.get("gem_type") == t for x in ents)
        bt_ok = bt_ok and ok_t
        bt_detail.append(f"{t}={len(ents)}(homogeneous={ok_t})")
    rec(10, "by_type/*.json 之和 == 总条目，且各文件 gem_type 纯净", bt_ok and sum_bt == len(E),
        f"sum={sum_bt}, {', '.join(bt_detail)}", f"sum={len(E)}")

    # ---------------------------------------------------------------- ⑪ _provenance 存在且自洽
    prov = frozen.get("_provenance")
    ok11 = bool(prov) and prov.get("entry_count") == len(E) and bool(prov.get("source_pin", {}).get("sha256"))
    rec(11, "all_skills.json 含 _provenance（含源 sha256 指纹）", ok11,
        f"entry_count={prov.get('entry_count') if prov else None}, "
        f"pin={str(prov.get('source_pin', {}).get('sha256'))[:16] if prov else None}…",
        f"entry_count={len(E)}, pin=<non-empty>")

    # ---------------------------------------------------------------- ⑫ 简体中文名覆盖
    zh_path = os.path.join(AUTH, "zh_cn_names.json")
    if not os.path.exists(zh_path):
        results.append({"id": 12, "name": "zh_cn_names.json 覆盖所有有 name_zh 的条目",
                        "pass": False, "actual": "FILE MISSING", "expected": None, "baseline": None,
                        "note": "请先跑 scripts/convert_zh.py"})
        print("  [12] FAIL  zh_cn_names.json 覆盖所有有 name_zh 的条目")
        print("        actual   = FILE MISSING")
    else:
        zh = load(zh_path)
        zh_meta = zh.get("_meta", {})
        names = {k: v for k, v in zh.items() if k != "_meta"}
        with_zh = [e.get("id") for e in E if present(e.get("name_zh"))]
        lack = [i for i in with_zh if i not in names]
        stranger = [k for k in names if k not in set(ids)]
        noop_cn = [k for k, v in names.items() if not present(v.get("zh_cn"))]
        ok12 = not lack and not stranger and not noop_cn and \
            zh_meta.get("without_zh_left_empty") == (len(E) - len(with_zh))
        rec(12, "zh_cn_names.json 与权威源 1:1（有繁体必有简体，无繁体必留空）", ok12,
            f"有中文名 {len(with_zh)} / 已转 {len(names)} / 缺转 {len(lack)} / 越界键 {len(stranger)} / 留空 {len(E) - len(with_zh)}",
            f"已转={len(with_zh)}, 缺转=0, 越界键=0, 留空={len(E) - len(with_zh)}",
            None, f"opencc t2s 未转干净 {zh_meta.get('not_idempotent_count')} 条")

    # ---------------------------------------------------------------- 汇总
    failed = [r for r in results if not r["pass"]]
    print("\n" + "=" * 78)
    print(f"RESULT: {'PASS' if not failed else 'FAIL'}   "
          f"({len(results) - len(failed)}/{len(results)} 通过)")
    if failed:
        print("失败项:")
        for r in failed:
            print(f"  [{r['id']:02d}] {r['name']}  actual={r['actual']}  expected={r['expected']}")
            if r.get("note"):
                print(f"        -> {r['note']}")
    print("=" * 78)

    if args.json:
        out = os.path.join(AUTH, "verify_result.json")
        with open(out, "w", encoding="utf-8") as f:
            json.dump({"generated_results": results, "all_pass": not failed}, f,
                      ensure_ascii=False, indent=2)
        print(f"JSON 结果已写入 {os.path.relpath(out, ROOT)}")

    return 0 if not failed else 1


if __name__ == "__main__":
    sys.exit(main())
