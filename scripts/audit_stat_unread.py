"""#59 审计：已注册属性（AttributeRegister）是否存在任何引擎消费点。

消费点定义（满足任一即非死线）：
- 代码直读：src 内出现 `AttributeRegister.<NAME>`（getAttributeValue / readInScope / fold / compute 等）
- datagen 接线：src 内出现属性 id 字符串（StatEffectGen / StatConditionGen 按 id 引用）
- 生成 JSON：src/generated 的 stat JSON 内出现该 id

用法：python scripts/audit_stat_unread.py   （项目根目录运行）
仅报告，不改代码。
"""
import os
import re

HERE = os.path.dirname(os.path.abspath(__file__))
PROJ = os.path.dirname(HERE)
ROOT = os.path.join(PROJ, "src", "main", "java", "com", "cimdy", "coe")
GEN = os.path.join(PROJ, "src", "generated", "resources", "data", "coe", "coe", "stat")
ATTR = os.path.join(ROOT, "data", "AttributeRegister.java")

STAT = re.compile(r"public static Holder<Attribute>\s+([A-Z][A-Z0-9_]*)\s*=\s*register\w*\(\s*\"([^\"]+)\"")


def collect_java():
    out = []
    for dp, _, fs in os.walk(ROOT):
        for f in fs:
            if f.endswith(".java"):
                p = os.path.join(dp, f)
                try:
                    out.append(open(p, encoding="utf-8", errors="ignore").read())
                except Exception:
                    pass
    return out


def collect_gen_json():
    out = []
    if not os.path.isdir(GEN):
        return out
    for dp, _, fs in os.walk(GEN):
        for f in fs:
            if f.endswith(".json"):
                p = os.path.join(dp, f)
                try:
                    out.append(open(p, encoding="utf-8", errors="ignore").read())
                except Exception:
                    pass
    return out


def main():
    stats = {}
    for line in open(ATTR, encoding="utf-8", errors="ignore"):
        m = STAT.search(line)
        if m:
            stats[m.group(1)] = m.group(2)

    java = collect_java()
    gen = collect_gen_json()

    dead = []
    for name, sid in sorted(stats.items()):
        direct = any(f"AttributeRegister.{name}" in t for t in java)
        wiring = any(f'"{sid}"' in t or f"'{sid}'" in t for t in java)
        jsonref = any(f'"{sid}"' in t for t in gen)
        if not (direct or wiring or jsonref):
            dead.append((name, sid))

    print(f"AttributeRegister 属性总数：{len(stats)}")
    print(f"疑似 #59（零消费点）：{len(dead)}")
    print("-" * 60)
    for n, s in dead:
        print(f"  {n}  (id=\"{s}\")")
    if not dead:
        print("  (none)")


if __name__ == "__main__":
    main()
