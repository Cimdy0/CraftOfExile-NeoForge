// 诊断：1) 等级表为何只抓到 1 行  2) gem_class 真实分布
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data/crawled');
const RAW = path.join(OUT, 'raw', 'html_gz');

const all = JSON.parse(fs.readFileSync(path.join(OUT, 'all_skills.json'), 'utf8'));
const E = all.entries;

console.log('--- gem_class 分布 ---');
const gc = {}; E.forEach(e => gc[e.gem_class || '(none)'] = (gc[e.gem_class || '(none)'] || 0) + 1);
console.log(JSON.stringify(gc, null, 1));

const one = E.filter(e => e.level_table.rows.length === 1);
console.log('\n等级表 1 行的条目 =', one.length);
console.log('其中 gem_type:', JSON.stringify(one.reduce((a, e) => (a[e.gem_type] = (a[e.gem_type] || 0) + 1, a), {})));
console.log('样例:', one.slice(0, 5).map(e => e.id).join(', '));

// 打开一个 1 行的样例，看它有哪些表
const sample = one[0];
console.log('\n=== 打开', sample.id, '===');
const gz = path.join(RAW, 'Bleed_I.html.gz');
const target = path.join(RAW, one[0].id + '.html.gz');
let file = fs.existsSync(target) ? target : gz;
// href 不等于 id，需要还原
const seen = JSON.parse(fs.readFileSync(path.join(ROOT, 'design/refactor/skill-data/_tools/discovered.json'), 'utf8'));
const entry = seen.find(s => s.href.toLowerCase() === one[0].id);
console.log('对应 href =', entry && entry.href);
if (entry) {
  const f2 = path.join(RAW, entry.href + '.html.gz');
  if (fs.existsSync(f2)) {
    const h = zlib.gunzipSync(fs.readFileSync(f2)).toString('utf8');
    const re = /<table\b[^>]*>([\s\S]*?)<\/table>/g;
    let m, i = 0;
    while ((m = re.exec(h))) {
      const th = [...m[1].matchAll(/<th\b[^>]*>([\s\S]*?)<\/th>/g)].map(x => x[1].replace(/<[^>]+>/g, '').trim());
      const bm = m[1].match(/<tbody\b[^>]*>([\s\S]*?)<\/tbody>/);
      const body = bm ? bm[1] : m[1];
      const rows = [...body.matchAll(/<tr\b[^>]*>([\s\S]*?)<\/tr>/g)];
      console.log(' 表' + (i++) + ' th=' + JSON.stringify(th.slice(0, 6)) + ' rows=' + rows.length);
    }
    console.log(' 包含 "Level Effect" ?', h.indexOf('Level Effect'));
    console.log(' 包含 "Requires Level" ?', h.indexOf('Requires Level'));
  } else console.log('raw 不存在', f2);
}
