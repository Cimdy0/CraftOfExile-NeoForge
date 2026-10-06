const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const t = fs.readFileSync(path.join(ROOT, 'design/refactor/skill-data/crawled/raw/probe/us__Skill_Gems.html'), 'utf8');

const plain = [...t.matchAll(/data-filters/g)].length;
console.log('data-filters 出现次数 =', plain);
const trdf = [...t.matchAll(/<tr[^>]*data-filters/g)].length;
console.log('<tr ... data-filters =', trdf);

const idxs = [];
let i = -1;
while ((i = t.indexOf('data-filters', i + 1)) > 0 && idxs.length < 3) idxs.push(i);
idxs.forEach((k, n) => {
  console.log('\n===== 第' + (n + 1) + '处 @' + k + ' 前240后600 =====');
  console.log(t.slice(Math.max(0, k - 240), k + 600).replace(/\s+/g, ' '));
});

// 找 Skill_Gems 页里所有形如 href="/us/Xxx" 的锚点，看 class 分布
const cls = {};
for (const m of t.matchAll(/<a\b[^>]*href="\/us\/([^"]+)"[^>]*>/g)) {
  const tag = m[0];
  const cm = tag.match(/class="([^"]*)"/);
  const key = cm ? cm[1] : '(no-class)';
  cls[key] = (cls[key] || 0) + 1;
}
console.log('\n--- /us/ 锚点 class 分布 (top 20) ---');
Object.entries(cls).sort((a, b) => b[1] - a[1]).slice(0, 20).forEach(([k, v]) => console.log('  ' + v + '  ' + k));
