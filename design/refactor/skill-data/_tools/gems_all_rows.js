// Gems 页全部 <tr> 行（含无 data-filters 的），补出被漏掉的宝石
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const PD = path.join(ROOT, 'design/refactor/skill-data/crawled/raw/index_pages');

const t = fs.readFileSync(path.join(PD, 'Gems.html'), 'utf8');
const chunks = t.split(/<tr\b/).slice(1);
console.log('Gems 页 <tr> 总数 =', chunks.length);

const withDF = chunks.filter(c => /^\s+data-filters="/.test(c));
console.log('含 data-filters =', withDF.length, ' 不含 =', chunks.length - withDF.length);

const hrefAll = new Set(), hrefDF = new Set(), hrefNoDF = new Set();
const noDFSamples = [];
for (const c of chunks) {
  const m = c.match(/href="\/us\/([^"]+)"/);
  if (!m) continue;
  hrefAll.add(m[1]);
  if (/^\s+data-filters="/.test(c)) hrefDF.add(m[1]);
  else { hrefNoDF.add(m[1]); if (noDFSamples.length < 25) noDFSamples.push(m[1]); }
}
console.log('唯一 href: 全部 =', hrefAll.size, ' 有data-filters =', hrefDF.size, ' 仅无data-filters =', hrefNoDF.size);
console.log('\n仅出现在"无 data-filters 行"的条目（前 60）:');
console.log([...hrefNoDF].filter(h => !hrefDF.has(h)).slice(0, 60).join(', '));

// 检查几个已知漏项是否在此
const check = ['Blight', 'Frost_Nova', 'Ice_Shard', 'Cyclone', 'Lacerate', 'Puncture', 'Toxic_Rain',
  'Consume_Corpse', 'Ground_Slam', 'Perforate', 'Shockwave', 'Wrath', 'Grace', 'Fire_Infusion'];
console.log('\n已知漏项在 Gems 页的分布:');
for (const c of check) console.log('  ' + c.padEnd(16) + (hrefDF.has(c) ? 'data-filters行' : (hrefNoDF.has(c) ? '无data-filters行' : '页面完全没出现')));
