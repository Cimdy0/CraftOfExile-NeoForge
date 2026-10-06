# -*- coding: utf-8 -*-
"""Phase 1 机械执行：由 xstrip_dryrun.json 驱动去 X。

- StatIds.java：RENAME_CLEAN 改名声明 + 同步去 id 串的 x；MERGE_LEGACY 整行删声明 + 紧邻 // 注释。
- 消费文件：StatIds.OLD -> StatIds.NEW（词边界 + 长优先，防子串误伤）。
不重跑生成器、不读 JSON。

用法：
  python _apply_xstrip.py --dry      # 仅打印将要发生的改动样本
  python _apply_xstrip.py --apply    # 实际改写
"""
import json
import re
import sys
from pathlib import Path

ROOT = Path("E:/McModding/Modding/CraftOfExile-NeoForge")
STATIDS = ROOT / "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"
SRC = ROOT / "src/main/java"
MAP = ROOT / "poe2_assets/_test/xstrip_dryrun.json"

DECL = re.compile(r'^(\s*)(public static final Identifier )(\w+)( = id\()("[^"]*")(\);)')

data = json.loads(MAP.read_text(encoding="utf-8"))
rename_clean = data["rename_clean"]   # old_name -> new_name
merge_legacy = data["merge_legacy"]   # old_name -> legacy_target


def strip_x_id(sid: str) -> str:
    return "_".join(t for t in sid.split("_") if t != "x")


def apply_statids(dry):
    lines = STATIDS.read_text(encoding="utf-8").splitlines()
    out = []
    renamed = deleted = 0
    id_samples = []
    i = 0
    while i < len(lines):
        ln = lines[i]
        m = DECL.match(ln)
        if m:
            name = m.group(3)
            if name in merge_legacy:
                if out and out[-1].lstrip().startswith("//"):
                    out.pop()
                deleted += 1
                i += 1
                continue
            if name in rename_clean:
                new_name = rename_clean[name]
                old_id = m.group(5)[1:-1]
                new_id = strip_x_id(old_id)
                if old_id != new_id:
                    id_samples.append((old_id, new_id))
                out.append(f"{m.group(1)}{m.group(2)}{new_name}{m.group(4)}\"{new_id}\"{m.group(6)}")
                renamed += 1
                i += 1
                continue
        out.append(ln)
        i += 1
    if not dry:
        STATIDS.write_text("\n".join(out) + "\n", encoding="utf-8")
    return renamed, deleted, id_samples


def apply_consumers(dry):
    items = sorted(rename_clean.items(), key=lambda kv: -len(kv[0]))
    changed_files = []
    total_repl = 0
    for p in SRC.rglob("*.java"):
        sp = str(p).replace("\\", "/")
        if "support/stat/StatIds.java" in sp:
            continue
        if ".step" in sp or ".bak" in sp or "pre_xstrip" in sp:
            continue
        text = p.read_text(encoding="utf-8")
        if "StatIds." not in text:
            continue
        new_text = text
        for old, new in items:
            new_text = re.sub(r'StatIds\.' + re.escape(old) + r'\b', 'StatIds.' + new, new_text)
        if new_text != text:
            c = sum(1 for _ in re.finditer(r'StatIds\.', new_text))  # noqa
            changed_files.append((p.name, len(text.splitlines())))
            total_repl += 1
            if not dry:
                p.write_text(new_text, encoding="utf-8")
    return changed_files, total_repl


def check_duplicate_ids():
    ids = {}
    for ln in STATIDS.read_text(encoding="utf-8").splitlines():
        m = re.search(r'id\("([^"]+)"\)', ln)
        if m:
            ids.setdefault(m.group(1), 0)
            ids[m.group(1)] += 1
    dups = {k: v for k, v in ids.items() if v > 1}
    return dups


def main():
    dry = "--apply" not in sys.argv
    print(f"[mode] {'DRY-RUN (no writes)' if dry else 'APPLY'}")
    rn, dl, ids = apply_statids(dry)
    cf, tr = apply_consumers(dry)
    print(f"StatIds.java   : renamed={rn} deleted={dl}")
    print(f"consumer files : changed={len(cf)} (reference rewrite passes={tr})")
    print(f"id-string X-strip samples (old -> new), first 25:")
    for a, b in ids[:25]:
        print(f"    {a}  ->  {b}")
    print(f"  ... total {len(ids)} id strings changed")
    if not dry:
        dups = check_duplicate_ids()
        print(f"duplicate id strings after apply: {dups if dups else 'NONE'}")
    print(f"\n[done] {'dry-run, review then re-run with --apply' if dry else 'applied'}")


if __name__ == "__main__":
    main()
