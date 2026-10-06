// 核实：poe2db 的 Spirit_Gems 页到底含哪些条目，与本地 spirit 分类的差异根源
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const PD = path.join(ROOT, 'design/refactor/skill-data/crawled/raw/index_pages');
const OUT = path.join(ROOT, 'design/refactor/skill-data/crawled');
const E = JSON.parse(fs.readFileSync(path.join(OUT, 'all_skills.json'), 'utf8')).entries;

function rowsOf(html) {
  const out = [];
  for (const seg of html.split(/<tr\s+data-filters="/).slice(1)) {
    const q = seg.indexOf('"'); if (q < 0) continue;
    const body = seg.slice(q + 1);
    const m = body.match(/href="\/us\/([^"]+)"/); if (!m) continue;
    const cand = [...body.replace(/<div class="gem_tags[\s\S]*$/, '').matchAll(/<a\b[^>]*href="\/us\/[^"]+"[^>]*>([^<]+)<\/a>/g)].map(x => x[1].trim());
    out.push({ href: m[1], name: (cand.length ? cand[cand.length - 1] : '').replace(/\s*\(\d+\)\s*$/, '').trim(), filters: seg.slice(0, q) });
  }
  return out;
}

const spirit = rowsOf(fs.readFileSync(path.join(PD, 'Spirit_Gems.html'), 'utf8'));
const skill = rowsOf(fs.readFileSync(path.join(PD, 'Skill_Gems.html'), 'utf8'));
console.log('Spirit_Gems 行数 =', spirit.length, ' Skill_Gems 行数 =', skill.length);
const spiritSet = new Set(spirit.map(r => r.href));
const skillSet = new Set(skill.map(r => r.href));
console.log('Spirit ⊆ Skill ?', [...spiritSet].every(h => skillSet.has(h)));

console.log('\n=== poe2db Spirit_Gems 全部条目名 ===');
console.log(spirit.map(r => r.href).join(', '));

console.log('\n=== 抽查：本地判为 spirit 但权威页不在 Spirit_Gems 的 ===');
const QUESTION = ['heart_of_ice', 'impurity', 'malice', 'purity_of_fire', 'purity_of_ice', 'purity_of_lightning', 'wrath', 'grace', 'clarity'];
for (const q of QUESTION) {
  const a = E.find(e => e.id === q);
  console.log('  ' + q.padEnd(22) + ' gem_type=' + (a ? a.gem_type : '不在数据集') +
    ' | 在Spirit_Gems页=' + spiritSet.has(a ? a.source_url_us.split('/').pop() : '') +
    ' | seen_in=' + (a ? (a.seen_in || []).join(',') : ''));
}
console.log('\n=== 抽查：本地判为 skill 但权威页在 Spirit_Gems 的 ===');
for (const q of ['elemental_conflux', 'alchemists_boon', 'plague_bearer', 'iron_ward', 'magma_barrier', 'war_banner', 'scavenged_plating', 'blood_pact']) {
  const a = E.find(e => e.id === q);
  console.log('  ' + q.padEnd(22) + (a ? ('gem_type=' + a.gem_type + ' | seen_in=' + (a.seen_in || []).join(',')) : '不在数据集'));
}
