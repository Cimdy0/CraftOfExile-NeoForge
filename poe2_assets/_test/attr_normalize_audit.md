# 属性层根因审计（非归一 + 占位未消费）

> 触发：用户指出"根据 ids 修复 datagen 接线是治标不治本，属性还是用的包含作用域/条件之类的非归一属性"。
> 结论：根因在属性层（AttributeRegister），不在 StatGen 条目层（tag/condition 接线）。

## 方法
- 解析 `AttributeRegister.java` 全部 `public static Holder<Attribute> X = register*(...)`（195 个）。
- 引擎消费判定（skill §10 精确法）：`AttributeRegister.<X>` 是否在 `src/main/java` 中被 AttributeRegister/StatGen 之外的文件引用。无引用 = 占位未消费。
- 非归一判定：属性名是否焊死作用域（SLAM/MACE/SHAPESHIFT/MINION/TOTEM/TAKEN/RED_GREEN/FORM…）或条件。

## 门禁指标
| 维度 | 数量 | 占比 |
|---|---|---|
| 属性总数 | 195 | — |
| 引擎真消费 | 111 | 57% |
| 占位未消费 | 84 | 43% |
| 非归一 | 84 | 43% |
| 非归一 且 占位未消费（最严重） | 46 | — |

## 我碰过的 5 个属性（反向盘点审计的修复对象）复核
| 属性 | 引擎消费? | 非归一? | 我之前的"修复"是否有效 |
|---|---|---|---|
| `AFTERSHOCK_AREA` | ✅ 消费（AreaDeliveryExecutor 内 slam scope） | ✅ 焊死 aftershock=area | 有效（tag→slam 匹配消费点） |
| `BLOCK_CHANCE` | ✅ 消费（BlockHelper） | 否 | 有效（去错 tag） |
| `MAX_ENERGY_SHIELD` | ✅ 消费（EnergyShieldHelper） | 否 | 有效（去错 tag） |
| `METRES_MELEE_STRIKE_RANGE` | ❌ **零消费** | 部分（recent_projectile 条件在 entry） | **moot**（条件接对也是死） |
| `ELEMENTAL_RESISTANCES_RED_GREEN` | ❌ **零消费** | ✅ 焊死 RGB 条件 | 删条目正确；但属性本身占位，接线救不了 |
| `DEFEND_ARMOUR` | ❌ **零消费** | 焊死 defend-vs-crit | `inc→pctAdd` 也救不了（无人读） |

→ 5 个里 3 个是占位未消费，之前的接线/计算符修复对它们是治标。

## 最严重 46 例（非归一 且 占位未消费，节选）
`MAX_TOTEM` `MAX_MINION` `MAX_BANNER` `MAX_FISSURE` `MAX_ORB` `AURA_MAGNITUDE`
`TRAP_THROWING_SPEED` `QUIVER_BONUSES` `TOTEM_PLACEMENT_RANGE` `ADDITIONAL_CURSE`
`ATTACK_TEN_INCISION` `ATTACKS_MAIM_POISONED` `AURA_BUFFS_AFFECT_ALLIES`
`CULLING_STRIKE_MARK` `DAMAGE_LUCKY` `ELEMENTAL_AILMENT_APPLICATION_SHAPESHIFTED`
`FASTER_CURSE_ACTIVATION` `GRENADES_ACTIVATE_TIME` `IGNITES_CAUSE_REFLECTED_BACK`
`INFLICT_BLEEDING_CANNOT_REGENERATE` `LIFE_FLASKS_CHARGES` `LIGHTNING_INFUSIONS`
`MACE_SLAM_CAUSE_ADDITIONAL` `MAGNITUDE_IMPALES_INFLICTED_SPELLS` `MANA_FLASKS_CHARGES`
`MARK_BLINDED` `MARK_CANNOT_CRITICAL` `MINIONS_GIGANTIC` `MINIONS_INFLICT_WITHERED`
`PLANTS_IMMEDIATELY_OVERGROW` `RAGE_LIFE_FLASK` `ADD_CURSE` `REMOVE_ELEMENTAL_INFUSIONS_BUT`
`REMOVE_IGNITE_WARCRY` `SHOCK_ELECTROCUTED` `STACKS_PUPPET_MASTER` `UNAFFECTED`
`WARCRIES_DEBILITATE` `WHICH_CREATE_FISSURES_ADDITIONAL` `ADDITIONAL_STUN_THRESHOLD_EQUAL_2`
`BONUSES_AMULET_MINIONS` `ELEMENTAL_RESISTANCES_RED_GREEN` `IGNORE_MOVEMENT_PENALTIES_ARMOUR`
`RAGE_SHAPESHIFTING_HUMAN_FORM`
（全量见 `attr_normalize_audit.json`）

## 根因修复方向（待裁定）
1. **全量根因修复**：以消费矩阵为准，84 占位逐一定性——实现引擎读取点 或 移除属性+条目（置空+TODO，遵循"机制未实现→数据落地但置空"共识）；同时把焊死作用域/条件的属性折叠到归一属性 + entry 层 scope/condition。最彻底，体量最大。
2. **边界收敛到本次碰过的 5 个**：归一化这 5 个（含未消费 3 个决定补引擎读取或移除）。快速见效。
3. **只出根因地图不改动**：本报告 + JSON 即交付物，由用户排优先级。

## 已知边界
- 非归一 ≠ 必错：PoE2 中 `Aftershock Area of Effect` 本就是独立 stat，是否折叠到 `AREA_OF_EFFECT`+slam 涉及"忠实度 vs 架构整洁"取舍，需逐属性裁定。
- `ELEMENTAL_RESISTANCES_RED_GREEN` 的 RGB 条件需玩家级插孔宝石颜色索引基础设施（当前不存在），归一化前须先建该设施或保持 TODO。
