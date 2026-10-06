# 属性层通道 / 归一 审计与改造总纲

> 配套：`STAT_NORMALIZATION_PLAN.md`（Ids 去 X + 实现真值表 + 已实现集重构归一 + 属性层归一 + 增量实现 + 静态验收门）
> 配套：`ATTRIBUTE_STAT_EFFECT_WIRING_PLAN.md`（属性替代 StatEffect 触发亚型越界）
> 审计脚本：`_test/_audit_attribute_channels.py`（纯读 .java，不读 JSON；本文件数字来自其输出）

## 0. 量化证据（只读核实，未改动任何代码）

`AttributeRegister` 当前 **121** 个属性常量。交叉比对 StatGen 接线点（`getAttributeValue(AttributeRegister.X)` 之外）与全仓消费端直读点：

| 集合 | 数量 | 含义 |
|---|---|---|
| StatGen 接线属性 `W` | 79 | 被 Stat 数据驱动写入 |
| 消费端直读属性 `R` | 50 | 被 `getAttributeValue` 读取 |
| 既接又读 `R∩W` | 31 | 正常 fold 属性 |
| **悬空直读 `R−W`** | **19** | 消费端读但 StatGen 从未写 → 读注册默认值（绕过 Stat） |
| **无字面消费者 `W−R`** | **48** | StatGen 写了但消费端无字面 `getAttributeValue`（含触发型 / 动态读取） |
| **零基 inc 静默死候选** | 26 | 属性 base=0 且被 `inc`/`valueMod` 接 |

去噪音后，真实缺陷集中在 §2 的 A/B/C/D 四类。

## 1. 根因：属性层有三套并存的"写入 / 读取"路径，纪律未收敛

1. **Stat 数据驱动**（应为主通道）：StatGen → StatEntry → STAT_STORE → `StatEngine.fold` 按当前栈顶 scope 聚合 → 消费端 `getAttributeValue` 自动获得 scope 内加成。
2. **原版 AttributeModifier 直挂**：词缀 `AdditionType`/天赋节点 `TalentNodeGen.attr` 直接 `entity.getAttribute(attr).addModifier(...)`，与 Stat 体系并存。机制本体属性（速度/资源）合法走此路，但 AttributeRegister 未逐条标注，审计盲区。
3. **触发型 StatEffect.fire 通道**：一次性资源/状态/伤害/几率授予，由 `DamageEvent` 事件驱动，消费端**不应**显式读属性。

"不通过通道直连读取" = 消费端在**错误/空 scope** 下 `getAttributeValue`，StatGen 写的带 scope 标签条目在 EMPTY scope 下恒不命中 → 等于读默认值。"属性未归一直接使用" = 命名/计算符违反正交维度（作用域焊死、零基 `inc` 静默死）。

## 2. 属性层三定律（后续接线必须遵守）

- **定律 1（scope 通道）**：属性是 Stat 的 fold 容器，不是数据源。StatGen 条目带 scope 标签时，消费端读取点必须处于 `StatScopes.enter(scope)` 内；否则该 stat 在 EMPTY scope 下恒失效，等价于"直连读默认值"。
- **定律 2（触发型走 fire）**：一次性资源恢复 / 状态授予 / 伤害施加 / 几率触发**必须**走 `StatEffect.fire`（DamageEvent 接线），禁止"裸属性 + 消费端硬编码读取"替代。
- **定律 3（零基禁 inc）**：base=0 属性只能接 `ADD_VALUE`(`pctAdd`)/`ADD_MULTIPLIED_TOTAL`，**禁止** `inc`/`ADD_MULTIPLIED_BASE`（fold 式 `base * ΣmultBase ≡ 0` 静默死）。`StatGen.inc()` 内部固定 `ADD_MULTIPLIED_BASE`，零基属性绝不可调 `inc()`。

## 3. 真实缺陷清单（去噪音）

### A. 触发型越界（替代 StatEffect）——已查实 3 处
- `PUPPET_MASTER_CHANCE`：StatGen:324 接属性，`SkillEvent:166` 在 Command 施放事件里硬编码读属性授予 buff → 应改 `GrantOnEventStatEffect`。
- `GLORY_GENERATION`：StatGen:1345 + 词缀全接，但全仓零消费者（"Banner 读取"注释不实）→ 悬空热属性。
- `REMNANT_EXTRA_CHANCE` / `REMNANT_DOUBLE_CHANCE`：`RemnantState:52-53` 硬编码读属性 → 接近 `chance_on_event` / `create_ground_on_ailment`。
（详见 `ATTRIBUTE_STAT_EFFECT_WIRING_PLAN.md`）

### B. 零基 inc 静默死（确定 bug，4 处）
`inc()` 接 base=0 属性，运行时恒 0：
1. `INC_FOUND_QUANTITY` → `FOUND_ITEM_NUMBER`（base 0.0 Percentage）[StatGen:1335]
2. `INC_FOUND_RARITY` → `FOUND_ITEM_RARITY`（base 0.0 Percentage）[StatGen:1336]
3. `INC_INTELLIGENCE_2` → `INTELLIGENCE`（base 0.0 Double）[StatGen:1377]
4. `WARCRIES_EMPOWERED_AN_ADDITIONAL_ATTACK` → `ADD_EMPOWERED_ATTACKS`（base 0.0 Double）[StatGen:1097]

> **轮回证据**：#1/#2 是上一轮"恢复断裂通道"（#64）时用 `inc` 接 base=0 的 `FOUND_ITEM_*` 引入的回归——修旧断裂的同时埋了静默死。修复 = 改 `pctAdd`（ADD_VALUE）。

### C. 悬空直读（R−W，19）——分两类
- **机制本体合法**（走 AttributeModifier 直挂或动态读取，非 Stat 驱动）：`WORK_SPEED`/`WORK_APTITUDE`/`RAGE_LOSS_RATE`/`RAGE_LOSS_GRACE`/`MAX_DARKNESS`/`MAX_INFUSION`/`DARKNESS_RESERVATION_LASTS`/`NO_SPIRIT_BASE_DARKNESS`/`BASE/CURRENT_ENERGY_SHIELD_RECHARGE`/`LEECH_RATE`/`PRESENCE_RANGE`/`ITEM_CAPACITY`/`SKILL_SLOT_COUNT`/`TAKE_NO_DAMAGE_VOLATILITY`/`REMNANT_MAGNITUDE`。这些属性由 Helper/机制自身读取，值来自机制 modifier，合法但**应逐条在 AttributeRegister 注释标注数据源**，消除审计盲区。
- **真待核悬空**（无数据源迹象）：`CORRUPTION_ENDURES_CHANCE`（`DamagePipeline:552` 读，StatGen 未接、词缀侧待核）、`AILMENT_THRESHOLD_ADAPTATION`（`StatusEngine:470` 读，StatGen 未接）、`REMNANT_DOUBLE_CHANCE`（触发越界，归 A）。

### D. scope 不匹配风险（W−R，48 中动态读取属性）
`ADDED_DAMAGE`/`DAMAGE`/`AREA_OF_EFFECT` 等被 `getAttributeValue(attr)` 动态变量读取（DamageAction:294 / StatAction / 数学节点），StatGen 带 scope 标签写入，但消费点是否 `enter` 对应 scope 需逐点核对。属"定律 1"合规审计，非当前阻断。

## 4. 改造 Phase（与 STAT_NORMALIZATION_PLAN 衔接，门禁统一用 compileJava，不读 JSON）

- **Phase-B（机械、低风险、可立即做）**：修 B 类 4 处零基 inc → 改 `pctAdd`。门禁 `compileJava`。不触 JSON、不轮回。
- **Phase-A（设计待裁定）**：A 类 3 处触发越界归位到 `StatEffect.fire`（GrantOnEvent / ResourceStatEffect / create_ground_on_ailment）。需先定 GLORY/REMNANT 机制消费者形态。
- **Phase-C（标注 + 接管）**：C 类机制本体属性逐条在 AttributeRegister 注释数据源；真待核悬空（CORRUPTION_ENDURES_CHANCE 等）定数据源或删。
- **Phase-D（scope 对齐审计）**：建消费端 `enter(scope)` 清单 vs StatGen scope 标签交叉表，标记"定律 1"违例点。

## 5. 下一步最小动作建议

建议**先落 Phase-B**（4 处零基 inc 静默死，确定性 bug、低风险、compileJava 验证），它直接修复 #64 引入的回归且不引发任何轮回。Phase-A/C/D 待你裁定优先级与机制消费者形态后再动手。

### 待你裁定
1. 是否先落 **Phase-B**（改 `pctAdd`）？
2. **Phase-A** 第一个归位点：`PUPPET_MASTER_CHANCE`（GrantOnEvent）还是 `GLORY_GENERATION`（ResourceStatEffect）？
3. C 类机制本体属性是否接受"AttributeRegister 注释标注数据源"作为消除审计盲区的手段？
