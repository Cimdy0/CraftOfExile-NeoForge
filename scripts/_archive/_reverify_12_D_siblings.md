# 12 条 D 类兄弟冲突项二次核验（2026-09-02 · 决定性结论）

## 方法
- 绕过 rg ignore 伪阴性：Python 直读 `src/`（而非 `rg`），确认 12 项 `StatIds` 全部已定义但 `map.put` 未接；12 兄弟全部已接且各有 EFF。
- 逐条 R3：回查原语义 + grep 引擎原语（RAGE_REGEN / KNOCKBACK 读点作用域 / ARMOR_BREAK 族 / 异常基础时长 / CHARGE_CONVERT rage / COMBO_RATE / WARCRY 下限 / recently-gained-rage COND / companion-flask 重定向 / deflect-bleed 防御钩子）。

## 结论：12 条无一条隐藏干净 B
兄弟已接 ≠ 项可零改动接。每条语义相对兄弟均有 **属性/机制/行为开关级实质分歧**，需新原语或为行为开关。

### D 类 3 条（行为开关，确证无读点，维持 D 不接）
| # | StatId | 缺口证据 |
|---|---|---|
| 5 | CANNOT_RECOVER_HEALTH_OTHER_THAN_FROM_HEALTH_LEECH | 全局治疗封锁+仅 leech 例外，无 heal-block 读点 |
| 7 | DEFLECTED_HIT_DAMAGE_CANNOT_INFLICT_BLEEDING_ON_YOU | 偏转命中禁流血防御钩子，无 DEFLECT/PREVENT_BLEED 读点 |
| 9 | NO_RAGE_EFFECT | 怒气效果清零行为开关，无禁用读点 |

### C 类 9 条（需新引擎原语，开工前须获授权）
| # | StatId | 缺口 |
|---|---|---|
| 1 | ALLIES_..._REGENERATE_RAGE_PER_SECOND_IF_YOU_HAVE_GAIN_RAGE_RECENTLY | 缺 RAGE_REGEN 属性+读点（兄弟用 HEALTH_REGEN） |
| 2 | AREA_SKILLS_HAVE_CHANCE_TO_KNOCK_ENEMIES_BACK_ON_HIT_DAMAGE | KNOCKBACK_CHANCE 属性存在（DamageAction.readKnockbackChance），读点非 skill-area 标签作用域，缺「area 技能」门控 → 最强 B 候选，须先验证 skill-area 标签在 readKnockbackChance 处是否入栈 |
| 3 | ARMOR_BREAK_ARMOR_ON_CRITICAL_HIT_WITH_SPELLS_... | ARMOR_BREAK 族仅 ARMOR_BREAK_TAKEN + MINION 专属 EFF；缺玩家/法术暴击破甲 EFF + spell-crit 门控（CAUSED_CRIT 仅 timer） |
| 4 | BASE_BLEEDING_DURATION_IS_SECOND | 异常时长仅 AILMENT_DURATION_TAKEN 乘算读点；基础时长硬编码常量，无 base-duration 属性 |
| 6 | CHANCE_THAT_IF_YOU_WOULD_GAIN_RAGE_ON_HIT_DAMAGE_... | CHARGE_CONVERT 仅 endurance 专属 EFF；rage 非 charge 系统，缺 rage 变体+机制 |
| 8 | HEALTH_RECOVERY_FROM_YOUR_FLASKS_ALSO_APPLIES_TO_YOUR_COMPANIONS | 缺 companion-flask 治疗重定向机制（兄弟为玩家 flask 乘算） |
| 10 | REGENERATE_RAGE_PER_SECOND_PER_RAGE_SPENT_RECENTLY | 缺 RAGE_REGEN 属性 + rage-spent 计数 COND |
| 11 | SKILLS_GAIN_COMBO_SLOWER | 缺 COMBO_GAIN_RATE 属性+读点（兄弟 GRANT_COMBO_ON_HIT 为事件平发） |
| 12 | WARCRIES_HAVE_A_MINIMUM_OF_POWER | 缺 warcry 能量下限（floor）读点（兄弟为 inc 乘算） |

## 与 INC_BLOCK_RECOVERY 先例对照
INC_BLOCK_RECOVERY 能纠错为干净 B，因项语义与既有 EFF（ARCHON_RECOVERY_RATE）**逐字等价**；本批 12 条项语义相对兄弟**均有实质分歧**，故无一可零改动接。sibling_scan 的 ⚠️兄弟信号对这 12 条是**误报**（兄弟用不同 EFF/属性）。

## 下一步
- 12 条维持 D/C 裁定，不接/不开工。
- C 类 9 条需新建原语——按「未授权不自选引擎方向」铁律，须逐条获授权后实施。
- 建议：先对最强 B 候选 #2 做 skill-area 标签作用域验证；再从语义最自洽单条 C 起步。

## 修正（2026-09-03）：#2 实为干净 B，已接线
- R3 深研发现引擎**已支持**技能标签作用域：`SkillStatScopes.enterAction` 把 `delivery.statTags()`（含 AREA/MELEE/PROJECTILE/ATTACK/SPELL，见 `AreaDeliveryExecutor` 的 `{AREA,MELEE,MINION}` 作用域）并入作用域栈；`DamageAction.readKnockbackChance` 在 `{knockback}` 作用域读 `KNOCKBACK_CHANCE` 时栈已 merge 外层 `AREA` → 仅范围技能生效。
- 故 #2 不是 C，而是**仅缺一个 `{area}` 作用域 EFF** 的干净 B：新增 `EFF_ADD_VALUE_ATTRIBUTE_COE_KNOCKBACK_CHANCE_AREA`（复用 KNOCKBACK_CHANCE 属性）+ `WireMisc` 一行 `self(...)`，零引擎改动。双门禁全绿（effect 765→766）。
- **可复用范式**：「X% [属性] 对 [技能类型] 技能」类条目，若 [属性] 已有 tag-scoped EFF 且 [技能类型] 是 `SkillStatScopes` 推送标签（AREA/MELEE/PROJECTILE/ATTACK/SPELL/ARROW），则可仅新增同属性异作用域 EFF + 一行接线完成（干净 B），无需引擎改动。一大批此前误归 C 的「for [skill-type]」条目可重新打开为潜在干净 B。
