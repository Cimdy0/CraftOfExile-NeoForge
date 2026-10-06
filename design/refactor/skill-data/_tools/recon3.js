// 精确类别归属 + 找 Meta 页
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const PD = path.join(ROOT, 'design/refactor/skill-data/crawled/raw/index_pages');
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36';

function rowsOf(html) {
  const out = [];
  const parts = html.split(/<tr\s+data-filters="/);
  for (let i = 1; i < parts.length; i++) {
    const seg = parts[i]; const q = seg.indexOf('"'); if (q < 0) continue;
    out.push({ filters: seg.slice(0, q), body: seg.slice(q + 1) });
  }
  return out;
}
function hrefOf(body) { const m = body.match(/href="\/us\/([^"]+)"/); return m ? m[1] : null; }
function strip(s) { return s.replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim(); }

const files = ['Skill_Gems', 'Support_Gems', 'Spirit_Gems', 'Gems'];
const sets = {};
for (const f of files) {
  const p = path.join(PD, f + '.html');
  if (!fs.existsSync(p)) { sets[f] = new Set(); continue; }
  const t = fs.readFileSync(p, 'utf8');
  const s = new Set();
  for (const r of rowsOf(t)) { const h = hrefOf(r.body); if (h) s.add(h); }
  sets[f] = s;
  console.log(f.padEnd(14), 'rows=' + rowsOf(t).length, 'unique=' + s.size);
}
console.log('');
const inter = (a, b) => [...a].filter(x => b.has(x));
console.log('Skill ∩ Support =', inter(sets.Skill_Gems, sets.Support_Gems).length);
console.log('Skill ∩ Spirit  =', inter(sets.Skill_Gems, sets.Spirit_Gems).length);
console.log('Support ∩ Spirit =', inter(sets.Support_Gems, sets.Spirit_Gems).length);
console.log('Spirit \\ Skill  =', [...sets.Spirit_Gems].filter(x => !sets.Skill_Gems.has(x)).length);
console.log('Gems = Skill ∪ Support ∪ Spirit ?',
  sets.Gems.size === new Set([...sets.Skill_Gems, ...sets.Support_Gems, ...sets.Spirit_Gems]).size);
const union3 = new Set([...sets.Skill_Gems, ...sets.Support_Gems, ...sets.Spirit_Gems]);
console.log('三者并集 =', union3.size, ' Gems页 =', sets.Gems.size);
const onlyGems = [...sets.Gems].filter(x => !union3.has(x));
console.log('仅 Gems 页有（无分类页归属）=', onlyGems.length, '→ 很可能就是 Meta', onlyGems.slice(0, 35).join(', '));

// 在 Gems 页找 Meta 分类页链接
(async () => {
  const t = fs.readFileSync(path.join(PD, 'Gems.html'), 'utf8');
  const cands = [...t.matchAll(/href="([^"]*Meta[^"]*)"/gi)].map(m => m[1]);
  console.log('\nGems 页里含 Meta 的链接:', [...new Set(cands)].slice(0, 20).join(' | ') || '(无)');
  const nav = [...t.matchAll(/href="\/(us\/)?([A-Za-z_0-9]+)"[^>]*>([^<]{0,40}Meta[^<]{0,40})</gi)].map(m => m[2] + ' -> ' + m[3]);
  console.log('导航含 Meta:', [...new Set(nav)].slice(0, 10).join(' | ') || '(无)');
  // 打印 Gems 页顶部 summary 区
  const i = t.indexOf('Gems Summary');
  if (i > 0) console.log('\nsummary 区原文:', strip(t.slice(i - 400, i + 700)));
})();
