# -*- coding: utf-8 -*-
"""
fetch_status_icons.py
=====================

从 poe2db（https://poe2db.tw）抓取 CraftOfExile-NeoForge 的 HUD 状态图标，
统一处理成 **64x64 RGBA PNG** 后落到：

    src/main/resources/assets/coe/textures/gui/status/<name>.png

用法：
    python scripts/fetch_status_icons.py              # 抓取缺失的图标（已存在则跳过）
    python scripts/fetch_status_icons.py --force      # 强制重新下载覆盖
    python scripts/fetch_status_icons.py --only infusion minion_reviving
    python scripts/fetch_status_icons.py --list       # 只列出映射表，不下载

补新图标时：只需在下面的 ICON_SOURCES 里加一行 "文件名": "来源 URL" 即可。

poe2db 图片路径规律（已验证）：
    Buff 图标   https://cdn.poe2db.tw/image/Art/2DArt/BuffIcons/<Name>.webp
    技能图标   https://cdn.poe2db.tw/image/Art/2DArt/SkillIcons/<Name>.webp
页面侧：https://poe2db.tw/us/<PageName> ，HTML 里搜 `/Art/2DArt/BuffIcons/` 即可定位图标。
CDN 需要带 User-Agent + Referer，否则会被挡。
"""

import argparse
import io
import os
import ssl
import struct
import sys
import urllib.error
import urllib.request

# --------------------------------------------------------------------------
# 配置：图标名 -> 来源 URL（以后补图标就在这里加一行）
# --------------------------------------------------------------------------
ICON_SOURCES = {
    # 元素灌注 Elemental Infusion：poe2db 有独立 buff 页
    #   https://poe2db.tw/us/Infusion
    # 页面 HTML 中的 <div class="buff-icon-container buff-icon-type__Buff"> 内即为该图标
    "infusion": "https://cdn.poe2db.tw/image/Art/2DArt/BuffIcons/InfusionBuff.webp",

    # 召唤物复活中 Minion Reviving：
    #   poe2db 上「Reviving Minions」只是关键字页（/us/Reviving_Minions），没有独立 buff 图标。
    #   退而求其次，取语义最接近的亡灵复苏类图标：Unearth（从地面/尸体中掘起骸骨构造体）。
    #   备选：SkillIcons/GraveCommand.webp（坟墓号令，骷髅主题）
    "minion_reviving": "https://cdn.poe2db.tw/image/Art/2DArt/SkillIcons/WitchUnearth.webp",
}

# 备用源（主源 404 时按顺序尝试），可选
ICON_FALLBACKS = {
    "minion_reviving": [
        "https://cdn.poe2db.tw/image/Art/2DArt/SkillIcons/GraveCommand.webp",
    ],
}

ICON_SIZE = 64
ICON_DIR = "src/main/resources/assets/coe/textures/gui/status"

HEADERS = {
    "User-Agent": (
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
        "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
    ),
    "Referer": "https://poe2db.tw/",
    "Accept": "image/avif,image/webp,image/apng,image/*,*/*;q=0.8",
    "Accept-Language": "zh-CN,zh;q=0.9,en;q=0.8",
}

# 部分环境下 poe2db 的证书链校验会失败，这里放宽 SSL 校验（仅用于抓取公开静态图片）
SSL_CTX = ssl.create_default_context()
SSL_CTX.check_hostname = False
SSL_CTX.verify_mode = ssl.CERT_NONE


def log(msg=""):
    print(msg, flush=True)


def http_get(url, timeout=30):
    """下载字节流，失败抛异常。"""
    req = urllib.request.Request(url, headers=HEADERS)
    with urllib.request.urlopen(req, timeout=timeout, context=SSL_CTX) as resp:
        if resp.status != 200:
            raise urllib.error.HTTPError(url, resp.status, "非 200 响应", resp.headers, None)
        return resp.read()


def to_64png(raw: bytes) -> bytes:
    """把任意 Pillow 可读的图片字节转成 64x64 RGBA PNG。"""
    try:
        from PIL import Image
    except ImportError as exc:  # pragma: no cover
        raise RuntimeError(
            "缺少 Pillow（PIL）。请先执行： python -m pip install Pillow\n"
            "Pillow 用于把 poe2db 的 webp 图标转码并缩放为 64x64 PNG。"
        ) from exc

    img = Image.open(io.BytesIO(raw))
    img.load()
    if img.mode != "RGBA":
        img = img.convert("RGBA")
    if img.size != (ICON_SIZE, ICON_SIZE):
        img = img.resize((ICON_SIZE, ICON_SIZE), Image.LANCZOS)
    buf = io.BytesIO()
    img.save(buf, format="PNG", optimize=True)
    return buf.getvalue()


def png_size(data: bytes):
    """读 PNG 头返回 (宽, 高)。"""
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        return None
    return struct.unpack(">II", data[16:24])


def fetch_one(name, url, out_path, force=False, extra_urls=()):
    """下载并处理单个图标，返回 True/False。"""
    target = os.path.join(out_path, name + ".png")

    if os.path.exists(target) and not force:
        with open(target, "rb") as fh:
            size = png_size(fh.read(33))
        log("  [跳过] %-18s 已存在 %s（加 --force 可覆盖）" % (name, size or "尺寸未知"))
        return True

    candidates = [url] + list(extra_urls or ())
    last_err = None
    for i, u in enumerate(candidates):
        tag = "主源" if i == 0 else "备源%d" % i
        try:
            log("  [下载] %-18s %s <- %s" % (name, tag, u))
            raw = http_get(u)
            log("          收到 %d 字节" % len(raw))
            png = to_64png(raw)
            os.makedirs(out_path, exist_ok=True)
            with open(target, "wb") as fh:
                fh.write(png)
            size = png_size(png)
            log("  [成功] %-18s -> %s  尺寸=%s  %d 字节"
                % (name, os.path.basename(target), size, len(png)))
            return True
        except urllib.error.HTTPError as exc:
            last_err = "HTTP %s %s（多半是防盗链或 URL 失效）" % (exc.code, exc.reason)
            log("  [失败] %-18s %s：%s" % (name, tag, last_err))
        except Exception as exc:  # noqa: BLE001 - 网络/解码错误都要兜住继续
            last_err = "%s: %s" % (type(exc).__name__, exc)
            log("  [失败] %-18s %s：%s" % (name, tag, last_err))

    log("  [放弃] %-18s 所有源都失败，最后一次错误：%s" % (name, last_err))
    return False


def main():
    ap = argparse.ArgumentParser(description="抓取 poe2db 状态图标并统一为 64x64 PNG")
    ap.add_argument("--force", action="store_true", help="已存在也重新下载覆盖")
    ap.add_argument("--only", nargs="*", default=None, help="只抓指定的图标名")
    ap.add_argument("--list", action="store_true", help="只列出映射表")
    args = ap.parse_args()

    root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    out_dir = os.path.join(root, *ICON_DIR.split("/"))

    if args.list:
        log("图标映射表（共 %d 条）：" % len(ICON_SOURCES))
        for k, v in ICON_SOURCES.items():
            log("  %-18s %s" % (k, v))
        return 0

    names = args.only if args.only else list(ICON_SOURCES)
    unknown = [n for n in names if n not in ICON_SOURCES]
    if unknown:
        log("[错误] 未知图标名：%s" % "、".join(unknown))
        log("       可用：%s" % "、".join(ICON_SOURCES))
        return 2

    log("=" * 72)
    log("CraftOfExile 状态图标抓取")
    log("  输出目录：%s" % out_dir)
    log("  目标尺寸：%dx%d PNG" % (ICON_SIZE, ICON_SIZE))
    log("  待处理  ：%d 个（%s）" % (len(names), "、".join(names)))
    log("=" * 72)

    ok, fail = [], []
    for n in names:
        if fetch_one(n, ICON_SOURCES[n], out_dir, args.force, ICON_FALLBACKS.get(n, ())):
            ok.append(n)
        else:
            fail.append(n)

    # ---------- 复核：直接读 PNG 头 ----------
    log("-" * 72)
    log("尺寸复核（读 PNG IHDR）：")
    all_ok = True
    for n in names:
        p = os.path.join(out_dir, n + ".png")
        if not os.path.exists(p):
            log("  [缺失] %-18s %s" % (n, p))
            all_ok = False
            continue
        with open(p, "rb") as fh:
            size = png_size(fh.read(33))
        good = size == (ICON_SIZE, ICON_SIZE)
        log("  [%s] %-18s %s" % ("通过" if good else "不符", n, size))
        all_ok = all_ok and good

    log("-" * 72)
    if fail:
        log("完成：成功 %d 个（%s），失败 %d 个（%s）" % (len(ok), "、".join(ok) or "无",
                                                       len(fail), "、".join(fail)))
        return 1
    log("全部完成：成功 %d 个（%s），尺寸校验%s" % (len(ok), "、".join(ok),
                                                    "通过" if all_ok else "未通过"))
    return 0 if all_ok else 1


if __name__ == "__main__":
    sys.exit(main())
