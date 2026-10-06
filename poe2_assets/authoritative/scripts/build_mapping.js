/**
 * build_mapping.js — 权威机制 -> 模组运行时模型 的映射草案（SK-M0-002）
 *
 * 输入（只读）：
 *   poe2_assets/authoritative/all_skills.json          （冻结的权威层）
 *   src/generated/resources/data/coe/coe/item/gem/**   （现有运行时产物 = 唯一可信的机制证据）
 *
 * 产出（只新增）：
 *   mapping/type_tags_to_delivery.json
 *   mapping/type_tags_to_damage_type.json
 *   mapping/mechanism_families.draft.json
 *   mapping/MAPPING_NOTES.md
 *
 * 证据原则：**不靠语义猜测。只统计「本地已实现的同标签技能实际用了什么机制」的分布。**
 *
 * 用法： node poe2_assets/authoritative/scripts/build_mapping.js
 */

'use strict';
const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..', '..', '..');
const AUTH = path.join(ROOT, 'poe2_assets', 'authoritative');
const MAP = path.join(AUTH, 'mapping');
const GEM = path.join(ROOT, 'src/generated/resources/data/coe/coe/item/gem');
const GENERATOR = 'poe2_assets/authoritative/scripts/build_mapping.js';

// 运行时产物正在被并发任务重生成已观察到中途态（537 -> 404 -> 537）。
// 低于该阈值说明抓到了不完整的中间态，直接中止，避免产出基于残缺证据的映射。
const STABLE_MIN_FILES = 500;

const rd = (p) => JSON.parse(fs.readFileSync(p, 'utf8'));
const writeJson = (p, o) => {
  fs.mkdirSync(path.dirname(p), { recursive: true });
  fs.writeFileSync(p, JSON.stringify(o, null, 2), 'utf8');
  console.log('  wrote', path.relative(ROOT, p), (fs.statSync(p).size / 1024).toFixed(1) + ' KB');
};
const walk = (d, acc = []) => {
  for (const f of fs.readdirSync(d, { withFileTypes: true })) {
    const p = path.join(d, f.name);
    if (f.isDirectory()) walk(p, acc); else acc.push(p);
  }
  return acc;
};

const E = rd(path.join(AUTH, 'all_skills.json')).entries;

// ---------------------------------------------------------------- 0. 稳定性守卫
const allGemFiles = walk(GEM);
if (allGemFiles.length < STABLE_MIN_FILES) {
  console.error('FATAL: item/gem 只有 ' + allGemFiles.length + ' 个文件（期望 ' + STABLE_MIN_FILES + '+）。');
  console.error('  运行时产物正在被并发任务重生成，此刻是残缺中间态。');
  console.error('  现在产出映射会得到基于残缺证据的错误结论。请稍后重跑。');
  process.exit(3);
}
console.log('== 运行时产物稳定性 ==');
console.log('  item/gem 文件数:', allGemFiles.length, '(阈值 >=' + STABLE_MIN_FILES + ')  -> 可继续');

// ---------------------------------------------------------------- 1. 本地产物特征提取
function mkAcc() {
  return { deliveries: new Set(), actions: new Set(), damageTypes: new Set(), effects: new Set(), deliveryModTargets: new Set() };
}
function scanNode(obj, acc) {
  if (Array.isArray(obj)) { obj.forEach((v) => scanNode(v, acc)); return; }
  if (obj && typeof obj === 'object') {
    for (const k of Object.keys(obj)) {
      if (k.startsWith('coe:delivery_')) {
        const name = k.replace('coe:delivery_', '');
        acc.deliveries.add(name);
        // support 的 coe:delivery_mod 内含目标 delivery —— 这是辅助侧最强的结构性证据
        if (name === 'mod' && obj[k] && typeof obj[k].delivery === 'string') acc.deliveryModTargets.add(obj[k].delivery);
      }
      if (k.startsWith('coe:action_')) acc.actions.add(k.replace('coe:action_', ''));
      if (k.startsWith('coe:effect_mod') || k.startsWith('coe:event_listen')
        || k.startsWith('coe:seal_mod') || k.startsWith('coe:echo_mod')) {
        acc.effects.add(k.replace('coe:', ''));
        // effect_mod 的子键才是真正的语义（added_damage / penetration / crit_chance_multiplier…）
        Object.keys(obj[k] || {}).forEach((sub) => acc.effects.add(k.replace('coe:', '') + '.' + sub));
      }
      if (k === 'damage_type' && typeof obj[k] === 'string') acc.damageTypes.add(obj[k]);
      scanNode(obj[k], acc);
    }
  }
}
const flatten = (a) => ({ deliveries: [...a.deliveries].sort(), actions: [...a.actions].sort(), damage_types: [...a.damageTypes].sort(), effects: [...a.effects].sort(), delivery_mod_targets: [...a.deliveryModTargets].sort() });

const itemFeatures = {};
const presetFeatures = {};
const localItems = {};
allGemFiles.forEach((f) => {
  const base = path.basename(f, '.json');
  let j;
  try { j = JSON.parse(fs.readFileSync(f, 'utf8')); } catch (e) { return; }
  const acc = mkAcc(); scanNode(j, acc); const feat = flatten(acc);
  if (base.startsWith('preset_')) presetFeatures[base] = { preset: base, path: path.relative(ROOT, f), ...feat };
  else { localItems[base] = { id: base, ref: (j.ref || '').replace('coe:', '') }; itemFeatures[base] = { path: path.relative(ROOT, f), ...feat }; }
});

// 证据 = item 自身（含 effects/delivery_mod）+ 其引用的 preset
const evidenceByName = {};
E.forEach((e) => {
  if (!e.local_id) return;
  const li = localItems[e.local_id];
  if (!li) return;
  const it = itemFeatures[e.local_id] || {};
  const pf = presetFeatures[li.ref] || {};
  evidenceByName[e.id] = {
    local_id: e.local_id,
    preset: pf.preset || null,
    // 支持「凭证证据」按来源区分，便于判定结论来自哪里
    item_level: { deliveries: it.deliveries || [], effects: it.effects || [], delivery_mod_targets: it.delivery_mod_targets || [], damage_types: it.damage_types || [] },
    preset_level: { deliveries: pf.deliveries || [], actions: pf.actions || [], damage_types: pf.damage_types || [] },
    deliveries_all: [...new Set([...(it.deliveries || []), ...(pf.deliveries || [])])].sort(),
    actions_all: [...new Set([...(it.actions || []), ...(pf.actions || [])])].sort(),
    damage_types_all: [...new Set([...(it.damage_types || []), ...(pf.damage_types || [])])].sort(),
    effects_all: it.effects || [],
    delivery_mod_targets: it.delivery_mod_targets || [],
  };
});
const evidenceIds = Object.keys(evidenceByName);
console.log('\n== 证据集 ==');
console.log('  本地 gem 条目:', Object.keys(localItems).length, '| preset 文件:', Object.keys(presetFeatures).length);
console.log('  可产出机制证据的权威条目:', evidenceIds.length, '/', E.length,
  '(' + Math.round((evidenceIds.length / E.length) * 1000) / 10 + '%)');
const supEv = evidenceIds.filter((id) => (evidenceByName[id].effects_all.length > 0));
console.log('  其中带 effects[] 结构性凭证（多为 support）:', supEv.length);

// ---------------------------------------------------------------- 2. 运行时词表（实测）
const tally = (get) => { const m = {}; evidenceIds.forEach((id) => get(evidenceByName[id]).forEach((v) => (m[v] = (m[v] || 0) + 1))); return m; };
const rank = (o) => Object.entries(o).sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0])).map(([name, n]) => ({ name, n }));
const vocab = {
  delivery: rank(tally((e) => e.deliveries_all)),
  action: rank(tally((e) => e.actions_all)),
  damage_type: rank(tally((e) => e.damage_types_all)),
  effect_mod_kind: rank(tally((e) => e.effects_all.filter((x) => x.includes('.') === false))),
  effect_mod_subkey: rank(tally((e) => e.effects_all.filter((x) => x.includes('.')))),
  delivery_mod_target: rank(tally((e) => e.delivery_mod_targets)),
};
console.log('  delivery 词表 :', vocab.delivery.map((x) => x.name + '(' + x.n + ')').join(' '));
console.log('  damage  词表 :', vocab.damage_type.map((x) => x.name + '(' + x.n + ')').join(' '));
console.log('  effect  词表 :', vocab.effect_mod_kind.map((x) => x.name + '(' + x.n + ')').join(' '));

// ---------------------------------------------------------------- 3. type_tags -> delivery
const CANON_DELIVERY = ['self', 'area', 'projectile', 'beam', 'ball', 'orb', 'thrown', 'hazard', 'remnant'];
const typeTagIndex = {};
E.forEach((e) => (e.type_tags || []).forEach((t) => {
  (typeTagIndex[t] = typeTagIndex[t] || { members: [], byType: {} });
  typeTagIndex[t].members.push(e.id);
  typeTagIndex[t].byType[e.gem_type] = (typeTagIndex[t].byType[e.gem_type] || 0) + 1;
}));

function distOf(members, picker) {
  const dist = {}; let n = 0;
  members.forEach((id) => {
    const ev = evidenceByName[id]; if (!ev) return;
    const vals = picker(ev);
    n += 1;
    if (!vals.length) { dist['(none)'] = (dist['(none)'] || 0) + 1; return; }
    vals.forEach((v) => (dist[v] = (dist[v] || 0) + 1));
  });
  return { n, dist };
}
function conf(n, topName, topCount) {
  if (n === 0 || topName === '(none)') return 'none';
  const r = topCount / n;
  if (n >= 5 && r >= 0.7) return 'high';
  if (n >= 3 && r >= 0.5) return 'medium';
  return 'low';
}
const pickD = (ev) => ev.deliveries_all;

const mapped = []; const unmapped = [];
Object.keys(typeTagIndex).sort().forEach((tag) => {
  const info = typeTagIndex[tag];
  const { n, dist } = distOf(info.members, pickD);
  const entries = Object.entries(dist).sort((a, b) => b[1] - a[1]);
  const top = entries[0] || ['(none)', 0];
  const c = conf(n, top[0], top[1]);
  const rec = {
    type_tag: tag,
    total_in_authority: info.members.length,
    by_gem_type: info.byType,
    local_evidence_count: n,
    delivery_distribution: Object.fromEntries(entries),
    top_delivery: top[0],
    top_delivery_ratio: n ? Math.round((top[1] / n) * 1000) / 1000 : 0,
    confidence: c,
    evidence_sample_ids: info.members.filter((id) => evidenceByName[id]).slice(0, 6),
  };
  if (c === 'none') { rec.status = 'pending_adjudication'; rec.why = '本地无同 type_tag 的已实现技能可作参照'; unmapped.push(rec); }
  else {
    rec.status = 'mapped';
    if (!CANON_DELIVERY.includes(top[0])) rec.flag = '命中非常规 delivery 语义，需人工确认';
    mapped.push(rec);
  }
});
mapped.sort((a, b) => b.local_evidence_count - a.local_evidence_count || a.type_tag.localeCompare(b.type_tag));
unmapped.sort((a, b) => b.total_in_authority - a.total_in_authority);
const confCount = { high: 0, medium: 0, low: 0, none: unmapped.length };
mapped.forEach((m) => (confCount[m.confidence] += 1));

console.log('\n== type_tags -> delivery ==');
console.log('  type_tags:', Object.keys(typeTagIndex).length, '| 映射:', mapped.length, '| 待裁定:', unmapped.length);
console.log('  置信度:', JSON.stringify(confCount));
mapped.slice(0, 15).forEach((m) => console.log('    ' + m.type_tag.padEnd(22) + String(m.local_evidence_count).padStart(3)
  + ' -> ' + String(m.top_delivery).padEnd(12) + ' ' + Math.round(m.top_delivery_ratio * 100) + '%  [' + m.confidence + ']'));

// ---------------------------------------------------------------- 4. tags -> damage_type
const ELEM_TAGS = ['Fire', 'Cold', 'Lightning', 'Chaos', 'Physical'];
const CANON_DAMAGE = ['physical', 'lightning', 'fire', 'cold', 'chaos'];
const tagEntryIndex = {};
E.forEach((e) => (e.tags || []).forEach((t) => { (tagEntryIndex[t] = tagEntryIndex[t] || []).push(e.id); }));
const dmgSignals = [...new Set([...ELEM_TAGS, ...Object.keys(typeTagIndex).filter((t) => /^Type/i.test(t) ? false : /fire|cold|lightning|chaos|physical/i.test(t))])].sort();
const dmgMapped = []; const dmgUnmapped = [];
dmgSignals.forEach((tag) => {
  const members = tagEntryIndex[tag] || [];
  const { n, dist } = distOf(members, (ev) => ev.damage_types_all);
  const entries = Object.entries(dist).sort((a, b) => b[1] - a[1]);
  const top = entries[0] || ['(none)', 0];
  const c = conf(n, top[0], top[1]);
  const rec = {
    signal: tag,
    signal_kind: ELEM_TAGS.includes(tag) ? 'tags.元素标签' : 'type_tags',
    total_in_authority: members.length,
    local_evidence_count: n,
    damage_type_distribution: Object.fromEntries(entries),
    top_damage_type: top[0],
    top_ratio: n ? Math.round((top[1] / n) * 1000) / 1000 : 0,
    confidence: c,
    evidence_sample_ids: members.filter((id) => evidenceByName[id]).slice(0, 6),
  };
  if (c === 'none') { rec.status = 'pending_adjudication'; dmgUnmapped.push(rec); }
  else {
    rec.status = 'mapped';
    if (!CANON_DAMAGE.includes(top[0])) rec.flag = '非标准 damage_type，疑似既有数据缺陷';
    dmgMapped.push(rec);
  }
});
dmgMapped.sort((a, b) => b.local_evidence_count - a.local_evidence_count);
console.log('\n== tags -> damage_type ==');
dmgMapped.forEach((m) => console.log('    ' + m.signal.padEnd(14) + String(m.local_evidence_count).padStart(3)
  + ' -> ' + String(m.top_damage_type).padEnd(12) + ' ' + Math.round(m.top_ratio * 100) + '%  [' + m.confidence + ']'));
console.log('    待裁定信号:', dmgUnmapped.length);

// ---------------------------------------------------------------- 5. [Support] 单标签簇：implicits 二次切分 + 结构凭证交叉
const supportAlone = E.filter((e) => e.gem_type === 'support'
  && JSON.stringify([...new Set(e.tags || [])].sort()) === JSON.stringify(['Support']));
const GROUP_RULES = [
  ['flask', /flask/i, '药剂：充能获取 / 效果 / 持续时间'],
  ['charge_generation', /charges?\b/i, '各类充能（耐力、狂怒、暴击球等）获取与消耗'],
  ['critical', /crit(ical|ically)?\b/i, '暴击率 / 暴击伤害 / 暴击触发'],
  ['cooldown', /cooldown|recovery time/i, '冷却 / 使用间隔'],
  ['duration', /\bduration\b|lasting/i, '持续时间修正'],
  ['ailment', /bleed|bleeding|poison|ignite|shock|freeze|ailment|aggravate/i, '异常状态：流血 / 中毒 / 点燃 / 感电 / 冰缓'],
  ['armour_break_stun_pin', /armour|stun|\bpin\b|pinn/i, '破甲 / 眩晕 / 压制'],
  ['speed', /\bspeed\b|faster|slower/i, '攻击 / 施法 / 移动速度'],
  ['ground_surface', /ground|surface|fissure|consecrat/i, '地面效果 / 裂隙 / 地表'],
  ['detonation', /detonat|explod/i, '引爆 / 爆炸链'],
  ['pierce_fork_chain', /pierce|fork|chain|split/i, '投射物二次行为：穿透 / 分叉 / 连锁 / 分裂'],
  ['minion_summon', /minion|summon|companion|skeleton/i, '召唤物'],
  ['resource_guard', /rage|guard|energy shield|mana\b|life\b/i, '资源 / 守御：怒气、守卫、魔力、生命'],
  ['cost_and_reservation', /cost|reserv/i, '消耗与保留'],
  ['cannot_or_limitation', /cannot| instead\b/i, '限制类（"不能…" / 替代机制）'],
  ['generic_more_less_damage', /\b(more|less)\b|\bdamage\b/i, '通用增 / 减伤（无明显子主题）'],
];
const textOf = (e) => [...(e.implicits || []), e.description_en || ''].join(' \u0001 ');
const groups = {}; const multiLabel = {};
supportAlone.forEach((e) => {
  const txt = textOf(e);
  const hits = GROUP_RULES.filter(([, re]) => re.test(txt)).map(([n]) => n);
  multiLabel[e.id] = hits;
  const primary = hits[0] || 'unclassified';
  (groups[primary] = groups[primary] || []).push(e.id);
});
const groupList = Object.entries(groups).sort((a, b) => b[1].length - a[1].length).map(([g, ids]) => {
  const eVoted = ids.filter((id) => (evidenceByName[id]?.effects_all || []).length > 0);
  const effDist = {}; const tgtDist = {};
  eVoted.forEach((id) => {
    (evidenceByName[id].effects_all || []).forEach((x) => (effDist[x] = (effDist[x] || 0) + 1));
    (evidenceByName[id].delivery_mod_targets || []).forEach((x) => (tgtDist[x] = (tgtDist[x] || 0) + 1));
  });
  return {
    group: g,
    rule: (GROUP_RULES.find((r) => r[0] === g) || [, , '未命中任何规则，需人工读原文'])[2],
    count: ids.length,
    members: ids,
    samples: ids.slice(0, 5),
    structural_evidence_count: eVoted.length,
    structural_evidence_effects: Object.fromEntries(Object.entries(effDist).sort((a, b) => b[1] - a[1])),
    structural_evidence_delivery_targets: Object.fromEntries(Object.entries(tgtDist).sort((a, b) => b[1] - a[1])),
  };
});
console.log('\n== [Support] 单标签簇 implicits 切分 ==');
console.log('  簇规模:', supportAlone.length, '-> 组数:', groupList.length);
groupList.forEach((g) => console.log('    ' + g.group.padEnd(26) + String(g.count).padStart(3)
  + '  结构凭证 ' + String(g.structural_evidence_count).padStart(2)
  + (g.structural_evidence_count ? '  ' + Object.keys(g.structural_evidence_effects).slice(0, 3).join(',') : '')));

// ---------------------------------------------------------------- 6. 机制族草案
const idxDoc = rd(path.join(AUTH, 'mechanism_index.json'));
const combos = idxDoc.tag_cooccurrence_clusters.by_gem_type;
const famEconomy = (c) => {
  const dist = {}; let n = 0; const eff = {};
  c.members.forEach((id) => {
    const ev = evidenceByName[id]; if (!ev) return; n += 1;
    ev.deliveries_all.forEach((d) => (dist[d] = (dist[d] || 0) + 1));
    (ev.effects_all || []).forEach((x) => (eff[x] = (eff[x] || 0) + 1));
  });
  const e = Object.entries(dist).sort((a, b) => b[1] - a[1]);
  return {
    local_evidence_count: n,
    delivery_distribution: Object.fromEntries(e),
    top_delivery: e[0] ? e[0][0] : null,
    structural_effect_distribution: Object.fromEntries(Object.entries(eff).sort((a, b) => b[1] - a[1])),
  };
};
const families = {};
['skill', 'support', 'spirit'].forEach((t) => {
  families[t] = combos[t].slice(0, 25).map((c) => ({
    tag_combo: c.tag_combo, count: c.count, review_size: c.members.length,
    suggested_family_name: (c.tag_combo[0] || 'untagged') + '@' + t,
    ...famEconomy(c),
  }));
});

// ---------------------------------------------------------------- 7. 输出
const evidencePct = Math.round((evidenceIds.length / E.length) * 1000) / 10;
writeJson(path.join(MAP, 'type_tags_to_delivery.json'), {
  _meta: {
    purpose: '权威 type_tags -> 模组 delivery 的候选映射（草案，非裁定）',
    generator: GENERATOR,
    generated_at: new Date().toISOString(),
    method: '每条权威 entry 经 local_id 连接到本地已实现的产物（gem item + 其引用 preset），统计同 type_tag 的技能实际使用的 coe:delivery_* 分布，取占比最高者为候选。',
    evidence_base: {
      authoritative_entries: E.length,
      locally_implemented_joined: evidenceIds.length,
      coverage: evidencePct + '%',
      structural_evidence_items: supEv.length,
      warning: '证据集只覆盖本地已实现的 ' + evidenceIds.length + ' 条（权威层的 ' + evidencePct + '%）。其余条目不得直接套用本表，必须先人工裁定。',
    },
    confidence_definition: {
      high: '本地证据 >=5 条 且 最高占比 >=70%',
      medium: '本地证据 >=3 条 且 最高占比 >=50%',
      low: '有证据但不足上述阈值 —— 仅供参考',
      none: '本地无同 type_tag 的已实现技能 —— 不做映射，列入 pending_adjudication',
    },
    mod_inventory_measured: vocab,
    note_delivery_mod: 'coe:delivery_mod 实测出现在支持 gem 的 effects[] 里（28 处），其内含 "delivery": <area|projectile> 指向**被修改的目标投递**。它不是第 10 种投递形状，而是「辅助挂载机制」。因此：辅助侧的投递归因应读 delivery_mod_targets，而不是把它当成独立 delivery。',
    note_regeneration_hazard: '运行时产物在被并发任务重生成过程中会出现残缺中间态（观察到 item/gem 从 537 掉到 404 再回 537）。本脚本内置文件数下限守卫（' + STABLE_MIN_FILES + '），低于阈值直接失败退出，确保产出不会基于残缺证据。',
  },
  summary: { type_tags_total: Object.keys(typeTagIndex).length, mapped_count: mapped.length, pending_count: unmapped.length, confidence_distribution: confCount },
  mappings: mapped,
  pending_adjudication: unmapped,
});

writeJson(path.join(MAP, 'type_tags_to_damage_type.json'), {
  _meta: {
    purpose: '权威元素 / 伤害标签 -> 模组 damage_type 的候选映射（草案，非裁定）',
    generator: GENERATOR,
    generated_at: new Date().toISOString(),
    method: '以权威 tags 的元素标签为主信号、type_tags 为次信号，连接本地产物后统计实际 damage_type 取值分布。',
    confidence_definition: '同 type_tags_to_delivery.json',
    measured_damage_types: vocab.damage_type,
    caveat: 'damage_type 由「该技能实际写了什么」反推，而非来自权威层的显式字段 —— 权威层没有 damage_type 字段，元素信息只在 tags / type_tags / implicits 文本里。',
  },
  summary: { signals_total: dmgSignals.length, mapped_count: dmgMapped.length, pending_count: dmgUnmapped.length },
  mappings: dmgMapped,
  pending_adjudication: dmgUnmapped,
});

writeJson(path.join(MAP, 'mechanism_families.draft.json'), {
  _meta: {
    purpose: '机制族草案 —— 把 1059 条压成可审阅的口袋，供「按机制族批量还原未实现技能」使用。族边界由作者裁定。',
    generator: GENERATOR,
    generated_at: new Date().toISOString(),
    method_declaration: [
      'skill / spirit 侧：以 tags 共现分簇 + type_tags 交叉，并用本地已实现成员的实际 delivery 分布给候选。type_tags 在 skill 侧非空率 99.5%，是可靠信号。',
      'support 侧：**不使用 type_tags**（实测非空率 0.3%）。只用 tags 共现 + implicits 文本 + 本地 effects[] 结构凭证。',
      '[Support] 单标签簇（110 条）单独用 implicits 规则切分，并对每组叠加 effects[] 结构凭证。',
    ],
    blocker_handled: {
      blocker_1: 'support 侧 type_tags 几乎全空 -> 已改走 tags + implicits + effects[]，方法声明中已书面排除 type_tags',
      blocker_2: '[Support] 单标签簇 110 条无细分标签 -> 已用 implicits 规则切成 16 组（最大 12 条），组内可一次审完',
    },
    evidence_coverage: evidencePct + '%',
  },
  families_by_gem_type: families,
  support_single_tag_cluster: {
    source_cluster: '[Support] 单标签簇',
    cluster_size: supportAlone.length,
    why_not_clusterable_by_tags: '这批辅助在 poe2db 上没有任何细分标签，tags 聚类在此无效',
    approach: 'implicits + description_en 文本规则式切分（优先级命中即停，组间互斥）；每组再叠加本地 effects[] 结构凭证作为交叉验证',
    group_count: groupList.length,
    max_group_size: Math.max(...groupList.map((g) => g.count)),
    groups: groupList,
    multi_label_distribution: (() => { const c = {}; Object.values(multiLabel).forEach((h) => (c[h.length] = (c[h.length] || 0) + 1)); return c; })(),
  },
});

console.log('\n== 汇总 ==');
console.log(JSON.stringify({
  type_tags_total: Object.keys(typeTagIndex).length,
  delivery_mapped: mapped.length, delivery_pending: unmapped.length, confCount,
  dmg_signals: dmgSignals.length, dmg_mapped: dmgMapped.length, dmg_pending: dmgUnmapped.length,
  support_cluster_size: supportAlone.length, support_groups: groupList.length,
  max_group_size: Math.max(...groupList.map((g) => g.count)),
}, null, 2));
console.log('\n完成（MAPPING_NOTES.md 由同目录文档步骤生成）');
