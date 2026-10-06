/**
 * 以爬取到的 poe2db 资料为准，裁定 18 个命名歧义
 * 手段：在权威数据里做名称/描述语义检索，给出证据链
 */
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data/crawled');
const E = JSON.parse(fs.readFileSync(path.join(OUT, 'all_skills.json'), 'utf8')).entries;

const desc = e => ((e.description_en || '') + ' ' + (e.implicits || []).join(' ') + ' ' + (e.quality_bonus || ''));
const list = t => E.filter(e => e.gem_type === t);

console.log('=== 权威数据里的辅助宝石名（含 added / infusion / exposure / ignition 等关键词）===');
list('support').filter(e => /added|infusion|exposure|ignition|penetrat|projectile|critical|crit/i.test(e.name_en))
  .forEach(e => console.log('  ' + e.id.padEnd(34) + e.name_en + '  | ' + e.gem_color + ' | ' + (e.tags || []).slice(0, 6).join('/')));

console.log('\n=== 权威数据里的技能名（含 strike / pact / shard / rake / maul / rend / shock / cyclone 等）===');
list('skill').concat(list('spirit')).filter(e => /strike|pact|shard|rake|maul|rend|shock|cyclone|lacerate|puncture|blight|nova/i.test(e.name_en))
  .forEach(e => console.log('  ' + e.id.padEnd(34) + e.name_en + '  | ' + e.gem_color + ' | ' + (e.tags || []).slice(0, 6).join('/')));

console.log('\n=== 关键词描述检索（用于判定语义等价）===');
const QUERIES = {
  'added_fire_damage': /adds? .{0,20}fire damage/i,
  'added_cold_damage': /adds? .{0,20}cold damage/i,
  'added_lightning_damage': /adds? .{0,20}lightning damage/i,
  'added_chaos_damage': /adds? .{0,20}chaos damage/i,
  'fire_infusion': /fire infusion|infus/i,
  'blood_pact': /blood pact/i,
  'lightning_strike': /lightning strike/i,
  'martial_projectiles': /martial|projectile/i,
  'biting_exposure': /exposure/i,
  'conductive_exposure': /conductive|exposure/i,
  'scorching_exposure': /scorch/i,
  'arbiter_ignition': /arbiter|ignit/i,
};
for (const [k, re] of Object.entries(QUERIES)) {
  const hits = E.filter(e => re.test(desc(e))).slice(0, 5);
  console.log('\n[' + k + '] 描述命中 ' + hits.length + ' 条:');
  hits.forEach(h => console.log('   ' + h.id + ' ("' + h.name_en + '", ' + h.gem_type + ') :: ' + (h.description_en || '').slice(0, 110)));
}

console.log('\n=== 关键词名称全库检索 ===');
for (const kw of ['Strike', 'Pact', 'Shard', 'Rake', 'Maul', 'Rend', 'Shockwave', 'Cyclone', 'Lacerate', 'Puncture', 'Blight', 'Nova', 'Grace', 'Wrath']) {
  const hits = E.filter(e => new RegExp('(^|\\s)' + kw, 'i').test(e.name_en)).slice(0, 6).map(e => e.id + '/' + e.gem_type);
  console.log('  ' + kw.padEnd(12) + (hits.join(', ') || '(无)'));
}
