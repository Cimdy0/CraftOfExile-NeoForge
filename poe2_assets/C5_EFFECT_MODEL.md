# C5 — 修正后的接线模型：Stat 实现 = Effect × Condition × Target

> 由用户纠正触发：「属性缺口绝对不可能那么大」「与 Effect 对照，Stat 不一定需要属性实现，属性只是实现途径的一种」。

## 0. 一句话结论
之前报的「424 / 816 / 883 设计缺口」**全部结构性偏高**——因为它们只拿节点属性去对照 **AttributeRegister(109)**。
正确对照对象是 **全量 StatEffectIds 注册表（545 条 Effect）**，而属性（AttributeRegister）只是 Effect 的一种实现机制。
一个 Stat 被「实现」= 在 `StatGen.buildEntries` 里把它 `map.put` 到某个已注册 `EFF_*`（attribute 或非 attribute 均可）。

## 1. 三层接线原语（相互独立）
| 维度 | 注册表 | 规模 | 作用 |
|---|---|---|---|
| **Effect** | `StatEffectIds` / `StatEffectGen` | **545** | *做什么*：属性加成 / 伤害转换 / 状态授予 / 免疫 / 资源 / 充能… |
| **Condition** | `StatConditionIds` / `StatConditionGen` | **39** | *何时/何种状态下*生效：living-state / status / recent-event / equipment / socket / ground / presence-rarity |
| **Target** | `StatTargetIds` / `StatTargetGen` | **14** | *作用于谁*：`SELF_ONLY` / `INLINE_MINION` / `INLINE_TOTEM` / `COMPANION_INLINE` / `PRESENCE_ALLIES` / `PRESENCE_ENEMIES` … |

接线原语：
- `stat(target, effect)`            // 无条件的自身效果
- `stat(condition, target, effect)` // 条件 + 作用域组合

## 2. Effect 不止属性一种（关键证据）
`StatEffectIds` 545 条中：
- **395 条 attribute-based**（`ModifierStatEffect` 改写某个 `AttributeRegister` 属性）
- **150 条 non-attribute**（其他实现机制，属性根本不参与）：
  - 伤害转换：`armor_cold_damage_taken_convert_extra`、`chaos_damage_convert_fire_gain`…
  - 命中/事件授予状态：`caused_hit_grant_on_event_*_status`、`aggravated_caused_hit_grant_on_event_status`…
  - 免疫：`bleed_immune`、`blind_immune`、`EFF_IMMUNE_POISON`…
  - 充能/资源：`ally_grant_charge_convert_gain_charge`、`charge_convert_*_to_max`、`caused_meta_energy_energy_grant_on_event_resource`…

→ 之前只看 395 条 attribute effect，直接漏掉这 150 条，是缺口被高估的主因之一。

## 3. 作用域（actor/type）不是「新属性」，而是三选一
节点里的 `Minions / Attack / Spell / on You / Herald / Totem / Aura / 元素` 等限定词，承载方式：
1. **Effect 自身的 type 变体后缀**（最常见）：
   - `EFF_..._COE_CRIT_CHANCE` ↔ `EFF_..._ATTACK_..._COE_CRIT_CHANCE`（攻击爆击）
   - `EFF_..._COE_CRIT_CHANCE_MINION` / `_HERALD`（随从/ Herald 爆击）
   - `EFF_..._REGENERATION_RATE_HEALTH_REGEN` / `_TOTEM`（图腾再生）
2. **Condition（39）**：low life / full life / stationary / surrounded / bleed age / shock status / recently killed / on chilled ground…  
3. **Target（14）**：`TGT_INLINE_MINION` / `TGT_INLINE_TOTEM` / `TGT_COMPANION_INLINE` / `TGT_INLINE_PRESENCE_ALLIES`…

→ 之前把 `MINIONS_DEAL_INC_DAMAGE` 的 `minions` 当成「需要新属性」，实际是 `EFF_..._COE_DAMAGE` + `TGT_INLINE_MINION`，零新属性、零新 effect。

## 4. 已验证「看似缺口实为已覆盖」的样例（证据）
| 节点（StatIds 常量） | 真实落地方式 | 说明 |
|---|---|---|
| `INC_CRITICAL_HIT_CHANCE_FOR_ATTACKS` | `EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_CRIT_CHANCE` + `TGT_SELF_ONLY` | 攻击爆击变体已存在 |
| `MINIONS_HAVE_INC_MAX_HEALTH` | `EFF_ADD_VALUE_ATTRIBUTE_MINECRAFT_MAX_HEALTH` + `TGT_INLINE_MINION` | 作用域=Target |
| `OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH` | `EFF_HEALTH_RECOUP` + `TGT_SELF_ONLY` | 非属性 effect |
| `IMMUNE_TO_POISON` | `EFF_IMMUNE_POISON` | 非属性 effect |
| `INC_SHOCK_DURATION_ON_YOU` | `EFF_..._AILMENT_DURATION_SHOCK_TAKEN` | `_TAKEN`=承受侧作用域 |
| `INC_WARCRY_COOLDOWN_RECOVERY_RATE` | `EFF_..._COOLDOWN_RECOVERY_RATE` | 作用域 warcry 走变体/条件 |

## 5. 真正约束：op 族必须匹配（这才是少数真缺口）
即便 effect 概念存在，**op 族不对也不能接**（否则语义错误）：
- `INC_*`(increased) → 需 `ADD_MULTIPLIED_BASE` 变体
- `TO_*` / `ADD_*`(flat) → 需 `ADD_VALUE` 变体
- `MORE_*` → 需 `ADD_MULTIPLIED_TOTAL` 变体

例：`INC_HEALTH_REGENERATION_RATE` 只有 `EFF_..._VALUE_..._REGENERATION_RATE_HEALTH_REGEN`（flat），
**没有** `MULTIPLIED_BASE`（inc）变体 → 这是真缺口，需先设计 inc 变体 effect，不能拿 flat 顶替。
同理 `MINIONS_HAVE_INC_MAX_HEALTH` 的 `MAX_HEALTH` 仅 `ADD_VALUE`，缺 inc 变体 → 真缺口。

## 6. 由此得出的正确工作流（替代「批量猜测」）
1. **按核心概念族聚类**未接线节点（damage / crit / 再生 / recoup / ailment-duration / speed / resistance / charge…）。
2. 对每族确定：**是否已有对应 Effect（含 op 族）**？
   - 有 + 作用域可解析（变体/条件/目标）→ **直接接线**（复用已有 EFF，零新注册）。
   - 有 Effect 但缺某个 op 族变体 → **设计一个新 effect 变体**（同属性/同机制，仅 op 不同），再接线。
   - 无任何 Effect → **设计新 effect**（真设计缺口，需你的语义裁定）。
3. 作用域词统一走 `Condition(39)` / `Target(14)` / 既有 type 变体，不新建属性。

## 7. 当前门禁基线
- `StatGen.buildEntries` map.put：**739**（本轮 C4 接入 3 条穿透家族，已 verify）
- 落地 entry 总数：**805**
- effect / target / condition：**545 / 14 / 39（不变，零新增 effect 注册 → R3 合规）**
- `compileJava` / `runData` / `verify_gen.py`(13/13) 全过。

## 8. 下一步（由你定）
机械 matcher 只能当 scout（异构词表下不可信，样例见 §4/§5），真缺口须按 §6 逐族设计。
我建议从**高频且概念已覆盖**的族先铺（damage/crit/ailment-duration/speed），作用域一律走变体+条件+目标；
仅对确实缺 op 变体或全新概念的族（如 inc 版 regen、any/all attributes）等你确认语义后再补 effect。
要我直接给出「按族分解 + 可立即接线清单」吗？
