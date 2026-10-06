/**
 * PoE2 技能全量爬取 —— 详情页抓取 + 解析
 * 源：https://poe2db.tw/us/<Page>（权威数值） + https://poe2db.tw/tw/<...>（中文名）
 * 输入：_tools/discovered.json（728 条 href/color/icon/filters）
 * 输出：crawled/*.json + *.md
 *
 * 用法：
 *   node crawl_details.js            # 抓全部
 *   node crawl_details.js 20         # 只抓前 20 条（试跑）
 *   node crawl_details.js all zh     # 抓全部 + 抓中文名
 */
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');

const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data/crawled');
const RAW = path.join(OUT, 'raw', 'html_gz');
const DISCOVERED = path.join(ROOT, 'design/refactor/skill-data/_tools/discovered_v3.json');
fs.mkdirSync(RAW, { recursive: true });
fs.mkdirSync(OUT, { recursive: true });

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36';
const HEADERS = { 'User-Agent': UA, 'Referer': 'https://poe2db.tw/us/', 'Accept-Language': 'en-US,en;q=0.9' };

const LIMIT = process.argv[2] && process.argv[2] !== 'all' ? parseInt(process.argv[2], 10) : Infinity;
const DO_ZH = process.argv.includes('zh');
const CONCURRENCY = 6;

// ---------- utils ----------
const sleep = ms => new Promise(r => setTimeout(r, ms));

async function fetchText(url, tries = 3) {
  for (let i = 0; i < tries; i++) {
    try {
      const ctl = new AbortController();
      const to = setTimeout(() => ctl.abort(), 30000);
      const r = await fetch(url, { headers: HEADERS, signal: ctl.signal });
      clearTimeout(to);
      if (r.status === 404) return null;
      if (!r.ok) throw new Error('HTTP ' + r.status);
      return await r.text();
    } catch (e) {
      if (i === tries - 1) return null;
      await sleep(800 * (i + 1));
    }
  }
  return null;
}

function stripTags(s) {
  return s
    .replace(/<span class="ndash">[^<]*<\/span>/g, '-')
    .replace(/<br\s*\/?>/g, ' ')
    .replace(/<[^>]+>/g, '')
    .replace(/&amp;/g, '&').replace(/&lt;/g, '<').replace(/&gt;/g, '>')
    .replace(/&quot;/g, '"').replace(/&#39;/g, "'").replace(/&nbsp;/g, ' ')
    .replace(/\s+/g, ' ').trim();
}

function tablesOf(html) {
  const out = [];
  const re = /<table\b[^>]*>([\s\S]*?)<\/table>/g;
  let m;
  while ((m = re.exec(html))) {
    const t = m[1];
    const th = [...t.matchAll(/<th\b[^>]*>([\s\S]*?)<\/th>/g)].map(x => stripTags(x[1]));
    const bodyM = t.match(/<tbody\b[^>]*>([\s\S]*?)<\/tbody>/);
    const body = bodyM ? bodyM[1] : t;
    const rows = [...body.matchAll(/<tr\b[^>]*>([\s\S]*?)<\/tr>/g)]
      .map(r => [...r[1].matchAll(/<td\b[^>]*>([\s\S]*?)<\/td>/g)].map(c => stripTags(c[1])))
      .filter(r => r.length);
    out.push({ headers: th, rows });
  }
  return out;
}

function num(s) {
  if (s == null) return null;
  const t = String(s).replace(/,/g, '').replace(/%/g, '').trim();
  if (t === '' || t === '-') return null;
  const v = parseFloat(t);
  return Number.isFinite(v) ? v : null;
}

// ---------- 详情页解析 ----------
function parseDetail(html, meta) {
  const d = {
    id: null, id_coe: null, name_en: meta.name, name_zh: null,
    gem_type: null, gem_color: meta.color || null,
    tags: [], category_tags: [], type_tags: [],
    description_en: null, description_zh: null,
    level_range: null, cost: null, cast_time: null,
    crit_chance: null, attack_speed: null,
    requirements: null, req_level_range: null, req_attr: null,
    quality_bonus: null, quality_range: null,
    implicits: [], stats: [],
    level_table: { headers: [], rows: [] }, max_level: null,
    recommended_supports: [], internal_id: null, base_type: null,
    target_types: null, acronyms: [],
    icon_url: meta.icon || null, icon_local_path: null,
    source_url_us: 'https://poe2db.tw/us/' + meta.href,
    source_url_tw: 'https://poe2db.tw/tw/' + meta.href,
    crawled_at: new Date().toISOString()
  };
  d.id = meta.href.replace(/_/g, '_').toLowerCase().replace(/[^a-z0-9_]/g, '');
  d.id_coe = 'preset_' + d.id;

  // tags (class 含 GemTags)
  d.tags = [...new Set([...html.matchAll(/<a[^>]*class="[^"]*GemTags[^"]*"[^>]*>([^<]+)<\/a>/g)].map(x => x[1].trim()))];
  // filters（索引页给的标签集合，含 Support/Skill 等分类词）
  d.category_tags = (meta.filters || '').split(/\s+/).filter(Boolean);

  // properties：Level / Cost / Cast Time / Critical Hit Chance / Attack Speed ...
  for (const m of html.matchAll(/<div class="property">([\s\S]*?)<\/div>/g)) {
    const t = stripTags(m[1]);
    const i = t.indexOf(':');
    if (i < 0) continue;
    const k = t.slice(0, i).trim(), v = t.slice(i + 1).trim();
    if (/^Level$/i.test(k)) d.level_range = v;
    else if (/^Cost$/i.test(k)) d.cost = v;
    else if (/^Cast Time$/i.test(k)) d.cast_time = v;
    else if (/Critical Hit Chance$/i.test(k)) d.crit_chance = v;
    else if (/^Attack Speed$/i.test(k)) d.attack_speed = v;
  }

  const rm = html.match(/<div class="requirements">([\s\S]*?)<\/div>/);
  if (rm) {
    d.requirements = stripTags(rm[1]).replace(/^Requires:\s*/i, '');
    const lm = d.requirements.match(/Level\s*\(?([\d\-–]+)/i);
    if (lm) d.req_level_range = lm[1];
    const am = d.requirements.match(/\(?(\d+[\-–]\d+)\)?\s*(Str|Dex|Int)/i);
    if (am) d.req_attr = { range: am[1], attr: am[2] };
  }

  const dm = html.match(/<div class="secDescrText">([\s\S]*?)<\/div>/);
  if (dm) d.description_en = stripTags(dm[1]);

  // quality
  for (const m of html.matchAll(/<div class="qualityMod">([\s\S]*?)<\/div>/g)) {
    const t = stripTags(m[1]);
    if (t) d.quality_bonus = (d.quality_bonus ? d.quality_bonus + ' | ' : '') + t;
  }
  const qm = d.quality_bonus && d.quality_bonus.match(/\((\d+)-(\d+)\)%/);
  if (qm) d.quality_range = [parseInt(qm[1], 10), parseInt(qm[2], 10)];

  // implicit / stats
  for (const m of html.matchAll(/<div class="explicitMod">([\s\S]*?)<\/div>/g)) {
    const t = stripTags(m[1]);
    if (t && !d.implicits.includes(t)) d.implicits.push(t);
  }

  // tables
  for (const t of tablesOf(html)) {
    const h0 = (t.headers[0] || '');
    if (h0 === 'Level' && t.headers.some(x => /Requires Level/i.test(x))) {
      d.level_table = t;
      d.max_level = t.rows.length ? num(t.rows[t.rows.length - 1][0]) : null;
    } else if (h0 === 'Implicit') {
      t.rows.forEach(r => r[0] && d.stats.push(r[0]));
    } else if (h0 === 'Tier' && t.headers.some(x => /Gems/i.test(x))) {
      t.rows.forEach(r => {
        if (r[1]) r[1].split(/(?=[A-Z][a-z]+ )/).map(s => s.trim()).filter(Boolean)
          .forEach(g => d.recommended_supports.push({ tier: num(r[0]), gem: g }));
      });
    } else if (h0 === 'Name' && t.headers.some(x => /Show Full Descriptions/i.test(x))) {
      for (const r of t.rows) {
        const k = (r[0] || '').replace(/\s*<[^>]*>\s*/g, '').trim();
        const v = r[1] || '';
        if (k === 'Id') d.internal_id = v;
        else if (k === 'BaseType') d.base_type = v;
        else if (k.startsWith('Class')) d.gem_class = v;
        else if (k === 'TargetTypes') d.target_types = v;
        else if (k === 'Type') d.type_tags = v.split(',').map(s => s.trim()).filter(Boolean);
        else if (k === 'Acronym') d.acronyms = v.split(',').map(s => s.trim()).filter(Boolean);
      }
    }
  }

  // gem_type 判定：优先索引页归属（权威），其次元数据 Class，最后 filters
  const cls = (d.gem_class || '').toLowerCase();
  const filt = d.category_tags.map(s => s.toLowerCase());
  if (meta.gem_type && meta.gem_type !== 'unknown') d.gem_type = meta.gem_type;
  else if (/support/.test(cls) || filt[0] === 'support') d.gem_type = 'support';
  else if (/spirit/.test(cls) || (meta.seenIn || []).includes('Spirit_Gems')) d.gem_type = 'spirit';
  else d.gem_type = 'skill';
  // 叠加标志（不互斥，呼应铁律 3「分类可交叉」）
  d.is_meta = !!meta.is_meta || /meta/i.test(d.gem_class || '');
  d.is_lineage = !!meta.is_lineage;
  d.index_tier = meta.tier == null ? null : meta.tier;
  d.art_name = meta.art || null;
  d.seen_in = meta.seenIn || [meta.page];
  // 索引页行内名称/颜色作为兜底
  if (!d.name_en) d.name_en = meta.name;
  if (!d.gem_color) d.gem_color = meta.color;

  // 图标兜底：索引页没给就从详情页提（详情页第一条 skillicons 图即本宝石图标）
  if (!d.icon_url) {
    const imgs = [...html.matchAll(/src="(https:\/\/cdn\.poe2db\.tw\/image\/[^"]+)"/gi)]
      .map(m => m[1]).filter(u => /skillicons/i.test(u));
    if (imgs.length) {
      const want = d.id.replace(/[^a-z0-9]/g, '');
      const best = imgs.find(u => {
        const b = u.split('/').pop().toLowerCase().replace(/\.[a-z0-9]+$/, '')
          .replace(/[^a-z0-9]/g, '').replace(/support$/, '');
        return b === want || b === want.replace(/support$/, '');
      });
      d.icon_url = best || imgs[0];
    }
  }
  return d;
}

// ---------- 中文名映射（只抓 tw 索引页，4 次请求） ----------
async function zhMap() {
  const pages = ['Gems', 'Skill_Gems', 'Support_Gems', 'Spirit_Gems', 'Lineage_Supports'];
  const map = {};
  for (const p of pages) {
    const t = await fetchText('https://poe2db.tw/tw/' + p);
    if (!t) { console.log('  [zh] miss', p); continue; }
    let n = 0;
    for (const m of t.matchAll(/<a[^>]*class="[^"]*gem_(?:red|green|blue)[^"]*"[^>]*href="\/tw\/([^"]+)"[^>]*>([^<]+)<\/a>/g)) {
      map[m[1]] = m[2].trim(); n++;
    }
    if (n === 0) {
      for (const m of t.matchAll(/href="\/tw\/([^"]+)"[^>]*>([^<]{1,60})<\/a>/g)) map[m[1]] = m[2].trim();
    }
    console.log('  [zh]', p, Object.keys(map).length, '累计');
    await sleep(400);
  }
  return map;
}

// ---------- 主流程 ----------
(async () => {
  const raw = JSON.parse(fs.readFileSync(DISCOVERED, 'utf8'));
  const list = raw.filter(e => !/^DNT[-_]/i.test(e.href) && !e.is_dnt);
  console.log('清单 =', raw.length, ' 剔除 DNT 后 =', list.length);
  const targets = list.slice(0, LIMIT === Infinity ? list.length : LIMIT);
  console.log('待抓页面 =', targets.length);

  if (DO_ZH) {
    console.log('抓中文名映射...');
    const zm = await zhMap();
    fs.writeFileSync(path.join(OUT, 'zh_names.json'), JSON.stringify(zm, null, 1));
    console.log('中文名映射 =', Object.keys(zm).length, '条');
  }

  const zh = fs.existsSync(path.join(OUT, 'zh_names.json'))
    ? JSON.parse(fs.readFileSync(path.join(OUT, 'zh_names.json'), 'utf8')) : {};

  const results = [];
  const failed = [];
  let done = 0;

  async function work(item) {
    const gz = path.join(RAW, item.href + '.html.gz');
    let html;
    if (fs.existsSync(gz)) {
      html = zlib.gunzipSync(fs.readFileSync(gz)).toString('utf8');
    } else {
      html = await fetchText('https://poe2db.tw/us/' + item.href);
      if (html) fs.writeFileSync(gz, zlib.gzipSync(Buffer.from(html, 'utf8')));
      await sleep(120);
    }
    if (!html) { failed.push(item.href); return; }
    try {
      const d = parseDetail(html, item);
      d.name_zh = zh[item.href] || null;
      results.push(d);
    } catch (e) {
      failed.push(item.href + ' :: ' + e.message);
    }
    done++;
    if (done % 50 === 0) console.log('  进度', done + '/' + targets.length);
  }

  for (let i = 0; i < targets.length; i += CONCURRENCY) {
    await Promise.all(targets.slice(i, i + CONCURRENCY).map(work));
  }

  console.log('解析成功 =', results.length, ' 失败 =', failed.length);
  if (failed.length) fs.writeFileSync(path.join(OUT, '_failed.json'), JSON.stringify(failed, null, 1));

  const byType = t => results.filter(r => r.gem_type === t);
  const skill = byType('skill'), support = byType('support'), spirit = byType('spirit');
  console.log('skill =', skill.length, 'support =', support.length, 'spirit =', spirit.length);

  const wrap = (t, arr) => ({
    sources: ['https://poe2db.tw/us/' + (t === 'skill' ? 'Skill_Gems' : t === 'support' ? 'Support_Gems' : 'Spirit_Gems')],
    meta: { type: t, count: arr.length, crawled_at: new Date().toISOString() },
    entries: arr
  });
  fs.writeFileSync(path.join(OUT, 'skill_gems.json'), JSON.stringify(wrap('skill', skill), null, 1));
  fs.writeFileSync(path.join(OUT, 'support_gems.json'), JSON.stringify(wrap('support', support), null, 1));
  fs.writeFileSync(path.join(OUT, 'spirit_gems.json'), JSON.stringify(wrap('spirit', spirit), null, 1));
  fs.writeFileSync(path.join(OUT, 'all_skills.json'), JSON.stringify({
    sources: ['https://poe2db.tw/us/', 'https://poe2db.tw/tw/'],
    meta: { total: results.length, skill: skill.length, support: support.length, spirit: spirit.length, crawled_at: new Date().toISOString() },
    entries: results
  }, null, 1));
  console.log('已写出 JSON ->', OUT);
})();
