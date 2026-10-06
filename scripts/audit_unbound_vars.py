"""#40-广义 审计：数据驱动 JSON 表达式中引用的模板变量是否都有注入来源。

缝#40 原始形态：stat entry 的 amount 写 "$value"，但创建路径未把 value 注入 vars
→ Variable.local("value") 默认 0.0 回落 → 静默恒零（真玩法失效）。

本扫描把该缺陷广义化到所有数据驱动 JSON 表达式字段（amount / value / knockback /
radius / cost 等任意 "$var" 字符串）：

- 数据驱动层（src/generated + src/main/resources 下 data/coe/coe/**/*.json）：
  每个文件内的 "$var" 引用，必须由【同文件 params 块声明的变量名】覆盖，
  或是引擎全局已知变量（当前仅 "value"，由 StatGen/CharmEngine 注入）。
  未覆盖者 = 引用变量却无注入源 = 缝#40 同型静默恒零。

- Java 表达式层（辅助）：ExpressionCodec.of("$X") / parse("$X") 字符串字面量表达式，
  若出现非 "value" 的裸变量，且无对应 Variable.from/local 声明，即为未绑定（编译期 AST
  构造本应自声明，故通常仅 catch 手写裸串）。

用法：python scripts/audit_unbound_vars.py
仅报告，不改代码。
"""
import os
import re
import json

HERE = os.path.dirname(os.path.abspath(__file__))
PROJ = os.path.dirname(HERE)
GEN = os.path.join(PROJ, "src", "generated", "resources", "data", "coe", "coe")
MAIN = os.path.join(PROJ, "src", "main", "resources", "data", "coe", "coe")
ROOT = os.path.join(PROJ, "src", "main", "java", "com", "cimdy", "coe")

# 引擎全局已知、由运行期注入的变量（数据 JSON 中可裸引用而不需在 params 声明）
KNOWN_GLOBAL = {"value"}

VARREF = re.compile(r"\$([A-Za-z_][A-Za-z0-9_.]*)")
JAVA_STR_EXPR = re.compile(r'(?:ExpressionCodec\.of|parse)\(\s*"(\$[A-Za-z_][A-Za-z0-9_.]*)"')


# ExpressionCodec 函数库目录：其中的 *.json 是函数【定义】体（如
# "$base + skill.level / $per * $add"），其 $var 为函数形参，由调用点传参绑定，
# 并非数据驱动条目需要的运行期注入。属定义而非引用，须从本审计排除。
EXPR_LIB_TAIL = "coe/coe/expression"


def iter_json_files():
    skipped_lib = 0
    for base in (GEN, MAIN):
        if not os.path.isdir(base):
            continue
        for dp, _, fs in os.walk(base):
            norm = dp.replace("\\", "/")
            if norm.endswith(EXPR_LIB_TAIL):
                skipped_lib += len([f for f in fs if f.endswith(".json")])
                continue
            for f in fs:
                if f.endswith(".json"):
                    yield os.path.join(dp, f)
    iter_json_files.skipped_lib = skipped_lib


def collect_params(node, acc):
    """收集文件中所有 'params' 字典的键，作为本文件已声明变量集合。"""
    if isinstance(node, dict):
        p = node.get("params")
        if isinstance(p, dict):
            for k in p.keys():
                if isinstance(k, str):
                    acc.add(k)
        for v in node.values():
            collect_params(v, acc)
    elif isinstance(node, list):
        for v in node:
            collect_params(v, acc)


def collect_refs(node, acc):
    """收集所有字符串值中的 $var 引用（跳过 params 子树，避免把定义当引用）。"""
    if isinstance(node, dict):
        for k, v in node.items():
            if k == "params":
                continue  # 定义在 params 内部，非引用
            collect_refs(v, acc)
    elif isinstance(node, list):
        for v in node:
            collect_refs(v, acc)
    elif isinstance(node, str):
        for m in VARREF.finditer(node):
            acc.add(m.group(1))


def scan_json():
    files = 0
    unbound = []
    total_refs = 0
    for path in iter_json_files():
        files += 1
        try:
            data = json.load(open(path, encoding="utf-8"))
        except Exception as e:
            unbound.append((path, [f"<parse-error:{e}>"]))
            continue
        declared = set()
        collect_params(data, declared)
        refs = set()
        collect_refs(data, refs)
        total_refs += len(refs)
        miss = sorted(refs - declared - KNOWN_GLOBAL)
        if miss:
            unbound.append((path, miss))
    return files, total_refs, unbound


def scan_java():
    hits = []
    for dp, _, fs in os.walk(ROOT):
        for f in fs:
            if not f.endswith(".java"):
                continue
            p = os.path.join(dp, f)
            try:
                txt = open(p, encoding="utf-8", errors="ignore").read()
            except Exception:
                continue
            for m in JAVA_STR_EXPR.finditer(txt):
                var = m.group(1)[1:]  # 去 $
                if var not in KNOWN_GLOBAL:
                    hits.append((p, var))
    return hits


def main():
    print("=" * 64)
    print("缝#40 广义 · 未注入模板变量审计 (audit_unbound_vars)")
    print("=" * 64)
    files, total_refs, unbound = scan_json()
    print(f"扫描 JSON 文件数（已排除 expression 函数库 {getattr(iter_json_files, 'skipped_lib', 0)} 个）：{files}")
    print(f"引用 $var 总数（去重/文件内）：{total_refs}")
    print(f"未绑定（引用变量但无注入源）：{len(unbound)}")
    print("-" * 64)
    if unbound:
        for path, miss in unbound:
            rel = os.path.relpath(path, PROJ)
            print(f"  [UNBOUND] {rel}")
            print(f"     缺失变量: {', '.join(miss)}")
    else:
        print("  (none) — 数据驱动层所有 $var 均有 params 声明或引擎全局")

    print("-" * 64)
    print("Java 字符串表达式辅助扫描（ExpressionCodec.of(\"$X\") / parse(\"$X\")）：")
    java_hits = scan_java()
    if java_hits:
        for p, var in java_hits:
            rel = os.path.relpath(p, PROJ)
            print(f"  [JAVA-STR] {rel}  ->  ${var}")
    else:
        print("  (none) — Java 裸串表达式仅含已知全局 value")
    print("=" * 64)


if __name__ == "__main__":
    main()
