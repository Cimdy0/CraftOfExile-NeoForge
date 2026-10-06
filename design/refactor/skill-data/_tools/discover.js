// 发现阶段：union 所有列表页的 gem href，输出 _tools/discovered.json
import fs from 'node:fs';

const RAW = 'e:/McModding/Modding/CraftOfExile-NeoForge/design/refactor/skill-data/crawled/raw/probe';

function tableChunks(html) {
  const chunks = [];
  const starts = [...html.matchAll(/<table[^>]*>/g)];
  const ends = [...html.matchAll(/<\/table>/g)];
  for (const s of starts) {
    const e = ends.find(x => x.index > s.index)?.index ?? html.length;
    chunks.push({ start: s.index, end: e, html: html.slice(s.index, e) });
  }
  return chunks;
}

function extract(html) {
  const out = [];
  for (const ch of tableChunks(html)) {
    for (const p of ch.html.split(/<tr(?=[ >])/).slice(1)) {
      if (!/href="\/us\/[A-Za-z0-9_%'\-\.]+"/.test(p)) continue;
      const attrs = p.slice(0, p.indexOf('>') + 1);
      const href = p.match(/href="\/us\/([^"]+)"/)?.[1];
      const color = p.match(/class="(gem_[a-z]+)"/)?.[1];
      if (!href || !color) continue;
      const name = p.match(/href="\/us\/[^"]+"[^>]*>([^<]+)<\/a>/)?.[1]?.trim() || null;
      const icon = p.match(/<img[^>]*src="([^"]+)"/)?.[1] || null;
      const filters = attrs.match(/data-filters="([^"]*)"/)?.[1] || null;
      out.push({ href, name, color: color.replace('gem_', ''), icon, filters, tableStart: ch.start });
    }
  }
  return out;
}

const pages = {};
for (const f of fs.readdirSync(RAW)) {
  const m = f.match(/^us__(\w+)\.html$/);
  if (!m) continue;
  pages[m[1]] = fs.readFileSync(`${RAW}/${f}`, 'utf8');
}

const union = new Map();
const perPage = {};
for (const [name, html] of Object.entries(pages)) {
  const rows = extract(html);
  perPage[name] = rows.length;
  for (const r of rows) {
    if (!union.has(r.href)) union.set(r.href, { href: r.href, name: r.name, color: r.color, icon: r.icon, filters: r.filters, seenIn: [] });
    const e = union.get(r.href);
    if (!e.seenIn.includes(name)) e.seenIn.push(name);
    if (!e.name && r.name) e.name = r.name;
    if (!e.icon && r.icon) e.icon = r.icon;
    if (!e.filters && r.filters) e.filters = r.filters;
  }
}

console.log('per-page row counts:', JSON.stringify(perPage));
console.log('UNION distinct hrefs:', union.size);

const byColor = {};
for (const e of union.values()) byColor[e.color] = (byColor[e.color] || 0) + 1;
console.log('by color:', JSON.stringify(byColor));

const combos = {};
for (const e of union.values()) {
  const k = e.seenIn.slice().sort().join('+');
  combos[k] = (combos[k] || 0) + 1;
}
console.log('by page-combo:', JSON.stringify(combos, null, 1));

const arr = [...union.values()].sort((a, b) => a.href.localeCompare(b.href));
fs.writeFileSync('e:/McModding/Modding/CraftOfExile-NeoForge/design/refactor/skill-data/_tools/discovered.json',
  JSON.stringify(arr, null, 1), 'utf8');
console.log('written _tools/discovered.json');
console.log(arr.map(e => `${e.color.padEnd(6)} ${e.href}`).join('\n'));
