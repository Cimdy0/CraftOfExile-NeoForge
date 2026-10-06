# -*- coding: utf-8 -*-
"""清洗 poe2_tree.json：移除暂不实现的部分，生成当前需要的副本 poe2_tree_clean.json。

移除规则：
- 升华节点（ascendancyId 非空，含从初始点直连树外的升华天赋团）
- 专精节点（isMastery）
- 不可达浮空节点（从 root BFS 不可达，含起点附近的浮空珠宝节点：
  5 个 Sinister 珠宝插孔 + Zarokh's Gift）

保留：
- root 与全部可达的常规节点
- 12 个有连线的常规珠宝插孔

同步过滤：groups / edges / jewelSlots / skillOverrides；剪除悬挂 out/in 引用。
"""
import json
from collections import deque

SRC = "poe2_assets/poe2_tree.json"
DST = "poe2_assets/poe2_tree_clean.json"


def main():
    with open(SRC, "r", encoding="utf-8") as f:
        data = json.load(f)

    nodes = data["nodes"]

    # 1) BFS from root
    visited = set()
    queue = deque(["root"])
    while queue:
        nid = queue.popleft()
        if nid in visited:
            continue
        visited.add(nid)
        node = nodes.get(nid)
        if node:
            for oid in node.get("out", []):
                if oid not in visited:
                    queue.append(oid)
            for iid in node.get("in", []):
                if iid not in visited:
                    queue.append(iid)

    # 2) 保留集：可达 ∧ 非升华 ∧ 非专精
    kept = {}
    removed = {"ascendancy": 0, "mastery": 0, "unreachable": 0}
    for nid, node in nodes.items():
        if node.get("ascendancyId"):
            removed["ascendancy"] += 1
        elif node.get("isMastery"):
            removed["mastery"] += 1
        elif nid not in visited:
            removed["unreachable"] += 1
        else:
            kept[nid] = node

    # 3) 剪除悬挂 out/in 引用（指向已删除节点的边）
    dangling = 0
    for nid, node in kept.items():
        out0 = node.get("out", [])
        in0 = node.get("in", [])
        out1 = [x for x in out0 if x in kept]
        in1 = [x for x in in0 if x in kept]
        dangling += (len(out0) - len(out1)) + (len(in0) - len(in1))
        if out1 != out0:
            node["out"] = out1
        if in1 != in0:
            node["in"] = in1

    # 4) 过滤 groups：仅保留被保留节点引用的 group（node.group 为 int，group 键为 str）
    used_groups = {str(node["group"]) for node in kept.values() if node.get("group") is not None}
    groups = {gid: g for gid, g in data.get("groups", {}).items() if gid in used_groups}

    # 5) 过滤 edges：两端均在保留集
    edges = [e for e in data.get("edges", [])
             if str(e.get("from")) in kept and str(e.get("to")) in kept]

    # 6) 过滤 jewelSlots：仅保留仍在树中的插孔（元素为数字索引）
    jewel_slots = [js for js in data.get("jewelSlots", []) if str(js) in kept]

    # 7) 过滤 skillOverrides：键为保留节点
    skill_overrides = {k: v for k, v in data.get("skillOverrides", {}).items() if k in kept}

    clean = {
        "tree": data.get("tree"),
        "classes": data.get("classes"),
        "groups": groups,
        "nodes": kept,
        "edges": edges,
        "skillOverrides": skill_overrides,
        "jewelSlots": jewel_slots,
        "min_x": data.get("min_x"),
        "min_y": data.get("min_y"),
        "max_x": data.get("max_x"),
        "max_y": data.get("max_y"),
    }

    with open(DST, "w", encoding="utf-8") as f:
        json.dump(clean, f, ensure_ascii=False, indent=1)

    # 汇总
    n_stats = sum(len(n.get("stats", [])) for n in kept.values())
    n_jewel = sum(1 for n in kept.values() if n.get("isJewelSocket"))
    print(f"原始节点: {len(nodes)}")
    print(f"移除: 升华 {removed['ascendancy']} + 专精 {removed['mastery']} + 浮空 {removed['unreachable']}"
          f" = {sum(removed.values())}")
    print(f"保留节点: {len(kept)}（含 root）")
    print(f"保留珠宝插孔: {n_jewel}")
    print(f"保留 stat 行: {n_stats}")
    print(f"groups: {len(data.get('groups', {}))} -> {len(groups)}")
    print(f"edges: {len(data.get('edges', []))} -> {len(edges)}")
    print(f"jewelSlots: {len(data.get('jewelSlots', []))} -> {len(jewel_slots)}")
    print(f"skillOverrides: {len(data.get('skillOverrides', {}))} -> {len(skill_overrides)}")
    print(f"剪除悬挂引用: {dangling}")


if __name__ == "__main__":
    main()
