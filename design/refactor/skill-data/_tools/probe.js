// 探测 poe2db.tw 页面结构 —— 拉取样例页并存到 raw/probe/
import fs from 'node:fs';
import path from 'node:path';

const OUT = 'e:/McModding/Modding/CraftOfExile-NeoForge/design/refactor/skill-data/crawled/raw/probe';
fs.mkdirSync(OUT, { recursive: true });

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36';

async function get(url) {
  const res = await fetch(url, {
    headers: {
      'User-Agent': UA,
      'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
      'Accept-Language': 'en-US,en;q=0.9',
      'Referer': 'https://poe2db.tw/us/',
    },
  });
  return { status: res.status, body: res.ok ? await res.text() : '' };
}

const targets = process.argv.slice(2);

for (const t of targets) {
  const [dir, page] = t.split(':');
  const url = `https://poe2db.tw/${dir}/${page}`;
  const r = await get(url);
  const file = path.join(OUT, `${dir}__${page}.html`);
  fs.writeFileSync(file, r.body, 'utf8');
  console.log(`${url} -> ${r.status} ${r.body.length} bytes -> ${file}`);
}
