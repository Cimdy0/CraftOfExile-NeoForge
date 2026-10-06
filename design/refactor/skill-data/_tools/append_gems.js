// 把「Class 属于宝石类」的新候选补进 discovered_v3.json
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const T = path.join(ROOT, 'design/refactor/skill-data/_tools');
const cls = JSON.parse(fs.readFileSync(path.join(T, 'extra_classified.json'), 'utf8'));
const GEMCLASS = /^(Skill Gems|Support Gems|Meta Skill Gem|Spirit Gems)$/i;

const newGems = cls.filter(c => c.Class && GEMCLASS.test(c.Class.trim()));
console.log('Class 属宝石类的新候选 =', newGems.length);
newGems.forEach(c => console.log('  ' + c.href.padEnd(30) + c.Class + '  Id=' + (c.Id || '-')));

const f = path.join(T, 'discovered_v3.json');
const arr = JSON.parse(fs.readFileSync(f, 'utf8'));
const have = new Set(arr.map(e => e.href));
const TYPE = { 'Skill Gems': 'skill', 'Support Gems': 'support', 'Meta Skill Gem': 'skill', 'Spirit Gems': 'spirit' };
let added = 0;
for (const c of newGems) {
  if (have.has(c.href)) { console.log('  已有', c.href); continue; }
  arr.push({
    href: c.href, name: c.href.replace(/_/g, ' '), color: null, filters: '', tier: null,
    icon: null, art: null, seenIn: ['extra:discovered'], gem_type: TYPE[c.Class.trim()],
    is_meta: /Meta Skill Gem/i.test(c.Class), is_lineage: false, is_dnt: false, is_extra: true
  });
  added++;
}
fs.writeFileSync(f, JSON.stringify(arr, null, 1));
console.log('\n清单', arr.length - added, '->', arr.length, '（新增', added, '）');
