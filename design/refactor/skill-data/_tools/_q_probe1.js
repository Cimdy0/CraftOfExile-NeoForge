/** 探测 all_skills.json 中 quality_bonus / quality_range 的实际形态 */
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data/crawled');
const J = JSON.parse(fs.readFileSync(path.join(OUT, 'all_skills.json'), 'utf8'));
console.log('top keys =', Object.keys(J));
const E = J.entries;
console.log('entries =', E.length);
console.log('sample keys of entry[0] =', JSON.stringify(Object.keys(E[0])));

// 统计字段出现情况
const keyCount = new Map();
for (const e of E) for (const k of Object.keys(e)) keyCount.set(k, (keyCount.get(k) || 0) + 1);
console.log('\n字段出现次数:');
[...keyCount.entries()].sort((a, b) => b[1] - a[1]).forEach(([k, v]) => console.log('  ' + k.padEnd(28) + v));

// quality_bonus 出现次数
const hasQB = E.filter(e => e.quality_bonus !== undefined && e.quality_bonus !== null);
console.log('\nquality_bonus 非空条目 =', hasQB.length);
const hasQR = E.filter(e => e.quality_range !== undefined && e.quality_range !== null);
console.log('quality_range 非空条目 =', hasQR.length);

// 类型分布
const tOf = new Map();
for (const e of hasQB) { const t = typeof e.quality_bonus + (Array.isArray(e.quality_bonus) ? '[]' : ''); tOf.set(t, (tOf.get(t) || 0) + 1); }
console.log('quality_bonus 类型分布 =', JSON.stringify([...tOf]));

console.log('\n=== 前 30 条 quality_bonus 原文 ===');
hasQB.slice(0, 30).forEach(e => {
  console.log('[' + e.id + '] qb=' + JSON.stringify(e.quality_bonus) + '  qr=' + JSON.stringify(e.quality_range));
});

// gem_type 分布
const gt = new Map();
for (const e of E) gt.set(e.gem_type, (gt.get(e.gem_type) || 0) + 1);
console.log('\ngem_type 分布 =', JSON.stringify([...gt]));
const gt2 = new Map();
for (const e of hasQB) gt2.set(e.gem_type, (gt2.get(e.gem_type) || 0) + 1);
console.log('含 quality_bonus 的 gem_type 分布 =', JSON.stringify([...gt2]));
