#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Build a review-ready triage list for the live 151 unwired StatIds.

Reuses the Sep-1 adjudication in scripts/_classify.json (A/B/C/D) and enriches
each item with sibling-scan data (does it have a wired "sibling" + which EFF?).
Outputs scripts/_Triage_151.md. Read-only on source; writes only the report.
"""
import json, re, os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
WIRING_DIR = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat", "wirings")
PUT_RE = re.compile(r"map\.put\(\s*StatIds\.([A-Z0-9_]+)\s*,")
EFF_RE = re.compile(r"StatEffectIds\.(EFF_[A-Z0-9_]+)")
COND_RE = re.compile(r"StatConditionIds\.(COND_[A-Z0-9_]+)")
STOP = {"A","AN","THE","OF","TO","WITH","AND","OR","FOR","ON","IN","AT","BY","IS","ARE",
        "IF","WHEN","WHILE","YOU","YOUR","HAVE","HAS","BEEN","BE","THAT","THEY","ENEMY","ENEMIES",
        "TARGET","TARGETS","SKILL","SKILLS","DAMAGE","HIT","HITS","INC","ADD","MORE","DECREASE",
        "REDUCED","GAIN","GRANT","PER","UP","MAX","MINIMUM","EACH","ALL","ANY","NOT","NO","CAN",
        "CANNOT","FROM","AS","THAN","THEIR","ITS"}

def parse_wirings():
    out = {}
    for fn in sorted(os.listdir(WIRING_DIR)):
        if not fn.endswith(".java"):
            continue
        src = open(os.path.join(WIRING_DIR, fn), encoding="utf-8").read()
        for m in PUT_RE.finditer(src):
            key = m.group(1)
            start = src.rfind("(", 0, m.end()); depth = 0; j = start
            while j < len(src):
                if src[j] == "(": depth += 1
                elif src[j] == ")":
                    depth -= 1
                    if depth == 0: break
                j += 1
            body = src[start:j+1]
            rec = out.setdefault(key, {"effs": set(), "conds": set()})
            rec["effs"].update(EFF_RE.findall(body))
            rec["conds"].update(COND_RE.findall(body))
    return out

def toks(n): return {t for t in n.split("_") if t and t not in STOP}
def jac(a, b): return len(a & b)/len(a | b) if a and b else 0.0

def main():
    wired = parse_wirings()
    live = sorted(set(json.load(open(os.path.join(ROOT, "scripts/_unwired.json")))) - set(wired))
    cls = json.load(open(os.path.join(ROOT, "scripts/_classify.json")))
    rev = {}
    for cat in ["A", "B", "C"]:
        for k, v in cls[cat].items():
            rev[k] = (cat, v)
    Dset = set(cls["D"])
    cats = {"A": [], "B": [], "C": [], "D": []}
    for k in live:
        if k in rev: cats[rev[k][0]].append((k, rev[k][1]))
        elif k in Dset: cats["D"].append((k, ""))
    wired_toks = [(k, toks(k)) for k in wired]
    def sibling(u):
        tu = toks(u); best = None; bs = 0.0
        for k, tk in wired_toks:
            s = jac(tu, tk)
            if s > bs: bs = s; best = k
        return (best, bs)
    L = []
    L.append("# 状态清单 · 151 条未接 StatId 全量分类（待审）\n")
    L.append("> 数据源：实时 `wirings/` diff（151 条未接）+ `scripts/_classify.json`（Sep-1 裁定 A/B/C/D）。\n")
    L.append("> ⚠️ Sep-1 快照：B/C 中建议的 EFF 行号/引擎读点为当时位置，**接线前须 R3 逐条重核**。\n")
    L.append("> ⚠️ 冲突标记 `⚠️兄弟`：该项被裁定 D（不接）但 sibling_scan 找到强相似已接兄弟，需二次核验。\n")
    L.append("\n## 汇总\n")
    L.append(f"- 总计未接：**{len(live)}**")
    L.append(f"- **A 干净接线(现有EFF)：{len(cats['A'])}** （已全部在前期接完）")
    L.append(f"- **B 仅需新EFF(属性已有读点)：{len(cats['B'])}**")
    L.append(f"- **C 需新属性+新引擎读点：{len(cats['C'])}**")
    L.append(f"- **D 非stat/行为开关(不接)：{len(cats['D'])}**\n")
    L.append("> 真实接线工作量 = B+C = **%d** 条（非 151）。" % (len(cats['B']) + len(cats['C'])))
    for cat, title in [("B", "B · 仅需新 EFF（属性已有引擎读点）"),
                       ("C", "C · 需新属性 + 新引擎读点"),
                       ("D", "D · 非 stat / 行为开关（默认不接）")]:
        L.append(f"\n## {title}（{len(cats[cat])}）\n")
        for k, detail in cats[cat]:
            sb, sc = sibling(k)
            flag = ""
            if cat == "D" and sc >= 0.5:
                flag = f"  `⚠️兄弟 {sb} ({sc:.2f})`"
            line = f"- `{k}`{flag}"
            if detail:
                d = detail.replace("\n", " ")
                if len(d) > 400: d = d[:400] + "…"
                line += f" — {d}"
            L.append(line)
    open(os.path.join(ROOT, "scripts/_Triage_151.md"), "w", encoding="utf-8").write("\n".join(L))
    print("wrote scripts/_Triage_151.md ; rows:", len(live))

if __name__ == "__main__":
    main()
