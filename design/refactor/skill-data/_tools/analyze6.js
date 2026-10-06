// 分析 gem 详情页结构
import fs from 'node:fs';
const _LIM = Number(process.env.LIMIT || 6000);
const _buf = []; const _orig = console.log;
console.log = (...a) => _buf.push(a.map(String).join(' '));
process.on('exit', () => _orig(_buf.join('\n').slice(0, _LIM)));

const file = process.argv[2];
const html = fs.readFileSync(file, 'utf8');
console.log('=== ', file, html.length);

// 1. 列所有 script 块（>200 chars）
let si = 0;
for (const m of html.matchAll(/<script[^>]*>([\s\S]*?)<\/script>/g)) {
  const s = m[1];
  if (s.length > 200) {
    console.log(`--- script #${si} len=${s.length}:`);
    console.log(s.slice(0, Number(process.env.SCRIPTPRE || 900)));
  }
  si++;
}

if (process.env.ONLY !== 'script') {
  // 2. 找 ModsView / new XView( 调用
  for (const m of html.matchAll(/new\s+([A-Za-z]+View)\s*\(/g)) {
    console.log(`### new ${m[1]}( at ${m.index}`);
    console.log(html.slice(m.index, m.index + Number(process.env.VIEWPRE || 1200)));
  }
  // 3. 找 ModsView 字样
  console.log('ModsView occurrences:', (html.match(/ModsView/g) || []).length);
  console.log('data-level occurrences:', (html.match(/data-level/g) || []).length);
  console.log('lvltbl occurrences:', (html.match(/lvltbl|leveltbl|perlevel|per_level/gi) || []).length);
}
