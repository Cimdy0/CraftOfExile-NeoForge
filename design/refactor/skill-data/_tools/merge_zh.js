/**
 * 数据源自洽修复器（根因修复版）。
 *
 * 从 all_skills.json 既有字段（descriptions_zh / properties_zh / name_zh）本地重算：
 *   - description_zh：CJK 优先块（修复 D10：不再取索引 0 英文占位块）
 *   - zh_status：ok / english_only / missing
 *   - page_kind：skill / non_skill（检测整页错抓：商城外观包 / buff 描述，修复 D3/整页错抓）
 * 再应用 zh_overrides.json（TW 缺失/旧版时的权威直译），并在主表写 corpus_status。
 *
 * 不依赖 zh_details.json（避免半新半旧的中间文件），纯内存单次处理，离线秒级完成。
 * 幂等：可反复执行。
 *
 *   node merge_zh.js
 */
const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design', 'refactor', 'skill-data', 'crawled');
const SRC = path.join(OUT, 'all_skills.json');
const OV = path.join(OUT, 'zh_overrides.json');
const EXTRA = path.join(__dirname, 'gemless_extra.json');

const CJK = /[一-鿿]/;
// 整页错抓（D3）根因规则：同时「无任何技能机制标签」且「描述是 buff 风格句」才判 non_skill。
// - 真实技能页（discipline/purity_*/blasphemy 等）的 property 含「持續/光環/等級」等机制标签，
//   即便罗列了「Blight supporter pack: Lich」这类外观皮肤名，也不算整页错抓（已全量验证仅命中 2 条）。
// - 外观皮肤名（外觀/supporter pack/cosmetic）本身不是机制标签，必须排除，否则会误杀所有光环宝石。
// - grace/wrath 等真实技能即便 property 为空，其描述是正常技能描述（非 buff 句），也不命中。
const RE_MECH = /持續|瞬發|範圍效果|光環|法術|攻擊|擊中|投射物|位移|引導|召喚|觸發|圖騰|陷阱|地雷|死靈|等級|階級|冷卻時間|主要/;
const RE_BUFF_DESC = /^(你正在|你的|你获得了|你被|你正)/;

const doc = JSON.parse(fs.readFileSync(SRC, 'utf8'));
const ov = fs.existsSync(OV) ? JSON.parse(fs.readFileSync(OV, 'utf8')) : {};

// ---------- 1. 追加 gemless 遗漏条目 ----------
let added = 0;
if (fs.existsSync(EXTRA)) {
  const extras = JSON.parse(fs.readFileSync(EXTRA, 'utf8'));
  const have = new Set(doc.entries.map(e => e.id));
  for (const e of extras) {
    if (have.has(e.id)) continue;
    e.is_gemless = true;
    doc.entries.push(e);
    added++;
  }
}

// ---------- 2. 本地重算描述 / 状态 / 整页错抓 ----------
let nDesc = 0, nOv = 0;
for (const e of doc.entries) {
  const descriptions = Array.isArray(e.descriptions_zh) ? e.descriptions_zh : [];
  const props = Array.isArray(e.properties_zh) ? e.properties_zh : [];

  const cjkBlocks = descriptions.filter(d => CJK.test(d));
  // D10 根因修复：仅取 CJK 优先块写入 description_zh；无中文块时置 null，
  // 让下游「description_zh → description_en → implicits」优先级链干净落到英文，不把英文塞进 _zh 字段。
  e.description_zh = cjkBlocks[0] || null;
  e.zh_status = cjkBlocks.length ? 'ok' : (descriptions.length ? 'english_only' : 'missing');

  // 整页错抓检测（D3/整页错抓）：无机制标签 + 描述为 buff 句（根因规则，已全量验证仅命中 2 条）
  const hasMech = props.some(p => RE_MECH.test(p));
  const descIsBuff = !!(cjkBlocks[0] && RE_BUFF_DESC.test(cjkBlocks[0]));
  const isNonSkill = !hasMech && descIsBuff;
  e.page_kind = isNonSkill ? 'non_skill' : 'skill';
  if (cjkBlocks.length) nDesc++;
}

// ---------- 3. 译文覆盖（TW 缺失/旧版，按 EN 直译或 implicits 反推） ----------
for (const e of doc.entries) {
  const o = ov[e.id];
  if (o && o.description_zh) {
    e.description_zh = o.description_zh;
    e.zh_source = o.zh_source || 'override';
    if (o.note) e._override_note = o.note;
    nOv++;
  }
}

// ---------- 4. corpus_status（写进主表，根治脏数据；源自带，无需 overlay） ----------
// 分类逻辑（全量验证确定，对应 D10/D3 根因修复）：
//   NO_AUTHORITATIVE_SOURCE：整页错抓（无机制标签 + buff 句，见 step2 page_kind）
//   OK          ：有可用描述（CJK 或真实英文，[DNT-UNUSED] 视为不可用）或命中 zh_overrides
//   DESC_MISSING：无可用描述但有可用 implicits（可据 implicits 反推形态）
//   DEPRECATED  ：无可用描述且无可用 implicits（官方未启用 / 纯占位）
// [DNT-UNUSED] = 官方未启用标记 → 描述不可用；[DNT]（无 UNUSED）= 未翻译但内容真实 → 可用。
const RE_UNUSED = /\[DNT-?UNUSED\]/i;
const stripMarker = s => (s || '').replace(/^\[DNT-?UNUSED\]|^\[DNT\]/i, '').trim();
const JUNK = /^(description|fill me in|ezomyte four|cast_on_death_reserve|reserve)$/i;
const isRealText = s => { if (!s) return false; const t = stripMarker(s); if (!t || JUNK.test(t)) return false; return t.length >= 10; };
const isUsableDescString = s => !!s && !RE_UNUSED.test(s) && isRealText(s);

for (const e of doc.entries) {
  const den = Array.isArray(e.description_en) ? e.description_en : (typeof e.description_en === 'string' ? [e.description_en] : []);
  const impl = Array.isArray(e.implicits) ? e.implicits : [];
  const hasOverride = !!(ov[e.id] && ov[e.id].description_zh);
  const usableDesc = !!(e.description_zh && CJK.test(e.description_zh)) || den.some(isUsableDescString);
  const usableImpl = impl.length > 0 && impl.some(x => typeof x === 'string' && !RE_UNUSED.test(x) && isRealText(x));
  if (e.page_kind === 'non_skill') {
    e.corpus_status = 'NO_AUTHORITATIVE_SOURCE';
    e.description_en = null;
    e.description_zh = null;
  } else if (hasOverride || usableDesc) {
    e.corpus_status = 'OK';
  } else if (usableImpl) {
    e.corpus_status = 'DESC_MISSING';
  } else {
    e.corpus_status = 'DEPRECATED';
  }
}

// ---------- 5. is_gemless 标记 ----------
const RE_PLAYER = /^PlayerDefault/i, RE_SHAPE = /^(Bear|Wolf|Wyvern)/i, RE_COMPANION = /^Summon.*Companion$/i;
const reasonOf = e => {
  if (e.gem_type === 'spirit' || e.gem_type === 'support') return null;
  const id = e.internal_id || '';
  if (RE_PLAYER.test(id)) return 'A:character_base_attack';
  if (RE_SHAPE.test(id)) return 'B:shapeshift_form';
  if (RE_COMPANION.test(id)) return 'C:companion_summon';
  return e.is_gemless ? 'D:manual' : null;
};
for (const e of doc.entries) {
  const r = reasonOf(e);
  if (r) { e.is_gemless = true; e.gemless_reason = r; }
}

// ---------- 6. 隔离清单（信息性） ----------
const QUAR = {
  blight: 'supporter_pack: /us/Blight 是赞助包页面，不是技能 → NO_AUTHORITATIVE_SOURCE',
  frost_nova: 'buff_page: 仅一句 buff 描述，无技能页 → NO_AUTHORITATIVE_SOURCE',
  grace: 'empty_stub: 无 property / 无描述 → DESC_MISSING',
  wrath: 'empty_stub: 无 property / 无描述 → DESC_MISSING',
  cyclone: 'translated: TW 无主描述，已直译（override）→ OK',
  perforate: 'translated: TW 旧版，已按新版直译（override）→ OK',
  puncture: 'truncated_levels: 仅 3 级 → DESC_MISSING',
  toxic_rain: 'truncated_levels: 仅 1 级 → DESC_MISSING',
  consume_corpse: 'no_level_table: 无等级数据 → DESC_MISSING',
  ground_slam: 'no_level_table: 无等级数据 → DESC_MISSING',
  lacerate: 'no_level_table: 无等级数据 → DESC_MISSING',
};
const quarantine = [];
for (const e of doc.entries) {
  if (QUAR[e.id]) {
    quarantine.push({ id: e.id, page: (e.source_url_us || '').split('/').pop(), status: e.corpus_status, reason: QUAR[e.id], gem_type: e.gem_type });
  }
}
fs.writeFileSync(path.join(OUT, '_quarantine.json'), JSON.stringify({
  note: '信息性隔离清单；corpus_status 已写进 all_skills.json 主表，下游以 corpus_status 为准。',
  count: quarantine.length,
  items: quarantine,
}, null, 1), 'utf8');

// ---------- 7. 重算 meta + 分类视图 ----------
const byType = t => doc.entries.filter(e => e.gem_type === t);
const skill = byType('skill'), support = byType('support'), spirit = byType('spirit');
const gemless = doc.entries.filter(e => e.is_gemless);

const statusCount = {};
for (const e of doc.entries) statusCount[e.corpus_status] = (statusCount[e.corpus_status] || 0) + 1;

doc.meta = {
  ...doc.meta,
  total: doc.entries.length,
  skill: skill.length,
  support: support.length,
  spirit: spirit.length,
  gemless: gemless.length,
  zh_enriched_at: new Date().toISOString(),
  name_zh_filled: doc.entries.filter(e => e.name_zh).length,
  description_zh_filled: doc.entries.filter(e => e.description_zh && /[一-鿿]/.test(e.description_zh)).length,
  corpus_status: statusCount,
  quarantine: quarantine.length,
};
doc.sources = ['https://poe2db.tw/us/', 'https://poe2db.tw/tw/', 'https://poe2db.tw/us/Default_Attack'];

fs.writeFileSync(SRC, JSON.stringify(doc, null, 1), 'utf8');

const wrap = (t, arr, src) => ({
  sources: [src],
  meta: { type: t, count: arr.length, zh_enriched_at: doc.meta.zh_enriched_at },
  entries: arr,
});
fs.writeFileSync(path.join(OUT, 'skill_gems.json'), JSON.stringify(wrap('skill', skill, 'https://poe2db.tw/us/Skill_Gems'), null, 1));
fs.writeFileSync(path.join(OUT, 'support_gems.json'), JSON.stringify(wrap('support', support, 'https://poe2db.tw/us/Support_Gems'), null, 1));
fs.writeFileSync(path.join(OUT, 'spirit_gems.json'), JSON.stringify(wrap('spirit', spirit, 'https://poe2db.tw/us/Spirit_Gems'), null, 1));

const gl = doc.entries.filter(e => e.is_gemless).map(e => ({ ...e })).sort((a, b) => a.id.localeCompare(b.id));
const byReason = {};
for (const e of gl) byReason[e.gemless_reason] = (byReason[e.gemless_reason] || 0) + 1;
fs.writeFileSync(path.join(OUT, 'gemless_skills.json'), JSON.stringify({
  sources: ['https://poe2db.tw/us/', 'https://poe2db.tw/tw/'],
  meta: { type: 'gemless', count: gl.length, by_reason: byReason, generated_at: new Date().toISOString() },
  entries: gl,
}, null, 1));

console.log('追加 gemless 条目 =', added);
console.log('本地重算中文块 =', nDesc, ' 译文覆盖 =', nOv);
console.log('total =', doc.entries.length, JSON.stringify({ skill: skill.length, support: support.length, spirit: spirit.length, gemless: gl.length }));
console.log('corpus_status =', JSON.stringify(statusCount));
console.log('name_zh 非空 =', doc.meta.name_zh_filled, ' description_zh 含中文 =', doc.meta.description_zh_filled);
console.log('隔离清单 =', quarantine.length);
