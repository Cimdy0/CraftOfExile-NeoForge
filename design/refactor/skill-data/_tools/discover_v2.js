/**
 * 发现 v2 —— 按 <tr data-filters="..."> 行切分（该页 <tr> 无闭合标签）
 * 目标：与 poe2db 官方计数对齐
 *   Skill_Gems 427 / Support_Gems 557 / Spirit_Gems 53 / Meta Skill Gem 29 / Gems 1066
 */
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const T = path.join(ROOT, 'design/refactor/skill-data/_tools');
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36';
const H = { 'User-Agent': UA, 'Referer': 'https://poe2db.tw/us/' };
const sleep = ms => new Promise(r => setTimeout(r, ms));

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
function strip(s) {
  return s.replace(/<span class="ndash">[^<]*<\/span>/g, '-').replace(/<[^>]+>/g, '')
    .replace(/&amp;/g, '&').replace(/&quot;/g, '"').replace(/&#39;/g, "'").replace(/\s+/g, ' ').trim();
}

// 按 data-filters 行切分；每行提取 gem href / 名称 / 颜色 / tier / 图标
function parseRows(html, pageName, gemType) {
  const out = [];
  const parts = html.split(/<tr\s+data-filters="/);
  for (let i = 1; i < parts.length; i++) {
    const seg = parts[i];
    const q = seg.indexOf('"');
    if (q < 0) continue;
    const filters = seg.slice(0, q);
    const body = seg.slice(q + 1);

    const hm = body.match(/href="\/us\/([^"]+)"/);
    if (!hm) continue;
    const href = hm[1];

    const cm = body.match(/class="(gem_(?:red|green|blue))"/);
    const color = cm ? cm[1].slice(4) : null;

    let name = null;
    const tail = body.replace(/<div class="gem_tags[\s\S]*$/, '');
    const anchors = [...tail.matchAll(/<a\b[^>]*>([^<]*)<\/a>/g)].map(m => m[1].trim()).filter(x => x && !/^\s*$/.test(x));
    // 名称锚点：文本非空且不是纯图标
    const cand = [...tail.matchAll(/<a\b[^>]*href="\/us\/[^"]+"[^>]*>([^<]+)<\/a>/g)].map(m => m[1].trim());
    name = cand.length ? cand[cand.length - 1] : (anchors[0] || href.replace(/_/g, ' '));
    name = name.replace(/\s*\(\d+\)\s*$/, '').trim();

    const tm = body.match(/\(\s*(\d+)\s*\)/);
    const tier = tm ? parseInt(tm[1], 10) : null;

    const im = body.match(/src="(https:\/\/cdn\.poe2db\.tw\/image\/[^"]+)"/);
    const alt = body.match(/alt="([^"]*)"/);

    out.push({
      href, name, color, filters, tier: tier,
      icon: im ? im[1] : null, art: alt ? alt[1] : null,
      page: pageName, gemType
    });
  }
  return out;
}

(async () => {
  const pages = [
    ['Skill_Gems', 'skill'],
    ['Support_Gems', 'support'],
    ['Spirit_Gems', 'spirit'],
    ['Gems', 'any'],
    ['Lineage_Supports', 'support'],
  ];
  const cacheDir = path.join(ROOT, 'design/refactor/skill-data/crawled/raw/index_pages');
  fs.mkdirSync(cacheDir, { recursive: true });

  const all = new Map();
  const report = [];
  for (const [p, gt] of pages) {
    const cf = path.join(cacheDir, p + '.html');
    let t;
    if (fs.existsSync(cf)) t = fs.readFileSync(cf, 'utf8');
    else { t = await get('https://poe2db.tw/us/' + p); if (t) fs.writeFileSync(cf, t); }
    if (!t) { report.push(p + ' : FETCH FAIL'); continue; }
    const rows = parseRows(t, p, gt);
    report.push(p + ' : rows=' + rows.length + ' (data-filters 出现 ' + (t.split('data-filters="').length - 1) + ')');
    for (const r of rows) {
      const prev = all.get(r.href);
      if (!prev) all.set(r.href, r);
      else {
        prev.alsoOn = prev.alsoOn || [];
        if (!prev.alsoOn.includes(p)) prev.alsoOn.push(p);
        if (!prev.color && r.color) prev.color = r.color;
        if (!prev.icon && r.icon) prev.icon = r.icon;
        if (!prev.filters && r.filters) prev.filters = r.filters;
        if (prev.tier == null && r.tier != null) prev.tier = r.tier;
      }
    }
    await sleep(300);
  }
  console.log(report.join('\n'));
  const arr = [...all.values()];
  console.log('\n去重合计 =', arr.length);
  const byPage = {};
  arr.forEach(r => byPage[r.page] = (byPage[r.page] || 0) + 1);
  console.log('page 归属:', JSON.stringify(byPage));
  const byColor = {};
  arr.forEach(r => byColor[r.color || '(null)'] = (byColor[r.color || '(null)'] || 0) + 1);
  console.log('颜色:', JSON.stringify(byColor));

  fs.writeFileSync(path.join(T, 'discovered_v2.json'), JSON.stringify(arr, null, 1));
  console.log('已写出 discovered_v2.json');
})();
