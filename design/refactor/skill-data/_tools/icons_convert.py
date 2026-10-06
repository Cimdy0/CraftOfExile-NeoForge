"""webp -> png：把下载的技能图标转成 Minecraft 可用的 PNG，落 textures/item/gem/"""
import sys, os
from PIL import Image

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', '..', '..'))
SRC = os.path.join(ROOT, 'design', 'refactor', 'skill-data', 'crawled', 'icons', 'downloaded_webp')
DST = os.path.join(ROOT, 'src', 'main', 'resources', 'assets', 'coe', 'textures', 'item', 'gem')

os.makedirs(DST, exist_ok=True)
files = [f for f in os.listdir(SRC) if f.lower().endswith('.webp')]
ok = fail = skip = 0
for f in sorted(files):
    name = os.path.splitext(f)[0] + '.png'
    out = os.path.join(DST, name)
    if os.path.exists(out):
        skip += 1
        continue
    try:
        im = Image.open(os.path.join(SRC, f)).convert('RGBA')
        im.save(out, 'PNG')
        ok += 1
    except Exception as e:
        print('FAIL', f, e)
        fail += 1

print('converted=%d skipped=%d failed=%d  -> %s' % (ok, skip, fail, DST))
