# -*- coding: utf-8 -*-
"""C5: 把未接线 StatIds 模板与 *全量* 545 Effects 对照（属性只是实现途径之一）。

核心纪律（来自用户纠正）：
- 比较对象是 Effect（StatEffectIds 全量 545），不是 AttributeRegister（109）。
- 一个 Stat 被“实现”= 接线到某个已注册 EFF_*（attribute 或非 attribute 皆可）。
- 作用域/承受侧(taken)/元素/类型 等是 Condition/Target 维度（39 cond + 14 tgt），
  不算“新属性 / 新 effect”，只是接 line 时加 stat(cond,tgt,eff) 的差异。

匹配规则（保守、可审计）：
- 节点核心词 = StatIds 常量名去 op 前缀 + 去 动词/纯 scope/命名空间 词，保留概念词。
- effect 核心词 = EFF path 去 op 前缀 + 去 'attribute'/'ailment' 标记 + 去 命名空间/纯 scope 词，保留概念词。
- 命中：node_core ⊆ eff_core，且 eff_core − node_core 只含 NS_SCOPE（纯作用域，不改概念）
        -> EXACT（安全接线，scope 由 cond/tgt 承载）。
- 候选但需复核：node_core ⊆ eff_core 但 eff 多出概念词 -> REVIEW（可能近义或真缺口）。
- 无命中：eff_core 不含 node_core -> NONE（需设计新 effect，或归为 cond/tgt 组合）。
"""
import re, json, collections

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"

# ---- op 前缀（StatIds 常量）----
OP_PREFIX = ('INC_','TO_','ADD_','MORE_','REDUCE_','LESS_','TO_MAX_','INC_MAX_',
             'MAX_','ADD_AILMENT_','INC_AILMENT_','TO_ALL_','INC_ALL_','ALL_','TO_ANY_')

# ---- 真正“中性”的命名空间 / 承受侧标记（不改核心概念，可由 TGT/cond 承载）----
# 注意：melee/attack/spell/cast/curse/元素/类型 等是 *effect 选择词*，不是中性 scope，必须保留为概念词。
NS_NEUTRAL = {'coe','minecraft','banner','bear_form','chaos','taken','per','second','x',
              'gain','gains','gained'}

# ---- 节点侧保守同义词桥接（仅拼写/缩写，不改语义范围；来自技能文档与 mc_replace 同族）----
SYN = {'critical':'crit','maximum':'max','armour':'armor','life':'health',
       'resistence':'resistance','defence':'defense'}

VERB = {'inc','increased','add','adds','added','more','reduced','less','to','and','or',
        'the','a','of','on','you','your','with','for','from','additional','non','rating',
        'amount','hit','damage'}  # 'hit'/'damage' 在节点核心里多为触发/冗余修饰，作噪声（触发侧归 cond）

def strip_op(s):
    for p in OP_PREFIX:
        if s.startswith(p):
            return s[len(p):]
    return s

def core(tokens, node_side=False):
    """token list -> 概念核心集合（去 op/动词/中性命名空间）。"""
    out = []
    for t in tokens:
        t = t.lower()
        if t in NS_NEUTRAL or t in VERB:
            continue
        if node_side and t in SYN:
            t = SYN[t]
        out.append(t)
    return out

# ---------------------------------------------------------------- EFF index
eff_src = open(f"{ROOT}/src/main/java/com/cimdy/coe/data/gen/stat/StatEffectIds.java", encoding="utf-8").read()
eff_pairs = re.findall(r'static final Identifier (EFF_[A-Z0-9_]+) = id\("([^"]+)"\)', eff_src)

def eff_opfamily(path):
    p = path.lower()
    if 'attribute' in p:
        if 'multiplied_base' in p: return 'base'
        if 'multiplied_total' in p: return 'total'
        if 'add_value' in p: return 'value'
        return 'attr_other'
    return 'nonattr'

def eff_core_of(path):
    p = path.lower()
    toks = p.split('_')
    # 去掉 op 前缀词
    toks = [t for t in toks if t not in
            ('add','multiplied','base','total','value','attribute','ailment','requirement')]
    return set(core(toks, False))

EFF = []  # (const, path, opfam, core_set)
for const, path in eff_pairs:
    EFF.append((const, path, eff_opfamily(path), eff_core_of(path)))

# ---------------------------------------------------------------- unwired StatIds
spec = json.load(open(f"{ROOT}/poe2_assets/stat_entry_spec.json", encoding="utf-8"))
# 已接线的常量（从 StatGen.buildEntries 解析）
sg = open(f"{ROOT}/src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java", encoding="utf-8").read()
wired = set(re.findall(r'map\.put\(StatIds\.(\w+),', sg))

UNWIRED = []
for t in spec:
    c = t.get('constant')
    if c in wired:
        continue
    UNWIRED.append(t)
print(f"spec total={len(spec)}  wired-in-spec(excluded)={len(spec)-len(UNWIRED)}  unwired={len(UNWIRED)}")

def node_opfamily(const):
    if const.startswith('INC_'): return 'base'
    if const.startswith('MORE_'): return 'total'
    if const.startswith('INC_MAX_'): return 'total'
    if const.startswith(('TO_','ADD_','TO_MAX_','MAX_')): return 'value'
    return None  # 宽松：任意

def node_core_of(const):
    s = strip_op(const)
    toks = s.split('_')
    return set(core(toks, True))

# ---------------------------------------------------------------- match
exact, review, none = [], [], []
for t in UNWIRED:
    const = t['constant']
    tmpl = t.get('template','')
    ncore = node_core_of(const)
    nopf = node_opfamily(const)
    if not ncore:
        # 纯 op 词（如 TO_ALL_ATTRIBUTES）无法机械判定，归 review
        review.append((const, tmpl, None, 'no-core'))
        continue
    exact_best = None      # (score, econst, epath, extra_scope)
    review_best = None     # (overlap, econst, epath)
    for econst, epath, eopf, ecore in EFF:
        # op 族对齐（宽松：nopf None 时任意；否则需一致；nonattr 永远允许）
        if nopf is not None and eopf not in ('nonattr', nopf):
            continue
        if not ncore.issubset(ecore):
            continue
        extra = ecore - ncore
        if extra <= NS_NEUTRAL:
            score = len(ecore)
            if exact_best is None or score < exact_best[0]:
                exact_best = (score, econst, epath, sorted(extra))
        else:
            ov = len(ncore)
            if review_best is None or ov > review_best[0]:
                review_best = (ov, econst, epath)
    if exact_best:
        exact.append((const, tmpl, exact_best[1], exact_best[2], exact_best[3]))
    elif review_best:
        review.append((const, tmpl, (review_best[1], review_best[2]), 'concept-extra'))
    else:
        none.append((const, tmpl))

print(f"\n=== 对照结果（全量 545 Effects）===")
print(f"  EXACT（安全接线，scope 由 cond/tgt 承载） : {len(exact)}")
print(f"  REVIEW（近义/需人工复核）               : {len(review)}")
print(f"  NONE（无对应 effect -> 真设计缺口）     : {len(none)}")
print(f"  -> 真设计缺口占未接线比例: {len(none)}/{len(UNWIRED)} = {100*len(none)/len(UNWIRED):.1f}%")

# 输出
out = {'exact':[{"const":c,"template":tm,"eff":e,"path":p,"extra_scope":x} for c,tm,e,p,x in exact],
        'review':[{"const":c,"template":tm,"hint":h,"reason":r} for c,tm,h,r in review],
        'none':[{"const":c,"template":tm} for c,tm in none]}
json.dump(out, open(f"{ROOT}/poe2_assets/effect_reconcile.json","w",encoding="utf-8"),
          ensure_ascii=False, indent=1)

print("\n=== EXACT 样本（前 30）===")
for c,tm,e,p,x in exact[:30]:
    print(f"  {c:46s} -> {e}")
    print(f"       tpl='{tm}'  extra_scope={x}")

print("\n=== NONE 样本（前 30，真缺口候选）===")
for c,tm in none[:30]:
    print(f"  {c:46s}  tpl='{tm}'")
PY = None
