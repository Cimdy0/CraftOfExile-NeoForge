"""事件键死接线审计：核对「被引用」的每个事件键是否真有 Event.record 写入方。

背景（Batch 22 教训）：EventName 里声明了大量事件常量，但很多**从未被 Event.record 写入**。
而 EventHistoryValue 的 count/recently/sum 模式读的是 Event.State 队列——若某键无写入方，
引用它的接线**恒返 0**（条件永不命中 / 缩放恒为 0），且不会有任何编译或运行期报错。
这类「静默死接线」必须靠本脚本静态扫出，不能靠人眼。

【关键：事件有两条互不相同的写入通道】
  A. 计数队列通道（Event.State.triggers / taggedTriggers）
     写入方：Event.record(...)、Event.get(x).record(...)、GameEvent.emit(...)、GameEvent.recordTaggedHit(...)
     读取方：count:<k> / recently:<k> / sum / avg / max / last / elapsed
  B. 计时仓库通道（ExpressionContext timer store）
     写入方：refreshTimer(...)、GameEvent.recordTimer(...)、GameEvent.record(owner)
     读取方：timer:<k> / time_<k>（TimerValue）
  → 只走 B 的键若被 A 类节点引用，即为「半死接线」：count/recently 恒 0 且无任何报错，
    但并非完全无用——改用 time_<k> 读法即可复活。本脚本分别列出，便于按情形处置。

判定口径
  已写入键 = 源码中 Event.record(<常量或字面量>) 的实参
           + Event.get(x).record(...) / GameEvent.emit(...) / GameEvent.recordTaggedHit 调用点
           + EventCatalog 动态前缀白名单（skill_event: / gain_ / consume_）
  被引用键 = 仅「事件上下文」：
           · 生成 JSON（限定 coe/stat/**）中 type ∈ EVENT_TYPES 且带 name 字段的对象
           · coe/stat/** JSON 内的 count:/recently:/sum:/avg:/max:/last:/elapsed: 中缀
           · 引擎源码（data/**、event/**）中的同类缀
  注意：绝不能用「任意 JSON 的 name 字段」——装备词缀 mod / 天赋 slug 的 name 会淹没结果。
"""
import json
import re
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = ROOT / "src/main/java"
GEN = ROOT / "src/generated/resources/data/coe"
STAT_JSON = GEN / "coe/stat"          # 仅 stat / condition / effect，排除 *_mod.json 词缀

EVENT_TYPES = {"count", "recently", "sum", "avg", "max", "last", "elapsed"}
KEY_RE = re.compile(r"^[a-z0-9_]+(?::[a-z0-9_]+)?(?:\.[0-9]+)?$")

# ---- 1. 收集 EventName 常量值（名 → 值）----
ev = (SRC / "com/cimdy/coe/event/EventName.java").read_text(encoding="utf-8")
consts = dict(re.findall(r'public static final String (\w+)\s*=\s*"([^"]+)"', ev))

# ---- 2. 收集真实写入点（按通道分类）----
java_files = list(SRC.rglob("*.java"))


def is_event_self(f: pathlib.Path) -> bool:
    return f.name == "Event.java" and "event/" in str(f).replace("\\", "/")


def call_args(text: str, pat: str, arg_index: int, want_arity: int = 0) -> set[str]:
    """按括号配平提取 pat 匹配到的每个调用的第 arg_index 个实参（支持实参内含 ()/字符串）。

    want_arity > 0 时只收集实参数 == want_arity 的调用（用于区分 Event.record 的两参/三参重载）。
    """
    out: set[str] = set()
    for m in re.finditer(pat, text):
        i, depth, start = m.end(), 1, m.end()
        args: list[str] = []
        while i < len(text):
            c = text[i]
            if c in "([{":
                depth += 1
            elif c in ")]}":
                depth -= 1
                if depth == 0:
                    args.append(text[start:i])
                    break
            elif c == '"':
                i += 1
                while i < len(text) and text[i] != '"':
                    i += 2 if text[i] == "\\" else 1
            elif c == "," and depth == 1:
                args.append(text[start:i])
                start = i + 1
            i += 1
        if not args or (want_arity and len(args) != want_arity):
            continue
        if arg_index == -1:
            out.update(a.strip() for a in args)   # 全部实参（供筛选型规则用）
        elif len(args) > arg_index:
            out.add(args[arg_index].strip())
    return out


def resolve(name: str) -> str:
    """EventName.X -> 常量值；裸标识符 -> 末段（可能是常量短名或方法调用，靠前缀规则兜底）。"""
    name = name.strip().strip('"')
    short = name.split(".")[-1]
    return consts.get(short, short)


# 过滤明显是表达式 / 方法调用而非事件键的实参（含空格 / 括号 / 三目 / 拼接）
RAW_NOISE = re.compile(r'[\s()?:+]')
KEY_NOISE = re.compile(r'[\s()?:+]')
CONST_VALUES = set(consts.values())


def clean(args: set[str], known_only: bool = False) -> set[str]:
    """known_only=True 时只保留能解析成 EventName 常量的实参（用于「全部实参」筛选型规则）。"""
    out: set[str] = set()
    for a in args:
        if not a or (a.startswith('"') != a.endswith('"')):
            continue
        if RAW_NOISE.search(a):
            continue  # 表达式实参，无法静态求解
        k = resolve(a)
        if not k or KEY_NOISE.search(k):
            continue
        if known_only and k not in CONST_VALUES:
            continue
        out.add(k)
    return out


# A1 通道：无标签计数队列 triggers（count:<k> / recentpy:<k> 读得到）
plain_args: set[str] = set()
mech_keys: set[str] = set()   # 机制侧引用兜底收集到的键（低置信）
# A2 通道：带标签计数队列 taggedTriggers（count:<k>:<tag> / recently:<k>:<tag> 读得到）
tagged_args: set[str] = set()
# B 通道：计时仓库（只有 time_<k> / timer:<k> 读得到）
timer_args: set[str] = set()

# (正则, 事件键实参下标, arity 过滤)  —— arity=0 表示不过滤实参个数
# Event.record 的两参重载 Event.record(e, name) 走 triggers，三参 Event.record(e, name, tags) 走 taggedTriggers。
QUEUE_PLAIN_PATS = [
    (r'Event\.record\s*\(', 1, 2),
    (r'Event\.get\([^()]*\)\.record\s*\(', 0, 2),
    (r'GameEvent\.emit\s*\(', 0, 0),                  # emit 统一走 Event.record(key, value) → triggers
    # Batch 23 起三参重载（record(e,name,tags) / (e,name,value)）双写 triggers + taggedTriggers
    (r'Event\.record\s*\(', 1, 3),
    (r'Event\.get\([^()]*\)\.record\s*\(', 0, 3),
]
QUEUE_TAGGED_PATS = [
    (r'Event\.record\s*\(', 1, 3),
    (r'Event\.get\([^()]*\)\.record\s*\(', 0, 3),
    (r'recordTaggedHit\s*\(', 1, 0),                  # recordTaggedHit(owner, key, tags)
]
# 转发型调用：方法内部会再调 Event.record(e, key)，其键对两条队列都算已写入
# （静态扫描看不见跨方法转发，需显式声明——否则造成误报）
FORWARD_TO_PLAIN = [
    (r'recordTaggedHit\s*\(', 1, 0),
    # GameEvent 工厂（caused/taken/ofPlain/ailmentCaused/...）：事件名作为实参传入，
    # 由实例字段 eventName 在 fire() 中写队列，静态扫描看不到跨方法传递 —— 取全部实参筛选。
    (r'GameEvent\.(?:caused|taken|ofPlain|of[A-Z]\w*|ailmentCaused|ailmentTaken)\s*\(', -1, 0),
]
TIMER_PATS = [
    (r'refreshTimer(?:Infinite)?\s*\(', 0, 0),        # refreshTimer(name[, maxTicks])
    (r'GameEvent\.recordTimer\s*\(', 1, 0),           # GameEvent.recordTimer(owner, key)
]

for f in java_files:
    if is_event_self(f):
        continue  # 跳过 State 自身定义
    text = f.read_text(encoding="utf-8", errors="ignore")
    for pat, idx, arity in QUEUE_PLAIN_PATS:
        plain_args |= call_args(text, pat, idx, arity)
    for pat, idx, arity in QUEUE_TAGGED_PATS:
        tagged_args |= call_args(text, pat, idx, arity)
    for pat, idx, arity in FORWARD_TO_PLAIN:
        # GameEvent 工厂的事件名经 fire() → Event.record(owner, eventName, tags) 三参双写，两队列皆入
        plain_args |= clean(call_args(text, pat, idx, arity), known_only=True)
        tagged_args |= clean(call_args(text, pat, idx, arity), known_only=True)
    for pat, idx, arity in TIMER_PATS:
        timer_args |= call_args(text, pat, idx, arity)

# 过滤明显是表达式 / 方法调用而非事件键的实参（含空格 / 括号 / 三目 / 拼接）
RAW_NOISE = re.compile(r'[\s()?:+]')
KEY_NOISE = re.compile(r'[\s()?:+]')


explicit_plain = clean(plain_args)
explicit_tagged = clean(tagged_args)

# 机制侧引用兜底：EventName.X 出现在机制包（event/ helper/ codec/ attach/ ...）而非数据生成包（data/gen）中，
# 说明该事件在机制代码里被真实发出（可能经 firePair 等多层间接传入，静态扫描看不到实参传递链）。
# 数据生成包（data/gen/**）里的 EventName.X 才是**读取侧**引用，不计入写入方。
MECH_RE = re.compile(r'EventName\.([A-Z][A-Z0-9_]*)')
MECH_DIRS = ("/event/", "/helper/", "/codec/", "/attach/", "/skill/", "/status/", "/effect/")

for f in java_files:
    rel = str(f).replace("\\", "/")
    # 排除常量声明文件本身（否则 78 个常量全部"命中"，口径失效）
    if f.name in ("EventName.java", "EventCatalog.java"):
        continue
    if not any(d in rel for d in MECH_DIRS) or "/data/gen/" in rel:
        continue
    for cname in MECH_RE.findall(f.read_text(encoding="utf-8", errors="ignore")):
        if cname in consts:
            mech_keys.add(consts[cname])
written_plain = explicit_plain | mech_keys
written_tagged = explicit_tagged | mech_keys
timer_only = clean(timer_args) - written_plain - written_tagged

PREFIXES = ("skill_event:", "gain_", "consume_")


def base_of(key: str) -> str:
    """剥离引用后缀，得到事件名：count:<name>[:<tag>][.<secs>] → <name>。"""
    k = key.split(":")[0]
    dot = k.rfind(".")
    if dot > 0 and k[dot + 1:].isdigit():
        k = k[:dot]
    return k


def is_written(key: str) -> bool:
    """带标签引用（k:tag）查 taggedTriggers（三参双写后 plain 亦是证据）；无标签引用查 triggers。"""
    pool = written_tagged | written_plain if ":" in key else written_plain
    return base_of(key) in pool or any(key.startswith(p) for p in PREFIXES)


# ── 置信度分层 ──
# 高置信：能直接看到 Event.record / GameEvent.emit / recordTaggedHit 的调用点，事件名以实参形式出现。
# 低置信：只有 EventName.X 出现在机制包中（可能经 firePair 等多层间接传入），无显式调用点可见。
explicit_plain = clean(plain_args)
explicit_tagged = clean(tagged_args)

# ---- 3. 收集被引用键（严格事件上下文）----
INFIX_RE = re.compile(r'(?<![\w/])(?:count|recently|sum|avg|max|last|elapsed):([A-Za-z0-9_.:/]+)')
refs: dict[str, set[str]] = {}


def add(key: str, where: str) -> None:
    key = key.rstrip(".,")
    if KEY_RE.match(key):
        refs.setdefault(key, set()).add(where)


def walk(node, where: str) -> None:
    if isinstance(node, dict):
        if isinstance(node.get("type"), str) and node["type"] in EVENT_TYPES:
            n = node.get("name")
            if isinstance(n, str):
                add(n, where)
        for v in node.values():
            walk(v, where)
    elif isinstance(node, list):
        for v in node:
            walk(v, where)
    elif isinstance(node, str):
        for k in INFIX_RE.findall(node):
            add(k, where)


for f in STAT_JSON.rglob("*.json"):
    try:
        data = json.loads(f.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError):
        continue
    walk(data, f.name)

# 引擎侧源码里写死的中缀（ContextValueTypes / *Value / Wire*）
for f in java_files:
    rel = str(f).replace("\\", "/")
    if "/data/codec/support/math/" not in rel and "/data/gen/stat/" not in rel:
        continue
    # 剔除行注释后再扫——否则「缺口·不接线」注释里写明的示例表达式会被误当成真实引用
    text = re.sub(r"//[^\n]*", "", f.read_text(encoding="utf-8", errors="ignore"))
    for k in INFIX_RE.findall(text):
        add(k, f.name)

# ---- 4. 报告 ----
half_dead = {k: v for k, v in refs.items() if not is_written(k) and k in timer_only}
dead = {k: v for k, v in refs.items() if not is_written(k) and k not in timer_only}
unused = sorted(k for k in consts.values() if k not in refs)
print(f"[EventName] 常量数 = {len(consts)}")
print(f"[A1 通道]   无标签队列写入键 = {len(written_plain)}（count:<k> / recently:<k> 可用）")
print(f"[A2 通道]   带标签队列写入键 = {len(written_tagged)}（count:<k>:<tag> 可用）")
print(f"[B 通道]    仅计时写入键     = {len(timer_only)}（只有 time_<k> 可用）")
print(f"[被引用]    接线引用的事件键 = {len(refs)}")
print()

if dead:
    print(f"=== 完全死接线：{len(dead)} 个键被引用但两条通道都无写入方（恒 0，必须撤回）===")
    for k in sorted(dead):
        where = ", ".join(sorted(dead[k])[:4])
        print(f"  {k:<38} <- {where}")
else:
    print("=== 无完全死接线 ===")
print()

if half_dead:
    print(f"=== 半死接线：{len(half_dead)} 个键只走计时通道，却被 count/recently 引用（恒 0，改用 time_<k> 可复活）===")
    for k in sorted(half_dead):
        where = ", ".join(sorted(half_dead[k])[:4])
        print(f"  {k:<38} <- {where}")
else:
    print("=== 无半死接线 ===")

low_conf = sorted(k for k in refs if base_of(k) in written_plain and base_of(k) not in explicit_plain)
print(f"\n=== 低置信（{len(low_conf)}）：仅由机制侧引用推断，无显式 Event.record/emit 调用点，建议人工抽查 ===")
for k in low_conf:
    print(f"  {k}")

print(f"\n=== EventName 声明但从未被任何接线引用（{len(unused)}，非问题，仅供参考）===")
for k in unused:
    print(f"  {k}")

out = ROOT / "poe2_assets/event_record_audit.json"
out.write_text(json.dumps({
    "queue_plain_written": sorted(written_plain),
    "queue_tagged_written": sorted(written_tagged),
    "timer_written": sorted(timer_only),
    "referenced": {k: sorted(v) for k, v in sorted(refs.items())},
    "dead": {k: sorted(v) for k, v in sorted(dead.items())},
    "half_dead": {k: sorted(v) for k, v in sorted(half_dead.items())},
    "declared_unused": unused,
}, ensure_ascii=False, indent=1), encoding="utf-8")
print(f"\n→ {out}")
