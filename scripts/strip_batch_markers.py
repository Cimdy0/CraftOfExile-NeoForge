#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
一次性清理脚本：剥离源码注释中的「Batch NN」批次标记。

背景（用户 2026-09-01 指令）：代码中大量「XX 次修改记录」类备注属变更史噪音，
仅保留必要语义信息即可，踩过的坑沉淀进记忆而非堆在代码里。

处理策略：
  1. **按行作用域**：仅重写「原本含 Batch 标记」的行，其余行原样保留。
  2. **字节级读写**（newline=''）：不做通用换行转换，CRLF/LF 与 BOM 一律原样保留。
  3. 剥离 `[Bb]atch <数字>` 及其紧随的空白/中西文逗号顿号，保留后续语义文本。
  4. 规整剥离产生的标点残留：悬空破折号、空括号、分隔线被吃掉的后空格等。
  5. 纯变更史叙述（"此前…"、"修正…"、"本批…"）不在本脚本范围，另行人工精简。
"""
import re
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent / "src" / "main" / "java"

# 1) 剥 Batch 标记本体（两侧加标识符边界，避免误伤 batch1 / MyBatch2 之类的真实标识符）
#    尾部额外吃掉空白与中西文逗号/顿号，避免留下「（ 补齐的…）」这类残缺括号。
P_BATCH = re.compile(r"(?<![A-Za-z0-9_$])[Bb]atch\s*\d+(?![A-Za-z0-9_$])[ \t]*[，,、]?[ \t]*")

# 2) 仅含批次号的括号紧邻分隔线时，整体让位为一个空格（保住「── 标题 ──」的对称留白）
P_PAREN_BAR = re.compile(r"（[Bb]atch\s*\d+）(?=[─━=])")

# 3) 行内标点规整（按序执行，作用域仅限含过 Batch 的行）
TIDY = [
    (re.compile(r"（\s+"), "（"),                      # 括号内残留前导空白
    (re.compile(r"\s+）"), "）"),                      # 右括号前空白
    (re.compile(r"（\s*）"), ""),                      # 空括号
    (re.compile(r"[：:]\s*[—–]\s*"), "："),            # 冒号后悬空破折号
    (re.compile(r"={2,}\s*[—–-]\s*"), lambda m: "=" * m.group(0).count("=") + " "),
    (re.compile(r"─+\s*[—–-]\s*"), "── "),            # 分隔线后悬空破折号
    (re.compile(r"─+\s*[：:]\s*"), "── "),            # 分隔线后悬空冒号
    (re.compile(r"={2,}\s*[：:]\s*"), lambda m: "=" * m.group(0).count("=") + " "),
    (re.compile(r"//\s*[：:]\s*"), "// "),             # 行注释前导悬空冒号
    (re.compile(r"^(\s*)\*\s*[：:]\s*"), r"\1* "),     # Javadoc 星号行悬空冒号
]


def strip_line(line: str) -> str:
    out = P_PAREN_BAR.sub(" ", line)
    out = P_BATCH.sub("", out)
    for pat, rep in TIDY:
        out = pat.sub(rep, out)
    # 缩进兜底：上述替换不得吃掉行首空白
    indent = line[: len(line) - len(line.lstrip())]
    if indent and not out.startswith(indent):
        out = indent + out.lstrip()
    return out


def main() -> int:
    dry = "--dry" in sys.argv
    files = sorted(ROOT.rglob("*.java"))
    changed, hits = [], 0
    for path in files:
        # newline='' —— 关闭通用换行转换，保证 CRLF/LF 与 BOM 原样进出
        with open(path, "r", encoding="utf-8", newline="") as f:
            src = f.read()
        lines = src.splitlines(keepends=True)
        out_lines, n_file = [], 0
        for line in lines:
            if P_BATCH.search(line):
                new = strip_line(line)
                if new != line:
                    n_file += 1
                    line = new
            out_lines.append(line)
        if not n_file:
            continue
        new_src = "".join(out_lines)
        if new_src != src:
            if not dry:
                with open(path, "w", encoding="utf-8", newline="") as f:
                    f.write(new_src)
            changed.append((path, n_file))
        hits += n_file
    for path, n in changed:
        print(f"  {n:>3}  {path.relative_to(ROOT)}")
    print(f"\nstripped {hits} markers in {len(changed)} files (dry={dry})")
    return 0


if __name__ == "__main__":
    sys.exit(main())
