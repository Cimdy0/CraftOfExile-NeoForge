# -*- coding: utf-8 -*-
"""Step 7 步骤 C：删除 AttributeRegister 零引用属性（含 _N 占位）。
默认 dry-run；--apply 落地（先 cp 备份 *.step7bak）。用户裁定：全删（C2）。
安全：额外校验属性 registry path 是否在资源文件中被字符串引用。"""
import re, os, sys

ATTRREG = "src/main/java/com/cimdy/coe/data/AttributeRegister.java"


def java_files():
    out = []
    for dp, _, fs in os.walk("src"):
        for f in fs:
            if f.endswith(".java"):
                out.append(os.path.join(dp, f))
    return out


def main():
    apply = "--apply" in sys.argv
    text = open(ATTRREG, encoding="utf-8").read()
    lines = text.split("\n")

    decl_re = re.compile(r"public static (?:final )?Holder<Attribute>\s+([A-Z][A-Z0-9_]+)\s*=\s*(\w+)\(\"([^\"]+)\"")
    attrs = {}  # name -> (line_idx, path)
    for i, ln in enumerate(lines):
        m = decl_re.search(ln)
        if m:
            attrs[m.group(1)] = (i, m.group(3))

    # 静态引用集
    refs = set()
    for p in java_files():
        if "AttributeRegister.java" in p:
            continue
        t = open(p, encoding="utf-8", errors="ignore").read()
        for m in re.finditer(r"AttributeRegister\.([A-Z][A-Z0-9_]+)", t):
            refs.add(m.group(1))

    # 资源文件按 registry path 引用
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
                for n, (i, path) in attrs.items():
                    if '"%s"' % path in t or "'%s'" % path in t:
                        res_refs.add(n)

    dead = sorted(n for n in attrs if n not in refs and n not in res_refs)

    # 待删行：声明行 + 其独占的 /** */ javadoc 块（不含共享 section divider）
    del_idx = set()
    for n in dead:
        i, path = attrs[n]
        del_idx.add(i)
        # 向上检测 javadoc 块：上一行含 */ 则说明上方有 /** 块
        j = i - 1
        if j >= 0 and "*/" in lines[j]:
            # 向上找块起点 /**
            k = j
            while k >= 0 and "/**" not in lines[k] and "/*" not in lines[k]:
                k -= 1
            if k >= 0 and ("/**" in lines[k] or "/*" in lines[k]):
                # 块起点到 j 全删（javadoc 不含其他声明，安全）
                for x in range(k, j + 1):
                    del_idx.add(x)

    # 降序删除
    for i in sorted(del_idx, reverse=True):
        del lines[i]
    out = "\n".join(lines)
    out = re.sub(r"\n{3,}", "\n\n", out)

    print("步骤 C：删除死属性 %d 条（静态引用 %d + 资源引用 %d 已排除）" % (len(dead), len(refs), len(res_refs)))
    print("涉及删除行数（含 javadoc）：%d" % len(del_idx))
    print("AttributeRegister 行数：%d -> %d" % (len(text.split("\n")), len(lines)))

    if not apply:
        print("[dry-run] 未落地。加 --apply 执行（会先备份 *.step7bak）。")
        return

    bak = ATTRREG + ".step7bak"
    if not os.path.exists(bak):
        open(bak, "w", encoding="utf-8").write(text)
    open(ATTRREG, "w", encoding="utf-8").write(out)
    print("已备份 -> %s" % bak)
    print("已落地。")


if __name__ == "__main__":
    main()
