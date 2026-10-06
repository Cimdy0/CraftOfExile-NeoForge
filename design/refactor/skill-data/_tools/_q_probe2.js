/** 探测2：quality_bonus 的双侧重复、数值形态、quality_range 与数值的关系 */
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data/crawled');
const E = JSON.parse(fs.readFileSync(path.join(OUT, 'all_skills.json'), 'utf8')).entries;
const Q = E.filter(e => typeof e.quality_bonus === 'string' && e.quality_bonus.trim());

console.log('=== 1. 分隔符 " | " 分段统计 ===');
const segCount = new Map();
for (const e of Q) { const n = e.quality_bonus.split('|').length; segCount.set(n, (segCount.get(n) || 0) + 1); }
console.log(JSON.stringify([...segCount].sort((a, b) => a[0] - b[0])));

console.log('\n=== 2. 各段是否完全相同（去重后段数） ===');
const uniqCount = new Map();
for (const e of Q) {
  const segs = e.quality_bonus.split('|').map(s => s.trim()).filter(Boolean);
  const n = new Set(segs).size; uniqCount.set(n, (uniqCount.get(n) || 0) + 1);
}
console.log(JSON.stringify([...uniqCount].sort((a, b) => a[0] - b[0])));

console.log('\n=== 3. 去重后段数 > 1 的样例 ===');
Q.filter(e => new Set(e.quality_bonus.split('|').map(s => s.trim())).size > 1).slice(0, 25)
  .forEach(e => console.log('[' + e.id + '] ' + e.quality_bonus));

console.log('\n=== 4. 数值形态正则统计 ===');
const RE_RANGE = /\(\s*(-?[\d.]+)\s*-\s*(-?[\d.]+)\s*\)/g;
let noRange = [], oneRange = [], multiRange = [];
for (const e of Q) {
  const seg = e.quality_bonus.split('|')[0].trim();
  const m = [...seg.matchAll(RE_RANGE)];
  if (m.length === 0) noRange.push(e); else if (m.length === 1) oneRange.push(e); else multiRange.push(e);
}
console.log('无 (L-U) 区间 =', noRange.length, ' 单区间 =', oneRange.length, ' 多区间 =', multiRange.length);
console.log('\n-- 无区间样例(全部) --');
noRange.forEach(e => console.log('  [' + e.id + '] ' + e.quality_bonus));
console.log('\n-- 多区间样例 --');
multiRange.slice(0, 15).forEach(e => console.log('  [' + e.id + '] ' + e.quality_bonus));

console.log('\n=== 5. quality_range 与文本区间的关系 ===');
let eq = 0, neq = 0, qrNull = 0; const neqS = [];
for (const e of Q) {
  const seg = e.quality_bonus.split('|')[0].trim();
  const m = [...seg.matchAll(RE_RANGE)];
  if (!e.quality_range) { qrNull++; continue; }
  if (m.length === 0) { neq++; neqS.push(e); continue; }
  const L = parseFloat(m[0][1]), U = parseFloat(m[0][2]);
  if (L === e.quality_range[0] && U === e.quality_range[1]) eq++; else { neq++; neqS.push(e); }
}
console.log('quality_range 为 null =', qrNull, ' 与文本区间一致 =', eq, ' 不一致 =', neq);
neqS.slice(0, 20).forEach(e => console.log('  [' + e.id + '] qb=' + e.quality_bonus.split('|')[0].trim() + '  qr=' + JSON.stringify(e.quality_range)));

console.log('\n=== 6. qr 是否恒等于 0 起点 ===');
const qrStart = new Map();
for (const e of Q) if (e.quality_range) qrStart.set(e.quality_range[0], (qrStart.get(e.quality_range[0]) || 0) + 1);
console.log('quality_range[0] 分布 =', JSON.stringify([...qrStart]));
const qrEnd = new Map();
for (const e of Q) if (e.quality_range) qrEnd.set(e.quality_range[1], (qrEnd.get(e.quality_range[1]) || 0) + 1);
console.log('quality_range[1] 分布(top30) =', JSON.stringify([...qrEnd].sort((a, b) => b[1] - a[1]).slice(0, 30)));

console.log('\n=== 7. 带 % 与不带 % 的比例 ===');
let pct = 0, nopct = 0;
for (const e of Q) {
  const seg = e.quality_bonus.split('|')[0].trim();
  if (/\(\s*-?[\d.]+\s*-\s*-?[\d.]+\s*\)\s*%/.test(seg)) pct++; else nopct++;
}
console.log('区间后紧跟 % =', pct, ' 其它 =', nopct);

console.log('\n=== 8. 关键词命中统计（第一段小写全文） ===');
const KW = {
  'more damage': /more damage/i, 'increased damage': /increased damage/i, 'more.*damage': /more.*damage/i,
  'attack speed': /attack speed/i, 'cast speed': /cast speed/i, 'cooldown recovery': /cooldown recovery/i,
  'duration': /duration/i, 'radius': /radius/i, 'area': /area of effect|aoe/i, 'range': /range/i,
  'chance': /chance/i, 'cost': /cost/i, 'mana': /mana/i, 'crit': /crit/i, 'ailment': /ailment/i,
  'armour': /armour|armor/i, 'life': /life/i, 'energy shield': /energy shield/i, 'stun': /stun/i,
  'projectile': /projectile/i, 'limit': /limit/i, 'metres': /metres|meters/i, 'seconds': /seconds/i,
  'gain.*as extra': /gain .* as extra|as extra/i, 'reduced': /reduced/i, 'penetrat': /penetrat/i,
  'exposure': /exposure/i, 'magnitude': /magnitude/i, 'stage': /stage/i, 'buff': /buff/i,
  'totem': /totem/i, 'minion': /minion/i, 'rage': /rage/i, 'block': /block/i, 'accuracy': /accuracy/i,
  'resistance': /resist/i, 'effect': /effect/i, 'speed': /speed/i, 'damage': /damage/i,
};
const hit = {};
for (const e of Q) {
  const seg = e.quality_bonus.split('|')[0].trim();
  for (const [k, r] of Object.entries(KW)) if (r.test(seg)) hit[k] = (hit[k] || 0) + 1;
}
Object.entries(hit).sort((a, b) => b[1] - a[1]).forEach(([k, v]) => console.log('  ' + k.padEnd(22) + v));
