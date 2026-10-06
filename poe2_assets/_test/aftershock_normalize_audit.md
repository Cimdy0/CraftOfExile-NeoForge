# 余震(aftershock)几率属性族作用域归一（治本）

> 用户根因批评：「属性还是用的包含作用域或者条件之类的非归一属性」。本批收敛余震几率族。

## 1. 根因（作用域焊死进属性名）

`AreaDeliveryExecutor` 读取**三个独立属性**作相加：

| 属性 | 作用域 | 问题 |
|---|---|---|
| `AFTERSHOCK_CHANCE` | 全部 slam | 基 |
| `AFTERSHOCK_CHANCE_MACE_SLAM` | 仅 [Mace][Slam] | 作用域焊进名字 |
| `AFTERSHOCK_CHANCE_SHAPESHIFT_SLAM` | 仅 [Shapeshift][Slam] | 作用域焊进名字 |

后两个把「mace / shapeshift」限定作用域烤进属性名——正是用户指的"非归一"。

## 2. 改动（4 文件）

- `AttributeRegister.java`：删除 `AFTERSHOCK_CHANCE_MACE_SLAM` / `AFTERSHOCK_CHANCE_SHAPESHIFT_SLAM`，收敛到 `AFTERSHOCK_CHANCE` 单属性。
- `StatIds.java`：重建被清理误删的 2 个常量
  - `CHANCE_FOR_SLAM_SKILLS_TO_CAUSE_AN_ADDITIONAL_AFTERSHOCK` → `chance_for_slam_skills_to_cause_an_additional_aftershock`
  - `CHANCE_FOR_SHAPESHIFT_SLAM_SKILLS_YOU_USE_YOURSELF_TO_CAUSE_AN_ADDITIONAL_AFTERSHOCK` → `chance_for_shapeshift_slam_skills_you_use_yourself_to_cause_an_additional_aftershock`
  - （mace 常量此前已在，复用）
- `StatGen.java`：以 `chance()`（=ADD_VALUE）重建 3 条条目，作用域标签区分：
  - base → `{slam}`
  - mace → `{mace, slam}`
  - form → `{form, slam}`
- `AreaDeliveryExecutor.java`：改为 `DamageHelper.readInScopePublic(lc, scope, AFTERSHOCK_CHANCE)`，按 `isMace`/`inForm` 动态组装作用域 `{slam}` / `{mace,slam}` / `{form,slam}`，等价原三段相加。

## 3. 门禁

| 门 | 结果 |
|---|---|
| compileJava | ✅ BUILD SUCCESSFUL |
| runData | ✅ BUILD SUCCESSFUL（stat_entry 再生，3 条 aftershock chance JSON 出现） |
| dangling | ✅ 无（`AFTERSHOCK_CHANCE_MACE_SLAM`/`_SHAPESHIFT_SLAM` 仅存于 .step*bak 备份） |

## 4. 遗留（独立缺口，非本批范围）

天赋树 JSON **全节点均不带 stat id**——节点→stat 由运行时注册表驱动，而非 `StatGen`。
故余震几率天赋节点仍处于 inert：清理删其 `StatGen` 条目后、且该运行时注册表未映射这些 id，
节点不产出几率。属性层已归一且引擎读取正确，但**节点真正生效需补运行时天赋效果注册表映射**——
属独立任务，不猜测、单独跟进。
