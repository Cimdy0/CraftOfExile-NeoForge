#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
将 StatConditionGen.java 中「ContextNode.CODEC.listOf().parse(JsonParser.parseString(<JSON>))」
形式的逐项 verbatim JSON 改写为「各节点类构造器构造真实 ContextNode 对象」的 CODEC 工厂形式：

    m.put(StatConditionIds.KEY, List.of(new LivingStateValue(...), new CompareValue(...)));

依据：每个节点类的记录构造器签名（与 CODEC optionalFieldOf 默认值一致），构造出的对象经
Provider 的 ContextNode.CODEC.listOf() 编码后，与原 verbatim 产物规范化（键排序）后逐字节等价。

用法：python scripts/condition_to_factory.py
仅改写 src/main/.../stat/StatConditionGen.java（原地重写）；不改 Provider。
"""
import os
import re
import json

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat", "StatConditionGen.java")

OP_MAP = {
    "==": "ComparisonOperator.EQUAL",
    "!=": "ComparisonOperator.NOT_EQUAL",
    "<": "ComparisonOperator.LESS_THAN",
    "<=": "ComparisonOperator.LESS_THAN_OR_EQUAL",
    ">": "ComparisonOperator.GREATER_THAN",
    ">=": "ComparisonOperator.GREATER_THAN_OR_EQUAL",
}


def unescape_java(s: str) -> str:
    out = []
    i = 0
    n = len(s)
    while i < n:
        c = s[i]
        if c == '\\' and i + 1 < n:
            nxt = s[i + 1]
            if nxt == 'n':
                out.append('\n')
            elif nxt == 't':
                out.append('\t')
            elif nxt == 'r':
                out.append('\r')
            elif nxt == '"':
                out.append('"')
            elif nxt == '\\':
                out.append('\\')
            else:
                out.append(nxt)
            i += 2
        else:
            out.append(c)
            i += 1
    return ''.join(out)


def num(v):
    if isinstance(v, bool):
        return "true" if v else "false"
    if isinstance(v, int):
        return f"{v}.0"
    if isinstance(v, float):
        return repr(v)
    return str(v)


def node_expr(n: dict) -> str:
    t = n["type"]
    if t == "living_state":
        mode = n["mode"]
        window = n.get("window", 80.0)
        return f'new LivingStateValue("{mode}", {num(window)})'
    if t == "status":
        kind = n["kind"]
        return f'new StatusValue("{kind}", CraftOfExile.EMPTY_LOCATION)'
    if t == "status_age":
        kind = n["kind"]
        min_t = n.get("min_ticks", 80.0)
        return f'new StatusAgeValue("{kind}", {num(min_t)})'
    if t == "count":
        name = n["name"]
        mode = n.get("mode", "count")
        window = n.get("window", 80.0)
        return f'new EventHistoryValue("{name}", "{mode}", {num(window)})'
    if t == "movement":
        mode = n["mode"]
        window = n.get("window", 20.0)
        return f'new MovementValue("{mode}", {num(window)})'
    if t == "socket":
        color = n.get("color")
        kind = n.get("kind", "all")
        aggregate = n.get("aggregate", "first")
        color_arg = f'Optional.of("{color}")' if color is not None else "Optional.empty()"
        return (f'new GemValue(Optional.empty(), Optional.empty(), {color_arg}, '
                f'"{kind}", Optional.empty(), "{aggregate}")')
    if t == "equipment":
        slots = n.get("slots")
        output = n.get("output")
        aggregate = n.get("aggregate", "first")
        if slots is None:
            slots_arg = "Optional.empty()"
        else:
            slot_list = ", ".join(f"EquipmentSlot.{s.upper()}" for s in slots)
            slots_arg = f"Optional.of(List.of({slot_list}))"
        output_arg = f'Optional.of("{output}")' if output is not None else "Optional.empty()"
        agg = f"EquipmentValue.Aggregate.{aggregate.upper()}"
        return f"new EquipmentValue({slots_arg}, Optional.empty(), {output_arg}, {agg})"
    if t == "compare":
        left = node_expr(n["left"])
        right = node_expr(n["right"])
        ls = n.get("left_scale", 1.0)
        rs = n.get("right_scale", 1.0)
        op = OP_MAP[n["operator"]]
        return f"new CompareValue({left}, {right}, {num(ls)}, {num(rs)}, {op})"
    if t == "presence_rarity":
        rarities = n["rarities"]
        enemies_only = n.get("enemies_only", True)
        mode = n.get("mode", "any")
        rar_list = ", ".join(f"Rarity.{r.upper()}" for r in rarities)
        m = f"PresenceRarityValue.Mode.{mode.upper()}"
        return f"new PresenceRarityValue(List.of({rar_list}), {str(enemies_only).lower()}, {m})"
    raise ValueError("unknown condition node type: " + t)


def main():
    with open(GEN, "r", encoding="utf-8") as f:
        src = f.read()

    pat = re.compile(
        r'StatConditionIds\.(\w+)\s*,\s*ContextNode\.CODEC\.listOf\(\)\.parse\('
        r'JsonOps\.INSTANCE,\s*JsonParser\.parseString\((.*?)\)\)\.result\(\)\.orElseThrow\(\)',
        re.DOTALL,
    )

    entries = []
    for m in pat.finditer(src):
        key = m.group(1).strip()
        lit = m.group(2)
        # 去外层引号
        if lit.startswith('"') and lit.endswith('"'):
            lit = lit[1:-1]
        raw = unescape_java(lit)
        arr = json.loads(raw)
        exprs = [node_expr(x) for x in arr]
        entries.append((key, exprs))

    if len(entries) != 39:
        raise SystemExit(f"ERROR: 解析到 {len(entries)} 个条件条目，期望 39，请检查正则/源文件")

    lines = []
    for key, exprs in entries:
        if len(exprs) == 1:
            lines.append(f"        m.put(StatConditionIds.{key}, List.of({exprs[0]}));")
        else:
            joined = ",\n                ".join(exprs)
            lines.append(f"        m.put(StatConditionIds.{key}, List.of(\n                {joined}));")

    header = '''package com.cimdy.coe.data.gen.stat;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.support.Rarity;
import com.cimdy.coe.data.codec.support.math.ComparisonOperator;
import com.cimdy.coe.data.codec.support.math.context.ContextNode;
import com.cimdy.coe.data.codec.support.math.context.nodes.CompareValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.EquipmentValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.EventHistoryValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.GemValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.LivingStateValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.MovementValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.PresenceRarityValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.StatusAgeValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.StatusValue;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stat condition 子条目内容注册表（CODEC 工厂方法：每个 ContextNode 经对应节点类构造器构造真实对象，
 * Provider 经 ContextNode.CODEC.listOf() 编码落盘）。与 effect/target 一致，彻底去除 verbatim JSON 字符串。
 */
public final class StatConditionGen {
    private StatConditionGen() {}

    public static Map<Identifier, List<ContextNode>> contents() {
        Map<Identifier, List<ContextNode>> m = new LinkedHashMap<>();
'''
    footer = '''        return m;
    }
}
'''
    out = header + "\n".join(lines) + "\n" + footer
    with open(GEN, "w", encoding="utf-8") as f:
        f.write(out)

    # 统计使用到的节点类型
    types = {}
    for _, exprs in entries:
        for e in exprs:
            for cls in ("LivingStateValue", "StatusValue", "StatusAgeValue", "EventHistoryValue",
                        "MovementValue", "GemValue", "EquipmentValue", "CompareValue", "PresenceRarityValue"):
                if cls in e:
                    types[cls] = types.get(cls, 0) + 1
    print(f"OK: 重写 StatConditionGen.java，共 {len(entries)} 条条件，节点使用分布 = {types}")


if __name__ == "__main__":
    main()
