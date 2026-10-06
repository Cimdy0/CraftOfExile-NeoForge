# -*- coding: utf-8 -*-
"""C5-v2：把节点分解为 (核心概念 -> Effect) × (作用域 -> Condition/Target/Effect变体)。

用户纠正的核心：
- 比较对象是 *全量 545 Effects*，不是 AttributeRegister(109)。属性只是实现途径之一。
- 作用域(actor/type) 通过三种方式承载，都不是“新属性/新 effect”：
    (1) Effect 自身的 type 变体后缀：_MINION / _HERALD / _ATTACK / _TOTEM / _SPELL / _TAKEN ...
    (2) StatCondition(39)：living-state/status/recent-event/equipment/socket/ground/presence
    (3) StatTarget(14)：SELF_ONLY / INLINE_MINION / INLINE_TOTEM / COMPANION / PRESENCE_*
- 真缺口 = 核心概念在 545 Effects 中完全无对应（且无法通过 cond/tgt 组合表达）。

判定：
- 抽取节点概念词 + 作用域词；
- 概念词 ⊆ 某 effect 概念核心(允许 effect 带额外作用域词) -> 命中 effect（取最贴合）；
- 作用域词逐个解析为 cond/tgt/变体 -> 全部可解析 => WIREABLE；部分不可 => NEEDS_COND；
- 概念无 effect => TRUE_GAP。
"""
import re, json, collections

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"

OP_PREFIX = ('INC_','TO_','ADD_','MORE_','REDUCE_','LESS_','TO_MAX_','INC_MAX_',
             'MAX_','ADD_AILMENT_','INC_AILMENT_','TO_ALL_','INC_ALL_','ALL_','TO_ANY_')

VERB = {'inc','increased','add','adds','added','more','reduced','less','to','and','or',
        'the','a','of','on','you','your','with','for','from','additional','non','rating',
        'amount','deal','equal','per','have','gain','gains','gained'}  # damage/hit 不在此：damage=概念, hit=作用域触发

SYN = {'critical':'crit','maximum':'max','armour':'armor','life':'health',
       'resistence':'resistance','defence':'defense','regen':'regeneration'}

# 作用域词（actor/type/状态触发）—— 解析到 cond/tgt/变体，而非新属性
SCOPE = {'minion','minions','companion','companions','totem','totems','aura','auras',
         'herald','heralds','warcry','warcries','mark','marks','offering','offerings',
         'projectile','projectiles','spell','spells','attack','attacks','melee','cast',
         'curse','curses','fire','cold','lightning','physical','chaos','elemental',
         'non_damaging','over_time','you','your','taken','low','life','enemy','enemies',
         'while','moving','using','skill','skills','presence','banner','bear_form','mace',
         'knife','spear','crossbow','staff','wand','bow','trap','mine','recently','kill',
         'kills','ground','chilled','ignited','jagged','shocked','rare','unique','full',
         'stationary','surrounded','blocking','parrying','shapeshifted','bleed','daze',
         'electrocute','freeze','heavy_stun','pin','shock','exposure','infusion'}

# 核心概念词（保留，用于命中 effect）
CONCEPT_DEFAULT = True  # 不在 VERB/SYN/SCOPE 的都算概念

# ---- 作用域解析表：scope 词 -> (kind, ref) ----
COND_NAMES = [c.split('COND_',1)[1].lower() for c in
              open(f"{ROOT}/src/main/java/com/cimdy/coe/data/gen/stat/StatConditionIds.java").read().split()
              if c.startswith('COND_')]
TGT_NAMES = [t.split('TGT_',1)[1].lower() for t in
             open(f"{ROOT}/src/main/java/com/cimdy/coe/data/gen/stat/StatTargetIds.java").read().split()
             if t.startswith('TGT_')]

# 关键字 -> 已注册 cond/tgt（用于把节点 scope 词解析到真实常量）
SCOPE_TO_COND = {
    'low':'living_state_low_life','full':'full_life_living_state','stationary':'movement_stationary',
    'surrounded':'living_state_surrounded','blocking':'blocking_living_state','parrying':'living_state_parrying',
    'shapeshifted':'living_state_shapeshifted','bleed':'bleed_status_age','daze':'daze_status',
    'electrocute':'electrocute_status','freeze':'freeze_status','heavy_stun':'heavy_stun_status',
    'pin':'pin_status','shock':'shock_status','exposure':'exposure_fire_status',
    'chilled':'on_chilled_ground_status','ignited':'on_ignited_ground_status',
    'jagged':'on_jagged_ground_status','shocked':'on_shocked_ground_status',
    'rare':'presence_rarity_rare_unique','unique':'presence_rarity_rare_unique',
    'kill':'count_kill_recently','recently':'count_recently_taken_hit','taken':'taken',
}
SCOPE_TO_TGT = {
    'minion':'inline_minion','minions':'inline_minion','companion':'companion_inline',
    'totem':'inline_totem','totems':'inline_totem','presence':'inline_presence_allies',
    'enemy':'all_on_target','enemies':'all_on_target','you':'self_only','your':'self_only',
}

def resolve_scope(word):
    w = word.lower()
    if w in SCOPE_TO_TGT: return ('tgt', 'TGT_'+SCOPE_TO_TGT[w].upper())
    if w in SCOPE_TO_COND: return ('cond', 'COND_'+SCOPE_TO_COND[w].upper())
    # 模糊：type/element 词归为“effect 变体/标签”，视为可解析（由 effect 自身承载）
    if w in {'attack','attacks','spell','spells','melee','cast','curse','curses','fire','cold',
             'lightning','physical','chaos','elemental','non_damaging','over_time','aura','auras',
             'herald','heralds','warcry','warcries','mark','marks','offering','offerings',
             'projectile','projectiles','mace','knife','spear','crossbow','staff','wand','bow',
             'trap','mine','infusion','banner','bear_form'}:
        return ('variant', w)
    return ('unresolved', w)

def strip_op(s):
    for p in OP_PREFIX:
        if s.startswith(p): return s[len(p):]
    return s

def split_node(const):
    s = strip_op(const)
    toks = s.split('_')
    concept, scope = [], []
    for t in toks:
        t = t.lower()
        if t in VERB: continue
        t = SYN.get(t, t)
        if t in SCOPE:
            scope.append(t)
        else:
            concept.append(t)
    return concept, scope

# ---- EFF index ----
eff_src = open(f"{ROOT}/src/main/java/com/cimdy/coe/data/gen/stat/StatEffectIds.java").read()
eff_pairs = re.findall(r'static final Identifier (EFF_[A-Z0-9_]+) = id\("([^"]+)"\)', eff_src)

def eff_concept_of(path):
    p = path.lower().split('_')
    p = [t for t in p if t not in
         ('add','multiplied','base','total','value','attribute','ailment','requirement')]
    # 去掉命名空间与纯 actor 标记
    p = [t for t in p if t not in ('coe','minecraft','banner','bear_form','chaos','taken','x')]
    return [t for t in p if t not in VERB and t not in SYN.values()]

EFF = [(c, path, set(eff_concept_of(path))) for c, path in eff_pairs]

# ---- unwired ----
spec = json.load(open(f"{ROOT}/poe2_assets/stat_entry_spec.json"))
sg = open(f"{ROOT}/src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java").read()
wired = set(re.findall(r'map\.put\(StatIds\.(\w+),', sg))
UNWIRED = [t for t in spec if t.get('constant') not in wired]
print(f"unwired={len(UNWIRED)}")

wireable, needs_cond, true_gap = [], [], []
for t in UNWIRED:
    const = t['constant']; tmpl = t.get('template','')
    concept, scope = split_node(const)
    if not concept:
        needs_cond.append((const, tmpl, concept, scope, ('', ''), 'no-concept'))
        continue
    # 命中 effect：eff_concept ⊇ node_concept（effect 可带额外作用域词）
    cands = []
    for econst, epath, econc in EFF:
        if set(concept).issubset(econc):
            extra = econc - set(concept)
            # extra 应只含作用域/中性词（否则算另一概念）
            cands.append((len(econc), econst, epath, sorted(extra)))
    if not cands:
        true_gap.append((const, tmpl, concept, scope))
        continue
    cands.sort()
    best = cands[0]
    # 解析作用域词
    scope_res = [resolve_scope(w) for w in scope]
    unresolved = [r for r in scope_res if r[0]=='unresolved']
    if unresolved:
        needs_cond.append((const, tmpl, concept, scope, [best[1],best[2]],
                           'unresolved-scope:'+','.join(u[1] for u in unresolved)))
    else:
        wireable.append((const, tmpl, concept, scope, [best[1],best[2]], scope_res))

print(f"\n=== 分解结果（Effect × Condition × Target）===")
print(f"  WIREABLE（概念有 effect + 作用域全可解析） : {len(wireable)}")
print(f"  NEEDS_COND（概念有 effect，部分作用域需新条件/裁定）: {len(needs_cond)}")
print(f"  TRUE_GAP（核心概念在 545 Effects 中无对应） : {len(true_gap)}")
print(f"  -> 真缺口占未接线比例: {len(true_gap)}/{len(UNWIRED)} = {100*len(true_gap)/len(UNWIRED):.1f}%")

out = {'wireable':[{"const":c,"template":tm,"concept":cn,"scope":sc,"eff":e,"path":p}
                   for c,tm,cn,sc,(e,p),sr in wireable],
       'needs_cond':[{"const":c,"template":tm,"concept":cn,"scope":sc,"eff":e,"path":p,"note":n}
                     for c,tm,cn,sc,(e,p),n in needs_cond],
       'true_gap':[{"const":c,"template":tm,"concept":cn,"scope":sc}
                   for c,tm,cn,sc in true_gap]}
json.dump(out, open(f"{ROOT}/poe2_assets/effect_reconcile_v2.json","w",encoding="utf-8"),
          ensure_ascii=False, indent=1)

print("\n=== WIREABLE 样本（前 25）===")
for c,tm,cn,sc,(e,p),sr in wireable[:25]:
    print(f"  {c:44s} -> {e}")
    print(f"       concept={cn} scope={sc}")
print("\n=== TRUE_GAP 样本（前 25）===")
for c,tm,cn,sc in true_gap[:25]:
    print(f"  {c:44s} concept={cn} scope={sc}  tpl='{tm}'")
