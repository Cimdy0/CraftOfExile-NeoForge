// 找出"本地有、但爬取清单里没有"的宝石 id，以及 DNT 统计
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data/crawled');
const GEMDIR = path.join(ROOT, 'src/generated/resources/data/coe/coe/item/gem');

function walk(d, out = [], cat = null) {
  let es; try { es = fs.readdirSync(d, { withFileTypes: true }); } catch (e) { return out; }
  for (const e of es) {
    const f = path.join(d, e.name);
    if (e.isDirectory()) walk(f, out, cat || (['skill', 'spirit', 'support'].includes(e.name) ? e.name : null));
    else if (e.name.endsWith('.json')) out.push({ file: f, cat, id: e.name.replace(/\.json$/, '') });
  }
  return out;
}
const local = walk(GEMDIR).filter(x => x.cat).filter(x => !/^(preset_|generic_)/.test(x.id));

const all = JSON.parse(fs.readFileSync(path.join(OUT, 'all_skills.json'), 'utf8'));
const E = all.entries;

function norm(s) {
  return String(s).toLowerCase().replace(/armour/g, 'armor')
    .replace(/['’]s_/g, 's_').replace(/_?s_/g, 's_')
    .replace(/_(i|ii|iii|iv|v|four|five)$/, '')
    .replace(/[^a-z0-9]/g, '');
}
const crawledNorm = new Map();
for (const e of E) if (!crawledNorm.has(norm(e.id))) crawledNorm.set(norm(e.id), e);

const unmatched = local.filter(l => !crawledNorm.has(norm(l.id)));
console.log('本地宝石条目 =', local.length, ' 未被爬取清单覆盖 =', unmatched.length);
const byCat = {};
unmatched.forEach(u => byCat[u.cat] = (byCat[u.cat] || 0) + 1);
console.log('  分类:', JSON.stringify(byCat));
unmatched.forEach(u => console.log('   ' + u.cat.padEnd(8) + u.id));

// 爬取侧：本地未实现
const localNorm = new Set(local.map(l => norm(l.id)));
const notImpl = E.filter(e => !localNorm.has(norm(e.id)));
console.log('\n爬取条目中本地未实现 =', notImpl.length);
const nt = {}; notImpl.forEach(e => nt[e.gem_type] = (nt[e.gem_type] || 0) + 1);
console.log('  分类:', JSON.stringify(nt));

// DNT 统计
const v3 = JSON.parse(fs.readFileSync(path.join(ROOT, 'design/refactor/skill-data/_tools/discovered_v3.json'), 'utf8'));
const dnt = v3.filter(e => /^DNT[-_]/i.test(e.href));
console.log('\n发现清单 =', v3.length, ' DNT =', dnt.length, ' 实际抓取 =', E.length);
console.log('  DNT 样例:', dnt.slice(0, 12).map(e => e.href).join(', '));
