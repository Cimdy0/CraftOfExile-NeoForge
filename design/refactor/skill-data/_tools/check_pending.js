// 核实 datagen-repair 留下的待审批项
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data/crawled');
const E = JSON.parse(fs.readFileSync(path.join(OUT, 'all_skills.json'), 'utf8')).entries;
const find = id => E.find(e => e.id === id);

console.log('=== A. 幽灵 offerings 在权威数据里存在吗 ===');
for (const q of ['bone_offering', 'pain_offering', 'soul_offering']) {
  const a = find(q);
  console.log('  ' + q.padEnd(18) + (a ? ('✅ ' + a.name_en + ' / ' + (a.name_zh || '-') + ' / type=' + a.gem_type + ' / color=' + a.gem_color + ' / tags=' + (a.tags || []).slice(0, 6).join('/')) : '❌ 权威数据无此条目'));
}
const off = E.filter(e => /offering/i.test(e.name_en));
console.log('  权威中所有 *Offering* :', off.map(e => e.id + '(' + e.name_en + ',' + e.gem_type + ')').join(', ') || '(无)');

console.log('\n=== B. malice 的权威归属 ===');
const m = find('malice');
console.log('  ', m ? JSON.stringify({ id: m.id, name: m.name_en, zh: m.name_zh, gem_type: m.gem_type, color: m.gem_color, seen_in: m.seen_in, tags: m.tags }) : '不在权威数据');

console.log('\n=== C. 6 个「有名字无描述」条目（能否从权威补描述）===');
for (const q of ['bloodhound_s_mark', 'wind_serpent_s_fury', 'alchemists_boon', 'trail_of_caltrops', 'combat_frenzy', 'convalescence']) {
  const a = find(q);
  console.log('  ' + q.padEnd(24) + (a ? ('desc=' + (a.description_en ? '"' + a.description_en.slice(0, 90) + '"' : '无') + ' | zh=' + (a.name_zh || '-')) : '权威无'));
}
console.log('\n  权威里 description_en 的非空率:', E.filter(e => e.description_en).length + '/' + E.length);

console.log('\n=== D. 繁体 vs 简体：本地 lang 现状抽样 ===');
const L = path.join(ROOT, 'src/generated/resources/assets/coe/lang/zh_cn.json');
try {
  const j = JSON.parse(fs.readFileSync(L, 'utf8'));
  const keys = Object.keys(j).filter(k => /^gem\.skill\.name\./.test(k)).slice(0, 12);
  keys.forEach(k => console.log('  ' + k.padEnd(44) + j[k]));
} catch (e) { console.log('  读取失败', e.message); }

console.log('\n=== E. 权威 name_zh 是繁体（/tw/ 源）抽样 ===');
E.filter(e => e.name_zh).slice(0, 10).forEach(e => console.log('  ' + e.id.padEnd(26) + e.name_zh));
