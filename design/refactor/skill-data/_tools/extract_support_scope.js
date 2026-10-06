/**
 * 从已缓存的详情页 gzip 中提取「辅助宝石的可辅助技能类型」（F6 的关键字段）
 * poe2db 详情页形如： Supported Gem /269 Allow Type: Attack, CrossbowAmmoSkill, Damage
 * 同时提取「被辅助技能列表」（Supported Skill Gems 区块）
 * 不重新联网，只读 raw/html_gz
 */
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data/crawled');
const RAW = path.join(OUT, 'raw', 'html_gz');

const E = JSON.parse(fs.readFileSync(path.join(OUT, 'all_skills.json'), 'utf8')).entries;
const seen = JSON.parse(fs.readFileSync(path.join(ROOT, 'design/refactor/skill-data/_tools/discovered_v3.json'), 'utf8'));
const hrefById = new Map();
for (const e of E) {
  const h = (e.source_url_us || '').split('/').pop();
  if (h) hrefById.set(e.id, h);
}
// 反查：href -> id
const idByHref = new Map();
for (const s of seen) idByHref.set(s.href, s.href);
for (const e of E) {
  const h = (e.source_url_us || '').split('/').pop();
  idByHref.set(h, e.id);
}

function strip(s) {
  return s.replace(/<span class="ndash">[^<]*<\/span>/g, '-').replace(/<[^>]+>/g, ' ')
    .replace(/&amp;/g, '&').replace(/&quot;/g, '"').replace(/&#39;/g, "'")
    .replace(/\s+/g, ' ').trim();
}

let allowHit = 0, supportedListHit = 0, noCache = 0;
const result = {};

for (const e of E) {
  const href = (e.source_url_us || '').split('/').pop();
  if (!href) continue;
  const f = path.join(RAW, href + '.html.gz');
  if (!fs.existsSync(f)) { noCache++; continue; }
  const h = zlib.gunzipSync(fs.readFileSync(f)).toString('utf8');

  // 1) Allow Type 列表
  const allowM = h.match(/Allow\s*Type:\s*([^<\n]{0,600})/i);
  let allowTypes = null;
  if (allowM) {
    allowTypes = allowM[1].split(',').map(s => s.trim()).filter(Boolean);
    allowHit++;
  }

  // 2) Supported Gem 计数（形如 "Supported Gem /269"）
  const cntM = h.match(/Supported\s+Gem\s*\/\s*(\d+)/i);
  const supportedGemCount = cntM ? parseInt(cntM[1], 10) : null;

  // 3) 被辅助的技能名列表：从 "Tier | Gems" 或 Skill_Gems 区块里抓（取表格中 gem_ 链接）
  const supportedGems = [...new Set(
    [...h.matchAll(/<a[^>]*class="gem_(?:red|green|blue)"[^>]*href="\/us\/([^"]+)"[^>]*>([^<]+)<\/a>/g)]
      .map(m => m[2].trim())
  )];

  result[e.id] = {
    gem_type: e.gem_type,
    allow_types: allowTypes,
    supported_gem_count: supportedGemCount,
    mentioned_gems_sample: supportedGems.slice(0, 40)
  };
}

const entries = Object.entries(result);
const supportsWithAllow = entries.filter(([, v]) => v.allow_types && v.allow_types.length);
console.log('读取缓存条目 =', entries.length, ' 无缓存 =', noCache);
console.log('含 Allow Type =', supportsWithAllow.length);
console.log('含 Supported Gem 计数 =', entries.filter(([, v]) => v.supported_gem_count != null).length);

console.log('\n--- Allow Type 值域（top 30）---');
const freq = {};
supportsWithAllow.forEach(([, v]) => v.allow_types.forEach(t => freq[t] = (freq[t] || 0) + 1));
Object.entries(freq).sort((a, b) => b[1] - a[1]).slice(0, 30)
  .forEach(([k, v]) => console.log('  ' + String(v).padStart(4) + '  ' + k));

console.log('\n--- 样例（前 8 条辅助）---');
supportsWithAllow.slice(0, 8).forEach(([id, v]) =>
  console.log('  ' + id.padEnd(26) + 'n=' + String(v.supported_gem_count).padEnd(5) + 'allow=' + v.allow_types.slice(0, 8).join(',')));

fs.writeFileSync(path.join(OUT, 'support_scope.json'), JSON.stringify({
  note: '辅助宝石的可辅助技能类型（Allow Type）+ 可辅助宝石数量。源自 poe2db 详情页，只读本地 gzip 缓存，未重新联网。',
  generated_at: new Date().toISOString(),
  total: entries.length, with_allow_types: supportsWithAllow.length,
  entries: result
}, null, 1));
console.log('\n已写出 support_scope.json');
