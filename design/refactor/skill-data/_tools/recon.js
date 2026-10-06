/**
 * 对账：poe2db 官方计数 vs 我的发现结果
 * 用户给的权威计数：Gems 983 / 技能宝石 427 / 辅助宝石 557 / Meta 29 / 精神宝石 53
 */
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const T = path.join(ROOT, 'design/refactor/skill-data/_tools');
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36';
const H = { 'User-Agent': UA, 'Referer': 'https://poe2db.tw/us/' };
const sleep = ms => new Promise(r => setTimeout(r, ms));

async function get(u) {
  try {
    const c = new AbortController(); const to = setTimeout(() => c.abort(), 30000);
    const r = await fetch(u, { headers: H, signal: c.signal }); clearTimeout(to);
    if (!r.ok) return null;
    return await r.text();
  } catch (e) { return null; }
}

function strip(s) {
  return s.replace(/<span class="ndash">[^<]*<\/span>/g, '-').replace(/<[^>]+>/g, '')
    .replace(/&amp;/g, '&').replace(/\s+/g, ' ').trim();
}

(async () => {
  const d728 = JSON.parse(fs.readFileSync(path.join(T, 'discovered.json'), 'utf8'));
  const d1063 = JSON.parse(fs.readFileSync(path.join(T, 'discovered_full.json'), 'utf8'));
  console.log('discovered.json =', d728.length, ' discovered_full.json =', d1063.length);

  const color728 = {}; d728.forEach(e => color728[e.color || '(null)'] = (color728[e.color || '(null)'] || 0) + 1);
  const color1063 = {}; d1063.forEach(e => color1063[e.color || '(null)'] = (color1063[e.color || '(null)'] || 0) + 1);
  console.log('728 颜色:', JSON.stringify(color728));
  console.log('1063 颜色:', JSON.stringify(color1063));

  const set728 = new Set(d728.map(e => e.href));
  const extra = d1063.filter(e => !set728.has(e.href));
  console.log('\n1063 中 728 之外 =', extra.length);
  const exColor = {}; extra.forEach(e => exColor[e.color || '(null)'] = (exColor[e.color || '(null)'] || 0) + 1);
  console.log('  其颜色分布:', JSON.stringify(exColor));
  console.log('  有颜色的（可能是真宝石）:', extra.filter(e => e.color).length);
  console.log('  样例(有颜色):', extra.filter(e => e.color).slice(0, 30).map(e => e.href).join(', '));

  // 关键页：Gems（全量）与各分类页，数一数到底有多少行
  const pages = ['Gems', 'Skill_Gems', 'Support_Gems', 'Spirit_Gems', 'Lineage_Supports', 'Meta_Skill_Gems'];
  for (const p of pages) {
    const t = await get('https://poe2db.tw/us/' + p);
    if (!t) { console.log('MISS', p); continue; }
    const rows = [...t.matchAll(/<tr[^>]*data-filters="([^"]*)"/g)].length;
    const links = new Set([...t.matchAll(/<a[^>]*class="[^"]*gem_(?:red|green|blue)[^"]*"[^>]*href="\/us\/([^"]+)"/g)].map(m => m[1]));
    const anyGem = new Set([...t.matchAll(/gem_(?:red|green|blue)/g)].length ? [...t.matchAll(/href="\/us\/([^"]+)"[^>]*class="[^"]*gem_(?:red|green|blue)/g)].map(m => m[1]) : []);
    // 页面里的分类计数（如 "技能宝石 /427"）
    const counts = [...t.matchAll(/([^<>]{0,30}?)\s*\/\s*(\d{2,4})\b/g)].map(m => strip(m[1]) + '=' + m[2]).slice(0, 15);
    console.log('\n页 ' + p + ' : data-filters行=' + rows + ' gem链接去重=' + links.size + ' 反向=' + anyGem.size + ' len=' + t.length);
    if (counts.length) console.log('   疑似计数:', counts.join(' | '));
    await sleep(300);
  }
})();
