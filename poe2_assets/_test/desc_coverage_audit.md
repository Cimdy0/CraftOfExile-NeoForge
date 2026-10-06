# §62 描述覆盖率审计（stat/note 接线 + 描述覆盖）

> 触发：read 构造（四段彻头彻尾 read）已闭环（见前序会话）。本轮回填「描述覆盖率」这一 backlog 维度——即 `StatIds` 词表（每条目上方 Java 注释 = 规范化 PoE2 stat 描述语料）经 `StatGen` 接线后，是否真能渲染出玩家可见的描述。
>
> 度量脚本：`poe2_assets/_test/desc_coverage_audit.py`（纯静态分析，复用 `_audit_coverage.py` 风格）。

## 背景：描述系统的两层来源

1. **canonical 描述语料**：`StatIds.java` 每条 `public static final Identifier X = id(...)` 上方注释即规范化的 PoE2 描述原文（如 `// +{X} to Armor`）。这是「stat/note」的 note 来源，共 **1517** 条（记忆中 602 已严重过时，词表已扩张至 1517）。
2. **运行时描述**：由 `StatEffect.describe(StatEntry)` 代码驱动重建，属性名取自 `Component.translatable("attribute.name." + attribute.getKey())`，`attribute` 为 `Holder<Attribute>`，`getKey()` 返回**带命名空间的 ResourceLocation**（`coe:armor`）；翻译键定义在 `src/generated/resources/assets/coe/lang/en_us.json`。

> 关键缺口：canonical 描述仅作 Java 注释，**未接到运行时**；运行时描述与 canonical 文本是两套独立来源，无自动对齐。

## 三轴度量与基线

### 轴1 接线覆盖（StatIds → 全工程写点）

| 指标 | 值 |
|---|---|
| StatIds 总声明（canonical 描述池） | 1517 |
| 已接线（全工程有 `StatIds.X` 引用） | 591 → **39.0%** |
| 孤儿（声明但零引用，描述永不现身） | 926 → 61.0% |
| 悬空引用（引用但未声明） | 0 |

### 轴2 属性名翻译覆盖（stat_effect 引用属性 → lang 键）

`stat_effect/*.json` 引用 **96** 个去重属性（`coe:<path>`）。

| 方案 | lang 键数 | 引用属性命中 | 覆盖率 |
|---|---|---|---|
| **真实方案**（describe 生成 `attribute.name.coe:<path>`） | 0（带 `coe:` 命名空间键） | **0/96** | **0.0%** |
| 兼容方案（`attribute.name.<path>`，无命名空间） | 117 | 14/96 | 14.6% |
| 纯缺失翻译键（两方案皆无） | — | 82 | — |

> **根因（轴2）**：`describe()` 生成**命名空间键** `attribute.name.coe:<path>`，而 `en_us.json` 仅定义**无命名空间键** `attribute.name.<path>`（0 个 `coe:` 键）。方案系统性不匹配 → 真实渲染覆盖趋近于 **0%**：所有属性名将泄漏为原始键（如 `attribute.name.coe:armor`），除非存在未发现的 key stripping 路径（需运行时确认）。
>
> 即便修正方案匹配，仍有 **82** 个引用属性**纯缺失翻译键**（如 `armor`/`crit_chance`/`damage`/`area_of_effect` 等均不在 lang 中）。

### 轴3 孤儿描述（canonical 描述永不现身）

926 条 StatIds 带规范化描述注释但全工程零引用 → 这些描述在游戏中无任何渲染机会。

### 复合描述覆盖率

近似 = 接线率(39.0%) × 真实属性翻译率(0.0%) ≈ **0.0%**。
（即使取乐观的兼容方案 14.6%：39.0% × 14.6% ≈ 5.7%。）

## 根因小结

| # | 根因 | 影响 | 置信度 |
|---|---|---|---|
| R1 | 键方案不匹配：describe 用 `getKey()`（命名空间）拼键，lang 仅有无命名空间键 | 全部属性描述名泄漏为原始键 | 高（静态证据确凿；运行时 stripping 路径未发现，待确认） |
| R2 | 82 个引用属性纯缺失翻译键 | 即便修 R1，这些属性仍无名称 | 高 |
| R3 | 926 条 StatIds 孤儿（有描述无接线） | canonical 描述永不现身 | 高 |

## 修复 backlog（§63–§68 节奏）

| 轮次 | 任务 | 复杂度 | 门禁 |
|---|---|---|---|
| §63 | **R1 键方案对齐**：将 `ModifierStatEffect.describe` 的 `attribute.getKey()` 改为取 path（`getKey().getPath()` 或 `unwrapKey().get().getPath()`），使查找键与 lang 无命名空间键一致；全量回归 attribute 描述。 | 低 | compileJava ✅ / runData ✅ / 运行时抽查验名 |
| §64 | **R2 补 82 缺失翻译键**：为 stat_effect 引用的 82 个属性在 `en_us.json`（及 `zh_cn.json`）补 `attribute.name.<path>` 条目，对齐 `AttributeRegister` 注册名。 | 中 | runData ✅ / 抽查验名 |
| §65 | **R3 孤儿 StatIds 处置**：926 条未接线 StatIds 二选一——(a) 在 `StatGen` 补写点接线；或 (b) 确认确为废弃语料后从 `StatIds.java` 清理（重跑 `gen_stat_ids.py` 收敛）。 | 高 | compileJava ✅ / runData ✅ |
| §66 | **canonical↔运行时对齐**：建立 `StatIds` 注释描述与 `describe()` 输出的对齐校验（避免两套来源漂移），或把注释作为生成 lang 描述的数据源。 | 中 | 新增校验脚本 |
| §67 | **effect 型 describe 覆盖**：逐 effect 子类型核查 `describe()` 是否对所有输入形态产出非空文本（如空 conditions / 表达式 amount 形态）。 | 中 | 静态 + 抽验 |
| §68 | **门禁固化**：将三轴度量纳入 `desc_coverage_audit.py` 常驻，CI/本地跑门禁时一并校验覆盖率不低于阈值。 | 低 | 脚本常驻 |

## 门禁（本轮）

| 门 | 结果 |
|---|---|
| compileJava | ✅（本轮仅静态分析 + 文档，零代码改动） |
| runData | ✅（产物未变） |
| 度量脚本 | ✅ `desc_coverage_audit.py` 可复跑，基线已落定 |

## 范围边界

- 本轮**仅度量 + 文档**，未改动任何 runtime 代码或 lang 数据。R1–R3 的修复留给 §63–§65。
- 轴2 的「0.0% 真实覆盖」为静态推断；若运行时存在未发现的 key stripping（如自定义 `IForgeRegistry` 描述 id 或资源包覆盖），实际覆盖可能高于此。建议 §63 修复前先做一次运行时抽查验名确认。
- 复合覆盖率近似公式未含「effect 型 describe 空文本」与「canonical 文本与运行时文本语义对齐」两因子，§67/§66 补。

## 结论

描述覆盖率当前**趋近于 0%**：接线仅 39%（926 孤儿），且已接线部分的属性名因**键方案系统性不匹配**（命名空间 vs 无命名空间）真实命中 0/96。这是描述系统的根因级缺口，非局部近似可解。§63 键方案对齐是性价比最高、最优先的修复；后续按 §64–§68 补齐缺失键、处置孤儿、固化门禁。
