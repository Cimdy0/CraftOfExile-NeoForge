#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Teach verify_gen's entry-amount check about StatGroup entries.

A group entry ("entry": "group") has NO top-level `amount`; the amount lives on each
member. Without this, every hybrid affix is reported as 'no-$value'.
"""
import io

P = "scripts/verify_gen.py"
src = io.open(P, encoding="utf-8").read()

OLD = '''    # amount is an expression object {value:"$value"} or {"$value"} etc.
    amt = obj.get("amount", obj.get("statValue", None))
    s = json.dumps(amt)
    if "$value" not in s and "value" not in s:
        # try nested
        if not (isinstance(amt, dict) and ("value" in amt or "$value" in amt)):
            bad_amount.append((fn, "no-$value"))'''

NEW = '''    # 组合条目（StatGroup / "entry":"group"）：无顶层 amount，金额由各 member 承载。
    # 校验方式改为逐个 member 检查，否则每条 hybrid 词缀都会误报 no-$value。
    if obj.get("entry") == "group":
        members = obj.get("members") or []
        if not members:
            bad_amount.append((fn, "group-no-members"))
            continue
        for i, mem in enumerate(members):
            mamt = mem.get("amount", mem.get("statValue", None))
            ms = json.dumps(mamt)
            if "$value" not in ms and "value" not in ms:
                bad_amount.append((fn, "group-member-%d-no-$value" % i))
        continue
    # amount is an expression object {value:"$value"} or {"$value"} etc.
    amt = obj.get("amount", obj.get("statValue", None))
    s = json.dumps(amt)
    if "$value" not in s and "value" not in s:
        # try nested
        if not (isinstance(amt, dict) and ("value" in amt or "$value" in amt)):
            bad_amount.append((fn, "no-$value"))'''

if OLD not in src:
    raise SystemExit("anchor not found - already patched?")
io.open(P, "w", encoding="utf-8").write(src.replace(OLD, NEW, 1))
print("verify_gen entry-amount check now understands group entries")
