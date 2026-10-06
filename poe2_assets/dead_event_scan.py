"""找出「已声明但全工程无发射点」的事件名常量 —— 这类缺口会让既有接线静默失效。

发射点判定：Event.record( / GameEvent.emit( / refreshTimer( 中出现该常量或其字符串字面量。
输出 poe2_assets/_dead_events.txt
"""
import re, os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
EN = os.path.join(ROOT, "src/main/java/com/cimdy/coe/event/EventName.java")

src = open(EN, encoding="utf-8").read()
names = re.findall(r'public static final String ([A-Z0-9_]+) = "([a-z0-9_]+)"', src)
print("EventName constants:", len(names))

# 收集全工程源码
allsrc = []
for dp, dn, fn in os.walk(os.path.join(ROOT, "src/main/java")):
    for f in fn:
        if f.endswith(".java"):
            allsrc.append(os.path.join(dp, f))

# 发射点判定：Event.record( / GameEvent.emit( / fire* 中出现常量名
emit_pat = re.compile(r'(Event\.record\s*\(|GameEvent\.emit\s*\(|refreshTimer\s*\()')
used_as_tag = re.compile(r'EventName\.([A-Z0-9_]+)')

lines = []
dead = []
live = []
for const, val in names:
    emit_hits = []
    tag_hits = 0
    for p in allsrc:
        if p.endswith("EventName.java"):
            continue
        t = open(p, encoding="utf-8").read()
        for m in emit_pat.finditer(t):
            # 取该调用后 400 字符的片段，看是否含本常量或字面量
            seg = t[m.start():m.start() + 400]
            if const in seg or f'"{val}"' in seg:
                emit_hits.append(os.path.basename(p))
        tag_hits += len(re.findall(r'EventName\.' + const + r'\b', t))
    if emit_hits:
        live.append((const, val, sorted(set(emit_hits))[:3], tag_hits))
    else:
        dead.append((const, val, tag_hits))

lines.append(f"EventName total = {len(names)}   live(有发射点) = {len(live)}   dead(无发射点) = {len(dead)}")
lines.append("")
lines.append("=== DEAD：已声明但无发射点（按被引用次数降序；引用>0 即有接线在等它）===")
for const, val, n in sorted(dead, key=lambda x: -x[2]):
    lines.append(f"  {const:44s} \"{val}\"   refs_in_gen={n}")
lines.append("")
lines.append("=== LIVE 抽样（前 25）===")
for const, val, files, n in live[:25]:
    lines.append(f"  {const:44s} refs={n}  emit@{','.join(files)}")

out = os.path.join(ROOT, "poe2_assets/_dead_events.txt")
open(out, "w", encoding="utf-8").write("\n".join(lines))
print("written _dead_events.txt  dead=", len(dead))
for const, val, n in sorted(dead, key=lambda x: -x[2])[:20]:
    print(f"   {const:40s} refs={n}")
