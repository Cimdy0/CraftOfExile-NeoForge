# Reverse Scope Inventory & Write-Point Alignment Audit

> Reliable signals only: ORPHAN_TAG (deterministic) + GLOBAL_ATTR_RISK (read-site grounded).
> The old DEAD_GLOBAL / DEAD_MISALIGNED lexical heuristics are removed because scope is
> ENTERED in a caller (DamageEvent) but READ in a callee (DamagePipeline via LivingAttributeValue.fold);
> a method-level lexical scan cannot see that wiring.

## 1. SCOPE DICTIONARY (every tag injectable into a stat context at READ time)

- StatTag constant values: 145 (+ 5 parent tags with child-containment)
- EventName constant values: 77 (+ dynamic gain_/consume_<charge> keys)
- Status/Ailment ids (StatusType.register/dynamic/of): 100
- Skill ids (SkillType/Skill.register/registerSkill/Skill.of): 0
- Literal scope tags found at entry sites: 8
- KNOWN_SCOPE_TAGS (free-string debuff scopes): ['electrocuted', 'elemental_weakness', 'fully_broken_armor', 'thorns', 'volatility_detonated']
- Private consts in scope: archon, form
- **TOTAL injectable universe size: 302**

### Literal scope-entry sites (provenance)

- **AilmentHelper.java** (5 sites)
  L  428  [DYNAMIC scope: tags built from variables/status.id/skill.id]
  L  691  tags=['ailment']
  L  712  tags=['ailment']
  L  727  tags=['ailment', 'bleed']
  L  742  tags=['chaos', 'physical']
- **AttributeEvent.java** (1 sites)
  L  210  [DYNAMIC scope: tags built from variables/status.id/skill.id]
- **BannerAction.java** (1 sites)
  L  101  [DYNAMIC scope: tags built from variables/status.id/skill.id]
- **ChargedState.java** (1 sites)
  L  130  tags=[]
- **CreateGroundAction.java** (1 sites)
  L   87  [DYNAMIC scope: tags built from variables/status.id/skill.id]
- **CreateGroundOnAilmentEffect.java** (1 sites)
  L  107  [DYNAMIC scope: tags built from variables/status.id/skill.id]
- **DamageAction.java** (1 sites)
  L  292  [DYNAMIC scope: tags built from variables/status.id/skill.id]
- **DamageEvent.java** (4 sites)
  L   78  [DYNAMIC scope: tags built from variables/status.id/skill.id]
  L  104  [DYNAMIC scope: tags built from variables/status.id/skill.id]
  L  128  [DYNAMIC scope: tags built from variables/status.id/skill.id]
  L  172  [DYNAMIC scope: tags built from variables/status.id/skill.id]
- **DamageHelper.java** (1 sites)
  L  104  [DYNAMIC scope: tags built from variables/status.id/skill.id]
- **DamagePipeline.java** (1 sites)
  L  757  [DYNAMIC scope: tags built from variables/status.id/skill.id]
- **DamageTypeConvert.java** (1 sites)
  L   69  tags=[]
- **EntityOperationEvent.java** (4 sites)
  L  173  tags=[]
  L  178  tags=[]
  L  217  tags=[]
  L  228  tags=[]
- **GameEvent.java** (2 sites)
  L  240  [DYNAMIC scope: tags built from variables/status.id/skill.id]
  L  268  [DYNAMIC scope: tags built from variables/status.id/skill.id]
- **MetaSkillAction.java** (2 sites)
  L  102  [DYNAMIC scope: tags built from variables/status.id/skill.id]
  L  108  tags=[]
- **MobsInfo.java** (1 sites)
  L  118  tags=[]
- **PlayerAttackSweepMixin.java** (1 sites)
  L   45  tags=[]
- **SkillStatScopes.java** (2 sites)
  L   47  [DYNAMIC scope: tags built from variables/status.id/skill.id]
  L   70  [DYNAMIC scope: tags built from variables/status.id/skill.id]
- **StatScopes.java** (2 sites)
  L   14  [DYNAMIC scope: tags built from variables/status.id/skill.id]
  L   75  [DYNAMIC scope: tags built from variables/status.id/skill.id]
- **StatusBehavior.java** (1 sites)
  L  397  [DYNAMIC scope: tags built from variables/status.id/skill.id]
- **StatusEngine.java** (5 sites)
  L   45  tags=['status']
  L  346  tags=['archon']
  L  358  tags=['archon']
  L  369  tags=['archon']
  L  468  tags=['ailment', 'threshold']
- **VolatilityDetonationManager.java** (1 sites)
  L   96  tags=['volatility_detonated']

### Dynamic scope families (valid in scope, not all literal)

- `proj:<id>` (projectile instance tags, e.g. proj:skill_arrow) — wildcard `proj:*` matches any at read time
- skill names (`<skillname>` from skill id path) — injected via SkillStatScopes + DamageEvent skill path
- status/ailment ids (sample): adrenaline, aggravated, arcane_surge, archon_recovery, armour_break, armour_broken_fully, banner_aura, bleed, blind, charm_amethyst_effect, charm_antidote_effect, charm_dousing_effect, charm_golden_effect, charm_grounding_effect, charm_ruby_effect, charm_sapphire_effect, charm_silver_effect, charm_staunching_effect, charm_stone_effect, charm_thawing_effect, charm_topaz_effect, chill, corrupted_blood, critical_weakness, daze, debilitate, electrocute, elemental_archon, endurance_charge, exposure_cold ...
- skill-type tags: area, arrow, attack, aura, banner, beam, bow, channeling, command, corpse, crossbow, curse, grenade, hazard, herald, jewellery, mace, mark, melee, meta, minion, offering, plant, projectile, ranged, recently_consumed, seal, self, slam, spear, spell, storm, warcry
- event-name keys (also added as tags via GameEvent.causedTags/takenTags): attack_start, caused_ailment, caused_block, caused_crit, caused_dodge, caused_heal, caused_hit, caused_knockback, caused_meta_energy, caused_parry, caused_parry_raise, caused_perfect_parry, caused_shield_raise, caused_sweep, change_target, collect_remnant, consume_charge, consume_glory, conversion, crit, destroy_block, dismount, drown, effect_added, effect_expired, effect_removed, enter_area, equipment_change, experience_drop, fall ...

## 2. ORPHAN TAGS (deterministic — write tag never injectable into ANY scope => entry is DEAD)

- Total write points parsed: 373
- Write points with no tags (PASSIVE / always-on): 89
- ORPHAN_TAG distinct tags: 0
- ORPHAN_TAG occurrences (write-point references): 0

- none

## 3. GLOBAL-READ-ATTR REVIEW (StatEngine.fold is scope-aware; EMPTY scope = global read path)

Attributes below are READ only outside any scope. A non-empty POSITIVE tag on such an
attribute can never match the EMPTY scope -> the modifier is silently dead at its only read path.
`not(X)` tags still match EMPTY (X absent), so they are safe on global attributes.

### BLOCK_CHANCE  —  global-only, no positive tags (safe)
- read sites: 1; read_under_scope=False
    - BlockHelper.java:L27  method=getBlock  in_scope=False
- write points carrying tags: none

### ACCURACY  —  global-only, no positive tags (safe)
- read sites: 1; read_under_scope=False
    - DamageHelper.java:L214  method=isHit  in_scope=False
- write points carrying tags: none

### EVASION  —  global-only, no positive tags (safe)
- read sites: 1; read_under_scope=False
    - DamageHelper.java:L213  method=isHit  in_scope=False
- write points carrying tags: none

### MAX_RAGE  —  global-only, no positive tags (safe)
- read sites: 1; read_under_scope=False
    - RageHelper.java:L33  method=getMaxRage  in_scope=False
- write points carrying tags: none

### MAX_ENERGY_SHIELD  —  global-only, no positive tags (safe)
- read sites: 1; read_under_scope=False
    - EnergyShieldHelper.java:L27  method=getMaxEnergyShield  in_scope=False
- write points carrying tags: none

## 4. SUPPLEMENTARY (heuristic, method-level — NOT authoritative)

- attributes read inside a scope method (heuristic): 112
- attributes read only globally (heuristic): 42
- UNUSED literal scope tags (injectable but never used by a write point): []

### Attributes read ONLY globally (heuristic; review before adding restrictive tags)

- ACCURACY
- AILMENT_THRESHOLD_ADAPTATION
- ARCHON_RECOVERY_RATE
- BASE_ENERGY_SHIELD_RECHARGE
- BLOCK_CHANCE
- CAST_SPEED
- CORRUPTION_ENDURES_CHANCE
- CURRENT_ENERGY_SHIELD_RECHARGE
- DARKNESS_RESERVATION_LASTS
- DEFLECTION_RATING
- DEXTERITY
- ENERGY_RECHARGE
- EVASION
- INTELLIGENCE
- ITEM_CAPACITY
- MAX_DARKNESS
- MAX_ENERGY_SHIELD
- MAX_INFUSION
- MAX_MANA
- MAX_RAGE
- MAX_RESISTANCES
- MAX_SPIRIT
- MAX_VOLATILITY
- META_ENERGY_MAX
- NO_SPIRIT_BASE_DARKNESS
- PRESENCE_RANGE
- PUPPET_MASTER_CHANCE
- PUPPET_MASTER_DURATION
- RAGE_LOSS_GRACE
- RAGE_LOSS_RATE
- REMNANTS_COLLECTED_FURTHER_AWAY
- REMNANT_DOUBLE_CHANCE
- REMNANT_EXTRA_CHANCE
- REMNANT_MAGNITUDE
- RESISTANCES
- SKILL_SLOT_COUNT
- STRENGTH
- TAKE_NO_DAMAGE_VOLATILITY
- VOLATILITY_DETONATION_TIME
- VOLATILITY_GENERATION
- WORK_APTITUDE
- WORK_SPEED
