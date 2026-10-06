/** 探测3：本地 gem JSON 的字段全集 / action 类型 / delivery 类型 / shape 类型，用于判定"作用点" */
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const D = path.join(ROOT, 'src/generated/resources/data/coe/coe/item/gem');
function walk(d, p = '') {
  let o = []; let es; try { es = fs.readdirSync(d, { withFileTypes: true }); } catch (e) { return o; }
  for (const e of es) {
    const f = path.join(d, e.name);
    if (e.isDirectory()) o = o.concat(walk(f, p + e.name + '/'));
    else if (e.name.endsWith('.json')) o.push({ p: p + e.name, f });
  }
  return o;
}
const files = walk(D);
console.log('files =', files.length);

const topKeys = new Set();
const actionTypes = new Set();
const deliveryTypes = new Set();
const shapeTypes = new Set();
const actionFields = new Map();   // actionType -> Set(field)
const deliveryFields = new Map();
const exprSamples = [];

function scanAction(a) {
  for (const k of Object.keys(a)) {
    if (!k.startsWith('coe:')) continue;
    actionTypes.add(k);
    const body = a[k] || {};
    if (!actionFields.has(k)) actionFields.set(k, new Set());
    for (const fk of Object.keys(body)) actionFields.get(k).add(fk);
    for (const [fk, fv] of Object.entries(body)) {
      if (typeof fv === 'string' && fv.startsWith('coe:')) exprSamples.push(k + '.' + fk + ' = ' + fv);
    }
  }
}
function scanDelivery(d) {
  if (!d) return;
  for (const k of Object.keys(d)) {
    if (!k.startsWith('coe:')) continue;
    deliveryTypes.add(k);
    const body = d[k] || {};
    if (!deliveryFields.has(k)) deliveryFields.set(k, new Set());
    for (const fk of Object.keys(body)) deliveryFields.get(k).add(fk);
    const sh = body.shape;
    if (sh) for (const sk of Object.keys(sh)) if (sk.startsWith('coe:')) shapeTypes.add(sk);
  }
}

for (const { f } of files) {
  let j; try { j = JSON.parse(fs.readFileSync(f, 'utf8')); } catch (e) { continue; }
  if (j.entry === 'read' && j.ref) continue;
  for (const k of Object.keys(j)) topKeys.add(k);
  if (j.cost) for (const c of j.cost) for (const ck of Object.keys(c)) topKeys.add('cost.' + ck);
  if (j.require) for (const rk of Object.keys(j.require)) topKeys.add('require.' + rk);
  if (j.cast) for (const ck of Object.keys(j.cast)) topKeys.add('cast.' + ck);
  (j.phases || []).forEach(ph => {
    const p = ph.phase || {};
    for (const pk of Object.keys(p)) topKeys.add('phase.' + pk);
    (p.actions || []).forEach(a => {
      for (const ak of Object.keys(a)) if (ak !== 'action') topKeys.add('action.' + ak);
      scanAction(a.action || {});
    });
    scanDelivery(p.delivery);
  });
}

console.log('\n=== 顶层字段 ===\n ', [...topKeys].sort().join(', '));
console.log('\n=== action 类型 & 字段 ===');
[...actionTypes].sort().forEach(t => console.log('  ' + t + ' : ' + [...actionFields.get(t)].sort().join(', ')));
console.log('\n=== delivery 类型 & 字段 ===');
[...deliveryTypes].sort().forEach(t => console.log('  ' + t + ' : ' + [...deliveryFields.get(t)].sort().join(', ')));
console.log('\n=== shape 类型 ===\n ', [...shapeTypes].sort().join(', '));

console.log('\n=== 表达式模板使用统计 ===');
const tpl = new Map();
for (const s of exprSamples) { const m = /coe:([a-z_0-9]+)\(/.exec(s); if (m) tpl.set(m[1], (tpl.get(m[1]) || 0) + 1); }
[...tpl].sort((a, b) => b[1] - a[1]).forEach(([k, v]) => console.log('  ' + k.padEnd(30) + v));
console.log('\n=== 含 quality 的表达式（应为 0） ===', exprSamples.filter(s => /quality/i.test(s)).length);
