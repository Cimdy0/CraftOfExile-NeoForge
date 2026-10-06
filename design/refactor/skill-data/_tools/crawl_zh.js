/**
 * 补抓繁中镜像 /tw/<Page>，抽取：
 *   - name_zh        （<div class="itemName"><span class="lc">）
 *   - description_zh （首个 <div class="secDescrText">）
 *   - descriptions_zh（全部 secDescrText）
 *   - implicits_zh   （全部 explicitMod，去重）
 *   - quality_bonus_zh（全部 qualityMod）
 * 缓存：crawled/raw/tw_html_gz/<Page>.html.gz（断点续跑）
 * 输出：crawled/zh_details.json  { <id>: {...} }
 *
 * 用法： node crawl_zh.js [--limit N] [--force]
 */
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');

const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data', 'crawled');
const RAW = path.join(OUT, 'raw', 'tw_html_gz');
const SRC = path.join(OUT, 'all_skills.json');
const DST = path.join(OUT, 'zh_details.json');
fs.mkdirSync(RAW, { recursive: true });

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36';
const H = {
  'User-Agent': UA,
  'Referer': 'https://poe2db.tw/tw/',
  'Accept-Language': 'zh-TW,zh;q=0.9,en;q=0.8',
};
const CONC = Number(process.env.CONC || 6);
const SLEEP = Number(process.env.SLEEP || 120);
const sleep = ms => new Promise(r => setTimeout(r, ms));

const arg = (k, d) => {
  const i = process.argv.indexOf(k);
  return i >= 0 ? process.argv[i + 1] : d;
};
const LIMIT = Number(arg('--limit', 0));
const FORCE = process.argv.includes('--force');

const txt = s => s.replace(/<[^>]+>/g, '').replace(/&nbsp;/g, ' ').replace(/\s+/g, ' ').trim();
const uniq = a => [...new Set(a.filter(Boolean))];

async function fetchPage(page, tries = 3) {
  const f = path.join(RAW, page + '.html.gz');
  if (!FORCE && fs.existsSync(f)) {
    try { return zlib.gunzipSync(fs.readFileSync(f)).toString('utf8'); } catch (e) { /* fallthrough */ }
  }
  for (let i = 0; i < tries; i++) {
    try {
      const r = await fetch('https://poe2db.tw/tw/' + page, { headers: H });
      if (r.status === 404) { fs.writeFileSync(f, zlib.gzipSync(Buffer.from('__404__', 'utf8'))); return null; }
      if (!r.ok) { await sleep(400 * (i + 1)); continue; }
      const t = await r.text();
      fs.writeFileSync(f, zlib.gzipSync(Buffer.from(t, 'utf8')));
      return t;
    } catch (e) {
      await sleep(500 * (i + 1));
    }
  }
  return null;
}

const CJK = /[一-鿿]/;
// 整页错抓检测：商城外观包 / buff 描述（非技能页）
const RE_NON_SKILL = /supporter pack|外观|cosmetic/i;
const RE_BUFF_DESC = /^(你正在|你的|你获得了|你被|你正)/;

function parse(t) {
  if (!t || t === '__404__') return null;
  const name_zh = (t.match(/<div class="itemName">[\s\S]*?<span class="lc">([^<]*)<\/span>/) || [])[1] || null;
  const descriptions = uniq([...t.matchAll(/<div class="secDescrText">([\s\S]*?)<\/div>/g)].map(m => txt(m[1])));
  const implicits = uniq([...t.matchAll(/<div class="explicitMod">([\s\S]*?)<\/div>/g)].map(m => txt(m[1])));
  const quality = uniq([...t.matchAll(/<div class="qualityMod">([\s\S]*?)<\/div>/g)].map(m => txt(m[1])));
  const props = uniq([...t.matchAll(/<div class="property">([\s\S]*?)<\/div>/g)].map(m => txt(m[1])));

  // 根因修复（D10）：description_zh 取首个含 CJK 的块，而非索引 0（首个块常为英文占位）。
  // 兜底：若无 CJK 块则保留索引 0（英文），并在 zh_status 标记。
  const cjkBlocks = descriptions.filter(d => CJK.test(d));
  let description_zh = cjkBlocks[0] || descriptions[0] || null;
  let zh_status = cjkBlocks.length ? 'ok' : (descriptions.length ? 'english_only' : 'missing');

  // 整页错抓检测（D3/整页错抓）：页面是商城外观包或首块是 buff 描述 → 非技能页
  const propText = props.join(' ');
  const isNonSkill = RE_NON_SKILL.test(propText) || (cjkBlocks[0] && RE_BUFF_DESC.test(cjkBlocks[0]));
  const page_kind = isNonSkill ? 'non_skill' : 'skill';

  return {
    name_zh,
    description_zh,
    zh_status,
    page_kind,
    descriptions_zh: descriptions,
    implicits_zh: implicits,
    quality_bonus_zh: quality.length ? quality.join(' | ') : null,
    properties_zh: props,
  };
}

(async () => {
  const doc = JSON.parse(fs.readFileSync(SRC, 'utf8'));
  let list = doc.entries;
  if (LIMIT) list = list.slice(0, LIMIT);

  const cachePath = path.join(ROOT, 'design/refactor/skill-data', '_tools', '.zh_progress.json');
  const out = fs.existsSync(DST) ? JSON.parse(fs.readFileSync(DST, 'utf8')) : {};
  const todo = list.filter(e => !out[e.id] || FORCE);
  console.log(`共 ${list.length} 条，待抓 ${todo.length} 条（已完成 ${Object.keys(out).length}）`);

  let done = 0, ok = 0, miss = 0;
  for (let i = 0; i < todo.length; i += CONC) {
    const batch = todo.slice(i, i + CONC);
    const rs = await Promise.all(batch.map(async e => {
      const page = (e.source_url_tw || '').split('/').pop() || (e.source_url_us || '').split('/').pop();
      if (!page) return [e.id, null];
      const t = await fetchPage(page);
      return [e.id, parse(t)];
    }));
    for (const [id, v] of rs) {
      out[id] = v;
      if (v && v.description_zh) ok++; else miss++;
    }
    done += batch.length;
    if (done % 120 === 0 || done === todo.length) {
      console.log(`  ${done}/${todo.length}  有中文描述=${ok} 缺失=${miss}`);
      fs.writeFileSync(DST, JSON.stringify(out, null, 1), 'utf8');
    }
    await sleep(SLEEP);
  }

  fs.writeFileSync(DST, JSON.stringify(out, null, 1), 'utf8');
  const withDesc = Object.values(out).filter(v => v && v.description_zh).length;
  console.log(`\n完成：${Object.keys(out).length} 条；有中文描述 ${withDesc}；无 ${Object.keys(out).length - withDesc}`);
  console.log('写出', DST);
})();
