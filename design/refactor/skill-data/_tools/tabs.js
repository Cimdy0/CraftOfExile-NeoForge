// 按 tab-pane 切片统计 Gems 页面，确定分类体系
import fs from 'node:fs';
const file = process.argv[2] || 'e:/McModding/Modding/CraftOfExile-NeoForge/design/refactor/skill-data/crawled/raw/probe/us__Gems.html';
const html = fs.readFileSync(file, 'utf8');

// tab 名列（从 nav-link 拿）
const tabs = [...html.matchAll(/<a class="nav-link"[^>]*href="#([^"]+)"[^>]*>([\s\S]*?)<\/a>/g)]
  .map(m => ({ id: m[1], label: m[2].replace(/<[^>]+>/g, '').trim() }));
console.log('tabs:', JSON.stringify(tabs));

const paneStarts = tabs.map(t => {
  const re = new RegExp(`<div id="${t.id}" class="tab-pane`);
  const m = re.exec(html);
  return { ...t, start: m ? m.index : -1 };
}).filter(t => t.start >= 0).sort((a, b) => a.start - b.start);

const all = new Map();
for (let i = 0; i < paneStarts.length; i++) {
  const p = paneStarts[i];
  const end = i + 1 < paneStarts.length ? paneStarts[i + 1].start : html.length;
  const seg = html.slice(p.start, end);
  const found = new Map();
  for (const m of seg.matchAll(/<a\s+class="(gem_[a-z]+|gemitem)"[^>]*href="\/us\/([^"]+)"[^>]*>([\s\S]*?)<\/a>/g)) {
    found.set(m[2], { cls: m[1], text: m[3].replace(/<[^>]+>/g, '').trim() });
  }
  console.log(`### ${p.label.padEnd(34)} hrefs=${found.size}`);
  if (process.env.DUMP === p.id) {
    for (const [k, v] of found) console.log(`   ${v.cls.padEnd(10)} ${k}  ::  ${v.text}`);
  }
  for (const [k, v] of found) {
    if (!all.has(k)) all.set(k, { ...v, tabs: [] });
    if (!all.get(k).tabs.includes(p.id)) all.get(k).tabs.push(p.id);
  }
}
console.log('=== UNION:', all.size);
if (process.env.LIST) {
  const f = JSON.parse(process.env.LIST);
  for (const [k, v] of all) {
    const inSupport = v.tabs.includes('SupportGems') || v.tabs.includes('UnknownSupport');
    const inSkill = v.tabs.includes('SkillGems') || v.tabs.includes('UnknownActive');
    const inSpirit = v.tabs.includes('SpiritGems');
    const inItem = v.tabs.includes('ItemSkillGems');
    const inMeta = v.tabs.includes('MetaSkillGem');
    if (f.want === 'notskill' && (inSkill || inSpirit || inSupport || inItem || inMeta)) continue;
    console.log(`${v.cls.padEnd(10)} ${k.padEnd(40)} tabs=${v.tabs.join(',')}`);
  }
}
