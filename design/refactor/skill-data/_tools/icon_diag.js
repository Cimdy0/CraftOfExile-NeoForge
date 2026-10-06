const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data/crawled');
const m = JSON.parse(fs.readFileSync(path.join(OUT, 'icons/manifest.json'), 'utf8'));
const miss = m.entries.filter(e => e.status === 'missing');
console.log('missing =', miss.length);
console.log('其中 url 为空 =', miss.filter(e => !e.url).length);
console.log('样例:', JSON.stringify(miss.slice(0, 8), null, 1));

// 详情页里有没有图标可提取？
const zlib = require('zlib');
const RAW = path.join(OUT, 'raw', 'html_gz');
const e0 = miss.find(x => !x.url);
if (e0) {
  const v3 = JSON.parse(fs.readFileSync(path.join(ROOT, 'design/refactor/skill-data/_tools/discovered_v3.json'), 'utf8'));
  const rec = v3.find(v => v.id === e0.id || v.href.toLowerCase().replace(/[^a-z0-9]/g, '') === e0.id.replace(/[^a-z0-9]/g, ''));
  console.log('\n对应 href =', rec && rec.href);
  if (rec) {
    const f = path.join(RAW, rec.href + '.html.gz');
    if (fs.existsSync(f)) {
      const h = zlib.gunzipSync(fs.readFileSync(f)).toString('utf8');
      const imgs = [...new Set([...h.matchAll(/src="(https:\/\/cdn\.poe2db\.tw\/image\/[^"]+)"/g)].map(x => x[1]))];
      console.log('详情页图片 URL（前 8）:');
      imgs.slice(0, 8).forEach(u => console.log('  ' + u));
      const skillIcon = imgs.filter(u => /SkillIcons/i.test(u));
      console.log('其中 SkillIcons:', skillIcon.join(', ') || '(无)');
    } else console.log('raw 不存在', f);
  }
}
