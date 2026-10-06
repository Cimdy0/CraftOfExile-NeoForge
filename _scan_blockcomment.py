import os, re, io

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge\src\main\java"

# 代码行特征：出现在块注释内部说明被吞掉了真实语句
CODE_PAT = re.compile(r"\b(m\.put|map\.put|return\s|new\s+[A-Z]\w*\(|[A-Za-z_]\w*\s*=\s)\b")

def scan_text(text):
    """返回 (start_line, end_line, swallowed_code_lines) 列表，块注释跨行且内部含代码特征"""
    out = []
    lines = text.split("\n")
    i = 0
    n = len(lines)
    in_block = False
    start = 0
    buf = []
    while i < n:
        line = lines[i]
        if not in_block:
            idx = line.find("/*")
            if idx >= 0:
                rest = line[idx + 2:]
                close = rest.find("*/")
                if close < 0:
                    in_block = True
                    start = i
                    buf = [line[idx:]]
                else:
                    # 单行块注释，跳过；检查同行后面是否还有新的 /* 开启
                    pass
        else:
            buf.append(line)
            close = line.find("*/")
            if close >= 0:
                body = buf[:-1] + [line[:close + 2]]
                swallowed = [b for b in body if CODE_PAT.search(b) and not b.lstrip().startswith("*")]
                # 只看像代码的：以 m.put( 或 map.put( 开头的
                real = [b for b in swallowed if re.search(r"\bm\.put\(|\bmap\.put\(", b)]
                if real:
                    out.append((start + 1, i + 1, real))
                in_block = False
                buf = []
        i += 1
    return out

hits = 0
for dirpath, dirnames, filenames in os.walk(ROOT):
    for fn in filenames:
        if not fn.endswith(".java"):
            continue
        p = os.path.join(dirpath, fn)
        try:
            text = open(p, "r", encoding="utf-8", errors="replace").read()
        except Exception as e:
            print("READFAIL", p, e)
            continue
        try:
            res = scan_text(text)
        except Exception as e:
            print("SCANFAIL", p, e)
            continue
        for s, e, real in res:
            hits += 1
            print("HIT %s  lines %d-%d" % (p, s, e))
            for b in real:
                print("    | " + b.strip()[:150])
print("TOTAL_HITS", hits)
