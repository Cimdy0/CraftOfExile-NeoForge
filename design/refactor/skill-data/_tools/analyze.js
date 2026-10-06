// 分析列表页结构
import fs from 'node:fs';

const file = process.argv[2];
const LIMIT = Number(process.argv[3] || 6000);
const origLog = console.log;
console.log = (...a) => { origLog(...a); };
const out = [];
console.log = (...a) => out.push(a.map(x => typeof x === 'string' ? x : String(x)).join(' '));
process.on('exit', () => origLog(out.join('\n').slice(0, LIMIT)));

const html = fs.readFileSync(file, 'utf8');

// 找所有 指向 /us/XXX 的链接，统计
const linkRe = /href="\/us\/([A-Za-z0-9_%'\-\.]+)"/g;
const seen = new Map();
let m;
while ((m = linkRe.exec(html))) {
  const name = m[1];
  seen.set(name, (seen.get(name) || 0) + 1);
}
console.log('total distinct /us/ links:', seen.size);

// 打印一些上下文: 找 table 结构
const idx = html.indexOf('<table');
console.log('--- first <table> at', idx);
if (idx >= 0) console.log(html.slice(idx, idx + 1500));

// 找 JSON / script 数据
const scriptRe = /<script[^>]*>([\s\S]*?)<\/script>/g;
let si = 0;
while ((m = scriptRe.exec(html))) {
  const s = m[1];
  if (s.length > 400) {
    console.log(`--- script #${si} len=${s.length} preview:`, JSON.stringify(s.slice(0, 300)));
  }
  si++;
}
