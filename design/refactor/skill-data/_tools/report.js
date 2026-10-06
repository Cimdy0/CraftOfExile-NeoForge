// 爬取结果质检：字段完整度、分类计数、等级表长度、样例
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data/crawled');

const all = JSON.parse(fs.readFileSync(path.join(OUT, 'all_skills.json'), 'utf8'));
const E = all.entries;
console.log('总条目 =', E.length, '| meta', JSON.stringify(all.meta));

const F = ['name_en', 'name_zh', 'description_en', 'cost', 'cast_time', 'crit_chance',
  'requirements', 'quality_bonus', 'internal_id', 'icon_url', 'tags', 'type_tags',
  'base_type', 'target_types', 'level_range', 'attack_speed'];
console.log('\n--- 字段非 null 占比 ---');
for (const f of F) {
  const n = E.filter(e => {
    const v = e[f];
    if (v == null) return false;
    if (Array.isArray(v)) return v.length > 0;
    return String(v).trim() !== '';
  }).length;
  console.log('  ' + f.padEnd(18) + String(n).padStart(5) + '  ' + (n / E.length * 100).toFixed(1) + '%');
}
const withLv = E.filter(e => e.level_table && e.level_table.rows && e.level_table.rows.length);
console.log('\n有等级表 =', withLv.length, ' 无等级表 =', E.length - withLv.length);
const lens = withLv.map(e => e.level_table.rows.length).sort((a, b) => a - b);
console.log('等级表行数: min', lens[0], ' median', lens[Math.floor(lens.length / 2)], ' max', lens[lens.length - 1]);
console.log('max_level 分布 top:', (() => {
  const m = {}; withLv.forEach(e => { m[e.max_level] = (m[e.max_level] || 0) + 1; });
  return Object.entries(m).sort((a, b) => b[1] - a[1]).slice(0, 8).map(x => x[0] + ':' + x[1]).join(' ');
})());

console.log('\n--- gem_type 计数 ---');
const t = {}; E.forEach(e => t[e.gem_type] = (t[e.gem_type] || 0) + 1);
console.log(JSON.stringify(t));
const c = {}; E.forEach(e => c[e.gem_color] = (c[e.gem_color] || 0) + 1);
console.log('颜色:', JSON.stringify(c));

console.log('\n--- 标签全集（GemTags，top 40）---');
const tg = {}; E.forEach(e => (e.tags || []).forEach(x => tg[x] = (tg[x] || 0) + 1));
console.log(Object.entries(tg).sort((a, b) => b[1] - a[1]).slice(0, 40).map(x => x[0] + '(' + x[1] + ')').join(', '));
console.log('标签总数 =', Object.keys(tg).length);

console.log('\n--- 样例：Solar_Orb ---');
const s = E.find(e => e.id === 'solar_orb');
if (s) {
  const { level_table, ...rest } = s;
  console.log(JSON.stringify(rest, null, 1));
  console.log('level_table.headers =', JSON.stringify(level_table.headers));
  console.log('level_table.rows[0..2] =', JSON.stringify(level_table.rows.slice(0, 3)));
  console.log('level_table 行数 =', level_table.rows.length);
}
console.log('\n--- 样例：中文名 ---');
console.log(E.filter(e => e.name_zh).slice(0, 8).map(e => e.name_en + ' = ' + e.name_zh).join('\n'));
console.log('有中文名 =', E.filter(e => e.name_zh).length);
