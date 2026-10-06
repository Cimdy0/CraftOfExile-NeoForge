// 补下 2 个权威层报告缺失的图标：chance_to_freeze、styrns_anthem
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data/crawled');
const RAW = path.join(OUT, 'raw', 'html_gz');
const DL = path.join(OUT, 'icons/downloaded_webp');
const ITEM = path.join(ROOT, 'src/main/resources/assets/coe/textures/item/gem');
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36';
fs.mkdirSync(DL, { recursive: true });

const E = JSON.parse(fs.readFileSync(path.join(OUT, 'all_skills.json'), 'utf8')).entries;

(async () => {
  for (const id of ['chance_to_freeze', 'styrns_anthem']) {
    const e = E.find(x => x.id === id);
    const href = e ? (e.source_url_us || '').split('/').pop() : null;
    console.log('\n=== ' + id + ' | href=' + href + ' | icon_url=' + (e ? e.icon_url : '-'));
    const f = path.join(RAW, href + '.html.gz');
    if (fs.existsSync(f)) {
      const h = zlib.gunzipSync(fs.readFileSync(f)).toString('utf8');
      const imgs = [...new Set([...h.matchAll(/src="(https:\/\/cdn\.poe2db\.tw\/image\/[^"]+)"/gi)].map(m => m[1]))]
        .filter(u => /skillicons/i.test(u));
      console.log('  详情页 skillicons 图片 ' + imgs.length + ' 条:');
      imgs.slice(0, 5).forEach(u => console.log('    ' + u));
      if (imgs.length) {
        const dst = path.join(DL, id + '.webp');
        try {
          const r = await fetch(imgs[0], { headers: { 'User-Agent': UA, 'Referer': 'https://poe2db.tw/' } });
          if (r.ok) {
            fs.writeFileSync(dst, Buffer.from(await r.arrayBuffer()));
            console.log('  ✅ 已下载 -> ' + dst);
          } else console.log('  ❌ HTTP ' + r.status);
        } catch (err) { console.log('  ❌ ' + err.message); }
      }
    } else console.log('  无 gzip 缓存');
  }
})();
