// 为 18 个未匹配的本地 id 生成别名候选表（人工/后续阶段确认）
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data/crawled');
const GEMDIR = path.join(ROOT, 'src/generated/resources/data/coe/coe/item/gem');

const all = JSON.parse(fs.readFileSync(path.join(OUT, 'all_skills.json'), 'utf8'));
const E = all.entries;
const N = s => String(s).toLowerCase().replace(/[^a-z0-9]/g, '');

function walk(d, out = [], cat = null) {
  let es; try { es = fs.readdirSync(d, { withFileTypes: true }); } catch (e) { return out; }
  for (const e of es) {
    const f = path.join(d, e.name);
    if (e.isDirectory()) walk(f, out, cat || (['skill', 'spirit', 'support'].includes(e.name) ? e.name : null));
    else if (e.name.endsWith('.json')) out.push({ cat, id: e.name.replace(/\.json$/, '') });
  }
  return out;
}
const local = walk(GEMDIR).filter(x => x.cat && !/^(preset_|generic_)/.test(x.id));
const crawledNorm = new Set(E.map(e => N(e.id)));
const unmatched = local.filter(l => !crawledNorm.has(N(l.id)));

const table = [];
for (const u of unmatched) {
  const nm = N(u.id);
  const cands = E.map(e => ({ e, score: score(nm, N(e.id), N(e.name_en)) }))
    .filter(x => x.score > 0).sort((a, b) => b.score - a.score).slice(0, 3);
  table.push({
    local_id: u.id, local_cat: u.cat,
    candidates: cands.map(c => ({ crawled_id: c.e.id, name_en: c.e.name_en, type: c.e.gem_type, score: c.score }))
  });
}
function score(a, b, c) {
  if (b === a || c === a) return 100;
  if (b.includes(a) || a.includes(b)) return 60 - Math.abs(a.length - b.length);
  if (c.includes(a) || a.includes(c)) return 50 - Math.abs(a.length - c.length);
  // 词级重合
  return 0;
}
fs.writeFileSync(path.join(OUT, 'alias_candidates.json'), JSON.stringify({
  note: '本地 gem id 在 poe2db 宝石清单中无同名页时的候选映射（需人工确认）',
  generated_at: new Date().toISOString(),
  count: table.length, entries: table
}, null, 1));

for (const t of table) {
  console.log(t.local_cat.padEnd(8) + t.local_id.padEnd(24) + '=> ' +
    (t.candidates.map(c => c.crawled_id + '(' + c.score + ')').join(', ') || '无候选'));
}
console.log('\n已写出 alias_candidates.json，共 ' + table.length + ' 条');
