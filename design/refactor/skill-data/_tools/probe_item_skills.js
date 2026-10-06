/**
 * 探查承载「物品技能 / 升华技能 / 固有技能」的页面
 * 目标：找到 Item Skill Gems / Ascendancy Skills / Unknown Active·Support 等清单页
 */
const fs = require('fs');
const path = require('path');
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36';
const H = { 'User-Agent': UA, 'Referer': 'https://poe2db.tw/us/' };
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const PD = path.join(ROOT, 'design/refactor/skill-data/crawled/raw/index_pages');
const sleep = ms => new Promise(r => setTimeout(r, ms));

function strip(s) { return s.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim(); }
function rowsOf(html) {
  const out = [];
  for (const seg of html.split(/<tr\s+data-filters="/).slice(1)) {
    const q = seg.indexOf('"'); if (q < 0) continue;
    const body = seg.slice(q + 1);
    const m = body.match(/href="\/us\/([^"]+)"/); if (!m) continue;
    out.push(m[1]);
  }
  return out;
}

// A) 先看 Gems 页里 ItemSkillGem tab 附近是什么结构
(async () => {
  const g = fs.readFileSync(path.join(PD, 'Gems.html'), 'utf8');
  for (const key of ['ItemSkillGem', 'Item Skill Gems Summary', 'Unknown Support', 'Unknown Active']) {
    const i = g.indexOf(key);
    console.log('=== Gems 页 "' + key + '" @' + i + ' ===');
    if (i > 0) {
      console.log(strip(g.slice(i - 120, i + 700)).slice(0, 700));
      // 该锚点之后 4000 字符内有多少 data-filters 行
      const seg = g.slice(i, i + 200000);
      console.log('  该区段去后 data-filters 行数 =', (seg.split('data-filters="').length - 1));
    }
    console.log('');
  }

  // B) 探测候选清单页
  const CAND = ['Item_Skill_Gems', 'Item_Skills', 'Ascendancy', 'Ascendancy_Skills', 'Skills',
    'Monster_Skills', 'Granted_Skills', 'Unique_Item_Skills', 'Shapeshift', 'Minions',
    'Default_Attack', 'Keywords', 'Weapon_Skills', 'Uncut_Skill_Gem', 'Uncut_Support_Gem',
    'Uncut_Spirit_Gem', 'Support_Gem_Categories', 'Gem_Tags'];
  console.log('=== 候选页探测 ===');
  for (const c of CAND) {
    let t = null;
    const f = path.join(PD, c + '.html');
    if (fs.existsSync(f)) t = fs.readFileSync(f, 'utf8');
    else {
      try {
        const r = await fetch('https://poe2db.tw/us/' + c, { headers: H });
        if (r.ok && r.status !== 404) { t = await r.text(); fs.writeFileSync(f, t); }
      } catch (e) { }
      await sleep(250);
    }
    if (!t) { console.log('  ' + c.padEnd(26) + '404/无'); continue; }
    const rows = rowsOf(t);
    const title = (t.match(/<title>([^<]*)<\/title>/) || [])[1] || '';
    console.log('  ' + c.padEnd(26) + 'len=' + String(t.length).padStart(8) +
      ' rows=' + String(rows.length).padStart(5) + '  ' + title.slice(0, 45));
  }
})();
