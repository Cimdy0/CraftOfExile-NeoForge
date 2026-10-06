#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""只打印「具体名（非通用维度载体）」的有接线属性，便于人工识别真正语义漂移。"""
import json, re, os
ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge/src/main/java/com/cimdy/coe"
def read(p):
    with open(p, encoding="utf-8") as f: return f.read()
attr_src = read(os.path.join(ROOT,"data","AttributeRegister.java"))
decl_re = re.compile(r"public static Holder<Attribute>\s+(\w+)\s*=\s*register\w+\(\s*\"([^\"]+)\"")
attrs={}
for m in decl_re.finditer(attr_src):
    const,key=m.group(1),m.group(2)
    start=attr_src.find("public static Holder<Attribute> "+const+" =")
    blk=attr_src[:start]; jd=blk.rfind("/**"); cmt=""
    if jd!=-1:
        end=blk.find("*/",jd)
        if end!=-1:
            txt=attr_src[jd:end+2].replace("/**","").replace("*/","")
            cmt="\n".join(l.split("*",1)[1].strip() if "*" in l else l.strip() for l in txt.splitlines()).strip()
    attrs[const]={"key":key,"comment":cmt}
ids_src=read(os.path.join(ROOT,"data","codec","support","stat","StatIds.java"))
ids={m.group(1):m.group(2) for m in re.finditer(r"public static final Identifier\s+(\w+)\s*=\s*id\(\s*\"([^\"]+)\"",ids_src)}
sg_src=read(os.path.join(ROOT,"data","gen","stat","StatGen.java"))
wr=re.compile(r"(\w+)\(\s*map,\s*StatIds\.(\w+),\s*AttributeRegister\.(\w+)((?:\s*,\s*[^)]*)*)\)")
wirings={}
for m in wr.finditer(sg_src):
    op,sid,attr=m.group(1),m.group(2),m.group(3)
    tags=re.findall(r"StatTag\.(\w+)",m.group(4))
    wirings.setdefault(attr,[]).append({"op":op,"id":ids.get(sid,"<UNK>"),"tags":tags})
# 通用维度载体：这些是「维度属性+{scope}」设计，非漂移
GENERIC=("DAMAGE","AILMENT","ADDED","RESISTANCE","RESISTANCES","MAX_","MAX","ENERGY_SHIELD",
         "MANA","RAGE","SPIRIT","FRENZY","ENDURANCE","POWER","INFUSION","DARKNESS","VOLATILITY",
         "EFFECT_AREA","AREA","MAGNITUDE","DURATION","BUILDUP","THRESHOLD","LEECH","RECOVERY",
         "CHANCE","PENETRATION","GENERATION","REGEN","RESERVATION","FLASK","CHARM","REMNANT",
         "PROJECTILE","PRESENCE","STRENGTH","DEXTERITY","INTELLIGENCE","EVASION","ACCURACY",
         "ARMOR","BLOCK","CRIT","CAST","DEFLECTION","DODGE","PARRY","KNOCKBACK","CULLING",
         "CORPSE","MANA_TAKE","STATUS_","SKILL_SLOT","ITEM_CAPACITY","META_ENERGY","BANNER",
         "TALENT_","WORK_","GLORY","THORNS","SHIELD","AMOUNT_LEECHED","LEECHES_RESPONSE",
         "REGENERATION","NO_INHERENT","NO_SPIRIT","RAGE_LOSS","TAKE_NO","CURRENCY","FOUND_ITEM",
         "RECENTLY_CONSUMED","ADD_EMPOWERED","CORRUPTION","TOTEM","PUPPET","ARCHON","GUARD",
         "CROSS_BOW","ELEMENTAL_DAMAGE","AILMENT_DAMAGE")
def is_generic(c):
    return any(c.startswith(g) or c==g for g in GENERIC)
print("=== 具体名（非通用维度载体）有接线属性 ===\n")
for const in sorted(attrs):
    if is_generic(const): continue
    ws=wirings.get(const)
    if not ws: continue
    print(f"### {const}  (key={attrs[const]['key']})")
    print(f"    注解: {attrs[const]['comment'][:90]}")
    for w in ws:
        tg=("{"+",".join(w["tags"])+"}") if w["tags"] else "{}"
        print(f"      {w['op']:10} {tg:22} {w['id']}")
    print()
