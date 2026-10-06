# gen/stat 字面量→缓存字段收尾

## 结论
「所有数字/字符串替换为变量或缓存字段」收尾完成，三道验收门禁全部通过。

| 门禁 | 结果 | 证据 |
|---|---|---|
| compileJava | ✅ | BUILD SUCCESSFUL（仅无关 unchecked 注记） |
| runData | ✅ | BUILD SUCCESSFUL（7m43s，干净重建）；Stat Entries / Talent Tree 完成 |
| grep 孤儿 | ✅ | 5 文件无残留代码字面量；30 个 StatGenConsts 字段全被引用 |

## 改造内容
- **新建 `StatGenConsts.java`**（同包 `com.cimdy.coe.data.gen.stat`）：集中托管共享缓存字段。
  - 幅度/比例：`FULL=1.0`(100%) / `ZERO=0.0`(0%) / `CONVERT_FULL=1.0`(转换100%) / `PUPPET_MASTER_DURATION=400.0` / `MOVEMENT_STATIONARY_WINDOW=20.0` / `LIVING_WINDOW=80.0`。
  - 标签/事件键：`TAG_COMMAND` / `TAG_PUPPET_MASTER` / `TAG_GAIN_FRENZY` / `TAG_GAIN_ENDURANCE` / `TAG_GAIN_POWER` / `TAG_GAIN_CHARGE` / `TAG_VOLATILITY_DETONATED` / `COMBO_DEFAULT`。
  - 条件对象（`record` 不可变，共享安全，同时消去配对字符串+阈值）：9×`LivingStateValue`、5×`EventHistoryValue`、1×`StatusAgeValue`、1×`MovementValue`。
- **批量替换** CoreStatEntries / DamageStatEntries / RecoveryStatEntries / StatGenHelpers / StatGen 内全部裸数字与字符串字面量 → `StatGenConsts.X`。
- **关键石 100%/0%**：`ExpressionCodec.of(1.0/0.0)` → `ExpressionCodec.of(FULL/ZERO)`（保留具体值、消除裸字面量；不改用 `$value` 以避免在无 Java 引用点的天赋节点数据上悬空）。

## 数据产物核验
- 每目录 777 `stat_entry` + 6 `stat_targets`（generated 与 build 两目录合计 1554+12）。
- 关键石 `chaos_inoculation_max_health.json` amount 仍为 `1.0`（保真）。
- `$value*20` 序列化为 `$value * 20`（与改造前文本一致）。
- `inc_deflection_rating_while_surrounded.json` 条件为 `{"mode":"surrounded","window":0.0}`（= `LIVING_SURROUNDED_NOW`）。

## 范围边界
- 刻意排除：`StatGenHelpers:181 new HashSet<>(base.size() + 3)` 的容量 padding（实现细节，非数据生成值）。
- 前次 runData「1554」为跨 generated + build 两目录重复计数；真实单目录值为 777。
