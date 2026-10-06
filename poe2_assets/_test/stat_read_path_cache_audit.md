# §63 Stat read 路径缓存字段类（StatPaths）

> Round：继 §62 描述覆盖率审计后。承接「Stat 各部分已 read 化」的既定成果，把 **Stat 条目这一层本身**
> 也固化为「read 路径 + 字段缓存」生成类，使「整个 Stat 的生成都使用 read 形式」闭环。

## 1. 背景与根因

Stat 四段（amount / conditions / targets / effect）此前已完成 read 化：
effect→`coe:stat_effect/<slug>`、targets→`coe:stat_targets/<name>`、conditions→`coe:stat_condition/<slug>`，
均内容寻址 + 路径引用。但 **Stat 条目层**的引用路径（`coe:<name>`）散落在两处、未收口为单一缓存类：

- `StatIds`（1517 常量）缓存 `coe:<name>`，由 `poe2_assets/gen_stat_ids.py` 从 poe2_tree.json 语料生成；
- `stat_entry`（777 文件）由 `StatGen` 注册、命名 `coe:<name>`，落盘 `coe/stat_entry/<name>.json`；
- 两者**独立两套来源**：586 个 stat_entry 有对应 StatIds 常量，191 个 stat_entry 无 StatIds（动态生成的 `ailment_*` 等），
  931 个 StatIds 孤儿（描述型，未接线）；
- `RecoveryStatEntries.registerAilmentFaces` 用裸 `Identifier.fromNamespaceAndPath(ns, "ailment_buildup_"+t.id)`
  生成异常面，属于**未走字段缓存路径**的动态生成点。

根因：Stat 条目层缺少一个**由 stat_entry 数据驱动生成**、每 Stat 一个 read 路径缓存字段的类，
导致动态生成点与静态 id 词表各写各的、路径字符串散落。

## 2. 处置（治本，非近似）

### 2.1 新增数据驱动类 `StatPaths`
- 文件：`src/main/java/com/cimdy/coe/data/codec/support/stat/StatPaths.java`
- 生成脚本：`poe2_assets/gen_stat_paths.py`（读 `stat_entry/*.json` 内容 → 每文件一个字段）
- 每 Stat 一个缓存字段：
  ```java
  // stat_entry/accuracy.json -> coe:accuracy
  public static final Identifier ACCURACY = id("accuracy");
  ```
- 额外 `byName(String name)`：按真实路径（`id.getPath()`，保留连字符）反查缓存，
  供动态生成点循环引用（`ailment_buildup_<t.id>` 等），替代裸 `fromNamespaceAndPath`。
- 字段名 sanitize：`-`→`_`、首字符数字加前缀、防重（NeoForge 路径允许 `-`，Java 标识符不允许）。

### 2.2 `StatIds` 委托真实 Stat 路径
- 对 `eid ∈ stat_entry 名称 且 NAME==eid.toUpperCase()` 的常量，由 `= id("x")` 改为 `= StatPaths.X`
  （surgical 编辑，未全量重生成，避免 1517 行无谓 diff）。
- 结果：**578 条委托**，939 条保留为描述型 `id()`（与 StatPaths 同源 `coe:<name>`，零悬空）。
- `StatIds` 与 `StatPaths` 同包（`com.cimdy.coe.data.codec.support.stat`），无需 import。

### 2.3 回接动态生成点
- `RecoveryStatEntries.registerAilmentFaces`：`ailment_buildup_/duration_/magnitude_/status_max_stacks_/status_extra_stacks_`
  × 8 异常类型共 40 条，由裸 `fromNamespaceAndPath` 改为 `StatPaths.byName("ailment_buildup_"+t.id)` 等。
- `DamageStatEntries`：`inc_offering_damage` 由裸 `fromNamespaceAndPath(ns, ...)` 改为 `StatPaths.INC_OFFERING_DAMAGE`
  （原注释「保留裸 path」是 StatPaths 出现前的旧决策，现已可走字段缓存）。
- `gen/stat` 包内裸 `fromNamespaceAndPath(ns, "<stat名>")` 拼接：**清零**。

## 3. 验证（三道门禁 + round-trip）

| 门禁 | 结果 |
|---|---|
| compileJava | ✅ EXIT=0 |
| runData | ✅ EXIT=0（static-init 经 `StatPaths.byName` 解析异常面，无「未缓存」报错） |
| round-trip | ✅ stat_entry(777) ↔ StatPaths 字段(777) **1:1 精确**；578 委托 **0 悬空 / 0 错配** |

- 残留 199 个 stat_entry 未被 StatIds 委托：47 个 runtime 动态（`talent_/livingmod_/modifier_`，`StatGen.*Attr` 返回 `StatRead(ref)` 已是 read 形式）、40 个异常面（走 `StatPaths.byName`）、其余代码生成（均经 `StatPaths` 字段缓存）。**无一处裸字符串拼接遗漏**。

## 4. 下一步（§64–§68 候选）
- **§64**：把 `talentAttr/livingModAttr/modifierAttr` 的 runtime `coe:talent_*` 等 id 也收口为生成类字段（需 build-time 反射或模板），彻底消除静态/动态双轨。
- **§65**：§62 遗留的 82 个缺失属性翻译键（en_us/zh_cn）补接。
- **§66**：`StatPaths` 接入 CI：每次 runData 后校验 stat_entry 名 ↔ StatPaths 字段 1:1，防止新增 stat 漏生成字段。

---

## 5. 子路径字段收口（effect / targets / condition）—— §63 续

> 承接 §2–§3 已把 **Stat 条目层**（`stat_entry` 777 文件）收口为 `StatPaths` 字段缓存类。
> 本段把 Stat 生成管线**剩余的 4 处内联路径拼接**（`effect` / `targets` / `condition` 三类子数据的 ref）
> 也全部收口为数据驱动字段缓存类，使「整个 Stat 生成 read 形式 + 字段缓存」彻底闭环。

### 5.1 背景与根因

`stat_entry` 四段中，`effect` / `targets` / `conditions` 三段此前已是 read 形态，但
`StatEntryProvider` 在生成 ref 时仍用**裸字符串拼接**：

```java
// 旧（内联拼接，未走字段缓存）
Identifier.fromNamespaceAndPath("coe", "stat_effect/" + slug);   // effect
Identifier.fromNamespaceAndPath("coe", "stat_targets/" + key);   // targets
```

根因：Stat 生成管线的**子路径引用**未像 `StatPaths` 那样由数据驱动生成缓存字段类，
而是散落在 `StatEntryProvider` 内的字符串拼接 → 与「生成类字段缓存路径」驱动原则相悖。

**额外发现的 latent 约定缺口（治本而非近似）**：
- `LoadEvent` 加载器把资源文件名截取为**裸 key**（`coe:<base>`，剥离 `stat_effect/` 等前缀）存入
  `STAT_EFFECT_MAP` / `STAT_TARGETS_MAP` / `STAT_CONDITION_MAP`；
- 但 `StatEntryProvider` 原生成的 ref 为 `coe:stat_effect/<slug>`（**带前缀**）。
- 二者不一致 → 运行期 `byName` 按裸 `coe:<base>` 反查时存在 latent miss 缺口。
- **收口统一为裸 `coe:<base>` 约定**，与 `StatPaths`（`coe:<name>`）同源，loader / ref / 字段类三方一致。

### 5.2 处置（治本，非近似）

生成脚本：`poe2_assets/gen_stat_subpaths.py`（读各子目录文件 → 每文件一个字段 + byName 反查）。
产物（数据驱动生成类，非手写）：

| 类 | 文件 | 字段数 | 内容 |
|---|---|---|---|
| `StatEffectPaths` | `…/support/stat/StatEffectPaths.java` | **539** | `E_<BASE_UPPER> = id("<base>")`；base 全为 64 位 hex slug |
| `StatTargetsPaths` | `…/support/stat/StatTargetsPaths.java` | **14** | `T_<BASE_UPPER> = id("<base>")`；6 命名（`self_only` 等）+ 8 内容寻址 |
| `StatConditionPaths` | `…/support/stat/StatConditionPaths.java` | **39** | `C_<BASE_UPPER> = id("<base>")`；base 全为 64 位 hex slug |

每个字段缓存裸 `coe:<base>`，并提供 `byName(base)` 严格反查（运行期未命中直接 fail-fast 抛错）。

`StatEntryProvider` 改写（4 处引用改为字段类反查）：
```java
return StatEffectPaths.byName(slug);                       // effect
StatTargetsPaths.byName(e.getKey().getPath());             // targets
return StatTargetsPaths.byName(slug);
return StatConditionPaths.byName(slug);
```
3 处写文件 `substring("stat_effect/".length())` 修正为 `id.getPath()`（path 现已裸，不再剥前缀）。

### 5.3 内容寻址稳定性（跨运行一致的关键）

- **slug 改全 64 位 SHA-256**：删除旧 16 位 hex + `_N` 碰撞后缀逻辑（`slugCount` 等局部）。
  全 hash 零碰撞、跨运行确定，杜绝 HashMap/HashSet 迭代顺序导致的 slug 漂移。
- **`canonicalize` 对数组元素递归排序**：
  ```java
  if (e instanceof JsonArray a) {
      List<JsonElement> items = new ArrayList<>();
      for (JsonElement x : a) items.add(canonicalize(x));
      items.sort(Comparator.comparing(JsonElement::toString));
      JsonArray out = new JsonArray();
      for (JsonElement x : items) out.add(x);
      return out;
  }
  ```
  仅排对象键不排数组（effect 的 `tags` / `conditions` 数组）会使同逻辑内容两次 runData 算出不同 slug
  → 与字段类失配。加数组排序后规范化 JSON 全序确定。

### 5.4 Bootstrap 鸡生蛋切换

全 hash 切换后旧字段类不含新 slug，直接 strict 会缺字段。技术：
1. 临时令 `byName` lenient（构造兜底）；→ 2. 清空 `stat_effect/stat_targets/stat_condition` 三目录；
3. runData 产出正确文件；→ 4. 切 strict 重生成字段类；→ 5. 严格 runData。
- 副作用（正确）：数组排序使此前因顺序不同而「不同」的两个 effect 合并，effect 文件由 **540 → 539**。

### 5.5 验证（三道门禁 + 一致性复检）

| 门禁 | 结果 |
|---|---|
| compileJava | ✅ EXIT=0 |
| runData | ✅ EXIT=0（written: 252，stale: 0）|
| ref 一致性复检 | ✅ `poe2_assets/check_subpath_consistency.py` |

一致性复检结论（裸 `coe:<base>` 三方一致）：

| 子类型 | 字段类 | 落盘文件 | stat_entry 引用 | 字段类=文件 | 悬空 |
|---|---|---|---|---|---|
| effect | 539 | 539 | 539 | ✅ 1:1 | 0 |
| targets | 14 | 14 | 10 | ✅ 1:1 | 0 |
| condition | 39 | 39 | 39 | ✅ 1:1 | 0 |

- effect / condition：引用集 == 字段集（每个缓存都被引用，每个引用都有字段）。
- targets：**4 个死缓存**（`all_on_target` / `self_by_target` / `target_by_target` / `target_only`）——
  合法可复用命名 targets，仅当前无条目引用，与「悬空（引用缺字段）」方向相反，无害。
- **零悬空**：无任何 `stat_entry` 引用了字段类未缓存的 `coe:<base>`（否则运行期 `byName` 抛错）。

### 5.6 下一步

- **§64**：把 `talentAttr/livingModAttr/modifierAttr` 的 runtime `coe:talent_*` 等 id 也收口为生成类字段，
  彻底消除静态/动态双轨（与 §63 §4 同源，优先级最高）。
- **§65**：§62 遗留的 82 个缺失属性翻译键补接。
- **§66**：`StatPaths` 接入 CI：每次 runData 后校验 stat_entry 名 ↔ StatPaths 字段 1:1，
  并扩展校验三子路径（effect/targets/condition）字段类 ↔ 落盘文件 ↔ stat_entry 引用三方一致。
