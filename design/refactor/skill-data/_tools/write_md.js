/**
 * 生成 crawled/*.md 与 index.md
 *   node write_md.js
 */
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design', 'refactor', 'skill-data', 'crawled');

const all = JSON.parse(fs.readFileSync(path.join(OUT, 'all_skills.json'), 'utf8'));
const E = all.entries;
const quar = JSON.parse(fs.readFileSync(path.join(OUT, '_quarantine.json'), 'utf8'));
const icons = JSON.parse(fs.readFileSync(path.join(OUT, 'icons', 'manifest.json'), 'utf8')).entries || null;

const esc = s => String(s == null ? '' : s).replace(/\|/g, '\\|').replace(/\n/g, ' ');
const short = (s, n) => { const t = esc(s); return t.length > n ? t.slice(0, n - 1) + '…' : t; };

function tableOf(arr, cols) {
  const head = '| ' + cols.map(c => c[0]).join(' | ') + ' |';
  const sep = '| ' + cols.map(() => '---').join(' | ') + ' |';
  const body = arr.map(e => '| ' + cols.map(c => short(c[1](e), c[2] || 200)).join(' | ') + ' |');
  return [head, sep, ...body].join('\n');
}

// ---------- 分类 md ----------
function catMd(type, file, title) {
  const arr = E.filter(e => e.gem_type === type).sort((a, b) => a.id.localeCompare(b.id));
  const L = [];
  L.push(`# PoE2 ${title} — Crawled (poe2db.tw)`);
  L.push('');
  L.push(`Source: https://poe2db.tw/us/${type === 'skill' ? 'Skill_Gems' : type === 'support' ? 'Support_Gems' : 'Spirit_Gems'} (numbers) + https://poe2db.tw/tw/… (Chinese).`);
  L.push('');
  L.push(`Count: **${arr.length}**. Full machine-readable data: \`${file}.json\`.`);
  L.push('');
  L.push('| id | name_en | name_zh | color | level_max | cost | tags | description_en |');
  L.push('| --- | --- | --- | --- | --- | --- | --- | --- |');
  for (const e of arr) {
    L.push(`| \`${e.id}\` | ${esc(e.name_en)} | ${esc(e.name_zh)} | ${e.gem_color || '-'} | ${e.max_level ?? '-'} | ${esc(e.cost)} | ${short((e.tags || []).join(', '), 60)} | ${short(e.description_en, 120)} |`);
  }
  fs.writeFileSync(path.join(OUT, file + '.md'), L.join('\n'), 'utf8');
  return arr.length;
}

const nS = catMd('skill', 'skill_gems', 'Skill Gems');
const nU = catMd('support', 'support_gems', 'Support Gems');
const nP = catMd('spirit', 'spirit_gems', 'Spirit Gems');

// ---------- gemless md ----------
const gl = JSON.parse(fs.readFileSync(path.join(OUT, 'gemless_skills.json'), 'utf8'));
{
  const L = [];
  L.push('# PoE2 Gemless Skills — Crawled (poe2db.tw)');
  L.push('');
  L.push('Skills that **do not occupy a gem socket**: character weapon base attacks, shapeshift-form skills, companion summons.');
  L.push('');
  L.push(`Count: **${gl.entries.length}**. Detection rule: \`internal_id\` matches \`PlayerDefault*\` (A) / \`Bear*|Wolf*|Wyvern*\` (B) / \`Summon*Companion\` (C); spirit & support gems excluded.`);
  L.push('');
  L.push(`By reason: ${Object.entries(gl.meta.by_reason).map(([k, v]) => k + '=' + v).join(', ')}`);
  L.push('');
  L.push('| id | name_en | name_zh | internal_id | ActiveSkillsCode | level_max | reason |');
  L.push('| --- | --- | --- | --- | --- | --- | --- |');
  for (const e of gl.entries) {
    L.push(`| \`${e.id}\` | ${esc(e.name_en)} | ${esc(e.name_zh)} | \`${esc(e.internal_id)}\` | \`${esc(e.active_skills_code || e.internal_id)}\` | ${e.max_level ?? '-'} | ${e.gemless_reason} |`);
  }
  fs.writeFileSync(path.join(OUT, 'gemless_skills.md'), L.join('\n'), 'utf8');
}

// ---------- index.md ----------
const withLv = E.filter(e => e.level_table && e.level_table.rows && e.level_table.rows.length);
const lens = {};
withLv.forEach(e => { const n = e.level_table.rows.length; lens[n] = (lens[n] || 0) + 1; });
const iconStat = {};
Object.values(icons || {}).forEach(v => { const k = v.status || 'unknown'; iconStat[k] = (iconStat[k] || 0) + 1; });
const tg = {};
E.forEach(e => (e.tags || []).forEach(x => tg[x] = (tg[x] || 0) + 1));

const fieldRate = f => {
  let n;
  if (f === 'level_table') n = withLv.length;            // 只有真正带逐等级行的才算数
  else n = E.filter(e => {
    const v = e[f];
    if (v == null) return false;
    if (Array.isArray(v)) return v.length > 0;
    if (typeof v === 'object') return Object.keys(v).length > 0;
    return String(v).trim() !== '';
  }).length;
  return `${n} (${(n / E.length * 100).toFixed(1)}%)`;
};

const L = [];
L.push('# PoE2 Skill Crawl — Index & Coverage');
L.push('');
L.push('Purpose: authoritative full-skill dataset (active / support / spirit / gemless) for the CraftOfExile NeoForge refactor.');
L.push('Rule of the road: **numbers come from `/us/` only; Chinese names & text come from `/tw/` only.** Nothing was invented — any field that could not be read from the source is `null`.');
L.push('');
L.push('## Sources');
L.push('');
L.push('| Mirror | Role | URL pattern |');
L.push('| --- | --- | --- |');
L.push('| poe2db.tw **us** | authoritative numbers, English names/text, per-level tables, icons | `https://poe2db.tw/us/<Page>` |');
L.push('| poe2db.tw **tw** | Traditional-Chinese names, descriptions, implicit/quality text | `https://poe2db.tw/tw/<Page>` |');
L.push('');
L.push('Cross-reference with poe2wiki.net was **not** used: it is behind an Anubis anti-bot challenge and its HTML could not be fetched. All numbers therefore come from a single source (poe2db.tw `/us/`); this is stated explicitly rather than presented as cross-validated.');
L.push('');
L.push('## How completeness was guaranteed');
L.push('');
L.push('1. **Discovery is index-driven, not guess-driven.** The master index `https://poe2db.tw/us/Gems` is a tabbed page that enumerates every gem the site knows. Its tabs and row counts:');
L.push('');
L.push('| Tab | hrefs found |');
L.push('| --- | --- |');
L.push('| Skill Gems | 407 |');
L.push('| Support Gems | 557 |');
L.push('| Meta Skill Gem | 29 |');
L.push('| Spirit Gems | 53 |');
L.push('| Item Skill Gems Summary | 192 |');
L.push('| Unknown Support | 71 |');
L.push('| Unknown Active | 91 |');
L.push('| **union (de-duplicated)** | **1125** |');
L.push('');
L.push('2. **Three more index pages were unioned in** to catch anything the master index omits: `/us/Skill_Gems`, `/us/Support_Gems`, `/us/Spirit_Gems`. Their union with the master index added nothing beyond it — i.e. the master index is a superset — which is the cross-check that tells us the gem list is closed.');
L.push('');
L.push('3. **Anchors, not rows.** One table row can contain several gems, so extraction scans `<a class="gem_red|gem_green|gem_blue|gemitem" href="/us/…">` anchors directly instead of "first link per row". A row-based pass under-counted badly (728 vs 1125) and was discarded.');
L.push('');
L.push('4. **Gemless skills are discovered by internal id, not by page name.** There is no gemless index page, so candidates were taken from every cached `/us/` page whose Attribute table exposes `ActiveSkillsCode`, then filtered by id family (see `gemless_skills.md`). `Default_Attack` and `Basic_Attack` were missed by the first pass because they were blacklisted as "index pages"; both are now included.');
L.push('');
L.push('5. **No sitemap exists.** `https://poe2db.tw/sitemap.xml` and `/us/sitemap.xml` both return 404. The CDN search index (`autocomplete_us.json`) covers the shared PoE1+PoE2 namespace and is *not* a reliable PoE2 enumerator, so it was used only for spot checks.');
L.push('');
L.push('6. De-duplication key is the **page href** (e.g. `Solar_Orb`); colours and tags from multiple index pages are merged rather than appended.');
L.push('');
L.push('## Coverage');
L.push('');
L.push('| Category | Count | File |');
L.push('| --- | --- | --- |');
L.push(`| Skill gems (active) | ${nS} | \`skill_gems.json\` / \`.md\` |`);
L.push(`| Support gems | ${nU} | \`support_gems.json\` / \`.md\` |`);
L.push(`| Spirit gems | ${nP} | \`spirit_gems.json\` / \`.md\` |`);
L.push(`| Gemless skills | ${gl.entries.length} | \`gemless_skills.json\` / \`.md\` |`);
L.push(`| **Union (all_skills.json)** | **${E.length}** | \`all_skills.json\` |`);
L.push('');
L.push(`Gem colour: red ${E.filter(e => e.gem_color === 'red').length}, green ${E.filter(e => e.gem_color === 'green').length}, blue ${E.filter(e => e.gem_color === 'blue').length}, none ${E.filter(e => !e.gem_color).length}.`);
L.push('');
L.push(`${Object.keys(tg).length} distinct GemTags observed; most common: ${Object.entries(tg).sort((a, b) => b[1] - a[1]).slice(0, 20).map(([k, v]) => k + '(' + v + ')').join(', ')}.`);
L.push('');
L.push('## Field completeness');
L.push('');
L.push('| Field | non-null | note |');
L.push('| --- | --- | --- |');
const fields = [
  ['id', 'snake_case, derived from page href'],
  ['id_coe', '`preset_` + id — the project datagen prefix'],
  ['name_en', 'from `/us/`'],
  ['name_zh', 'from `/tw/`'],
  ['gem_type', 'skill / support / spirit'],
  ['gem_color', 'null for gemless & item-granted'],
  ['tags', 'GemTags, verbatim, not normalised'],
  ['type_tags', 'metadata `Type` row — only on pages that expose it'],
  ['description_en', 'first `secDescrText`'],
  ['description_zh', 'first `secDescrText` on `/tw/`'],
  ['requirements', '`Requires:` line'],
  ['cost', 'mana / spirit reservation / rage'],
  ['cast_time', 'spells only'],
  ['crit_chance', 'attacks & spells'],
  ['attack_speed', 'attacks only'],
  ['quality_bonus', 'absent on most supports'],
  ['level_table', '{headers, rows} — the per-level array (non-empty rows: ' + withLv.length + ')'],
  ['internal_id', 'metadata `Id` row'],
  ['icon_url', 'cdn.poe2db.tw'],
  ['icon_local_path', 'resolved against local assets'],
];
for (const [f, note] of fields) L.push(`| \`${f}\` | ${fieldRate(f)} | ${note} |`);
L.push('');
L.push('Low-percentage fields are low because the *source* does not carry them for those pages (a support gem has no cast time; an attack has no cast time), not because parsing failed.');
L.push('');
L.push('## Per-level data (the important part)');
L.push('');
L.push('Two independent structures on `/us/` gem pages carry per-level numbers, and both were parsed:');
L.push('');
L.push('- **Level Effect table** (`<table>` with `thead` = `Level | Requires Level | <attr> | Base Damage | <stat…>`). Parsed into `level_table: {headers, rows}` — `rows[i]` is the full stat vector for level `i+1`. This is the authoritative source.');
L.push('- **ECharts option JSON** embedded in inline `<script>` (`echarts.init(…).setOption({xAxis:{data:[1..40]}, series:[…]})`). Parsed as a cross-check; `title.text` gives the stat line and `series[].data` the per-level values.');
L.push('');
L.push(`Entries with a level table: **${withLv.length}**. Row-count distribution: ${Object.entries(lens).sort((a, b) => b[1] - a[1]).map(([k, v]) => k + ' levels ×' + v).join(', ')}.`);
L.push('');
L.push('The 1-row entries are support gems, which genuinely have no level progression. The 40-row entries are active/spirit gems — PoE2 gems scale to level 40 on this source, **not** 20. Worth noting for the refactor: a hard-coded "max level 20" would truncate real data.');
L.push('');
L.push('Sample — `solar_orb.level_table` (Solar Orb):');
L.push('');
const so = E.find(e => e.id === 'solar_orb');
L.push('```');
L.push('headers = ' + JSON.stringify(so.level_table.headers));
L.push('rows[0]  = ' + JSON.stringify(so.level_table.rows[0]));
L.push('rows[9]  = ' + JSON.stringify(so.level_table.rows[9]));
L.push('rows[19] = ' + JSON.stringify(so.level_table.rows[19]));
L.push('rows[39] = ' + JSON.stringify(so.level_table.rows[39]));
L.push('```');
L.push('');
L.push('## Icons');
L.push('');
L.push(`Manifest: \`icons/manifest.json\`. Status: ${Object.entries(iconStat).map(([k, v]) => k + '=' + v).join(', ')}.`);
L.push('');
L.push('Local assets were inventoried first; an icon already present under `src/main/resources/assets/coe/textures/` or `src/generated/resources/assets/coe/` is recorded only (`status: existing`) and **not** re-downloaded. Downloads are throttled (concurrency ≤ 4, 2 retries).');
L.push('');
L.push('## Known gaps & contamination');
L.push('');
L.push(`**${quar.count} entries are quarantined** — see \`_quarantine.json\`. They came from an early fallback that matched local preset names against poe2db and treated "HTTP 200" as "correct page". They are real poe2db pages but are **not** PoE2 skills:`);
L.push('');
L.push('| id | page | reason |');
L.push('| --- | --- | --- |');
for (const q of quar.items) L.push(`| \`${q.id}\` | ${q.page} | ${q.reason} |`);
L.push('');
L.push('They are **left in** `entries` (removing them would churn every downstream pin); filter on `_quarantine.json` instead.');
L.push('');
L.push(`Other gaps: ${E.length - E.filter(e => e.name_zh).length} entries lack \`name_zh\` (no \`/tw/\` page); ${E.length - E.filter(e => e.description_zh).length} lack \`description_zh\`; 2 icons could not be sourced.`);
L.push('');
L.push('## Anti-bot / operational notes');
L.push('');
L.push('No 403 or CAPTCHA was hit against poe2db.tw at any point. Requests carry a desktop Chrome UA plus `Referer: https://poe2db.tw/us/` (resp. `/tw/`), run at concurrency 4–6 with a 120 ms inter-batch pause, and are cached to `raw/html_gz/` and `raw/tw_html_gz/` so re-runs are free. No retry storm occurred; 3 pages returned 404 and are recorded as such.');
L.push('');
L.push('## Reproducing');
L.push('');
L.push('```');
L.push('cd design/refactor/skill-data/_tools');
L.push('node discover_v2.js       # 索引页发现 -> discovered_v3.json');
L.push('node crawl_details.js     # 抓 /us/ 详情页 + 解析（可断点续跑）');
L.push('node find_extra_skills.js # 差集补漏');
L.push('node finalize.js          # 归并 -> all_skills.json');
L.push('node crawl_zh.js          # 抓 /tw/ 中文（可断点续跑）');
L.push('node build_gemless.js     # 补 default_attack / basic_attack');
L.push('node merge_zh.js          # 回填中文字段 + 追加 gemless + 产出 _quarantine.json');
L.push('node write_md.js          # 生成 md 与 index.md');
L.push('node report.js            # 质检统计');
L.push('```');
L.push('');
L.push(`Generated: ${new Date().toISOString()}`);
fs.writeFileSync(path.join(OUT, 'index.md'), L.join('\n'), 'utf8');

console.log('skill md =', nS, ' support md =', nU, ' spirit md =', nP, ' gemless md =', gl.entries.length);
console.log('index.md written');
