# Step 9 — 按描述去重：AVATAR_OF_FIRE 四合一

## 1. 发现

用户指出 StatIds.java 中 4 个常量描述完全相同：

```java
// 75% of Damage Converted to Fire Damage Deal no Non-Fire Damage
public static final Identifier AVATAR_OF_FIRE_CHAOS_TO_FIRE = id("avatar_of_fire_chaos_to_fire");
// 75% of Damage Converted to Fire Damage Deal no Non-Fire Damage
public static final Identifier AVATAR_OF_FIRE_COLD_TO_FIRE = id("avatar_of_fire_cold_to_fire");
// 75% of Damage Converted to Fire Damage Deal no Non-Fire Damage
public static final Identifier AVATAR_OF_FIRE_LIGHT_TO_FIRE = id("avatar_of_fire_light_to_fire");
// 75% of Damage Converted to Fire Damage Deal no Non-Fire Damage
public static final Identifier AVATAR_OF_FIRE_PHYS_TO_FIRE = id("avatar_of_fire_phys_to_fire");
```

## 2. 核查

### 2.1 StatGen 接线

四条接线完全同构，仅 `from` 来源标签不同：

| 常量 | from | to | ratio | mode |
|------|------|----|-------|------|
| AVATAR_OF_FIRE_PHYS_TO_FIRE | PHYSICAL | FIRE | 0.75 | REPLACE |
| AVATAR_OF_FIRE_LIGHT_TO_FIRE | LIGHTNING | FIRE | 0.75 | REPLACE |
| AVATAR_OF_FIRE_COLD_TO_FIRE | COLD | FIRE | 0.75 | REPLACE |
| AVATAR_OF_FIRE_CHAOS_TO_FIRE | CHAOS | FIRE | 0.75 | REPLACE |

### 2.2 引擎语义

`DamagePipeline.java:734` 对每个伤害槽位取 `fromTag`，用 `conv.fromTags().contains(fromTag)` 判定是否命中：

```java
if (!conv.fromTags().contains(fromTag)) continue;  // UNION 语义
```

即 `fromTags` 是「命中集合」，`Set.of(physical, lightning, cold, chaos)` 会分别命中四个槽位。这与代码库既有约定一致（StatGen L492-510 早已用 `Set.of(全5类型)` 做单条多来源转换）。

**注意**：`DamageConvertStatEffect.java` 的 javadoc 原写 `slotTags.containsAll(fromTags)`（INTERSECTION 语义），与运行时实现**相反**，直接误导了最初判断。已修正为 `contains`（UNION）。

### 2.3 外部引用

四常量全工程零外部引用：
- `AVATAR_OF_FIRE_CHAOS_TO_FIRE` → refs=1, ext=0
- `AVATAR_OF_FIRE_COLD_TO_FIRE` → refs=1, ext=0
- `AVATAR_OF_FIRE_LIGHT_TO_FIRE` → refs=1, ext=0
- `AVATAR_OF_FIRE_PHYS_TO_FIRE` → refs=1, ext=0

`avatar_of_fire` keystone 节点（`KeystoneNodes01.java:26-31`）的 effects 列表为空 `List.of()`——这 4 条当前根本没被任何天赋节点接线。合并绝对安全。

## 3. 合并

### 3.1 StatIds.java

4 常量 → 1 规范常量：

```java
// 75% of Damage Converted to Fire Damage Deal no Non-Fire Damage
public static final Identifier AVATAR_OF_FIRE = id("avatar_of_fire");
```

### 3.2 StatGen.java

4 `map.put` → 1 多来源 entry：

```java
// avatar_of_fire：物理/闪电/冰霜/混沌 各 75% REPLACE 转火（单条多来源；引擎对 fromTags 做 contains 匹配，见 DamagePipeline L734）
map.put(StatIds.AVATAR_OF_FIRE,
        DamageConvertStatEffect.entry(Set.of(StatTag.PHYSICAL, StatTag.LIGHTNING, StatTag.COLD, StatTag.CHAOS), StatTag.FIRE, 0.75));
```

### 3.3 注释修正

- `DamageConvertStatEffect.java`：javadoc `slotTags.containsAll(fromTags)` → `fromTags.contains(fromTag)`（UNION 语义）
- `DamagePipeline.java`：注释 `containsAll` → `contains`

## 4. 验收

| 门 | 结果 |
|----|------|
| compileJava | ✅ BUILD SUCCESSFUL |
| runData | ✅ BUILD SUCCESSFUL（Stat Entries 387ms） |
| stat_entry 数量 | 856 → 857（+`avatar_of_fire.json`） |
| avatar_of_fire.json | `{from:[physical,chaos,lightning,cold], mode:replace, amount:0.75, to:fire}` |
| 旧 4 文件残留 | 0（从未存在） |

## 5. 全量去重扫描

`_dedup.py` 扫描全部 676 个 StatIds 常量：
- 描述聚类（>1 成员）：57 簇
- 签名聚类（>1 成员）：7 簇

**除 AVATAR_OF_FIRE 外无真重复合并目标**：

| 簇 | 判定 | 原因 |
|----|------|------|
| Chaos Inoculation 三件套 | 不同效果组件 | 免疫流血/免疫混沌/生命=1 各自独立 |
| Glancing Blows 双修饰面 | 不同效果组件 | evade-unlucky 与 deflect-lucky 不同修饰面 |
| Blood Magic vs OF_SKILL_MANA_COSTS | 不同 ratio | 100% 全转换 vs X% 部分转换 |
| Immune to Maim ×2 | 不同条件 | 无条件 vs while Shapeshifted |
| Recoup/MinionRecoup 签名簇 | 截断假阳性 | sig 截断在 `StatTargets.ofTarget(T`，底层 effect 类型不同 |
| 其余描述簇 | 归一化假阳性 | 脚本过度压缩导致不同 stat 描述碰撞 |

## 6. 改动文件

| 文件 | 改动 |
|------|------|
| StatIds.java | 4 常量→1 `AVATAR_OF_FIRE` |
| StatGen.java | 4 `map.put`→1 多来源 entry |
| DamageConvertStatEffect.java | javadoc `containsAll`→`contains` |
| DamagePipeline.java | 注释 `containsAll`→`contains` |
