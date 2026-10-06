# -*- coding: utf-8 -*-
"""死属性扫描：找出「stat_entry 引用了、但运行时代码从不读取」的属性。

判定口径：
  - 属性来源：AttributeRegister.java 的 register*("id", base) 声明 → (常量名, 属性id)
  - 生成侧引用：src/generated/resources/.../stat_entry/*.json 里的 attribute 字段
  - 运行时读取：全工程 .java 中对该常量名的引用，排除
      * data/gen/**            （数据生成侧，不是运行时读取）
      * AttributeRegister.java （声明自身）
      * StatIds.java           （id 词表）
  - 无运行时读取 → 死属性：写入 modifier 也不会被任何逻辑消费
"""
import io, os, re, json, collections

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SRC = os.path.join(ROOT, "src", "main", "java")
ATTR_FILE = os.path.join(SRC, "com", "cimdy", "coe", "data", "AttributeRegister.java")
GEN_DIR = os.path.join(ROOT, "src", "generated", "resources", "data", "coe", "coe", "stat_entry")
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "dead_attributes.md")

REG_RE = re.compile(
    r'public\s+static\s+Holder<Attribute>\s+(\w+)\s*=\s*register\w*\(\s*"([^"]+)"')


def load_attrs():
    """常量名 -> 属性 id（coe:xxx）"""
    s = io.open(ATTR_FILE, encoding="utf-8").read()
    out = {}
    for m in REG_RE.finditer(s):
        out[m.group(1)] = "coe:" + m.group(2)
    return out


def scan_runtime_refs(const_names):
    """统计每个常量名在运行时代码中的引用次数（文件 -> 命中）"""
    hits = collections.defaultdict(list)
    names = set(const_names)
    # 单次遍历全部 java 文件，避免 N×文件 的重复 IO
    pat = re.compile(r'\b(' + '|'.join(sorted(names, key=len, reverse=True)) + r')\b')
    for dirpath, dirnames, filenames in os.walk(SRC):
        norm = dirpath.replace("\\", "/")
        if "/data/gen" in norm:
            continue
        for fn in filenames:
            if not fn.endswith(".java"):
                continue
            if fn in ("AttributeRegister.java", "StatIds.java"):
                continue
            fp = os.path.join(dirpath, fn)
            try:
                s = io.open(fp, encoding="utf-8").read()
            except Exception:
                continue
            for m in pat.finditer(s):
                hits[m.group(1)].append(fn)
    return hits


def load_generated():
    """属性 id -> [stat 条目名]"""
    used = collections.defaultdict(list)
    if not os.path.isdir(GEN_DIR):
        return used
    for fn in sorted(os.listdir(GEN_DIR)):
        if not fn.endswith(".json"):
            continue
        fp = os.path.join(GEN_DIR, fn)
        try:
            j = json.load(io.open(fp, encoding="utf-8"))
        except Exception:
            continue
        name = fn[:-5]
        stack = [j]
        while stack:
            cur = stack.pop()
            if isinstance(cur, dict):
                a = cur.get("attribute")
                if isinstance(a, str):
                    used[a].append(name)
                for v in cur.values():
                    if isinstance(v, (dict, list)):
                        stack.append(v)
            elif isinstance(cur, list):
                for v in cur:
                    if isinstance(v, (dict, list)):
                        stack.append(v)
    return used


def main():
    attrs = load_attrs()                    # const -> coe:id
    id2const = {v: k for k, v in attrs.items()}
    refs = scan_runtime_refs(attrs.keys())  # const -> [file]
    used = load_generated()                 # coe:id -> [entry]

    rows = []
    for aid, entries in sorted(used.items()):
        if not aid.startswith("coe:"):
            continue                         # 原版属性由 MC 自身消费
        const = id2const.get(aid)
        if const is None:
            rows.append((aid, "?", 0, sorted(set(entries)), "未在 AttributeRegister 声明"))
            continue
        n = len(set(refs.get(const, [])))
        if n == 0:
            rows.append((aid, const, 0, sorted(set(entries)), "无运行时读取器"))

    total_coe = len([a for a in used if a.startswith("coe:")])
    dead_entries = sorted(set(e for r in rows for e in r[3]))

    L = []
    L.append("# 死属性扫描：stat_entry 引用了但运行时从不读取的属性\n")
    L.append("- AttributeRegister 声明属性：**%d**" % len(attrs))
    L.append("- stat_entry 实际引用的 coe:* 属性：**%d**" % total_coe)
    L.append("- 其中**无运行时读取器**：**%d**" % len(rows))
    L.append("- 受影响 stat 条目（修饰恒不被消费）：**%d**\n" % len(dead_entries))
    L.append("> 判定：排除 data/gen/**、AttributeRegister.java、StatIds.java 后，"
             "全工程 .java 无该常量引用 → 写入的 AttributeModifier 无任何消费方。\n")

    L.append("## 死属性明细（按受影响条目数降序）\n")
    L.append("| 属性 | 常量 | 受影响条目数 | stat 条目 |")
    L.append("|---|---|---:|---|")
    for aid, const, _, entries, why in sorted(rows, key=lambda r: -len(r[3])):
        show = ", ".join("`%s`" % e for e in entries[:6])
        if len(entries) > 6:
            show += " … 共 %d" % len(entries)
        L.append("| `%s` | `%s` | %d | %s |" % (aid, const, len(entries), show))

    io.open(OUT, "w", encoding="utf-8", newline="\n").write("\n".join(L) + "\n")
    print("死属性 %d 个 / coe 属性被引用 %d 个；受影响条目 %d" %
          (len(rows), total_coe, len(dead_entries)))
    print("报告 -> %s" % OUT)
    for aid, const, _, entries, why in sorted(rows, key=lambda r: -len(r[3]))[:25]:
        print("  %-42s %-34s %d 条" % (aid, const, len(entries)))


if __name__ == "__main__":
    main()
