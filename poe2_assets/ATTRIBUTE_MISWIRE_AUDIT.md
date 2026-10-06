# 属性错接审计 · 检索归一提案

> 方法：解析 `AttributeRegister` 全 119 属性 + 全仓 `AttributeRegister.X` 消费端读取（getAttributeValue / readInScope / fold）+ stat_entry JSON 驱动关系，交叉 Stat 实现通道。
> 参照先例：`DamagePipeline.java:657`「取代原 SHOCK/WITHER 硬编码分支（通道统一）」——即承伤硬编码分支已折入 `TAKEN_DAMAGE` 的 `{taken}` scope fold。

## 结论：3 处真实错接（其余伤害类型限定读取均正确）

扫描确认：伤害应用（`DamagePipeline`）内的「伤害类型限定值回调」全部落在 `DAMAGE`/`TAKEN_DAMAGE` 基础属性的 scope fold（带 `fire`/`electrocuted`/`fully_broken_armor` 等标签），已是正确 Stat 通道，**无额外独立错接属性**。真实错接仅 3 处：

### A. `TOTEM_TAKEN_AURA` — 独立错接属性 → 折入 `TAKEN_DAMAGE`（用户指明范例）
- **现状**：base=0.0；声明 `AttributeRegister:341`；消费端 `DamagePipeline:672/675` 以 `readInScope(attacker, Set.of(), TOTEM_TAKEN_AURA)` 读取（无 scope）；`perTotem * totems` 计算图腾承伤倍率。
- **接线**：**当前全仓无任何 StatGen / 天赋 / affix 接线**（StatId `EACH_TOTEM_APPLIES_INC_DAMAGE_TAKEN_TO_ENEMIES_IN_THEIR_PRESENCE` 已定义但未使用，旧 `NotableNodes07` 接线仅存于 `_backup`）。即该机制**完全失效（恒 0）**。
- **错接本质**：独立属性充当「每图腾 increased Damage taken」累加器，应走统一 `TAKEN_DAMAGE` 的 `{totem, taken}` scope fold（对齐 SHOCK/WITHER 先例）。
- **归一方案**：
  1. 天赋节点（5580 Watchtowers）将 `StatIds.EACH_TOTEM_APPLIES_...` 接为 `inc(map, ..., TAKEN_DAMAGE, StatTag.TAKEN, StatTag.TOTEM)`（值 0.02/图腾）。
  2. `DamagePipeline:672/675` 改读 `readInScope(attacker, Set.of(StatTag.TAKEN, StatTag.TOTEM), TAKEN_DAMAGE)`（保留 `perTotem * totems` 动态计数逻辑）。
  3. 删 `TOTEM_TAKEN_AURA` 属性声明。
- **行为保持**：图腾计数逻辑不变；perTotem 来源从独立属性 → `TAKEN_DAMAGE{totem,taken}` fold，值源一致（天赋 0.02）。`StatTag.TOTEM` 已存在（`StatTag.java:111`）。
- **风险**：中（需改天赋节点接线机制 + runData 重生成）。

### B. `DAMAGE_TAKEN_BYPASSES_ENERGY` — 消费端绕过作用域（属性本身正确）
- **现状**：base=1.0；StatGen:922 以 `inc(..., {taken, energy_shield})` 接线（stat_entry `of_damage_taken_bypasses_energy_shield.json` 存在，`add_multiplied_base` + tags `[taken, energy_shield]`）；消费端 `DamagePipeline:514` 用 `getAttributeValue`（**无 scope**）。
- **错接本质**：`StatEngine.fold` 按当前作用域栈聚合 scope 条目（`StatEngine.java:220-230`），`getAttributeValue` 仅读基值 → `{taken, energy_shield}` 的 inc **被绕过，esBypass 恒 0**（「受击转能量护盾」affix 实际永不生效）。
- **归一方案**：`DamagePipeline:514` 改 `readInScope(s.living, Set.of(StatTag.TAKEN, StatTag.ENERGY_SHIELD), DAMAGE_TAKEN_BYPASSES_ENERGY)`。
- **行为保持**：与 StatGen 接线 scope 对齐，使 `{taken, energy_shield}` 加成生效。属性不删。
- **风险**：低（纯消费端读修复）。

### C. `CORRUPTION_ENDURES_CHANCE` — 纯孤儿 + 消费端绕过作用域
- **现状**：base=0.0；**无 StatGen 接线、无 stat_entry JSON**（纯孤儿）；消费端 `DamagePipeline:550-552` 用 `getAttributeValue`（无 scope）判「致死伤害按几率压至 1 HP」。
- **错接本质**：孤儿属性 + 作用域绕过双错。当前恒 0（「腐蚀续航」affix 永不生效）。
- **归一方案（二选一）**：
  - **C1 补接 Stat**：为 `CORRUPTION_ENDURES_CHANCE` 接 `chance(..., {corruption})` Stat fold + 消费端 `readInScope(living, Set.of(StatTag.CORRUPTION), CORRUPTION_ENDURES_CHANCE)`（需确认 `StatTag.CORRUPTION` 是否存在，否则新增）。
  - **C2 保留 data先行**：按 R2 保留属性、标注 `[数据先行]`，待 P5 补消费端（与既孤儿同类处理）。
- **风险**：中（C1 需新增 Stat 接线 + 作用域标签）。

## 其余 31 项「孤儿」判定（非错接，正常直读）
审计脚本标记的 31 项 `orphan_unwired` 多为**合法直读属性**（vanilla 风格，无需 Stat 通道）：`WORK_SPEED`/`WORK_APTITUDE`/`MAX_*`/`RESISTANCES`/`EVASION`/`ITEM_CAPACITY`/`SKILL_SLOT_COUNT`/`PRESENCE_RANGE`/`TALENT_*` 等。这些消费端用 `getAttributeValue` 直读是设计预期（非 scope 限定的 Stat 累加器），**不属错接**，不纳入本次归一。

## 待裁定
- A 是否执行（天赋节点接线机制需确认 `TalentNodeGen.stat` 类 API）。
- C 走 C1（补接 Stat）还是 C2（保留 data先行）。
- B 为明确 bug 修复，建议直接执行。

---

## 执行结果（2026-08-21）

三处错接全部归一完成；三道门禁（compileJava / runData / grep 验证）均通过。

### A — 修订：保留属性，走 `{totem,taken}` scope 通道（未折入 TAKEN_DAMAGE）
> **偏离原方案说明**：原提案要把 `TOTEM_TAKEN_AURA` 删属性、折入 `TAKEN_DAMAGE{totem,taken}`。
> 实际落地的承伤机制为 `perTotem(攻击方每图腾投资) × countTotemsNear(承伤方附近图腾数)`，
> 其中 `perTotem` 来自**攻击方（图腾主人）**，计数依赖**空间**。若折入受害方 `TAKEN_DAMAGE`
> （在受害方作用域读取），静态条目无法携带「攻击方投资 × 附近图腾数」的动态乘子，会丢失语义。
> 故保留 `TOTEM_TAKEN_AURA` 作为攻击方累加器，仅归一其通道：
- `NotableNodes07.java:997` watchtowers(5580) 节点 `effects` 由空恢复为
  `attrCond(TOTEM_TAKEN_AURA, 0.02, ADD_VALUE, List.of(), StatTag.TOTEM, StatTag.TAKEN)`。
- `DamagePipeline:675/678` 由 `readInScope(attacker, Set.of(), TOTEM_TAKEN_AURA)`（空 scope）
  改为 `readInScope(attacker/master, Set.of(StatTag.TOTEM, StatTag.TAKEN), TOTEM_TAKEN_AURA)`。
- 属性声明保留（`AttributeRegister:341`）；`StatIds.EACH_TOTEM_APPLIES_...` 仍仅作词表占位（未用）。

### B — 已完成（消费端作用域绕过修复）
- `DamagePipeline:514` 改 `readInScope(s.living, Set.of(StatTag.TAKEN, StatTag.ENERGY_SHIELD), DAMAGE_TAKEN_BYPASSES_ENERGY)`，
  与 StatGen:922 `{taken, energy_shield}` 接线对齐，使「受击转能量护盾」inc 生效。

### C — 已完成（C1：补接 Stat + 新增 `StatTag.CORRUPTION`）
- `StatTag.java:261` 新增 `public static final String CORRUPTION = "corruption";`。
- `NotableNodes02.java:270` corruption_endures(49769) 节点 `effects` 由空恢复为
  `attrCond(CORRUPTION_ENDURES_CHANCE, 0.07, ADD_VALUE, List.of(), StatTag.CORRUPTION)`。
- `DamagePipeline:554` 由 `getAttributeValue(CORRUPTION_ENDURES_CHANCE)`（恒 0）
  改为 `readInScope(s.living, Set.of(StatTag.CORRUPTION), CORRUPTION_ENDURES_CHANCE)`。

### 门禁指标
| 门禁 | 结果 |
|---|---|
| compileJava（JDK21 offline） | 通过（仅 deprecation/unchecked 提示，无错） |
| runData | 通过（Stat Entries / Talent Tree / Progression Data 等 provider 全部 finished，~3024ms） |
| grep 验证（死读零残留） | `getAttributeValue(CORRUPTION_ENDURES_CHANCE)` × 0；`readInScope(...,TOTEM_TAKEN_AURA)` × 2（均 `{totem,taken}`）；两节点 attrCond 接线 × 各 1；`StatTag.CORRUPTION` 定义+使用 × 3 |

### 下一步建议
- A 的「保留属性 vs 折入 TAKEN_DAMAGE」分歧已按语义正确性裁定（保留）；若后续确认该机制应建模为受害方承伤（而非攻击方光环），可再评估折入。
- 其余 31 项孤儿直读属性维持原判定（合法 vanilla 直读，不归一）。
- 若需把 `TOTEM_TAKEN_AURA` / `CORRUPTION_ENDURES_CHANCE` 也暴露给词缀/装备来源，可在 `StatGen` 增 `talentAttr`/affix 接线（当前仅天赋节点来源）。
