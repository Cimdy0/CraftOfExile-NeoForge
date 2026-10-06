# StatIds 按 StatGen 实现流程分类排序（预估）

> 只读规划，未改码。脚本 `_test/_classify_statids_plan.py` → `classified_statids.json`（1516 行，按 layer/flow/mech/name 排序）。
> 标注均为 **EST（预估）**，非事实接线；供 P5 逐机制真实现批次排序使用。
> 纪律：红线条 R2（不删 StatIds 治未实现）、R3（实现=引擎真读）适用；本表为"应走哪条流程"的规划，非"已接"断言。

## 规模

- 常量总数 **1516**；已接(wired, ref>0) **589** / reserved **927**
- **Layer**：FIRE（触发型 StatEffect，事件执行）= **146** ；FOLD（ModifierStatEffect，读时聚合）= **1370**

## Flow 分布（对应 StatGen 接线方法族，已核源码）

| layer/flow | 计数 | StatGen 入口 | 计算符 / 语义 |
|---|---|---|---|
| FIRE/convert | 14 | `DamageConvertStatEffect`(GAIN/EXTRA) | 转伤 as extra |
| FIRE/dtc | 31 | `DamageTakenConvertStatEffect`(EXTRA/REROUTE) | 承伤转化/重路由 |
| FIRE/recoup | 4 | `RecoupStatEffect` | 回复转化率 |
| FIRE/leech | 17 | `LeechStatEffect` | 偷取 |
| FIRE/recover | 50 | `ResourceStatEffect`(heal) | 事件触发回复 |
| FIRE/grant | 30 | `GrantOnEvent`/`ChanceOnEvent` | 事件授予/几率 |
| FOLD/more | 25 | `more` | ADD_MULTIPLIED_TOTAL（乘算%） |
| FOLD/inc | 747 | `inc` | ADD_MULTIPLIED_BASE（加算%，**零基静默死**） |
| FOLD/chance | 101 | `chance`(→add) | flat 几率 |
| FOLD/regen | 1 | `regenMod` | 回复率 |
| FOLD/flat | 240 | `valueMod`/`add` | ADD_VALUE（flat 加） |
| FOLD/condition | 27 | `conditionMod` | 条件门控 |
| FOLD/entity | 7 | `entityMod` | 实体作用域（minion/totem/...） |
| FOLD/esRecharge | 11 | `esRechargeMod` | 护盾回充 |
| FOLD/unknown | 211 | — | 裸属性名/特殊修饰（IS_DOUBLED/CANNOT_BE/...），需人工定 flow |

## Mechanism 分布（top，驱动 P5 批次）

`other` 287 · `damage` 239 · `health` 146 · `speed` 111 · `crit` 87 · `ailment_elem` 79 · `armor` 78 · `mana` 49 · `energy_shield` 41 · `stun` 41 · `charge` 40 · `attack` 35 · `resistance` 30 · `rage` 27 · `accuracy` 27 · `evasion` 27 · `ailment` 20 · `spell` 18 · `charm` 18 · `attribute` 14 · `block` 13 …

## 关键观察（支撑 P5 排序）

1. **inc 占 FOLD 半壁（747）**：其中接**零基属性**者静默死（呼应 Phase 3 的零基 inc 4 处 + 属性层三定律）。P5 接 inc 族前须先确认目标属性 base>0，否则改 `pctAdd`。
2. **FIRE 146 个基本是 reserved（事件机制未接）**：convert/dtc/recoup/leech/recover/grant 都是"数据先行、引擎无读取钩子"——这正是 reserved-empty 的集合，P5 按机制族逐个补引擎读取端。
3. **unknown 211 个**：多为裸属性暴露（ACCURACY/ARMOR/...）或特殊修饰（翻倍/禁止/阈值），不归入标准 flow，需人工裁定（部分就是属性层直接引用，非 StatGen 条目）。
4. **机制族规模**直接给出 P5 优先级信号：damage(239)/health(146)/speed(111)/crit(87)/armor(78) 是最大族，应先接；charm(18)/flask(8)/ground(5) 等小族可批量收尾。

## P5 批次建议（按机制族，每批带真实引擎钩子）

1. accuracy(27) + evasion(27) — 小族，属性暴露明确，先验证
2. armor(78) + resistance(30) — FOLD/flat + pctAdd 为主
3. damage(239) — 最大族，含 inc/more/convert/dtc 多 flow，必须分 sub-flow 接
4. health(146) + mana(49) + energy_shield(41) — 资源族，含 recover/leech/recoup FIRE
5. crit(87) + speed(111) — 通用乘算/加算
6. ailment(20) + ailment_elem(79) + stun(41) — 状态族，含 chance/grant FIRE
7. charge(40) + rage(27) + glory(8) — 资源/充能族
8. attack(35) + spell(18) + projectile(10) + melee — scope 维度族
9. minion(7)/totem(7)/presence(5)/curse(7)/aura(7) — 实体/光环族
10. charm(18)/flask(8)/ground(5)/hazard(5) — 小子系统族

## 待裁定

- 是否认可此分类体系作为 P5 批次排序依据？
- P5 首个机制族（accuracy/evasion 小族先验证，还是直接攻 damage 大族）？
- unknown 211 个的处理（裸属性暴露归 flat / 特殊修饰单独建"修饰"流程 / 保留人工）？
