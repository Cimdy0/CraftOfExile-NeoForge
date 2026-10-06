/**
 * 输出最终裁定文档：本地 id -> poe2db 权威 id
 * 铁证：PoE2 引擎内部 id（internal_id）不随改名变化，可与本地语义一一对应
 */
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const OUT = path.join(ROOT, 'design/refactor/skill-data/crawled');
const E = JSON.parse(fs.readFileSync(path.join(OUT, 'all_skills.json'), 'utf8')).entries;
const byId = new Map(E.map(e => [e.id, e]));
const b = id => byId.get(id);

const V = [
  // [本地 id, 本地分类, 权威 id, 依据]
  ['added_fire_damage', 'support', 'fire_attunement', 'PoE2 内部 id = `AddedFireDamageSupport`，铁证'],
  ['added_cold_damage', 'support', 'cold_attunement', 'PoE2 内部 id = `AddedColdDamageSupport`，铁证'],
  ['added_lightning_damage', 'support', 'lightning_attunement', 'PoE2 内部 id = `AddedLightningDamageSupport`，铁证'],
  ['added_chaos_damage', 'support', 'chaos_attunement', 'PoE2 内部 id = `AddedChaosDamageSupport`，铁证'],
  ['biting_exposure', 'support', 'cold_exposure', 'PoE2 内部 id = `ColdExposureSupport`；本地效果 damage_type=cold，一致'],
  ['conductive_exposure', 'support', 'lightning_exposure', 'PoE2 内部 id = `LightningExposureSupport`；本地 damage_type=lightning，一致'],
  ['scorching_exposure', 'support', 'fire_exposure', 'PoE2 内部 id = `FireExposureSupport`；本地 damage_type=fire，一致'],
  ['arbiter_ignition', 'support', 'arbiters_ignition', "权威名为 Arbiter's Ignition（所有格），本地写法丢了撇号"],
  ['cast_on_crit', 'support', 'cast_on_critical', '权威名 Cast on Critical；本地为缩写'],
  ['ice_shard', 'skill', 'ice_shards', '权威名 Ice Shards（复数）'],
  ['wolf_rake', 'skill', 'rake', '权威 `rake` 为 Shapeshift 系 Strike；本地 ref = generic_shapeshift_physical（狼形态）'],
  ['bear_maul', 'skill', 'maul', '权威 `maul` tags 含 Shapeshift/Bear；本地 ref = generic_shapeshift_physical_rage（熊形态）'],
  ['wyvern_rend', 'skill', 'rend', '权威 `rend` tags 含 Shapeshift/Wyvern；本地 ref = generic_shapeshift_lightning（飞龙形态）'],
];

const UNRESOLVED = [
  ['fire_infusion', 'support', '本地效果 = 物理转火焰(convert physical→fire) + 25% more fire。权威无同名辅助；`infusion` 实为 BuffedHeraldSupport(不同语义)，`runic_infusion` 为 Runic Ward 系。语义最接近 `crater` / `volcanic_eruption`（Convert 80% Physical to Fire），但非等同 → 需人工裁定'],
  ['martial_projectiles', 'support', '本地效果 = 额外 2 投射物。权威辅助中无同名；+投射物数量的近邻为 `double_barrel_i/ii`（仅限弩箭装填）→ 需人工裁定（PoE2 的投射物数量辅助另有其名，如 Scattershot，未在本次爬取名单中命中）'],
  ['shockwave', 'skill', '权威无 "Shockwave" 技能；近邻 `shockwave_totem`(技能) 与 `impact_shockwave`(辅助) → 需人工裁定本地意图'],
  ['lightning_strike', 'skill', 'poe2db 无此页（PoE1 专有技能名）→ 非 PoE2 技能'],
  ['blood_pact', 'spirit', 'poe2db 无此页。权威近邻 `abyssal_pact` / `dark_pact` / `paquates_pact` → 需人工裁定，或为 PoE1 残留'],
];

const EXTRA = ['blight', 'frost_nova', 'cyclone', 'lacerate', 'puncture', 'ground_slam', 'perforate',
  'consume_corpse', 'toxic_rain', 'wrath', 'grace'];

let md = `# 本地技能 id ↔ poe2db 权威命名 · 最终裁定表

> **唯一判据**：\`design/refactor/skill-data/crawled/all_skills.json\`（poe2db 爬取资料）。
> **铁证方法**：PoE2 的**引擎内部 id**（poe2db 元数据表的 \`Id\` 字段）**不随改名变化**，
> 因此当本地语义 id 与某权威条目的 \`internal_id\` 一致时，即为同一条目，命名以权威**当前**名称为准。
> 用户裁定：**名以资料库为准；PoE1 专有名称一律不认。**

## 一、可确定映射（13 条）

| 本地 id | 分类 | 权威 id | 权威名 | 中文名 | 依据 |
|---|---|---|---|---|---|
`;
for (const [loc, cat, tgt, why] of V) {
  const t = b(tgt);
  md += `| \`${loc}\` | ${cat} | \`${tgt}\` | ${t ? t.name_en : '?'} | ${t ? (t.name_zh || '—') : '—'} | ${why} |\n`;
}

md += `

### 命名演变链（为何这 4 个 "Added X Damage" 属于 PoE2 而非 PoE1）
\`\`\`
PoE2 0.1   Added Fire Damage          （skill_id = SupportAddedFireDamagePlayer）
PoE2 0.2   → Fire Infusion
PoE2 0.3   → Fire Attunement          （poe2wiki: "The Lightning/Cold/Fire/Chaos Infusion Support Gems
                                       have been renamed to Lightning/Cold/Fire/Chaos Attunement"）
\`\`\`
**引擎内部 id 至今仍为 \`AddedFireDamageSupport\`** —— 这是它们确属 PoE2、且本地旧名可溯源的决定性证据。
冷/电/混沌三条同理（\`AddedColdDamageSupport\` / \`AddedLightningDamageSupport\` / \`AddedChaosDamageSupport\`）。

Exposure 三条同理：本地用的是 **PoE1 命名**（Biting/Conductive/Scorching），
PoE2 的现行名是 \`cold_exposure\` / \`lightning_exposure\` / \`fire_exposure\`，内部 id 一一对应。

## 二、需人工裁定（5 条，**不臆造**）

| 本地 id | 分类 | 权威数据中的情况 |
|---|---|---|
`;
for (const [loc, cat, why] of UNRESOLVED) md += `| \`${loc}\` | ${cat} | ${why} |\n`;

md += `

## 三、本地有、且已确认为**真实 PoE2 无宝石技能**（11 条，已补抓）

以下名称在 poe2db 上**不存在于宝石索引**，但页面真实存在且元数据表含完整 \`Type\` 字段
（即 PoE2 的武器/角色固有技能，非宝石形态）：

\`${EXTRA.join('`、`')}\`

佐证（\`Type\` 字段节选）：
- \`Cyclone\` → \`Attack, Area, Melee, Movement, Channel, Physical, AttackInPlace\`
- \`Puncture\` → \`Attack, RangedAttack, MirageArcherCanUse, Projectile, ProjectilesFromUser, Duration, ...\`
- \`Ground_Slam\` → \`Attack, Area, Melee, Slam, Totemable, AttackInPlace\`
- \`Wrath\` → \`Spell, Area, Buff, HasReservation, ..., Aura, Lightning, Persistent\`（光环类）
- \`Grace\` → \`Spell, Area, Buff, HasReservation, ..., Aura, Persistent\`（光环类）

## 四、结论

- 本地 243 个宝石条目中，**13 条为命名过时/PoE1 命名**，须按上表重命名为权威名。
- **5 条无法在权威数据中定性**，须由作者裁定（其中 \`lightning_strike\` / \`blood_pact\` 在 poe2db 完全无页面，疑 PoE1 残留）。
- **11 条为真实 PoE2 无宝石技能**，此前未被纳入清单，现已补抓进 \`all_skills.json\`。
- 裁定后，本地 243 条中 **229 条**可在权威数据中找到对应条目。

生成时间：${new Date().toISOString()}
`;
fs.writeFileSync(path.join(OUT, 'name_resolution.md'), md);
fs.writeFileSync(path.join(OUT, 'name_resolution.json'), JSON.stringify({
  generated_at: new Date().toISOString(),
  confirmed: V.map(([l, c, t, w]) => ({ local_id: l, local_cat: c, authoritative_id: t, evidence: w })),
  unresolved: UNRESOLVED.map(([l, c, w]) => ({ local_id: l, local_cat: c, note: w })),
  poe2_gemless_skills: EXTRA
}, null, 1));
console.log('已写出 name_resolution.md / .json');
console.log('确定映射 =', V.length, ' 待裁定 =', UNRESOLVED.length, ' 无宝石技能 =', EXTRA.length);
