#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Reverse inventory of the STAT CONTEXT SCOPE (reading side) for CraftOfExile-NeoForge.

Goal (user directive "反向盘点读取 context scope"):
  Enumerate every tag that can be PRESENT in a stat context at READ time (the
  scope is entered by DamageEvent / AilmentHelper / StatusEngine / SkillStatScopes
  / GameEvent.fire), build a complete SCOPE DICTIONARY with provenance, then
  reverse-check every StatGen write point's tags against it:
    - ORPHAN_TAG  : write tag never injectable into any scope -> entry can NEVER match -> dead.
    - GLOBAL_ATTR_RISK : attribute is only ever READ outside any scope (scope == EMPTY); a
                    non-empty POSITIVE tag on such an attribute can never match EMPTY scope ->
                    the modifier is silently dead whenever read globally (StatEngine.fold is
                    scope-aware, so EMPTY scope is the global read path).

Reliable signals only:
  * ORPHAN_TAG is deterministic: a tag that is not in the injectable universe (and is not a
    proj:<id> wildcard match) can by construction never be present in any scope.
  * GLOBAL_ATTR_RISK is grounded: we directly find every read site of the 5 curated global
    attributes and check whether any of those reads occurs inside a scope-entry method.

This supersedes _audit_reverse.py (which only scanned literal StatContext.of(...) and found
only 7 scope tags) by parsing the actual scope-ENTRY methods and the dynamic tag families
(status ids, ailment ids, skill names, proj:<id>, event-name keys, archon/form).
"""
import os, re, json, glob

ROOT = "e:/McModding/Modding/CraftOfExile-NeoForge/src/main/java"
TEST = "e:/McModding/Modding/CraftOfExile-NeoForge/poe2_assets/_test"

def read(p):
    with open(p, encoding="utf-8", errors="ignore") as f:
        return f.read()

# ----------------------------------------------------------------------------
# 1. StatTag constants + hierarchy (BUILTIN parent->children)
# ----------------------------------------------------------------------------
def parse_stattag(path):
    text = read(path)
    consts = dict(re.findall(r'public static final String\s+(\w+)\s*=\s*"([^"]*)"', text))
    hierarchy = {}
    for m in re.finditer(r'm\.put\(\s*(\w+)\s*,\s*Set\.of\((.*?)\)\s*\)', text, re.S):
        parent = consts.get(m.group(1))
        if not parent:
            continue
        children = set()
        for part in m.group(2).split(","):
            part = part.strip()
            if not part:
                continue
            if part.startswith('"') and part.endswith('"'):
                children.add(part[1:-1])
            elif part in consts:
                children.add(consts[part])
        if children:
            hierarchy[parent] = children
    return consts, hierarchy

# ----------------------------------------------------------------------------
# 2. EventName constants (+ dynamic gain_/consume_ charge keys)
# ----------------------------------------------------------------------------
def parse_eventname(path):
    text = read(path)
    evs = set(re.findall(r'public static final String\s+\w+\s*=\s*"([^"]*)"', text))
    return evs

# ----------------------------------------------------------------------------
# 3. status/ailment ids (StatusType.register("id", ...))
# ----------------------------------------------------------------------------
def parse_status_ids():
    ids = set()
    for p in glob.glob(os.path.join(ROOT, "**", "*.java"), recursive=True):
        for m in re.finditer(r'StatusType\.(?:register|dynamic|of)\(\s*"([^"]+)"', read(p)):
            ids.add(m.group(1))
    return ids

# ----------------------------------------------------------------------------
# 3b. skill ids (registered via SkillType.register / Skill.register / registerSkill / Skill.of)
# ----------------------------------------------------------------------------
def parse_skill_ids():
    ids = set()
    pats = [
        r'SkillType\.register\(\s*"([^"]+)"',
        r'Skill\.register\(\s*"([^"]+)"',
        r'registerSkill\(\s*"([^"]+)"',
        r'Skill\.of\(\s*"([^"]+)"',
        r'SkillManager\.register\(\s*"([^"]+)"',
    ]
    for p in glob.glob(os.path.join(ROOT, "**", "*.java"), recursive=True):
        t = read(p)
        for pat in pats:
            for m in re.finditer(pat, t):
                ids.add(m.group(1))
    return ids

# ----------------------------------------------------------------------------
# 4. literal scope tags at scope-ENTRY sites + private string consts per file
# ----------------------------------------------------------------------------
def parse_private_consts(text):
    return dict(re.findall(r'private static final String\s+(\w+)\s*=\s*"([^"]*)"', text))

def resolve_tag_token(tok, consts, priv):
    tok = tok.strip()
    if not tok:
        return None
    if tok.startswith('"') and tok.endswith('"'):
        return tok[1:-1]
    m = re.match(r'StatTag\.(\w+)', tok)
    if m:
        return consts.get(m.group(1))
    if tok in consts:
        return consts[tok]
    if tok in priv:
        return priv[tok]
    return None  # variable -> dynamic, not captured as literal

def extract_set_tags(argstr, consts, priv):
    """argstr is the inside of Set.of(...). Returns (tags, had_var)."""
    tags = set()
    had_var = False
    for part in re.split(r',', argstr):
        part = part.strip()
        if not part:
            continue
        if re.match(r'^(new\s|Set\.|List\.|Map\.)', part):
            had_var = True
            continue
        t = resolve_tag_token(part, consts, priv)
        if t is not None:
            tags.add(t)
        else:
            had_var = True
    return tags, had_var

# documented free-string / debuff scope tags that are valid but not captured by the
# StatusType.register("...") scan (custom debuff scopes, status ids registered via
# alternate paths). Verified by grep + source doc comments.
KNOWN_SCOPE_TAGS = {"thorns", "elemental_weakness", "volatility_detonated",
                    "fully_broken_armor", "electrocuted"}

def parse_scope_sites(consts):
    sites = []
    set_re = re.compile(r'Set\.of\(\s*(.*?)\s*\)', re.S)
    for p in glob.glob(os.path.join(ROOT, "**", "*.java"), recursive=True):
        text = read(p)
        priv = parse_private_consts(text)
        for m in re.finditer(r'StatContext\.of\(', text):
            window = text[m.start():m.start()+400]
            sm = set_re.search(window)
            if sm:
                tags, had_var = extract_set_tags(sm.group(1), consts, priv)
                line = text[:m.start()].count("\n") + 1
                sites.append({"file": p, "line": line, "tags": sorted(tags), "had_var": had_var, "kind": "literal"})
            else:
                line = text[:m.start()].count("\n") + 1
                sites.append({"file": p, "line": line, "tags": [], "had_var": True, "kind": "dynamic"})
        for m in re.finditer(r'withScope\(', text):
            window = text[m.start():m.start()+400]
            sm = set_re.search(window)
            if sm:
                tags, had_var = extract_set_tags(sm.group(1), consts, priv)
                line = text[:m.start()].count("\n") + 1
                sites.append({"file": p, "line": line, "tags": sorted(tags), "had_var": had_var, "kind": "withScope"})
    return sites

# ----------------------------------------------------------------------------
# 5. heuristic method-level attribute read-scope map (supplementary, NOT authoritative)
# ----------------------------------------------------------------------------
def parse_attr_read_scopes(consts):
    attr_in_scope = {}
    attr_global = set()
    set_re = re.compile(r'Set\.of\(\s*(.*?)\s*\)', re.S)
    for p in glob.glob(os.path.join(ROOT, "**", "*.java"), recursive=True):
        text = read(p)
        priv = parse_private_consts(text)
        lines = text.splitlines()
        depth = 0
        stack = []
        for line in lines:
            sig = re.search(r'\b([A-Za-z_]\w*)\s*\([^()]*\)\s*\{', line)
            if sig:
                name = sig.group(1)
                if name not in ("if","for","while","switch","try","catch","synchronized","new","return","throw","do"):
                    depth += 1
                    stack.append((name, depth))
            else:
                depth += line.count("{") - line.count("}")
            while stack and depth < stack[-1][1]:
                stack.pop()
            if not stack:
                continue
            cur = stack[-1][0]
            has_scope = bool(re.search(r'StatScopes\.enter|enterMerged|withScope\(', line))
            mset = set_re.search(line)
            if mset:
                tags, _ = extract_set_tags(mset.group(1), consts, priv)
                attr_in_scope.setdefault(cur, set()).update(tags)
            if has_scope:
                for am in re.finditer(r'getAttributeValue\(\s*AttributeRegister\.(\w+)', line):
                    attr_in_scope.setdefault(am.group(1), set())
                for wm in re.finditer(r'withScope\([^,]+,\s*[^,]+,\s*AttributeRegister\.(\w+)', line):
                    attr_in_scope.setdefault(wm.group(1), set())
            else:
                for am in re.finditer(r'getAttributeValue\(\s*AttributeRegister\.(\w+)', line):
                    attr_global.add(am.group(1))
    return attr_in_scope, attr_global

# ----------------------------------------------------------------------------
# 6. authoritative read-site analysis for curated global attributes
# ----------------------------------------------------------------------------
SCOPE_KW = re.compile(
    r'StatScopes\.(enter|enterMerged|mergeWithCurrent)'
    r'|withScope\('
    r'|GameEvent\.(causedTags|takenTags|emit|fire)'
    r'|StatContext\.of'
    r'|StatusEngine\.scoped'
    r'|AilmentHelper\.(withScope|ailmentDurationMul|ailmentThresholdMul|ailmentThresholdAdd|buildupMultiplier|ailmentMagnitude|takenDurationMul|takenMagnitude)'
)

def attr_read_sites(attr):
    """Find every getAttributeValue(AttributeRegister.<attr>) read and whether it sits
    inside a scope-entry method (same method body)."""
    results = []
    needle = f"getAttributeValue(AttributeRegister.{attr}"
    for p in glob.glob(os.path.join(ROOT, "**", "*.java"), recursive=True):
        text = read(p)
        lines = text.splitlines()
        for idx, line in enumerate(lines):
            if needle not in line:
                continue
            # find method-start line (signature with '{')
            start = idx
            lo = max(0, idx - 250)
            for j in range(idx, lo - 1, -1):
                if re.search(r'\b([A-Za-z_]\w*)\s*\([^()]*\)\s*\{', lines[j]):
                    start = j
                    break
            body = "\n".join(lines[start:idx+1])
            in_scope = bool(SCOPE_KW.search(body))
            # method name
            mname = None
            for j in range(idx, lo - 1, -1):
                sig = re.search(r'\b([A-Za-z_]\w*)\s*\([^()]*\)\s*\{', lines[j])
                if sig and sig.group(1) not in ("if","for","while","switch","try","catch","synchronized","new","return","throw","do"):
                    mname = sig.group(1)
                    break
            results.append({"file": os.path.basename(p), "line": idx+1, "method": mname, "in_scope": in_scope})
    return results

# ----------------------------------------------------------------------------
# 7. StatGen write points
# ----------------------------------------------------------------------------
HELPERS = {"inc","add","pctAdd","more","chance","faceMod","valueMod",
           "incSet","addSet","pctAddSet","moreSet","chanceSet"}

def extract_helper_tags(rest, consts):
    tags = []
    seen = set()
    def add(t):
        if t and t not in seen:
            seen.add(t); tags.append(t)
    # not(...) with StatTag.X or bare const
    for m in re.finditer(r'StatTag\.not\(\s*(?:StatTag\.)?(\w+)\s*\)', rest):
        v = consts.get(m.group(1))
        if v: add("!" + v)
    for m in re.finditer(r'StatTag\.not\(\s*"([^"]+)"\s*\)', rest):
        add("!" + m.group(1))
    # direct StatTag.X
    for m in re.finditer(r'(?<!not\()StatTag\.(\w+)', rest):
        v = consts.get(m.group(1))
        if v: add(v)
    # string literals (tags passed as literals)
    for m in re.finditer(r'"([a-z_][a-z0-9_]*)"', rest):
        add(m.group(1))
    # bare uppercase StatTag constants (static-import form), e.g. ATTACK, CRIT
    for m in re.finditer(r'\b([A-Z][A-Z0-9_]+)\b', rest):
        name = m.group(1)
        if name in consts:
            add(consts[name])
    return tags

def parse_write_points(consts):
    p = os.path.join(ROOT, "com/cimdy/coe/data/gen/stat/StatGen.java")
    text = read(p)
    priv = parse_private_consts(text)
    out = []
    lines = text.splitlines()
    for i, line in enumerate(lines):
        hm = re.search(r'\b(' + "|".join(HELPERS) + r')\(\s*map\s*,\s*StatIds\.(\w+)\s*,\s*AttributeRegister\.(\w+)', line)
        if hm:
            attr = hm.group(3)
            # rest of the line after the attr arg
            rest = line[hm.end():]
            tags = extract_helper_tags(rest, consts)
            out.append({"line": i+1, "id": hm.group(2), "attr": attr, "tags": tags, "kind": "helper:"+hm.group(1)})
            continue
        if "ModifierStatEffect.entry(" in line or "new Stat(" in line:
            am = re.search(r'AttributeRegister\.(\w+)', line)
            if am:
                attr = am.group(1)
                # only inspect the portion AFTER the attribute argument, so the
                # AttributeRegister.X token itself is never mistaken for a tag
                rest = line[am.end():]
                # extract tags from a Set.of(...) on the line if present
                sm = re.search(r'Set\.of\(\s*(.*?)\s*\)', rest)
                if sm:
                    tags, _ = extract_set_tags(sm.group(1), consts, priv)
                else:
                    tags = extract_helper_tags(rest, consts)
                out.append({"line": i+1, "id": "?", "attr": attr, "tags": sorted(tags), "kind": "direct"})
    return out

# ----------------------------------------------------------------------------
# 8. scope matching (mirror StatTag.match / TagExpr semantics)
# ----------------------------------------------------------------------------
def tag_in_scope(tag, S, hierarchy):
    if tag in S:
        return True
    if tag.startswith("proj:"):
        return any(t.startswith("proj:") for t in S)
    children = hierarchy.get(tag)
    if children:
        return any(c in S for c in children)
    return False

def is_orphan(tag, injectable, hierarchy):
    base = tag[1:] if tag.startswith("!") else tag
    if base.startswith("proj:"):
        return False  # wildcard family always matchable at read time
    if base in injectable:
        # parent tags (elemental etc.) are matchable via children containment -> not orphan
        return False
    # parent-tag-like? if base is a parent in hierarchy, it's matchable when any child present
    if base in hierarchy:
        return False
    return True

# ----------------------------------------------------------------------------
# main
# ----------------------------------------------------------------------------
def main():
    stattag_consts, hierarchy = parse_stattag(os.path.join(ROOT, "com/cimdy/coe/data/codec/support/stat/StatTag.java"))
    event_tags = parse_eventname(os.path.join(ROOT, "com/cimdy/coe/event/EventName.java"))
    status_ids = parse_status_ids()
    skill_ids = parse_skill_ids()
    scope_sites = parse_scope_sites(stattag_consts)
    attr_in_scope, attr_global = parse_attr_read_scopes(stattag_consts)
    write_points = parse_write_points(stattag_consts)

    # Build injectable tag universe (the SCOPE DICTIONARY core)
    literal_scope_tags = set()
    for s in scope_sites:
        for t in s["tags"]:
            literal_scope_tags.add(t)
    proj_family = {"proj:*"}  # wildcard placeholder (any proj:<id> matchable)
    skill_type_tags = {"warcry","curse","mark","storm","plant","banner","command","offering",
                       "seal","meta","hazard","grenade","corpse","recently_consumed","herald",
                       "channeling","spell","attack",
                       # skill-type / delivery / weapon scope tags injected by SkillStatScopes + DamageEvent
                       "slam","bow","crossbow","spear","mace","melee","ranged","area","beam",
                       "aura","self","projectile","arrow","minion","jewellery"}
    # parent tags are matchable via child containment -> include all parents from hierarchy
    parent_tags = set(hierarchy.keys())
    injectable = (set(stattag_consts.values())
                  | event_tags | status_ids | skill_ids
                  | proj_family | skill_type_tags | parent_tags
                  | KNOWN_SCOPE_TAGS | {"archon","form"})

    # ---- ORPHAN detection (deterministic) ----
    orphan_tags = {}   # tag -> [write point refs]
    for wp in write_points:
        for t in wp["tags"]:
            if is_orphan(t, injectable, hierarchy):
                base = t[1:] if t.startswith("!") else t
                orphan_tags.setdefault(base, []).append(f"{wp['id']}@{wp['line']}")

    # ---- curated GLOBAL-READ-ATTR review (authoritative, read-site grounded) ----
    GLOBAL_ATTRS = ["BLOCK_CHANCE", "ACCURACY", "EVASION", "MAX_RAGE", "MAX_ENERGY_SHIELD"]
    global_review = {}
    for attr in GLOBAL_ATTRS:
        sites = attr_read_sites(attr)
        any_scope = any(s["in_scope"] for s in sites)
        wps = [w for w in write_points if w["attr"] == attr and w["tags"]]
        positive = [w for w in wps if any(not t.startswith("!") for t in w["tags"])]
        # risk: read only globally (never under a scope) AND has positive-tagged write points
        risk = (not any_scope) and bool(positive)
        global_review[attr] = {
            "read_sites": sites,
            "read_under_scope": any_scope,
            "write_points_with_tags": [f"{w['id']}@{w['line']} tags={w['tags']}" for w in wps],
            "positive_tagged_write_points": [f"{w['id']}@{w['line']} tags={w['tags']}" for w in positive],
            "risk": risk,
        }

    # ---- supplementary: unused literal scope tags (capacity wired but unwritten) ----
    used_tags = set()
    for wp in write_points:
        for t in wp["tags"]:
            used_tags.add(t[1:] if t.startswith("!") else t)
    unused = sorted(t for t in literal_scope_tags if t not in used_tags and t not in injectable)

    # ---- report ----
    L = []
    A = L.append
    A("# Reverse Scope Inventory & Write-Point Alignment Audit")
    A("")
    A("> Reliable signals only: ORPHAN_TAG (deterministic) + GLOBAL_ATTR_RISK (read-site grounded).")
    A("> The old DEAD_GLOBAL / DEAD_MISALIGNED lexical heuristics are removed because scope is")
    A("> ENTERED in a caller (DamageEvent) but READ in a callee (DamagePipeline via LivingAttributeValue.fold);")
    A("> a method-level lexical scan cannot see that wiring.")
    A("")
    A("## 1. SCOPE DICTIONARY (every tag injectable into a stat context at READ time)")
    A("")
    A(f"- StatTag constant values: {len(stattag_consts)} (+ {len(hierarchy)} parent tags with child-containment)")
    A(f"- EventName constant values: {len(event_tags)} (+ dynamic gain_/consume_<charge> keys)")
    A(f"- Status/Ailment ids (StatusType.register/dynamic/of): {len(status_ids)}")
    A(f"- Skill ids (SkillType/Skill.register/registerSkill/Skill.of): {len(skill_ids)}")
    A(f"- Literal scope tags found at entry sites: {len(literal_scope_tags)}")
    A(f"- KNOWN_SCOPE_TAGS (free-string debuff scopes): {sorted(KNOWN_SCOPE_TAGS)}")
    A(f"- Private consts in scope: archon, form")
    A(f"- **TOTAL injectable universe size: {len(injectable)}**")
    A("")
    A("### Literal scope-entry sites (provenance)")
    A("")
    by_file = {}
    for s in scope_sites:
        if s["kind"] == "dynamic":
            by_file.setdefault(os.path.basename(s["file"]), []).append(
                f"  L{s['line']:>5}  [DYNAMIC scope: tags built from variables/status.id/skill.id]")
        else:
            by_file.setdefault(os.path.basename(s["file"]), []).append(
                f"  L{s['line']:>5}  tags={s['tags']}")
    for f, items in sorted(by_file.items()):
        A(f"- **{f}** ({len(items)} sites)")
        for it in items:
            A(it)
    A("")
    A("### Dynamic scope families (valid in scope, not all literal)")
    A("")
    A("- `proj:<id>` (projectile instance tags, e.g. proj:skill_arrow) — wildcard `proj:*` matches any at read time")
    A("- skill names (`<skillname>` from skill id path) — injected via SkillStatScopes + DamageEvent skill path")
    A("- status/ailment ids (sample): " + ", ".join(sorted(status_ids)[:30]) + (" ..." if len(status_ids) > 30 else ""))
    A("- skill-type tags: " + ", ".join(sorted(skill_type_tags)))
    A("- event-name keys (also added as tags via GameEvent.causedTags/takenTags): " + ", ".join(sorted(event_tags)[:30]) + (" ..." if len(event_tags) > 30 else ""))
    A("")
    A("## 2. ORPHAN TAGS (deterministic — write tag never injectable into ANY scope => entry is DEAD)")
    A("")
    A(f"- Total write points parsed: {len(write_points)}")
    A(f"- Write points with no tags (PASSIVE / always-on): {sum(1 for w in write_points if not w['tags'])}")
    A(f"- ORPHAN_TAG distinct tags: {len(orphan_tags)}")
    A(f"- ORPHAN_TAG occurrences (write-point references): {sum(len(v) for v in orphan_tags.values())}")
    A("")
    if orphan_tags:
        for t, wps in sorted(orphan_tags.items()):
            A(f"- `{t}`  used by {len(wps)} write point(s): {wps[:12]}")
    else:
        A("- none")
    A("")
    A("## 3. GLOBAL-READ-ATTR REVIEW (StatEngine.fold is scope-aware; EMPTY scope = global read path)")
    A("")
    A("Attributes below are READ only outside any scope. A non-empty POSITIVE tag on such an")
    A("attribute can never match the EMPTY scope -> the modifier is silently dead at its only read path.")
    A("`not(X)` tags still match EMPTY (X absent), so they are safe on global attributes.")
    A("")
    for attr in GLOBAL_ATTRS:
        r = global_review[attr]
        verdict = "**RISK**" if r["risk"] else ("read-under-scope (safe)" if r["read_under_scope"] else "global-only, no positive tags (safe)")
        A(f"### {attr}  —  {verdict}")
        A(f"- read sites: {len(r['read_sites'])}; read_under_scope={r['read_under_scope']}")
        for s in r["read_sites"]:
            A(f"    - {s['file']}:L{s['line']}  method={s['method']}  in_scope={s['in_scope']}")
        if r["write_points_with_tags"]:
            A(f"- write points carrying tags ({len(r['write_points_with_tags'])}):")
            for w in r["write_points_with_tags"]:
                A(f"    - {w}")
        else:
            A("- write points carrying tags: none")
        if r["positive_tagged_write_points"]:
            A(f"- **POSITIVE-tagged write points (dead under global read):**")
            for w in r["positive_tagged_write_points"]:
                A(f"    - {w}")
        A("")
    A("## 4. SUPPLEMENTARY (heuristic, method-level — NOT authoritative)")
    A("")
    A(f"- attributes read inside a scope method (heuristic): {len(attr_in_scope)}")
    A(f"- attributes read only globally (heuristic): {len(attr_global - set(attr_in_scope))}")
    A(f"- UNUSED literal scope tags (injectable but never used by a write point): {unused if isinstance(unused,list) else sorted(unused)}")
    A("")
    A("### Attributes read ONLY globally (heuristic; review before adding restrictive tags)")
    A("")
    for a in sorted(attr_global - set(attr_in_scope)):
        A(f"- {a}")
    A("")

    report = "\n".join(L)
    os.makedirs(TEST, exist_ok=True)
    with open(os.path.join(TEST, "reverse_scope_audit.md"), "w", encoding="utf-8") as f:
        f.write(report)
    with open(os.path.join(TEST, "reverse_scope_audit.json"), "w", encoding="utf-8") as f:
        json.dump({
            "scope_dictionary_size": len(injectable),
            "stattag_consts": len(stattag_consts),
            "event_tags": len(event_tags),
            "status_ids": len(status_ids),
            "skill_ids": len(skill_ids),
            "literal_scope_tags": sorted(literal_scope_tags),
            "write_points": write_points,
            "orphan_tags": orphan_tags,
            "global_attr_review": global_review,
            "attr_in_scope_heuristic": {k: sorted(v) for k, v in attr_in_scope.items()},
            "attr_global_heuristic": sorted(attr_global),
            "unused_literal_scope_tags": unused,
        }, f, ensure_ascii=False, indent=1)
    print(report)
    print("\n[written]", os.path.join(TEST, "reverse_scope_audit.md"))

if __name__ == "__main__":
    main()
