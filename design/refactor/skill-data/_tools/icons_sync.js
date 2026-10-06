/**
 * 图标补齐：
 *  - source_available：把 poe2_assets/gems|icons 的 PascalCase PNG 复制为 textures/item/gem/<snake>.png
 *  - missing：从 poe2db CDN 下载 webp（存到 crawled/icons/downloaded_webp/，供后续转 png）
 * 用法：node icons_sync.js [limit]
 */
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data/crawled');
const ITEM = path.join(ROOT, 'src/main/resources/assets/coe/textures/item/gem');
const SRC1 = path.join(ROOT, 'poe2_assets/gems');
const SRC2 = path.join(ROOT, 'poe2_assets/icons');
const DL = path.join(OUT, 'icons/downloaded_webp');
fs.mkdirSync(DL, { recursive: true });

const LIMIT = process.argv[2] ? parseInt(process.argv[2], 10) : Infinity;
const manifest = JSON.parse(fs.readFileSync(path.join(OUT, 'icons/manifest.json'), 'utf8'));
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36';
const sleep = ms => new Promise(r => setTimeout(r, ms));

let copied = 0, downloaded = 0, failed = 0;
const toCopy = manifest.entries.filter(e => e.status === 'source_available').slice(0, LIMIT);
const toDl = manifest.entries.filter(e => e.status === 'missing').slice(0, LIMIT);

console.log('可本地复制 =', toCopy.length, ' 需下载 =', toDl.length);

// 1) 本地复制
for (const e of toCopy) {
  const src = e.source.find(s => fs.existsSync(path.join(SRC1, s))) ? SRC1 : SRC2;
  const name = e.source.find(s => fs.existsSync(path.join(src, s)));
  if (!name) continue;
  const dst = path.join(ITEM, e.id + '.png');
  if (fs.existsSync(dst)) continue;
  fs.copyFileSync(path.join(src, name), dst);
  copied++;
}
console.log('已复制到 textures/item/gem =', copied);

// 2) 下载缺失（webp 原始）
(async () => {
  for (let i = 0; i < toDl.length; i += 6) {
    await Promise.all(toDl.slice(i, i + 6).map(async e => {
      if (!e.url) return;
      const dst = path.join(DL, e.id + '.webp');
      if (fs.existsSync(dst)) { downloaded++; return; }
      for (let k = 0; k < 2; k++) {
        try {
          const c = new AbortController(); const to = setTimeout(() => c.abort(), 20000);
          const r = await fetch(e.url, { headers: { 'User-Agent': UA, 'Referer': 'https://poe2db.tw/' }, signal: c.signal });
          clearTimeout(to);
          if (!r.ok) throw new Error('HTTP ' + r.status);
          const b = Buffer.from(await r.arrayBuffer());
          fs.writeFileSync(dst, b);
          downloaded++; return;
        } catch (err) { await sleep(600); }
      }
      failed++;
    }));
    await sleep(150);
  }
  console.log('已下载 webp =', downloaded, ' 失败 =', failed);
  console.log('webp 存于', DL);
  console.log('提示：webp 需转 png 后才能放入 textures/item/gem（Mojang 只认 png）');
})();
