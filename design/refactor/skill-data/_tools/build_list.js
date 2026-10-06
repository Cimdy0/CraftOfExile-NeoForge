/**
 * 定版清单 discovered_v3.json
 * - 抓 Meta_Gems 等剩余 tab，确认 Meta 29 的归属
 * - 每个 href 记录 seenIn（多页归属）与 gem_type
 */
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const PD = path.join(ROOT, 'design/refactor/skill-data/crawled/raw/index_pages');
const T = path.join(ROOT, 'design/refactor/skill-data/_tools');
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36';
const H = { 'User-Agent': UA, 'Referer': 'https://poe2db.tw/us/' };
const sleep = ms => new Promise(r => setTimeout(r, ms));
fs.mkdirSync(PD, { recursive: true });

async function get(u) {
  for (let i = 0; i < 3; i++) {
    try {
      const c = new AbortController(); const to = setTimeout(() => c.abort(), 40000);
      const r = await fetch(u, { headers: H, signal: c.signal }); clearTimeout(to);
      if (r.status === 404) return null;
      if (!r.ok) throw new Error('HTTP ' + r.status);
      return await r.text();
    } catch (e) { await sleep(900); }
  }
  return null;
}
async function page(name) {
  const f = path.join(PD, name + '.html');
  if (fs.existsSync(f)) return fs.readFileSync(f, 'utf8');
  const t = await get('https://poe2db.tw/us/' + name);
  if (t) fs.writeFileSync(f, t);
  await sleep(300);
  return t;
}

// 行切分；也支持无 data-filters 的普通表（Lineage / Meta）
function parseIndex(html) {
  const out = new Map();
  // A) data-filters 行
  const parts = html.split(/<tr\s+data-filters="/);
  for (let i = 1; i < parts.length; i++) {
    const seg = parts[i]; const q = seg.indexOf('"'); if (q < 0) continue;
    const filters = seg.slice(0, q); const body = seg.slice(q + 1);
    const hm = body.match(/href="\/us\/([^"]+)"/); if (!hm) continue;
    const cm = body.match(/class="(gem_(?:red|green|blue))"/);
    const tm = body.match(/\(\s*(\d+)\s*\)/);
    const im = body.match(/src="(https:\/\/cdn\.poe2db\.tw\/image\/[^"]+)"/);
    const am = body.match(/alt="([^"]*)"/);
    let name = null;
    const tail = body.replace(/<div class="gem_tags[\s\S]*$/, '');
    const cand = [...tail.matchAll(/<a\b[^>]*href="\/us\/[^"]+"[^>]*>([^<]+)<\/a>/g)].map(m => m[1].trim()).filter(Boolean);
    name = (cand.length ? cand[cand.length - 1] : hm[1].replace(/_/g, ' ')).replace(/\s*\(\d+\)\s*$/, '').trim();
    const prev = out.get(hm[1]);
    const rec = {
      href: hm[1], name, color: cm ? cm[1].slice(4) : null,
      filters: filters, tier: tm ? parseInt(tm[1], 10) : null,
      icon: im ? im[1] : null, art: am ? am[1] : null
    };
    if (!prev) out.set(hm[1], rec);
    else {
      if (!prev.color && rec.color) prev.color = rec.color;
      if (!prev.icon && rec.icon) prev.icon = rec.icon;
      if (!prev.filters && rec.filters) prev.filters = rec.filters;
      if (prev.tier == null && rec.tier != null) prev.tier = rec.tier;
    }
  }
  // B) 兜底：任何指向 /us/X 且带 gem_ 颜色 class 的锚点
  for (const m of html.matchAll(/<a\b[^>]*class="(gem_(?:red|green|blue))"[^>]*href="\/us\/([^"]+)"[^>]*>([^<]*)<\/a>/g)) {
    if (out.has(m[2])) continue;
    out.set(m[2], { href: m[2], name: (m[3] || m[2].replace(/_/g, ' ')).trim(), color: m[1].slice(4), filters: '', tier: null, icon: null, art: null, loose: true });
  }
  return out;
}

(async () => {
  const PAGES = ['Skill_Gems', 'Support_Gems', 'Spirit_Gems', 'Gems', 'Lineage_Supports', 'Meta_Gems'];
  const all = new Map();
  for (const p of PAGES) {
    const t = await page(p);
    if (!t) { console.log(p.padEnd(18), 'FETCH FAIL'); continue; }
    const m = parseIndex(t);
    const rows = t.split('data-filters="').length - 1;
    console.log(p.padEnd(18), 'rows=' + String(rows).padStart(5), 'unique=' + String(m.size).padStart(4), 'len=' + t.length);
    for (const [href, rec] of m) {
      const prev = all.get(href);
      if (!prev) all.set(href, { ...rec, seenIn: [p] });
      else {
        if (!prev.seenIn.includes(p)) prev.seenIn.push(p);
        if (!prev.color && rec.color) prev.color = rec.color;
        if (!prev.icon && rec.icon) prev.icon = rec.icon;
        if (!prev.filters && rec.filters) prev.filters = rec.filters;
        if (prev.tier == null && rec.tier != null) prev.tier = rec.tier;
      }
    }
  }

  const arr = [...all.values()];
  for (const e of arr) {
    const s = e.seenIn;
    // 互斥主类型：support > spirit > skill（spirit ⊆ skill，故 spirit 优先）
    if (s.includes('Support_Gems')) e.gem_type = 'support';
    else if (s.includes('Spirit_Gems')) e.gem_type = 'spirit';
    else if (s.includes('Skill_Gems')) e.gem_type = 'skill';
    else if (s.includes('Meta_Gems')) e.gem_type = 'skill';
    else e.gem_type = 'unknown';
    // meta 是叠加标志，不是互斥类型（呼应铁律 3：分类可交叉）
    e.is_meta = s.includes('Meta_Gems');
    e.is_lineage = s.includes('Lineage_Supports');
    e.is_dnt = /^DNT[-_]/i.test(e.href) || /^DNT_/i.test(e.href);
  }
  const c = {}; arr.forEach(e => c[e.gem_type] = (c[e.gem_type] || 0) + 1);
  console.log('\n合计唯一 =', arr.length, ' 类型:', JSON.stringify(c));
  const cc = {}; arr.forEach(e => cc[e.color || '(null)'] = (cc[e.color || '(null)'] || 0) + 1);
  console.log('颜色:', JSON.stringify(cc));
  const meta = arr.filter(e => e.gem_type === 'meta');
  console.log('meta 条目:', meta.map(e => e.href).slice(0, 35).join(', ') || '(无)');
  const unk = arr.filter(e => e.gem_type === 'unknown');
  console.log('unknown 条目数 =', unk.length, unk.slice(0, 20).map(e => e.href).join(', '));

  fs.writeFileSync(path.join(T, 'discovered_v3.json'), JSON.stringify(arr, null, 1));
  console.log('\n已写出 discovered_v3.json');
})();
