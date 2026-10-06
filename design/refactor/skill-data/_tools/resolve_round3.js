const fs = require('fs');
const path = require('path');
const zlib = require('zlib');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data/crawled');
const PD = path.join(OUT, 'raw/index_pages');

// A) 从 Support_Gems 索引页取全部辅助名，查 Added / Exposure / Infusion
const h = fs.readFileSync(path.join(PD, 'Support_Gems.html'), 'utf8');
const names = [];
for (const seg of h.split(/<tr\s+data-filters="/).slice(1)) {
  const q = seg.indexOf('"'); if (q < 0) continue;
  const body = seg.slice(q + 1);
  const cand = [...body.replace(/<div class="gem_tags[\s\S]*$/, '').matchAll(/<a\b[^>]*href="\/us\/[^"]+"[^>]*>([^<]+)<\/a>/g)].map(m => m[1].trim());
  const n = (cand.length ? cand[cand.length - 1] : '').replace(/\s*\(\d+\)\s*$/, '').trim();
  if (n) names.push(n);
}
console.log('Support_Gems 索引名总数 =', names.length);
for (const kw of ['Added', 'Exposure', 'Infusion', 'Ignition', 'Projectile', 'Critical']) {
  const hit = [...new Set(names.filter(n => new RegExp('^' + kw, 'i').test(n)))];
  console.log('  以 "' + kw + '" 开头:', hit.length ? hit.slice(0, 14).join(', ') : '(无)');
}
console.log('  含 "Added" 的任意名:', [...new Set(names.filter(n => /added/i.test(n)))].join(', ') || '(无)');

// B) 争议页身份（用更宽松的元数据解析）
function strip(s) { return s.replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim(); }
console.log('\n=== 争议页身份 ===');
for (const n of ['Blight', 'Fire_Infusion', 'Frost_Nova', 'Lacerate', 'Wrath', 'Grace',
  'Ground_Slam', 'Perforate', 'Consume_Corpse', 'Toxic_Rain', 'Cyclone', 'Puncture', 'Shockwave']) {
  const f = path.join(OUT, 'raw/html_gz', n + '.html.gz');
  if (!fs.existsSync(f)) { console.log('  ' + n.padEnd(18) + '(无缓存)'); continue; }
  const t = zlib.gunzipSync(fs.readFileSync(f)).toString('utf8');
  const typeM = t.match(/>Type<\/td><td>([^<]{0,200})/);
  const classM = t.match(/>Class[^<]*<\/span><\/td><td>([\s\S]{0,120}?)<\/td>/);
  const idM = t.match(/>Id<\/td><td>([^<]{0,60})/);
  const baseM = t.match(/>BaseType[^<]*<\/span><\/td><td>([^<]{0,80})/);
  const tags = [...new Set([...t.matchAll(/class="[^"]*GemTags[^"]*"[^>]*>([^<]+)</g)].map(x => x[1]))];
  console.log('  ' + n.padEnd(18) +
    'Id=' + (idM ? idM[1] : '?').padEnd(22) +
    'Base=' + (baseM ? baseM[1] : '?').padEnd(14) +
    'Class=' + (classM ? strip(classM[1]) : '?').slice(0, 14).padEnd(14) +
    'tags=' + tags.slice(0, 6).join('/'));
  if (typeM) console.log('      Type=' + strip(typeM[1]).slice(0, 150));
}
