// 分析列表页的 tr 行结构
import fs from 'node:fs';

const file = process.argv[2];
const html = fs.readFileSync(file, 'utf8');

const trRe = /<tr([^>]*)>([\s\S]*?)<\/tr>/g;
let m, n = 0;
const rows = [];
while ((m = trRe.exec(html))) {
  const attrs = m[1], inner = m[2];
  if (!/href="\/us\//.test(inner)) continue;
  rows.push({ attrs, inner });
}

console.log(`file=${file} rows=${rows.length}`);

// 统计 attrs 模式
const attrCounts = new Map();
for (const r of rows) {
  const key = r.attrs.replace(/data-filters="[^"]*"/, 'data-filters="..."').trim();
  attrCounts.set(key, (attrCounts.get(key) || 0) + 1);
}
console.log('--- <tr> attr patterns:');
for (const [k, v] of attrCounts) console.log(`  [${v}] ${k}`);

// 统计 thead
const thead = html.match(/<thead>([\s\S]*?)<\/thead>/);
console.log('--- thead:', thead ? thead[1].replace(/</g, '\n<').slice(0, 800) : 'none');

console.log('--- sample row 0 raw:');
console.log(rows[0] ? rows[0].inner.replace(/></g, '>\n<').slice(0, 2500) : 'none');

// 所有 gem_ 颜色 class
const colors = new Set();
for (const r of rows) {
  for (const c of r.inner.matchAll(/class="(gem_[a-z]+)"/g)) colors.add(c[1]);
}
console.log('--- gem color classes:', [...colors].join(', '));

// 提取 href + name + icon + color + filters
console.log('--- extracted entries (first 12):');
for (const r of rows.slice(0, 12)) {
  const href = r.inner.match(/href="\/us\/([^"]+)"/)?.[1];
  const name = r.inner.match(/href="\/us\/[^"]+">([^<]+)<\/a>/)?.[1];
  const icon = r.inner.match(/<img[^>]*src="([^"]+)"/)?.[1];
  const color = r.inner.match(/class="(gem_[a-z]+)"/)?.[1];
  const filters = r.attrs.match(/data-filters="([^"]*)"/)?.[1];
  const tds = [...r.inner.matchAll(/<td[^>]*>([\s\S]*?)(?=<td|<\/tr|$)/g)].map(x => x[1].replace(/<[^>]+>/g, '').trim()).filter(Boolean);
  console.log(JSON.stringify({ href, name, color, filters, tds: tds.slice(0, 6) }, null, 0));
}
