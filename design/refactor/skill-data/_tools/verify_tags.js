// 精确核实：read 型 gem 条目的 tags 情况 + delivery 类型真实使用（排除 delivery_mod）
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const GEM = path.join(ROOT, 'src/generated/resources/data/coe/coe/item/gem');

function walk(d, out = []) {
  let es; try { es = fs.readdirSync(d, { withFileTypes: true }); } catch (e) { return out; }
  for (const e of es) {
    const f = path.join(d, e.name);
    if (e.isDirectory()) walk(f, out);
    else if (e.name.endsWith('.json')) out.push(f);
  }
  return out;
}
const files = walk(GEM);
let readN = 0, readWithTags = 0, readNoTags = 0;
const readTagSamples = [];
for (const f of files) {
  let j; try { j = JSON.parse(fs.readFileSync(f, 'utf8')); } catch (e) { continue; }
  if (j.entry !== 'read') continue;
  readN++;
  const t = j.tags;
  if (Array.isArray(t) && t.length) { readWithTags++; if (readTagSamples.length < 10) readTagSamples.push(path.basename(f) + ' -> ' + JSON.stringify(t)); }
  else readNoTags++;
}
console.log('read 型 gem 条目 =', readN, ' 其中有 tags =', readWithTags, ' 无 tags =', readNoTags);
console.log(readTagSamples.join('\n'));

// delivery 类型（排除 delivery_mod）
const deliv = {};
for (const f of files) {
  const s = fs.readFileSync(f, 'utf8');
  for (const m of s.matchAll(/\"coe:delivery_([a-z_]+)\"/g)) {
    if (m[1] === 'mod') continue;
    deliv[m[1]] = (deliv[m[1]] || 0) + 1;
  }
}
console.log('\ndelivery 类型（排除 delivery_mod）:', JSON.stringify(deliv));
console.log('种类数 =', Object.keys(deliv).length);
const allTypes = ['area', 'projectile', 'self', 'aura', 'orb', 'beam', 'thrown', 'hazard', 'ball', 'remnant'];
console.log('零使用:', allTypes.filter(t => !deliv[t]).join(',') || '(无)');
