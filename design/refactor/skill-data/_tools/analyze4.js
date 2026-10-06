// 表感知的列表页提取：只取主宝石列表 table 内的行
import fs from 'node:fs';

const _LIM = Number(process.env.LIMIT || 6000);
const _buf = [];
const _orig = console.log;
console.log = (...a) => _buf.push(a.map(String).join(' '));
process.on('exit', () => _orig(_buf.join('\n').slice(0, _LIM)));

const file = process.argv[2];
const html = fs.readFileSync(file, 'utf8');

// 切出每个 table 块：从 <table 到 </table> 或下一个 <table 或 </tbody> 之后
const tableChunks = [];
let last = 0;
const starts = [...html.matchAll(/<table[^>]*>/g)];
const ends = [...html.matchAll(/<\/table>/g)];
for (let i = 0; i < starts.length; i++) {
  const s = starts[i].index;
  const e = ends.find(x => x.index > s)?.index ?? html.length;
  tableChunks.push(html.slice(s, e));
}

console.log(`file=${file} tables=${tableChunks.length}`);

for (let ti = 0; ti < tableChunks.length; ti++) {
  const ch = tableChunks[ti];
  const rows = ch.split(/<tr(?=[ >])/).slice(1);
  const parsed = [];
  for (const p of rows) {
    if (!/href="\/us\/[A-Za-z0-9_%'\-\.]+"/.test(p)) continue;
    const attrs = p.slice(0, p.indexOf('>') + 1);
    if (!/data-filters=/.test(attrs)) continue;
    const href = p.match(/href="\/us\/([^"]+)"/)?.[1];
    if (!href) continue;
    const name = p.match(/href="\/us\/[^"]+"[^>]*>([^<]+)<\/a>/)?.[1]?.trim();
    const icon = p.match(/<img[^>]*src="([^"]+)"/)?.[1] || null;
    const color = p.match(/class="(gem_[a-z]+)"/)?.[1] || null;
    const filters = attrs.match(/data-filters="([^"]*)"/)?.[1] || '';
    if (!/^(gem_)/.test(color || '')) continue;
    parsed.push({ href, name, icon, color, filters });
  }
  if (parsed.length === 0) continue;
  const cols = (ch.match(/<thead>([\s\S]*?)<\/thead>/)?.[1] || '').replace(/<[^>]+>/g, '|');
  console.log(`--- table #${ti}: rows(with data-filters&gem_)=${parsed.length}  thead=${cols.slice(0, 200)}`);
  if (process.env.SHOW === String(ti)) {
    for (const r of parsed) console.log('   ', JSON.stringify(r));
  }
}

// 汇总全页 distinct
const all = new Map();
for (const ch of tableChunks) {
  for (const p of ch.split(/<tr(?=[ >])/).slice(1)) {
    if (!/href="\/us\/[A-Za-z0-9_%'\-\.]+"/.test(p)) continue;
    const attrs = p.slice(0, p.indexOf('>') + 1);
    const href = p.match(/href="\/us\/([^"]+)"/)?.[1];
    const color = p.match(/class="(gem_[a-z]+)"/)?.[1] || null;
    if (!href || !color || !/^gem_/.test(color)) continue;
    if (!all.has(href)) all.set(href, { href, color, hasFilters: /data-filters=/.test(attrs) });
  }
}
console.log(`=== TOTAL distinct gem hrefs: ${all.size}`);
const cc = {};
for (const r of all.values()) cc[r.color] = (cc[r.color] || 0) + 1;
console.log('=== colors:', JSON.stringify(cc));
console.log('=== with data-filters:', [...all.values()].filter(x => x.hasFilters).length);
