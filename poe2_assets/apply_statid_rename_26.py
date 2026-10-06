# -*- coding: utf-8 -*-
"""
STATID-RENAME-26
Applies, in order:
  STEP 1 - 14 corpus renames (path + Java constant) from _rename_table.json
  STEP 2 - 19 corrupted keystone-named constants: DELETE / MERGE / RENAME
  STEP 3 - retire 6 hand-forged bullet ids, add 6 pipeline ids
  STEP 4 - add every path in _rename_table.json.added
  STEP 5 - re-emit the original corpus text comment above every field
  STEP 6 - regenerate KeystoneNodes01 StatList arguments from node_stat_map.json
"""
import json
import os
import re
import sys

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
os.chdir(REPO)

STATIDS = "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"
KEYSTONE = "src/main/java/com/cimdy/coe/data/gen/talent/node/keystone/KeystoneNodes01.java"

APPLY = "--apply" in sys.argv

DRY = []


def log(*a):
    print(*a)


def read_text(p):
    """Read without newline translation so we can preserve CRLF vs LF per file."""
    with open(p, encoding="utf-8", newline="") as fh:
        return fh.read()


def write_text(p, text):
    with open(p, "w", encoding="utf-8", newline="") as fh:
        fh.write(text)


def load(p):
    raw = read_text(p)
    crlf = "\r\n" in raw
    return raw.replace("\r\n", "\n"), crlf


def save(p, text, crlf):
    write_text(p, text.replace("\n", "\r\n") if crlf else text)


def san(x):
    return re.sub(r"[^A-Z0-9]", "_", x.upper())


# ---------------------------------------------------------------- ground truth
nsm = json.load(open("poe2_assets/node_stat_map.json", encoding="utf-8"))
sti = json.load(open("poe2_assets/stat_text_index.json", encoding="utf-8"))
rt = json.load(open("poe2_assets/_rename_table.json", encoding="utf-8"))
RENAMES = rt["rename"]
ADDED = rt["added"]

# ------------------------------------------------------- java file enumeration
JAVA = []
for dp, dn, fn in os.walk("src"):
    for f in fn:
        if f.endswith(".java"):
            JAVA.append(os.path.join(dp, f).replace("\\", "/"))

# =============================================================== parse StatIds
src0, SRC_CRLF = load(STATIDS)
src = src0
lines = src.split("\n")
cls_i = next(i for i, l in enumerate(lines) if l.startswith("public final class"))
mth_i = next(i for i, l in enumerate(lines) if "static Identifier id(String path)" in l)
# the javadoc block for id() starts 10 lines earlier
tail_i = mth_i - 10
while tail_i > 0 and not lines[tail_i].strip().startswith("/**"):
    tail_i -= 1

prefix = lines[:cls_i + 1]
suffix = lines[tail_i:]
body = lines[cls_i + 1:tail_i]

FIELD_RE = re.compile(r'^(\s*)public static final Identifier (\w+) = id\("([^"]+)"\);\s*$')

entries = []          # {section:[], keep:[], const, path}
pending = []
for l in body:
    if not l.strip():
        continue
    m = FIELD_RE.match(l)
    if m:
        sec = [c for c in pending if c.strip().startswith("// ===")]
        keep = [c for c in pending
                if c.strip().startswith("// stat_entry/")
                or c.strip().startswith("// explicit code-referenced")]
        entries.append({"section": sec, "keep": keep, "const": m.group(2), "path": m.group(3)})
        pending = []
    else:
        pending.append(l)
orphan_tail = pending
if orphan_tail:
    log("WARN orphan trailing lines:", orphan_tail)

before_count = len(entries)
by_const = {e["const"]: e for e in entries}
by_path = {e["path"]: e for e in entries}
log("parsed fields:", before_count)

# ============================================================ operation ledger
repoint = {}   # OLD_CONST -> NEW_CONST   (references get rewritten)
literals = {}  # "old path" -> "new path" (string literals get rewritten)
removed = set()  # constants whose field disappears from StatIds entirely
kill_put = set()  # constants whose map.put(...) statement must be DELETED
#   (= step2 + step3 only: their targets already carry canonical wiring,
#    so re-pointing the old put would register the same key twice)
added_paths = []  # new (const, path) to append


def do_rename(old_const, new_const):
    if old_const != new_const:
        repoint[old_const] = new_const
    removed.add(old_const)


# ---------------------------------------------------------------- STEP 1
step1_applied = 0
for old, new in RENAMES.items():
    old_c, new_c = san(old), san(new)
    if old in by_path:
        if new in by_path:
            # target already present -> pure merge onto the existing constant
            do_rename(old_c, by_path[new]["const"])
        else:
            e = by_path[old]
            e["path"] = new
            e["const"] = new_c
            by_path.pop(old)
            by_path[new] = e
            by_const.pop(old_c)
            by_const[new_c] = e
            do_rename(old_c, new_c)
        literals[old] = new
        step1_applied += 1
    else:
        log("STEP1 WARN old path absent:", old)
log("STEP1 renames applied:", step1_applied)

# ---------------------------------------------------------------- STEP 2
STEP2 = [
    # (old const, target const, mode)
    ("AVATAR_OF_FIRE", None, "delete"),
    ("BLOOD_MAGIC_ENERGY_TO_LIFE", "SKILL_MANA_COSTS_STAT_CONVERSION_TO_HEALTH_COSTS", "merge"),
    ("BLOOD_MAGIC_MANA_TO_LIFE", "SKILL_MANA_COSTS_STAT_CONVERSION_TO_HEALTH_COSTS", "merge"),
    ("CHAOS_INOCULATION_IMMUNE_BLEED", "IMMUNE_TO_CHAOS_DAMAGE_AND_BLEEDING", "merge"),
    ("CHAOS_INOCULATION_IMMUNE_CHAOS", "IMMUNE_TO_CHAOS_DAMAGE_AND_BLEEDING", "merge"),
    ("CHAOS_INOCULATION_MAX_HEALTH", "MAX_HEALTH_IS", "rename"),
    ("BLACKFLAME_COVENANT_FIRE_TO_CHAOS", "FIRE_SPELLS_CONVERSION_OF_FIRE_DAMAGE_TO_CHAOS_DAMAGE", "rename"),
    ("ELDRITCH_BATTERY_ES_TO_MANA", "STAT_CONVERSION_OF_MAX_ENERGY_SHIELD_TO_MAX_MANA", "rename"),
    ("GLANCING_BLOWS_DEFLECT_LUCKY", "CHANCE_TO_DEFLECT_IS_LUCKY", "rename"),
    ("GLANCING_BLOWS_EVADE_UNLUCKY", "CHANCE_TO_EVASION_IS_UNLUCKY", "rename"),
    ("IRON_REFLEXES_EVASION_TO_ARMOR", "STAT_CONVERSION_ALL_EVASION_TO_ARMOR", "rename"),
    ("MIND_OVER_MATTER_MANA_TAKE", None, "delete"),
    ("RESOLUTE_TECHNIQUE_ACCURACY", "ACCURACY_IS_DOUBLED", "rename"),
    ("RESOLUTE_TECHNIQUE_CRIT_CHANCE", "NEVER_DEAL_CRITICAL_HITS", "rename"),
    ("SCARRED_FAITH_PHYS_RECOUP_ENERGY", "OF_PHYSICAL_DAMAGE_PREVENTED_RECOUPED_AS_ENERGY_SHIELD_PER_ENEMY_POWER", "rename"),
    ("UNWAVERING_STANCE_IMMUNE_STUN", "CANNOT_BE_LIGHT_STUNNED", "rename"),
    ("CHARGE_CONVERT_CONDUIT", None, "delete"),
    ("CHARGE_CONVERT_RESONANCE", None, "delete"),
    ("ATTRIBUTE_REQUIREMENTS", "TRIPLE_ATTRIBUTES_REQUIREMENTS_OF_MARTIAL_WEAPONS", "rename"),
]
step2_counts = {"delete": 0, "merge": 0, "rename": 0}
for old_c, tgt, mode in STEP2:
    if old_c not in by_const:
        log("STEP2 WARN missing:", old_c)
        continue
    if mode == "delete":
        removed.add(old_c)
    else:
        if tgt not in by_const:
            log("STEP2 ERROR target missing:", tgt)
            sys.exit(1)
        do_rename(old_c, tgt)
    # the old path carries a keystone node name -> drop the field, drop its put
    by_path.pop(by_const[old_c]["path"], None)
    kill_put.add(old_c)
    step2_counts[mode] += 1
log("STEP2:", step2_counts)

# ---------------------------------------------------------------- STEP 3
HOLLOW_PALM = "UNARMED_ATTACKS_THAT_WOULD_USE_AN_EQUIPPED_QUARTERSTAFF_S_DAMAGE_HAVE_BASE_UNARMED_DAMAGE_PHYSICAL_DAMAGE_REPLACED_WITH_DAMAGE_BASED_ON_THEIR_SKILL_LEVEL_MORE_ATTACK_SPEED_PER_ITEM_EVASION_ON_EQUIPPED_ARMOR_ITEMS_TO_CRITICAL_HIT_CHANCE_PER_ITEM_ENERGY_SHIELD_ON_EQUIPPED_ARMOR_ITEMS"
STEP3_RETIRE = [
    "STORM_SPELLS_HAVE_50_PERCENT_LESS_COST",
    "PLANT_SPELLS_HAVE_50_PERCENT_LESS_COST",
    "STORM_SPELLS_HAVE_75_PERCENT_LESS_DURATION",
    "PLANT_SPELLS_HAVE_75_PERCENT_LESS_DURATION",
    "STORM_AND_PLANT_SPELLS_DEAL_MORE_DAMAGE_COST_MORE_HAVE_MORE_DURATION",
    HOLLOW_PALM,
]
step3_retired = 0
for c in STEP3_RETIRE:
    if c not in by_const:
        log("STEP3 WARN missing:", c)
        continue
    by_path.pop(by_const[c]["path"], None)
    removed.add(c)
    kill_put.add(c)
    step3_retired += 1
log("STEP3 retired:", step3_retired)

# ---------------------------------------------------------- STEP 3+4 additions
step4_added = 0
for p in ADDED:
    if p in by_path:
        continue
    c = san(p)
    if c in by_const:
        log("STEP4 WARN const collision:", c, p)
        continue
    e = {"section": [], "keep": [], "const": c, "path": p}
    entries.append(e)
    by_path[p] = e
    by_const[c] = e
    added_paths.append((c, p))
    step4_added += 1
log("STEP4 added:", step4_added)

# ------------------------------------------------------------- drop removed
entries = [e for e in entries if e["const"] not in removed]
for c in removed:
    by_const.pop(c, None)
log("fields after ops:", len(entries), "(was", before_count, ")")

# ------------------------------------------------------------ sanity: lengths
too_long = [e["path"] for e in entries if len(e["path"]) + 5 > 255]
if too_long:
    log("ERROR path too long:", too_long)
    sys.exit(1)
dupes = [p for p in set(e["path"] for e in entries)
         if [e["path"] for e in entries].count(p) > 1]
if dupes:
    log("ERROR duplicate paths:", dupes)
    sys.exit(1)

# ============================================================ STEP 5 emit body
out = []
for e in entries:
    out.extend(e["section"])
    raw = sti.get(e["path"], {}).get("raw")
    out.append("    // " + (raw if raw else "(no corpus text)"))
    out.extend(e["keep"])
    out.append('    public static final Identifier %s = id("%s");' % (e["const"], e["path"]))
    out.append("")

new_src = "\n".join(prefix + [""] + out + suffix)
write_text(STATIDS + ".bak26", src0)
if APPLY:
    save(STATIDS, new_src, SRC_CRLF)
log("STEP5 StatIds emitted, fields:", len(entries))

# ================================================== global reference rewriting
SKIP = {STATIDS, KEYSTONE}  # both regenerated wholesale
ref_re = re.compile(r"StatIds\.(" + "|".join(sorted((re.escape(k) for k in repoint), key=len, reverse=True)) + r")\b") if repoint else None
lit_re = re.compile(r'"(' + "|".join(sorted((re.escape(k) for k in literals), key=len, reverse=True)) + r')"') if literals else None

kill_put_sorted = sorted(kill_put, key=len, reverse=True)

ref_hits = 0
lit_hits = 0
put_hits = 0
changed_files = []


def strip_put(text):
    """Remove every map.put(StatIds.<removed const>, ...) statement (paren-balanced)."""
    global put_hits
    out_lines = []
    L = text.split("\n")
    i = 0
    while i < len(L):
        line = L[i]
        hit = None
        for c in kill_put_sorted:
            if ("map.put(StatIds." + c + ",") in line or ("map.put(StatIds." + c + ")") in line \
                    or ("map.put(StatIds." + c + " ") in line:
                hit = c
                break
        if not hit:
            out_lines.append(line)
            i += 1
            continue
        # delete from this line until parens balance
        depth = 0
        j = i
        while j < len(L):
            for ch in L[j]:
                if ch == "(":
                    depth += 1
                elif ch == ")":
                    depth -= 1
            j += 1
            if depth <= 0:
                break
        put_hits += 1
        i = j
    return "\n".join(out_lines)


for f in JAVA:
    if f in SKIP:
        continue
    t0, crlf = load(f)
    # 1) delete doomed put statements FIRST, so the re-point pass below
    #    cannot re-create a duplicate map key onto an already-wired target
    t = strip_put(t0)
    if ref_re:
        t, n = ref_re.subn(lambda m: "StatIds." + repoint[m.group(1)], t)
        ref_hits += n
    if lit_re:
        t, n = lit_re.subn(lambda m: '"' + literals[m.group(1)] + '"', t)
        lit_hits += n
    if t != t0:
        changed_files.append(f)
        if APPLY:
            save(f, t, crlf)

log("global: StatIds. refs rewritten:", ref_hits, " string literals:", lit_hits,
    " map.put statements removed:", put_hits)
log("global: files changed:", len(changed_files))
for f in changed_files:
    log("   *", f)

# ================================================= STEP 6 regenerate keystones
ks0, KS_CRLF = load(KEYSTONE)
KL = ks0.split("\n")

NODE_HDR = re.compile(r'^\s*"(\d+)",\s*(-?[\d.]+f),\s*(-?[\d.]+f),\s*$', re.M)

out_k = []
i = 0
nodes_done = 0
pending_comments = []
while i < len(KL):
    l = KL[i]
    if l.strip().startswith("list.add(TalentNodeDef.of("):
        # pending_comments are the comment block that belongs to this node
        out_k.extend(pending_comments)
        pending_comments = []
        out_k.append(l)
        i += 1
        # copy until the stat Map.of line
        while i < len(KL) and "TalentNodeGen.stat(Map.of(" not in KL[i]:
            out_k.append(KL[i])
            i += 1
        if i >= len(KL):
            break
        # ---- rebuild the Map.of(...) argument list
        # find node id from the block we just copied
        block = "\n".join(out_k[-8:])
        m = NODE_HDR.search(block)
        if not m:
            log("STEP6 ERROR cannot find node id near line", i + 1)
            sys.exit(1)
        nid = m.group(1)
        recs = nsm.get(nid)
        if recs is None:
            log("STEP6 ERROR node not in node_stat_map:", nid)
            sys.exit(1)
        args = []
        for r in recs:
            p = r.get("path")
            if not p:
                continue
            c = san(p)
            if c not in by_const:
                log("STEP6 ERROR unknown constant", c, "for node", nid)
                sys.exit(1)
            vals = r.get("values") or []
            v = vals[0].get("value") if vals and vals[0].get("value") is not None else 1.0
            args.append("                        StatIds.%s, %s" % (c, repr(float(v))))
        # emit, preserving the Map.of( opener line's original indentation prefix
        opener = KL[i]
        out_k.append(opener)
        out_k.append(",\n".join(args) + "))),")
        # skip the old argument lines
        i += 1
        while i < len(KL) and ")))," not in KL[i]:
            i += 1
        i += 1
        nodes_done += 1
        continue
    if l.strip().startswith("//") or not l.strip():
        # buffer comments/blanks so they stay attached to the next node
        if l.strip():
            pending_comments.append(l)
        else:
            if pending_comments:
                out_k.extend(pending_comments)
                pending_comments = []
            out_k.append(l)
        i += 1
        continue
    out_k.extend(pending_comments)
    pending_comments = []
    out_k.append(l)
    i += 1
out_k.extend(pending_comments)

# scope TODO marker for storm|plant node
new_ks = "\n".join(out_k)
new_ks = new_ks.replace(
    '                        StatIds.STORM_AND_PLANT_SPELLS_DEAL_MORE_DAMAGE, 0.5,',
    '                        // TODO(scope): 需 COND 表达 storm|plant 作用域\n'
    '                        StatIds.STORM_AND_PLANT_SPELLS_DEAL_MORE_DAMAGE, 0.5,')

write_text(KEYSTONE + ".bak26", ks0)
if APPLY:
    save(KEYSTONE, new_ks, KS_CRLF)
log("STEP6 keystone nodes regenerated:", nodes_done)

# ==================================================================== summary
log("")
log("SUMMARY fields: before=%d after=%d" % (before_count, len(entries)))
log("  step1 renames      :", step1_applied)
log("  step2 delete/merge/rename:", step2_counts)
log("  step3 retired      :", step3_retired)
log("  step4 added        :", step4_added)
log("  removed constants  :", len(removed))
log("MODE:", "APPLIED" if APPLY else "DRY-RUN")
