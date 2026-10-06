// 看 Skill_Gems 页里"有 data-filters 但没有 gem_ 颜色 class 锚点"的行长什么样
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const RAW = path.join(ROOT, 'design/refactor/skill-data/crawled/raw/probe');
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36';

(async () => {
  let t;
  const local = path.join(RAW, 'us__Skill_Gems.html');
  if (fs.existsSync(local)) { t = fs.readFileSync(local, 'utf8'); console.log('(用本地缓存)'); }
  else {
    const r = await fetch('https://poe2db.tw/us/Skill_Gems', { headers: { 'User-Agent': UA, 'Referer': 'https://poe2db.tw/us/' } });
    t = await r.text();
  }
  console.log('len', t.length);

  const rows = [...t.matchAll(/<tr[^>]*data-filters="[^"]*"[\s\S]*?<\/tr>/g)].map(m => m[0]);
  console.log('data-filters 行数 =', rows.length);

  let withGem = 0, withoutGem = 0;
  const samples = [];
  for (const r of rows) {
    if (/class="[^"]*gem_(red|green|blue)/.test(r)) withGem++;
    else { withoutGem++; if (samples.length < 4) samples.push(r); }
  }
  console.log('含 gem_ 颜色 class 的行 =', withGem, ' 不含 =', withoutGem);

  samples.forEach((s, i) => {
    console.log('\n===== 不含颜色 class 的行 #' + i + ' =====');
    console.log(s.slice(0, 1100).replace(/\s+/g, ' '));
  });
})();
