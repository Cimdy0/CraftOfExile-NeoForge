const fs = require('fs');
const path = require('path');
const zlib = require('zlib');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const RAW = path.join(ROOT, 'design/refactor/skill-data/crawled/raw/html_gz');

function strip(s) {
  return s.replace(/<span class="ndash">[^<]*<\/span>/g, '-')
    .replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim();
}
const h = zlib.gunzipSync(fs.readFileSync(path.join(RAW, 'Abiding_Hex.html.gz'))).toString('utf8');
console.log('=== Abiding_Hex (support) 30000-34000 ===');
console.log(strip(h.slice(30000, 33500)));
console.log('\n=== 全部表 ===');
const re = /<table\b[^>]*>([\s\S]*?)<\/table>/g;
let m, i = 0;
while ((m = re.exec(h))) {
  const th = [...m[1].matchAll(/<th\b[^>]*>([\s\S]*?)<\/th>/g)].map(x => strip(x[1]));
  const bm = m[1].match(/<tbody\b[^>]*>([\s\S]*?)<\/tbody>/);
  const rows = [...(bm ? bm[1] : m[1]).matchAll(/<tr\b[^>]*>([\s\S]*?)<\/tr>/g)]
    .map(r => [...r[1].matchAll(/<td\b[^>]*>([\s\S]*?)<\/td>/g)].map(c => strip(c[1])));
  console.log('表' + (i++) + ' th=' + JSON.stringify(th) + '\n   rows=' + JSON.stringify(rows.slice(0, 4)));
}
