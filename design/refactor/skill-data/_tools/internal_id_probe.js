/**
 * 仅用 poe2db 爬取数据（all_skills.json）做裁定：
 * 以引擎内部 id（internal_id，不随改名变化）为锚点，穷举剩余 5 条争议
 */
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data/crawled');
const E = JSON.parse(fs.readFileSync(path.join(OUT, 'all_skills.json'), 'utf8')).entries;

console.log('权威条目 =', E.length, ' 有 internal_id 的 =', E.filter(e => e.internal_id).length);

const show = (label, re) => {
  const hits = E.filter(e => re.test(e.internal_id || ''));
  console.log('\n[' + label + '] internal_id 命中 ' + hits.length + ':');
  hits.slice(0, 10).forEach(e => console.log('   ' + (e.internal_id || '').padEnd(38) + e.id.padEnd(28) +
    e.name_en.padEnd(28) + e.gem_type.padEnd(9) + (e.gem_color || '-')));
};

show('Infusion（争议: fire_infusion）', /infusion/i);
show('Martial（争议: martial_projectiles）', /martial/i);
show('Shockwave（争议: shockwave）', /shockwave/i);
show('LightningStrike（争议: lightning_strike）', /lightningstrike/i);
show('BloodPact（争议: blood_pact）', /bloodpact/i);
show('Projectile 数量类（争议: martial_projectiles）', /projectile/i);

console.log('\n=== 现有全部 internal_id（去重、按字母序）===');
const ids = [...new Set(E.map(e => e.internal_id).filter(Boolean))].sort();
console.log('总数 =', ids.length);
console.log(ids.join('\n'));
