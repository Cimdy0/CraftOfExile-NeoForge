/** 探测4：本地可用的 stat / attribute / operation 取值域，用于判定品质加成"有没有落点" */
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

const statIds = new Map();     // action_stat.id -> count
const modAttr = new Map();     // action_modifier.attribute -> count
const modOp = new Map();       // action_modifier.operation -> count
const modTags = new Map();
const paramsKeys = new Set();
const modifiersKeys = new Set();
const effectsKeys = new Set();

for (const { f } of files) {
  let j; try { j = JSON.parse(fs.readFileSync(f, 'utf8')); } catch (e) { continue; }
  if (j.entry === 'read' && j.ref) continue;
  if (j.params) for (const k of Object.keys(j.params)) paramsKeys.add(k);
  if (Array.isArray(j.modifiers)) j.modifiers.forEach(m => Object.keys(m).forEach(k => modifiersKeys.add(k)));
  else if (j.modifiers && typeof j.modifiers === 'object') Object.keys(j.modifiers).forEach(k => modifiersKeys.add(k));
  if (j.effects) Object.keys(j.effects).forEach(k => effectsKeys.add(k));
  (j.phases || []).forEach(ph => {
    (ph.phase?.actions || []).forEach(a => {
      const act = a.action || {};
      if (act['coe:action_stat']) {
        const id = act['coe:action_stat'].id;
        statIds.set(String(id), (statIds.get(String(id)) || 0) + 1);
      }
      if (act['coe:action_modifier']) {
        const m = act['coe:action_modifier'];
        modAttr.set(String(m.attribute), (modAttr.get(String(m.attribute)) || 0) + 1);
        modOp.set(String(m.operation), (modOp.get(String(m.operation)) || 0) + 1);
        (m.tags || []).forEach(t => modTags.set(t, (modTags.get(t) || 0) + 1));
      }
    });
  });
}

console.log('=== action_stat.id 取值域 (' + statIds.size + ') ===');
[...statIds].sort((a, b) => b[1] - a[1]).forEach(([k, v]) => console.log('  ' + String(k).padEnd(40) + v));
console.log('\n=== action_modifier.attribute 取值域 (' + modAttr.size + ') ===');
[...modAttr].sort((a, b) => b[1] - a[1]).forEach(([k, v]) => console.log('  ' + String(k).padEnd(40) + v));
console.log('\n=== action_modifier.operation 取值域 (' + modOp.size + ') ===');
[...modOp].sort((a, b) => b[1] - a[1]).forEach(([k, v]) => console.log('  ' + String(k).padEnd(40) + v));
console.log('\n=== action_modifier.tags ===');
[...modTags].sort((a, b) => b[1] - a[1]).slice(0, 30).forEach(([k, v]) => console.log('  ' + String(k).padEnd(30) + v));
console.log('\n=== params keys ===\n ', [...paramsKeys].sort().join(', '));
console.log('=== modifiers keys ===\n ', [...modifiersKeys].sort().join(', '));
console.log('=== effects keys ===\n ', [...effectsKeys].sort().join(', '));

// 查 stat 注册表目录
const SD = path.join(ROOT, 'src/generated/resources/data/coe/coe');
try {
  console.log('\n=== coe data 子目录 ===');
  console.log(fs.readdirSync(SD, { withFileTypes: true }).filter(e => e.isDirectory()).map(e => e.name).join(', '));
} catch (e) { console.log('no dir', e.message); }
