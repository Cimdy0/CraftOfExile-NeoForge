"""一次性机械迁移：BootstrapContext<Skill> -> BootstrapContext<SkillEntry>（宝石注册表元素类型变更）。

只改签名与 reg 形参类型，不动任何技能数据。幂等：已迁移则跳过。
"""
import io
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "src/main/java/com/cimdy/coe"

TARGETS = [
    "data/gen/gem/GemGen.java",
    "data/gen/gem/skill/SkillRedGen.java",
    "data/gen/gem/skill/SkillGreenGen.java",
    "data/gen/gem/skill/SkillBlueGen.java",
    "data/gen/gem/spirit/SpiritRedGen.java",
    "data/gen/gem/spirit/SpiritGreenGen.java",
    "data/gen/gem/spirit/SpiritBlueGen.java",
]

IMPORT_LINE = "import com.cimdy.coe.data.codec.skill.SkillEntry;\n"

changed = []
for rel in TARGETS:
    p = SRC / rel
    text = p.read_text(encoding="utf-8")
    orig = text

    # 1) 泛型参数
    text = text.replace("BootstrapContext<Skill>", "BootstrapContext<SkillEntry>")

    # 2) reg 的技能形参类型（Skill skill / Skill gem -> SkillEntry ...）
    text = re.sub(r"(private static void reg\(BootstrapContext<SkillEntry> bootstrap, String path, )Skill (\w+)\)",
                  r"\1SkillEntry \2)", text)

    # 3) import 补齐
    if "BootstrapContext<SkillEntry>" in text and IMPORT_LINE not in text:
        lines = text.splitlines(keepends=True)
        idx = None
        for i, ln in enumerate(lines):
            if ln.startswith("import "):
                idx = i
                if ln > IMPORT_LINE:  # 按字典序插入
                    break
        if idx is None:
            raise SystemExit(f"no import block in {rel}")
        # 定位第一个字典序大于目标的 import
        pos = None
        for i, ln in enumerate(lines):
            if ln.startswith("import ") and ln > IMPORT_LINE:
                pos = i
                break
        if pos is None:
            # 放在最后一个 import 之后
            last = max(i for i, ln in enumerate(lines) if ln.startswith("import "))
            pos = last + 1
        lines.insert(pos, IMPORT_LINE)
        text = "".join(lines)

    if text != orig:
        p.write_text(text, encoding="utf-8")
        changed.append(rel)

print("changed:", len(changed))
for c in changed:
    print(" -", c)
