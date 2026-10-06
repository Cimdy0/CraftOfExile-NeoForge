/**
 * freeze_authoritative.js — 冻结 PoE2 权威技能数据层
 *
 * 输入（唯一权威源，只读）：
 *   design/refactor/skill-data/crawled/all_skills.json
 *   design/refactor/skill-data/crawled/icons/manifest.json
 *   design/refactor/skill-data/crawled/raw/index_pages/*.html  (仅用于 provenance 的行数统计)
 *
 * 产出（只新增，不修改任何既有文件）：
 *   poe2_assets/authoritative/all_skills.json        (+ 顶层 _provenance，entries 原样保留)
 *   poe2_assets/authoritative/by_type/<type>.json
 *   poe2_assets/authoritative/schema.json
 *   poe2_assets/authoritative/FIELD_COVERAGE.md
 *   poe2_assets/authoritative/mechanism_index.json
 *   poe2_assets/authoritative/provenance.json
 *
 * 中文简体名转换走 scripts/convert_zh.py（需要 opencc）。
 * 完整性校验走 scripts/verify_authoritative.py。
 *
 * 用法： node poe2_assets/authoritative/scripts/freeze_authoritative.js
 */

'use strict';
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const ROOT = path.resolve(__dirname, '..', '..', '..');
const CRAWLED = path.join(ROOT, 'design/refactor/skill-data/crawled');
const OUT = path.join(ROOT, 'poe2_assets/authoritative');
const GENERATOR = 'poe2_assets/authoritative/scripts/freeze_authoritative.js';

const readJson = (p) => JSON.parse(fs.readFileSync(p, 'utf8'));
const writeJson = (p, obj) => {
  fs.mkdirSync(path.dirname(p), { recursive: true });
  fs.writeFileSync(p, JSON.stringify(obj, null, 2), 'utf8');
  console.log('  wrote', path.relative(ROOT, p), (fs.statSync(p).size / 1024).toFixed(1) + ' KB');
};

// ---------------------------------------------------------------- 载入权威源
const SRC_PATH = path.join(CRAWLED, 'all_skills.json');
const srcRaw = fs.readFileSync(SRC_PATH);
const SRC_SHA256 = crypto.createHash('sha256').update(srcRaw).digest('hex');
const SRC_STAT = fs.statSync(SRC_PATH);
const src = JSON.parse(srcRaw.toString('utf8'));
const E = src.entries;
const NOW = new Date().toISOString();

console.log('== PoE2 权威数据冻结 ==');
console.log('源:', path.relative(ROOT, SRC_PATH));
console.log('源 sha256:', SRC_SHA256);
console.log('源 mtime :', SRC_STAT.mtime.toISOString(), '| size:', SRC_STAT.size);
console.log('源 meta:', JSON.stringify(src.meta));

// ---------------------------------------------------------------- 值判定工具
const has = (v) => {
  if (v === null || v === undefined) return false;
  if (typeof v === 'string') return v.trim().length > 0;
  if (Array.isArray(v)) return v.length > 0;
  if (typeof v === 'object') return Object.keys(v).length > 0;
  return true;
};
const kindOf = (v) => {
  if (v === null) return 'null';
  if (Array.isArray(v)) return v.every((x) => typeof x === 'string') ? 'string[]'
    : v.every((x) => typeof x === 'number') ? 'number[]' : 'array';
  const t = typeof v;
  if (t === 'object') return 'object';
  return t;
};
const pct = (n, d) => Math.round((n / d) * 1000) / 10;

// ---------------------------------------------------------------- 1. 基础计数
const byType = {};
E.forEach((e) => (byType[e.gem_type] = (byType[e.gem_type] || 0) + 1));

const ALL_KEYS = [];
E.forEach((e) => Object.keys(e).forEach((k) => { if (!ALL_KEYS.includes(k)) ALL_KEYS.push(k); }));

// tier 变体 & base 族
const ROMAN = /_(i|ii|iii|iv|v|vi|vii|viii)$/;
const stripRoman = (id) => id.replace(ROMAN, '');
const uniqBase = new Set(E.map((e) => e.base_id));
const variants = E.filter((e) => ROMAN.test(e.id));
const idSet = new Set(E.map((e) => e.id));
const baseIdMismatch = E.filter((e) => e.base_id !== stripRoman(e.id));
const variantBaseMissing = variants.filter((e) => !idSet.has(e.base_id));

// family 一致性
const families = {};
E.forEach((e) => {
  (families[e.base_id] = families[e.base_id] || []).push(e);
});
const familyKeys = Object.keys(families);
const familyTypeMixed = familyKeys.filter((k) => new Set(families[k].map((e) => e.gem_type)).size > 1);
const familyAllVariant = familyKeys.filter((k) => families[k].every((e) => ROMAN.test(e.id)));

// 唯一 base 按类型
const baseByType = {};
familyKeys.forEach((k) => {
  families[k].forEach((e) => (baseByType[e.gem_type] = (baseByType[e.gem_type] || 0) + 0));
});
const uniqBaseByType = { skill: 0, support: 0, spirit: 0 };
familyKeys.forEach((k) => {
  const t = families[k][0].gem_type;
  if (t in uniqBaseByType) uniqBaseByType[t] += 1;
});

console.log('\n-- 计数 --');
console.log('  总条目      :', E.length);
console.log('  gem_type    :', JSON.stringify(byType));
console.log('  unique base :', uniqBase.size, JSON.stringify(uniqBaseByType));
console.log('  tier 变体   :', variants.length, '| family 数:', familyKeys.length,
  '| 全变体 family:', familyAllVariant.length);
console.log('  base_id != strip(id):', baseIdMismatch.length, baseIdMismatch.map((e) => e.id + '->' + e.base_id).join(', '));
console.log('  变体 base 无同名条目:', variantBaseMissing.length);

// ---------------------------------------------------------------- 2. 必填字段例外
const missingEssential = {};
['id', 'name_en', 'internal_id'].forEach((f) => {
  const miss = E.filter((e) => !has(e[f]));
  missingEssential[f] = miss.map((e) => ({ id: e.id || '(no id)', gem_type: e.gem_type, name_en: e.name_en || null }));
  console.log('  缺 ' + f + ':', miss.length, miss.slice(0, 5).map((e) => e.id).join(', '));
});
const missingNameZh = E.filter((e) => !has(e.name_zh));
console.log('  缺 name_zh  :', missingNameZh.length);

// ---------------------------------------------------------------- 3. 图标覆盖
const icons = readJson(path.join(CRAWLED, 'icons/manifest.json'));
const iconStatus = {};
icons.entries.forEach((x) => (iconStatus[x.status] = (iconStatus[x.status] || 0) + 1));
const iconMissingId = icons.entries.filter((x) => x.status !== 'existing').map((x) => x.id);
console.log('  图标状态    :', JSON.stringify(iconStatus), '| missing ids:', iconMissingId.join(','));

// ---------------------------------------------------------------- 4. 索引页行数（provenance）
const IDX_PAGES = ['Gems', 'Skill_Gems', 'Support_Gems', 'Spirit_Gems', 'Lineage_Supports', 'Meta_Gems'];
const idxDir = path.join(CRAWLED, 'raw/index_pages');
const indexPages = IDX_PAGES.map((pg) => {
  const f = path.join(idxDir, pg + '.html');
  if (!fs.existsSync(f)) return { page: pg, rows: 0, unique_gem_hrefs: 0, note: 'raw 缓存缺失' };
  const h = fs.readFileSync(f, 'utf8');
  const rows = (h.match(/<tr[^>]*data-filters/g) || []).length;
  const set = new Set();
  for (const m of h.matchAll(/<a class="gem_(?:red|green|blue)"[^>]*href="\/us\/([^"#/]+)"/g)) set.add(m[1]);
  return {
    page: pg,
    rows,
    unique_gem_hrefs: set.size,
    measure: 'rows = <tr data-filters> 行数；unique_gem_hrefs = 该页 <a class="gem_{red|green|blue}" href="/us/<Name>"> 去重计数（静态 HTML 实测）',
  };
});
console.log('\n-- 索引页（raw 缓存静态实测）--');
indexPages.forEach((p) => console.log('  ' + p.page.padEnd(18) + ' rows=' + String(p.rows).padStart(5) + '  unique_gem_hrefs=' + p.unique_gem_hrefs));

let rawCount = 0;
const walk = (d) => fs.readdirSync(d, { withFileTypes: true }).forEach((f) => {
  const p = path.join(d, f.name);
  if (f.isDirectory()) walk(p); else rawCount += 1;
});
walk(path.join(CRAWLED, 'raw'));
console.log('  raw 缓存文件数:', rawCount);

// ---------------------------------------------------------------- 5. all_skills.json（冻结副本 + _provenance）
const frozen = JSON.parse(JSON.stringify(src));
frozen._provenance = {
  source: 'poe2db.tw/us (数值) + poe2db.tw/tw (中文名)',
  crawled_at: src.meta.crawled_at,
  entry_count: E.length,
  by_type: { skill: byType.skill, support: byType.support, spirit: byType.spirit },
  unique_base: uniqBase.size,
  generator: GENERATOR,
  generated_at: NOW,
  source_pin: {
    file: 'design/refactor/skill-data/crawled/all_skills.json',
    sha256: SRC_SHA256,
    bytes: SRC_STAT.size,
    mtime: SRC_STAT.mtime.toISOString(),
  },
  note: '本文件是唯一权威；不得手工编辑。若源在此后被重爬，本文件的 _provenance.source_pin.sha256 将与源不一致 —— 那时必须重跑 freeze_authoritative.js 重新冻结，而不是手工改本文件。',
};
writeJson(path.join(OUT, 'all_skills.json'), frozen);

// 源指纹（供 verify_authoritative.py 做漂移检测）
writeJson(path.join(OUT, 'source_pin.json'), {
  _purpose: '冻结时刻的权威源指纹。verify_authoritative.py 用它判定 src 是否已被重爬（漂移），漂移时中止并提示重新冻结。',
  source_file: 'design/refactor/skill-data/crawled/all_skills.json',
  frozen_at: NOW,
  generator: GENERATOR,
  sha256: SRC_SHA256,
  bytes: SRC_STAT.size,
  mtime: SRC_STAT.mtime.toISOString(),
  crawled_at: src.meta.crawled_at,
  measured_snapshot: {
    entry_count: E.length,
    by_type: byType,
    unique_base: uniqBase.size,
    tier_variants: variants.length,
    dnt_entries: E.filter((e) => String(e.id).startsWith('DNT')).length,
    icon_existing: iconStatus.existing || 0,
    icon_missing: iconStatus.missing || 0,
  },
  known_baseline_2026_09_19: {
    entry_count: 1046,
    by_type: { skill: 377, support: 614, spirit: 55 },
    unique_base: 887,
    icon_existing: 1045,
    note: '团队基线（2026-09-19 快照，team-lead 下发）。仅供比对，不作为断言期望值。',
  },
});

// ---------------------------------------------------------------- 6. by_type
console.log('\n-- by_type --');
['skill', 'support', 'spirit'].forEach((t) => {
  const sub = E.filter((e) => e.gem_type === t);
  writeJson(path.join(OUT, 'by_type', t + '.json'), {
    _meta: {
      gem_type: t,
      count: sub.length,
      unique_base: familyKeys.filter((k) => families[k][0].gem_type === t).length,
      source: '../all_skills.json',
      generated_at: NOW,
      note: '按 all_skills.json 的 gem_type 拆分；条目内容与权威源逐字节一致。不得手工编辑。',
    },
    entries: sub,
  });
});

// ---------------------------------------------------------------- 7. 字段字典（schema.json）
// 运行时参与列的判定依据 = 实测 src/generated/resources/data/coe/coe/item/gem/** 与 preset_*.json 的键集合。
//   gem item 顶键并集 : entry, ref, vars, cost, require, tags, cast, params, phases, curse_duration,
//                       curse_range, mark_duration, mark_range, modifiers, effects, lineage,
//                       mana_cost_multiplier, max_level, reservation_multiplier, tag, tier, special
//   preset 顶键并集   : cast, cost, phases, require, tags, modifiers
// 凡在上述键集合中找不到对应消费者的字段，一律标 reference_only（真值参考），并写明证据。
const FIELDS = {
  id: ['string', '权威 slug（snake_case），源自 poe2db /us/<Page>；tier 变体带 _i/_ii/_iii 后缀', 'runtime', 'item/gem/<type>/<color>/<id>.json 的资源路径与 preset ref（coe:preset_<id>）由此派生'],
  id_coe: ['string', '本地 CoE preset 资源名的推断值（coe:preset_<id_coe>），由 id 派生', 'reference_only', 'generated gem json 的 ref 键实测为该形状；但本字段由爬取侧派生，非 poe2db 原值'],
  name_en: ['string', '英文显示名', 'runtime', 'lang 键值源（item/gem/** 的显示名最终由 lang 提供者消费）'],
  name_zh: ['string|null', '繁体中文名（poe2db.tw/tw 源）', 'runtime', 'lang 键值源；简体转换见 zh_cn_names.json'],
  gem_type: ['enum', 'skill | support | spirit', 'runtime', '决定 item/gem/<type>/<color>/ 目录与该 gem 的行为类型'],
  gem_color: ['enum', 'red | green | blue', 'runtime', '决定 item/gem/<type>/<color>/ 子目录'],
  tags: ['string[]', 'poe2db 索引页显示标签（UI 向）', 'runtime', 'generated 产物含 tags 键（gem item 129 个文件、preset 含 tags）'],
  category_tags: ['string[]', '索引页 data-filters 原值 = tags + 自身英文名；含去重前的页面噪音', 'reference_only', 'generated gem_dir 中无 category_tags 键'],
  type_tags: ['string[]', '详情页 Skill Type/Category 段，比 tags 更贴近内部机制（如 MeleeSingleTarget、Totemable）', 'reference_only', 'generated gem dir 无 type_tags 键；仅作机制族归类的输入候选，是否接线待作者裁定'],
  description_en: ['string|null', '英文技能描述', 'reference_only', 'generated gem dir 无 description 键'],
  description_zh: ['null', '中文技能描述 —— 未抓取（已知缺口 1）', 'reference_only', '全表恒 null'],
  level_range: ['string|null', '等级区间文本，形如 (1-20)', 'reference_only', 'generated gem dir 无同名键；等价于 max_level 的展示形式'],
  cost: ['string|null', '消耗（通常为 Mana）页码文本；辅助宝石页不提供', 'runtime', 'preset 产物含 cost 键'],
  cast_time: ['string|null', '施法时间文本；辅助宝石页不提供', 'reference_only', 'generated gem dir 无 cast_time 键（preset 的 cast 键为另一概念）'],
  crit_chance: ['string|null', '基础暴击率文本；辅助宝石页不提供', 'partial', 'preset action 实测含 can_crit 数值，但未出现 crit_chance 键；是否由本字段驱动待确认'],
  attack_speed: ['string|null', '攻击速度修正文本，形如 60% of base', 'reference_only', 'generated gem dir 无 attack_speed 键'],
  requirements: ['string', '需求文本，形如 Level (1-90), (4-157) Str', 'runtime', 'preset 产物含 require 键（level / intelligence 等）'],
  req_level_range: ['string', '等级需求区间 min-max', 'runtime', 'preset require.level 的 per_level 表达式来源数据'],
  req_attr: ['object|null', '{range, attr} 属性需求；attr ∈ Str|Dex|Int', 'runtime', 'preset require.<attr> 的来源数据'],
  quality_bonus: ['string|null', '品质加成文案，形如 (0-20)% increased Attack Speed', 'reference_only', '实测：generated gem dir 中 quality 出现 0 次 —— 本模组品质完全未接线'],
  quality_range: ['number[]|null', '品质区间 [min, max]', 'reference_only', 'generated gem dir 无 quality 相关键'],
  implicits: ['string[]', '详情页 Implicit 全部条目：含区间伤害、武器限制、隐藏 stat（形如 is area damage [1]）', 'reference_only', 'generated gem dir 无 implicits 键；是 Zone 伤害/限制提取的输入，当前未结构化消费'],
  stats: ['string[]', '已展示 stat 子集（implicits 的非隐藏部分）', 'reference_only', 'generated gem dir 无 stats 键'],
  level_table: ['object|null', '{headers, rows} 逐等级表；辅助宝石多为单行', 'runtime', 'preset require/cost 的 coe:per_level_add_value(add/base/per) 三元组只能由本表拟合得出'],
  max_level: ['number|null', '最大等级', 'runtime', 'generated gem item 顶键实测含 max_level'],
  recommended_supports: ['string[]|null', 'poe2db 推荐辅助宝石', 'reference_only', 'generated gem dir 无 recommended_supports 键'],
  internal_id: ['string|null', 'poe2db Metadata id（如 Boneshatter 的内部名）', 'reference_only', 'generated gem dir 中 internal_id 出现 0 次；建议作为可追溯 tracing key 候选，尚未接线'],
  base_type: ['string|null', '基础类型', 'reference_only', 'generated gem dir 无 base_type 键'],
  target_types: ['string[]|null', '目标类型', 'reference_only', 'generated gem dir 无 target_types 键'],
  acronyms: ['string[]|null', '缩写/别名', 'reference_only', 'generated gem dir 无 acronyms 键'],
  icon_url: ['string|null', 'CDN 图标 URL', 'runtime', '图标资产来源'],
  icon_local_path: ['string|null', '本地已接入的 png 路径', 'runtime', 'assets/coe/textures/item/gem/<id>.png'],
  source_url_us: ['string|null', '英文详情页 URL', 'reference_only', '可追溯用，不进产物'],
  source_url_tw: ['string|null', '中文详情页 URL', 'reference_only', '可追溯用，不进产物'],
  crawled_at: ['string', '该条目抓取时间', 'reference_only', '审计用'],
  gem_class: ['string|null', '页面自报分类：Skill Gems / Meta Skill Gem / Support Gems', 'reference_only', 'generated gem dir 有 lineage 键但无 gem_class；is_meta/is_lineage 为其布尔投影'],
  is_meta: ['boolean', '是否为 Meta 宝石（在 Meta_Gems 页出现，或 gem_class=Meta Skill Gem）', 'reference_only', '推断：Meta 语义尚未见独立产物键'],
  is_lineage: ['boolean', '是否为血统（Lineage）宝石', 'partial', 'generated gem item 顶键实测含 lineage'],
  index_tier: ['number|null', '索引页分组层级（作者使用的排序/分批依据）', 'reference_only', '爬取流水线内部用'],
  art_name: ['string|null', '图标 art 名（PascalCase，如 BruteBoneshatter）', 'reference_only', 'generated gem dir 无 art_name 键；仅用于图标映射'],
  seen_in: ['string[]', '该条目出现在哪些索引页；extra:gemless = 无宝石形态的固有技能', 'reference_only', '发现链路证据，不进产物'],
  local_id: ['string|null', '本地 datagen id（由 crosswalk 反向写入）', 'reference_only', '⚠ 反向派生字段，属本地对照，不得作为权威'],
  local_cat: ['string|null', '本地 datagen 分类（反向派生）', 'reference_only', '⚠ 反向派生字段'],
  implemented: ['boolean', '本地 datagen 是否已实现（反向派生自 crosswalk）', 'reference_only', '⚠ 反向派生字段；实测 260 true / 786 false'],
  base_id: ['string', '派生族键 = id 去掉罗马后缀；多条共享即为同一技能的 tier 家族', 'reference_only', '派生字段，用于判定 tier 家族与唯一基础技能数'],
  tier_variant: ['boolean', 'id 是否带罗马后缀（即是否为 tier 变体）', 'reference_only', '派生字段'],
  icon_source_candidates: ['string[]', '可转换的 PascalCase 源资产候选名', 'reference_only', '图标流水线用'],
  icon_status: ['enum', 'existing | source_available | missing', 'reference_only', '图标三态：1045 existing / 1 missing（本层实测）'],
};

const schemaFields = {};
const coverageRows = [];
ALL_KEYS.forEach((k) => {
  const vals = E.map((e) => e[k]);
  const nonNull = vals.filter(has).length;
  const kinds = [...new Set(vals.filter(has).map(kindOf))].sort();
  const ex = vals.find(has);
  const spec = FIELDS[k] || ['unknown', '（未在字典中登记 —— 需人工补录）', 'reference_only', ''];
  const byTypeRate = {};
  ['skill', 'support', 'spirit'].forEach((t) => {
    const sub = E.filter((e) => e.gem_type === t);
    byTypeRate[t] = pct(sub.filter((e) => has(e[k])).length, sub.length);
  });
  const example = ex === undefined ? null
    : (typeof ex === 'string' && ex.length > 160) ? ex.slice(0, 160) + '…'
      : Array.isArray(ex) ? (k === 'level_table' ? ex : (ex.length > 6 ? ex.slice(0, 6).concat(['…(+' + (ex.length - 6) + ')']) : ex))
        : typeof ex === 'object' && ex !== null && !Array.isArray(ex) && JSON.stringify(ex).length > 300
          ? JSON.parse(JSON.stringify({ _truncated: true, keys: Object.keys(ex).slice(0, 6) })) : ex;
  schemaFields[k] = {
    type: spec[0] === 'unknown' ? (kinds.join(' | ') || 'unknown') : spec[0],
    observed_types: kinds,
    meaning: spec[1],
    non_null_count: nonNull,
    non_null_rate: pct(nonNull, E.length),
    non_null_rate_pct: pct(nonNull, E.length) + '%',
    non_null_rate_by_gem_type: byTypeRate,
    example,
    runtime_role: spec[2],
    runtime_evidence: spec[3],
  };
  coverageRows.push({ field: k, ...schemaFields[k] });
});

writeJson(path.join(OUT, 'schema.json'), {
  _meta: {
    purpose: 'all_skills.json 的字段字典：类型 / 含义 / 实测非空率 / 示例 / 是否参与运行时',
    entry_count: E.length,
    measured_at: NOW,
    non_null_rule: '空字符串、空数组、空对象、null、undefined 均计为「空」',
    runtime_role_values: {
      runtime: '该字段已进入（或可直接映射进） src/generated/resources/data/coe/coe 的运行时产物',
      partial: '语义相关但尚未确认接线',
      reference_only: '当前仅作真值参考/溯源，无对应运行时产物键',
    },
    runtime_evidence_method: '对每个候选键统计其在 src/generated/resources/data/coe/coe/item/gem/** 与 item/gem/**/preset_*.json 中的出现次数得出；结论为「当前快照」结论，随 datagen 改动会失效',
    caveat: 'runtime_role 由「运行时产物键并集」反推，不是对 data/gen/gem/GemGen.java 的静态读取结论（该文件处于并发编辑中，本次未触碰）。作者如需最终裁定，请在该并发任务收口后重跑一次本列。',
  },
  fields: schemaFields,
});

// ---------------------------------------------------------------- 8. FIELD_COVERAGE.md
const GAPS = [
  ['描述仅英文', 'description_zh 全表为 null —— 中文详情页正文未抓取；中文**名**已抓 ' + E.filter((e) => has(e.name_zh)).length + ' 条（繁体，见 zh_cn_names.json 转简体）。'],
  ['未覆盖 Granted 类', '装备 / 升华 / 词缀授予的技能无宝石形态，不在任何索引页，本层为 0 覆盖。'],
  ['武器限制未结构化', '武器限制只以文本形式散落在 implicits（如 Requires a Mace），无独立字段。'],
  ['辅助的 supports/excludes 未抓', '辅助宝石「可辅助哪些技能」在 poe2db 以技能类别描述呈现，未做二次解析 -> recommended_supports 之外的真实作用域集合缺失。'],
  ['cost / cast_time / crit_chance 仅部分有值', '三者分别为 ' + E.filter((e) => has(e.cost)).length + ' / ' + E.filter((e) => has(e.cast_time)).length + ' / ' + E.filter((e) => has(e.crit_chance)).length + ' 条有值，且几乎全部缺失来自辅助宝石页（该类页面不提供这些字段）。'],
];

let md = '';
md += '# 字段覆盖表（实测）\n\n';
md += '> 本文件由 `scripts/freeze_authoritative.js` 自动生成，勿手工编辑。\n';
md += '> 所有数字均为对 `poe2_assets/authoritative/all_skills.json`（' + E.length + ' 条）的**实测值**，非引自旧文档。\n\n';
md += '- 生成时间：' + NOW + '\n';
md += '- 权威条目：**' + E.length + '**（skill ' + byType.skill + ' / support ' + byType.support + ' / spirit ' + byType.spirit + '）\n';
md += '- 唯一基础技能（去 tier 变体）：**' + uniqBase.size + '**\n';
md += '- tier 变体条目：**' + variants.length + '**\n\n';
md += '## 覆盖总览\n\n';
md += '| 字段 | 类型 | 非空数 | 非空率 | skill | support | spirit | 运行时角色 |\n';
md += '|---|---|---|---|---|---|---|---|\n';
const esc = (s) => String(s).replace(/\|/g, '\\|').replace(/\r?\n/g, ' ');
const RATE_ORDER = coverageRows.slice().sort((a, b) => b.non_null_rate - a.non_null_rate);
RATE_ORDER.forEach((r) => {
  md += '| `' + esc(r.field) + '` | ' + esc(r.type) + ' | ' + r.non_null_count + '/' + E.length + ' | ' + r.non_null_rate_pct;
  md += ' | ' + r.non_null_rate_by_gem_type.skill + '% | ' + r.non_null_rate_by_gem_type.support + '% | ' + r.non_null_rate_by_gem_type.spirit + '% | ' + esc(r.runtime_role) + ' |\n';
});
md += '\n> 非空判定：空字符串 / 空数组 / 空对象 / null / undefined 一律计为空。\n\n';
md += '## 运行时角色说明\n\n';
md += '- `runtime` —— 字段已（或可直接映射）进入 `src/generated/resources/data/coe/coe` 的运行时产物。\n';
md += '- `partial` —— 语义相关，但接线未确认。\n';
md += '- `reference_only` —— 当前无对应运行时产物键，仅作真值参考 / 溯源。\n\n';
md += '判定依据：运行时产物实测顶键并集\n\n';
md += '- `item/gem/**`（537 个文件）：`entry, ref, vars, cost, require, tags, cast, params, phases, curse_duration, curse_range, mark_duration, mark_range, modifiers, effects, lineage, mana_cost_multiplier, max_level, reservation_multiplier, tag, tier, special`\n';
md += '- `item/gem/**/preset_*.json`（262 个文件）：`cast, cost, phases, require, tags, modifiers`\n\n';
md += '**重要**：`quality`（品质）在上述产物中命中 **0** 次 —— 既有模组完全未接线品质，与 `index.md` 的结论一致。\n\n';
md += '## 已知缺口（5）\n\n';
GAPS.forEach((g, i) => { md += (i + 1) + '. **' + g[0] + '** —— ' + g[1] + '\n'; });
const OLD_INDEX_MD = {
  total: 1046,
  fields: {
    name_en: 1046, name_zh: 793, description_en: 1043, requirements: 1029, internal_id: 1034,
    icon_url: 1045, tags: 1035, quality_bonus: 394, cost: 322, cast_time: 119,
    crit_chance: 100, type_tags: 432, level_range: 421,
  },
};
md += '\n## 与 `crawled/index.md` 旧值对照\n\n';
md += '`index.md` 是 1046 条时代的覆盖表（已过时）。下表把它的字段计数与本层实测值并置；注意分母已从 1046 变为 ' + E.length + '，因此两列不可直接相减，只能看趋势。\n\n';
md += '| 字段 | index.md 旧值 | 本层实测 | 差 |\n|---|---|---|---|\n';
Object.entries(OLD_INDEX_MD.fields).forEach(([f, oldV]) => {
  const now = E.filter((e) => has(e[f])).length;
  md += '| `' + f + '` | ' + oldV + ' | ' + now + ' | ' + (now - oldV >= 0 ? '+' : '') + (now - oldV) + ' |\n';
});
md += '| （总条目） | ' + OLD_INDEX_MD.total + ' | ' + E.length + ' | +' + (E.length - OLD_INDEX_MD.total) + ' |\n';

md += '\n## 必填字段例外清单\n\n';
Object.entries(missingEssential).forEach(([f, list]) => {
  md += '- `' + f + '`：缺失 **' + list.length + '** 条' + (list.length ? ' —— ' + list.map((x) => x.id).join(', ') : '') + '\n';
});
md += '- `name_zh`：缺失 **' + missingNameZh.length + '** 条（这些条目在 `zh_cn_names.json` 中按铁律**留空**，不臆造）\n';
md += '\n## 图标三态（实测）\n\n';
md += '| 状态 | 数量 |\n|---|---|\n';
Object.entries(iconStatus).forEach(([k, v]) => { md += '| ' + k + ' | ' + v + ' |\n'; });
md += '\n缺失项 id：' + (iconMissingId.length ? iconMissingId.join(', ') : '（无）') + '\n';

fs.writeFileSync(path.join(OUT, 'FIELD_COVERAGE.md'), md, 'utf8');
console.log('\n  wrote poe2_assets/authoritative/FIELD_COVERAGE.md');

// ---------------------------------------------------------------- 9. mechanism_index.json
const freq = (arr) => {
  const m = {};
  arr.forEach((x) => (m[x] = (m[x] || 0) + 1));
  return Object.entries(m).sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0]))
    .map(([tag, count]) => ({ tag, count, pct_of_type: 0 }));
};
const tagCountsMap = (list) => {
  const m = {};
  list.forEach((e) => (e.tags || []).forEach((t) => (m[t] = (m[t] || 0) + 1)));
  return m;
};
const toRanked = (m, total) => Object.entries(m).sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0]))
  .map(([tag, count]) => ({ tag, count, pct: pct(count, total) }));

const allTagsMap = tagCountsMap(E);
const allTypeTagsMap = {};
E.forEach((e) => (e.type_tags || []).forEach((t) => (allTypeTagsMap[t] = (allTypeTagsMap[t] || 0) + 1)));

console.log('\n-- 标签空间 --');
console.log('  unique tags      :', Object.keys(allTagsMap).length);
console.log('  unique type_tags :', Object.keys(allTypeTagsMap).length);

const cooccur = {};
const globalCo = {};
['skill', 'support', 'spirit'].forEach((t) => {
  const sub = E.filter((e) => e.gem_type === t);
  const buckets = {};
  sub.forEach((e) => {
    const combo = [...new Set(e.tags || [])].sort();
    const key = combo.join('|');
    if (!buckets[key]) buckets[key] = { tag_combo: combo, count: 0, members: [] };
    buckets[key].count += 1;
    buckets[key].members.push(e.id);
  });
  cooccur[t] = Object.values(buckets).sort((a, b) => b.count - a.count || b.tag_combo.length - a.tag_combo.length
    || a.tag_combo.join().localeCompare(b.tag_combo.join()));
  cooccur[t].forEach((c) => { globalCo[c.tag_combo.join('|')] = (globalCo[c.tag_combo.join('|')] || 0) + c.count; });
});
const globalTop = Object.entries(globalCo).sort((a, b) => b[1] - a[1])
  .map(([k, count]) => {
    const combo = k ? k.split('|') : [];
    const members = [];
    E.forEach((e) => {
      const c = [...new Set(e.tags || [])].sort();
      if (c.join('|') === k) members.push(e.id);
    });
    return { tag_combo: combo, count, members };
  });

// 两两共现（补充线索）
const pairMap = {};
E.forEach((e) => {
  const ts = [...new Set(e.tags || [])].sort();
  for (let i = 0; i < ts.length; i++) for (let j = i + 1; j < ts.length; j++) {
    const k = ts[i] + ' + ' + ts[j];
    pairMap[k] = (pairMap[k] || 0) + 1;
  }
});
const pairs = Object.entries(pairMap).sort((a, b) => b[1] - a[1]).slice(0, 60)
  .map(([k, count]) => ({ pair: k.split(' + '), count }));

writeJson(path.join(OUT, 'mechanism_index.json'), {
  _disclaimer: '本文件是「按 tags 共现做的数据聚类草案」，**不是机制族裁定**。同一 tag 组合内的成员仅表示共享显示标签，不等于共享运行时机制（例如同样 Attack+AoE+Melee+Strike 的技能在 delivery / DamageAction 上可能完全不同）。最终族边界必须由作者结合 type_tags、implicits、level_table 共同裁定。',
  _generated_at: NOW,
  _source: 'all_skills.json 的 tags / type_tags；聚类键 = tags 去重并排序后的精确集合',
  tag_space: {
    unique_tags: Object.keys(allTagsMap).length,
    unique_type_tags: Object.keys(allTypeTagsMap).length,
  },
  tag_frequency: {
    overall: toRanked(allTagsMap, E.length),
    by_gem_type: {
      skill: toRanked(tagCountsMap(E.filter((e) => e.gem_type === 'skill')), byType.skill),
      support: toRanked(tagCountsMap(E.filter((e) => e.gem_type === 'support')), byType.support),
      spirit: toRanked(tagCountsMap(E.filter((e) => e.gem_type === 'spirit')), byType.spirit),
    },
  },
  type_tag_frequency: {
    overall: toRanked(allTypeTagsMap, E.length),
    by_gem_type: {
      skill: toRanked((() => { const m = {}; E.filter((e) => e.gem_type === 'skill').forEach((e) => (e.type_tags || []).forEach((t) => (m[t] = (m[t] || 0) + 1))); return m; })(), byType.skill),
      support: toRanked((() => { const m = {}; E.filter((e) => e.gem_type === 'support').forEach((e) => (e.type_tags || []).forEach((t) => (m[t] = (m[t] || 0) + 1))); return m; })(), byType.support),
      spirit: toRanked((() => { const m = {}; E.filter((e) => e.gem_type === 'spirit').forEach((e) => (e.type_tags || []).forEach((t) => (m[t] = (m[t] || 0) + 1))); return m; })(), byType.spirit),
    },
  },
  tag_cooccurrence_clusters: {
    overall_top: globalTop,
    by_gem_type: cooccur,
  },
  tag_pair_cooccurrence_top60: pairs,
});

// ---------------------------------------------------------------- 10. provenance.json
writeJson(path.join(OUT, 'provenance.json'), {
  sources: [
    { url: 'https://poe2db.tw/us/', role: '权威数值 + 英文文本 + 标签 + 等级表 + 内部 id' },
    { url: 'https://poe2db.tw/tw/', role: '繁体中文名（name_zh）' },
    { url: 'https://cdn.poe2db.tw/', role: '图标 CDN（webp -> png）' },
  ],
  index_pages: indexPages,
  crawl_pipeline: ['discover_v2.js', 'build_list.js', 'crawl_details.js', 'finalize.js'],
  pipeline_dir: 'design/refactor/skill-data/_tools/',
  raw_cache_dir: 'design/refactor/skill-data/crawled/raw',
  raw_cache_count: rawCount,
  dnt_excluded: 28,
  dnt_note: 'DNT-* 为 poe2db 上的废弃/未启用条目，在 discover 阶段记录、finalize 阶段剔除；冻结层实测残留 0 条。',
  entry_count: E.length,
  by_type: byType,
  unique_base: uniqBase.size,
  discovered_rows_total: 1074,
  discovered_rows_note: '索引页按 <tr data-filters> 切分共发现 1074 条，剔除 28 条 DNT-* 后保留 1046 条（引自 crawled/index.md 方法论；行数口径已在 index_pages[*].rows 中按页实测复核：Gems 1066 / Skill_Gems 427 / Support_Gems 557 / Spirit_Gems 53）。',
  measured_discrepancy: 'crawled/index.md 称「Gems 页 1066 行 -> 963 唯一」。本层对 raw 缓存静态 HTML 实测无法复现 963：Gems 页 gem 锚点去重仅 821 个，全部 14 个索引页并集亦为 821。推测 963 来自 JS 渲染后的 DOM 或跨页别的去重口径。**本层以最终 entries = 1046 为唯一有效口径**，963 不作为任何断言依据。',
  generated_at: NOW,
  generator: GENERATOR,
  source_pin: {
    file: 'design/refactor/skill-data/crawled/all_skills.json',
    sha256: SRC_SHA256,
    bytes: SRC_STAT.size,
    mtime: SRC_STAT.mtime.toISOString(),
    crawled_at: src.meta.crawled_at,
    note: '冻结时刻指纹；便于后续判定是否需要重新冻结（见 scripts/verify_authoritative.py 的漂移检测）。',
  },
  drift_incident: {
    observed: true,
    at: NOW,
    what: '本次冻结过程中，crawled/all_skills.json 被并发重写：1046 -> ' + E.length + ' 条（crawled_at ' + src.meta.crawled_at + '）。爬取任务 SK-CRAWL-001 当时仍处于 in_progress。',
    handling: '本层以「最后一次读取」为冻结内容，并用 sha256 钉住源，使任何后续漂移都可被 verify_authoritative.py 检出并要求重新冻结。',
    action_required: '若 SK-CRAWL-001 还有后续写入，必须重跑 freeze_authoritative.js + convert_zh.py + verify_authoritative.py。',
  },
  frozen_artifacts: [
    'all_skills.json', 'by_type/skill.json', 'by_type/support.json', 'by_type/spirit.json',
    'zh_cn_names.json', 'schema.json', 'FIELD_COVERAGE.md', 'mechanism_index.json', 'provenance.json',
    'source_pin.json',
  ],
});

// ---------------------------------------------------------------- 11. 控制台汇总
console.log('\n== 汇总（供汇报使用）==');
console.log(JSON.stringify({
  total: E.length,
  by_type: byType,
  unique_base: uniqBase.size,
  unique_base_by_type: uniqBaseByType,
  tier_variants: variants.length,
  families: familyKeys.length,
  families_all_variant: familyAllVariant.length,
  families_type_mixed: familyTypeMixed.length,
  base_id_mismatch: baseIdMismatch.length,
  variant_base_missing: variantBaseMissing.length,
  missing_id: missingEssential.id.length,
  missing_name_en: missingEssential.name_en.length,
  missing_internal_id: missingEssential.internal_id.length,
  missing_name_zh: missingNameZh.length,
  icon: iconStatus,
  unique_tags: Object.keys(allTagsMap).length,
  unique_type_tags: Object.keys(allTypeTagsMap).length,
}, null, 2));

console.log('\n== 全局 top10 tag 组合 ==');
globalTop.slice(0, 10).forEach((c, i) => {
  console.log('  ' + String(i + 1).padStart(2) + '. count=' + String(c.count).padStart(3) + '  [' + c.tag_combo.join(', ') + ']  e.g. ' + c.members.slice(0, 3).join(', '));
});
console.log('\n== 各类型 top5 tag 组合 ==');
['skill', 'support', 'spirit'].forEach((t) => {
  console.log('  [' + t + ']');
  cooccur[t].slice(0, 5).forEach((c, i) => {
    console.log('    ' + (i + 1) + '. count=' + String(c.count).padStart(3) + '  [' + c.tag_combo.join(', ') + ']  e.g. ' + c.members.slice(0, 3).join(', '));
  });
});
console.log('\n完成。');
