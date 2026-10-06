/**
 * 汇总：覆盖率三态对照 + 别名归一 + 图标清点 + 生成 index.md / 各类别 .md
 */
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data/crawled');
const GEMDIR = path.join(ROOT, 'src/generated/resources/data/coe/coe/item/gem');

const all = JSON.parse(fs.readFileSync(path.join(OUT, 'all_skills.json'), 'utf8'));
const E = all.entries;

// ---------- 本地 datagen 宝石 id ----------
function walk(d, out = [], cat = null) {
  let es; try { es = fs.readdirSync(d, { withFileTypes: true }); } catch (e) { return out; }
  for (const e of es) {
    const f = path.join(d, e.name);
    if (e.isDirectory()) walk(f, out, cat || (['skill', 'spirit', 'support'].includes(e.name) ? e.name : null));
    else if (e.name.endsWith('.json')) out.push({ file: f, cat, id: e.name.replace(/\.json$/, '') });
  }
  return out;
}
const local = walk(GEMDIR).filter(x => x.cat);
const localReal = local.filter(x => !/^(preset_|generic_)/.test(x.id));
console.log('本地 datagen 宝石条目 =', localReal.length, ' 预设/模板 =', local.length - localReal.length);

// ---------- 别名归一 ----------
function norm(s) {
  return String(s).toLowerCase()
    .replace(/armour/g, 'armor')
    .replace(/['’]s_/g, 's_').replace(/_?s_/g, 's_')
    .replace(/_i+$|_ii+$|_iii+$|_iv+$|_five$|_four$/, '')
    .replace(/[^a-z0-9]/g, '');
}
const localNorm = new Map();
for (const l of localReal) localNorm.set(norm(l.id), l);

let matched = 0; const unmatched = [];
const pairs = [];
for (const e of E) {
  const n = norm(e.id);
  const l = localNorm.get(n);
  if (l) { e.local_id = l.id; e.local_cat = l.cat; e.implemented = true; matched++; pairs.push([e.id, l.id]); }
  else { e.implemented = false; unmatched.push(e); }
}
console.log('爬取↔本地 匹配 =', matched, ' 未实现 =', unmatched.length);

// ---------- tier 变体 ----------
for (const e of E) {
  const m = e.id.match(/^(.*)_(i|ii|iii|iv)$/);
  e.base_id = m ? m[1] : e.id;
  e.tier_variant = !!m;
}
const baseSet = new Set(E.map(e => e.base_id));
console.log('去重 base_id =', baseSet.size, '（含 tier 变体共 ' + E.length + '）');

// ---------- 图标清点 ----------
const ITEM = path.join(ROOT, 'src/main/resources/assets/coe/textures/item/gem');
const GUI = path.join(ROOT, 'src/main/resources/assets/coe/textures/gui/gem');
const SRC1 = path.join(ROOT, 'poe2_assets/gems');
const SRC2 = path.join(ROOT, 'poe2_assets/icons');
const rd = d => { try { return new Set(fs.readdirSync(d).filter(f => f.endsWith('.png'))); } catch (e) { return new Set(); } };
const itemP = rd(ITEM), guiP = rd(GUI), src1 = rd(SRC1), src2 = rd(SRC2);
console.log('图标: item/gem', itemP.size, ' gui/gem', guiP.size, ' poe2_assets/gems', src1.size, ' icons', src2.size);

for (const e of E) {
  const f = e.id + '.png';
  e.icon_local_path = itemP.has(f) ? 'src/main/resources/assets/coe/textures/item/gem/' + f : null;
  // PascalCase 反查：优先 internal_id（如 SolarOrb / AbidingHexSupport），其次 id 反推
  const pascal = e.id.split('_').map(w => w[0].toUpperCase() + w.slice(1)).join('');
  const ii = e.internal_id;
  const cands = [];
  if (ii) {
    cands.push(ii + '.png');
    if (/Support$/.test(ii)) cands.push(ii.replace(/Support$/, '') + '.png');
  }
  cands.push(pascal + '.png', pascal + 'Support.png');
  const uniq = [...new Set(cands)];
  e.icon_source_candidates = uniq.filter(x => src1.has(x) || src2.has(x));
  e.icon_status = e.icon_local_path ? 'existing' : (e.icon_source_candidates.length ? 'source_available' : 'missing');
}
const st = {}; E.forEach(e => st[e.icon_status] = (st[e.icon_status] || 0) + 1);
console.log('图标三态:', JSON.stringify(st));

// ---------- 覆盖率 ----------
const byType = {};
const localMatchedByCat = {};   // 去重后的本地 id（避免 tier 变体重复计数）
for (const e of E) {
  const t = e.gem_type;
  byType[t] = byType[t] || { truth: 0, impl_entries: 0, impl_distinct: 0 };
  byType[t].truth++;
  if (e.implemented) {
    byType[t].impl_entries++;
    localMatchedByCat[t] = localMatchedByCat[t] || new Set();
    localMatchedByCat[t].add(e.local_id);
  }
}
for (const t of Object.keys(byType)) byType[t].impl_distinct = localMatchedByCat[t] ? localMatchedByCat[t].size : 0;
const localByCat = {};
for (const l of localReal) localByCat[l.cat] = (localByCat[l.cat] || 0) + 1;
console.log('\n覆盖率:', JSON.stringify(byType), ' 本地:', JSON.stringify(localByCat));

// ---------- 输出 ----------
fs.writeFileSync(path.join(OUT, 'coverage.json'), JSON.stringify({
  generated_at: new Date().toISOString(),
  total_crawled: E.length, unique_base: baseSet.size,
  note: 'impl_distinct = 去重后的本地已实现 gem 数（tier 变体只算一次）；impl_entries = 命中的爬取条目数（含变体重复）',
  by_type: byType, local_by_cat: localByCat,
  matched_crawl_entries: matched,
  matched_local_ids: new Set(pairs.map(x => x[1])).size,
  unimplemented: unmatched.length,
  icon_status: st,
  unimplemented_ids: unmatched.map(e => e.id)
}, null, 1));

fs.writeFileSync(path.join(OUT, 'id_map.json'), JSON.stringify({
  note: 'snake_case id ↔ poe2db 页名 ↔ 中文名 ↔ 本地 datagen id（已实现为 null 表示未实现）',
  map: E.map(e => ({
    id: e.id, base_id: e.base_id, href: e.source_url_us.split('/').pop(),
    name_en: e.name_en, name_zh: e.name_zh, gem_type: e.gem_type, gem_color: e.gem_color,
    internal_id: e.internal_id, local_id: e.local_id || null, implemented: e.implemented
  }))
}, null, 1));

fs.mkdirSync(path.join(OUT, 'icons'), { recursive: true });
fs.writeFileSync(path.join(OUT, 'icons/manifest.json'), JSON.stringify({
  generated_at: new Date().toISOString(),
  item_dir_count: itemP.size, gui_dir_count: guiP.size, source_dir_count: src1.size,
  status: st,
  entries: E.map(e => ({ id: e.id, url: e.icon_url, local: e.icon_local_path, source: e.icon_source_candidates, status: e.icon_status }))
}, null, 1));

// 覆盖写回 all_skills.json
fs.writeFileSync(path.join(OUT, 'all_skills.json'), JSON.stringify(all, null, 1));
for (const t of ['skill', 'support', 'spirit']) {
  const arr = E.filter(e => e.gem_type === t);
  fs.writeFileSync(path.join(OUT, t + '_gems.json'), JSON.stringify({
    sources: ['https://poe2db.tw/us/'], meta: { type: t, count: arr.length, implemented: arr.filter(x => x.implemented).length, generated_at: new Date().toISOString() },
    entries: arr
  }, null, 1));
}

// ---------- Markdown ----------
function mdRow(e) {
  const q = e.quality_bonus ? '✓' : '—';
  const lv = e.level_table.rows.length;
  return `| ${e.name_zh || ''} | ${e.name_en} | \`${e.id}\` | ${e.gem_color} | ${e.tags.slice(0, 4).join(' ')} | ${lv} | ${q} | ${e.implemented ? '✓ ' + e.local_id : '—'} |`;
}
for (const t of ['skill', 'support', 'spirit']) {
  const arr = E.filter(e => e.gem_type === t).sort((a, b) => a.id.localeCompare(b.id));
  let s = `# PoE2 ${t} Gems（权威爬取）\n\n`;
  s += `- 条目数：**${arr.length}**（其中已在本模组实现 ${arr.filter(x => x.implemented).length}）\n`;
  s += `- 来源：https://poe2db.tw/us/${t === 'skill' ? 'Skill_Gems' : t === 'support' ? 'Support_Gems' : 'Spirit_Gems'} ；中文名取自 https://poe2db.tw/tw/\n\n`;
  s += `| 中文名 | 英文名 | id | 色 | 标签 | 等级档 | 品质 | 本地已实现 |\n|---|---|---|---|---|---|---|---|\n`;
  s += arr.map(mdRow).join('\n') + '\n';
  fs.writeFileSync(path.join(OUT, t + '_gems.md'), s);
}

// index.md（用数组拼接，避免模板字符串反引号冲突）
const B = '`';
const L = [];
const p = s => L.push(s);
const F = ['name_en', 'name_zh', 'description_en', 'requirements', 'internal_id', 'icon_url', 'tags', 'quality_bonus', 'cost', 'cast_time', 'crit_chance', 'type_tags', 'level_range'];

p('# PoE2 技能资料爬取 — Index & Coverage');
p('');
p('> 本目录是 **CraftOfExile 技能系统的唯一权威真值源**。');
p('> 本地 ' + B + 'src/generated/resources/data/coe/coe/item/gem/**' + B + ' 的既有技能数据**不作为判据**，仅用于覆盖率对照。');
p('');
p('## 数据源');
p('');
p('| 用途 | 源 |');
p('|---|---|');
p('| 权威数值（英文） | https://poe2db.tw/us/ |');
p('| 中文名 | https://poe2db.tw/tw/ |');
p('');
p('索引页：' + B + 'Gems' + B + '、' + B + 'Skill_Gems' + B + '、' + B + 'Support_Gems' + B + '、' + B + 'Spirit_Gems' + B + '、' + B + 'Lineage_Supports' + B + '。');
p('发现方式：索引页表格行解析（' + B + '<a class="gem_{red|green|blue}" href="/us/<Page>">' + B + '），按页名去重。');
p('**纳入范围**：索引页按 `<tr data-filters>` 行切分后发现 ' + (E.length + 28) + ' 条，剔除 **28 条** ' + B + 'DNT-*' + B + ' 废弃条目后保留 **' + E.length + '** 条。');
p('其中 1035 条来自宝石索引页（Skill_Gems 407 / Support_Gems 557 / Spirit_Gems 53 / Gems 页并集 963 去重），');
p('另 **11 条**为不在宝石索引、但 poe2db 页面真实存在的**无宝石技能**（武器/角色固有技能，如 `Cyclone`、`Ground_Slam`、`Wrath`、`Grace`）。');
p('');
p('**关键口径**（易错）：poe2db 页面自报的 `Gems Summary /983` **= Skill Gems 427 + Support Gems 557 的行数之和**，不是唯一技能数。');
p('唯一技能数以 **href 去重**为准：Gems 页 1066 行 → **963 唯一**；且 **Spirit ⊆ Skill（∩=53）**，精魂宝石全部包含在技能页内。');
p('');
p('## 覆盖计数');
p('');
p('| 类别 | 权威条目 | 基础技能(去 tier) | 本地已实现(去重) | 覆盖率 | 本地 datagen 条目 |');
p('|---|---|---|---|---|---|');
for (const t of ['skill', 'support', 'spirit']) {
  const b = byType[t] || { truth: 0, impl_distinct: 0 };
  const baseN = new Set(E.filter(e => e.gem_type === t).map(e => e.base_id)).size;
  p('| ' + t + ' | ' + b.truth + ' | ' + baseN + ' | ' + b.impl_distinct + ' | ' + (b.impl_distinct / b.truth * 100).toFixed(0) + '% | ' + (localByCat[t] || 0) + ' |');
}
p('');
p('- 爬取总条目 **' + E.length + '**，按 tier 变体（' + B + '_i/_ii/_iii' + B + '）合并后 **' + baseSet.size + '** 个基础技能。');
p('- 与本地 datagen 匹配 **' + matched + '**，**未实现 ' + unmatched.length + '**。');
p('');
p('## 字段完整度');
p('');
p('| 字段 | 覆盖 |');
p('|---|---|');
for (const f of F) {
  const n = E.filter(e => {
    const v = e[f]; if (v == null) return false;
    if (Array.isArray(v)) return v.length > 0; return String(v).trim() !== '';
  }).length;
  p('| ' + B + f + B + ' | ' + n + '/' + E.length + ' (' + (n / E.length * 100).toFixed(0) + '%) |');
}
p('');
p('## 关键结构说明');
p('');
p('### 等级表（level_table）');
p('- ' + E.filter(e => e.level_table.rows.length > 1).length + ' 条含**多档**逐等级表（最长 ' + Math.max(...E.map(e => e.level_table.rows.length)) + ' 档）。');
p('- **506 条辅助宝石的等级表只有 1 行**（' + B + 'Level 1 / Requires Level 0 / Int 0' + B + '）—— 这是 poe2db 的真实呈现，**不是抓取缺陷**：');
p('  辅助宝石的等级成长写在 ' + B + 'Implicit' + B + ' 字段里（如 ' + B + 'Mana: 100%' + B + '、' + B + 'Supported Skills have 80% more duration...' + B + '），');
p('  且其强度分级由 **tier 变体**（' + B + 'Xxx I / II / III' + B + '）承载，而非同页逐等级表。');
p('- 表头形如 ' + B + 'Level | Requires Level | Int | Base Damage | Deals X to Y <Type> Damage' + B + '；数值列形如 ' + B + '"4, 5"' + B + '（min, max）。');
p('');
p('### 品质（quality）');
p('- ' + E.filter(e => e.quality_bonus).length + '/' + E.length + ' 条含品质加成文案，形如 ' + B + '(0-20)% more Damage with Hits against Burning Enemies' + B + '。');
p('- 注意：本模组当前 **品质完全未接线**（537 个 gem JSON 中 ' + B + 'quality' + B + ' 出现 0 次）。');
p('');
p('### 区间伤害');
p('- 真值形如 ' + B + 'Deals (4-181) to (5-271) Fire Damage' + B + '，已抓进 ' + B + 'implicits' + B + '。');
p('- 注意：本模组 ' + B + 'DamageAction' + B + ' 只有单值 ' + B + 'amount' + B + '，**区间伤害当前无法表达**。');
p('');
p('## 图标三态');
p('');
p('| 状态 | 数量 | 含义 |');
p('|---|---|---|');
p('| ' + B + 'existing' + B + ' | ' + (st.existing || 0) + ' | 已接入 ' + B + 'src/main/resources/assets/coe/textures/item/gem/<id>.png' + B + ' |');
p('| ' + B + 'source_available' + B + ' | ' + (st.source_available || 0) + ' | 未接入，但 ' + B + 'poe2_assets/gems|icons' + B + ' 有 PascalCase 源资产可转换 |');
p('| ' + B + 'missing' + B + ' | ' + (st.missing || 0) + ' | 本地无，需从 CDN 下载 |');
p('');
p('源资产：' + B + 'poe2_assets/gems' + B + ' ' + src1.size + ' 张、' + B + 'poe2_assets/icons' + B + ' ' + src2.size + ' 张（PascalCase）。');
p('已接入：' + B + 'textures/item/gem' + B + ' ' + itemP.size + ' 张、' + B + 'textures/gui/gem' + B + ' ' + guiP.size + ' 张（snake_case）。');
p('命名映射：PascalCase → snake_case 并去尾 ' + B + 'Support' + B + '。');
p('');
p('## 文件清单');
p('');
p('| 文件 | 内容 |');
p('|---|---|');
p('| ' + B + 'all_skills.json' + B + ' | 全量 ' + E.length + ' 条（机器可读） |');
p('| ' + B + 'skill_gems.json' + B + ' / ' + B + '.md' + B + ' | 主动技能 |');
p('| ' + B + 'support_gems.json' + B + ' / ' + B + '.md' + B + ' | 辅助宝石 |');
p('| ' + B + 'spirit_gems.json' + B + ' / ' + B + '.md' + B + ' | 精魂宝石 |');
p('| ' + B + 'coverage.json' + B + ' | 覆盖率三态 + 未实现 id 清单 |');
p('| ' + B + 'id_map.json' + B + ' | id ↔ 页名 ↔ 中文名 ↔ 本地 datagen id 映射 |');
p('| ' + B + 'icons/manifest.json' + B + ' | 图标三态清单 |');
p('| ' + B + 'zh_names.json' + B + ' | 中文名映射（' + Object.keys(JSON.parse(fs.readFileSync(path.join(OUT, 'zh_names.json'), 'utf8'))).length + ' 条） |');
p('| ' + B + 'raw/html_gz/' + B + ' | 原始 HTML（gzip 缓存，可断点续跑） |');
p('');
p('## 可复现脚本');
p('');
p(B + 'design/refactor/skill-data/_tools/' + B + '：');
p('- ' + B + 'discover_all.js' + B + ' — 覆盖面发现（含噪音剔除）');
p('- ' + B + 'crawl_details.js' + B + ' — 详情页抓取 + 解析 + 落盘（' + B + 'node crawl_details.js all zh' + B + '）');
p('- ' + B + 'finalize.js' + B + ' — 覆盖率对照 + 图标清点 + 文档生成');
p('- ' + B + 'report.js' + B + ' / ' + B + 'diag.js' + B + ' / ' + B + 'diag2.js' + B + ' — 质检与结构诊断');
p('');
p('## 已知缺口');
p('');
p('1. **描述仅英文**：' + B + 'description_zh' + B + ' 未抓（中文详情页正文未取）；中文名已抓 ' + E.filter(e => e.name_zh).length + ' 条。');
p('2. **未覆盖 Granted 类**（装备/升华授予的技能，无宝石形态）。');
p('3. **武器限制**未结构化（仅出现在 ' + B + 'implicits' + B + ' 文本中）。');
p('4. **辅助宝石的 supports/excludes 集合**未抓 —— poe2db 用"可辅助的技能类别"描述，需二次解析。');
p('5. ' + B + 'cost' + B + '/' + B + 'cast_time' + B + '/' + B + 'crit_chance' + B + ' 仅 ' + E.filter(e => e.cost).length + '/' + E.filter(e => e.cast_time).length + '/' + E.filter(e => e.crit_chance).length + ' 条有值（辅助宝石页面不提供这些字段）。');
p('');
p('生成时间：' + new Date().toISOString());
fs.writeFileSync(path.join(OUT, 'index.md'), L.join('\n'));
console.log('\n已写出 index.md + 各类别 .md + coverage.json + id_map.json + icons/manifest.json');
