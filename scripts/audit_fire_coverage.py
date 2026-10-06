#!/usr/bin/env python3
"""
Fire-coverage audit for event-driven stat effects (silent dead-line hunter).

A fire-driven effect (GrantOnEventStatEffect / ResourceStatEffect /
StatusStatEffect) requires tag(s) inside its TagExpr. If NO fire anywhere
carries that tag, the effect can never trigger -> silent dead line (#65).

How fires carry tags (per GameEvent.java):
  - GameEvent.emit(key,...)             -> fireTags = tags u {key}
  - GameEvent.fireCaused/fireTaken     -> tags = formTags u {eventName} u {ailmentId, ailmentKey}
  - StatEngine.fire(..., StatContext.of(tags, skill, eventName,..))
So the fireable tag universe =
  * every EventName value used as a fire KEY (caused/taken/ailmentCaused/
    ailmentTaken/emit/ofPlain) or as the eventName arg of StatContext.of,
  * every inline literal / StatTag.X in a fire's tag Set (1st StatContext arg),
  * every SkillEventType value (these flow into fires as skill tags),
  * formTags universe (melee / projectile / arrow),
  * ailment id args of ailmentCaused/ailmentTaken.

Handles BOTH constants and literal strings, balanced-paren over whole files.
"""
import re, os, glob

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge\src\main\java"

def balanced(text, open_pos):
    depth = 0
    i = open_pos
    while i < len(text):
        c = text[i]
        if c == '(':
            depth += 1
        elif c == ')':
            depth -= 1
            if depth == 0:
                return text[open_pos + 1:i]
        i += 1
    return text[open_pos + 1:]

def resolve_const(s, table):
    s = s.strip()
    if s.startswith('"') and s.endswith('"'):
        return [s[1:-1]]
    if s.startswith("StatTag."):
        return [table.get(s[len("StatTag."):], f"<UNK:{s}>")] if s[len("StatTag."):] in table else []
    if s.startswith("EventName."):
        return [table.get(s[len("EventName."):], f"<UNK:{s}>")] if s[len("EventName."):] in table else []
    return []

# ---- 1. enum maps ----
stattag, eventname, skillev, statusid = {}, {}, {}, {}
for p in glob.glob(os.path.join(ROOT, "**", "StatTag.java"), recursive=True):
    for m in re.finditer(r'public static final String (\w+) = "([^"]+)";', open(p, encoding="utf-8").read()):
        stattag[m.group(1)] = m.group(2)
for p in glob.glob(os.path.join(ROOT, "**", "EventName.java"), recursive=True):
    for m in re.finditer(r'public static final String (\w+) = "([^"]+)";', open(p, encoding="utf-8").read()):
        eventname[m.group(1)] = m.group(2)
for p in glob.glob(os.path.join(ROOT, "**", "SkillEventType.java"), recursive=True):
    for m in re.finditer(r'\b(\w+)\("([^"]+)"\)', open(p, encoding="utf-8").read()):
        skillev[m.group(2)] = m.group(1)
for p in glob.glob(os.path.join(ROOT, "**", "StatusDefs.java"), recursive=True):
    txt = open(p, encoding="utf-8").read()
    for m in re.finditer(r'(\w+)\s*=\s*StatusTypeDef\.[a-z]+\(\s*"([^"]+)"', txt):
        statusid[m.group(1)] = m.group(2)

# ---- 2. fire corpus ----
fire_tags = set()
fire_calls = 0

def add_inline(s):
    for lit in re.findall(r'"([^"]+)"', s):
        fire_tags.add(lit)
    for ref in re.findall(r'StatTag\.(\w+)', s):
        if ref in stattag: fire_tags.add(stattag[ref])
    for ref in re.findall(r'EventName\.(\w+)', s):
        if ref in eventname: fire_tags.add(eventname[ref])
    for ref in re.findall(r'StatusDefs\.(\w+)(?:\.id)?', s):
        if ref in statusid: fire_tags.add(statusid[ref])

for p in glob.glob(os.path.join(ROOT, "**", "*.java"), recursive=True):
    text = open(p, encoding="utf-8").read()
    # GameEvent.* fire-key sources
    for m in re.finditer(r'GameEvent\.(caused|taken|ailmentCaused|ailmentTaken|emit|ofPlain)\s*\(', text):
        fire_calls += 1
        args = balanced(text, m.end() - 1)
        parts = [a for a in re.split(r',', args) if a.strip()]
        for a in parts:
            add_inline(a)  # covers eventName (arg3) + ailmentId (arg4 for ailment*/ofPlain)
    # firePair(causedEvent, takenEvent) -> both become fire tags
    for m in re.finditer(r'firePair\s*\(', text):
        fire_calls += 1
        args = balanced(text, m.end() - 1)
        parts = [a for a in re.split(r',', args) if a.strip()]
        for a in parts[3:5]:
            add_inline(a)
    # StatContext.of / StatEngine.fire -> all args may carry inline tags / StatTag / EventName
    for m in re.finditer(r'(StatContext\.of|StatEngine\.fire)\s*\(', text):
        fire_calls += 1
        args = balanced(text, m.end() - 1)
        parts = [a for a in re.split(r',', args) if a.strip()]
        for a in parts:
            add_inline(a)  # tag set, event name, and any nested literal/StatTag/EventName

# skill-event-type + form-tag universes (these flow into fires)
for v in skillev: fire_tags.add(v)
for t in ("melee", "projectile", "arrow"): fire_tags.add(t)

# ---- 3. effect required tags ----
effect_reqs = []
for p in glob.glob(os.path.join(ROOT, "**", "*.java"), recursive=True):
    bn = os.path.basename(p)
    if bn in ("GrantOnEventStatEffect.java", "ResourceStatEffect.java", "StatusStatEffect.java"):
        continue  # class definitions, not effect instances
    text = open(p, encoding="utf-8").read()
    for m in re.finditer(r'new (GrantOnEventStatEffect|ResourceStatEffect|StatusStatEffect)\s*\(', text):
        kind = m.group(1)
        inner = balanced(text, m.end() - 1)
        bl = text[:m.start()].count(chr(10)) + 1
        eid = "?"
        em = re.search(r'(StatEffectIds\.\w+|"[A-Za-z0-9_]+")\s*,', inner)
        if em: eid = em.group(1)
        tags = []
        tx = re.search(r'TagExpr\.of\s*\(', inner)
        if tx:
            ti = balanced(inner, tx.end() - 1)
            if "ALWAYS" in ti:
                tags = ["<ALWAYS>"]
            else:
                for lit in re.findall(r'"([^"]+)"', ti): tags.append(lit)
                for ref in re.findall(r'StatTag\.(\w+)', ti):
                    if ref in stattag: tags.append(stattag[ref])
                for ref in re.findall(r'EventName\.(\w+)', ti):
                    if ref in eventname: tags.append(eventname[ref])
                for ref in re.findall(r'StatusDefs\.(\w+)(?:\.id)?', ti):
                    if ref in statusid: tags.append(statusid[ref])
        if not tags:
            if "ALWAYS" in inner:
                tags = ["<ALWAYS>"]
            else:
                tags = ["<none/ALWAYS>"]
        effect_reqs.append((os.path.basename(p), bl, kind, eid, tags))

# ---- 4. report ----
print(f"[stat] enums: StatTag={len(stattag)} EventName={len(eventname)} SkillEventType={len(skillev)}")
print(f"[stat] fire/emit call sites scanned: {fire_calls}")
print(f"[stat] distinct fireable tags in corpus: {len(fire_tags)}")
print(f"[stat] effects scanned: {len(effect_reqs)}")
print()

dead = []
for (fn, ln, kind, eid, tags) in effect_reqs:
    uncovered = [t for t in tags if t not in fire_tags and t != "<ALWAYS>"]
    status = "OK" if not uncovered else "DEAD?"
    if uncovered: dead.append((fn, ln, kind, eid, uncovered))
    print(f"{status:6} {kind:22} {fn}:{ln:4} {eid:58} tags={tags}")

print()
if dead:
    print(f"=== {len(dead)} effect(s) with UNCOVERED required tag(s) ===")
    for (fn, ln, kind, eid, uncovered) in dead:
        print(f"  {fn}:{ln} {kind} {eid} -> MISSING: {uncovered}")
else:
    print("No uncovered effect tags found.")
