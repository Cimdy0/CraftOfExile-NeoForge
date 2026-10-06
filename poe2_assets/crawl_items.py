#!/usr/bin/env python3
"""
PoE2 item texture crawler + Minecraft pixel-art converter (v2).

Usage:
    python crawl_items.py --categories Helmets Boots --out-root E:\\...\\poe2_assets --manifest manifest_shard1.csv
    python crawl_items.py --shard 1 --total 4   (auto-split all 33 categories)

Only categories in PIXELATE_CATEGORIES get pixelated; all get webp + manifest.
Pixelation v2: 32x32 shape-preserving (crop-square -> autocontrast -> median-cut
no-dither quantization -> BOX -> alpha threshold + 1px dark outline).
"""
import argparse
import csv
import os
import re
import sys
import time
import urllib.request
import urllib.error
from pathlib import Path

from PIL import Image, ImageOps

# ── Constants ──────────────────────────────────────────────────────────────
ALL_CATEGORIES = [
    "Claws", "Daggers", "Wands", "One_Hand_Swords", "One_Hand_Axes",
    "One_Hand_Maces", "Sceptres", "Spears", "Flails", "Bows", "Staves",
    "Two_Hand_Swords", "Two_Hand_Axes", "Two_Hand_Maces", "Quarterstaves",
    "Crossbows", "Traps", "Talismans", "Quivers", "Shields", "Bucklers",
    "Foci", "Gloves", "Boots", "Body_Armours", "Helmets", "Amulets",
    "Rings", "Belts", "Life_Flasks", "Mana_Flasks", "Charms", "Jewels",
]

PIXELATE_CATEGORIES = {"Helmets", "Body_Armours", "Boots", "Gloves"}

BASE_URL = "https://poe2db.tw/cn/"
CDN_BASE = "https://cdn.poe2db.tw/image/"

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
    "Accept": "text/html,application/xhtml+xml",
}

IMG_HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
    "Accept": "image/webp,image/apng,image/*,*/*;q=0.8",
    "Referer": "https://poe2db.tw/",
}

DELAY = 0.35       # seconds between requests
MAX_RETRY = 3
TIMEOUT = 20

# In-memory cache: CDN URL -> bytes (shared images used by multiple basetypes)
_url_cache = {}

# ── Helpers ────────────────────────────────────────────────────────────────

def http_get(url, headers=None):
    """GET with retry + backoff. Returns bytes."""
    hdrs = headers if headers else HEADERS
    for attempt in range(1, MAX_RETRY + 1):
        try:
            req = urllib.request.Request(url, headers=hdrs)
            with urllib.request.urlopen(req, timeout=TIMEOUT) as resp:
                data = resp.read()
                if not data:
                    raise ValueError("empty response")
                return data
        except Exception as e:
            if attempt == MAX_RETRY:
                raise
            wait = DELAY * attempt * 2
            print(f"    [retry {attempt}/{MAX_RETRY}] {url} -> {e}; wait {wait:.1f}s")
            time.sleep(wait)
    return None  # unreachable


def download_image(url):
    """Download a CDN image with Referer header and in-memory URL cache."""
    global _url_cache
    if url in _url_cache:
        return _url_cache[url]
    data = http_get(url, headers=IMG_HEADERS)
    _url_cache[url] = data
    return data


def is_valid_webp(data: bytes) -> bool:
    """Check RIFF....WEBP header and non-trivial size."""
    return len(data) > 100 and data[:4] == b"RIFF" and data[8:12] == b"WEBP"


def to_snake_case(name: str) -> str:
    """Shabby_Hood -> shabby_hood; IronBuckler -> iron_buckler."""
    s = name.strip()
    if "_" in s:
        return s.lower()
    # CamelCase -> snake_case
    s = re.sub(r"(?<!^)(?=[A-Z])", "_", s).lower()
    return s


def _is_valid_pixel(png_path: Path) -> bool:
    """A pixel PNG is valid only if it exists and is exactly 32x32 (algorithm v2)."""
    try:
        with Image.open(png_path) as im:
            return im.size == (32, 32)
    except Exception:
        return False


def parse_category_page(html: str, category: str):
    """
    Parse basetype items from a category page.
    Returns list of dicts: {english_name, chinese_name, image_url, image_alt}
    Item images live under cdn.poe2db.tw/image/Art/2DItems/... (path varies by
    category: some under /Basetypes/, others under e.g. Offhand/Shields/).
    """
    # Find all <a ... href="X"> ... </a> blocks (non-greedy)
    anchor_re = re.compile(r'<a\b[^>]*?href="([^"]+)"[^>]*>(.*?)</a>', re.DOTALL)
    # Group anchors by href
    groups = {}
    for m in anchor_re.finditer(html):
        href = m.group(1)
        inner = m.group(2)
        groups.setdefault(href, []).append(inner)

    items = []
    for href, inners in groups.items():
        # href should be a simple basetype name (no slash, no http)
        if "/" in href or href.startswith("http"):
            continue
        english_name = href
        image_url = None
        image_alt = None
        chinese_name = ""
        for inner in inners:
            # look for img under cdn.poe2db.tw/image/.../2DItems/...
            img_m = re.search(
                r'<img[^>]*src="(https://cdn\.poe2db\.tw/image/[^"]*2DItems/[^"]+\.webp)"[^>]*>',
                inner,
            )
            if img_m:
                image_url = img_m.group(1)
                alt_m = re.search(r'alt="([^"]*)"', inner)
                if alt_m:
                    image_alt = alt_m.group(1)
            # text content (strip tags) for Chinese name
            text = re.sub(r"<[^>]+>", "", inner).strip()
            if text and not chinese_name:
                chinese_name = text
        if image_url:
            items.append({
                "english_name": english_name,
                "chinese_name": chinese_name,
                "image_url": image_url,
                "image_alt": image_alt or "",
            })
    return items


# ── Pixelation (v2: 32x32 shape-preserving) ────────────────────────────────

try:
    import _pixel_v2 as pixel_v2
except ImportError:
    _here = Path(__file__).resolve().parent
    sys.path.insert(0, str(_here))
    import _pixel_v2 as pixel_v2


def pixelate(src_path: Path, dst_path: Path, size: int = 32) -> bool:
    """
    Convert a PoE2 webp item icon to an MC-style 32x32 pixel PNG (algorithm v2):
    silhouette extraction -> foreground-weighted area downscale -> per-channel
    autocontrast -> weighted k-means 16-color quantization -> binary alpha ->
    1px dark outline. Keeps the recognizable silhouette of each basetype.
    """
    try:
        img = pixel_v2.convert(str(src_path), size=size, ncolors=16)
    except Exception as e:
        print(f"    [pixelate FAIL open] {src_path.name}: {e}")
        return False
    if img is None:
        print(f"    [pixelate FAIL empty] {src_path.name}")
        return False
    dst_path.parent.mkdir(parents=True, exist_ok=True)
    img.save(dst_path, "PNG")
    return True


# ── Main pipeline ──────────────────────────────────────────────────────────

def process_category(category: str, out_root: Path, writer):
    """Crawl one category: download webps, pixelate if applicable, write manifest."""
    url = BASE_URL + category
    print(f"[{category}] fetching {url}")
    try:
        html_bytes = http_get(url)
        html = html_bytes.decode("utf-8", errors="replace")
    except Exception as e:
        print(f"[{category}] FATAL page fetch failed: {e}")
        return {"category": category, "total": 0, "downloaded": 0, "pixelated": 0, "failed": [f"page: {e}"]}

    items = parse_category_page(html, category)
    print(f"[{category}] found {len(items)} basetype items")

    webp_dir = out_root / "item" / category
    webp_dir.mkdir(parents=True, exist_ok=True)

    pixel_dir = out_root / "item_pixel" / category
    do_pixel = category in PIXELATE_CATEGORIES
    if do_pixel:
        pixel_dir.mkdir(parents=True, exist_ok=True)

    downloaded = 0
    pixelated = 0
    failed = []

    for i, item in enumerate(items):
        eng = item["english_name"]
        webp_path = webp_dir / f"{eng}.webp"
        # Skip if already downloaded and valid
        if webp_path.exists() and webp_path.stat().st_size > 100:
            try:
                with open(webp_path, "rb") as f:
                    if is_valid_webp(f.read(12)):
                        downloaded += 1
                        # still need manifest row
                        writer.writerow([
                            category, eng, item["chinese_name"],
                            item["image_url"], f"item/{category}/{eng}.webp",
                        ])
                        # pixelate if needed and not already done
                        if do_pixel:
                            png_path = pixel_dir / f"{to_snake_case(eng)}.png"
                            if png_path.exists() and _is_valid_pixel(png_path):
                                pixelated += 1
                            else:
                                if pixelate(webp_path, png_path):
                                    pixelated += 1
                                else:
                                    failed.append(f"pixelate: {eng}")
                        continue
            except Exception:
                pass

        # Download (cached by URL to avoid re-fetching shared images)
        try:
            data = download_image(item["image_url"])
            if not is_valid_webp(data):
                failed.append(f"invalid webp: {eng}")
                print(f"    [{i+1}/{len(items)}] INVALID: {eng}")
                time.sleep(DELAY)
                continue
            with open(webp_path, "wb") as f:
                f.write(data)
            downloaded += 1
            writer.writerow([
                category, eng, item["chinese_name"],
                item["image_url"], f"item/{category}/{eng}.webp",
            ])
            print(f"    [{i+1}/{len(items)}] {eng} ({item['chinese_name']}) -> {len(data)} bytes")
        except Exception as e:
            failed.append(f"download: {eng}: {e}")
            print(f"    [{i+1}/{len(items)}] FAIL: {eng}: {e}")
            time.sleep(DELAY)
            continue

        # Pixelate
        if do_pixel:
            png_path = pixel_dir / f"{to_snake_case(eng)}.png"
            if pixelate(webp_path, png_path):
                pixelated += 1
            else:
                failed.append(f"pixelate: {eng}")

        time.sleep(DELAY)

    print(f"[{category}] done: {downloaded}/{len(items)} downloaded, {pixelated} pixelated, {len(failed)} failed")
    return {
        "category": category,
        "total": len(items),
        "downloaded": downloaded,
        "pixelated": pixelated,
        "failed": failed,
    }


def main():
    parser = argparse.ArgumentParser(description="Crawl PoE2 item textures and pixelate armor.")
    parser.add_argument("--categories", nargs="+", help="Explicit category list")
    parser.add_argument("--shard", type=int, help="Shard index (1-based)")
    parser.add_argument("--total", type=int, help="Total number of shards")
    parser.add_argument("--out-root", required=True, help="poe2_assets root directory")
    parser.add_argument("--manifest", required=True, help="Output manifest CSV path for this shard")
    args = parser.parse_args()

    if args.categories:
        cats = args.categories
    elif args.shard and args.total:
        # Distribute categories round-robin for balance
        cats = [ALL_CATEGORIES[i] for i in range(args.shard - 1, len(ALL_CATEGORIES), args.total)]
    else:
        parser.error("Provide --categories or --shard/--total")

    out_root = Path(args.out_root)
    out_root.mkdir(parents=True, exist_ok=True)

    manifest_path = Path(args.manifest)
    manifest_path.parent.mkdir(parents=True, exist_ok=True)

    print(f"Shard categories ({len(cats)}): {cats}")
    print(f"Output root: {out_root}")
    print(f"Manifest: {manifest_path}")
    print(f"Pixelating: {sorted(PIXELATE_CATEGORIES)}")
    print("=" * 60)

    all_results = []
    with open(manifest_path, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(["category", "english_name", "chinese_name", "source_url", "image_file"])
        for cat in cats:
            result = process_category(cat, out_root, writer)
            all_results.append(result)
            f.flush()

    # Summary
    print("\n" + "=" * 60)
    print("SHARD SUMMARY")
    print("=" * 60)
    total_items = 0
    total_dl = 0
    total_px = 0
    total_fail = 0
    for r in all_results:
        print(f"  {r['category']:20s}  items={r['total']:3d}  dl={r['downloaded']:3d}  px={r['pixelated']:3d}  fail={len(r['failed'])}")
        total_items += r["total"]
        total_dl += r["downloaded"]
        total_px += r["pixelated"]
        total_fail += len(r["failed"])
        for fail in r["failed"]:
            print(f"      FAIL: {fail}")
    print(f"  {'TOTAL':20s}  items={total_items:3d}  dl={total_dl:3d}  px={total_px:3d}  fail={total_fail}")
    print("=" * 60)


if __name__ == "__main__":
    main()
