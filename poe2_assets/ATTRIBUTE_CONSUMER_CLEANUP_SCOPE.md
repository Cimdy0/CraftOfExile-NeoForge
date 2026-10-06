# 属性消费端调用 × 清理范围补充审计

> 阶段：只读比对，不改代码（延续「保留修改，不修复」）。
> 目标：在上一轮「Stat 通道 × 引擎 Effect 类型」确认 2 项冗余=纯 scope 折叠的基础上，
> 进一步从**属性当前使用端（consumer-side）调用**出发，核实合并/删除安全性，并全量扫描补充清理范围。
> 硬约束沿用：R1 不读 runData JSON 回比｜R2 不删 StatIds/属性「未实现」｜R3 实现判定=引擎真读。

---

## 1. 消费端调用模型（三类路径）

| 路径 | 代码形态 | 被脚本捕获为 |
|---|---|---|
| ① scope-fold 读 | `StatEngine.fold(e, tags, AttributeRegister.X)` / `DamageHelper.readInScopePublic(e, tags, AttributeRegister.X)` | NORMAL |
| ② vanilla 直读 | `getAttributeValue(AttributeRegister.X)`（Helper / mixin） | NORMAL / CONSUMED_NO_WIRE |
| ③ 泛型迭代 | `TalentNodeGen.attr(List<Attribute>...)` 遍历传入的属性列表挂修饰（不引用具体常量） | 不现常量 → 易被误判孤儿 |

关键：属性要产生玩法效果，**必须**存在①或②的运行时读取。仅经③泛型挂修饰而无①/②读取 ⇒ 折叠值被计算但永不读取 ⇒ 功能悬空。

---

## 2. 全量扫描结果（122 属性）

脚本：`poe2_assets/_test/_audit_attr_consumers.py`，输出 `attr_consumer_audit.json`。

| 分类 | 计数 | 含义 | 清理含义 |
|---|---|---|---|
| DEAD_ZERO | 0 | 无任何引用 | 无（全属性至少接线或直读） |
| **ORPHAN_WIRED** | **12** | StatGen 接线但无 `AttributeRegister.X` 消费端引用 | **补充清理候选**（见 §4） |
| CONSUMED_NO_WIRE | 33 | 仅直读、无 StatGen 接线 | 保持（base 锚定型，如 REMNANT_MAGNITUDE / 天赋预留 / max-*） |
| NORMAL | 77 | 接线 + 读取齐备 | 保持 |

> 注：首次跑因 Windows 反斜杠路径导致全误判为 CONSUMED_NO_WIRE，已修 `rel.replace("\\","/")` 后重跑，结果如上。

---

## 3. Q3 候选族消费端详证（承接上一轮）

| 属性 | StatGen 接线 | 消费端读取 | 判定 |
|---|---|---|---|
| `AILMENT_MAGNITUDE` | StatGen:781/822/849/853（含 {AURA} 分支） | AilmentHelper / StatusEngine / SkillEvent 等 scope-fold | 通用维度，NORMAL |
| `AILMENT_DURATION` | StatGen:873（{CHARM}）/… | StatusEngine / CharmStatusTypes 等 | 通用维度，NORMAL |
| `AURA_MAGNITUDE` | StatGen:882（{AURA}） | **零消费端** | **孤儿 + 功能重复**（见 §4-Tier1） |
| `CHARM_DURATION` | 无（StatGen:873 路由到 AILMENT_DURATION） | 仅 CharmEngine:194 误读恒 1.0 | **死属性 + bug**（见 §4-Tier1） |
| `CHARM_EFFECT` | StatGen:875（{CHARM}） | CharmStatusTypes:33 `readInScopePublic({CHARM})` | 真实消费，保留 |
| `REMNANT_MAGNITUDE` | 无（StatGen 不引此常量） | RemnantState:54 `getAttributeValue`（base 锚定直读） | 独立维度，保留 |

---

## 4. 补充清理范围（消费端驱动）

### Tier 1 — 可安全清理（不违反 R2：非「未实现」，而是重复/死）

| 属性 | 消费端证据 | 修复动作 | 通道影响 |
|---|---|---|---|
| `AURA_MAGNITUDE` | 零消费端；同功能已由 `AILMENT_MAGNITUDE+{AURA}`（StatGen:853 `INC_BUFF_MAGNITUDE`）承载 | StatGen:882 改 `AILMENT_MAGNITUDE`（留 {AURA}）+ 删声明 | 仍 FOLD，{AURA} 入栈证据确凿（§5） |
| `CHARM_DURATION` | 无接线；CharmEngine:194 误读恒 1.0 死值；真实接线在 AILMENT_DURATION+{CHARM}（StatGen:873） | 删声明 + CharmEngine:194 改读 `AILMENT_DURATION`（已在 {CHARM} 作用域内） | 修复误接 bug，仍 FOLD |

### Tier 2 — 接线但无读取方（按 R2 保留，列入跟踪，不删）

以下 11 项 `ORPHAN_WIRED`：StatGen 词缀接线存在，但**无①/②运行时读取、无天赋节点引用**（否则 `AttributeRegister.X` 必现于消费端），折叠值当前无玩法效果。属「数据先行/待实现」，须逐条裁定「补读取方」或「显式标 reserved」，**本轮不删**：

`FOUND_ITEM_RARITY`(79) `FOUND_ITEM_NUMBER`(81) `AREA_OF_EFFECT`(87) `COOLDOWN_RECOVERY_RATE`(89)
`DEFEND_ARMOUR`(211) `GLORY`(215) `GLORY_GENERATION`(237) `THORNS`(249)
`STUN_RECOVERY`(251) `KNOCKBACK`(253) `CULLING_STRIKE_THRESHOLD`(255)

> 备注：`StatGen:1164` 已自注 `COOLDOWN_RECOVERY_RATE`「当前无技能侧生产者读取该属性，数据惰性落地」——印证 Tier2 为待实现而非死。

---

## 5. 合并目标 scope 通道核实（Task #81，坐实合并可行性）

**{AURA} 作用域运行时确实入栈**：
- `AuraDelivery.statTags()` → `Set.of("aura","area")`（AuraDelivery.java:34）
- `SpiritGemFactory:82/100` 光环/ Herald 宝石挂 `{AURA}` 标签
- `BannerAction.java:73` 入 `{AURA, BANNER}` 作用域
- `AILMENT_MAGNITUDE` 已接 `{AURA}`：StatGen:853 `INC_BUFF_MAGNITUDE`（ADD_MULTIPLIED_BASE）、:822 `BANNER_SKILLS_HAVE_INC_AURA_BUFF_MAGNITUDE`、:849 `INC_BUFF_EFFECT_TAKEN`
- ⇒ `AURA_MAGNITUDE → AILMENT_MAGNITUDE+{AURA}` 合并后，光环/buff 管线在 {AURA} 栈内读到的 magnitude 不丢语义。

**{CHARM} 作用域运行时确实入栈**：
- `CharmEngine.java:143/166/183/186/194` 全部 `StatScopes.enter(player, {CHARM})` 后读取护符属性
- `CharmStatusTypes.java:33` `readInScopePublic({CHARM}, CHARM_EFFECT)`
- `AILMENT_DURATION` 已接 `{CHARM}`：StatGen:873 `INC_CHARM_DURATION`（not taken）
- ⇒ `CHARM_DURATION → AILMENT_DURATION+{CHARM}` 合并后，CharmEngine:194 改读 `AILMENT_DURATION` 即能在 {CHARM} 栈内命中 `INC_CHARM_DURATION` 修正（**修复当前完全无效的护符时长加成**）。

---

## 6. 与上一轮 Effect 类型比对的交叉结论（范围未扩大）

- 候选 6 属性通道 **100% FOLD**（ModifierStatEffect 读时聚合），引擎 21 个 StatEffect 类型无 magnitude/duration/charm/aura 专属型。
- 本轮消费端取证进一步确认：Tier1 两项即使经消费端重定向（`AURA_MAGNITUDE→AILMENT_MAGNITUDE`、`CHARM_DURATION→AILMENT_DURATION`），读取方仍是 scope-fold，**[不新增/不改用任何 Effect 类型，不重接事件通道]**。
- 因此归一化修复范围与上一轮报告完全一致：**Tier1 = 2 项（纯 scope 折叠）**，未因消费端分析而扩大。

---

## 7. 待裁定

1. 是否执行 Tier1 两项安全合并（编译门禁 `compileJava`，不读 JSON、不删 StatIds、不触 Effect 通道）？
2. Tier2 的 11 项 ORPHAN_WIRED，逐条裁定「补读取方」还是「显式 reserved」？（R2 约束：本轮不删。）
3. 是否将 `_audit_attr_consumers.py` 纳入 `coe-corpus-stat-wiring` skill 作为「消费端孤儿扫描」标准步骤？
