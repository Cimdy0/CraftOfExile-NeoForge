// 刻画 Allow Type 表达式的形态，判断需要什么样的求值器
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data/crawled');
const S = JSON.parse(fs.readFileSync(path.join(OUT, 'support_scope.json'), 'utf8')).entries;
const E = JSON.parse(fs.readFileSync(path.join(OUT, 'all_skills.json'), 'utf8')).entries;
const typeTags = new Set();
E.forEach(e => (e.type_tags || []).forEach(t => typeTags.add(t)));

const OPS = new Set(['AND', 'OR', 'NOT']);
let n0 = 0, n1 = 0, nImplAnd = 0, nExplicitOp = 0, nOr = 0, nNot = 0, nNested = 0;
const samples = { or: [], not: [], nested: [] };
const unknownTerms = {};

for (const [id, v] of Object.entries(S)) {
  const a = v.allow_types;
  if (!a || !a.length) { n0++; continue; }
  const ops = a.filter(t => OPS.has(t));
  const terms = a.filter(t => !OPS.has(t));
  if (a.length === 1 && !ops.length) { n1++; }
  else if (ops.length === 0 && terms.length > 1) { nImplAnd++; }
  else {
    nExplicitOp++;
    if (ops.includes('OR')) { nOr++; if (samples.or.length < 6) samples.or.push(id + ' = ' + a.join(',')); }
    if (ops.includes('NOT')) { nNot++; if (samples.not.length < 6) samples.not.push(id + ' = ' + a.join(',')); }
    if (ops.length >= 2) { nNested++; if (samples.nested.length < 6) samples.nested.push(id + ' = ' + a.join(',')); }
  }
  terms.forEach(t => { if (!typeTags.has(t)) unknownTerms[t] = (unknownTerms[t] || 0) + 1; });
}
console.log('无 Allow Type =', n0);
console.log('单词（纯 term） =', n1);
console.log('多词无操作符（隐式 AND） =', nImplAnd);
console.log('含显式操作符 =', nExplicitOp, ' 其中含 OR =', nOr, ' 含 NOT =', nNot, ' 多操作符(嵌套) =', nNested);
console.log('\nOR 样例:'); samples.or.forEach(s => console.log('  ' + s));
console.log('NOT 样例:'); samples.not.forEach(s => console.log('  ' + s));
console.log('嵌套样例:'); samples.nested.forEach(s => console.log('  ' + s));

console.log('\n--- Allow Type 词表 与 权威 type_tags 词表 的对齐情况 ---');
console.log('type_tags 词表大小 =', typeTags.size);
const unk = Object.entries(unknownTerms).sort((a, b) => b[1] - a[1]);
console.log('出现在 Allow Type 但不在任何技能 type_tags 里的词 =', unk.length);
unk.slice(0, 25).forEach(([k, v]) => console.log('  ' + String(v).padStart(4) + '  ' + k));

// 反向：type_tags 里最常作为 Allow Type 词的
const ALLOW = {};
for (const [, v] of Object.entries(S)) (v.allow_types || []).filter(t => !OPS.has(t)).forEach(t => ALLOW[t] = (ALLOW[t] || 0) + 1);
console.log('\nAllow Type 词表中同时也是 type_tags 的高频词 (top 20):');
Object.entries(ALLOW).filter(([k]) => typeTags.has(k)).sort((a, b) => b[1] - a[1]).slice(0, 20)
  .forEach(([k, v]) => console.log('  ' + String(v).padStart(4) + '  ' + k));
