// 探测 poe2db 的搜索接口，用于裁定命名歧义
const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36';
const H = { 'User-Agent': UA, 'Referer': 'https://poe2db.tw/us/' };

const CAND = [
  'https://poe2db.tw/us/search?q=Added+Fire+Damage',
  'https://poe2db.tw/us/Full_Search?q=Added+Fire+Damage',
  'https://poe2db.tw/us/Search?q=Added%20Fire%20Damage',
  'https://poe2db.tw/search?q=Added+Fire+Damage',
  'https://poe2db.tw/us/?q=Added+Fire+Damage',
];

(async () => {
  for (const u of CAND) {
    try {
      const r = await fetch(u, { headers: H, redirect: 'follow' });
      const t = r.ok ? await r.text() : '';
      console.log(u.padEnd(60), r.status, 'len=' + t.length, r.redirected ? ('-> ' + r.url) : '');
      if (t.length > 3000) {
        // 找结果里是否有 gem 链接
        const hits = [...new Set([...t.matchAll(/href="\/us\/([^"]+)"/g)].map(m => m[1]))]
          .filter(x => /added|fire|damage/i.test(x)).slice(0, 15);
        if (hits.length) console.log('   候选:', hits.join(', '));
      }
    } catch (e) { console.log(u.padEnd(60), 'ERR', e.message); }
  }
})();
