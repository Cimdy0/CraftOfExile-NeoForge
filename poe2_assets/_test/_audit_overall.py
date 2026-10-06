import re, os
from collections import Counter

base = "E:/McModding/Modding/CraftOfExile-NeoForge"
ar_path = os.path.join(base, "src/main/java/com/cimdy/coe/data/AttributeRegister.java")
sg_path = os.path.join(base, "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java")
src_dir = os.path.join(base, "src/main/java")

with open(ar_path, encoding="utf-8") as f:
    ar_lines = f.readlines()
with open(sg_path, encoding="utf-8") as f:
    sg_text = f.read()

decl_re = re.compile(r'public static Holder<Attribute>\s+(\w+)\s*=\s*register(\w+)\(\s*"([^"]+)"\s*,\s*([\d.]+)\s*\)')
props = []  # (name, type, id, lineno)
for i, line in enumerate(ar_lines, 1):
    m = decl_re.search(line)
    if m:
        props.append((m.group(1), m.group(2), m.group(3), i))

sg_refs = set(re.findall(r'AttributeRegister\.(\w+)', sg_text))

read_refs = {}
for root, dirs, files in os.walk(src_dir):
    for fn in files:
        if not fn.endswith(".java"):
            continue
        p = os.path.join(root, fn)
        if p == ar_path:
            continue
        with open(p, encoding="utf-8", errors="ignore") as f:
            for i, line in enumerate(f, 1):
                for m in re.finditer(r'AttributeRegister\.(\w+)', line):
                    name = m.group(1)
                    read_refs.setdefault(name, []).append((p, i))

wave12_start = 264
total = len(props)
written = [p for p in props if p[0] in sg_refs]
written_names = set(p[0] for p in written)
unwritten = [p for p in props if p[0] not in sg_refs]

def has_non_sg_read(name):
    refs = read_refs.get(name, [])
    others = [(f, l) for (f, l) in refs if "gen/stat/StatGen.java" not in f]
    return others

written_with_read = [p for p in written if has_non_sg_read(p[0])]
written_no_read = [p for p in written if not has_non_sg_read(p[0])]

w12 = [p for p in props if p[3] >= wave12_start]
w12_written = [p for p in w12 if p[0] in sg_refs]
w12_unwritten = [p for p in w12 if p[0] not in sg_refs]
w12_before = [p for p in props if p[3] < wave12_start]

print(f"=== AttributeRegister 总属性数: {total} ===")
print(f"注册类型分布: {dict(Counter(p[1] for p in props))}")
print()
print(f"=== StatGen 写点覆盖 ===")
print(f"有 StatGen 写点的属性: {len(written)}/{total}")
print(f"  其中另有非-StatGen 运行时读取端的: {len(written_with_read)}")
print(f"  有写点但无任何非-StatGen 读取端的: {len(written_no_read)}")
print(f"无 StatGen 写点的属性(可能纯声明/空转): {len(unwritten)}")
print()
print(f"=== Wave-12 块 (>=line {wave12_start}) ===")
print(f"块内属性总数: {len(w12)}")
print(f"  有 StatGen 写点: {len(w12_written)}")
print(f"  无 StatGen 写点: {len(w12_unwritten)}")
print(f"块外(<=line {wave12_start-1})属性数: {len(w12_before)}")
print()
print(f"=== 无 StatGen 写点属性清单 ({len(unwritten)}) ===")
for p in sorted(unwritten, key=lambda x: x[3]):
    # 该属性是否有任何读取端引用?
    r = has_non_sg_read(p[0])
    tag = "有读取端" if r else "纯孤立"
    print(f"  L{p[3]:<4} {p[0]:<52} id={p[2]:<42} {tag}")
print()
print(f"=== 有写点但无运行时读取端清单 ({len(written_no_read)}) ===")
for p in sorted(written_no_read, key=lambda x: x[3]):
    print(f"  L{p[3]:<4} {p[0]:<52} id={p[2]:<42}")
