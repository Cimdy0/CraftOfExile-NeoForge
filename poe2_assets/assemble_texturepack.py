#!/usr/bin/env python3
"""
Assemble a standalone texture-pack folder from pixelated armor textures.
User rule: flat-item model -> icons only, NO json at all.

Output layout:
    <out-root>/texturepack/item/<snake>.png        (all icons in one folder)
    <out-root>/texturepack/<category>/<snake>.png  (also kept grouped by category)
"""
import shutil
from pathlib import Path

PIXELATE_CATEGORIES = {"Helmets", "Body_Armours", "Boots", "Gloves"}


def assemble(pixel_root: Path, pack_root: Path):
    flat_dir = pack_root / "item"
    flat_dir.mkdir(parents=True, exist_ok=True)
    total = 0
    for cat in sorted(PIXELATE_CATEGORIES):
        src = pixel_root / cat
        if not src.is_dir():
            print(f"[skip] {cat}: no pixel dir")
            continue
        cat_dir = pack_root / cat
        cat_dir.mkdir(parents=True, exist_ok=True)
        for png in sorted(src.glob("*.png")):
            shutil.copyfile(png, flat_dir / png.name)
            shutil.copyfile(png, cat_dir / png.name)
            total += 1
    print(f"assembled {total} icons into {pack_root} (flat: {flat_dir})")
    return total


if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--pixel-root", required=True)
    parser.add_argument("--pack-root", required=True)
    args = parser.parse_args()
    assemble(Path(args.pixel_root), Path(args.pack_root))
