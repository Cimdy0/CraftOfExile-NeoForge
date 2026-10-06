/**
 * 产出「无宝石技能」(gemless) 集合 + 补全遗漏的 2 条
 *
 * 判定规则（写在 index.md 里）：
 *   A. internal_id 以 PlayerDefault 开头      -> 角色武器基础攻击（不含宝石槽）
 *   B. internal_id 以 Bear / Wolf / Wyvern 开头 -> 变形形态技能
 *   C. internal_id 匹配 /^Summon.*Companion$/  -> 伙伴召唤
 *   D. ActiveSkillsCode 存在但 internal_id 非 XxxSupport / 且无 gem_color 且 gem_class != 'Support Gems'
 *      —— 这条只作为候选，不自动纳入（避免误吞 lineage/unique 授予技能）
 *
 * 额外补抓：Default_Attack(player_melee) / Basic_Attack(melee)
 *   —— 这两页之前被 find_extra_skills.js 的 BIG 黑名单误当"索引页"排除了
 *
 * 输出：
 *   crawled/gemless_skills.json / .md
 *   _tools/gemless_extra.json（2 条新条目，供 merge 进 all_skills.json）
 */
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');

const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design', 'refactor', 'skill-data', 'crawled');
const RAW = path.join(OUT, 'raw', 'html_gz');
const SRC = path.join(OUT, 'all_skills.json');

// ---- 从 crawl_details.js 复用 parseDetail：按名字 + 花括号配对精确切片 ----
const src = fs.readFileSync(path.join(__dirname, 'crawl_details.js'), 'utf8');
// stripTags/tablesOf/num 是纯同步小函数，按区间切片即可（避开 async 的 fetchText）
const hStart = src.indexOf('function stripTags');
const pStart = src.indexOf('function parseDetail');
// 注意 zhMap 声明带 async 前缀，必须连前缀一起定位，否则会残留一个孤立 `async`
const pEnd = Math.min(
  ...['async function zhMap', 'function zhMap'].map(k => {
    const i = src.indexOf(k);
    return i < 0 ? Infinity : i;
  })
);
if (!Number.isFinite(pEnd)) throw new Error('找不到 zhMap 边界');
if (hStart < 0 || pStart < 0 || pEnd < 0) throw new Error('无法从 crawl_details.js 定位解析器');
const sandbox = src.slice(hStart, pStart) + '\n' + src.slice(pStart, pEnd);
// 用 AsyncFunction 构造（切片里可能夹带 async 辅助函数）；返回 Promise，故整体包在 main() 里
const AsyncFunction = Object.getPrototypeOf(async function () { }).constructor;
const parseDetailPromise = new AsyncFunction(sandbox + '\nreturn parseDetail;')();

const gunzip = f => zlib.gunzipSync(fs.readFileSync(f)).toString('utf8');
const txt = s => s.replace(/<[^>]+>/g, '').replace(/&nbsp;/g, ' ').replace(/\s+/g, ' ').trim();

async function main() {
const parseDetail = await parseDetailPromise;
const doc = JSON.parse(fs.readFileSync(SRC, 'utf8'));
const E = doc.entries;

// ---- 1. 从已有条目里挑 gemless ----
const RE_PLAYER = /^PlayerDefault/i;
const RE_SHAPE = /^(Bear|Wolf|Wyvern)/i;
const RE_COMPANION = /^Summon.*Companion$/i;

function gemlessReason(e) {
  // 精神/辅助宝石一律不算 gemless（它们占宝石槽；wolf_pack 等即属此类）
  if (e.gem_type === 'spirit' || e.gem_type === 'support') return null;
  const id = e.internal_id || '';
  if (RE_PLAYER.test(id)) return 'A:character_base_attack';
  if (RE_SHAPE.test(id)) return 'B:shapeshift_form';
  if (RE_COMPANION.test(id)) return 'C:companion_summon';
  return null;
}

let picked = E.filter(e => gemlessReason(e)).map(e => ({ ...e, gemless_reason: gemlessReason(e), is_gemless: true }));

// ---- 2. 补抓被黑名单误排除的 2 条 ----
const EXTRA = [
  { href: 'Default_Attack', name: 'Default Attack' },
  { href: 'Basic_Attack', name: 'Basic Attack' },
];
const extras = [];
for (const it of EXTRA) {
  const f = path.join(RAW, it.href + '.html.gz');
  if (!fs.existsSync(f)) { console.log('  缺缓存，跳过', it.href); continue; }
  const html = gunzip(f);
  const code = (html.match(/>ActiveSkillsCode<\/td><td>([^<]*)/) || [])[1] || null;
  const d = parseDetail(html, {
    href: it.href, name: it.name, color: null, icon: null, filters: '',
    gem_type: 'skill', seenIn: ['gemless_probe'], is_meta: false, is_lineage: false,
    tier: null, art: null, page: it.href,
  });
  d.gemless_reason = 'A:character_base_attack';
  d.is_gemless = true;
  d.active_skills_code = code;
  if (!d.internal_id) d.internal_id = code;
  // 中文名/描述：若尚未补抓则留 null，由 merge_zh 统一回填
  extras.push(d);
  console.log('  补抓', it.href, 'internal_id=' + d.internal_id, 'code=' + code, 'levels=' + (d.level_table.rows || []).length);
}

const all = [...picked, ...extras];
const byId = new Map();
for (const e of all) if (!byId.has(e.id)) byId.set(e.id, e);
const entries = [...byId.values()].sort((a, b) => a.id.localeCompare(b.id));

const reasons = {};
for (const e of entries) reasons[e.gemless_reason] = (reasons[e.gemless_reason] || 0) + 1;

fs.writeFileSync(path.join(OUT, 'gemless_skills.json'), JSON.stringify({
  sources: ['https://poe2db.tw/us/', 'https://poe2db.tw/tw/', 'https://poe2db.tw/us/Default_Attack'],
  meta: { type: 'gemless', count: entries.length, by_reason: reasons, generated_at: new Date().toISOString() },
  entries,
}, null, 1));

fs.writeFileSync(path.join(__dirname, 'gemless_extra.json'), JSON.stringify(extras, null, 1), 'utf8');

console.log('\ngemless 总数 =', entries.length);
console.log('按原因 =', JSON.stringify(reasons));
console.log('\n清单：');
for (const e of entries) {
  console.log('  ' + String(e.gemless_reason).padEnd(26) + e.id.padEnd(26) +
    String(e.internal_id || '').padEnd(34) + (e.name_zh || '-'));
}
console.log('\n已写出 gemless_skills.json 与 _tools/gemless_extra.json');
}

main().catch(e => { console.error(e); process.exit(1); });
