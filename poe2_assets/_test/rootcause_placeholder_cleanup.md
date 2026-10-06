# 属性层根因清理报告（治标→治本的第一刀）

> 背景：用户指出「根据 ids 修复 datagen 接线是治标不治本——属性还是用的包含作用域/条件之类的非归一属性」。
> 根因定位：StatGen 只把属性 1:1 透传，根因在 `AttributeRegister` 属性层本身（名字焊死作用域/条件 + 43% 属性引擎零消费）。

## 1. 分类（三层证据，纯 python，脚本 `_normalize_classify.py`）

对 195 个 `AttributeRegister` 属性，用三层证据判定：

| 证据 | 含义 |
|---|---|
| `consumed` | `AttributeRegister.<X>` 在 src 中被引用（排除定义 + StatGen） |
| `node_refs` | 其 StatGen 条目 `StatIds.<ID>` 在 talent gen 目录被引用 |
| `json_refs` | 其 id 字符串在 `src/generated` + `src/main/resources` 的 json 中被引用 |

结果：

| 分类 | 数量 | 处理 |
|---|---|---|
| **IMPLEMENT**（核心，待补引擎 reader） | 4 | 保留：AREA_OF_EFFECT / DAMAGE / DEFEND_ARMOUR / TAKEN_DAMAGE |
| **REMOVE**（静默死，零引用） | 74 | 删 StatGen 条目，保留属性+StatIds 作预留 |
| 正常（有消费/有节点引用） | 其余 | 不动 |

## 2. 关键发现：归一化 ≠ 已接 reader，不能批量删

- `REVIVE_SPEED`（L1125 注释）：**之前已做过归一化**（"复活速度归一 = REVIVE_SPEED 基类 × 作用域"），但引擎 reader 未写 → 仍静默死。
- 74 个 REMOVE 里混了「已归一未接 reader」与「真占位机制」两类。
- 故**只删 StatGen 条目、保留 AttributeRegister 属性 + StatIds 常量**（符合项目约定「机制未实现→数据落地但置空」），不删属性本身，避免毁掉已归一结构与预留词表。

## 3. 已执行动作（`_remove_placeholders.py`）

- 删 **80 条** StatGen 条目（helper 形式 77 + 漏网 3：直接形式 `METRES_MELEE_STRIKE_RANGE`/`MOVEMENT_PENALTY_REDUCTION` + `sbMod` 形式的 `FOUND_ITEM_RARITY`；多行 `REVIVE_SPEED` 按区间删到 `;`）。
- 清 **110 个**陈旧 `stat_entry/*.json`（`src/generated` + `build`）。
- **保留**：74 个 `AttributeRegister` 属性声明 + 80 个 `StatIds` 常量（预留，落地但空，待补 reader）。
- StatGen 中 REMOVE 属性残留引用 = **0**。

## 4. 门禁

| 门 | 结果 |
|---|---|
| compileJava | ✅ BUILD SUCCESSFUL (30s) |
| runData | ✅ BUILD SUCCESSFUL (57s, exit 0；mixin WARN 为既有无关警告) |
| 生成产物 | stat_entry 总数 767（原 ~847 − 80 删除）；被删条目文件消失、保留条目存在、无 dangling |

## 5. 诚实结论与遗留（IMPLEMENT backlog）

本次消除的是「编译但通过即死」的**假接线面**——这正是用户说的"治标"一层。但：

- 属性**名仍非归一**（焊死作用域/条件），且**仍 reserved-empty**；
- 真正的根因是**引擎 reader 缺失**：这 74 个属性的机制从未被引擎读取。非数据层 tag 重写能解决，须 **per-mechanic 补引擎 reader（IMPLEMENT）**。
- 4 个 IMPLEMENT 核心属性同样待补 reader（如 `AREA_OF_EFFECT` 的范围缩放、`DEFEND_ARMOUR` 的防御管线）。

**下一步建议**（需你裁定优先级）：从 4 个核心 IMPLEMENT 属性起，逐个补引擎 reader，使其成为 live 数据；对确实永不实现的冷僻占位（`ADDITIONAL_STUN_THRESHOLD_EQUAL_2`、`MACE_SLAM_CAUSE_ADDITIONAL` 等），再彻底删属性+StatIds+节点接线。

## 6. 产物

- `poe2_assets/_test/_normalize_classify.py` — 分类脚本（三层证据）
- `poe2_assets/_test/normalize_classify.json` — 分类结果（IMPLEMENT/REMOVE 全量）
- `poe2_assets/_test/_remove_placeholders.py` — 条目清理脚本（DRY 先核，仅删条目）
- `poe2_assets/_test/attr_normalize_audit.md` — 初始根因地图
