# -*- coding: utf-8 -*-
"""Step 7 清理：A) 移除误标 TODO(接线)  B) 删除零引用 StatIds 常量。
默认 dry-run；--apply 落地（先 cp 备份 *.step7bak）。"""
import re, os, sys

STATIDS = "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"


def java_files():
    out = []
    for dp, _, fs in os.walk("src"):
        for f in fs:
            if f.endswith(".java"):
                out.append(os.path.join(dp, f))
    return out


def main():
    apply = "--apply" in sys.argv
    text = open(STATIDS, encoding="utf-8").read()
    lines = text.split("\n")

    # ── 解析常量：注释行号 + 声明行号 ──
    consts = {}  # name -> (comment_line_idx, decl_line_idx)
    for i, ln in enumerate(lines):
        m = re.match(r"\s*public static final Identifier ([A-Z][A-Z0-9_]+) = id\(\"([^\"]+)\"\);", ln)
        if m:
            name = m.group(1)
            # 向上找紧邻注释行
            ci = i - 1
            if ci >= 0 and lines[ci].lstrip().startswith("//"):
                consts[name] = (ci, i)
            else:
                consts[name] = (None, i)  # 无注释

    # ── 计算被引用集 ──
    refs = set()
    for p in java_files():
        if p == STATIDS:
            continue
        t = open(p, encoding="utf-8", errors="ignore").read()
        for m in re.finditer(r"StatIds\.([A-Z][A-Z0-9_]+)", t):
            refs.add(m.group(1))
    # 自引用
    for m in re.finditer(r"StatIds\.([A-Z][A-Z0-9_]+)", text):
        refs.add(m.group(1))
    # 资源文件中的 id path 引用
    res_refs = set()
    for dp, _, fs in os.walk("."):
        if "/build/" in dp or "\\build\\" in dp:
            continue
        for f in fs:
            if f.endswith((".json", ".txt", ".snbt", ".mcmeta")):
                try:
                    t = open(os.path.join(dp, f), encoding="utf-8", errors="ignore").read()
                except Exception:
                    continue
                for n, (ci, di) in consts.items():
                    # 取出该常量 id path
                    mm = re.search(r'id\("([^"]+)"\)', lines[di])
                    if mm and ('"%s"' % mm.group(1)) in t:
                        res_refs.add(n)

    dead = sorted(n for n in consts if n not in refs and n not in res_refs)

    # ── 步骤 A：移除 TODO(接线) ──
    a_count = 0
    new_lines = []
    for ln in lines:
        if re.match(r"\s*//\s*TODO\(接线\)", ln):
            ln2 = re.sub(r"//\s*TODO\(接线\)\s*", "// ", ln)
            a_count += 1
            new_lines.append(ln2)
        else:
            new_lines.append(ln)
    lines = new_lines

    # ── 步骤 B：删除死常量（注释行 + 声明行）──
    del_idx = set()
    for n in dead:
        ci, di = consts[n]
        del_idx.add(di)
        if ci is not None:
            del_idx.add(ci)
    # 降序删除，避免索引漂移
    for i in sorted(del_idx, reverse=True):
        del lines[i]
    # 折叠多余空行（3+ 连续空行 -> 1）
    out = "\n".join(lines)
    out = re.sub(r"\n{3,}", "\n\n", out)

    print("步骤 A：移除 TODO(接线) 注释 %d 处" % a_count)
    print("步骤 B：删除死常量 %d 条（含 X_ %d / 非X_ %d）" % (
        len(dead), sum(1 for n in dead if n.startswith("X_")), sum(1 for n in dead if not n.startswith("X_"))))
    print("保留被引用常量 %d 条" % len(consts))
    print("StatIds 行数：%d -> %d" % (len(text.split("\n")), len(lines)))

    if not apply:
        print("[dry-run] 未落地。加 --apply 执行（会先备份 *.step7bak）。")
        return

    # 备份
    bak = STATIDS + ".step7bak"
    if not os.path.exists(bak):
        open(bak, "w", encoding="utf-8").write(text)
    open(STATIDS, "w", encoding="utf-8").write(out)
    print("已备份 -> %s" % bak)
    print("已落地。")


if __name__ == "__main__":
    main()
