#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""B3 Batch A 续：三个非 Dark 预设覆盖新令牌（否则换肤依旧不生效）。"""
import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from b3_apply import apply, assert_no_collision

PRESET = "src/main/java/com/cimdy/coe/ui/widget/render/StylePreset.java"

print("[Batch A2] 预设覆盖")
assert_no_collision([
    "0xA5000000", "0xFFDDDDDD", "0xFFFF8A8A",          # HighContrast
    "0x38000000", "0xFF757575", "0xFFC62828",          # Paper
    "0xFFEAF2FF", "0x8A000000", "0xFF8A94A6", "0xFFE8736A",  # Midnight
])

apply(PRESET, [
    # ---- HighContrast ----
    ("""        @Override public String id()          { return "high_contrast"; }""",
     """        @Override public int scrim()          { return 0xA5000000; }
        @Override public int textMuted()      { return 0xFFDDDDDD; }
        @Override public int textDanger()     { return 0xFFFF8A8A; }
        @Override public String id()          { return "high_contrast"; }"""),
    # ---- Paper ----
    ("""        @Override public String id()          { return "paper"; }""",
     """        @Override public int scrim()          { return 0x38000000; }
        @Override public int textMuted()      { return 0xFF757575; }
        @Override public int textDanger()     { return 0xFFC62828; }
        @Override public String id()          { return "paper"; }"""),
    # ---- Midnight ----
    ("""        @Override public String id()          { return "midnight"; }""",
     """        @Override public int textBright()     { return 0xFFEAF2FF; }
        @Override public int scrim()          { return 0x8A000000; }
        @Override public int textMuted()      { return 0xFF8A94A6; }
        @Override public int textDanger()     { return 0xFFE8736A; }
        @Override public String id()          { return "midnight"; }"""),
])
print("[Batch A2] done")
