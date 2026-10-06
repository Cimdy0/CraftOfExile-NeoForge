#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""B3 迁移写入器：保留原文件换行符（CRLF/LF），E 盘安全写回。"""
import os, sys, re

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"


def read_text(rel):
    p = os.path.join(ROOT, rel)
    with open(p, "rb") as fh:
        b = fh.read()
    crlf = b.count(b"\r\n")
    lf = b.count(b"\n") - crlf
    nl = "\r\n" if crlf > lf else "\n"
    return b.decode("utf-8").replace("\r\n", "\n"), nl


def write_text(rel, text, nl):
    p = os.path.join(ROOT, rel)
    data = text.replace("\n", nl).encode("utf-8")
    with open(p, "wb") as fh:
        fh.write(data)
    print(f"  wrote {rel} ({len(data)} bytes, nl={'CRLF' if nl == chr(13)+chr(10) else 'LF'})")


def apply(rel, pairs):
    text, nl = read_text(rel)
    for old, new in pairs:
        n = text.count(old)
        if n == 0:
            print(f"  !! MISS in {rel}: {old[:80]!r}")
            sys.exit(1)
        text = text.replace(old, new)
        print(f"  ok  {rel}: x{n}  {old.strip()[:70]}")
    write_text(rel, text, nl)


THEME37 = {
    "ff6ba8d6", "ff8fc4e8", "ff3a3a3a", "60000000", "ff1a1a1a", "a0000000",
    "c0101010", "f0121212", "ff4a4a4a", "40000000", "401e2735", "663d5a80",
    "90202020", "90104060", "b0000000", "40101010", "ff5a5a5a", "ff2a2a2a",
    "90ffffff", "40ffffff", "806ba8d6", "60ffffff", "ffffffcc", "ff666666",
    "ffe8e8e8", "50202020", "50000000", "ff3a6e94", "ffd8d8d8", "40202020",
    "401a1a1a", "90204060", "403a3a3a", "ff5ba85b", "ffd9a23b", "ffd05a5a",
    "40181818",
}


def assert_no_collision(values):
    bad = []
    for v in values:
        key = v.lower().replace("0x", "")
        if len(key) == 6:
            key = "00" + key
        if key in THEME37:
            bad.append(v)
    if bad:
        print("  !! COLLIDES with Theme constants: " + ", ".join(bad))
        sys.exit(1)
    print(f"  ok  {len(values)} new default values: 0 collisions with the 37 Theme constants")
