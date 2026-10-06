// 探测"本地有但爬取清单没有"的 29 个 id 在 poe2db 上是否存在、以什么页名存在
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36';
const H = { 'User-Agent': UA, 'Referer': 'https://poe2db.tw/us/' };
const sleep = ms => new Promise(r => setTimeout(r, ms));

const MISSING = ['blight', 'frost_nova', 'ice_shard', 'cyclone', 'lacerate', 'lightning_strike',
  'puncture', 'toxic_rain', 'wolf_rake', 'bear_maul', 'consume_corpse', 'ground_slam',
  'perforate', 'shockwave', 'wyvern_rend', 'wrath', 'grace', 'blood_pact',
  'added_chaos_damage', 'added_cold_damage', 'added_fire_damage', 'added_lightning_damage',
  'arbiter_ignition', 'biting_exposure', 'cast_on_crit', 'conductive_exposure',
  'scorching_exposure', 'martial_projectiles', 'fire_infusion'];

function cand(name) {
  const c = [];
  const pascal = name.split('_').map(w => w[0].toUpperCase() + w.slice(1)).join('_');
  c.push(pascal);
  c.push(pascal + '_Support');
  c.push(pascal.replace(/_/g, ''));
  return [...new Set(c)];
}

(async () => {
  const v3 = JSON.parse(fs.readFileSync(path.join(ROOT, 'design/refactor/skill-data/_tools/discovered_v3.json'), 'utf8'));
  const crawled = new Set(v3.map(e => e.href.toLowerCase().replace(/[^a-z0-9]/g, '')));
  const out = [];
  for (const m of MISSING) {
    const hits = [];
    for (const c of cand(m)) {
      const k = c.toLowerCase().replace(/[^a-z0-9]/g, '');
      if (crawled.has(k)) { hits.push('清单内:' + c); continue; }
      const r = await fetch('https://poe2db.tw/us/' + c, { headers: H });
      if (r.ok) { const t = await r.text(); if (t.length > 20000) hits.push('HTTP200:' + c); }
      await sleep(250);
    }
    out.push(m + ' -> ' + (hits.join(', ') || '无候选命中'));
    console.log(out[out.length - 1]);
  }
  fs.writeFileSync(path.join(ROOT, 'design/refactor/skill-data/crawled/_missing_probe.txt'), out.join('\n'));
})();
