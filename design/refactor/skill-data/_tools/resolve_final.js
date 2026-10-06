/**
 * 以 poe2db 爬取资料为唯一权威，对 18 个本地 id 出最终裁定
 * 输出：crawled/name_resolution.json + name_resolution.md
 */
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data/crawled');
const E = JSON.parse(fs.readFileSync(path.join(OUT, 'all_skills.json'), 'utf8')).entries;

const N = s => String(s).toLowerCase().replace(/[^a-z0-9]/g, '');
const byId = new Map(E.map(e => [e.id, e]));
const find = kw => E.filter(e => new RegExp(kw, 'i').test(e.name_en));
const pick = re => E.filter(e => re.test(e.name_en)).map(e => ({
  id: e.id, name: e.name_en, zh: e.name_zh, type: e.gem_type, color: e.gem_color,
  tags: (e.tags || []).slice(0, 7).join('/'), desc: (e.description_en || '').slice(0, 90)
}));

// 判定表：本地 id -> { verdict, target, evidence }
const CASES = [
  { local: 'ice_shard', cat: 'skill', probe: /ice shard/i },
  { local: 'wolf_rake', cat: 'skill', probe: /(^|\s)rake$/i },
  { local: 'bear_maul', cat: 'skill', probe: /(^|\s)maul$/i },
  { local: 'wyvern_rend', cat: 'skill', probe: /(^|\s)rend$/i },
  { local: 'cast_on_crit', cat: 'support', probe: /cast on crit/i },
  { local: 'shockwave', cat: 'skill', probe: /shockwave/i },
  { local: 'fire_infusion', cat: 'support', probe: /infusion/i },
  { local: 'arbiter_ignition', cat: 'support', probe: /arbiter/i },
  { local: 'biting_exposure', cat: 'support', probe: /exposure/i },
  { local: 'conductive_exposure', cat: 'support', probe: /exposure/i },
  { local: 'scorching_exposure', cat: 'support', probe: /exposure|scorch/i },
  { local: 'martial_projectiles', cat: 'support', probe: /martial|projectile/i },
  { local: 'added_fire_damage', cat: 'support', probe: /added|fire/i },
  { local: 'added_cold_damage', cat: 'support', probe: /added|cold/i },
  { local: 'added_lightning_damage', cat: 'support', probe: /added|lightning/i },
  { local: 'added_chaos_damage', cat: 'support', probe: /added|chaos/i },
  { local: 'lightning_strike', cat: 'skill', probe: /lightning strike/i },
  { local: 'blood_pact', cat: 'spirit', probe: /pact/i },
  // 已补抓的无宝石技能页
  { local: 'blight', cat: 'skill', probe: /^blight$/i },
  { local: 'frost_nova', cat: 'skill', probe: /frost nova/i },
  { local: 'cyclone', cat: 'skill', probe: /^cyclone$/i },
  { local: 'lacerate', cat: 'skill', probe: /^lacerate$/i },
  { local: 'puncture', cat: 'skill', probe: /^puncture$/i },
  { local: 'ground_slam', cat: 'skill', probe: /ground slam/i },
  { local: 'perforate', cat: 'skill', probe: /perforate/i },
  { local: 'consume_corpse', cat: 'skill', probe: /consume corpse/i },
  { local: 'toxic_rain', cat: 'skill', probe: /toxic rain/i },
  { local: 'wrath', cat: 'spirit', probe: /^wrath$/i },
  { local: 'grace', cat: 'spirit', probe: /^grace$/i },
];

const rows = [];
for (const c of CASES) {
  const exact = byId.get(c.local);
  const cands = pick(c.probe).slice(0, 6);
  rows.push({ local: c.local, local_cat: c.cat, exact_hit: exact ? { id: exact.id, name: exact.name_en, zh: exact.name_zh } : null, candidates: cands });
}

let md = '# 本地技能 id vs poe2db 权威命名 —— 裁定表\n\n';
md += '> 判据：`design/refactor/skill-data/crawled/all_skills.json`（poe2db 爬取资料，唯一权威）。\n';
md += '> 原则：**名以资料库为准；PoE1 专有名称一律不认**。\n\n';
for (const r of rows) {
  md += '## `' + r.local + '`（本地分类 ' + r.local_cat + '）\n\n';
  if (r.exact_hit) {
    md += '- ✅ **权威数据中存在同名条目**：`' + r.exact_hit.id + '` / ' + r.exact_hit.name + ' / ' + (r.exact_hit.zh || '—') + '\n';
  } else {
    md += '- ❌ 权威数据中**无同名条目**\n';
  }
  if (r.candidates.length) {
    md += '\n| 候选 | 英文名 | 中文名 | 类型 | 色 | 标签 | 描述 |\n|---|---|---|---|---|---|---|\n';
    for (const c of r.candidates) {
      md += '| `' + c.id + '` | ' + c.name + ' | ' + (c.zh || '—') + ' | ' + c.type + ' | ' + (c.color || '—') + ' | ' + c.tags + ' | ' + c.desc.replace(/\|/g, '/') + ' |\n';
    }
  } else md += '\n（无候选）\n';
  md += '\n';
}
fs.writeFileSync(path.join(OUT, 'name_resolution.md'), md);
fs.writeFileSync(path.join(OUT, 'name_resolution.json'), JSON.stringify({ generated_at: new Date().toISOString(), rows }, null, 1));

// 控制台摘要
for (const r of rows) {
  console.log(r.local.padEnd(24) + (r.exact_hit ? '【同名存在】' : '【无同名】') +
    '  候选: ' + (r.candidates.map(c => c.id).join(', ') || '无'));
}
console.log('\n已写出 name_resolution.md / .json');
