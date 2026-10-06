/**
 * 覆盖面扩展发现：在既有 728 条宝石之外，把"无宝石技能 / 变形 / 召唤 / 授予 / 关键词族"
 * 一并纳入，产出 discovered_full.json。
 * 策略：先试 sitemap；再用种子索引页 + BFS 抽取 /us/<Page> 链接。
 */
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data/_tools');
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36';
const H = { 'User-Agent': UA, 'Referer': 'https://poe2db.tw/us/' };
const sleep = ms => new Promise(r => setTimeout(r, ms));

async function get(url, tries = 2) {
  for (let i = 0; i < tries; i++) {
    try {
      const c = new AbortController(); const to = setTimeout(() => c.abort(), 25000);
      const r = await fetch(url, { headers: H, signal: c.signal }); clearTimeout(to);
      if (!r.ok) return null;
      return await r.text();
    } catch (e) { await sleep(700); }
  }
  return null;
}

const SEEDS = [
  'Gems', 'Skill_Gems', 'Support_Gems', 'Spirit_Gems', 'Lineage_Supports',
  'Minions', 'Shapeshift', 'Keywords', 'Default_Attack',
  'Bear_Form', 'Wolf_Form', 'Wyvern_Form', 'Fire_Infusion', 'Fire_Mastery',
  'Herald_of_Ash', 'Glacial_Bolt', 'Solar_Orb'
];

(async () => {
  // 1) sitemap 尝试
  let urls = [];
  for (const s of ['https://poe2db.tw/sitemap.xml', 'https://poe2db.tw/us/sitemap.xml', 'https://poe2db.tw/sitemap.txt']) {
    const t = await get(s);
    if (t && /<loc>|http/.test(t)) {
      const m = [...t.matchAll(/<loc>([^<]+)<\/loc>/g)].map(x => x[1]);
      urls = m.length ? m : t.split(/\s+/).filter(x => x.startsWith('http'));
      console.log('sitemap', s, '->', urls.length, 'URL');
      if (urls.length) break;
    }
  }

  const found = new Map(); // href -> {href,name,color,icon,filters,seenIn}
  function addRow(href, name, color, icon, filters, seenIn) {
    if (!href || href.includes('%') && href.indexOf('%2C') < 0) { /* keep commas */ }
    const prev = found.get(href);
    if (prev) { if (seenIn && !prev.seenIn.includes(seenIn)) prev.seenIn.push(seenIn); return; }
    found.set(href, { href, name: name || href.replace(/_/g, ' '), color: color || null, icon: icon || null, filters: filters || '', seenIn: seenIn ? [seenIn] : [] });
  }

  // 2) 种子页抓取 + 表格行解析
  for (const p of SEEDS) {
    const t = await get('https://poe2db.tw/us/' + p);
    if (!t) { console.log('miss', p); continue; }
    let n = 0;
    // 索引表行：<a class="gem_blue" ... href="/us/Xxx">Name</a>
    for (const m of t.matchAll(/<a[^>]*class="[^"]*gem_(red|green|blue)[^"]*"[^>]*href="\/us\/([^"]+)"[^>]*>([^<]+)<\/a>/g)) {
      addRow(m[2], m[3].trim(), m[1], null, '', p); n++;
    }
    // 带 data-filters 的行
    for (const m of t.matchAll(/<tr[^>]*data-filters="([^"]*)"[^>]*>[\s\S]{0,400}?<a[^>]*href="\/us\/([^"]+)"[^>]*>([^<]+)<\/a>/g)) {
      const href = m[2], name = m[3].trim(), filters = m[1];
      const prev = found.get(href);
      if (prev) { if (!prev.filters) prev.filters = filters; if (!prev.seenIn.includes(p)) prev.seenIn.push(p); }
      else addRow(href, name, null, null, filters, p);
      n++;
    }
    // 图标（同一 tr 内的 cdn 图）
    console.log('seed', p.padEnd(18), 'rows+', n, 'total', found.size);
    await sleep(300);
  }

  // 3) sitemap 补全（若有）
  if (urls.length) {
    let added = 0;
    for (const u of urls) {
      const m = u.match(/poe2db\.tw\/us\/([^?#]+)$/);
      if (!m) continue;
      const href = decodeURIComponent(m[1]);
      if (!found.has(href)) { addRow(href, href.replace(/_/g, ' '), null, null, '', 'sitemap'); added++; }
    }
    console.log('sitemap 新增', added);
  }

  const arr = [...found.values()];
  fs.writeFileSync(path.join(OUT, 'discovered_full.json'), JSON.stringify(arr, null, 1));
  console.log('写出 discovered_full.json =', arr.length);

  const old = JSON.parse(fs.readFileSync(path.join(OUT, 'discovered.json'), 'utf8'));
  const oldSet = new Set(old.map(x => x.href));
  console.log('新增（728 之外）=', arr.filter(x => !oldSet.has(x.href)).length);
  console.log('样例新增:', arr.filter(x => !oldSet.has(x.href)).slice(0, 25).map(x => x.href).join(', '));
})();
