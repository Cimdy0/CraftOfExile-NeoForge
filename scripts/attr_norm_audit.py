"""属性归一化 + 接线四向交叉检测（静态扫描，禁 runData）。

四项检查：
  A. 归一化：属性 id 含条件/时序/关系词 → 该语义应落 Condition / Tag / Scope，而非新建属性。
  B. 悬空属性：注册了但 StatEffectGen 无任何 EFF 引用 → 不可被 Stat 授予（UNUSED ATTR）。
  C. 悬空 EFF：StatEffectGen 注册了但 Wire* 未接线 → Stat 侧拿不到（DANGLING EFF）。
  D. 死线属性：属性无任何「引擎读点」（data/gen 之外、非注册类本身的 AttributeRegister.X 引用）。

用法：python scripts/attr_norm_audit.py
"""
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "src/main/java")
ATTR_FILE = os.path.join(SRC, "com/cimdy/coe/data/AttributeRegister.java")
EFF_GEN = os.path.join(SRC, "com/cimdy/coe/data/gen/stat/StatEffectGen.java")
WIRE_DIR = os.path.join(SRC, "com/cimdy/coe/data/gen/stat/wirings")

# 归一化违禁词：条件/时序/关系语义不应固化进属性 id
COND_WORDS = [
    "recently", "recent", "while", "when", "during", "against", "at_least",
    "instead_of", "unless", "below", "above", "near_", "if_", "_if",
]


def read(p):
    try:
        return open(p, encoding="utf-8").read()
    except OSError:
        return ""


def walk_java():
    for dp, _, fs in os.walk(SRC):
        for f in fs:
            if f.endswith(".java"):
                yield os.path.join(dp, f)


def is_gen(path):
    p = path.replace("\\", "/")
    return "/data/gen/" in p


def main():
    attr_txt = read(ATTR_FILE)
    # NAME -> id
    attrs = re.findall(
        r'public static Holder<Attribute>\s+(\w+)\s*=\s*register\w*\(\s*"([^"]+)"', attr_txt)
    attr_ids = {name: aid for name, aid in attrs}

    # EFF -> attr（StatEffectGen）
    gen_txt = read(EFF_GEN)
    eff_to_attr = {}
    for m in re.finditer(r'm\.put\(StatEffectIds\.(\w+)\s*,', gen_txt):
        eff = m.group(1)
        win = gen_txt[m.end():m.end() + 500]
        am = re.search(r'AttributeRegister\.(\w+)', win)
        if am:
            eff_to_attr[eff] = am.group(1)

    # Wire -> EFF
    wired_effs = set()
    for f in os.listdir(WIRE_DIR):
        if not f.endswith(".java"):
            continue
        wired_effs.update(re.findall(r'StatEffectIds\.(\w+)', read(os.path.join(WIRE_DIR, f))))

    # 引擎读点：data/gen 之外对 AttributeRegister.X 的引用
    # 例外：StatEffectGen.java 虽在 gen 包，但它把属性作为构造参数交给 EFF（如 durationAttr），
    # 由引擎在运行时 fold 读取——这类「以引用传入」不算死线（#61：puppet_master_duration 假阳性即由此而来）。
    engine_reads = set()
    for path in walk_java():
        if path.endswith("AttributeRegister.java"):
            continue
        if is_gen(path) and not path.replace("\\", "/").endswith("/data/gen/stat/StatEffectGen.java"):
            continue
        engine_reads.update(re.findall(r'AttributeRegister\.(\w+)', read(path)))

    print("=" * 72)
    print(f"属性总数 {len(attrs)}   EFF→属性映射 {len(eff_to_attr)}   Wire 引用 EFF {len(wired_effs)}")
    print("=" * 72)

    # A. 归一化
    print("\n[A] 归一化违规：属性 id 含条件/时序/关系词")
    hits = []
    for name, aid in attrs:
        low = aid.lower()
        words = [w for w in COND_WORDS if w in low]
        if words:
            has_eff = name in set(eff_to_attr.values())
            hits.append((aid, name, words, has_eff))
    if not hits:
        print("    NONE")
    for aid, name, words, has_eff in sorted(hits):
        print(f"    {aid:<52} {name:<46} 词={','.join(words)} 已接EFF={has_eff}")
    print(f"    合计 {len(hits)}")

    # B. 悬空属性
    # 白名单：语料（StatIds，602 条词条）中**不存在**对应词条的系统内部属性——由天赋/技能数据/子系统内部
    # 直接写入，本就不该经 Stat 授予；命中白名单即视为合法，不计悬空（#62）。
    WHITELIST = {
        "WORK_APTITUDE": "工作系统内部",
        "WORK_SPEED": "工作系统内部",
        "MAX_DARKNESS": "darkness 子系统内部",
        "DARKNESS_RESERVATION_LASTS": "darkness 子系统内部",
        "BASE_ENERGY_SHIELD_RECHARGE": "护盾回复中间量",
        "CURRENT_ENERGY_SHIELD_RECHARGE": "护盾回复中间量",
        "AILMENT_DAMAGE_CONTRIBUTION": "异常贡献度中间量",
        "AILMENT_THRESHOLD_ADAPTATION": "异常阈值适应中间量",
        "GUARD_RECOVERY": "guard 子系统内部（语料无对应词条）",
        "ECHO_COUNT": "echo 子系统内部（由技能数据直接给）",
        "AVOID_AILMENT_CHANCE": "语料无「avoid ailment」词条",
        "LEECHES_RESPONSE": "语料无对应词条",
        "PROJECTILE_RANDOM_FIRE": "语料无对应词条",
        "TALENT_AUTO_SMELT": "天赋功能维度（天赋树直接写）",
        "TALENT_EXTRA_XP": "天赋功能维度（天赋树直接写）",
        "TALENT_FORTUNE": "天赋功能维度（天赋树直接写）",
        "TALENT_LOOTING": "天赋功能维度（天赋树直接写）",
        "TALENT_MAGNET": "天赋功能维度（天赋树直接写）",
        "TALENT_MENDING": "天赋功能维度（天赋树直接写）",
        "ITEM_CAPACITY": "系统容量（非词条维度）",
        "SKILL_SLOT_COUNT": "系统容量（非词条维度）",
        "RAGE_REGEN_PER_SECOND": "语料无「自身每秒回怒」词条（仅 presence 授予通道）",
    }
    used_attrs = set(eff_to_attr.values())
    print("\n[B] 悬空属性：无 EFF 引用（Stat 侧不可授予）")
    dangling_attr = [(n, i) for n, i in attrs if n not in used_attrs and n not in WHITELIST]
    wl = [(n, i) for n, i in attrs if n not in used_attrs and n in WHITELIST]
    if not dangling_attr:
        print("    NONE")
    for n, i in dangling_attr:
        print(f"    {i:<52} {n}")
    print(f"    合计 {len(dangling_attr)}（另有白名单 {len(wl)} 条：语料无对应词条的系统内部属性）")
    for n, i in wl:
        print(f"    [白名单] {i:<46} {n:<34} {WHITELIST[n]}")

    # C. 悬空 EFF
    print("\n[C] 悬空 EFF：StatEffectGen 已注册但 Wire* 未接线")
    dangling_eff = sorted(e for e in eff_to_attr if e not in wired_effs)
    if not dangling_eff:
        print("    NONE")
    for e in dangling_eff:
        print(f"    {e:<52} -> {eff_to_attr[e]}")
    print(f"    合计 {len(dangling_eff)}")

    # D. 死线属性
    print("\n[D] 死线属性：无引擎读点（data/gen 之外无 AttributeRegister.X 引用）")
    dead = sorted(n for n in attr_ids if n not in engine_reads)
    if not dead:
        print("    NONE")
    for n in dead:
        print(f"    {attr_ids[n]:<52} {n}")
    print(f"    合计 {len(dead)}")
    print()


if __name__ == "__main__":
    sys.exit(main())
