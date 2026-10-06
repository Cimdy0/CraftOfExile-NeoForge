/**
 * 用元数据表精确分类 137 个候选：技能 / 暗金物品 / 升华天赋 / 其它
 * 判据：ItemType（Metadata/Items/Gem/... = 宝石）、Class、是否有 "Level:" 属性块
 */
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const RAW = path.join(ROOT, 'design/refactor/skill-data/crawled/raw/html_gz');
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36';
const H = { 'User-Agent': UA, 'Referer': 'https://poe2db.tw/us/' };
const sleep = ms => new Promise(r => setTimeout(r, ms));
const cands = JSON.parse(fs.readFileSync(path.join(ROOT, 'design/refactor/skill-data/_tools/extra_skill_candidates.json'), 'utf8'))
  .filter(h => !/^DNT[-_]/i.test(h) && !/playtest/i.test(h));

function strip(s) { return s.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim(); }

async function meta(h) {
  const f = path.join(RAW, h + '.html.gz');
  let t = null;
  if (fs.existsSync(f)) t = zlib.gunzipSync(fs.readFileSync(f)).toString('utf8');
  else {
    try {
      const r = await fetch('https://poe2db.tw/us/' + h, { headers: H });
      if (r.ok) { t = await r.text(); fs.writeFileSync(f, zlib.gzipSync(Buffer.from(t, 'utf8'))); }
    } catch (e) { }
    await sleep(180);
  }
  if (!t) return null;
  const out = { href: h };
  const tableM = t.match(/<table[^>]*>[\s\S]*?Show Full Descriptions[\s\S]*?<\/table>/);
  if (tableM) {
    for (const m of tableM[0].matchAll(/<tr[^>]*>([\s\S]*?)<\/tr>/g)) {
      const cels = [...m[1].matchAll(/<td[^>]*>([\s\S]*?)<\/td>/g)].map(c => strip(c[1]));
      if (cels.length >= 2) out[cels[0].replace(/\s+/g, ' ')] = cels[1];
    }
  }
  out._title = ((t.match(/<title>([^<]*)<\/title>/) || [])[1] || '').replace(' - PoE2DB, Path of Exile Wiki us', '');
  out._hasLevelProp = /<div class="property">\s*Level:|class="property">Level:/i.test(t);
  out._hasLevelEffect = /Level\s*Effect/i.test(t);
  out._gemTags = /class="[^"]*GemTags[^"]*"/.test(t);
  out._len = t.length;
  return out;
}

(async () => {
  const res = [];
  for (let i = 0; i < cands.length; i += 6) {
    const rs = await Promise.all(cands.slice(i, i + 6).map(meta));
    rs.forEach(r => { if (r) res.push(r); });
    await sleep(100);
  }
  const cls = {};
  for (const r of res) {
    const itemType = r.ItemType || '';
    const klass = (r.Class || '').trim();
    let kind;
    if (/^Metadata\/Items\/Gem\//i.test(itemType)) kind = 'GEM';
    else if (/^Metadata\/Items\//i.test(itemType)) kind = 'ITEM';
    else if (/_hasLevelProp|_hasLevelEffect/.test('') || r._hasLevelProp || r._hasLevelEffect) kind = 'SKILL_NOGEM';
    else if (/Gem|Support|Skill/i.test(klass)) kind = 'GEM';
    else kind = 'OTHER';
    r._kind = kind;
    cls[kind] = (cls[kind] || 0) + 1;
  }
  console.log('候选（去 DNT/Playtest）=', res.length);
  console.log('分类:', JSON.stringify(cls));

  const show = k => res.filter(r => r._kind === k);
  console.log('\n=== GEM（宝石类，应纳入权威集）' + show('GEM').length + ' ===');
  show('GEM').forEach(r => console.log('  ' + r.href.padEnd(32) + (r.Class || '-').slice(0, 20).padEnd(22) + 'Id=' + (r.Id || '-')));
  console.log('\n=== SKILL_NOGEM（非宝石技能，应纳入）' + show('SKILL_NOGEM').length + ' ===');
  show('SKILL_NOGEM').slice(0, 40).forEach(r => console.log('  ' + r.href.padEnd(32) + (r._title || '').slice(0, 34).padEnd(36) + 'type=' + (r.Type || '-').slice(0, 60)));
  console.log('\n=== ITEM（暗金/物品，不纳入）' + show('ITEM').length + ' ===');
  show('ITEM').slice(0, 15).forEach(r => console.log('  ' + r.href.padEnd(32) + (r.Class || '-').slice(0, 40)));
  console.log('\n=== OTHER（升华天赋/职业/页面等）' + show('OTHER').length + ' ===');
  show('OTHER').slice(0, 25).forEach(r => console.log('  ' + r.href.padEnd(32) + (r._title || '').slice(0, 40)));

  fs.writeFileSync(path.join(ROOT, 'design/refactor/skill-data/_tools/extra_classified.json'),
    JSON.stringify(res.map(r => ({ href: r.href, kind: r._kind, title: r._title, Class: r.Class, ItemType: r.ItemType, Id: r.Id, Type: r.Type })), null, 1));
  console.log('\n已写出 extra_classified.json');
})();
