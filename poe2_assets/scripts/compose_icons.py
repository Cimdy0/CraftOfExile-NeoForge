# -*- coding: utf-8 -*-
"""图标合成 v11（仿制图章版）：美术不放大 + 纹理反射克隆填充 + 严格收边。

规则（用户裁定 + 多轮更正收敛）：
 1. 代码核查（COEModelProvider.java / ItemRegister.java）：模组物品图标统一
    FLAT_ITEM（item/generated），图标即贴图 alpha，渲染/模型无法改变图标
    形状；护甲穿戴走原版 humanoidArmor（LEATHER），手套=LEGS 腿甲槽确认；
 2. 美术【不放大】（contain 居中，形制与细节完整保留）；
 3. 形状框内未覆盖处：用【仿制图章】式纹理延拓——以美术剪影边缘为反射轴，
    把美术自身的纹理反射克隆到空缺区（无缝、有纹理、无平涂、无渐变、无
    合成色区）；反射落空处再以反射瓦片采样兜底，保证处处有纹理；
 4. 【严格收在原版剪影边界内】：不再绘制任何超出原样式的虚框/外圈描边；
 5. Gloves（腿甲槽）：以【原版铁裤明暗结构】为底 + 装备亮半段色系覆盖
    （用户方案：原版贴图为模板、POE2 色彩覆盖修饰），美术光照零参与，
    杜绝"往裤子里塞手套"；
 6. 扁平物品模型，只输出贴图 PNG，不生成任何 json。

槽位映射：Helmets->iron_helmet, Body_Armours->iron_chestplate,
         Boots->iron_boots, Gloves->iron_leggings(material)。
输出 32x32，透明背景。
"""
from PIL import Image
import numpy as np
import os

LUMA = np.array([0.299, 0.587, 0.114], np.float32)

TEMPLATES = {
    'Helmets': ('iron_helmet.png', 'art'),
    'Body_Armours': ('iron_chestplate.png', 'art'),
    'Boots': ('iron_boots.png', 'art'),
    'Gloves': ('iron_leggings.png', 'material'),
}

# 原版模板边缘/内部亮度比（实测）：描边强度按分类对齐原版
RIM_RATIO = {'Helmets': 0.40, 'Body_Armours': 0.27, 'Boots': 0.30, 'Gloves': 0.23}

_NEIGHBORS8 = ((-1, 0), (1, 0), (0, -1), (0, 1), (-1, -1), (-1, 1), (1, -1), (1, 1))


def _rim16(mask16):
    """16x16 模板 alpha 的 1px 内描边（4邻有透明的实体像素）。"""
    m = mask16.astype(np.uint8)
    nb = np.zeros_like(m, bool)
    nb[:-1] |= m[1:] == 0
    nb[1:] |= m[:-1] == 0
    nb[:, :-1] |= m[:, 1:] == 0
    nb[:, 1:] |= m[:, :-1] == 0
    return m.astype(bool) & nb


def _lift_colors(px):
    """亮度 gamma 提升 + 饱和微增：放大 PoE2 装备固有色调差异、提升可读性。"""
    import colorsys
    hsv = np.array([[colorsys.rgb_to_hsv(r, g, b) for r, g, b in px]], dtype=float)[0]
    h, s, v = hsv[:, 0], hsv[:, 1], hsv[:, 2]
    v = np.clip(v ** 0.55, 0, 1)
    s = np.clip(s * 1.35, 0, 1)
    return np.array([colorsys.hsv_to_rgb(hh, ss, vv) for hh, ss, vv in zip(h, s, v)])


def _dilate(mask):
    d = np.zeros_like(mask)
    d[1:, :] |= mask[:-1, :]
    d[:-1, :] |= mask[1:, :]
    d[:, 1:] |= mask[:, :-1]
    d[:, :-1] |= mask[:, 1:]
    d[1:, 1:] |= mask[:-1, :-1]
    d[:-1, :-1] |= mask[1:, 1:]
    d[1:, :-1] |= mask[:-1, 1:]
    d[:-1, 1:] |= mask[1:, :-1]
    return d


def _load_template(tpl_path, size):
    t = Image.open(tpl_path).convert('RGBA')
    if t.size != (size, size):
        t = t.resize((size, size), Image.NEAREST)
    ta = np.asarray(t).astype(np.float32)
    mask = ta[:, :, 3] >= 128
    ys, xs = np.where(mask)
    return mask, (ys.min(), ys.max() + 1, xs.min(), xs.max() + 1)


def _load_lifted_art(src_path):
    """PoE2 美术：前景裁剪 + 缩放前色彩提升。返回 (RGBA 数组, fg 像素 RGB, bbox)。"""
    im = Image.open(src_path).convert('RGBA')
    a = np.asarray(im).astype(np.float32)
    fgm = a[:, :, 3] >= 96
    if not fgm.any():
        return None
    fy, fx = np.where(fgm)
    fgpx = _lift_colors(a[fgm][:, :3] / 255.0) * 255.0
    lifted = np.zeros_like(a)
    lifted[..., 3] = a[..., 3]
    lifted[fy, fx, :3] = fgpx
    y0, y1, x0, x1 = fy.min(), fy.max() + 1, fx.min(), fx.max() + 1
    lim = Image.fromarray(np.clip(lifted, 0, 255).astype(np.uint8), 'RGBA')
    return np.asarray(lim.crop((int(x0), int(y0), int(x1), int(y1)))).astype(np.float32), fgpx, (y0, y1, x0, x1)


def _place_art_aspect_fit(art, mask_bbox, size):
    """美术等比（contain，不放大）居中放入形状框 bbox，形制与细节完整保留。"""
    ah, aw = art.shape[:2]
    wb, hb = mask_bbox[3] - mask_bbox[2], mask_bbox[1] - mask_bbox[0]
    s = min(wb / aw, hb / ah)
    nw, nh = max(1, int(round(aw * s))), max(1, int(round(ah * s)))
    art_img = Image.fromarray(np.clip(art, 0, 255).astype(np.uint8), 'RGBA')
    art_img = art_img.resize((nw, nh), Image.LANCZOS)
    ca = np.asarray(art_img).astype(np.float32)
    out = np.zeros((size, size, 4), np.float32)
    ox = mask_bbox[2] + (wb - nw) // 2
    oy = mask_bbox[0] + (hb - nh) // 2
    out[oy:oy + nh, ox:ox + nw] = ca
    return out, (ox, oy, nw, nh)


def _nearest_art_index(a_vis, mask, size):
    """每个框内像素 -> 最近不透明美术像素索引（迭代膨胀传播）。"""
    near = np.zeros((size, size, 2), np.int32)
    filled = a_vis.copy()
    ys, xs = np.where(filled)
    near[ys, xs, 0] = ys
    near[ys, xs, 1] = xs
    for _ in range(size * 2):
        grow = _dilate(filled) & (~filled) & mask
        if not grow.any():
            break
        for y, x in zip(*np.where(grow)):
            for dy, dx in _NEIGHBORS8:
                ny, nx = y + dy, x + dx
                if 0 <= ny < size and 0 <= nx < size and filled[ny, nx]:
                    near[y, x] = near[ny, nx]
                    break
        filled |= grow
    return near


def _tile_reflect_sample(tile_ca, ax0, ay0, nw, nh, gy, gx):
    """仿制图章兜底采样：沿美术瓦片边缘【单次折叠反射】（不周期重复，避免
    花纹反复出现），采样始终落在瓦片内，纹理连续且只折一层。"""
    ly = gy - ay0
    ly = np.where(ly < 0, -ly, ly)
    ly = np.where(ly >= nh, 2 * nh - 1 - ly, ly)
    ly = np.clip(ly, 0, nh - 1)
    lx = gx - ax0
    lx = np.where(lx < 0, -lx, lx)
    lx = np.where(lx >= nw, 2 * nw - 1 - lx, lx)
    lx = np.clip(lx, 0, nw - 1)
    return tile_ca[ly, lx][:, :3]


def _tile_hole_fill(tile_ca):
    """瓦片镂空处：反射克隆补洞（环形孔内也会是纹理延续而非平涂）。"""
    nh, nw = tile_ca.shape[:2]
    t_vis = tile_ca[:, :, 3] >= 96
    tcol = tile_ca[:, :, :3].copy()
    if t_vis.all():
        return tcol
    near = np.zeros((nh, nw, 2), np.int32)
    filled = t_vis.copy()
    ys, xs = np.where(filled)
    near[ys, xs, 0] = ys
    near[ys, xs, 1] = xs
    for _ in range(max(nh, nw) * 2):
        grow = _dilate(filled) & ~filled
        if not grow.any():
            break
        for y, x in zip(*np.where(grow)):
            for dy, dx in _NEIGHBORS8:
                ny, nx = y + dy, x + dx
                if 0 <= ny < nh and 0 <= nx < nw and filled[ny, nx]:
                    near[y, x] = near[ny, nx]
                    by, bx = near[ny, nx]
                    py, px = 2 * by - y, 2 * bx - x
                    if 0 <= py < nh and 0 <= px < nw and t_vis[py, px]:
                        tcol[y, x] = tcol[py, px]
                    else:
                        tcol[y, x] = tcol[by, bx]
                    break
        filled |= grow
    return tcol


def _clone_stamp_fill(ca, mask, bbox, size):
    """仿制图章填充：空缺区 = 美术纹理沿剪影边缘反射克隆 + 反射瓦片兜底。"""
    ax0, ay0, nw, nh = bbox
    a_vis = (ca[:, :, 3] >= 96) & mask
    col = np.zeros((size, size, 3), np.float32)
    col[a_vis] = ca[a_vis][:, :3]
    gap = mask & ~a_vis
    if gap.any():
        gy, gx = np.where(gap)
        near = _nearest_art_index(a_vis, mask, size)
        by = near[gy, gx][:, 0]
        bx = near[gy, gx][:, 1]
        # 边界对齐反射：p' = 2b - p
        py = np.clip(2 * by - gy, 0, size - 1)
        px = np.clip(2 * bx - gx, 0, size - 1)
        hit = a_vis[py, px]
        if hit.any():
            col[gy[hit], gx[hit]] = ca[py[hit], px[hit]][:, :3]
        miss = ~hit
        if miss.any():
            tile = ca[ay0:ay0 + nh, ax0:ax0 + nw]
            tcol = _tile_hole_fill(tile)
            col[gy[miss], gx[miss]] = _tile_reflect_sample(tcol, ax0, ay0, nw, nh,
                                                           gy[miss], gx[miss])
    return col


def _material_lut(fgpx, K=12):
    """按亮度分位建立 亮度->材质色 映射表（该装备自身的材质色调）。"""
    lum = fgpx @ LUMA
    order = np.argsort(lum)
    q = np.linspace(0, len(fgpx) - 1, K + 1).astype(int)
    lut = np.zeros((K, 3), np.float32)
    for k in range(K):
        seg = fgpx[order[q[k]:q[k + 1]]]
        lut[k] = seg.mean(axis=0) if len(seg) else (lut[k - 1] if k else fgpx.mean(0))
    return lut


def _material_vertical_fill(fgpx, mask, mask_bbox, size):
    """材质模式：装备色系按垂直明暗（上亮 0.85 -> 下暗 0.35）给形状上色。"""
    lut = _material_lut(fgpx, K=12)
    fg_lum = fgpx @ LUMA
    y0, y1 = mask_bbox[0], mask_bbox[1]
    hb = max(1, y1 - y0)
    ys, xs = np.where(mask)
    g = 0.85 - (ys - y0).astype(np.float32) / hb * 0.5
    col = np.zeros((size, size, 3), np.float32)
    col[ys, xs] = _lut_color(g, lut, fg_lum.min(), fg_lum.max())
    return col


def _material_pants_fill(fgpx, tpl_rgba, mask, mask_bbox, size, rim=None, rim_ratio=0.23):
    """腿甲专用材质模式 v5：原版铁裤结构 + 装备亮半段色系 + 裤形细节。

    在原版明暗结构基础上补充裤形语义细节（腰带、膝/踝折痕、裤脚口），
    颜色取装备亮半段 8 级量化色，美术光照零参与；外圈按原版强度做
    1px 内描边（rim16->2x NEAREST）。"""
    lut = _material_lut(fgpx, K=12)
    lut_b = lut[4:]  # 亮 2/3 段：8 个量化色，细节层次更多
    t = np.asarray(tpl_rgba).astype(np.float32)
    t_lum = t[:, :, :3] @ LUMA
    t_lum = t_lum / max(1e-6, float(t_lum.max()))
    ys, xs = np.where(mask)
    pos = np.clip(t_lum[ys, xs] * (len(lut_b) - 1), 0, len(lut_b) - 1)
    i0 = np.clip(np.floor(pos).astype(int), 0, len(lut_b) - 2)
    f = (pos - i0)[:, None]
    col = lut_b[i0] * (1 - f) + lut_b[i0 + 1] * f
    y_frac = (ys - mask_bbox[0]).astype(np.float32) / max(1, mask_bbox[1] - mask_bbox[0])
    # 环境光：上 1.08 -> 下 0.78
    col = col * (1.08 - 0.30 * y_frac)[:, None]
    # 裤形细节（1-2px 语义线，均来自裤形结构本身）
    belt = (y_frac >= 0.05) & (y_frac <= 0.14)       # 腰带
    knee = (y_frac >= 0.50) & (y_frac <= 0.55)       # 膝折痕
    ankle = (y_frac >= 0.80) & (y_frac <= 0.85)      # 踝折痕
    cuff = y_frac >= 0.96                            # 裤脚口
    col[belt] *= 0.72
    col[knee] *= 0.80
    col[ankle] *= 0.76
    col[cuff] *= 0.70
    # 腿内侧高光（双腿弧面）
    m = mask.astype(np.uint8)
    up = np.zeros_like(m); up[:-1] = m[1:]
    down = np.zeros_like(m); down[1:] = m[:-1]
    gap = (~mask) & (up > 0) & (down > 0)
    gl = np.zeros_like(m, bool); gl[:, :-1] = gap[:, 1:]
    gr = np.zeros_like(m, bool); gr[:, 1:] = gap[:, :-1]
    inner = (gl | gr) & mask
    col[inner[ys, xs]] = np.clip(col[inner[ys, xs]] * 1.12, 0, 255)
    # 原版强度内描边
    if rim is not None:
        col[rim[ys, xs]] *= rim_ratio
    out = np.zeros((size, size, 3), np.float32)
    out[ys, xs] = np.clip(col, 0, 255)
    return out


def _lut_color(lum_vals, lut, lo, hi):
    """亮度->材质色：归一化到映射表范围后分段线性插值。"""
    span = max(1e-6, hi - lo)
    pos = np.clip((lum_vals - lo) / span * (len(lut) - 1), 0, len(lut) - 1)
    i0 = np.clip(np.floor(pos).astype(int), 0, len(lut) - 2)
    f = (pos - i0)[:, None]
    return lut[i0] * (1 - f) + lut[i0 + 1] * f


def compose(src_path, tpl_path, mode='art', size=32, rim_ratio=0.30):
    mask, bbox = _load_template(tpl_path, size)
    if not mask.any():
        return None
    art_data = _load_lifted_art(src_path)
    if art_data is None:
        return None
    art, fgpx, _ = art_data
    # 原版 16x16 模板 1px 内描边 -> 2x NEAREST（与原版描边比例一致）
    tpl_src = Image.open(tpl_path).convert('RGBA')
    r16 = _rim16(np.asarray(tpl_src).astype(np.float32)[:, :, 3] >= 128)
    rim = np.asarray(Image.fromarray((r16 * 255).astype(np.uint8), 'L').resize((size, size), Image.NEAREST)) > 128

    if mode == 'material':
        tpl_rgba = tpl_src.resize((size, size), Image.NEAREST)
        col = _material_pants_fill(fgpx, tpl_rgba, mask, bbox, size, rim, rim_ratio)
    else:
        ca, abbox = _place_art_aspect_fit(art, bbox, size)
        col = _clone_stamp_fill(ca, mask, abbox, size)
        col[rim & mask] *= rim_ratio

    out = np.zeros((size, size, 4), np.uint8)
    out[mask, :3] = np.clip(col[mask], 0, 255).astype(np.uint8)
    out[mask, 3] = 255
    return Image.fromarray(out, 'RGBA')


def to_snake(name: str) -> str:
    import re
    s = name.strip()
    if "_" in s:
        return s.lower()
    return re.sub(r"(?<!^)(?=[A-Z])", "_", s).lower()


def main():
    base = r'E:\McModding\Modding\CraftOfExile-NeoForge\poe2_assets'
    tpl_dir = os.path.join(base, 'item_pixel')
    ok, fail = 0, []
    for cat, (tplname, mode) in TEMPLATES.items():
        raw_dir = os.path.join(base, 'item', cat)
        out_dir = os.path.join(base, 'item_pixel', cat)
        os.makedirs(out_dir, exist_ok=True)
        tpl = os.path.join(tpl_dir, tplname)
        for f in sorted(os.listdir(raw_dir)):
            if not f.endswith('.webp'):
                continue
            name = os.path.splitext(f)[0]
            try:
                img = compose(os.path.join(raw_dir, f), tpl, mode=mode, rim_ratio=RIM_RATIO[cat])
            except Exception as e:
                fail.append(f'{cat}/{name}: {e}')
                continue
            if img is None:
                fail.append(f'{cat}/{name}: empty')
                continue
            img.save(os.path.join(out_dir, to_snake(name) + '.png'))
            ok += 1
        print(f'[{cat} {mode}] done')
    print(f'composed ok={ok} fail={len(fail)}')
    for f in fail[:30]:
        print('  FAIL', f)


if __name__ == '__main__':
    main()
