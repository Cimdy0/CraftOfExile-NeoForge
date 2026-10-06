# StatIds 语义重复判定报告

> 只读审计，未改任何代码。判定方法：`_test/_audit_semantic_dup.py`（纯读 `StatIds.java` + 全仓引用点）。
> 纪律：仅输出候选 + 证据，**不自动合并**（红线：不词法臆造、语义驱动）。

## 方法（三层判定）

| 层 | 规则 | 含义 |
|---|---|---|
| L1 | 两个常量名指向同一 `id("...")` 串 | 硬重复（确定 bug） |
| L2 | 词序无关归一（缩写展开 + 算子同义词合一，维度不交叉）后 token 集合相同 | 强候选（高概率同语义） |
| L3 | 某常量归一 token 集合是另一常量的严格子集 | 弱候选（多为 scope 扩展，非重复） |

算子维度严格区分：`flat(+X/to/add/gain)` ≠ `inc(additive%)` ≠ `more(multiplicative%)`，不得归一。

## 结果规模

| 层 | 组数 | 常量数 | 结论 |
|---|---|---|---|
| L1 硬重复 | **0** | 0 | 无同 id 串 |
| L2 强候选 | 18 组 | 36 | 16 真重复 + 2 误报 |
| L3 子集 | 1595 对 | — | 噪音（scope 维度扩展，非重复，不处理） |

## 真重复明细（16 组）

### A. 干净死别名（TO_X 零引用 vs ADD_X 被引用）—— 安全合并，仅删零引用声明

| # | 死别名（ref=0，删） | 保留者（被引用） | 备注 |
|---|---|---|---|
| 1 | `TO_ACCURACY` | `ADD_ACCURACY` (2) | flat +X to Accuracy |
| 2 | `TO_DEXTERITY` | `ADD_DEXTERITY` (2) | |
| 3 | `TO_EVASION` | `ADD_EVASION` (2) | |
| 4 | `TO_INTELLIGENCE` | `ADD_INTELLIGENCE` (2) | |
| 5 | `TO_STRENGTH` | `ADD_STRENGTH` (2) | |
| 6 | `TO_MAX_ENERGY_SHIELD` | `ADD_MAX_ENERGY_SHIELD` (2) | **注释错位**（贴成 Ailment Threshold 描述），id 语义同 Max ES，合并时顺修注释 |
| 7 | `TO_MAX_HEALTH` | `ADD_MAX_HEALTH` (2) | |
| 8 | `TO_MAX_MANA` | `ADD_MAX_MANA` (2) | |
| 9 | `TO_CHAOS_RESISTANCE` | `ADD_CHAOS_RESISTANCE` (2) | |
| 10 | `TO_COLD_RESISTANCE` | `ADD_COLD_RESISTANCE` (3) | |
| 11 | `TO_FIRE_RESISTANCE` | `ADD_FIRE_RESISTANCE` (3) | |
| 12 | `TO_LIGHTNING_RESISTANCE` | `ADD_LIGHTNING_RESISTANCE` (3) | |
| 13 | `TO_MAX_COLD_RESISTANCE` | `ADD_MAX_COLD_RESISTANCE` (3) | |
| 14 | `TO_MAX_LIGHTNING_RESISTANCE` | `ADD_MAX_LIGHTNING_RESISTANCE` (3) | |

### B. 词序死别名（零引用未接线 vs 有引用已接线）—— 安全合并

| # | 死别名（ref=0，删） | 保留者（被引用） | 接线 |
|---|---|---|---|
| 15 | `INC_ATTACK_PHYSICAL_DAMAGE` | `INC_ATTACK_DAMAGE_PHYSICAL` (2) | `ATTACK_DAMAGE` + `{physical}` |

### C. 接线分叉（语义同，接不同属性）—— 需归一决策，归 Phase 3

| # | 常量 A | 常量 B | 接线差异 |
|---|---|---|---|
| 16 | `INC_ATTACK_DAMAGE` (3) | `INC_DAMAGE_ATTACK` (1) | A 接 `Attributes.ATTACK_DAMAGE`（**无 scope**）；B 接 `AttributeRegister.DAMAGE` + `{attack}` scope。同一语义两种不归一接线 |

## 误报排除（2 组，非重复，不动）

- `INC_ATTACK_DAMAGE_FOR_EACH_DIFFERENT_NON_INSTANT_SPELL_...` vs `INC_SPELL_DAMAGE_FOR_EACH_DIFFERENT_NON_INSTANT_ATTACK_...`
  —— ATTACK/SPELL 主语互换，语义不同（attack damage per spell vs spell damage per attack）。
- `GAIN_OF_COLD_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE` vs `GAIN_OF_LIGHTNING_DAMAGE_AS_EXTRA_COLD_DAMAGE`
  —— COLD/LIGHTNING 互换**方向**，前者 cold→lightning、后者 lightning→cold，方向相反。

## 合并建议

- **安全合并 15 组（A+B）**：删 15 个零引用死别名声明（含紧邻 `//` 注释，不删保留者、不动任何引用点），门禁 `compileJava`。与 Phase 1 MERGE_LEGACY 同范式（已批准）。
- **组 16 接线分叉**：不在此轮机械删，归 Phase 3「已实现集重构归一」——决定合一接线（建议都接 `DAMAGE` + `{attack}` scope，或都接专用 `ATTACK_DAMAGE` 属性）后合并为一个常量。
- **误报 2 组**：不处理。

## 根因

生成器对同一条 `"+{X} to Accuracy"` 类描述产出双命名（`TO_` 与 `ADD_`），且 `TO_` 系列全零引用 —— 即 corpus 同义描述未去重。Phase 1 去 X 未触及此层（X 已在词中），故遗留为语义重复。**修法**应在生成器层对 flat/inc 同义描述做归一，但从根上消灭重复需 corpus 清洗（不在本次范围）。

## 下一步

待裁定：
1. 是否执行 **A+B 共 15 组安全合并**（机械删零引用死别名 + 修组6注释）？
2. 组 16 接线分叉的归一方向（接 `ATTACK_DAMAGE` 属性 还是 `DAMAGE`+`{attack}` scope）？
