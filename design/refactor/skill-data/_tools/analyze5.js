// 分析 table 前的标题/h2 等上下文，判断每张表是什么
import fs from 'node:fs';
const file = process.argv[2];
const html = fs.readFileSync(file, 'utf8');

// 提取 heading 标记序列
const marks = [];
for (const m of html.matchAll(/<(h1|h2|h3|h4|h5|caption|legend)[^>]*>([\s\S]*?)<\/\1>/g)) {
  marks.push({ i: m.index, tag: m[1], text: m[2].replace(/<[^>]+>/g, '').trim() });
}
for (const m of html.matchAll(/<table[^>]*>/g)) marks.push({ i: m.index, tag: 'TABLE', text: '' });
for (const m of html.matchAll(/<(div|nav)[^>]*class="[^"]*(card-header|card-title|nav-link)[^"]*"[^>]*>/g)) {
  const seg = html.slice(m.index, m.index + 300).replace(/<[^>]+>/g, '').trim().slice(0, 80);
  marks.push({ i: m.index, tag: 'DIV', text: seg });
}
marks.sort((a, b) => a.i - b.i);

let out = '';
for (const mk of marks.sort((a, b) => a.i - b.i)) {
  if (mk.tag === 'TABLE') out += `TABLE @${mk.i}\n`;
  else out += `  ${mk.tag}: ${mk.text.slice(0, 90)}\n`;
}
process.stdout.write(out.slice(0, Number(process.env.LIMIT || 4000)));
