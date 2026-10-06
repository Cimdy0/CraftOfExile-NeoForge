# StatIds 归一化与真·实现方案（终结轮回版）

> 目标：不再“生成 → 回比 JSON → 重建垃圾”的循环。
> 命名唯一真值 = `gen_stat_ids.py` + 代码引用；实现唯一真值 = 引擎运行时是否真的读取该 stat id。
> **绝不**把 `runData` 产物（stat_entry JSON）回比进接线逻辑——那是轮回的根源。

## 0. 现状诊断（已核实，只读，未改动任何源文件）

| 项 | 值 | 说明 |
|---|---|---|
| `StatIds.java` 常量总数 | **1587** | 4784 行（含注释） |
| 带 `X` 参数标记的常量 | **1083** | 由 `gen_stat_ids.py` 将 `stat_templates.txt` 的 `{X}` 占位转成 `x_` 前缀生成 |
| 无 `X` 的常量 | **504** | 历史手写扩展 / 旧族（`inc_*`、`arrows_pierce_an_additional_target` 等） |
| `StatGen.java` 引用 `StatIds` | **602** 处 | 约 985 条常量“无 StatGen 条目” |
| `AttributeRegister.java` | 约 122 条属性 | 历史已删 72 死属性；文件头 16–51 行有正交维度设计说明 |

**两套命名共存的根因**：`gen_stat_ids.py` 的 `to_name/to_id` 把 `{X}` 变成 `X`/`x` 前缀。
这正是 `X_TO_ACCURACY` 与手写 `TO_ACCURACY` 永远对不齐、无法去重合并的原因。

## 1. 轮回根因（为什么之前一直在原地打转）

旧循环：
1. 以 corpus（`poe2_tree.json` → `stat_templates.txt`）为 StatIds 真值；
2. `gen_stat_ids.py` 重新生成全量 id（带 `X`）；
3. 为“实现”，把生成的 `stat_entry` JSON **回比**到属性层 → 误接的 StatGen 条目 + 不归一属性被重建；
4. 每跑一轮，进度归零。

**断点**：实现与否的判定标准是“引擎运行时是否真的读取该 stat id”，不是“JSON 文件是否存在 / 常量是否存在”。回比 JSON 只会把垃圾重建。

## 2. 总原则（本方案与旧循环的分水岭）

- 命名唯一真值 = `gen_stat_ids.py` + 代码引用（**不是** JSON）。
- 实现唯一真值 = 引擎读取端（StatEngine.fold 消费方 / AttributeRegister 使用点 / AreaDeliveryExecutor / DamagePipeline 等），**不是**数据文件。
- 未实现 = 保留常量（reserved-empty）：**绝不删除、绝不乱接**。删除 StatIds = 删 bug 治 bug，禁止。
- 验收只做：源码静态审计 + `compileJava` + `runData`；**`runData` 仅由 StatGen 生成 JSON，产物不回读、不回比**。

## 3. 分阶段方案

### Phase 1 — 移除数值 X（命名归一，机械、可回退）【第一步执行】
1. 改 `gen_stat_ids.py` 的 `to_name`/`to_id`：
   - `t.replace("{X}", "X")` / `t.replace("{X}", "x")` → **直接剔除 `{X}`**（前导 `{X}` 则常量以次词起首；中间 `_x_` 去标记后合并下划线）。
   - 同步更新 `HEADER` 注释里“数字归一为 {X}”的描述。
2. 备份当前 `StatIds.java` 为 `StatIds.java.pre_xstrip`。
3. 跑 `gen_stat_ids.py` 全量重生成 → 所有常量无 `X`。
4. 生成 `old常量名 → new常量名` 改名映射（1083 受影响），脚本批量改 **全部** 引用点：
   `StatGen` / `AdditionType` / `StatusBehavior` / `SkillFactory` / 天赋节点 / `AreaDeliveryExecutor` 等。
   冲突时按生成器既有 `_2` 规则合并到单一常量（旧族名优先，corpus 端改指旧名）——这正是上次合并失败之处，去 `X` 后自然对齐。
5. **门禁**：`gradle compileJava` 通过（无任何 `StatIds.X_*` 残留引用）。
   - 此阶段**只跑 compile，不跑 runData、不比 JSON**。

### Phase 2 — 实现真值表（引擎驱动，非数据驱动）
- 写静态审计脚本扫引擎读取端，对每个 id 标注三态：
  - `IMPLEMENTED`：引擎确实读取；
  - `WIRED_BUT_DEAD`：有 StatGen 条目但引擎从不读 → 候选 reserved；
  - `UNWIRED`：无 StatGen、无引擎读取。
- 取代“文件存在即实现”的误判，切断轮回。

### Phase 3 — 已实现集分类筛选重构归一（用户建议新增，在重名去 X 之后）【清基座，再往上接】
- **输入**：Phase 2 标出的 `IMPLEMENTED` 子集（有 StatGen 条目且引擎真实读取）。
- **为什么必须这一步（用户洞察）**：Phase 5 要在“已实现”之上增量接线；若基座里还埋着误接 Stat 与不归一属性，Phase 5 会在垃圾上盖楼，且旧“回比 JSON”冲动会趁机重建它们。故**先清基座，再往上接**。本步同样只读源码 + 引擎读取端，**绝不读 JSON**。
- 动作：
  1. **分类（classify）**：对 `IMPLEMENTED` 集按「所接属性 × 作用域标签 × 机制族」建矩阵；同 `(属性, 作用域)` 的多个 stat 自动聚类为合并候选。
  2. **筛选（filter）**：对照引擎读取端 scope（StatEngine.fold 的 `currentStatContext`），标出三类问题：
     - **误接 Stat**：StatGen 接的属性/作用域与读取端不匹配 → 运行时静默失效或范围错（如 step15 `PROJECTILE_PIERCE` 反例：读取端以 `{projectile,arrow}` 读，按词法补 `{pierce}` 即损坏）；
     - **不归一属性**：作用域焊进属性名、可折叠为「基类 + 作用域标签」；
     - **计算符误用**：inc 作用于零基属性（base=0）→ 折叠式 `attributeBaseValue·ΣmultBase` 贡献恒 0（静默死）。
  3. **重构（refactor）**：对三类问题逐条修正——
     - StatGen 改接正确属性 / 补正确作用域标签 / 换正确计算符；
     - 严格**语义驱动 + 反向 scope 对齐**（step15/16b 纪律），不词法臆造基类；真正可并的“作用域_基类”需基类确已注册。
  4. **归一（normalize）**：同族可并的已实现 stat 折叠为「基类 + 作用域」（`AFTERSHOCK_CHANCE_MACE_SLAM` → `AFTERSHOCK_CHANCE + {mace,slam}` 范式），删冗余属性/StatIds 常量（保留预留语义，常量不删）。
- **门禁**：`compileJava` + `runData`（仅由 StatGen 再生，不回比）；Phase 6 静态门对基座**零误接 / 零不归一**。
- **产出**：干净、自洽、引擎真读的“已实现基座”，Phase 5 在其上增量接线，而非在其上重建垃圾。

### Phase 4 — 属性层归一（不归一属性，按正交维度）【紧随 Phase 3】
- 以 `AttributeRegister` 文件头 16–51 行“事件 × 资源正交维度”设计为唯一规则，收敛剩余拆分（taken 作用域族、按资源族等）。
- 与 Phase 3 的归一互补：Phase 3 收敛“已实现 stat 直接接的属性”，本步处理余下非已实现但已注册的属性的命名形态。
- 无引擎读取点的属性一律不新建；残留死属性标 reserved 理由。
- **门禁**：DEAD 属性 = 0；每个属性 ≥1 读取点或 documented reserved。

### Phase 5 — 逐机制真实现（每批带引擎钩子，由你定优先级）【增量、不一次性铺开】
- 一次只接一个机制族（accuracy / armor / damage / area-of-effect / taken / 等），每批：
  1. 确认引擎有该属性 reader；
  2. 建 StatGen 条目（按属性基数选 `ADD_VALUE` / `ADD_MULTIPLIED_BASE`，**零基属性禁 inc**）；
  3. 接天赋节点 / 词缀（`TalentNodeGen.attrCond` / `AdditionType`）；
  4. **门禁**：`compileJava` + `runData`（runData 仅由 StatGen 生成 JSON，不回比）。
- 无引擎钩子的机制：常量保留（reserved-empty），不接不删。

### Phase 6 — 结构验收门（取代 JSON 回比，持续运行）
- 静态审计（扫 `.java`，**不扫 JSON**）持续标记：
  - 残留 `StatIds.X_*` 引用（Phase 1 未完成）；
  - StatGen 引用了引擎从不读的属性（**误接的 Stat 检测器**）；
  - 零读取且无 reserved 理由的属性（不归一残留）；
  - EMPTY 作用域加法（StatEngine.fold scope-aware 违规）；
  - 零基属性上用 inc（静默死）。
- 该门读源码、不读产物，是阻断轮回的关键。

## 4. 与旧方案的关键差异（一句话）

| 维度 | 旧循环 | 本方案 |
|---|---|---|
| 命名来源 | corpus JSON | `gen_stat_ids.py` + 代码引用 |
| 实现判定 | JSON 文件存在 | 引擎运行时读取 |
| 未实现处理 | 删除常量（删 bug 治 bug） | 保留 reserved-empty |
| 数据产物 | 回比进接线（重建垃圾） | 只生成，不回读 |
| 验收 | 比对 JSON | 源码静态审计 + compile/runData |
| 基座 | 直接在其上盖楼 | Phase 3 先清基座再接 |

## 5. 需你拍板
- A. 是否按本方案执行，先落地 Phase 1（去 X）？
- B. Phase 5 第一个机制族优先接哪个？（accuracy / armor / damage / area-of-effect / taken）
