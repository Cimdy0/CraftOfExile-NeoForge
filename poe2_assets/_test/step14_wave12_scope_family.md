# Step14 — Wave-12 作用域/条件归一续（批次 2/3/4/5）

> 续 step13。方法学（用户指令）：先检查 `StatTag` 作用域词表 → 按被引用 `StatIds` 的语义归一；词族同单词属性严格检查一致性。

## 一、发现与处置（共 39 处落点编辑）

### 批次 2 — 属性合并 + 条件标签（6 项）
| 原属性 | 归一 | 动作 |
|---|---|---|
| `MINIONS_CRITICAL` | `CRIT_CHANCE` + `{minion}` | 删属性 |
| `REMOVE_CURSE_CHANNELLING` | 新基类 `ADD_CURSE` + `{curse,channeling}` | 删属性 |
| `REMOVE_CURSE_MANA_FLASK` | 新基类 `ADD_CURSE` + `{curse,flask}` | 删属性 |
| `METRES_MELEE_STRIKE_RANGE_2` | `METRES_MELEE_STRIKE_RANGE` + `{"recent_projectile_8s"}` | 删属性 |
| `CANNOT_BLINDED_FULL_LIFE` | `ImmuneStatEffect(BLIND)` + `{"full_health"}` | 删属性 |
| `CANNOT_CRITICALLY_PARRYING` | 保留 + `{parry}` | 条件标签 |

### 批次 3 — 合并 + C类条件显式化（4 项）
| 属性 | 归一 |
|---|---|
| `ATTACKS_CRITICAL` | `CRIT_CHANCE` + `{attack}`（删属性，与批次2同族） |
| `MINIONS_GIGANTIC` | 保留 + `{minion}` |
| `BONUSES_AMULET_MINIONS` | 保留 + `{jewellery,minion}` |
| `STACKS_PUPPET_MASTER` | 保留 + `{companion}` |

### 批次 4 — 主语/作用域标签补全（27 项）
按 StatIds 语义对 27 个占位属性补 StatTag 作用域标签：`arrow`(2)、`grenade`、`ignite`(2)、`bleed`、`energy_shield`(2)、`leech`(2)、`chaos`、`cold`、`health`、`lightning`、`infusion`、`impale`、`spell`、`plant`、`kill`、`remnant`、`elemental`+`attack`、`warcry`、`debilitate`、`stun`、`elemental`+`resistance`、`life`+`mana`+`cost`(2)、`energy_shield`+`leech`、`flask`(2)、`mana`、`chaos`/`cold`/`lightning`(各对应)、`withered`、`shock`。
> 脚本初版用裸子串匹配（`"ARROW" in line`）误判 20 项已存在（因 `StatIds.ARROWS_PIERCE_*` 含 ARROW 子串），修正为 StatTag 令牌解析后 25 项真应用、2 项本已完整。

### 批次 5 — 词族一致性严格检查（2 项）
- `MINIONS_INFLICT_WITHERED` 补 `{minion}` → `{minion,withered}`（与 `MINIONS_GIGANTIC`/`BONUSES_AMULET_MINIONS`/`RECOVER_LIFE_MINIONS_IS` 同族）。
- `ELEMENTAL_AILMENT_APPLICATION_SHAPESHIFTED` 补 `{elemental}` → `{elemental,shapeshifted}`（与 `remove_elemental_infusions_but`/`elemental_resistances_red_green`/`INCREASED_ELEMENTAL_DAMAGE` 同族）。
- 审计脚本 `_family_audit.py` 按作用域词分组，绝大多数"不一致"为**子串噪声**（不同类属性恰含同单词）。落点明确的词族缺口仅上述 2 处。

## 二、严格检查后判定「不宜动」的项
| 类别 | 成员 | 理由 |
|---|---|---|
| 未接线占位 | `REMNANT_DOUBLE_CHANCE`、`REMNANT_MAGNITUDE`、`ELEMENTAL_DAMAGE_CONTRIBUTES_BLEEDING`、`base/current_energy_shield_recharge`、`AFTERSHOCK_CHANCE*` 等 | 经核实**无 StatGen 写点**，无法打标（需先有对应 `StatIds` 写点） |
| 共享基类随从条目 | `INC_MINIONS_COOLDOWN_RECOVERY_RATE`、`MINIONS_HAVE_INCREASED_CAST_SPEED`、`MINIONS_HAVE_INCREASED_HEALTH_RECOVERY_RATE` | `CAST_SPEED` 经 `SkillCast.java:220` 直接 `getAttributeValue` 读取，标签对直接读取基类惰性，原作留空正确 |

## 三、门禁指标（三道门全过）
| 门 | 结果 |
|---|---|
| compileJava | ✅ BUILD SUCCESSFUL |
| runData | ✅ BUILD SUCCESSFUL（851 stat_entry，真完成签名：`Stat Entries finished` / `Closing FML Loader`） |
| `_verify_batch4.py` | ✅ 27 个作用域标签全部在位 |
| `_verify_batch5.py` | ✅ 2 词族落点正确（`elemental,shapeshifted` / `minion,withered`） |
| `_plana.py` | ✅ `SPLIT=0 RISK=0 FIX=0 MIXED=0 ORPHAN=0 BARE-RISK=0` |

## 四、下一步建议
1. **Wave-12 作用域/条件归一已基本收口**：可并基类已无干净目标，原子基类与未接线占位不在本轮回填（避免推测性改动）。
2. 若要继续，方向应是**未接线占位属性的消费端补写**（先建 `StatIds` 写点再决定是否合并），而非在 StatGen 侧硬拉标签。
3. 环境提醒：`runData` 偶发守护进程 socket 超时假阴性（exit1/空输出），重跑前清 `.gradle/9.2.1/checksums/checksums.lock`；本次最后一轮 9m 长耗时属偶发，复跑稳定 50s 级。
