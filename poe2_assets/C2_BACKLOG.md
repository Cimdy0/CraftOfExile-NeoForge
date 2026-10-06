# C2 天赋树节点 → StatGen entry 接线：完成状态与剩余 backlog（v3 更新）

> 生成日期：2026-08-27（v3 / v3.2）
> 权威源：`poe2_assets/poe2_tree_clean.json`（已裁剪）
> 纪律来源：`coe-corpus-stat-wiring` skill §14 三红线（R1 不读 runData 产物 / R2 不删 StatIds / R3 只复用已注册 effect）

## 一、已完成（安全零猜测切片，门禁全过）

### 门禁三连（每批必跑，当前状态）
- `compileJava` BUILD SUCCESSFUL
- `runData --no-configuration-cache` BUILD SUCCESSFUL
- `scripts/verify_gen.py` **13/13 ALL CHECKS PASS**（effect 545 / target 14 / condition 39 不变；entry 802；全部 `$value`）

### 落地条目统计
- `StatGen.buildEntries`：`map.put` 计数 **736**（基准 711 + C 任务 25 条）。
- **零新增 effect 注册**（R3 合规：每条均复用 `StatEffectGen` 已注册 `EFF_*` 常量）。

### 三批落地明细
| 批次 | 条数 | 方法 | 条目 |
|---|---|---|---|
| 原 C（前序） | 6 | 短语精确匹配 | INC_ARMOR / INC_AREA_OF_EFFECT / INC_HEALTH_RECOVERY_FROM_FLASKS / TO_EVASION / TO_ARMOR / TO_ACCURACY |
| C3（v3） | 7 | 全量注册表字典 + 保守拼写桥接 | TO_STRENGTH / TO_DEXTERITY / TO_INTELLIGENCE / INC_DAMAGE / TO_MAX_ENERGY_SHIELD / TO_MAX_MANA / INC_MAX_ENERGY_SHIELD |
| C3.2（v3.2） | 12 | token-subset 候选 + 逐条签名审计 | INC_LIGHTNING_DAMAGE / INC_ARMOR_AND_EVASION / INC_WARCRY_SPEED / INC_FLASK_CHARGES_GAIN / TO_MAX_ENDURANCE_CHARGES / TO_MAX_POWER_CHARGES / TO_MAX_FRENZY_CHARGES / INC_DEFLECTION_RATING / INC_ATTACK_PHYSICAL_DAMAGE / INC_POISON_DURATION_ON_YOU / INC_DAMAGE_PER_CURSE_ON_YOU / INC_IGNITE_DURATION_ON_YOU |

### 方法演进（为何能安全推进到 25）
1. **v2（旧）**：字典仅从「已接线的 6 条」反推 `短语→SEG` → 封顶 6。
2. **v3（registry-driven）**：字典从**全量 545 条已注册 effect** 反推 `(属性短语, op)→EFF`，保守拼写桥接（lightning→light / critical→crit / maximum→max / armour→armor）→ 命中 6 CLEAN + 1 verify。
3. **v3.2（token-subset）**：节点属性 token 集 ⊆ 已注册 effect 的 SEG token 集即命中（多候选取最小超集），再对每候选**解析真实签名 (attribute, op, tags) 人工核验 op 意图匹配**。由此多命中 12 条（含 `_TAKEN` 作用域正确匹配 "on you"）。
   - 否决 2 个伪命中（纪律：不猜 op/scope）：`INC_ATTACK_COLD_DAMAGE`（节点"increased"但仅 `ADD_VALUE` 注册→扁平非乘算，语义错）、`INC_MANA_RECOVERY_RATE`（节点"Mana Recovery rate"通用，仅命中 flask 专用 effect→作用域错）。均留待设计/语义裁定。

## 二、剩余 905 模板（需语义/设计裁定，禁止批量臆造）

930 个需生成 entry 的模板 − 25 已落地 = **905 待办**，分三类：

### A. COMPLEX / 条件·作用域变体 —— 479 条
特征：含 `against bleeding enemies` / `while moving` / `per enemy ignited` / `on low life` 等条件/作用域从句。
处理原则（skill §3/§11）：不可表达的条件从句**丢弃并留 reserved**；可表达的须按 `StatTag`/`StatTarget` 维度接线（多数需新增条件/作用域条目或新 effect 注册）。**必须逐节点人工核验，不在批量脚本范围。**

### B. SIMPLE 无映射 —— 426 条
| 子类 | 条数 | 状态 | 出路 |
|---|---|---|---|
| B0 候选已接（v3/v3.2 已落） | 17→已落 15 | 完成 | — |
| B1 候选但语义否决（留裁定） | 2 | 待你裁定 | `INC_ATTACK_COLD_DAMAGE`（需 MULTIPLIED 变体）/ `INC_MANA_RECOVERY_RATE`（需通用 mana regen effect） |
| B2 真设计缺口（无注册 effect） | 424 | 设计 | 节点属性在 `StatEffectGen` 全量 545 中**零 token 命中** → 须先设计 `AttributeRegister` 属性 + `ModifierStatEffect` 注册 |

B2 高频缺口属性（节选）：`critical hit chance`×N、`shock duration`、`armour and evasion`（复合）、`life regeneration rate`、`chance to inflict ailments`、`amount of life leeched`、`armour/evasion/energy shield (equipped/shield/companion)`、`power charge duration`、`warcry cooldown recovery rate`、`damage with X skills` 等——均为复合/全新属性，需设计层补 effect。

## 三、通往"完整实现"的路径

生成器已做成**字典/候选驱动**（`gen_talent_entries_v3.py` 全量注册表字典 + `analyze_gaps.py` token-subset 二分 + `audit_candidates.py` 签名审计）。一旦你提供/确认：
1. **B2 的 424 条**：设计属性（`AttributeRegister`）+ effect 注册后，重新跑 `analyze_gaps.py` 即可一次性补接（复用同范式）。
2. **B1 的 2 条**：确认 op/scope（建 MULTIPLIED 变体或通用 mana-regen effect）。
3. **A 的 479 条**：逐节点（或按条件族）人工核验接线，不在批量脚本范围。

> 交付物：`stat_entry_spec.json`（930 模板规格）、`c3_wire.txt`（C3 候选片段）、`c3_gap.json`（441 SIMPLE 缺口）、`c3_gap_classified.json`（17 候选 / 424 真缺口二分）、`gen_talent_entries_v3.py`（全量注册表字典驱动）、`analyze_gaps.py`（token-subset 二分）、`audit_candidates.py`（签名审计）、`resolve_node_stats.py`（节点 stat→Ids+值 解析器）。
