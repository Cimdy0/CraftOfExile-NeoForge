#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
convert_zh.py — 权威中文名：繁体 -> 简体

权威源 design/refactor/skill-data/crawled/all_skills.json 的 name_zh 是**繁体**（台湾源 poe2db.tw/tw）。
本模组既有 lang 是**简体**（如 alchemist_s_boon=炼金师的恩典、arc=电弧连锁），
因此 datagen 消费前必须先做 t2s 转换。

铁律：name_zh 为 null 的条目**留空，不臆造** —— 宁可缺翻译，不可编中文名。

产出： poe2_assets/authoritative/zh_cn_names.json

用法：
  C:/Users/19063/.workbuddy/binaries/python/envs/default/Scripts/python.exe ^
      poe2_assets/authoritative/scripts/convert_zh.py
"""
# ruff: noqa: E501

import json
import os
import sys
from datetime import datetime, timezone

try:
    from opencc import OpenCC
except ImportError:  # pragma: no cover
    print("FATAL: 需要 opencc。请确认使用了 venv 的 python：")
    print(r"  C:/Users/19063/.workbuddy/binaries/python/envs/default/Scripts/python.exe")
    sys.exit(2)

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", ".."))
SRC = os.path.join(ROOT, "design", "refactor", "skill-data", "crawled", "all_skills.json")
ZH_NAMES = os.path.join(ROOT, "design", "refactor", "skill-data", "crawled", "zh_names.json")
OUT_DIR = os.path.join(ROOT, "poe2_assets", "authoritative")
OUT = os.path.join(OUT_DIR, "zh_cn_names.json")
GENERATOR = "poe2_assets/authoritative/scripts/convert_zh.py"


def empty(v):
    return v is None or (isinstance(v, str) and v.strip() == "")


def main():
    cc = OpenCC("t2s")

    with open(SRC, encoding="utf-8") as f:
        src = json.load(f)
    entries = src["entries"]

    out = {}
    skipped = []
    unchanged_from_tw = []    # t2s 前后完全相同（多为单字词或已是简体形态），仅供参考
    not_idempotent = []       # 再转一次还会变化 => opencc 未转干净，是真缺陷，需人工看

    for e in entries:
        sid = e.get("id")
        zh_tw = e.get("name_zh")
        if empty(zh_tw):
            skipped.append(sid)
            continue  # 铁律：留空
        zh_cn = cc.convert(zh_tw)
        if cc.convert(zh_cn) != zh_cn:
            # 幂等性检查：简体再转换一次应当不变；变了说明还有繁体残留
            not_idempotent.append({"id": sid, "zh_tw": zh_tw, "zh_cn": zh_cn})
        out[sid] = {"zh_tw": zh_tw, "zh_cn": zh_cn}
        if zh_cn == zh_tw:
            unchanged_from_tw.append(sid)

    # 与 crawled/zh_names.json 交叉校验。注意：zh_names.json 的键是 poe2db **页名**
    # （PascalCase，如 Leap_Slam），不是 slug id，必须经 source_url_tw 的末段做桥接。
    cross = {"loaded": False}
    if os.path.exists(ZH_NAMES):
        with open(ZH_NAMES, encoding="utf-8") as f:
            zn = json.load(f)
        mismatch = []
        # 需要 source_url，回读一次建立 id -> 页名 桥接
        page_of = {}
        for e in entries:
            url = e.get("source_url_tw") or e.get("source_url_us") or ""
            page_of[e.get("id")] = url.rstrip("/").split("/")[-1]
        for sid, v in out.items():
            page = page_of.get(sid, "")
            if page in zn:
                if zn[page] != v["zh_tw"]:
                    mismatch.append({"id": sid, "page": page, "all_skills": v["zh_tw"], "zh_names": zn[page]})
        used_pages = set(page_of.values())
        leftover = [k for k in zn if k not in used_pages]
        cross.update({
            "loaded": True,
            "key_space": "poe2db 页名（PascalCase），经 source_url_tw 末段桥接",
            "zh_names_entry_count": len(zn),
            "matched_by_page": len(zn) - len(leftover),
            "leftover_keys": len(leftover),
            "leftover_are_dnt": all(str(k).upper().startswith("DNT") for k in leftover),
            "leftover_keys_list": leftover,
            "name_zh_mismatch_count": len(mismatch),
            "mismatch_samples": mismatch[:10],
        })

    result = {
        "_meta": {
            "purpose": "权威 id -> 中文名。zh_tw 为 poe2db.tw/tw 原始繁体，zh_cn 为 opencc(t2s) 转换后的简体。",
            "generator": GENERATOR,
            "source": "design/refactor/skill-data/crawled/all_skills.json",
            "opencc_config": "t2s",
            "generated_at": datetime.now(timezone.utc).isoformat(),
            "source_crawled_at": src.get("meta", {}).get("crawled_at"),
            "total_entries": len(entries),
            "with_zh": len(out),
            "without_zh_left_empty": len(skipped),
            "left_empty_ids": skipped,
            "note": "以 _ 开头的键是元数据，datagen 消费时请跳过；其余键均为权威 skill id。",
            "iron_rule": "name_zh 缺失的条目一律留空，不得臆造中文名。",
            "unchanged_after_t2s_count": len(unchanged_from_tw),
            "unchanged_after_t2s_note": "t2s 转换前后完全相同的条目（多为单字词、含罗马数字的 tier 变体如「流血 I」，或本身已是简体形态）。仅供参考，不视为缺陷。",
            "not_idempotent_count": len(not_idempotent),
            "not_idempotent_note": "对 zh_cn 再跑一次 t2s 仍会变化的条目 = opencc 未转干净的繁体残留；这是真实缺陷清单，请人工复核。",
            "not_idempotent_items": not_idempotent,
            "crosscheck_vs_zh_names_json": cross,
        }
    }
    result.update(out)

    os.makedirs(OUT_DIR, exist_ok=True)
    with open(OUT, "w", encoding="utf-8") as f:
        json.dump(result, f, ensure_ascii=False, indent=2)

    kb = os.path.getsize(OUT) / 1024
    print(f"wrote {os.path.relpath(OUT, ROOT)}  ({kb:.1f} KB)")
    print(f"  总条目          : {len(entries)}")
    print(f"  有中文名(已转换): {len(out)}")
    print(f"  缺中文名(留空)  : {len(skipped)}")
    print(f"  t2s 后未变      : {len(unchanged_from_tw)}（多为单字词 / tier 变体罗马数字，非缺陷）")
    print(f"  t2s 未转干净    : {len(not_idempotent)}  <- 需人工复核")
    if cross.get("loaded"):
        print(f"  vs zh_names.json: 总键={cross['zh_names_entry_count']} 命中={cross['matched_by_page']} "
              f"值不一致={cross['name_zh_mismatch_count']} 剩余键={cross['leftover_keys']}（全为 DNT-* = {cross['leftover_are_dnt']}）")

    print("\n抽查 8 条:")
    for i, sid in enumerate(list(out)[:8]):
        print(f"  {sid:28s} {out[sid]['zh_tw']}  ->  {out[sid]['zh_cn']}")


if __name__ == "__main__":
    main()
