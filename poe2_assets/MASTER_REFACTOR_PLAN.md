# Stat / 属性层 完整重构计划（唯一权威版 · 终结轮回）

> 本文件是**唯一**执行依据，取代并合并以下三份（保留为证据附录，不再各自演进）：
> - `STAT_NORMALIZATION_PLAN.md`（Ids 去 X + 实现真值表 + 已实现集归一）
> - `ATTRIBUTE_LAYER_AUDIT_PLAN.md`（属性通道量化 + 三定律）
> - `ATTRIBUTE_STAT_EFFECT_WIRING_PLAN.md`（属性替代 StatEffect 越界）
>
> **数字全部来自只读静态审计脚本**（`_test/_dryrun_xstrip.py`、`_test/_audit_attribute_channels.py`），未改动任何源码。

---

## 0. 三条不可破的红线（轮回的成因，逐条封死）

| # | 红线 | 违反后果（历史教训） |
|---|---|---|
| R1 | **绝不读 / 回比 `runData` 产物（stat_entry JSON）** | 把生成 JSON 当真值 → 误接 StatGen 条目 + 不归一属性被反复重建 |
| R2 | **绝不删 `StatIds` 常量来"消除未实现"** | 删 StatIds = 删 bug 治 bug；未实现 = 保留 reserved-empty |
| R3 | **实现判定 = 引擎运行时是否真的读取**，不是"常量/文件/JSON 是否存在" | "文件存在即实现"的误判是所有误接的源头 |

**双真值定义**（本计划一切判断的地基）：
- **命名真值** = `gen_stat_ids.py` + 代码引用点（826 处），**不是** JSON。
- **实现真值** = 引擎读取端（`StatEngine.fold` 消费方 / `getAttributeValue` / `AreaDeliveryExecutor` / `DamagePipeline` / 事件处理器），**不是** 数据文件。

---

## 1. 现状真态（已核实，只读）

### 1.1 StatIds 层
| 项 | 值 |
|---|---|
| 常量总数 | **1587** |
| 带 `X` 参数标记 | **1041** |
| 无 `X` | **546** |
| 全仓引用点 | **826**（StatGen 602 / AdditionType 147 / StatusBehavior 32 / 三 StatusTypes 35 / SkillFactory 1） |
| 被引用的 unique 常量 | **589** |
| 零引用常量（reserved 候选） | **998** |
| 断裂引用（引用不存在的常量） | **0** ✅ |

### 1.2 「去 X」真实规模（`_dryrun_xstrip.py` 干跑，颠覆上次"合并失败"恐惧）
| 类别 | 常量数 | 引用点 | 处理方式 |
|---|---|---|---|
| **A. RENAME_CLEAN** 去 X 后名唯一 | 970 | **171** | 机械改名 + 改 171 引用点 |
| **B. MERGE_LEGACY** 去 X 后撞现存旧族 | 71 | **0** | 带 X 版本**全零引用** → 直接删，旧族保留，**不动任何引用点** |
| **C. COLLIDE_XX** X 常量间互撞 | **0** | 0 | **无冲突需人工定夺** ✅ |

> **结论**：去 X 是**机械、零硬冲突、仅 171 引用点改动**的操作。上次"误合并成 1733"是 `dict` 合并 + 命名粒度差异所致，与去 X 本身无关。这一步安全。

### 1.3 属性层（`_audit_attribute_channels.py`）
| 集合 | 数量 | 含义 |
|---|---|---|
| `AttributeRegister` 属性总数 | **121** | |
| StatGen 接线 `W` | 79 | 被 Stat 数据驱动写入 |
| 消费端直读 `R` | 50 | 被 `getAttributeValue` 读取 |
| 既接又读 `R∩W` | 31 | 正常 fold 属性 |
| **悬空直读 `R−W`** | **19** | 读但 StatGen 从未写 → 读注册默认值（绕过 Stat） |
| **无字面消费者 `W−R`** | **48** | 写了但无字面读取（含触发型 / 动态读取） |
| **零基 inc 静默死（确定 bug）** | **4** | `inc()`=ADD_MULTIPLIED_BASE 接 base=0 → 恒 0 |

---

## 2. 三定律（属性层接线宪法，后续每一步都要过）

- **定律 1（scope 通道）**：属性是 Stat 的 fold 容器，不是数据源。StatGen 条目带 scope 标签时，消费端读取点**必须**处于 `StatScopes.enter(scope)` 内；否则该 stat 在 EMPTY scope 下恒失效 = "直连读默认值"。
- **定律 2（触发型走 fire）**：一次性资源恢复 / 状态授予 / 伤害施加 / 几率触发**必须**走 `StatEffect.fire`（DamageEvent 接线），禁止"裸属性 + 消费端硬编码读取"替代。
- **定律 3（零基禁 inc）**：base=0 属性只能 `pctAdd`(ADD_VALUE)/`ADD_MULTIPLIED_TOTAL`，**禁** `inc`(ADD_MULTIPLIED_BASE)（fold 式 `base·ΣmultBase≡0`）。

---

## 3. 执行阶段（严格顺序，每阶段独立门禁 + 回退点）

### Phase 1 — 移除数值 X（命名归一）｜机械 · 低风险 · 可回退 ｜**首个执行**
**输入**：`gen_stat_ids.py`、当前 StatIds、826 引用点、`xstrip_dryrun.json`（改名映射真值）。
**动作**：
1. 改 `gen_stat_ids.py` 的 `to_name`/`to_id`：`{X}` → **直接剔除**（token 级删 `X`/`x`，合并下划线），同步改 HEADER 注释。
2. 备份 `StatIds.java` → `StatIds.java.pre_xstrip`。
3. 由 `xstrip_dryrun.json` 驱动（**不重跑生成器覆盖、不补回、不读 JSON**）：
   - B 类 71 个零引用带 X 常量：直接删声明（旧族已存在，无引用点受影响）。
   - A 类 970 个：按映射改常量声明名 + 批量改 171 引用点（StatGen/AdditionType/StatusBehavior/StatusTypes/SkillFactory）。
4. reserved 998 零引用常量**原样保留**（R2）。
**门禁**：`gradle compileJava` PASS；全仓 `StatIds.X_*` / `StatIds.*_X_*` 残留引用 = **0**；**不跑 runData、不比 JSON**。
**回退**：`StatIds.java.pre_xstrip` + git。
**产出**：无 X 的统一命名池，1587−71 = **1516** 常量（571 被引用 / 945 reserved）。

### Phase 2 — 实现真值表（引擎驱动）｜只读审计
**动作**：写 `_audit_impl_truth.py`，对每个常量扫引擎读取端，三态标注：
- `IMPLEMENTED`：StatGen 有条目 **且** 引擎确实读取。
- `WIRED_BUT_DEAD`：有 StatGen 条目但引擎从不读 → 候选 reserved，**不删**。
- `UNWIRED`：无 StatGen 无读取 → reserved-empty。
**门禁**：产出 `impl_truth.json`（源码事实表，取代"文件存在即实现"）。**不改代码**。

### Phase 3 — 已实现集分类筛选重构归一｜清基座（用户核心诉求）
**输入**：Phase 2 的 `IMPLEMENTED` 子集。
**为什么必须在增量接线之前**：Phase 5 在"已实现"上盖楼；基座若埋误接 Stat / 不归一属性，新接线等于在垃圾上盖楼，旧"回比"冲动会趁机重建。**先清基座，再往上接**。
**动作**：
1. **分类**：按「属性 × 作用域标签 × 机制族」建矩阵，同 `(属性, scope)` 聚为合并候选。
2. **筛选**（对照引擎读取端 scope）：
   - **误接 Stat**：StatGen 接的属性/作用域与读取端不匹配 → 运行时静默失效或范围错。
   - **不归一属性**：作用域焊进名，可折叠为「基类 + scope 标签」。
   - **计算符误用**：零基 inc 静默死（**含 §4-B 已定位的 4 处**）。
3. **重构**：逐条改接正确属性 / 补正确 scope / 换正确计算符，**语义驱动 + 反向 scope 对齐**（step15/16b 纪律），不词法臆造。
4. **归一**：同族折叠为「基类 + scope」（`AFTERSHOCK_CHANCE_MACE_SLAM` → `AFTERSHOCK_CHANCE + {mace,slam}`）；删冗余属性/StatGen 条目，**常量保留**（R2）。
**门禁**：`compileJava` + `runData`（仅再生 JSON，不回比）；Phase 6 静态门对基座**零误接/零不归一**。

### Phase 4 — 属性层归一（正交维度）
**动作**：以 `AttributeRegister` 头 16–51 行"事件 × 资源正交维度"为唯一规则，收敛剩余拆分（taken 族 / 按资源族）。无引擎读取点的属性不新建；残留死属性标 reserved 理由。同步处理 **§4-A/C 触发越界与悬空**：
- **A. 触发越界（3 处）**：`PUPPET_MASTER_CHANCE`→`GrantOnEventStatEffect`+删 SkillEvent 硬编码；`GLORY_GENERATION` 补消费者或标 reserved；`REMNANT_*` 待裁定（保留 #54 范式 / 迁 `create_ground_on_ailment`）。
- **C. 悬空直读（19）**：机制本体合法者逐条在 AttributeRegister 注释数据源；真待核（`CORRUPTION_ENDURES_CHANCE`/`AILMENT_THRESHOLD_ADAPTATION`）定源或标 reserved。
**门禁**：DEAD 属性 = 0；每属性 ≥1 读取点或 documented reserved。

### Phase 5 — 逐机制真实现（增量，用户定优先级）
**动作**：一次只接一个机制族（accuracy / armor / damage / area-of-effect / taken / …）。每批：① 确认引擎有 reader；② 建 StatGen 条目（零基禁 inc）；③ 接天赋节点/词缀；④ 门禁 `compileJava`+`runData`（不回比）。无引擎钩子的机制：常量保留 reserved-empty，不接不删。

### Phase 6 — 结构验收门（取代 JSON 回比，持续运行）
**动作**：静态审计（扫 `.java`，**不扫 JSON**）持续标记：残留 `StatIds.X_*`；StatGen 引用了引擎从不读的属性（误接检测器）；零读取且无 reserved 理由的属性；EMPTY 作用域加法；零基 inc。此门读源码不读产物，是阻断轮回的关键。

---

## 4. 真实缺陷清单（去噪音，供各 Phase 消费）

### A. 触发型越界（替代 StatEffect）— 已查实 3 处 → Phase 4
- `PUPPET_MASTER_CHANCE`：StatGen:324 接属性，`SkillEvent:166` 硬编码读属性授 buff → 应 `GrantOnEventStatEffect`。同族 EFFECT/DURATION 合法保留。
- `GLORY_GENERATION`：StatGen:1345 + 词缀全接，全仓**零消费者**（"Banner 读取"注释不实）→ 悬空热属性。
- `REMNANT_EXTRA_CHANCE`/`REMNANT_DOUBLE_CHANCE`：`RemnantState:52-53` 硬编码读 → 待裁定。

### B. 零基 inc 静默死（确定 bug，4 处）→ Phase 3 优先修
1. `INC_FOUND_QUANTITY`→`FOUND_ITEM_NUMBER`（base 0）[StatGen:1335]
2. `INC_FOUND_RARITY`→`FOUND_ITEM_RARITY`（base 0）[StatGen:1336]
3. `INC_INTELLIGENCE_2`→`INTELLIGENCE`（base 0）[StatGen:1377]
4. `WARCRIES_EMPOWERED_AN_ADDITIONAL_ATTACK`→`ADD_EMPOWERED_ATTACKS`（base 0）[StatGen:1097]
> **轮回实证**：#1/#2 是上一轮 #64"恢复断裂通道"用 inc 接 base=0 埋下的回归。修复 = 改 `pctAdd`(ADD_VALUE)。

### C. 悬空直读（R−W，19）→ Phase 4
- 机制本体合法（走 AttributeModifier 直挂）：WORK_SPEED/RAGE_LOSS_*/MAX_DARKNESS/…（应逐条注释数据源）。
- 真待核：`CORRUPTION_ENDURES_CHANCE`（DamagePipeline:552 读，StatGen 未接）、`AILMENT_THRESHOLD_ADAPTATION`（StatusEngine:470 读，未接）。

### D. scope 不匹配风险（W−R，48 中动态读取）→ Phase 6 审计
`ADDED_DAMAGE`/`DAMAGE`/`AREA_OF_EFFECT` 被动态 `getAttributeValue` 读取，需逐点核 `enter(scope)` 是否对齐。属定律 1 合规审计，非阻断。

---

## 5. 执行顺序与 go/no-go

```
Phase 1 去X ──[compileJava PASS + 0残留]──> Phase 2 真值表 ──[impl_truth.json]──>
Phase 3 清基座(含B类4处) ──[compile+runData PASS + 0误接]──> Phase 4 属性归一(含A/C) ──>
Phase 5 逐机制(用户定序) ──[每批 compile+runData]──> Phase 6 常驻静态门
```

每个 `[...]` 门禁不过则**停在该 Phase 修复**，绝不带病进入下一步。全程遵守 R1/R2/R3 三红线与三定律。

## 6. 需你拍板
- **A. 是否按本计划先执行 Phase 1（去 X）？** —— 已量化为机械低风险（0 硬冲突 / 171 引用点 / 71 零引用删除）。
- **B. Phase 5 首个机制族优先接哪个？**（accuracy / armor / damage / area-of-effect / taken）
