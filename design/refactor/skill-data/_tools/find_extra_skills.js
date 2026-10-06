/**
 * 从 Minions / Shapeshift / Default_Attack / Keywords / Support_Gem_Categories / Gems
 * 抽取全部 /us/X 链接，与已爬取的 1046 条做差集，再判定哪些是"技能页"
 */
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data/crawled');
const PD = path.join(OUT, 'raw', 'index_pages');
const RAW = path.join(OUT, 'raw', 'html_gz');
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36';
const H = { 'User-Agent': UA, 'Referer': 'https://poe2db.tw/us/' };
const sleep = ms => new Promise(r => setTimeout(r, ms));

const E = JSON.parse(fs.readFileSync(path.join(OUT, 'all_skills.json'), 'utf8')).entries;
const crawledHrefs = new Set(E.map(e => (e.source_url_us || '').split('/').pop()));
const crawledIds = new Set(E.map(e => e.id));

// 非技能页黑名单（词缀/物品/怪物/地图等页面特征）
const BLACK = /(Modifiers?|_Mods?|List_of|Unique|Basetype|Base_Type|Item_Class|Monster|Map|Boss|Passive|Atlas|League|Version|Craft|Vendor|Recipe|Quest|Divination|Card|Essence|Rune|Currency|Fragment|Breach|Delirium|Expedition|Trial|Ultimatum|Ritual|Incursion|Betrayal|Heist|Blighted|BlightedMap|Sentinel|Kalguur|Kirac|Sanctum|Tattoo|Omen|Idol|Wand|Bow|Staff|Axe|Sword|Mace|Claw|Dagger|Flail|Spear|Quarterstaff|Crossbow|Armour|Helmet|Gloves|Boots|Shield|Belt|Ring|Amulet|Flask|Charm|Jewel|Sceptre|Focus|Buckler)/i;
// 明显是物品/怪物/其它大类的目录页
const BIG = new Set(['Gems', 'Skill_Gems', 'Support_Gems', 'Spirit_Gems', 'Meta_Gems', 'Lineage_Supports',
  'Minions', 'Shapeshift', 'Default_Attack', 'Keywords', 'Support_Gem_Categories',
  'Uncut_Skill_Gem', 'Uncut_Support_Gem', 'Uncut_Spirit_Gem']);

const cands = new Set();
for (const f of fs.readdirSync(PD)) {
  const t = fs.readFileSync(path.join(PD, f), 'utf8');
  for (const m of t.matchAll(/href="\/us\/([^"]+)"/g)) {
    const h = m[1];
    if (/%/.test(h)) continue;            // URL 编码的怪名
    if (BIG.has(h)) continue;
    if (BLACK.test(h)) continue;
    cands.add(h);
  }
}
console.log('候选链接（已爬页面上去重后）=', cands.size);
const diff = [...cands].filter(h => !crawledHrefs.has(h));
console.log('与已爬取 1046 条的差集 =', diff.length);

// 判定：抓取页面，看是否含技能特征（property 块 / 元数据 Type 行 / GemTags）
async function classify(h) {
  const f = path.join(RAW, h + '.html.gz');
  let t = null;
  if (fs.existsSync(f)) t = zlib.gunzipSync(fs.readFileSync(f)).toString('utf8');
  else {
    try {
      const r = await fetch('https://poe2db.tw/us/' + h, { headers: H });
      if (r.ok) { t = await r.text(); fs.writeFileSync(f, zlib.gzipSync(Buffer.from(t, 'utf8'))); }
    } catch (e) { }
    await sleep(200);
  }
  if (!t) return null;
  const hasProp = /<div class="property"/.test(t);
  const typeM = t.match(/>Type<\/td><td>([^<]{0,200})/);
  const classM = t.match(/>Class\b/);
  const gemTags = /class="[^"]*GemTags[^"]*"/.test(t);
  const title = ((t.match(/<title>([^<]*)<\/title>/) || [])[1] || '').replace(' - PoE2DB, Path of Exile Wiki us', '');
  const isSkill = hasProp || !!typeM || gemTags;
  return { href: h, title, isSkill, hasProp, hasType: !!typeM, hasClass: !!classM, gemTags, type: typeM ? typeM[1].slice(0, 120) : null, len: t.length };
}

(async () => {
  const out = [];
  for (let i = 0; i < diff.length; i += 6) {
    const batch = diff.slice(i, i + 6);
    const rs = await Promise.all(batch.map(classify));
    rs.forEach((r, j) => { if (r) out.push(r); });
    if (i % 60 === 0) console.log('  已判定', Math.min(i + 6, diff.length), '/', diff.length);
    await sleep(120);
  }
  const skills = out.filter(r => r.isSkill);
  console.log('\n判定完成：总', out.length, ' 其中技能页 =', skills.length);
  console.log('\n=== 新增技能页（前 60）===');
  skills.slice(0, 60).forEach(r => console.log('  ' + r.href.padEnd(34) + (r.title || '').slice(0, 38).padEnd(40) +
    'prop=' + (r.hasProp ? 'Y' : 'n') + ' type=' + (r.hasType ? 'Y' : 'n') + ' tags=' + (r.gemTags ? 'Y' : 'n')));

  fs.writeFileSync(path.join(ROOT, 'design/refactor/skill-data/_tools/extra_skill_candidates.json'),
    JSON.stringify(skills.map(s => s.href), null, 1));
  console.log('\n已写出 extra_skill_candidates.json（' + skills.length + ' 条）');
})();
