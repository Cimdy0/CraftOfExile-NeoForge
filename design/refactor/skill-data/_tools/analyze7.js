// 提取详情页里所有 echarts option JSON（含逐等级 series）
import fs from 'node:fs';
const _LIM = Number(process.env.LIMIT || 8000);
const _buf = []; const _orig = console.log;
console.log = (...a) => _buf.push(a.map(String).join(' '));
process.on('exit', () => _orig(_buf.join('\n').slice(0, _LIM)));

const file = process.argv[2];
const html = fs.readFileSync(file, 'utf8');

const results = [];
let idx = 0;
while (true) {
  const i = html.indexOf('setOption(', idx);
  if (i < 0) break;
  const open = html.indexOf('{', i);
  if (open < 0) break;
  let depth = 0, j = open, inStr = false, esc = false;
  for (; j < html.length; j++) {
    const c = html[j];
    if (inStr) {
      if (esc) esc = false;
      else if (c === '\\') esc = true;
      else if (c === '"') inStr = false;
      continue;
    }
    if (c === '"') inStr = true;
    else if (c === '{') depth++;
    else if (c === '}') { depth--; if (depth === 0) { j++; break; } }
  }
  const raw = html.slice(open, j);
  idx = j;
  try {
    results.push(JSON.parse(raw));
  } catch (e) {
    results.push({ __parseError: String(e).slice(0, 120), __raw: raw.slice(0, 200) });
  }
}

console.log(`found ${results.length} echarts options`);
for (const r of results) {
  if (r.__parseError) { console.log('!! parse error', r.__parseError, r.__raw); continue; }
  const title = r.title?.text;
  const levels = r.xAxis?.data;
  const series = r.series?.map(s => ({ name: s.name, data: s.data }));
  console.log(`- TITLE: ${title}`);
  console.log(`  levels(${levels?.length}): ${JSON.stringify(levels)}`);
  console.log(`  series: ${JSON.stringify(series)}`);
}
