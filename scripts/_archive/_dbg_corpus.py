import re, os, glob

ROOT = r"src/main/java"

def balanced(t, o):
    d = 0
    i = o
    while i < len(t):
        c = t[i]
        if c == '(':
            d += 1
        elif c == ')':
            d -= 1
            if d == 0:
                return t[o + 1:i]
        i += 1
    return t[o + 1:]

stattag = {}
eventname = {}
for p in glob.glob(os.path.join(ROOT, "**", "StatTag.java"), recursive=True):
    for m in re.finditer(r'public static final String (\w+) = "([^"]+)";', open(p, encoding="utf-8").read()):
        stattag[m.group(1)] = m.group(2)
for p in glob.glob(os.path.join(ROOT, "**", "EventName.java"), recursive=True):
    for m in re.finditer(r'public static final String (\w+) = "([^"]+)";', open(p, encoding="utf-8").read()):
        eventname[m.group(1)] = m.group(2)

ft = set()
def add(s):
    for l in re.findall(r'"([^"]+)"', s):
        ft.add(l)
    for r in re.findall(r'StatTag\.(\w+)', s):
        if r in stattag:
            ft.add(stattag[r])
    for r in re.finditer(r'EventName\.(\w+)', s):
        if r.group(1) in eventname:
            ft.add(eventname[r.group(1)])

for p in glob.glob(os.path.join(ROOT, "**", "*.java"), recursive=True):
    t = open(p, encoding="utf-8").read()
    for m in re.finditer(r'GameEvent\.(caused|taken|ailmentCaused|ailmentTaken|emit|ofPlain)\s*\(', t):
        args = balanced(t, m.end() - 1)
        for a in [x for x in re.split(r',', args) if x.strip()]:
            add(a)
    for m in re.finditer(r'(StatContext\.of|StatEngine\.fire)\s*\(', t):
        args = balanced(t, m.end() - 1)
        parts = [x for x in re.split(r',', args) if x.strip()]
        if parts:
            add(parts[0])
        if len(parts) >= 3:
            add(parts[2])
for v in ('melee', 'projectile', 'arrow'):
    ft.add(v)

print('warcry/flask/shield/use_ tags in corpus:')
print(sorted([x for x in ft if ('warcry' in x or 'flask' in x or 'shield' in x or x.startswith('use_'))]))
print('use_warcry present:', 'use_warcry' in ft)
print('use_warcry_enemy present:', 'use_warcry_enemy' in ft)
print('use_flask present:', 'use_flask' in ft)
print('shield_block present:', 'shield_block' in ft)
print('caused_block present:', 'caused_block' in ft)
print('caused_ailment present:', 'caused_ailment' in ft)
print('minion_revived present:', 'minion_revived' in ft)
print('ignite present:', 'ignite' in ft)
print('poison present:', 'poison' in ft)
