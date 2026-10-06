// 分析列表页的 tr 行结构 (基于 <tr 切分，因为 tr 未闭合)
import fs from 'node:fs';

const _LIM = Number(process.env.LIMIT || 6000);
const _buf = [];
const _orig = console.log;
console.log = (...a) => _buf.push(a.map(String).join(' '));
process.on('exit', () => _orig(_buf.join('\n').slice(0, _LIM)));

const file = process.argv[2];
const html = fs.readFileSync(file, 'utf8');

// 按 <tr 切分
const parts = html.split(/<tr(?=[ >])/).slice(1);
const rows = parts.filter(p => /href="\/us\/[A-Za-z0-9_%'\-]+"/.test(p));
console.log(`file=${file} candidate rows=${rows.length}`);

// 找 table 标题/上下文
const tableStarts = [...html.matchAll(/<table[^>]*>/g)].map(m => m.index);
console.log('tables:', tableStarts.length);

const out = [];
for (let i = 0; i < rows.length; i++) {
  const p = rows[i];
  const attrs = p.slice(0, p.indexOf('>') + 1);
  const href = p.match(/href="\/us\/([^"]+)"/)?.[1];
  const name = p.match(/href="\/us\/[^"]+"[^>]*>([^<]+)<\/a>/)?.[1]?.trim();
  const icon = p.match(/<img[^>]*src="([^"]+)"/)?.[1];
  const color = p.match(/class="(gem_[a-z]+)"/)?.[1];
  const filters = attrs.match(/data-filters="([^"]*)"/)?.[1] || null;
  const plain = p.replace(/<[^>]+>/g, '|').replace(/\|+/g, '|').replace(/&nbsp;/g, ' ').trim();
  out.push({ href, name, color, filters, plain: plain.slice(0, 160) });
}

console.log('--- first 15 rows:');
for (const r of out.slice(0, 15)) console.log(JSON.stringify(r));

// 去重统计
const uniq = new Map();
for (const r of out) if (r.href) uniq.set(r.href, r);
console.log(`--- distinct hrefs: ${uniq.size}`);
const colorCount = {};
for (const r of uniq.values()) colorCount[r.color || 'null'] = (colorCount[r.color || 'null'] || 0) + 1;
console.log('--- colors:', JSON.stringify(colorCount));

// 打印全部 href
console.log('--- all hrefs:');
console.log([...uniq.keys()].join('\n'));
