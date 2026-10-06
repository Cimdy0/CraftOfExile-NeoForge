"""Crawl missing active-skill-gem icons from poe2db CDN and produce mod PNGs.

Confirmed pipeline (validated against rolling_slam):
  - gui/gem/<id>.png  == the SkillIcons webp (icon_url) at 64x64  (getIcon() renders this)
  - item/gem/<id>.png == same SkillIcons webp at native res (matches cyclone/toxic_rain siblings)

The 6 both-missing names map to DISTINCT poe2db artifacts:
  lightning_strike -> lightning_spear (LightningSpear)
  wolf_rake        -> rake (Rake)
  aftershock       -> aftershock_i (Aftershock support)
  armor_breaker    -> armour_breaker
  shockwave        -> shockwave_totem
  ice_shard        -> ice_shards
"""
import os, urllib.request
from PIL import Image

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', '..', '..'))
WEBP = os.path.join(ROOT, 'design', 'refactor', 'skill-data', 'crawled', 'icons', 'downloaded_webp')
PGUI = os.path.join(ROOT, 'src', 'main', 'resources', 'assets', 'coe', 'textures', 'gui', 'gem')
PITEM = os.path.join(ROOT, 'src', 'main', 'resources', 'assets', 'coe', 'textures', 'item', 'gem')
for d in (WEBP, PGUI, PITEM):
    os.makedirs(d, exist_ok=True)

# COE gem id -> poe2db SkillIcons webp url
MAPPING = {
    'lightning_strike': 'https://cdn.poe2db.tw/image/Art/2DArt/SkillIcons/HuntressStormSpear.webp',
    'wolf_rake':        'https://cdn.poe2db.tw/image/Art/2DArt/SkillIcons/HuntressBloodHunt.webp',
    'aftershock':       'https://cdn.poe2db.tw/image/Art/2DArt/SkillIcons/Support/AftershockSupport.webp',
    'armor_breaker':    'https://cdn.poe2db.tw/image/Art/2DArt/SkillIcons/BruteHeavyStrike.webp',
    'shockwave':        'https://cdn.poe2db.tw/image/Art/2DArt/SkillIcons/BruteShockwaveTotem.webp',
    'ice_shard':        'https://cdn.poe2db.tw/image/Art/2DArt/SkillIcons/RapidShotPermafrost.webp',
    'cyclone':          'https://cdn.poe2db.tw/image/Art/2DArt/SkillIcons/cyclone.webp',
    'toxic_rain':       'https://cdn.poe2db.tw/image/Art/2DArt/SkillIcons/RainOfSpores.webp',
    'frost_nova':       'https://cdn.poe2db.tw/image/Art/2DArt/SkillIcons/FrostBomb.webp',
    'lacerate':         'https://cdn.poe2db.tw/image/Art/2DArt/SkillIcons/BloodLacerate.webp',
    'puncture':         'https://cdn.poe2db.tw/image/Art/2DArt/SkillIcons/puncture.webp',
    'blight':           'https://cdn.poe2db.tw/image/Art/2DArt/SkillIcons/Blight.webp',
    'voltaic_grenade':  'https://cdn.poe2db.tw/image/Art/2DArt/SkillIcons/ShockGrenade.webp',
    'consume_corpse':   'https://cdn.poe2db.tw/image/Art/2DArt/SkillIcons/WitchConsumeCorpse.webp',
    'ground_slam':      'https://cdn.poe2db.tw/image/Art/2DArt/SkillIcons/icongroundslam.webp',
    'perforate':        'https://cdn.poe2db.tw/image/Art/2DArt/SkillIcons/Bloodspears.webp',
}
# names that ALSO need item/gem (those whose item/gem png is missing)
NEED_ITEM = {'lightning_strike', 'wolf_rake', 'aftershock', 'armor_breaker', 'shockwave', 'ice_shard'}

UA = {'User-Agent': 'Mozilla/5.0'}

def download(url, name):
    out = os.path.join(WEBP, name)
    if os.path.exists(out) and os.path.getsize(out) > 0:
        return out, 'cached'
    req = urllib.request.Request(url, headers=UA)
    data = urllib.request.urlopen(req, timeout=45).read()
    open(out, 'wb').write(data)
    return out, 'downloaded'

def to_png(webp_path, dst, size=None):
    im = Image.open(webp_path).convert('RGBA')
    if size and im.size != size:
        im = im.resize(size, Image.LANCZOS)
    im.save(dst, 'PNG')
    return im.size

ok = fail = skip = 0
report = []
for gid, url in MAPPING.items():
    try:
        wp, st = download(url, gid + '.webp')
        # gui (always 64x64)
        gdst = os.path.join(PGUI, gid + '.png')
        if os.path.exists(gdst):
            gsz = 'skip'
        else:
            gsz = to_png(wp, gdst, (64, 64)); ok += 1
        # item (only if required)
        isz = 'n/a'
        if gid in NEED_ITEM:
            idst = os.path.join(PITEM, gid + '.png')
            if os.path.exists(idst):
                isz = 'skip'
            else:
                isz = to_png(wp, idst); ok += 1
        report.append((gid, st, str(gsz), str(isz)))
    except Exception as e:
        fail += 1
        report.append((gid, 'FAIL', str(e), ''))

print(f"converted={ok} failed={fail}")
for r in report:
    print('  ', r)

# verify
print("\n=== verification: missing files now present? ===")
missing_gui = [g for g in MAPPING if not os.path.exists(os.path.join(PGUI, g + '.png'))]
missing_item = [g for g in NEED_ITEM if not os.path.exists(os.path.join(PITEM, g + '.png'))]
print('gui still missing:', missing_gui)
print('item still missing:', missing_item)
