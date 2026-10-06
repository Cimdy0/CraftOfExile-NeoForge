const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data/crawled');
const E = JSON.parse(fs.readFileSync(path.join(OUT, 'all_skills.json'), 'utf8')).entries;
const S = E.filter(e => e.gem_type === 'support');

console.log('=== 1. Attunement 家族（Added X Damage 的 PoE2 现行名）===');
S.filter(e => /attunement/i.test(e.name_en)).forEach(e =>
  console.log('  ' + e.id.padEnd(26) + e.name_en.padEnd(24) + (e.gem_color || '-').padEnd(7) +
    (e.internal_id || '') + '  :: ' + ((e.implicits || []).join(' | ') || '').slice(0, 110)));

console.log('\n=== 2. Infusion 家族 ===');
S.filter(e => /infusion/i.test(e.name_en)).forEach(e =>
  console.log('  ' + e.id.padEnd(26) + e.name_en.padEnd(24) + (e.gem_color || '-').padEnd(7) +
    (e.internal_id || '') + '  :: ' + ((e.implicits || []).join(' | ') || '').slice(0, 110)));

console.log('\n=== 3. Exposure 家族（对照 biting/conductive/scorching）===');
S.filter(e => /exposure/i.test(e.name_en)).forEach(e =>
  console.log('  ' + e.id.padEnd(26) + e.name_en.padEnd(24) + (e.gem_color || '-').padEnd(7) +
    (e.internal_id || '') + '  :: ' + ((e.implicits || []).join(' | ') || '').slice(0, 110)));

console.log('\n=== 4. 含"额外投射物/多重"语义的辅助（对照 martial_projectiles / multishot）===');
S.filter(e => /scatter|multishot|multiple|extra|barrage|volley|additional/i.test(e.name_en + ' ' + (e.implicits || []).join(' ')))
  .slice(0, 14).forEach(e =>
    console.log('  ' + e.id.padEnd(26) + e.name_en.padEnd(26) + (e.gem_color || '-').padEnd(7) +
      ' :: ' + ((e.implicits || []).join(' | ') || '').slice(0, 100)));

console.log('\n=== 5. 含物理→元素转换语义的辅助（对照 fire_infusion）===');
S.filter(e => /convert|conversion|physical damage as|gain.*as extra/i.test((e.implicits || []).join(' ') + ' ' + (e.description_en || '')))
  .slice(0, 12).forEach(e =>
    console.log('  ' + e.id.padEnd(26) + e.name_en.padEnd(26) + (e.gem_color || '-').padEnd(7) +
      ' :: ' + ((e.implicits || []).join(' | ') || '').slice(0, 100)));

console.log('\n=== 6. Crit 相关（对照 cast_on_crit）===');
E.filter(e => /cast on crit|critical/i.test(e.name_en)).forEach(e =>
  console.log('  ' + e.id.padEnd(30) + e.name_en.padEnd(28) + e.gem_type.padEnd(9) + (e.gem_color || '-') +
    '  tags=' + (e.tags || []).slice(0, 6).join('/')));

console.log('\n=== 7. Arbiter / Pact / Martial / Strike 直查 ===');
for (const kw of ['Arbiter', 'Pact', 'Martial', 'Strike']) {
  const hits = E.filter(e => new RegExp(kw, 'i').test(e.name_en));
  console.log('  ' + kw.padEnd(10) + hits.map(e => e.id + '(' + e.name_en + ',' + e.gem_type + ')').slice(0, 8).join(', '));
}
