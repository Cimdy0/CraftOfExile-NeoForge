#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""生成全量 119 属性逐条裁定表：常量/key/section/接线(op+StatIds+scope)/真实语义/通道判定/漂移裁定。
通道判定规则（用户）：chance -> Stat值(ModifierStatEffect scope-fold)；倍率(multiplier) -> Effect数值(fire 通道)。
"""
import re, os
ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge/src/main/java/com/cimdy/coe"
def read(p):
    with open(p, encoding="utf-8") as f: return f.read()
attr_src = read(os.path.join(ROOT,"data","AttributeRegister.java"))
ids_src = read(os.path.join(ROOT,"data","codec","support","stat","StatIds.java"))
sg_src = read(os.path.join(ROOT,"data","gen","stat","StatGen.java"))

# 属性
decl_re = re.compile(r"public static Holder<Attribute>\s+(\w+)\s*=\s*register\w+\(\s*\"([^\"]+)\"")
attrs=[]
for m in decl_re.finditer(attr_src):
    const,key=m.group(1),m.group(2)
    ln=attr_src[:m.start()].count("\n")+1
    start=attr_src.find("public static Holder<Attribute> "+const+" =")
    blk=attr_src[:start]; jd=blk.rfind("/**"); cmt=""
    if jd!=-1:
        end=blk.find("*/",jd)
        if end!=-1:
            txt=attr_src[jd:end+2].replace("/**","").replace("*/","")
            cmt="\n".join(l.split("*",1)[1].strip() if "*" in l else l.strip() for l in txt.splitlines()).strip()
    attrs.append({"const":const,"key":key,"line":ln,"comment":cmt})
# section
sec_re=re.compile(r"=+\s*(§[A-M])\s*([^=]+?)\s*=+")
sections=[(int(m.group(0)[:0] or 0), m.group(1), m.group(2).strip()) for m in []]
secs=[]
for m in sec_re.finditer(attr_src):
    ln=attr_src[:m.start()].count("\n")+1
    secs.append((ln, m.group(1), m.group(2).strip()))
def section_of(ln):
    cur=("?","")
    for s in secs:
        if s[0]<=ln: cur=(s[1],s[2])
        else: break
    return cur
for a in attrs:
    a["sec"],a["secname"]=section_of(a["line"])

ids={m.group(1):m.group(2) for m in re.finditer(r"public static final Identifier\s+(\w+)\s*=\s*id\(\s*\"([^\"]+)\"",ids_src)}
wr=re.compile(r"(\w+)\(\s*map,\s*StatIds\.(\w+),\s*AttributeRegister\.(\w+)((?:\s*,\s*[^)]*)*)\)")
wirings={}
for m in wr.finditer(sg_src):
    op,sid,attr=m.group(1),m.group(2),m.group(3)
    tags=re.findall(r"StatTag\.(\w+)",m.group(4))
    wirings.setdefault(attr,[]).append({"op":op,"id":ids.get(sid,"<UNK>"),"tags":tags})

KNOWN_DRIFT={"DEFEND_ARMOUR"}  # 审计坐实
def classify(a):
    ws=wirings.get(a["const"],[])
    if not ws:
        return "直读/保留", "无 StatGen 接线（base 锚定或天赋预留）"
    ops={w["op"] for w in ws}
    has_chance="chance" in ops
    mult_ops={"inc","pctAdd","more","add","addMultipliedBase","addMultipliedTotal","valueMod"}
    has_mult=bool(ops & mult_ops)
    if has_chance and has_mult:
        ch="混合：chance=Stat值 / 倍率=Effect数值"
    elif has_chance:
        ch="Stat值(chance)"
    elif has_mult:
        ch="Effect数值(倍率)"
    else:
        ch="其他Effect("+"/".join(ops)+")"
    return ch, ""

# 漂移裁定：已知 + 混合维度(单属性载 chance 与倍率两机制)
drift=[]
for a in attrs:
    ws=wirings.get(a["const"],[])
    ops={w["op"] for w in ws}
    if a["const"] in KNOWN_DRIFT:
        drift.append(a["const"])
    elif "chance" in ops and (ops & {"inc","pctAdd","more","add","addMultipliedBase","addMultipliedTotal","valueMod"}):
        drift.append(a["const"])

lines=[]
lines.append("# 全量 119 属性逐条裁定表\n")
lines.append("> 通道判定（用户规则）：chance → Stat值(ModifierStatEffect scope-fold 读)；倍率(multiplier) → Effect数值(StatEffect.fire 通道)。\n")
lines.append(f"> 属性总数={len(attrs)}；有接线={sum(1 for a in attrs if a['const'] in wirings)}；漂移候选={len(drift)}（已知 {len(KNOWN_DRIFT)} + 混合维度 {len(drift)-len(KNOWN_DRIFT)}）。\n")
lines.append("")
# 漂移候选先列
lines.append("## 漂移候选（优先裁定）\n")
for c in drift:
    a=next(x for x in attrs if x["const"]==c)
    ws=wirings.get(c,[])
    lines.append(f"- **{c}** (`{a['key']}`)：{a['comment'][:60]}")
    for w in ws:
        tg=("{"+",".join(w["tags"])+"}") if w["tags"] else "{}"
        lines.append(f"    - `{w['op']}` {tg} `{w['id']}`")
lines.append("")
# 全表
lines.append("## 全量裁定\n")
lines.append("| # | 属性 | key | section | 接线(op/tags/StatIds) | 真实语义锚 | 通道判定 | 裁定 |")
lines.append("|---|---|---|---|---|---|---|---|")
for i,a in enumerate(attrs,1):
    ws=wirings.get(a["const"],[])
    if ws:
        wiring=" / ".join(f"{w['op']}{('·'+','.join(w['tags'])) if w['tags'] else ''}:{w['id']}" for w in ws)
    else:
        wiring="—"
    sem=a["comment"].replace("|","/")[:40]
    ch,_=classify(a)
    verdict="**漂移·改名**" if a["const"] in KNOWN_DRIFT else ("混合维度·待归一" if a["const"] in drift else "保留")
    lines.append(f"| {i} | {a['const']} | {a['key']} | {a['sec']} | {wiring} | {sem} | {ch} | {verdict} |")

out=os.path.join(os.path.dirname(__file__),"..","ATTRIBUTE_FULL_ADJUDICATION.md")
with open(out,"w",encoding="utf-8") as f:
    f.write("\n".join(lines))
print("written", out, "rows=",len(attrs), "drift=",drift)
