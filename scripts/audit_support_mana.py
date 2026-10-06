#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
辅助宝石「Cost & Reservation Multiplier」全量对照。

权威源：SupportLang.java 的 `gem.skill.description.coe.<gem>` 描述首段 `Mana: N%`。
建模源：Support*Gen.java 中 SupportGemFactory.of(...) 的 costMultiplier 参数
        （6 参重载的倒数第 3 位，形如 `null, 1.3, 20, false`）。

用法：
    python scripts/audit_support_mana.py

输出：逐条列出 权威 Mana% / 建模 cost 及是否一致，末尾汇总不符清单。
注意：只报不符，不改文件（修正由人工核对后落笔）。
"""
import glob
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LANG = os.path.join(ROOT, 'src', 'main', 'java', 'com', 'cimdy', 'coe',
                    'data', 'gen', 'gem', 'support', 'SupportLang.java')
GENS = os.path.join(ROOT, 'src', 'main', 'java', 'com', 'cimdy', 'coe',
                    'data', 'gen', 'gem', 'support', 'Support*Gen.java')

LANG_PAT = re.compile(r'gem\.skill\.description\.coe\.(\w+)",\s*"([^"]+)"')
MANA_PAT = re.compile(r'^Mana:\s*(\d+)%$')
REG_PAT = re.compile(r'reg\(bootstrap,\s*"\w+/(\w+)",\s*SupportGemFactory\.of\((.*?)\)\);', re.S)
COST_PAT = re.compile(r',\s*([\d.]+),\s*(\d+),\s*(true|false)\s*$')


def read(path):
    with open(path, encoding='utf-8') as fh:
        return fh.read()


def load_authoritative():
    """返回 {gem: mana_percent_int}"""
    out = {}
    for m in LANG_PAT.finditer(read(LANG)):
        key, desc = m.group(1), m.group(2)
        for frag in desc.split(';'):
            mm = MANA_PAT.match(frag.strip())
            if mm:
                out[key] = int(mm.group(1))
    return out


def load_modelled():
    """返回 {gem: cost_multiplier_float}"""
    out = {}
    for f in glob.glob(GENS):
        for m in REG_PAT.finditer(read(f)):
            mm = COST_PAT.search(m.group(2))
            if mm:
                out[m.group(1)] = float(mm.group(1))
    return out


def main():
    lang, mod = load_authoritative(), load_modelled()
    both = sorted(set(lang) & set(mod))
    print('有 Mana 权威: %d   可提取建模: %d   交集: %d' % (len(lang), len(mod), len(both)))
    print()
    print('%-26s %10s %10s  判定' % ('gem', '权威Mana', '建模cost'))
    print('-' * 58)
    bad = []
    for k in both:
        exp, act = lang[k] / 100.0, mod[k]
        ok = abs(exp - act) < 1e-9
        if not ok:
            bad.append((k, lang[k], act))
        print('%-26s %9d%% %10.2f  %s' % (k, lang[k], act, 'OK' if ok else '>>> 不符'))
    print()
    print('不符数: %d' % len(bad))
    for k, e, a in bad:
        print('   %-26s 权威 %d%%  建模 %.2f' % (k, e, a))

    only_lang = sorted(set(lang) - set(mod))
    if only_lang:
        print()
        print('有权威但无法提取建模（非 6 参 of 重载，需人工看）: %s' % only_lang)


if __name__ == '__main__':
    main()
