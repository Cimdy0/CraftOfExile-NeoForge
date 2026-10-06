# Step15 — 以 StatIds 为主体的归一（Ids-driven，批次 6）

> 方法翻转：以每个 `StatIds`（真实数据点）为单位，查其 StatGen 写点（接到哪个属性 + 作用域标签），按 StatIds 语义名归一；而非从属性侧迭代。

## 一、全量 Ids 侧扫描（`_ids_driven.py`）
- StatIds 665 · StatGen 写点 386 · 被引用属性 149（**共享基类 37 / 1:1 占位 112**）。
- 对每个 1:1 占位属性尝试"基类 + 作用域"归并建议。

## 二、关键纪律（本次最重要发现）
> **作用域标签必须对齐属性的【读取端 scope】，而非 StatIds 名字里的词汇。**

反例（词法驱动会出错）：
| 属性 | 读取端 scope | 词法误补 | 后果 |
|---|---|---|---|
| `PROJECTILE_PIERCE` | `ArrowPiercing.java:47` 以 `{projectile,arrow}` 读 | 补 `{pierce}` | 条目 tags⊃读取scope → 不再匹配 → **损坏** |
| `ADD_MAX_MINION` | 玩家随从上限（非随从作用域） | 补 `{minion}` | 语义错（应为玩家作用域） |
| `ADD_MAX_TOTEM` | 玩家图腾上限 | 补 `{totem}` | 语义错 |
| `TAKEN_CRIT_DAMAGE` | `EntityGen.java:94` 运行时读取 | 补 `{crit}` | 动到运行时读取端，风险 |
| 多个 `*_DAMAGE_*` | 各自独立机制 | "MERGE→damage" | 全是假阳性（名含 damage ≠ 通用 damage 属性） |

## 三、安全落点（6 项，已应用 + 三道门全过）
| StatIds | 归一 | 类型 | 安全依据 |
|---|---|---|---|
| `TO_CRITICAL_HIT_CHANCE_OF_HERALD_SKILLS` | `CRIT_CHANCE` + `{herald}`（删 `critical_herald` 占位） | 基类合并 | 无运行时读取端；与 `ATTACKS_CRITICAL`→{attack} / `MINIONS_CRITICAL`→{minion} 同族 |
| `INCREASED_PARRY_RANGE` | +`{parry}` | 作用域补标 | `DamagePipeline.java:414` 即以 `{parry}` 读 `PARRY_RANGE`，仅此一读取端 |
| `INCREASED_STUN_RECOVERY` | +`{stun}` | 作用域补标 | StatGen-only 占位 |
| `TO_STUN_THRESHOLD_PER_STRENGTH` | +`{stun}` | 作用域补标 | StatGen-only 占位 |
| `BLIND_ENEMIES_WHEN_THEY_STUN_YOU` | +`{stun}` | 条件补标 | 原仅 `{blind}` 效果类，补被击晕条件 |
| `CHANCE_FOR_ATTACKS_TO_MAIM_ON_HIT_DAMAGE_AGAINST_POISONED_ENEMIES` | +`{attack}` | 主语补标 | 原仅 `{maim}` 效果类，补攻击主语 |

## 四、门禁指标（全绿）
| 门 | 结果 |
|---|---|
| compileJava | ✅ BUILD SUCCESSFUL |
| runData | ✅ BUILD SUCCESSFUL（851 stat_entry，真完成签名） |
| `_verify_batch6.py` | ✅ 6 落点正确（attribute=基类 + tags=作用域）+ 无 `critical_herald` 残留 JSON |
| `_plana.py` | ✅ `SPLIT/RISK/FIX/MIXED/ORPHAN/BARE-RISK = 0` |

> 验证脚本坑：`tags` 字段仅为**作用域标签**；基类是 `attribute` 字段（`coe:crit_chance`），**不是 tag**。初版 `_verify_batch6` 误将 `crit_chance` 期望进 tags → 假 FAIL；修正为查 `attribute` 字段后 PASS。

## 五、下一步
- 其余 1:1 占位若要继续归并/补标，**须逐属性核对读取端 scope 后**才能动手（不可词法驱动）。
- 安全方向：对已确认"仅 StatGen 写点、无运行时读取端"的占位，按 StatIds 语义补作用域标签（如本批 stun/blind/attack 类）；对"基类 + 作用域"清晰且读取端对齐者做合并（如 herald→crit_chance）。
- 环境提醒：`runData` 偶发长耗时（本次 11m）/ 守护进程 socket 超时假阴性，重跑前清 `.gradle/9.2.1/checksums/checksums.lock`。
