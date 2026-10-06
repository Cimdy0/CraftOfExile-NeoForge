# -*- coding: utf-8 -*-
import importlib.util, collections, json
spec = importlib.util.spec_from_file_location("s10","poe2_assets/step10_classify.py")
m = importlib.util.module_from_spec(spec); spec.loader.exec_module(m)
ids=m.load_statids(); gen=m.load_generated(); bases=m.load_attr_bases()
want={"R1","R2","R4","R5","R6","R8","S2","S3","A2","A0"}
buck=collections.defaultdict(list)
for name,path,cmt,ln in ids:
    j=gen.get(path)
    if j is None:
        exp=sorted(k for k in gen if k.startswith(path+"_from_"))
        if exp: j=dict(gen[exp[0]])
    for ru,sv,ms in m.check_rules(name,path,cmt,j,bases):
        if ru in want:
            a=f"{(j or {}).get('type')}"
            if (j or {}).get('type')=='attribute':
                a+=f" | {j.get('attribute')} | {j.get('operation')}"
            buck[ru].append((name,cmt[:80],a,ms,ln))
for ru in sorted(buck):
    print(f"\n########## {ru}  ({len(buck[ru])}) ##########")
    for name,cmt,a,ms,ln in buck[ru]:
        print(f"L{ln:<5} {name}\n   desc> {cmt}\n   impl> {a}\n   msg > {ms}")
