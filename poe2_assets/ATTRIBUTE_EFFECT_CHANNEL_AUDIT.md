# 属性 Stat 通道 × 引擎 Effect 类型 交叉比对报告

> 目的：对用户关心的候选属性（Q3 magnitude/duration 族 + 两个真冗余项），抽取其在 `StatGen.java` 的
> 全部条目通道，判定每条通道使用的 **Effect 类型**；再对照引擎已有 `StatEffect` 实现类，界定
> 「归一化属性」该修的范围——区分「仅 scope 折叠（FOLD，可安全归一）」vs「本应走 Effect 直接通道
> （事件/转换/免疫…，超出纯属性归一）」。
> 纪律：本轮**只读不改**（用户「先保留不修复」）；R1 不读 runData JSON；R3 实现判定＝引擎真读。
> 脚本：`poe2_assets/_test/_audit_attr_effect_channels.py` → `attr_effect_channel_audit.json`

---

## 0. 结论（先给）

- **候选属性 5 个 + 边界项 1 个，全部通道 100% 为 FOLD（ModifierStatEffect 读时聚合）**，零 FIRE / 转换 / 免疫 / 掷骰 / 光环等直接通道。
- 引擎 21 个 StatEffect 实现类中，**没有任何「magnitude / duration / charm / aura 专属型」**——这些维度本就是 ModifierStatEffect 属性通道。
- ⇒ **两个真冗余项（AURA_MAGNITUDE / CHARM_DURATION）的修复纯属 scope 折叠（重定向属性引用 + 删死声明），不触及任何 Effect 类型、不重接事件**。修复范围与上一轮报告（`ATTRIBUTE_SEMANTIC_SCOPE_AUDIT.md`）一致，**未扩大**。
- 受动侧（承受侧）语义不丢：`AILMENT_MAGNITUDE` / `AILMENT_DURATION` 已各自带 `{TAKEN}` 分支条目（如 `INC_IGNITE_MAGNITUDE_TAKEN` / `INC_BLEED_DURATION_TAKEN`），合并后承受/施加双侧均保留。

---

## 1. 引擎 StatEffect 类型全景（21 类，11 种 EffectKind）

来源：`src/.../codec/support/stat/effect/`，由 `StatEffect.EffectKind` 枚举分类。

| EffectKind | 实现类 | 触发语义 |
|---|---|---|
| **FOLD** | `ModifierStatEffect` | 读属性时按作用域即时聚合（`StatEngine.fold`）。**属性通道唯一类型**。 |
| **FIRE** | `StatusStatEffect` / `ResourceStatEffect` / `RecoupStatEffect` / `LeechStatEffect` / `GrantOnEventStatEffect` / `DamageStatEffect` / `DamageTakenConvertStatEffect`* / `CreateGroundOnAilmentEffect` / `ChargeConvertStatEffect` / `ChanceOnEventStatEffect` | 事件（`StatEngine.fire`）过筛后执行 |
| **DAMAGE_CONVERT** | `DamageConvertStatEffect` / `RandomConvertStatEffect` | 伤害类型重路由 |
| **DAMAGE_TAKEN_CONVERT** | `RandomTakenConvertStatEffect` | 承伤转化 |
| **RESOURCE_CONVERT** | `ResourceConvertStatEffect` | 扣费前重定向 |
| **IMMUNE** | `ImmuneStatEffect` | `StatusEngine.apply` 查询免疫短路 |
| **LUCKY** | `LuckyStatEffect` | 伤害/暴击/闪避/格挡双掷 |
| **HIT_FLAG** | `HitFlagStatEffect` | 不可闪避/绝不暴击短路 |
| **DELIVERY** | `DeliveryStatEffect` | 额外投射物/穿透/范围（fold 型无副作用） |
| **AURA** | `PresenceAuraStatEffect` | 每 tick 在场光环 |
| **INERT** | `RequirementStatEffect` | 不进 StatStore，仅词缀生成器读取 |

> 属性通道只有 `ModifierStatEffect`（FOLD）。当一个属性通过 `inc/pctAdd/more/add/chance/valueMod/entityMod/regenMod/conditionMod/recoup` 接线时，其 effect 固定为 ModifierStatEffect。

---

## 2. 候选属性通道明细（StatGen 抽取）

| 属性 | StatGen 条目数 | Effect 类型分布 | 全 FOLD? | 备注 |
|---|---|---|---|---|
| `AILMENT_MAGNITUDE` | **34** | FOLD ×34 | ✅ | 通用 magnitude 维度（承载 28+ 作用域 incl. `{AURA}` `{CHARM}` 边界） |
| `AURA_MAGNITUDE` | **1** | FOLD ×1（`INC_AURA_MAGNITUDE`+{AURA}） | ✅ | 孤儿 entry：已接线但**全仓零消费者**（上一轮核实）→ 重定向即复活 |
| `REMNANT_MAGNITUDE` | **0** | — | n/a | 零接线 reserved（数据先行占位） |
| `AILMENT_DURATION` | **30** | FOLD ×30 | ✅ | 通用 duration 维度（承载 29 作用域 incl. `{CHARM}`） |
| `CHARM_DURATION` | **0** | — | n/a | **死属性**：StatGen 零条目；仅 `CharmEngine:194` 读恒为 1.0 的自己 |
| `CHARM_EFFECT`（边界） | 1 | FOLD ×1（`INC_CHARM_EFFECT`+{CHARM}） | ✅ | effect 为独立维度，`CharmStatusTypes:33` 消费 → **非冗余**（支持上一轮结论） |

**关键事实**：
- `AILMENT_MAGNITUDE` 已含 `{AURA}` 条目（`INC_BUFF_MAGNITUDE` + `{AURA,!TAKEN}`、`BANNER_SKILLS_HAVE_INC_AURA_BUFF_MAGNITUDE` + `{AURA,BANNER,!TAKEN}`）→ `AURA_MAGNITUDE` 的 `{AURA}` 通道与之完全重叠。
- `AILMENT_DURATION` 已含 `{CHARM}` 条目（`INC_CHARM_DURATION` + `{CHARM,!TAKEN}`）→ `CHARM_DURATION` 本应汇入此处，但 `CharmEngine:194` 误读死属性。
- 受动侧：`AILMENT_MAGNITUDE` 有 `INC_IGNITE_MAGNITUDE_TAKEN`(+{TAKEN,IGNITE}) 等；`AILMENT_DURATION` 有 `INC_BLEED_DURATION_TAKEN`/`INC_CHILL_DURATION_TAKEN` 等 → 合并不丢承受侧。

---

## 3. 交叉比对 → 修复范围界定

将候选属性通道的 effect 类型与引擎 StatEffect 类型交叉：

1. **是否需新增/改用某个 StatEffect 类型？** 否。magnitude/duration/charm/aura 维度在引擎中**没有**对应的非 FOLD Effect 类型，它们本质就是 `ModifierStatEffect`（属性通道）。故修复不可能、也不应是「改用 Effect 类型」，只能是「scope 折叠归一」。
2. **是否触发型（FIRE）误接属性？** 否。5 个候选属性 64 条通道全为 FOLD，无一条走 `StatEngine.fire`。即它们不是「本应走事件通道却写成属性」的越界项（与上轮 `ATTRIBUTE_STAT_EFFECT_WIRING_PLAN` 的 A 类越界不同）——它们是干净的属性折叠，只错在**作用域焊死成独立属性**。
3. **修复动作是否改变 effect 类型？** 否。
   - `AURA_MAGNITUDE`→`AILMENT_MAGNITUDE`+{AURA}：仅把 StatGen:882 的 `AttributeRegister.AURA_MAGNITUDE` 改为 `AILMENT_MAGNITUDE`，保留 `inc()` + `{AURA}` → 仍是 ModifierStatEffect FOLD，entry 从孤儿变活。
   - `CHARM_DURATION`→`AILMENT_DURATION`+{charm}：删死属性声明；`CharmEngine:194` 改读 `AILMENT_DURATION`（其 `{CHARM,!TAKEN}` 条目 `INC_CHARM_DURATION` 已就位）→ 仍是 FOLD，且修复「X% increased Charm Effect Duration 无效」bug。
4. **是否扩大修复范围？** 否。比对证明：
   - 两个真冗余项修复 = 纯 scope 折叠，**不引入 Effect 类型改动、不重接事件**。
   - Q3 其余 9 族（max/chance/gained/range/generation/recovery/rate/contribution 等）均为独立维度/资源池，同为 FOLD ModifierStatEffect（属性通道的通用形态），保持现状正确——与上一轮「保护集 DO_NOT_MERGE」结论一致。
   - 边界项 `CHARM_EFFECT` 为 FOLD + 单消费者，确认**非冗余**，不并入。

⇒ **最终归一化属性修复范围 = 2 项**（AURA_MAGNITUDE、CHARM_DURATION），与上一轮报告 §5 完全一致，**经 Effect 类型比对后未扩大、未缩水**。

---

## 4. 修复范围确认表（待用户裁定执行）

| 属性 | 通道性质 | 修复动作 | 是否触 Effect 通道 | 门禁 |
|---|---|---|---|---|
| `AURA_MAGNITUDE` | FOLD 孤儿 entry（1 条，零消费者） | StatGen:882 属性改 `AILMENT_MAGNITUDE`（留 `{AURA}`）+ 删声明 | 否（仍 FOLD） | compileJava |
| `CHARM_DURATION` | FOLD 死属性（0 条目，Engine 误读） | 删声明 + `CharmEngine:194` 改读 `AILMENT_DURATION` | 否（仍 FOLD，且修复误接 bug） | compileJava |
| `REMNANT_MAGNITUDE` | 0 条目 reserved | **保留**（数据先行占位，R2） | — | — |
| `CHARM_EFFECT` | FOLD 单消费者 | **保留**（独立 effect 维度，非冗余） | — | — |
| `AILMENT_MAGNITUDE`/`AILMENT_DURATION` | 通用维度（已含 aura/charm 分支） | **保留**（归并目标） | — | — |

---

## 5. 风险与边界

- `AURA_MAGNITUDE` 重定向后，`INC_AURA_MAGNITUDE` 这一 StatIds 仍被 AdditionType/词缀引用即生效（此前因无消费者静默死，重定向后走 AILMENT_MAGNITUDE 的 `{aura}` 折叠，被 AilmentHelper 等读取端消费）。
- `CHARM_DURATION` 删除后，若未来有代码再 `AttributeRegister.CHARM_DURATION` 会编译失败（正确拦截）；当前仅 `CharmEngine:194` 引用，已一并改读。
- 所有操作不读/不回比 runData JSON（R1），不删 StatIds 治未实现（R2）；`INC_AURA_MAGNITUDE`/`INC_CHARM_DURATION` 两个 StatIds 常量保留。
- 受动侧语义：合并后 `{TAKEN}` 分支条目仍挂在 `AILMENT_MAGNITUDE`/`AILMENT_DURATION` 上，承受侧 magnitude/duration 不受影响。

---

## 6. 产出文件
- 报告：`poe2_assets/ATTRIBUTE_EFFECT_CHANNEL_AUDIT.md`（本文件）
- 审计脚本：`poe2_assets/_test/_audit_attr_effect_channels.py`
- 审计数据：`poe2_assets/_test/attr_effect_channel_audit.json`
