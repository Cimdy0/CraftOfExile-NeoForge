"""导出全部 EFF 的 (属性, op, 标签集) 三元组，供按族查询可复用落点。

输出 poe2_assets/_eff_index.txt
"""
import re, os, collections

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
GEN = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatEffectGen.java")

src = open(GEN, encoding="utf-8").read()

# 匹配 m.put(StatEffectIds.XXX, <构造>);  构造可能跨行
pat = re.compile(r'm\.put\(\s*(StatEffectIds\.[A-Z0-9_]+)\s*,\s*(.*?)\);\s*\n', re.DOTALL)

attr = re.compile(r'AttributeRegister\.([A-Z0-9_]+)')
op = re.compile(r'AttributeModifier\.Operation\.([A-Z_]+)')
tag = re.compile(r'StatTag\.([A-Z0-9_]+)')
rawtag = re.compile(r'"([a-z0-9_]+)"')

rows = []
for m in pat.finditer(src):
    eid = m.group(1)
    body = m.group(2)
    a = attr.search(body)
    o = op.search(body)
    tags = sorted(set(tag.findall(body)))
    body_nostat = tag.sub("", body)
    raws = sorted(set(rawtag.findall(body_nostat)))
    rows.append((eid, a.group(1) if a else "-", o.group(1) if o else "-", tags, raws))

print("EFF parsed:", len(rows))

# 按标签建索引
by_tag = collections.defaultdict(list)
for eid, a, o, tags, raws in rows:
    for t in tags:
        by_tag[t.lower()].append((eid, a, o))
    for r in raws:
        by_tag[r].append((eid, a, o))

out = []
out.append(f"EFF total = {len(rows)}")
out.append("")
TARGET = ["mark", "offering", "seal", "totem", "grenade", "banner", "companion",
          "curse", "warcry", "herald", "command", "puppet_master", "storm", "plant",
          "corpse", "channeling", "impale", "hazard", "detonator", "echo",
          "ancestral", "trigger", "ballista", "brand", "mine", "trap", "golem",
          "minion", "remnant", "infusion", "pin", "culling"]
for t in TARGET:
    items = by_tag.get(t, [])
    out.append(f"\n## tag={t}  ({len(items)})")
    for eid, a, o in items:
        out.append(f"   {eid[len('StatEffectIds.'):]}")
        out.append(f"        attr={a}  op={o}")

open(os.path.join(ROOT, "poe2_assets/_eff_index.txt"), "w", encoding="utf-8").write("\n".join(out))
print("written _eff_index.txt")
