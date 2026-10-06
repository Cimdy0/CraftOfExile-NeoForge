// 把"不在宝石索引页、但 poe2db 上真实存在"的技能页补进 discovered_v3.json
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const T = path.join(ROOT, 'design/refactor/skill-data/_tools');
const f = path.join(T, 'discovered_v3.json');
const arr = JSON.parse(fs.readFileSync(f, 'utf8'));

const EXTRA = [
  ['Blight', 'skill'], ['Frost_Nova', 'skill'], ['Cyclone', 'skill'], ['Lacerate', 'skill'],
  ['Puncture', 'skill'], ['Toxic_Rain', 'skill'], ['Consume_Corpse', 'skill'],
  ['Ground_Slam', 'skill'], ['Perforate', 'skill'], ['Wrath', 'spirit'], ['Grace', 'spirit']
];
const have = new Set(arr.map(e => e.href));
let added = 0;
for (const [href, t] of EXTRA) {
  if (have.has(href)) { console.log('已有', href); continue; }
  arr.push({
    href, name: href.replace(/_/g, ' '), color: null, filters: '', tier: null,
    icon: null, art: null, seenIn: ['extra:gemless'], gem_type: t,
    is_meta: false, is_lineage: false, is_dnt: false, is_extra: true
  });
  added++;
}
fs.writeFileSync(f, JSON.stringify(arr, null, 1));
console.log('清单', arr.length - added, '->', arr.length, '（新增', added, '）');
