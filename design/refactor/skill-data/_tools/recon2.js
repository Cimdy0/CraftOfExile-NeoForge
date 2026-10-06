// 定位 Gems 页 1066 行 vs 去重 963 的差额，以及多页归属
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const PD = path.join(ROOT, 'design/refactor/skill-data/crawled/raw/index_pages');

function strip(s) {
  return s.replace(/<span class="ndash">[^<]*<\/span>/g, '-').replace(/<[^>]+>/g, '')
    .replace(/&amp;/g, '&').replace(/&quot;/g, '"').replace(/&#39;/g, "'").replace(/\s+/g, ' ').trim();
}
function rowsOf(html) {
  const out = [];
  const parts = html.split(/<tr\s+data-filters="/);
  for (let i = 1; i < parts.length; i++) {
    const seg = parts[i];
    const q = seg.indexOf('"');
    if (q < 0) continue;
    out.push({ filters: seg.slice(0, q), body: seg.slice(q + 1) });
  }
  return out;
}

const gems = fs.readFileSync(path.join(PD, 'Gems.html'), 'utf8');
const rows = rowsOf(gems);
console.log('Gems 页行数 =', rows.length);

let noHref = 0, enc = 0;
const hrefs = [];
const noHrefSamples = [];
for (const r of rows) {
  const hm = r.body.match(/href="\/us\/([^"]+)"/);
  if (!hm) { noHref++; if (noHrefSamples.length < 6) noHrefSamples.push(strip(r.body).slice(0, 160)); continue; }
  hrefs.push(hm[1]);
  if (/%/.test(hm[1])) enc++;
}
console.log('无 /us/ href 的行 =', noHref, ' 含 URL 编码的 href =', enc);
console.log('唯一 href =', new Set(hrefs).size, ' 总 href =', hrefs.length);
noHrefSamples.forEach((s, i) => console.log('  无href样例' + i + ':', s));

// 重复的 href（同一页内多次出现）
const cnt = {};
hrefs.forEach(h => cnt[h] = (cnt[h] || 0) + 1);
const dup = Object.entries(cnt).filter(([, v]) => v > 1).sort((a, b) => b[1] - a[1]);
console.log('\n同页重复 href 数 =', dup.length, ' 重复总行 =', dup.reduce((a, [, v]) => a + v, 0));
console.log('  样例:', dup.slice(0, 12).map(([k, v]) => k + '×' + v).join(', '));

// 与 discovered_v2 对比
const v2 = JSON.parse(fs.readFileSync(path.join(ROOT, 'design/refactor/skill-data/_tools/discovered_v2.json'), 'utf8'));
const v2set = new Set(v2.map(e => e.href));
const gemSet = new Set(hrefs);
const missing = [...gemSet].filter(h => !v2set.has(h));
console.log('\ndiscovered_v2 =', v2set.size, ' Gems页唯一 href =', gemSet.size);
console.log('Gems 页有但 v2 缺 =', missing.length);
console.log('  ', missing.slice(0, 30).join(', '));

// 分类归属：用 filters 首词 + 页面归属判断
const typeCnt = {};
for (const r of rows) {
  const hm = r.body.match(/href="\/us\/([^"]+)"/);
  if (!hm) continue;
  const f = r.filters.split(/\s+/);
  const t = f[0] === 'Support' ? 'support' : (f.includes('Persistent') && f.includes('Spirit') ? 'spirit' : 'skill');
  typeCnt[t] = (typeCnt[t] || 0) + 1;
}
console.log('\n按 filters 初判类型:', JSON.stringify(typeCnt));
