/**
 * 建立 本地 datagen id ↔ poe2db 权威 id 的完整对照表
 * 仅以爬取数据 all_skills.json 为权威
 */
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data/crawled');
const GEMDIR = path.join(ROOT, 'src/generated/resources/data/coe/coe/item/gem');
const E = JSON.parse(fs.readFileSync(path.join(OUT, 'all_skills.json'), 'utf8')).entries;

const N = s => String(s).toLowerCase().replace(/[^a-z0-9]/g, '');
// tier 后缀：_i/_ii/_iii/_iv/_v、_four/_five、以及 poe2db 的 Two/Three/Four 后缀
const stripTier = s => s
  .replace(/_(i|ii|iii|iv|v|four|five)$/i, '')
  .replace(/(two|three|four|five)$/i, '');
const norm = s => N(stripTier(String(s)))
  .replace(/armour/g, 'armor')
  .replace(/s$/, '');   // 单复数宽松

// 权威索引
const byNorm = new Map();          // normalized id -> entries[]
const byInternal = new Map();      // normalized internal_id -> entries[]
for (const e of E) {
  const k = norm(e.id);
  if (!byNorm.has(k)) byNorm.set(k, []);
  byNorm.get(k).push(e);
  if (e.internal_id) {
    const ki = N(e.internal_id).replace(/support$/, '');
    if (!byInternal.has(ki)) byInternal.set(ki, []);
    byInternal.get(ki).push(e);
  }
}

// 本地 id
function walk(d, out = [], cat = null) {
  let es; try { es = fs.readdirSync(d, { withFileTypes: true }); } catch (e) { return out; }
  for (const e of es) {
    const f = path.join(d, e.name);
    if (e.isDirectory()) walk(f, out, cat || (['skill', 'spirit', 'support'].includes(e.name) ? e.name : null));
    else if (e.name.endsWith('.json')) out.push({ cat, id: e.name.replace(/\.json$/, '') });
  }
  return out;
}
const local = walk(GEMDIR).filter(x => x.cat && !/^(preset_|generic_)/.test(x.id));

// 显式别名（来自 name_resolution，全部以 internal_id 为铁证）
const ALIAS = {
  added_fire_damage: 'fire_attunement', added_cold_damage: 'cold_attunement',
  added_lightning_damage: 'lightning_attunement', added_chaos_damage: 'chaos_attunement',
  biting_exposure: 'cold_exposure', conductive_exposure: 'lightning_exposure',
  scorching_exposure: 'fire_exposure', arbiter_ignition: 'arbiters_ignition',
  cast_on_crit: 'cast_on_critical', ice_shard: 'ice_shards',
  wolf_rake: 'rake', bear_maul: 'maul', wyvern_rend: 'rend',
};

const rows = [];
for (const l of local) {
  const ln = norm(l.id);
  let hit = null, how = null;
  if (ALIAS[l.id]) { hit = E.find(e => e.id === ALIAS[l.id]); how = 'alias'; }
  if (!hit && byNorm.has(ln)) { hit = byNorm.get(ln)[0]; how = 'id-normalized'; }
  if (!hit && byInternal.has(N(l.id))) { hit = byInternal.get(N(l.id))[0]; how = 'internal_id'; }
  if (!hit) {
    // 宽松：本地 id 作为 internal_id 前缀/包含
    const cand = [...byInternal.entries()].filter(([k]) => k.includes(N(l.id)) || N(l.id).includes(k));
    if (cand.length) { hit = cand[0][1][0]; how = 'internal_id-fuzzy'; }
  }
  rows.push({
    local_id: l.id, local_cat: l.cat,
    authoritative_id: hit ? hit.id : null,
    authoritative_name: hit ? hit.name_en : null,
    authoritative_zh: hit ? (hit.name_zh || null) : null,
    authoritative_type: hit ? hit.gem_type : null,
    authoritative_color: hit ? hit.gem_color : null,
    internal_id: hit ? (hit.internal_id || null) : null,
    matched_by: how,
    renamed: !!(hit && hit.id !== l.id)
  });
}

const ok = rows.filter(r => r.authoritative_id);
const bad = rows.filter(r => !r.authoritative_id);
console.log('本地条目 =', rows.length, ' 已对上权威 =', ok.length, ' 未对上 =', bad.length);
const byHow = {}; ok.forEach(r => byHow[r.matched_by] = (byHow[r.matched_by] || 0) + 1);
console.log('匹配方式:', JSON.stringify(byHow));
console.log('\n需重命名（本地 id ≠ 权威 id）:');
ok.filter(r => r.renamed).forEach(r => console.log('  ' + r.local_id.padEnd(24) + '=> ' + r.authoritative_id.padEnd(24) + '[' + r.matched_by + ']  ' + r.authoritative_name));
console.log('\n仍未对上权威:', bad.length);
bad.forEach(r => console.log('  ' + r.local_cat.padEnd(8) + r.local_id));
console.log('\n分类一致性检查（本地分类 vs 权威类型）:');
const mism = ok.filter(r => r.authoritative_type && r.local_cat !== r.authoritative_type);
mism.forEach(r => console.log('  ' + r.local_id.padEnd(24) + 'local=' + r.local_cat + ' -> auth=' + r.authoritative_type + ' (' + r.authoritative_id + ')'));
console.log('  不一致 =', mism.length);

fs.writeFileSync(path.join(OUT, 'crosswalk.json'), JSON.stringify({
  note: '本地 gem id ↔ poe2db 权威 id 对照。matched_by: alias | id-normalized | internal_id | internal_id-fuzzy',
  generated_at: new Date().toISOString(),
  local_total: rows.length, matched: ok.length, unmatched: bad.length,
  entries: rows
}, null, 1));
console.log('\n已写出 crosswalk.json');
