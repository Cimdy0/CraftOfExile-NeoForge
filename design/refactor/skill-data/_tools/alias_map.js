// 把"本地有但未匹配"的 id 分成三类：① 别名匹配上（爬取里以别的页名存在） ② 需探测真实页名 ③ 真不存在
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data/crawled');
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36';
const sleep = ms => new Promise(r => setTimeout(r, ms));

const all = JSON.parse(fs.readFileSync(path.join(OUT, 'all_skills.json'), 'utf8'));
const E = all.entries;
const N = s => String(s).toLowerCase().replace(/[^a-z0-9]/g, '');
const crawled = E.map(e => ({ e, n: N(e.id), nAlt: N(e.name_en) }));

const MISSING = ['blight', 'frost_nova', 'ice_shard', 'cyclone', 'lacerate', 'lightning_strike',
  'puncture', 'toxic_rain', 'wolf_rake', 'bear_maul', 'consume_corpse', 'ground_slam',
  'perforate', 'shockwave', 'wyvern_rend', 'wrath', 'grace', 'blood_pact',
  'added_chaos_damage', 'added_cold_damage', 'added_fire_damage', 'added_lightning_damage',
  'arbiter_ignition', 'biting_exposure', 'cast_on_crit', 'conductive_exposure',
  'scorching_exposure', 'martial_projectiles', 'fire_infusion'];

(async () => {
  const lines = [];
  for (const m of MISSING) {
    const nm = N(m);
    // ① 模糊匹配：爬取条目里包含或被包含
    const fuzzy = crawled.filter(c =>
      c.n.includes(nm) || nm.includes(c.n) || c.nAlt.includes(nm) || nm.includes(c.nAlt))
      .filter(c => Math.abs(c.n.length - nm.length) <= 6);
    if (fuzzy.length) {
      const best = fuzzy.sort((a, b) => Math.abs(a.n.length - nm.length) - Math.abs(b.n.length - nm.length))[0];
      lines.push(m + ' => 别名命中: ' + best.e.id + ' ("' + best.e.name_en + '", ' + best.e.gem_type + ')');
      console.log(lines[lines.length - 1]);
      continue;
    }
    // ② 探测真实页名
    const pascal = m.split('_').map(w => w[0].toUpperCase() + w.slice(1)).join('_');
    const cands = [pascal, pascal.replace(/_/g, ''), pascal + '_Support', pascal.replace(/s$/, '')];
    let hit = null;
    for (const c of cands) {
      try {
        const r = await fetch('https://poe2db.tw/us/' + c, { headers: { 'User-Agent': UA, 'Referer': 'https://poe2db.tw/us/' } });
        if (r.ok) { const t = await r.text(); if (t.length > 20000) { hit = c; break; } }
      } catch (err) { }
      await sleep(200);
    }
    lines.push(m + ' => ' + (hit ? '需补抓页名: ' + hit : 'poe2db 无此页（本地自定义/已移除）'));
    console.log(lines[lines.length - 1]);
  }
  fs.writeFileSync(path.join(OUT, '_missing_resolution.txt'), lines.join('\n'));
  console.log('\n已写出 _missing_resolution.txt');
})();
