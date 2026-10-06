/**
 * 从权威 all_skills.json 的 quality_bonus 解析品质参数，产出 crawled/quality_params.json
 *
 * 所有规则均为实测得出（见 quality_params.md「解析规则」章节）。
 * 用法： node parse_quality.js [--diag]
 */
const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data/crawled');
const GEMDIR = path.join(ROOT, 'src/generated/resources/data/coe/coe/item/gem');
const DIAG = process.argv.includes('--diag');

/* ---------------------------------------------------------------- 1 权威数据 */
const ALL = JSON.parse(fs.readFileSync(path.join(OUT, 'all_skills.json'), 'utf8'));
const E = ALL.entries;
const Q = E.filter(e => typeof e.quality_bonus === 'string' && e.quality_bonus.trim() !== '');

/* ------------------------------------------- 2 权威 id -> 本地 datagen id（反向 crosswalk） */
const CW = JSON.parse(fs.readFileSync(path.join(OUT, 'crosswalk.json'), 'utf8')).entries;
const auth2local = new Map();
for (const r of CW) if (r.authoritative_id && !auth2local.has(r.authoritative_id))
  auth2local.set(r.authoritative_id, r.local_id);
for (const e of E) if (e.local_id && !auth2local.has(e.id)) auth2local.set(e.id, e.local_id);

/* ------------------------------------------- 3 本地 gem JSON：ref 链 + 槽位收集 */
function walk(dir, rel = '') {
  let o = []; let es;
  try { es = fs.readdirSync(dir, { withFileTypes: true }); } catch (e) { return o; }
  for (const e of es) {
    const f = path.join(dir, e.name);
    if (e.isDirectory()) o = o.concat(walk(f, rel + e.name + '/'));
    else if (e.name.endsWith('.json')) o.push({ rel: rel + e.name.replace(/\.json$/, ''), abs: f });
  }
  return o;
}
const byBase = new Map();
for (const g of walk(GEMDIR)) {
  const b = g.rel.split('/').pop();
  if (!byBase.has(b)) byBase.set(b, []);
  byBase.get(b).push(g.abs);
}
function readJson(abs) { try { return JSON.parse(fs.readFileSync(abs, 'utf8')); } catch (e) { return null; } }

function collectSlots(j, slots) {
  if (!j || typeof j !== 'object') return;
  if (j.params && typeof j.params === 'object')
    for (const k of Object.keys(j.params)) slots.add('params.' + k);
  if (Array.isArray(j.cost)) for (const c of j.cost)
    if (c && typeof c === 'object') for (const k of Object.keys(c)) slots.add('cost.' + k);
  if (j.require) for (const k of Object.keys(j.require)) slots.add('require.' + k);
  if (j.cast) for (const k of Object.keys(j.cast)) slots.add('cast.' + k);
  for (const k of ['curse_duration', 'curse_range', 'mark_duration', 'mark_range',
    'mana_cost_multiplier', 'reservation_multiplier', 'max_level'])
    if (j[k] !== undefined) slots.add(k);
  for (const ph of (j.phases || [])) {
    const p = ph.phase || {};
    for (const a of (p.actions || []))
      for (const [tk, body] of Object.entries(a.action || {})) {
        if (!tk.startsWith('coe:')) continue;
        for (const fk of Object.keys(body || {})) slots.add(tk.slice(4) + '.' + fk);
      }
    for (const [tk, body] of Object.entries(p.delivery || {})) {
      if (!tk.startsWith('coe:')) continue;
      for (const fk of Object.keys(body || {})) slots.add(tk.slice(4) + '.' + fk);
      const sh = (body || {}).shape;
      if (sh) for (const [sk, sb] of Object.entries(sh)) {
        if (!sk.startsWith('coe:')) continue;
        for (const fk of Object.keys(sb || {})) slots.add(sk.slice(4) + '.' + fk);
      }
    }
  }
}
const slotCache = new Map();
function slotsFor(localId) {
  if (slotCache.has(localId)) return slotCache.get(localId);
  const slots = new Set(); const seen = new Set();
  let queue = [localId], depth = 0;
  while (queue.length && depth < 6) {
    depth++;
    const next = [];
    for (const id of queue) {
      if (seen.has(id)) continue;
      seen.add(id);
      for (const abs of (byBase.get(id) || [])) {
        const j = readJson(abs);
        if (!j) continue;
        if (j.entry === 'read' && j.ref) { next.push(String(j.ref).replace(/^coe:/, '').split('/').pop()); continue; }
        collectSlots(j, slots);
      }
    }
    queue = next;
  }
  slotCache.set(localId, slots);
  return slots;
}

/* ---------------------------------------------------------------- 4 数值解析 */
const RE_PAREN = /\(\s*(-?\d+(?:\.\d+)?)\s*-\s*(-?\d+(?:\.\d+)?)\s*%?\s*\)/g;
const RE_BRACK = /\[\s*(-?\d+(?:\.\d+)?)\s*,\s*(-?\d+(?:\.\d+)?)\s*\]/g;
function extractRanges(text) {
  const out = [];
  for (const m of text.matchAll(RE_PAREN)) out.push({ L: parseFloat(m[1]), U: parseFloat(m[2]), raw: m[0] });
  for (const m of text.matchAll(RE_BRACK)) out.push({ L: parseFloat(m[1]), U: parseFloat(m[2]), raw: m[0] });
  return out;
}
const PER = 20;   // PoE2 品质上限；实测结论见 md「per 的取值」

/* ---------------------------------------------------------------- 5 语义识别 */
const COND_PATTERNS = [
  /\bwhile\b/i, /\bif\b/i, /\bagainst\b/i, /\bfor each\b/i, /\bper (endurance|power|frenzy|intelligence)/i,
  /\bbased on\b/i, /\bwhen\b/i, /\bon (hit|kill|crit|ignit|shock|freez|stun|block)/i,
  /\bwith hits\b/i, /\bempowered\b/i, /\bup to\b/i, /\btravel\b/i, /\bin range\b/i,
  /\bto (enemies|targets)\b/i, /\bconsumed\b/i, /\bper additional\b/i, /\bwith attached\b/i,
  /\bof (overkill|minion|its)\b/i, /\bdestroyed by\b/i, /\bchannelling\b/i, /\bsocketed\b/i,
];

function detectOperation(t) {
  if (/\bas extra\b|gain .*% of damage as/i.test(t)) return 'gain_as_extra';
  if (/\breduced\b/i.test(t)) return 'reduced';
  if (/\bmore\b/i.test(t)) return 'more';
  if (/\bincreased\b/i.test(t)) return 'increased';
  if (/\bless\b/i.test(t)) return 'reduced';
  // 平坦加值：+(0-X) / X is (0-Y) / Limit (0-N) ... / 以单位结尾
  if (/\+\s*[\[(]/.test(t)) return 'added';
  if (/\bis\s+[\[(]/.test(t)) return 'added';
  if (/^\s*(limit|fires|chains|consumes|gains?|grants?|regenerate|recovers?|leeches|plant)\b/i.test(t)) return 'added';
  if (/^\s*[\[(]/.test(t)) return 'added';
  if (/\b(metres|seconds|second)\b/.test(t)) return 'added';
  return 'unknown';
}

// poe2db 内部 stat 原文形态：<snake_case stat> [L,U]，无人类可读动词
const RE_INTERNAL = /^([a-z0-9_ ]+?)\s*\[\s*-?[\d.]+\s*,\s*-?[\d.]+\s*\]\s*$/i;
const INTERNAL_DIM = [
  [/damage/, 'damage'], [/radius|area of effect/, 'radius'], [/duration/, 'duration'],
  [/chill effect|ailment|magnitude/, 'ailment_magnitude'],
  [/max .*|number of|allowed/, 'count_limit'],
];
function internalDim(stat) {
  for (const [re, d] of INTERNAL_DIM) if (re.test(stat)) return d;
  return 'unknown';
}

const DIM_RULES = [
  ['cooldown_recovery', /cooldown recovery/i],
  ['attack_speed', /attack speed/i],
  ['cast_speed', /cast speed/i],
  ['movement_speed', /movement speed/i],
  ['projectile_speed', /projectile speed/i],
  ['recovery_rate', /regeneration rate|recovery rate|regen/i],
  ['reservation_efficiency', /reservation efficiency/i],
  ['energy_gain', /energy gained/i],
  ['flask_charge', /flask charges/i],
  ['stun_threshold', /stun threshold/i],
  ['rage', /\brage\b/i],
  ['duration', /duration|seconds to|lifetime|lasting|\bfaster\b/i],
  ['radius', /radius|metres to/i],
  ['length', /\blength\b/i],
  ['height_width', /\bwidth\b/i],
  ['area', /area of effect|\baoe\b/i],
  ['knockback', /knockback/i],
  ['crit', /crit/i],
  ['block', /block chance/i],
  ['accuracy', /accuracy/i],
  ['resistance', /resist/i],
  ['penetration', /penetrat/i],
  ['armour_break', /break .*armour|armour.*break/i],
  ['evasion', /evasion/i],
  ['energy_shield', /energy shield/i],
  ['exposure', /exposure/i],
  ['ailment_buildup', /buildup/i],
  ['ailment_magnitude', /magnitude|effect of/i],
  ['chance', /\bchance\b/i],
  ['stage', /stage/i],
  ['damage', /damage/i],
  ['count_limit', /\blimit\b|maximum number|number of|patch|pustule|crystal|\bbolts?\b|\bmissiles?\b|tendrils?|corpses?|chains?\b|\btargets?\b|\btimes\b|split/i],
  ['cost', /\bcost\b|mana cost/i],
  ['require_level', /level requirement|requires level/i],
  ['range', /\brange\b/i],
  ['health', /maximum life|\blife\b|\bhealth\b/i],
  ['effect', /effect/i],
  ['speed', /speed/i],
];
function detectDimension(t) {
  for (const [d, re] of DIM_RULES) if (re.test(t)) return d;
  return 'unknown';
}

/* ------------------------------------------- 6 维度 -> 槽位（含上下文收窄） */
const DIM_SLOTS = {
  damage: ['action_damage.amount', 'params.dmg', 'params.totem_damage', 'params.fissure_damage',
    'params.warcry_damage', 'params.mark_damage'],
  radius: ['shape_sphere.radius', 'action_create_ground.radius', 'action_aura.radius',
    'action_banner.radius', 'action_spawn.radius', 'delivery_orb.radius',
    'delivery_thrown.radius', 'params.warcry_radius', 'params.totem_range',
    'params.aura_radius', 'params.banner_radius', 'params.fissure_range',
    'params.curse_range', 'params.mark_range'],
  length: ['delivery_beam.range', 'delivery_beam.width', 'params.fissure_range'],
  height_width: ['delivery_beam.width'],
  duration: ['action_aura.area_duration', 'action_aura.status_duration',
    'action_banner.area_duration', 'action_spawn.duration', 'action_stat.duration',
    'delivery_hazard.lifetime', 'delivery_orb.lifetime', 'delivery_thrown.lifetime',
    'params.totem_duration', 'params.fissure_duration', 'params.summon_duration',
    'params.banner_duration', 'params.aura_area_duration', 'params.aura_status_duration',
    'curse_duration', 'mark_duration', 'params.curse_duration', 'params.mark_duration'],
  ailment_duration: ['action_aura.status_duration', 'params.aura_status_duration',
    'curse_duration', 'mark_duration'],
  cost: ['cost.value', 'mana_cost_multiplier', 'reservation_multiplier'],
  count_limit: ['action_spawn.count', 'params.summon_count'],
  interval: ['action_spawn.interval', 'params.totem_interval', 'params.fissure_interval'],
  health: ['action_spawn.health', 'params.totem_health', 'params.fissure_health'],
  magnitude: ['action_aura.magnitude', 'action_banner.magnitude',
    'params.aura_magnitude', 'params.banner_magnitude'],
  knockback: ['action_damage.knockback', 'params.warcry_knockback'],
  range: ['params.curse_range', 'params.mark_range', 'delivery_beam.range',
    'curse_range', 'mark_range'],
  crit: ['cast.crit'],
  cast_time: ['cast.time'],
  cooldown: ['cast.cooldown'],
  require_level: ['require.level'],
};
// 无量纲/无落点维度（恒 B）
const NO_SLOT_DIMS = new Set([
  'attack_speed', 'cast_speed', 'movement_speed', 'projectile_speed', 'cooldown_recovery',
  'recovery_rate', 'reservation_efficiency', 'energy_gain', 'flask_charge', 'stun_threshold',
  'rage', 'block', 'accuracy', 'resistance', 'penetration', 'armour_break', 'evasion',
  'energy_shield', 'exposure', 'ailment_buildup', 'ailment_magnitude', 'chance', 'stage',
  'effect', 'speed', 'area', 'unknown', 'gain_as_extra',
]);

// 上下文收窄：先按文案关键词挑更精确的槽位子集，再用全集兜底
const SLOT_CTX = {
  damage: [
    [/totem/i, ['params.totem_damage', 'action_damage.amount']],
    [/fissure/i, ['params.fissure_damage', 'action_damage.amount']],
    [/warcry|shockwave|aftershock/i, ['params.warcry_damage', 'action_damage.amount']],
    [/mark/i, ['params.mark_damage', 'action_damage.amount']],
  ],
  radius: [
    [/warcry|shockwave/i, ['params.warcry_radius']],
    [/totem/i, ['params.totem_range']],
    [/fissure/i, ['params.fissure_range']],
    [/curse/i, ['params.curse_range', 'curse_range']],
    [/mark/i, ['params.mark_range', 'mark_range']],
    [/aura|banner|presence|sigil/i, ['params.aura_radius', 'params.banner_radius',
      'action_aura.radius', 'action_banner.radius']],
    [/(explosion|impact|pulse|cloud|eruption|bloom|ritual|blast|nova|swarm|light)/i,
      ['shape_sphere.radius', 'delivery_orb.radius', 'delivery_thrown.radius',
        'action_create_ground.radius']],
  ],
  duration: [
    [/warcry/i, ['params.warcry_duration']],
    [/totem/i, ['params.totem_duration', 'action_spawn.duration']],
    [/fissure/i, ['params.fissure_duration']],
    [/curse/i, ['curse_duration', 'params.curse_duration']],
    [/mark/i, ['mark_duration', 'params.mark_duration']],
    [/banner/i, ['params.banner_duration', 'action_banner.area_duration']],
    [/cloud|hazard|orb|ground|trap|rune|tornado/i,
      ['delivery_hazard.lifetime', 'delivery_orb.lifetime', 'delivery_thrown.lifetime']],
    [/buff|wisp|mirage|aura|presence|sigil|construct|bone|plating|stage|weakness/i,
      ['action_aura.area_duration', 'params.aura_area_duration', 'action_stat.duration',
        'action_spawn.duration']],
  ],
  health: [[/totem/i, ['params.totem_health', 'action_spawn.health']]],
};
function candidateSlots(dim, text) {
  const full = DIM_SLOTS[dim] || [];
  const ctx = SLOT_CTX[dim];
  if (!ctx) return full.slice();
  for (const [re, list] of ctx) if (re.test(text)) return list.concat(full.filter(x => !list.includes(x)));
  return full.slice();
}

/* ---------------------------------------------------------------- 7 主解析 */
const rows = [];
let nCompound = 0, nNoRange = 0, nMulti = 0, nNegL = 0, nInternal = 0;

for (const e of Q) {
  const raw = e.quality_bonus;
  const segs = [...new Set(raw.split('|').map(s => s.trim()).filter(Boolean))];

  const rec = {
    id: e.id,
    local_id: auth2local.get(e.id) || null,
    gem_type: e.gem_type,
    name_en: e.name_en,
    name_zh: e.name_zh || null,
    implemented: !!e.implemented,
    raw_quality_bonus: raw,
    quality_range: e.quality_range || null,
    base: null, add: null, per: null,
    operation: null, dimension: null,
    action_point: null,
    action_point_class: null,
    wire_field: null,
    wire_mechanism: null,
    zero_change_ready: false,
    candidate_fields: null,
    conditional: false,
    condition_text: null,
    parse_confidence: null,
    parse_note: null,
  };

  // C-1：去重后仍多段 → 复合加成
  if (segs.length > 1) {
    nCompound++;
    Object.assign(rec, {
      action_point_class: 'C', parse_confidence: 'low',
      parse_note: '去重后仍有 ' + segs.length + ' 个不同子句（复合加成），无法归因到单一参数：' + segs.join(' ／ '),
    });
    rows.push(rec); continue;
  }
  const t = segs[0];

  // 内部 stat 原文（无人类动词，但 stat 名可直接读）
  const im = RE_INTERNAL.exec(t);
  if (im) { nInternal++; rec.parse_note = 'poe2db 内部 stat 原文形态，stat 名 = "' + im[1].trim() + '"'; }

  const ranges = extractRanges(t);
  if (ranges.length === 0) {
    nNoRange++;
    Object.assign(rec, {
      action_point_class: 'C', parse_confidence: 'low',
      parse_note: '文本中未找到 (L-U) 或 [L,U] 数值区间',
    });
    rows.push(rec); continue;
  }
  if (ranges.length > 1) {
    nMulti++;
    Object.assign(rec, {
      action_point_class: 'C', parse_confidence: 'low',
      parse_note: '单句含 ' + ranges.length + ' 个数值区间（' + ranges.map(r => r.raw).join(', ') + '），疑为多 stat 连写，不臆造拆分',
    });
    rows.push(rec); continue;
  }

  const { L, U } = ranges[0];
  if (L < 0) nNegL++;
  rec.base = L;
  rec.add = +(U - L).toFixed(6);
  rec.per = PER;
  rec.operation = detectOperation(t);
  rec.dimension = im ? internalDim(im[1].toLowerCase()) : detectDimension(t);
  rec.conditional = COND_PATTERNS.some(re => re.test(t));
  if (rec.conditional) {
    const mm = t.match(/(while .*|if .*|against .*|for each .*|based on .*|up to .*|per .*|with .*)/i);
    rec.condition_text = mm ? mm[1] : t;
  }
  if (rec.operation === 'gain_as_extra') rec.dimension = 'gain_as_extra';

  const dim = rec.dimension;
  // B：维度明确但当前 schema 无槽位
  if (NO_SLOT_DIMS.has(dim)) {
    Object.assign(rec, {
      action_point_class: 'B', action_point: dim === 'unknown' ? null : dim,
      wire_mechanism: null, parse_confidence: dim === 'unknown' ? 'low' : 'medium',
      parse_note: dim === 'unknown'
        ? '无法从文案判定作用维度'
        : '作用维度 = ' + dim + '，当前 gem schema 无对应数值槽位（需新增字段或接入 stat/effect-mod 体系）',
    });
    rows.push(rec); continue;
  }

  const cand = candidateSlots(dim, t);
  rec.candidate_fields = cand;
  if (cand.length === 0) {
    Object.assign(rec, {
      action_point_class: 'B', action_point: dim, parse_confidence: 'medium',
      parse_note: '作用维度 = ' + dim + '，无对应数值槽位',
    });
    rows.push(rec); continue;
  }

  const mech = (rec.operation === 'added' || rec.operation === 'reduced')
    ? 'per_quality_add_value'
    : (rec.operation === 'increased' || rec.operation === 'more')
      ? 'per_quality_scale(需新注册乘法表达式)'
      : 'unknown';

  if (rec.local_id) {
    const slots = slotsFor(rec.local_id);
    const hit = cand.filter(c => slots.has(c));
    if (hit.length) {
      Object.assign(rec, {
        action_point_class: 'A', action_point: dim,
        wire_field: hit[0], wire_mechanism: mech,
        zero_change_ready: mech === 'per_quality_add_value',
        candidate_fields: hit, parse_confidence: 'high',
        parse_note: im ? rec.parse_note : null,
      });
    } else {
      Object.assign(rec, {
        action_point_class: 'B', action_point: dim, wire_mechanism: null,
        parse_confidence: 'medium',
        parse_note: '作用维度 = ' + dim + '，但该 gem（' + rec.local_id + '）JSON 中不存在候选槽位 [' + cand.join(', ') + ']',
      });
    }
  } else {
    Object.assign(rec, {
      action_point_class: 'A', action_point: dim,
      wire_field: cand[0], wire_mechanism: mech,
      zero_change_ready: mech === 'per_quality_add_value',
      parse_confidence: 'medium',
      parse_note: '无本地 datagen id，未对 gem JSON 实测槽位；按作用维度给出首选槽位',
    });
  }
  rows.push(rec);
}

/* ---------------------------------------------------------------- 8 统计 & 输出 */
const cnt = f => rows.filter(f).length;
const byClass = {}; for (const r of rows) byClass[r.action_point_class] = (byClass[r.action_point_class] || 0) + 1;
function tally(pred, key) {
  const m = {}; for (const r of rows) if (pred(r)) m[r[key] ?? 'null'] = (m[r[key] ?? 'null'] || 0) + 1; return m;
}
const aDim = tally(r => r.action_point_class === 'A', 'dimension');
const aOp = tally(r => r.action_point_class === 'A', 'operation');
const aMech = tally(r => r.action_point_class === 'A', 'wire_mechanism');
const aField = tally(r => r.action_point_class === 'A', 'wire_field');
const bDim = tally(r => r.action_point_class === 'B', 'dimension');

console.log('权威总条目           =', E.length);
console.log('含 quality_bonus     =', Q.length);
console.log('A / B / C            =', JSON.stringify(byClass));
console.log('C 细分: 复合多段 =', nCompound, ' 无区间 =', nNoRange, ' 多区间 =', nMulti);
console.log('内部 stat 原文形态   =', nInternal, '   L<0 =', nNegL);
console.log('映射到本地 id        =', cnt(r => r.local_id), ' / 未映射 =', cnt(r => !r.local_id));
console.log('A 中已对 gem 实测    =', cnt(r => r.action_point_class === 'A' && r.parse_confidence === 'high'));
console.log('A 中零改动可接线     =', cnt(r => r.action_point_class === 'A' && r.zero_change_ready));
console.log('A 中带条件从句       =', cnt(r => r.action_point_class === 'A' && r.conditional));
console.log('\nA 按作用维度:'); Object.entries(aDim).sort((a, b) => b[1] - a[1]).forEach(([k, v]) => console.log('   ' + k.padEnd(22) + v));
console.log('\nA 按运算:'); Object.entries(aOp).sort((a, b) => b[1] - a[1]).forEach(([k, v]) => console.log('   ' + String(k).padEnd(22) + v));
console.log('\nA 按接线机制:'); Object.entries(aMech).sort((a, b) => b[1] - a[1]).forEach(([k, v]) => console.log('   ' + String(k).padEnd(40) + v));
console.log('\nA 按目标字段:'); Object.entries(aField).sort((a, b) => b[1] - a[1]).forEach(([k, v]) => console.log('   ' + String(k).padEnd(30) + v));
console.log('\nB 按作用维度:'); Object.entries(bDim).sort((a, b) => b[1] - a[1]).forEach(([k, v]) => console.log('   ' + k.padEnd(22) + v));

if (DIAG) {
  console.log('\n===== A 样例 (前 45) =====');
  rows.filter(r => r.action_point_class === 'A').slice(0, 45).forEach(r =>
    console.log('  [' + r.dimension + '/' + r.operation + '] ' + String(r.id).padEnd(24) + '-> ' + String(r.wire_field).padEnd(24) + ' :: ' + r.raw_quality_bonus.split('|')[0].trim()));
  console.log('\n===== B 样例 (前 45) =====');
  rows.filter(r => r.action_point_class === 'B').slice(0, 45).forEach(r =>
    console.log('  [' + String(r.dimension).padEnd(20) + '] ' + String(r.id).padEnd(24) + ':: ' + r.raw_quality_bonus.split('|')[0].trim()));
  console.log('\n===== dimension=unknown 全部 =====');
  rows.filter(r => r.dimension === 'unknown').forEach(r =>
    console.log('  ' + r.action_point_class + ' ' + String(r.id).padEnd(26) + ':: ' + r.raw_quality_bonus.split('|')[0].trim()));
  console.log('\n===== operation=unknown 全部 =====');
  rows.filter(r => r.operation === 'unknown').forEach(r =>
    console.log('  ' + r.action_point_class + ' [' + r.dimension + '] ' + String(r.id).padEnd(26) + ':: ' + r.raw_quality_bonus.split('|')[0].trim()));
}

fs.writeFileSync(path.join(OUT, 'quality_params.json'), JSON.stringify({
  note: 'PoE2 权威 quality_bonus → 可落地品质参数。数值语义：value = base + (quality / per) * add，'
    + '落盘形态 "coe:per_quality_add_value(add = <add>, base = <base>, per = 20)"（见 GenEvent.registerExpression）。'
    + 'action_point_class: A=有现有字段可落 / B=语义清楚但当前无落点 / C=无法解析。',
  generated_at: new Date().toISOString(),
  source: 'design/refactor/skill-data/crawled/all_skills.json (quality_bonus + quality_range)',
  per_rationale: 'per 恒为 20。实测：quality_range 是「数值区间」而非「品质区间」——259 条非空中 100% 起点为 0，'
    + '257/259 与文案 (L-U) 完全一致（另 2 条为多区间复合条目），故 U 是 quality=20（品质上限）时的取值，per=20。',
  counts: {
    authoritative_entries: E.length,
    with_quality_bonus: Q.length,
    class_A: byClass.A || 0, class_B: byClass.B || 0, class_C: byClass.C || 0,
    mapped_to_local_id: cnt(r => r.local_id),
    unmapped: cnt(r => !r.local_id),
    A_verified_against_gem_json: cnt(r => r.action_point_class === 'A' && r.parse_confidence === 'high'),
    A_zero_change_ready: cnt(r => r.action_point_class === 'A' && r.zero_change_ready),
    A_conditional: cnt(r => r.action_point_class === 'A' && r.conditional),
    C_compound_segment: nCompound, C_no_range: nNoRange, C_multi_range: nMulti,
    internal_stat_form: nInternal,
  },
  a_by_dimension: aDim, a_by_operation: aOp, a_by_mechanism: aMech, a_by_field: aField,
  b_by_dimension: bDim,
  entries: rows,
}, null, 1));
console.log('\n已写出 crawled/quality_params.json');
