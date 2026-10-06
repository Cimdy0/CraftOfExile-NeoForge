// 正确版发现：扫描页面内所有 <a class="gem_xxx" href="/us/YYY">ZZZ</a>
import fs from 'node:fs';

const RAW = 'e:/McModding/Modding/CraftOfExile-NeoForge/design/refactor/skill-data/crawled/raw/probe';

function extractAnchors(html) {
  const out = [];
  for (const m of html.matchAll(/<a\s+class="(gem_[a-z]+)"([^>]*)href="\/us\/([^"]+)"([^>]*)>([\s\S]*?)<\/a>/g)) {
    out.push({ color: m[1].replace('gem_', ''), href: m[3], text: m[5].replace(/<[^>]+>/g, '').trim() });
  }
  // gemitem = 物品/词缀授予的宝石
  for (const m of html.matchAll(/<a\s+class="gemitem"([^>]*)href="\/us\/([^"]+)"([^>]*)>([\s\S]*?)<\/a>/g)) {
    out.push({ color: null, href: m[2], text: m[4].replace(/<[^>]+>/g, '').trim() });
  }
  return out;
}

const pages = {};
for (const f of fs.readdirSync(RAW)) {
  const m = f.match(/^us__(\w+)\.html$/);
  if (!m) continue;
  pages[m[1]] = extractAnchors(fs.readFileSync(`${RAW}/${f}`, 'utf8'));
}

for (const [n, a] of Object.entries(pages)) {
  const u = new Set(a.map(x => x.href));
  console.log(`${n.padEnd(16)} anchors=${String(a.length).padStart(5)} distinct=${u.size}`);
}

const union = new Map();
for (const a of Object.values(pages)) {
  for (const x of a) {
    if (!union.has(x.href)) union.set(x.href, { ...x, seenIn: [] });
  }
}
console.log('UNION distinct:', union.size);
const cc = {};
for (const x of union.values()) cc[x.color] = (cc[x.color] || 0) + 1;
console.log('colors:', JSON.stringify(cc));
