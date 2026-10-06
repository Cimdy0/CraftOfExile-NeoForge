#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Re-verify the Stat generation layer: completeness + no duplicates.

Checks:
  L1 Ids classes: unique field names, unique paths, 1:1 with generated files.
  L2 StatGen.buildEntries: unique map.put keys, all EFF_/TGT_/COND_ refs resolve.
  L3 Generated JSON files: counts effect/targets/condition/entry, name<->path 1:1.
  L4 Amount distribution: every built entry uses $value (stat() factory).
"""
import os, re, sys, json

# 门禁分层（用户 2026-09-01 明令）：datagen 全部由 Codec + 工厂方法构成，
# compileJava 通过 ⇒ 生成产物必然正确，故 **runData 不再作为核检手段**（仅发布打包流程需要）。
# 增量检查 = compileJava + 本脚本的源级 STRICT 检查；generated-JSON 比对项默认仅提示（PENDING）。
# 仅当显式 COE_REQUIRE_RUNDATA=1 时才把 JSON 检查升格为硬门禁（仅在确实跑过 runData 之后使用）。
REQUIRE_GEN = os.environ.get("COE_REQUIRE_RUNDATA") == "1"

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")

def _statgen_sources():
    """StatGen.java plus every wirings/Wire*.java partition (wirings were split out
    of StatGen, so parsing only StatGen.java would silently yield zero wirings)."""
    base = os.path.join(GEN, "StatGen.java")
    parts = [read(base)]
    wdir = os.path.join(GEN, "wirings")
    if os.path.isdir(wdir):
        for f in sorted(os.listdir(wdir)):
            if f.startswith("Wire") and f.endswith(".java"):
                parts.append(read(os.path.join(wdir, f)))
    return "\n".join(parts)

RES = os.path.join(ROOT, "src", "generated", "resources", "data", "coe", "coe", "stat")

def read(p):
    with open(p, "r", encoding="utf-8") as f:
        return f.read()

# ---------- L1: parse Ids classes ----------
def parse_ids(path):
    txt = read(path)
    fields = []  # (lineno, name, pathstr)
    pat = re.compile(r'public static final Identifier (\w+) = id\("([^"]+)"\);')
    for i, line in enumerate(txt.splitlines(), 1):
        m = pat.search(line)
        if m:
            fields.append((i, m.group(1), m.group(2)))
    return fields

effect_ids = parse_ids(os.path.join(GEN, "StatEffectIds.java"))
target_ids = parse_ids(os.path.join(GEN, "StatTargetIds.java"))
cond_ids   = parse_ids(os.path.join(GEN, "StatConditionIds.java"))

def dup_check(name, fields):
    names = [f[1] for f in fields]
    paths = [f[2] for f in fields]
    dn = len(names) - len(set(names))
    dp = len(paths) - len(set(paths))
    print(f"[{name}] fields={len(fields)}  unique_names={len(set(names))}  unique_paths={len(set(paths))}"
          f"  dup_names={dn}  dup_paths={dp}")
    if dn:
        from collections import Counter
        c = Counter(names)
        print("    DUP NAMES:", [k for k,v in c.items() if v>1])
    if dp:
        from collections import Counter
        c = Counter(paths)
        print("    DUP PATHS:", [k for k,v in c.items() if v>1])

dup_check("StatEffectIds", effect_ids)
dup_check("StatTargetIds", target_ids)
dup_check("StatConditionIds", cond_ids)

eff_field_by_name = {f[1] for f in effect_ids}
tgt_field_by_name = {f[1] for f in target_ids}
cond_field_by_name = {f[1] for f in cond_ids}

# ---------- L0: parse StatIds field names (coverage of built keys) ----------
statids_path = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "codec", "support", "stat", "StatIds.java")
statids_fields = set()
if os.path.isfile(statids_path):
    sid_pat = re.compile(r'public static final Identifier (\w+) =')
    for line in read(statids_path).splitlines():
        m = sid_pat.search(line)
        if m:
            statids_fields.add(m.group(1))
    print(f"[StatIds] total field constants = {len(statids_fields)}")

# ---------- L2: parse StatGen.buildEntries ----------
sg = _statgen_sources()
# Wirings were split out of StatGen into stat/wirings/Wire*.java, so buildEntries()
# now only holds `WireXxx.build(map)` calls. Scan ALL sources instead of the method
# body, otherwise the count silently drops to 0 and every check false-PASSes.
block = sg
# 组合条目（PoE hybrid 词缀）写法必须一并列出：漏掉会让这些接线对全部检查不可见
# （正是技能 §11.10 记载的「门禁静默失效」模式）。
# 工厂形态无关 + 跨行匹配：除 self/stat/onTarget/group 外，StatBuilder.effect(...).param(...).build() 等
# 多行接线也一并捕获，避免「门禁静默失效」（§11.10）。第二分组捕获工厂实参，供 EFF/TGT/COND 引用解析。
put_pat = re.compile(r'map\.put\(StatIds\.(\w+),\s*(.+?)\)\s*;', re.DOTALL)
entries = put_pat.findall(block)
print(f"\n[StatGen.buildEntries] map.put count = {len(entries)}")

keys = [e[0] for e in entries]
dk = len(keys) - len(set(keys))
print(f"    unique keys={len(set(keys))}  dup_keys={dk}")

# coverage: every built key must be a real StatIds constant (else compile error / typo)
if statids_fields:
    bad_keys = [k for k in keys if k not in statids_fields]
    print(f"    built keys referencing real StatIds constants = {len(keys)-len(bad_keys)}/{len(keys)}"
          f"  unknown={len(bad_keys)}")
    if bad_keys:
        print("    UNKNOWN STATIDS KEYS:", bad_keys[:20])
else:
    bad_keys = []
if dk:
    from collections import Counter
    c = Counter(keys)
    print("    DUP KEYS:", [k for k,v in c.items() if v>1])

# resolve EFF_/TGT_/COND_ refs used in stat(...) calls
arg_pat = re.compile(r'(StatEffectIds|StatTargetIds|StatConditionIds)\.(\w+)')
unresolved = []
all_refs = []
for key, args in entries:
    for cls, fld in arg_pat.findall(args):
        all_refs.append((cls, fld))
        if cls == "StatEffectIds" and fld not in eff_field_by_name:
            unresolved.append(("EFF", fld, key))
        elif cls == "StatTargetIds" and fld not in tgt_field_by_name:
            unresolved.append(("TGT", fld, key))
        elif cls == "StatConditionIds" and fld not in cond_field_by_name:
            unresolved.append(("COND", fld, key))

print(f"    total sub-entry refs in stat() calls = {len(all_refs)}")
print(f"    unresolved refs = {len(unresolved)}")
for u in unresolved[:50]:
    print("    UNRESOLVED:", u)

# ---------- L3: generated JSON counts ----------
def jcount(sub):
    d = os.path.join(RES, sub)
    if not os.path.isdir(d):
        return 0, []
    fs = [f for f in os.listdir(d) if f.endswith(".json")]
    return len(fs), sorted(fs)

eff_n, eff_fs = jcount("effect")
tgt_n, tgt_fs = jcount("targets")
cond_n, cond_fs = jcount("condition")
entry_n, entry_fs = jcount("entry")
print(f"\n[Generated JSON] effect={eff_n}  targets={tgt_n}  condition={cond_n}  entry={entry_n}")

# path<->filename 1:1 for effect/targets/condition
def name_path_mismatch(ids_fields, fs):
    names = {f[2] + ".json" for f in ids_fields}   # path.json
    files = set(fs)
    missing = names - files
    orphan = files - names
    return missing, orphan

m1, o1 = name_path_mismatch(effect_ids, eff_fs)
m2, o2 = name_path_mismatch(target_ids, tgt_fs)
m3, o3 = name_path_mismatch(cond_ids, cond_fs)
print(f"    effect   path-vs-file  missing_in_json={len(m1)}  orphan_json={len(o1)}")
print(f"    targets  path-vs-file  missing_in_json={len(m2)}  orphan_json={len(o2)}")
print(f"    condition path-vs-file missing_in_json={len(m3)}  orphan_json={len(o3)}")
if m1: print("      EFF missing:", sorted(m1)[:10])
if o1: print("      EFF orphan :", sorted(o1)[:10])
if m2: print("      TGT missing:", sorted(m2)[:10])
if o2: print("      TGT orphan :", sorted(o2)[:10])
if m3: print("      COND missing:", sorted(m3)[:10])
if o3: print("      COND orphan :", sorted(o3)[:10])

# ---------- L4: entry JSON amount check ($value) ----------
# Every built entry should reference "$value" in its amount.
bad_amount = []
for fn in entry_fs:
    p = os.path.join(RES, "entry", fn)
    try:
        obj = json.load(open(p, encoding="utf-8"))
    except Exception:
        bad_amount.append((fn, "parse-error"))
        continue
    # 组合条目（StatGroup / "entry":"group"）：无顶层 amount，金额由各 member 承载。
    # 校验方式改为逐个 member 检查，否则每条 hybrid 词缀都会误报 no-$value。
    if obj.get("entry") == "group":
        members = obj.get("members") or []
        if not members:
            bad_amount.append((fn, "group-no-members"))
            continue
        for i, mem in enumerate(members):
            mamt = mem.get("amount", mem.get("statValue", None))
            ms = json.dumps(mamt)
            if "$value" not in ms and "value" not in ms:
                bad_amount.append((fn, "group-member-%d-no-$value" % i))
        continue
    # amount is an expression object {value:"$value"} or {"$value"} etc.
    amt = obj.get("amount", obj.get("statValue", None))
    s = json.dumps(amt)
    if "$value" not in s and "value" not in s:
        # try nested
        if not (isinstance(amt, dict) and ("value" in amt or "$value" in amt)):
            bad_amount.append((fn, "no-$value"))
print(f"\n[Entry amount] checked={len(entry_fs)}  entries_without_$value_or_value={len(bad_amount)}")
for b in bad_amount[:20]:
    print("    BAD:", b)

# ---------- Summary ----------
print("\n================ SUMMARY ================")
ok = True
# STRICT: purely source-level -- always enforced.
checks = [
    ("effect Ids unique", len(effect_ids)==len(set(f[1] for f in effect_ids)) and len(effect_ids)==len(set(f[2] for f in effect_ids))),
    ("target Ids unique", len(target_ids)==len(set(f[1] for f in target_ids)) and len(target_ids)==len(set(f[2] for f in target_ids))),
    ("condition Ids unique", len(cond_ids)==len(set(f[1] for f in cond_ids)) and len(cond_ids)==len(set(f[2] for f in cond_ids))),
    ("buildEntries non-empty (guard vs silent parse failure)", len(entries) > 0),
    ("buildEntries unique keys", dk==0),
    ("all built keys are real StatIds constants", len(bad_keys if 'bad_keys' in dir() else [])==0),
    ("all sub-entry refs resolve", len(unresolved)==0),
]
# PENDING when generated JSON is intentionally stale (no runData run this batch).
gen_checks = [
    # effect 基线 = 实测 StatEffectIds 字段数（grep -c "public static final Identifier" StatEffectIds.java）。
    # #33 诅咒敌两缺口经 CURSE_STAT_PROVIDER 消解 +2（794→796）；#39 presence 全簇经 grant_attribute 通道消解 +11（796→807：3 敌 + 8 友/敌友）；
    # 割裂·创口未接线：thousand_cuts 词缀 ENEMIES_YOU_APPLY_INCISION_TO_TAKE_INC_PHYSICAL_DAMAGE_PER_INCISION
    # 经 INCISION statProvider 跨实体读施加者属性注入受害者承伤（R3 安全）+1（808→809）。
    # 同伴上限：新增 EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_COMPANIONS（809→810），
    # 消费端 SpawnAction.canSpawnCompanion 真读 MAX_COMPANIONS（原硬编码 return 1 = 死线）。
    # 波动授予：新增 EFF_GRANT_ON_EVENT_RESOURCE_MINION_DEATH_VOLATILITY（810→811）。
    # 近战触及距离：新增 EFF_ADD_VALUE_ATTRIBUTE_MINECRAFT_ENTITY_INTERACTION_RANGE（811→812），
    # 消费端为 vanilla 自身（AttributeEvent:88 已为全实体注册 ENTITY_INTERACTION_RANGE）。
    # 危害物重布置几率：新增 EFF_ADD_VALUE_ATTRIBUTE_COE_HAZARD_REARM_CHANCE（812→813），
    # 消费端 HazardEntity#shouldRearm 真读；此前仅天赋可旁路授予，接线后词缀/传奇可用。
    # Fork 附加投射物：新增 EFF_ADD_VALUE_ATTRIBUTE_COE_PROJECTILE_FORK_EXTRA_CHANCE_PROJECTILE（813→814），
    # 消费端 ProjectileDeliveryExecutor#chanceForkExtra 真读（仅 fork>0 时掷骰）。
    # #5 闪电幸运类型分维：新增 EFF_DAMAGE_LUCKY_LIGHTNING（带 {lightning} 标签，814→815），
    # 消费端 StatEngine#rollModifier 现按伤害类型分维过滤，仅对闪电伤害生效（不与全伤害幸运冲突）。
    # 2026-09-07 实测对齐（基线随 datagen 实测更新，勿用旧值+N 推算）：effect = 847。
    #   本轮新增 4 条：CHARGE_CONSUME_SKIP_CHANCE / INFUSION_CONSUME_SKIP_CHANCE /
    #   INVOKED_HALF_ENERGY_CHANCE / TRIGGER_ENERGY_REFUND_CHANCE（旧基线 815 已长期滞后，本次一并校正）。
    # 2026-09-07 死线清理：删除 13 条「已注册但无任何 Wire 引用」的悬空 EFF（852→839），
    #   含 AILMENT_MAGNITUDE 施加侧同义异写 5 条、CAST_SPEED_CURSE、CRIT_CHANCE_HERALD、MAX_RAGE、
    #   LAUNCH_ADDITIONAL_PROJECTILE_SPELL、RAGE_REGEN_PER_SECOND、INFUSION_{COLD,LIGHTNING,VAAL}_STACKS。
    #   INTIMIDATE 家族新增 EFF_CAUSED_HIT_GRANT_ON_EVENT_INTIMIDATE_STATUS_SECONDS（已接线，839→840）。
    # 2026-09-08 死线修复：补 exposure_{fire,cold,lightning} / damage_dealt /
    #   speed_per_stack_{attack,cast,movement_speed} 共 7 条 StatGen 条目（StatusBehavior 引用过但从未注册），
    #   datagen 实测 effect 840→847（本项此前长期 PEND，故未暴露）。
    ("effect count 847", len(effect_ids)==847 and eff_n==847),
    ("target count 14", len(target_ids)==14 and tgt_n==14),
    # S5：RECENTLY_CONSUMED 状态建模改事件门控 → 新增 COND_RECENTLY_CONSUME_CORPSE（141→142）。
    # 「召唤物近期死亡」：新增 COND_RECENTLY_MINION_DIED（+1，142→143），发射点 DamageEvent.LivingDeathEvent（记在主人名下）。
    # 「ES 充能已启动」：新增 COND_ES_RECHARGE_STARTED_2S（+1，143→144），发射点 AttributeEvent ES 充能分支（边沿触发）。
    # 2026-09-07 实测对齐：condition 147（基线随 datagen 实测更新）。
    ("condition count 147", len(cond_ids)==147 and cond_n==147),
    ("effect path<->file 1:1", len(m1)==0 and len(o1)==0),
    ("target path<->file 1:1", len(m2)==0 and len(o2)==0),
    ("condition path<->file 1:1", len(m3)==0 and len(o3)==0),
    ("all entry amounts use $value/value", len(bad_amount)==0),
]
for label, v in checks:
    print(("  PASS " if v else "  FAIL ") + label)
    ok = ok and v
if not REQUIRE_GEN:
    print("  ---- generated-JSON checks PENDING (runData 非核检手段；需硬校验时设 COE_REQUIRE_RUNDATA=1) ----")
    for label, v in gen_checks:
        print(("  PEND " if not v else "  PASS ") + label)
else:
    for label, v in gen_checks:
        print(("  PASS " if v else "  FAIL ") + label)
        ok = ok and v
print("\nRESULT:", "ALL CHECKS PASS" if ok else "SOME CHECKS FAILED")
sys.exit(0 if ok else 1)
