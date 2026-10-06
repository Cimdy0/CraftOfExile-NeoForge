# StatGen 拆分 + withAmount 默认化 + 常量→变量 — 三道门禁最终收束

## 结论
全部三道验收门禁通过，常量→变量改造闭环完成。

| 门禁 | 结果 | 证据 |
|---|---|---|
| compileJava | ✅ | 前序两次 BUILD SUCCESSFUL（仅 deprecation/unchecked 注记，无错误） |
| runData | ✅ | BUILD SUCCESSFUL（6m59s）；`Stat Entries` 784ms / `Talent Tree` 1549ms 完成 |
| grep 孤儿 | ✅ | 见下 |

## runData 产物核验
- 生成 **1554** 个 `data/coe/coe/stat_entry/*.json`
- 生成 **12** 个 `data/coe/coe/stat_targets/*.json`
- HashCache：`total 4173, old 4173, new 4173, removed stale 0, written 67` → 无 stale 残留

## grep 孤儿门禁明细
1. **活跃 `withAmount` 调用点**：除 `StatEntry.java` / `StatRead.java` 定义与内部缩放逻辑外，无调用点 → 干净。
2. **`MathParser.parse("$value")`**：仅 `ExpressionCodec.java:28`（`public static final Value VALUE` 单一来源）。`.bak` 不编译。
3. **gen/stat 字面 amount 常量**：
   - `RecoveryStatEntries.java:486/492/499/503` → `registerKeystoneEntries` 刻意常量（REPLACE=1 生命 / SET_VALUE=0 暴击 / mana_take=1.0 / accuracy+100%），参数化会破坏语义，保持常量。
   - `StatGen.java:77/104/132` → `modRead`/`talentAttr`/`livingModAttr` 的 `ExpressionCodec.of(amount)`，`amount` 是方法形参（变量），属参数化复用助手 —— 符合「一条 StatGen 尽可能广泛被复用」意图，非字面量。

## runData 阻塞修复（build/resources 污染）
- 根因：被 kill 的 runData 进程遗留损坏的 `build/resources/main`（icons 目录仅 2 个 png，缺 `masteryinstillationspattern.png`），Gradle 增量 `:processResources` 无法调和 → `Failed to clean up stale outputs` / `Cannot access a file in the destination directory... as it does not exist`。
- 修复三连：`taskkill` 残留 gradle → `rm -rf build/resources .gradle/9.2.1/fileHashes` → `./gradlew runData --no-daemon --no-parallel --rerun-tasks` → 从干净源重建成功。

## 改造范围回顾
- **StatGen 拆分**：2572 行 → `StatGen`(facade) + `StatGenHelpers` + 3 个域构建类；可见性 private→public 修复。
- **withAmount 默认化**：`ExpressionCodec.VALUE` 中性单一来源；effect 工厂新增省略 amount 的 VALUE 默认重载。
- **常量→变量**：转伤/补给/资源转换 ratio、属性增减幅度、Rage 量、Lucky 占位 1.0 全部改为 `$value`，一般生成路径不再使用字面幅度常量。
