"""列出「零发射点标签」各自被哪些已接线条目使用，评估影响面。"""
import re
import os

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                    '..', 'src', 'main', 'java', 'com', 'cimdy', 'coe')
GEN = os.path.join(ROOT, 'data', 'gen', 'stat')
WIR = os.path.join(GEN, 'wirings')


def read(p):
    with open(p, encoding='utf-8') as f:
        return f.read()


ORPHAN = ['GRENADE', 'WITHERED', 'BEAST', 'DEFLECT', 'BOLT', 'ICE_CRYSTAL']

# 标签 -> 带该标签的 EFF 常量名
tag2eff = {t: [] for t in ORPHAN}
for line in read(os.path.join(GEN, 'StatEffectGen.java')).splitlines():
    m = re.match(r'\s*m\.put\(StatEffectIds\.([A-Z0-9_]+),.*TagExpr\.of\(Set\.of\(([^)]*)\)\)\)', line)
    if not m:
        continue
    eff, args = m.group(1), m.group(2)
    for t in ORPHAN:
        if re.search(r'StatTag\.' + t + r'\b', args):
            tag2eff[t].append(eff)

# EFF -> 接线条目
eff2stats = {}
for fn in sorted(os.listdir(WIR)):
    if not fn.endswith('.java'):
        continue
    txt = read(os.path.join(WIR, fn))
    # 跨行 map.put(...) 合并后再匹配
    flat = re.sub(r'\s+', ' ', txt)
    for m in re.finditer(r'map\.put\(\s*StatIds\.([A-Z0-9_]+)\s*,.*?StatEffectIds\.([A-Z0-9_]+)\)', flat):
        eff2stats.setdefault(m.group(2), []).append((m.group(1), fn))

for t in ORPHAN:
    effs = tag2eff[t]
    print(f"\n=== StatTag.{t}  ({len(effs)} EFF) ===")
    for e in effs:
        stats = eff2stats.get(e, [])
        if stats:
            for s, fn in stats:
                print(f"  {e}")
                print(f"      -> {s}   [{fn}]")
        else:
            print(f"  {e}")
            print(f"      -> (无接线 / 未被使用)")
