# Stat 生成层「完整无重复」复检报告

执行时间：2026-08-27
复检脚本：`scripts/verify_gen.py`（对照源码 + 已生成 JSON 静态分析）

## 结论

**生成完整，且无任何重复。** 全部 13 项静态校验 PASS。

## 校验明细

### L1 — 三个子条目 Ids 类（路径缓存）
| 类 | 字段数 | 唯一字段名 | 唯一路径 | 重复名 | 重复路径 |
|---|---|---|---|---|---|
| StatEffectIds | 545 | 545 | 545 | 0 | 0 |
| StatTargetIds | 14 | 14 | 14 | 0 | 0 |
| StatConditionIds | 39 | 39 | 39 | 0 | 0 |

### L2 — StatGen.buildEntries（纯路径组装）
- `map.put(StatIds.X, stat(...))` 计数 = **711**，唯一键 = 711，**重复键 = 0**。
- 711 个 built key **全部是真实 `StatIds` 常量**（0 个未知 / 拼写错误）。
- stat() 调用内共 **1546** 个 `EFF_/TGT_/COND_` 子条目引用，**未解析引用 = 0**（全部命中对应 Ids 字段）。

### L3 — 已生成 JSON 文件 1:1 对照
| 目录 | 文件数 | 路径↔文件 缺失 | 孤儿文件 |
|---|---|---|---|
| stat/effect | 545 | 0 | 0 |
| stat/targets | 14 | 0 | 0 |
| stat/condition | 39 | 0 | 0 |
| stat/entry | 777 | — | — |

- effect/targets/condition 三类 Ids 的 path 与生成文件名 **完全 1:1**，无缺失无孤儿。

### L4 — entry 数值分布
- 777 个 entry JSON 全部含 `$value` 或 `value`（amount 恒为 `$value` 模板），**无异常 0 个**。

## 完整性推导
- `StatEntryProvider` 直接以 `StatGen.entries().entrySet()` 落盘 → entry JSON 数 = `StatGen.entries()` 大小。
- 若 711 built 与 66 external 存在键重叠，`entries()` 合并后 distinct 数 < 777，则落盘文件数 < 777。
- 实测 entry JSON = **777** ⇒ 711(built) + 66(external) 键集合 **零重叠**，无重复键、无遗漏。
- 外部注册含循环生成（livingmod_resist_*/livingmod_convert_phys_to_* 等），其数量在运行期得出；777 实测值即其运行期真实结果。

## 验收门（待 runData 动态确认）
- compileJava：预期 PASS（Ids/Gen 均为合法 Java，711 键均为真实常量）。
- runData：需产出 effect=545 / targets=14 / condition=39 / entry=777。
- `coe_event_key_audit`：断言 `StatGen.entries()` / `StatEffectGen` / `StatConditionGen` / `StatTargetGen` 每个 key 均有对应落盘文件，缺失即抛错终止构建。

## 复检脚本
`scripts/verify_gen.py` 可重复执行，输出上述全部指标并给出 RESULT: ALL CHECKS PASS / SOME CHECKS FAILED。
