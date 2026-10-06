# Step 10 — EXTRA_*_FROM_* 多源转伤条目合并

## 背景

AdditionType 中 3 个词缀常量（`EXTRA_FIRE_DAMAGE`、`EXTRA_LIGHTNING_DAMAGE`、`EXTRA_PHYSICAL_DAMAGE`）各自引用多个单源 `EXTRA_<T>_FROM_<S>` StatIds，形成 Effect 列表膨胀。与 AVATAR_OF_FIRE 四合一同理，底层均为 `DamageConvertStatEffect`（GAIN 模式），运行时用 `fromTags.contains(fromTag)` UNION 语义逐槽位匹配，支持多来源合并。

## 合并

| 合并前（12 常量） | 合并后（3 常量） | fromTags | to |
|---|---|---|---|
| EXTRA_FIRE_FROM_{PHYSICAL,FIRE,COLD,CHAOS} | `EXTRA_FIRE_DAMAGE` | {physical, fire, cold, chaos} | fire |
| EXTRA_LIGHTNING_FROM_{PHYSICAL,FIRE,COLD,CHAOS} | `EXTRA_LIGHTNING_DAMAGE` | {physical, fire, cold, chaos} | lightning |
| EXTRA_PHYSICAL_FROM_{PHYSICAL,FIRE,COLD,LIGHTNING,CHAOS} | `EXTRA_PHYSICAL_DAMAGE` | {physical, fire, cold, lightning, chaos} | physical |

保留单源条目（不合并）：
- `EXTRA_CHAOS_FROM_CHAOS` → Set.of(chaos) → chaos（仅混沌→混沌，单来源）
- `EXTRA_COLD_FROM_CHAOS` → Set.of(chaos) → cold（仅混沌→冰霜，单来源）

## 改动文件

| 文件 | 改动 |
|---|---|
| `StatIds.java` | 12 常量 → 3 常量（L1159-1196 → 3 行） |
| `StatGen.java` | 12 行 `extraDamageFrom()` → 3 个多源内联 `DamageConvertStatEffect.entry(Set.of(...), ...)` |
| `AdditionType.java` | 3 个多引用 `from(Kind, path, more...)` → 3 个单引用 `from(Kind, path)` |

## 判定依据

- **运行时语义**：`DamagePipeline.java:734` `conv.fromTags().contains(fromTag)` 逐槽位匹配——`Set.of(4类)` 分别命中物理/火焰/冰霜/混沌四个槽位，等价于 4 条单源条目。
- **零外部引用**：12 个 `EXTRA_*_FROM_*` 常量全工程零外部引用（仅 StatIds 定义 + StatGen 注册 + AdditionType 引用）。
- **AVATAR_OF_FIRE 先例**：上一轮已验证同样合并模式（4→1 多来源），runData + 守卫通过。

## 不合并的 AdditionType 多引用

以下 AdditionType 也使用多引用 `from(Kind, path, more...)`，但**不可合并**：

| AdditionType | 引用 | 不合并原因 |
|---|---|---|
| `GLOBAL_CRIT_DAMAGE` | ADD_CRIT_DAMAGE + ADD_CAST_CRIT_DAMAGE | 不同属性（攻击暴击 vs 施法暴击） |
| `ATTACK_ELEMENTAL_DAMAGE_INC` | INC_FIRE/COLD/LIGHT_DAMAGE | 不同属性（火/冰/电伤害各为独立 inc 属性） |
| `ADD_ELEMENTAL_RESISTANCE` | ADD_FIRE/COLD/LIGHT_RESISTANCE | 同属性不同标签作用域（{fire}/{cold}/{lightning}） |
| `ADD_MAX_ELEMENTAL_RESISTANCE` | 同上 | 同上 |
| `ATTACK_ELEMENTAL_SKILL_DAMAGE_INC` | INC_FIRE/COLD/LIGHT_DAMAGE_SPELL | 不同属性（法术火/冰/电） |

## 验收

- `compileJava` ✅ EXIT=0
- `runData` ✅（Stat Entries provider 完成，守卫通过）
- stat_entry 总数 857 → 847（-12 旧 +3 新 -5 stale 清理 = -10 净减）
- 新条目 JSON 结构正确：
  - `extra_fire_damage.json`: `{from:[cold,fire,physical,chaos], to:fire, mode:gain, hits_only:true, amount:$value}`
  - `extra_lightning_damage.json`: `{from:[cold,fire,physical,chaos], to:lightning, mode:gain, hits_only:true, amount:$value}`
  - `extra_physical_damage.json`: `{from:[physical,fire,chaos,cold,lightning], to:physical, mode:gain, hits_only:true, amount:$value}`
- 零 stale 旧文件残留
