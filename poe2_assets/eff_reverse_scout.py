"""EFF 名反查 scout：为每条未接线条目找「名字上就是为它而生」的 EFF。

思路：EFF 的 id 由 <OP>_ATTRIBUTE_<命名空间>_<属性名>_<标签...> 构成，而 StatIds 常量名
由语料派生。二者在「属性名 + 标签」令牌上应当重合。本脚本按
    score = |EFF令牌 ∩ 常量令牌| , tie-break = -|EFF令牌 - 常量令牌|
排序，输出每个未接线常量的最佳 EFF 候选，供人工逐条核验（R3：不批量臆造）。
"""
import re
import sys
import json
import glob

sys.stdout.reconfigure(encoding='utf-8')

STAT_IDS = 'src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java'
EFF_IDS = 'src/main/java/com/cimdy/coe/data/gen/stat/StatEffectIds.java'
WIRE_DIR = 'src/main/java/com/cimdy/coe/data/gen/stat/wirings/*.java'
NODE_MAP = 'poe2_assets/node_stat_map.json'

OPS = {'ADD_VALUE', 'ADD_MULTIPLIED_BASE', 'ADD_MULTIPLIED_TOTAL', 'REPLACE', 'ATTRIBUTE'}
NS = {'COE', 'MINECRAFT'}
STOP = {
    'INC', 'TO', 'MORE', 'OF', 'AND', 'WITH', 'THE', 'A', 'AN', 'IF', 'WHILE', 'YOU', 'YOUR',
    'HAVE', 'HAVENT', 'VE', 'IS', 'ARE', 'AT', 'ON', 'IN', 'EACH', 'ALL', 'ANY', 'FROM',
    'THAT', 'WHEN', 'FOR', 'BE', 'CAN', 'HAS', 'PER', 'AGAINST', 'ENEMIES', 'ENEMY', 'TARGETS',
    'YOUVE', 'DAMAGE', 'HIT', 'SKILLS', 'DEAL', 'TAKE', 'TAKEN', 'RATE', 'BONUS',
}


def main() -> None:
    allc = re.findall(
        r'public static final Identifier ([A-Z0-9_]+) = id\("([^"]+)"\)',
        open(STAT_IDS, encoding='utf-8').read())
    effs = re.findall(
        r'public static final Identifier (EFF_[A-Z0-9_]+) = id\("([^"]+)"\)',
        open(EFF_IDS, encoding='utf-8').read())
    wired = set()
    for f in glob.glob(WIRE_DIR):
        for m in re.finditer(r'StatIds\.([A-Z0-9_]+)\s*,', open(f, encoding='utf-8').read()):
            wired.add(m.group(1))
    tpl = {}
    for _k, arr in json.load(open(NODE_MAP, encoding='utf-8')).items():
        for e in arr:
            if e.get('constant') and e.get('template'):
                tpl.setdefault(e['constant'], e['template'])

    print(f'constants={len(allc)} effs={len(effs)} wired={len(wired)} templates={len(tpl)}')

    def etok(eid: str) -> set:
        # 注意：EFF 的 id path 是<b>小写</b>（如 add_value_attribute_coe_damage_cold_damage），
        # 而常量名是大写（INC_COLD_DAMAGE）。必须统一成大写再比对，否则令牌永不命中。
        return {x for x in eid.upper().split('_') if x not in OPS and x not in NS}

    res = []
    for c, _p in allc:
        if c in wired or not tpl.get(c):
            continue
        st = {x for x in c.split('_') if x not in STOP}
        if not st:
            continue
        best = None
        for ec, eid in effs:
            et = etok(eid)
            if not et:
                continue
            inter = et & st
            if len(inter) < 2:
                continue
            score = (len(inter), -len(et - st))
            if best is None or score > best[0]:
                best = (score, ec, et, inter)
        if best:
            res.append((best[0][0], c, best[1], sorted(best[3]), tpl[c]))
    res.sort(key=lambda r: (-r[0], r[1]))
    print('candidates:', len(res))
    for sc, c, ec, it, t in res:
        print(f'{sc}  {c}\n      {ec}\n      inter={it}\n      {t}')


if __name__ == '__main__':
    main()
