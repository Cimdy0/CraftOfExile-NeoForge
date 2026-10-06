// 抓取详情页主要结构：headings + 关键 class 块
import fs from 'node:fs';
const file = process.argv[2];
const html = fs.readFileSync(file, 'utf8');
const mode = process.env.MODE || 'outline';

function strip(s) { return s.replace(/<[^>]+>/g, ' ').replace(/&nbsp;/g, ' ').replace(/\s+/g, ' ').trim(); }

if (mode === 'outline') {
  // 打印 body 内所有带 class/id 的 div 的开标签层级摘要
  let depth = 0;
  const re = /<(\/?)(div|table|tr|td|h[1-6]|span|p|ul|li)([^>]*)>/g;
  let m;
  const startIdx = Math.max(0, html.indexOf('<main') >= 0 ? html.indexOf('<main') : html.indexOf('MainContent'));
  const cw = html.indexOf('Community Wiki');
  const endIdx = cw > startIdx ? cw : html.length;
  const seg = html.slice(startIdx, endIdx);
  while ((m = re.exec(seg))) {
    const [, close, tag, attrs] = m;
    if (tag !== 'div' && tag !== 'h1' && tag !== 'h2' && tag !== 'h3') continue;
    const cls = (attrs.match(/class="([^"]*)"/)?.[1] || '').slice(0, 60);
    const id = (attrs.match(/id="([^"]*)"/)?.[1] || '').slice(0, 40);
    if (close) { depth--; continue; }
    const after = strip(seg.slice(m.index + m[0].length, m.index + m[0].length + 120)).slice(0, 80);
    process.stdout.write(`${'  '.repeat(Math.max(0, depth))}<${tag} class="${cls}" id="${id}"> ${after}\n`);
    depth++;
  }
}

if (mode === 'modsview') {
  const i = html.indexOf('ModsView');
  process.stdout.write(html.slice(Math.max(0, i - 500), i + Number(process.env.PRE || 3000)));
}

if (mode === 'raw') {
  const key = process.env.KEY;
  const i = html.indexOf(key);
  process.stdout.write(strip(html.slice(i, i + Number(process.env.PRE || 3000))));
}

if (mode === 'rawhtml') {
  const key = process.env.KEY;
  const i = html.indexOf(key);
  process.stdout.write(html.slice(i, i + Number(process.env.PRE || 3000)));
}
