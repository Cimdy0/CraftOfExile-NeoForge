# Step 11 — 承受（TAKEN）后缀作用域归一收尾

日期：2026-08-20
前置：Step 10（EXTRA_*_FROM_* 多源合并）、2026-08-20「承受后缀可否作作用域」可行性分析。
目标：A 类 `*_EFFECT_TAKEN` 独立属性全部删除，承受侧统一走 `AILMENT_MAGNITUDE + {受方主题, taken}` 标签作用域。

## 一、迁移判定总表

| StatId（条目） | 旧落点 | 新落点 | 依据（读取端 / 攻方对称面） |
|---|---|---|---|
| `INCREASED_BUFF_EFFECT_OF_SHOCK_ON_YOU` | `SHOCK_EFFECT_TAKEN` | `AILMENT_MAGNITUDE` + `{ailment, shock, taken}` | AilmentHelper:378 `takenMagnitude`（已存在的运行时读取点，原为死条目 → 复活）；范式 = :835 BLEEDING_ON_YOU |
| `INCREASED_BUFF_EFFECT_OF_CURSES_ON_YOU` | `CURSE_EFFECT_TAKEN` | `AILMENT_MAGNITUDE` + `{curse, taken}` | 攻方对称 :1230 `INCREASED_CURSE_BUFF_MAGNITUDE`（`{curse, not(taken)}`，无 ailment 标签）；数据先行 |
| `INCREASED_BUFF_EFFECT_OF_WITHERED_ON_YOU` | `WITHERED_EFFECT_TAKEN` | `AILMENT_MAGNITUDE` + `{taken, withered}` | 攻方对称 :1707 `INCREASED_WITHERED_BUFF_MAGNITUDE`（`{withered, not(taken)}`，无 ailment 标签）；数据先行 |
| `INCREASED_EFFECT_OF_ARCHON_BUFFS_ON_YOU` | `ARCHON_EFFECT_TAKEN` | `AILMENT_MAGNITUDE` + `{archon, !taken}` | 读取端 StatusEngine:367 `archonBuffEffectMultiplier` 以 `{archon}` 作用域读 AILMENT_MAGNITUDE（**无 taken 标签**）；范式 = :792 ARCHON_BUFF_DURATION。原条目同属死条目 → 复活 |
| `INC_BUFF_EFFECT_TAKEN` | `BUFF_EFFECT_TAKEN` | `AILMENT_MAGNITUDE` + `{aura, taken}` | 承益侧通用增益，与 :851 `INC_BUFF_MAGNITUDE`（`{aura, not(taken)}`）攻方族对称；SuffixMod `BUFF_EFFECT_TAKEN_MOD` 消费，保留 StatId |
| `INC_INCISION_EFFECT` | `INCISION_EFFECT` + `{taken}` | **整条删除** | 语料 `stat_templates.txt:1120`「{X}% increased effect of Incision」为**攻方语义**（你施加的切割更强），与 :1409 `INC_INCISION_MAGNITUDE`（`AILMENT_MAGNITUDE + {incision, not(taken)}`）描述完全重复；按同描述严格去重原则保留 :1409，删除错接承受侧的重复项 |

## 二、代码变更

- `StatGen.java`：5 处 `inc`/`valueMod` 落点改写 + 1 处删除（:886），各带范式注释。
- `AttributeRegister.java`：删 6 属性 `CURSE/ARCHON/SHOCK/WITHERED_EFFECT_TAKEN`、`BUFF_EFFECT_TAKEN`、`INCISION_EFFECT`；A 规则头部注释更新——攻受双方读值发生在**不同实体**，同乘式可走同一归一属性（受方经 `takenMagnitude`/`takenDurationMul` 的 `{ailment, 异常名, taken}` 作用域），推翻旧「必须双属性分离」结论。
- `StatIds.java`：删 `INC_INCISION_EFFECT` 死常量。
- `StatusBehavior.java:290`：修正过时注释（`ARCHON_BUFF_EFFECT` 属性不存在，实际读 `AILMENT_MAGNITUDE`）。

## 三、泄漏分析（为何不会串扰）

- 匹配语义 = 条目正标签 ⊆ 读取作用域；`taken` 标签条目仅在作用域含 `taken` 时命中，攻方读取（`{curse}`/`{withered}`/`{aura}`/`{archon}` 无 taken）一律不漏。
- 攻方条目带 `not(taken)`，承受侧读取（`{ailment, X, taken}`）不吸攻方修正。
- `{aura, taken}`（承益）不会命中 BannerAction `{aura, banner}` 攻方读取（缺 taken）。

## 四、验收门

| 门 | 结果 |
|---|---|
| compileJava | ✅ EXIT=0 |
| runData | ✅ `removed stale: 1`（`inc_incision_effect.json` 自动清除，无手动清理）；stat_entry 总数 **851** |
| `_plana.py` | ✅ `SPLIT=RISK=FIX=MIXED=ORPHAN=BARE-RISK=0`（entries=851） |
| 生成物属性引用 | ✅ 6 个旧属性 id 在 stat_entry/*.json 中 0 残留 |
| 前轮成果回归 | ✅ `avatar_of_fire.json`、`extra_{fire,lightning,physical}_damage.json`、`inc_incision_magnitude.json` 均在且未回退 |

## 五、结论与后续

- taken-as-scope 归一**全部收尾**：异常/诅咒/枯萎/Archon/通用增益承受侧统一 `AILMENT_MAGNITUDE`，时长侧早已归一 `AILMENT_DURATION`。存量 B 类（`TAKEN_DAMAGE`/`TAKEN_CRIT_*`，加性/跨实体语义）维持独立属性，C 类机制占位待读取端接入。
- 惯例已写入 `AttributeRegister` A 规则注释：**新「X effect on you」语料一律 `AILMENT_MAGNITUDE + {taken, X}`，禁止新建 `*_TAKEN` 独立属性**。
- `INC_BUFF_EFFECT_TAKEN`（SuffixMod 承益词缀）当前无运行时读取点，属 Wave-9 数据先行；未来通用增益读取端建议以 `{aura, taken}` 作用域读 `AILMENT_MAGNITUDE`。
- 死属性清单中 `WITHERED`/`CURSE` 承受侧读取端仍缺（数据先行占位），机制接入时按上表作用域落读取即可。
