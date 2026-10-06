const fs = require('fs');
const path = require('path');
const zlib = require('zlib');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data/crawled');
const E = JSON.parse(fs.readFileSync(path.join(OUT, 'all_skills.json'), 'utf8')).entries;
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36';

function strip(s) { return s.replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim(); }

// A) 权威数据里"给技能加伤害"的辅助（判定 added_*_damage 的等价物）
console.log('=== A. 权威辅助里 "Adds ... Damage" 类 ===');
const ad = E.filter(e => e.gem_type === 'support' &&
  /adds?\s+[\d(]|adds?\s+\w*\s*damage/i.test(((e.implicits || []).join(' ') + ' ' + (e.description_en || ''))));
ad.slice(0, 20).forEach(e => {
  const txt = ((e.implicits || []).join(' | ') + ' ' + (e.description_en || '')).slice(0, 150);
  console.log('  ' + e.id.padEnd(32) + e.name_en.padEnd(26) + e.gem_color + '  :: ' + txt);
});
console.log('  合计', ad.length, '条');

// B) 本地 11 个未匹配 support 的真实定义
console.log('\n=== B. 本地未匹配 support 的定义 ===');
const GD = path.join(ROOT, 'src/generated/resources/data/coe/coe/item/gem/support');
const LOCALS = ['added_chaos_damage', 'added_cold_damage', 'added_fire_damage', 'added_lightning_damage',
  'arbiter_ignition', 'biting_exposure', 'cast_on_crit', 'conductive_exposure',
  'scorching_exposure', 'martial_projectiles', 'fire_infusion'];
for (const l of LOCALS) {
  let found = null;
  for (const col of ['red', 'green', 'blue']) {
    const f = path.join(GD, col, l + '.json');
    if (fs.existsSync(f)) { found = { col, json: JSON.parse(fs.readFileSync(f, 'utf8')) }; break; }
  }
  if (!found) { console.log('  ' + l.padEnd(26) + '(无文件)'); continue; }
  const j = found.json;
  console.log('  ' + l.padEnd(26) + '[' + found.col + '] tag=' + j.tag + ' lineage=' + j.lineage +
    ' effects=' + JSON.stringify(j.effects).slice(0, 130));
}

// C) Fire_Infusion / Shockwave / Lightning_Strike / Blood_Pact / Martial_Projectiles 页的真实身份
console.log('\n=== C. 争议页真实身份（读详情页元数据表）===');
async function probe(name) {
  const f = path.join(OUT, 'raw/html_gz', name + '.html.gz');
  let h = null;
  if (fs.existsSync(f)) h = zlib.gunzipSync(fs.readFileSync(f)).toString('utf8');
  else {
    try {
      const r = await fetch('https://poe2db.tw/us/' + name, { headers: { 'User-Agent': UA, 'Referer': 'https://poe2db.tw/us/' } });
      if (r.ok) h = await r.text();
    } catch (e) { }
  }
  if (!h) { console.log('  ' + name.padEnd(22) + '页面取不到'); return; }
  const meta = {};
  for (const m of (h.match(/<table[^>]*>[\s\S]*?<\/table>/g) || [])) {
    if (!/Show Full Descriptions/.test(m)) continue;
    const rows = [...m.matchAll(/<tr[^>]*>([\s\S]*?)<\/tr>/g)].map(r =>
      [...r[1].matchAll(/<td[^>]*>([\s\S]*?)<\/td>/g)].map(c => strip(c[1])));
    for (const r of rows) if (r.length >= 2) meta[r[0].replace(/\s+/g, ' ')] = r[1];
  }
  const tags = [...new Set([...h.matchAll(/class="[^"]*GemTags[^"]*"[^>]*>([^<]+)</g)].map(x => x[1]))];
  console.log('  ' + name.padEnd(22) + 'Id=' + (meta.Id || '?') + ' | BaseType=' + (meta.BaseType || '?') +
    ' | Class=' + (meta.Class || '?').slice(0, 30) + ' | Type=' + (meta.Type || '').slice(0, 80) +
    ' | tags=' + tags.slice(0, 8).join('/'));
}
(async () => {
  for (const n of ['Fire_Infusion', 'Shockwave', 'Lightning_Strike', 'Blood_Pact', 'Martial_Projectiles', 'Blight', 'Cyclone', 'Puncture']) await probe(n);
})();
