# 属性层 StatEffect 越界接线 — 审计与归位方案

> 配套 `STAT_NORMALIZATION_PLAN.md`。本方案专门处理用户指出的"属性里还有错误接线试图替代 StatEffect"。

## 1. 反模式定义（先立标准，再找证据）

StatEffect 有两亚型，职责边界由 `StatEffect.java` / `StatEffectTypes.java` 定义：

- **属性亚型**（`type=attribute` → `ModifierStatEffect`）：只承载**读时折叠的数值修饰**。由 `StatEngine.fold` / `readInScopePublic` / `getAttributeValue` 在读取点即时聚合。消费者必须把属性当**纯数值**用（伤害倍率、抗性、幅度、速度、上限、范围…）。
- **触发亚型**（fire / convert / `chance_on_event` / `grant_on_event` / `leech` / `recoup` / `resource` / `damage` / `status` / `create_ground_on_ailment` …）：负责**事件触发动作**（授予状态/资源/充能、造成伤害、转换伤害/资源、投放地面…），由 `StatEngine.fire` 过筛后执行。

**越界接线（本方案要消灭的）两类：**
1. `FIRE_BYPASS`：语义本该是触发亚型效果，却用 `ModifierStatEffect` 接一个属性 + 在**事件处理器**里硬编码"读属性 → 做动作"，绕过了 `fire` 通道。
2. `ORPHAN`：属性被 `StatGen`/`AdditionType`/`SuffixMod` 接了词缀，但**全仓无任何消费者**（注释常声称"某机制读取"但实际没有）——悬空热属性，伪装成"效果桥接"。

判据杜绝"文件存在即实现"：是否真正被读取端消费 = 真值。

## 2. 已查实的越界接线（本轮只读核实）

### A. `PUPPET_MASTER_CHANCE` —— FIRE_BYPASS（确认）
- 接线：`StatGen.java:324` `chance(map, X_SURPASSING_CHANCE_TO_GAIN_A_PUPPET_MASTER_STACK_WHENEVER_YOU_USE_A_COMMAND_SKILL, AttributeRegister.PUPPET_MASTER_CHANCE, "puppet_master")` → `ModifierStatEffect` 属性亚型。
- 消费：`SkillEvent.java:166` `tryGainPuppetMaster` 在「使用 Command 技能」施放事件里 `getAttributeValue(PUPPET_MASTER_CHANCE)` 后 `StatusEngine.apply(player, BuffStatusTypes.PUPPET_MASTER, ...)` 授予叠层 buff。
- 判定：语义 = "X% chance on [command skill] to gain [Puppet Master stack]"，**正是 `grant_on_event` / `chance_on_event` 触发亚型**。当前绕过 `fire` 通道，把逻辑焊死在事件处理器。属教科书级越界。
- 同族 `PUPPET_MASTER_EFFECT`（`inc_effect_of_puppet_master`）、`PUPPET_MASTER_DURATION`（`inc_duration_of_puppet_master`）：buff 幅度/时长缩放，纯数值 fold 修饰 → **合法属性亚型，保留**。

### B. `GLORY_GENERATION` —— ORPHAN（确认）
- 接线：`StatGen.java:1345` `valueMod(map, X_INC_GLORY_GENERATION, AttributeRegister.GLORY_GENERATION, ADD_MULTIPLIED_BASE, StatTag.GLORY)` + `AdditionType.GLORY_GENERATION_INC`（`AdditionType.java:178`）+ `SuffixMod.GLORY_GENERATION_MOD`（`SuffixMod.java:159-170`）。
- 消费：全仓扫描 `getAttributeValue(GLORY_GENERATION)` / `readInScopePublic(..., GLORY_GENERATION)` / `StatEngine.fold(..., GLORY_GENERATION)` → **零命中**。注释（AttributeRegister:259）声称"由 Banner 类技能读取放大生成量"不实。
- 判定：**悬空热属性**，伪装成"荣耀生成速率效果桥接"，实际无任何消费者。应补真正的荣耀生成生产者或删除接线（常量按"预留"保留，不删）。

### C. `REMNANT_EXTRA_CHANCE` / `REMNANT_DOUBLE_CHANCE` —— 待裁定（borderline）
- 接线：`StatGen.java:455` `chance(..., REMNANT_EXTRA_CHANCE, StatTag.REMNANT)`；`StatGen.java:1087` `inc(..., REMNANTS_COLLECTED_FURTHER_AWAY, REMNANT)`。
- 消费：`RemnantState.java:52-53` `drop()`（消耗灌入事件路径）读 `REMNANT_EXTRA_CHANCE`/`REMNANT_DOUBLE_CHANCE` 决定残迹数量/双倍；`:78` 读 `REMNANTS_COLLECTED_FURTHER_AWAY` 缩放拾取范围。
- 判定：语义趋近"消耗时几率造额外残迹 / 双倍"——接近 `chance_on_event` / `create_ground_on_ailment`。但注释（AttributeRegister:286-300、RemnantState:23-26）已明确辩护"#54 平属性 + getAttributeValue 范式为规避 REMNANT 作用域读取陷阱"。`REMNANT_MAGNITUDE` / `REMNANTS_COLLECTED_FURTHER_AWAY` 为纯数值倍率，**合法**。本组列为**待裁定**：保留（数值倍率语义）或迁移为 `create_ground_on_ailment` 变体。

## 3. 系统化检测（静态，不读 JSON，复用 `_audit_unreferenced.py` 纪律）

写 `poe2_assets/_audit_attr_as_effect.py`：
1. 抽取所有接线点源：`StatGen` / `TalentNodeGen` / `AdditionType` / `ValueMod` / `SuffixMod` 中出现的 `AttributeRegister.<X>`（含 `StatTalentEffect.of(AttributeRegister.X, ...)`）。
2. 对每个 `<X>`，全仓扫描消费者：`getAttributeValue` / `readInScopePublic` / `StatEngine.fold` / `StatEngine.compute` / `AttributeApplicator`。
3. 按消费者上下文分类：
   - `FOLD_OK`：消费者在数值修饰上下文（伤害倍率/抗性/幅度/速度/上限/范围/阈值…）→ 合法属性亚型。
   - `FIRE_BYPASS`：消费者位于事件处理器（`SkillEvent` / `DamageEvent` / `DamagePipeline` fire 段 / `KillHandle` / `RemnantState.drop` / `CharmEngine` 触发段…）且动作 = 授予状态/资源/充能/伤害/转换/投放 → 越界。
   - `ORPHAN`：无任何消费者命中 → 悬空。
4. 输出矩阵表：`属性 | 接线点(file:line) | 消费者(file:line) | 类别`。

> 扫描须排除 `.step*/.bak` 备份与 `poe2_assets/` 工作区噪音（step15 纪律）。

## 4. 归位方案（按机制，每批带真引擎钩子）

- **A**：`StatGen` 改 `GrantOnEventStatEffect.statusEntry(BuffStatusTypes.PUPPET_MASTER, $value, tags={command-skill 事件})`（或 `ChanceOnEventStatEffect`）；删 `SkillEvent.tryGainPuppetMaster` 里硬编码的属性读取，保留事件钩子调用 `StatEngine.fire`。`PUPPET_MASTER_EFFECT`/`DURATION` 保留为 fold 属性。
- **B**：先定 GLORY 生成语义——若"被动按比例生成"→ 在 `GloryState` 补 `fold` 消费者读 `GLORY_GENERATION`（合法属性亚型）；若"事件触发生成"→ 改 `ResourceStatEffect`/`grant_on_event`。当前无消费者，**先补消费者或删除接线**（常量保留为预留，不删）。
- **C**：待裁定（保留 #54 范式，或迁移 `create_ground_on_ailment`）。

## 5. 验收门（与全局方案一致）
- `compileJava` PASS；`runData` PASS（再生 `stat_entry`，**不回比 JSON**）。
- 静态复核：本批 `FIRE_BYPASS = 0`、`ORPHAN = 0`。

## 6. 待你裁定
1. 先建 `_audit_attr_as_effect.py` 全量矩阵（防漏），还是先只修 A、B 两个确认项？
2. B 的 `GLORY_GENERATION` 走「被动 fold 消费者」还是「事件 trigger 效果」？
3. C（`REMNANT_*`）保留还是迁移？
